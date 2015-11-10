package com.directv.apg.mad.notification.client.jms;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Contains all configurations for notifications
 * {@link JmsConfig}
 * @author cuongtv8
 *
 */
@Configuration(value="jms")
public class JmsConfig {

	@Value("${jms.provider.url}")
	private String jmsProviderUrl;

	@Value("${jms.username}")
	private String username;

	@Value("${jms.password}")
	private String password;

	@Value("${topic.poster.update}")
	private String posterUpdateTopicName;

	@Value("${jms.connection.clientId}")
	private String connClientId;

	/**
	 * @return the jmsProviderUrl
	 */
	public String getJmsProviderUrl() {
		return jmsProviderUrl;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return the posterUpdateTopicName
	 */
	public String getPosterUpdateTopicName() {
		return posterUpdateTopicName;
	}

	/**
	 * @return the connClientId
	 */
	public String getConnClientId() {
		return connClientId;
	}
	
}
