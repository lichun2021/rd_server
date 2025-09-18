package com.hawk.match;

import java.util.HashMap;
import java.util.Map;

import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigStorage;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.obj.HawkObjBase;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.hawk.zmq.HawkZmq;
import org.hawk.zmq.HawkZmqManager;

import com.hawk.match.msg.ServerProtocolMsg;
import com.hawk.match.msg.ServerResponseMsg;
import com.hawk.match.service.MatchService;
import com.hawk.match.service.impl.BattleFieldMatch;
import com.hawk.match.util.MatchConst;
import com.hawk.match.util.MatchConst.MatchType;

/**
 * 
 * @author hawk
 *
 */
public class MatchApp extends HawkApp {
	/**
	 * 服务会话
	 */
	private HawkZmq session;
	/**
	 * 时间超时时间
	 */
	private int eventTimeout;

	/**
	 * 战场服务
	 */
	private Map<Integer, MatchService> matchServices;

	/**
	 * 全局静态对象
	 */
	private static MatchApp instance = null;

	/**
	 * 获取全局静态对象
	 * 
	 * @return
	 */
	public static MatchApp getInstance() {
		return instance;
	}

	/**
	 * 构造函数
	 */
	public MatchApp() {
		super(HawkXID.valueOf(MatchConst.ObjType.MANAGER, MatchConst.ObjId.APP));
		if (instance == null) {
			instance = this;
		}

		matchServices = new HashMap<Integer, MatchService>();
	}

	/**
	 * 从配置文件初始化
	 * 
	 * @param cfg
	 * @return
	 */
	public boolean init(String cfg) {
		MatchCfg appCfg = null;
		try {
			HawkConfigStorage cfgStorage = new HawkConfigStorage(MatchCfg.class);
			appCfg = (MatchCfg) cfgStorage.getConfigByIndex(0);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}

		// 初始化参数
		this.eventTimeout = appCfg.getEventTimeout();

		// 设置路径
		if (!HawkOSOperator.installLibPath()) {
			return false;
		}

		// 父类初始化
		if (!super.init(appCfg)) {
			return false;
		}

		// 初始化网络
		if (!initSession()) {
			return false;
		}

		// 添加战场服务
		if (!registerMatchService()) {
			return false;
		}

		return true;
	}

	/**
	 * 初始化服务
	 * 
	 * @return
	 */
	private boolean initSession() {
		if (MatchCfg.getInstance().getServerPort() <= 0) {
			HawkLog.errPrintln("match server port failed, port: {}", MatchCfg.getInstance().getServerPort());
			return false;
		}
		
		session = HawkZmqManager.getInstance().createZmq(HawkZmq.ZmqType.ROUTER);

		// 绑定到服务地址
		String addr = "tcp://*:" + MatchCfg.getInstance().getServerPort();
		if (!session.bind(addr)) {
			HawkLog.errPrintln("init match session failed, addr: {}", addr);
			return false;
		}
		HawkLog.logPrintln("init match session success, addr: {}", addr);
		return true;
	}

	/**
	 * 注册战场服务
	 */
	private boolean registerMatchService() {
		// 创建管理器对象
		createObjMan(MatchConst.ObjType.MANAGER, true).allocObject(getXid(), this);
		
		// 创建玩家管理器
		createObjMan(MatchConst.ObjType.MATCH, true);

		// 跨服战场
		createObj(HawkXID.valueOf(MatchConst.ObjType.MATCH, MatchType.BATTLEFIELD));

		// 初始化
		for (int matchType = MatchType.BATTLEFIELD; matchType < MatchType.TOTAL_COUNT; matchType++) {
			HawkObjBase<HawkXID, HawkAppObj> objBase = queryObject(HawkXID.valueOf(MatchConst.ObjType.MATCH, matchType));
			if (objBase == null) {
				return false;
			}
			
			MatchService service = (MatchService) objBase.getImpl();
			if (!service.init()) {
				HawkLog.errPrintln("match service register failed, xid: {}", objBase.getObjKey());
				return false;
			}
			
			matchServices.put(objBase.getObjKey().getId(), service);
			HawkLog.logPrintln("match service register success, xid: {}", objBase.getObjKey());
		}
		return true;
	}

	/**
	 * 创建应用对象
	 */
	@Override
	protected HawkAppObj onCreateObj(HawkXID xid) {
		HawkAppObj appObj = null;
		if (xid.getType() == MatchConst.ObjType.MATCH) {
			if (xid.getId() == MatchType.BATTLEFIELD) {
				appObj = new BattleFieldMatch(xid);
			}
		}

		if (appObj == null) {
			HawkLog.errPrintln("create obj failed: {}", xid);
		}
		return appObj;
	}
	
	/**
	 * 每帧更新
	 * 
	 * @return
	 */
	@Override
	public boolean onTick() {
		// 更新会话事件
		updateSession();

		return super.onTick();
	}

	/**
	 * 更新会话
	 */
	private boolean updateSession() {
		try {
			if (session.pollEvent(HawkZmq.HZMQ_EVENT_READ, eventTimeout) <= 0) {
				return false;
			}

			// 地址标识
			byte[] serverId = session.recv(0);
			if (serverId == null || serverId.length <= 0 || !session.hasReceiveMore()) {
				return false;
			}

			// 接收请求数据
			HawkProtocol protocol = session.recvProtocol(0);
			if (protocol == null) {
				return false;
			}

			// 处理协议
			processProtocol(new String(serverId, "UTF-8"), protocol);
			
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}

	/**
	 * 处理协议
	 * 
	 * @param serverId
	 * @param protocol
	 */
	private void processProtocol(String serverId, HawkProtocol protocol) {
		// 分发协议到对应的匹配服务上
		for (int matchType = MatchType.BATTLEFIELD; matchType < MatchType.BATTLEFIELD + MatchType.TOTAL_COUNT; matchType++) {
			// 获取匹配服务
			HawkXID xid = HawkXID.valueOf(MatchConst.ObjType.MATCH, matchType);
			MatchService service = (MatchService) queryObject(xid).getImpl();
			
			// 是本服务所监听的协议, 交由本服务进行处理
			if (service.isListenProto(protocol.getType())) {
				HawkTaskManager.getInstance().postMsg(xid, ServerProtocolMsg.valueOf(serverId, protocol));
				break;
			}
		}
	}
	
	/**
	 * 响应服务器请求
	 * 
	 * @param msg
	 * @return
	 */
	@MessageHandler
	public boolean onServerResponse(ServerResponseMsg msg) {		
		// 发送地址
		session.send(msg.getServerId().getBytes(), HawkZmq.HZMQ_SNDMORE);
		
		// 发送协议
		session.sendProtocol(msg.getProtocol(), 0);
		return true;
	}
}
