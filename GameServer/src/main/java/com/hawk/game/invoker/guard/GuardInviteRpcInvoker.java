package com.hawk.game.invoker.guard;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.item.ConsumeItems;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;
import com.hawk.log.Action;

public class GuardInviteRpcInvoker extends HawkRpcInvoker {
	Player player;
	String targetPlayerId;
	ConsumeItems items;
	
	public GuardInviteRpcInvoker(Player player, String targetPlayerId, ConsumeItems items) {
		this.player = player;
		this.targetPlayerId = targetPlayerId;
		this.items = items;
	}
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int rlt = RelationService.getInstance().onGuardInvitePlayer(player, targetPlayerId);
		result.put("rlt", rlt);

		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int rlt = (Integer) result.get("rlt");
		if (rlt != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(HP.code.GUARD_INVITE_PLAYER_REQ_VALUE, rlt, 0);
		} else {			
			player.responseSuccess(HP.code.GUARD_INVITE_PLAYER_REQ_VALUE);
			items.consumeAndPush(player, Action.GUARD_INVITE);
		}
		
		return true;
	}
}
