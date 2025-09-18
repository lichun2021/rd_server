package com.hawk.gamelog;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigStorage;
import org.hawk.db.HawkDBManager;
import org.hawk.db.mysql.HawkMysqlSession;
import org.hawk.log.HawkLog;
import org.hawk.net.udp.HawkUdpClient;
import org.hawk.net.udp.HawkUdpHandler;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkTickable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.log.LogConst.LogInfoType;
import com.hawk.sdk.config.TencentCfg;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;

public class GameLog extends HawkTickable {
	/**
	 * 日志打印服务对象
	 */
	static final Logger logger = LoggerFactory.getLogger("Tlog");
	/**
	 * udp客户端
	 */
	private HawkUdpClient client;
	/**
	 * mysql会话
	 */
	private HawkMysqlSession mysqlSession;
	/**
	 * tlog表结构信息
	 */
	private HawkConfigStorage logTableCfg;
	/**
	 * 失败的在线表数据
	 */
	private Queue<String> failedOnlineSql;
	/**
	 * 上报的tlog条数统计
	 */
	private AtomicLong tlogCount;
	/**
	 * 上报的tlog日志大小统计
	 */
	private AtomicLong tlogSize;
	
	/**
	 * 单例对象
	 */
	private static GameLog instance;
	
	public static GameLog getInstance() {
		if (instance == null) {
			instance = new GameLog();
		}
		return instance;
	}
	
	private GameLog() {
		tlogCount = new AtomicLong(0);
		tlogSize = new AtomicLong(0);
		HawkApp.getInstance().addTickable(this);
	}
	
