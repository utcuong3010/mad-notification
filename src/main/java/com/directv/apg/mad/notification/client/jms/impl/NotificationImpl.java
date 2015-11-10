package com.directv.apg.mad.notification.client.jms.impl;

import java.io.Serializable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.MessageListener;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.directv.apg.mad.notification.client.jms.JmsManager;
import com.directv.apg.mad.notification.client.jms.Notification;
import com.directv.apg.mad.notification.client.jms.NotificationType;
import com.directv.apg.mad.notification.client.jms.common.MessageInfo;
import com.directv.apg.mad.notification.client.jms.common.ObjectMessageInfo;
import com.directv.apg.mad.notification.client.jms.common.TextMessageInfo;
import com.directv.apg.mad.notification.client.jms.worker.DeliverySendingMessageWorker;
import com.directv.apg.mad.notification.client.jms.worker.MonitorWorker;

/**
 * This is the new version for new implement
 * implement the inteface {@link Notification}
 * @author cuongtv8
 *
 */
@Component
public class NotificationImpl implements Notification,InitializingBean,DisposableBean {	
	
	/***
	 * Contains all messages that will be put into mq server when the server is ready
	 * <code>{@link BlockingQueue}</code>
	 * <code>the implement {@link LinkedBlockingQueue}}</code>
	 */
	private final BlockingQueue<MessageInfo> sendingMessagesQueue = new LinkedBlockingQueue<MessageInfo>();
	
	
	/***
	 * worker send all message from sendingMessagesQueue
	 * implement {@link DeliverySendingMessageWorker}
	 */
	private  DeliverySendingMessageWorker deliverySendingWorker  = null;
	
	
	/***
	 * {@link JmsManager}
	 */
	@Autowired
	private JmsManager jmsManager;
	

	@Override
	public void afterPropertiesSet() throws Exception {
		initializing();
		
	}
	
	@Override
	public void destroy() throws Exception {
		//TODO: please handler it
	}

	/*** 
	 * init all workers
	 */
	private void initializing() {
		
		deliverySendingWorker = new DeliverySendingMessageWorker(jmsManager, sendingMessagesQueue);
		//start thread
		new Thread(deliverySendingWorker).start();
		//start monitor message in queue
		new Thread(new MonitorWorker("Sending Messages Queue",sendingMessagesQueue)).start();	
	}
	
	
	@Override
	public void send(NotificationType notificationType, Serializable obj) throws Exception {
		ObjectMessageInfo info = new ObjectMessageInfo();
		info.setNotificationType(notificationType);
		info.setContent(obj);
		//put into queue
		sendingMessagesQueue.add(info);
		
		
	}

	@Override
	public void send(NotificationType notificationType, String message) throws Exception {
		TextMessageInfo info = new TextMessageInfo();
		info.setNotificationType(notificationType);
		info.setContent(message);
		//put into queue
		sendingMessagesQueue.add(info);		
	}
	
	@Override
	public void receive(NotificationType notificationType, MessageListener listener) throws Exception {
		jmsManager.registerMessageListener(notificationType, listener);	
	}
}
