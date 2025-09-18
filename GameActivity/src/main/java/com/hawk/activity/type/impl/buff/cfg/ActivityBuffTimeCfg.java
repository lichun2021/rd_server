package com.hawk.activity.type.impl.buff.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;

@HawkConfigManager.XmlResource(file = "activity/server_buff/activity_buff_time.xml")
public class ActivityBuffTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	@Id
	private final int termId;
	private final long showTime;
	private final long startTime;
	private final long endTime;
	private final long hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public ActivityBuffTimeCfg() {
		this.termId = 0;
		this.showTime = 0;
		this.startTime = 0;
		this.endTime = 0;
		this.hiddenTime = 0;
	}
	
	@Override
	public int getTermId() {
		return this.termId;
	}

	@Override
	public long getShowTimeValue() {
		return this.showTimeValue;
	}

	@Override
	public long getStartTimeValue() {
		return this.startTimeValue;
	}

	@Override
	public long getEndTimeValue() {
		return this.endTimeValue;
	}

	@Override
	public long getHiddenTimeValue() {
		return this.hiddenTimeValue;
	}
	
	@Override
	public boolean assemble() {
		this.startTimeValue = this.startTime * 1000;
		this.showTimeValue = this.showTime * 1000;
		this.endTimeValue = this.endTime * 1000;
		this.hiddenTimeValue = this.hiddenTime * 1000;
		return true;
	}

	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
	
	
}
