package dmitry.polyakov.services;

import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.UserPhrase;
import dmitry.polyakov.repositories.PhraseRepository;
import dmitry.polyakov.repositories.UserPhraseRepository;
import dmitry.polyakov.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserPhraseService {
    private UserPhraseRepository userPhraseRepository;
    private UserRepository userRepository;
    private PhraseRepository phraseRepository;

    @Autowired
    public void setUserPhraseRepository(UserPhraseRepository userPhraseRepository,
                                        UserRepository userRepository,
                                        PhraseRepository phraseRepository) {
        this.userPhraseRepository = userPhraseRepository;
        this.userRepository = userRepository;
        this.phraseRepository = phraseRepository;
    }

    public List<String> findUserPhrasesById(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserId(userId);
    }

    public void saveUserPhrase(UserPhrase userPhrase) {
        userPhraseRepository.save(userPhrase);
    }

    public List<UserPhrase> findAllUsersPhrases() {
        return userPhraseRepository.findAll();
    }

    public boolean findUserPhraseExists(Long userId, Long phraseId) {
        return userPhraseRepository.existsByUserUserIdAndPhrasePhraseId(userId, phraseId);
    }

    public String findSingleUserPhrase(Long userId, Long phraseId) {
        return userPhraseRepository.findSpecificUserPhrasesByUserIdAndPhraseIds(userId, phraseId);
    }

    public int countUserPhrases(Long userId) {
        return findUserPhrasesById(userId).size();
    }

    @Transactional
    public void deleteUserPhrase(Long userId, Long phraseId) {
        userPhraseRepository.deleteByUserIdAndPhraseId(userId, phraseId);
    }

    public Long findPhraseIdByUserIdAndPhrase(Long userId, String phraseText) {
        return userPhraseRepository.findPhraseIdByUserIdAndPhrase(userId, phraseText);
    }

    public UserPhrase findUserPhraseByIds(Long userId, Long phraseId) {
        return userPhraseRepository.findUserPhraseByUserIdAndPhraseId(userId, phraseId);
    }
}
