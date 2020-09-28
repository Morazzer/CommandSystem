package at.moritz.commandsystem.spigot.types.abstracts;

import at.moritz.commandsystem.spigot.logs.Logger;
import at.moritz.commandsystem.spigot.types.annotations.CommandParam;
import at.moritz.commandsystem.spigot.types.annotations.SubCommand;
import at.moritz.commandsystem.spigot.types.interfaces.TypeParser;
import com.sun.tools.javac.Main;
import org.apache.logging.log4j.message.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:24:54
 */
public abstract class CommandExecutor extends Command implements at.moritz.commandsystem.spigot.types.interfaces.Command {

    private final String name;
    private final List<String> aliases;
    private final List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> subCommands;
    private final ConcurrentHashMap<String, at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> subCommandMap;

    public CommandExecutor(@NotNull String name) {
        super(name);
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = new ArrayList<>();
        this.indexSubCommands();
    }

    public CommandExecutor(@NotNull String name, @NotNull String... aliases) {
        super(name);
        super.setAliases(Arrays.asList(aliases));
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = Arrays.asList(aliases);
        this.indexSubCommands();
    }

    public CommandExecutor(@NotNull String name, @NotNull List<String> aliases) {
        super(name);
        super.setAliases(aliases);
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = aliases;
        this.indexSubCommands();
    }

    private void indexSubCommands() {
        Arrays.asList(getClass().getMethods()).forEach(method -> {
            if (Arrays.stream(method.getAnnotations()).map(Annotation::annotationType).collect(Collectors.toList()).contains(SubCommand.class)) {
                this.subCommands.add(new at.moritz.commandsystem.spigot.types.dataclasses.SubCommand(method, this));
            }
        });
    }

    public final void addTypeParser(List<TypeParser> typeParsers) {
        subCommands.forEach(subCommand -> {
            this.subCommandMap.put(subCommand.getCommandPath(), subCommand);
            subCommand.addTypeParsers(typeParsers);
        });
    }

    @Override
    public @NotNull String getName() {
        return this.name;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return this.aliases;
    }

    @Override
    public final @NotNull List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {


        return true;
    }

    @Override
    public final @NotNull List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> getSubCommand(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        StringBuilder stringBuilder = new StringBuilder();
        Arrays.asList(args).forEach(stringBuilder::append);
        String content = stringBuilder.toString();

        AtomicReference<List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand>> atomicReference = new AtomicReference<>(new ArrayList<>());
        this.subCommands.forEach(subCommand -> {
            if (Arrays.stream(subCommand.getMethod().getAnnotations()).filter(annotation -> annotation instanceof CommandParam).count() > content.split(" ").length)
                return;

            if (content.split(" ").length < subCommand.getCommandPath().split(" ").length)
                return;

            if (subCommand.getCommandPath().startsWith("{") && subCommand.getCommandPath().endsWith("}")) {
                List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                subCommands.add(subCommand);
                atomicReference.set(subCommands);
            } else if (content.startsWith(subCommand.getCommandPath())) {
                List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                subCommands.add(subCommand);
                atomicReference.set(subCommands);
            } else if (subCommand.getCommandPath().contains(" ") && subCommand.getCommandPath().contains("{") && subCommand.getCommandPath().contains("}")) {
                AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                for (int i = 0; i < subCommand.getCommandPath().split(" ").length; i++) {
                    if (!subCommand.getCommandPath().split(" ")[i].startsWith("{") && !subCommand.getCommandPath().split(" ")[i].endsWith("}")) {
                        Logger.getLogger().trace("SubCommand entry: " + subCommand.getCommandPath().split(" ")[i]);
                        if (atomicBoolean.get())
                            atomicBoolean.set(subCommand.getCommandPath().split(" ")[i].equalsIgnoreCase(content.split(" ")[i]));
                    }
                }
                if (atomicBoolean.get()) {
                    List<at.moritz.commandsystem.spigot.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                    subCommands.add(subCommand);
                    atomicReference.set(subCommands);
                }
            }
        });
        Logger.getLogger().trace("Sub command found Path: (" +
                atomicReference.get().stream().map(at.moritz.commandsystem.spigot.types.dataclasses.SubCommand::getCommandPathWithSpaceAnd).collect(Collectors.joining()) + ")");
        return atomicReference.get();
    }
}
