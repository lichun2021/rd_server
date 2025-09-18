package com.hawk.robot.action.chat;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.annotation.RobotAction;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.robot.HawkRobotAction;
import org.hawk.robot.HawkRobotEntity;

import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Chat.HPChatStateChangeReq;
import com.hawk.game.protocol.Chat.HPSendChat;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotApp;
import com.hawk.robot.GameRobotEntity;

/**
 * 
 * 聊天行为
 * 
 * @author Jesse
 *
 */
@RobotAction(valid = false)
public class PlayerChatAction extends HawkRobotAction {
	private static Random random = new Random();
	private static AtomicInteger msgIndex = new AtomicInteger(0);
	HPChatState chatstate;

	@Override
	public void doAction(HawkRobotEntity robotEntity) {
		GameRobotEntity gameRobotEntity = (GameRobotEntity) robotEntity;
		int percent = GameRobotApp.getInstance().getConfig().getInt("robot.chatProbability");
		// 按概率发送聊天信息
		if (HawkRand.randPercentRate(percent)) {
			sendMsg(gameRobotEntity);
		}

		// TODO 联盟聊天信息发送
	}

	/**
	 * 随机发送世界/联盟聊天信息
	 * 
	 * @param robotEntity
	 */
	private synchronized void sendMsg(GameRobotEntity robotEntity) {
		HPChatState oldState = chatstate;
		chatstate = random.nextInt(100) < 10 ? HPChatState.CHAT : HPChatState.NOT_CHAT;
		if (oldState != chatstate) {
			// 切换状态
			robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MSG_STATE_C_VALUE, HPChatStateChangeReq.newBuilder().setState(chatstate)));
		}
		if (chatstate != HPChatState.CHAT) {
			return;
		}

		String guildId = robotEntity.getGuildId();
		HPSendChat.Builder builder = HPSendChat.newBuilder();
		builder.setChatMsg(
				msgIndex.incrementAndGet() + ": " + HawkOSOperator.randomString(1 + random.nextInt(15), "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
		// 无联盟则创建联盟
		if (HawkOSOperator.isEmptyString(guildId)) {
			builder.setChatType(0);
		} else {
			builder.setChatType(random.nextInt(1));
		}
		robotEntity.sendProtocol(HawkProtocol.valueOf(HP.code.SEND_CHAT_C_VALUE, builder));

	}

}
