package com.hawk.activity.type.impl.seaTreasure.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;

import com.hawk.activity.config.IActivityTimeCfg;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
@HawkConfigManager.XmlResource(file = "activity/sea_treasure/sea_treasure_time.xml")
public class SeaTreasureTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	
	@Id
	private final int termId;

	private final String showTime;

	private final String startTime;

	private final String endTime;

	private final String hiddenTime;
	
	private long showTimeValue;
	
	private long startTimeValue;
	
	private long endTimeValue;
	
	private long hiddenTimeValue;
	
	public SeaTreasureTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
	}

	@Override
	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		return true;
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
}
