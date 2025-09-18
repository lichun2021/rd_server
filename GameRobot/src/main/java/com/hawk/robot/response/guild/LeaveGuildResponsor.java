package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 * 退出公会
 * @author zhenyu.shang
 * @since 2017年8月9日
 */
@RobotResponse(code = HP.code.PLAYER_LEAVE_GUILD_VALUE)
public class LeaveGuildResponsor extends RobotResponsor {

	
	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		robotEntity.getGuildData().clearGuildData();
	}
}
