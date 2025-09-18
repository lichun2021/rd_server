package com.hawk.game.player.strength.imp.core;

import java.util.List;

import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthen;
import com.hawk.game.module.plantsoldier.strengthen.soldierStrengthen.SoldierStrengthenTech;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能战士四阶加成
 * @author Administrator
 *
 */
public class StrengthImpStrTech implements StrengthBaseImp {

	@Override
	public void calc(PlayerData playerData, SoldierType soldierType, PlayerStrengthCell cell) {
		PlantSoldierSchool plantSchoolObj = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
		SoldierStrengthen strengthen = plantSchoolObj.getSoldierStrengthenByType(soldierType);
		if (strengthen == null) {
			return;
		}
		int atkValue = 0;
		int hpValue = 0;
		List<SoldierStrengthenTech> chips = strengthen.getChips();
		for (SoldierStrengthenTech chip : chips) {
			atkValue += chip.getCfg().getBaseAtkAttr(soldierType.getNumber());
			hpValue += chip.getCfg().getBaseHpAttr(soldierType.getNumber());
		}
		cell.setAtk(atkValue);
		cell.setHp(hpValue);
	}
	
	@Override
	public String getStrengthType() {
		return "tain4";
	}
}
