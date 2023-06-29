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

    public LinkedList<String> findUserPhrasesByIdOrderByPhraseIdAsc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseIdAsc(userId);
    }

    public LinkedList<String> findUserPhrasesByIdOrderByPhraseIdDesc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseIdDesc(userId);
    }

    public LinkedList<String> findUserPhrasesByIdOrderByCountPhraseViewsAsc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByCountPhraseViewsAsc(userId);
    }
    public LinkedList<String> findUserPhrasesByIdOrderByCountPhraseViewsDesc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByCountPhraseViewsDesc(userId);
    }
    public LinkedList<String> findUserPhrasesByUserIdOrderByPhraseLengthAsc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseLengthAsc(userId);
    }

    public LinkedList<String> findUserPhrasesByUserIdOrderByPhraseLengthDesc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseLengthDesc(userId);
    }

    public LinkedList<String>findUserPhrasesByUserIdOrderByAlphabeticalAsc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseAsc(userId);
    }

    public LinkedList<String>findUserPhrasesByUserIdOrderByAlphabeticalDesc(Long userId) {
        return userPhraseRepository.findUserPhrasesByUserIdOrderByPhraseDesc(userId);
    }


    public void saveUserPhrase(UserPhrase userPhrase) {
        userPhraseRepository.save(userPhrase);
    }

    public boolean findUserPhraseExists(Long userId, Long phraseId) {
        return userPhraseRepository.existsByUserUserIdAndPhrasePhraseId(userId, phraseId);
    }

    public int countUserPhrases(Long userId) {
        return findUserPhrasesByIdOrderByPhraseIdAsc(userId).size();
    }

    @Transactional
    public void deleteUserPhrase(Long userId, Long phraseId) {
        userPhraseRepository.deleteByUserIdAndPhraseId(userId, phraseId);
    }

    public Long findPhraseIdByUserIdAndPhrase(Long userId, String phraseText) {
        return userPhraseRepository.findPhraseIdByUserIdAndPhrase(userId, phraseText);
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
    public Long findMaxId() {
        return userPhraseRepository.findMaxId();
    }
}
