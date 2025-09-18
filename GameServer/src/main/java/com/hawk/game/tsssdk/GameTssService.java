package com.hawk.game.tsssdk;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkThreadPool;
import org.hawk.util.HawkClassScaner;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.tsssdk.invoker.TsssdkInvoker;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.sdk.config.PlatformConstCfg;
import com.hawk.tsssdk.manager.TssSdkManager;
import com.hawk.tsssdk.util.UicChatDataInfo;
import com.hawk.tsssdk.util.UicNameDataInfo;

import tsssdk.jni.TssAccountPlatId;
import tsssdk.jni.TssAccountType;

/**
 * 安全sdk接入服务
 * 
 * @author lating
 *
 */
public class GameTssService {

	/**
	 * 实例
	 */
	private static GameTssService instance;

	/**
	 * 任务
	 */
	private static Map<Integer, TsssdkInvoker> sceneInvokers;
	
	/**
	 * 构造
	 */
	private GameTssService() {
		
	}

	/**
	 * 获取实例
	 * @return
	 */
	public static GameTssService getInstance() {
		if (instance == null) {
			instance = new GameTssService();
		}
		
		return instance;
	}

	/**
	 * 获取任务
	 * @param missionType
	 * @return
	 */
	public TsssdkInvoker getInvoker(int scene) {
		return sceneInvokers.get(scene);
	}
	
	public void addInvoker(Class<? extends TsssdkInvoker> cls){
		if (cls.isInterface()) {
			return;
		}
		
		try {
			sceneInvokers.put(cls.getAnnotation(Category.class).scene(), (TsssdkInvoker) cls.newInstance());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 初始化
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		sceneInvokers = new HashMap<Integer, TsssdkInvoker>();
		String packageName = TsssdkInvoker.class.getPackage().getName();
		List<Class<?>> classList = HawkClassScaner.scanClassesFilter(packageName, Category.class);
		for (Class<?> cls : classList) {
			if (!TsssdkInvoker.class.isAssignableFrom(cls)) {
				continue;
			}
			addInvoker((Class<? extends TsssdkInvoker>) cls);
		}
	}
	
	/**
	 * 回调
	 * @param player
	 * @param category
	 * @param protocol
	 * @param content
	 * @param callback
	 */
	private void categoryInvoke(Player player, int category, int protocol, String content, String callback) {
		TsssdkInvoker invoker = GameTssService.getInstance().getInvoker(category);
		if (invoker == null) {
			HawkLog.errPrintln("GameTssService tsssdk invoke failed, scene: {}", category);
			return;
		}
		
		invoker.invoke(player, 0, content, protocol, callback);
	}
	
	/**
	 * 敏感词过滤
	 * 
	 * @param player 
	 * @param content 过滤内容
	 * @param reportCategory 上报的场景
	 * @param innerCategory 内部处理场景
	 * @param callback 透传数据（回调时用）
	 * @param gameDataJson sdk上报的游戏数据
	 * @param protocol 对应的处理协议
	 * @return
	 */
	public String wordUicChatFilter(Player player, String content, int reportCategory, int innerCategory,
			String callback, JSONObject gameDataJson, int protocol) {
		try {
			if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
				categoryInvoke(player, innerCategory, protocol, content, callback);
				return GsApp.getInstance().getWordFilter().filterWord(content);
			}
			
			String gameData = "";
			if (gameDataJson == null) {
				gameData = getTssReportGameData(player).toString();
			} else {
				StringBuilder sb = getTssReportGameData(player);
				for (String key : gameDataJson.keySet()) {
					sb.append("&").append(key).append("=").append(gameDataJson.get(key));
				}
				gameData = sb.toString();
			}
			
			return wordUicChatFilter(player, content, reportCategory, innerCategory, callback, protocol, gameData);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return content;
	}
	
	/**
	 * 敏感词过滤
	 * 
	 * @param player 
	 * @param content 过滤内容
	 * @param reportCategory 上报的场景
	 * @param innerCategory 内部处理场景
	 * @param callback 透传数据（回调时用）
	 * @param gameData sdk上报的游戏数据
	 * @param protocol 对应的处理协议
	 * @return
	 */
	public String wordUicChatFilter(Player player, String content, int reportCategory, int innerCategory,
			String callback, int protocol, String gameData) {
		try {
			if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
				categoryInvoke(player, innerCategory, protocol, content, callback);
				return GsApp.getInstance().getWordFilter().filterWord(content);
			}
			
			return wordUicChatFilter(player, content, reportCategory, innerCategory, callback, protocol, gameData, player.getNameEncoded());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return content;
	}
	
	/**
	 * 敏感词过滤
	 * 
	 * @param player 
	 * @param content 过滤内容
	 * @param reportCategory 上报的场景
	 * @param innerCategory 内部处理场景
	 * @param callback 透传数据（回调时用）
	 * @param gameData sdk上报的游戏数据
	 * @param protocol 对应的处理协议
	 * @return
	 */
	public String wordUicChatFilter(Player player, String content, int reportCategory, int innerCategory,
			String callback, int protocol, String gameData, String playerName) {
		try {
			if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
				categoryInvoke(player, innerCategory, protocol, content, callback);
				return GsApp.getInstance().getWordFilter().filterWord(content);
			}
			
			JSONObject json = new JSONObject();
			json.put("playerId", player.getId());
			json.put("msgId", innerCategory);
			json.put("callbackData", callback);
			json.put("protocol", protocol);
			String callbackData = json.toJSONString();

			int worldId = GameUtil.getServerId();
			long clientIP = HawkOSOperator.isEmptyString(player.getClientIp()) ? 0 : GameUtil.ipToNumber(player.getClientIp());
			int clientIpInt = clientIP > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)clientIP;
			
			TssAccountType accountType = "wx".equalsIgnoreCase(player.getChannel()) ? TssAccountType.TSSACCOUNT_TYPE_WECHAT : TssAccountType.TSSACCOUNT_TYPE_QQ;
			TssAccountPlatId platId = GameUtil.isAndroidAccount(player) ? TssAccountPlatId.TSSPLAT_ID_ANDROID : TssAccountPlatId.TSSPLAT_ID_IOS;
			
			UicChatDataInfo dataInfo = UicChatDataInfo.newBuilder()
			        .setAccountType(accountType)
			        .setPlatId(platId)
			        .setOpenid(player.getOpenId())
			        .setWorldId(worldId)
			        .setMsgCategory(reportCategory)
			        .setChannelId(0)
			        .setClientIp(clientIpInt)
			        .setRoleId(0)
			        .setRoleLevel(player.getLevel())
			        .setRoleName(playerName)
			        .setCharacNo(player.getId())
			        .setMsg(content)
			        .setDoorLevel(GsConst.UIC_DOOR_LEVEL)
			        .setCallbackData(callbackData.getBytes())
			        .setGameData(gameData != null ? gameData.getBytes() : null)
			        .build();
			       
			TssSdkManager.getInstance().uicAddMessage(dataInfo);

			return "";
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return content;
	}
	
