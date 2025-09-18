package com.hawk.game.heroTrial;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.game.heroTrial.mission.HeroTrialChecker;
import com.hawk.game.heroTrial.mission.IHeroTrialChecker;

public class HeroTrialCheckerFactory {

	private static HeroTrialCheckerFactory instance;

	private Map<HeroTrialType, IHeroTrialChecker> map;
	
	public static HeroTrialCheckerFactory getInstance() {
		if (instance == null) {
			instance = new HeroTrialCheckerFactory();
		}
		return instance;
	}
	
	private HeroTrialCheckerFactory() {
		
	}
	
	public void init() {
		map = new ConcurrentHashMap<HeroTrialType, IHeroTrialChecker>();
		
		String packageName = IHeroTrialChecker.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, HeroTrialChecker.class);
		for (Class<?> cls : classList) {
			try {
				map.put(cls.getAnnotation(HeroTrialChecker.class).type(), (IHeroTrialChecker) cls.newInstance());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	public IHeroTrialChecker getChecker(int type) {
		HeroTrialType trialType = HeroTrialType.valueOf(type);
		if (trialType == null) {
			return null;
		}
		return map.get(trialType);
	}
}
