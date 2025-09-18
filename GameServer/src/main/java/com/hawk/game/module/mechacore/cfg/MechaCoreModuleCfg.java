package com.hawk.game.module.mechacore.cfg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心的模块配置
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_module.xml")
public class MechaCoreModuleCfg extends HawkConfigBase {
	/**
	 * 模块id
	 */
	@Id
	protected final int moduleId;
	
	/**
	 * 模块类型
	 */
	protected final int moduleType;
	
	/**
	 * 固定属性
	 */
	protected final String moduleFixedAttr;

	/**
	 * 随机属性池
	 */
	protected final int randomAttrPool;

	/**
	 * 模块品质
	 */
	protected final int moduleQuality;
	
	/**
	 * 分解返还
	 */
	protected final String breakDownGetItem;
	/**
	 * 基础战力
	 */
	protected final int power;
	/**
	 * 对应兵种
	 */
	protected final String troopsTypes;
	
	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	private Map<Integer, Integer> fixedAttrMap = new HashMap<>();
	private Set<Integer> troopsTypeSet = new HashSet<>();
	
	public MechaCoreModuleCfg() {
		moduleId = 0;
		moduleFixedAttr = "";
		randomAttrPool = 0;
		moduleQuality = 0;
		moduleType = 0;
		breakDownGetItem = "";
		power = 0;
		troopsTypes = "";
		atkAttr = "";
		hpAttr = "";
	}
	
	@Override
	protected boolean assemble() {
		fixedAttrMap = SerializeHelper.stringToMap(moduleFixedAttr, Integer.class, Integer.class, "_", ",");
		troopsTypeSet = SerializeHelper.stringToSet(Integer.class, troopsTypes, ",");
		return true;
	}
	
	public Map<Integer, Integer> getFixedAttrMap() {
		return fixedAttrMap;
	}

	public int getId() {
		return moduleId;
	}

	public String getModuleFixedAttr() {
		return moduleFixedAttr;
	}

	public int getRandomAttrPool() {
		return randomAttrPool;
	}

	public int getModuleQuality() {
		return moduleQuality;
	}

	public int getModuleType() {
		return moduleType;
	}

	public String getBreakDownGetItem() {
		return breakDownGetItem;
	}
	
	public int getPower() {
		return power;
	}
	
	public Set<Integer> getTroopsTypeSet() {
		return troopsTypeSet;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
}
