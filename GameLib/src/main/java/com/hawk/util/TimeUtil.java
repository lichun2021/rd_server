package com.hawk.util;

import java.util.Calendar;

import org.hawk.os.HawkTime;

public class TimeUtil {
	/**
	 * 给定整点重置时间与当前时间进行比较，判断是否需要进行重置
	 * @param resetHour			整点小时数
	 * @param now				当前时间
	 * @param lastResetTime		最后一次重置时间
	 * @return
	 */
	public static boolean isNeedReset(int resetHour, long now, long lastResetTime) {
		Calendar calendar = HawkTime.getCalendar(true);
		calendar.set(Calendar.HOUR_OF_DAY, resetHour);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		long todayResetTime = calendar.getTimeInMillis();
		// 若当前时间在今天的刷新时间之前，则对最后一次重置时间与昨天的刷新时间进行比较，否则对最后一次重置时间与今天的刷新时间进行比较
		if (now < todayResetTime) {
			// 若最后一次重置时间在昨天刷新时间之前，则重置数据，否则不重置
			calendar.add(Calendar.DAY_OF_YEAR, -1);
			long yesterdayResetTime = calendar.getTimeInMillis();
			if (lastResetTime < yesterdayResetTime) {
				return true;
			} else {
				return false;
			}
		} else {
			// 若最后一次重置时间在今天刷新时间之前，则重置数据，否则不重置
			if (lastResetTime < todayResetTime) {
				return true;
			} else {
				return false;
			}
		}
	}
}
