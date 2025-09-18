package com.hawk.activity.type.impl.buildlevel;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.buildlevel.cfg.BuildLevelActivityKVCfg;
import com.hawk.activity.type.impl.buildlevel.cfg.BuildLevelActivityTimeCfg;

public class BuildLevelTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BuildLevelActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		BuildLevelActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BuildLevelActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
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
		BuildLevelActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BuildLevelActivityKVCfg.class);
		if (cfg.getResetTime() > 0) {
			long time1 = getForerver2Limit45Start();
			return time1 + cfg.getResetTime();
		}
		return super.getEndTimeByTermId(termId);
	}
}
