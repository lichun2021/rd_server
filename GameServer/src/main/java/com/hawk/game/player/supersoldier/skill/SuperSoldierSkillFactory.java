package com.hawk.game.player.supersoldier.skill;

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
public class SuperSoldierSkillFactory {
	private static transient SuperSoldierSkillFactory INSTANCE;
	private Map<Integer, Class<? extends ISuperSoldierSkill>> map;

	private SuperSoldierSkillFactory() {
		init();
	}

	/**
	 * 创建空技能 当技能未实现时返回null
	 * 
	 * @param skillID
	 * @return
	 */
	public ISuperSoldierSkill createEmptySkill(int skillID) {
		if (!map.containsKey(skillID)) {
			return new CommonSuperSoldierSkill(skillID);
		}
		Class<? extends ISuperSoldierSkill> cls = map.get(skillID);
		try {
			return cls.newInstance();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public static SuperSoldierSkillFactory getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new SuperSoldierSkillFactory();

		}

		return INSTANCE;

	}

	@SuppressWarnings("unchecked")
	private void init() {
		map = new HashMap<>();
		String packageName = ISuperSoldierSkill.class.getPackage().getName();
		try {
			ClassPath classPath = ClassPath.from(ISuperSoldierSkill.class.getClassLoader());
			ImmutableSet<ClassInfo> set = classPath.getTopLevelClasses(packageName);
			for (ClassInfo info : set) {
				Class<?> cls = info.load();
				if (!cls.isAnnotationPresent(SuperSoldierSkill.class)) {
					continue;
				}
				if (!ISuperSoldierSkill.class.isAssignableFrom(cls)) {
					continue;
				}

				map.put(cls.getAnnotation(SuperSoldierSkill.class).skillID(), (Class<? extends ISuperSoldierSkill>) cls);

			}
			map = ImmutableMap.copyOf(map);
		} catch (IOException e) {
			HawkException.catchException(e);
		}
	}
}
