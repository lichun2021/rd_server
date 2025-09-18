package com.hawk.game.script;

import java.util.Map;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.GsConfig;
import com.hawk.game.config.WarFeverCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;

/**
 * 屏蔽保护罩
 * 
 * http://localhost:8080/script/breakProtect?playerId=1aau-2ayfd6-1
 * 
 * @author lating
 *
 */
public class BreakProtectTimeHandler extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						breakProtect(player);
						return null;
					}
				}, threadIdx);
			} else {
				breakProtect(player);
			}
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	private void breakProtect(Player player) {
		player.removeCityShield();
		
		WarFeverCfg warFeverCfg = HawkConfigManager.getInstance().getConfigByKey(WarFeverCfg.class, player.getCityLevel());
		if (warFeverCfg != null) {
			player.getPlayerBaseEntity().setWarFeverEndTime(HawkTime.getMillisecond() + warFeverCfg.getWarFeverTime());
			player.getPush().syncPlayerInfo();
		}
		
		// 打破全服保护
		GlobalData.getInstance().addBrokenProtectPlayer(player.getId());
		player.getPush().syncPlayerInfo();
		
		WorldPlayerService.getInstance().updateWorldPointProtected(player.getId(), 0L);
		
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(player.getId());
		if (worldPoint != null) {
			WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		}
	}
	
}
