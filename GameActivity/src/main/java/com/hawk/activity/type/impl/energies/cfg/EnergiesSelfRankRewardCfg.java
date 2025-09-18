package com.hawk.activity.type.impl.energies.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 排名奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/energies/energies_personal_rank_award.xml")
public class EnergiesSelfRankRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 高排名*/
	private final int rankHigh;
	/** 低排名*/
	private final int rankLow;
	/** 奖励列表*/
	private final String gainItem;
	
	private List<RewardItem.Builder> rewardList;
	
	public EnergiesSelfRankRewardCfg() {
		id = 0;
		rankHigh = 0;
		rankLow = 0;
		gainItem = "";
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
			throw new InvalidParameterException(String.format("EnergiesSelfRankRewardCfg reward error, id: %s , reward: %s", id, gainItem));
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

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
}
