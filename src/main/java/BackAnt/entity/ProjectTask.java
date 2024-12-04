package BackAnt.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
    날짜 : 2024/12/2
    이름 : 강은경
    내용 : ProjectTask 엔티티 생성
*/

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "ProjectTask")
public class ProjectTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // 작업 제목

    private String content; // 작업 설명

    private int priority; // 0: 낮음, 1: 보통, 2: 높음

    private int status; // 0: 미완료, 1: 완료

    private String size; // 작업 크기 (예: S, M, L 등)

    private LocalDate dueDate; // 작업 마감일

    private int position; // 보드 내 위치 (작업 순서)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private ProjectState state; // 작업이 속한 상태

    // 나중에 추가해야함
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "assigned_user_id")
//    private User assignedUser;  // 작업 담당자

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 날짜

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 날짜



}
