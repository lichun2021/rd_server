package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 超武技能配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/manhattan_sw_skill.xml")
public class ManhattanSWSkillCfg extends HawkConfigBase {

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
	 * 技能等级
	 */
	protected final int level;
	/**
	 * 解锁品阶
	 */
	protected final int unlockStage;

	/**
	 * 技能属性
	 */
	protected final String attr;
	/**
	 * 属性map
	 */
	private Map<Integer, Integer> attrMap = new HashMap<>();
	
	/**
	 * 每一阶对应的配置<swId*1000+stage, config>
	 */
	private static Map<Long, ManhattanSWSkillCfg> stageCfgMap = new HashMap<>();
	
	
	public ManhattanSWSkillCfg() {
		id = 0;
		swId = 0;
		level = 0;
		unlockStage = 0;
		attr = "";
	}
	
	public int getId() {
		return id;
	}

	public int getSwId() {
		return swId;
	}


	public int getLevel() {
		return level;
	}
	
	public int getUnlockStage() {
		return unlockStage;
	}

	public String getAttr() {
		return attr;
	}

	@Override
	protected boolean assemble() {
		attrMap = SerializeHelper.stringToMap(attr, Integer.class, Integer.class, "_", ",");
		long key = getMapKey(swId, unlockStage);
		stageCfgMap.put(key, this);
		return true;
	}
	
	public Map<Integer, Integer> getAttrMap() {
		return attrMap;
	} 
	
	private static long getMapKey(int swId, int stage) {
		return swId * 1000L + stage;
	}
	
	public static ManhattanSWSkillCfg getConfig(int swId, int stage) {
		while (stage > 0) {
			long key = getMapKey(swId, stage);
			ManhattanSWSkillCfg cfg = stageCfgMap.get(key);
			if (cfg != null) {
				return cfg;
			}
			stage--;
		}
		
		return null;
	}
	
}
