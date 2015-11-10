package com.directv.apg.mad.notification.client.jms.worker;

import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * Monitoring data
 * @author utcuong3010
 *
 */
public class MonitorWorker implements Runnable {
	
	private final Logger LOG = LoggerFactory.getLogger(MonitorWorker.class);
	
	private final String monitorName;
	
	/** 
	 * monitor data in queue
	 */
	private final Queue dataQueue;
		
	public MonitorWorker(final String name,final Queue data) {
		monitorName = name;
		dataQueue = data;
	}
		
	@Override
	public void run() {
		while(true) {
					
			try {
				LOG.info("[Monitor]-The {} has total {} messages", monitorName,dataQueue.size());	
				Thread.sleep(10*1000);				
			} catch (InterruptedException e) {
				LOG.error("Monitor worker error {}",e);
			}
		}
		
	}

}
