package com.hawk.game.player;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.xid.HawkXID;

import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.world.robot.WORPlayer;
import com.hawk.game.world.service.WorldRobotService;

public class PlayerFactory {
	private static PlayerFactory playerFactory = new PlayerFactory();

	private PlayerFactory() {

	}

	public static PlayerFactory getInstance() {
		return playerFactory;
	}

	public Player newPlayer(HawkXID hawkXid) {
		String fromServerId = CrossService.getInstance().getImmigrationPlayerServerId(hawkXid.getUUID());
		if (hawkXid.getUUID().startsWith(WorldRobotService.ROBOT_PRE)) {
			return new WORPlayer(hawkXid);
		}
		
		if (!HawkOSOperator.isEmptyString(fromServerId)) {
			CsPlayer csPlayer = new CsPlayer(hawkXid);
			int crossType = RedisProxy.getInstance().getPlayerCrossType(fromServerId, hawkXid.getUUID());
			Player.logger.info("player load cross type:{}", crossType);
			if (crossType > 0) {
				csPlayer.setCrossType(crossType);
			} else {
				//一般跨服这里
				HawkLog.errPrintln("can not load cross type xid:{}", hawkXid);
				csPlayer.setCrossType(CrossType.CROSS_VALUE);
			}			
			
			return csPlayer;
		} else {
			return new Player(hawkXid);
		}
	}
}
