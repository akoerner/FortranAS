import java.util.logging.*;
import java.io.IOException;

public class FortranASLogger {
    private static final Logger LOGGER = Logger.getLogger(FortranASLogger.class.getName());

    static {
        setupLogger();
    }

    private static void setupLogger() {
        try {
            if (LOGGER.getHandlers().length == 0) {
                FileHandler fileHandler = new FileHandler(System.getProperty("user.dir") + "/FortranAS.log");
                fileHandler.setFormatter(new BriefFormatter());
                LOGGER.addHandler(fileHandler);
                LOGGER.setLevel(Level.ALL);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static class BriefFormatter extends Formatter {
        public BriefFormatter() {
            super();
        }

        @Override
        public String format(final LogRecord record) {
            return record.getMessage();
        }
    }
}

