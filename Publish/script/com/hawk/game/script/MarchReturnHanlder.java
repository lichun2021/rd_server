package com.hawk.game.script;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 行军立即返回脚本
 * @author zhenyu.shang
 * @since 2017年12月11日
 */
public class MarchReturnHanlder extends HawkScript  {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String marchId = params.get("marchId");
			if (HawkOSOperator.isEmptyString(marchId)) {
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "params is error");
			}
			IWorldMarch worldMarch = WorldMarchService.getInstance().getMarch(marchId);
			if(worldMarch == null){
				return HawkScript.failedResponse(ScriptError.PARAMS_ERROR_VALUE, "march is null");
			}
			WorldMarchService.getInstance().onPlayerNoneAction(worldMarch, HawkTime.getMillisecond());
			return HawkScript.successResponse(null);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(ScriptError.EXCEPTION_VALUE, "");
	}

}
