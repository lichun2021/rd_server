package com.hawk.activity.timeController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.entity.ActivityTermStatus;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityState;

/**
 * 基础活动时间控制器(按期配置,循环)
 * 
 * @author Jesse
 *
 */
public abstract class ITimeController {

	abstract public Class<? extends HawkConfigBase> getTimeCfgClass();

	/**
	 * 是否基于玩家开启的活动(例: 根据玩家注册时间开启)
	 * 
	 * @return
	 */
	public boolean isPlayerActivityTimeController() {
		return false;
	}

	/**
	 * 服务器开服延时开启活动时间
	 * 
	 * @return
	 */
	abstract public long getServerDelay();

	/**
	 * 获取当期活动时间配置(注册时间类活动)
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	protected Optional<IActivityTimeCfg> getTimeCfg(long now, String playerId) {
		return getTimeCfg(now);
	}

	/**
	 * 获取当期活动时间配置
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			if (now >= timeCfg.getShowTimeValue() && now < timeCfg.getHiddenTimeValue()) {
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}

	/**
	 * 获取当前活动期数(注册开启类活动)
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	public int getActivityTermId(long now, String playerId) {
		if (isPlayerActivityTimeController()) {
			throw new RuntimeException("wrong use, only for Player register time Activity");
		}
		return getActivityTermId(now);
	}

	/**
	 * 获取当前活动期数
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	public int getActivityTermId(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getTermId();
	}

	/**
	 * 获取当期活动的显示时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	protected long getCurShowTime(long now, String playerId) {
		return getCurShowTime(now);
	}

	/**
	 * 获取当期活动的显示时间
	 * 
	 * @param now
	 * @return
	 */
	protected long getCurShowTime(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getShowTimeValue();
	}

	/**
	 * 获取当期活动的开始时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	protected long getCurStartTime(long now, String playerId) {
		return getCurStartTime(now);
	}

	/**
	 * 获取当期活动的开始时间
	 * 
	 * @param now
	 * @return
	 */
	protected long getCurStartTime(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getStartTimeValue();
	}

	/**
	 * 获取当期活动的结束时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	protected long getCurEndTime(long now, String playerId) {
		return getCurEndTime(now);
	}

	/**
	 * 获取当期活动的结束时间
	 * 
	 * @param now
	 * @return
	 */

	protected long getCurEndTime(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getEndTimeValue();
	}

	/**
	 * 获取当期活动的隐藏时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	protected long getCurHiddenTime(long now, String playerId) {
		return getCurHiddenTime(now);
	}

	/**
	 * 获取当期活动的隐藏时间
	 * 
	 * @param now
	 * @return
	 */

	protected long getCurHiddenTime(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getHiddenTimeValue();
	}
	
	/**
	 * 更新活动状态(仅供ActivityManager更新活动状态,外部禁止调用!!!!!)
	 * 
	 * @param config
	 * @param activity
	 */
	public void updateState(ActivityBase activity) {
		ActivityEntity activityEntity = activity.getActivityEntity();
		int activityTermId = activityEntity.getTermId();
		ActivityTermStatus data = getActivityData(activity);
		ActivityState currentState = data.getCurState();
		int currentTermId = data.getTermId();
		ActivityState activityState = activityEntity.getActivityState();
		// 活动未关闭,且entity记录的活动期数大于当前活动期数
		if (currentTermId != 0 && activityTermId > currentTermId) {
			throw new RuntimeException("activity term rollback");
		}
		int cycleLimit = 0;
		final int cycleMaxCnt = 2 * ActivityState.values().length;
		// 上期活动已结束,或者新的一起活动已经开启
		if (currentTermId == 0 || currentTermId > activityTermId) {
			// 处理上一个时间周期的各个时间点
			while (activityState != ActivityState.HIDDEN && cycleLimit <= cycleMaxCnt) {
				cycleLimit ++;
				activityState = toNextState(activity, activityEntity, activityState);
			}
		}

		// 刷新活动期数
		if (currentTermId > activityTermId) {
			activityEntity.setTermId(currentTermId);
		}
		cycleLimit = 0;
		// 当前活动周期内状态切换
		while (activityState != currentState  && cycleLimit <= cycleMaxCnt) {
			cycleLimit++;
			activityState = toNextState(activity, activityEntity, activityState);
		}
	}

	protected ActivityState toNextState(ActivityBase activity, ActivityEntity activityEntity, ActivityState activityState) {
		ActivityState nextState = activityState.nextState();
		activityState = nextState;
		activityEntity.setState(nextState.intValue());
		PlayerPushHelper.getInstance().pushActivityState(activity);
		switch (nextState) {
		case SHOW: {
			activity.onShow();
			break;
		}
		case OPEN: {
			// 活动开启时若活动进行次数大于1则清理玩家活动数据
			activityEntity.setNewlyTime(HawkTime.getNextAM0Date());
			activity.onOpen();
			break;
		}
		case END: {
			activity.onEnd();
			break;
		}
		case HIDDEN: {
			activityEntity.setNewlyTime(0);
			activity.onHidden();
			break;
		}
		default:
			HawkLog.errPrintln("activity next state not case! nextState: {}", nextState);
			break;
		}

		return activityState;
	}

	/**
	 * 获取当前活动状态
	 * 
	 * @param activity
	 * @param timeCfg
	 * @return
	 */
	protected ActivityState getCurrentState(ActivityBase activity) {
		long now = HawkTime.getMillisecond();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return ActivityState.HIDDEN;
		}
		long showTime = getCurShowTime(now);
		long startTime = getCurStartTime(now);
		long endTime = getCurEndTime(now);
		long hiddenTime = getCurHiddenTime(now);

