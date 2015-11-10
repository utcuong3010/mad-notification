package com.directv.apg.mad.notification.client.jms.worker;

import javax.jms.JMSException;

import com.directv.apg.mad.notification.client.jms.common.MessageInfo;

/**
 * This is the worker working background process
 * @author utcuong3010
 *
 */
public interface Handler {

	public abstract void handle(MessageInfo messageInfo) throws JMSException;
}
