package com.hawk.activity.type.impl.backFlow.developSput.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 发展冲刺活动配置
 * 
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/return_develop/return_develop_cfg.xml")
public class DevelopSpurtKVConfg extends HawkConfigBase {
	
	private final int serverDelay;
	
	private final String iosPayId;

	private final String androidPayId;



	public DevelopSpurtKVConfg() {
		serverDelay =0;
		iosPayId = "";
		androidPayId = "";
	}

	public long getServerDelay() {
		return serverDelay * 1000L;
	}
	
	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayId() {
		return androidPayId;
	}


	
	
	
	
}
