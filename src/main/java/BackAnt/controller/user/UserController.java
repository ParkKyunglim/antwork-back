package BackAnt.controller.user;

import BackAnt.dto.RequestDTO.AdminRequestDTO;
import BackAnt.dto.RequestDTO.InviteRequestDTO;
import BackAnt.dto.RequestDTO.LoginRequestDTO;
import BackAnt.dto.RequestDTO.UserRegisterRequestDTO;
import BackAnt.dto.ResponseDTO.ApiResponseDTO;
import BackAnt.dto.UserDTO;
import BackAnt.dto.common.ResponseDTO;
import BackAnt.entity.Invite;
import BackAnt.entity.User;
import BackAnt.security.MyUserDetails;
import BackAnt.service.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.util.Map;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final InviteService inviteService;
    private final AuthService authService;
    private final EmailService emailService;
    private final ImageService imageService;

    // 초기 관리자 멤버 추가
    @PostMapping("/create")
    public ResponseEntity<?> addUser(@RequestBody AdminRequestDTO adminDTO) {
        try {
            log.info("유저 " + adminDTO.toString());
            User savedUser = userService.createUser(adminDTO);
            // 성공 응답
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "사용자 저장 성공", savedUser.getId()));
        } catch (Exception e) {
            // 실패 응답
            return ResponseEntity.status(500)
                    .body(ApiResponseDTO.fail("사용자 저장 실패: " + e.getMessage()));
        }
    }

    // 초대 생성 요청
    @PostMapping("/invite")
    public ResponseEntity<?> createInvite(@RequestBody InviteRequestDTO inviteRequestDTO) {
        try {
            log.info("초대 생성 요청 수신");
            log.info("초대DTO"+ inviteRequestDTO.toString());
            String inviteToken = inviteService.createInvite(inviteRequestDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "이메일 전송 성공", inviteToken));
        } catch (Exception e) {
            log.error("초대 생성 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDTO.failure("초대 생성 중 오류 발생: " + e.getMessage()));
        }
    }

    // 아이디 중복 확인
    @PostMapping("/checkduplicateId")
    public ResponseEntity<?> checkDuplicateId(@RequestBody Map<String, String> request) {
        String uid = request.get("uid");
        log.info("유아이디" + uid);

        boolean isAvailable = userService.isIdAvailable(uid);
        return ResponseEntity.ok(Map.of("isAvailable", isAvailable));
    }


    // 초대 인증
    @GetMapping("/verify")
    public ResponseEntity<?> verifyInviteToken(@RequestParam String token) {
        try {
            log.info("검증하니?");
            Invite invite = inviteService.verifyToken(token);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "초대 토큰 인증 성공", invite));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }


    // 회원가입 처리
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDTO<Long>> registerUser(
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(value = "formData") UserRegisterRequestDTO userDTO) {
        try {
            log.info("DTO 상태 확인: {}", userDTO.toString());

            // 이미지 업로드 처리
            if (profileImage != null && !profileImage.isEmpty()) {
                String imageUrl = imageService.uploadImage(profileImage);
                userDTO.setProfileImageUrl(imageUrl);
            }

            User savedUser = userService.registerUser(userDTO);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "회원가입이 성공적으로 완료되었습니다.", savedUser.getId()));
        } catch (IllegalArgumentException e) {
            log.error("회원가입 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponseDTO<>(false, e.getMessage(), null));
        } catch (Exception e) {
            log.error("회원가입 실패 - 서버 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDTO<>(false, "서버 오류가 발생했습니다.", null));
        }
    }



    // 이메일 인증처리
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            String email = emailService.verifyAndCheckEmail(token);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "이메일 인증 성공", email));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDTO<>(false, "이메일 인증 실패: " + e.getMessage(), null));
        }
    }



    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO loginRequest, HttpServletResponse response) {
        try {
            log.info("로그인들어오나");
            System.out.println("로그인??");
            // 로그인 처리
            String accessToken = authService.login(loginRequest.getUid(), loginRequest.getPassword());

            log.info("엑세스토큰 " + accessToken);
            // 리프레시 토큰 생성
            String refreshToken = authService.refreshToken(accessToken);
            log.info("리프레시토큰 " + refreshToken);
            // 리프레시 토큰을 쿠키로 저장
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(false);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일 유효
            response.addCookie(refreshCookie);

            log.info("Access Token: {}", accessToken);
            log.info("Refresh Token: {}", refreshToken);


            // 응답 반환
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "로그인 성공", accessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(false, "로그인 실패: " + e.getMessage(), null));
        }
    }

    // 리프레시 토큰 검증
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue("refreshToken") String refreshToken) {
        try {
            log.info("리프레시들어오나");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Principal에서 User 객체 가져오기
            if (authentication != null && authentication.isAuthenticated()) {
                User user = (User) authentication.getPrincipal();
                System.out.println("현재 유저: " + user);
            } else {
                System.out.println("인증되지 않은 사용자입니다.");
            }
            // 토큰 갱신
            String newAccessToken = authService.refreshToken(refreshToken);
            log.info("리프레시 갱신 성공? " + newAccessToken);
            return ResponseEntity.ok(new ApiResponseDTO<>(true, "토큰 갱신 성공", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(new ApiResponseDTO<>(false, "토큰 갱신 실패: " + e.getMessage(), null));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        try {
            log.info("로그아웃 요청");

            // 리프레시 토큰 쿠키 삭제
            Cookie deleteCookie = new Cookie("refreshToken", null);
            deleteCookie.setHttpOnly(true);
            deleteCookie.setSecure(true);
            deleteCookie.setPath("/");
            deleteCookie.setMaxAge(0); // 쿠키 삭제
            response.addCookie(deleteCookie);

            return ResponseEntity.ok(new ApiResponseDTO<>(true, "로그아웃 성공", null));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDTO<>(false, "로그아웃 실패: " + e.getMessage(), null));
        }
    }


    @GetMapping("/me")
    public ResponseEntity<?> getAuthenticatedUser(@AuthenticationPrincipal MyUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("사용자가 인증되지 않았습니다.");
        }

        User user = userDetails.getUser();
        return ResponseEntity.ok(user);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<UserDTO>> getMembers(
            @RequestParam Long company,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserDTO> members = userService.getMembersByCompany(company, page - 1, size); // Spring Data는 0-based 페이지
        return ResponseEntity.ok(members);
    }

}
