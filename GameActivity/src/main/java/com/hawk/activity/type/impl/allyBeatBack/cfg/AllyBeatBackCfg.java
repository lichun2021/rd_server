package com.hawk.activity.type.impl.allyBeatBack.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.KVResource(file = "activity/ally_beat_back/activity_allybeatback_cfg.xml")
public class AllyBeatBackCfg extends HawkConfigBase {
	private final long serverDelay;
	private final String everydayAward;
	private List<RewardItem.Builder> everydayAwardList;
	
	private static AllyBeatBackCfg instance = null;
	
	public static AllyBeatBackCfg getInstance() {
		return instance;
	}
	
	public AllyBeatBackCfg() {
		serverDelay = 0l;
		everydayAward = "";
		instance = this;
	}
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	public String getEverydayAward() {
		return everydayAward;
	}
	public List<RewardItem.Builder> getEverydayAwardList() {
		return everydayAwardList;
	}
	
	@Override
	public boolean assemble() {
		everydayAwardList = RewardHelper.toRewardItemImmutableList(everydayAward);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		return ConfigChecker.getDefaultChecker().checkAwardsValid(everydayAward);		
	}
}
