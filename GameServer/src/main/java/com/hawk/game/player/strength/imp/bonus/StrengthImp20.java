package com.hawk.game.player.strength.imp.bonus;

import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.entity.TechnologyEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 科技
 * @author Golden
 *
 */
@StrengthType(strengthType = 20)
public class StrengthImp20 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		List<TechnologyEntity> technologyEntities = playerData.getTechnologyEntities();
		int atkValue = 0;
		int hpValue = 0;
		for (TechnologyEntity entity : technologyEntities) {
			TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, entity.getCfgId());
			if (cfg == null) {
				continue;
			}
			atkValue += cfg.getAtkAttr(soldierType.getNumber());
			hpValue += cfg.getHpAttr(soldierType.getNumber());
		}
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}