		if (now >= showTime && now < startTime) {
			return ActivityState.SHOW;
		} else if (now >= startTime && now < endTime) {
			return ActivityState.OPEN;
		} else if (now >= endTime && now < hiddenTime) {
			return ActivityState.END;
		} else {
			return ActivityState.SHOW;
		}
	}
	
	private ActivityTermStatus getActivityData(ActivityBase activity){
		ActivityTermStatus data = new ActivityTermStatus();
		long now = HawkTime.getMillisecond();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			data.setCurState(ActivityState.HIDDEN);
			return data;
		}
		data.setTermId(opTimeCfg.get().getTermId());
		long showTime = getCurShowTime(now);
		long startTime = getCurStartTime(now);
		long endTime = getCurEndTime(now);
		long hiddenTime = getCurHiddenTime(now);
		if (inMergeDate(showTime, hiddenTime, activity.getActivityCfg())) {
			data.setCurState(ActivityState.HIDDEN);
			return data; 
		}
		if (now >= showTime && now < startTime) {
			data.setCurState(ActivityState.SHOW);
		} else if (now >= startTime && now < endTime) {
			data.setCurState(ActivityState.OPEN);
		} else if (now >= endTime && now < hiddenTime) {
			data.setCurState(ActivityState.END);
		} else {
			data.setCurState(ActivityState.SHOW);
		}
		return data;
	}

	protected boolean inMergeDate(long showTime, long hiddenTime, ActivityCfg activityCfg) {
		if (!activityCfg.isCheckMergeServer() && !activityCfg.isCheckSeparateServer()) {
			return false;
		}
		
		Long mergeTime = ActivityManager.getInstance().getDataGeter().getServerMergeTime();
		if (mergeTime == null) {
			return false;
		}
		
		return mergeTime >= showTime && mergeTime <= hiddenTime;
	}
	/**
	 * 根据活动期数获取时间配置
	 * 
	 * @param termId
	 * @return
	 */
	protected Optional<IActivityTimeCfg> getTimeCfgByTermId(int termId) {
		IActivityTimeCfg it = (IActivityTimeCfg) HawkConfigManager.getInstance().getConfigByKey(getTimeCfgClass(), termId);
		return Optional.ofNullable(it);
	}

	/**
	 * 根据活动期数活动的显示时间
	 * 
	 * @param termId
	 * @param playerId
	 * @return
	 */
	public long getShowTimeByTermId(int termId, String playerId) {
		return getShowTimeByTermId(termId);
	}

	/**
	 * 根据活动期数活动的显示时间
	 * 
	 * @param now
	 * @return
	 */
	public long getShowTimeByTermId(int termId) {
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getShowTimeValue();
	}

	/**
	 * 根据活动期数活动的开始时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getStartTimeByTermId(int termId, String playerId) {
		return getStartTimeByTermId(termId);
	}

	/**
	 * 根据活动期数活动的开始时间
	 * 
	 * @param now
	 * @return
	 */
	public long getStartTimeByTermId(int termId) {
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getStartTimeValue();
	}

	/**
	 * 根据活动期数活动的结束时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getEndTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId);
	}

	/**
	 * 根据活动期数活动的结束时间
	 * 
	 * @param now
	 * @return
	 */
	public long getEndTimeByTermId(int termId) {
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getEndTimeValue();
	}

	/**
	 * 根据活动期数活动的隐藏时间
	 * 
	 * @param now
	 * @param playerId
	 * @return
	 */
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getHiddenTimeByTermId(termId);
	}

	/**
	 * 根据活动期数活动的隐藏时间
	 * 
	 * @param now
	 * @return
	 */
	public long getHiddenTimeByTermId(int termId) {
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getHiddenTimeValue();
	}

	
	
	public List<long[]> getOpenTimes(long startTime, long endTime,ActivityCfg acfg) {
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		List<long[]> list = new ArrayList<long[]>();
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			long showCfg = timeCfg.getShowTimeValue();
			long startCfg= timeCfg.getStartTimeValue();
			long endCfg = timeCfg.getEndTimeValue();
			long hiddenCfg = timeCfg.getHiddenTimeValue();
			//配置开启   开始   结束  配置结束
			boolean isOpne = true;
			//如果在合服期间
			if (inMergeDate(showCfg, hiddenCfg, acfg)) {
				isOpne = false;
			}
			// 开始  结束 配置开启     配置 结束  
			if (startCfg >=  endTime) {
				isOpne =false;
			}
			//配置开启     配置结束   开始  结束 
			if (startTime >= endCfg) {
				isOpne =false;
			}
			if(isOpne){
				long[] arr = new long[3];
				arr[0] = startCfg;
				arr[1] = endCfg;
				arr[2] = timeCfg.getTermId();
				list.add(arr);
			}
		}
		return list;
	}
	
	/**
	 * #3005620892 【0321版本】【功能优化】调整永久活动排期为限时 https://meego.feishu.cn/ccredalert/story/detail/3005620892
	 * @return
	 */
	public long getForerver2Limit45Start() {
		long time1 = 1710950400000L;// HawkTime.parseTime("2024-03-21 00:00:00");
		long serverOpenDate = ActivityManager.getInstance().getDataGeter().getServerOpenAM0Date();
		return Math.max(time1, serverOpenDate);
	}
}
