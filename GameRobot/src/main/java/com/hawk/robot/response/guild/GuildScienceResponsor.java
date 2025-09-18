package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.GuildScience.GetGuildScienceInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILD_SCIENCE_GET_INFO_S_VALUE)
public class GuildScienceResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GetGuildScienceInfoResp resp = protocol.parseProtocol(GetGuildScienceInfoResp.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			WorldDataManager.getInstance().refreshGuildScienceInfo(guildId, resp.getScienceInfoList());
		}
	}

}
