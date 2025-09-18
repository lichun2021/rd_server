package com.hawk.activity.type.impl.midAutumn.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**中秋庆典礼包数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/mid_autumn/mid_autumn_gift.xml")
public class MidAutumnGiftCfg extends HawkConfigBase{
	@Id
	private final int giftId;
	
	private final int chooseItems;
	
	private final String cost;
	
	private List<RewardItem.Builder> costItemList;
	
	public MidAutumnGiftCfg(){
		giftId = 0;
		chooseItems = 0;
		cost = "";
	}

	public boolean assemble() {
		try {
			this.setCostItemList(RewardHelper.toRewardItemImmutableList(this.cost));
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(cost);
		if (!valid) {
			throw new InvalidParameterException(String.format("MidAutumnGiftCfg reward error, giftId: %s , cost: %s", giftId, cost));
		}
		return super.checkValid();
	}

	public int getGiftId() {
		return giftId;
	}


	public int getChooseItems() {
		return chooseItems;
	}


	public String getCost() {
		return cost;
	}

	public List<RewardItem.Builder> getCostItemList() {
		return costItemList;
	}

	public void setCostItemList(List<RewardItem.Builder> costItemList) {
		this.costItemList = costItemList;
	}
	
	
}
