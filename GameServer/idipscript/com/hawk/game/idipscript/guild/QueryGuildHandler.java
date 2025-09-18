package com.hawk.game.idipscript.guild;

import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.GuildService;
import com.hawk.game.util.GameUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家联盟状态 -- 10282103
 *
 * localhost:8080/script/idip/4343
 *
 * @param OpenId  用户openId
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4343")
public class QueryGuildHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		// 联盟不存在
		if (HawkOSOperator.isEmptyString(player.getGuildId())) {
			result.getBody().put("Result", IdipConst.SysError.ALLIANCE_NOT_FOUND);
			result.getBody().put("RetMsg", "guild not eixt");
			return result;
		}
		
		/**
		 *  L0 = 0;
		    L1 = 1;
			L2 = 2;
			L3 = 3;
			L4 = 4;
			L5 = 5;   //盟主
			L14 = 14; //官员
		 */
		int authority = GuildService.getInstance().getPlayerGuildAuthority(player.getId());
		// 联盟名称
		String guildName = player.getGuildName();
		long battlePointTotal = GuildService.getInstance().getGuildBattlePoint(player.getGuildId());
		
		result.getBody().put("AllianceName", GameUtil.getStrEncoded(guildName));
		result.getBody().put("AllianceFight", battlePointTotal);
		result.getBody().put("AllianceLevel", authority);
		
		return result;
	}
}


