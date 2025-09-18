package com.hawk.activity.type.impl.battlefield;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldActivityKVCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldActivityTimeCfg;

public class BattleFieldTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BattleFieldActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BattleFieldActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		
		return 0;
	}

}
