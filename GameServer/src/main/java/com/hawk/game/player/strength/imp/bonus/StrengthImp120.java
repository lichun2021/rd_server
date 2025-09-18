package com.hawk.game.player.strength.imp.bonus;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.tuple.HawkTuple2;

import com.hawk.game.cfgElement.ArmourEffObject;
import com.hawk.game.config.ArmourAdditionalCfg;
import com.hawk.game.config.ArmourBreakthroughCfg;
import com.hawk.game.config.ArmourCfg;
import com.hawk.game.config.ArmourConstCfg;
import com.hawk.game.config.ArmourLevelCfg;
import com.hawk.game.config.ArmourSuitCfg;
import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.entity.ArmourEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

/**
 * 装备
 * @author Golden
 *
 */
@StrengthType(strengthType = 120)
public class StrengthImp120 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		PlayerData playerData = player.getData();
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		HawkTuple2<Integer, Integer> strength = armourStrength(playerData, soldierType.getNumber());
		
		cell.setAtk(Math.min(strength.first, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min(strength.second, typeCfg.getHpAttrMax()));
	}
	
	private HawkTuple2<Integer, Integer> armourStrength(PlayerData playerData, int soldierType) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		int retAtk = 0;
		int retHp = 0;
		
		int armourSuitCount = playerData.getPlayerEntity().getArmourSuitCount();
		for (int suitId = 1; suitId <= armourSuitCount; suitId++) {
			
			// 装备战力
			int armourAtkStrength = 0;
			int armourHpStrength = 0;
			
			// 装备套装战力
			int armourSuitAtkStrength = 0;
			int armourSuitHpStrength = 0;
			
			// 附加属性战力
			int extraAttrAtkStrength = 0;
			int extraAttrHpStrength = 0;
			
			// 装备本体战力计算
			List<ArmourEntity> armours = playerData.getSuitArmours(suitId);
			for (ArmourEntity armour : armours) {
				ArmourCfg armourCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourCfg.class, armour.getArmourId());
				ArmourBreakthroughCfg armourQualityCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourBreakthroughCfg.class, armour.getQuality());
				ArmourLevelCfg armourLevelCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourLevelCfg.class, armour.getLevel());
				
				armourAtkStrength += (int)(1L * armourCfg.getAtkAttr(soldierType) * armourQualityCfg.getAtkAttr(soldierType) * armourLevelCfg.getAtkAttr(soldierType) * GsConst.EFF_PER * GsConst.EFF_PER);
				armourHpStrength += (int)(1L * armourCfg.getHpAttr(soldierType) * armourQualityCfg.getHpAttr(soldierType) * armourLevelCfg.getHpAttr(soldierType) * GsConst.EFF_PER * GsConst.EFF_PER);
				
				for (ArmourEffObject eff : armour.getExtraAttrEff()) {
					ArmourAdditionalCfg armourAttrCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourAdditionalCfg.class, eff.getAttrId());
					int power = (int) (1L * eff.getEffectValue() * armourAttrCfg.getArmourCombat() * GsConst.EFF_PER);
					extraAttrAtkStrength += power * Integer.parseInt(typeCfg.getParam1()) * GsConst.EFF_PER;
					extraAttrHpStrength += power * Integer.parseInt(typeCfg.getParam2()) * GsConst.EFF_PER;
				}
			}

			// 装备套装战力计算
			Map<Integer, List<ArmourEntity>> armourSuitMap = playerData.getArmourAttrSuitMap(armours);
			for (Entry<Integer, List<ArmourEntity>> armourSuit : armourSuitMap.entrySet()) {
				HawkTuple2<Integer, Integer> armourSuitEffect = getArmourSuitAttr(playerData, armourSuit.getKey(), armourSuit.getValue(), soldierType);
				armourSuitAtkStrength += armourSuitEffect.first;
				armourSuitHpStrength += armourSuitEffect.second;
			}
			
			int atkAttr = armourAtkStrength + armourSuitAtkStrength + extraAttrAtkStrength;
			int hpAttr = armourHpStrength + armourSuitHpStrength + extraAttrHpStrength;
			
			retAtk = Math.max(atkAttr, retAtk);
			retHp = Math.max(hpAttr, retHp);
		}
		
		return new HawkTuple2<>(retAtk, retHp);
	}
	
	/**
	 * 获取铠甲套装作用号
	 */
	private HawkTuple2<Integer, Integer> getArmourSuitAttr(PlayerData playerData, int armourSuitId, List<ArmourEntity> armours, int soldierType) {
		ArmourSuitCfg armourSuitCfg = HawkConfigManager.getInstance().getConfigByKey(ArmourSuitCfg.class, armourSuitId);
		if (armourSuitCfg == null) {
			return new HawkTuple2<>(0, 0);
		}

		int suitTochIndex = ArmourConstCfg.getInstance().getSuitCombination(armours.size());
		if (suitTochIndex <= 0) {
			return new HawkTuple2<>(0, 0);
		}

		int atkAttr = 0;
		int hpAttr = 0;
		
		for (int i = suitTochIndex; i > 0; i--) {
			int count = 0;
			int minQuality = 0;
			
			switch (i) {
			case 1:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = GameUtil.getCountMinQuality(armours, count);
				atkAttr += armourSuitCfg.getAtkAttr1(minQuality, soldierType);
				hpAttr += armourSuitCfg.getHpAttr1(minQuality, soldierType);
				break;
				
			case 2:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = GameUtil.getCountMinQuality(armours, count);
				atkAttr += armourSuitCfg.getAtkAttr2(minQuality, soldierType);
				hpAttr += armourSuitCfg.getHpAttr2(minQuality, soldierType);
				break;
				
			case 3:
				count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
				minQuality = GameUtil.getCountMinQuality(armours, count);
				atkAttr += armourSuitCfg.getAtkAttr3(minQuality, soldierType);
				hpAttr += armourSuitCfg.getHpAttr3(minQuality, soldierType);
				break;
			
			case 4:
				if (playerData.getEffVal(EffType.ARMOUR_1603) > 0) {
					count = ArmourConstCfg.getInstance().getSuitCombination().get(i - 1);
					minQuality = GameUtil.getCountMinQuality(armours, count);
					atkAttr += armourSuitCfg.getAtkAttr4(minQuality, soldierType);
					hpAttr += armourSuitCfg.getHpAttr4(minQuality, soldierType);
				}
				break;
				
			default:
				break;
			}
		}
		
		return new HawkTuple2<>(atkAttr, hpAttr);
	}
}