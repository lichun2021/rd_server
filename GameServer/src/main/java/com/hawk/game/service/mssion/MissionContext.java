package com.hawk.game.service.mssion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.util.HawkClassScaner;

import com.hawk.game.service.mssion.funtype.IFunMission;
import com.hawk.game.service.mssion.type.IMission;

/**
 * 任务上下文
 * 
 * @author golden
 *
 */
public class MissionContext {

	/**
	 * 实例
	 */
	private static MissionContext instance;

	/**
	 * 任务
	 */
	private static Map<MissionType, IMission> missions;
	
	/**
	 * 主线任务专属
	 */
	private static Map<Integer, IFunMission> funMissions;
	
	/**
	 * 构造
	 */
	private MissionContext() {
		
	}

	/**
	 * 获取实例
	 * @return
	 */
	public static MissionContext getInstance() {
		if (instance == null) {
			instance = new MissionContext();
		}
		
		return instance;
	}

	/**
	 * 获取任务
	 * @param missionType
	 * @return
	 */
	public IMission getMissions(MissionType missionType) {
		return missions.get(missionType);
	}
	
	/**
	 * 获取主线任务
	 * @param funType
	 * @return
	 */
	public IFunMission getMissions(int funType) {
		return funMissions.get(funType);
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		missions = new HashMap<MissionType, IMission>();
		String packageName = IMission.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, Mission.class);
		for (Class<?> cls : classList) {
			try {
				missions.put(cls.getAnnotation(Mission.class).missionType(), (IMission) cls.newInstance());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		funMissions = new HashMap<Integer, IFunMission>();
		packageName = IFunMission.class.getPackage().getName();
		classList = HawkClassScaner.getAllClasses(packageName);
		for (Class<?> cls : classList) {
			if (cls.isInterface()) {
				continue;
			}
			
			try {
				IFunMission funMission = (IFunMission) cls.newInstance();
				funMissions.put(funMission.getFunType().intValue(), funMission);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
}
