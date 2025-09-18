package com.hawk.game.gmscript;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.hotfixagent.HotfixPremain;

public class HotfixHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		List<String> hotfixFiles = new LinkedList<String>();
		if (HotfixPremain.doHotfix(hotfixFiles) && !hotfixFiles.isEmpty()) {
			return HawkScript.successResponse("ok: " + hotfixFiles.toString());
		}
		
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, hotfixFiles.toString());
	}
}
