package com.hawk.game.idipscript.online.pack0604;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.util.GsConst.GlobalControlType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 全服禁止修改联盟名称、联盟堡垒名称、联盟宣言
 *
 * localhost:8080/script/idip/4377
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4377")
public class ChangeGuildNameControlHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		// idip请求参数中还包含联盟ID参数，但此接口又是针对全服的，后面向飞飞确认过，是参数填错了，所以这里直接无视那个参数。
		
		// 修改类型：1-联盟名称，2-联盟堡垒名称，3-联盟宣言
		int type = request.getJSONObject("body").getIntValue("Type"); 
		GlobalControlType controlType = GlobalControlType.CHANGE_GUILD_NAME;
		switch (type) {
		case 1:
			controlType = GlobalControlType.CHANGE_GUILD_NAME;
			break;
		case 2:
			controlType = GlobalControlType.CHANGE_GUILD_MANOR_NAME;
			break;
		case 3:
			controlType = GlobalControlType.CHANGE_GUILD_ANNOUNCE;
			break;
		default:
			break;
		}
		
		// 1是禁止，0是解除禁止
		int switchVal = request.getJSONObject("body").getIntValue("Switch");
		if (switchVal == 0) {
			GlobalData.getInstance().cancelGlobalBan(controlType);
			LocalRedis.getInstance().cancelGlobalControlBan(controlType);
		} else {
			GlobalData.getInstance().addGlobalBanType(controlType);
			LocalRedis.getInstance().addGlobalControlBan(controlType);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
}


