package com.hawk.game.crossproxy;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.GsConfig;
import com.hawk.game.global.RedisProxy;

public class ProxyHelper {
	/**
	 * 心跳周期
	 */
	public static final int HEART_BEAT_PERIOD = 3000;
	/**
	 * 协议过期时间
	 */
	public static final int PROTOCOL_EXPIRE = 10000;
	/**
	 * 最大空闲周期周期
	 */
	public static final int MASTER_CHECK_PERIOD = 10000;
	/**
	 * 自动解锁时间
	 */
	public static final int AUTO_UNLOCK_PERIOD = 120000;
	
	/**
	 * 检测主服务器
	 */
	public static void contendMasterServer(boolean aliveCheck) {
		String proxyKey = String.format("csproxy:%s", GsConfig.getInstance().getAreaId());
		try {
			// 起服时, 不存在主服即注册自己为主服
			if (!aliveCheck) {
				do {
					String masterServer = getMasterServer();
					// 已经有master了
					if (!HawkOSOperator.isEmptyString(masterServer)) {
						break;
					}
					
					// 没有拿到锁
					if (!waitCsProxyLock(proxyKey)) {
						break;
					}
					
					try {
						masterServer = getMasterServer();						
						if (HawkOSOperator.isEmptyString(masterServer)) {
							// 主服务器信息
							RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "master", GsConfig.getInstance().getServerId());
							RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "heartbeat", String.valueOf(HawkTime.getMillisecond()));
							
							HawkLog.logPrintln("csproxy contend master server: {}  old master server is null", GsConfig.getInstance().getServerId());
						}
					} finally {
						freeCsProxyLock(proxyKey);
					}
					
				} while (false);
				
				return;
			}
			
			// 是主服务器, 更新心跳
			if (isMasterServer()) {
				RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "heartbeat", String.valueOf(HawkTime.getMillisecond()));
				return;
			}
			
			// 主服务器存活情况下不处理(后面开始竞争锁, 进行自注册)
			if (isMasterAlive()) {
				return;
			}
			
			// 发现master不存活, 抢锁, 注册自己
			if (!waitCsProxyLock(proxyKey)) {
				return;
			}
			
			try {
				// double check
				if (isMasterAlive()) {
					return;
				}
				
				//老的主服信息.
				String oldMasterServer = RedisProxy.getInstance().getRedisSession().hGet(proxyKey, "master");
				// 注册自己为主服务器
				RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "master", GsConfig.getInstance().getServerId());
				RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "heartbeat", String.valueOf(HawkTime.getMillisecond()));
				
				HawkLog.logPrintln("csproxy contend master server: {} old master server:{}", GsConfig.getInstance().getServerId(), oldMasterServer);
				
			} finally {
				freeCsProxyLock(proxyKey);
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 等待跨服锁
	 * 
	 * @param proxyKey
	 * @return
	 */
	private static boolean waitCsProxyLock(String proxyKey) {
		// 等待操作锁
		try {

			long curTime = HawkTime.getMillisecond();
			long lock = RedisProxy.getInstance().getRedisSession().hSetNx(proxyKey, "lock", String.valueOf(curTime));
			if (lock > 0) {
				return true;
			}
			
			String val = RedisProxy.getInstance().getRedisSession().hGet(proxyKey, "lock");
			if (!HawkOSOperator.isEmptyString(val)) {
				long lastTime = Long.valueOf(val);
				if (curTime - lastTime > AUTO_UNLOCK_PERIOD) {
					RedisProxy.getInstance().getRedisSession().hDel(proxyKey, "lock");
				}
			}									
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;
	}
	
	/**
	 * 释放跨服锁
	 */
	private static void freeCsProxyLock(String proxyKey) {
		RedisProxy.getInstance().getRedisSession().hDel(proxyKey, "lock");
	}
	
	/**
	 * 获取当前节点地址
	 * 
	 * @return
	 */
	public static String getProxyAddress() {
		String proxyKey = String.format("csproxy:%s", GsConfig.getInstance().getAreaId());
		return RedisProxy.getInstance().getRedisSession().hGet(proxyKey, "address");
	}
	
	/**
	 * 获取主服务器id
	 * 
	 * @return
	 */
	public static String getMasterServer() {
		String proxyKey = String.format("csproxy:%s", GsConfig.getInstance().getAreaId());
		String serverId = RedisProxy.getInstance().getRedisSession().hGet(proxyKey, "master");
		return serverId;
	}

	/**
	 * 注册当前节点
	 * 
	 * @param address
	 */
	public static void registerProxyNode(String address) {
		String proxyKey = String.format("csproxy:%s", GsConfig.getInstance().getAreaId());
		RedisProxy.getInstance().getRedisSession().hSet(proxyKey, "address", address);
	}
	
	/**
	 * 是否为主服务器
	 * 
	 * @return
	 */
	public static boolean isMasterServer() {
		String serverId = getMasterServer();
		if (HawkOSOperator.isEmptyString(serverId)) {
			return false;
		}
		return serverId.equals(GsConfig.getInstance().getServerId());
	}
	
	/**
	 * 主服务器是否存活
	 * 
	 * @return
	 */
	public static boolean isMasterAlive() {
		try {
			String proxyKey = String.format("csproxy:%s", GsConfig.getInstance().getAreaId());
			String value = RedisProxy.getInstance().getRedisSession().hGet(proxyKey, "heartbeat");
			if (HawkOSOperator.isEmptyString(value)) {
				return false;
			}
			
			long heartbeatTime = Long.valueOf(value);
			return HawkTime.getMillisecond() - heartbeatTime <= MASTER_CHECK_PERIOD;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return false;		
	}
}
