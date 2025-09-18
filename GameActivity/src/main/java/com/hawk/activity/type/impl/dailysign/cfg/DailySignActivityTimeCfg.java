package com.hawk.activity.type.impl.dailysign.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.config.IActivityTimeCfg;

@HawkConfigManager.XmlResource(file = "activity/daily_sign/daily_sign_time.xml")
public class DailySignActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg {


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
	public DailySignActivityTimeCfg() {
		termId = 0;
		showTime = 0;
		startTime = 0;
		endTime = 0;
		hiddenTime = 0;
		showTimeValue = 0;
		startTimeValue = 0;
		endTimeValue = 0;
		hiddenTimeValue = 0;
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
