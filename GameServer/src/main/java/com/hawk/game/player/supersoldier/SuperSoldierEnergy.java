package com.hawk.game.player.supersoldier;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.supersoldier.energy.ISuperSoldierEnergy;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierEffect;

/**
 * 机甲赋能
 * @author Golden
 *
 */
public class SuperSoldierEnergy {
	
	/**
	 * 机甲
	 */
	private SuperSoldier parent;
	
	/**
	 * 赋能部件
	 */
	private Set<ISuperSoldierEnergy> energys;

	/**
	 * 加载赋能属性
	 * @param soldier
	 * @param str
	 * @return
	 */
	public static SuperSoldierEnergy load(SuperSoldier soldier, String str) {
		SuperSoldierEnergy energy = new SuperSoldierEnergy();
		energy.loadEnergys(soldier, str);
		return energy;
	}
	
	/**
	 * 加载赋能属性
	 * @param soldier
	 * @param str
	 * @return
	 */
	private boolean loadEnergys(SuperSoldier soldier, String str) {
		parent = soldier;
		energys = new ConcurrentHashSet<>();
		
		if (HawkOSOperator.isEmptyString(str)) {
			return true;
		}
		
		JSONArray arr = JSONArray.parseArray(str);
		arr.forEach(s -> {
			ISuperSoldierEnergy energy = new ISuperSoldierEnergy(this);
			energy.load(s.toString());
			energys.add(energy);
		});
		return true;
	}
	
	/**
	 * 解锁机甲赋能
	 * @param id
	 */
	public boolean unlockEnergy() {
		ConfigIterator<SuperSoldierEnergyCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierEnergyCfg.class);
		while (cfgIter.hasNext()) {
			SuperSoldierEnergyCfg cfg = cfgIter.next();
			if (cfg.getSupersoldierId() != parent.getCfgId()) {
				continue;
			}
			if (cfg.getEnablingLevel() != 0) {
				continue;
			}
			ISuperSoldierEnergy energy = new ISuperSoldierEnergy(this);
			energy.init(cfg.getId());
			energys.add(energy);
		}
		notifyUpdate();
		return true;
	}
	
	/**
	 * 序列化
	 * @return
	 */
	public String serializ() {
		JSONArray arr = new JSONArray();
		energys.stream().map(ISuperSoldierEnergy::serializ).forEach(arr::add);
		return arr.toJSONString();
	}
	
	/**
	 * 更新db
	 */
	public void notifyUpdate() {
		parent.getDBEntity().setChanged(true);
	}
	
	/**
	 * 获取赋能部件
	 * @param energyId
	 * @return
	 */
	public ISuperSoldierEnergy getEnergy(int energyId) {
		for (ISuperSoldierEnergy energy : energys) {
			if (energy.getCfgId() == energyId) {
				return energy;
			}
		}
		return null;
	}
	
	/**
	 * 是否解锁赋能
	 */
	public boolean isUnlockEnergy() {
		return !energys.isEmpty();
	}
	
	/**
	 * 获取解锁赋能消耗
	 * @return
	 */
	public List<ItemInfo> getUnlockConsume() {
		return ItemInfo.valueListOf(parent.getConfig().getUnlockEnabling());
	}
	
	/**
	 * 获取所有部件id
	 * @return
	 */
	public List<Integer> getAllEnergyIds() {
		List<Integer> list = new ArrayList<>();
		for (ISuperSoldierEnergy energy : energys) {
			list.add(energy.getCfgId());
		}
		return list;
	}
	
	/**
	 * 获取战力
	 * @return
	 */
	public int getPower() {
		int power = 0;
		for (ISuperSoldierEnergy energy : energys) {
			SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, energy.getCfgId());
			power += cfg.getPowerCoeff();
		}
		return power;
	}
	
	/**
	 * 获取作用号
	 * @return
	 */
	public List<PBSuperSoldierEffect> getEffect() {
		List<PBSuperSoldierEffect> effect = new ArrayList<>();
		for (ISuperSoldierEnergy energy : energys) {
			SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, energy.getCfgId());
			String effectList = cfg.getEffectList();
			if (HawkOSOperator.isEmptyString(effectList)) {
				continue;
			}
			String[] split = effectList.split(",");
			for (int i = 0; i < split.length; i++) {
				String effStr = split[i];
				PBSuperSoldierEffect builder = PBSuperSoldierEffect.newBuilder()
					.setEffectId(Integer.parseInt(effStr.split("_")[0]))
					.setValue(Integer.parseInt(effStr.split("_")[1])).build();
				effect.add(builder);
			}
		}
		
		// 解锁赋能作用号
		if (isUnlockEnergy()) {
			String effectList = parent.getConfig().getEnablingEffect();
			if (!HawkOSOperator.isEmptyString(effectList)) {
				String[] split = effectList.split(",");
				for (int i = 0; i < split.length; i++) {
					String effStr = split[i];
					PBSuperSoldierEffect builder = PBSuperSoldierEffect.newBuilder()
							.setEffectId(Integer.parseInt(effStr.split("_")[0]))
							.setValue(Integer.parseInt(effStr.split("_")[1])).build();
					effect.add(builder);
				}
			}
		}
		return effect;
	}
	
	/**
	 * 赋能总等级
	 * @return
	 */
	public int getLevel() {
		int level = 0;
		for (ISuperSoldierEnergy energy : energys) {
			SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, energy.getCfgId());
			level += cfg.getEnablingLevel();
		}
		return level;
	}

	public Set<ISuperSoldierEnergy> getEnergys() {
		return energys;
	}
	
}
