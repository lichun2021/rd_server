package com.hawk.activity.type.impl.firstRecharge.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 首充奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/first_recharge/first_recharge_reward.xml")
public class FirstRechargeRewardCfg extends HawkConfigBase {

	@Id
	private final int id;
	
	/**
	 * 额外奖励大本等级限制
	 */
	private final int cityLvlLimit;
	
	/**
	 * 普通奖励
	 */
	private final String commonAward;
	
	/**
	 * 额外奖励
	 */
	private final String extrAward;

	/**
	 * 普通奖励
	 */
	private List<RewardItem.Builder> commonAwardList;
	
	/**
	 * 额外奖励
	 */
	private List<RewardItem.Builder> extrAwardList;
	
	public FirstRechargeRewardCfg() {
		this.id = 0;
		this.cityLvlLimit = 0;
		this.commonAward = "";
		this.extrAward = "";
	}
	
	public int getId() {
		return id;
	}

	public int getCityLvlLimit() {
		return cityLvlLimit;
	}

	public String getCommonAward() {
		return commonAward;
	}

	public String getExtrAward() {
		return extrAward;
	}
	
	@Override
	protected boolean assemble() {
		try {
			commonAwardList = RewardHelper.toRewardItemImmutableList(commonAward);
			extrAwardList = RewardHelper.toRewardItemImmutableList(extrAward);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(commonAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("FirstRechargeRewardCfg reward error, id: %s , commonAward: %s", id, commonAward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(extrAward);
		if (!valid) {
			throw new InvalidParameterException(String.format("FirstRechargeRewardCfg reward error, id: %s , extrAward: %s", id, extrAward));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getCommonAwardList() {
		return commonAwardList;
	}

	public List<RewardItem.Builder> getExtrAwardList() {
		return extrAwardList;
	}
}
