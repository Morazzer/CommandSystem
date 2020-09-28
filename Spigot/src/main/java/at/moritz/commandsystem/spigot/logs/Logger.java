package at.moritz.commandsystem.spigot.logs;

/**
 * @author Morazzer
 * @since Date 25.09.2020 12:27:14
 */
public class Logger {

    private static final org.apache.logging.log4j.Logger logger = LoggerUtils.getLogger(Logger.class);



    public static org.apache.logging.log4j.Logger getLogger() {
        return logger;
    }
}
