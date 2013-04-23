package com.indicrowd.wowza.studiomanager;

import java.util.Set;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.wowza.wms.application.*;
import com.wowza.wms.amf.*;
import com.wowza.wms.client.*;
import com.wowza.wms.module.*;
import com.wowza.wms.request.*;
import com.wowza.wms.stream.*;
import com.wowza.wms.util.ModuleUtils;
import com.wowza.wms.rtp.model.*;
import com.wowza.wms.httpstreamer.model.*;
import com.wowza.wms.httpstreamer.cupertinostreaming.httpstreamer.*;
import com.wowza.wms.httpstreamer.smoothstreaming.httpstreamer.*;

public class StudioManager extends ModuleBase {

	IApplicationInstance appInstance = null;
	
	public StudioManager() {
	}

	public void doSomething(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("doSomething");
		sendResult(client, params, "Hello Wowza");
	}

	public void onAppStart(IApplicationInstance appInstance) {
		String hostname = appInstance.getProperties().getPropertyStr("redisHostname");
		String auth = appInstance.getProperties().getPropertyStr("redisAuth");
		this.appInstance = appInstance;
		
		getLogger().info("RedisHostname: " + hostname);
		getLogger().info("RedisAuth: " + auth);
		
		JedisHelper.startJedis(hostname, auth);	
		
		JedisHelper.getInstance().startSubscriber(appInstance);
	}

	public void onAppStop(IApplicationInstance appInstance) {
		String fullname = appInstance.getApplication().getName() + "/"
				+ appInstance.getName();
		getLogger().info("onAppStop: " + fullname);
		JedisHelper.getInstance().stopSubscriber();
	}

	public void onConnect(IClient client, RequestFunction function,
			AMFDataList params) {
		getLogger().info("onConnect: " + client.getClientId());
	}

	public void onConnectAccept(IClient client) {
		getLogger().info("onConnectAccept: " + client.getClientId());
	}

	public void onConnectReject(IClient client) {
		getLogger().info("onConnectReject: " + client.getClientId());
	}

	public void onDisconnect(IClient client) {
		getLogger().info("onDisconnect: " + client.getClientId());
	}

	public void onStreamCreate(IMediaStream stream) {
	}
	
	public void publish(IClient client, RequestFunction function, AMFDataList params) 
	{
		JedisHelper jedisHelper = JedisHelper.getInstance();
		
		String streamName = extractStreamName(client, function, params);
		Set<String> data = jedisHelper.getSetByKey(JedisHelper.startConcertKey);
		
		
		boolean valid = false;
		for (String validConcertId : data) {
			
			if (validConcertId.equals(streamName)) {
				valid = true;
				break;
			}
			
		}

		if (streamName == null) {
			streamName = "none";
		}
		
		if (valid) {
			invokePrevious(client, function, params);
		}
		else {		
			getLogger().info("ModuleBlocked.publish["+appInstance.getContextStr()+"]: Stream name is not valid: "+streamName);
			sendClientOnStatusError(client, "NetStream.Publish.Denied", "Stream name is not valid: "+streamName);

		}
	}
	
	public void onStreamDestroy(IMediaStream stream) {
		getLogger().info("onStreamDestroy: " + stream.getSrc());
	}
	

	public String extractStreamName(IClient client, RequestFunction function, AMFDataList params)
	{
		String streamName = params.getString(PARAM1);
		if (streamName != null)
		{
			String streamExt = MediaStream.BASE_STREAM_EXT;
			
			String[] streamDecode = ModuleUtils.decodeStreamExtension(streamName, streamExt);
			streamName = streamDecode[0];
			streamExt = streamDecode[1];
		}

		return streamName;
	}

}