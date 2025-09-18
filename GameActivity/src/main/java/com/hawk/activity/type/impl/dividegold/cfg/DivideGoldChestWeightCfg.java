package com.hawk.activity.type.impl.dividegold.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**瓜分金币宝箱开福字数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/divide_gold/dividegold_chest_weight.xml")
public class DivideGoldChestWeightCfg extends HawkConfigBase{
	@Id
	private final int id;
	//奖励
	private final String reward;
	//权重
	private final int weight;
	
	private List<RewardItem.Builder> rewardItem;

	public DivideGoldChestWeightCfg() {
		id = 0;
		reward = "";
		weight = 0;
	}

	@Override
	protected boolean assemble() {
		try {
			rewardItem = RewardHelper.toRewardItemImmutableList(reward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	public int getId() {
		return id;
	}

	public String getReward() {
		return reward;
	}

	public int getWeight() {
		return weight;
	}

	public List<RewardItem.Builder> getRewardItem() {
		return rewardItem;
	}

}
