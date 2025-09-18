package com.hawk.game.script;

import java.util.Map;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

/**
 * 查询系统时间
 * 
 * localhost:8080/script/queryTime
 * 
 * @author lating
 *
 */
public class QuerySysTimeHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return HawkScript.successResponse(HawkTime.formatNowTime());
	}
}
