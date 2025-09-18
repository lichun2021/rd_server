package com.hawk.activity.type.impl.skinPlan.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;

/**
 * 皮肤计划
 * @author Winder
 *
 */
@HawkConfigManager.XmlResource(file ="activity/hero_skin/hero_skin_time.xml")
public class SkinPlanActivityTimerCfg extends HawkConfigBase implements IActivityTimeCfg{
	//活动期数
	@Id
	private final int termId;
	//预览时间
	private final String showTime;
	//开始时间
	private final String startTime;
	//结束时间
	private final String endTime;
	//消失时间
	private final String hiddenTime;
	
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	
	
	public SkinPlanActivityTimerCfg() {
		this.termId = 0;
		this.showTime = "";
		this.startTime = "";
		this.endTime = "";
		this.hiddenTime = "";
	}

	@Override
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
