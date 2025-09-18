package com.hawk.activity.type.impl.commandAcademy.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
/**
 * 场景分享活动配置
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/commander_college/%s/commander_college_cfg.xml", autoLoad=false, loadParams="162")
public class CommandAcademyKVCfg extends HawkConfigBase {

	
	/** 总榜ID*/
	private final int cycleRankId;
	/** 时间限制字符串*/
	private final String timeLimit;

	private long activityTimeLimt;
	/**
	 * 开服时间在startTimeLimit-endTimeLimit之内开启活动
	 */
	private final String startTimeLimit;
	private final String endTimeLimit;
	private long timeStart;
	private long timeEnd;
	
	public CommandAcademyKVCfg() {
	
		cycleRankId = 0;
		timeLimit = "";
		startTimeLimit="";
		endTimeLimit="";
	}

	public long getActivityTimeLimt() {
		return activityTimeLimt;
	}

	public int getCycleRankId() {
		return cycleRankId;
	}

	@Override
	protected boolean assemble() {
		this.activityTimeLimt = HawkTime.parseTime(this.timeLimit);
		this.timeStart = HawkTime.parseTime(startTimeLimit);
		this.timeEnd   = HawkTime.parseTime(endTimeLimit);
		return true;
	}

	public long getTimeStart() {
		return timeStart;
	}

	public long getTimeEnd() {
		return timeEnd;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

}
