package com.hawk.activity.type.impl.giftzeronew.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;


/**
 * 新0元礼包活动配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/new_gift_zero/%s/activity_new_gift_zero_time.xml", autoLoad=false, loadParams="262")
public class GiftZeroNewActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
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
	/**
	 * 活动开启临界时间，开发时间在这之前的不开启活动
	 */
	private final String triggerTime;
	
	private long triggerTimeValue;
	
	public GiftZeroNewActivityTimeCfg() {
		termId = 0;
		showTime = 0;
		startTime = 0;
		endTime = 0;
		hiddenTime = 0;
		triggerTime = "";
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
	
	public long getTriggerTimeValue() {
		return triggerTimeValue;
	}

	@Override
	protected boolean assemble() {
		triggerTimeValue = HawkTime.parseTime(triggerTime);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}
