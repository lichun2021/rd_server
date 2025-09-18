package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武品阶配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_sw_stage.xml")
public class ManhattanSWStageCfg extends HawkConfigBase {

	/**
	 * 序列id
	 */
	@Id
	protected final int id;

	/**
	 * 对应超武
	 */
	protected final int swId;
	
	/**
	 * 对应品阶
	 */
	protected final int stage;
	
	/**
	 * 前置底座品阶
	 */
	protected final int unlockBaseStage;

	/**
	 * 升阶消耗
	 */
	protected final String consumption;
	
	/**
	 * 消耗返还
	 */
	protected final String returnConsumption;

	/**
	 * 品阶战力
	 */
	protected final int power;
	
	/**
	 * 品阶属性
	 */
	protected final String attr;
	
	/**
     * 强度配置
     */
    protected final String atkAttr;
    protected final String hpAttr;
	
	

	
	/**
	 * 属性map
	 */
	private Map<Integer, Integer> attrMap = new HashMap<>();
	
	/**
	 * 每一阶对应的配置<sw*1000+stage, config>
	 */
	private static Map<Long, ManhattanSWStageCfg> stageCfgMap = new HashMap<>();
	
	public ManhattanSWStageCfg() {
		id = 0;
		swId = 0;
		stage = 0;
		unlockBaseStage = 0;
		consumption = "";
		returnConsumption = "";
		power = 0;
		attr = "";
		
		atkAttr = "";
		hpAttr = "";
	}
	
	public int getId() {
		return id;
	}

	public int getSwId() {
		return swId;
	}

	public int getStage() {
		return stage;
	}

	public int getUnlockBaseStage() {
		return unlockBaseStage;
	}

	public String getConsumption() {
		return consumption;
	}
	
	public String getReturnConsumption() {
		return returnConsumption;
	}

	public int getPower() {
		return power;
	}

	public String getAttr() {
		return attr;
	}

	@Override
	protected boolean assemble() {
		long key = getMapKey(swId, stage);
		stageCfgMap.put(key, this);
		attrMap = SerializeHelper.stringToMap(attr, Integer.class, Integer.class, "_", ",");
		return true;
	}
	
	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 
	
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}
	
	public int getHpAttr(int soldierType) {
	    return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
	
	private static long getMapKey(int swId, int stage) {
		return swId * 1000L + stage;
	}
	
	public static ManhattanSWStageCfg getConfig(int swId, int stage) {
		long key = getMapKey(swId, stage);
		return stageCfgMap.get(key);
	}
}
