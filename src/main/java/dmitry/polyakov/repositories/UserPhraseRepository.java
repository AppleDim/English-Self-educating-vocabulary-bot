package dmitry.polyakov.repositories;

import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.UserPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPhraseRepository extends JpaRepository<UserPhrase, Long> {
    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId")
    List<String> findUserPhrasesByUserId(@Param("userId") Long userId);

    boolean existsByUserUserIdAndPhrasePhraseId(Long userId, Long phraseId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "AND p.phraseId = :phraseId")
    String findSpecificUserPhrasesByUserIdAndPhraseIds(@Param("userId") Long userId, @Param("phraseId") Long phraseId);

    @Modifying
    @Query("DELETE FROM UserPhrase up " +
            "WHERE up.user.userId = :userId " +
            "AND up.phrase.phraseId = :phraseId")
    void deleteByUserIdAndPhraseId(@Param("userId") Long userId, @Param("phraseId") Long phraseId);

    @Query("SELECT p.phraseId " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "AND p.phrase = :phrase")
    Long findPhraseIdByUserIdAndPhrase(@Param("userId") Long userId, @Param("phrase") String phrase);

    @Query("SELECT up " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "AND p.phraseId = :phraseId")
    UserPhrase findUserPhraseByUserIdAndPhraseId(@Param("userId") Long userId, @Param("phraseId") Long phraseId);
}
