package com.hawk.activity.type.impl.medalFund.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import com.hawk.activity.config.IActivityTimeCfg;

@HawkConfigManager.XmlResource(file = "activity/medal_invest/medal_invest_time.xml")
public class MedalFundTimeCfg extends HawkConfigBase implements IActivityTimeCfg{
	
	@Id
	private final int termId;
	
	private final String showTime;
	
	private final String startTime;
	
	//购买截至时间
	private final String buyEndTime;
	
	private final String endTime;
	
	private final String hiddenTime;
	
	private long showTimeValue;
	private long startTimeValue;
	private long buyEndTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public MedalFundTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		buyEndTime = "";
		endTime = "";
		hiddenTime = "";
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

	public long getBuyEndTimeValue() {
		return buyEndTimeValue;
	}

	public String getBuyEndTime() {
		return buyEndTime;
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
		buyEndTimeValue = HawkTime.parseTime(buyEndTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}
