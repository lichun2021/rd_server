package com.hawk.game.idipscript.query;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.PlayerRelationEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.RelationService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询玩家守护角色信息请求  -- 10282174
 *
 * localhost:8080/script/idip/4497
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4497")
public class QueryGuardRole4497Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String playerId = player.getId();
		String targetId = RelationService.getInstance().getGuardPlayer(playerId);
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if (tarPlayer == null) {
			result.getBody().put("Result", -1);
			result.getBody().put("RetMsg", "no guard role exist");
			return result;
		}
		
		PlayerRelationEntity relationEntity = RelationService.getInstance().getPlayerRelationEntity(player.getId(), targetId);		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("OpenId", tarPlayer.getOpenId());
		result.getBody().put("RoleId", tarPlayer.getId());
		result.getBody().put("RoleName", IdipUtil.encode(tarPlayer.getName()));
		result.getBody().put("GuardVal", relationEntity == null ? 0 : relationEntity.getGuardValue());
		return result;
	}
	
}
