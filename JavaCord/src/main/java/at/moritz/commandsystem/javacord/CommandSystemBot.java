package at.moritz.commandsystem.javacord;

import at.moritz.commandsystem.javacord.listener.CommandListener;
import at.moritz.commandsystem.javacord.logs.Logger;
import org.javacord.api.AccountType;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;

import java.util.Scanner;

/**
 * Discord Bot Template without shards
 *
 * @author Morazzer
 */
public class CommandSystemBot {

    private static CommandSystemBot instance;
    private Logger logger;
    private static String prefix = "!";
    private boolean traceEnabled, debugEnabled;
    private final DiscordApi api;
    private CommandManager commandManager;

    public static void main(String[] args) {
        new CommandSystemBot();
    }

    public CommandSystemBot() {
        instance = this;
        traceEnabled = true;
        debugEnabled = true;
        logger = new Logger();

        this.api = new DiscordApiBuilder()
                .setToken("NzM1NjgwOTExNDAzOTA5MTgw.XxjyPQ.Cw2NwQLSPJlGFPEDxmxBWYtL10k")
                .setAccountType(AccountType.BOT)
                .setWaitForServersOnStartup(true)
                .login().join();

        this.api.addMessageCreateListener(new CommandListener());

        this.commandManager = new CommandManager();

        consoleListener();

    }

    public void consoleListener() {
        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String line;
            if ((line = scanner.nextLine()) != null) {
                if (line.equalsIgnoreCase("exit")) {
                    this.api.disconnect();
                    Logger.getLogger().info("Shutdown");
                    System.exit(0);
                }
            }
        }).start();
    }

    public boolean isDebugEnabled() {
        return debugEnabled;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }
    public static CommandSystemBot getInstance() {
        return instance;
    }

    public DiscordApi getApi() {
        return api;
    }

    public static Logger getLogger() {
        return instance.logger;
    }

    public static String getPrefix() {
        return prefix;
    }
}
