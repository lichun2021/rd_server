package com.hawk.game.player.strength.imp.bonus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.ArmourQuantumConsumeCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GameUtil;

/**
 * 装备量子扩充
 * @author che
 *
 */
@StrengthType(strengthType = 320)
public class StrengthImp320 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		int atkAttr = 0;
		int hpAttr = 0;
		Map<Integer,Long> powerMap = new HashMap<>();
		Map<Integer,Integer> atkMap = new HashMap<>();
		Map<Integer,Integer> hpMap = new HashMap<>();
		
		int suitCount = player.getEntity().getArmourSuitCount();
		for (int suitIndex = 1; suitIndex <= suitCount; suitIndex++) {
			Map<Integer, String> map = player.getArmourSuit(suitIndex);
			for (Map.Entry<Integer, String> entry : map.entrySet()) {
				ArmourEntity armour = player.getData().getArmourEntity(entry.getValue());
				if(Objects.isNull(armour)){
					continue;
				}
				
				//战力
				int armourPower = GameUtil.getArmourPower(armour);
				long power = powerMap.getOrDefault(suitIndex, 0l) + armourPower;
				powerMap.put(suitIndex, power);
			
				ArmourQuantumConsumeCfg armourQuantumCfg = HawkConfigManager.getInstance()
						.getConfigByKey(ArmourQuantumConsumeCfg.class, armour.getQuantum());
				if (Objects.nonNull(armourQuantumCfg)) {
					//atk
					int atk = atkMap.getOrDefault(suitIndex, 0) + armourQuantumCfg.getAtkAttr(soldierType.getNumber());
					atkMap.put(suitIndex, atk);
					//hp
					int hp = hpMap.getOrDefault(suitIndex, 0) + armourQuantumCfg.getHpAttr(soldierType.getNumber());
					hpMap.put(suitIndex, hp);
				}
			}
		}
		//选取做大战力值
		int use = -1;
		long power = 0;
		for (Map.Entry<Integer, Long> entry : powerMap.entrySet()) {
			if(entry.getValue() > power){
				use = entry.getKey();
			}
		}
		//获取目标值
		if(atkMap.containsKey(use)){
			atkAttr = atkMap.get(use);
		}
		if(hpMap.containsKey(use)){
			hpAttr = hpMap.get(use);
		}
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}