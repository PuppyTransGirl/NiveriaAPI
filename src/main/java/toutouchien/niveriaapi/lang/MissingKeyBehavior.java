package toutouchien.niveriaapi.lang;

/**
 * Defines behavior when a translation key is not found.
 */
public enum MissingKeyBehavior {
    /**
     * Return the key itself (default)
     */
    RETURN_KEY,
    /** Return "!key" to make it obvious it's missing */
    RETURN_PLACEHOLDER,
    /** Return empty string */
    RETURN_EMPTY,
    /** Return key and log a warning */
    LOG_WARNING
}
