package com.hawk.activity.type.impl.buildlevel.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 可达成活动配置
 * @author PhilChen
 *
 */
@HawkConfigManager.XmlResource(file = "activity/build_level/%s/build_level_activity.xml", autoLoad=false, loadParams="6")
public class BuildLevelActivityCfg extends HawkConfigBase {
	
	/** 活动项id*/
	@Id
	private final int itemId;
	
	/** 建筑类型*/
	private final int buildType;
	
	/** 建筑等级*/
	private final int level;
	
	/** 达成奖励列表(通用奖励格式)*/
	private final String rewards;
	
	private List<RewardItem.Builder> rewardList;

	public BuildLevelActivityCfg() {
		itemId = 0;
		buildType = 0;
		level = 0;
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
			throw new InvalidParameterException(String.format("BuildLevelActivityCfg reward error, itemId: %s , rewards: %s", itemId, rewards));
		}
		return super.checkValid();
	}

	public int getItemId() {
		return itemId;
	}

	public int getBuildType() {
		return buildType;
	}

	public int getLevel() {
		return level;
	}

	public String getRewards() {
		return rewards;
	}

	public List<RewardItem.Builder> getRewardList() {
		return rewardList;
	}
}
