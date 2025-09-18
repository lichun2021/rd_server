package com.hawk.activity.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(files = {"cfg/activity.cfg"})
public class ActivityConfig extends HawkConfigBase {
	/**
	 * 初始数据缓存
	 */
	protected final int cacheInitSize;
	/**
	 * 最大数据缓存
	 */
	protected final int cacheMaxSize;
	/**
	 * 数据缓存过期时间(s)
	 */
	protected final int cacheExpireTime;
	/**
	 * 最强指挥官活动排行榜玩家数量
	 */
	protected final int activityCircularRankSize;
	
	/**
	 * 客户端配置文件大小分包标准
	 */
	protected final int clientCfgLimitSize;

	/**
	 * 全局静态对象
	 */
	protected static ActivityConfig instance = null;
	
	/**
	 * 获取全局静态对象
	 *
	 * @return
	 */
	public static ActivityConfig getInstance() {
		return ActivityConfig.instance;
	}

	public ActivityConfig() {
		instance = this;
		cacheInitSize = 0;
		cacheMaxSize = 0;
		cacheExpireTime = 0;
		activityCircularRankSize = 0;
		clientCfgLimitSize = 100 * 1024;
	}

	public int getCacheInitSize() {
		return cacheInitSize;
	}

	public int getCacheMaxSize() {
		return cacheMaxSize;
	}

	public int getCacheExpireTime() {
		return cacheExpireTime;
	}

	public int getActivityCircularRankSize() {
		return activityCircularRankSize;
	}

	public int getClientCfgLimitSize() {
		return clientCfgLimitSize;
	}
	
	
}
