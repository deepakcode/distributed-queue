import producer.IProducer;
import producer.MessageProducer;
import queue.IQueue;
import queue.InMemoryMessageQueue;
import subscriber.SidelineSubscriber;
import subscriber.Subscriber2;
import subscriber.Subscriber1;
import domain.Message;

public class Test1 {

    public static void main(String[] args) throws InterruptedException {

        IQueue inMemoryQueue = new InMemoryMessageQueue(3);
        IProducer messageIProducer = new MessageProducer(inMemoryQueue);

        Subscriber1 consumer1 = new Subscriber1(inMemoryQueue, "consumer_1");
        consumer1.subscribe("m1", "consumer_1");

        messageIProducer.produce(new Message("m1", "200"));
        messageIProducer.produce(new Message("m1", "201"));
        messageIProducer.produce(new Message("m1", "202"));

        Thread.sleep(100);

     /*




        SidelineSubscriber  sidelineSubscriber1 = new SidelineSubscriber("sc1");
        inMemoryQueue.subscribeSideline(sidelineSubscriber1);
        SidelineSubscriber  sidelineSubscriber2 = new SidelineSubscriber("sc2");
        inMemoryQueue.subscribeSideline(sidelineSubscriber2);

        //consumer2.subscribe("m1","consumer_2");
        for(int i=1; i<=4; i++)
            messageIProducer.produce(new Message("m"+i, "200"));
        Thread.sleep(100);*/
    }
}