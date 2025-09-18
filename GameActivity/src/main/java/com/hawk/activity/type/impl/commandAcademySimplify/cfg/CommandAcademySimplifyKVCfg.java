package com.hawk.activity.type.impl.commandAcademySimplify.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
/**
 * 场景分享活动配置
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/commander_college_cut/%s/commander_college_cut_cfg.xml", autoLoad=false, loadParams="284")
public class CommandAcademySimplifyKVCfg extends HawkConfigBase {
	/** 总榜ID*/
	private final int cycleRankId;
	/** 时间限制字符串*/
	private final String timeLimit;

	private long activityTimeLimt;
	
	/**
	 *活动开启大本限制 
	 */
	private final int buildLevelLimit;
	/**
	 * 开服时间在startTimeLimit-endTimeLimit之内开启活动
	 */
	private final String startTimeLimit;
	private final String endTimeLimit;
	
	private long timeStart;
	private long timeEnd;
	
	public CommandAcademySimplifyKVCfg() {
	
		cycleRankId = 0;
		timeLimit = "";
		buildLevelLimit=0;
		startTimeLimit="";
		endTimeLimit="";
	}

	public long getActivityTimeLimt() {
		return activityTimeLimt;
	}

	public int getCycleRankId() {
		return cycleRankId;
	}

	public int getBuildLevelLimit() {
		return buildLevelLimit;
	}

	public long getTimeStart() {
		return timeStart;
	}

	public long getTimeEnd() {
		return timeEnd;
	}

	@Override
	protected boolean assemble() {
		this.activityTimeLimt = HawkTime.parseTime(this.timeLimit);
		this.timeStart = HawkTime.parseTime(startTimeLimit);
		this.timeEnd   = HawkTime.parseTime(endTimeLimit);
		return true;
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

}
