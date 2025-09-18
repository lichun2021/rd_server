package com.hawk.activity.type.impl.machineLab.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/machine_lab/machine_lab_rank.xml")
public class MachineLabRankCfg extends HawkConfigBase {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	
	private final int rankUpper;
	
	private final int rankLower;
	
	private final String item;
	

	public MachineLabRankCfg() {
		id = 0;
		rankUpper = 0;
		rankLower = 0;
		item = "";
	}

	public int getId() {
		return id;
	}
	
	public int getRankUpper() {
		return rankUpper;
	}
	
	public int getRankLower() {
		return rankLower;
	}

	
	public List<RewardItem.Builder> getRewardItemList() {
		return RewardHelper.toRewardItemImmutableList(this.item);
	}
	
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(item);
		if (!valid) {
			throw new InvalidParameterException(String.format("MachineLabRankCfg item error, id: %s , needItem: %s", id, item));
		}
		return super.checkValid();
	}

}
