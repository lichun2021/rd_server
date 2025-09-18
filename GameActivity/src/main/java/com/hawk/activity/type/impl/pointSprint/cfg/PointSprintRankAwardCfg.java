package com.hawk.activity.type.impl.pointSprint.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/point_sprint/point_sprint_rank.xml")
public class PointSprintRankAwardCfg extends HawkConfigBase {
	/**
	 * 奖励组id
	 */
	@Id
	private final int id;
	
	/**
	 * 排名上限
	 */
	private final int rankUpper;
	
	/**
	 * 排名下限
	 */
	private final int rankLower;
	
	/**
	 * 排名奖励
	 */
	private final String item;

	private List<RewardItem.Builder> rewardList;
	
	/**
	 * 构造
	 */
	public PointSprintRankAwardCfg() {
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

	public String getItem() {
		return item;
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(item);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(item);
		if (!valid) {
			throw new InvalidParameterException(String.format("PointSprintRankAwardCfg reward error, id: %s , reward: %s", id, item));
		}
		return super.checkValid();
	}
	
	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
