package com.hawk.robot.response.guild;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILD_BASIC_INFO_SYNC_S_VALUE)
public class GuildBasicInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPGuildInfoSync guildInfoSync = protocol.parseProtocol(HPGuildInfoSync.getDefaultInstance());
		robotEntity.getGuildData().refreshGuildInfoSync(guildInfoSync);
	}

}
