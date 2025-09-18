package com.hawk.activity.type.impl.seasonpuzzle;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleConstCfg;
import com.hawk.activity.type.impl.seasonpuzzle.cfg.SeasonPuzzleTimeCfg;

/**
 * 赛季拼图
 */
public class SeasonPuzzleTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		SeasonPuzzleConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SeasonPuzzleConstCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return SeasonPuzzleTimeCfg.class;
	}

}
