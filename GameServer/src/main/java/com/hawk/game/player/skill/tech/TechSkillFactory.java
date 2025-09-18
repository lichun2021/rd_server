package com.hawk.game.player.skill.tech;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

public class TechSkillFactory {

	private static TechSkillFactory instance;

	private Map<Integer, ITechSkill> techSkillMap;
	
	private TechSkillFactory() {}
	
	static{
		instance = new TechSkillFactory();
		instance.init();
	}
	
	public static TechSkillFactory getInstance() {
		return instance;
	}

	/**
	 * 初始化
	 */
	private void init() {
		techSkillMap = new HashMap<>();
		String packageName = ITechSkill.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, TechSkill.class);

		for (Class<?> cls : classList) {
			try {
				techSkillMap.put(cls.getAnnotation(TechSkill.class).skillID(), (ITechSkill)cls.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 获取科技技能
	 * @param skillID
	 * @return
	 */
	public ITechSkill getSkill(int skillID) {
		if (!techSkillMap.containsKey(skillID)) {
			return null;
		}
		return techSkillMap.get(skillID);
	}
}
