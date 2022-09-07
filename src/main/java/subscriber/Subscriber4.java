package subscriber;

import domain.Message;
import queue.IQueue;

public class Subscriber4 extends Subscriber {

    public Subscriber4(IQueue messageIQueue, String consumerName) {
        super(messageIQueue, consumerName);
    }

    public void consume_4(Message message) {
        System.out.println("Subscriber " + this.getConsumerName() + " consumed messageId " + message.getMessageId());
    }

}
