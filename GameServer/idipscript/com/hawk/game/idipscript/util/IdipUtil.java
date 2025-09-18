package com.hawk.game.idipscript.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.net.URLCodec;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.controler.SystemControler;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.player.Player;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.log.LogConst.Platform;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;

/**
 * IDIP工具类
 * 
 * @author lating
 *
 */
public class IdipUtil {

	/**
	 * 排行榜单查询返回数据条目数量
	 */
	public static final int RANK_RECORD_COUNT = 50;
	/**
	 * 开关：0代表关， 1代表开
	 */
	public static class Switch {
		public static final int ON = 1;
		public static final int OFF = 0;
	}
	
	/**
	 * 货币类型：0代表金币，1代表钻石
	 */
	public static class MoneyType {
		public static final int GOLD = 0;
		public static final int DIAMOND = 1;
	}
	
	/**
	 * 邮件奖励类型：货币1，物品2
	 */
	public static class MailAwardItemType {
		public static final int MONEY = 1;
		public static final int ITEM = 2;
	}
	
	/**
	 * 账号校验
	 * @return
	 */
	public static Player checkAccountAndPlayer(JSONObject request, IdipResult result) {
		return checkAccountAndPlayer(request, result, true);
	}
	
	/**
	 * 账号校验
	 * @param request
	 * @param result
	 * @param mainCheck true表示按原始区服取不到角色情况下，通过主服id去取
	 * @return
	 */
	public static Player checkAccountAndPlayer(JSONObject request, IdipResult result, boolean mainCheck) {
		AccountInfo accountInfo = accounCheck(request, result, mainCheck);
		if (accountInfo == null) {
			return null;
		}
		
		String playerId = accountInfo.getPlayerId();
		Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
		if (player == null) {
			result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getBody().put("RetMsg", "role not found, roleId: " + playerId);
			return null;
		}
		
		return player;
	}
	
	/**
	 * 账号校验
	 * @param request
	 * @param result
	 * @return
	 */
	public static AccountInfo accounCheck(JSONObject request, IdipResult result, boolean mainCheck) {
		int platId = request.getJSONObject("body").getIntValue("PlatId");
		String platform = platId == Platform.ANDROID.intVal() ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
		String puid = request.getJSONObject("body").getString("OpenId");
		puid = GameUtil.getPuidByPlatform(puid, platform);
		String serverId = request.getJSONObject("body").getString("Partition");
		if (HawkOSOperator.isEmptyString(serverId) || serverId.equals("0")) {
			serverId = GsConfig.getInstance().getServerId();
		}
		
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo == null) {
			if (mainCheck) {
				accountInfo = getAccountInfoFromMergeServer(puid, serverId);
				if (accountInfo != null) {
					return accountInfo;
				}
			}
			
			result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getBody().put("RetMsg", "account not found, openId: " + request.getJSONObject("body").getString("OpenId"));
			return null;
		}
		
