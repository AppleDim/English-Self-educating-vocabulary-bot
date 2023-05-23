package dmitry.polyakov.services;

import dmitry.polyakov.bot.PersonalVocabularyBot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface BotService {
    void getUpdate(Update update, PersonalVocabularyBot bot);

}
