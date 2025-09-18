package com.hawk.game.player.strength.imp.bonus;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkTime;

import com.hawk.game.config.DressCfg;
import com.hawk.game.config.DressGroupCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 装扮套装
 * @author Golden
 *
 */
@StrengthType(strengthType = 230)
public class StrengthImp230 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		long currentTime = HawkTime.getMillisecond();

		int attrValueMark = 0;
		
		int atkAttrValue = 0;
		int hpAttrValue = 0;
		
		ConfigIterator<DressGroupCfg> dressGroupIter = HawkConfigManager.getInstance().getConfigIterator(DressGroupCfg.class);
		while (dressGroupIter.hasNext()) {
			DressGroupCfg dressGroupCfg = dressGroupIter.next();
			if (dressGroupCfg.getAttrValue() < attrValueMark) {
				continue;
			}
			attrValueMark = dressGroupCfg.getAttrValue();
			
			// 是否触发套装属性
			boolean touch = true;
			for (int dressId : dressGroupCfg.getDressIdList()) {
				DressCfg dressCfg = HawkConfigManager.getInstance().getConfigByKey(DressCfg.class, dressId);
				DressItem dressInfo = playerData.getDressEntity().getDressInfo(dressCfg.getDressType(), dressCfg.getModelType());
				if (dressInfo == null || currentTime > dressInfo.getStartTime() + dressInfo.getContinueTime()) {
					touch = false;
					break;
				}
			}
			
			if (touch) {
				atkAttrValue = dressGroupCfg.getAtkAttr(soldierType.getNumber());
				hpAttrValue = dressGroupCfg.getHpAttr(soldierType.getNumber());
			}
		}
		
		cell.setAtk(Math.min(atkAttrValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttrValue, typeCfg.getHpAttrMax()));
	}
}