package toutouchien.niveriaapi.database.exception;

/**
 * Exception thrown when there is an error during the generation of a default document in the database.
 */
public class DefaultDocumentGenerationException extends RuntimeException {
    /**
     * Constructs a new DefaultDocumentGenerationException with the specified detail message.
     *
     * @param message the detail message
     * @param cause   the cause of the exception
     */
    public DefaultDocumentGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}