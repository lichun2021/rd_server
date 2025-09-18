package com.hawk.game.tsssdk.invoker;

import org.hawk.log.HawkLog;

import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.invoker.GuildChangeNameRpcInvoker;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.service.GuildService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.CHANGE_GUILD_NAME)
public class ChangeGuildNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String guildName, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			int changeNameGold = GuildConstProperty.getInstance().getChangeGuildNameGold();
			ConsumeItems consume = ConsumeItems.valueOf();
			consume.addConsumeInfo(PlayerAttr.GOLD, changeNameGold);
			if (!consume.checkConsume(player)) {
				HawkLog.errPrintln("player change guild name tsssdk invoker, playerId: {}", player.getId());
			}
			
			player.rpcCall(MsgId.CHANGE_GUILD_NAME, GuildService.getInstance(),
					new GuildChangeNameRpcInvoker(player, guildName, consume, protocol, Integer.parseInt(callback) > 0));
		}
		
		return 0;
	}

}
