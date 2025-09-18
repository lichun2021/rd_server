package com.hawk.activity.type.impl.recallFriend;

import org.hawk.config.HawkConfigBase;

import com.hawk.activity.timeController.impl.ExceptCurrentTermTimeController;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendCfg;
import com.hawk.activity.type.impl.recallFriend.cfg.RecallFriendTimeCfg;

public class RecallFriendTimeController extends ExceptCurrentTermTimeController {

	@Override
	public long getServerDelay() {
		return RecallFriendCfg.getInstance().getServerDealy();
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return RecallFriendTimeCfg.class;
	}

}
