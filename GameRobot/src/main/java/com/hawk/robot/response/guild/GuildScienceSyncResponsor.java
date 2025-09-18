package com.hawk.robot.response.guild;

import com.hawk.robot.annotation.RobotResponse;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.GuildScience.GuildScienceInfoSync;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILD_SCIENCE_INFO_SYNC_S_VALUE)
public class GuildScienceSyncResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GuildScienceInfoSync resp = protocol.parseProtocol(GuildScienceInfoSync.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			WorldDataManager.getInstance().refreshGuildScienceInfo(guildId, resp.getScienceInfoList());
		}
	}

}
