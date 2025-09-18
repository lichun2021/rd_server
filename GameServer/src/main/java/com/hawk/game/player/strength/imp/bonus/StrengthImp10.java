package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 天赋
 * @author Golden
 *
 */
@StrengthType(strengthType = 10)
public class StrengthImp10 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		int level = playerData.getPlayerBaseEntity().getLevel();
		PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, level);
		if (cfg == null) {
			return;
		}
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		cell.setAtk(Math.min(cfg.getAtkAttr(soldierType.getNumber()), typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(cfg.getHpAttr(soldierType.getNumber()), typeCfg.getHpAttrMax()));
	}

}
