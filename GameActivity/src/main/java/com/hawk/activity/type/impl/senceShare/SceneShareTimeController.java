package com.hawk.activity.type.impl.senceShare;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.senceShare.cfg.SceneShareActivityTimeCfg;
import com.hawk.activity.type.impl.senceShare.cfg.SceneShareKVCfg;
/**
 * 场景分享活动时间控制器
 * @author che
 *
 */
public class SceneShareTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SceneShareKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(SceneShareKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SceneShareActivityTimeCfg.class;
	}
}
