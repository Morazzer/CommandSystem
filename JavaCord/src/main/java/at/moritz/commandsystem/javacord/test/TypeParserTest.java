package at.moritz.commandsystem.javacord.test;

import at.moritz.commandsystem.javacord.types.interfaces.TypeParser;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.channel.TextChannel;
import org.javacord.api.entity.message.Message;

/**
 * @author Morazzer
 * @since Date 27.09.2020 14:27:33
 */
public class TypeParserTest implements TypeParser {
    @Override
    public Object parseObject(String argument, Class<?> clazz, Message message) {
        if (clazz.equals(TextChannel.class)) {
            return message.getChannel();
        } else if (clazz.equals(ServerTextChannel.class)) {
            return message.getServerTextChannel().isPresent() ? message.getServerTextChannel().get() : null;
        }

        return null;
    }
}
