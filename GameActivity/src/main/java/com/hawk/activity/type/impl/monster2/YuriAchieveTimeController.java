package com.hawk.activity.type.impl.monster2;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.monster2.cfg.YuriAchieveActivityKVCfg;
import com.hawk.activity.type.impl.monster2.cfg.YuriAchieveActivityTimeCfg;

public class YuriAchieveTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return YuriAchieveActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		YuriAchieveActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriAchieveActivityKVCfg.class);
		long endTime = cfg.getEndDateTime();//5.18
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();//5.12
		//在endTime时间后开服不触发该活动
		if (serverOpenTime > endTime) {
 			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}

	
}
