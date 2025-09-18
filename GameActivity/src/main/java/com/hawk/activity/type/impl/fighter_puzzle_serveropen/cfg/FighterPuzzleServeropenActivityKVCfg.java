package com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

/**
 * 武者拼图活动配置（新版）
 * 
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.KVResource(file = "activity/fighter_puzzle_sopen/%s/fighter_puzzle_sopen_cfg.xml", autoLoad=false, loadParams="282")
public class FighterPuzzleServeropenActivityKVCfg extends HawkConfigBase {
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

	public FighterPuzzleServeropenActivityKVCfg() {
		buildLevelLimit = 0;
		startTimeLimit ="";
		endTimeLimit="";
	}

	public int getBuildLevelLimit() {
		return buildLevelLimit;
	}


	public String getStartTimeLimit() {
		return startTimeLimit;
	}


	public String getEndTimeLimit() {
		return endTimeLimit;
	}

	public long getTimeStart() {
		return timeStart;
	}


	public void setTimeStart(long timeStart) {
		this.timeStart = timeStart;
	}


	public long getTimeEnd() {
		return timeEnd;
	}


	public void setTimeEnd(long timeEnd) {
		this.timeEnd = timeEnd;
	}


	@Override
	protected boolean assemble() {
		this.timeStart = HawkTime.parseTime(startTimeLimit);
		this.timeEnd   = HawkTime.parseTime(endTimeLimit);
		return true;
	}
	
}
