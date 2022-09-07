import producer.IProducer;
import queue.InMemoryMessageQueue;
import subscriber.*;
import subscriber.Subscriber3;
import subscriber.Subscriber1;
import domain.Message;
import producer.MessageProducer;
import queue.IQueue;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        IQueue IQueue = new InMemoryMessageQueue(2);
        IProducer messageIProducer = new MessageProducer(IQueue);
        Subscriber1 consumer1 = new Subscriber1(IQueue, "consumer_1");
        Subscriber2 consumer2 = new Subscriber2(IQueue, "consumer_2");
        Subscriber3 consumer3 = new Subscriber3(IQueue, "consumer_3");
        Subscriber4 consumer4 = new Subscriber4(IQueue, "consumer_4");

        consumer3.subscribe("m1", "consume_3");
        consumer1.subscribe("m1", "consume_1", consumer3);
        consumer2.subscribe("m1", "consume_2", consumer3);
        consumer4.subscribe("m1", "consume_4", consumer1, consumer2, consumer3);

        messageIProducer.produce(new Message("m1", "200"));
        messageIProducer.produce(new Message("m2", "200"));
        messageIProducer.produce(new Message("m3", "200"));
        messageIProducer.produce(new Message("m4", "200"));
        Thread.sleep(100);
    }
}