package com.hawk.activity.type.impl.urlModelTwo.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;


/**
 * url模版活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/url_model_two/url_model_two_time.xml")
public class UrlModelTwoActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
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
	
	public UrlModelTwoActivityTimeCfg() {
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
		return checkTimeCfgValid(this.getClass());
	}
}
