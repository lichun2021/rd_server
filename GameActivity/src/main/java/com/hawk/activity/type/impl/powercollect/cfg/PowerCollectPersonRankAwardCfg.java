package com.hawk.activity.type.impl.powercollect.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/super_lab/super_lab_personal_rank_award.xml")
public class PowerCollectPersonRankAwardCfg extends HawkConfigBase {
	
	private final int id;
	
	private final int rankHigh;
	
	private final int rankLow;
	
	private final String gainItem;
	
	private List<RewardItem.Builder> rewardList;
	
	public PowerCollectPersonRankAwardCfg(){
		this.id = 0;
		this.rankHigh = 0;
		this.rankLow = 0;
		this.gainItem = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(gainItem);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("PowerCollectPersonRankAwardCfg reward error, id: %s , reward: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getRankHigh() {
		return rankHigh;
	}

	public int getRankLow() {
		return rankLow;
	}

	public String getGainItem() {
		return gainItem;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
