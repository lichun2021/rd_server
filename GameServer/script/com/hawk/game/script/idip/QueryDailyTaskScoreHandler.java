package com.hawk.game.script.idip;

import java.util.Map;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.entity.TavernEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Script.ScriptError;
import com.hawk.game.service.TavernService;

/**
 * 查询玩家每日任务积分数
 * 
 * http://localhost:8080/script/queryDailyScore?playerId=1aat-uu2yx-1
 * 
 * @author lating
 *
 */
public class QueryDailyTaskScoreHandler extends HawkScript {
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
		if (player == null) {
			return HawkScript.failedResponse(ScriptError.ACCOUNT_NOT_EXIST_VALUE, params.toString());
		}
		
		long now = HawkTime.getMillisecond();
		TavernEntity tavernEntity = player.getData().getTavernEntity();
		int score = 0;
		if (HawkTime.isSameDay(tavernEntity.getLastRefreshTime(), now)) {
			score = TavernService.getInstance().getTotalScore(tavernEntity);
		}
		
		return HawkScript.successResponse("Score: " + score);
	}
	
}
