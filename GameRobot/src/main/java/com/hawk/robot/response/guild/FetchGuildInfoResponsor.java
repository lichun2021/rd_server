package com.hawk.robot.response.guild;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILDMANAGER_GETINFO_S_VALUE)
public class FetchGuildInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GetGuildInfoResp guildInfo = protocol.parseProtocol(GetGuildInfoResp.getDefaultInstance());
		robotEntity.getGuildData().refreshGuildInfo(guildInfo);
	}

}
