package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.config.VipCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * vip
 * @author Golden
 *
 */
@StrengthType(strengthType = 250)
public class StrengthImp250 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int vipLevel = playerData.getPlayerEntity().getVipLevel();
		VipCfg cfg = HawkConfigManager.getInstance().getConfigByKey(VipCfg.class, vipLevel);
		if (cfg == null) {
			return;
		}
		
		cell.setAtk(Math.min(cfg.getAtkAttr(soldierType.getNumber()), typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(cfg.getHpAttr(soldierType.getNumber()), typeCfg.getHpAttrMax()));
	}
}