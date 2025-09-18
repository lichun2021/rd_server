package com.hawk.game.invoker.guard;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.RelationService;

public class GuardDressUpdateInvoker extends HawkMsgInvoker {
	
	Player player;
	int dressId;
	private boolean fromItem;
	public GuardDressUpdateInvoker(Player player, int dressId) {
		this(player, dressId, false);
	}
	public GuardDressUpdateInvoker(Player player, int dressId, boolean fromItem) {
		this.player = player;
		this.dressId = dressId;
		this.fromItem = fromItem;
	}
	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		int result = RelationService.getInstance().onGuardDressUpdate(player.getId(), dressId);
		if (!fromItem) {
			if (result == Status.SysError.SUCCESS_OK_VALUE) {
				player.responseSuccess(HP.code.GUARD_DRESS_UPDATE_REQ_VALUE);
			} else {
				player.sendError(HP.code.GUARD_DRESS_UPDATE_REQ_VALUE, result, 0);
			}
		} else {
			//通过道具使用的，需要同步一次dress
			if (result != Status.SysError.SUCCESS_OK_VALUE) {
				RelationService.getInstance().synGuardDressId(player.getId());
			}
		}		
		 
		return true;
	}

}
