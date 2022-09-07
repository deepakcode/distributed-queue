package subscriber;

import domain.Message;
import queue.IQueue;

public class Subscriber2 extends Subscriber {

    public Subscriber2(IQueue messageIQueue, String consumerName) {
        super(messageIQueue, consumerName);
    }

    public void consumer_2(Message message) {
        System.out.println("Subscriber " + this.getConsumerName() + " consumed messageId " + message.getMessageId());
    }

}
