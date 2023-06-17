package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.utils.LanguageLocalisation;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static dmitry.polyakov.utils.LanguageLocalisation.messages;

@Component
public class ReplyKeyboardFactory {
    protected ReplyKeyboardMarkup createMainKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(createMainKeyboard());
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    protected ReplyKeyboardMarkup createReturnToMenuKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createReturnToMenuRow());
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboardMarkup createLanguageKeyboardMarkup() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createLanguageRow());
        replyKeyboardMarkup.setKeyboard(keyboardRows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        return replyKeyboardMarkup;
    }

    private KeyboardRow createLanguageRow() {
        KeyboardRow row = new KeyboardRow();

        KeyboardButton engButton = new KeyboardButton();
        engButton.setText(LanguageLocalisation.englishLang);

        KeyboardButton ruButton = new KeyboardButton();
        ruButton.setText(LanguageLocalisation.russianLang);

        row.add(engButton);
        row.add(ruButton);

        return row;
    }

    private KeyboardRow createReturnToMenuRow() {
        KeyboardRow row = new KeyboardRow();
        String buttonText = EmojiParser.parseToUnicode(messages.getString("button.name.return")
                + ":house:");
        KeyboardButton returnButton = new KeyboardButton(buttonText);

        row.add(returnButton);

        return row;
    }

    private List<KeyboardRow> createMainKeyboard() {
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton writeButton = new KeyboardButton(EmojiParser.parseToUnicode(messages.getString("button.name.write")
                + ":writing:"));
        KeyboardButton dictionaryButton = new KeyboardButton(EmojiParser.parseToUnicode(messages.getString("button.name.dictionary")
                + ":scroll:"));
        row.add(writeButton);
        row.add(dictionaryButton);
        keyboardRows.add(row);

        return keyboardRows;
    }
}
