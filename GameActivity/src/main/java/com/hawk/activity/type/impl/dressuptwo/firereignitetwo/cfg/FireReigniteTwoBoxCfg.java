package com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * 圣诞节系列活动二:冬日装扮活动
 */
@HawkConfigManager.XmlResource(file = "activity/christmas_winter_dress/christmas_winter_dress_box.xml")
public class FireReigniteTwoBoxCfg extends HawkConfigBase {
	/**
	 * 唯一ID 宝箱id,即等级
	 */
	@Id
	private final int boxId;

	/**
	 * 兑换给的经验
	 */
	private final int conditionExp;

	/**
	 * 宝箱等级奖励
	 */
	private final String levelRewards;

	/**
	 * 宝箱固定奖励
	 */
	private final String fixedRewards;


	private List<RewardItem.Builder> levelRewardsList;

	private List<RewardItem.Builder> fixedRewardsList;

	public FireReigniteTwoBoxCfg() {
		boxId = 0;
		conditionExp = 0;
		levelRewards = "";
		fixedRewards = "";

	}

	public int getBoxId() {
		return boxId;
	}

	public int getConditionExp() {
		return conditionExp;
	}

	public String getLevelRewards() {
		return levelRewards;
	}

	public String getFixedRewards() {
		return fixedRewards;
	}

	public List<RewardItem.Builder> getLevelRewardsList() {
		return levelRewardsList;
	}


	public List<RewardItem.Builder> getFixedRewardsList() {
		return fixedRewardsList;
	}


	public boolean assemble() {
		try {
			this.levelRewardsList = RewardHelper.toRewardItemImmutableList(this.levelRewards);
			this.fixedRewardsList = RewardHelper.toRewardItemImmutableList(this.fixedRewards);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(levelRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("FireReigniteTwoBoxCfg reward error, id: %s , needItem: %s", boxId, levelRewards));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(fixedRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("FireReigniteTwoBoxCfg reward error, id: %s , gainItem: %s", boxId, fixedRewards));
		}
		return super.checkValid();
	}

}
