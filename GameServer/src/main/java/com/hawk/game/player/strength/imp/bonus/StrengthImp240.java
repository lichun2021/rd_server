package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.DressPointCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.world.service.WorldPointService;

/**
 * 装扮点
 * @author Golden
 *
 */
@StrengthType(strengthType = 240)
public class StrengthImp240 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int index = 0;
		
		// 装扮点加成作用号
		int dressPoint = WorldPointService.getInstance().getDressPoint(playerData);
		ConfigIterator<DressPointCfg> dressPointCfgIter = HawkConfigManager.getInstance().getConfigIterator(DressPointCfg.class);
		while (dressPointCfgIter.hasNext()) {
			DressPointCfg cfg = dressPointCfgIter.next();
			if (dressPoint >= cfg.getNeedPoint()) {
				index = cfg.getId();
			}
		}
		
		DressPointCfg cfg = HawkConfigManager.getInstance().getConfigByKey(DressPointCfg.class, index);
		if (cfg == null) {
			return;
		}
		cell.setAtk(Math.min(cfg.getAtkAttr(soldierType.getNumber()), typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(cfg.getHpAttr(soldierType.getNumber()), typeCfg.getHpAttrMax()));
	}
}