package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.constants.SettingsOrderEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserPhraseService;
import dmitry.polyakov.services.UserService;
import dmitry.polyakov.utils.HtmlConnector;
import dmitry.polyakov.utils.LanguageLocalisation;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.LinkedList;
import java.util.ResourceBundle;

@Component
@Slf4j
public class ChatHandler {
    private final UserService userService;
    private final UserPhraseService userPhraseService;
    private final InlineKeyboardFactory inlineKeyboardFactory;
    private final ReplyKeyboardFactory replyKeyboardFactory;
    private final LanguageLocalisation languageLocalisation;
    private final HtmlConnector htmlConnector;


    @Autowired
    public ChatHandler(UserService userService,
                       UserPhraseService userPhraseService,
                       InlineKeyboardFactory inlineKeyboardFactory,
                       ReplyKeyboardFactory replyKeyboardFactory,
                       LanguageLocalisation languageLocalisation,
                       HtmlConnector htmlConnector) {
        this.userService = userService;
        this.userPhraseService = userPhraseService;
        this.inlineKeyboardFactory = inlineKeyboardFactory;
        this.replyKeyboardFactory = replyKeyboardFactory;
        this.languageLocalisation = languageLocalisation;
        this.htmlConnector = htmlConnector;
    }

    public void sendMessage(Update update, Long chatId, PersonalVocabularyBot bot, String text) throws TelegramApiException, UserNotFoundException {
        SendMessage sendMessage = createBaseSendMessage(update, chatId, text);
        setSendingMessageReplyMarkup(sendMessage, chatId, bot);

        executeMessage(bot, sendMessage);
    }

    public void deleteMessage(PersonalVocabularyBot bot, long chatId, int messageId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(String.valueOf(chatId));
        deleteMessage.setMessageId(messageId);
        deleteMessage.setChatId(String.valueOf(chatId));
        executeMessage(bot, deleteMessage);
    }

