package com.hawk.game.idipscript.guild;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 个人联盟信息查询 -- 10282036
 *
 * localhost:8080/script/idip/4217
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4217")
public class QueryPersonalGuildInfoHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			result.getBody().put("AllianceName", 0);
			result.getBody().put("AllianceId", 0);
			result.getBody().put("Fight", 0);
			result.getBody().put("AllianceIntegral", 0);
			result.getBody().put("AllianceBound", 0);
			return result;
		}
		
		GuildInfoObject object = GuildService.getInstance().getGuildInfoObject(guildId);
		result.getBody().put("AllianceName", IdipUtil.encode(object.getName()));
		result.getBody().put("AllianceId", object.getNumTypeId());
		result.getBody().put("Fight", GuildService.getInstance().getGuildBattlePoint(guildId));
		result.getBody().put("AllianceIntegral", player.getGuildContribution());
		// TODO  联盟绑定微信\qq群ID
		String boundId = object.getGuildBoundId();
		result.getBody().put("AllianceBound", HawkOSOperator.isEmptyString(boundId) ? "0" : boundId);
		return result;
	}
	
}
