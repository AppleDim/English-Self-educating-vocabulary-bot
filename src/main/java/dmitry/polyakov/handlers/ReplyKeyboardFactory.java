package dmitry.polyakov.handlers;

import com.vdurmont.emoji.EmojiParser;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

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

    private KeyboardRow createReturnToMenuRow() {
        KeyboardRow row = new KeyboardRow();
        String buttonText = EmojiParser.parseToUnicode(("return")
                + ":house:");
        KeyboardButton returnButton = new KeyboardButton(buttonText);

        row.add(returnButton);

        return row;
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
}
