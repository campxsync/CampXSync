package logger.logging;

import logger.annotation.LogExecution;
import logger.annotation.Sensitive;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * Aspect for intercepting methods annotated with @LogExecution or within classes annotated with @LogExecution.
 * Logs method entry, arguments, exit, execution duration, and exceptions automatically using AppLogger.
 *
 * PII Protection: Parameters annotated with @Sensitive are masked as "[REDACTED]" in log output.
 * Use @Sensitive on any parameter containing email addresses, passwords, tokens, or other PII.
 */
@Aspect
public class MethodLoggingAspect {

    private static final String REDACTED = "[REDACTED]";

    @Around("@annotation(logger.annotation.LogExecution) || @within(logger.annotation.LogExecution)")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = joinPoint.getTarget().getClass();
        AppLogger log = AppLogger.getLogger(targetClass);

        LogExecution annotation = method.getAnnotation(LogExecution.class);
        if (annotation == null) {
            annotation = targetClass.getAnnotation(LogExecution.class);
        }

        String methodName = signature.getName();
        Object[] args = joinPoint.getArgs();

        boolean logArgs = annotation == null || annotation.logArguments();
        boolean logResult = annotation == null || annotation.logResult();

        if (logArgs && args != null && args.length > 0) {
            Object[] maskedArgs = maskSensitiveArgs(method, args);
            log.info("--> ENTRY Method: {}() | Args: {}", methodName, java.util.Arrays.toString(maskedArgs));
        } else {
            log.info("--> ENTRY Method: {}()", methodName);
        }

        long startTime = System.currentTimeMillis();
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (logResult && method.getReturnType() != void.class) {
                log.info("<-- EXIT Method: {}() | Duration: {}ms | Return: {}", methodName, duration, result);
            } else {
                log.info("<-- EXIT Method: {}() | Duration: {}ms", methodName, duration);
            }
            return result;
        } catch (Throwable t) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("<-- EXCEPTION Method: {}() | Duration: {}ms | Error: {}", methodName, duration, t.getMessage());
            throw t;
        }
    }

    /**
     * Returns a copy of the args array with sensitive parameters replaced by "[REDACTED]".
     * A parameter is considered sensitive if it is annotated with @Sensitive.
     */
    private Object[] maskSensitiveArgs(Method method, Object[] args) {
        Parameter[] parameters = method.getParameters();
        Object[] masked = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            if (i < parameters.length && parameters[i].isAnnotationPresent(Sensitive.class)) {
                masked[i] = REDACTED;
            } else {
                masked[i] = args[i];
            }
        }
        return masked;
    }
}
