package dmitry.polyakov.handlers;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.constants.SettingsOrderEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.LinkedList;

@Component
@Slf4j
public class CallbackHandler {
    private final UserService userService;
    private final UserPhraseService userPhraseService;
    private final ChatHandler chatHandler;

    public CallbackHandler(UserService userService,
                           UserPhraseService userPhraseService,
                           ChatHandler chatSender) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
        this.chatHandler = chatSender;
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

        chatHandler.sendPhrasesPage(bot, chatId);

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

        chatHandler.sendPhrasesPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled to the previous page: " + user.getCurrentPageNumber());
    }

    public void handleSettingsButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);

        SendMessage sendMessage = chatHandler.createSettingsPageMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);

        log.info("@" + userService.findUserById(chatId).getNickname() + " went to the settings page");
    }

    public void handleCancelButtonPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException, TelegramApiException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        userService.saveUser(user);

        chatHandler.sendMessage(update, chatId, bot, "/return_to_main_menu");

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned to the main menu");
    }

    public void handleSearchingButtonPressed(Update update, PersonalVocabularyBot bot, long chatId, int messageId) throws UserNotFoundException, TelegramApiException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SEARCHING_PHRASE);
        userService.saveUser(user);

        chatHandler.sendMessage(update, chatId, bot, "/enter_phrase_to_search");
    }

    public void handleForwardButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / user.getPhrasesPerPage());
        int currentPage = user.getCurrentPageNumber();

        if (currentPage < maxPage - 1) {
            user.setCurrentPageNumber(currentPage + 1);
        } else if (maxPage > 0) {
            user.setCurrentPageNumber(maxPage - 1);
        }
        userService.saveUser(user);

        chatHandler.sendPhrasesPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled to the next page: " + currentPage);
    }

    public void handleFastForwardButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / user.getPhrasesPerPage());
        int currentPage = user.getCurrentPageNumber();

        if (currentPage < maxPage - 10) {
            user.setCurrentPageNumber(currentPage + 10);
        } else if (maxPage > 0) {
            user.setCurrentPageNumber(maxPage - 1);
        }

        userService.saveUser(user);

        chatHandler.sendPhrasesPage(bot, chatId);

        log.info("@" + userService.findUserById(chatId).getNickname() + " scrolled 10 pages forward to the page: " + user.getCurrentPageNumber());
    }

    public void handlePhraseNumberPressed(PersonalVocabularyBot bot, Long chatId, int messageId, String callBackData) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        LinkedList<String> phrasesText = chatHandler.retrievePhrasesByOrder(chatId);


        for (String str : phrasesText) {
            if (isChosenPhrase(callBackData, user, str)) {
                user.setUserBotState(BotStateEnum.READING_PHRASE);
                userService.saveUser(user);

                SendMessage sendMessage = chatHandler.createPhraseWatchingPage(chatId, callBackData);
                chatHandler.executeMessage(bot, sendMessage);

                log.info("@" + user.getNickname() + " opened the page with the phrase: " + str);
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
        chatHandler.sendPhrasesPage(bot, chatId);
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_DICTIONARY);
        userService.saveUser(user);

        log.info("@" + userService.findUserById(chatId).getNickname() + " canceled deleting a phrase");
    }

    public void handleOrderButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS_ORDER);
        userService.saveUser(user);

        SendMessage sendMessage = chatHandler.createSettingsOrderMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);

        log.info("@" + userService.findUserById(chatId).getNickname() + " pressed the order button");
    }

    public void handleNumberButtonPressed(PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatHandler.deleteMessage(bot, chatId, messageId);

        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS_NUMBER);
        userService.saveUser(user);

        SendMessage sendMessage = chatHandler.createSettingsNumberMessage(chatId);
        chatHandler.executeMessage(bot, sendMessage);

        log.info("@" + userService.findUserById(chatId).getNickname() + " pressed the number button");
    }

    public void handleOrderOptionButtonPressed(String callbackData, Long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);

        switch (callbackData) {
            case "LEN_ASC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_LENGTH_ASC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to descending order based on phrase length.");
            }
            case "LEN_DESC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_LENGTH_DESC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to ascending order based on phrase length.");
            }
            case "VIEWS_ASC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_VIEWS_ASC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to ascending order based on phrase views.");
            }
            case "VIEWS_DESC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_VIEWS_DESC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to descending order based on phrase views.");
            }
            case "DATE_ASC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_ID_ASC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to ascending order based on phrase adding date.");
            }
            case "DATE_DESC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.PHRASE_ID_DESC);

                log.info("@" + userService.findUserById(chatId).getNickname() + " changed phrase sorting to descending order based on phrase adding date.");
            }
            case "ALPHABET_ASC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.ALPHABETICAL_ASC);

                log.info("@" + userService.findUserById(chatId).getNickname() + "  changed phrase sorting to ascending order based on phrase first letter.");
            }
            case "ALPHABET_DESC_BUTTON" -> {
                user.setPhraseSortingState(SettingsOrderEnum.ALPHABETICAL_DESC);

                log.info("@" + userService.findUserById(chatId).getNickname() + "  changed phrase sorting to descending order based on phrase first letter.");
            }
        }

        user.setCurrentPageNumber(0);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        userService.saveUser(user);
    }

    public void handleReturnToMenu(Update update, PersonalVocabularyBot bot, Long chatId) throws UserNotFoundException, TelegramApiException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        userService.saveUser(user);

        chatHandler.sendMessage(update, chatId, bot, "/return_to_main_menu");

        log.info("@" + userService.findUserById(chatId).getNickname() + " returned to the main menu");
    }

    private boolean isChosenPhrase(String callBackData, User user, String phrase) {
        return callBackData.split(": ")[0].equals(String.valueOf(user.getUserId()))
                && callBackData.split(": ")[1].equals(phrase);
    }
}
