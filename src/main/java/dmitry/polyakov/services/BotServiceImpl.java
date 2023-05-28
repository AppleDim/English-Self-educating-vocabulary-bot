package dmitry.polyakov.services;

import com.vdurmont.emoji.EmojiParser;
import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.BotStateEnum;
import dmitry.polyakov.exceptions.UserNotFoundException;
import dmitry.polyakov.handlers.MessageHandler;
import dmitry.polyakov.models.User;
import dmitry.polyakov.repositories.PhraseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Service
public class BotServiceImpl implements BotService {
    private final PhraseRepository phraseRepository;
    private final MessageHandler commandHandler;
    private final UserService userService;

    @Autowired
    public BotServiceImpl(PhraseRepository phraseRepository, MessageHandler commandHandler, UserService userService) {
        this.phraseRepository = phraseRepository;
        this.commandHandler = commandHandler;
        this.userService = userService;
    }

    @Override
    public void getUpdate(Update update, PersonalVocabularyBot bot) {
        if (update.getMessage().hasText()) {
            String command = update.getMessage().getText();
            try {
                handleMessages(command, update, bot);
            } catch (UserNotFoundException e) {
                log.warn("Error finding user with id = " + update.getMessage().getChatId(), e);
            }
        } else if (update.hasCallbackQuery()) {
            try {
                handleCallback(update, bot);
            } catch (TelegramApiException e) {
                log.warn("Unexpected error has occurred while processing", e);
            }

        }
    }

    private void handleMessages(String command, Update update, PersonalVocabularyBot bot) throws UserNotFoundException {

        if (command.equals("/start")) {
            commandHandler.handleStartCommandReceived(update, bot);
        }

        User user = userService.findUserById(update.getMessage().getChatId());

        if (command.equals("/help")) {
            commandHandler.handleHelpCommandReceived(update, bot);

        } else if (command.equals("/dictionary")) {
            user.setUserBotState(BotStateEnum.READING_DICTIONARY);
            userService.saveUser(user);
            commandHandler.handleDictionaryCommandReceived(update, bot);

        } else if (command.equals(EmojiParser.parseToUnicode(("dictionary")
                + ":gb:"))
                && user.getUserBotState().equals(BotStateEnum.DEFAULT_STATE)) {
            user.setUserBotState(BotStateEnum.READING_DICTIONARY);
            userService.saveUser(user);
            commandHandler.handleDictionaryCommandReceived(update, bot);

        } else if (command.equals("/settings")) {
            commandHandler.handleSettingsCommandReceived(update, bot);

        } else if (command.equals("/language")) {
            commandHandler.handleLanguageCommandReceived(update, bot);

        } else if (command.equals("/write")) {
            user.setUserBotState(BotStateEnum.WRITING_WORDS);
            userService.saveUser(user);
            commandHandler.handleWriteCommandReceived(update, bot);

        } else if (command.equals(EmojiParser.parseToUnicode(("write")
                + ":abc:"))
                && user.getUserBotState().equals(BotStateEnum.DEFAULT_STATE)) {
            user.setUserBotState(BotStateEnum.WRITING_WORDS);
            userService.saveUser(user);
            commandHandler.handleWriteCommandReceived(update, bot);

        } else if (user.getUserBotState().equals(BotStateEnum.WRITING_WORDS)) {
            commandHandler.handlePhraseReceived(update, bot);
        }
        else if (command.equals(EmojiParser.parseToUnicode(("Back to main menu") +
                ":x:")) && !user.getUserBotState().equals(BotStateEnum.DEFAULT_STATE)) {
            user.setUserBotState(BotStateEnum.DEFAULT_STATE);
            userService.saveUser(user);
        }
    }

    private void handleCallback(Update update, PersonalVocabularyBot bot) throws TelegramApiException {
    }
}
