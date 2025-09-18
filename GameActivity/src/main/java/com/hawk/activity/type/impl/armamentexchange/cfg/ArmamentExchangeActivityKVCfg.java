package com.hawk.activity.type.impl.armamentexchange.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;

/**
 * 配置
 * @author luke
 *
 */
@HawkConfigManager.KVResource(file = "activity/arms_upgrade/arms_upgrade_cfg.xml")
public class ArmamentExchangeActivityKVCfg extends HawkConfigBase {
	//服务器开服延时开启活动时间
	private final int serverDelay;

	/**
	 * android
	 */
	private final String androidPayId;
	/**
	 * ios
	 */
	private final String iosPayId;
	/**
	 * 解锁高级宝箱一次奖励
	 */
	private final String unlockReward;
	
	private List<RewardItem.Builder> awardItems;
	
	public ArmamentExchangeActivityKVCfg() {
		serverDelay = 0;
		androidPayId="";
		iosPayId="";
		unlockReward="";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getUnlockReward() {
		return unlockReward;
	}

	public List<RewardItem.Builder> getAwardItems() {
		return awardItems;
	}

	@Override
	protected boolean assemble() {
		awardItems = RewardHelper.toRewardItemImmutableList(unlockReward);
		return true;
	}

}
