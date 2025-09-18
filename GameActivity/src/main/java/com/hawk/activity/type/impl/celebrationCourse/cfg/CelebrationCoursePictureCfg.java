package com.hawk.activity.type.impl.celebrationCourse.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * @author luke
 */
@HawkConfigManager.XmlResource(file = "activity/celebration_course/celebration_course_picture.xml")
public class CelebrationCoursePictureCfg extends HawkConfigBase {
	/** 成就id */
	@Id
	private final int id;
	/** 条件类型 */
	private final int sharePicUnlockTime;

	public CelebrationCoursePictureCfg() {
		id = 0;
		sharePicUnlockTime = 0;
	}

	public int getId() {
		return id;
	}

	public int getSharePicUnlockTime() {
		return sharePicUnlockTime;
	}
	
	
	

}

