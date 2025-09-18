package com.hawk.activity.type.impl.commandAcademy;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.IActivityTimeCfg;
import com.hawk.activity.timeController.impl.ServerOpenTimeController;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyActivityTimeCfg;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyKVCfg;
/**
 * 指挥官学院活动时间控制器
 * @author che
 *
 */
public class CommandAcademyTimeController extends ServerOpenTimeController {

	@Override
	public long getServerDelay() {
		
		return 0;
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return CommandAcademyActivityTimeCfg.class;
	}
	
	
	@Override
	protected Optional<IActivityTimeCfg> getTimeCfg(long now) {
		CommandAcademyKVCfg config = HawkConfigManager.getInstance().getKVInstance(CommandAcademyKVCfg.class);
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
