package com.hawk.game.tsssdk.invoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;

@Category(scene = GameMsgCategory.MECHACORE_SUIT_CHANGE_NAME)
public class MechaCoreSuitChangeNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result == 0) {
			int suitType = Integer.valueOf(callback);
			player.getPlayerMechaCore().changeSuitName(suitType, name);
		} else {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		}
		return 0;
	}

}
