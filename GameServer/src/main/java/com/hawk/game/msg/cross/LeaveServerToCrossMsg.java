package com.hawk.game.msg.cross;

import org.hawk.msg.HawkMsg;

/**
 * A->B
 * A处理此消息
 * 该消息会被客户端发起跨服请求的时候同步处理.
 * @author jm
 *
 */
public class LeaveServerToCrossMsg extends HawkMsg {
	private String targetServerId;
	public LeaveServerToCrossMsg() {
		
	}
	
	public LeaveServerToCrossMsg(String targetServerId) {
		this.targetServerId = targetServerId;
	}

	public String getTargetServerId() {
		return targetServerId;
	}
}
