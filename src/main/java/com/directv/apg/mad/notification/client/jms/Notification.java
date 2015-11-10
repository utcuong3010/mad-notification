package com.directv.apg.mad.notification.client.jms;

import java.io.Serializable;

import javax.jms.MessageListener;

/**
 * @author KhanhNH2, NgocNV10
 */
public interface Notification {

    /**
     * Send a {@link Serializable} object to the JMS server by the {@link NotificationType}
     *
     * @param notificationType
     *            {@link NotificationType} object need to notify.
     * @param obj
     *            {@link Serializable} object need to send.
     * @throws Exception
     *             Any exceptions occur when send the message.
     */
    public void send(NotificationType notificationType, Serializable obj) throws Exception;

    /**
     * Send a text message to the JMS server by the {@link NotificationType}
     *
     * @param notificationType
     *            {@link NotificationType} object need to notify.
     * @param message
     *            Message need to send.
     * @throws Exception
     *             Any exceptions occur when send the message.
     */
    public void send(NotificationType notificationType, String message) throws Exception;

    /**
     * Receive the message with asynchronous mode with the {@link NotificationType}.
     *
     * @param notificationType
     *            {@link NotificationType} object need to listen a new message.
     * @param listener
     *            {@link MessageListener} object to listen new message as asynchronous mode.
     * @throws Exception
     *             Any exceptions occur when receive the message.
     */
    public void receive(NotificationType notificationType, MessageListener listener) throws Exception;



}
