package com.hawk.activity.timeController.impl;

import java.util.Collection;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.entity.ActivityTermStatus;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.gamelib.GameConst.MsgId;

/**
 * 根据玩家注册时间开启活动的时间控制器(每个玩家是独立的活动周期)
 * 
 * @author Jesse
 *
 */
public abstract class RegisterTimeController extends ITimeController {

	/**
	 * 是否基于玩家开启的活动(例: 根据玩家注册时间开启)
	 */
	@Override
	public boolean isPlayerActivityTimeController() {
		return true;
	}

	/**
	 * 获取当期活动时间配置
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	protected Optional<IActivityTimeCfg> getTimeCfg(long now, String playerId) {
		long registerTime = ActivityManager.getInstance().getPlayerRegistTime(playerId);
		long timeGap = now - registerTime;
		ConfigIterator<? extends HawkConfigBase> it = HawkConfigManager.getInstance().getConfigIterator(getTimeCfgClass());
		for (HawkConfigBase cfg : it) {
			IActivityTimeCfg timeCfg = (IActivityTimeCfg) cfg;
			if (timeGap >= timeCfg.getShowTimeValue() && timeGap < timeCfg.getHiddenTimeValue()) {
				return Optional.of(timeCfg);
			}
		}
		return Optional.empty();
	}

	/**
	 * 获取当前活动期数
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	public int getActivityTermId(long now, String playerId) {
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getTermId();
	}

	/**
	 * 获取当期活动的显示时间
	 * 
	 * @param playerId
	 * @param now
	 * @param activityCfg
	 * @param entity
	 * @return
	 */
	protected long getCurShowTime(long now, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getShowTimeValue() + registerTime;
	}

	/**
	 * 获取当期活动的开始时间
	 * 
	 * @param playerId
	 * @param now
	 * @param activityCfg
	 * @param entity
	 * @return
	 */
	protected long getCurStartTime(long now, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getStartTimeValue() + registerTime;
	}

	/**
	 * 获取当期活动的结束时间
	 * 
	 * @param playerId
	 * @param now
	 * @param activityCfg
	 * @param entity
	 * @return
	 */
	protected long getCurEndTime(long now, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getEndTimeValue() + registerTime;
	}

