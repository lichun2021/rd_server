package com.hawk.game.tsssdk.invoker;

import org.hawk.net.protocol.HawkProtocol;

import com.hawk.game.config.ConstProperty;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.GuildManager.GetSearchGuildListResp;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.LogUtil;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.LogMsgType;

@Category(scene = GameMsgCategory.SEARCH_GUILD)
public class SearchGuildInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String guildId) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}

		GetSearchGuildListResp.Builder builder = GuildService.getInstance().onSearchGuild(name, ConstProperty.getInstance().getSearchPrecise() > 0);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_SEARCH_S, builder));
		LogUtil.logSecTalkFlow(player, null, LogMsgType.SEARCH_GUILD, "", name);
		LogUtil.logGuildAction(GuildAction.GUILD_SEARCH, null);
		
		return 0;
	}

}
