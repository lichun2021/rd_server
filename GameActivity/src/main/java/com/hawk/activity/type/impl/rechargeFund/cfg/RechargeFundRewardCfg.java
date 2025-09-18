package com.hawk.activity.type.impl.rechargeFund.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

@XmlResource(file = "activity/recharge_fund/recharge_fund_reward.xml")
public class RechargeFundRewardCfg extends HawkConfigBase {
	@Id
	private final int id;

	private final int giftId;

	private final int loginDay;

	private final String goldBar;

	private final String fixedRewards;

	private final String chooseRewards;

	private final int chooseNum;

	private List<RewardItem.Builder> goldBarList;

	private List<RewardItem.Builder> fixedList;

	private List<RewardItem.Builder> chooseList;

	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getGoldBarList() {
		return goldBarList;
	}

	public List<RewardItem.Builder> getFixedList() {
		return fixedList;
	}

	public List<RewardItem.Builder> getChooseList() {
		return chooseList;
	}

	public int getGiftId() {
		return giftId;
	}

	public int getLoginDay() {
		return loginDay;
	}

	public String getGoldBar() {
		return goldBar;
	}

	public String getFixedRewards() {
		return fixedRewards;
	}

	public String getChooseRewards() {
		return chooseRewards;
	}

	public int getChooseNum() {
		return chooseNum;
	}
	
	public boolean canSelect(String itemStr){
		return this.chooseRewards.indexOf(itemStr) != -1;
	}

	public RechargeFundRewardCfg() {
		this.id = 0;
		this.giftId = 0;
		this.loginDay = 0;
		this.goldBar = "";
		this.fixedRewards = "";
		this.chooseRewards = "";
		this.chooseNum = 1;
	}

	@Override
	protected boolean assemble() {
		try {
			goldBarList = RewardHelper.toRewardItemImmutableList(goldBar);
			fixedList = RewardHelper.toRewardItemImmutableList(fixedRewards);
			chooseList = RewardHelper.toRewardItemImmutableList(chooseRewards);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	@Override
	protected boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.goldBar);
		if (!valid) {
			throw new InvalidParameterException(String.format("reward error, id: %s, Class name: %s", getId(), getClass().getName()));
		}

		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.fixedRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("reward error, id: %s, Class name: %s", getId(), getClass().getName()));
		}

		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(this.chooseRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("reward error, id: %s, Class name: %s", getId(), getClass().getName()));
		}
		return super.checkValid();
	}
}
