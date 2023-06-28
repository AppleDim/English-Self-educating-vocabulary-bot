package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.utils.LanguageLocalisation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;


@Component
public class ReplyKeyboardFactory {
    private LanguageLocalisation languageLocalisation;

    @Autowired
    public void setLanguageLocalisation(LanguageLocalisation languageLocalisation) {
        this.languageLocalisation = languageLocalisation;
    }

    protected ReplyKeyboardMarkup createMainKeyboardMarkup(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(createMainKeyboard(messages));
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        return replyKeyboardMarkup;
    }

    protected ReplyKeyboardMarkup createReturnToMenuKeyboardMarkup(Long chatId) {
        ResourceBundle messages = languageLocalisation.getMessages(chatId);
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createReturnToMenuRow(messages));
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

    private List<KeyboardRow> createMainKeyboard(ResourceBundle messages) {
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

    private KeyboardRow createLanguageRow() {
        KeyboardRow row = new KeyboardRow();

        KeyboardButton engButton = new KeyboardButton();
        engButton.setText(languageLocalisation.englishLang);

        KeyboardButton ruButton = new KeyboardButton();
        ruButton.setText(languageLocalisation.russianLang);

        row.add(engButton);
        row.add(ruButton);

        return row;
    }

    private KeyboardRow createReturnToMenuRow(ResourceBundle messages) {
        KeyboardRow row = new KeyboardRow();
        String buttonText = EmojiParser.parseToUnicode(messages.getString("button.name.return")
                + ":house:");
        KeyboardButton returnButton = new KeyboardButton(buttonText);

        row.add(returnButton);

        return row;
    }
}
