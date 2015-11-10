package notification;

import notification.NotificationTest.User;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.directv.apg.mad.notification.client.jms.Notification;
import com.directv.apg.mad.notification.client.jms.NotificationType;

public class ProducerTest {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		ApplicationContext ctx = new ClassPathXmlApplicationContext("jmsTestContext.xml");
		
		Notification notification = ctx.getBean(Notification.class);
		int count = 0;
		
		User user = new User("this", 20);
		
		while(true) {
			notification.send(NotificationType.POSTER_UPDATE_EVENT, "dddddddddddd");
			
			Thread.sleep(2000);
		}

	}

}
