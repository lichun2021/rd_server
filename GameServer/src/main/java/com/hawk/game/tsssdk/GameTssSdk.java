package com.hawk.game.tsssdk;

import org.hawk.app.HawkApp;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.ChatMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomCreateMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomNameFilterFinishMsg;
import com.hawk.game.msg.ReportingInfoFilterFinishMsg;
import com.hawk.game.msg.SelfChatMsgFilterFinishMsg;
import com.hawk.game.msg.SendRedPacketMsg;
import com.hawk.game.msg.TsssdkInvokeMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SecuritySdk.HPAntiRecvDataInfo;
import com.hawk.tsssdk.manager.TssSdkManager;

import tsssdk.jni.TssSdk;
import tsssdk.jni.TssSdkAntiSendDataInfo;
import tsssdk.jni.TssSdkUicChatJudgeResultInfo;

/**
 * 腾讯安全SDK数据包发送前端实现
 * 
 * @author lating
 * @date 2022-02-16
 */
public class GameTssSdk extends TssSdk {
	
	@Override
	public int AntiSendDataToClient(TssSdkAntiSendDataInfo sendPkgInfo) {
		String playerId = null;
		if (sendPkgInfo.user_ext_data != null) {
			playerId = new String(sendPkgInfo.user_ext_data);
		}

		if (HawkOSOperator.isEmptyString(playerId)) {
			return -1;
		}

		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		if (player != null) {
			HPAntiRecvDataInfo.Builder builder = HPAntiRecvDataInfo.newBuilder();
			String account = new String(sendPkgInfo.account_info.account_id.account);
			builder.setOpenid(account);
			builder.setPlatid(sendPkgInfo.account_info.plat_id);
			ByteString antiData = ByteString.copyFrom(sendPkgInfo.anti_data);
			builder.setAntiData(antiData);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TSS_SDK_RECV_DATA_S, builder));
		}

		return 0;
	}
	
	@Override
	 public int UicOnChatJudgeResult(TssSdkUicChatJudgeResultInfo result_info) {
		TssSdkManager.getInstance().getMessageCount().decrementAndGet();
		String callBackData = new String(result_info.callback_data);
		try {
			JSONObject json = JSONObject.parseObject(callBackData);
			String playerId = json.getString("playerId");
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player == null) {
				return -1;
			}
			
			int msgFlag = result_info.msg_result_flag;
			String msg = result_info.msg;
			int msgId = json.getIntValue("msgId");
			String callbackData = json.getString("callbackData");
			
			switch (msgId) {
			case GameMsgCategory.CHATROOM_CHAT:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomMsgFilterFinishMsg.valueOf(msgFlag, msg, callbackData));
				break;
			case GameMsgCategory.WORLD_CHAT:
				HawkApp.getInstance().postMsg(player.getXid(), ChatMsgFilterFinishMsg.valueOf(msgFlag, msg, callbackData));
				break;
			case GameMsgCategory.CREATE_CHATROOM:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomCreateMsgFilterFinishMsg.valueOf(msg, callbackData));
				break;
			case GameMsgCategory.SELF_CHAT:
				HawkApp.getInstance().postMsg(player.getXid(), SelfChatMsgFilterFinishMsg.valueOf(msg, callbackData));
				break;
			case GameMsgCategory.REPORTING:
				HawkApp.getInstance().postMsg(player.getXid(), ReportingInfoFilterFinishMsg.valueOf(msg, callbackData));
				break;
			case GameMsgCategory.CHATROOM_NAME:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomNameFilterFinishMsg.valueOf(msgFlag, msg, callbackData));
				break;
			case GameMsgCategory.RED_PACKET:
				HawkApp.getInstance().postMsg(player.getXid(), SendRedPacketMsg.valueOf(msg, callbackData));
				break;
			default:
				HawkApp.getInstance().postMsg(player.getXid(), TsssdkInvokeMsg.valueOf(msgId, msgFlag, json.getIntValue("protocol"), msg, callbackData));
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("UicOnChatJudgeResult, msg: {}, callback data: {}", result_info.msg, callBackData);
		}
		
		return 0;
	 }
}
