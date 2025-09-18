package com.hawk.game.player.supersoldier.energy;

import java.util.Arrays;
import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;

import com.alibaba.fastjson.JSONArray;
import com.hawk.game.config.SuperSoldierEnergyCfg;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.supersoldier.SuperSoldierEnergy;

/**
 * 机甲赋能部件
 * @author Golden
 *
 */
public class ISuperSoldierEnergy {
	
	/**
	 * 机甲赋能模块
	 */
	private SuperSoldierEnergy parent;
	
	/**
	 * id
	 */
	private int cfgId;
	
	/**
	 * 构造方法
	 * @param energy
	 */
	public ISuperSoldierEnergy(SuperSoldierEnergy energy) {
		this.parent = energy;
	}
	
	/**
	 * 加载
	 * @param str
	 * @return
	 */
	public boolean load(String str) {
		JSONArray array = JSONArray.parseArray(str);
		this.cfgId = array.getIntValue(0);
		return true;
	}

	/**
	 * 初始化
	 * @param id
	 * @return
	 */
	public boolean init(int id) {
		SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, id);
		this.cfgId = cfg.getId();
		return true;
	}
	
	/**
	 * 升级
	 */
	public void upLevel() {
		this.cfgId = getNextLevelCfg().getId();
		parent.notifyUpdate();
	}
	
	/**
	 * GM直接设置等级
	 */
	public void gmSetLevel(int cfgId) {
		this.cfgId = cfgId;
		parent.notifyUpdate();
	}
	
	/**
	 * 序列化
	 * @return
	 */
	public String serializ() {
		Object[] arr = new Object[1];
		arr[0] = cfgId;
		JSONArray array = new JSONArray(Arrays.asList(arr));
		return array.toJSONString();
	}
	
	/**
	 * 是否已经达到最大等级
	 * @return
	 */
	public boolean isMaxLevel() {
		return getNextLevelCfg() == null;
	}
	
	/**
	 * 位置升级限制
	 * @return
	 */
	public boolean positionUpLevelLimit() {
		SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, cfgId);
		// 第五条线需要总等级10级解锁,暂时写死
		if (cfg.getEnablingPosition() == 5 && parent.getLevel() < 10) {
			return true;
		}
		return false;
	}
	
	/**
	 * 获取赋能消耗
	 * @return
	 */
	public List<ItemInfo> getLevelConsume() {
		return ItemInfo.valueListOf(getNextLevelCfg().getEnablingCost());
	}
	
	/**
	 * 获取下一等级配置
	 * @return
	 */
	private SuperSoldierEnergyCfg getNextLevelCfg() {
		SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, cfgId);
		int pos = cfg.getEnablingPosition();
		int nextLevel = cfg.getEnablingLevel() + 1;
		int soldierId = cfg.getSupersoldierId();
		
		ConfigIterator<SuperSoldierEnergyCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierEnergyCfg.class);
		while (cfgIter.hasNext()) {
			SuperSoldierEnergyCfg nextCfg = cfgIter.next();
			if (nextCfg.getEnablingPosition() == pos && nextCfg.getEnablingLevel() == nextLevel && nextCfg.getSupersoldierId() == soldierId) {
				return nextCfg;
			}
		}
		return null;
	}
	
	/**
	 * 获取赋能部件位置
	 * @return
	 */
	public int getPos() {
		SuperSoldierEnergyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SuperSoldierEnergyCfg.class, cfgId);
		return cfg.getEnablingPosition();
	}

	/**
	 * 配置id
	 * @return
	 */
	public int getCfgId() {
		return cfgId;
	}
}
