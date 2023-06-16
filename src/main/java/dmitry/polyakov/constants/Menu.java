package dmitry.polyakov.constants;

import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.List;

public class Menu {

    public static List<BotCommand> addBotCommands() {
        return List.of(
                new BotCommand("/start", "Initiate the bot commence"),
                new BotCommand("/help", "Get the helping page"),
                new BotCommand("/language", "Change a language")
        );
    }
}
