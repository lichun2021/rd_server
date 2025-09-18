package com.hawk.game.tsssdk.invoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.util.GuildUtil;

@Category(scene = GameMsgCategory.CHECK_GUILD_NAME)
public class CheckGuildNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String guildName, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		int operationResult = GuildUtil.checkGuildName(guildName);
		if (operationResult != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(protocol, operationResult, 0);
			return 0;
		}

		player.responseSuccess(protocol);
		
		return 0;
	}

}
