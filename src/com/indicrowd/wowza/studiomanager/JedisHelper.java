package com.indicrowd.wowza.studiomanager;

import java.util.Set;

import org.apache.log4j.Logger;

import com.wowza.wms.application.IApplicationInstance;
import com.wowza.wms.logging.WMSLogger;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisHelper {
	private static Logger logger = WMSLogger.getRootLogger();
	
	private static JedisHelper instance;
	private final static int COMMON_EXPIRE_SECOND = 7 * 24 * 60 * 60; 
	public static final String channelName = "commonChannel";
	public static final String startConcertKey ="startConcertEventList";
	public static final String endConcertKey = "endConcert_";
	
	private JedisPool jedisPool;
	private String hostname;
	private String auth;
	private Jedis subscriberJedis = null;
	private Subscriber subscriber = null;
	
	public JedisHelper(String hostname, String auth) {
		JedisPoolConfig config = new JedisPoolConfig();
		
		jedisPool = new JedisPool(config, hostname, 6379, 0);
		this.hostname = hostname;
		this.auth = auth;

	}
	
	public void startSubscriber(IApplicationInstance appInstance) 
	{
		stopSubscriber();

		subscriber = new Subscriber(appInstance);

		new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.info("Subscribing to \"commonChannel\". This thread will be blocked.");
                    subscriberJedis.subscribe(subscriber, channelName);
                    System.out.println("Subscription ended.");
                } catch (Exception e) {
                	logger.info("Subscribing failed." + e);
                }
            }
        }).start();
	}
	
	public void stopSubscriber()
	{

		if (subscriberJedis != null)
		{
			if (subscriber != null)
				subscriber.unsubscribe();
			
			jedisPool.returnResource(subscriberJedis);
			
			subscriberJedis = null;
			subscriber = null;
		}
	}

	public void set(String key, Object obj) {
		Jedis jedis = jedisPool.getResource();
		jedis.auth(auth);
		
		jedis.setnx(key, obj.toString());
		
		jedisPool.returnResource(jedis);
	}
	
	public String get(String key) {
		Jedis jedis = jedisPool.getResource();
		jedis.auth(auth);
		
		String data = jedis.get(key);
		jedisPool.returnResource(jedis);
		return data;
	}
	
	public Set<String> getSetByKey(String key) {
		Jedis jedis = jedisPool.getResource();
		jedis.auth(auth);
		
		Set<String> data = jedis.smembers(key);
		jedisPool.returnResource(jedis);
		return data;
	}

	public void addSetElement(String key, String targetKey) {
		//System.out.println("sadd:" + key + "," + targetKey);
		Jedis jedis = jedisPool.getResource();
		jedis.auth(auth);
		
		jedis.sadd(key, targetKey);
		jedis.expire(key, COMMON_EXPIRE_SECOND);
		jedisPool.returnResource(jedis);
	}

	public void removeSetElement(String key, String targetKey) {
		//System.out.println("srem:" + key + "," + targetKey);
		Jedis jedis = jedisPool.getResource();
		jedis.auth(auth);
		
		jedis.srem(key, targetKey);
		jedis.expire(key, COMMON_EXPIRE_SECOND);
		jedisPool.returnResource(jedis);
	}
	
	public String getHostname() {
		return hostname;
	}
	
	public String getAuth() {
		return auth;
	}
	
	public void setAuth(String auth) {
		this.auth = auth;
	}
	
	public void destory() {
		jedisPool.destroy();
		jedisPool = null;
	}
	
	public static void startJedis(String hostname, String auth) {
		if (instance != null) {
			
			if (hostname.equals(instance.getHostname())) {
				instance.setAuth(auth);
				return ;
			}
			
			instance.destory();
		}
		
		instance = new JedisHelper(hostname, auth);
	}
	
	public static JedisHelper getInstance() {
		return instance;
	}
	
}
