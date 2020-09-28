package at.moritz.commandsystem.javacord.types.interfaces;

import at.moritz.commandsystem.javacord.types.dataclasses.SubCommand;
import org.javacord.api.entity.message.Message;

import java.util.List;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:24:15
 */
public interface Command {

    public String getName();
    public List<String> getAliases();
    public List<SubCommand> getSubCommands();
    public List<SubCommand> getSubCommand(Message message);
    public void addTypeParser(List<TypeParser> typeParsers);


}
