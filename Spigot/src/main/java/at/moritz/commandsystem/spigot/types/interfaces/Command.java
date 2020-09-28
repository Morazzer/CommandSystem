package at.moritz.commandsystem.spigot.types.interfaces;

import at.moritz.commandsystem.spigot.types.dataclasses.SubCommand;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:24:15
 */
public interface Command {

    public String getName();
    public List<String> getAliases();
    public List<SubCommand> getSubCommands();
    public List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> getSubCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args);
    public void addTypeParser(List<TypeParser> typeParsers);


}
