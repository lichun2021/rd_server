package com.hawk.game.invoker.tiberium;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.service.tiberium.TiberiumWarService;

/**
 * 修改联盟旗帜
 * 
 * @author Jesse
 *
 */
public class TWMemberQuitInvoker extends HawkMsgInvoker {
	private Player player;

	/** 小组标识 */
	private String guildId;

	public TWMemberQuitInvoker(Player player, String guildId) {
		this.player = player;
		this.guildId = guildId;
	}

	@Override
	public boolean onMessage(HawkAppObj arg0, HawkMsg arg1) {
		TiberiumWarService.getInstance().onMemberQuit(player, guildId);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	public String getGuildId() {
		return guildId;
	}

	public void setGuildId(String guildId) {
		this.guildId = guildId;
	}

	

}