    public void sendPhrasesPage(PersonalVocabularyBot bot, long chatId) throws UserNotFoundException {
        LinkedList<String> phrasesText = retrievePhrasesByOrder(chatId);
        User user = userService.findUserById(chatId);
        int page = user.getCurrentPageNumber();

        int pageSize = user.getPhrasesPerPage();
        int maxPage = (int) Math.ceil((double) userPhraseService.countUserPhrases(chatId) / pageSize);
        int startIndex = page * pageSize;
        int endIndex = Math.min(startIndex + pageSize, phrasesText.size());

        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createPhrasesPageInlineMarkup(phrasesText, user, startIndex, endIndex);
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

    protected SendMessage createPhraseWatchingPage(Long chatId, String callBackData) throws UserNotFoundException {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        String phrase = callBackData.split(": ")[1];
        return createTextWithInlineSendMessage(chatId, messages.getString("message.chosen_phrase") + "\n" + phrase + "");
    }

    protected SendMessage createDeleteConfirmationMessage(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        SendMessage sendMessage = createTextSendMessage(chatId, messages.getString("message.confirm_deleting"));
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createPhrasesPageInlineMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    protected SendMessage createPhraseWatchingMessage(Long chatId, String text) {
        SendMessage sendMessage = createTextSendMessage(chatId, text);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createPhraseWatchingInlineMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    protected SendMessage createSettingsPageMessage(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        String str = messages.getString("settings.options.string_1") + "\n" + messages.getString("settings.options.string_2") +
                EmojiParser.parseToUnicode(":twisted_rightwards_arrows:") + "\n" +
                messages.getString("settings.options.string_3") + EmojiParser.parseToUnicode(":arrows_clockwise:");
        SendMessage sendMessage = createTextSendMessage(chatId, str);
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createSettingsInlineMarkup();
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    protected SendMessage createSettingsOrderMessage(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        SendMessage sendMessage = createTextSendMessage(chatId, messages.getString("settings.options.order"));
        InlineKeyboardMarkup inlineKeyboardMarkup = inlineKeyboardFactory.createOrderInlineMarkup(chatId);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        return sendMessage;
    }

    protected SendMessage createSettingsAmountMessage(long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        return createTextSendMessage(chatId, messages.getString("settings.options.amount"));
    }

    protected SendMessage createTextSendMessage(Long chatId, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    private SendMessage createBaseSendMessage(Update update, Long chatId, String text) throws UserNotFoundException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textBuilder(update, chatId, text));
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    private SendMessage createTextWithInlineSendMessage(Long chatId, String text) throws UserNotFoundException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setReplyMarkup(inlineKeyboardFactory.createEnglishInlineKeyboard(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        return sendMessage;
    }

    private SendMessage createPhrasesPageMessage(long chatId, int page, int maxPage, LinkedList<String> phrasesText, int startIndex, int endIndex) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        StringBuilder sb = new StringBuilder(256);

        if (maxPage == 0) {
            maxPage = 1;
        }

        sb.append(EmojiParser.parseToUnicode(":page_facing_up:"))
                .append(messages.getString("chat.page_number")).append(" ").append(page + 1).append("/").append(maxPage).append(":\n")
                .append("-----------------------------------------\n");

        if (startIndex == 0 && endIndex == 0) {
            sb.append(EmojiParser.parseToUnicode(":exclamation:")).append("*You haven't added any phrases yet*")
                    .append(EmojiParser.parseToUnicode(":exclamation:"));
        }
        for (int i = startIndex; i < endIndex; i++) {
            String phraseText = phrasesText.get(i);
            int countPhraseViews = userPhraseService.getNumberPhraseViews(chatId, phraseText);
            sb.append(i + 1).append(". ");
            if (countPhraseViews == 0) {
                sb.append("*").append(phraseText).append("*");
            } else {
                sb.append(phraseText);
            }
            sb.append(" ").append("(").append(countPhraseViews).append(")").append("\n");
        }

        return createTextSendMessage(chatId, sb.toString());
    }

    private void setSendingMessageReplyMarkup(SendMessage sendMessage, Long chatId, PersonalVocabularyBot bot) throws UserNotFoundException {
        if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.DEFAULT_STATE)) {
            ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardFactory.createMainKeyboardMarkup(chatId);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

        } else if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.READING_DICTIONARY)) {
            sendPhrasesPage(bot, chatId);

        } else if (userService.isUserMatchedWithBotState(chatId, BotStateEnum.LANGUAGE_CHANGE)) {
            ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardFactory.createLanguageKeyboardMarkup();
            sendMessage.setReplyMarkup(replyKeyboardMarkup);

        } else {
            ReplyKeyboardMarkup replyKeyboardMarkup = replyKeyboardFactory.createReturnToMenuKeyboardMarkup(chatId);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
        }
    }

    private String textBuilder(Update update, Long chatId, String text) throws UserNotFoundException {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);

