package com.hawk.activity.type.impl.redrecharge.cfg;

import com.hawk.activity.config.IActivityTimeCfg;

import java.security.InvalidParameterException;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


@HawkConfigManager.XmlResource(file = "activity/red_recharge/red_recharge_activity_time.xml")
public class HappyRedRechargeTimeConfig extends HawkConfigBase implements IActivityTimeCfg{
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
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public HappyRedRechargeTimeConfig() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
	}
	
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
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if (HawkTime.formatTime(endTimeValue, "yyyy-MM-dd HH:mm:ss").endsWith("00:00:00") ||
				HawkTime.formatTime(hiddenTimeValue, "yyyy-MM-dd HH:mm:ss").endsWith("00:00:00")) {
			throw new InvalidParameterException(String.format("red_recharge_activity_time.xml 配置错误， termId: %d", termId));
		}
		return checkTimeCfgValid(this.getClass());
	}
}
