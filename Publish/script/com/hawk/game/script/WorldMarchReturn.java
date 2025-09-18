package com.hawk.game.script;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 行军移除，出征状态英雄重置
 * 
 * localhost:8080/script/marchReturn?playerName=l0001
 *
 */
public class WorldMarchReturn extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}

		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
		if (marchs != null) {
			for (IWorldMarch march : marchs) {
				WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
			}
		}

		return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, "");
	}
}