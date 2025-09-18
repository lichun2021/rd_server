package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import com.hawk.game.config.ArmourQuantumConsumeCfg;
import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.config.ArmourStarConsumeCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 装备泰晶
 * @author Golden
 *
 */
@StrengthType(strengthType = 130)
public class StrengthImp130 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		HawkTuple2<Integer, Integer> strength = armourStrength(playerData, soldierType.getNumber());
		
		cell.setAtk(Math.min(strength.first, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(strength.second, typeCfg.getHpAttrMax()));
	}
	
	private HawkTuple2<Integer, Integer> armourStrength(PlayerData playerData, int soldierType) {
		int retAtk = 0;
		int retHp = 0;
		
		int armourSuitCount = playerData.getPlayerEntity().getArmourSuitCount();
		for (int suitId = 1; suitId <= armourSuitCount; suitId++) {
			int atkAttr = 0;
			int hpAttr = 0;
			
			List<ArmourEntity> armours = playerData.getSuitArmours(suitId);
			for (ArmourEntity armour : armours) {
				ArmourStarConsumeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ArmourStarConsumeCfg.class, armour.getStar());
				if (cfg != null) {
					atkAttr += cfg.getAtkAttr(soldierType);
					hpAttr += cfg.getHpAttr(soldierType);
				}
				ArmourQuantumConsumeCfg quantumConsumeCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourQuantumConsumeCfg.class, armour.getQuantum());
				if (quantumConsumeCfg != null) {
					atkAttr += quantumConsumeCfg.getAtkAttr(soldierType);
					hpAttr += quantumConsumeCfg.getHpAttr(soldierType);
				}
			}
			
			retAtk = Math.max(atkAttr, retAtk);
			retHp = Math.max(hpAttr, retHp);
		}
		
		return new HawkTuple2<>(retAtk, retHp);
	}
}