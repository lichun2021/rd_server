package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthenTech;
import com.hawk.game.player.PlayerData;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 礼包类型 = 2805
 * 礼包条件：战士强化达到一定等级，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
    - 组ID：plant_soldier_strengthen_tech    group
    - 节点ID：plant_soldier_strengthen_tech    pointId
    - 等级：plant_crystal_analysis_chip     techLevel
 * 配置格式：兵种ID_组ID_等级_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftCondition3600 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		
		List<Integer> cfgParam = cfg.getParamList();
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(3) || vipLevel > cfgParam.get(4)) {
			return false;
		}
		
		int retLevel = 0;
		
		// 兵种类型
		int soldierId = param.get(0);
		if (soldierId != cfgParam.get(0)) {
			return false;
		}
		
		// 组
		int groupId = param.get(1);
		if (groupId != cfgParam.get(1)) {
			return false;
		}
		
		SoldierStrengthen soldierStrengthen = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj().getSoldierStrengthenByType(SoldierType.valueOf(soldierId));
		for (SoldierStrengthenTech chip : soldierStrengthen.getChips()) {
			if (chip.getCfg().getGroup() != groupId) {
				continue;
			}
			retLevel += chip.getLevel();
		}
		
		// 等级
		if (retLevel != cfgParam.get(2)) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.PUSH_GIFT_3600.getType();
	}
}
