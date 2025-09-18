package com.hawk.game.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.KVResource;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.util.HawkClassScaner;

import com.hawk.activity.config.ActivityCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

public class ActivityConfigLoader {

	/**
	 * 单例
	 */
	private static ActivityConfigLoader instance;

	/**
	 * 活动配置版本
	 */
	private Map<String, String> activityCfgVersion;
	
	/**
	 * 获取单例
	 * 
	 * @return
	 */
	public static ActivityConfigLoader getInstance() {
		if (instance == null) {
			instance = new ActivityConfigLoader();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 */
	public void init() {
		activityCfgVersion = RedisProxy.getInstance().getAllActiviyCfgVersion();
	}
	
	/**
	 * 重设配置文件路径
	 * @return
	 * @throws Exception
	 */
	public boolean resetConfigFilePath() {
		try {
			List<Class<? extends HawkConfigBase>> needLoadClass = scanNeedLoadClass();
			for (Class<? extends HawkConfigBase> clazz : needLoadClass) {
				String filePath = "";
				
				// xml配置
				if (clazz.getAnnotation(HawkConfigManager.XmlResource.class) != null) {
					HawkConfigManager.XmlResource xmlRes = clazz.getAnnotation(HawkConfigManager.XmlResource.class);
					String version = getVersionByActivityId(xmlRes.loadParams());
					filePath = String.format(xmlRes.file(), version);
				}
				
				// kv配置
				if (clazz.getAnnotation(HawkConfigManager.KVResource.class) != null) {
					HawkConfigManager.KVResource kvRes = clazz.getAnnotation(HawkConfigManager.KVResource.class);
					String version = getVersionByActivityId(kvRes.loadParams());
					filePath = String.format(kvRes.file(), version);
				}
				
				// 设置配置文件filePath
				if (!HawkOSOperator.isEmptyString(filePath)) {
					HawkConfigManager.getInstance().setFilePath(clazz, filePath);
					HawkLog.logPrintln("resetConfigFilePath, class:{}, path:{}", clazz.getName(), filePath);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 扫描需要手动加载的配置文件
	 * @return
	 */
	private List<Class<? extends HawkConfigBase>> scanNeedLoadClass() {
		List<Class<? extends HawkConfigBase>> retClasses = new ArrayList<>();
		String configPackages = GsConfig.getInstance().getConfigPackages();
		if (HawkOSOperator.isEmptyString(configPackages)) {
			return retClasses;
		}
		String[] configPackageArray = configPackages.split(",");
		if (configPackageArray == null || configPackageArray.length <= 0) {
			return retClasses;
		}
		for (String configPackage : configPackageArray) {
			configPackage = configPackage.trim();
			List<Class<?>> classList = HawkClassScaner.scanClassesFilter(configPackage, XmlResource.class, KVResource.class);
			for (Class<?> configClass : classList) {
				if (configClass.getAnnotation(HawkConfigManager.XmlResource.class) != null) {
					HawkConfigManager.XmlResource xmlRes = configClass.getAnnotation(HawkConfigManager.XmlResource.class);
					// 已经自动装载的配置不处理
					if (xmlRes.autoLoad()) {
						continue;
					}
				} else if (configClass.getAnnotation(HawkConfigManager.KVResource.class) != null) {
					HawkConfigManager.KVResource kvRes = configClass.getAnnotation(HawkConfigManager.KVResource.class);
					// 已经自动装载的配置不处理
					if (kvRes.autoLoad()) {
						continue;
					}
				} else if (configClass.getSuperclass() != HawkConfigBase.class) {
					continue;
				}
				@SuppressWarnings("unchecked")
				Class<? extends HawkConfigBase> clazz = (Class<? extends HawkConfigBase>) configClass;
				retClasses.add(clazz);
			}
		}
		return retClasses;
	}
	
	/**
	 * 根据活动id获取配置版本
	 * @param activityId
	 */
	private String getVersionByActivityId(String activityId) {
		String version = activityCfgVersion.get(activityId);
		if (HawkOSOperator.isEmptyString(version)) {
			ActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, Integer.valueOf(activityId));
			int cfgVersion = cfg.getCfgVersion();
			if (cfgVersion == 0) {
				throw new RuntimeException("getVersionByActivityId error, activityId:" + activityId);
			}
			version = "version" + cfgVersion;
			activityCfgVersion.put(activityId, version);
			RedisProxy.getInstance().updateActivityCfgVersion(activityId, version);
		}
		return version;
	}
	
	/**
	 * 获取活动配置版本
	 * @return
	 */
	public Map<String, String> getActivityCfgVersionMap() {
		return activityCfgVersion;
	}
}
