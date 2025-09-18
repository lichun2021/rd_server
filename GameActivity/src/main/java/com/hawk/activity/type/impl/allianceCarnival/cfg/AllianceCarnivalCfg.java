package com.hawk.activity.type.impl.allianceCarnival.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 联盟总动员配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "activity/alliance_carnival/alliance_carnival_cfg.xml")
public class AllianceCarnivalCfg extends HawkConfigBase {

	/**
	 * 开服延迟开放时间
	 */
	private final long serverDelay;

	/**
	 * 刷新任务数量
	 */
	private final int refreshCount;
	
	/**
	 * 默认接受任务次数
	 */
	private final int defaultReceiveTime;
	
	/**
	 * 初始等级
	 */
	private final int initExp;

	/**
	 * 购买次数限制
	 */
	private final int buyLimit;
	
	/**
	 * 购买消耗
	 */
	private final int buyPrice;
	
	/**
	 * 重复报箱开始领取等级
	 */
	private final int awardLvLimit;
	/**
	 * 重复报箱每个所需积分数量
	 */
	private final int awardScore;
	/**
	 * 重复报箱道具id
	 */
	private final String award;
	/**
	 * 重复报箱付费道具
	 */
	private final String payAward;
	private final String androidPayId;
	private final String iosPayId;
	
	/**
	 * 单例
	 */
	private static AllianceCarnivalCfg instance = null;

	
	public static AllianceCarnivalCfg getInstance() {
		return instance;
	}

	/**
	 * 构造
	 */
	public AllianceCarnivalCfg() {
		serverDelay = 0L;
		refreshCount = 20;
		defaultReceiveTime = 7;
		instance = this;
		initExp = 0;
		buyLimit = 1;
		buyPrice = 0;
		award="";
		awardScore=0;
		awardLvLimit=0;
		payAward = "";
		androidPayId = "";
		iosPayId = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}

	public int getRefreshCount() {
		return refreshCount;
	}

	public int getDefaultReceiveTime() {
		return defaultReceiveTime;
	}

	public int getInitExp() {
		return initExp;
	}

	public int getBuyLimit() {
		return buyLimit;
	}

	public int getBuyPrice() {
		return buyPrice;
	}

	public int getAwardLvLimit() {
		return awardLvLimit;
	}

	public int getAwardScore() {
		return awardScore;
	}

	public String getAward() {
		return award;
	}

	public String getPayAward() {
		return payAward;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}
	
}
