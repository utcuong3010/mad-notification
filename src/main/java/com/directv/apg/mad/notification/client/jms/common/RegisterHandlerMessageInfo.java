package com.directv.apg.mad.notification.client.jms.common;

import javax.jms.MessageListener;

import com.directv.apg.mad.notification.client.jms.NotificationType;

/**
 * contains all info reveal for how to add an handler on topic
 * @author utcuong3010
 *
 */
public class RegisterHandlerMessageInfo extends MessageInfo {
	
	
	private MessageListener listener;
	private String subscriberName;
	
	/**
	 * @return the listener
	 */
	public MessageListener getListener() {
		return listener;
	}
	/**
	 * @param listener the listener to set
	 */
	public void setListener(MessageListener listener) {
		this.listener = listener;
	}
	/**
	 * @return the subscriberName
	 */
	public String getSubscriberName() {
		return subscriberName;
	}
	/**
	 * @param subscriberName the subscriberName to set
	 */
	public void setSubscriberName(String subscriberName) {
		this.subscriberName = subscriberName;
	}
	@Override
	public String toString() {
		return "RegisterHandlerMessageInfo [subscriberName=" + subscriberName + ", getContent()=" + getContent()
				+ ", getNotificationType()=" + getNotificationType() + "]";
	}
	
	
	

}
