package com.hawk.activity.type.impl.commandAcademySimplify.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
/**
 *  指挥官学院活动时间配置
 * @author huangfei -> lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/commander_college_cut/%s/commander_college_cut_time.xml", autoLoad=false, loadParams="284")
public class CommandAcademySimplifyActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	@Id
	private final int termId;
	
	private final long showTime;

	private final long startTime;

	private final long endTime;

	private final long hiddenTime;

	private  long showTimeValue;

	private  long startTimeValue;

	private  long endTimeValue;

	private  long hiddenTimeValue;
	

	public CommandAcademySimplifyActivityTimeCfg() {
		termId = 0;
		showTime = 0;
		startTime = 0;
		endTime = 0;
		hiddenTime = 0;
	}

	@Override
	public int getTermId() {
		return termId;
	}

	
	@Override
	public long getShowTimeValue() {
		return showTimeValue;
	}

	@Override
	public long getStartTimeValue() {
		return startTimeValue;
	}

	@Override
	public long getEndTimeValue() {
		return endTimeValue;
	}

	@Override
	public long getHiddenTimeValue() {
		return hiddenTimeValue;
	}

	@Override
	protected boolean assemble() {
		showTimeValue = showTime * 1000;
		startTimeValue = startTime * 1000;
		endTimeValue = endTime * 1000;
		hiddenTimeValue = hiddenTime *1000;
		return true;
	}
}
