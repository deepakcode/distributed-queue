package subscriber;

import domain.Message;
import queue.IQueue;

public class Subscriber3 extends Subscriber {

    public Subscriber3(IQueue messageIQueue, String consumerName) {
        super(messageIQueue, consumerName);
    }

    public void consume_3(Message message) {
        //Subscriber A consumed messageId abc
        System.out.println("Subscriber " + this.getConsumerName() + " consumed messageId " + message.getMessageId());
    }

}
