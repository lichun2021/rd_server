package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 排名奖励配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_season_rank_reward.xml")
public class CrossSeasonRankRewardCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 高排名*/
	private final int rankUpper;
	/** 低排名*/
	private final int rankLower;
	/** 奖励列表*/
	private final String reward;
	
	
	public CrossSeasonRankRewardCfg() {
		id = 0;
		rankUpper = 0;
		rankLower = 0;
		reward = "";
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("CrossSeasonRankRewardCfg reward error, id: %s , reward: %s", id, reward));
		}
		return super.checkValid();
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
	
	public String getReward() {
		return reward;
	}
	

	public List<ItemInfo> getRewardList() {
		return ItemInfo.valueListOf(this.reward);
	}
	
}
