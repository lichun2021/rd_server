package com.hawk.game.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;

/**
 * 国家科技
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/nation_tech_level.xml")
public class NationTechCfg extends HawkConfigBase {
	
	@Id
	private final int id;
	
	/**
	 * 科技等级
	 */
	private final int techLevel;
	
	/**
	 * 科技id
	 */
	private final int techId;
	
	/**
	 * 科技类型
	 */
	private final int techType;
	
	/**
	 * 前置科技
	 */
	private final String frontTech;
	
	/**
	 * 前置建筑等级
	 */
	private final int frontBuildLv;
	
	/**
	 * 研究消耗
	 */
	private final int techCost;
	
	/**
	 * 研究时间
	 */
	private final int techTime;
	
	/**
	 * 作用号
	 */
	private final String techEffect;
	
	/**
	 * 科技技能cd
	 */
	private final int techSkillCd;
	
	/**
	 * 释放技能消耗科技值
	 */
	private final int skillCost;
	
	/**
	 * 技能参数
	 */
	private final String param1;
	private final String param2;
	private final String param3;
	private final String param4;
	private final String param5;
	
	/**
	 * 作用号集合
	 */
	private List<EffectObject> effect;
	
	/**
	 * 前置建筑要求集合
	 */
	private Map<Integer, Integer> frontTechMap;
	
	public NationTechCfg() {
		id = 0;
		techLevel = 0;
		techId = 0;
		techType = 0;
		frontTech = "";
		frontBuildLv = 0;
		techCost = 0;
		techTime = 0;
		techEffect = "";
		techSkillCd = 0;
		skillCost = 0;
		param1 = "";
		param2 = "";
		param3 = "";
		param4 = "";
		param5 = "";
	}

	public int getId() {
		return id;
	}

	public int getTechLevel() {
		return techLevel;
	}

	public int getTechId() {
		return techId;
	}

	public int getTechType() {
		return techType;
	}

	public String getFrontTech() {
		return frontTech;
	}

	public int getFrontBuildLv() {
		return frontBuildLv;
	}

	public int getTechCost() {
		return techCost;
	}

	public long getTechTime() {
		return techTime * 1000L;
	}

	public String getTechEffect() {
		return techEffect;
	}

	public long getTechSkillCd() {
		return techSkillCd * 1000L;
	}

	public int getSkillCost() {
		return skillCost;
	}

	public List<EffectObject> getEffect() {
		return effect;
	}

	public Map<Integer, Integer> getFrontTechMap() {
		return frontTechMap;
	}
	
	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public String getParam3() {
		return param3;
	}

	public String getParam4() {
		return param4;
	}

	public String getParam5() {
		return param5;
	}
	
	@Override
	protected boolean assemble() {
		try {
			
			this.effect = GameUtil.assambleEffectObject(this.techEffect);
			
			Map<Integer, Integer> frontTechMap = new HashMap<>();
			if (!HawkOSOperator.isEmptyString(frontTech)) {
				String[] split = frontTech.split(",");
				for (int i = 0; i < split.length; i++) {
					String[] split2 = split[i].split("_");
					frontTechMap.put(Integer.valueOf(split2[0]), Integer.valueOf(split2[1]));
				}
			}
			this.frontTechMap = frontTechMap;
			
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		return true;
	}
}

