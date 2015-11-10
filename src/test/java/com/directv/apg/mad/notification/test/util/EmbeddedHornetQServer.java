package com.directv.apg.mad.notification.test.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.io.FilenameUtils;
import org.hornetq.api.core.TransportConfiguration;
import org.hornetq.core.config.Configuration;
import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.remoting.impl.netty.NettyAcceptorFactory;
import org.hornetq.core.remoting.impl.netty.NettyConnectorFactory;
import org.hornetq.core.server.JournalType;
import org.hornetq.jms.server.config.ConnectionFactoryConfiguration;
import org.hornetq.jms.server.config.JMSConfiguration;
import org.hornetq.jms.server.config.TopicConfiguration;
import org.hornetq.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.hornetq.jms.server.config.impl.JMSConfigurationImpl;
import org.hornetq.jms.server.config.impl.TopicConfigurationImpl;
import org.hornetq.jms.server.embedded.EmbeddedJMS;
import org.jnp.server.Main;
import org.jnp.server.NamingBeanImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the embedded hornet QM server {@link EmbeddedHornetQServer}
 * 
 * @author NGOCNV
 *
 */
public class EmbeddedHornetQServer {

    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedHornetQServer.class);

    private String connectAddress;
    private int connectPort;
    private String topicName;
    private String topicBinding;
    private String dataDir = "./hornetq-data";

    private EmbeddedJMS jmsServer;
    private NamingBeanImpl naming;
    private Main jndiServer;
    

    public EmbeddedHornetQServer() {
        try {
            startJNDIServer();
            setConnectAddress("localhost");
            setConnectPort(5445);
            setTopicName("PosterUpdate");
            setTopicBinding("/topic/PosterUpdate");
        } catch (Exception e) {
        }
    }
    

    /**
	 * @return the connectAddress
	 */
	public String getConnectAddress() {
		return connectAddress;
	}

	/**
	 * @param connectAddress the connectAddress to set
	 */
	public void setConnectAddress(String connectAddress) {
		this.connectAddress = connectAddress;
	}


	/**
	 * @return the connectPort
	 */
	public int getConnectPort() {
		return connectPort;
	}

	/**
	 * @param connectPort the connectPort to set
	 */
	public void setConnectPort(int connectPort) {
		this.connectPort = connectPort;
	}




	/**
	 * @return the topicName
	 */
	public String getTopicName() {
		return topicName;
	}


	/**
	 * @param topicName the topicName to set
	 */
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	/**
	 * @return the topicBinding
	 */
	public String getTopicBinding() {
		return topicBinding;
	}

	/**
	 * @param topicBinding the topicBinding to set
	 */
	public void setTopicBinding(String topicBinding) {
		this.topicBinding = topicBinding;
	}


	/**
	 * @return the dataDir
	 */
	public String getDataDir() {
		return dataDir;
	}


	/**
	 * @param dataDir the dataDir to set
	 */
	public void setDataDir(String dataDir) {
		this.dataDir = dataDir;
	}

	/**
	 * @return the jmsServer
	 */
	public EmbeddedJMS getJmsServer() {
		return jmsServer;
	}

	/**
	 * @param jmsServer the jmsServer to set
	 */
	public void setJmsServer(EmbeddedJMS jmsServer) {
		this.jmsServer = jmsServer;
	}

	/**
	 * @return the naming
	 */
	public NamingBeanImpl getNaming() {
		return naming;
	}

	/**
	 * @param naming the naming to set
	 */
	public void setNaming(NamingBeanImpl naming) {
		this.naming = naming;
	}

	/**
	 * @return the jndiServer
	 */
	public Main getJndiServer() {
		return jndiServer;
	}
	
	/**
	 * @param jndiServer the jndiServer to set
	 */
	public void setJndiServer(Main jndiServer) {
		this.jndiServer = jndiServer;
	}


	public void start() throws Exception {
        try {

            Thread.sleep(1000);
            Configuration hornetqConfig = createHornetQConfig();
            JMSConfiguration jmsConfig = createJmsConfig();

            jmsServer = new EmbeddedJMS();
            jmsServer.setConfiguration(hornetqConfig);
            jmsServer.setJmsConfiguration(jmsConfig);
            jmsServer.setContext(getContext());
            jmsServer.start();
            LOG.info("Embedded HornetQ JMS server started");
        } catch (Exception e) {
        	LOG.error("Embedded HornetQ JMS server can not start because of the error:" + e.getMessage());
            throw new RuntimeException("Error initializing embedded HornetQ server", e);
        }
    }

    private JMSConfiguration createJmsConfig() {
        JMSConfiguration jmsConfig = new JMSConfigurationImpl();

        ArrayList<String> connectorNames = new ArrayList<String>();
        connectorNames.add("connector");
        ConnectionFactoryConfiguration cfConfig = new ConnectionFactoryConfigurationImpl("ConnectionFactory", false,
            connectorNames, "/ConnectionFactory");
        jmsConfig.getConnectionFactoryConfigurations().add(cfConfig);

        TopicConfiguration topicConfig = new TopicConfigurationImpl(topicName, topicBinding);
        jmsConfig.getTopicConfigurations().add(topicConfig);

        return jmsConfig;
    }

    private Configuration createHornetQConfig() {
        ConfigurationImpl cfg = new ConfigurationImpl();

        cfg.setSecurityEnabled(false);

        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("host", this.connectAddress);
        params.put("port", this.connectPort);

        TransportConfiguration acceptorCfg = new TransportConfiguration(NettyAcceptorFactory.class.getName(), params);
        cfg.getAcceptorConfigurations().add(acceptorCfg);

        TransportConfiguration connectorCfg = new TransportConfiguration(NettyConnectorFactory.class.getName());
        cfg.getConnectorConfigurations().put("connector", connectorCfg);

        cfg.setJournalType(JournalType.NIO);

        cfg.setPagingDirectory(FilenameUtils.concat(dataDir, "paging"));
        cfg.setBindingsDirectory(FilenameUtils.concat(dataDir, "bindings"));
        cfg.setJournalDirectory(FilenameUtils.concat(dataDir, "journal"));
        cfg.setLargeMessagesDirectory(FilenameUtils.concat(dataDir, "large-messages"));

        return cfg;
    }

    public void stop() throws InterruptedException {
        if (jmsServer != null) {
            try {
                jmsServer.stop();
                LOG.info("Embedded HornetQ JMS server stopped");
            } catch (Exception e) {
                LOG.warn("Error stopping the embedded HornetQ server. Reason: {}", e.getMessage(), e);
            }
        }
   }

    private void stopJNDIServer() {
        if (naming != null) {
            naming.stop();
        }

        if (jndiServer != null) {
            jndiServer.stop();
        }
    }

    public static void main(String[] args) throws Exception {
    	try {
	        EmbeddedHornetQServer server = new EmbeddedHornetQServer();
	        server.setConnectAddress("localhost");
	        server.setConnectPort(5445);
	        server.setTopicName("TestTopic");
	        server.setTopicBinding("/topic/TestTopic");
	        server.start();
	        
    	} catch(Exception ex) {
    		
    	}
    }

    /**
     * Get context Naming
     * @return
     * @throws NamingException
     */
    private Context getContext() throws NamingException {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        props.put(Context.PROVIDER_URL, "jnp://localhost:1099");
        props.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");

        return new InitialContext(props);
    }
    
    /**
     * Start Server
     * @throws Exception
     */
    private void startJNDIServer() throws Exception {
        System.setProperty("java.naming.factory.initial", "org.jnp.interfaces.NamingContextFactory");
        naming = new NamingBeanImpl();
        naming.start();

        jndiServer = new Main();
        jndiServer.setNamingInfo(naming);
        jndiServer.setPort(1099);
        jndiServer.setBindAddress("localhost");
        jndiServer.setRmiPort(1098);
        jndiServer.setRmiBindAddress("localhost");
        jndiServer.start();
    }

}
