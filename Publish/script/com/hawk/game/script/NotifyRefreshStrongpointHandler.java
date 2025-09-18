package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldStrongPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

public class NotifyRefreshStrongpointHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REFRESH_MONSTER) {
			@Override
			public boolean onInvoke() {
				WorldStrongPointService.getInstance().notifyStrongpointRefresh();
				return true;
			}
		});
		
		return HawkScript.successResponse("ok");
	}
}
