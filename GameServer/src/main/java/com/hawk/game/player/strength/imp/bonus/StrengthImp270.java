package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.CrossTechCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.CrossTechEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 远征科技
 * @author Golden
 *
 */
@StrengthType(strengthType = 270)
public class StrengthImp270 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		List<CrossTechEntity> crossTechEntities = playerData.getCrossTechEntities();
		for (CrossTechEntity entity : crossTechEntities) {
			CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, entity.getCfgId());
			if (cfg == null) {
				continue;
			}
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
		}
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}