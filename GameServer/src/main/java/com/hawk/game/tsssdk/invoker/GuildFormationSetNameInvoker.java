package com.hawk.game.tsssdk.invoker;

import com.hawk.game.module.GuildFormationModule;
import com.hawk.game.player.Player;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GsConst;

@Category(scene = GameMsgCategory.GUILD_FORMATION)
public class GuildFormationSetNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		GuildFormationModule module = player.getModule(GsConst.ModuleType.GUILD_FORMATION);
		module.setNameInvoker(result == 0, Integer.parseInt(callback), name);
		return 0;
	}

}
