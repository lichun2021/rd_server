package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.os.HawkOSOperator;

/**
 * 荣耀
 * 
 * @author lwt
 * @date 2019年2月22日
 */
@HawkConfigManager.XmlResource(file = "xml/battle_soldier_honor.xml")
@HawkConfigBase.CombineId(fields = { "id", "honor", "skillLevel","militaryLevel" })
public class BattleSoldierHonorCfg extends HawkConfigBase {
	protected final int id;
	// 兵种荣耀
	protected final int honor;
	// 泰能兵技能强化等级
	protected final int skillLevel;
	// 攻击
	protected final double attack;
	// 生命
	protected final double hp;
	// 防御
	protected final double defence;
	// 技能id
	protected final String skillIds;

	protected final int militaryLevel;
	// 技能列表
	protected List<Integer> skillIdList;

	public BattleSoldierHonorCfg() {
		id = 0;
		honor = 0;
		attack = 0;
		defence = 0;
		hp = 0;
		skillLevel = 0;
		skillIds = "";
		militaryLevel = 0;
		skillIdList = new ArrayList<>();
	}

	@Override
	protected boolean checkValid() {
		for (int skillId : skillIdList) {
			if (skillId <= 0) {
				continue;
			}
			BattleSoldierSkillCfg skillcfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierSkillCfg.class, skillId);
			HawkAssert.notNull(skillcfg, "BattleSoldierSkillCfg not found skillId = " + skillId);

		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getHonor() {
		return honor;
	}

	public List<Integer> getSkillIdList() {
		return skillIdList;
	}

	public double getAttack() {
		return attack;
	}

	public double getDefence() {
		return defence;
	}

	public double getHp() {
		return hp;
	}

	@Override
	protected boolean assemble() {
		if (!HawkOSOperator.isEmptyString(skillIds)) {
			String[] type_strs = skillIds.split("_");
			for (String type_str : type_strs) {
				skillIdList.add(Integer.parseInt(type_str));
			}
		}
		return true;
	}

	public int getSkillLevel() {
		return skillLevel;
	}

	public int getMilitaryLevel() {
		return militaryLevel;
	}
}
