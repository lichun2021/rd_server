package com.hawk.activity.type.impl.monster2Show;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.monster2Show.cfg.YuriAchieveShowActivityKVCfg;
import com.hawk.activity.type.impl.monster2Show.cfg.YuriAchieveShowActivityTimeCfg;

public class YuriAchieveShowTimeController extends ServerOpenTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return YuriAchieveShowActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		YuriAchieveShowActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(YuriAchieveShowActivityKVCfg.class);
		long endTime = cfg.getEndDateTime();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		//在endTime时间后开服不触发该活动
		if (serverOpenTime > endTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
	
}
