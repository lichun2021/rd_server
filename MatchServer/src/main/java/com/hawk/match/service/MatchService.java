package com.hawk.match.service;

import java.lang.reflect.Method;

import org.hawk.app.HawkAppObj;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.hawk.match.MatchApp;
import com.hawk.match.msg.ServerProtocolMsg;
import com.hawk.match.msg.ServerResponseMsg;

public abstract class MatchService extends HawkAppObj {
	/**
	 * 构造函数
	 */
	public MatchService(HawkXID xid) {
		super(xid);
	}

	/**
	 * 服务器消息
	 * 
	 * @param msg
	 * @return
	 */
	protected boolean dispatchProtocol(String serverId, HawkProtocol protocol) {
		try {
			// 注解模式的处理器
			Method method = getProtoMethod(protocol.getType(), "");
			if (method != null) {
				method.invoke(this, serverId, protocol);
				return true;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 制定服务器响应请求
	 * 
	 * @param serverId
	 * @param protocol
	 * @return
	 */
	public boolean responseProtocol(String serverId, HawkProtocol protocol) {
		HawkTaskManager.getInstance().postMsg(MatchApp.getInstance().getXid(), ServerResponseMsg.valueOf(serverId, protocol));
		return true;
	}
	
	/**
	 * 初始化服务
	 * 
	 * @return
	 */
	public abstract boolean init();	
	
	/**
	 * 每个服务都必须copy实现此接口
	 * 
	 * @param msg
	 * @return
	 */
	public abstract boolean onServerProtocol(ServerProtocolMsg msg);
}
