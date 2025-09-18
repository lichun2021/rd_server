package com.hawk.activity.type.impl.commandAcademySimplify.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.game.protocol.Reward.RewardItem;


/**
 * 指挥官学院活动配置
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/commander_college_cut/%s/commander_college_cut_rank.xml", autoLoad=false, loadParams="284")
public class CommandAcademySimplifyRankScoreCfg extends HawkConfigBase {
	/** 成就id*/
	@Id
	private final int id;
	/** 榜ID*/
	private final int rankId;
	/** 名次开始*/
	private final int rankUpper;
	/** 名次结束*/
	private final int rankLower;
	/** 评分*/
	private final int rankScore;
	/** 奖励*/
	private final String reward;
	
	private AchieveType achieveType;
	private List<RewardItem.Builder> rewardList;
	private List<Integer> conditionValueList;

	public CommandAcademySimplifyRankScoreCfg() {
		id = 0;
		rankId = 0;
		rankUpper = 0;
		rankLower = 0;
		rankScore =0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		rewardList =  RewardHelper.toRewardItemImmutableList(reward);
		return true;
	}

	public AchieveType getAchieveType() {
		return achieveType;
	}

	public void setAchieveType(AchieveType achieveType) {
		this.achieveType = achieveType;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<RewardItem.Builder> rewardList) {
		this.rewardList = rewardList;
	}

	public List<Integer> getConditionValueList() {
		return conditionValueList;
	}

	public void setConditionValueList(List<Integer> conditionValueList) {
		this.conditionValueList = conditionValueList;
	}

	public int getId() {
		return id;
	}

	public int getRankId() {
		return rankId;
	}

	public int getRankUpper() {
		return rankUpper;
	}

	public int getRankLower() {
		return rankLower;
	}

	public int getRankScore() {
		return rankScore;
	}

	public String getReward() {
		return reward;
	}
}
