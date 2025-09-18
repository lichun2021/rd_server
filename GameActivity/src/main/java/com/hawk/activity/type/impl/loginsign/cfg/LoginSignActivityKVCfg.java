package com.hawk.activity.type.impl.loginsign.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 登录签到活动全局K-V配置
 * @author PhilChen
 *
 */
@HawkConfigManager.KVResource(file = "activity/login_sign/login_sign_activity_cfg.xml")
public class LoginSignActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	public LoginSignActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}
}