package logger.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {
    private final Logger logger;

    private AppLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    private AppLogger(String name) {
        this.logger = LoggerFactory.getLogger(name);
    }

    public static AppLogger getLogger(Class<?> clazz) {
        return new AppLogger(clazz);
    }

    public static AppLogger getLogger(String name) {
        return new AppLogger(name);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public void trace(String msg) {
        logger.trace(msg);
    }

    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }
}