        switch (text) {
            case "/start" -> {
                if (!userService.isUserFoundById(chatId)) {
                    registerUser(update);
                    return String.format(messages.getString("chat.hello")
                            + " @%s", userService.findUserById(chatId).getNickname());

                } else
                    return messages.getString("message.bot_started");
            }

            case "/help" -> {
                return messages.getString("message.helping");
            }
            case "/write" -> {
                return messages.getString("message.writing");
            }
            case "/phrase" -> {
                return messages.getString("message.phrase_stored");
            }
            case "/language" -> {
                return messages.getString("message.choose_language");
            }
            case "/lang" -> {
                return messages.getString("message.lang_chosen");
            }
            case "/illegal_characters" -> {
                return messages.getString("message.illegal_chars");
            }
            case "/phrase_already_stored" -> {
                return messages.getString("message.phrase_already_stored");
            }
            case "/return_to_main_menu" -> {
                return messages.getString("message.moving_back.main_menu");
            }
            case "/return_to_dictionary" -> {
                return messages.getString("message.moving_back.dict");
            }
            case "/number_saved" -> {
                return messages.getString("settings.valid_number");
            }
            case "/invalid_number_entered" -> {
                return messages.getString("settings.invalid_number");
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
        user.setPhraseSortingState(SettingsOrderEnum.PHRASE_ID_ASC);
        user.setPhrasesPerPage(10);

        userService.saveUser(user);
    }

    public LinkedList<String> retrievePhrasesByOrder(Long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        SettingsOrderEnum sortingState = user.getPhraseSortingState();

        return switch (sortingState) {
            case PHRASE_ID_ASC -> userPhraseService.findUserPhrasesByIdOrderByPhraseIdAsc(chatId);
            case PHRASE_ID_DESC -> userPhraseService.findUserPhrasesByIdOrderByPhraseIdDesc(chatId);
            case PHRASE_LENGTH_ASC -> userPhraseService.findUserPhrasesByUserIdOrderByPhraseLengthAsc(chatId);
            case PHRASE_LENGTH_DESC -> userPhraseService.findUserPhrasesByUserIdOrderByPhraseLengthDesc(chatId);
            case PHRASE_VIEWS_ASC -> userPhraseService.findUserPhrasesByIdOrderByCountPhraseViewsAsc(chatId);
            case PHRASE_VIEWS_DESC -> userPhraseService.findUserPhrasesByIdOrderByCountPhraseViewsDesc(chatId);
            case ALPHABETICAL_ASC -> userPhraseService.findUserPhrasesByUserIdOrderByAlphabeticalAsc(chatId);
            case ALPHABETICAL_DESC -> userPhraseService.findUserPhrasesByUserIdOrderByAlphabeticalDesc(chatId);
        };
    }

    public String createEnglishPhraseMeaningText(Long chatId) throws UserNotFoundException {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        User user = userService.findUserById(chatId);

        Document doc = htmlConnector.getDocFromUrl("https://dictionary.cambridge.org/dictionary/english/" + user.getCurrentPhrase());
        Elements elements = doc.select(".ddef_h");
        if (elements.size() == 0) {
            return messages.getString("message.no_definitions_found");
        }

        StringBuilder sb = new StringBuilder();
        String lastDefinition = null;
        int totalLength = 0;

        for (Element element : elements) {
            String str = !element.getElementsByClass("def-info ddef-info").hasText() ?
                    (element.text().replace(":", "") + "\n") :
                    (element.getElementsByClass("def-info ddef-info").text().replace(":", "") + "\n") +
                            element.getElementsByClass("def ddef_d db").text().replace(":", "") + "\n";

            if (totalLength + str.length() > 4056) {
                break;
            }

            sb.append(str);
            sb.append("\n");
            totalLength += str.length();
            lastDefinition = str;
        }

        if (sb.length() > 4056 && lastDefinition != null) {
            sb.replace(sb.lastIndexOf(lastDefinition), sb.length(), "");
        }

        return sb.toString().replaceAll("\n{3,}", "\n\n");
    }

    public String createSentencesWithPhrase(Long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        Document doc = htmlConnector.getDocFromUrl("https://context.reverso.net/translation/english-russian/"  + user.getCurrentPhrase());
        Elements elements = doc.body().getElementsByClass("example");
        StringBuilder sb = new StringBuilder();
        String lastDefinition = null;
        int totalLength = 0;
        for (Element element : elements) {
            String str = EmojiParser.parseToUnicode(":gb:")
                    + element.getElementsByClass("src ltr").text() + "\n"
                    + EmojiParser.parseToUnicode(":ru:")
                    + element.getElementsByClass("trg ltr").text() + "\n";

            if (totalLength + str.length() > 4056) {
                break;
            }

            sb.append(str);
            sb.append("\n");
            totalLength += str.length();
            lastDefinition = str;
        }

        if (sb.length() > 4056 && lastDefinition != null) {
            sb.replace(sb.lastIndexOf(lastDefinition), sb.length(), "");
        }

        return sb.toString();
    }
}
