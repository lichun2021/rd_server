package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;

public class RelationDeleteRpcInvoker extends HawkRpcInvoker {
	/**
	 * 操作者
	 */
	private Player player;
	/**
	 * 被操作者ID
	 */
	private String operatoredId;
	
	public RelationDeleteRpcInvoker(Player player, String operatoredId) {
		this.player = player;
		this.operatoredId = operatoredId;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int rlt = RelationService.getInstance().friendDelete(player.getId(), operatoredId);
		result.put("rlt", rlt);

		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int rlt = (Integer) result.get("rlt");
		if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.DELETE_FRIEND_REQ_VALUE);
		} else {
			player.sendError(HP.code.DELETE_FRIEND_REQ_VALUE, rlt, 0);
		}

		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getOperatoredId() {
		return operatoredId;
	}
	
}
