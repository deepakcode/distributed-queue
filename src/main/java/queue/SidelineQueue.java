package queue;

import domain.Message;
import org.codehaus.jackson.map.ObjectMapper;
import subscriber.SidelineSubscriber;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SidelineQueue {

    private final BlockingQueue<String> sidelineQueue;
    private final ObjectMapper objectMapper;
    private final List<SidelineSubscriber> sidelineConsumerList;

    public SidelineQueue(int queueCapacity) {
        sidelineQueue = new ArrayBlockingQueue<>(queueCapacity);
        sidelineConsumerList= new ArrayList<>();
        objectMapper = new ObjectMapper();
        System.out.println("Initial Capacity of SidelineQueue is  - " + sidelineQueue.remainingCapacity());
    }

    public void addSubscriber(SidelineSubscriber sidelineConsumer){
        sidelineConsumerList.add(sidelineConsumer);
    }

    public void addMessage(Message message) {
        try {
            String queueMessage = getJsonMessage(message);
            sidelineQueue.put(queueMessage);
            System.out.println("Message " + queueMessage + " added to the SidelineQueue, size: " + sidelineQueue.size());
            startConsumerThread();
        } catch (InterruptedException | IOException e) {
            System.err.println(e.getMessage());
            System.err.println("Exception occurred during addition of message with messageName -" + message.getMessageId() + " to queue, discarding message");
        }
    }

    private String getJsonMessage(Message message) throws IOException {
        QueueMessageFormat queueMessageFormat = new QueueMessageFormat(0, message);
        return objectMapper.writeValueAsString(queueMessageFormat);
    }

    public void startConsumerThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                consume();
            }
        }).start();
    }

    private void consume() {
        for (final SidelineSubscriber subscriber:sidelineConsumerList) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String jsonQueueMessage = null;
            final QueueMessageFormat queueMessageFormat;
            try {
                jsonQueueMessage = sidelineQueue.take();
                queueMessageFormat = objectMapper.readValue(jsonQueueMessage, QueueMessageFormat.class);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        consume(queueMessageFormat.getPayload(), subscriber);
                    }
                }).start();
            } catch (InterruptedException e) {
                System.err.println("InterruptedException occurred during consume of message from queue so discarding message");
            } catch (IOException e) {
                System.err.println("IOException occurred during conversion of json message " + jsonQueueMessage + " to object so discarding message");
            }
        }
    }

    private void consume(Message message, SidelineSubscriber sidelineSubscriber) {
        synchronized (sidelineSubscriber) {
            try {
                sidelineSubscriber.getClass().getMethod("subscribeMessage", Message.class).invoke(sidelineSubscriber, message);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                System.err.println("Exception occurred while consumption of message from SideLineQueue - " + message.getMessageId() +
                        " Can't find callback consumeMessage for sidelineSubscriber - " + sidelineSubscriber.getId());
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.err.println("Exception occurred during consumption of messageFromQueue for " + sidelineSubscriber.getId() + " for message " + message.getMessageId());
            }
            sidelineSubscriber.notify();
        }
    }
}