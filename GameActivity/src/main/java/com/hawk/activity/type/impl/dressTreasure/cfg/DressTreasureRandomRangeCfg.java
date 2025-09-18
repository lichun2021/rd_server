package com.hawk.activity.type.impl.dressTreasure.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/dress_treasure/dress_treasure_random_range.xml")
public class DressTreasureRandomRangeCfg extends HawkConfigBase{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	private final int rangeStart;
	
	private final int rangeEnd;
	
	private final String randomCost;
	


	public DressTreasureRandomRangeCfg() {
		id = 0;
		rangeStart = 0;
		rangeEnd = 0;
		randomCost = "";
	}


	public List<RewardItem.Builder> getRandomCostList() {
		return RewardHelper.toRewardItemImmutableList(this.randomCost);
	}

	public int getId() {
		return id;
	}

	public int getRangeStart() {
		return rangeStart;
	}

	public int getRangeEnd() {
		return rangeEnd;
	}

	public String getRandomCost() {
		return randomCost;
	}


	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(randomCost);
		if (!valid) {
			throw new InvalidParameterException(String.format("DressTreasureRandomRangeCfg reward error, id: %s , needItem: %s", id, randomCost));
		}
		
		List<DressTreasureRandomRangeCfg> list = HawkConfigManager.getInstance().
				getConfigIterator(DressTreasureRandomRangeCfg.class).toList();
		int count = list.size();
		for(int i =1;i<= count;i++){
			DressTreasureRandomRangeCfg cfg = list.get(i-1);
			if(cfg.getId() != i){
				throw new InvalidParameterException(String.format("DressTreasureRandomRangeCfg id error, id: %s ", i));
			}
		}
		return super.checkValid();
	}

	
	
}
