package at.moritz.commandsystem.javacord.types.abstracts;

import at.moritz.commandsystem.javacord.CommandSystemBot;
import at.moritz.commandsystem.javacord.logs.Logger;
import at.moritz.commandsystem.javacord.types.annotations.CommandParam;
import at.moritz.commandsystem.javacord.types.annotations.SubCommand;
import at.moritz.commandsystem.javacord.types.interfaces.Command;
import at.moritz.commandsystem.javacord.types.interfaces.TypeParser;
import org.javacord.api.entity.message.Message;
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
public abstract class CommandExecutor implements Command {

    private final String name;
    private final List<String> aliases;
    private final List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> subCommands;
    private final ConcurrentHashMap<String, at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> subCommandMap;

    public CommandExecutor(@NotNull String name) {
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = new ArrayList<>();
        this.indexSubCommands();
    }

    public CommandExecutor(@NotNull String name, @NotNull String... aliases) {
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = Arrays.asList(aliases);
        this.indexSubCommands();
    }

    public CommandExecutor(@NotNull String name, @NotNull List<String> aliases) {
        this.subCommandMap = new ConcurrentHashMap<>();
        this.subCommands = new ArrayList<>();
        this.name = name;
        this.aliases = aliases;
        this.indexSubCommands();
    }

    private void indexSubCommands() {
        Arrays.asList(getClass().getMethods()).forEach(method -> {
            if (Arrays.stream(method.getAnnotations()).map(Annotation::annotationType).collect(Collectors.toList()).contains(SubCommand.class)) {
                this.subCommands.add(new at.moritz.commandsystem.javacord.types.dataclasses.SubCommand(method, this));
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
    public final @NotNull List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> getSubCommands() {
        return this.subCommands;
    }

    @Override
    public final @NotNull List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> getSubCommand(Message message) {
        String contet = message.getContent().substring(CommandSystemBot.getPrefix().length() + this.getName().length() + " ".length());

        AtomicReference<List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand>> atomicReference = new AtomicReference<>(new ArrayList<>());
        this.subCommands.forEach(subCommand -> {
            if (Arrays.stream(subCommand.getMethod().getAnnotations()).filter(annotation -> annotation instanceof CommandParam).count() > contet.split(" ").length)
                return;

            if (contet.split(" ").length < subCommand.getCommandPath().split(" ").length)
                return;

            if (subCommand.getCommandPath().startsWith("{") && subCommand.getCommandPath().endsWith("}")) {
                List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                subCommands.add(subCommand);
                atomicReference.set(subCommands);
            } else if (contet.startsWith(subCommand.getCommandPath())) {
                List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                subCommands.add(subCommand);
                atomicReference.set(subCommands);
            } else if (subCommand.getCommandPath().contains(" ") && subCommand.getCommandPath().contains("{") && subCommand.getCommandPath().contains("}")) {
                AtomicBoolean atomicBoolean = new AtomicBoolean(true);
                for (int i = 0; i < subCommand.getCommandPath().split(" ").length; i++) {
                    if (!subCommand.getCommandPath().split(" ")[i].startsWith("{") && !subCommand.getCommandPath().split(" ")[i].endsWith("}")) {
                        Logger.getLogger().trace("SubCommand entry: " + subCommand.getCommandPath().split(" ")[i]);
                        if (atomicBoolean.get())
                            atomicBoolean.set(subCommand.getCommandPath().split(" ")[i].equalsIgnoreCase(contet.split(" ")[i]));
                    }
                }
                if (atomicBoolean.get()) {
                    List<at.moritz.commandsystem.javacord.types.dataclasses.SubCommand> subCommands = atomicReference.get();
                    subCommands.add(subCommand);
                    atomicReference.set(subCommands);
                }
            }
        });
        Logger.getLogger().trace("Sub command found Path: (" +
                atomicReference.get().stream().map(at.moritz.commandsystem.javacord.types.dataclasses.SubCommand::getCommandPathWithSpaceAnd).collect(Collectors.joining()) + ")");
        return atomicReference.get();
    }
}
