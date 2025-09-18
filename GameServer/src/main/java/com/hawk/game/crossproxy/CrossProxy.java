package com.hawk.game.crossproxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.net.session.HawkSession;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.profiler.HawkSysProfiler;
import org.hawk.profiler.HawkProfilerAnalyzer;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.thread.HawkThreadPool;
import org.hawk.tickable.HawkTickable;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ProxyNodeCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.SysProtocol.PlayerIdList;
import com.hawk.game.util.GsConst;

public class CrossProxy extends HawkTickable {
	/**
	 * 跨服协议类型
	 * 
	 * @author hawk
	 *
	 */
	public static class ProtoType {
		public static final int PROTOCOL  = 0;
		
		public static final int NOTIFY  = 1;
		
		public static final int RPC_REQ = 2;
		public static final int RPC_REP = 3;
		
		public static final int BROADCAST = 4;
		
		public static final int HEART_BEAT = 99;
	}
	
	/**
	 * 跨服通信对象
	 */
	private HawkZmq activeZmq;
	/**
	 * 当前所有连接的通信对象
	 */
	private List<HawkZmq> zmqList;
	/**
	 * 节点心跳时间
	 */
	private long heartbeatTime;
	/**
	 * 主服的节点检测时间
	 */
	private long masterCheckTime;
	/**
	 * 保持活跃的心跳回复时间
	 */
	private long keepaliveTime;
	/**
	 * 代理头信息缓存对象
	 */
	private byte[] headerBytes;
	/**
	 * 发送队列
	 */
	private Queue<HawkProtocol> protoSendQueue;
	/**
	 * rpc请求存根信息
	 */
	private LoadingCache<String, CsRpcStub> rpcStubCache;
	/**
	 * rpc时间
	 */
	private Map<String, Long> stubTimeMap;
	
	/**
	 * 单例对象
	 */
	private static CrossProxy instance = null;
	/**
	 * 上一次的记录时间
	 */
	private long lastRecordTime;
	/**
	 * 发送协议数据量
	 */
	private int sendProtocolNum;
	/**
	 * 发送协议消耗时间
	 */
	private long sendProtocolCostTime;
	/**
	 * 接受协议数量
	 */
	private int receivedProtocolNum;
	/**
	 * 接受协议消耗时间.
	 */
	private long receivedProtocolCostTime;
	/**
	 * 获取实体对象
	 * 
	 * @return
	 */
	public static CrossProxy getInstance() {
		if (instance == null) {
			synchronized (CrossProxy.class) {
				if (instance == null) {
					instance = new CrossProxy();
				}
			}			
		}
		return instance;
	}

	/**
	 * 默认构造
	 * 
	 * @param xid
	 */
	private CrossProxy() {
		headerBytes = new byte[8192];
		
		protoSendQueue = new LinkedBlockingQueue<HawkProtocol>();
		
		stubTimeMap = new ConcurrentHashMap<String, Long>();
		
		rpcStubCache = CacheBuilder.newBuilder().recordStats().maximumSize(16384).initialCapacity(32).expireAfterAccess(60, TimeUnit.SECONDS)
				.build(new CacheLoader<String, CsRpcStub>() {
					@Override
					public CsRpcStub load(String rpcId) {
						return null;
					}
				});
		
		instance = this;
	}
	
