package com.hawk.activity.type.impl.loginfund;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.loginfund.cfg.LoginFundActivityKVCfg;
import com.hawk.activity.type.impl.loginfund.cfg.LoginFundActivityTimeCfg;

import java.util.Optional;

public class LoginFundTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginFundActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LoginFundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		LoginFundActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityKVCfg.class);
		long startTime = cfg.getOpenTimeBeginValue();
		long endTime = cfg.getOpenTimeEndValue();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		//不在配置时间范围内的不开启
		if (serverOpenTime < startTime || serverOpenTime > endTime) {
			return Optional.empty();
		}
		return super.getTimeCfg(now);
	}
}
