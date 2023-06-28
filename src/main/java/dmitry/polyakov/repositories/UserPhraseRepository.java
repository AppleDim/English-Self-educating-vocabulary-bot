package dmitry.polyakov.repositories;

import dmitry.polyakov.models.UserPhrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.LinkedList;

@Repository
public interface UserPhraseRepository extends JpaRepository<UserPhrase, Long> {
    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY p.phraseId ASC")
    LinkedList<String> findUserPhrasesByUserIdOrderByPhraseIdAsc(@Param("userId") Long userId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY up.countPhraseViews ASC")
    LinkedList<String> findUserPhrasesByUserIdOrderByCountPhraseViewsAsc(@Param("userId") Long userId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY LENGTH(up.phrase.phrase) ASC")
    LinkedList<String> findUserPhrasesByUserIdOrderByPhraseLengthAsc(@Param("userId") Long userId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY p.phraseId DESC")
    LinkedList<String> findUserPhrasesByUserIdOrderByPhraseIdDesc(@Param("userId") Long userId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY LENGTH(up.phrase.phrase) DESC")
    LinkedList<String> findUserPhrasesByUserIdOrderByPhraseLengthDesc(@Param("userId") Long userId);

    @Query("SELECT up.phrase.phrase " +
            "FROM UserPhrase up " +
            "JOIN up.user u " +
            "JOIN up.phrase p " +
            "WHERE u.userId = :userId " +
            "ORDER BY up.countPhraseViews DESC")
    LinkedList<String> findUserPhrasesByUserIdOrderByCountPhraseViewsDesc(@Param("userId") Long userId);

    boolean existsByUserUserIdAndPhrasePhraseId(Long userId, Long phraseId);

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

    @Query("SELECT MAX(up.userPhraseId) FROM UserPhrase up")
    Long findMaxId();
}
