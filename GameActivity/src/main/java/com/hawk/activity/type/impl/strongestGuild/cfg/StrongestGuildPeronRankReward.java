package com.hawk.activity.type.impl.strongestGuild.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/***
 * 王者联盟个人排行奖励
 * @author yang.rao
 *
 */

@HawkConfigManager.XmlResource(file = "activity/strongest_alliance/activity_allian_rank_reward.xml")
public class StrongestGuildPeronRankReward extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 排名id*/
	private final int rankId;
	/** 高排名*/
	private final int rankUpper;
	/** 低排名*/
	private final int rankLower;
	/** 奖励列表*/
	private final String reward;
	
	private List<RewardItem.Builder> rewardList;
	
	public StrongestGuildPeronRankReward() {
		id = 0;
		rankId = 0;
		rankUpper = 0;
		rankLower = 0;
		reward = "";
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
			throw new InvalidParameterException(String.format("StrongestGuildPeronRankReward reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getRankId() {
		return rankId;
	}

	public int getRankUpper() {
		return rankUpper;
	}

	public int getRankLower() {
		return rankLower;
	}
	
	public String getReward() {
		return reward;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
