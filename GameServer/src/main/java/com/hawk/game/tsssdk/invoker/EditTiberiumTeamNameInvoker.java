package com.hawk.game.tsssdk.invoker;

import com.hawk.game.invoker.tiberium.TWEditTeamNameInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst.MsgId;

@Category(scene = GameMsgCategory.EDIT_TIBERIUM_TEAM)
public class EditTiberiumTeamNameInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String name, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
		} else {
			int teamIndex = Integer.parseInt(callback);
			player.msgCall(MsgId.TIBERIUM_EDIT_TEAM_NAME, TiberiumWarService.getInstance(),
					new TWEditTeamNameInvoker(player, teamIndex, name, protocol));
		}
		
		return 0;
	}

}
