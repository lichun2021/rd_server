package com.hawk.game.player.strength.imp.core;

import com.hawk.game.module.plantsoldier.strengthen.PlantSoldierSchool;
import com.hawk.game.module.plantsoldier.strengthen.soldierCrack.PlantSoldierCrack;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 泰能战士二阶加成
 * @author Administrator
 *
 */
public class StrengthImpCarack implements StrengthBaseImp {

	@Override
	public void calc(PlayerData playerData, SoldierType soldierType, PlayerStrengthCell cell) {
		PlantSoldierSchool plantSchoolObj = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj();
		PlantSoldierCrack upfactory = plantSchoolObj.getSoldierCrackByType(soldierType);
		if (upfactory == null) {
			return;
		}
		cell.setAtk(upfactory.getCfg().getBaseAtkAttr(soldierType.getNumber()));
		cell.setHp(upfactory.getCfg().getBaseHpAttr(soldierType.getNumber()));
	}
	
	@Override
	public String getStrengthType() {
		return "tain2";
	}
}