		return accountInfo;
	}
	
	/**
	 * 获取 AccountInfo 信息
	 * @param openid
	 * @param request
	 * @param result
	 * @param mainCheck true表示按原始区服取不到角色情况下，通过主服id去取
	 * @return
	 */
	public static AccountInfo accounCheck(String openid, JSONObject request, IdipResult result, boolean mainCheck) {
		int platId = request.getJSONObject("body").getIntValue("PlatId");
		String platform = platId == Platform.ANDROID.intVal() ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
		String puid = GameUtil.getPuidByPlatform(openid, platform);
		String serverId = request.getJSONObject("body").getString("Partition");
		if (HawkOSOperator.isEmptyString(serverId) || serverId.equals("0")) {
			serverId = GsConfig.getInstance().getServerId();
		}
		
		AccountInfo accountInfo = GlobalData.getInstance().getAccountInfo(puid, serverId);
		if (accountInfo == null) {
			if (mainCheck) {
				accountInfo = getAccountInfoFromMergeServer(puid, serverId);
				if (accountInfo != null) {
					return accountInfo;
				}
			}
			
			result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getBody().put("RetMsg", "account not found, openId: " + request.getJSONObject("body").getString("OpenId"));
			return null;
		}
		
		return accountInfo;
	}
	
	/**
	 * 从合服整体中获取accounInfo信息
	 * 
	 * @param puid
	 * @param serverId
	 * @return
	 */
	private static AccountInfo getAccountInfoFromMergeServer(String puid, String serverId) {
		List<String> mergeServerList = GlobalData.getInstance().getMergeServerList(serverId);
		if (mergeServerList == null || mergeServerList.isEmpty()) {
			return null;
		}
		
		AccountInfo accountInfo = null;
		for (String mergeServerId : mergeServerList) {
			accountInfo = GlobalData.getInstance().getAccountInfo(puid, mergeServerId);
			if (accountInfo != null) {
				return accountInfo;
			}
		}
		
		return null;
	}
	
	/**
	 * 账号校验
	 * @param request
	 * @param result
	 * @return
	 */
	public static Player playerCheck(JSONObject request, IdipResult result) {
		return playerCheck(request, result, true);
	}
	
	/**
	 * 账号校验
	 * @param request
	 * @param result
	 * @param mainCheck true表示按原始区服取不到角色情况下，通过主服id去取
	 * @return
	 */
	public static Player playerCheck(JSONObject request, IdipResult result, boolean mainCheck) {
		String playerId = request.getJSONObject("body").getString("RoleId");
		String roleId = playerId;
		if (HawkOSOperator.isEmptyString(playerId)) {
			AccountInfo accountInfo = accounCheck(request, result, mainCheck);
			if (accountInfo == null) {
				return null;
			}
			
			playerId = accountInfo.getPlayerId();
		}
		
		Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
		if (player == null) {
			result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getBody().put("RetMsg", "role not found, roleId: " + playerId);
			return null;
		}
		
		// 判断参数是否匹配
		if (!HawkOSOperator.isEmptyString(roleId)) {
			String openid = request.getJSONObject("body").getString("OpenId");
			if (!HawkOSOperator.isEmptyString(openid) && !openid.equals(player.getOpenId())) {
				result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
				result.getBody().put("RetMsg", "RoleId not match the OpenId");
				return null;
			}
			
			if (request.getJSONObject("body").containsKey("PlatId")) {
				int platId = request.getJSONObject("body").getIntValue("PlatId");
				String platform = platId == Platform.ANDROID.intVal() ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
				if (!player.getPlatform().equals(platform)) {
					result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
					result.getBody().put("RetMsg", "RoleId not match the PlatId account role");
					return null;
				}
			}
		}
		
		return player;
	}
	
	/**
	 * 账号校验
	 * @param request
	 * @param result
	 * @param mainCheck true表示按原始区服取不到角色情况下，通过主服id去取
	 * @return
	 */
	public static Player playerCheck(String openid, JSONObject request, IdipResult result, boolean mainCheck) {
		String playerId = request.getJSONObject("body").getString("RoleId");
		String roleId = playerId;
		if (HawkOSOperator.isEmptyString(playerId)) {
			AccountInfo accountInfo = accounCheck(openid, request, result, mainCheck);
			if (accountInfo == null) {
				return null;
			}
			
			playerId = accountInfo.getPlayerId();
		}
		
		Player player = GlobalData.getInstance().scriptMakesurePlayer(playerId);
		if (player == null) {
			result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
			result.getBody().put("RetMsg", "role not found, roleId: " + playerId);
			return null;
		}
		
		// 判断参数是否匹配
		if (!HawkOSOperator.isEmptyString(roleId)) {
			if (!HawkOSOperator.isEmptyString(openid) && !openid.equals(player.getOpenId())) {
				result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
				result.getBody().put("RetMsg", "RoleId not match the OpenId");
				return null;
			}
			
			if (request.getJSONObject("body").containsKey("PlatId")) {
				int platId = request.getJSONObject("body").getIntValue("PlatId");
				String platform = platId == Platform.ANDROID.intVal() ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
				if (!player.getPlatform().equals(platform)) {
					result.getBody().put("Result", IdipConst.SysError.ACCOUNT_NOT_FOUND);
					result.getBody().put("RetMsg", "RoleId not match the PlatId account role");
					return null;
				}
			}
		}
		
		return player;
	}
	
	/**
	 * URLDecoder 解码
	 * @param content
	 * @return
	 */
	public static String urlDecode(String content) {
		try {
			content = URLDecoder.decode(content, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		
		return content;
	}
	
	/**
	 * url codec解码
	 * @param content
	 * @return
	 */
	public static String urlCodecDecode(String content) {
		try {
			byte[] bytes = URLCodec.decodeUrl(content.getBytes());
			content = new String(bytes, "UTF-8");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return content;
	}
	
	/**
	 * 字符内容解码
	 * 
	 * @param content
	 * @return
	 */
	public static String decode(String content) {
		try {
			content = new String(content.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			HawkException.catchException(e);
		}
		
		return content;
	}
	
	/**
	 * 字符编码
	 * @param content
	 * @return
	 */
	public static String encode(String content) {
		try {
			content = URLEncoder.encode(content, "UTF-8");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return content;
	}
	
	/**
	 * 系统开关控制
	 * @param switchVal
	 * @param systemType
	 * @param systemTypeItem
	 */
	public static void systemSwitchControl(int switchVal, ControlerModule module, int... systemTypeItem) {
		int systemItem = systemTypeItem.length > 0 ? systemTypeItem[0] : 0;
		if (switchVal == Switch.OFF) {
			LocalRedis.getInstance().pushClosedSystemItem(module.value(), String.valueOf(systemItem));
			SystemControler.getInstance().closeSystemItem(module, systemItem);
		} else {
			LocalRedis.getInstance().removeClosedSystemItem(module.value(), String.valueOf(systemItem));
			SystemControler.getInstance().openClosedSystemItem(module, systemItem);
		}
	}
	
	/**
	 * 等待异步处理
	 * 
	 * @param checkResult
	 */
	public static void wait4AsyncProccess(IdipResult result, AtomicInteger checkResult, String errMsg) {
		int count = 0;
		do {
			HawkOSOperator.osSleep(10L);
			count++;
		} while (checkResult.get() < 0 && count < 5);
		
		if (checkResult.get() > 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", errMsg);
		} else {
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
		}
	}
}
