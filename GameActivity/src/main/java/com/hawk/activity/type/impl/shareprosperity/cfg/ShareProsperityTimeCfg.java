package com.hawk.activity.type.impl.shareprosperity.cfg;

import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

@HawkConfigManager.XmlResource(file = "activity/share_prosperity/%s/share_prosperity_time.xml", autoLoad=false, loadParams="376")
public class ShareProsperityTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
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
	
	public ShareProsperityTimeCfg() {
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
		return showTime * 1000L;
	}
	
	@Override
	public long getStartTimeValue() {
		return startTime * 1000L;
	}
	
	@Override
	public long getEndTimeValue() {
		return endTime * 1000L;
	}
	
	@Override
	public long getHiddenTimeValue() {
		return hiddenTime * 1000L;
	}

	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}