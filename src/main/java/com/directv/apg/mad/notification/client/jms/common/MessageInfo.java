package com.directv.apg.mad.notification.client.jms.common;

import java.io.Serializable;

import com.directv.apg.mad.notification.client.jms.NotificationType;

/**
 * contains message info
 * @author utcuong3010
 *
 */
public abstract class MessageInfo{
	
	private NotificationType notificationType;
	protected Serializable content;
	/***
	 * keep how many times for trying to post in queue server
	 */
	private int retry = 0;

	public NotificationType getNotificationType() {
		return notificationType;
	}

	public void setNotificationType(NotificationType notificationType) {
		this.notificationType = notificationType;
	}
	
	public void setContent(Serializable content) {
		this.content = content;
	}
	public Serializable getContent() {
		return content;
	}
	
	/***
	 * increase times to retry
	 */
	public void increaseRetry() {
		this.retry++;
	}

	
}
