package com.hawk.game.serverproxy;

import org.hawk.log.HawkLog;
import org.hawk.net.client.HawkClientSession;
import org.hawk.net.protocol.HawkProtocol;

import io.netty.bootstrap.Bootstrap;

public class ServerProxySession extends HawkClientSession {
	/**
	 * 服务器id
	 */
	protected String serverId;

	/**
	 * 构造
	 */
	protected ServerProxySession(Bootstrap bootstrap, String serverId) {
		super(bootstrap);
		this.serverId = serverId;
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
	 * 回话开启通知
	 */
	protected void onSessionOpened() {
		HawkLog.logPrintln("server proxy session opend, serverId: {}", serverId);
		// 添加会话对象
		ServerProxyManager.getInstance().addServerProxySession(this);
	}

	/**
	 * 回话被关闭通知
	 */
	protected void onSessionClosed() {
		HawkLog.logPrintln("server proxy session closed, serverId: {}", serverId);
		// 移除会话对象
		ServerProxyManager.getInstance().removeServerProxySession(this);
		// 父类处理
		super.onSessionClosed();
	}

	/**
	 * 空闲超时直接关闭连接
	 */
	protected void onSessionHeartbeat() {
		close();
		HawkLog.logPrintln("server proxy session idle timeout closed, serverId: {}", serverId);
	}

	/**
	 * 回话协议通知
	 * 
	 * @param protocol
	 */
	protected void onSessionProtocol(HawkProtocol protocol) {
		ServerProxyManager.getInstance().rpcResponse(serverId, protocol);
	}
}
