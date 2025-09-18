package com.hawk.activity.type.impl.playerComeBack.cfg.buy;

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
 * 低折回馈
 * @author yang.rao
 */

@HawkConfigManager.XmlResource(file = "activity/return_activity/return_activity_108.xml")
public class PlayerComeBackBuyConfig extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final int limit;
	
	private final String price;
	
	private final String rewards;
	
	private List<RewardItem.Builder> gainItemList;

	private List<RewardItem.Builder> priceList;
	
	public PlayerComeBackBuyConfig(){
		id = 0;
		limit = 0;
		price = "";
		rewards = "";
	}
	
	public boolean assemble() {
		try {
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.rewards);
			this.priceList = RewardHelper.toRewardItemImmutableList(this.price);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}

	public int getId() {
		return id;
	}

	public int getLimit() {
		return limit;
	}

	public String getPrice() {
		return price;
	}

	public String getRewards() {
		return rewards;
	}
	
	/***
	 * 构建价格
	 * 
	 * @param buyCnt
	 * @return
	 */
	public List<RewardItem.Builder> buildPrize(int buyCnt) {
		List<RewardItem.Builder> list = new ArrayList<RewardItem.Builder>();
		if (buyCnt <= 0) {
			return list;
		}
		for (int i = 0; i < buyCnt; i++) {
			list.addAll(RewardHelper.toRewardItemList(this.price));
		}
		return list;
	}
	
	public List<RewardItem.Builder> getGainItemList() {
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : gainItemList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}

	public List<RewardItem.Builder> getNeedItemList() {
		List<RewardItem.Builder> list = new ArrayList<>();
		for(RewardItem.Builder builder : priceList){
			RewardItem.Builder clone = RewardItem.newBuilder();
			clone.setItemId(builder.getItemId());
			clone.setItemType(builder.getItemType());
			clone.setItemCount(builder.getItemCount());
			list.add(clone);
		}
		return list;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(price);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , needItem: %s", id, price));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("ActivityExchangeCfg reward error, id: %s , gainItem: %s", id, rewards));
		}
		return super.checkValid();
	}
}
