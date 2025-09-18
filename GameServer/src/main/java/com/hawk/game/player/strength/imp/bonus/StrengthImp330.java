package com.hawk.game.player.strength.imp.bonus;

import java.util.Objects;

import com.hawk.game.cfgElement.ArmourStarExploreObj;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.ArmourStarExploreCfg;
import com.hawk.game.config.ArmourStarExploreUpgradeCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 星能探索
 * @author che
 *
 */
@StrengthType(strengthType = 330)
public class StrengthImp330 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		CommanderEntity entity = playerData.getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		
		for (int starId = 1; starId <= ArmourStarExploreCfg.getStarCount(); starId++) {
			ArmourStarExploreObj starObj = starExplores.getStar(starId);
			if(Objects.isNull(starObj)){
				continue;
			}
			int curLevel = starObj.getCurrentLevel();
			ArmourStarExploreUpgradeCfg cfg = ArmourStarExploreUpgradeCfg.getLevelCfg(starId, curLevel);
			if(Objects.isNull(cfg)){
				continue;
			}
			atkAttr += cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += cfg.getHpAttr(soldierType.getNumber());
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}