package exception;

public class InvalidMessageException extends Throwable {
    public InvalidMessageException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
