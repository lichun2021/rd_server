package com.hawk.activity.type.impl.newyearlottery.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.gamelib.activity.ConfigChecker;

@HawkConfigManager.XmlResource(file = "activity/new_year_lottery/new_year_lottery_slot.xml")
public class NewyearLotterySlotCfg extends HawkConfigBase {
	@Id
	private final int id;
	/** 类型*/
	private final int lotteryType;
	/** 是否是大奖 */
	private final int isBigPrize;
	/** 奖励列表*/
	private final String rewards;
	/** 权重*/
	private final int weight;
	
	private static Map<Integer, List<NewyearLotterySlotCfg>> cfgListMap = new HashMap<>();
	private static Map<Integer, List<Integer>> cfgListWeightMap = new HashMap<>();
	
	public NewyearLotterySlotCfg() {
		id = 0;
		lotteryType = 0;
		isBigPrize = 0;
		weight = 0;
		rewards = "";
	}
	
	public int getId() {
		return id;
	}

	public int getLotteryType() {
		return lotteryType;
	}

	public int getWeight() {
		return weight;
	}

	public int getIsBigPrize() {
		return isBigPrize;
	}

	public String getRewards() {
		return rewards;
	}

	@Override
	protected boolean assemble() {
		List<NewyearLotterySlotCfg> cfgList = cfgListMap.get(lotteryType);
		if (cfgList == null) {
			cfgList = new ArrayList<>();
			cfgListMap.put(lotteryType, cfgList);
		}
		cfgList.add(this);
		
		List<Integer> weightList = cfgListWeightMap.get(lotteryType);
		if (weightList == null) {
			weightList = new ArrayList<>();
			cfgListWeightMap.put(lotteryType, weightList);
		}
		
		weightList.add(this.getWeight());
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(rewards);
		if (!valid) {
			throw new InvalidParameterException(String.format("NewYearLotterySlotCfg reward error, id: %s , reward: %s", id, rewards));
		}
		return super.checkValid();
	}
	
	public static List<NewyearLotterySlotCfg> getCfgList(int lotteryType) {
		return cfgListMap.get(lotteryType);
	}
	
	public static List<Integer> getCfgWeightList(int lotteryType) {
		return cfgListWeightMap.get(lotteryType);
	}

}
