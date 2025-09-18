package com.hawk.game.invoker;

import org.hawk.app.HawkAppObj;
import org.hawk.msg.HawkMsg;
import org.hawk.msg.invoker.HawkMsgInvoker;

import com.hawk.game.player.Player;
import com.hawk.game.protocol.GuildManager.VoiceRoomModel;
import com.hawk.game.service.GuildService;

/**
 * 修改联盟语音聊天室模式
 * 
 * @author Jesse
 *
 */
public class GuildChangeRoomModelInvoker extends HawkMsgInvoker {
	/** 玩家 */
	private Player player;

	/** 聊天室模式 */
	private VoiceRoomModel model;

	/** 协议Id */
	private int hpCode;

	public GuildChangeRoomModelInvoker(Player player, VoiceRoomModel model, int hpCode) {
		this.player = player;
		this.model = model;
		this.hpCode = hpCode;
	}

	@Override
	public boolean onMessage(HawkAppObj targetObj, HawkMsg msg) {
		GuildService.getInstance().changeVoiceRoomModel(player, model);
		player.responseSuccess(hpCode);
		return true;
	}

	public Player getPlayer() {
		return player;
	}

	public VoiceRoomModel getModel() {
		return model;
	}

	public int getHpCode() {
		return hpCode;
	}
	
}
