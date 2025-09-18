package com.hawk.game.player.strength.imp.bonus;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.hawk.game.config.ManhattanSWLevelCfg;
import com.hawk.game.config.ManhattanSWStageCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.manhattan.PlayerManhattan;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 超级武器
 * @author che
 *
 */
@StrengthType(strengthType = 360)
public class StrengthImp360 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkValue = 0;
		int hpValue = 0;
		
		List<PlayerManhattan> swList = player. getAllManhattanSW();
		if(Objects.nonNull(swList)){
			for(PlayerManhattan sw : swList){
				//模块
				Map<Integer, Integer> map = sw.getPosLevelMap();
				for (Entry<Integer, Integer> entry : map.entrySet()) {
					ManhattanSWLevelCfg baseCfg = ManhattanSWLevelCfg.getConfig(sw.getSWCfgId(),entry.getKey(), entry.getValue());
					if(Objects.nonNull(baseCfg)){
						atkValue += baseCfg.getAtkAttr(soldierType.getNumber());
						hpValue += baseCfg.getHpAttr(soldierType.getNumber());
					}
				}
				//品阶
				ManhattanSWStageCfg stageCfg = ManhattanSWStageCfg.getConfig(sw.getSWCfgId(),sw.getStage());
				if(Objects.nonNull(stageCfg)){
					atkValue += stageCfg.getAtkAttr(soldierType.getNumber());
					hpValue += stageCfg.getHpAttr(soldierType.getNumber());
				}
			}
		}
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}