	/**
	 * 重新加载配置文件
	 * 
	 * @return
	 */
	public final boolean reloadConfig() {
		if (logTableCfg == null || !logTableCfg.checkUpdate()) {
			return false;
		}
		
		try {
			HawkConfigStorage logTableCfgNew = new HawkConfigStorage(LogTableCfg.class);
			logTableCfg = logTableCfgNew;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return true;
	}
	
	/**
	 * 初始化
	 */
	public boolean init() {
		try {
			logTableCfg = new HawkConfigStorage(LogTableCfg.class);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		for (LogInfoType type : LogInfoType.values()) {
			if (!LogTableCfg.isExist(type.name())) {
				HawkLog.logPrintln("tlog init failed, logType not exist: {}", type.name());
				return false;
			}
		}
		
		failedOnlineSql = new LinkedBlockingQueue<String>();
		
		TencentCfg tencentCfg = TencentCfg.getInstance();
		String dbUrl = tencentCfg.getTlogDbConnUrl();
		String dbUser = tencentCfg.getTlogDbUserName();
		String dbPwd = tencentCfg.getTlogDbPassWord();
		
		// 实时在线的mysql配置
		if(!HawkOSOperator.isEmptyString(dbUrl)) {
			mysqlSession = new HawkMysqlSession();
			if(!mysqlSession.init(dbUrl, dbUser, dbPwd)) {
				return false;
			}
			
			if (mysqlSession.getConnection() == null) {
				return false;
			}
		}
		
		// tlog服务
		if(!HawkOSOperator.isEmptyString(tencentCfg.getTlogSvrIp()) && tencentCfg.getTlogSvrPort() > 0) {
			try {
				client = new HawkUdpClient();
				client.init(new HawkUdpHandler() {
					@Override
					public void onReceive(ChannelHandlerContext context, DatagramPacket packet) {
						
					}
				}, new InetSocketAddress(tencentCfg.getTlogSvrIp(), tencentCfg.getTlogSvrPort()));
			} catch(Exception e) {
				HawkException.catchException(e);
				return false;
			}
		}
		
		HawkLog.logPrintln("tlog init success....");
		return true;
	}
	
	/**
	 * 帧更新
	 */
	@Override
	public void onTick() {
		if(mysqlSession != null) {
			mysqlSession.onTick();
		}
	}
	
	/**
	 * 关闭日志传输对象
	 */
	public void close() {
		if (client != null) {
			client.close();
			client = null;
		}
	}
	
	/**
	 * 获取TLOG日志表结构
	 * 
	 * @param logType
	 * @return
	 */
	public LogTableCfg getLogTable(String logType) {
		return (LogTableCfg) logTableCfg.getConfigByKey(logType);
	}
	
	public long getTlogCount() {
		return tlogCount.get();
	}
	
	public long getTlogSize() {
		return tlogSize.get();
	}
	
	/**
	 * 记录日志信息
	 */
	public void info(LogParam logParam) {
		try {
			String logType = logParam.get("logType");
			LogInfoType logInfoType = LogInfoType.valueOf(logType);
			if (logInfoType == null) {
				return;
			}
			
			// 实时在线数据记录
			if(logInfoType == LogInfoType.onlineInfo){
				insertOnlineInfo(logParam);
				return;
			}
			
			String logInfo = logParam.joinToString();
			if(logInfo == null) {
				return;
			}
			
			if(client != null) {
				client.send(logInfo + "\n", null);
			}
			
			logger.info(logInfo);

			tlogCount.addAndGet(1);
			tlogSize.addAndGet(logInfo.getBytes().length);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * db存储实时在线人数信息
	 * @param logParam
	 * @throws Exception
	 */
	private void insertOnlineInfo(LogParam logParam) throws Exception {
		String gameappid = logParam.get("gameappid");
		int timekey = logParam.get("timekey");
		String gsid = logParam.get("gsid");
		String zoneArea = logParam.get("zoneareaid");
		int onlinecntios = logParam.get("onlinecntios");
		int onlinecntandroid = logParam.get("onlinecntandroid");
		int registerNum = logParam.get("registernum");
		int queuesize = logParam.get("queuesize");
		
		// 日志记录
		logger.debug(String.format("ServerOnline|%s|%d|%s|%s|%d|%d|%d|%d", 
				gameappid, timekey, gsid, zoneArea, onlinecntios, onlinecntandroid, registerNum, queuesize));
		
		int zoneareaid = Integer.valueOf(zoneArea);
		if(mysqlSession == null || HawkOSOperator.isEmptyString(zoneArea)) {
			return;
		}
		
		// 存库的sql构建
		final String sql = String.format("INSERT INTO %s(gameappid, timekey, gsid, zoneareaid, onlinecntios, onlinecntandroid, registernum, queuesize) "
				+ "VALUES ('%s', %s, '%s', %s, %s, %s, %s, %s)", 
				TencentCfg.getInstance().getTlogTbName(), gameappid, timekey, gsid, zoneareaid, onlinecntios, onlinecntandroid, registerNum, queuesize);
		
		// 构建任务进行写入
		HawkDBManager.getInstance().getThreadPool().addTask(new HawkTask() {
			@Override
			public Object run() {
				insertOnlineSql(sql);
				return true;
			}
		}, 0, false);
	}
	
	private void insertOnlineSql(String sql) {
		try {
			// mysql进行db存储
			if (!mysqlSession.getConnection().isValid()) {
				// 添加到失败队列
				failedOnlineSql.add(sql);
				// 状态信息切换log
				HawkLog.errPrintln("gamelog sql change to exception, cacheSql: {}", sql);
				return;
			}
			
			Object retObj = mysqlSession.executeInsert(sql, null, 0);
			
			// 容灾处理
			if (retObj != null && !failedOnlineSql.isEmpty()) {
				Iterator<String> it = failedOnlineSql.iterator();
		        while(it.hasNext()){
		        	String failedSql = it.next();
		        	mysqlSession.executeInsert(failedSql, null, 0);
		        	it.remove();
		        	
		        	// 日志记录
		        	HawkLog.errPrintln("gamelog sql recover from exception, compensateSql: {}", failedSql);
		        }
			}
		} catch (Exception e) {
			// 添加到失败队列
			failedOnlineSql.add(sql);
			
			// 状态信息切换log
			HawkLog.errPrintln("gamelog sql change to exception, cacheSql: {}", sql);
			
			HawkException.catchException(e);
		}	
	}
}
