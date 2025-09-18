package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.college.CollegeService;

/**
 * 主动退出军事学院
 * 
 * @author Jesse
 *
 */
public class CollegeQuitInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 协议Id */
	private int hpCode;

	public CollegeQuitInvoker(Player player, int hpCode) {
		this.player = player;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int result = CollegeService.getInstance().quitCollege(player);
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

	public int getHpCode() {
		return hpCode;
	}
	
}
