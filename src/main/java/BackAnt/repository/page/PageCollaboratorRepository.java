package BackAnt.repository.page;

import BackAnt.entity.page.PageCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PageCollaboratorRepository extends JpaRepository<PageCollaborator, Integer> {
    List<PageCollaborator> findByPageId(String projectId);
    void deleteByPageIdAndUser_Uid(String projectId, String userId);
    @Query("SELECT pc.pageId FROM PageCollaborator pc WHERE pc.user.uid = :userId")
    List<String> findPageIdsByUserId(@Param("userId") String userId);
    List<PageCollaborator> findByUser_Uid(String userId);

}
