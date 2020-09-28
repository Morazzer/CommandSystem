package at.moritz.commandsystem.javacord.types.dataclasses;

import at.moritz.commandsystem.javacord.CommandSystemBot;
import at.moritz.commandsystem.javacord.logs.Logger;
import at.moritz.commandsystem.javacord.types.annotations.CommandMessage;
import at.moritz.commandsystem.javacord.types.annotations.CommandName;
import at.moritz.commandsystem.javacord.types.annotations.CommandParam;
import at.moritz.commandsystem.javacord.types.annotations.CommandPathParam;
import at.moritz.commandsystem.javacord.types.interfaces.Command;
import at.moritz.commandsystem.javacord.types.interfaces.TypeParser;
import org.javacord.api.entity.message.Message;
import org.javacord.api.entity.user.User;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Morazzer
 * @since Date 25.09.2020 17:51:52
 */
public class SubCommand {

    private final int parameterCount;
    private final Method method;
    private final Command command;
    private final ConcurrentHashMap<Integer, Parameter> parameterTypes;
    private final String commandPath;
    private final List<TypeParser> typeParsers;

    public SubCommand(Method method, Command command) {
        this.commandPath = Arrays.stream(method.getAnnotations())
                .noneMatch(annotation -> annotation instanceof at.moritz.commandsystem.javacord.types.annotations.SubCommand)
                ? ""
                : Arrays.stream(method.getAnnotations())
                .filter(annotation -> annotation instanceof at.moritz.commandsystem.javacord.types.annotations.SubCommand)
                .map(annotation -> (at.moritz.commandsystem.javacord.types.annotations.SubCommand) annotation)
                .collect(Collectors.toList()).get(0).path();
        this.typeParsers = new ArrayList<>();
        this.command = command;
        this.method = method;
        parameterCount = method.getParameters().length;
        parameterTypes = new ConcurrentHashMap<>();

        for (int i = 0; i < parameterCount; i++) {
            parameterTypes.put(i, method.getParameters()[i]);
        }
    }

    public void addTypeParsers(TypeParser... typeParsers) {
        this.typeParsers.addAll(Arrays.asList(typeParsers));
    }

    public void addTypeParsers(List<TypeParser> typeParsers) {
        this.typeParsers.addAll(typeParsers);
    }

    public Object[] getParameters(String content, Message message) {
        Object[] objects = new Object[parameterCount];
        int commandParamIndex = 0;
        int pathParamIndex = 0;

        for (int i = 0; i < parameterCount; i++) {
            if (parameterTypes.containsKey(i)) {
                Parameter parameter = parameterTypes.get(i);

                Annotation annotation = parameter.getAnnotations()[0];

                if (annotation instanceof CommandMessage) {
                    objects[i] = message;
                } else if (annotation instanceof CommandParam) {
                    CommandParam commandParam = (CommandParam) annotation;

                    if (commandParam.args() != 0) {
                        objects[i] = parseCommandParam(commandParam.args(), content, parameter.getType(), message);
                    } else {
                        objects[i] = parseCommandParam(commandParamIndex, content, parameter.getType(), message);
                        commandParamIndex++;
                    }
                } else if (annotation instanceof CommandName) {
                    objects[i] = command.getName();
                } else if (annotation instanceof CommandPathParam) {
                    CommandPathParam pathParam = (CommandPathParam) annotation;

                    if (pathParam.param().isEmpty()) {
                        objects[i] = parsePathParam(pathParamIndex, content, parameter.getType(), message);
                    } else {
                        objects[i] = parsePathParam(pathParam.param(), content, parameter.getType(), message);
                    }
                }
                if (objects[i] != null)
                    Logger.getLogger().trace("Object " + i + " == " + objects[i] + " type " + objects[i].getClass().getSimpleName());
            }
        }
        return objects;
    }

    private @Nullable Object parsePathParam(String paramName, String content, Class<?> clazz, Message message) {
        int index = 0;

        Logger.getLogger().trace("Path param from {" + paramName + "}");

        for (int i = 0; i < getCommandPath().split(" ").length; i++) {
            if (getCommandPath().split(" ")[i].equalsIgnoreCase("{" + paramName + "}"))
                index = i;
        }

        Logger.getLogger().trace("Path param == " + index);

        return parsePathParam(index, content, clazz, message);
    }

