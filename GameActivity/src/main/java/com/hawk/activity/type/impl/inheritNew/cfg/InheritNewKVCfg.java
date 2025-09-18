package com.hawk.activity.type.impl.inheritNew.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.KVResource(file = "activity/inherit_new/inherit_new_cfg.xml")
public class InheritNewKVCfg extends HawkConfigBase {

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
	
	/** 老服活动有效期*/
	private final int validTime;
	
	/** 最大返现金币数量*/
	private final int maxGift;

	/** 金币返现比例(万分比)*/
	private final int goldRate;

	/** 最大返现金币数量*/
	private final int specialMaxGift;

	/** 金币返现比例(万分比)*/
	private final int specialGoldRate;
	

	public InheritNewKVCfg() {
		serverDelay = 0;
		offlineTime = 0;
		vipLimit = 0;
		lastTime = 0;
		maxGift = 0;
		goldRate = 0;
		specialMaxGift = 0;
		specialGoldRate = 0;
		level = 0;
		battlePoint = 0;
		buildlevel = 0;
		validTime = 3*24*3600;
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
	

	public long getValidTime() {
		return validTime * 1000l;
	}

	public int getMaxGift() {
		return maxGift;
	}

	public long getGoldRate() {
		return goldRate;
	}

	public int getSpecialMaxGift() {
		return specialMaxGift;
	}

	public int getSpecialGoldRate() {
		return specialGoldRate;
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
