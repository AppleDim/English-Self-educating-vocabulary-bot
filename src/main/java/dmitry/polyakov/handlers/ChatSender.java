package dmitry.polyakov.handlers;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.models.Phrase;
import dmitry.polyakov.models.User;
import dmitry.polyakov.services.PhraseService;
import dmitry.polyakov.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;

@Component
@Slf4j
public class ChatSender {
    private final UserService userService;
    private final PhraseService phraseService;

    @Autowired
    public ChatSender(UserService userService, PhraseService phraseService) {
        this.userService = userService;
        this.phraseService = phraseService;
    }

    public void sendMessage(Update update, PersonalVocabularyBot bot, String text) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(textBuilder(update, text));
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setParseMode(ParseMode.MARKDOWN);

        executeMessage(bot, sendMessage);
    }

    public void sendVoiceMessage(Update update, PersonalVocabularyBot bot) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();

        SendVoice sendVoice = new SendVoice();
        sendVoice.setVoice(new InputFile("file"));
        sendVoice.setChatId(String.valueOf(chatId));
        sendVoice.setParseMode(ParseMode.MARKDOWN);

       executeMessage(bot, sendVoice);
    }

    private String textBuilder(Update update, String text) {
        switch (text) {
            case "/start" -> {
                if (!userService.isUserFoundById(update.getMessage().getChatId())) {
                    Chat chat = update.getMessage().getChat();

                    User user = new User();

                    user.setId(update.getMessage().getChatId());
                    user.setFirstName(chat.getFirstName());
                    user.setNickname(chat.getUserName());
                    user.setRegisteredDate(new Timestamp(System.currentTimeMillis()));
                    user.setUserBotState(BotStateEnum.DEFAULT_STATE);

                    userService.saveUser(user);

                    return String.format("Hello, @%s", user.getNickname());
                } else
                    return "The bot was already started.";
            }
            case "/help" -> {
                return "Here's the helping page: ";
            }
            case "/write" -> {
                return  "Enter your words or phrase";
            }
            case "/phrase" -> {
                return "Phrase has been stored.";
            }
            case "/show_phrases" -> {
                StringBuilder textBuilder = new StringBuilder("List of phrases:\n");
                for (Phrase phrase : phraseService.getAllPhrases()) {
                    textBuilder.append("__").append(phrase.getPhrase()).append("__").append("\n");
                }
                return textBuilder.toString();
            }
        }
        return "";
    }

    public void executeMessage(PersonalVocabularyBot bot, SendMessage message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.warn("Error occurred while trying to send a text message", e);
        }
    }
    public void executeMessage(PersonalVocabularyBot bot, SendVoice message) {
        try {
            bot.execute(message);
        } catch (TelegramApiException e) {
            log.warn("Error occurred while trying to send a voice message", e);
        }
    }
}
