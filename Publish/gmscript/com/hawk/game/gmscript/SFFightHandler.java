package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.module.SampleFight.SampleFightUtil;
import com.hawk.game.module.SampleFight.data.SFData;

/**
 * localhost:8080/script/sfFight?data=
 */
public class SFFightHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		String data = params.get("data");
		SFData dataObj = JSONObject.parseObject(data, SFData.class);
		
		
		String msg = SampleFightUtil.fight(dataObj);
		return HawkScript.successResponse(msg);
	}
}
