package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 情报交易所常量配置
 * 
 * @author Golden
 *
 */
@HawkConfigManager.KVResource(file = "xml/agency_const.xml")
public class AgencyConstCfg extends HawkConfigBase {

	/**
	 * 首个事件的到达时间（秒）
	 */
	private final int firstEventTime;
	
	/**
	 * 每个任务增加的经验值
	 */
	private final int eventExp;
	
	/**
	 * 刷新时间
	 */
	private final String refreshTime;
	
	/**
	 * 刷新延迟时间
	 */
	private final int refreshDelay;
	
	/**
	 * 刷新CD
	 */
	private final int refreshCd;
	
	/**
	 * 事件的消失时间
	 */
	private final int eventDisappearTime;
	
	/**
	 * 事件的体力消耗
	 */
	private final int strengthConsume;
	
	/**
	 * 开启实力验证的情报中心等级
	 */
	private final int startSpecialLv;
	
	/**
	 * 救援事件固定速度
	 */
	private final int helpSpeed;
	
	/**
	 * 自动升级等级限制
	 */
	private final int autoLevelUpLimit;
	
	/**
	 * 情报中心开放等级
	 */
	private final int agencyUnlockLevel;
	
	/**
	 * 特殊事件每天刷新限制
	 */
	private final int specialEventDailyRefreshLimit;
	/**
	 * 单例
	 */
	private static AgencyConstCfg instance = null;

	/**
	 * 获取单例
	 * @return
	 */
	public static AgencyConstCfg getInstance() {
		return instance;
	}

	/**
	 * 构造
	 */
	public AgencyConstCfg() {
		instance = this;
		firstEventTime = 3;
		eventExp = 1;
		refreshTime = "8,16,24";
		refreshDelay = 3600;
		refreshCd = 28800;
		eventDisappearTime = 57600;
		strengthConsume = 5;
		startSpecialLv = 6;
		helpSpeed = 1;
		autoLevelUpLimit = 6;
		agencyUnlockLevel = 1;
		specialEventDailyRefreshLimit = 0;
	}

	public int getFirstEventTime() {
		return firstEventTime;
	}

	public int getEventExp() {
		return eventExp;
	}

	public int[] getRefreshTimeArray() {
		String[] split = refreshTime.split(",");
		int[] refresTimeArray = new int[split.length];
		for (int i = 0; i < split.length; i++) {
			refresTimeArray[i] = Integer.parseInt(split[i]);
		}
		return refresTimeArray;
	}

	public int getRefreshDelay() {
		return refreshDelay;
	}

	public long getRefreshCd() {
		return refreshCd * 1000L;
	}

	public long getEventDisappearTime() {
		return eventDisappearTime * 1000L;
	}

	public int getStrengthConsume() {
		return strengthConsume;
	}

	public int getStartSpecialLv() {
		return startSpecialLv;
	}

	public int getHelpSpeed() {
		return helpSpeed;
	}

	public int getAutoLevelUpLimit() {
		return autoLevelUpLimit;
	}

	public int getAgencyUnlockLevel() {
		return agencyUnlockLevel;
	}
	
	public int getSpecialEventDailyRefreshLimit() {
		return specialEventDailyRefreshLimit;
	}
}
