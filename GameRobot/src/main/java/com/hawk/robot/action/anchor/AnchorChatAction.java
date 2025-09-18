package com.hawk.robot.action.anchor;

import java.util.Date;

import org.hawk.annotation.RobotAction;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Anchor.AnchorServerInfoGS;
import com.hawk.game.protocol.Anchor.LoginAnchorServerAS;
import com.hawk.game.protocol.Anchor.RoleType;
import com.hawk.game.protocol.Anchor.RoomInfo;
import com.hawk.game.protocol.Anchor.SendChatMessageInfoAS;
import com.hawk.game.protocol.SysProtocol.HPHeartBeat;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.data.AnchorData.AnchorState;

/**
 *
 * @author zhenyu.shang
 * @since 2018年4月4日
 */
@RobotAction(valid = true)
public class AnchorChatAction extends HawkRobotAction {

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		AnchorState state = gameRobotEntity.getAnchorData().getState();
		switch (state) {
		case OFFLINE:
			//请求服务器获取连接信息
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.ENTER_ANCHOR_SERVER_C_VALUE));
			gameRobotEntity.getAnchorData().setState(AnchorState.GETINGSERVER);
			HawkLog.logPrintln("send get anchor info msg !!!");
			break;
		case GETINGSERVER:
			AnchorServerInfoGS info = gameRobotEntity.getAnchorData().getServerInfo();
			if(info != null){
				gameRobotEntity.initAnchorServer(info.getServerip(), info.getServerport());
				gameRobotEntity.getAnchorData().setState(AnchorState.CONNECT);
				HawkLog.logPrintln("init connect anchor server ip:{}, port:{}", info.getServerip(), info.getServerport());
			}
			break;
		case CONNECT:
			if(gameRobotEntity.isAnchorSessionConnect()){
				AnchorServerInfoGS serverinfo = gameRobotEntity.getAnchorData().getServerInfo();
				LoginAnchorServerAS.Builder builder = LoginAnchorServerAS.newBuilder();
				builder.setToken(serverinfo.getToken());
				builder.setRoomID(String.valueOf(serverinfo.getRoomID()));
				builder.setRoleType(RoleType.PLAYER);
				gameRobotEntity.sendAnchorProtocol(HawkProtocol.valueOf(HP.code.LOGIN_ANCHORSERVER_C_VALUE, builder));
				gameRobotEntity.getAnchorData().setState(AnchorState.LOGINING);
				HawkLog.logPrintln("send login anchor server token:{}, roomId:{}", serverinfo.getToken(), serverinfo.getRoomID());
			}
			break;
		case LOGINING:
			RoomInfo roomInfo = gameRobotEntity.getAnchorData().getRoomInfo();
			if(roomInfo != null){
				gameRobotEntity.getAnchorData().setState(AnchorState.ONLINE);
				HawkLog.logPrintln("success enter room ,roomID : {}, viewer num is {}", roomInfo.getRoomID(), roomInfo.getViewerNum());
			}
			break;
		case ONLINE:
			if(HawkRand.randPercentRate(3)){
				gameRobotEntity.getAnchorSession().close();
				gameRobotEntity.getAnchorData().clearOffline();
				HawkLog.logPrintln("offline the anchor server , robot : {}", gameRobotEntity.getName());
			} else {
				//进入房间后，直接发送聊天消息
				String content = getRandomChatContent(gameRobotEntity);
				SendChatMessageInfoAS.Builder builder = SendChatMessageInfoAS.newBuilder();
				builder.setChatContent(content);
				gameRobotEntity.sendAnchorProtocol(HawkProtocol.valueOf(HP.code.SEND_ANCHOR_MESSAGE_C_VALUE, builder));
				//发送心跳
				HPHeartBeat.Builder heartBuilder = HPHeartBeat.newBuilder();
				heartBuilder.setTimeStamp(System.currentTimeMillis());
				gameRobotEntity.sendAnchorProtocol(HawkProtocol.valueOf(HP.sys.HEART_BEAT_VALUE, heartBuilder));
			}
			break;
		default:
			break;
		}
	}

	private String getRandomChatContent(GameRobotEntity gameRobotEntity){
		String chatContent = "";
		int rnd = HawkRand.randInt(0, 100);
		if(rnd < 10){
			chatContent = "大家好，我是" + gameRobotEntity.getName() + ", 初次见面请多关照";
		} else if(rnd < 20){
			chatContent = "现在是北京时间：" + new Date();
		} else if(rnd < 30){
			chatContent = "主播好厉害";
		} else if(rnd < 40){
			chatContent = "[]~(￣▽￣)~*o(￣▽￣)db(￣▽￣)d";
		} else if(rnd < 50){
			chatContent = "没有意思不看了~~~~~！！！";
		} else if(rnd < 60){
			chatContent = "我说啥我自己都看不懂";
		} else if(rnd < 70){
			chatContent = "我为什么要分这么多类型呢~！";
		} else if(rnd < 80){
			chatContent = "马上就完了，还差一个";
		} else if(rnd < 90){
			chatContent = "最后一个";
		} else {
			chatContent = "- -！！！！！";
		}
		return chatContent;
	}
}
