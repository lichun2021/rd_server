package com.hawk.activity.type.impl.logingift.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.config.IActivityTimeCfg;


@HawkConfigManager.XmlResource(file = "activity/login_gifts/%s/login_gifts_time.xml", autoLoad=false, loadParams="313")
public class LoginGiftActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 预览时间*/
	private final long showTime; 
	
	/** 开启时间*/
	private final long startTime;
	
	/** 结束时间*/
	private final long endTime;
	
	/** 消失时间*/
	private final long hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public LoginGiftActivityTimeCfg() {
		termId = 0;
		showTime = 0;
		startTime = 0;
		endTime = 0;
		hiddenTime = 0;
	}
	
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
