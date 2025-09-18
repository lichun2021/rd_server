package com.hawk.activity.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;

public interface IActivityTimeCfg {
	/**
	 * 获取活动期数
	 * @return
	 */
	public int getTermId();
	
	/**
	 * 获取活动开始显示时间
	 * @return
	 */
	public long getShowTimeValue();
	
	/**
	 * 获取活动开始时间
	 * @return
	 */
	public long getStartTimeValue();
	
	/**
	 * 获取活动结束时间
	 * @return
	 */
	public long getEndTimeValue();
	
	/**
	 * 获取活动消失时间
	 * @return
	 */
	public long getHiddenTimeValue();
	
	/**
	 * 检测活动时间配置正确性
	 * @return
	 * @throws Exception 
	 */
	default boolean checkTimeCfgValid(Class<? extends HawkConfigBase> t) {
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(t);
		long baseTime = 0;
		long baseTermId = 0;
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			int termId = timeCfg.getTermId();
			if (termId <= baseTermId) {
				HawkLog.errPrintln(" activityTimeConfig check valid failed, term order error: className: {}, termId: {}", cfg.getClass().getName(), termId);
				throw new RuntimeException("activityTimeConfig check valid failed");
			}
			baseTermId = termId;
			long showTime = timeCfg.getShowTimeValue();
			long startTime = timeCfg.getStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			long hiddenTime = timeCfg.getHiddenTimeValue();
			if (showTime < baseTime || startTime < showTime || endTime < startTime || hiddenTime < endTime) {
				HawkLog.errPrintln(" activityTimeConfig check valid failed, time error, className: {}, termId: {}", cfg.getClass().getName(), termId);
				throw new RuntimeException("activityTimeConfig check valid failed");
			}
			baseTime = hiddenTime;
		}
		return true;
	}
}
