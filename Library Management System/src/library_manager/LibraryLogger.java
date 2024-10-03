package library_manager;
import java.io.IOException;
import java.util.logging.*;

public class LibraryLogger
{

    private static final Logger logger = Logger.getLogger(LibraryLogger.class.getName());

    static
    {
        try
        {
            // Setup file handler to write to the log file
            FileHandler fileHandler = new FileHandler("library_system.log", true);  // Append to log file
            fileHandler.setFormatter(new SimpleFormatter());

            // Remove default console handler
            logger.setUseParentHandlers(false);  // Disable parent handlers (console logging)

            // Add only the file handler to the logger
            logger.addHandler(fileHandler);

            // Set logging level
            logger.setLevel(Level.ALL);

        }
        catch (IOException e)
        {
            logger.log(Level.SEVERE, "Failed to initialize log handler", e);
        }
    }

    public static Logger getLogger() {
        return logger;
    }
}

