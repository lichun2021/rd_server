package com.hawk.game.invoker.tiberium;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.service.tiberium.TiberiumWarService;

/**
 * 修改联盟旗帜
 * 
 * @author Jesse
 *
 */
public class TWTeamMemberResetInvoker extends HawkMsgInvoker {
	private Player player;

	/** 小组标识 */
	private int teamIndex;

	/** 协议Id */
	private int hpCode;

	public TWTeamMemberResetInvoker(Player player, int teamIndex, int hpCode) {
		this.player = player;
		this.teamIndex = teamIndex;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		int result = TiberiumWarService.getInstance().onTeamMemberReset(player, teamIndex, hpCode);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			player.sendError(hpCode, result, 0);
		}
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public int getTeamIndex() {
		return teamIndex;
	}

	public void setTeamIndex(int teamIndex) {
		this.teamIndex = teamIndex;
	}

	public int getHpCode() {
		return hpCode;
	}

	public void setHpCode(int hpCode) {
		this.hpCode = hpCode;
	}

	
	
}
