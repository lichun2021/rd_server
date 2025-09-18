package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武基座升阶
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_base_stage.xml")
public class ManhattanBaseStageCfg extends HawkConfigBase {

	/**
	 * id
	 */
	@Id
	protected final int id;
	
	/**
	 * 阶级
	 */
	protected final int stage;
	
	/**
	 * 升级消耗
	 */
	protected final String consumption;
	
	/**
	 * 战力
	 */
	protected final int power;
	
	/**
	 * 属性
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
	 * 每一阶对应的配置
	 */
	private static Map<Integer, ManhattanBaseStageCfg> stageCfgMap = new HashMap<>();
	
	public ManhattanBaseStageCfg() {
		id = 0;
		stage = 0;
		consumption = "";
		power = 0;
		attr = "";
		atkAttr = "";
		hpAttr = "";
	}

	public int getId() {
		return id;
	}
	
	public int getStage() {
		return stage;
	}

	public String getConsumption() {
		return consumption;
	}

	public int getPower() {
		return power;
	}

	public String getAttr() {
		return attr;
	}

	@Override
	protected boolean assemble() {
		stageCfgMap.put(stage, this);
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

	
	public static ManhattanBaseStageCfg getConfigByStage(int stage) {
		return stageCfgMap.get(stage);
	}
	
}
