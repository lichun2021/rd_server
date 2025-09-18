package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 个保法开关配置
 *
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/privacy_setting_option.xml")
public class PrivateSettingOptionCfg extends HawkConfigBase {
	@Id
	protected final int id;
	
	static int preId = 0;
	public static final int friend_setting_option = 23;  // 好友
	public static final int stranger_setting_option = 32; // 陌生人

	public PrivateSettingOptionCfg() {
		id = 0;
	}

	public int getId() {
		return id;
	}
	
	public boolean assemble() {
		if (id - preId != 1) {
			return false;
		}
		preId = id;
		return true;
	}

}
