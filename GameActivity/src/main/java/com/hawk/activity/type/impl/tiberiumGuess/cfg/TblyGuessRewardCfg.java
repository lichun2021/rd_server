package com.hawk.activity.type.impl.tiberiumGuess.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;


/**泰伯利亚联赛竞猜活动 奖励
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/tiberium_bet/tiberium_bet_reward.xml")
public class TblyGuessRewardCfg extends HawkConfigBase{
	@Id
	private final int id;
	//轮数
	private final int round;
	//奖励
	private final String rewards;
	//猜对否
	private final int isWin;
	
	private List<RewardItem.Builder> rewardItems;
	
	public TblyGuessRewardCfg(){
		id = 0;
		round = 0;
		rewards = "";
		isWin = 0;
	}

	public int getId() {
		return id;
	}

	public int getRound() {
		return round;
	}

	public String getRewards() {
		return rewards;
	}

	public int getIsWin() {
		return isWin;
	}
	public boolean isWin() {
		return isWin == 1;
	}

	@Override
	protected boolean assemble() {
		rewardItems = RewardHelper.toRewardItemImmutableList(rewards);
		return true;
	}

	public List<RewardItem.Builder> getRewardItems() {
		return rewardItems;
	}

	public void setRewardItems(List<RewardItem.Builder> rewardItems) {
		this.rewardItems = rewardItems;
	}
	
}
