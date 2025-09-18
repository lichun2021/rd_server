package com.hawk.game.module.dayazhizhan.playerteam.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;


/**
 * 机甲觉醒活动时间配置
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/dyzz_time.xml")
public class DYZZTimeCfg extends HawkConfigBase{
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
	
	public DYZZTimeCfg() {
		termId = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
	}
	
	public int getTermId() {
		return termId;
	}

	
	public long getShowTimeValue() {
		return showTimeValue;
	}
	
	
	public long getStartTimeValue() {
		return startTimeValue;
	}
	
	
	public long getEndTimeValue() {
		return endTimeValue;
	}
	
	
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
		ConfigIterator<DYZZSeasonTimeCfg> seasonit = HawkConfigManager.getInstance().getConfigIterator(DYZZSeasonTimeCfg.class);
		ConfigIterator<DYZZTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(DYZZTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (DYZZTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" DYZZTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			
			if (showTime < baseTime || startTime < showTime || endTime < startTime
					|| hiddenTime < endTime) {
				HawkLog.errPrintln(" DYZZTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			baseTime = hiddenTime;
		}
		return true;
	}
}
