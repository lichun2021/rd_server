package com.hawk.game.module;

import com.hawk.game.protocol.HP;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.annotation.ProtocolHandler;
import com.hawk.game.protocol.SysProtocol.UrgencyReq;
import com.hawk.game.protocol.SysProtocol.UrgencyResp;

/**
 * 
 * 线上紧急事件处理类
 *
 * @author lating
 */
public class PlayerUrgencyModule extends PlayerModule {
		
	/**
	 * 构造
	 *
	 * @param player
	 */
	public PlayerUrgencyModule(Player player) {
		super(player);
	}

	/**
	 * 线上紧急事件处理
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = HP.code.URGENCY_LOGIC_C_VALUE)
	private boolean onHeartBeat(HawkProtocol protocol) {
		UrgencyReq cmd = protocol.parseProtocol(UrgencyReq.getDefaultInstance());
		
		// 处理逻辑  .....
		
		UrgencyResp.Builder builder = UrgencyResp.newBuilder();
		builder.setRetMsg("retMsg: " + cmd.getType());
		protocol.response(HawkProtocol.valueOf(HP.code.URGENCY_LOGIC_S, builder));
		return true;
	}
	
}
