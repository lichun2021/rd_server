package com.hawk.activity.type.impl.militaryprepare.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 军事备战活动配置
 * @author Winder
 *
 */
@HawkConfigManager.KVResource(file = "activity/military_prepare/military_prepare_activity_cfg.xml")
public class MilitaryPrepareActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	private final String iosAdvance;
	
	private final String androidAdvance;
	
	private final String advanceReward;
	
	private List<RewardItem.Builder> rewardList; 

	public MilitaryPrepareActivityKVCfg() {
		serverDelay = 0;
		iosAdvance = "";
		androidAdvance = "";
		advanceReward = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getIosAdvance() {
		return iosAdvance;
	}

	public String getAndroidAdvance() {
		return androidAdvance;
	}
	
	
	public String getAdvanceReward() {
		return advanceReward;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}


	@Override
	protected boolean assemble() {
		rewardList = RewardHelper.toRewardItemImmutableList(advanceReward);
		return true;
	}
	
}
