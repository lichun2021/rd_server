package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.SoldierStrengthHeroCollectCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.PlayerEffect;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 英雄羁绊
 * @author Golden
 *
 */
@StrengthType(strengthType = 80)
public class StrengthImp80 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		PlayerEffect playerEffect = playerData.getPlayerEffect();
		
		int atkAttr = 0;
		int hpAttr = 0;
		
		ConfigIterator<SoldierStrengthHeroCollectCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SoldierStrengthHeroCollectCfg.class);
		while (cfgIter.hasNext()) {
			SoldierStrengthHeroCollectCfg cfg = cfgIter.next();
			int effValue = playerEffect.getHerosCollectEffVal(EffType.valueOf(cfg.getEffectId()));
			atkAttr += effValue * cfg.getAtkAttr(soldierType.getNumber());
			hpAttr += effValue * cfg.getHpAttr(soldierType.getNumber());
		}
		
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		cell.setAtk(Math.min((int)(atkAttr * GsConst.EFF_PER), typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min((int)(hpAttr * GsConst.EFF_PER), typeCfg.getHpAttrMax()));
	}
}