package dmitry.polyakov.utils;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class LanguageLocalisation {
    private UserService userService;
    public final String russianLang = EmojiParser.parseToUnicode("Русский:ru:");
    public final String englishLang = EmojiParser.parseToUnicode("English:gb:");

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    private final Map<Long, ResourceBundle> userMessages = new HashMap<>();

    public ResourceBundle getMessages(Long userId) {
        return userMessages.getOrDefault(userId, ResourceBundle.getBundle("messages", getLocaleForUser(userId)));
    }

    public void setMessages(Long userId, ResourceBundle messages) {
        userMessages.put(userId, messages);
    }

    public  Locale getLocaleForUser(Long userId) {
        String language = userService.getLanguage(userId);
        return new Locale(Objects.requireNonNullElse(language, "en"));
    }
}