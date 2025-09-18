package com.hawk.activity.type.impl.timeLimitDrop.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

@HawkConfigManager.XmlResource(file = "activity/time_limit_drop/time_limit_drop_rank_award.xml")
public class TimeLImitDropRankAwardCfg extends HawkConfigBase {
	@Id
	private final int id;	
	private final int rankHigh;
	private final int rankLow;
	private final String gainItem;
	private List<RewardItem.Builder> gainItemList;
	
	public TimeLImitDropRankAwardCfg() {
		id = 0;
		rankHigh = 0;
		rankLow = 0;
		gainItem = "";
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
	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}
	
	@Override
	public boolean assemble() {
		gainItemList = RewardHelper.toRewardItemImmutableList(gainItem);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {		
		return true;
	}
}
