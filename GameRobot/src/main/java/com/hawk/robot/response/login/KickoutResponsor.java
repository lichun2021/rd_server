package com.hawk.robot.response.login;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Login.HPPlayerKickout;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 被踢出通知
 * 
 * @author
 *
 */
@RobotResponse(code = HP.code.PLAYER_KICKOUT_S_VALUE)
public class KickoutResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPPlayerKickout kickout = (HPPlayerKickout) protocol.parseProtocol(HPPlayerKickout.getDefaultInstance());
		HawkLog.logPrintln("robot is kicked out, puid: {}, playerId: {}, reason: {}, msg: {}", 
				robotEntity.getPuid(), robotEntity.getPlayerId(), kickout.getReason(), kickout.hasMsg() ? kickout.getMsg() : "");
		robotEntity.doLogout();
	}

}
