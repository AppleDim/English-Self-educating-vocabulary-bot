package dmitry.polyakov.handlers;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.PhraseService;
import dmitry.polyakov.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.Set;

@Component
@Slf4j
public class MessageHandler {
    private final ChatSender chatSender;
    private final UserService userService;
    private final PhraseService phraseService;

    @Autowired
    public MessageHandler(ChatSender chatSender, UserService userService, PhraseService phraseService) {
        this.chatSender = chatSender;
        this.userService = userService;
        this.phraseService = phraseService;
    }

    public void handleStartCommandReceived(Update update, PersonalVocabularyBot bot) {
        try {
            chatSender.sendMessage(update, bot, "/start");
        } catch (TelegramApiException e) {
            log.warn("An error occurred while initiating /start command\n", e);
        }
    }

    public void handleHelpCommandReceived(Update update, PersonalVocabularyBot bot) {
        try {
            chatSender.sendMessage(update, bot, "/help");
        } catch (TelegramApiException e) {
            log.warn("An error occurred while initiating /help command\n", e);
        }
    }

    public void handleDictionaryCommandReceived(Update update, PersonalVocabularyBot bot) {
        Long chatId = update.getMessage().getChatId();
        try {
            User user = userService.findUserById(chatId);

            if (user.getId().equals(chatId)) {
                chatSender.sendMessage(update, bot, "/show_phrases");
            }
        } catch (UserNotFoundException e) {
            log.warn("An error occurred while searching for user by id = " + chatId + "\n", e);
        } catch (TelegramApiException e) {
            log.warn("An error occurred while showing dictionary\n", e);
        }
    }

    public void handleSettingsCommandReceived(Update update, PersonalVocabularyBot bot) {
        try {
            chatSender.sendMessage(update, bot, "/settings");
        } catch (TelegramApiException e) {
            log.warn("An error occurred while initiating /settings command\n", e);
        }
    }

    public void handleLanguageCommandReceived(Update update, PersonalVocabularyBot bot) {
        try {
            chatSender.sendMessage(update, bot, "/language");
        } catch (TelegramApiException e) {
            log.warn("An error occurred while initiating /language command\n", e);
        }
    }

    public void handleWriteCommandReceived(Update update, PersonalVocabularyBot bot) throws UserNotFoundException {
        Long chatId = update.getMessage().getChatId();
        User user = userService.findUserById(chatId);

        user.setUserBotState(BotStateEnum.WRITING_WORDS);

        try {
            chatSender.sendMessage(update, bot, "/write");
        } catch (TelegramApiException e) {
            log.warn("An error occurred while initiating /write command\n", e);
        }

        userService.saveUser(user);
    }

    public void handlePhraseReceived(Update update, PersonalVocabularyBot bot) throws UserNotFoundException {
        if (userService.isUserMatchedWithBotState(update.getMessage().getChatId(), BotStateEnum.WRITING_WORDS)) {
            Phrase phrase = new Phrase();
            Set<User> users = userService.getAllUsers();

            users.add(userService.findUserById(update.getMessage().getChatId()));

            if (phraseService.getAllPhrases().isEmpty()) {
                phrase.setId(1L);
            } else {
                long maxPhraseId = phraseService.getAllPhrases().size();
                phrase.setId(maxPhraseId + 1);
            }

            phrase.setUsers(users);
            phrase.setSearchedDate(new Timestamp(System.currentTimeMillis()));
            phrase.setPhrase(update.getMessage().getText());

            phraseService.savePhrase(phrase);

            try {
                chatSender.sendMessage(update, bot, "/phrase");
            } catch (TelegramApiException e) {
                log.warn("An error occurred while storing the given phrase from id = " + update.getMessage().getChatId() + "\n", e);
            }
        }
    }
}