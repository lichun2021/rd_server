package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Friend.OperationType;
import com.hawk.game.service.RelationService;

public class GuardInviteHandleInvoker extends HawkMsgInvoker {
	Player player;
	String reqPlayerId;
	OperationType oper;
	public GuardInviteHandleInvoker(Player player, String reqPlayerId, OperationType oper) {
		this.player = player;
		this.reqPlayerId = reqPlayerId;
		this.oper = oper;
	} 
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		int code = RelationService.getInstance().onGuardInvieteHandle(player.getId(), reqPlayerId, oper);
		if (code != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(HP.code.GUARD_INVITE_HANDLE_REQ_VALUE, code, 0);
		}
		
		return true;
	}

}
