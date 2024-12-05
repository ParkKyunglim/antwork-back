package BackAnt.service;

import BackAnt.dto.RequestDTO.AdminRequestDTO;
import BackAnt.dto.RequestDTO.UserRegisterRequestDTO;
import BackAnt.dto.UserDTO;
import BackAnt.entity.Company;
import BackAnt.entity.Department;
import BackAnt.entity.Invite;
import BackAnt.entity.User;
import BackAnt.entity.enums.Role;
import BackAnt.entity.enums.Status;
import BackAnt.repository.CompanyRepository;
import BackAnt.repository.InviteRepository;
import BackAnt.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/*
    날짜 : 2024/11/29
    이름 : 최준혁
    내용 : 유저 서비스 생성
*/

@RequiredArgsConstructor
@Log4j2
@Service
public class UserService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final InviteRepository inviteRepository;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final InviteService inviteService;


    // 매퍼 사용 엔티티 - DTO 상호전환
    public UserDTO toDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public User toEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    // 아이디 중복확인
    public boolean isIdAvailable(String uid) {
        return !userRepository.existsByUid(uid); // 아이디가 존재하지 않으면 true
    }

    // 회원 회원가입
    public User registerUser(UserRegisterRequestDTO userDTO) throws Exception {
        Department department = null;
        // 초대 상태 업데이트
        if (userDTO.getTokenid() != null) {
            Optional<Invite> optionalInvite = inviteRepository.findById(userDTO.getTokenid());

            if (optionalInvite.isPresent()) {
                Invite invite = optionalInvite.get();

                // 초대 상태를 INVITE_COMPLETE로 변경
                invite.setStatus(Status.INVITE_COMPLETE);
                department = invite.getDepartment();
                inviteRepository.save(invite);
            } else {
                throw new IllegalArgumentException("유효하지 않은 초대 토큰입니다.");
            }
        }

        // User 엔티티 생성 및 저장
        User user = User.builder()
                .name(userDTO.getName())
                .uid(userDTO.getUid())
                .password(passwordEncoder.encode(userDTO.getPassword())) // 비밀번호 암호화
                .nick(userDTO.getNick())
                .phoneNumber(userDTO.getPhoneNumber())
                .profileImageUrl(userDTO.getProfileImageUrl())
                .email(userDTO.getEmail())
                .role(Role.valueOf(userDTO.getRole()))
                .position(userDTO.getPosition())
                .company(department.getCompany())
                .department(department)
                .status(Status.ACTIVE)
                .build();

        return userRepository.save(user);
    }

    // 관리자 회원가입
    public User createUser(AdminRequestDTO adminDTO) {
        Company company = companyRepository.findById(adminDTO.getCompanyId())
                .orElseThrow(() -> new IllegalArgumentException("회사 ID가 잘못되었습니다."));

        User user = modelMapper.map(adminDTO, User.class);
        user.setCompany(company); // 회사 매핑
        user.setPassword(passwordEncoder.encode(adminDTO.getPassword())); // 비밀번호 암호화
        return userRepository.save(user);
    }




}
