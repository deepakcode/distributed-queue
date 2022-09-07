package subscriber;

import exception.ConsumerSubscribedException;
import exception.DependentConsumersNotValid;
import queue.IQueue;

public abstract class Subscriber {

    private final IQueue inMemoryQueue;

    private final String consumerName;

    Subscriber(IQueue inMemoryQueue, String consumerName) {
        this.inMemoryQueue = inMemoryQueue;
        this.consumerName = consumerName;
    }

    public void subscribe(String messageName, String callbackMethod, Subscriber... dependentSubscribers) {
        try {
            inMemoryQueue.subscribe(this, messageName, callbackMethod, dependentSubscribers);
            System.out.println(this.getConsumerName() + " subscribed to message - " + messageName);
            for (Subscriber dependentSubscriber : dependentSubscribers) {
                System.out.println("\tDepends on - " + dependentSubscriber.getConsumerName());
            }
        } catch (DependentConsumersNotValid | ConsumerSubscribedException e) {
            System.err.println(e.getMessage());
        }
    }

    public String getConsumerName() {
        return this.consumerName;
    }
}
