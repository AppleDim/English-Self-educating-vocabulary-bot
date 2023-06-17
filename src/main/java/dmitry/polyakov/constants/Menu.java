package dmitry.polyakov.constants;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

import static dmitry.polyakov.utils.LanguageLocalisation.messages;

public class Menu {

    public static List<BotCommand> addBotCommands() {
        return List.of(
                new BotCommand("/start", messages.getString("menu.start_bot")),
                new BotCommand("/help", messages.getString("menu.helping_page")),
                new BotCommand("/language", messages.getString("menu.change_language"))
        );
    }
}
