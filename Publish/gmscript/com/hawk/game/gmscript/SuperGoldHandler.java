package com.hawk.game.gmscript;

import java.util.Map;
import java.util.Optional;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.supergold.SuperGoldActivity;
import com.hawk.game.protocol.Activity;

/***
 * 超级金矿测试脚本
 * localhost:8080/script/superGold?count=
 * @author 
 *
 */
public class SuperGoldHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		int count = Integer.valueOf(params.get("count"));
		if(count > 100 * 10000){
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "最大次数不能超过100万.");
		}
		if(count <= 0){
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "count数据异常.");
		}
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.SUPER_GOLD_VALUE);
		SuperGoldActivity activity = null;
		if (opActivity.isPresent()) {
			activity = (SuperGoldActivity)opActivity.get();
		}
		if(activity == null){
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "活动未开启");
		}
		String msg = activity.superGoldTestScript(count);
		return HawkScript.successResponse(msg);
	}

}
