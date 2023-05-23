package dmitry.polyakov.services;

import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.repositories.PhraseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PhraseService {
    private PhraseRepository phraseRepository;

    @Autowired
    public void setPhraseRepository(PhraseRepository phraseRepository) {
        this.phraseRepository = phraseRepository;
    }

    public void savePhrase(Phrase phrase) {
        phraseRepository.save(phrase);
    }

    public List<Phrase> getAllPhrases() {
        return phraseRepository.findAll();
    }
}
