package com.hawk.game.player.strength.imp.bonus;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.hawk.game.config.ManhattanBaseLevelCfg;
import com.hawk.game.config.ManhattanBaseStageCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 能量底座
 * @author che
 *
 */
@StrengthType(strengthType = 350)
public class StrengthImp350 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkValue = 0;
		int hpValue = 0;
		
		PlayerManhattan manhattanbase = player.getManhattanBase();
		if(Objects.nonNull(manhattanbase)){
			//模块
			Map<Integer, Integer> map = manhattanbase.getPosLevelMap();
			for (Entry<Integer, Integer> entry : map.entrySet()) {
				ManhattanBaseLevelCfg baseCfg = ManhattanBaseLevelCfg.getConfig(entry.getKey(), entry.getValue());
				if(Objects.nonNull(baseCfg)){
					atkValue += baseCfg.getAtkAttr(soldierType.getNumber());
					hpValue += baseCfg.getHpAttr(soldierType.getNumber());
				}
			}
			//品阶
			ManhattanBaseStageCfg stageCfg = ManhattanBaseStageCfg.getConfigByStage(manhattanbase.getStage());
			if(Objects.nonNull(stageCfg)){
				atkValue += stageCfg.getAtkAttr(soldierType.getNumber());
				hpValue += stageCfg.getHpAttr(soldierType.getNumber());
			}
		}
		
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}