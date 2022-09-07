package queue;

import domain.Message;

public class QueueMessageFormat {

    private int retryCount;

    private Message payload;

    public QueueMessageFormat(int retryCount, Message payload) {
        this.retryCount = retryCount;
        this.payload = payload;
    }

    public QueueMessageFormat() {
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public Message getPayload() {
        return payload;
    }

    public void setPayload(Message payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "QueueMessageFormat{" +
                "  retryCount = " + retryCount +
                ", payload = " + payload +
                '}';
    }
}
