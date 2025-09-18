package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;


/**
 * 航海赛季
 * @author chechangda
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cross_season_time.xml")
public class CrossSeasonTimeCfg extends HawkConfigBase{
	/** 活动期数*/
	@Id
	private final int season;
	
	/** 预览时间*/
	private final String showTime; 
	
	/** 开启时间*/
	private final String startTime;
	
	/** 结束时间*/
	private final String endTime;
	
	/** 消失时间*/
	private final String hiddenTime;
	
	private final int startTerm;
	
	private final int endTerm;
	
	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;
	
	public CrossSeasonTimeCfg() {
		season = 0;
		showTime = "";
		startTime = "";
		endTime = "";
		hiddenTime = "";
		startTerm = 0;
		endTerm = 0;
	}
	
	public int getSeason() {
		return season;
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
	
	public int getStartTerm() {
		return startTerm;
	}
	
	public int getEndTerm() {
		return endTerm;
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
		ConfigIterator<CrossSeasonTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(CrossSeasonTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (CrossSeasonTimeCfg timeCfg : it) {
			int termId = timeCfg.getSeason();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" CrossSeasonTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			
			if (showTime < baseTime || startTime < showTime || endTime < startTime
					|| hiddenTime < endTime) {
				HawkLog.errPrintln(" CrossSeasonTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			baseTime = hiddenTime;
		}
		return true;
	}
}
