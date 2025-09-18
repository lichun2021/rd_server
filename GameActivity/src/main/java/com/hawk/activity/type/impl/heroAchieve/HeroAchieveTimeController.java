package com.hawk.activity.type.impl.heroAchieve;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.heroAchieve.cfg.ActivityHeroAchieveKVCfg;
import com.hawk.activity.type.impl.heroAchieve.cfg.HeroAchieveActivityTimeCfg;

public class HeroAchieveTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return HeroAchieveActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		Optional<IActivityTimeCfg> tcfgop = super.getTimeCfg(now);
		if (tcfgop.isPresent() && now > getEndTimeByTermId(tcfgop.get().getTermId())) {
			return Optional.empty();
		}
		return tcfgop;
	}
	
	@Override
	public long getEndTimeByTermId(int termId, String playerId){
		return getEndTimeByTermId(termId);
	}

	@Override
	public long getEndTimeByTermId(int termId) {
		ActivityHeroAchieveKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(ActivityHeroAchieveKVCfg.class);
		if (cfg.getResetTime() > 0) {
			long time1 = getForerver2Limit45Start();
			return time1 + cfg.getResetTime();
		}
		return super.getEndTimeByTermId(termId);
	}
}
