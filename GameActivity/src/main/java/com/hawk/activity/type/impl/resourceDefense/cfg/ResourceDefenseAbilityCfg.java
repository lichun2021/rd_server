package com.hawk.activity.type.impl.resourceDefense.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源保卫战特工能力配置
 * @author hf
 *
 */
@HawkConfigManager.XmlResource(file = "activity/resource_defense/resource_defense_ability.xml")
public class ResourceDefenseAbilityCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int skillType;
	//品质
	private final int quality;
	private final int group;
	private final int randomWeight;
	private final int effectNum;
	private final int para;
	//Map<group, Map<id, odd>>
	private static Map<Integer, Map<Integer, Integer>> groupMap = new HashMap<>();

	public ResourceDefenseAbilityCfg(){
		id = 0;
		skillType = 0;
		quality = 0;
		group = 0;
		randomWeight = 0;
		effectNum = 0;
		para = 0;
	}


	@Override
	protected boolean assemble() {
		Map<Integer, Integer> map = null;
		try {
			map = groupMap.getOrDefault(group, new HashMap<>());
			map.put(id, randomWeight);
			groupMap.put(group, map);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	public Map<Integer, Map<Integer, Integer>> getGroupMap() {
		return groupMap;
	}

	public int getId() {
		return id;
	}

	public int getSkillType() {
		return skillType;
	}

	public int getQuality() {
		return quality;
	}

	public int getGroup() {
		return group;
	}

	public int getRandomWeight() {
		return randomWeight;
	}

	public int getEffectNum() {
		return effectNum;
	}

	public int getPara() {
		return para;
	}

}
