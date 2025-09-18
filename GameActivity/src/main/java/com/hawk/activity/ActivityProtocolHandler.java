package com.hawk.activity;

import org.hawk.net.protocol.HawkProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;

public class ActivityProtocolHandler {
	
	protected static final Logger logger = LoggerFactory.getLogger("Server");

	public ActivityProtocolHandler() {
	}

	@SuppressWarnings("unchecked")
	protected <T> T getActivity(ActivityType activityType) {
		for (ActivityBase activity : ActivityManager.getInstance().activityMap.values()) {
			if (activityType.intValue() == activity.getActivityCfg().getActivityType()) {
				return (T) activity;
			}
		}
		return null;
	}
	
	/**
	 * 通知错误码
	 *
	 * @param hpCode
	 * @param errCode
	 * @param errFlag
	 */
	@Deprecated
	public void sendError(String playerId, int hpCode, int errCode) {
		PlayerPushHelper.getInstance().sendError(playerId, hpCode, errCode);
	}
	
	
	public void sendErrorAndBreak(String playerId, int hpCode, int errCode) {
		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hpCode, errCode);
	}
	
	/**
	 * 通用的操作成功回复协议
	 */
	public void responseSuccess(String playerId, int hpCode) {
		PlayerPushHelper.getInstance().responseSuccess(playerId, hpCode);
	}
	
	/**
	 * 发送协议
	 * @param playerId
	 * @param msg
	 */
	public void sendProtocol(String playerId, HawkProtocol msg){
		PlayerPushHelper.getInstance().pushToPlayer(playerId, msg);
	}
}
