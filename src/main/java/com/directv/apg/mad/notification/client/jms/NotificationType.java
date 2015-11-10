package com.directv.apg.mad.notification.client.jms;

/**
 * @author KhanhNH2
 */
public enum NotificationType {

    POSTER_UPDATE_EVENT("PosterUpdate");

    private String topicName;

    NotificationType(String topicName) {
        this.topicName = topicName;
    }
    
    public String getTopicName() {
        return topicName;
    }
    
    @Override
    public String toString() {
    	return this.topicName;
    }

}
