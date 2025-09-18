package com.hawk.activity.type.impl.boss_invade.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;


/**
 * 首领入侵
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "activity/boss_invade/boss_invade_activity_time.xml")
public class BossInvadeActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
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
	
	public BossInvadeActivityTimeCfg() {
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
		return showTime * 1000;
	}
	
	@Override
	public long getStartTimeValue() {
		return startTime * 1000;
	}
	
	@Override
	public long getEndTimeValue() {
		return endTime * 1000;
	}
	
	@Override
	public long getHiddenTimeValue() {
		return hiddenTime * 1000;
	}

	@Override
	protected boolean assemble() {
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}
