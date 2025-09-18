package com.hawk.activity.type.impl.luckyBox.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * @author Richard
 */
@HawkConfigManager.XmlResource(file = "activity/lucky_box/lucky_box_reward.xml")
public class LuckyBoxRewardCfg extends HawkConfigBase implements HawkRandObj{
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;
	/**
	 * 实际的奖励
	 */
	private final String reward;

	public LuckyBoxRewardCfg() {
		id = 0;
		reward = "";
	}

	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getRewardList() {
		return RewardHelper.toRewardItemImmutableList(this.reward);
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(reward);
		if (!valid) {
			throw new InvalidParameterException(String.format("LuckyBoxRewardCfg reward error, id: %s , needItem: %s", id, reward));
		}
		return super.checkValid();
	}

	@Override
	public int getWeight() {
		return 0;
	}
}