    private @Nullable Object parsePathParam(int index, String content, Class<?> clazz, Message message) {
        String fullPath = message.getContent().substring(CommandSystemBot.getPrefix().length() + message.getContent().split(" ")[0].length());

        Logger.getLogger().trace("Fullpath length: " + fullPath.split(" ").length);

        if (index > fullPath.split(" ").length)
            return null;

        Logger.getLogger().trace("FullPath param: " + fullPath.split(" ")[index]);

        Logger.getLogger().trace("FullPath: " + fullPath);

        fullPath = new StringBuilder(new StringBuilder(fullPath).reverse().substring(parseContent(fullPath).length())).reverse().toString();

        Logger.getLogger().trace("FullPath: " + fullPath);

        String arg = fullPath.split(" ")[index];

        if (clazz.equals(String.class)) {
            return arg;
        } else if (clazz.equals(User.class)) {
            try {
                long userid = Long.parseLong(arg);

                return CommandSystemBot.getInstance().getApi().getUserById(userid).join();
            } catch (NumberFormatException e) {
                if (!arg.startsWith("<@!"))
                    return null;
                if (!arg.endsWith(">"))
                    return null;

                return CommandSystemBot.getInstance().getApi().getUserById(arg.substring(3, arg.length() - 1)).join();
            }
        }

        return null;
    }


    public final void perform(Message message, String content) {
        try {
            this.method.invoke(this.command, this.getParameters(content, message));
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private String parseContent(String content) {
        String parsedContent = content;

        if (this.commandPath.contains(" ") && !this.commandPath.startsWith("{") && !this.commandPath.endsWith("}")) {
            parsedContent = parsedContent.substring(this.commandPath.length()).trim();
        } else {
            if (!this.commandPath.contains(" ")) {
                if (this.commandPath.equalsIgnoreCase(content.split(" ")[0])) {
                    parsedContent = parsedContent.substring(this.commandPath.split(" ")[0].length()).trim();
                } else if (this.commandPath.startsWith("{") && this.commandPath.endsWith("}")) {
                    parsedContent = parsedContent.substring(content.split(" ")[0].length()).trim();
                }
            } else {
                for (int i = 0; i < this.commandPath.split(" ").length && i < content.split(" ").length; i++) {
                    if (this.commandPath.split(" ")[i].equalsIgnoreCase(content.split(" ")[i])) {
                        parsedContent = parsedContent.substring(this.commandPath.split(" ")[i].length()).trim();
                    } else if (this.commandPath.split(" ")[i].startsWith("{") && this.commandPath.split(" ")[i].endsWith("}")) {
                        parsedContent = parsedContent.substring(content.split(" ")[i].length()).trim();
                    }
                }
            }
        }

        Logger.getLogger().trace("Converte " + content + " to " + parsedContent);

        return parsedContent;
    }

    private @Nullable Object parseCommandParam(int index, String content, Class<?> clazz, Message message) {
        Logger.getLogger().trace("Command param index == " + index);

        if (index > content.split(" ").length)
            return null;

        String parseContent = parseContent(content);

        for (TypeParser typeParser : this.typeParsers) {
            Object object = typeParser.parseObject(parseContent.split(" ")[index], clazz, message);
            if (object != null) {
                System.out.println(object.toString());
                return object;
            }
        }

        Logger.getLogger().trace(clazz.getSimpleName() + " " + index + " length " + parseContent.split(" ").length);
        if (clazz.equals(String.class)) {
            return parseContent.split(" ")[index];
        } else if (clazz.equals(User.class)) {
            String userArg = parseContent.split(" ")[index];

            try {
                long userid = Long.parseLong(userArg);

                return CommandSystemBot.getInstance().getApi().getUserById(userid).join();
            } catch (NumberFormatException e) {
                if (!userArg.startsWith("<@!"))
                    return null;
                if (!userArg.endsWith(">"))
                    return null;

                return CommandSystemBot.getInstance().getApi().getUserById(userArg.substring(3, userArg.length() - 1)).join();
            }
        }

        return null;
    }

    public String getCommandPath() {
        return commandPath;
    }

    public String getCommandPathWithSpace() {
        return commandPath + " ";
    }

    public String getCommandPathWithSpaceAnd() {
        return commandPath + ", ";
    }


    public Method getMethod() {
        return method;
    }
}
