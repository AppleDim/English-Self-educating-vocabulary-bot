package dmitry.polyakov.repositories;

import dmitry.polyakov.models.UserPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface UserPhraseRepository extends JpaRepository<UserPhrase, Long> {
    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId")
    Set<String> findUserPhrasesByUserId(@Param("userId") Long userId);

    boolean existsByUserUserIdAndPhrasePhraseId(Long userId, Long phraseId);
}
