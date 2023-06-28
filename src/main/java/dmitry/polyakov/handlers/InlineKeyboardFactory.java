package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.UserService;
import dmitry.polyakov.utils.HtmlConnector;
import dmitry.polyakov.utils.LanguageLocalisation;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.*;

@Component
public class InlineKeyboardFactory {
    private final UserService userService;
    private final LanguageLocalisation languageLocalisation;
    private final HtmlConnector htmlConnector;

    @Autowired
    public InlineKeyboardFactory(UserService userService,
                                 LanguageLocalisation languageLocalisation,
                                 HtmlConnector htmlConnector) {
        this.userService = userService;
        this.languageLocalisation = languageLocalisation;
        this.htmlConnector = htmlConnector;
    }

    public InlineKeyboardMarkup createSettingsInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton orderButton =
                createInlineKeyboardButton(EmojiParser.parseToUnicode(":twisted_rightwards_arrows:"), "ORDER_BUTTON");
        InlineKeyboardButton cancelButton =
                createInlineKeyboardButton(EmojiParser.parseToUnicode(":house:"), "CANCEL_BUTTON");
        InlineKeyboardButton amountButton =
                createInlineKeyboardButton(EmojiParser.parseToUnicode(":arrows_clockwise:"), "AMOUNT_BUTTON");

        row.add(orderButton);
        row.add(cancelButton);
        row.add(amountButton);

        inlineKeyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createPhrasesPageInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        InlineKeyboardButton yesButton = createInlineKeyboardButton(":white_check_mark:", "YES_BUTTON");
        InlineKeyboardButton noButton = createInlineKeyboardButton(":x:", "NO_BUTTON");

        currentRow.add(yesButton);
        currentRow.add(noButton);

        inlineKeyboard.add(currentRow);
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createPhrasesPageInlineMarkup(List<String> phrasesText, User user, int startIndex, int endIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        putGeneralDictionaryButtons(inlineKeyboard);

        int elementsPerRow = 5;
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int i = startIndex; i < endIndex; i++) {
            InlineKeyboardButton button = createPhraseInlineKeyboardButton(user, phrasesText, i);
            currentRow.add(button);

            if (currentRow.size() == elementsPerRow || i == endIndex - 1) {
                inlineKeyboard.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);
        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createOrderInlineMarkup(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        inlineKeyboard.add(Collections.singletonList(createInlineKeyboardButton(EmojiParser.parseToUnicode(":house:"), "CANCEL_BUTTON")));
        inlineKeyboard.add(Arrays.asList(
                createInlineKeyboardButton(messages.getString("button.length.ascending"), "LEN_ASC_BUTTON"),
                createInlineKeyboardButton(messages.getString("button.length.descending"), "LEN_DESC_BUTTON")
        ));
        inlineKeyboard.add(Arrays.asList(
                createInlineKeyboardButton(messages.getString("button.views.ascending"), "VIEWS_ASC_BUTTON"),
                createInlineKeyboardButton(messages.getString("button.views.descending"), "VIEWS_DESC_BUTTON")
        ));
        inlineKeyboard.add(Arrays.asList(
                createInlineKeyboardButton(messages.getString("button.date.ascending"), "DATE_ASC_BUTTON"),
                createInlineKeyboardButton(messages.getString("button.date.descending"), "DATE_DESC_BUTTON")
        ));

        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);
        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createEnglishInlineKeyboard(Long chatId) throws UserNotFoundException {
        User user = userService.findUserById(chatId);
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();

        Document doc1 = htmlConnector.getDocFromUrl("https://dictionary.cambridge.org/dictionary/english/" + user.getCurrentPhrase());
        Elements elements = doc1.select(".ddef_h");
        if (elements.size() != 0) {
            InlineKeyboardButton button1 = createInlineKeyboardButton("meanings", "ENGLISH_MEANINGS_BUTTON");
            inlineKeyboard.add(Collections.singletonList(button1));
        }

        Document doc = htmlConnector.getDocFromUrl("https://context.reverso.net/translation/english-russian/" + user.getCurrentPhrase());
        if (doc != null) {
            InlineKeyboardButton button2 = createInlineKeyboardButton("english-russian sentences", "SENTENCES_BUTTON");
            inlineKeyboard.add(Collections.singletonList(button2));
        }

        inlineKeyboard.add(createPhraseGeneralRow());
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createPhraseWatchingInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        inlineKeyboard.add(createPhraseGeneralRow());
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);
        return inlineKeyboardMarkup;
    }


    private InlineKeyboardButton createInlineKeyboardButton(String text, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode(text));
        button.setCallbackData(callbackData);
        return button;
    }

    private InlineKeyboardButton createPhraseInlineKeyboardButton(User user, List<String> phrasesText, int index) {
        String buttonText = String.valueOf(index + 1);
        String callbackData = user.getUserId() + ": " + phrasesText.get(index);

        InlineKeyboardButton button = new InlineKeyboardButton(buttonText);
        button.setCallbackData(callbackData);
        return button;
    }

    private void putGeneralDictionaryButtons(List<List<InlineKeyboardButton>> rowsInline) {
        rowsInline.add(Arrays.asList(
                createInlineKeyboardButton(":rewind:", "FAST_BACK_BUTTON"),
                createInlineKeyboardButton(":arrow_left:", "BACK_BUTTON"),
                createInlineKeyboardButton(":gear:", "SETTINGS_BUTTON"),
                createInlineKeyboardButton(":house:", "CANCEL_BUTTON"),
                createInlineKeyboardButton(":mag:", "SEARCHING_BUTTON"),
                createInlineKeyboardButton(":arrow_right:", "FORWARD_BUTTON"),
                createInlineKeyboardButton(":fast_forward:", "FAST_FORWARD_BUTTON")
        ));
    }

    private List<InlineKeyboardButton> createPhraseGeneralRow() {
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton cancelButton = createInlineKeyboardButton(":house:", "CANCEL_BUTTON");
        InlineKeyboardButton deleteButton = createInlineKeyboardButton(":x:", "DELETE_BUTTON");

        row.add(deleteButton);
        row.add(cancelButton);

        return row;
    }
}
