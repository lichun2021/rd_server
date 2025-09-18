package com.hawk.game.tsssdk.invoker;

import com.hawk.game.invoker.GuildPostAnnouncementInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.POST_GUILD_ANNOUNCE)
public class PostGuildAnnounceInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String announcement, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.Error.GUILD_ANNOUNCEMENT_ILLEGAL, 0);
			return 0;
		}
		
		int checkResult = Integer.parseInt(callback);
		GuildService.getInstance().dealMsg(MsgId.GUILD_OPEN_ANNOUNCEMENT,
				new GuildPostAnnouncementInvoker(player, announcement, protocol, checkResult > 0));
		
		return 0;
	}

}
