package queue;

import subscriber.SidelineSubscriber;
import subscriber.Subscriber;
import domain.Message;
import exception.*;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryMessageQueue implements IQueue {

    private static final int RETRY_COUNT = 3;
    private BlockingQueue<String> inMemoryQueue;
    private final SidelineQueue sidelineQueue;

    private final Map<String, MessageSubscribersInfo> messagesConsumerDetails; // messageName:messageSubscribersDetails

    private final ObjectMapper objectMapper;

    public InMemoryMessageQueue(int queueCapacity) {
        objectMapper = new ObjectMapper();
        inMemoryQueue = new ArrayBlockingQueue<>(queueCapacity);
        sidelineQueue = new SidelineQueue(5);
        messagesConsumerDetails = new ConcurrentHashMap<>();
        System.out.println("InMemoryMessageQueue created with total capacity - " + inMemoryQueue.remainingCapacity());
    }

    @Override
    public void setQueueCapacity(int newCapacity) {
        synchronized (this) {
            try {
                checkIfNewCapacityIsValid(newCapacity);
                BlockingQueue<String> newQueue = new ArrayBlockingQueue<>(newCapacity);
                while (!inMemoryQueue.isEmpty()) {
                    newQueue.put(inMemoryQueue.remove());
                }
                inMemoryQueue = newQueue;
                System.out.println("Changed IQueue capacity to " + newCapacity);
            } catch (InterruptedException | QueueResizeValidation e) {
                System.err.println(e.getMessage());
            }
        }
    }

    private void startConsumerThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                consume();
            }
        }).start();
    }

    @Override
    public void addMessage(Message message) {
        try {
            Thread.sleep(1000);
            String queueMessage = getJsonMessageString(message);
            inMemoryQueue.put(queueMessage);
            System.out.println("New message " + queueMessage + " added to the InMemoryMessageQueue, IQueue size: " + inMemoryQueue.size());
            startConsumerThread();
        } catch (InterruptedException | IOException e) {
            System.err.println("Exception occurred during addition of message with messageName -" + message.getMessageId() + " to queue, discarding message");
        }
    }

    @Override
    public int getQueueSize() {
        return inMemoryQueue.size();
    }

    private void consume() {
        String jsonQueueMessage = null;
        QueueMessageFormat queueMessageFormat = null;
        try {

            jsonQueueMessage = inMemoryQueue.take();

            queueMessageFormat = objectMapper.readValue(jsonQueueMessage, QueueMessageFormat.class);

            checkIfMessageIsValid(queueMessageFormat);

            final String messageId = queueMessageFormat.getPayload().getMessageId();
            final Message message = queueMessageFormat.getPayload();

            MessageSubscribersInfo messageSubscribersInfo = messagesConsumerDetails.get(messageId);

            checkIfAnyConsumerHasSubscribedToMessage(messageId, messageSubscribersInfo);
            final MessageSubscribersInfo messageSubscriberClone = (MessageSubscribersInfo) messageSubscribersInfo.clone();
            for (Map.Entry<Subscriber, String> subscribedConsumer : messageSubscriberClone.getConsumerCallbackInfo().entrySet()) {
                final Subscriber subscriber = subscribedConsumer.getKey();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        consume(message, messageId, subscriber, messageSubscriberClone);
                    }
                }).start();
            }
        } catch (InterruptedException e) {
            System.err.println("InterruptedException occurred during consumption of message from queue, discarding message");
        } catch (SubscriberNotFoundException e) {
            System.err.println(e.getMessage());
            retryMessage(queueMessageFormat);
        } catch (IOException e) {
            System.err.println("IOException occurred during conversion of json message " + jsonQueueMessage + " to Java object, discarding message");
        } catch (InvalidMessageException e) {
            System.err.println(e.getMessage());
        } catch (CloneNotSupportedException e) {
            System.err.println("Exception occurred during consumption of message from queue, discarding message");
        }
    }
    private void checkIfNewCapacityIsValid(int newCapacity) throws QueueResizeValidation {
        if (newCapacity < inMemoryQueue.size()) {
            throw new QueueResizeValidation("Cannot update queue size to - " + newCapacity + " as it is less than current queue size - " + inMemoryQueue.size());
        }
    }

    @Override
    public int getRemainingQueueCapacity() {
        return inMemoryQueue.remainingCapacity();
    }
    private void consume(Message message, String messageName, Subscriber subscriber, MessageSubscribersInfo messageSubscribersInfo) {
        Set<Subscriber> subscribedSubscribers = messageSubscribersInfo.getSubscribedConsumerInfo();
        synchronized (subscribedSubscribers) {
            try {
                String callBackMethod = messageSubscribersInfo.getConsumerCallbackInfo().get(subscriber);
                for (Subscriber dependentSubscriber : messageSubscribersInfo.getConsumerDependencyMap().get(subscriber)) {
                    while (subscribedSubscribers.contains(dependentSubscriber)) {
                        subscribedSubscribers.wait();
                    }
                }
                try {
                    subscriber.getClass().getMethod(callBackMethod, Message.class).invoke(subscriber, message);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    System.err.println("Exception occurred while consumption of message from queue " + messageName +
                            " Can't find callback method " + callBackMethod + " for consumer - " + subscriber.getConsumerName());
                } catch (Exception e) {
                    System.err.println("Exception occurred during consumption of messageFromQueue for " + subscriber.getConsumerName() + " for message " + messageName);
                }
            } catch (InterruptedException e) {
                System.err.println("InterruptedException occurred during consumption of messageFromQueue for " + subscriber.getConsumerName() + " for message " + messageName);
            }
            //subscribedSubscribers.remove(subscriber);
            subscribedSubscribers.notifyAll();
        }
    }


    private void checkIfMessageIsValid(QueueMessageFormat queueMessageFormat) throws InvalidMessageException {
        if (queueMessageFormat.getRetryCount() <= 0) {
            sidelineQueue.addMessage(queueMessageFormat.getPayload());
            //Retries over so moving messageId abc to sideline
            throw new InvalidMessageException("Retries exhausted, moving messageId " + queueMessageFormat.getPayload().getMessageId() + " abc to sideline");
        }
    }

    private void retryMessage(QueueMessageFormat queueMessageFormat) {
        queueMessageFormat.setRetryCount(queueMessageFormat.getRetryCount() - 1);
        try {
            String jsonMessage = getJsonMessageString(queueMessageFormat);
            System.out.println("Message processing failed - messageId: " + queueMessageFormat.getPayload().getMessageId() + ", Number of remaining retries " + (queueMessageFormat.getRetryCount() + 1));
            inMemoryQueue.put(jsonMessage);
            startConsumerThread();
        } catch (InterruptedException | IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Exception occurred while addition of message for messageName -" + queueMessageFormat.getPayload().getMessageId() + " to queue so discarding message");
        }
    }

    private void checkIfAnyConsumerHasSubscribedToMessage(String messageId, MessageSubscribersInfo messageSubscribers) throws SubscriberNotFoundException {
        if (messageSubscribers == null || messageSubscribers.getSubscribedConsumerInfo() == null || messageSubscribers.getSubscribedConsumerInfo().size() == 0) {
            throw new SubscriberNotFoundException("No consumer subscribed to message - " + messageId);
        }
    }

    @Override
    public void subscribe(Subscriber subscriber, String messageName, String callbackMethod, Subscriber... dependentSubscribers) throws DependentConsumersNotValid, ConsumerSubscribedException {
        MessageSubscribersInfo messageSubscribers = this.messagesConsumerDetails.get(messageName);
        if (messageSubscribers == null || messageSubscribers.getSubscribedConsumerInfo() == null || messageSubscribers.getSubscribedConsumerInfo().size() == 0) {
            checkIfDependentConsumerListIsEmptyForNewSubscription(subscriber, messageName, dependentSubscribers);
            Set<Subscriber> subscribedSubscribers = new HashSet<>();
            subscribedSubscribers.add(subscriber);
            Map<Subscriber, String> newMessageSubscriber = new ConcurrentHashMap<>();
            newMessageSubscriber.put(subscriber, callbackMethod);
            Map<Subscriber, List<Subscriber>> dependentMessageConsumers = new ConcurrentHashMap<>();
            dependentMessageConsumers.put(subscriber, new LinkedList<Subscriber>());
            messageSubscribers = new MessageSubscribersInfo(newMessageSubscriber, dependentMessageConsumers, subscribedSubscribers);
            this.messagesConsumerDetails.put(messageName, messageSubscribers);
        } else {
            checkIfDependentConsumersAreValidAndSubscribedForExistingMessage(subscriber, messageName, messageSubscribers, dependentSubscribers);
            messageSubscribers.getSubscribedConsumerInfo().add(subscriber);
            messageSubscribers.getConsumerCallbackInfo().put(subscriber, callbackMethod);
            messageSubscribers.getConsumerDependencyMap().put(subscriber, Arrays.asList(dependentSubscribers));
        }
    }

    @Override
    public void subscribeSideline(SidelineSubscriber sidelineSubscriber) {
        sidelineQueue.addSubscriber(sidelineSubscriber);
    }

    private void checkIfDependentConsumerListIsEmptyForNewSubscription(Subscriber subscriber, String messageName, Subscriber[] dependentSubscribers) throws DependentConsumersNotValid {
        if (dependentSubscribers != null && dependentSubscribers.length != 0) {
            throw new DependentConsumersNotValid("Dependent Consumers for Subscriber " + subscriber.getConsumerName() + " have not yet subscribed to message - " + messageName);
        }
    }

    private String getJsonMessageString(QueueMessageFormat queueMessageFormat) throws IOException {
        return objectMapper.writeValueAsString(queueMessageFormat);
    }

    private void checkIfDependentConsumersAreValidAndSubscribedForExistingMessage(Subscriber givenSubscriber, String messageName, MessageSubscribersInfo messageSubscribers, Subscriber[] dependentSubscribers) throws DependentConsumersNotValid {
        Set<Subscriber> subscribedSubscribers = messageSubscribers.getSubscribedConsumerInfo();
        List<Subscriber> dependentSubscriberList = Arrays.asList(dependentSubscribers);
        if (dependentSubscriberList.contains(givenSubscriber)) {
            String exceptionMessage = "Subscription of message failed for Subscriber " + givenSubscriber.getConsumerName() + " for Message " + messageName;
            exceptionMessage += " Dependent consumer is same as given consumer";
            throw new DependentConsumersNotValid(exceptionMessage);
        } else if (!subscribedSubscribers.containsAll(dependentSubscriberList)) {
            String exceptionMessage = "Subscription of message failed for Subscriber " + givenSubscriber.getConsumerName() + " for Message " + messageName;
            exceptionMessage += " Dependent Consumers for Input Subscriber - " + givenSubscriber.getConsumerName() + " have not yet subscribed to message - " + messageName;
            throw new DependentConsumersNotValid(exceptionMessage);
        }
    }

    private String getJsonMessageString(Message message) throws IOException {
        QueueMessageFormat queueMessageFormat = new QueueMessageFormat(RETRY_COUNT, message);
        return objectMapper.writeValueAsString(queueMessageFormat);
    }
}