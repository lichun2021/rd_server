package com.hawk.game.crossproxy.skill;

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
public class CrossSkillFactory {
	private static transient CrossSkillFactory INSTANCE;
	private Map<String, Class<? extends ICrossSkill>> map;

	private CrossSkillFactory() {
		init();
	}

	public ICrossSkill createEmptySkill(String skillID) {
		if (!map.containsKey(skillID)) {
			return new CommonCrossSkill(skillID);
		}
		Class<? extends ICrossSkill> cls = map.get(skillID);
		try {
			return cls.newInstance();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public static CrossSkillFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrossSkillFactory();

		}

		return INSTANCE;

	}

	@SuppressWarnings("unchecked")
	private void init() {
		map = new HashMap<>();
		String packageName = ICrossSkill.class.getPackage().getName();
		try {
			ClassPath classPath = ClassPath.from(ICrossSkill.class.getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				if (!cls.isAnnotationPresent(CrossSkill.class)) {
					continue;
				}
				if (!ICrossSkill.class.isAssignableFrom(cls)) {
					continue;
				}

				map.put(cls.getAnnotation(CrossSkill.class).skillID(), (Class<? extends ICrossSkill>) cls);

			}
			map = ImmutableMap.copyOf(map);
		} catch (IOException e) {
			HawkException.catchException(e);
		}
	}
}
