package com.hawk.robot.action.march.type.impl;

import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.WorldUtil;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchPB;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;

/**
 * 机器人行军 - 参与集结攻击玩家
 * @author golden
 *
 */
@RobotMarch(marchType = "MASS_JOIN")
public class MassJoin implements March {

	@Override
	public void startMarch(GameRobotEntity robot) {
		String guildId = robot.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		
		WorldMarchPB massMarch = WorldDataManager.getInstance().getMassMarch(guildId);
		if(massMarch == null) {
			RobotLog.worldErrPrintln("start march failed, mass join march need mass march, playerId: {}", robot.getPlayerId());
			return;
		}
		
		if (massMarch.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING) {
			RobotLog.worldErrPrintln("start march failed, mass march status not waiting, playerId: {}", robot.getPlayerId());
			return;
		}
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, massMarch.getOrigionX(), massMarch.getOrigionY(), null, true);
		if (builder == null) {
			return;
		}
		builder.setMarchId(massMarch.getMarchId());
		builder.setType(WorldMarchType.MASS_JOIN);
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MASS_JOIN_C_VALUE, builder));
		RobotLog.worldPrintln("start march action, playerId: {}, marchType: {}", robot.getPlayerId(), MarchType.MASS_JOIN.name());
	}

	@Override
	public int getMarchType() {
		return MarchType.MASS_JOIN.intVal();
	}

}
