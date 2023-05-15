package dmitry.polyakov.constants;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public class Menu {

    public static List<BotCommand> addBotCommands() {
        return List.of(
                new BotCommand("/start", "menu_start"),
                new BotCommand("/help", "menu_help"),
                new BotCommand("/dictionary", "menu_dictionary"),
                new BotCommand("/settings", "menu_settings"),
                new BotCommand("/language", "menu_language"),
                new BotCommand("/write", "menu_write")
        );

    }
}