	/**
	 * 初始化跨服通信
	 * 
	 * @return
	 */
	public boolean init() {
		if (zmqList != null) {
			return false;
		}
		
		int nodeCount = HawkConfigManager.getInstance().getConfigSize(ProxyNodeCfg.class);
		if (nodeCount <= 0) {
			HawkLog.warnPrintln("csproxy node miss");
			return false;
		}
		
		// 检测主服务器
		ProxyHelper.contendMasterServer(false);
		
		// 初始化上下文
		HawkZmqManager.getInstance().init(HawkZmq.HZMQ_CONTEXT_THREAD);
		
		int localNodeCount = 0;
		// 初始化所有连接
		zmqList = new ArrayList<HawkZmq>(nodeCount);
		for (int i = 0; i < nodeCount; i++) {
			ProxyNodeCfg nodeCfg = HawkConfigManager.getInstance().getConfigByIndex(ProxyNodeCfg.class, i);
			if (nodeCfg == null || !nodeCfg.getAreaId().equals(GsConfig.getInstance().getAreaId())) {
				continue;
			}
			
			localNodeCount++;
			// 初始化通信对象
			HawkZmq zmqObj = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.DEALER);
			
			// 设置缓冲区大小(1M)
			zmqObj.checkCacheBuffer(1024 * 1024);
			
			// 设置标识
			String identify = GsConfig.getInstance().getServerId();
			if (GsConfig.getInstance().getZmqIdentifyMode() > 0) {
				identify = String.format("%s@%s", identify, HawkUUIDGenerator.genUUID());
			}
			zmqObj.setIdentity(identify.getBytes());
			
			// 修改高低水位
			if (GsConfig.getInstance().getZmqSndHwm() > 0) {
				zmqObj.getSocket().setSndHWM(GsConfig.getInstance().getZmqSndHwm());
			}
			
			if (GsConfig.getInstance().getZmqRcvHwm() > 0) {
				zmqObj.getSocket().setRcvHWM(GsConfig.getInstance().getZmqRcvHwm());
			}			
			
			// 连接到对端服务器			
			zmqObj.connect(nodeCfg.getAddr());
			
			// 添加到列表中
			zmqList.add(zmqObj);
			
			HawkLog.logPrintln("csproxy init node success, address: {}", nodeCfg.getAddr());		
		}
		
		if (GsConfig.getInstance().isDebug() && localNodeCount <= 0) {
			HawkLog.errPrintln("csproxy node this server miss");
			throw new RuntimeException("cfg/cs/proxyNode.xml配置有误");
		}
		
		// 检测起始时间
		masterCheckTime = HawkTime.getMillisecond();
		
