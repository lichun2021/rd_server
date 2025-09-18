package com.hawk.game.player.skill.talent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

/**
 * 天赋技能上下文
 * 
 * @author golden
 *
 */
public class TalentSkillContext {

	/**
	 * 实例
	 */
	private static TalentSkillContext instance;

	/**
	 * 天赋技能map<技能Id, 技能实现类>
	 */
	private Map<Integer, ITalentSkill> talentSkillMap;

	/**
	 * 获取实例
	 * @return
	 */
	public static TalentSkillContext getInstance() {
		if (instance == null) {
			instance = new TalentSkillContext();
		}
		return instance;
	}

	/**
	 * 构造
	 */
	private TalentSkillContext() {
		init();
	}

	/**
	 * 初始化
	 */
	private void init() {
		talentSkillMap = new HashMap<>();
		String packageName = ITalentSkill.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, TalentSkill.class);

		for (Class<?> cls : classList) {
			try {
				talentSkillMap.put(cls.getAnnotation(TalentSkill.class).skillID(), (ITalentSkill) cls.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 获取所有天赋技能
	 * 
	 * @return
	 */
	public List<ITalentSkill> getSkills() {
		List<ITalentSkill> skills = new ArrayList<ITalentSkill>();
		for (ITalentSkill tanlentSkill : talentSkillMap.values()) {
			skills.add(tanlentSkill);
		}
		return skills;
	}

	/**
	 * 获取天赋技能
	 * 
	 * @param skillID
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends ITalentSkill> T getSkill(int skillID) {
		if (!talentSkillMap.containsKey(skillID)) {
			return null;
		}
		return (T) talentSkillMap.get(skillID);
	}
}
