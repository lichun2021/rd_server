package com.hawk.activity.type.impl.commandAcademySimplify;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyActivityTimeCfg;
import com.hawk.activity.type.impl.commandAcademySimplify.cfg.CommandAcademySimplifyKVCfg;
/**
 * 指挥官学院活动时间控制器
 * 
 * @author huangfei -> lating
 *
 */
public class CommandAcademySimplifyTimeController extends ServerOpenTimeController {

	@Override
	public long getServerDelay() {
		
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CommandAcademySimplifyActivityTimeCfg.class;
	}
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		CommandAcademySimplifyKVCfg config = HawkConfigManager.getInstance().getKVInstance(CommandAcademySimplifyKVCfg.class);
		if(now < config.getActivityTimeLimt()){
			return Optional.empty();
		}
		
		long serverOpenTime = ActivityManager.getInstance().getDataGeter().getServerOpenDate();
		if (serverOpenTime < config.getTimeStart() ||serverOpenTime > config.getTimeEnd()) {
			return Optional.empty();
		}
			
		return super.getTimeCfg(now);
	}
}
