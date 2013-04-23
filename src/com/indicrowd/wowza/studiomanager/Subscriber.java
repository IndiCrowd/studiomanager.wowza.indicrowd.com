package com.indicrowd.wowza.studiomanager;

import java.util.Set;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.stream.IMediaStream;
import com.wowza.wms.stream.MediaStreamMap;

import redis.clients.jedis.JedisPubSub;
 
public class Subscriber extends JedisPubSub {
	IApplicationInstance appInstance;

	public Subscriber(IApplicationInstance appInstance) {
		this.appInstance = appInstance;
	}
 
    @Override
    public void onMessage(String channel, String message) {
    	System.out.println("onMessage -> " + channel + " " + message);
    	
    	if (appInstance != null) {
    		
    		MediaStreamMap streamMap = appInstance.getStreams();
    		JedisHelper helper = JedisHelper.getInstance();
    		
    		Set<String> concertIds = helper.getSetByKey(JedisHelper.startConcertKey);
    		
    		for (IMediaStream stream : streamMap.getStreams()) {
    			boolean valid = false;
    			
	    		for (String concertId : concertIds) {
	    			
	    			if (stream.getName().equals(concertId))
	    			{
	    				valid = true;
	    				break;
	    			}
	    		}
	    		
	    		if (!valid) {
	    			stream.send("exitStream");
	    			stream.shutdown();
	    			streamMap.clearStreamName(stream.getName());
	    		}
    		}
    		
    	}
    }
 
    @Override
    public void onPMessage(String pattern, String channel, String message) {
    	System.out.println("onPMessage -> " + pattern + " " + channel + " " + message);
 
    }
 
    @Override
    public void onSubscribe(String channel, int subscribedChannels) {
    	System.out.println("onSubscribe -> " + channel + " " + subscribedChannels);
 
    }
 
    @Override
    public void onUnsubscribe(String channel, int subscribedChannels) {
    	System.out.println("onUnsubscribe -> " + channel + " " + subscribedChannels);
 
    }
 
    @Override
    public void onPUnsubscribe(String pattern, int subscribedChannels) {
    	System.out.println("onPUnsubscribe -> " + pattern + " " + subscribedChannels);
 
    }
 
    @Override
    public void onPSubscribe(String pattern, int subscribedChannels) {
    	System.out.println("onPSubscribe -> " + pattern + " " + subscribedChannels);
    }
}
