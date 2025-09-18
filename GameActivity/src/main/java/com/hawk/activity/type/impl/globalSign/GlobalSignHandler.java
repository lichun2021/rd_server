package com.hawk.activity.type.impl.globalSign;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.type.ActivityType;
import com.hawk.game.protocol.Activity.GlobalSignInPlayerJoinReq;
import com.hawk.game.protocol.Activity.GlobalSignSetBullectChatControlReq;
import com.hawk.game.protocol.HP;

public class GlobalSignHandler extends ActivityProtocolHandler {
	
	/***
	 * 查看界面
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GLOBAL_SIGN_INFO_REQ_VALUE)
	public void globalSignInfo(HawkProtocol protocol, String playerId){
		GlobalSignActivity activity = getActivity(ActivityType.GLOBAL_SIGN_ACTIVITY);
		activity.syncActivityDataInfo(playerId);
	}
	
	/***
	 * 签到
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GLOBAL_SIGN_PLAYER_SIGN_VALUE)
	public void greatGiftBuyBag(HawkProtocol protocol, String playerId){
		GlobalSignActivity activity = getActivity(ActivityType.GLOBAL_SIGN_ACTIVITY);
		
		GlobalSignInPlayerJoinReq req = protocol.parseProtocol(
				GlobalSignInPlayerJoinReq.getDefaultInstance());
		activity.playerSign(playerId,req.getChatId());
	}
	
	/**
	 * 设置弹幕开关
	 * @param protocol
	 * @param playerId
	 */
	@ProtocolHandler(code = HP.code.GLOBAL_SIGN_PLAYER_CHAT_CONTROL_REQ_VALUE)
	public void settingBulletChat(HawkProtocol protocol, String playerId){
		GlobalSignActivity activity = getActivity(ActivityType.GLOBAL_SIGN_ACTIVITY);
		
		GlobalSignSetBullectChatControlReq req = protocol.parseProtocol(
				GlobalSignSetBullectChatControlReq.getDefaultInstance());
		activity.settingBullectChatControl(playerId, req.getControl());
	}
	
	
}
