package com.hawk.activity.type.impl.fighter_puzzle;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.fighter_puzzle.cfg.FighterPuzzleActivityKVCfg;
import com.hawk.activity.type.impl.fighter_puzzle.cfg.FighterPuzzleTimeCfg;

public class FighterPuzzleTimeController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FighterPuzzleTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		FighterPuzzleActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(FighterPuzzleActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
