package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询玩家坐标 -- 10282087
 *
 * localhost:8080/script/idip/4299
 *
 * @param Openid   
 * @param RoleId   
 * 
 * @author jesse
 */
@HawkScript.Declare(id = "idip/4299")
public class QueryUserCoordinateHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		int[] coord = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());
		result.getBody().put("RoleName", player.getNameEncoded());
		result.getBody().put("Coordinate", coord[0] + "," + coord[1]);
		return result;
	}
}
