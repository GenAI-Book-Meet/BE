package genai.bookmeet.repository;

import genai.bookmeet.entity.Chat;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    @Query(value = "select c from Chat c join fetch c.messages WHERE c.userId = :userId")
    Chat findByUserIdWithMessages(@Param("userId") String userId);

    void deleteByUserId(String userId);

}
