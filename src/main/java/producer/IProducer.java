package producer;

import domain.Message;

public interface IProducer extends Runnable {
    void produce();
    void produce(Message message);
}
