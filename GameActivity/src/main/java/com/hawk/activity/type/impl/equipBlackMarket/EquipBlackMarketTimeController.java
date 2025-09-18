package com.hawk.activity.type.impl.equipBlackMarket;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.equipBlackMarket.cfg.EquipBlackMarketActivityTimeCfg;
import com.hawk.activity.type.impl.equipBlackMarket.cfg.EquipBlackMarketKVCfg;
/**
 * 装备黑市活动时间控制器
 * @author che
 *
 */
public class EquipBlackMarketTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		EquipBlackMarketKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(EquipBlackMarketKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EquipBlackMarketActivityTimeCfg.class;
	}
}
