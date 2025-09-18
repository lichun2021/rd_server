package com.hawk.activity.type.impl.recallFriend.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;

@XmlResource(file = "activity/recall_friend/recall_friend_time.xml")
public class RecallFriendTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
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
	
	public RecallFriendTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
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
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		
		return true;
	}
}
