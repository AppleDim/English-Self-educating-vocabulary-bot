package dmitry.polyakov.handlers;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.PhraseNotFoundException;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.User;
import dmitry.polyakov.models.UserPhrase;
import dmitry.polyakov.services.PhraseService;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import dmitry.polyakov.utils.LanguageLocalisation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.*;

import static dmitry.polyakov.constants.BotStateEnum.*;

@Component
@Slf4j
public class MessageHandler {
    private final ChatHandler chatHandler;
    private final UserService userService;
    private final PhraseService phraseService;
    private final UserPhraseService userPhraseService;


    @Autowired
    public MessageHandler(ChatHandler chatSender,
                          UserService userService,
                          PhraseService phraseService,
                          UserPhraseService userPhraseService) {
        this.chatHandler = chatSender;
        this.userService = userService;
        this.phraseService = phraseService;
        this.userPhraseService = userPhraseService;
    }

    public void handleStartCommandReceived(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        if (!userService.isUserFoundById(chatId)) {
            executeSendingMessage(update, chatId, bot, "/start");
        } else {
            User user = userService.findUserById(chatId);
            user.setUserBotState(BotStateEnum.DEFAULT_STATE);
            userService.saveUser(user);

            executeSendingMessage(update, chatId, bot, "/start");
        }

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed /start command.");
    }

    public void handleHelpCommandReceived(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/help");

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed help command.");
    }

    public void handleDictionaryCommandReceived(Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException, TelegramApiException {
        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed /dictionary command.");
    }

    public void handleSettingsCommandReceived(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/settings");

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed /settings command.");
    }

    public void handleBotLanguageChange(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/language");

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed /language command.");
    }

    public void handleWriteCommandReceived(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/write");

        log.info("@" + userService.findUserById(chatId).getNickname() + " executed /write command.");
    }

    public void handleReturnButtonPressed(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/return_to_main_menu");

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned to the main menu");
    }

    public void handleCancelButtonWhileReadingPhrasePressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        chatHandler.deleteMessage(bot, chatId, messageId);
        user.setUserBotState(BotStateEnum.READING_DICTIONARY);
        userService.saveUser(user);

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned their dictionary page");
    }

    public void handlePhraseReceived(Update update, Long chatId, String text, PersonalVocabularyBot bot) throws UserNotFoundException, PhraseNotFoundException {
        if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.WRITING_WORDS)) {
            if (text.matches("(?:[a-zA-Z']+(?:[-'][a-zA-Z]+)*,?\\s?)+[a-zA-Z']+")) {
                handleValidPhrase(update, chatId, text, bot);
            } else {
                handleInvalidPhrase(update, chatId, bot);
            }
        }
    }

    public void deletePhrase(PersonalVocabularyBot bot, Long chatId) throws UserNotFoundException, TelegramApiException {
        String phraseText = userService.getSavedPhrase(chatId);
        Long phraseId = userPhraseService.findPhraseIdByUserIdAndPhrase(chatId, phraseText);
        userPhraseService.deleteUserPhrase(chatId, phraseId);

        List<String> phrases = userPhraseService.findUserPhrasesById(chatId);
        phrases.remove(phraseText);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_DICTIONARY);
        userService.saveUser(user);

        handleDictionaryCommandReceived(chatId, bot);
    }

    private void executeSendingMessage(Update update, Long chatId, PersonalVocabularyBot bot, String text) {
        try {
            chatHandler.sendMessage(update, chatId, bot, text);
        } catch (TelegramApiException e) {
            log.warn("An error occurred while sending the message with chatId = " + chatId + "\n", e);
        } catch (UserNotFoundException e) {
            log.warn("Error finding user with id = " + chatId, e);
        }
    }

    private void handleValidPhrase(Update update, Long chatId, String text, PersonalVocabularyBot bot) throws UserNotFoundException {
        List<String> userTextPhrases = userPhraseService.findUserPhrasesById(chatId);
        boolean phraseFound = false;

        for (String userPhrase : userTextPhrases) {
            if (userPhrase.equalsIgnoreCase(text)) {
                executeSendingMessage(update, chatId, bot, "/phrase_already_stored");
                phraseFound = true;

                log.info("@" + userService.findUserById(chatId).getNickname() + " tried to save the phrase that already is saved.");
                break;
            }
        }

        if (!phraseFound) {
            Phrase phrase = getPhraseFromUser(chatId, text);
            saveUserPhrase(chatId, phrase);

            executeSendingMessage(update, chatId, bot, "/phrase");
        }
    }

    private void handleInvalidPhrase(Update update, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        executeSendingMessage(update, chatId, bot, "/illegal_characters");
        log.warn("@" + userService.findUserById(chatId).getNickname() + " tried to find a phrase with illegal characters");
    }

    private Phrase getPhraseFromUser(Long chatId, String text) throws UserNotFoundException {
        Phrase phrase = new Phrase();
        User currentUser = userService.findUserById(chatId);

        Set<User> users = new HashSet<>();
        users.add(currentUser);

        phrase.setUsers(users);
        phrase.setSearchedDate(new Timestamp(System.currentTimeMillis()));
        phrase.setPhrase(text);

        if (phraseService.getAllPhrases().isEmpty()) {
            phrase.setPhraseId(1L);
        } else {
            long maxPhraseId = phraseService.getAllPhrases().size();
            phrase.setPhraseId(maxPhraseId + 1);
        }

        log.info("@" + userService.findUserById(currentUser.getUserId()).getNickname() + " saved their phrase: " + phrase.getPhrase());
        phraseService.savePhrase(phrase);

        return phrase;
    }

    private void saveUserPhrase(Long chatId, Phrase phrase) throws UserNotFoundException {
        User currentUser = userService.findUserById(chatId);

        UserPhrase userPhrase = new UserPhrase();
        userPhrase.setUser(currentUser);
        userPhrase.setPhrase(phrase);

        boolean userPhraseExists = userPhraseService.findUserPhraseExists(currentUser.getUserId(), phrase.getPhraseId());

        if (!userPhraseExists) {
            if (userPhraseService.findAllUsersPhrases().isEmpty()) {
                userPhrase.setUserPhraseId(1L);
            } else {
                long maxUserPhraseId = userPhraseService.findAllUsersPhrases().size();
                userPhrase.setUserPhraseId(maxUserPhraseId + 1);
            }

            userPhraseService.saveUserPhrase(userPhrase);

            log.info("@" + userService.findUserById(currentUser.getUserId()).getNickname() + " saved their phrase");
        }
    }

    public void changeBotLanguage (Update update, Long chatId, String text, PersonalVocabularyBot bot) throws UserNotFoundException {
        User user = userService.findUserById(chatId);

        if (text.equals(LanguageLocalisation.englishLang)) {
            LanguageLocalisation.messages = ResourceBundle.getBundle("messages", new Locale("en"));
            executeSendingMessage(update, chatId, bot, "/eng_lang");
        } else if (text.equals(LanguageLocalisation.russianLang)) {
            LanguageLocalisation.messages = ResourceBundle.getBundle("messages", new Locale("ru"));
            executeSendingMessage(update, chatId, bot, "/rus_lang");
        }

        user.setUserBotState(DEFAULT_STATE);
        userService.saveUser(user);
    }
}