package subscriber;

import domain.Message;
import queue.IQueue;

public class Subscriber1 extends Subscriber {

    public Subscriber1(IQueue messageIQueue, String consumerName) {
        super(messageIQueue, consumerName);
    }

    public void consumer_1(Message message) {
        System.out.println("Subscriber " + this.getConsumerName() + " consumed messageId " + message.getMessageId());
    }
}
