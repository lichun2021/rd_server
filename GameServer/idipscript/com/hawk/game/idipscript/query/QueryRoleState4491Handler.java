package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst.IDIPDailyStatisType;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询角色状态请求 -- 10282171
 *
 * @param Type 类型：1：查询当日在线时间 2：查询当日训练部队数量 3：查询当日消耗加速道具的总时长
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4491")
public class QueryRoleState4491Handler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int type = request.getJSONObject("body").getIntValue("Type");
		int amount = 0;
		switch (type) {
		case 1:
			amount = RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.ONLINE_TIME);
			break;
		case 2:
			amount = RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.TRAIN_ARMY);
			break;
		case 3:
			amount = RedisProxy.getInstance().getIdipDailyStatis(player.getId(), IDIPDailyStatisType.ITEM_SPEED_TIME);
			break;
		default:
			break;
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("RoleId", player.getId());
		result.getBody().put("RoleName", IdipUtil.encode(player.getName()));
		result.getBody().put("Level", player.getCityLevel());
		result.getBody().put("Amount", amount);
		return result;
	}
	
}
