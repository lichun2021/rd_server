package com.hawk.activity.type.impl.dressuptwo.firereignitetwo.cfg;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward;
import com.hawk.gamelib.activity.ConfigChecker;
import com.hawk.serialize.string.SerializeHelper;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Map;

/**
 * 圣诞节系列活动二:冬日装扮活动
 * @author hf
 */
@HawkConfigManager.KVResource(file = "activity/christmas_winter_dress/christmas_winter_dress_cfg.xml")
public class FireReigniteTwoActivityKVCfg extends HawkConfigBase {

	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	/**每个宝箱对应的经验*/
	private final int boxExp;

	/**
	 * 购买兑换限制
	 */
	private final int exchangeLimit;

	/**每个冬日长袜消耗金条数*/
	private final String itemPrice;

	/**奖励暴击几率*/
	private final String strength;

	/**超过等级上限后,每个宝箱的奖励*/
	private final String boxReward;



	//奖励暴击权重map
	private Map<Integer, Integer> strengthMap;
	private List<Reward.RewardItem.Builder> boxRewardList;

	public FireReigniteTwoActivityKVCfg() {
		serverDelay = 0;
		boxExp = 0;
		exchangeLimit = 0;
		itemPrice = "";
		strength =  "";
		boxReward = "";
	}

	@Override
	protected boolean assemble() {
		try {
			this.strengthMap = SerializeHelper.stringToMap(strength, Integer.class, Integer.class, SerializeHelper.ATTRIBUTE_SPLIT, SerializeHelper.SEMICOLON_ITEMS);
			boxRewardList = RewardHelper.toRewardItemImmutableList(boxReward);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}


	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(itemPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("FireReigniteTwoActivityKVCfg reward error, itemPrice: %s", itemPrice));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(boxReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("FireReigniteTwoActivityKVCfg reward error, boxReward: %s", boxReward));
		}
		return super.checkValid();
	}
	public int getBoxExp() {
		return boxExp;
	}


	public int getExchangeLimit() {
		return exchangeLimit;
	}

	public String getItemPrice() {
		return itemPrice;
	}

	public String getStrength() {
		return strength;
	}

	public Map<Integer, Integer> getStrengthMap() {
		return strengthMap;
	}

	public List<Reward.RewardItem.Builder> getBoxRewardList() {
		return boxRewardList;
	}
}
