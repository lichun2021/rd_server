package com.hawk.game.tsssdk.history;

import org.hawk.app.HawkApp;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.google.protobuf.ByteString;
import com.hawk.game.global.GlobalData;
import com.hawk.game.msg.ChatMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomCreateMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomMsgFilterFinishMsg;
import com.hawk.game.msg.ChatRoomNameFilterFinishMsg;
import com.hawk.game.msg.ReportingInfoFilterFinishMsg;
import com.hawk.game.msg.SelfChatMsgFilterFinishMsg;
import com.hawk.game.msg.SendRedPacketMsg;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SecuritySdk.HPAntiRecvDataInfo;
import com.hawk.game.util.GsConst.UicMsgResultFlag;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.tsssdk.manager.history.TssSdkManager;

import tsssdk.jni.history.TssSdk;
import tsssdk.jni.history.TssSdkAntiSendDataInfoV3;
import tsssdk.jni.history.TssSdkUicChatJudgeResultInfoV2;

/**
 * 腾讯安全SDK数据包发送前端实现
 * 
 * @author Nannan.Gao
 * @date 2017-4-10 15:46:10
 */
public class GameTssSdk extends TssSdk {
	
	@Override
	public int AntiSendDataToClientV3(TssSdkAntiSendDataInfoV3 sendPkgInfo) {
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
			builder.setOpenid(sendPkgInfo.openid);
			builder.setPlatid(sendPkgInfo.platid);
			ByteString antiData = ByteString.copyFrom(sendPkgInfo.anti_data);
			builder.setAntiData(antiData);
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TSS_SDK_RECV_DATA_S, builder));
		}

		return 0;
	}
	
	@Override
	 public int UicOnChatJudgeResultV2(TssSdkUicChatJudgeResultInfoV2 result_info) {
		TssSdkManager.getInstance().getMessageCount().decrementAndGet();
		
		int msgFlag = result_info.msg_result_flag;
		if (msgFlag == UicMsgResultFlag.ILLEGAL) {
			return -1;
		}
		
		String callBackData = new String(result_info.callback_data);
		String[] callbackStrs = callBackData.split("\\|");
		String playerId = callbackStrs[0];
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			return -1;
		}
		
		String msg = result_info.msg;
		int msgId = Integer.valueOf(callbackStrs[1]);
		
		switch (msgId) {
			case MsgId.CHATROOM_MSG_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomMsgFilterFinishMsg.valueOf(msgFlag, msg, callbackStrs[2]));
				break;
			case MsgId.CHAT_MSG_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), ChatMsgFilterFinishMsg.valueOf(msgFlag, msg, callbackStrs[2]));
				break;
			case MsgId.CREATE_CHATROOM_MSG_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomCreateMsgFilterFinishMsg.valueOf(msg, callbackStrs[2]));
				break;
			case MsgId.SELF_CHAT_MSG_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), SelfChatMsgFilterFinishMsg.valueOf(msg, callbackStrs[2]));
				break;
			case MsgId.REPORTING_INFO_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), ReportingInfoFilterFinishMsg.valueOf(msg, callbackStrs[2]));
				break;
			case MsgId.CHATROOM_NAME_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), ChatRoomNameFilterFinishMsg.valueOf(msgFlag, msg, callbackStrs[2]));
				break;
			case MsgId.RED_PACKET_MSG_FILTER:
				HawkApp.getInstance().postMsg(player.getXid(), SendRedPacketMsg.valueOf(msg, callbackStrs[2]));
				break;
			default:
				break;
		}
		
		return 0;
	 }
}
