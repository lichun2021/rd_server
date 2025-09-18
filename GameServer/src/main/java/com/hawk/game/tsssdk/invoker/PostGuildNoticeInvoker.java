package com.hawk.game.tsssdk.invoker;

import com.hawk.game.invoker.GuildPostNoticeInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.POST_GUILD_NOTICE)
public class PostGuildNoticeInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String notice, int protocol, String guildId) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.GUILD_NOTICE_ILLEGAL, 0);
			return 0;
		}
		
		GuildService.getInstance().dealMsg(MsgId.GUILD_NOTICE,
				new GuildPostNoticeInvoker(player, notice, protocol));
		
		return 0;
	}

}
