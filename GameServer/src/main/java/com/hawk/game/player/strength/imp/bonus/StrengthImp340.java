package com.hawk.game.player.strength.imp.bonus;

import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.cfgElement.ArmourStarExploreCollect;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.ArmourStarExploreCollectCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GsConst;

/**
 * 星能跃迁
 * @author che
 *
 */
@StrengthType(strengthType = 340)
public class StrengthImp340 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkValue = 0;
		int hpValue = 0;
		CommanderEntity entity = playerData.getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		
		ConfigIterator<ArmourStarExploreCollectCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourStarExploreCollectCfg.class);
		for(ArmourStarExploreCollectCfg cfg : iterator){
			ArmourStarExploreCollect collect = starExplores.getCollect(cfg.getId());
			if(Objects.isNull(collect)){
				continue;
			}
			Map<Integer, Integer> fixMap = collect.getFixAttrMap();
			int atkAttr = fixMap.getOrDefault(cfg.getFixAttr().first, 0);
			int hpAttr = fixMap.getOrDefault(cfg.getFixAttr().first, 0);
			
			atkValue += Math.min(atkAttr * 10000 / cfg.getMaxLogistics(), 10000) *  cfg.getAtkAttr(soldierType.getNumber()) * GsConst.EFF_PER;
			hpValue += Math.min(hpAttr * 10000 / cfg.getMaxLogistics(), 10000) * cfg.getHpAttr(soldierType.getNumber()) * GsConst.EFF_PER;
		}
		cell.setAtk(Math.min(atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpValue, typeCfg.getHpAttrMax()));
	}
}