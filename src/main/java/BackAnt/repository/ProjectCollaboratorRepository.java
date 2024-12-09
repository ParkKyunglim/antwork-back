package BackAnt.repository;

import BackAnt.entity.User;
import BackAnt.entity.project.Project;
import BackAnt.entity.project.ProjectCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
    날짜 : 2024/12/2
    이름 : 강은경
    내용 : ProjectCollaboratorRepository 생성
*/
@Repository
public interface ProjectCollaboratorRepository extends JpaRepository<ProjectCollaborator, Long> {


    // 특정 사용자와 관련된 ProjectCollaborator 조회
    @Query("SELECT pc.project FROM ProjectCollaborator pc WHERE pc.user.uid = :uid")
    List<Project> findProjectsByUserUid(@Param("uid") String uid);

    // 프로젝트 ID를 기준으로 협업자 목록 조회
    @Query("SELECT pc.user FROM ProjectCollaborator pc WHERE pc.project.id = :projectId")
    List<User> findUsersByProjectId(Long projectId);

    // 프로젝트 id와 사용자 id를 기준으로 협업자 조회
    Optional<ProjectCollaborator> findByProjectIdAndUserId(Long projectId, Long userId);

}