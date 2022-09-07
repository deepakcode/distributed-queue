package producer;

import domain.Message;
import queue.IQueue;

public class MessageProducer implements IProducer {

    private final IQueue IQueue;

    public MessageProducer(IQueue IQueue) {
        this.IQueue = IQueue;
    }

    public void produce(Message message) {
        IQueue.addMessage(message);
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
    }

    //for testing only
    public void produce() {
        for (int i = 1; i <= 5; i++) {
            String messageId = "m" + i;
            String httpCode = "200";
            Message message = new Message(messageId, httpCode);
            IQueue.addMessage(message);
            try {
                Thread.sleep(i * 20);
            } catch (InterruptedException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        produce();
    }
}
