package com.hawk.activity.type.impl.chronoGift;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronoGiftActivityKVCfg;
import com.hawk.activity.type.impl.chronoGift.cfg.ChronoGiftActivityTimeCfg;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class ChronoGiftTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ChronoGiftActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		ChronoGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ChronoGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
