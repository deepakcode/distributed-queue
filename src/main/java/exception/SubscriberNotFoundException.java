package exception;

public class SubscriberNotFoundException extends Throwable {
    public SubscriberNotFoundException(String exceptionMessage) {
        super(exceptionMessage);
    }
}
