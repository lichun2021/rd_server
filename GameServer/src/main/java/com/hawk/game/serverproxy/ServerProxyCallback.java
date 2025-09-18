package com.hawk.game.serverproxy;

import org.hawk.net.protocol.HawkProtocol;

public abstract class ServerProxyCallback {
	/**
	 * 请求的协议相应
	 * 
	 * @param protocol
	 * @return
	 */
	protected abstract boolean onResponse(HawkProtocol protocol);
	
	/**
	 * 请求失败
	 */
	protected abstract boolean onFailed(int errorCode);
}
