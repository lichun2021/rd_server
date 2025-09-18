package com.hawk.game.service;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.http.HawkHttpUrlService;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.hawk.common.AccountRoleInfo;
import com.hawk.common.ServerInfo;
import com.hawk.game.GsConfig;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.PushAddrCfg;
import com.hawk.game.config.PushCfg;
import com.hawk.game.config.PushLangZhCNCfg;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerData;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GsConst;

public class PushService extends HawkAppObj {
	/**
	 * 日志记录器
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 设备推送时间缓存
	 */
	private LoadingCache<String, MsgPushInfo> devicePushCache;

	/**
	 * 全局实例对象
	 */
	private static PushService instance = null;

	/**
	 * 获取实例对象
	 *
	 * @return
	 */
	public static PushService getInstance() {
		if (instance.devicePushCache == null) {
			instance.init();
		}
		return instance;
	}

	/**
	 * 构造
	 * 
	 * @param xid
	 */
	public PushService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 消息推送信息
	 */
	class MsgPushInfo {
		/**
		 * 上一次发出推送消息的时间
		 */
		long lastPushTime;
		/**
		 * 等待推送的消息数量
		 */
		int waitPushCount;
		
		public long getLastPushTime() {
			return lastPushTime;
		}
		
		public void setLastPushTime(long lastPushTime) {
			this.lastPushTime = lastPushTime;
		}

		public int getWaitPushCount() {
			return waitPushCount;
		}
		
		public void setWaitPushCount(int waitPushCount) {
			this.waitPushCount = waitPushCount;
		}
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		devicePushCache = CacheBuilder.newBuilder().recordStats().maximumSize(10000)
				.initialCapacity(1000)
				.expireAfterAccess(300000, TimeUnit.MILLISECONDS)
				.build(new CacheLoader<String, MsgPushInfo>() {
					@Override
					public MsgPushInfo load(String device) {
						return new MsgPushInfo();
					}
				});
		return true;
	}

