package at.moritz.commandsystem.javacord.logs;

import at.moritz.commandsystem.javacord.CommandSystemBot;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.Properties;

/**
 * @author Morazzer
 * @since Date 25.09.2020 12:29:33
 */
public class LoggerUtils {

    public static SimpleLogger getLogger(String name) {

        Level level = CommandSystemBot.getInstance().isTraceEnabled() ? Level.TRACE : (CommandSystemBot.getInstance().isDebugEnabled() ? Level.DEBUG : Level.INFO);

        return new SimpleLogger(name, level, false, false, true, true, "[yyyy-MM-dd HH:mm:ss]", null, new PropertiesUtil(new Properties()), System.out);
    }

    public static SimpleLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

}
