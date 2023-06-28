package dmitry.polyakov.repositories;

import dmitry.polyakov.models.Phrase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhraseRepository extends JpaRepository<Phrase, Long> {
    @Query("SELECT MAX(p.phraseId) FROM Phrase p")
    Long getMaxId();
}
