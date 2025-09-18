package com.hawk.activity.type.impl.jigsawconnect.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**
 * 双十一拼图活动配置
 * @author hf
 *
 */
@HawkConfigManager.KVResource(file = "activity/ssy_jigsaw_connect/ssy_jigsaw_connect_activity_cfg.xml")
public class JigsawConnectActivityKVCfg extends HawkConfigBase {
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;


	public JigsawConnectActivityKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}
	
}
