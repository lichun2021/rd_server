package com.hawk.activity.type.impl.sendFlower.cfg;

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
@HawkConfigManager.XmlResource(file = "activity/send_flower/send_flower_rank_award.xml")
public class SendFlowerRankRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 排行类型*/
	private final int rankType;
	/** 高排名*/
	private final int rankUpper;
	/** 低排名*/
	private final int rankLower;
	/** 奖励列表*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;
	
	public SendFlowerRankRewardCfg() {
		id = 0;
		rankType = 0;
		rankUpper = 0;
		rankLower = 0;
		rewards = "";
	}
	
	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("SongHuaRankRewardCfg reward error, id: %s , reward: %s", id, rewards));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getRankType() {
		return rankType;
	}

	public int getRankUpper() {
		return rankUpper;
	}

	public int getRankLower() {
		return rankLower;
	}
	
	public String getRewards() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
	
}
