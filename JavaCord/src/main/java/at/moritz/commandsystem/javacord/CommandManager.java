package at.moritz.commandsystem.javacord;

import at.moritz.commandsystem.javacord.logs.Logger;
import at.moritz.commandsystem.javacord.test.TypeParserTest;
import at.moritz.commandsystem.javacord.types.interfaces.Command;
import at.moritz.commandsystem.javacord.types.interfaces.TypeParser;
import org.javacord.api.entity.message.Message;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:40:00
 */
public class CommandManager {

    private List<Command> commands;
    private ConcurrentHashMap<String, List<Command>> commandMap;
    private List<TypeParser> typeParsers;

    public CommandManager() {
        commands = new ArrayList<>();
        this.commandMap = new ConcurrentHashMap<>();
        this.typeParsers = new ArrayList<>();
        this.loadTypeParsers();

        Reflections reflections = new Reflections(getClass().getPackageName());

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(at.moritz.commandsystem.javacord.types.annotations.Command.class)) {
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .filter(constructors -> constructors.getParameterCount() == 0)
                    .findFirst().orElse(null);

            if (constructor != null) {
                try {
                    Object object = constructor.newInstance();

                    if (object instanceof Command) {
                        this.commands.add((Command) object);
                        if (this.commandMap.containsKey(((Command) object).getName())) {
                            List<Command> commands = this.commandMap.remove(((Command) object).getName());
                            commands.add((Command) object);
                            this.commandMap.put(((Command) object).getName(), commands);
                        } else {
                            this.commandMap.put(((Command) object).getName(), Collections.singletonList((Command) object));
                        }
                    }
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        this.commands.forEach(command -> command.addTypeParser(this.typeParsers));
    }

    public void loadTypeParsers() {
        this.typeParsers.addAll(Arrays.asList(
           new TypeParserTest()
        ));
    }

    public void perform(Message message) {
        String content = message.getContent();
        String tempCommand = content.split(" ")[0];

        String command = tempCommand.substring(CommandSystemBot.getPrefix().length());

        Logger.getLogger().info("[Perform Server Command] User: " + message.getAuthor().getId()
                + " Command: " + command);

        if (!commandMap.containsKey(command))
            return;

        commandMap.get(command).stream()
                .filter(commands -> commands.getSubCommand(message) != null)
                .forEach(commands -> commands.getSubCommand(message).forEach(subCommand -> subCommand.perform(message, content.substring(tempCommand.length() + 1))));
    }

    public List<TypeParser> getTypeParsers() {
        return typeParsers;
    }
}
