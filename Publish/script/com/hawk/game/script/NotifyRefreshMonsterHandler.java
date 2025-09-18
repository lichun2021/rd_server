package com.hawk.game.script;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.util.GsConst;
import com.hawk.game.world.service.WorldMonsterService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;

/**
 * 刷新野怪
 * @author golden
 *
 */
public class NotifyRefreshMonsterHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {

		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.REFRESH_MONSTER) {
			@Override
			public boolean onInvoke() {
				WorldMonsterService.getInstance().notifyRefreshCommonMonster();
				WorldMonsterService.getInstance().notifyRefreshActivityMonster();
				return true;
			}
		});
		
		return HawkScript.successResponse("ok");
	}
}
