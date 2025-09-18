package com.hawk.activity.type.impl.inherit.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/inherit/inherit_cfg.xml")
public class InheritKVCfg extends HawkConfigBase {

	/** 服务器开服延时开启活动时间 单位:s */
	private final int serverDelay;
	
	/** 离线时长 单位:s*/
	private final int offlineTime;
	
	/** vip等级要求*/
	private final int vipLimit;
	
	/** 指挥官等级*/
	private final int level;
	
	/** 战力*/
	private final int battlePoint;
	
	/** 大本等级*/
	private final int buildlevel;
	
	/** 活动开启后持续时间*/
	private final int lastTime;
	
	/** 最大返现金币数量*/
	private final int maxGift;

	/** 金币返现比例(万分比)*/
	private final int goldRate;
	

	public InheritKVCfg() {
		serverDelay = 0;
		offlineTime = 0;
		vipLimit = 0;
		lastTime = 0;
		maxGift = 0;
		goldRate = 0;
		level = 0;
		battlePoint = 0;
		buildlevel = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public long getOfflineTime() {
		return offlineTime * 1000l;
	}

	public int getVipLimit() {
		return vipLimit;
	}


	public long getLastTime() {
		return lastTime * 1000l;
	}

	public int getMaxGift() {
		return maxGift;
	}

	public long getGoldRate() {
		return goldRate;
	}

	public int getLevel() {
		return level;
	}

	public int getBattlePoint() {
		return battlePoint;
	}

	public int getBuildlevel() {
		return buildlevel;
	}
	
}
