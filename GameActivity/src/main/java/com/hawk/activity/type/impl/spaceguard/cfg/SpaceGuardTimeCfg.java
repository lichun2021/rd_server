package com.hawk.activity.type.impl.spaceguard.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import com.hawk.activity.config.IActivityTimeCfg;

@HawkConfigManager.XmlResource(file = "activity/space_machine_guard/space_machine_guard_time.xml")
public class SpaceGuardTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
	
	@Id
	private final int termId;
	
	private final String showTime;
	
	private final String startTime;
	
	private final String stopTime;
	
	private final String endTime;
	
	private final String hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long stopTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public SpaceGuardTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		stopTime = "";
		endTime = "";
		hiddenTime = "";
	}
	
	@Override
	public int getTermId() {
		return termId;
	}

	public String getShowTime() {
		return showTime;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getHiddenTime() {
		return hiddenTime;
	}

	@Override
	public long getShowTimeValue() {
		return showTimeValue;
	}

	@Override
	public long getStartTimeValue() {
		return startTimeValue;
	}
	
	public long getStopTimeValue() {
		return stopTimeValue;
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
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		if (!HawkOSOperator.isEmptyString(stopTime)) {
			stopTimeValue = HawkTime.parseTime(stopTime);
		} else {
			stopTimeValue = endTimeValue - HawkTime.MINUTE_MILLI_SECONDS * 30;
		}
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
	
}
