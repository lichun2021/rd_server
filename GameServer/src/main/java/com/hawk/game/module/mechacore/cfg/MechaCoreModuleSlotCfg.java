package com.hawk.game.module.mechacore.cfg;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲核心的槽位表
 * @author lating
 */
@HawkConfigManager.XmlResource(file = "xml/mecha_core_module_slot.xml")
public class MechaCoreModuleSlotCfg extends HawkConfigBase {

	@Id
	protected final int id;
	/**
	 * 核心槽等级
	 */
	protected final int slotLevel;
	/**
	 * 槽位类型
	 */
	protected final int slotType;
	/**
	 * 升级消耗
	 */
	protected final String needItem;
	/**
	 * 对应品质成长系数 
	 */
	protected final String upFactor;

	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	private Map<Integer, Integer> qualityUpFactorMap = new HashMap<>();

	private static Map<Integer, Integer> slotTypeMaxLevelMap = new HashMap<>();
	private static Map<Integer, Integer> slotInitIdMap = new HashMap<>();
	private static Map<Integer, MechaCoreModuleSlotCfg> slotLevelConfigMap = new HashMap<>();
	
	public MechaCoreModuleSlotCfg() {
		id = 0;
		slotLevel = 0;
		upFactor = "";
		slotType = 0;
		needItem = "";
		atkAttr = "";
		hpAttr = "";
	}
	
	@Override
	protected boolean assemble() {
		if (slotLevel == 0) {
			slotInitIdMap.put(slotType, id);
		}
		int oldLevel = slotTypeMaxLevelMap.getOrDefault(slotType, 0);
		if (slotLevel > oldLevel) {
			slotTypeMaxLevelMap.put(slotType, slotLevel);
		}
		int key = slotType * 10000 + slotLevel;
		slotLevelConfigMap.put(key, this);
		
		String[] array = upFactor.split(",");
		for (String kv : array) {
			String[] kvStr = kv.split("_");
			qualityUpFactorMap.put(Integer.parseInt(kvStr[0]), Integer.parseInt(kvStr[1]));
		}
		return true;
	}
	
	public Map<Integer, Integer> getQualityUpFactorMap() {
		return qualityUpFactorMap;
	}
	
	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public int getSlotLevel() {
		return slotLevel;
	}

	public int getSlotType() {
		return slotType;
	}

	public static Set<Integer> getSlotTypes() {
		return slotTypeMaxLevelMap.keySet();
	}
	
	public static int getMaxLevel(int type) {
		return slotTypeMaxLevelMap.getOrDefault(type, 0);
	}
	
	public static int getInitId(int type) {
		return slotInitIdMap.get(type);
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

    public int getHpAttr(int soldierType) {
        return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
    }
	
	public static MechaCoreModuleSlotCfg getConfig(int type, int level) {
		int key = type * 10000 + level;
		return slotLevelConfigMap.get(key);
	}
}
