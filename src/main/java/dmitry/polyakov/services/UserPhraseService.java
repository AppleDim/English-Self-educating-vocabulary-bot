package dmitry.polyakov.services;

import dmitry.polyakov.models.UserPhrase;
import dmitry.polyakov.repositories.UserPhraseRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
public class UserPhraseService {
    private UserPhraseRepository userPhraseRepository;

    @Autowired
    public void setUserPhraseRepository(UserPhraseRepository userPhraseRepository) {
        this.userPhraseRepository = userPhraseRepository;
    }

    public LinkedList<String> findUserPhrasesByIdOrderByPhraseId(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseId(userId);
    }
    public LinkedList<String> findUserPhrasesByIdOrderByCountPhraseViewsDesc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByCountPhraseViewsDesc(userId);
    }
    public LinkedList<String> findUserPhrasesByUserIdOrderByPhraseLengthAsc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseLengthAsc(userId);
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
        return findUserPhrasesByIdOrderByPhraseId(userId).size();
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

    public int getNumberPhraseViews(Long userId, String phraseText) {
        Long phraseId = userPhraseRepository.findPhraseIdByUserIdAndPhrase(userId, phraseText);
        UserPhrase userPhrase = userPhraseRepository.findUserPhraseByUserIdAndPhraseId(userId, phraseId);
        return userPhrase.getCountPhraseViews();
    }

    @Transactional
    public void incrementCountPhraseViews(Long userId, String phraseText) {
        Long phraseId = userPhraseRepository.findPhraseIdByUserIdAndPhrase(userId, phraseText);
        UserPhrase userPhrase = userPhraseRepository.findUserPhraseByUserIdAndPhraseId(userId, phraseId);
        userPhrase.setCountPhraseViews(userPhrase.getCountPhraseViews() + 1);
        userPhraseRepository.save(userPhrase);
    }
}
