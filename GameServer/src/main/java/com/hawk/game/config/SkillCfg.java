package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "xml/skill.xml")
public class SkillCfg extends HawkConfigBase {
	@Id
	protected final int id;// ="10104"
	protected final int type;// ="1"
	protected final int cd;// ="43200"
	protected final int mutexSkills;// ="10103"
	protected final int continueTime;// ="-1"
	protected final int uiType;// 1 = 指挥官技能 2 = 科技 3 = 英雄技能 4 = 龙
	protected final int heroLevel;// ="38" 英雄等级
	protected final int talentId;// ="10217"要求点出天赋
	protected final String effect;// "352_800" 英雄被动技能作用号
	protected final String param1;// ="5000000"
	protected final String param2;// ="3"
	protected final String param3;// ="200000" />
	protected final int buffId;
	protected final int tblyUse;
	
	public SkillCfg() {
		id = 0;
		type = 0;
		cd = 0;
		mutexSkills = 0;
		continueTime = 0;
		uiType = 0;
		heroLevel = 0;
		talentId = 0;
		effect = "";
		param1 = "";
		param2 = "";
		param3 = "";
		buffId = 0;
		tblyUse = 0;
	}

	public int getId() {
		return id;
	}

	public int getType() {
		return type;
	}

	public int getCd() {
		return cd;
	}

	public int getMutexSkills() {
		return mutexSkills;
	}

	public int getContinueTime() {
		return continueTime;
	}

	public int getUiType() {
		return uiType;
	}

	public String getEffect() {
		return effect;
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

	public int getHeroLevel() {
		return heroLevel;
	}

	public int getTalentId() {
		return talentId;
	}

	public int getBuffId() {
		return buffId;
	}

	public int getTblyUse() {
		return tblyUse;
	}
	
}
