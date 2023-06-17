package dmitry.polyakov.handlers;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
@Slf4j
public class CallbackHandler {
    private final UserService userService;
    private final UserPhraseService userPhraseService;
    private final ChatHandler chatHandler;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    public CallbackHandler(UserService userService,
                           UserPhraseService userPhraseService,
                           ChatHandler chatSender,
                           InlineKeyboardFactory inlineKeyboardFactory) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
        this.chatHandler = chatSender;
        this.inlineKeyboardFactory = inlineKeyboardFactory;
    }

    public void handleBackButtonPressed(PersonalVocabularyBot bot, long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        int currentPage = user.getCurrentPageNumber();

        if (user.getCurrentPageNumber() > 0) {
            user.setCurrentPageNumber(--currentPage);
        } else {
            user.setCurrentPageNumber(0);
        }

        userService.saveUser(user);

        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled to the next page: " + currentPage);
    }

    public void handleSettingsButtonPressed(Update update, PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);
        chatHandler.getPhrasesFromPage(bot, chatId);
    }

    public void handleCancelButtonPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException, TelegramApiException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        chatHandler.deleteMessage(bot, chatId, messageId);
        userService.saveUser(user);
        chatHandler.sendMessage(update, chatId, bot, "/return_to_main_menu");

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned to the main");
    }

    public void handleForwardButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / 10);
        int currentPage = user.getCurrentPageNumber();
        if (currentPage < maxPage - 1) {
            user.setCurrentPageNumber(currentPage + 1);
        } else {
            user.setCurrentPageNumber(maxPage - 1);
        }

        userService.saveUser(user);

        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled to the next page: " + currentPage);
    }

    public void handleSearchingButtonPressed(Update update, PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);
        chatHandler.getPhrasesFromPage(bot, chatId);
    }


    public void handlePhraseNumberPressed(PersonalVocabularyBot bot, Long chatId, int messageId, String callBackData) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_PHRASE);
        userService.saveUser(user);
        List<String> phrasesText = userPhraseService.findUserPhrasesById(chatId);
        for (String str : phrasesText) {
            if (isChosenPhrase(callBackData, user, str)) {
                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createDeletePageInlineKeyboardMarkup();
                SendMessage sendMessage = chatHandler.createPhraseWatchingPage(chatId, callBackData);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                chatHandler.executeMessage(bot, sendMessage);
                break;
            }
        }
    }

    public void handleDeletePhraseButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) {
        chatHandler.deleteMessage(bot, chatId, messageId);
        SendMessage sendMessage = chatHandler.createDeleteConfirmationMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);
    }

    public void handleNOButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.getPhrasesFromPage(bot, chatId);
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_DICTIONARY);
        chatHandler.deleteMessage(bot, chatId, messageId);
        userService.saveUser(user);

        log.info("@" + userService.findUserById(chatId).getNickname() + " canceled delete of phrase");
    }

    private boolean isChosenPhrase(String callBackData, User user, String phrase) {
        return callBackData.split(": ")[0].equals(String.valueOf(user.getUserId()))
                && callBackData.split(": ")[1].equals(phrase);
    }
}
