package com.hawk.game.serverproxy;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.game.protocol.ServerProxy;
import com.hawk.game.protocol.ServerProxy.HPProxyTestResponse;

public class ServerProxyResponser extends HawkAppObj {
	/**
	 * 全局实例对象
	 */
	private static ServerProxyResponser instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static ServerProxyResponser getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 * 
	 * @param xid
	 */
	public ServerProxyResponser(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 服务器代理协议检测
	 * 
	 * @param session
	 * @param protocol
	 * @return
	 */
	public boolean dispatchServerProxyProtocol(HawkSession session, HawkProtocol protocol) {
		if (protocol.getType() >= ServerProxy.code.SERVER_PROXY_BASE_CMD_VALUE) {
			// 设置服务器代理会话标记, 去除心跳超时关闭会话功能
			if (session.getUserObject("ServerProxy") == null) {
				// 移除心跳检测
				if (session.getChannel().pipeline().context("idle") != null) {
					session.getChannel().pipeline().remove("idle");
					session.getChannel().pipeline().remove("heartbeat");
				}

				// 设置已标记状态
				session.setUserObject("ServerProxy", true);
			}

			// 分发
			HawkTaskManager.getInstance().postProtocol(getXid(), protocol);
			return true;
		}
		return false;
	}

	/**
	 * 基础测试请求
	 * 
	 * @param protocol
	 * @return
	 */
	@ProtocolHandler(code = ServerProxy.code.SERVER_PROXY_BASE_CMD_VALUE)
	private boolean onBaseRequest(HawkProtocol protocol) {
		HPProxyTestResponse.Builder builder = HPProxyTestResponse.newBuilder();
		builder.setRspMsg(HawkOSOperator.randomString(32));
		protocol.response(HawkProtocol.valueOf(protocol.getType(), builder).setReserve(protocol.getReserve()));
		return true;
	}
}
