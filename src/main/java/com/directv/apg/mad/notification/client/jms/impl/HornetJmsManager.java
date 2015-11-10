package com.directv.apg.mad.notification.client.jms.impl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.directv.apg.mad.notification.client.jms.JmsConfig;
import com.directv.apg.mad.notification.client.jms.JmsManager;
import com.directv.apg.mad.notification.client.jms.NotificationType;
import com.directv.apg.mad.notification.client.jms.common.RegisterHandlerMessageInfo;
import com.directv.apg.mad.notification.client.jms.worker.MonitorWorker;

@Component
public class HornetJmsManager implements JmsManager,InitializingBean,DisposableBean {

	
	/*** LOGGER */
	private static final Logger LOG = LoggerFactory.getLogger(HornetJmsManager.class);
	
	
	/***
	 * this status server is running or not
	 */
	private AtomicBoolean isRunning =  new AtomicBoolean(false);
	

	
	/***
	 * the list handlers
	 */
	private Queue<RegisterHandlerMessageInfo> handlers = new ConcurrentLinkedQueue<RegisterHandlerMessageInfo>();
	
	@Autowired
	private JmsConfig jmsConfig;
	
	
	/***
	 * Initial context {@link InitialContext}
	 */
	private Context initialContext;
	
	
	/**
	 * create a connect {@link ConnectionFactory}
	 */
	private ConnectionFactory connectionFactory;
	
