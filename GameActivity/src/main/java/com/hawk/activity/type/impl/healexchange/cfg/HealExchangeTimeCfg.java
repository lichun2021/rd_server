package com.hawk.activity.type.impl.healexchange.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import com.hawk.activity.config.IActivityTimeCfg;

/**
 * @author Winder
 *
 */
@HawkConfigManager.XmlResource(file = "activity/heal_exchange/heal_exchange_time.xml")
public class HealExchangeTimeCfg extends HawkConfigBase implements IActivityTimeCfg {
	
	@Id
	private final int termId;

	private final String showTime;

	private final String startTime;

	private final String endTime;

	private final String hiddenTime;
	private final String battleTime;

	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;

	public HealExchangeTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		battleTime = "";
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

	public String getBattleTime() {
		return battleTime;
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
