package com.hawk.activity.type.impl.dressup.firereignite.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.security.InvalidParameterException;
import java.util.List;

/**
 * 装扮投放系列活动三:重燃战火  配置
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/dress_war_reburning/dress_war_reburning_cfg.xml")
public class FireReigniteActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**每个宝箱对应的经验*/
	private final int boxExp;

	/**每个宝箱对应的奖励*/
	private final String boxReward;
	/**
	 * 购买兑换限制
	 */
	private final int exchangeLimit;


	private List<Reward.RewardItem.Builder> boxRewardList;

	public FireReigniteActivityKVCfg() {
		serverDelay = 0;
		boxExp = 0;
		boxReward = "";
		exchangeLimit = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		try {
			boxRewardList = RewardHelper.toRewardItemImmutableList(boxReward);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(boxReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("FireReigniteActivityKVCfg reward error, boxReward: %s", boxReward));
		}
		return super.checkValid();
	}
	public int getBoxExp() {
		return boxExp;
	}

	public String getBoxReward() {
		return boxReward;
	}

	public int getExchangeLimit() {
		return exchangeLimit;
	}

	public List<Reward.RewardItem.Builder> getBoxRewardList() {
		return boxRewardList;
	}

	public void setBoxRewardList(List<Reward.RewardItem.Builder> boxRewardList) {
		this.boxRewardList = boxRewardList;
	}
}
