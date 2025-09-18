package com.hawk.game.player.strength.imp.bonus;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.os.HawkTime;

import com.hawk.game.config.DressCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.DressEntity;
import com.hawk.game.entity.item.DressItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 装扮
 * @author Golden
 *
 */
@StrengthType(strengthType = 220)
public class StrengthImp220 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int atkGainAttr = 0;
		int hpGainAttr = 0;
		
		Map<Integer, Integer> attrUseMark = new HashMap<>();
		Map<Integer, Integer> atkUseAttr = new HashMap<>();
		Map<Integer, Integer> hpUseAttr = new HashMap<>();
		
		long currentTime = HawkTime.getMillisecond();
		DressEntity dressEntity = playerData.getDressEntity();
		for (DressItem dressInfo : dressEntity.getDressInfo()) {
			// 装扮已经失效
			if (currentTime > dressInfo.getStartTime() + dressInfo.getContinueTime()) {
				continue;
			}
			DressCfg dressCfg = AssembleDataManager.getInstance().getDressCfg(dressInfo.getDressType(), dressInfo.getModelType());
			atkGainAttr += dressCfg.getAtkAttrGain(soldierType.getNumber());
			hpGainAttr += dressCfg.getHpAttrGain(soldierType.getNumber());
			
			int mark = attrUseMark.getOrDefault(dressInfo.getDressType(), 0);
			if (mark == 0 || dressCfg.getAttrUseValue() > mark) {
				attrUseMark.put(dressInfo.getDressType(), dressCfg.getAttrUseValue());
				atkUseAttr.put(dressInfo.getDressType(), dressCfg.getAtkAttrUse(soldierType.getNumber()));
				hpUseAttr.put(dressInfo.getDressType(), dressCfg.getHpAttrUse(soldierType.getNumber()));
			}
		}
		
		int atkAttr = atkGainAttr;
		for (Entry<Integer, Integer> atk : atkUseAttr.entrySet()) {
			atkAttr += atk.getValue();
		}
		
		int hpAttr = hpGainAttr;
		for (Entry<Integer, Integer> hp : hpUseAttr.entrySet()) {
			hpAttr += hp.getValue();
		}
		
		cell.setAtk(Math.min(atkAttr, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(hpAttr, typeCfg.getHpAttrMax()));
	}
}