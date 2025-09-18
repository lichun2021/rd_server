package com.hawk.activity.type.impl.newyearlottery.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/new_year_lottery/new_year_lottery_gift.xml")
public class NewyearLotteryGiftCfg extends HawkConfigBase {
	@Id
	private final int id;
	/** 类型 */
	private final int lotteryType;
	/** 自选奖励列表 */
	private final String selectedReward;
	/** 固定奖励列表*/
	private final String regularRewards;
	/** 随机奖励列表*/
	private final String randomRewards;
	
	private List<String> randomRewardItems = new ArrayList<>();
	private List<Integer> randomRewardWeight = new ArrayList<>();
	private static Map<Integer, NewyearLotteryGiftCfg> giftDefaultRewardMap = new HashMap<>();

	public NewyearLotteryGiftCfg() {
		id = 0;
		lotteryType = 0;
		selectedReward = "";
		regularRewards = "";
		randomRewards = "";
	}
	
	public int getId() {
		return id;
	}

	public int getLotteryType() {
		return lotteryType;
	}

	public String getSelectedReward() {
		return selectedReward;
	}

	public String getRegularRewards() {
		return regularRewards;
	}

	public String getRandomRewards() {
		return randomRewards;
	}
	
	public List<String> getRandomRewardItems() {
		return Collections.unmodifiableList(randomRewardItems);
	}
	
	public List<Integer> getRandomRewardWeight() {
		return Collections.unmodifiableList(randomRewardWeight);
	}

	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(selectedReward)) {
			return false;
		}
		if (HawkOSOperator.isEmptyString(regularRewards)) {
			return false;
		}
		if (HawkOSOperator.isEmptyString(randomRewards)) {
			return false;
		}
		
		String[] strArr = randomRewards.split(",");
		for (String str : strArr) {
			String[] split = str.split("_");
			if (split.length != 4) {
				return false;
			}
			randomRewardItems.add(String.format("%s_%s_%s", split[0], split[1], split[2]));
			randomRewardWeight.add(Integer.parseInt(split[3]));
		}
		
		if (!giftDefaultRewardMap.containsKey(lotteryType)) {
			giftDefaultRewardMap.put(lotteryType, this);
		}
		
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(selectedReward);
		if (!valid) {
			throw new InvalidParameterException(String.format("NewYearLotteryGiftCfg reward error, id: %s , selectedReward: %s", id, selectedReward));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(regularRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("NewYearLotteryGiftCfg reward error, id: %s , regularRewards: %s", id, regularRewards));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(randomRewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("NewYearLotteryGiftCfg reward error, id: %s , randomRewards: %s", id, randomRewards));
		}
		return super.checkValid();
	}
	
	public static NewyearLotteryGiftCfg getDefaultRewardGiftCfg(int type) {
		return giftDefaultRewardMap.get(type);
	}
	
	public static Set<Integer> getLotteryTypes() {
		return giftDefaultRewardMap.keySet();
	}
}
