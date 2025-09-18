package com.hawk.game.idipscript.query;

import java.util.Optional;

import org.hawk.os.HawkException;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.inheritNew.InheritNewActivity;
import com.hawk.activity.type.impl.inheritNew.entity.InheritNewEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.InheritStatus;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 查询军魂传承活动数据请求 -- 10282192
 *
 * localhost:8080/script/idip/4533
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4533")
public class QueryActivityInheritHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		try {
			Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(ActivityType.INHERIT_NEW_VALUE);
			InheritNewActivity activity = (InheritNewActivity)opActivity.get();
			/** 1.未触发传承活动，不可领取 */
			if (!activity.isOpening(player.getId())) {
				return assembleResult(result, 1, 0);
			}
			
			Optional<InheritNewEntity> opEntity = activity.getPlayerDataEntity(player.getId());
			/** 1.未触发传承活动，不可领取 */
			if (!opEntity.isPresent()) {
				return assembleResult(result, 1, 0);
			}
			
			InheritNewEntity entity = opEntity.get();
			/** 2.非新角色，不可领取 */
			if (entity.getState() == InheritStatus.OLD_SERVER_VALUE) {
				return assembleResult(result, 2, 0);
			}
			
			/** 4.还未激活传承，不可领取 */
			if (entity.getState() == InheritStatus.NEED_ACTIVE_VALUE) {
				return assembleResult(result, 4, 0);
			}
			
			return assembleResult(result, 0, entity.getTotalGold());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		/** 5.服务器处理异常，不可领取 */
		return assembleResult(result, 5, 0);
	}
	
	private IdipResult assembleResult(IdipResult result, int state, int diamonds) {
		result.getBody().put("State", state);
		result.getBody().put("Gold", diamonds);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	} 
	
}
