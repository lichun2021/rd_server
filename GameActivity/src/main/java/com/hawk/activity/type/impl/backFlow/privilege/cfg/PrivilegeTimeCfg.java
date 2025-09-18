package com.hawk.activity.type.impl.backFlow.privilege.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import com.hawk.activity.config.IActivityTimeCfg;

@HawkConfigManager.XmlResource(file = "activity/back_privilege_activity/back_privilege_activity_time.xml")
public class PrivilegeTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 预览时间*/
	private final String showTime;
	
	/** 开启时间*/
	private final String startTime;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 消失时间*/
	private final String hiddenTime;
	
	/** 停止触发时间*/
	private final String stopTrigger;
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	private long stopTriggerValue;
	
	public PrivilegeTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		stopTrigger = "";
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
		stopTriggerValue =  HawkTime.parseTime(stopTrigger);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if(stopTriggerValue > hiddenTimeValue){
			return false;
		}
		if(stopTriggerValue < startTimeValue){
			return false;
		}
		return checkTimeCfgValid(this.getClass());
	}

	public long getStopTriggerValue() {
		return stopTriggerValue;
	}
	
}
