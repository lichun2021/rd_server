package com.hawk.game.idipscript.activityscore;

import java.util.Optional;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.game.GsConfig;
import com.hawk.game.data.ActivityScoreParamsInfo;
import com.hawk.game.global.LocalRedis;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 全服修改免费装扮与战令积分请求 -- 10282164
 *
 * localhost:8080/script/idip/4477
 * 
 *   <entry name="CreditId" type="uint32" desc="积分ID" test="1" isverify="true" isnull="true"/>
 *	 <entry name="Value" type="int64" desc="修改值：只支持正数" test="100" isverify="true" isnull="true"/>
 *	 <entry name="Undo" type="uint32" desc="撤销操作：1，撤销2，正常发放" test="1" isverify="true" isnull="true"/>
 * 	 <entry name="EndTime" type="uint32" desc="截止时间：道具可领取时间" test="100" isverify="true" isnull="true"/>
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4477")
public class UpdateActivityScoreGlobalHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
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
		
		ActivityScoreParamsInfo paramsInfo = new ActivityScoreParamsInfo(null, GsConfig.getInstance().getServerId(), addScore, endTime * 1000L);
		// 撤销之前的操作
		if (undo == 1) {
			LocalRedis.getInstance().delActivityScoreParams(activityId, paramsInfo);
		} else {
			LocalRedis.getInstance().addActivityScoreParams(activityId, paramsInfo);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");

		return result;
	}
	
}
