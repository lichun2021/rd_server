package com.hawk.game.idipscript.buff;

import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.config.BuffCfg;
import com.hawk.game.entity.StatusDataEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改角色作用号请求 -- 10282188
 *
 * localhost:8080/script/idip/4525
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4525")
public class ModifyPlayerBuff4525Handler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		int buffId = request.getJSONObject("body").getIntValue("BuffId");
		long endTime = request.getJSONObject("body").getIntValue("EndTime") * 1000L;
		BuffCfg buffCfg = HawkConfigManager.getInstance().getConfigByKey(BuffCfg.class, buffId);
		if (buffCfg == null) {
			result.getBody().put("Result", -1);
			result.getBody().put("RetMsg", "BuffId param error");
			return result;
		}
		
		long now = HawkTime.getMillisecond();
		if (endTime <= now) {
			result.getBody().put("Result", -1);
			result.getBody().put("RetMsg", "EndTime param error");
			return result;	
		}
		
		StatusDataEntity entity = player.getData().getStatusById(buffCfg.getEffect(), "");
		if (entity != null && entity.getEndTime() > now) {
			endTime += (entity.getEndTime() - now);
		}
		
		// 修改作用号结束时间
		long finalEndTime = endTime;
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				player.addStatusBuff(buffId, finalEndTime);
				return null;
			}
		}, threadIdx);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("ExpireTime", (int)(endTime / 1000));
		LogUtil.logIdipSensitivity(player, request, 0, 0); // 添加敏感日志
		return result;
	}
	
}
