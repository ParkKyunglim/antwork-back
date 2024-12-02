package BackAnt.repository;

import BackAnt.entity.Project;
import BackAnt.entity.ProjectState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/*
    날짜 : 2024/12/2
    이름 : 강은경
    내용 : ProjectStateRepository 생성
*/
@Repository
public interface ProjectStateRepository extends JpaRepository<ProjectState, Long> {


}