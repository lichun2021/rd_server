package com.hawk.activity.type.impl.hellfire.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/hellfire/activity_hellfire_cfg.xml")
public class ActivityHellFireKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 解锁条件
	 */
	private final int unlockCondition;
	/**
	 * 生效时间
	 */
	private final int effectiveTime;
	/**
	 * 结算时间
	 */
	private final int accountTime;
	/**
	 * 难度
	 */
	private final String difficultRndWeight;
	/**
	 * 
	 */
	private int[][] difficultRndWeightArray;
	/**
	 * 排行榜
	 */
	private final int rankSize;
	/**
	 * 排行榜类型
	 */
	private final int rankType;
	/**单例*/
	private static ActivityHellFireKVCfg instance;
	
	public static ActivityHellFireKVCfg getInstance() {
		return instance;
	}
	public ActivityHellFireKVCfg() {
		this.serverDelay = 0;
		this.unlockCondition = 0;
		this.effectiveTime = 0;
		this.accountTime = 0;
		this.difficultRndWeight = "";
		this.rankSize = 0;
		this.rankType = 0;
		instance = this;
	}
	
	@Override
	public boolean assemble() {						
		difficultRndWeightArray = SerializeHelper.string2IntIntArray(difficultRndWeight);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {		
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getEffectiveTime() {
		return effectiveTime;
	}

	public int getAccountTime() {
		return accountTime;
	}

	public int[][] getDifficultRndWeightArray() {
		return difficultRndWeightArray;
	}

	public int getUnlockCondition() {
		return unlockCondition;
	}
	public int getRankSize() {
		return rankSize;
	}
	public int getRankType() {
		return rankType;
	}
}
