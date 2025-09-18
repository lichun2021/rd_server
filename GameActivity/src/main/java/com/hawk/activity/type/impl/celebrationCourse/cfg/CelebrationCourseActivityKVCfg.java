package com.hawk.activity.type.impl.celebrationCourse.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 配置
 * @author luke
 *
 */
@HawkConfigManager.KVResource(file = "activity/celebration_course/celebration_course_cfg.xml")
public class CelebrationCourseActivityKVCfg extends HawkConfigBase {

	//服务器开服延时开启活动时间
	private final int serverDelay;

	/**
	 * 分享获得奖励
	 */
	private final String shareReward;
	
	public CelebrationCourseActivityKVCfg() {
		serverDelay = 0;
		shareReward="";
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	public String getShareReward() {
		return shareReward;
	}
	
}
