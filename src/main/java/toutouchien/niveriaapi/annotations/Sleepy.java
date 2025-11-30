package toutouchien.niveriaapi.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the annotated code/element is slow, inefficient.
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Sleepy {
}
