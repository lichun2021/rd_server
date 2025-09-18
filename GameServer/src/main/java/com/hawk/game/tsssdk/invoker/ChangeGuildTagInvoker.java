package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.invoker.GuildChangeTagRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CHANGE_GUILD_TAG)
public class ChangeGuildTagInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String guildTag, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(player.getGuildId());
		
		int changeTagGold = 0;
		if (guild.isHasChangeTag()) {
			changeTagGold = GuildConstProperty.getInstance().getChangeGuildTagGold();
		}
		
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(PlayerAttr.GOLD, changeTagGold);
		if (!consume.checkConsume(player)) {
			HawkLog.errPrintln("player change guild tag tsssdk invoker, playerId: {}", player.getId());
		}
		player.rpcCall(MsgId.CHANGE_GUILD_TAG, GuildService.getInstance(),
				new GuildChangeTagRpcInvoker(player, guildTag, consume, protocol));
		
		return 0;
	}

}
