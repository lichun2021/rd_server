package com.hawk.game.data;

import org.hawk.os.HawkOSOperator;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.log.LogConst.Platform;

/**
 * 玩家转平台信息
 * 
 * @author lating
 *
 */
public class PlatTransferInfo {
	/**
	 * 角色id
	 */
	private String playerId;
	/**
	 * 最初的平台信息（这个就是创角的时候确定的，之后永远不会变）
	 */
	private String bornPlatorm;
	/**
	 * 转换之后的平台信息
	 */
	private String transferTo;
	/**
	 * 出生服信息
	 */
	private String bornServerId;
	/**
	 * 转换的时间
	 */
	private int time;
	/**
	 * 转换后首次登录的时间
	 */ 
	private int loginTime;
	
	public PlatTransferInfo() {
		
	}
	
	public PlatTransferInfo(String playerId, String bornPlatorm, String transferTo, String bornServer, int time) {
		this.playerId = playerId;
		this.bornPlatorm = bornPlatorm;
		this.transferTo = transferTo;
		this.bornServerId = bornServer;
		this.time = time;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getBornPlatorm() {
		return bornPlatorm;
	}

	public void setBornPlatorm(String bornPlatorm) {
		this.bornPlatorm = bornPlatorm;
	}

	public String getTransferTo() {
		return transferTo;
	}

	public void setTransferTo(String transferTo) {
		this.transferTo = transferTo;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public int getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(int loginTime) {
		this.loginTime = loginTime;
	}

	public String getBornServerId() {
		return bornServerId;
	}

	public void setBornServerId(String bornServerId) {
		this.bornServerId = bornServerId;
	}
	
	public String toJsonStr() {
		JSONObject json = new JSONObject();
		json.put("playerId", playerId);
		json.put("bornPlatorm", bornPlatorm);
		json.put("transferTo", transferTo);
		json.put("bornServerId", bornServerId);
		json.put("time", time);
		json.put("loginTime", loginTime);
		return json.toJSONString();
	}
	
	public static PlatTransferInfo valueOf(String jsonStr) {
		if (HawkOSOperator.isEmptyString(jsonStr)) {
			return null;
		}
		
		JSONObject json = JSONObject.parseObject(jsonStr);
		String playerId = json.getString("playerId");
		String bornPlatorm = json.getString("bornPlatorm");
		String transferTo = json.getString("transferTo");
		String bornServer = json.getString("bornServer");
		int time = json.getIntValue("time");
		PlatTransferInfo info = new PlatTransferInfo(playerId, bornPlatorm, transferTo, bornServer, time);
		info.setLoginTime(json.getIntValue("loginTime"));
		return info;
	}
	
	/**
	 * 判断转完之后的平台信息跟创角时的平台信息是否一样
	 * @return
	 */
	public boolean platformChanged() {
		return !this.bornPlatorm.equals(this.transferTo);
	}
	
	/**
	 * 获取转换前的平台信息
	 * @param platform
	 * @return
	 */
	public String getSourcePlatform(String platform) {
		if (platformChanged()) {
			return Platform.IOS.strLowerCase().equals(platform) ? Platform.ANDROID.strLowerCase() : Platform.IOS.strLowerCase();
		}
		
		return platform;
	}
	
	/**
	 * 获取转换前的平台信息
	 * @param platform
	 * @return
	 */
	public static String getSourcePlatform(String playerId, String platform) {
		PlatTransferInfo transferInfo = RedisProxy.getInstance().getPlatTransferInfo(playerId);
		if(transferInfo != null){
			platform = transferInfo.getSourcePlatform(platform);
		}
		
		return platform;
	}
	
}
