package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 推送地址配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.KVResource(file = "cfg/pushAddr.cfg")
public class PushAddrCfg extends HawkConfigBase {

	/**
	 * 微信推送地址
	 */
	protected final String wxPushAddr;
	/**
	 * 手Q推送地址
	 */
	protected final String qqPushAddr;
	/**
	 * 实例
	 */
	private static PushAddrCfg instance = null;
	
	/**
	 * 构造
	 */
	public PushAddrCfg() {
		instance = this;
		wxPushAddr = "";
		qqPushAddr = "";
	}
	
	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static PushAddrCfg getInstance() {
		return instance;
	}

	public String getWxPushAddr() {
		return wxPushAddr;
	}

	public String getQqPushAddr() {
		return qqPushAddr;
	}
	
}