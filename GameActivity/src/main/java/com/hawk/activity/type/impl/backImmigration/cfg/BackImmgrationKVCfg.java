package com.hawk.activity.type.impl.backImmigration.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 *  中部养成计划
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/back_immgration/back_immgration_cfg.xml")
public class BackImmgrationKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 触发-流失天数*/
	private final int triggerLossDays;
	/** 触发-基地等级*/
	private final int triggerCityLevel;
	/** 活动持续时间*/
	private final int continueDays;
	
	private final int intervalDays;
		
	private final int intregisterDay;

	private final int migrantDay;

	private final int vipLevel;

	//战力排行上限
	private final int powerRankFrom;
	//战力排行下限
	private final int powerRankEnd;
	//推荐移民服务器个数
	private final int recommendServerCount;
	
	public BackImmgrationKVCfg() {
		serverDelay = 0;
		triggerLossDays =0;
		continueDays = 0;
		triggerCityLevel= 0;
		intervalDays = 0;
		powerRankFrom= 0;
		powerRankEnd = 0;
		recommendServerCount = 0;
		migrantDay = 0;
		intregisterDay = 0;
		vipLevel = 0;
	}
	
	
	@Override
	protected boolean assemble() {
		return true;
	}


	public long getServerDelay() {
		return serverDelay  * 1000l;
	}

	
	public int getContinueDays() {
		return continueDays;
	}

	public int getTriggerCityLevel() {
		return triggerCityLevel;
	}
	
	public int getTriggerLossDays() {
		return triggerLossDays;
	}
	
	public int getIntervalDays() {
		return intervalDays;
	}
	
	public int getPowerRankFrom() {
		return powerRankFrom;
	}
	
	public int getPowerRankEnd() {
		return powerRankEnd;
	}

	public int getRecommendServerCount() {
		return recommendServerCount;
	}
	
	public int getMigrantDay() {
		return migrantDay;
	}
	public int getIntregisterDay() {
		return intregisterDay;
	}
	
	public int getVipLevel() {
		return vipLevel;
	}
}