package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigBase.CombineId;

import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.util.GameUtil;

/**
 * 天赋
 * 
 * @author golden
 * 
 */
@HawkConfigManager.XmlResource(file = "xml/talent_level.xml")
@CombineId(fields = {"talentId", "level"})
public class TalentLevelCfg extends HawkConfigBase {

	protected final int id;

	protected final int level;

	protected final int talentId;

	/** 作用号  格式：作用号id_数值*/
	protected final String effect;

	protected final int skill;

	protected final int point;

	/**
	 * 触发作用号列表
	 */
	private List<EffectObject> effList;
	
	public TalentLevelCfg() {
		id = 0;
		level = 0;
		talentId = 0;
		effect = "";
		skill = 0;
		point = 0;
	}

	@Override
	protected boolean assemble() {
		effList = GameUtil.assambleEffectObject(effect);
		return super.assemble();
	}

	public int getId() {
		return id;
	}

	public int getLevel() {
		return level;
	}

	public int getTalentId() {
		return talentId;
	}

	public String getEffect() {
		return effect;
	}

	public int getSkill() {
		return skill;
	}

	public int getPoint() {
		return point;
	}

	public List<EffectObject> getEffList() {
		return effList;
	}
}
