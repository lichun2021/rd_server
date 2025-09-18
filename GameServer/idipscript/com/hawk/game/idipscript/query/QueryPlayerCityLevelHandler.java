package com.hawk.game.idipscript.query;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.tuple.HawkTuple2;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询基地等级 -- 10282022
 *
 * localhost:8080/script/idip/4167
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4167")
public class QueryPlayerCityLevelHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		try {
			HawkTuple2<Integer, Long> tuple = RankService.getInstance().getRankTuple(RankType.PLAYER_CASTLE_KEY, player.getId(), player);
			if (tuple != null) {
				result.getBody().put("Rank", tuple.first);
			}
			result.getBody().put("RoleName", player.getNameEncoded());
			result.getBody().put("Level", player.getCityLv());
			result.getBody().put("RetMsg", "");
		} catch (Exception e) {
			HawkException.catchException(e);
			result.getBody().put("Result", IdipConst.SysError.API_EXCEPTION);
			result.getBody().put("RetMsg", "query base level failed");
		}

		return result;
	}
}
