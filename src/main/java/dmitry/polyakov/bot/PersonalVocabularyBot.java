package dmitry.polyakov.bot;

import dmitry.polyakov.services.BotService;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@RequiredArgsConstructor
public class PersonalVocabularyBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final String botToken;
    private final BotService botService;

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        botService.getUpdate(update, this);
    }
}
