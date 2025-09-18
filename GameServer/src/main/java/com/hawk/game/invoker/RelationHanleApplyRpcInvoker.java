package com.hawk.game.invoker;

import java.util.List;
import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Friend.OperationType;
import com.hawk.game.service.RelationService;

public class RelationHanleApplyRpcInvoker extends HawkRpcInvoker {
	private Player player;
	private List<String> playerIds;
	private OperationType type;
	
	public RelationHanleApplyRpcInvoker(Player player, List<String> playerIds, OperationType type) {
		this.player = player;
		this.playerIds = playerIds;
		this.type = type;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int rlt = RelationService.getInstance().handleApply(player.getId(), type, playerIds);
		result.put("rlt", rlt);

		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int rlt = (Integer) result.get("rlt");
		if (rlt != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(HP.code.HANDLE_FRIEND_APPLY_REQ_VALUE, rlt, 0);
		} else {
			player.responseSuccess(HP.code.HANDLE_FRIEND_APPLY_REQ_VALUE);
		}

		player.getPush().syncFriendBuildStatus();
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public List<String> getPlayerIds() {
		return playerIds;
	}

	public void setPlayerIds(List<String> playerIds) {
		this.playerIds = playerIds;
	}

	public OperationType getType() {
		return type;
	}
	
}
