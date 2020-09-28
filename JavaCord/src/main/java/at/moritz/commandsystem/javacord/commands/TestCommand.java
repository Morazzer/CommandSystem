package at.moritz.commandsystem.javacord.commands;

import at.moritz.commandsystem.javacord.types.abstracts.CommandExecutor;
import at.moritz.commandsystem.javacord.types.annotations.*;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;

/**
 * @author Morazzer
 * @since Date 25.09.2020 16:59:24
 */
@Command
public class TestCommand extends CommandExecutor {

    public TestCommand() {
        super("test");
    }

    @DefaultCommand
    public void test(@CommandParam Message message) {

    }
    @SubCommand(path = "{name} {test}")
    public void idk(@CommandMessage Message message, @CommandPathParam(param = "test") String pathParam, @CommandParam TextChannel textChannel) {
        message.getChannel().sendMessage(pathParam);
        textChannel.sendMessage(message.getAuthor().toString());
    }

    @SubCommand(path = "{}")
    public void abc(@CommandName String name, @CommandMessage Message message) {
        message.getChannel().sendMessage("ka");
    }

    @SubCommand(path = "test")
    public void test(@CommandName String name, @CommandParam(args = 2) String args0, @CommandParam User user, @CommandMessage Message message) {
        message.getChannel().sendMessage("test");
    }

    @SubCommand(path = "ka {test} a")
    public void testMessage(@CommandMessage Message message, @CommandPathParam(param = "test") String path) {
        message.getChannel().sendMessage(message.getAuthor().getDiscriminatedName() + "  " + path);
    }
}
