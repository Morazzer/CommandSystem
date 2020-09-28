package at.moritz.commandsystem.spigot.logs;

import at.moritz.commandsystem.javacord.Main;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.simple.SimpleLogger;
import org.apache.logging.log4j.util.PropertiesUtil;

import java.util.Properties;

/**
 * @author Morazzer
 * @since Date 25.09.2020 12:29:33
 */
public class LoggerUtils {

    public static Logger getLogger(String name) {
        Level level = Main.getInstance().isTraceEnabled() ? Level.TRACE : (Main.getInstance().isDebugEnabled() ? Level.DEBUG : Level.INFO);

        return new SimpleLogger(name, level, false, false, true, true, "[yyyy-MM-dd HH:mm:ss]", null, new PropertiesUtil(new Properties()), System.out);
    }

    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getSimpleName());
    }

}
