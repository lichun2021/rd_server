package com.hawk.game.serverproxy;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tickable.HawkTickable;

import com.hawk.common.ServerInfo;
import com.hawk.game.GsApp;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.protocol.ServerProxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerProxyManager extends HawkTickable {
	/**
	 * 空闲超时
	 */
	protected int idleTimeout;
	/**
	 * 连接超时
	 */
	protected int connectTimeout;
	/**
	 * 启动对象
	 */
	protected Bootstrap bootstrap;
	/**
	 * 当前等待中的rpc请求队列
	 */
	protected Queue<ServerProxyRpc> proxyRpcQueue;
	/**
	 * rpc请求对象和id的映射表
	 */
	protected Map<Integer, ServerProxyRpc> proxyRpcMap;
	/**
	 * 当前的连接集合
	 */
	protected Map<String, ServerProxySession> clientSessions;
	/**
	 * 全局实例对象
	 */
	private static ServerProxyManager instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static ServerProxyManager getInstance() {
		if (ServerProxyManager.instance == null) {
			ServerProxyManager.instance = new ServerProxyManager();
		}
		return instance;
	}

	/**
	 * 构造函数
	 */
	private ServerProxyManager() {
		GsApp.getInstance().addTickable(this);
	}

	/**
	 * 初始化
	 * 
	 * @param connectTimeout
	 * @param idleTimeout
	 * @return
	 */
	public boolean init(int connectTimeout, int idleTimeout) {
		try {
			this.connectTimeout = connectTimeout;
			this.idleTimeout = idleTimeout;

			proxyRpcQueue = new LinkedBlockingQueue<ServerProxyRpc>();
			proxyRpcMap = new ConcurrentHashMap<Integer, ServerProxyRpc>();
			clientSessions = new ConcurrentHashMap<String, ServerProxySession>();

			// 创建时间驱动器
			bootstrap = new Bootstrap();
			NioEventLoopGroup eventGroup = new NioEventLoopGroup();
			bootstrap.group(eventGroup).channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			if (connectTimeout > 0) {
				bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
			}
			HawkLog.logPrintln("server proxy manager init success");

			// 每分钟显示一次当前rpc队列数量
			GsApp.getInstance().addTickable(new HawkPeriodTickable(60000, 60000) {
				@Override
				public void onPeriodTick() {
					HawkLog.logPrintln("server proxy rpc queue size: {}", proxyRpcQueue.size());
				}
			});
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 添加代理会话
	 * 
	 * @param session
	 * @return
	 */
	protected boolean addServerProxySession(ServerProxySession session) {
		clientSessions.put(session.getServerId(), session);
		return true;
	}

	/**
	 * 移除代理会话
	 * 
	 * @param session
	 * @return
	 */
	protected boolean removeServerProxySession(ServerProxySession session) {
		clientSessions.remove(session.getServerId());
		return true;
	}

	/**
	 * 内部rpc请求
	 * 
	 * @param proxyRpc
	 * @return
	 */
	protected boolean rpcRequest(ServerProxyRpc proxyRpc) {
		ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(proxyRpc.getServerId());
		if (serverInfo == null) {
			proxyRpc.onFailed(ServerProxy.error.SERVER_NOT_FOUND_VALUE);
			return false;
		}

		ServerProxySession session = clientSessions.get(proxyRpc.getServerId());

		// 无效的会话先移除
		if (session != null && !session.isActive()) {
			removeServerProxySession(session);
			session = null;
		}

		// 不存在会话即创建新连接
		if (session == null) {
			session = new ServerProxySession(bootstrap, proxyRpc.getServerId());
			
			// 初始化连接
			String[] addrInfo = serverInfo.getHost().split(":");
			if (!session.init(addrInfo[0], Integer.valueOf(addrInfo[1]), connectTimeout, idleTimeout)) {
				// 日志记录
				HawkLog.errPrintln("server proxy session create failed, serverId: {}, host: {}",
						proxyRpc.getServerId(), serverInfo.getHost());

				// 通知rpc回调失败
				proxyRpc.onFailed(ServerProxy.error.SERVER_CONNECT_FAILED_VALUE);
				return false;
			} else {
				// 创建会话记录
				HawkLog.logPrintln("server proxy session create success, serverId: {}, host: {}",
						proxyRpc.getServerId(), serverInfo.getHost());
			}
		}

		// 发送请求
		session.sendProtocol(proxyRpc.getRequest());

		// 添加到检测队列
		if (proxyRpc.update()) {
			proxyRpcQueue.offer(proxyRpc);
			proxyRpcMap.put(proxyRpc.getRpcId(), proxyRpc);
		}

		return true;
	}

	/**
	 * rpc相应回调
	 * 
	 * @param serverId
	 * @param protocol
	 * @return
	 */
	protected boolean rpcResponse(String serverId, HawkProtocol protocol) {
		ServerProxyRpc proxyRpc = proxyRpcMap.remove(protocol.getReserve());
		if (proxyRpc != null) {
			proxyRpcQueue.remove(proxyRpc);
			proxyRpc.onResponse(protocol);
			return true;
		}
		return false;
	}

	/**
	 * 帧更新检测
	 */
	@Override
	public void onTick() {
		// rpc队列更新检测
		while (proxyRpcQueue.size() > 0) {
			ServerProxyRpc proxyRpc = proxyRpcQueue.peek();
			if (proxyRpc.update()) {
				break;
			}

			// 移除
			proxyRpc = proxyRpcQueue.poll();
			proxyRpcMap.remove(proxyRpc.getRpcId());
		}
	}
}
