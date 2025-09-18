package com.hawk.match.service.impl;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.xid.HawkXID;

import com.hawk.match.msg.ServerProtocolMsg;
import com.hawk.match.service.MatchService;

public class BattleFieldMatch extends MatchService {
	/**
	 * 构造战场匹配
	 */
	public BattleFieldMatch(HawkXID xid) {
		super(xid);
	}

	/**
	 * 战场匹配初始化
	 */
	@Override
	public boolean init() {
		return true;
	}

	/**
	 * 响应服务器协议封装消息
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onServerProtocol(ServerProtocolMsg msg) {
		return dispatchProtocol(msg.getServerId(), msg.getProtocol());
	}
	
	/**
	 * 请求匹配服务
	 * 
	 * @param serverId
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = 0)
	protected boolean onMatchBattle(String serverId, HawkProtocol protocol) {
		responseProtocol(serverId, protocol);
		return true;
	}
}
