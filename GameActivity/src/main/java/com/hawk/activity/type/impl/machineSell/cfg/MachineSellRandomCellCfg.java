
package com.hawk.activity.type.impl.machineSell.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 机甲破世 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/machine_sell/machine_sell_random_cell.xml")
public class MachineSellRandomCellCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	private final String reward;
	
	private final int rate;
	
	private List<RewardItem.Builder> rewardList;
	
	public MachineSellRandomCellCfg() {
		id = 0;
		reward = "";
		rate = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(reward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("machine_sell_random_cell reward error, id: %s , rewards: %s", id, reward));
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

	public int getRate() {
		return rate;
	}

	public static int getTotalWeight() {
		int ret = 0;
		ConfigIterator<MachineSellRandomCellCfg> iter = HawkConfigManager.getInstance().getConfigIterator(MachineSellRandomCellCfg.class);
		while(iter.hasNext()){
			ret += iter.next().getRate();
		}
		return ret;
	}
}
