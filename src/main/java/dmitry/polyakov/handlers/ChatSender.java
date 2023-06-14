package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ChatSender {
    private final UserService userService;
    private final UserPhraseService userPhraseService;

    @Autowired
    public ChatSender(UserService userService,
                      UserPhraseService userPhraseService) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
    }

    public void sendMessage(Update update, Long chatId, PersonalVocabularyBot bot, String text) throws TelegramApiException, UserNotFoundException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textBuilder(update, chatId, text));
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.DEFAULT_STATE)) {
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            replyKeyboardMarkup.setKeyboard(createMainKeyboard());
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        } else if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.READING_DICTIONARY)) {
            getPhrasesFromPage(bot, chatId);
        } else {
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
            List<KeyboardRow> keyboardRows = new ArrayList<>();
            keyboardRows.add(createReturnToMenuRow());
            replyKeyboardMarkup.setKeyboard(keyboardRows);
            replyKeyboardMarkup.setResizeKeyboard(true);
            replyKeyboardMarkup.setOneTimeKeyboard(true);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }

        executeMessage(bot, sendMessage);
    }

    public void deleteMessage(Update update, PersonalVocabularyBot bot, long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(update.getCallbackQuery().getId());
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(String.valueOf(chatId));
        executeMessage(bot, deleteMessage);
    }

    public void getPhrasesFromPage(PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        List<String> phrasesText = userPhraseService.findUserPhrasesById(chatId);
        User user = userService.findUserById(chatId);
        int page = user.getCurrentPageNumber();

        int pageSize = 10;
        int elementsPerRow = 5;
        int currentRowElements = 0;

        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, phrasesText.size()); // Исправленное значение endIndex
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        putGeneralButtons(inlineKeyboard);
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        sb.append(EmojiParser.parseToUnicode(":page_facing_up:"))
                .append("Page ").append(page + 1).append(":\n")
                .append("-----------------------------------------\n");
        for (int i = startIndex; i < endIndex; i++) {
            sb.append(i + 1).append(". ").append("*").append(phrasesText.get(i)).append("*").append("\n");
            InlineKeyboardButton button = new InlineKeyboardButton(String.valueOf(i + 1));
            button.setCallbackData(user.getUserId() + ": " + phrasesText.get(i));
            currentRow.add(button);
            currentRowElements++;

            if (currentRowElements == elementsPerRow || i == endIndex - 1) {
                inlineKeyboard.add(currentRow);

                currentRow = new ArrayList<>();
                currentRowElements = 0;
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(sb.toString());
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        executeMessage(bot, sendMessage);
    }


    public void sendVoiceMessage(Update update, PersonalVocabularyBot bot) {
        Long chatId = update.getMessage().getChatId();

        SendVoice sendVoice = new SendVoice();
        sendVoice.setVoice(new InputFile("file"));
        sendVoice.setChatId(String.valueOf(chatId));
        sendVoice.setParseMode(ParseMode.MARKDOWN);

        executeMessage(bot, sendVoice);
    }

    private String textBuilder(Update update, Long chatId, String text) throws UserNotFoundException {
        switch (text) {
            case "/start" -> {
                if (!userService.isUserFoundById(chatId)) {
                    registerUser(update);
                    return String.format("Hello, @%s", userService.findUserById(chatId).getNickname());

                } else
                    return "The bot was already started.";
            }

            case "/help" -> {
                return "Here's the helping page: ";
            }

            case "/write" -> {
                return "Enter your words or phrase";
            }

            case "/phrase" -> {
                return "Phrase has been stored.";
            }

            case "/illegal_characters" -> {
                return "Only latin letters are allowed.";
            }

            case "/phrase_already_stored" -> {
                return "This phrase was already stored.";
            }
            case "/return_to_main_menu" -> {
                return "Moving back... Press the button.";
            }
        }
        return "";
    }

    public <T> void executeMessage(PersonalVocabularyBot bot, T message) {
        try {
            if (message instanceof SendMessage) {
                bot.execute((SendMessage) message);
            } else if (message instanceof SendVoice) {
                bot.execute((SendVoice) message);
            } else if (message instanceof SendSticker) {
                bot.execute((SendSticker) message);
            } else if (message instanceof DeleteMessage) {
                bot.execute((DeleteMessage) message);
            } else {
                log.warn("Unsupported message type: " + message.getClass().getSimpleName() + "\n");
            }
        } catch (TelegramApiException e) {
            log.warn("Error occurred while trying to send a message", e);
        }
    }

    public void registerUser(Update update) {
        Chat chat = update.getMessage().getChat();

        User user = new User();

        user.setUserId(update.getMessage().getChatId());
        user.setFirstName(chat.getFirstName());
        user.setNickname(chat.getUserName());
        user.setRegisteredDate(new Timestamp(System.currentTimeMillis()));
        user.setUserBotState(BotStateEnum.DEFAULT_STATE);
        user.setCurrentPageNumber(0);

        userService.saveUser(user);
    }

    private List<KeyboardRow> createMainKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton writeButton = new KeyboardButton(EmojiParser.parseToUnicode(("write")
                + ":writing:"));
        KeyboardButton dictionaryButton = new KeyboardButton(EmojiParser.parseToUnicode(("dictionary")
                + ":scroll:"));
        row.add(writeButton);
        row.add(dictionaryButton);
        keyboardRows.add(row);

        return keyboardRows;
    }

    private KeyboardRow createReturnToMenuRow() {
        KeyboardRow row = new KeyboardRow();
        String buttonText = EmojiParser.parseToUnicode(("return")
                + ":x:");
        KeyboardButton returnButton = new KeyboardButton(buttonText);

        row.add(returnButton);

        return row;
    }


    public void putGeneralButtons(List<List<InlineKeyboardButton>> rowsInline) {
        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(EmojiParser.parseToUnicode(":arrow_left:"));
        backButton.setCallbackData("BACK_BUTTON");

        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        settingsButton.setText(EmojiParser.parseToUnicode(":gear:"));
        settingsButton.setCallbackData("SETTINGS_BUTTON");

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode(":x:"));
        cancelButton.setCallbackData("CANCEL_BUTTON");

        InlineKeyboardButton searchingButton = new InlineKeyboardButton();
        searchingButton.setText(EmojiParser.parseToUnicode(":mag:"));
        searchingButton.setCallbackData("SEARCHING_BUTTON");

        InlineKeyboardButton forwardButton = new InlineKeyboardButton();
        forwardButton.setText(EmojiParser.parseToUnicode(":arrow_right:"));
        forwardButton.setCallbackData("FORWARD_BUTTON");

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        rowInLine.add(backButton);
        rowInLine.add(settingsButton);
        rowInLine.add(cancelButton);
        rowInLine.add(searchingButton);
        rowInLine.add(forwardButton);

        rowsInline.add(rowInLine);
    }
}
