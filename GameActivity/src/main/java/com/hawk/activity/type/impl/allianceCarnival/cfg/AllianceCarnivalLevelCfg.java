package com.hawk.activity.type.impl.allianceCarnival.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 联盟总动员等级配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/alliance_carnival/alliance_carnival_level.xml")
public class AllianceCarnivalLevelCfg extends HawkConfigBase {

	/**
	 * 等级
	 */
	@Id
	private final int level;

	/**
	 * 基础奖励
	 */
	private final String normalReward;
	
	/**
	 * 进阶奖励
	 */
	private final String advReward;
	
	/**
	 * 进阶奖励所需玩家数量
	 */
	private final int advancePlayerNum;
	
	/**
	 * 当前等级经验条
	 */
	private final int levelUpExp;
	/**
	 * 点藏奖励
	 */
	private final String payReward;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> baseRewardList;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> advRewardList;
	
	/**
	 * 奖励列表
	 */
	private List<RewardItem.Builder> payRewardList;
	
	
	public AllianceCarnivalLevelCfg() {
		level = 0;
		normalReward = "";
		advReward = "";
		levelUpExp = 0;
		advancePlayerNum = 0;
		payReward = "";
	}

	public int getLevel() {
		return level;
	}

	public String getNormalReward() {
		return normalReward;
	}

	public String getAdvReward() {
		return advReward;
	}

	public int getAdvancePlayerNum() {
		return advancePlayerNum;
	}

	public int getLevelUpExp() {
		return levelUpExp;
	}
	
	public String getPayReward() {
		return payReward;
	}

	public List<RewardItem.Builder> getBaseRewardList() {
		return baseRewardList;
	}

	public List<RewardItem.Builder> getAdvRewardList() {
		return advRewardList;
	}
	
	public List<RewardItem.Builder> getPayRewardList() {
		return payRewardList;
	}

	@Override
	protected boolean assemble() {
		
		List<RewardItem.Builder> baseRewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(normalReward)) {
			baseRewardList = RewardHelper.toRewardItemImmutableList(normalReward);
		}
		this.baseRewardList = baseRewardList;
		
		List<RewardItem.Builder> advRewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(advReward)) {
			advRewardList = RewardHelper.toRewardItemImmutableList(advReward);
		}
		this.advRewardList = advRewardList;
		
		List<RewardItem.Builder> payRewardList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(payReward)) {
			payRewardList = RewardHelper.toRewardItemImmutableList(payReward);
		}
		this.payRewardList = payRewardList;
		
		return true;
	}
}
