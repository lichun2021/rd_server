package com.hawk.activity.type.impl.dressTreasure.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/dress_treasure/dress_treasure_award.xml")
public class DressTreasureAwardCfg extends HawkConfigBase implements HawkRandObj{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	/**
	 * 奖励
	 */
	private final String awards;
	/**
	 * 权重
	 */
	private final int weight;
	
	

	public DressTreasureAwardCfg() {
		id = 0;
		awards = "";
		weight = 0;
	}

	

	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getAwardList() {
		return RewardHelper.toRewardItemImmutableList(this.awards);
	}

	@Override
	public int getWeight() {
		return weight;
	}

	
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(awards);
		if (!valid) {
			throw new InvalidParameterException(String.format("DressTreasureAwardCfg reward error, id: %s , needItem: %s", id, awards));
		}
		return super.checkValid();
	}

	
	
}
