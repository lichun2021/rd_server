package com.hawk.game.util;

import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ItemCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;

/**
 * 配置检查
 *
 * @author hawk
 *
 */
public class ConfigUtil {

	/**
	 * 检测ItemType的itemId
	 *
	 * @param ItemType
	 * @param itemId
	 * @return
	 */
	public static boolean checkItemType(int itemType, int itemId) {
		itemType = GameUtil.convertToStandardItemType(itemType) / GsConst.ITEM_TYPE_BASE;
		if (itemType == Const.ItemType.PLAYER_ATTR_VALUE) {
			return true;
		} else if (itemType == Const.ItemType.ROLE_VALUE) {
			return true;
		} else if (itemType == Const.ItemType.SKILL_VALUE) {
			return true;
		} else if (itemType == Const.ItemType.EQUIP_VALUE) {
			return false;
		} else if (itemType == Const.ItemType.TOOL_VALUE) {
			if (HawkConfigManager.getInstance().getConfigByKey(ItemCfg.class, itemId) == null) {
				HawkLog.errPrintln("item config not found, itemId: {}", itemId);
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * 获取已解锁的最高级别的兵种ID
	 * @param soldierType 兵种类型
	 */
	public static int getAdvancedSoldierId(Player player, int soldierType) {
		Map<Integer, List<Integer>> soldierTypeMap = AssembleDataManager.getInstance().getSoldierTypeMap();
		if (soldierTypeMap != null) {
			List<Integer> soldierIdList = soldierTypeMap.get(soldierType);
			if (soldierIdList != null && soldierIdList.size() > 0) {
				int soldierId = 0;
				for (Integer id : soldierIdList) {
					if (id > soldierId && isSoldierUnlocked(player, id)) {
						soldierId = id;
					}
				}
				return soldierId;
			}
		}
		return 0;
	}
	
	/**
	 * 判断兵种是否已解锁
	 * @param player
	 * @param armyId
	 * @return
	 */
	public static boolean isSoldierUnlocked(Player player, int armyId) {
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		if(cfg == null) {
			return false;
		}
		if (cfg.isPlantSoldier()) {
			try {
				boolean poyiMax = player.getPlantSoldierSchool().getSoldierCrackByType(cfg.getSoldierType()).isMax();
				return poyiMax;
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			return false;
		}
		
		int building = cfg.getBuilding();
		// 判断配置的是建筑ID还是建筑类型
		if(BuildingType.valueOf(building) != null) {
			// 建筑类型
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByType(BuildingType.valueOf(building));
			if(buildingList == null || buildingList.size() == 0) {
				return false;
			}
			
			int buildingCfgId = 0;
			int maxLvl = 0;
			for(BuildingBaseEntity entity : buildingList) {
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, entity.getBuildingCfgId());
				// 取同类建筑中等级最高的一个
				if(buildingCfg.getLevel() > maxLvl) {
					maxLvl = buildingCfg.getLevel();
					buildingCfgId = entity.getBuildingCfgId();
				}
			}
			
			building = buildingCfgId;
		} else {
			List<BuildingBaseEntity> buildingList = player.getData().getBuildingListByCfgId(building);
			if(buildingList == null || buildingList.size() == 0) {
				return false;
			}
		}
		
		if(!GameUtil.isSoldierUnlocked(player, building, armyId)) {
			return false;
		}
		
		return true;
	}


}
