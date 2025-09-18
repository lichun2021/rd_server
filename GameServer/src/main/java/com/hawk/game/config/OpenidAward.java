package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 上次参与测试玩家奖励
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "cfg/openidAward.xml")
public class OpenidAward  extends HawkConfigBase {

	@Id
	private final String openid;
	
	/** 奖励*/
	private final String award;
	
	public OpenidAward() {
		openid = "";
		award = "";
	}

	public String getOpenid() {
		return openid;
	}

	public String getAward() {
		return award;
	}
}
