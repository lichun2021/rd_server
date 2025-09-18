package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.cyborgWar.CyborgWarService;

/**
 * 解散战队
 * 
 * @author Jesse
 *
 */
public class CyborgDismissTeamInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 战队id */
	private String teamId;

	public CyborgDismissTeamInvoker(Player player, String teamId) {
		this.player = player;
		this.teamId = teamId;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		int operationResult = CyborgWarService.getInstance().onDismissTeam(player, teamId);
		if (operationResult == Status.SysError.SUCCESS_OK_VALUE) {
			player.responseSuccess(HP.code.CYBORG_WAR_DISMISS_TEAM_C_VALUE);
			return true;
		}
		player.sendError(HP.code.CYBORG_WAR_DISMISS_TEAM_C_VALUE, operationResult, 0);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public String getTeamId() {
		return teamId;
	}

}
