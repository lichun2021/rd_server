package com.hawk.activity.type.impl.playerteamback;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.playerteamback.cfg.PlayerTeamBackActivityKVCfg;
import com.hawk.activity.type.impl.playerteamback.cfg.PlayerTeamBackActivityTimeCfg;

/**
 * 玩家回流H5活动时间控制配置
 * 
 * @author lating
 *
 */
public class PlayerTeamBackActivityController extends ExceptCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlayerTeamBackActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		PlayerTeamBackActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(PlayerTeamBackActivityKVCfg.class);
		if (cfg != null) {
			return cfg.getServerDelay();
		}
		return 0;
	}

}
