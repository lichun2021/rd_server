package com.hawk.activity.type.impl.luckyStar.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/***
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/lucky_star/lucky_star_lottery.xml")
public class LuckyStartLotterCfg extends HawkConfigBase {
	
	
	@Id
	private final int id;
	
	private final String rewards;
	
	private final int singleWeight;
	
	private List<RewardItem.Builder> rewardList;
	
	public LuckyStartLotterCfg() {
		id = 0;
		rewards = "";
		singleWeight = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("LotteryDrawCellCfg reward error, id: %s , rewards: %s", id, rewards));
		}
		return super.checkValid();
	}
	
	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getRewardList() {
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

	public int getSingleWeight() {
		return singleWeight;
	}
}
