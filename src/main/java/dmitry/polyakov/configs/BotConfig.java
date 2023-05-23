package dmitry.polyakov.configs;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.constants.Menu;
import dmitry.polyakov.services.BotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class BotConfig {

    @Value("${telegram.bot.name}")
    private String botUserName;

    @Value("${telegram.bot.token}")
    private String botToken;

    private BotService botService;

    @Autowired
    public void setBotService(BotService botService) {
        this.botService = botService;
    }

    @Bean
    public PersonalVocabularyBot personalVocabularyBot() throws TelegramApiException {
        PersonalVocabularyBot bot = new PersonalVocabularyBot(botUserName, botToken, botService);
        bot.execute(new SetMyCommands(Menu.addBotCommands(), new BotCommandScopeDefault(), null));

        return bot;
    }
}
