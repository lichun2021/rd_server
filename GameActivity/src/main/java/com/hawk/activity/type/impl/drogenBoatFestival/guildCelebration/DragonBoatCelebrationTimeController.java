package com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg.DragonBoatCelebrationKVCfg;
import com.hawk.activity.type.impl.drogenBoatFestival.guildCelebration.cfg.DragonBoatCelebrationTimeCfg;


/**
 * 时空豪礼活动时间控制配置
 * 
 * @author che
 *
 */
public class DragonBoatCelebrationTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return DragonBoatCelebrationTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		DragonBoatCelebrationKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(DragonBoatCelebrationKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
