package at.moritz.commandsystem.javacord.listener;

import at.moritz.commandsystem.javacord.CommandSystemBot;
import at.moritz.commandsystem.javacord.logs.Logger;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

/**
 * @author Morazzer
 * @since Date 25.09.2020 12:59:07
 */
public class CommandListener implements MessageCreateListener {
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        Logger.getLogger().trace("Message getted with content " + messageCreateEvent.getMessage().getContent());

        if (messageCreateEvent.getMessage().getContent().startsWith(CommandSystemBot.getPrefix())) {
            CommandSystemBot.getInstance().getCommandManager().perform(messageCreateEvent.getMessage());
        }
    }
}
