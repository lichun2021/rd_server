package com.hawk.game.config;

import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.hawk.game.player.Player;

@HawkConfigManager.XmlResource(file = "xml/alliance_care.xml")
public class AllianceCareCfg extends HawkConfigBase {
	@Id
	protected final int cityLevel;// ="1"
	protected final int id;
	protected final double powerCoefficientPercent;// ="0.30" 警戒线, 损失超过该值触发补偿
	protected final double allianceMemberUpLimit;// 0.01 盟友帮助百分比【浮点小数】
	protected final double maxPercent;// ="0.05" 最大奖励提升百分比. 每有一人帮助提升1%
	protected final int minGrandTotal;// ="500" 脱敏阈值 . 当累计数量低于该值, 不可触发
	protected final double overwhelmingRate;// 碾压系数【浮点小数】
	protected final double overwhelmingPay;// 碾压关怀加成【浮点小数】
	protected final String rewardCheckCondition1;// ="830000|830001|830002|830003|830004|830005|830006"
	protected final String compensationItem1;// ="30000_830005_1"
	protected final String rewardCheckCondition2;// ="800100|800101|800134"
	protected final String compensationItem2;// ="30000_800100_1"
	protected final double injuredSoldierSpeedUpCoefficient;// ="0.6"
	protected final double injuredSoldierResourceCoefficient;// ="0.6"
	protected final double deadSoldierSpeedUpCoefficient;// ="0.6"
	protected final double deadSoldierResourceCoefficient;// ="0.6"
	protected final double pyrrhicVictoryPercent;// 0.85
	protected final int allianceCareHelpDailyLimit;
	private ImmutableList<Integer> rewardCheckCondition1List;
	private ImmutableList<Integer> rewardCheckCondition2List;

	public AllianceCareCfg() {
		id = 0;
		cityLevel = 1;
		powerCoefficientPercent = 30;// 警戒线, 损失超过该值触发补偿
		maxPercent = 5; // 最大奖励提升百分比. 每有一人帮助提升1%
		minGrandTotal = 500;// 脱敏阈值 . 当累计数量低于该值, 不可触发
		injuredSoldierSpeedUpCoefficient = 0.6;
		injuredSoldierResourceCoefficient = 0.6;
		deadSoldierSpeedUpCoefficient = 0.6;
		deadSoldierResourceCoefficient = 0.6;
		allianceMemberUpLimit = 0.01;
		overwhelmingRate = 1;
		overwhelmingPay = 0.01;
		pyrrhicVictoryPercent = 0;
		rewardCheckCondition1 = "";
		rewardCheckCondition2 = "";
		compensationItem1 = "";
		compensationItem2 = "";
		allianceCareHelpDailyLimit = 5;
	}

	@Override
	protected boolean assemble() {
		{

			List<Integer> list = Splitter.on("|").omitEmptyStrings().splitToList(rewardCheckCondition1).stream()
					.map(Integer::valueOf)
					.collect(Collectors.toList());
			rewardCheckCondition1List = ImmutableList.copyOf(list);
		}
		{

			List<Integer> list = Splitter.on("|").omitEmptyStrings().splitToList(rewardCheckCondition2).stream()
					.map(Integer::valueOf)
					.collect(Collectors.toList());
			rewardCheckCondition2List = ImmutableList.copyOf(list);
		}
		return super.assemble();
	}

	public boolean nonRewardCheckCondition1Item(Player player) {
		for (Integer itemId : rewardCheckCondition1List) {
			if (player.getData().getItemNumByItemId(itemId) > 0) {
				return false;
			}
		}
		return true;
	}

	public boolean nonRewardCheckCondition2Item(Player player) {
		for (Integer itemId : rewardCheckCondition2List) {
			if (player.getData().getItemNumByItemId(itemId) > 0) {
				return false;
			}
		}
		return true;
	}

	public int getCityLevel() {
		return cityLevel;
	}

	public double getInjuredSoldierSpeedUpCoefficient() {
		return injuredSoldierSpeedUpCoefficient;
	}

	public double getInjuredSoldierResourceCoefficient() {
		return injuredSoldierResourceCoefficient;
	}

	public double getDeadSoldierSpeedUpCoefficient() {
		return deadSoldierSpeedUpCoefficient;
	}

	public double getDeadSoldierResourceCoefficient() {
		return deadSoldierResourceCoefficient;
	}

	public double getAllianceMemberUpLimit() {
		return allianceMemberUpLimit;
	}

	public double getOverwhelmingRate() {
		return overwhelmingRate;
	}

	public double getOverwhelmingPay() {
		return overwhelmingPay;
	}

	public String getCompensationItem1() {
		return compensationItem1;
	}

	public String getCompensationItem2() {
		return compensationItem2;
	}

	public double getPowerCoefficientPercent() {
		return powerCoefficientPercent;
	}

	public double getMaxPercent() {
		return maxPercent;
	}

	public int getMinGrandTotal() {
		return minGrandTotal;
	}

	public double getPyrrhicVictoryPercent() {
		return pyrrhicVictoryPercent;
	}

	public int getAllianceCareHelpDailyLimit() {
		return allianceCareHelpDailyLimit;
	}

}
