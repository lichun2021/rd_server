package com.hawk.match;

import org.hawk.app.HawkAppCfg;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "cfg/app.cfg")
public class MatchCfg extends HawkAppCfg {
	/**
	 * 服务端口
	 */
	protected final int serverPort;
	/**
	 * 事件超时
	 */
	protected final int eventTimeout;
	/**
	 * redis主机地址
	 */
	protected final String redisHost;
	/**
	 * redis端口
	 */
	protected final int redisPort;
	/**
	 * redis密码
	 */
	protected final String redisAuth;
	/**
	 * redis超时时间
	 */
	protected final int redisTimeout;
	/**
	 * redis可用连接实例最大数目
	 */
	protected final int redisMaxActive;
	/**
	 * redis最大空闲连接数
	 */
	protected final int redisMaxIdle;
	/**
	 * redis等待可用连接的最大时间
	 */
	protected final int redisMaxWait;

	/**
	 * 全局静态对象
	 */
	protected static MatchCfg instance = null;

	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static MatchCfg getInstance() {
		return instance;
	}

	public MatchCfg() {
		instance = this;
		serverPort = 0;
		eventTimeout = 5;
		redisHost = null;
		redisPort = 6397;
		redisAuth = null;
		redisTimeout = 3000;
		redisMaxActive = 16;
		redisMaxIdle = 8;
		redisMaxWait = 10000;
	}

	public int getServerPort() {
		return serverPort;
	}

	public int getEventTimeout() {
		return eventTimeout;
	}

	public String getRedisHost() {
		return redisHost;
	}

	public int getRedisPort() {
		return redisPort;
	}

	public String getRedisAuth() {
		return redisAuth;
	}

	public int getRedisTimeout() {
		return redisTimeout;
	}

	public int getRedisMaxActive() {
		return redisMaxActive;
	}

	public int getRedisMaxIdle() {
		return redisMaxIdle;
	}

	public int getRedisMaxWait() {
		return redisMaxWait;
	}
}
