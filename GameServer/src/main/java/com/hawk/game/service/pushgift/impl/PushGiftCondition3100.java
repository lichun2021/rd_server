package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.module.plantfactory.tech.TechChip;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 礼包类型 = 3100
 * 礼包条件：四兵营/大本的5个泰能模块强达到X级，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：X_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftCondition3100 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		List<Integer> cfgParam = cfg.getParamList();
		// vip等级判断
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		if (vipLevel < cfgParam.get(1) || vipLevel > cfgParam.get(2)) {
			return false;
		}
		
		PlantTech plantTech = null;
		int buildType = param.get(0);
		for (PlantTechEntity factory : playerData.getPlantTechEntities()) {
			if (buildType == factory.getBuildType()) {
				plantTech = factory.getTechObj();
				break;
			}
		}
		
		if (plantTech == null) {
			return false;
		}
		
		// 技能达到最高等级
		for (TechChip chip : plantTech.getChips()) {
			if (chip.getCfg().getLevel() < cfgParam.get(0)) {
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.PUSH_GIFT_3100.getType();
	}
}
