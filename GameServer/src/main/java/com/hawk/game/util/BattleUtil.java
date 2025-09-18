package com.hawk.game.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.PlayerAttr;

public class BattleUtil {	
	
	public static List<ItemInfo> cureCost(Player player, List<ArmyInfo> marchArmy, double injuredSoldierSpeedUpCoefficient, double injuredSoldierResourceCoefficient) {
		List<ArmySoldierPB> cureList = marchArmy.stream()
				.map(army -> ArmySoldierPB.newBuilder().setArmyId(army.getArmyId()).setCount(army.getWoundedCount()).build())
				.collect(Collectors.toList());
		List<ItemInfo> list = new ArrayList<>();
		double recoverTime = GameUtil.recoverTime(player, cureList);
		List<ItemInfo> itemInfos = GameUtil.cureItems(player, cureList);
		// 时间道具
		int speedItemCount = (int) (recoverTime * injuredSoldierSpeedUpCoefficient / 300);
		list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getInjuredSoldierUpSpeedItem(), speedItemCount));
		for (ItemInfo res : itemInfos) { // 消耗资源补偿
			int resId = res.getItemId();
			int resCnt = (int) (res.getCount() * injuredSoldierResourceCoefficient);
			switch (resId) {
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationGold(), resCnt / 1000));
				break;
			case PlayerAttr.OIL_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationOil(), resCnt / 1000));
				break;
			case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationMetal(), resCnt / 150));
				break;
			case PlayerAttr.STEEL_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationUranium(), resCnt / 40));
				break;
			default:
				break;
			}

		}
		return list;
	}

	public static List<ItemInfo> deadCost(Player player, List<ArmyInfo> marchArmy, double deadSoldierSpeedUpCoefficient, double deadSoldierResourceCoefficient) {
		List<ItemInfo> list = new ArrayList<>();
		AwardItems award = AwardItems.valueOf();
		double trainTime = 0;
		for (ArmyInfo army : marchArmy) {
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			List<ItemInfo> consumeRes = GameUtil.trainCost(player, soldierCfg, army.getDeadCount());
			award.addItemInfos(consumeRes);
			trainTime = trainTime + GameUtil.calTrainTime(player, soldierCfg, army.getDeadCount(), 0);
		}
		// 时间道具
		int speedItemCount = (int) (trainTime * deadSoldierSpeedUpCoefficient / 300);
		list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getTrainSoldierUpSpeedItem(), speedItemCount));
		for (ItemInfo res : award.getAwardItems()) { // 消耗资源补偿
			int resId = res.getItemId();
			int resCnt = (int) (res.getCount() * deadSoldierResourceCoefficient);
			;
			switch (resId) {
			case PlayerAttr.GOLDORE_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationGold(), resCnt / 1000));
				break;
			case PlayerAttr.OIL_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationOil(), resCnt / 1000));
				break;
			case PlayerAttr.TOMBARTHITE_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationMetal(), resCnt / 150));
				break;
			case PlayerAttr.STEEL_UNSAFE_VALUE:
				list.add(new ItemInfo(ItemType.TOOL_VALUE, ConstProperty.getInstance().getCompensationUranium(), resCnt / 40));
				break;
			default:
				break;
			}

		}
		return list;
	}
	
	
	/**
	 * 
	 * @param playerId
	 * @return
	 */
	public static int getWoundedCount(List<ArmyInfo> armyInfoList) {
		if (armyInfoList == null || armyInfoList.isEmpty()) {
			return 0;
		}
		
		int num = 0;
		for (ArmyInfo armyInfo : armyInfoList) {
			num+=armyInfo.getWoundedCount();
		}
		
		return num;
	}
	
	/**
	 * 防守死亡人数
	 * @param playerId
	 * @return
	 */
	public static int getDeadCount(List<ArmyInfo> armyInfoList) {
		if (armyInfoList == null || armyInfoList.isEmpty()) {
			return 0;
		}
		
		int num = 0;
		for (ArmyInfo armyInfo : armyInfoList) {
			num+=armyInfo.getDeadCount();
		}
		
		return num;
	}
}
