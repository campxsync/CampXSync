package logger.validation;

import logger.exception.ValidationException;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public final class ValidationUtils {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private ValidationUtils() {
        // Prevent instantiation
    }

    public static void notNull(Object obj, String fieldName) {
        if (obj == null) {
            throw new ValidationException(fieldName + " must not be null");
        }
    }

    public static void notEmpty(String str, String fieldName) {
        if (str == null || str.trim().isEmpty()) {
            throw new ValidationException(fieldName + " must not be null or empty");
        }
    }

    public static void notEmpty(Collection<?> col, String fieldName) {
        if (col == null || col.isEmpty()) {
            throw new ValidationException(fieldName + " must not be null or empty");
        }
    }

    public static void notEmpty(Map<?, ?> map, String fieldName) {
        if (map == null || map.isEmpty()) {
            throw new ValidationException(fieldName + " must not be null or empty");
        }
    }

    public static void validEmail(String email, String fieldName) {
        notEmpty(email, fieldName);
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(fieldName + " must be a valid email address");
        }
    }

    public static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new ValidationException(message);
        }
    }

    public static void notLessThan(long value, long min, String fieldName) {
        if (value < min) {
            throw new ValidationException(fieldName + " must not be less than " + min);
        }
    }
}
