package at.moritz.commandsystem.spigot;

import at.moritz.commandsystem.spigot.types.abstracts.CommandExecutor;
import at.moritz.commandsystem.spigot.types.interfaces.Command;
import at.moritz.commandsystem.spigot.types.interfaces.TypeParser;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:40:00
 */
public class CommandManager {

    private List<Command> commands;
    private List<TypeParser> typeParsers;

    public CommandManager() {

        loadTypeParsers();

        Reflections reflections = new Reflections(getClass().getPackageName());

        for (Class<?> clazz : reflections.getTypesAnnotatedWith(at.moritz.commandsystem.spigot.types.annotations.Command.class)) {
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .filter(constructors -> constructors.getParameterCount() == 0)
                    .findFirst().orElse(null);

            if (constructor != null) {
                try {
                    Object object = constructor.newInstance();

                    if (object instanceof CommandExecutor) {
                        this.commands.add((Command) object);
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

        ));
    }
}
