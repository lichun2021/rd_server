package com.hawk.game.script;

import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.GsConfig;
import com.hawk.game.config.ArmourPoolCfg;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;

public class UnlockArmourHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return doAction(params);
	}
	
	public static String doAction(Map<String, String> params) {
		try {
			if (!GsConfig.getInstance().isDebug()) {
				return null;
			}
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
			}
			
			ConfigIterator<ArmourPoolCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(ArmourPoolCfg.class);
			while(iterator.hasNext()) {
				ArmourPoolCfg cfg = iterator.next();
				player.addArmour(cfg.getId());
			}
			
			return HawkScript.successResponse("SUCC");
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
		
	}
}