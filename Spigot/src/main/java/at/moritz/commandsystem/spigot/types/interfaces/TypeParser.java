package at.moritz.commandsystem.spigot.types.interfaces;

import org.javacord.api.entity.message.Message;

/**
 * @author Morazzer
 * @since Date 27.09.2020 14:19:10
 */
public interface TypeParser {

    public Object parseObject(String argument, Class<?> clazz, Message message);

}
