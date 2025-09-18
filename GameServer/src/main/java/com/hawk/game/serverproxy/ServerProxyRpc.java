package com.hawk.game.serverproxy;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;

import com.hawk.game.protocol.ServerProxy;

public class ServerProxyRpc {
	/**
	 * 全局的RPCid生成器
	 */
	static AtomicInteger GLOBAL_RPCID = new AtomicInteger();
	
	/**
	 * rpc的唯一id
	 */
	protected int rpcId;
	/**
	 * rpc的服务器对象
	 */
	protected String serverId;
	/**
	 * rpc请求协议
	 */
	protected HawkProtocol request;
	/**
	 * rpc的回调
	 */
	protected ServerProxyCallback callback;
	/**
	 * 回调线程
	 */
	protected int callbackThreadIndex = -1;
	/**
	 * 请求超时到期时间
	 */
	protected long requestTimeout;

	/**
	 * 创建rpc对象
	 * 
	 * @param serverId
	 * @param request
	 * @param callback
	 * @return
	 */
	public static ServerProxyRpc valueOf(String serverId) {
		ServerProxyRpc rpc = new ServerProxyRpc(serverId);
		return rpc;
	}

	/**
	 * 构造函数
	 * 
	 * @param serverId
	 * @param request
	 * @param callback
	 */
	private ServerProxyRpc(String serverId) {
		this.rpcId = GLOBAL_RPCID.incrementAndGet();
		this.serverId = serverId;
	}

	/**
	 * 获取uuid
	 * 
	 * @return
	 */
	protected int getRpcId() {
		return rpcId;
	}

	/**
	 * 获取请求服务器id
	 * 
	 * @return
	 */
	protected String getServerId() {
		return serverId;
	}

	/**
	 * 获取请求协议
	 * 
	 * @return
	 */
	protected HawkProtocol getRequest() {
		return request;
	}
	
	/**
	 * 清理rpc数据
	 */
	protected void clear() {
		this.request = null;
		this.callback = null;
	}

	/**
	 * 发起请求
	 * 
	 * @return
	 */
	public boolean request(HawkProtocol request, ServerProxyCallback callback, int timeout) {
		this.request = request;
		this.callback = callback;
		
		// 记录回调逻辑线程序号(和发起请求的线程一致)
		if (callback != null) {
			HawkThreadPool taskExecutor = HawkTaskManager.getInstance().getTaskExecutor(); 
			callbackThreadIndex = taskExecutor.getThreadIndex(HawkOSOperator.getThreadId());
		}
		
		if (request != null) {
			request.setReserve(rpcId);
			if (timeout > 0) {
				requestTimeout = HawkTime.getMillisecond() + timeout;
			}
			return ServerProxyManager.getInstance().rpcRequest(this);
		}
		
		return false;
	}

	/**
	 * rpc请求的协议回应
	 * 
	 * @param response
	 * @return
	 */
	protected boolean onResponse(final HawkProtocol response) {
		requestTimeout = 0;
		if (callback == null) {
			return false;
		}
		
		final ServerProxyCallback rpcCallback = callback;
		if (callbackThreadIndex >= 0) {
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					rpcCallback.onResponse(response);
					return null;
				}
			}, callbackThreadIndex);
		} else {
			rpcCallback.onResponse(response);
		}
		
		// 清理数据
		clear();
		return true;
	}

	/**
	 * rpc请求出错
	 * 
	 * @param errorCode
	 * @return
	 */
	protected boolean onFailed(final int errorCode) {
		if (callback == null) {
			return true;
		}
		
		final ServerProxyCallback rpcCallback = callback;
		if (callbackThreadIndex >= 0) {
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					rpcCallback.onFailed(errorCode);
					return null;
				}
			}, callbackThreadIndex);
		} else {
			rpcCallback.onFailed(errorCode);
		}
		
		// 清理数据
		clear();
		return true;
	}

	/**
	 * 更新检测
	 */
	protected boolean update() {
		// 是否有到期超时时间？
		if (requestTimeout > 0) {
			// 已到超时？
			if (HawkTime.getMillisecond() > requestTimeout && callback != null) {
				onFailed(ServerProxy.error.SERVER_REQUEST_TIMEOUT_VALUE);
				requestTimeout = 0;
			}
			return true;
		}
		return false;
	}
}
