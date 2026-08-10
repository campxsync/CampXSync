package logger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
  Annotation to automatically log method entry, exit, argument values, execution duration (ms),
  and thrown exceptions across Spring components using AspectJ AOP.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface LogExecution {
    boolean logArguments() default true;
    boolean logResult() default true;
}
