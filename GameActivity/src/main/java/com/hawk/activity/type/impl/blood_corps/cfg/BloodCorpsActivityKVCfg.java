package com.hawk.activity.type.impl.blood_corps.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 铁血军团活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/blood_corps/blood_corps_activity_cfg.xml")
public class BloodCorpsActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	
	/** 积分任务是否每日重置(零点跨天重置) */
	private final int isDailyReset;
	
	/** 排行榜单是否每日重置(零点跨天重置) */
	private final int isRankDailyReset;
	
	/** 排行奖励领取条件*/
	private final int rankRewardNeedPower;
	
	/** 排行数量*/
	private final int rankLimit;
	
	/** 排行刷新周期*/
	private final long rankPeriod;
	
	public BloodCorpsActivityKVCfg() {
		serverDelay = 0;
		isDailyReset = 0;
		isRankDailyReset = 0;
		rankRewardNeedPower = 0;
		rankLimit = 100;
		rankPeriod = 600000;
	}
	
	public boolean isScoreDailyReset() {
		return isDailyReset == 1;
	}
	
	public boolean getIsRankDailyReset() {
		return isRankDailyReset == 1;
	}

	public int getRankRewardNeedPower() {
		return rankRewardNeedPower;
	}

	public int getRankLimit() {
		return rankLimit;
	}

	public long getRankPeriod() {
		return rankPeriod;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
}
