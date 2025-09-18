package com.hawk.robot.response.guild;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.robot.GameRobotEntity;
import com.hawk.robot.WorldDataManager;
import com.hawk.robot.annotation.RobotResponse;
import com.hawk.robot.response.RobotResponsor;

@RobotResponse(code = { HP.code.GUILD_GET_SHOP_INFO_S_VALUE, HP.code.GUILD_SHOP_BUY_S_VALUE, HP.code.GUILD_ADD_SHOP_ITEM_S_VALUE})
public class GuildShopInfoResponsor extends RobotResponsor {

	@Override
	public void response(GameRobotEntity robotEntity, HawkProtocol protocol) {
		HPGetGuildShopInfoResp guildShopInfo = protocol.parseProtocol(HPGetGuildShopInfoResp.getDefaultInstance());
		String guildId = robotEntity.getGuildId();
		if (!HawkOSOperator.isEmptyString(guildId)) {
			WorldDataManager.getInstance().refreshGuildShopInfo(guildId, guildShopInfo);
		}
	}

}
