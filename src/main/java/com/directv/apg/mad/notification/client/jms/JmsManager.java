package com.directv.apg.mad.notification.client.jms;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.MessageListener;
import javax.jms.Topic;

/**
 * This is the common interface for all jms 
 * @author utcuong3010
 *
 */
public interface JmsManager{
	
	/***
	 * send {@link Topic} to destination {@link Destination}
	 * @return
	 *  True: sent succeed else false  
	 * @param message
	 */
	public boolean send(final NotificationType notificationType,final String message);
	
	
	/***
	 * send {@link NotificationType} to destination {@link Destination}
	 * @return
	 *  True: sent succeed else false  
	 * @param message
	 */
	public boolean send(final NotificationType notificationType,final Serializable obj);
	
	
	/**
	 * 
	 * @param NotificationType
	 * @param listener
	 * @return
	 */
	public boolean registerMessageListener(final NotificationType notificationType,final MessageListener listener);
	

}
