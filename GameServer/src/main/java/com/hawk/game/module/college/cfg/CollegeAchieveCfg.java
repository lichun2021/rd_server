package com.hawk.game.module.college.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.service.college.CollegeMissionType;

/**
 * 军事学院在线时长奖励
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/college_achieve.xml")
public class CollegeAchieveCfg extends HawkConfigBase {
	@Id
	private final int achieveId;
	private final int conditionType;
	private final int conditionValue;

	private final String collegeScore;
	private final int collegeExp;

	/** 达成奖励列表(通用奖励格式)*/
	private final String rewards;

	private CollegeMissionType missionType;

	public CollegeAchieveCfg() {
		achieveId = 0;
		conditionType = 0;
		conditionValue = 0;
		rewards = "";
		collegeScore = "";
		collegeExp = 0;
	}

	@Override
	protected boolean assemble() {
		missionType = CollegeMissionType.valueOf(conditionType);
		if (missionType == null) {
			return false;
		}
		return super.assemble();
	}

	public int getAchieveId() {
		return achieveId;
	}

	public int getConditionType() {
		return conditionType;
	}

	public int getConditionValue() {
		return conditionValue;
	}

	public String getCollegeScore() {
		return collegeScore;
	}

	public int getCollegeExp() {
		return collegeExp;
	}

	public String getRewards() {
		return rewards;
	}

	public CollegeMissionType getMissionType() {
		return missionType;
	}
	
	public int getCollegeScoreCount(){
		RewardItem.Builder builder = RewardHelper.toRewardItem(this.collegeScore);
		return (int) builder.getItemCount();
	}

}
