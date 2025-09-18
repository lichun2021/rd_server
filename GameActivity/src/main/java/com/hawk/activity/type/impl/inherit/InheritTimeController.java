package com.hawk.activity.type.impl.inherit;

import java.util.Collection;
import java.util.Date;
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
import com.hawk.activity.entity.ActivityPlayerEntity;
import com.hawk.activity.entity.ActivityTermStatus;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.inherit.cfg.InheritActivityTimeCfg;
import com.hawk.activity.type.impl.inherit.cfg.InheritKVCfg;
import com.hawk.gamelib.GameConst.MsgId;

public class InheritTimeController extends ITimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return InheritActivityTimeCfg.class;
	}
	
	/**
	 * 是否基于玩家开启的活动(例: 根据玩家注册时间开启)
	 * 
	 * @return
	 */
	public boolean isPlayerActivityTimeController() {
		return true;
	}

	@Override
	public long getServerDelay() {
		InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}
	
	
	/**
	 * 获取当前活动期数
	 * 
	 * @param now
	 * @param activityCfg
	 * @return
	 */
	public int getActivityTermId(long now, String playerId) {
		BackPlayerInfo backInfo = ActivityManager.getInstance().getDataGeter().getBackPlayerInfoById(playerId);
		if (backInfo == null) {
			return 0;
		} else {
			int currTermId = getCurrRealTermId();
			// 当前传承活动开启,且实际期数大于记录期数,则本期不进行开放
			if (currTermId != 0 && currTermId > backInfo.getTermId()) {
				return 0;
			}
			// 回归新角色
			if (playerId.equals(backInfo.getCurNewPlayer())) {
				return backInfo.getTermId();
			}
			// 活动开启,且为回归老帐号/被传承者,活动期间内显示
			if (currTermId != 0 || (playerId.equals(backInfo.getCurOldPlayerId()) || playerId.equals(backInfo.getCurrInheritPlayerId()))) {
				return currTermId;
			}
			return 0;
		}
	}
	
	/**
	 * 根据当前时间获取实际承接活动的期数
	 * @return
	 */
	public int getCurrRealTermId(){
		ConfigIterator<InheritActivityTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(InheritActivityTimeCfg.class);
		long now = HawkTime.getMillisecond();
		int termId = 0;
		for(InheritActivityTimeCfg cfg : its){
			if(now > cfg.getStartTimeValue() && now < cfg.getEndTimeValue()){
				termId = cfg.getTermId();
			}
		}
		return termId;
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
		BackPlayerInfo backInfo = ActivityManager.getInstance().getDataGeter().getBackPlayerInfoById(playerId);
		long showtime = 0;
		int currTermId = getCurrRealTermId();
		if (backInfo == null) {
			showtime = 0;
		} else {
			// 新角色回归玩家
			if (playerId.equals(backInfo.getCurNewPlayer())) {
				showtime = HawkTime.getAM0Date(new Date(backInfo.getRegistTime())).getTime();
			}
			// 承接源
			else if (playerId.equals(backInfo.getCurrInheritPlayerId())) {
				if (currTermId != 0 && currTermId == backInfo.getTermId()) {
					showtime = HawkTime.getAM0Date(new Date(backInfo.getInheritTime())).getTime();
				}
			}
			// 老服回归玩家
			else if (playerId.equals(backInfo.getCurOldPlayerId())) {
				if (currTermId != 0 && currTermId == backInfo.getTermId()) {
					showtime = HawkTime.getAM0Date(new Date(backInfo.getBackTime())).getTime();
				}
			}
		}
		return showtime;
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
		return getCurShowTime(now, playerId);
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
		InheritKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(InheritKVCfg.class);
		BackPlayerInfo backInfo = ActivityManager.getInstance().getDataGeter().getBackPlayerInfoById(playerId);
		long endTime = 0;
		int currTermId = getCurrRealTermId();
		if (backInfo == null) {
			endTime = 0;
		} else {
			// 新角色回归玩家
			if (playerId.equals(backInfo.getCurNewPlayer())) {
				endTime = HawkTime.getAM0Date(new Date(backInfo.getRegistTime())).getTime() + cfg.getLastTime();
			}
			// 承接源
			else if (playerId.equals(backInfo.getCurrInheritPlayerId()) || playerId.equals(backInfo.getCurOldPlayerId())) {
				if (currTermId != 0 && currTermId == backInfo.getTermId()) {
					Optional<IActivityTimeCfg> opTimeCfg = getTimeCfg(now);
					if (opTimeCfg.isPresent()) {
						endTime = opTimeCfg.get().getEndTimeValue();
					}
				}
			}
		}
		return endTime;
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
		return getCurEndTime(now, playerId);
	}

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
			getActivityData(data, playerId);
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
			return ActivityState.HIDDEN;
		}
	}
	
	public void getActivityData(ActivityTermStatus data, String playerId){
		long now = HawkTime.getMillisecond();
		BackPlayerInfo backInfo = ActivityManager.getInstance().getDataGeter().getBackPlayerInfoById(playerId);
		if (backInfo == null) {
			data.setCurState(ActivityState.HIDDEN);
			return;
		}
		data.setTermId(backInfo.getTermId());
		long showTime = getCurShowTime(now, playerId);
		long startTime = getCurStartTime(now, playerId);
		long endTime = getCurEndTime(now, playerId);
		long hiddenTime = getCurHiddenTime(now, playerId);

		if (now >= showTime && now < startTime) {
			data.setCurState(ActivityState.SHOW);
		} else if (now >= startTime && now < endTime) {
			data.setCurState(ActivityState.OPEN);
		} else if (now >= endTime && now < hiddenTime) {
			data.setCurState(ActivityState.END);
		} else {
			data.setCurState(ActivityState.HIDDEN);
		}
	}


	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		return getCurShowTime(HawkTime.getMillisecond(), playerId);
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		return getCurStartTime(HawkTime.getMillisecond(), playerId);
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		return getCurEndTime(HawkTime.getMillisecond(), playerId);
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getCurEndTime(HawkTime.getMillisecond(), playerId);
	}

}
