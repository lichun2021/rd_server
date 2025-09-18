package com.hawk.activity.timeController.impl;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;

/**
 * 永久开放
 * 
 * @author golden
 *
 */
public class PerpetualOpenTimeController extends ITimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return null;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	/**
	 * 获取最近一期活动期数
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public int getRecentActivityTermId(long now, String playerId) {
		return 0;
	}
	
	/**
	 * 获取当前活动状态
	 */
	protected ActivityState getCurrentState(ActivityBase activity) {
		return ActivityState.OPEN;
	}
	
	/**
	 * 更新活动状态
	 * 
	 * @param activity
	 */
	public void updateState(ActivityBase activity) {
		if (activity.getActivityEntity().getState() != ActivityState.OPEN.intValue()) {
			activity.getActivityEntity().setState(ActivityState.OPEN.intValue());
			PlayerPushHelper.getInstance().pushActivityState(activity);
		}
	}
	
	/**
	 * 根据活动期数活动的显示时间
	 * 
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public long getShowTimeByTermId(int termId, String playerId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的显示时间
	 * 
	 * @param now
	 * @return
	 */
	public long getShowTimeByTermId(int termId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的开始时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getStartTimeByTermId(int termId, String playerId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的开始时间
	 * 
	 * @param now
	 * @return
	 */
	public long getStartTimeByTermId(int termId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的结束时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getEndTimeByTermId(int termId, String playerId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的结束时间
	 * 
	 * @param now
	 * @return
	 */
	public long getEndTimeByTermId(int termId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的隐藏时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return 0L;
	}

	/**
	 * 根据活动期数活动的隐藏时间
	 * 
	 * @param now
	 * @return
	 */
	public long getHiddenTimeByTermId(int termId) {
		return 0L;
	}
}
