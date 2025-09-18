package com.hawk.match.msg;

import org.hawk.msg.HawkMsg;
import org.hawk.net.protocol.HawkProtocol;

public class ServerResponseMsg extends HawkMsg {
	/**
	 * 服务器id
	 */
	private String serverId;
	/**
	 * 协议对象
	 */
	private HawkProtocol protocol;

	/**
	 * 私有构造
	 */
	private ServerResponseMsg() {

	}

	/**
	 * 获取服务器id
	 * 
	 * @return
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * 获取协议
	 * 
	 * @return
	 */
	public HawkProtocol getProtocol() {
		return protocol;
	}

	/**
	 * 创建消息
	 * 
	 * @param serverId
	 * @param protocol
	 * @return
	 */
	public static ServerResponseMsg valueOf(String serverId, HawkProtocol protocol) {
		ServerResponseMsg msg = new ServerResponseMsg();
		msg.serverId = serverId;
		msg.protocol = protocol;
		return msg;
	}
}
