package com.hawk.game.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.hawk.game.protocol.Const.EffType;

@HawkConfigManager.XmlResource(file = "xml/cross_skill.xml")
public class CrossSkillCfg extends HawkConfigBase {

	@Id
	protected final String id;
	protected final int crosstype;// ="1"
	protected final int crossCd;// ="300"
	protected final String crossMutexSkills;// ="2_3"
	protected final int continueTime;// ="120"
	protected final String effect;// ="110_3000,132_3000"
	protected final int uiType;// ="1"
	protected final int buffId;// ="0"
	protected final String param1;// =""
	protected final String param2;// =""
	protected final String param3;// ="" />

	private Set<String> mutexSkills;
	private Map<EffType, Integer> effectMap;

	public CrossSkillCfg() {
		id = "";
		crosstype = 1;
		crossCd = 300;
		crossMutexSkills = "2_3";
		continueTime = 120;
		effect = "110_3000,132_3000";
		uiType = 1;
		buffId = 0;
		param1 = "";
		param2 = "";
		param3 = "";
	}

	public boolean isMutexSkill(String skillId) {
		return mutexSkills.contains(skillId);
	}

	public Map<EffType, Integer> getEffectMap() {
		return effectMap;
	}

	public String getEffect() {
		return effect;
	}

	public int getEffectVal(EffType effType) {
		return effectMap.getOrDefault(effType, 0);
	}

	@Override
	protected boolean assemble() {
		mutexSkills = Splitter.on("_").splitToList(crossMutexSkills).stream().collect(Collectors.toSet());

		Map<EffType, Integer> tampMap = new HashMap<>();
		if (!HawkOSOperator.isEmptyString(effect)) {
			String[] array = effect.split(",");
			for (String val : array) {
				String[] info = val.split("_");
				tampMap.put(EffType.valueOf(Integer.parseInt(info[0])), Integer.parseInt(info[1]));
			}
		}

		this.effectMap = ImmutableMap.copyOf(tampMap);
		return true;
	}

	public String getId() {
		return id;
	}

	public int getCrosstype() {
		return crosstype;
	}

	public int getCrossCd() {
		return crossCd;
	}

	public String getCrossMutexSkills() {
		return crossMutexSkills;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public int getUiType() {
		return uiType;
	}

	public int getBuffId() {
		return buffId;
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

}
