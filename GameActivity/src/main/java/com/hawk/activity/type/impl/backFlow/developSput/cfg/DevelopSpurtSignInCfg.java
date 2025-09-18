package com.hawk.activity.type.impl.backFlow.developSput.cfg;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/***
 * 回归签到配置
 * 
 * @author lating
 *
 */

@HawkConfigManager.XmlResource(file = "activity/return_develop/return_develop_sign.xml")
public class DevelopSpurtSignInCfg extends HawkConfigBase {
	
	
	@Id
	private final int id;
	
	/**
	 * 签到天数
	 */
	private final int day;
	
	/**
	 * 类型
	 */
	private final int playerType;
	
	/**
	 * 普通奖励
	 */
	private final String commonRewards;
	
	/**
	 * 进阶奖励
	 */
	private final String advancedReward;
	

	
	private List<RewardItem.Builder> rewardList;
	
	private List<RewardItem.Builder> advancedRewardList;
	
	public DevelopSpurtSignInCfg(){
		id = 0;
		day = 0;
		playerType = 0;
		commonRewards = "";
		advancedReward = "";
	}
	
	@Override
	protected boolean assemble() {
		
		rewardList = RewardHelper.toRewardItemImmutableList(commonRewards);
		advancedRewardList = RewardHelper.toRewardItemImmutableList(advancedReward);
		return true;
	}

	public int getDay() {
		return day;
	}




	public List<RewardItem.Builder> getRewardList() {
		return rewardList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public List<RewardItem.Builder> getAdvancedRewardList() {
		return advancedRewardList.stream().map(e -> e.clone()).collect(Collectors.toList());
	}

	public int getPlayerType() {
		return playerType;
	}

	
	
	
}
