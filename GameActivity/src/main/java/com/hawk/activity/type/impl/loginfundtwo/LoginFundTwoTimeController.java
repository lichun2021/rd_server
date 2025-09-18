package com.hawk.activity.type.impl.loginfundtwo;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoKVCfg;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoTimeCfg;

/**
 * @author hf
 */
public class LoginFundTwoTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return LoginFundActivityTwoTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		LoginFundActivityTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		LoginFundActivityTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		long startTime = cfg.getOpenTimeBeginValue();
		long endTime = cfg.getOpenTimeEndValue();
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		//不在配置时间范围内的不开启
		if (serverOpenTime < startTime || serverOpenTime > endTime) {
			return Optional.empty();
		}

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
		LoginFundActivityTwoKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginFundActivityTwoKVCfg.class);
		if (cfg.getResetTime() > 0) {
			long time1 = getForerver2Limit45Start();
			return time1 + cfg.getResetTime();
		}
		return super.getEndTimeByTermId(termId);
	}
}
