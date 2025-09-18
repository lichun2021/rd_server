package com.hawk.game.player.strength.imp.bonus;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierEnergy;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 能量底座
 * @author che
 *
 */
@StrengthType(strengthType = 370)
public class StrengthImp370 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkValue = 0;
		int hpValue = 0;

		List<SuperSoldier> ssList = player.getAllSuperSoldier();
		if(Objects.nonNull(ssList)){
			for(SuperSoldier ss : ssList){
				//赋能
				SuperSoldierEnergy superSoldierEnergy = ss.getSoldierEnergy();
				Set<ISuperSoldierEnergy> energyset = superSoldierEnergy.getEnergys();
				if(Objects.nonNull(energyset)){
					for (ISuperSoldierEnergy energy : energyset) {
						SuperSoldierEnergyCfg encfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, energy.getCfgId());
						if(Objects.nonNull(encfg)){
							atkValue += encfg.getAtkAttr(soldierType.getNumber());
							hpValue += encfg.getHpAttr(soldierType.getNumber());
						}
					}
				}
			}
		}
		
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}