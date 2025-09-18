package com.hawk.robot.action.march;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.util.HawkClassScaner;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;

public class MarchFactory {

	public static final String RESP_PACK_PATH = "com.hawk.robot.action.march.type.impl";
	private Map<String, Class<?>> typeMarchs = new HashMap<>();
	private Map<MarchType, Integer> marchTypeWeights = new LinkedHashMap<>();
	private Map<MarchType, Integer> simpleMarchWeights = new LinkedHashMap<>();
	private static MarchFactory instance = null;
	
	public static MarchFactory getInstance() {
		if (instance == null) {
			instance = new MarchFactory();
			instance.scanMarchClasses();
			instance.initMarchTypeWeight();
		}
		return instance;
	}
	
	private void scanMarchClasses() {
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(RESP_PACK_PATH, RobotMarch.class);
		for (Class<?> clzz : classList) {
			try {
				RobotMarch annotation = clzz.getAnnotation(RobotMarch.class);
				if (annotation != null && !HawkOSOperator.isEmptyString(annotation.marchType())) {
					typeMarchs.put(annotation.marchType(), clzz);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private void initMarchTypeWeight() {
		String weightStrs = GameRobotApp.getInstance().getConfig().getString("marchTypeWeight");
		if (HawkOSOperator.isEmptyString(weightStrs)) {
			return;
		}
		
		try {
			for (String weightStr : weightStrs.split(";")) {
				String[] typeWeight = weightStr.split("_");
				MarchType type = MarchType.valueOf(Integer.valueOf(typeWeight[0]));
				if (type == null) {
					continue;
				}
				
				int weight = Integer.valueOf(typeWeight[1]);
				marchTypeWeights.put(type, weight);
				if (MarchType.isSimpleMarchType(type)) {
					simpleMarchWeights.put(type, weight);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}
	
	/**
	 * 随机获取一个行军类型
	 * 
	 * @param simpleMarch 
	 * @return
	 */
	public March randomMarch(boolean simpleMarch) {
		MarchType marchType = null;
		if(simpleMarch) {
			marchType = HawkRand.randomWeightObject(simpleMarchWeights);
		} else {
			marchType = HawkRand.randomWeightObject(marchTypeWeights);
		}

		return getMarch(marchType);
	}
	
	/**
	 * 通过marchType获取行军类型
	 * @param marchType
	 * @return
	 */
	public March getMarch(MarchType marchType) {
		
		if(!typeMarchs.containsKey(marchType.name())) {
			return null;
		}
		
		Class<?> clazz = typeMarchs.get(marchType.name());
		try {
			return (March) clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			HawkException.catchException(e);
		}
		
		return null;
	}
}
