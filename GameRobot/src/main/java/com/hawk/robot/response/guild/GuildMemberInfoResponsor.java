package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

/**
 *
 * @author zhenyu.shang
 * @since 2017年8月9日
 */
@RobotResponse(code = HP.code.GUILDMANAGER_GETMEMBERINFO_S_VALUE)
public class GuildMemberInfoResponsor extends RobotResponsor{

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		GetGuildMemeberInfoResp guildMemInfo = protocol.parseProtocol(GetGuildMemeberInfoResp.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if(guildId != null){
			WorldDataManager.getInstance().refreshGuildMemberList(guildId, guildMemInfo);
		}
	}

}
