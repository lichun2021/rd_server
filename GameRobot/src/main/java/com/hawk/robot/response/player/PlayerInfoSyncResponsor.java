package com.hawk.robot.response.player;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Player.HPPlayerInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

/**
 * 接收玩家信息
 * 
 * @author lating
 *
 */
@RobotResponse(code = HP.code.PLAYER_INFO_SYNC_S_VALUE)
public class PlayerInfoSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPPlayerInfoSync playerInfo = protocol.parseProtocol(HPPlayerInfoSync.getDefaultInstance());
		robotEntity.getBasicData().updatePlayerInfo(playerInfo.getPlayerInfo());
	}

}
