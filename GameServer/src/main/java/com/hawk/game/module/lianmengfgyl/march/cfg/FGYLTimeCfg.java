package com.hawk.game.module.lianmengfgyl.march.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;


/**
 * 月球之战时间配置
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "xml/fgyl_time.xml")
public class FGYLTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int termId;

	/** 预览时间 */
	private final String showTime;

	/** 开启时间 */
	private final String startTime;

	/** 结算发奖时间*/
	private final String endTime;
	
	private final String hiddenTime;

	private long showTimeValue;
	private long startTimeValue;
	private long endTimeValue;
	private long hiddenTimeValue;

	

	public FGYLTimeCfg() {
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
	

	protected boolean assemble() {
		showTimeValue = HawkTime.parseTime(showTime);
		startTimeValue = HawkTime.parseTime(startTime);
		endTimeValue = HawkTime.parseTime(endTime);
		hiddenTimeValue = HawkTime.parseTime(hiddenTime);
		return true;
	}

	@Override
	protected boolean checkValid() {
		ConfigIterator<FGYLTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(FGYLTimeCfg.class);
		long baseTime = 0;
		long baseTermId = 0;
		for (FGYLTimeCfg timeCfg : it) {
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" FGYLTimeCfg check valid failed, term order error:termId: {}", termId);
				return false;
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			if (showTime < baseTime || startTime < showTime || endTime < startTime ||
					hiddenTime < endTime ) {
				HawkLog.errPrintln(" FGYLTimeCfg check valid failed, termId: {}", termId);
				return false;
			}
			baseTime = hiddenTime;
		}
		return true;
	}


}
