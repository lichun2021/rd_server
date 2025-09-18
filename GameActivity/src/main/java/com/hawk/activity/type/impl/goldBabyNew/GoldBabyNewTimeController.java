package com.hawk.activity.type.impl.goldBabyNew;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewKVCfg;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewTimeCfg;

public class GoldBabyNewTimeController extends ServerOpenTimeController{

	public GoldBabyNewTimeController(){
		
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GoldBabyNewTimeCfg.class;
	}
	

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		GoldBabyNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GoldBabyNewKVCfg.class);
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenTime < cfg.getStartTime()){
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
}
