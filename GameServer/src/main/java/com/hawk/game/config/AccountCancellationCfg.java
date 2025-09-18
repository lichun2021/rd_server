package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 账号注销配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "cfg/accountCancellation.cfg")
public class AccountCancellationCfg extends HawkConfigBase {
	
	/**
	 * 全局静态对象
	 */
	private static AccountCancellationCfg instance = null;

	
	/**
	 * 获取全局静态对象
	 */
	public static AccountCancellationCfg getInstance() {
		return instance;
	}

	/**
	 * 
	 */
	public AccountCancellationCfg() {
		instance = this;
		debugMode = 0;
		formal = 0;
		certificationContentType = "";
		certificationDestination = "";
		certificationName = "";
		certificationSecret = "";
		certificationTestL5 = "";
		certificationFormalL5 = "";
		qqAppId = "";
		wxAppId = "";
		closePushContentType = "";
		closePushDestination = "";
		closePushTestL5 = "";
		closePushFormalL5 = "";

	}
	
	/**
	 * debug模式
	 */
	private final int debugMode;
	
	/**
	 * 是否是正式环境
	 */
	private final int formal;
	
	/**
	 * 实名认证-ContentType
	 */
	private final String certificationContentType;
	
	/**
	 * 实名认证-调用者服务名
	 */
	private final String certificationDestination;
	
	/**
	 * 实名认证-调用者服务器英文名
	 */
	private final String certificationName;
	
	
	/**
	 * 实名认证-调用者自身服务秘钥
	 */
	private final String certificationSecret;
	
	/**
	 * 实名认证-测试环境L5
	 */
	private final String certificationTestL5;
	
	/**
	 * 实名认证-正式环境L5
	 */
	private final String certificationFormalL5;

	/**
	 * 手Q appId
	 */
	private final String qqAppId;
	
	/**
	 * wx appId
	 */
	private final String wxAppId;
	
	/**
	 * 注销上报-ContentType
	 */
	private final String closePushContentType;

	/**
	 * 注销上报-调用者服务名
	 */
	private final String closePushDestination;
	
	/**
	 * 注销上报-测试环境L5 
	 */
	private final String closePushTestL5;

	/**
	 * 注销上报-正式环境L5
	 */
	private final String closePushFormalL5;

	
	public boolean isDebugMode() {
		return debugMode == 1;
	}

	public int getFormal() {
		return formal;
	}

	public String getCertificationContentType() {
		return certificationContentType;
	}

	public String getCertificationDestination() {
		return certificationDestination;
	}

	public String getCertificationName() {
		return certificationName;
	}

	public String getCertificationSecret() {
		return certificationSecret;
	}

	public String getCertificationTestL5() {
		return certificationTestL5;
	}

	public String getCertificationFormalL5() {
		return certificationFormalL5;
	}

	public String getQqAppId() {
		return qqAppId;
	}

	public String getWxAppId() {
		return wxAppId;
	}

	public String getClosePushContentType() {
		return closePushContentType;
	}

	public String getClosePushDestination() {
		return closePushDestination;
	}

	public String getClosePushTestL5() {
		return closePushTestL5;
	}

	public String getClosePushFormalL5() {
		return closePushFormalL5;
	}

	/**
	 * 获取实名认证L5
	 */
	public String[] getCertificationL5() {
		String[] L5Str = null;
		
		if (formal == 1) {
			L5Str = certificationFormalL5.split(":");
		} else {
			L5Str = certificationTestL5.split(":");
		}
		return L5Str;
	}

	/**
	 * 获取注销上报L5
	 */
	public String[] getClosePushL5() {
		String[] L5Str = null;
		
		if (formal == 1) {
			L5Str = closePushFormalL5.split(":");
		} else {
			L5Str = closePushTestL5.split(":");
		}
		return L5Str;
	}

	/**
	 * 获取appId
	 */
	public String getAuthAppId(String channel) {
		if (channel.equals("qq")) {
			return qqAppId;
		} else {
			return wxAppId;
		}
	}
}
