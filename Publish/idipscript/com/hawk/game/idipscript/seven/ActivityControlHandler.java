package com.hawk.game.idipscript.seven;

import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.msg.GmCloseActivityMsg;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.idipscript.util.IdipUtil.Switch;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.ControlerModule;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 运营活动开启/关闭
 *
 * localhost:8080/script/idip/4305
 *
 * @param ActivityId  活动ID
 * @param State       操作状态：1上线/0下线
 * 
 * @author jesse
 */
@HawkScript.Declare(id = "idip/4305")
public class ActivityControlHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		
		int activityId = request.getJSONObject("body").getIntValue("ActivityId");
		int switchVal = Switch.OFF;
		if (request.getJSONObject("body").containsKey("State")) {
			switchVal = request.getJSONObject("body").getIntValue("State");
		}
		
		IdipUtil.systemSwitchControl(switchVal, ControlerModule.ACTIVITY, activityId);
		if (switchVal == Switch.OFF) {
			HawkTaskManager.getInstance().postMsg(HawkXID.valueOf(ObjType.MANAGER, ObjType.ID_SERVER_ACTIVITY), GmCloseActivityMsg.valueOf(activityId));
		}
		
		// 添加铭感日志
		LogUtil.logIdipSensitivity(null, request, 0, 0);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}