	private StringBuilder getTssReportGameData(Player player) {
		String pfIconPrimitive = player.getData().getPrimitivePfIcon();
		if (GlobalData.getInstance().isBanPortraitAccount(player.getOpenId())) {
			pfIconPrimitive = PlatformConstCfg.getInstance().getImage_def();
		}
		
		StringBuilder builder = new StringBuilder();
		builder.append("client_version=").append(player.getAppVersion())
		       .append("&user_ip=").append(player.getClientIp())
		       .append("&role_battlepoint=").append(player.getPower())
		       .append("&role_head_url=").append(pfIconPrimitive)
		       .append("&role_group_id=").append(player.getGuildId())
		       .append("&role_group_name=").append(player.getGuildName())
		       .append("&role_total_cash=").append(player.getRechargeTotal())
		       .append("&role_vip_level=").append(player.getVipLevel())
		       .append("&charac_no=").append(player.getId());
		return builder;
	}
	
	/**
	 * 文字内容敏感词校验（name类型的用户输入不需要替换敏感词，检测不通过时直接返回）
	 * 
	 * @param content 文字内容
	 * @return
	 */
	public void wordUicNameCheck(String content, HawkCallback callback, int threadIndex) {
		UicNameDataInfo dataInfo = new UicNameDataInfo(content, GsConst.UIC_DOOR_LEVEL, GsConst.UIC_SENSITIVE_REPLACE);

		if (HawkOSOperator.isEmptyString(content)) {
			callback.invoke(dataInfo);
			return;
		}

		try {
			// 先判断，是否可以通过本地敏感词库过滤，本地通过了才调用uic接口进一步检测
			if (GsApp.getInstance().getWordFilter().hasWord(content)) {
				dataInfo.msg_result_flag = 1;
				callback.invoke(dataInfo);
				return;
			}

			if (!GsConfig.getInstance().isTssSdkEnable() || !GsConfig.getInstance().isTssSdkUicEnable()) {
				callback.invoke(dataInfo);
				return;
			}

			// 线程的监控状态
			if (!TssSdkManager.getInstance().checkAlive()) {
				HawkLog.errPrintln("tssSdk alive check failed");
				callback.invoke(dataInfo);
				return;
			}

			if (threadIndex < 0) {
				HawkThreadPool taskExecutor = HawkTaskManager.getInstance().getTaskExecutor();
				threadIndex = taskExecutor.getThreadIndex(HawkOSOperator.getThreadId());
				if (threadIndex < 0) {
					throw new RuntimeException("uicNameCheck must in taskExecutor");
				}
			}

			TssSdkManager.getInstance().uicNameCheck(dataInfo, callback, threadIndex);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
}
