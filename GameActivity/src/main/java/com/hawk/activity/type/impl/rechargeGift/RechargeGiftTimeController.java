package com.hawk.activity.type.impl.rechargeGift;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.rechargeGift.cfg.RechargeGiftActivityKVCfg;
import com.hawk.activity.type.impl.rechargeGift.cfg.RechargeGiftActivityTimeCfg;

public class RechargeGiftTimeController extends JoinCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		RechargeGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeGiftActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RechargeGiftActivityTimeCfg.class;
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
		RechargeGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeGiftActivityKVCfg.class);
		if (cfg.getResetTime() > 0) {
			long time1 = getForerver2Limit45Start();
			return time1 + cfg.getResetTime();
		}
		return super.getEndTimeByTermId(termId);
	}
}
