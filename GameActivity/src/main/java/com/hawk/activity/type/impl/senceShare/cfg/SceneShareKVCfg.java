package com.hawk.activity.type.impl.senceShare.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
/**
 * 场景分享活动配置
 * @author che
 *
 */
@HawkConfigManager.KVResource(file = "activity/scene_share/scene_share_cfg.xml")
public class SceneShareKVCfg extends HawkConfigBase {
	
	/** 服务器开服延时开启活动时间*/
	private final int serverDelay;
	
	/** 是否跨天重置，1重置0不重置 **/
	private final int isDailyReset;
	
	
	
	public SceneShareKVCfg() {
		isDailyReset = 0;
		serverDelay = 0;
	}

	
	
	public long getServerDelay() {
		return serverDelay *1000l;
	}



	public int getIsReset() {
		return isDailyReset;
	}

	public boolean isReset() {
		return isDailyReset == 1;
	}

	
	@Override
	protected boolean assemble() {
		return super.assemble();
	}

	@Override
	protected boolean checkValid() {
		return super.checkValid();
	}

}
