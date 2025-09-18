package com.hawk.activity.type.impl.airdrop.cfg;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
@HawkConfigManager.KVResource(file = "activity/airdrop_supply/airdrop_supply_cfg.xml")
public class AirdropSupplyActivityKVCfg extends HawkConfigBase {
	private final int serverDelay;
	//普通奖励
	private final String commonReward;
	//高级奖励
	private final String highReward;
	
	private List<RewardItem.Builder> commonRewardList;
	private List<RewardItem.Builder> highRewardList;
	
	
	public AirdropSupplyActivityKVCfg() {
		this.serverDelay = 0;
		this.commonReward = "";
		this.highReward ="";
	}

	@Override
	protected boolean assemble() {
		try {
			commonRewardList = RewardHelper.toRewardItemImmutableList(commonReward);
			highRewardList = RewardHelper.toRewardItemImmutableList(highReward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public long getServerDelay() {
		return ((long)serverDelay) * 1000;
	}

	public String getCommonReward() {
		return commonReward;
	}

	public String getHighReward() {
		return highReward;
	}

	public List<RewardItem.Builder> getCommonRewardList() {
		return commonRewardList;
	}

	public List<RewardItem.Builder> getHighRewardList() {
		return highRewardList;
	}


}
