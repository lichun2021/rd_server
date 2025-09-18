package com.hawk.game.msg;

import org.hawk.msg.HawkFutureMsg;

import com.hawk.gamelib.GameConst;

/**
 * 玩家迁入消息
 * @author jm
 *
 */
public class ImmigratePlayerMsg extends HawkFutureMsg<Boolean> {
	
	public ImmigratePlayerMsg() {
		this(GameConst.MsgId.IMMIGRATE);
	}
	
	public ImmigratePlayerMsg(int type) {
		super(type);
	}

	/**
	 * 玩家ID
	 */
	private String playerId;
	
	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public static ImmigratePlayerMsg valueOf(String playerId) {
		ImmigratePlayerMsg msg = new ImmigratePlayerMsg();
		msg.playerId = playerId;
		
		return msg;
	}
	
}
