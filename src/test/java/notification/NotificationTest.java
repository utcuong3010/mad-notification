package notification;

import java.io.Serializable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

import org.apache.commons.lang3.StringUtils;
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

import com.directv.apg.mad.notification.client.jms.Notification;
import com.directv.apg.mad.notification.client.jms.NotificationType;
import com.directv.apg.mad.notification.test.util.EmbeddedHornetQServer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:jmsTestContext.xml" })
public class NotificationTest {

    /** The logger. */
    private static final Logger LOG = LoggerFactory.getLogger(NotificationTest.class);

    /** The JMS server. */
    private static EmbeddedHornetQServer server;

    /** The object to test on */
    @Autowired
    private Notification notification;
    
    
  //variables 
  	private String actualText;
  	private User actualObject;
  	private final User messageObject = new User("test", 10);
  	private final String messageText = "this is a text message";
    
    
	@BeforeClass
	public static void initServer() throws Exception{
		server = new EmbeddedHornetQServer();
		server.start();		
		Thread.sleep(1000);
		LOG.info("[Server]-started sucessully");
	}
	
   
    @Before
    public void prepare() throws Exception{
    	//variables 
    	actualText = "";
    	actualObject = null;
    }
    /**
     * Test for normal case: server start and try to send a messages and receive messages
     */
    @Test
    public void sendReceiveTextMsgTest() throws InterruptedException {
      
        try {
            notification.receive(NotificationType.POSTER_UPDATE_EVENT,new MessageListener() {
                @Override
                public void onMessage(Message msg) {
                    if (msg instanceof TextMessage) {
                        try {
                            actualText = ((TextMessage) msg).getText();
                            LOG.info("Receive text message: '{}'", actualText);
                            
                        } catch (JMSException e) {
                            LOG.error(e.getMessage(), e);
                        }
                    }                    
                }
            });
                       
            notification.send(NotificationType.POSTER_UPDATE_EVENT, messageText);
            Thread.sleep(2000);
            
            Assert.assertEquals("testSendReceiveTextMsg:", messageText, actualText);
                                               
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
    
    @Test
    public void sendReceiveSerializableObjMsgTest() throws InterruptedException {
      
        try {
            notification.receive(NotificationType.POSTER_UPDATE_EVENT,new MessageListener() {
                @Override
                public void onMessage(Message message) {            				
					try {										
						if(message instanceof ObjectMessage) {
							Object obj = ((ObjectMessage)message).getObject();
							if(obj instanceof User) {
								
								actualObject = (User)obj;								
								
							}
						}
					} catch (JMSException e) {					
						e.printStackTrace();
					}									             
                }
            });
                       
            notification.send(NotificationType.POSTER_UPDATE_EVENT, messageObject);
            Thread.sleep(10000);
            
            Assert.assertEquals("testSendReceiveTextMsg:", messageObject, actualObject);
                                               
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
 

    /**
     * Scenario 1: JMS Server stop then register a receive a message listener and when server start again we want
     * consumer still able to receive messages
     */
    @Test
    public void testRetryingWithScenarioOne() throws Exception {
        server.stop();
        Thread.sleep(2000); // Normally, server take 1s to stop completely.     
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    notification.receive(NotificationType.POSTER_UPDATE_EVENT,
                        new MessageListener() {
                            @Override
                            public void onMessage(Message msg) {
                                if (msg instanceof TextMessage) {
                                    try {
                                        actualText = ((TextMessage) msg).getText();
                                        LOG.info("Receive text message: '{}'", actualText);
                                    } catch (JMSException e) {
                                        LOG.error(e.getMessage(), e);
                                    }
                                }
                                                               
                            }
                        });
                } catch (Exception e) {
                    LOG.error(e.getMessage(),e);
                }
            }
        }).start();

        Thread.sleep(500); // Make sure the receiving running when server die.
        server.start();
        Thread.sleep(1000); // Server take 2s to restart
        LOG.info("Send a text message: '{}'", messageText);
        notification.send(NotificationType.POSTER_UPDATE_EVENT, messageText);
        Thread.sleep(1000); // Waiting for sending complete.
       
        Assert.assertEquals("Check string message", messageText, actualText);
 
    }

    /**
     * Scenario 2: register a listener to consumer message while server is alive and then server restart, consumer
     * still be able to receive incoming messages.
     */
    @Test
    public void testRetryingWithScenarioTwo() throws Exception {
       
        notification.receive(NotificationType.POSTER_UPDATE_EVENT, new MessageListener() {

            @Override
            public void onMessage(Message msg) {
                if (msg instanceof TextMessage) {
                    try {
                    	actualText = ((TextMessage) msg).getText();
                        LOG.info("Receive text message: '{}'", messageText);
                    } catch (JMSException e) {
                    	LOG.error(e.getMessage(), e);
                    }
                                        
                }
            }
        });
        Thread.sleep(100);
        server.stop();
        Thread.sleep(1000);// server take about 1s to stop completely

        server.start();
        Thread.sleep(2000); // server take about 2s to start completely

        LOG.info("Send a text message: '{}'", messageText);
        notification.send(NotificationType.POSTER_UPDATE_EVENT, messageText);
        Thread.sleep(1000);  // We want to make sure the sending action complete when server is still alive
        Assert.assertEquals("Check string message", messageText, actualText);
        actualText="";
        server.stop();
        Thread.sleep(1000); // 1s to stop
        server.start();
        Thread.sleep(2000); // 2s to restart
        LOG.info("Send a text message: '{}'", messageText);
        notification.send(NotificationType.POSTER_UPDATE_EVENT, messageText);
        Thread.sleep(1000); // Waiting for sending is complete.
        Assert.assertEquals("Check string message", messageText, actualText); // Check the result
        
    }



    /**
     * Serializable object to send to JMS Server
     */
    public static class User implements Serializable {

        private static final long serialVersionUID = 1L;
        
     
        /** The name  */
        private String name;

        /** The age. */
        private int age;
        
        
        public User(String name, int age) {
        	this.name = name;
        	this.age = age;
		}

        /**
         * Gets the name.
         *
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the name.
         *
         * @param name
         *            the new name
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Gets the age.
         *
         * @return the age
         */
        public int getAge() {
            return age;
        }

        /**
         * Sets the age.
         *
         * @param age
         *            the new age
         */
        public void setAge(int age) {
            this.age = age;
        }

        /*
         * (non-Javadoc)
         *
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "User [name=" + name + ", age=" + age + "]";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + age;
            result = prime * result + ((name == null) ? 0 : name.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
        	if(obj instanceof User) {
	    		 User other = (User) obj;
	             if (this.age != other.getAge())
	                 return false;
	             else if(!StringUtils.equalsIgnoreCase(this.name, other.getName())) {
	            	 return false;
	             } else {
	            	 return true;
	             }                                              
        	} else {
        		return false;
        	}    
        }
    }
}
