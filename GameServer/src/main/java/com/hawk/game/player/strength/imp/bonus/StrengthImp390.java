package com.hawk.game.player.strength.imp.bonus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.hawk.game.config.SoldierStrengthTypeCfg;
import com.hawk.game.module.mechacore.PlayerMechaCore;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleAddtionalCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreModuleSlotCfg;
import com.hawk.game.module.mechacore.cfg.MechaCoreTabCfg;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEffObject;
import com.hawk.game.module.mechacore.entity.MechaCoreModuleEntity;
import com.hawk.game.module.mechacore.entity.MechaCoreSlotObject;
import com.hawk.game.module.mechacore.entity.MechaCoreSuitObject;
import com.hawk.game.player.Player;
import com.hawk.game.player.strength.imp.PlayerStrengthCell;
import com.hawk.game.protocol.Const.SoldierType;

/**
 * 机甲核心模块装配
 * @author che
 *
 */
@StrengthType(strengthType = 390)
public class StrengthImp390 implements StrengthBonusImp {
	
	@Override
	public void calc(Player player, SoldierType soldierType, PlayerStrengthCell cell) {
		SoldierStrengthTypeCfg typeCfg = getStrengthTypeCfg();
		
		double atkValue = 0;
		double hpValue = 0;
		//科技
		PlayerMechaCore mechacore = player.getPlayerMechaCore();
		List<MechaCoreModuleEntity> moduleList = player.getData().getMechaCoreModuleEntityList();
		//被装载的模块
		Map<String,MechaCoreModuleEntity> loads = new HashMap<>();
		for(MechaCoreModuleEntity entity : moduleList){
			if(entity.isLoaded()){
				loads.put(entity.getId(), entity);
			}
		}
		
		//页签
		ConfigIterator<MechaCoreTabCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MechaCoreTabCfg.class);
		for(MechaCoreTabCfg cfg : iterator){
			MechaCoreSuitObject suit = mechacore.getSuitObj(cfg.getId());
			if(Objects.nonNull(suit)){
				Map<Integer, String> slotModuleInfo = suit.getSlotModuleInfo();
				for(Map.Entry<Integer, String> entry : slotModuleInfo.entrySet()){
					int slot = entry.getKey();
					String module = entry.getValue();
					MechaCoreSlotObject slotObj = mechacore.getSlotObj(slot);
					if(Objects.isNull(slotObj)){
						continue;
					}
					MechaCoreModuleSlotCfg slotCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleSlotCfg.class, slotObj.getSlotId());
					if(Objects.isNull(slotCfg)){
						continue;
					}
					MechaCoreModuleEntity moduleEntity = loads.get(module);
					if(Objects.isNull(moduleEntity)){
						continue;
					}
					MechaCoreModuleCfg moduleCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleCfg.class, moduleEntity.getCfgId());
					if(Objects.isNull(moduleCfg)){
						continue;
					}
					
					//本体攻击加成
					double mmAtk = moduleCfg.getAtkAttr(soldierType.getNumber());
					double sAtk = slotCfg.getAtkAttr(soldierType.getNumber());
					double slotRltAtk = mmAtk * sAtk / 10000;
					
					double mHp = moduleCfg.getHpAttr(soldierType.getNumber());
					double sHp = slotCfg.getAtkAttr(soldierType.getNumber());
					double slotRltHp = mHp * sHp / 10000;
					
					atkValue += slotRltAtk;
					hpValue += slotRltHp;
					
					//条目
					double attrRltAtk = 0d;
					double attrRltHp = 0d;
					List<MechaCoreModuleEffObject> randomAttr = moduleEntity.getRandomAttrEff();
					for(MechaCoreModuleEffObject attr : randomAttr){
						MechaCoreModuleAddtionalCfg attrCfg = HawkConfigManager.getInstance().getConfigByKey(MechaCoreModuleAddtionalCfg.class, attr.getAttrId());
						if(Objects.isNull(attrCfg)){
							continue;
						}
						double attrPower = attrCfg.getPower();
						double attrAtk = Double.valueOf(typeCfg.getParam1());
						attrRltAtk += attrPower * attrAtk / 10000;
						
						double attrHp = Double.valueOf(typeCfg.getParam2());
						attrRltHp += attrPower * attrHp / 10000;
					}
					atkValue += attrRltAtk;
					hpValue += attrRltHp;
				}
			}
		}
		cell.setAtk(Math.min((int)atkValue, typeCfg.getAtkAttrMax()));
		cell.setHp(Math.min((int)hpValue, typeCfg.getHpAttrMax()));
	}
}