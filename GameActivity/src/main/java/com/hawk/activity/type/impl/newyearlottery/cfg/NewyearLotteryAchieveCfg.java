package com.hawk.activity.type.impl.newyearlottery.cfg;

import java.util.HashMap;
import java.util.Map;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

@HawkConfigManager.XmlResource(file = "activity/new_year_lottery/new_year_lottery_achieve.xml")
public class NewyearLotteryAchieveCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int lotteryType;
	
	private final int Value;
	
	/** 奖励列表*/
	private final String rewards;

	private static Table<Integer, Integer, NewyearLotteryAchieveCfg> table = HashBasedTable.create();
	private static Map<Integer, Integer> maxConditionValMap = new HashMap<>();
	
	public NewyearLotteryAchieveCfg() {
		id = 0;
		lotteryType = 0;
		Value = 0;
		rewards = "";
	}
	
	public int getId() {
		return id;
	}

	public int getValue() {
		return Value;
	}

	public int getLotteryType() {
		return lotteryType;
	}
	
	public String getRewards() {
		return rewards;
	}
	
	@Override
	protected boolean assemble() {
		table.put(lotteryType, Value, this);
		int maxVal = maxConditionValMap.getOrDefault(lotteryType, 0);
		if (Value > maxVal) {
			maxConditionValMap.put(lotteryType, Value);
		}
		return true;
	}
	
	public static NewyearLotteryAchieveCfg getCfg(int lotteryType, int count) {
		return table.get(lotteryType, count);
	}
	
	public static int getMaxConditionVal(int lotteryType) {
		return maxConditionValMap.get(lotteryType);
	}
	
 }
