package com.hawk.activity.type.impl.jigsawconnect326;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.jigsawconnect326.cfg.JXJigsawConnectActivityKVCfg;
import com.hawk.activity.type.impl.jigsawconnect326.cfg.JXJigsawConnectActivityTimeCfg;

public class JXJigsawConnectTimeController extends ExceptCurrentTermTimeController{

	@Override
	public long getServerDelay() {
		JXJigsawConnectActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JXJigsawConnectActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return JXJigsawConnectActivityTimeCfg.class;
	}
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		Optional<IActivityTimeCfg> opTimeCfg = super.getTimeCfg(now);
		if (!opTimeCfg.isPresent()) {
			return opTimeCfg;
		}
		//如果开放时间在 限制时间则不开 
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		JXJigsawConnectActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(JXJigsawConnectActivityKVCfg.class);
		if(cfg.getServerOpenTimeValue()<=serverOpenTime && serverOpenTime <= cfg.getServerEndOpenTimeValue()){
			return Optional.empty();
		}
		return opTimeCfg;
	}

}
