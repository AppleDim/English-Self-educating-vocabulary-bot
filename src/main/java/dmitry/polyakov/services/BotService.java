package dmitry.polyakov.services;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import dmitry.polyakov.exceptions.UserNotFoundException;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface BotService {
    void getUpdate(Update update, PersonalVocabularyBot bot);

}
