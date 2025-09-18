package com.hawk.activity.type.impl.coreplate.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;

/**
 * 壮志雄心活动
 */
@HawkConfigManager.XmlResource(file = "activity/coreplate/%s/coreplate_activity_time.xml", autoLoad=false, loadParams="283")
public class CoreplateActivityTimeCfg extends HawkConfigBase implements IActivityTimeCfg{

	@Id
	private final int termId;

	private final long showTime;

	private final long startTime;

	private final long endTime;

	private final long hiddenTime;


	public CoreplateActivityTimeCfg() {
		termId = 0;
		showTime = 0;
		startTime = 0;
		endTime = 0;
		hiddenTime = 0;
	}

	@Override
	public int getTermId() {
		return termId;
	}


	@Override
	public long getShowTimeValue() {
		return this.showTime * 1000;
	}

	@Override
	public long getStartTimeValue() {
		return this.startTime * 1000;
	}

	@Override
	public long getEndTimeValue() {
		return this.endTime * 1000;
	}

	@Override
	public long getHiddenTimeValue() {
		return this.hiddenTime * 1000;
	}


	@Override
	protected boolean checkValid() {
		return checkTimeCfgValid(this.getClass());
	}
}
