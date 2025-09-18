package com.hawk.game.player.strength.imp.bonus;

import java.util.Map;
import java.util.Objects;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.cfg.MechaCoreRankLevelCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreTechLevelCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 机甲核心科技
 * @author che
 *
 */
@StrengthType(strengthType = 380)
public class StrengthImp380 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkValue = 0;
		int hpValue = 0;
		//科技
		PlayerMechaCore mechacore = player.getPlayerMechaCore();
		Map<Integer, Integer> map = mechacore.getTechLevelCfgMap();
		for(Map.Entry<Integer, Integer> entry : map.entrySet()){
			int techId = entry.getKey();
			int techLv = entry.getValue();
			MechaCoreTechLevelCfg config = MechaCoreTechLevelCfg.getCfgByLevel(techId, techLv);
			if(Objects.nonNull(config)){
				atkValue += config.getAtkAttr(soldierType.getNumber());
				hpValue += config.getHpAttr(soldierType.getNumber());
			}
		}
		//突破
		int rankLevel = mechacore.getRankLevel();
		if(rankLevel > 0){
			MechaCoreRankLevelCfg rankCfg = MechaCoreRankLevelCfg.getCfgByLevel(rankLevel);
			if(Objects.nonNull(rankCfg)){
				atkValue += rankCfg.getAtkAttr(soldierType.getNumber());
				hpValue += rankCfg.getHpAttr(soldierType.getNumber());
			}
		}
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}