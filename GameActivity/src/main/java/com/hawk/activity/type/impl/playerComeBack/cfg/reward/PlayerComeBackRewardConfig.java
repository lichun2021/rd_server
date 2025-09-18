package com.hawk.activity.type.impl.playerComeBack.cfg.reward;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/***
 * 回归大礼
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/return_activity/return_activity_reward.xml")
public class PlayerComeBackRewardConfig extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	public PlayerComeBackRewardConfig(){
		id = 0;
		rewards = "";
	}

	@Override
	protected boolean assemble() {
		this.rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		return super.assemble();
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlayerComeBackRewardConfig reward error, id: %s , needItem: %s", id, rewards));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getRewards() {
		return rewards;
	}
	
	public List<RewardItem.Builder> getRewardList(){
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : rewardList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}
}
