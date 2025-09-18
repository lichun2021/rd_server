package com.hawk.game.tsssdk.invoker;

import java.util.ArrayList;
import java.util.List;
import com.hawk.game.invoker.RelationAddReqRpcInvoker;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;
import com.hawk.game.tsssdk.Category;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.gamelib.GameConst;

@Category(scene = GameMsgCategory.ADD_FRIEND)
public class FriendAddInvoker implements TsssdkInvoker {

	@Override
	public int invoke(Player player, int result, String content, int protocol, String callback) {
		if (result != 0) {
			player.sendError(protocol, Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE, 0);
			return 0;
		}
		
		String[] ids = callback.split(",");
		List<String> newTargetIds = new ArrayList<>();
		for (String remainId : ids) {
			newTargetIds.add(remainId);
		}
		player.rpcCall(GameConst.MsgId.FRIEND_ADD_REQ, RelationService.getInstance(), new RelationAddReqRpcInvoker(player, newTargetIds, content));
		
		return 0;
	}

}
