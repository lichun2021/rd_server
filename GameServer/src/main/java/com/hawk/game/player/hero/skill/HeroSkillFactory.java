package com.hawk.game.player.hero.skill;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hawk.os.HawkException;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
/**
 * 
 * @author lwt
 * @date 2017年7月26日
 */
public class HeroSkillFactory {
	private static transient HeroSkillFactory INSTANCE;
	private Map<Integer, Class<? extends IHeroSkill>> map;

	private HeroSkillFactory() {
		init();
	}

	/**
	 * 创建空技能 当技能未实现时返回null
	 * 
	 * @param skillID
	 * @return
	 */
	public IHeroSkill createEmptySkill(int skillID) {
		if (!map.containsKey(skillID)) {
			return new CommonSkill(skillID);
		}
		Class<? extends IHeroSkill> cls = map.get(skillID);
		try {
			return cls.newInstance();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public static HeroSkillFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new HeroSkillFactory();

		}

		return INSTANCE;

	}

	@SuppressWarnings("unchecked")
	private void init() {
		map = new HashMap<>();
		String packageName = IHeroSkill.class.getPackage().getName();
		try {
			ClassPath classPath = ClassPath.from(IHeroSkill.class.getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				if (!cls.isAnnotationPresent(HeroSkill.class)) {
					continue;
				}
				if (!IHeroSkill.class.isAssignableFrom(cls)) {
					continue;
				}
				
				for(int skillId: cls.getAnnotation(HeroSkill.class).skillID()){
					map.put(skillId, (Class<? extends IHeroSkill>) cls);
				}


			}
			map = ImmutableMap.copyOf(map);
		} catch (IOException e) {
			HawkException.catchException(e);
		}
	}
}
