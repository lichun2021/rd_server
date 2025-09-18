package com.hawk.robot.action.march.type.impl;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.GuildManor.GuildManorBase;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.RobotLog;
import com.hawk.robot.action.march.March;
import com.hawk.robot.action.march.type.MarchType;
import com.hawk.robot.annotation.RobotMarch;
import com.hawk.robot.util.GuildUtil;
import com.hawk.robot.util.WorldUtil;

/**
 * 单人攻击联盟领地
 * @author zhenyu.shang
 * @since 2017年10月17日
 */
@RobotMarch(marchType = "ATTACK_MANOR")
public class AttackGuildManor implements March {

	@Override
	public void startMarch(GameRobotEntity robot) {
		GuildManorBase manor = GuildUtil.randomManor(robot);
		if(manor == null){
			return;
		}
		//发起行军
		//判断一下坐标,有可能没有及时刷新
		int x = manor.getX();
		int y = manor.getY();
		if(x == 0 || y == 0){
			robot.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_LIST_C_VALUE));
			return;
		}
		
		WorldMarchReq.Builder builder = WorldUtil.generatorMarchBuilder(robot, x, y, WorldMarchType.MANOR_SINGLE, true);
		if (builder == null) {
			return;
		}
		robot.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_MANOR_MARCH_C_VALUE, builder));
		RobotLog.guildPrintln("start march action manor, playerId: {}, marchType: {}, pos : {}", robot.getPlayerId(), WorldMarchType.MANOR_SINGLE, x + "," + y);
	}

	@Override
	public int getMarchType() {
		return MarchType.ATTACK_MANOR.intVal();
	}

}
