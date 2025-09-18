package com.hawk.game.invoker;

import java.util.List;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.guild.manor.building.GuildManorWarehouse;
import com.hawk.game.protocol.Const.TerritoryType;
import com.hawk.game.service.GuildManorService;
import com.hawk.game.service.GuildService;

public class BackGuildStorageMsgInvoker extends HawkMsgInvoker {

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			GuildManorWarehouse wareHouse = GuildManorService.getInstance().getBuildingByTypeAndIdx(guildId, 1, TerritoryType.GUILD_STOREHOUSE);
			if (wareHouse != null) {
				GuildManorService.getInstance().removeManorBuilding(wareHouse);
			}
		}
		
		return true;
	}

}
