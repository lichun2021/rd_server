package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.PlantTechnologyCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.PlantTechEntity;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.module.plantfactory.tech.TechChip;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能强化
 * @author Golden
 *
 */
@StrengthType(strengthType = 160)
public class StrengthImp160 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		for (PlantTechEntity factory : playerData.getPlantTechEntities()) {
			PlantTechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlantTechnologyCfg.class, factory.getCfgId());
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
			
			PlantTech techObj = factory.getTechObj();
			for (TechChip chip : techObj.getChips()) {
				atkAttr += chip.getCfg().getAtkAttr(soldierType.getNumber());
				hpAttr += chip.getCfg().getHpAttr(soldierType.getNumber());
			}
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}