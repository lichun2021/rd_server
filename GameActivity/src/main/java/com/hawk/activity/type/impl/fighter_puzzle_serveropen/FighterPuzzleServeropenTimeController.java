package com.hawk.activity.type.impl.fighter_puzzle_serveropen;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg.FighterPuzzleServeropenActivityKVCfg;
import com.hawk.activity.type.impl.fighter_puzzle_serveropen.cfg.FighterPuzzleServeropenTimeCfg;

public class FighterPuzzleServeropenTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return FighterPuzzleServeropenTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		FighterPuzzleServeropenActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(FighterPuzzleServeropenActivityKVCfg.class);
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenTime < config.getTimeStart() || serverOpenTime > config.getTimeEnd()) {
			return Optional.empty();
		}
			
		return super.getTimeCfg(now);
	}
	
}