package com.hawk.activity.helper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRTException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.IActivityEntity;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ActivityPB.Builder;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.HPErrorCode;
import com.hawk.game.protocol.SysProtocol.HPOperateSuccess;
import com.hawk.gamelib.GameConst.MsgId;

public class PlayerPushHelper {
	
	private PlayerPushHelper() {}
	
	private static PlayerPushHelper instance;

	public static PlayerPushHelper getInstance() {
		if (instance == null) {
			instance = new PlayerPushHelper();
		}
		return instance;
	}

	public void pushToPlayer(String playerId, HawkProtocol msg) {
		ActivityManager.getInstance().getDataGeter().sendProtocol(playerId, msg);
	}
	
	/**
	 * 发送错误码协议并且中断当前的处理流程.
	 * 调用此方法的不要乱catch,否则后果自负.
	 */
	public void sendErrorAndBreak(String playerId, int hpCode, int errorCode) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(hpCode);
		builder.setErrCode(errorCode);
		builder.setErrFlag(0);
		pushToPlayer(playerId, HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
		
		throw new HawkRTException(String.format(" playerId:%s, hpCode:%d, errorCode:%d", playerId, hpCode, errorCode));
	}
	
	
	@Deprecated
	public void sendError(String playerId, int hpCode, int errCode) {
		sendError(playerId, hpCode, errCode, 0);
	}
	/**
	 * 通知错误码
	 *
	 * @param hpCode
	 * @param errCode
	 * @param errFlag 默认为0
	 */
	@Deprecated
	public void sendError(String playerId, int hpCode, int errCode, int errFlag) {
		HPErrorCode.Builder builder = HPErrorCode.newBuilder();
		builder.setHpCode(hpCode);
		builder.setErrCode(errCode);
		builder.setErrFlag(errFlag);
		pushToPlayer(playerId, HawkProtocol.valueOf(HP.sys.ERROR_CODE, builder));
	}
	
	/**
	 * 通用的操作成功回复协议
	 */
	public void responseSuccess(String playerId, int hpCode) {
		HPOperateSuccess.Builder builder = HPOperateSuccess.newBuilder().setHpCode(hpCode);
		pushToPlayer(playerId, HawkProtocol.valueOf(HP.sys.OPERATE_SUCCESS, builder));
	}
	
	/**
	 * 向所有在线玩家推送活动状态
	 * 
	 * @param activity
	 */
	public void pushActivityState(ActivityBase activity) {
		// 向所有在线玩家推送活动状态变更
		Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			activity.callBack(playerId, MsgId.SYNC_ACTIVITY_STATE_INFO, () -> {
				// 推送活动状态信息
				syncActivityStateInfo(playerId, activity);
			});
		}
		HawkLog.logPrintln("[activity] activity status change. activityId: {}, activityType: {}, status: {}", activity.getActivityId(), activity.getActivityType(),
				activity.getActivityEntity().getState());
	}
	
	/**
	 * 向所有在线玩家推送活动内容
	 * @param activity
	 */
	public void pushActivityDataInfo(ActivityBase activity) {
		// 向所有在线玩家推送活动内容数据
		Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
		int msgId = MsgId.SYNC_ACTIVITY_DATA_INFO * ActivityConst.MSGID_OFFSET + activity.getActivityId();
		for (String playerId : onlinePlayerIds) {
			if (activity.isShow(playerId)) {
				activity.callBack(playerId, msgId, () -> {
					activity.syncActivityDataInfo(playerId);
				});
			}
		}
	}
	
	/**
	 * 向所有在线玩家同步活动成就数据
	 * @param achieveProvider
	 */
	public void pushAchieveItemInfo(AchieveProvider achieveProvider){
		// 向所有在线玩家推送成就内容数据
		Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			if (!achieveProvider.isProviderNeedSync(playerId)) {
				continue;
			}
			List<AchieveItem> itemList = new ArrayList<>();
			Optional<AchieveItems> opAchieveItems = achieveProvider.getAchieveItems(playerId);
			if(!opAchieveItems.isPresent()){
				continue;
			}
			AchieveItems achieveItems = opAchieveItems.get();
			itemList.addAll(achieveItems.getItems());
			if(!itemList.isEmpty()){
				AchievePushHelper.pushAchieveInfo(playerId, itemList);
			}
		}
	}
	
	/**
	 * 向玩家推送单个活动信息
	 * @param player
	 */
	public void syncActivityStateInfo(String playerId, ActivityBase activity) {
		IActivityEntity activityEntity = activity.getIActivityEntity(playerId);
		Builder builder = buildActivityPB(playerId, activity, activityEntity);
		// 推送活动状态信息
		pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACTIVITY_INFO_VALUE, builder));
	}

	/**
	 * 创建单个活动协议数据
	 * @param player 
	 * @param activity 
	 * @param activityEntity
	 * @return
	 */
	public Builder buildActivityPB(String playerId, ActivityBase activity, IActivityEntity activityEntity) {
		ActivityCfg activityCfg = activity.getActivityCfg();
		int termId = activityEntity.getTermId();
		Builder builder = Activity.ActivityPB.newBuilder();
		builder.setActivityId(activityCfg.getActivityType());
		builder.setStage(activityEntity.getTermId());
		if(activity.isHidden(playerId)){
			builder.setState(ActivityState.HIDDEN.getState());
		}else{
			builder.setState(activityEntity.getActivityState().getState());
		}
		builder.setTimeType(activityCfg.getTimeType());
		ITimeController timeController =  activity.getTimeControl();
		builder.setShowTime(timeController.getShowTimeByTermId(termId, playerId));
		builder.setStartTime(timeController.getStartTimeByTermId(termId, playerId));
		builder.setEndTime(timeController.getEndTimeByTermId(termId, playerId));
		builder.setHiddenTime(timeController.getHiddenTimeByTermId(termId, playerId));
		builder.setEntranceType(activityCfg.getEntranceType());
		builder.setNewlyTime(activityEntity.getNewlyTime());
		return builder;
	}
	
	/**
	 * 推送活动隐藏消息
	 * @param activity
	 * @return
	 */
	public void pushActivityHidden(ActivityBase activity){
		Builder builder = buildActivityHiddenPB(activity);
		// 向所有在线玩家推送活动状态变更
		Collection<String> onlinePlayerIds = PlayerDataHelper.getInstance().getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			// 推送活动状态信息
			pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.PUSH_ACTIVITY_INFO_VALUE, builder));
		}
	}
	
	/**
	 * 构建活动隐藏协议
	 * @param activity
	 * @return
	 */
	public Builder buildActivityHiddenPB(ActivityBase activity){
		ActivityCfg activityCfg = activity.getActivityCfg();
		Builder builder = Activity.ActivityPB.newBuilder();
		builder.setActivityId(activityCfg.getActivityType());
		builder.setStage(0);
		builder.setState(ActivityState.HIDDEN.getState());
		builder.setTimeType(activityCfg.getTimeType());
		builder.setShowTime(Long.MAX_VALUE);
		builder.setStartTime(Long.MAX_VALUE);
		return builder;
	}
		
}
