package com.hawk.game.invoker.tiberium;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.TiberiumWar.TWMemberMangeType;
import com.hawk.game.service.tiberium.TiberiumWarService;

/**
 * 修改联盟旗帜
 * 
 * @author Jesse
 *
 */
public class TWTeamMemberManageInvoker extends HawkMsgInvoker {
	private Player player;

	/** 小组标识 */
	private int teamIndex;

	/** 小组name */
	private TWMemberMangeType type;
	
	private String memberId;

	/** 协议Id */
	private int hpCode;

	public TWTeamMemberManageInvoker(Player player, int teamIndex, TWMemberMangeType type, String memberId, int hpCode) {
		this.player = player;
		this.teamIndex = teamIndex;
		this.type = type;
		this.memberId = memberId;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		TiberiumWarService.getInstance().onMemberManage(player, teamIndex, type, memberId, hpCode);
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

	public TWMemberMangeType getType() {
		return type;
	}

	public void setType(TWMemberMangeType type) {
		this.type = type;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public int getHpCode() {
		return hpCode;
	}

	public void setHpCode(int hpCode) {
		this.hpCode = hpCode;
	}

	
	
}
