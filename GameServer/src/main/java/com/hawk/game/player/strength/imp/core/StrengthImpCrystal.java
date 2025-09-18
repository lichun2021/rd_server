package com.hawk.game.player.strength.imp.core;

import com.google.common.collect.ImmutableList;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.CrystalAnalysisChip;
import com.hawk.game.module.plantsoldier.strengthen.crystalAnalysis.PlantCrystalAnalysis;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能战士三阶加成
 * @author Administrator
 *
 */
public class StrengthImpCrystal implements StrengthBaseImp {

	@Override
	public void calc(PlayerData playerData, SoldierType soldierType, PlayerStrengthCell cell) {
		PlantSoldierSchool plantSchoolObj = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
		PlantCrystalAnalysis crystal = plantSchoolObj.getCrystal();
		if (crystal == null) {
			return;
		}
		int atkValue = 0;
		int hpValue = 0;
		ImmutableList<CrystalAnalysisChip> chips = crystal.getChips();
		for (CrystalAnalysisChip chip : chips) {
			atkValue += chip.getCfg().getBaseAtkAttr(soldierType.getNumber());
			hpValue += chip.getCfg().getBaseHpAttr(soldierType.getNumber());
		}
		cell.setAtk(atkValue);
		cell.setHp(hpValue);
	}
	
	@Override
	public String getStrengthType() {
		return "tain3";
	}
}
