package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.models.User;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Component
public class InlineKeyboardFactory {
    protected InlineKeyboardMarkup createDeletePageInlineKeyboardMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton cancelButton = createInlineKeyboardButton(":house:", "CANCEL_BUTTON");
        InlineKeyboardButton deleteButton = createInlineKeyboardButton(":x:", "DELETE_BUTTON");

        row.add(deleteButton);
        row.add(cancelButton);

        inlineKeyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }
    public InlineKeyboardMarkup createSettingsInlineMarkup() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton orderButton = createInlineKeyboardButton("order", "ORDER_BUTTON");
        InlineKeyboardButton cancelButton = createInlineKeyboardButton(EmojiParser.parseToUnicode(":house:"), "CANCEL_BUTTON");
        InlineKeyboardButton amountButton = createInlineKeyboardButton("amount", "AMOUNT_BUTTON");

        row.add(orderButton);
        row.add(cancelButton);
        row.add(amountButton);

        inlineKeyboard.add(row);
        inlineKeyboardMarkup.setKeyboard(inlineKeyboard);

        return inlineKeyboardMarkup;
    }

    protected InlineKeyboardMarkup createDeleteConfirmationInlineMarkup() {
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

    protected InlineKeyboardMarkup createDeleteConfirmationInlineMarkup(List<String> phrasesText, User user, int startIndex, int endIndex) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> inlineKeyboard = new ArrayList<>();
        putGeneralDictionaryButtons(inlineKeyboard);
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        int elementsPerRow = 5;
        int currentRowElements = 0;

        for (int i = startIndex; i < endIndex; i++) {
            InlineKeyboardButton button = createPhraseInlineKeyboardButton(user, phrasesText, i);
            currentRow.add(button);
            currentRowElements++;

            if (currentRowElements == elementsPerRow || i == endIndex - 1) {
                inlineKeyboard.add(currentRow);
                currentRow = new ArrayList<>();
                currentRowElements = 0;
            }
        }

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
        InlineKeyboardButton fastBackButton = new InlineKeyboardButton();
        fastBackButton.setText(EmojiParser.parseToUnicode(":rewind:"));
        fastBackButton.setCallbackData("FAST_BACK_BUTTON");

        InlineKeyboardButton backButton = new InlineKeyboardButton();
        backButton.setText(EmojiParser.parseToUnicode(":arrow_left:"));
        backButton.setCallbackData("BACK_BUTTON");

        InlineKeyboardButton settingsButton = new InlineKeyboardButton();
        settingsButton.setText(EmojiParser.parseToUnicode(":gear:"));
        settingsButton.setCallbackData("SETTINGS_BUTTON");

        InlineKeyboardButton cancelButton = new InlineKeyboardButton();
        cancelButton.setText(EmojiParser.parseToUnicode(":house:"));
        cancelButton.setCallbackData("CANCEL_BUTTON");

        InlineKeyboardButton searchingButton = new InlineKeyboardButton();
        searchingButton.setText(EmojiParser.parseToUnicode(":mag:"));
        searchingButton.setCallbackData("SEARCHING_BUTTON");

        InlineKeyboardButton forwardButton = new InlineKeyboardButton();
        forwardButton.setText(EmojiParser.parseToUnicode(":arrow_right:"));
        forwardButton.setCallbackData("FORWARD_BUTTON");

        InlineKeyboardButton fastForwardButton = new InlineKeyboardButton();
        fastForwardButton.setText(EmojiParser.parseToUnicode(":fast_forward:"));
        fastForwardButton.setCallbackData("FAST_FORWARD_BUTTON");

        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        rowInLine.add(fastBackButton);
        rowInLine.add(backButton);
        rowInLine.add(settingsButton);
        rowInLine.add(cancelButton);
        rowInLine.add(searchingButton);
        rowInLine.add(forwardButton);
        rowInLine.add(fastForwardButton);

        rowsInline.add(rowInLine);
    }
}
