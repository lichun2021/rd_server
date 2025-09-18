package com.hawk.game.idipscript.query;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.entity.TavernEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.service.TavernService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询玩家当前每日任务积分 -- 10282095
 *
 * localhost:8081/idip/4325
 *
 * @param OpenId     用户openId
 * @param BeginTime  查询时间范围的开始时间
 * @param EndTime    查询时间范围的结束时间
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4325")
public class QueryDailyTaskScore extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		long now = HawkTime.getMillisecond();
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		if (!HawkTime.isSameDay(tavernEntity.getLastRefreshTime(), now)) {
			result.getBody().put("Score", 0);
		} else {
			int score = TavernService.getInstance().getTavernBoxScore(player.getId());
			result.getBody().put("Score", score);
		}
		
		return result;
	}
}


