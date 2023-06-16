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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.List;

@Component
@Slf4j
public class ChatSender {
    private final UserService userService;
    private final UserPhraseService userPhraseService;
    private final InlineKeyboardFactory inlineKeyboardFactory ;
    private final ReplyKeyboardFactory replyKeyboardFactory;

    @Autowired
    public ChatSender(UserService userService,
                      UserPhraseService userPhraseService,
                      InlineKeyboardFactory inlineKeyboardFactory,
                      ReplyKeyboardFactory replyKeyboardFactory) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
        this.inlineKeyboardFactory = inlineKeyboardFactory;
        this.replyKeyboardFactory = replyKeyboardFactory;
    }

    public void sendMessage(Update update, Long chatId, PersonalVocabularyBot bot, String text) throws TelegramApiException, UserNotFoundException {
        SendMessage sendMessage = createBaseSendMessage(update, chatId, text);
        setSendingMessageReplyMarkup(sendMessage, chatId, bot);

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

    public void deleteMessage(PersonalVocabularyBot bot, long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(String.valueOf(chatId));
        executeMessage(bot, deleteMessage);
    }

    public void getPhrasesFromPage(PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        List<String> phrasesText = userPhraseService.findUserPhrasesById(chatId);
        User user = userService.findUserById(chatId);
        int page = user.getCurrentPageNumber();

        int pageSize = 10;
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / pageSize);

        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, phrasesText.size());

        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createDeleteConfirmationInlineMarkup(phrasesText, user, startIndex, endIndex);
        SendMessage sendMessage = createPhrasesPageMessage(chatId, page, maxPage, phrasesText, startIndex, endIndex);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);

        executeMessage(bot, sendMessage);
    }

    protected <T> void executeMessage(PersonalVocabularyBot bot, T message) {
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

    protected SendMessage createPhraseWatchingPage(Long chatId, String callBackData) {
        String phrase = callBackData.split(": ")[1];
        return createTextSendMessage(chatId, "Chosen phrase:\n" + phrase);
    }

    protected SendMessage createDeleteConfirmationMessage(Long chatId) {
        SendMessage sendMessage = createTextSendMessage(chatId, "Confirm deleting the phrase");
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createDeleteConfirmationInlineMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    private SendMessage createBaseSendMessage(Update update, Long chatId, String text) throws UserNotFoundException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textBuilder(update, chatId, text));
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    private SendMessage createTextSendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    private SendMessage createPhrasesPageMessage(long chatId, int page, int maxPage, List<String> phrasesText, int startIndex, int endIndex) {
        StringBuilder sb = new StringBuilder();
        sb.append(EmojiParser.parseToUnicode(":page_facing_up:"))
                .append("Page ").append(page + 1).append("/").append(maxPage).append(":\n")
                .append("-----------------------------------------\n");

        for (int i = startIndex; i < endIndex; i++) {
            sb.append(i + 1).append(". ").append("*").append(phrasesText.get(i)).append("*").append("\n");
        }

        return createTextSendMessage(chatId, sb.toString());
    }

    private void setSendingMessageReplyMarkup(SendMessage sendMessage, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.DEFAULT_STATE)) {
            ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardFactory.createMainKeyboardMarkup();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        } else if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.READING_DICTIONARY)) {
            getPhrasesFromPage(bot, chatId);
        } else {
            ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardFactory.createReturnToMenuKeyboardMarkup();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
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
            case "/return_to_dictionary" -> {
                return "Moving back to the dictionary...";
            }
        }
        return "";
    }

    private void registerUser(Update update) {
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
}
