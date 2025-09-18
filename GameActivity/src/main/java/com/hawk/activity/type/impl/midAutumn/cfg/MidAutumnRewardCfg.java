package com.hawk.activity.type.impl.midAutumn.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**中秋庆典奖励
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/mid_autumn/mid_autumn_reward.xml")
public class MidAutumnRewardCfg extends HawkConfigBase{
	@Id
	private final int id;
	
	private final int giftId;
	
	private final String reward;
	
	private List<RewardItem.Builder> rewardItemList;
	
	public MidAutumnRewardCfg(){
		id = 0;
		giftId = 0;
		reward = "";
	}

	public boolean assemble() {
		try {
			rewardItemList = RewardHelper.toRewardItemImmutableList(this.reward);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("MidAutumnRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}

	public int getGiftId() {
		return giftId;
	}

	public List<RewardItem.Builder> getRewardItemList() {
		return rewardItemList;
	}

	public void setRewardItemList(List<RewardItem.Builder> rewardItemList) {
		this.rewardItemList = rewardItemList;
	}

	public int getId() {
		return id;
	}

	public String getReward() {
		return reward;
	}

}
