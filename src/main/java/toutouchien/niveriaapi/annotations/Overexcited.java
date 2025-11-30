package toutouchien.niveriaapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated code/element is expensive in terms of resources or performance.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Overexcited {
    String reason() default "";
}
