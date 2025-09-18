package com.hawk.activity.type.impl.heroTrial.cfg;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 英雄试炼任务配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/hero_trial/hero_trial_mission.xml")
public class HeroTrialMissionCfg extends HawkConfigBase {

	/**
	 * 任务id
	 */
	@Id
	private final int missionId;

	/**
	 * 品质
	 */
	private final int quality;
	
	/**
	 * 基础奖励
	 */
	private final String baseAward;
	
	/**
	 * 进阶奖励
	 */
	private final String advAward;
	
	/**
	 * 基础条件
	 */
	private final String baseCondition;
	
	/**
	 * 进阶条件
	 */
	private final String advCondition;

	private final int weight;
	
	/**
	 * 持续时间
	 */
	private final int continueTime;
	
	protected List<RewardItem.Builder> baseAwardList;
	protected List<RewardItem.Builder> baseAndAdvAward;
	protected List<List<Integer>> baseConditionList;
	protected List<List<Integer>> advConditionList;
	
	public HeroTrialMissionCfg() {
		missionId = 0;
		quality = 0;
		baseAward = "";
		advAward = "";
		baseCondition = "";
		advCondition = "";
		continueTime = 0;
		weight = 0;
	}
	
	@Override
	protected boolean assemble() {
		baseAwardList = RewardHelper.toRewardItemImmutableList(baseAward);
		
		List<RewardItem.Builder> baseAndAdvAward = RewardHelper.toRewardItemList(advAward);
		baseAndAdvAward.addAll(baseAwardList);
		this.baseAndAdvAward = ImmutableList.copyOf(baseAndAdvAward);
		
		List<List<Integer>> baseConditionList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(baseCondition)) {
			String[] split1 = baseCondition.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				
				List<Integer> baseCondition = new ArrayList<>();
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				for (int j = 0; j < split2.length; j++) {
					baseCondition.add(Integer.valueOf(split2[j]));
				}
				
				baseConditionList.add(baseCondition);
			}
		}
		this.baseConditionList = baseConditionList;
		
		
		List<List<Integer>> advConditionList = new ArrayList<>();
		if (!HawkOSOperator.isEmptyString(advCondition)) {
			String[] split1 = advCondition.split(SerializeHelper.BETWEEN_ITEMS);
			for (int i = 0; i < split1.length; i++) {
				
				List<Integer> advCondition = new ArrayList<>();
				String[] split2 = split1[i].split(SerializeHelper.ATTRIBUTE_SPLIT);
				for (int j = 0; j < split2.length; j++) {
					advCondition.add(Integer.valueOf(split2[j]));
				}
				
				advConditionList.add(advCondition);
			}
		}
		this.advConditionList = advConditionList;
		
		return true;
	}

	public List<RewardItem.Builder> getBaseAwardList() {
		return baseAwardList;
	}

	public List<RewardItem.Builder> getBaseAndAdvAward() {
		return baseAndAdvAward;
	}

	public List<List<Integer>> getBaseConditionList() {
		return baseConditionList;
	}

	public List<List<Integer>> getAdvConditionList() {
		return advConditionList;
	}

	public int getMissionId() {
		return missionId;
	}

	public int getQuality() {
		return quality;
	}
	
	public long getContinueTime() {
		return continueTime * 1000L;
	}

	public int getWeight() {
		return weight;
	}
}
