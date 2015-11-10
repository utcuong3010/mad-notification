package com.directv.apg.mad.notification.client.jms.worker;

import java.util.concurrent.BlockingQueue;

import javax.jms.JMSException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.directv.apg.mad.notification.client.jms.JmsManager;
import com.directv.apg.mad.notification.client.jms.common.MessageInfo;
import com.directv.apg.mad.notification.client.jms.common.ObjectMessageInfo;
import com.directv.apg.mad.notification.client.jms.common.TextMessageInfo;
import com.directv.apg.mad.notification.client.jms.impl.HornetJmsManager;

/**
 * 
 * @author utcuong3010
 *
 */
public class DeliverySendingMessageWorker implements Runnable,Handler {
	
	private final Logger LOG = LoggerFactory.getLogger(DeliverySendingMessageWorker.class);
	
	/***
	 * reading message from this queue and delivery to the external mq
	 */
	private final BlockingQueue<MessageInfo> sendingMessageQueue ;
	
	/**
	 * send to jms server with the implement {@link HornetJmsManager}
	 */
	private final JmsManager jmsManager;
	
	public DeliverySendingMessageWorker(final JmsManager jmsManager,final BlockingQueue queue) {
		this.sendingMessageQueue = queue;
		this.jmsManager = jmsManager;
	}

	
	@Override
	public void run() {
		while(true) {
			try {
				handle(sendingMessageQueue.take());
			} catch (Exception ex) {
				LOG.error("Sending Message queue has error {}", ex);
			}			
		}
	}
	
	@Override
	public void handle(MessageInfo info) throws JMSException {
		
		LOG.info("Delivering the {} into server", info);
		boolean isSucceed = false;
		if(info instanceof TextMessageInfo) {			
			TextMessageInfo messageInfo = (TextMessageInfo)info;			
			isSucceed = jmsManager.send(messageInfo.getNotificationType(),messageInfo.getContent().toString());
												
		} else if(info instanceof ObjectMessageInfo) {
			ObjectMessageInfo objectInfo = (ObjectMessageInfo)info;
			isSucceed = jmsManager.send(objectInfo.getNotificationType(), objectInfo.getContent());
		} else {
			LOG.info("Message Info Can not be handle");
		}
		//handle in case error
		if(!isSucceed) {
			try {
						
				//1. increase retry and put back the queue
				info.increaseRetry();
				sendingMessageQueue.put(info);						
				//2. sleep 1000
				Thread.sleep(10*1000);
			} catch (Exception ex) {
				LOG.error("There were some errors when trying to connect sever {}" + ex);
			}
		}	
		
	}

}
