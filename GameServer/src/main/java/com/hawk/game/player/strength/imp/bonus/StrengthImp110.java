package com.hawk.game.player.strength.imp.bonus;

import java.util.Objects;

import org.hawk.config.HawkConfigManager;

import com.hawk.game.config.LaboratoryBlockCfg;
import com.hawk.game.config.LaboratoryKVCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.LaboratoryEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.laboratory.Laboratory;
import com.hawk.game.player.laboratory.LaboratoryEnum.PowerBlockIndex;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.player.laboratory.PowerBlock;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 能量核心
 * @author Golden
 *
 */
@StrengthType(strengthType = 110)
public class StrengthImp110 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int page = Math.max(1, playerData.getPlayerEntity().getLaboratory());
		Laboratory lab = getLaboratory(playerData, page);
		if (lab == null) {
			return;
		}
		
		PowerBlock powerBlock = lab.getPowerBlock();
		if (powerBlock == null) {
			return;
		}
		
		int atkValue = 0;
		int hpValue = 0;
		for (PowerBlockIndex index : PowerBlockIndex.values()) {
			LaboratoryBlockCfg cfg = powerBlock.getBlockCfg(index);
			if (powerBlock.isIndexUnlock(index) && Objects.nonNull(cfg)) {
				atkValue += cfg.getAtkAttr(soldierType.getNumber());
				hpValue += cfg.getHpAttr(soldierType.getNumber());
			}
		}
		
		cell.setAtk(Math.min(typeCfg.getAtkAttrMax(), atkValue));
		cell.setHp(Math.min(typeCfg.getHpAttrMax(), hpValue));
	}
	
	public Laboratory getLaboratory(PlayerData playerData, int pageIndex) {
		if (pageIndex < 1 || pageIndex > getMaxPage()) {
			return null;
		}
		for (LaboratoryEntity lab : playerData.getLaboratoryEntityList()) {
			if (lab.getPageIndex() == pageIndex) {
				return lab.getLabObj();
			}
		}
		return null;
	}
	
	private int getMaxPage() {
		LaboratoryKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(LaboratoryKVCfg.class);
		return kvCfg.getMaxPage();
	}
}
