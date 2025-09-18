package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.EquipResearchEntity;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 装备科技
 * @author Golden
 *
 */
@StrengthType(strengthType = 150)
public class StrengthImp150 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		List<EquipResearchEntity> equipResearchEntityList = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : equipResearchEntityList) {
			EquipResearchLevelCfg cfg = AssembleDataManager.getInstance().getEquipResearchLevelCfg(entity.getResearchId(), entity.getResearchLevel());
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