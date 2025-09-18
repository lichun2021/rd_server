package com.hawk.activity.type.impl.guildbanner.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/flag_king/activity_flagking_cfg.xml")
public class ActivityGuildBannerKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 排行榜
	 */
	private final int rankSize;
	
	/** 单例 */
	private static ActivityGuildBannerKVCfg instance;

	public static ActivityGuildBannerKVCfg getInstance() {
		return instance;
	}

	public ActivityGuildBannerKVCfg() {
		this.serverDelay = 0;
		this.rankSize = 0;
		instance = this;
	}

	@Override
	public boolean assemble() {
		return true;
	}

	@Override
	public boolean checkValid() {
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getRankSize() {
		return rankSize;
	}

}
