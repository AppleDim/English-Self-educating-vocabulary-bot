package dmitry.polyakov.services;

import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User findUserById(Long chatId) throws UserNotFoundException {
        return userRepository.findById(chatId).orElseThrow(() -> new UserNotFoundException(chatId));
    }

    public boolean isUserFoundById(Long chatId) {
        return userRepository.findById(chatId).isPresent();
    }

    public boolean isUserMatchedWithBotState(Long chatId, BotStateEnum userBotState) {
        return userRepository.findById(chatId).isPresent() && userRepository.findById(chatId).get().getUserBotState().equals(userBotState);
    }

    public Set<User> getAllUsers() {
        return new HashSet<>(userRepository.findAll());
    }

    public void saveUser(User user) {
        userRepository.save(user);
    }
}