	/**
	 * 推送给指定玩家id的系统消息
	 * 
	 * @param playerId
	 * @param msgId
	 * @return
	 */
	public boolean pushMsg(String playerId, int msgId, String... msgParam) {
		try {
			if (!GsConfig.getInstance().isPushEnable()) {
				return false;
			}
			
			JSONObject json = getPushInfoJson(playerId, msgId);
			if (json == null) {
				return false;
			}
			
			//获取消息内容
			String msg = null;
			PushCfg pushCfg = HawkConfigManager.getInstance().getConfigByKey(PushCfg.class, msgId);
			if (pushCfg == null) {
				logger.error("push msg failed, pushCfg error, playerId: {}, msgId: {}", playerId, msgId);
				return false;
			}
			
			// 判断单组控制开关是否已关闭
			if (isSwitchEnable(playerId, msgId, pushCfg.getGroup())) {
				return false;
			}
			
			long now = HawkApp.getInstance().getCurrentTime();
			// 免打扰机制(此处freeOfControl>0表示受控制，策划定义的字段名有点反常识)
			if (pushCfg.getFreeofcontrol() > 0 && PushCfg.isBlockingEnable()) {
				// 玩家设置的免打扰开关
				String switchVal = getCustomArg(playerId, PushCfg.getBlockingSwitchKey());
				// 没设开关时默认开启（受免打扰时间控制）
				if (switchVal == null || Integer.valueOf(switchVal) != GameConstCfg.getInstance().getPushSwitchCloseValue()) {
					// 当前处于免打扰期间
					if (PushCfg.getBlockingStartTime() < now && now < PushCfg.getBlockingEndTime()) {
						return false;
					}
				}
			} 
			
			if (pushCfg.getFreeofcontrol() > 0 && PushCfg.getPushTimePeriod() > 0) {
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				CustomDataEntity entity = player.getData().getCustomDataEntity(pushCfg.getGroup());
				if (entity != null && now/1000 - entity.getValue() < PushCfg.getPushTimePeriod()) {
					return false;
				} else if (entity == null) {
					entity = player.getData().createCustomDataEntity(pushCfg.getGroup(), 0, "");
				}
				
				entity.setValue((int) (HawkApp.getInstance().getCurrentTime()/1000));
			}
			
			String lang = json.getString("lang");
			if (HawkOSOperator.isEmptyString(lang)) {
				lang = "zh_CN";
			}
			
			switch (lang) {
			case "zh_CN":
				PushLangZhCNCfg langCfg = HawkConfigManager.getInstance().getConfigByKey(PushLangZhCNCfg.class, pushCfg.getText());
				msg = langCfg.getText();
				break;
			}
			
			
			if (!HawkOSOperator.isEmptyString(msg)) {
				String serverId = CrossService.getInstance().getImmigrationPlayerServerId(playerId);
				if (HawkOSOperator.isEmptyString(serverId)) {
					AccountRoleInfo accountRoleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
					if (accountRoleInfo != null) {
						serverId = accountRoleInfo.getServerId();
					} else {
						serverId = GsConfig.getInstance().getServerId();
					}
				}
				
				ServerInfo serverInfo = RedisProxy.getInstance().getServerInfo(serverId);
				@SuppressWarnings("null")
				String finalMsg = msg.replaceAll(" ", "");
				int count = 0;
				if (serverInfo != null) {
					finalMsg = finalMsg.replace("{0}", serverInfo.getName());
					count++;
				}
				
				for (String param : msgParam) {
					finalMsg = finalMsg.replace(String.format("{%s}", count++), param);
				}
				
				json.put("title", getPushTitle());
				json.put("msg", finalMsg);
			}
			
			json.put("msgId", msgId);
			json.put("playerId", playerId);
			json.put("expireTime", pushCfg.getUsefulTime());
			long delayTime = getPushDelayTime(json.getString("pushDeviceId"));
			json.put("delay", delayTime);
			json.put("gameId", GsConfig.getInstance().getGameId());
			json.put("timestamp", (int) (HawkApp.getInstance().getCurrentTime() / 1000));
			pushReport(json);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 发推送信息
	 * 
	 * @param json
	 * @return
	 */
	protected boolean pushReport(JSONObject json) {
		HttpClient httpClient = HawkHttpUrlService.getInstance().getHttpClient();
		if (httpClient == null || !httpClient.isRunning()) {
			HawkLog.logPrintln("xinge push httpClient invalid");
			return false;
		}
		
		String pushAddr = PushAddrCfg.getInstance().getQqPushAddr();
		int server = Integer.parseInt(GsConfig.getInstance().getServerId());
		int area = server / 10000;
		if (area == 1) {
			pushAddr = PushAddrCfg.getInstance().getWxPushAddr();
		}
		
		String pushBody = json.toJSONString();
		if (HawkOSOperator.isEmptyString(pushAddr)) {
			HawkLog.errPrintln("xinge push addr empty, content: {}", pushBody);
			return false;
		}
		
		try {
			Request request = httpClient.POST(pushAddr).content(new StringContentProvider(pushBody));
			request.header("Connection", "close");
			request.header("Content-Type", "application/json");
			request.timeout(3000, TimeUnit.MILLISECONDS);
			
			ContentResponse response = request.send();
			
			if (response != null) {
				HawkLog.logPrintln("xinge push success, content: {}, status: {}, response: {}", 
						pushBody, response.getStatus(), response.getContentAsString());
			} else {
				HawkLog.errPrintln("xinge push failed, content: {}", pushBody);
			}
			
			return true;
			
		} catch (Exception e) {
			HawkLog.errPrintln("xinge push exception, content: {}", pushBody);
			HawkException.catchException(e);
		}
		
		return false;
	}
	
	/**
	 * 判断推送控制开关是否已关闭
	 * @param playerId
	 * @param msgId
	 * @param switchKey
	 * @return
	 */
	private boolean isSwitchEnable(String playerId, int msgId, String switchKey) {
		String switchVal = getCustomArg(playerId, switchKey);
		if (switchVal != null && Integer.valueOf(switchVal) == GameConstCfg.getInstance().getPushSwitchCloseValue()) {
			logger.error("push msg failed, switch closed, playerId: {}, switchKey: {}, msgId: {}", playerId, switchKey, msgId);
			return true;
		}
		
		return false;
	}
	
	/**
	 * 获取custom数据
	 * 
	 * @param playerId
	 * @param key
	 * @return
	 */
	private String getCustomArg(String playerId,  String key) {
		PlayerData playerData = GlobalData.getInstance().getPlayerData(playerId, false);
		if (playerData == null) {
			playerData = GlobalData.getInstance().getPlayerData(playerId, true);
		}
		
		if (playerData == null) {
			return null;
		}
		
		CustomDataEntity entity = playerData.getCustomDataEntity(key);
		if (entity == null || HawkOSOperator.isEmptyString(entity.getArg())) {
			return null;
		}
		
		return entity.getArg();
	}

	/**
	 * 获取发送对象相关信息
	 * @param playerId
	 * @param msg
	 * @return
	 */
	private JSONObject getPushInfoJson(String playerId, int msgId) {
		Player player = GlobalData.getInstance().getActivePlayer(playerId);
		
		// 在线且在前台不推送消息
		if (player != null && player.isActiveOnline() && !player.isBackground()) {
			logger.debug("push msg failed, player is online, playerId: {}, msgId: {}", playerId, msgId);
			return null;
		}

		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
		if (accountInfo == null) {
			logger.error("push msg failed, player account null, playerId: {}, msgId: {}", playerId, msgId);
			return null;
		}
		
		// 离线时长超出pushEffectiveTime后不再推送
		PushCfg pushCfg = HawkConfigManager.getInstance().getConfigByKey(PushCfg.class, msgId);
		if (pushCfg != null && pushCfg.getNotControlledByTime() == 0) {
			if (HawkTime.getMillisecond() - accountInfo.getLogoutTime() >= ConstProperty.getInstance().getPushEffectiveTime() * 1000L) {
				logger.debug("push msg failed, player offline too long, playerId: {}, msgId: {}", playerId, msgId);
				return null;
			}
		}

		// 获取推送信息
		String info = getCustomArg(playerId, GsConst.PUSH_INFO_KEY);
		if (HawkOSOperator.isEmptyString(info)) {
			logger.error("push msg failed, push info is null, playerId: {}, msgId: {}", playerId, msgId);
			return null;
		}

		JSONObject json = JSONObject.parseObject(info);
		if (json != null && HawkOSOperator.isEmptyString(json.getString("pushDeviceId"))) {
			logger.error("push msg failed, pushDeviceId is null, playerId: {}, msgId: {}", playerId, msgId);
			return null;
		}
		
		return json;
	}
	
	/**
	 * 判断一条消息是否要延迟推送
	 * @param device
	 */
	private long getPushDelayTime(String device) {
		try {
			MsgPushInfo msgPushInfo = devicePushCache.getIfPresent(device);
			if (msgPushInfo == null) {
				msgPushInfo = devicePushCache.get(device);
				msgPushInfo.setLastPushTime(HawkTime.getMillisecond());
				return 0;
			}
			
			long now = HawkTime.getMillisecond();
			if (now - msgPushInfo.getLastPushTime() < 500) {
				msgPushInfo.setLastPushTime(msgPushInfo.getLastPushTime() + 500);
				msgPushInfo.setWaitPushCount(msgPushInfo.getWaitPushCount() + 1);
				return msgPushInfo.getLastPushTime() - now;
			} 
			
			msgPushInfo.setLastPushTime(now);
			msgPushInfo.setWaitPushCount(0);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	/**
	 * 获取推送标题
	 * @return
	 */
	private String getPushTitle() {
		PushLangZhCNCfg lanCfg = HawkConfigManager.getInstance().getConfigByKey(PushLangZhCNCfg.class, GsConst.LanguageKeyPrefix.pushTitle);
		if(lanCfg == null) {
			return "RedAlert-OL";
		}
		return lanCfg.getText();
	}
}
