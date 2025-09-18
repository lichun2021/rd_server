package com.hawk.game.invoker;

import java.util.Map;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkRpcMsg;
import org.hawk.msg.invoker.HawkRpcInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Friend.BlackListOper;
import com.hawk.game.service.RelationService;

public class RelationBlacklistOperRpcInvoker extends HawkRpcInvoker {
	/**
	 * 操作的玩家
	 */
	private Player player;
	/**
	 * 被操作的玩家ID
	 */
	private String targetId;
	/**
	 * 类型
	 */
	private int type;
	
	public RelationBlacklistOperRpcInvoker(Player player, String targetId, int type) {
		this.player = player;
		this.targetId = targetId;
		this.type = type;
	}
	
	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkRpcMsg msg, Map<String, Object> result) {
		int rlt = 0;
		if (type == BlackListOper.DELETE_VALUE) {
			rlt = RelationService.getInstance().blacklistDelete(player.getId(), targetId);
		} else {
			rlt = RelationService.getInstance().blacklistAdd(player.getId(), targetId);
		}
		result.put("rlt", rlt);

		return true;
	}

	@Override
	public boolean onComplete(HawkAppObj callerObj, Map<String, Object> result) {
		int rlt = (Integer) result.get("rlt");
		if (rlt == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.BLACKLIST_OPERATION_REQ_VALUE);
		} else {
			player.sendError(HP.code.BLACKLIST_OPERATION_REQ_VALUE, rlt, 0);
		}

		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTargetId() {
		return targetId;
	}

	public int getType() {
		return type;
	}

}
