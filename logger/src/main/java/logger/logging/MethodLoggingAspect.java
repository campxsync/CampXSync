package logger.logging;

import logger.annotation.LogExecution;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Aspect for intercepting methods annotated with @LogExecution or within classes annotated with @LogExecution.
 * Logs method entry, arguments, exit, execution duration, and exceptions automatically using AppLogger.
 */
@Aspect
public class MethodLoggingAspect {

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
            log.info("--> ENTRY Method: {}() | Args: {}", methodName, Arrays.toString(args));
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
}
