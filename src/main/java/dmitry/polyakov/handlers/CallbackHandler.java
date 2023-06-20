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

import java.util.LinkedList;

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

    public void handleFastBackButtonPressed(PersonalVocabularyBot bot, long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        int currentPage = user.getCurrentPageNumber();

        if (currentPage >= 10) {
            user.setCurrentPageNumber(currentPage - 10);
        } else {
            user.setCurrentPageNumber(0);
        }

        userService.saveUser(user);

        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled 10 pages back to the page: " + user.getCurrentPageNumber());
    }

    public void handleBackButtonPressed(PersonalVocabularyBot bot, long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        int currentPage = user.getCurrentPageNumber();

        if (currentPage > 0) {
            user.setCurrentPageNumber(currentPage - 1);
        } else {
            user.setCurrentPageNumber(0);
        }

        userService.saveUser(user);

        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled to the previous page: " + user.getCurrentPageNumber());
    }

    public void handleSettingsButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);

        SendMessage sendMessage = chatHandler.createSettingsPageMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);
    }

    public void handleCancelButtonPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException, TelegramApiException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        userService.saveUser(user);

        chatHandler.sendMessage(update, chatId, bot, "/return_to_main_menu");

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned to the main");
    }

    public void handleSearchingButtonPressed(Update update, PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);
        chatHandler.getPhrasesFromPage(bot, chatId);
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

    public void handleFastForwardButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / 10);
        int currentPage = user.getCurrentPageNumber();

        if (currentPage < maxPage - 10) {
            user.setCurrentPageNumber(currentPage + 10);
        } else {
            user.setCurrentPageNumber(maxPage - 1);
        }

        userService.saveUser(user);

        chatHandler.getPhrasesFromPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled 10 pages forward to the page: " + user.getCurrentPageNumber());
    }


    public void handlePhraseNumberPressed(PersonalVocabularyBot bot, Long chatId, int messageId, String callBackData) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        LinkedList<String> phrasesText = userPhraseService.findUserPhrasesByIdOrderByPhraseId(chatId);

        String phrase = "";

        for (String str : phrasesText) {
            if (isChosenPhrase(callBackData, user, str)) {
                phrase = str;
                user.setUserBotState(BotStateEnum.READING_PHRASE);
                userService.saveUser(user);

                InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createDeletePageInlineKeyboardMarkup();
                SendMessage sendMessage = chatHandler.createPhraseWatchingPage(chatId, callBackData);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);
                chatHandler.executeMessage(bot, sendMessage);
                break;
            }
        }

        log.info("@" + user.getNickname() + " opened the page with the phrase: " + phrase);
    }


    public void handleDeletePhraseButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) {
        chatHandler.deleteMessage(bot, chatId, messageId);
        SendMessage sendMessage = chatHandler.createDeleteConfirmationMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);
    }

    public void handleNOButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.getPhrasesFromPage(bot, chatId);
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_DICTIONARY);
        userService.saveUser(user);

        log.info("@" + userService.findUserById(chatId).getNickname() + " canceled delete of phrase");
    }

    private boolean isChosenPhrase(String callBackData, User user, String phrase) {
        return callBackData.split(": ")[0].equals(String.valueOf(user.getUserId()))
                && callBackData.split(": ")[1].equals(phrase);
    }
}
