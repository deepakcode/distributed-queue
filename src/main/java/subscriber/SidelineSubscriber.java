package subscriber;

import domain.Message;
/*
*
*
* */
public class SidelineSubscriber {

    final private String id;

    public SidelineSubscriber(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void subscribeMessage(Message message) {
        System.out.println("Subscriber " + getId() + " consumed messageId " + message.getMessageId());
    }
}
