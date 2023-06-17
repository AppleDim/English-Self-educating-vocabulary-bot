package dmitry.polyakov.utils;

import com.vdurmont.emoji.EmojiParser;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageLocalisation {
    public static ResourceBundle messages = ResourceBundle.getBundle("messages", new Locale("en"));
    public static final String russianLang = EmojiParser.parseToUnicode("Русский:ru:");
    public static final String englishLang = EmojiParser.parseToUnicode("English:gb:");

}
