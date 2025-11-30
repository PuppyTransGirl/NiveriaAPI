package toutouchien.niveriaapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated code/element can/will break on minecraft updates or other external changes.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Shivery {
    String reason() default "";
}
