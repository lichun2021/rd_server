package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILDMANAGER_SEARCH_S_VALUE)
public class GuildSearchResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
//		GetSearchGuildListResp guildListResp = protocol.parseProtocol(GetSearchGuildListResp.getDefaultInstance());
//		PublicDataManager.getInstance().refreshGuildList(guildListResp);
	}

}
