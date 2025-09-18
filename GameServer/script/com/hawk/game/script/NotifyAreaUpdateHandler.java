package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.util.GsConst;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldDelayTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 世界区块刷新
 * @author golden
 *
 * localhost:8080/script/areaUpdate?refreshTime=1800
 */
public class NotifyAreaUpdateHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		long refreshTime = 1800 * 1000L;
		if (params.containsKey("refreshTime")) {
			refreshTime = Integer.valueOf(params.get("refreshTime")) * 1000L;
		}
		
		for (AreaObject area : WorldPointService.getInstance().getAreaVales()) {
			long delayTime = refreshTime / WorldPointService.getInstance().getAreaSize() * area.getId();
			WorldThreadScheduler.getInstance().postDelayWorldTask(new WorldDelayTask(GsConst.WorldTaskType.AREA_UPDATE, delayTime, delayTime, 1) {
				@Override
				public boolean onInvoke() {
					WorldPointService.getInstance().notifyAreaUpdate(area, 0);
					return true;
				}
			});
		}
		return null;
	}

}
