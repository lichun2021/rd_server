package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 英雄试练奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cr_mission_reward.xml")
public class CrMissionRewardCfg extends HawkConfigBase {
	@Id
	/**
	 * id
	 */
	protected final int id;
	/**
	 * 积分区间-最低值
	 */
	private final int minScore;
	/**
	 * 积分区间-最高值
	 */
	private final int maxScore;
	/**
	 * 奖励
	 */
	private final int awardId;
	

	public CrMissionRewardCfg() {
		this.id = 0;
		this.minScore = 0;
		this.maxScore = 0;
		this.awardId = 0;
	}

	public int getId() {
		return id;
	}

	public int getMinScore() {
		return minScore;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public int getAwardId() {
		return awardId;
	}

	@Override
	protected boolean checkValid() {
		AwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(awardCfg == null){
			return false;
		}
		return super.checkValid();
	}

}
