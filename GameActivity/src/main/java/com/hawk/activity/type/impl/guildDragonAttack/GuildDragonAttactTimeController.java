package com.hawk.activity.type.impl.guildDragonAttack;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.guildDragonAttack.cfg.GuildDragonAttackTimeCfg;

public class GuildDragonAttactTimeController extends JoinCurrentTermTimeController {

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return GuildDragonAttackTimeCfg.class;
	}
	@Override
	public long getServerDelay() {
		return 0;
	}
}
