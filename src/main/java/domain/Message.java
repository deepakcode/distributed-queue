package domain;

public class Message {

    private String messageId;
    private String httpCode;

    public Message() {
    }

    public Message(String messageId, String httpCode) {
        this.messageId = messageId;
        this.httpCode = httpCode;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(String httpCode) {
        this.httpCode = httpCode;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                "httpCode='" + httpCode + '\'' +
                '}';
    }
}