	/***
	 * Connection {@link Connection}
	 */
	private Connection connection;
	
	
	

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			initializing();				
		}catch (Exception ex) {
			LOG.error("Initialized Bean error {}", ex);
		}
		//waitching handlers
		new Thread(new MonitorWorker("Messages Handlers", handlers)).start();
		new Thread(new InitialConnectionTask()).start();//maintain the connection
	}
	
	/**
	 * initializing all componment as 
	 * {@link InitialContext}
	 * {@link TopicSession}
	 * {@link Topic}
	 * 
	 */
	private void initializing() throws Exception {
		try {
			//1. get context
			initialContext = getInitialContext();
	
			//2. get Connnection Factory from the context
			connectionFactory = (ConnectionFactory)initialContext.lookup("/ConnectionFactory");
			
			//3. Connection
			connection = connectionFactory.createConnection();
			connection.setExceptionListener(new ConnectionExceptionHandler());
			connection.start();		//active subcribers			
			
			
			//4. binding all handlers
			bindingHandlers();
						
			//4.set server is ready
			isRunning.set(true);	
			
			LOG.info("Initialized Contection to Jms Server");
		} catch (Exception ex) {
			throw new Exception("Can not initializing connect with HornetQ server {}" , ex);
		}		
	}
	
	/***
	 * get context
	 * @return
	 * @throws NamingException
	 */
	private Context getInitialContext() throws NamingException {
		
		Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, jmsConfig.getJmsProviderUrl());
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        props.put(Context.SECURITY_PRINCIPAL, jmsConfig.getUsername());
        props.put(Context.SECURITY_CREDENTIALS, jmsConfig.getUsername());
		
        return new InitialContext(props);
	}

	
	@Override
	public void destroy() throws Exception {
		
		if(connection != null) connection.close();
		if(initialContext != null) initialContext.close();
		isRunning.set(false);		
		//TODO: clear all resource if needed
	}
	
	/***
	 * binding all handlers in the cause reconnect
	 */
	private void bindingHandlers() throws Exception{
		//read handler
		Iterator<RegisterHandlerMessageInfo>its = handlers.iterator();
		while(its.hasNext()) {
			RegisterHandlerMessageInfo handlerInfo = its.next();
			//register again handlers
			bindingHandler(handlerInfo.getNotificationType(), handlerInfo.getListener());			
		}		
	}
	
	
	/***
	 * 
	 * @param topicName
	 * @return
	 * @throws NamingException
	 */
    private Topic lookupTopic(String topicName, Session session) throws Exception {
    	
		Topic topic = null;
		try {
			topic = (Topic) initialContext.lookup("/topic/" + topicName);

		} catch (NamingException ex) {
			topic = session.createTopic(topicName);
		}

		return topic;
    }
	
	/***
	 * binding handler to destination
	 * @param notificationType
	 * @param listener
	 */
	private void bindingHandler(final NotificationType notificationType, final MessageListener listener) throws Exception {
		
		MessageConsumer consumer = null;
		Topic topic = null;
		Session session =  null;
		try {
			//4. create session
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			topic = lookupTopic(notificationType.getTopicName(),session);			
			consumer = session.createConsumer(topic);
			consumer.setMessageListener(listener);							
		} catch (Exception e) {					
			throw new Exception("Can not add listener handler exeption {}" + e);			
		} 			
		
	}
	/***
	 * retrying connection
	 */
	private void retryConnectServer() {
		try {
			//1.destroy all sessions or connections
			destroy();
		
			//2. initializing
			initializing();			
			
			LOG.info("Retry connect to server successfully");			
		} catch(Exception ex) {
			LOG.error("ReTry connect to server failed with error {}" + ex);
		}
	}


	@Override
	public boolean send(NotificationType notificationType, String message) {
		boolean isSuccessed = false;
		if(isRunning.get()) {//ready to process
			//look up topic
			MessageProducer producer =  null;
			Topic topic = null;
			Session session =  null;
			try {
				//create session
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				topic = lookupTopic(notificationType.getTopicName(),session);					
				producer = session.createProducer(topic);
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
				TextMessage textMessage = session.createTextMessage(message);
				producer.send(textMessage);
				LOG.info("DONE sending message={}", message);
				isSuccessed =  true;
			} catch (Exception e) {					
				LOG.error("Can not lookup the topic with exeption" + e);				
			}
			
		}		
		return isSuccessed;
	}
	
	@Override
	public boolean send(NotificationType notificationType, Serializable obj) {
		boolean isSuccessed = false;
		if(isRunning.get()) {//ready to process
			//look up topic
			MessageProducer producer =  null;
			Topic topic = null;
			Session session =  null;
			try {
				//create session
				session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
				topic = lookupTopic(notificationType.getTopicName(),session);						
				producer = session.createProducer(topic);
				producer.setDeliveryMode(DeliveryMode.PERSISTENT);
				ObjectMessage objectMessage = session.createObjectMessage(obj);
				producer.send(objectMessage);				
				LOG.info("Done sending object obj={}",ReflectionToStringBuilder.toString(obj));
				isSuccessed =  true;
			} catch (Exception e) {					
				LOG.error("Can not lookup the topic with exeption" + e);				
			}
			
		}		
		return isSuccessed;
	}
	
		
	@Override
	public boolean registerMessageListener(final NotificationType notificationType, final MessageListener listener) {
		boolean isSuccessed = false;		
		try {
			//save handlers
			RegisterHandlerMessageInfo handlerInfo = new RegisterHandlerMessageInfo();
			handlerInfo.setListener(listener);
			handlerInfo.setNotificationType(notificationType);
			handlers.add(handlerInfo);
			
			//binding listener
			bindingHandler(notificationType, listener);
			
			LOG.info("Binded listeners on the topic successfully");
			isSuccessed =  true;
		} catch (Exception e) {					
			LOG.error("Can not add listener handler exeption {}" + e);			
		} 			
		return isSuccessed;
	}
	
	/***
	 * THis class handle the exception connection
	 * 
	 */
	private class ConnectionExceptionHandler implements ExceptionListener {
		@Override
		public void onException(JMSException exception) {	
			LOG.info("The connection hasn't disconnected,Please make the connection alive");
			isRunning.set(false);
		}
	}
	/***
	 * 
	 * this class is responsible for initial and maintain the connection
	 *
	 */
	private class InitialConnectionTask implements Runnable {
		@Override
		public void run() {
			while(true) {
				try {	
					if(!isRunning.get()) {//reconnect
						LOG.info("Running Task is retrying the connection");
						retryConnectServer();
					}
					//sleep
					Thread.sleep(1000);										           				
				} catch (Exception ex) {			
					LOG.error("The maintain connection task has error {}", ex);				
				}
			}			
		}
	}	
	
}
