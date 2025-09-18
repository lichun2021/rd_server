package com.hawk.activity;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;

/**
 * 活动逻辑对象工厂
 * @author PhilChen
 *
 */
public class GameActivityFactory {

	static final Logger logger = LoggerFactory.getLogger("Server");

	private static GameActivityFactory factory;

	private Map<ActivityType, ActivityBase> activityMap = new HashMap<>();

	public static GameActivityFactory getInstance() {
		if (factory == null) {
			factory = new GameActivityFactory();
		}
		return factory;
	}

	public void init() {
		for (ActivityType activityType : ActivityType.values()) {
			registerActivity(activityType.getActivity());
		}
	}

	public void registerActivity(ActivityBase activity) {
		activityMap.put(activity.getActivityType(), activity);
	}

	public ActivityBase buildActivity(ActivityCfg config, ActivityEntity activityEntity) {
		ActivityBase gameActivity = activityMap.get(config.getType());
		if (gameActivity == null) {
			logger.error("game activity not found! activityId: {}, activityType: {}", config.getActivityId(), config.getType());
			return null;
		}
		return gameActivity.newInstance(config, activityEntity);
	}

}
