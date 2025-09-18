package com.hawk.activity.type.impl.equipCraftsman;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.equipCraftsman.cfg.EquipCarftsmanKVCfg;
import com.hawk.activity.type.impl.equipCraftsman.cfg.EquipCarftsmanTimeCfg;

/**
 * 装备工匠
 * 
 * @author Golden
 *
 */
public class EquipCarftsmanTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		return EquipCarftsmanKVCfg.getInstance().getServerDelay();
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return EquipCarftsmanTimeCfg.class;
	}

}
