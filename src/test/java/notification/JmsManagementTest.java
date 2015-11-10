package notification;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import notification.NotificationTest.User;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.directv.apg.mad.notification.client.jms.JmsManager;
import com.directv.apg.mad.notification.client.jms.NotificationType;
import com.directv.apg.mad.notification.test.util.EmbeddedHornetQServer;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jmsTestContext.xml"})
public class JmsManagementTest {
	
	private final static Logger LOG = LoggerFactory.getLogger(JmsManagementTest.class);
	
	@Autowired
	private JmsManager jmsManager;
	
	//variables 
	private String actualText;
	private User actualObject;
	private final User messageObject = new User("test", 10);
	private final String messageText = "this is a text message";
	

	/**
	 * jms server for testing only
	 */
	private static EmbeddedHornetQServer server;
	
	@BeforeClass
	public static void initServer() throws Exception{
		server = new EmbeddedHornetQServer();
		server.start();		
//		Thread.sleep(1000);
		LOG.info("[Server]-started sucessully");
	}
	
	@AfterClass
	public static void destroy() throws InterruptedException {
		server.stop();
//		Thread.sleep(1000);
		LOG.info("[Server]-stopped sucessully");
	}
	
	/**
	 * prepare server for test
	 * @throws Exception 
	 */
	@Before
	public void prepare() throws Exception {
		//init value for testing
		actualText = "";
		actualObject = null;		
	}
	
	
	
	
	
	@Test
	public void sendTextMessageTest() {				
		try {			
			//register notification
			jmsManager.registerMessageListener(NotificationType.POSTER_UPDATE_EVENT,new MessageListener(){
				@Override
				public void onMessage(Message message) {					
					if(message instanceof TextMessage) {						
						try {
							actualText = ((TextMessage)message).getText();
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
										
				}				
			});
			jmsManager.send(NotificationType.POSTER_UPDATE_EVENT, messageText);
			Thread.sleep(1000);
			Assert.assertEquals("sendTextMessageTest:", messageText,actualText);
			
		
		} catch (Exception ex) {
			
		}
	}
	
	@Test
	public void sendObjectMessageTest() {		
		
		try {			
			//register notification
			jmsManager.registerMessageListener(NotificationType.POSTER_UPDATE_EVENT,new MessageListener(){
				@Override
				public void onMessage(Message message) {					
					if(message instanceof ObjectMessage) {						
						try {
							Object obj = ((ObjectMessage)message).getObject();
							if(obj instanceof User) {
								actualObject = (User)obj;
							}
						} catch (JMSException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
										
				}				
			});
			jmsManager.send(NotificationType.POSTER_UPDATE_EVENT, messageObject);
			Thread.sleep(1000);
			Assert.assertEquals("sendObjectMessageTest:", messageObject,actualObject);
			
		
		} catch (Exception ex) {
			
		}
	}
	
	
}
