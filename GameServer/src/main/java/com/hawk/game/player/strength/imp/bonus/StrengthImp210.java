package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.OfficerCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.president.PresidentOfficier;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.President.OfficerType;

/**
 * 王国官员
 * @author Golden
 *
 */
@StrengthType(strengthType = 210)
public class StrengthImp210 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int officerId = PresidentOfficier.getInstance().getOfficerId(playerData.getPlayerId());
		if (officerId == OfficerType.OFFICER_00_VALUE) {
			return;
		}

		OfficerCfg cfg = HawkConfigManager.getInstance().getConfigByKey(OfficerCfg.class, officerId);
		cell.setAtk(Math.min(cfg.getAtkAttr(soldierType.getNumber()), typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(cfg.getHpAttr(soldierType.getNumber()), typeCfg.getHpAttrMax()));
	}
}