package logger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as sensitive — its value will be replaced with
 * "[REDACTED]" in all aspect-based logging (e.g. {@code MethodLoggingAspect}).
 *
 * Use this annotation on any parameter that contains:
 * - Passwords or tokens
 * - Email addresses or PII
 * - API keys or secrets
 * - Financial amounts or account numbers
 *
 * Example:
 * <pre>
 * {@code @LogExecution}
 * public void createUser(String institutionId, {@code @Sensitive} CreateUserRequest request) { ... }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
}
