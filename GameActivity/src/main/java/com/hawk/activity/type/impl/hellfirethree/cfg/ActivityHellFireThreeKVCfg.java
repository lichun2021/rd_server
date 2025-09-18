package com.hawk.activity.type.impl.hellfirethree.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/hellfire_thr/activity_hellfire_thr_cfg.xml")
public class ActivityHellFireThreeKVCfg extends HawkConfigBase {
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
	/** 单例 */
	private static ActivityHellFireThreeKVCfg instance;

	public static ActivityHellFireThreeKVCfg getInstance() {
		return instance;
	}

	public ActivityHellFireThreeKVCfg() {
		this.serverDelay = 0;
		this.unlockCondition = 0;
		this.effectiveTime = 0;
		this.accountTime = 0;
		this.difficultRndWeight = "";
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
}
