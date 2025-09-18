package com.hawk.activity.type.impl.alliesWishing.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;


/**
 * 装扮投放系列活动三:重燃战火
 * @author hf
 */
@HawkConfigManager.XmlResource(file = "activity/alliance_wish/alliance_wish_time.xml")
public class AllianceWishTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
	/** 活动期数*/
	@Id
	private final int termId;
	
	/** 预览时间*/
	private final String showTime; 
	
	/** 开启时间*/
	private final String startTime;
	
	/**
	 * 签到结束时间
	 */
	private final String signEndTime;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 消失时间*/
	private final String hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long signEndTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public AllianceWishTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		signEndTime= "";
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
		signEndTimeValue =  HawkTime.parseTime(signEndTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		if(this.signEndTimeValue > this.endTimeValue){
			return false;
		}
		if(this.signEndTimeValue < this.startTimeValue){
			return false;
		}
		return checkTimeCfgValid(this.getClass());
	}
	
	
	public long getSignEndTimeValue() {
		return signEndTimeValue;
	}
}
