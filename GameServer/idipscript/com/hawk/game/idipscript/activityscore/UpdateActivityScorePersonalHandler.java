package com.hawk.game.idipscript.activityscore;

import java.util.Optional;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.order.activityOrderTwo.OrderTwoActivity;
import com.hawk.activity.type.impl.order.activityOrderTwo.entity.OrderTwoEntity;
import com.hawk.game.GsConfig;
import com.hawk.game.data.ActivityScoreParamsInfo;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Activity;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改免费装扮与战令积分请求 -- 10282162
 *
 * localhost:8080/script/idip/4473
 * 
    <entry name="OpenId" type="string" size="MAX_OPENID_LEN" desc="用户OpenId" openid="1" test="732945400" isverify="false" isnull="true"/>
    <entry name="RoleId" type="string" size="MAX_ROLEID_LEN" desc="角色ID" test="100" isverify="false" isnull="true"/>
    <entry name="CreditId" type="uint32" desc="积分ID" test="1" isverify="true" isnull="true"/>
    <entry name="Value" type="int64" desc="修改值：只支持正数" test="100" isverify="true" isnull="true"/>
    <entry name="Undo" type="uint32" desc="撤销操作：1，撤销2，正常发放" test="1" isverify="true" isnull="true"/>
    <entry name="EndTime" type="uint32" desc="截止时间：道具可领取时间" test="100" isverify="true" isnull="true"/>
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4473")
public class UpdateActivityScorePersonalHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}

		int activityId = request.getJSONObject("body").getIntValue("CreditId");
		int addScore = request.getJSONObject("body").getIntValue("Value");
		int undo = request.getJSONObject("body").getIntValue("Undo");
		int endTime = request.getJSONObject("body").getIntValue("EndTime");
		
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(activityId);
		if (!activityOp.isPresent()) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "CreditId param invalid");
			return result;
		}
		
		if (undo <= 0 && (addScore <= 0 || endTime < HawkTime.getSeconds())) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetMsg", "Value or EndTime param invalid");
			return result;
		}
		
		ActivityScoreParamsInfo paramsInfo = new ActivityScoreParamsInfo(player.getId(), GsConfig.getInstance().getServerId(), addScore, endTime * 1000L);
		// 撤销之前的操作
		if (undo == 1) {
			LocalRedis.getInstance().delActivityScoreParams(activityId, paramsInfo);
		} else if(!player.isActiveOnline()) {
			LocalRedis.getInstance().addActivityScoreParams(activityId, paramsInfo);
		} else {
			//玩家在线，实时发放
			int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
			HawkTaskManager.getInstance().postTask(new HawkTask() {
				@Override
				public Object run() {
					updateScore(player, activityId, addScore);
					return null;
				}
			}, threadIdx);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		result.getBody().put("Value1", 0);  // 修改前数量
		result.getBody().put("Value2", 0);  // 修改后数量

		return result;
	}
	
	private void updateScore(Player player, int activityId, int addScore) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getActivity(activityId);
		// 免费装扮-189
		if (activityId == Activity.ActivityType.EXCHANGE_DECORATE_VALUE) {
			ExchangeDecorateActivity activity = (ExchangeDecorateActivity) activityOp.get();
			Optional<ActivityExchangeDecorateEntity> entity = activity.getPlayerDataEntity(player.getId());
			activity.addExp(entity.get(), addScore);
			activity.syncActivityDataInfo(player.getId());
			HawkLog.logPrintln("player activity score add by idip, playerId: {}, activityId: {}, score: {}", player.getId(), activityId, addScore);
			return;
		}
		
		// 红警战令-236 
		if (activityId == Activity.ActivityType.ORDER_TWO_VALUE) {
			OrderTwoActivity activity = (OrderTwoActivity) activityOp.get();
			Optional<OrderTwoEntity> entity = activity.getPlayerDataEntity(player.getId());
			activity.addExp(entity.get(), addScore, 0, 0);
			activity.syncActivityDataInfo(player.getId());
			HawkLog.logPrintln("player activity score add by idip, playerId: {}, activityId: {}, score: {}", player.getId(), activityId, addScore);
			return;
		}
	}
	
}