	/**
	 * 获取当期活动的隐藏时间
	 * 
	 * @param playerId
	 * @param now
	 * @param activityCfg
	 * @param entity
	 * @return
	 */
	protected long getCurHiddenTime(long now, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return 0;
		}
		return opTimeCfg.get().getHiddenTimeValue() + registerTime;
	}

	/********************************************/
	/********************************************/
	/********************************************/
	/********************************************/
	/********************************************/

	/**
	 * 更新活动状态
	 * 
	 * @param config
	 * @param activity
	 */
	public void updateState(ActivityBase activity) {
		Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
		ActivityTermStatus data = new ActivityTermStatus();
		for (String playerId : onlinePlayerIds) {
			try {
				if(!activity.isChannelAllow(playerId) || !activity.isVersionAllow(playerId)){
					continue;
				}
				
				ActivityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityCfg.class, activity.getActivityId());
				if (cfg == null) {
					continue;
				}
				// 跨服玩家,且该活动跨服开关为关闭,不处理
				if (ActivityManager.getInstance().getDataGeter().isCrossPlayer(playerId) && !cfg.isCrossOpen()) {
					continue;
				}
				
				
				data.clear();
				getActivityData(data, playerId, activity.getActivityCfg());
				int currentTermId = data.getTermId();
				ActivityState currentState = data.getCurState();
				ActivityPlayerEntity activityPlayerEntity = activity.getPlayerActivityEntity(playerId);
				ActivityState activityState = activityPlayerEntity.getActivityState();
				int activityTermId = activityPlayerEntity.getTermId();
				
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
						cycleLimit++;
						activityState = toNextState(activity, activityPlayerEntity, activityState);
					}
				}
				
				// 刷新活动期数
				if (currentTermId > activityTermId) {
					activityPlayerEntity.setTermId(currentTermId);
				}
				
				// 当前活动周期内状态切换
				cycleLimit = 0;
				while (activityState != currentState && cycleLimit <= cycleMaxCnt) {
					cycleLimit++;
					activityState = toNextState(activity, activityPlayerEntity, activityState);
				}
			} catch (Exception e) {
				HawkException.catchException(e, playerId);
			}
		}
	}
	
	/**
	 * 活动进入下一状态
	 * 
	 * @param activity
	 * @param activityPlayerEntity
	 * @param activityState
	 * @return
	 */
	protected ActivityState toNextState(ActivityBase activity, ActivityPlayerEntity activityPlayerEntity, ActivityState activityState) {
		ActivityState nextState = activityState.nextState();
		String playerId = activityPlayerEntity.getPlayerId();
		activityState = nextState;
		activityPlayerEntity.setState(nextState.intValue());
		PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, activity);
		switch (nextState) {
		case SHOW: {
			activity.callBack(playerId, MsgId.REGISTER_ACTIVITY_UPDATE_STATE, ()-> {
				activity.onShowForPlayer(playerId);
			});
			break;
		}
		case OPEN: {
			activityPlayerEntity.setNewlyTime(HawkTime.getNextAM0Date());
			activity.callBack(playerId, MsgId.REGISTER_ACTIVITY_UPDATE_STATE, ()-> {
				activity.onOpenForPlayer(playerId);
			});
			break;
		}
		case END: {
			activity.callBack(playerId, MsgId.REGISTER_ACTIVITY_UPDATE_STATE, ()-> {
				activity.onEndForPlayer(playerId);
			});
			break;
		}
		case HIDDEN: {
			activityPlayerEntity.setNewlyTime(0);
			activity.callBack(playerId, MsgId.REGISTER_ACTIVITY_UPDATE_STATE, ()-> {
				activity.onHiddenForPlayer(playerId);
			});
			break;
		}
		default:
			HawkLog.errPrintln("activity next state not case! nextState: {}", nextState);
			break;
		}
		return activityState;
	}

	/**
	 * 获取当前实际活动状态
	 * 
	 * @param activity
	 * @param timeCfg
	 * @param playerId
	 * @return
	 */
	public ActivityState getCurrentState(ActivityBase activity, String playerId) {
		long now = HawkTime.getMillisecond();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			return ActivityState.HIDDEN;
		}

		long showTime = getCurShowTime(now, playerId);
		long startTime = getCurStartTime(now, playerId);
		long endTime = getCurEndTime(now, playerId);
		long hiddenTime = getCurHiddenTime(now, playerId);

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
	
	public void getActivityData(ActivityTermStatus data, String playerId, ActivityCfg activityCfg){
		long now = HawkTime.getMillisecond();
		Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now, playerId);
		if (!opTimeCfg.isPresent()) {
			data.setCurState(ActivityState.HIDDEN);
			return;
		}
		data.setTermId(opTimeCfg.get().getTermId());
		long showTime = getCurShowTime(now, playerId);
		long startTime = getCurStartTime(now, playerId);
		long endTime = getCurEndTime(now, playerId);
		long hiddenTime = getCurHiddenTime(now, playerId);
		if (this.inMergeDate(showTime, hiddenTime, activityCfg)) {
			data.setCurState(ActivityState.HIDDEN);
			return;
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
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getShowTimeValue() + registerTime;
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getStartTimeValue() + registerTime;
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getEndTimeValue() + registerTime;
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		long registerTime = ActivityManager.getInstance().getDataGeter().getPlayerCreateAM0Date(playerId);
		Optional<IActivityTimeCfg> opCfg = getTimeCfgByTermId(termId);
		if (!opCfg.isPresent()) {
			return 0;
		}
		return opCfg.get().getHiddenTimeValue() + registerTime;
	}

}