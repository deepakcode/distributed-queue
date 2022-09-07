package queue;

import subscriber.Subscriber;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MessageSubscribersInfo implements Cloneable {

    private final Map<Subscriber, String> consumerCallbackMap;
    private final Map<Subscriber, List<Subscriber>> consumerDependentMap;
    private final Set<Subscriber> subscribedSubscribers;

    public MessageSubscribersInfo(Map<Subscriber, String> consumerCallbackMap, Map<Subscriber, List<Subscriber>> consumerDependentMap, Set<Subscriber> subscribedSubscribers) {
        this.consumerCallbackMap = consumerCallbackMap;
        this.consumerDependentMap = consumerDependentMap;
        this.subscribedSubscribers = subscribedSubscribers;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public Map<Subscriber, List<Subscriber>> getConsumerDependencyMap() {
        return consumerDependentMap;
    }

    public Map<Subscriber, String> getConsumerCallbackInfo() {
        return consumerCallbackMap;
    }

    public Set<Subscriber> getSubscribedConsumerInfo() {
        return subscribedSubscribers;
    }

}

