package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 兵种信息配置
 *
 * @author julia
 *
 */
@HawkConfigManager.XmlResource(file = "xml/deviceToken.xml")
public class DeviceTokenCfg extends HawkConfigBase {
	@Id
	protected final String token;

	// 激活码
	protected final int type;

	public DeviceTokenCfg() {
		token = null;
		type = 0;
	}

	protected String getToken() {
		return token;
	}

	protected int getCode() {
		return type;
	}

	@Override
	protected boolean assemble() {
		if(HawkOSOperator.isEmptyString(token)){
			return false;
		}
		return true;
	}

}
