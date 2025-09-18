package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.module.plantsoldier.science.PlantScience;
import com.hawk.game.module.plantsoldier.science.PlantScienceComponent;
import com.hawk.game.module.plantsoldier.science.cfg.PlantScienceCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能科技
 * @author Golden
 *
 */
@StrengthType(strengthType = 180)
public class StrengthImp180 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		PlantScience sciencObj = playerData.getPlantScienceEntity().getSciencObj();
		for (PlantScienceComponent component : sciencObj.getComponents().values()) {
			PlantScienceCfg cfg = HawkConfigManager.getInstance().getCombineConfig(PlantScienceCfg.class, component.getScienceId(), component.getLevel());
			if (cfg == null) {
				continue;
			}
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}