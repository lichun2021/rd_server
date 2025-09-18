package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.LaboratoryKVCfg;
import com.hawk.game.config.SoldierStrengthLaboraryCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 能量矩阵
 * @author Golden
 *
 */
@StrengthType(strengthType = 100)
public class StrengthImp100 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int energyCount = getEnergyCount(playerData);
		ConfigIterator<SoldierStrengthLaboraryCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SoldierStrengthLaboraryCfg.class);
		while(cfgIter.hasNext()) {
			SoldierStrengthLaboraryCfg cfg = cfgIter.next();
			if (energyCount >= cfg.getNumMin() && energyCount <= cfg.getNumMax()) {
				int atkValue = Math.min(typeCfg.getAtkAttrMax(), cfg.getAtkAttr(soldierType.getNumber()));
				int hpValue = Math.min(typeCfg.getHpAttrMax(), cfg.getHpAttr(soldierType.getNumber()));
				
				cell.setAtk(atkValue);
				cell.setHp(hpValue);
			}
		}
		
	}
	
	/**
	 * 获取能量源数量
	 * @param playerData
	 * @return
	 */
	public int getEnergyCount(PlayerData playerData) {
		LaboratoryKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		return playerData.getItemNumByItemId(kvcfg.getLockItemId());
	}
}
