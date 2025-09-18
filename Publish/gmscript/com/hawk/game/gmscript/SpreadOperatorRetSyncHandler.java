package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SpreadOperatorRetEvent;

/**
 * 推广码被绑定成功 
 *
 * localhost:8080/script/spreadOperatorRet?op=&&playerId=7py-4uwfp-1&code=&friendAchieves=
 *
 * @param playerId
 * @param friendOpenid
 * @param serverId 
 * @author RickMei 
 */

public class SpreadOperatorRetSyncHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			
			String op = params.get("op");
			String ret = params.get("ret");
			//Integer type = Integer.valueOf(params.get("type"));
			String playerId = params.get("playerId");
			String code = params.get("code");
			String achieves = params.get("achieves");
			//List<Integer> achieveIds = new ArrayList<>();
			
			if(null == achieves){
				achieves = "";
			}
			// 参数错误
			if (HawkOSOperator.isEmptyString(op) || HawkOSOperator.isEmptyString(ret)    
					|| HawkOSOperator.isEmptyString(code)) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}

			SpreadOperatorRetEvent event = new SpreadOperatorRetEvent(op,ret,playerId, code, achieves);
			ActivityManager.getInstance().postEvent(event);	
			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
