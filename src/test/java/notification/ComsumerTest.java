package notification;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.directv.apg.mad.notification.client.jms.Notification;
import com.directv.apg.mad.notification.client.jms.NotificationType;

public class ComsumerTest {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("jmsTestContext.xml");
		
		Notification notification = ctx.getBean(Notification.class);
		notification.receive(NotificationType.POSTER_UPDATE_EVENT, new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				// TODO Auto-generated method stub
				
				if(message instanceof TextMessage) {
					try {
						System.err.println("Receive1=" +((TextMessage)message).getText());
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (message instanceof ObjectMessage) {
					System.err.println("object received......");
				}
					
				
			}
		});
		
		
		notification.receive(NotificationType.POSTER_UPDATE_EVENT, new MessageListener() {
			
			@Override
			public void onMessage(Message message) {
				// TODO Auto-generated method stub
				
				if(message instanceof TextMessage) {
					try {
						System.err.println("Receive2=" +((TextMessage)message).getText());
					} catch (JMSException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		});

	}

}
