package at.xirado.simplejson;

public class ParsingException extends IllegalStateException {
    public ParsingException(String message, Exception cause) {
        super(message, cause);
    }

    public ParsingException(String message) {
        super(message);
    }

    public ParsingException(Exception cause) {
        super(cause);
    }
}
