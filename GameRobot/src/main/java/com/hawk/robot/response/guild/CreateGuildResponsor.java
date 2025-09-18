package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.GuildManager.CreateGuildResp;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = HP.code.GUILDMANAGER_CREATE_S_VALUE)
public class CreateGuildResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		CreateGuildResp createResp = protocol.parseProtocol(CreateGuildResp.getDefaultInstance());
		robotEntity.getGuildData().refreshGuildInfo(createResp.getInfo());
		//把联盟名称加入到列表中
		WorldDataManager.getInstance().addGuildId(createResp.getInfo().getId());
	}

}
