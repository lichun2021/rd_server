package com.hawk.activity.type.impl.machineAwakeTwo.cfg;

import java.security.InvalidParameterException;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.machineAwakeTwo.MachineAwakeTwoActivity;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 排名奖励配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/machine_awake_two/machine_awake_two_rank_award.xml")
public class MachineAwakeTwoRankRewardCfg extends HawkConfigBase {
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

	private final String ghostRewards;

	private List<RewardItem.Builder> rewardList;
	private List<RewardItem.Builder> ghostRewardList;

	public MachineAwakeTwoRankRewardCfg() {
		id = 0;
		rankType = 0;
		rankUpper = 0;
		rankLower = 0;
		rewards = "";
		ghostRewards = "";
	}

	@Override
	protected boolean assemble() {
		try {
			rewardList = RewardHelper.toRewardItemImmutableList(rewards);
			ghostRewardList = RewardHelper.toRewardItemImmutableList(ghostRewards);
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
			throw new InvalidParameterException(String.format("MachineAwakeRankRewardCfg reward error, id: %s , reward: %s", id, rewards));
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
		
		ActivityManager instance = ActivityManager.getInstance();
		if (instance == null) {
			return rewardList;
		}

		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.MACHINE_AWAKE_TWO_VALUE);
		if(opActivity.isPresent()){
			MachineAwakeTwoActivity activity = (MachineAwakeTwoActivity)opActivity.get();
			if (activity.isGhost()) {
				return ghostRewardList;
			}
		}
		
		return rewardList;
	}

}
