package com.hawk.activity.type.impl.sceneImport.cfg;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.type.impl.urlReward.URLRewardBaseCfg;

/**
 * url模板活动1 KV配置
 * @author Jesse
 *
 */
@HawkConfigManager.KVResource(file = "activity/scene_import/scene_import_cfg.xml")
public class SceneImportKVCfg extends URLRewardBaseCfg {
	
	/**
	 * 服务器开服延时开启活动时间
	 */
	private final int serverDelay;

	public SceneImportKVCfg() {
		serverDelay = 0;
	}

	public long getServerDelay() {
		return serverDelay * 1000l;
	}

}
