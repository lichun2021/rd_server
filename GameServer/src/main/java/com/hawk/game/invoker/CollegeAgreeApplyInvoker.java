package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;

/**
 * 同意学院成员申请
 * 
 * @author Jesse
 *
 */
public class CollegeAgreeApplyInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;
	
	/** 申请者id*/
	private String applyerId;

	/** 协议Id */
	private int hpCode;

	public CollegeAgreeApplyInvoker(Player player, String applyerId, int hpCode) {
		this.player = player;
		this.applyerId = applyerId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = CollegeService.getInstance().onAgreeApply(player, applyerId);
		if (result == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(hpCode);
		} else {
			player.sendError(hpCode, result, 0);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getApplyerId() {
		return applyerId;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
