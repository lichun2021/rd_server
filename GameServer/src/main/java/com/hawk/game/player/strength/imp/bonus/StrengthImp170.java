package com.hawk.game.player.strength.imp.bonus;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.InstrumentChip;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.PlantInstrument;
import com.hawk.game.module.plantsoldier.strengthen.instrumentUpgrade.cfg.PlantInstrumentUpgradeChipCfg;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.CrackChip;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.cfg.PlantSoldierCrackChipCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能战士
 * @author Golden
 *
 */
@StrengthType(strengthType = 170)
public class StrengthImp170 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		PlantSoldierSchool plantSchoolObj = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
		PlantSoldierCrack factory = plantSchoolObj.getSoldierCrackByType(soldierType);
		for (CrackChip chip : factory.getChips()) {
			PlantSoldierCrackChipCfg chipCfg = chip.getCfg();
			atkAttr += chipCfg.getAtkAttr(soldierType.getNumber());
			hpAttr += chipCfg.getHpAttr(soldierType.getNumber());
		}
		
		PlantInstrument instrument = plantSchoolObj.getInstrument();
		for (InstrumentChip chip : instrument.getChips()) {
			PlantInstrumentUpgradeChipCfg cfg = chip.getCfg();
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}