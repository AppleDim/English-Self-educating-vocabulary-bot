package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Component
public class CallbackHandler {
    private final UserService userService;
    private final UserPhraseService userPhraseService;
    private final ChatSender chatSender;
    public CallbackHandler(UserService userService,
                           UserPhraseService userPhraseService,
                           ChatSender chatSender) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
        this.chatSender = chatSender;
    }

    public void handleBackButtonPressed(Update update, PersonalVocabularyBot bot, long chatId, int messageId) throws UserNotFoundException {
        chatSender.deleteMessage(update, bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        if (user.getCurrentPageNumber() > 0) {
            int page = user.getCurrentPageNumber();
            user.setCurrentPageNumber(--page);
        } else {
            user.setCurrentPageNumber(0);
        }

        userService.saveUser(user);

        chatSender.getPhrasesFromPage(bot, chatId);
    }

    public void handleSettingsButtonPressed(Update update, PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);
        chatSender.getPhrasesFromPage(bot, chatId);
    }

    public void handleCancelButtonPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException, TelegramApiException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        chatSender.deleteMessage(update, bot, chatId, messageId);
        userService.saveUser(user);
        chatSender.sendMessage(update, chatId, bot, "/return_to_main_menu");
    }

    public void handleSearchingButtonPressed(Update update, PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.SETTINGS);
        userService.saveUser(user);
        chatSender.getPhrasesFromPage(bot, chatId);
    }

    public void handleForwardButtonPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId) throws UserNotFoundException {
        chatSender.deleteMessage(update, bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        int maxPage = userPhraseService.countUserPhrases(chatId) / 10;
        int currentPage = user.getCurrentPageNumber();

        if (currentPage < maxPage) {
            user.setCurrentPageNumber(currentPage + 1);
        } else {
            user.setCurrentPageNumber(maxPage);
        }

        userService.saveUser(user);

        chatSender.getPhrasesFromPage(bot, chatId);

    }

    public void handlePhraseNumberPressed(Update update, PersonalVocabularyBot bot, Long chatId, int messageId, String callBackData) throws UserNotFoundException {
        chatSender.deleteMessage(update, bot, chatId, messageId);
        User user = userService.findUserById(chatId);
        user.setUserBotState(BotStateEnum.READING_WORD);
        List<String> phrasesText = userPhraseService.findUserPhrasesById(chatId);
        for (String s : phrasesText) {
            if (callBackData.split(": ")[0].equals(String.valueOf(user.getUserId()))
                    && callBackData.split(": ")[1].equals(s)) {

                InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
                List<InlineKeyboardButton> row = new ArrayList<>();
                InlineKeyboardButton cancelButton = new InlineKeyboardButton();
                cancelButton.setText(EmojiParser.parseToUnicode(":x:"));
                cancelButton.setCallbackData("CANCEL_BUTTON");
                row.add(cancelButton);
                inlineKeyboard.add(row);
                inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

                SendMessage sendMessage = new SendMessage();
                sendMessage.setChatId(String.valueOf(chatId));
                sendMessage.setText("Chosen phrase:\n" + callBackData.split(": ")[1]);
                sendMessage.setReplyMarkup(inlineKeyboardMarkup);

                chatSender.executeMessage(bot, sendMessage);

                userService.saveUser(user);
                break;
            }
        }
    }
}
