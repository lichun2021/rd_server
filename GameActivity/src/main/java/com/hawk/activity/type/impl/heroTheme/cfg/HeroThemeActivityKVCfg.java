package com.hawk.activity.type.impl.heroTheme.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.KVResource(file = "activity/hero_theme/hero_theme_cfg.xml")
public class HeroThemeActivityKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间 单位:s*/
	private final int serverDelay;
	
	public HeroThemeActivityKVCfg() {
		serverDelay = 0;
	}
	
	public long getServerDelay() {
		return serverDelay * 1000l;
	}

	@Override
	protected boolean assemble() {
		return true;
	}

}
