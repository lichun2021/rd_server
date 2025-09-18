package com.hawk.game.player.strength.imp.core;

import java.util.List;

import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitary;
import com.hawk.game.module.plantsoldier.strengthen.plantMilitary.PlantSoldierMilitaryChip;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能战士五阶加成
 * @author Administrator
 *
 */
public class StrengthImpMilitary implements StrengthBaseImp {

	@Override
	public void calc(PlayerData playerData, SoldierType soldierType, PlayerStrengthCell cell) {
		PlantSoldierMilitary military = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj().getSoldierMilitaryByType(soldierType);;
		if (military == null) {
			return;
		}
		int atkValue = 0;
		int hpValue = 0;
		
		List<PlantSoldierMilitaryChip> chips = military.getChips();
		for (PlantSoldierMilitaryChip chip : chips) {
			atkValue += chip.getCfg().getBaseAtkAttr(soldierType.getNumber());
			hpValue += chip.getCfg().getBaseHpAttr(soldierType.getNumber());
		}
		cell.setAtk(atkValue);
		cell.setHp(hpValue);
	}
	
	@Override
	public String getStrengthType() {
		return "tain5";
	}
}
