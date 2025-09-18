package com.hawk.activity.type.impl.customgift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.customgift.cfg.CustomGiftActivityKVCfg;
import com.hawk.activity.type.impl.customgift.cfg.CustomGiftActivityTimeCfg;

/**
 * 定制礼包活动时间控制配置
 * 
 * @author lating
 *
 */
public class CustomGiftController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CustomGiftActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		CustomGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(CustomGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
