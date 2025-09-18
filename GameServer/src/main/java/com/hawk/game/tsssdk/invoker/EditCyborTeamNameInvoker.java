package com.hawk.game.tsssdk.invoker;

import com.hawk.game.invoker.CynorgEditNameRpcInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.EDIT_CYBOR_TEAM)
public class EditCyborTeamNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String teamId) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			player.rpcCall(MsgId.CYBOGR_EDIT_TEAM_NAME, CyborgWarService.getInstance(),
					new CynorgEditNameRpcInvoker(player, teamId, name, protocol));
		}

		return 0;
	}

}
