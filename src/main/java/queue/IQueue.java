package queue;

import subscriber.SidelineSubscriber;
import subscriber.Subscriber;
import domain.Message;
import exception.ConsumerSubscribedException;
import exception.DependentConsumersNotValid;

public interface IQueue {
    void setQueueCapacity(int size);
    int getRemainingQueueCapacity();
    int getQueueSize();
    void addMessage(Message message);
    void subscribe(Subscriber subscriber, String messageName, String callbackMethod, Subscriber... dependentSubscribers) throws DependentConsumersNotValid, ConsumerSubscribedException;
    void subscribeSideline(SidelineSubscriber sidelineSubscriber);
}
