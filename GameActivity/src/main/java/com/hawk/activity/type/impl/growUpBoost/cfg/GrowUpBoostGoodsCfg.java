package com.hawk.activity.type.impl.growUpBoost.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 *  中部养成计划
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/grow_up_boost/grow_up_boost_goods.xml")
public class GrowUpBoostGoodsCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	private final int configId;
	private final int group;
	private final String needItem;
	private final int awardId;
	private final int buyLimit;

	public GrowUpBoostGoodsCfg() {
		id = 0;
		configId = 0;
		group = 0;
		needItem = "";
		awardId = 0;
		buyLimit = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}

	public int getConfigId() {
		return configId;
	}

	public int getGroup() {
		return group;
	}

	public int getAwardId() {
		return awardId;
	}


	public int getBuyLimit() {
		return buyLimit;
	}
	
	
	
	public List<RewardItem.Builder> getNeedItemList() {
		return RewardHelper.toRewardItemImmutableList(this.needItem);
	}
	

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("GrowUpBoostGoodsCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		return super.checkValid();
	}
	

}
