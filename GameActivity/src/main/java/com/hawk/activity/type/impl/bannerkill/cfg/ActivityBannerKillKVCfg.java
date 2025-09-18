package com.hawk.activity.type.impl.bannerkill.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.KVResource(file = "activity/flag_god/activity_flaggod_cfg.xml")
public class ActivityBannerKillKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;
	/**
	 * 解锁条件
	 */
	private final int unlockCondition;
	/**
	 * 排行榜
	 */
	private final int rankSize;
	/**
	 * 积分目标ID
	 */
	private final String scoreTargetId;
	/**
	 * 活动杀敌积分系数
	 */
	private final String killScoreCof;
	
	/**
	 * 活动伤敌积分系数
	 */
	private final String hurtScoreCof;
	/**
	 * 杀敌伤敌积分比例（进攻方万分比，防守方万分比）
	 */
	private final String scoreRatio;
	
	/**
	 * 活动杀敌积分系数
	 */
	private Map<Integer, Integer> killScoreCfgMap;
	/**
	 * 活动伤敌积分系数
	 */
	private Map<Integer, Integer> hurtScoreCfgMap;
	
	/**
	 * 目标ID数组
	 */
	private int[] targetIdArray;
	
	/**
	 * 进攻方积分计算比例
	 */
	private int atkScoreRatio;
	/**
	 * 防守方积分计算比例
	 */
	private int defScoreRatio;
	
	/** 单例 */
	private static ActivityBannerKillKVCfg instance;

	public static ActivityBannerKillKVCfg getInstance() {
		return instance;
	}

	public ActivityBannerKillKVCfg() {
		this.serverDelay = 0;
		this.unlockCondition = 0;
		this.rankSize = 0;
		this.killScoreCof = "";
		this.hurtScoreCof = "";
		this.scoreTargetId = "";
		this.scoreRatio = "";
		instance = this;
	}

	@Override
	public boolean assemble() {
		String[] scoreCofArray = killScoreCof.split(SerializeHelper.BETWEEN_ITEMS);
		killScoreCfgMap = new HashMap<>();
		for (String scoreCofItem : scoreCofArray) {
			String[] scoreCofItemArray = scoreCofItem.split(SerializeHelper.ATTRIBUTE_SPLIT);
			if (scoreCofItemArray.length !=2) {
				throw new InvalidParameterException("ActivityBannerKillKVCfg killScoreCof itemLength not equal 2 scoreCofItemError => " + killScoreCof);
			}
			
			Integer oldValue = killScoreCfgMap.put(Integer.parseInt(scoreCofItemArray[0]), Integer.parseInt(scoreCofItemArray[1]));
			if (oldValue != null) {
				throw new InvalidParameterException("ActivityBannerKillKVCfg killScoreCof repeated item scoreCofItemError => " + killScoreCof);
			}
		}
		
		String[] hurtscoreCofArray = hurtScoreCof.split(SerializeHelper.BETWEEN_ITEMS);
		hurtScoreCfgMap = new HashMap<>();
		for (String scoreCofItem : hurtscoreCofArray) {
			String[] scoreCofItemArray = scoreCofItem.split(SerializeHelper.ATTRIBUTE_SPLIT);
			if (scoreCofItemArray.length !=2) {
				throw new InvalidParameterException("ActivityBannerKillKVCfg hurtScoreCof itemLength not equal 2 scoreCofItemError => " + hurtScoreCof);
			}
			
			Integer oldValue = hurtScoreCfgMap.put(Integer.parseInt(scoreCofItemArray[0]), Integer.parseInt(scoreCofItemArray[1]));
			if (oldValue != null) {
				throw new InvalidParameterException("ActivityBannerKillKVCfg hurtScoreCof repeated item scoreCofItemError => " + hurtScoreCof);
			}
		}
		
		targetIdArray = SerializeHelper.string2IntArray(scoreTargetId);
		
		if (targetIdArray.length ==0) {
			throw new InvalidParameterException("ActivityBannerKillKVCfg targetId length incorrect");
		}
		
		if (!HawkOSOperator.isEmptyString(scoreRatio)) {
			String[] ratioStrs = scoreRatio.split(",");
			atkScoreRatio = Integer.parseInt(ratioStrs[0].trim());
			defScoreRatio = Integer.parseInt(ratioStrs[1].trim());
		}
		
		return true;
	}

	@Override
	public boolean checkValid() {
		return true;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public int getUnlockCondition() {
		return unlockCondition;
	}

	public int getRankSize() {
		return rankSize;
	}
	
	public Map<Integer, Integer> getKillScoreCfgMap() {
		return killScoreCfgMap;
	}
	
	public Map<Integer, Integer> getHurtScoreCfgMap() {
		return hurtScoreCfgMap;
	}
	
	public int[] getTargetIdArray() {
		return targetIdArray;
	}

	public int getAtkScoreRatio() {
		return atkScoreRatio;
	}

	public int getDefScoreRatio() {
		return defScoreRatio;
	}

}
