package com.hawk.activity.type.impl.backSoldierExchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/** 十连抽活动全局K-V配置
 * 
 * @author Jesse */
@HawkConfigManager.KVResource(file = "activity/back_soldier_exchange/back_soldier_exchange_cfg.xml")
public class BackSoldierExchangeKVCfg extends HawkConfigBase {
	// # 服务器开服延时开启活动时间；单位：秒
	private final long serverDelay;// = 0
	// # 最小大本等级可见
	private final int buildMinLevel;
	// # 转换消耗
	private final String exchangeCost;// = 30000_9990001_1
	// # 活动期间总共转换次数
	private final int exchangeTotalTime;
	// # 转换CD；单位：秒
	private final int exchangeCD;

	/** 触发-流失天数*/
	private final int triggerLossDays;
	/** 活动持续时间*/
	private final int continueDays;
	/** 活动触发间隔时间*/
	private final int intervalDays;
	
	
	public BackSoldierExchangeKVCfg() {
		serverDelay = 0;
		buildMinLevel = 0;
		exchangeTotalTime = 0;
		exchangeCD = 0;
		exchangeCost = "";
		triggerLossDays =0;
		continueDays = 0;
		intervalDays = 0;
	}

	@Override
	protected boolean assemble() {
		return super.assemble();
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getBuildMinLevel() {
		return buildMinLevel;
	}

	public String getExchangeCost() {
		return exchangeCost;
	}

	public int getExchangeTotalTime() {
		return exchangeTotalTime;
	}

	public int getExchangeCD() {
		return exchangeCD;
	}

	public int getContinueDays() {
		return continueDays;
	}
	
	public int getIntervalDays() {
		return intervalDays;
	}
	
	public int getTriggerLossDays() {
		return triggerLossDays;
	}
}