		// 开启一个事件线程
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				csEventLoop();
			}
		});
		thread.setName("CSProxy");
		thread.setDaemon(true);
		thread.start();
		
		// 注册更新机制
		GsApp.getInstance().addTickable(this);
		
		return true;
	}
	
	/**
	 * 初始化通信对象
	 */
	private HawkZmq getActiveZmq() {
		return activeZmq;
	}

	/**
	 * 发送通知
	 * 
	 * @param protocol
	 * @param serverId
	 * @param targetPlayer
	 * @return
	 */
	public boolean sendNotify(HawkProtocol protocol, String serverId, String targetPlayer) {
		return  sendNotify(protocol, serverId, null, targetPlayer);
	}
	
	/**
	 * 发送通知
	 * 
	 * @param protocol
	 * @param targetSid
	 * @param targetXid
	 */
	public boolean sendNotify(HawkProtocol protocol, String serverId, String sourcePlayer, String targetPlayer) {
		return sendProtocol(protocol, serverId, sourcePlayer, targetPlayer, ProtoType.NOTIFY);
	}
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @param serverId
	 * @param targetPlayer
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol, String serverId, String targetPlayer) {
		return sendProtocol(protocol, serverId, null, targetPlayer, ProtoType.PROTOCOL);
	}
	
	/**
	 * 发送协议
	 * 
	 * @param protocol
	 * @param serverId
	 * @param targetPlayer
	 * @param protoType
	 * @return
	 */
	public boolean sendProtocol(HawkProtocol protocol, String serverId, String sourcePlayer, String targetPlayer, int protoType) {
		ProxyHeader header = new ProxyHeader();
		header.setType(protoType);
		header.setFrom(GsConfig.getInstance().getServerId());
		header.setTo(serverId);
		header.setSource(sourcePlayer);
		header.setTarget(targetPlayer);
		
		return sendProxyProtocol(header, protocol);
	}
	
	/**
	 * 跨服协议广播
	 */
	public boolean broadcastProtocolV2(String serverId, Set<String> playerIds, HawkProtocol protocol) {
		serverId = GlobalData.getInstance().getMainServerId(serverId);
		return broadcastProtocol(serverId, playerIds, protocol);
	}
	
	/**
	 * 协议广播
	 * 没有做强制主服转换, 建议使用 {@link #broadcastProtocolV2()}
	 * @param serverId(这个必须是转换之后的所在物理服务器的id)
	 * @param playerIds
	 * @param protocol
	 * @return
	 */
	@Deprecated
	public boolean broadcastProtocol(String serverId, Set<String> playerIds, HawkProtocol protocol) {
		try {
			if (playerIds == null || playerIds.isEmpty()) {
				return true;
			}
			// serverId = GlobalData.getInstance().getMainServerId(serverId);
			
			// 自己给自己服进行广播
			if (serverId.equals(GsConfig.getInstance().getServerId())) {
				throw new RuntimeException("cannot broadcast to local server");
			}
			
			ProxyHeader header = new ProxyHeader();
			header.setType(ProtoType.BROADCAST);
			header.setFrom(GsConfig.getInstance().getServerId());
			header.setTo(serverId);
			header.getBroadcastIds().addAll(playerIds);
			
			return sendProxyProtocol(header, protocol);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 发送心跳
	 * 
	 * @return
	 */
	public boolean sendHeartbeat() {
		// 判断连接状态
		HawkZmq csZmq = getActiveZmq();
		if (csZmq == null) {
			return false;
		}
		try {
			ProxyHeader header = new ProxyHeader();
			header.setType(ProtoType.HEART_BEAT);
			header.setFrom(GsConfig.getInstance().getServerId());
			header.setSource(new String(csZmq.getSocket().getIdentity(), "UTF-8"));
			
			return sendProxyProtocol(header, HawkProtocol.valueOf());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
		
	}
	
	/**
	 * 向指定服rpc请求
	 * 
	 * @param protocol
	 * @param callback
	 * @param serverId
	 * @param targetXid
	 */
	public boolean rpcRequest(HawkProtocol protocol, CsRpcCallback callback, String serverId, String sourcePlayer, String targetPlayer) {
		ProxyHeader header = new ProxyHeader();
		header.setType(ProtoType.NOTIFY);
		header.setFrom(GsConfig.getInstance().getServerId());
		header.setTo(serverId);
		header.setSource(sourcePlayer);
		header.setTarget(targetPlayer);
		header.setRpcid(HawkOSOperator.randomUUID());
		
		// 注册rpc
		int threadIdx = HawkTaskManager.getInstance().getTaskExecutor().getThreadIndex(HawkOSOperator.getThreadId());
		rpcStubCache.put(header.getRpcid(), new CsRpcStub(header, threadIdx, callback));
		stubTimeMap.put(header.getRpcid(), HawkTime.getMillisecond());
		
		return sendProxyProtocol(header, protocol);
	}
	
	/**
	 * 响应rpc请求
	 * 
	 * @param header
	 * @param protocol
	 */
	public boolean rpcResponse(ProxyHeader header, HawkProtocol protocol) {
		if (header == null) {
			return false;
		}
		
		ProxyHeader respHeader = new ProxyHeader();
		respHeader.setType(ProtoType.RPC_REP);
		respHeader.setFrom(GsConfig.getInstance().getServerId());
		respHeader.setTo(header.getFrom());
		respHeader.setSource(header.getTarget());
		respHeader.setRpcid(header.getRpcid());
		respHeader.setTarget(header.getSource());
		
		return sendProxyProtocol(respHeader, protocol);
	}
	
	/**
	 * 发送跨服代理协议
	 * 
	 * @param header
	 * @param protocol
	 * @return
	 */
	private boolean sendProxyProtocol(ProxyHeader header, HawkProtocol protocol) {
		try {
			// 判断连接状态
			HawkZmq csZmq = getActiveZmq();
			if (csZmq == null) {
				return false;
			}
			
			// 添加到协议发送队列
			header.setTimestamp(HawkTime.getMillisecond());
			protocol.setUserData(header);
			protoSendQueue.add(protocol);
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 事件处理主循环
	 */
	private void csEventLoop() {
		while (true) {
			try {
				// 监视器事件
				updateProxyState();
				
				// 发送队列中的协议数据
				flushProtoQueue();
				
				// 代理服务器事件
				updateProxyEvent();
				
				//输出性能数据.
				recordMonitor();
			} catch (Exception e) {
				HawkException.catchException(e);
			}			
		}
	}
	
	private void recordMonitor() {
		try {
			if (HawkTime.getMillisecond() - lastRecordTime >= 10000) {
				//时间为毫秒
				HawkLog.logPrintln("crossProxy ten seconds sendProtocolNum:{} sendProtocolCostTime:{}, receivedProtocolNum:{} receiveProtocolCostTime:{}", 
						sendProtocolNum, sendProtocolCostTime / 1000000, receivedProtocolNum, receivedProtocolCostTime / 1000000);
				
				sendProtocolNum = 0;
				sendProtocolCostTime = 0;
				receivedProtocolCostTime = 0;
				receivedProtocolNum = 0;
				lastRecordTime = HawkTime.getMillisecond();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
		
	}

	/**
	 * 发送心跳
	 */
	private void updateProxyState() {
		long currentTime = HawkTime.getMillisecond();
		
		// 周期发送心跳
		if (currentTime - heartbeatTime >= ProxyHelper.HEART_BEAT_PERIOD) {
			heartbeatTime = currentTime;
			
			// 主服务器竞争
			ProxyHelper.contendMasterServer(true);
						
			// 和主节点选择的转发节点一致
			if (!ProxyHelper.isMasterServer()) {
				String proxyAddr = ProxyHelper.getProxyAddress();
				if (!HawkOSOperator.isEmptyString(proxyAddr)) {
					for (HawkZmq zmqObj : zmqList) {
						if (!zmqObj.getAddress().equals(proxyAddr)) {
							continue;
						}
						
						if (activeZmq != zmqObj) {
							activeZmq = zmqObj;
							HawkLog.logPrintln("csproxy slave select proxy: {}", proxyAddr);
						}
						break;
					}					
				}
			}
			
			// 发送心跳
			sendHeartbeat();
		}
		
		// 主节点心跳检测
		if (currentTime - masterCheckTime > ProxyHelper.MASTER_CHECK_PERIOD ) {
			masterCheckTime = currentTime;
			if (ProxyHelper.isMasterServer()) {
				// 没有激活节点或者检测周期内没有任何心跳, 切换节点
				if (activeZmq == null || currentTime - keepaliveTime >= ProxyHelper.MASTER_CHECK_PERIOD) {
					// 准备切换
					if (activeZmq != null) {
						HawkLog.logPrintln("csproxy master discard proxy: {}", activeZmq.getAddress());
					}
					
					// 遍历可用节点准备切换
					for (HawkZmq zmqObj : zmqList) {
						// 切换节点
						if (activeZmq != zmqObj) {
							// 尝试另一个节点
							activeZmq = zmqObj;						
							HawkLog.logPrintln("csproxy master select proxy: {}", zmqObj.getAddress());
							
							// 向新节点发心跳
							sendHeartbeat();
							break;
						}
					}
				}
			}
			
		}
	}

	/**
	 * 响应的心跳通知
	 */
	private void onHeartbeat() {
		HawkZmq csZmq = getActiveZmq();
		if (csZmq == null) {
			return;
		}
		
		// 更新心跳时间
		keepaliveTime = HawkTime.getMillisecond();
		
		String indentify = "";
		byte[] identityBytes = csZmq.getSocket().getIdentity();
		if (identityBytes != null) {
			indentify = new String(identityBytes);
		}
		HawkLog.logPrintln("csproxy heartbeat: {}, identify: {}", csZmq.getAddress(), indentify);
		
		// 注册使用的节点
		if (ProxyHelper.isMasterServer()) {
			String proxyAddr = ProxyHelper.getProxyAddress();
			if (!csZmq.getAddress().equals(proxyAddr)) {
				ProxyHelper.registerProxyNode(csZmq.getAddress());
				HawkLog.logPrintln("csproxy master select proxy: {}", csZmq.getAddress());	
			}			
		}
	}
	
	/**
	 * 发送所有队列协议
	 */
	private void flushProtoQueue() {
		// 判断连接状态
		HawkZmq csZmq = getActiveZmq();
		if (csZmq == null) {
			return;
		}
		long nanoStartTime = System.nanoTime();
		int oldSendProtocolNum = sendProtocolNum;
		while (protoSendQueue.size() > 0) {
			try {
				HawkProtocol protocol = protoSendQueue.poll();
				if (protocol == null) {
					continue;
				}
				
				ProxyHeader header = protocol.getUserData();
				
				if (header.getType() == ProtoType.HEART_BEAT) {
					csZmq.send(header.pack().getBytes(), HawkZmq.HZMQ_NOBLOCK);
				} else if (header.getType() == ProtoType.BROADCAST) {
					// 构建广播对象列表协议
					if (header.getBroadcastIds().size() > 0) {
						PlayerIdList.Builder idList = PlayerIdList.newBuilder();
						idList.addAllPlayerId(header.getBroadcastIds());
						
						csZmq.send(header.pack().getBytes(), HawkZmq.HZMQ_NOBLOCK | HawkZmq.HZMQ_SNDMORE);
						csZmq.sendProtocol(protocol, HawkZmq.HZMQ_NOBLOCK | HawkZmq.HZMQ_SNDMORE);
						csZmq.sendProtocol(HawkProtocol.valueOf(0, idList), HawkZmq.HZMQ_NOBLOCK);
					}
				} else {
					csZmq.send(header.pack().getBytes(), HawkZmq.HZMQ_NOBLOCK | HawkZmq.HZMQ_SNDMORE);
					csZmq.sendProtocol(protocol, HawkZmq.HZMQ_NOBLOCK);
				}
				
				sendProtocolNum ++;
				
				// 统计信息
				HawkProfilerAnalyzer.getInstance().addSendProtocolInfo(protocol.getSize() + HawkProtocol.HEADER_SIZE);
				HawkSysProfiler.getInstance().incSendProtoTask(protocol.getType());
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (oldSendProtocolNum != sendProtocolNum) {
			long nanoEndStartTime = System.nanoTime();
			sendProtocolCostTime += (nanoEndStartTime - nanoStartTime);
		}		
	}
	
	/**
	 * 运行跨服事件
	 */
	private boolean updateProxyEvent() {
		try {
			// 判断连接状态
			HawkZmq csZmq = getActiveZmq();
			if (csZmq == null) {
				return false;
			}
			
			// 检测事件
			int event = csZmq.pollEvent(HawkZmq.HZMQ_EVENT_READ, 50);
			if (event <= 0) {
				return false;
			}
			
			long nanoStartTime = System.nanoTime();
			int oldReceiveProtocolNum = receivedProtocolNum;
			// 取出所有协议
			while (true) {
				// 接收协议头信息
				int headSize = csZmq.recv(headerBytes, HawkZmq.HZMQ_DONTWAIT);
				if (headSize <= 0) {
					break;
				}
				
				// 解析协议头
				ProxyHeader header = new ProxyHeader();
				header.unpack(new String(headerBytes, 0, headSize, "UTF-8"));
				
				// 心跳处理
				if (header.getType() == ProtoType.HEART_BEAT) {
					receivedProtocolNum++;
					onHeartbeat();
					continue;
				}
				
				// 接收协议体
				if (!csZmq.hasReceiveMore()) {
					continue;
				}
				
				// 接收协议体
				HawkProtocol protocol = csZmq.recvProtocol(HawkZmq.HZMQ_DONTWAIT);
				
				// 广播模式下, 还有参数待接收
				HawkProtocol idsProto = null;
				if (csZmq.hasReceiveMore()) {
					idsProto = csZmq.recvProtocol(HawkZmq.HZMQ_DONTWAIT);
				}
				
				// 头标记信息
				if (!header.isValid()) {
					HawkLog.errPrintln("csproxy header error: {}", header.getOri());
					continue;
				}
				
				// 协议正确性
				if (protocol == null) {
					HawkLog.errPrintln("csproxy decode protocol fail header:{}", header.toString());					
					continue;
				}			

				// 超时的协议直接丢弃
				if (header.getTimestamp() + ProxyHelper.PROTOCOL_EXPIRE < HawkTime.getMillisecond()) {
					HawkLog.errPrintln("csproxy header timeout: {}, protocol: {}", header.getOri(), protocol.getType());
					continue;
				}
				
				// 协议处理
				protocol.setUserData(header);
				
				if (header.getType() == ProtoType.BROADCAST) {
					onBroadcast(header, protocol, idsProto);
				} else {
					onProtocol(header, protocol);
				}
				
				receivedProtocolNum++;
			}
			
			if (oldReceiveProtocolNum != receivedProtocolNum) {
				long nanoEndTime = System.nanoTime();
				receivedProtocolCostTime += (nanoEndTime - nanoStartTime);
			}			
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 协议处理
	 * 
	 * @param header
	 * @param protocol
	 * @return
	 */
	private boolean onProtocol(ProxyHeader header, HawkProtocol protocol) {
		try {
			// 协议处理
			if (header.getType() == ProtoType.PROTOCOL) {
				transportProtocol(header, protocol);
				return true;
			}
			
			// 通知处理
			if (header.getType() == ProtoType.NOTIFY) {
				dispatchProtocol(header, protocol);
			}
			
			// rpc处理
			if (header.getType() == ProtoType.RPC_REP) {
				CsRpcStub stub = rpcStubCache.getIfPresent(header.getRpcid());
				if (stub != null) {
					rpcStubCache.invalidate(header.getRpcid());
					stubTimeMap.remove(header.getRpcid());
					
					HawkTaskManager.getInstance().postTask(new HawkTask() {
						@Override
						public Object run() {
							stub.getCallback().invoke(protocol);
							return null;
						}
					}, stub.getThreadIdx());
					return true;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		} 
		
		return false;
	}
	
	
	/**
	 * 广播协议
	 * 
	 * @param header
	 * @param protocol
	 * @param idsProto
	 */
	private void onBroadcast(ProxyHeader header, HawkProtocol protocol, HawkProtocol idsProto) {
		if (header == null || protocol == null || header.getType() != ProtoType.BROADCAST || idsProto == null) {
			return;
		}
		
		try {
			HawkTask broadcastTask = new HawkTask() {			
				@Override
				public Object run() {
					// 构建广播任务
					PlayerIdList idList = idsProto.parseProtocol(PlayerIdList.getDefaultInstance());
					if (idList == null || idList.getPlayerIdCount() <= 0) {
						return null;
					}
					
					int count = idList.getPlayerIdCount();
					for (int i = 0; i < count; i++) {
						try {
							Player player = GlobalData.getInstance().queryPlayer(idList.getPlayerId(i));							
							if (player != null) {
								HawkSession session = player.getSession();
								if (session != null) {
									session.sendProtocol(protocol);	
								}								
							}
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
					
					return null;
				}
			};
			
			// 线程池中固定线程执行
			HawkThreadPool threadPool = HawkTaskManager.getInstance().getThreadPool("task");
			if (threadPool != null) {
				threadPool.addTask(broadcastTask, threadPool.getThreadNum() - 1, false);
			} else {
				HawkTaskManager.getInstance().postTask(broadcastTask,  0);		
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 协议转发
	 * 
	 * @param header
	 * @param protocol
	 */
	private void transportProtocol(ProxyHeader header, HawkProtocol protocol) {
		CrossPlayerProtocolHandler.getInstance().onTransportProtocol(header, protocol);
	}
	
	/**
	 * 投递协议
	 * 
	 * @param header
	 * @param protocol
	 */
	private void dispatchProtocol(ProxyHeader header, HawkProtocol protocol) {
		String targetId = header.getTarget() == null ? "" : header.getTarget();
		
		// 指定目标分发协议
		if (HawkOSOperator.isEmptyString(targetId)) {
			HawkTaskManager.getInstance().postProtocol(CrossService.getInstance().getXid(), protocol, 0);
			return;
		}
		
		int threadIndex = Math.abs(targetId.hashCode()) % HawkTaskManager.getInstance().getThreadNum();
		HawkTaskManager.getInstance().postTask(new HawkTask(){			
			@Override
			public Object run() {
				try {
					// 分发给管理器
					boolean result = CrossPlayerProtocolHandler.getInstance().onProtocol(header, protocol);
					if (result) {
						HawkTaskManager.getInstance().postProtocol(HawkXID.valueOf(GsConst.ObjType.PLAYER, header.getTarget()), protocol, 0);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
				return null;
			}
		},  threadIndex);
	}

	/**
	 * 帧更新检测rpc超时
	 * 
	 */
	@Override
	public void onTick() {
		try {
			int timeout = GsConfig.getInstance().getProxyRpcTimeout();
			
			long curTime = HawkTime.getMillisecond();			
			Iterator<Entry<String, Long>> it = stubTimeMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<String, Long> entry = it.next();
				if (timeout > 0 && curTime - entry.getValue() >= timeout) {
					it.remove();
					onRpcTimeout(entry.getKey());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * rpc 超时处理
	 * 
	 * @param rpcId
	 */
	private void onRpcTimeout(String rpcId) {
		CsRpcStub stub = rpcStubCache.getIfPresent(rpcId);
		if (stub != null) {
			rpcStubCache.invalidate(rpcId);
			
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					stub.getCallback().onTimeout(stub);
					return null;
				}
			}, stub.getThreadIdx());
		}
	}
	
	/**
	 * 做动态开启
	 * @return
	 */
	public boolean isInit() {
		return zmqList != null;
	}
}
