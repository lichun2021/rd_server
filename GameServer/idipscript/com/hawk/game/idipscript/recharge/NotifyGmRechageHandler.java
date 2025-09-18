package com.hawk.game.idipscript.recharge;

import java.util.HashMap;
import java.util.Map;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.IDIPGmRechargeEvent;
import com.hawk.activity.event.impl.RechargeAllRmbEvent;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.recharge.RechargeType;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.LogInfoType;


/**
 * 通知游戏侧--玩家通过管家渠道充值数据 -- 10282147
 *
 * localhost:8081/idip/4443
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4443")
public class NotifyGmRechageHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		int diamond = request.getJSONObject("body").getIntValue("Diamond");
		if (diamond <= 0) {
			result.getBody().put("Result", IdipConst.SysError.PARAM_ERROR);
			result.getBody().put("RetErrMsg", "Diamond param error: " + diamond);
			return result;
		}
		String serialID = request.getJSONObject("body").getString("Serial");
		if (!RedisProxy.getInstance().saveIdipSerialID(serialID)) {
			HawkLog.errPrintln("NotifyGmRechage4443 request repeatd of serial: {}", serialID);
			result.getBody().put("Result", IdipConst.SysError.SERVER_BUSY);
			result.getBody().put("RetMsg", "request of Serial repeated");
			return result;
		}
		
		Map<String, Object> param = new HashMap<>();
		param.put("type", RechargeType.GIFT);
		param.put("diamonds",diamond);
		LogUtil.logActivityCommon(player, LogInfoType.external_purchase, param);
		
		if (player.isCsPlayer() && !player.isActiveOnline()) {
			String key = RedisKey.IDIP_GM_RECHARGE + ":loginProcess:" + player.getId();
			RedisProxy.getInstance().getRedisSession().hIncrBy(key, String.valueOf(HawkTime.getYyyyMMddIntVal()), diamond, (int)(HawkTime.DAY_MILLI_SECONDS/1000));
		} else {
			ActivityManager.getInstance().postEvent(new IDIPGmRechargeEvent(player.getId(), diamond));
			int rmb = diamond / 10;
			if (diamond % 10 > 0) {
				rmb += 1;
			}
			ActivityManager.getInstance().postEvent(new RechargeAllRmbEvent(player.getId(), rmb));
		}
		
		//记录这个数据，用于防止玩家当天没登陆过，上面抛的活动事件累计成就还是当天之前的数据，等玩家在今天登录时会把跨天重置的成就数据给重置掉 
		String key = RedisKey.IDIP_GM_RECHARGE + ":" + player.getId();
		RedisProxy.getInstance().getRedisSession().hIncrBy(key, String.valueOf(HawkTime.getYyyyMMddIntVal()), diamond, (int)(HawkTime.DAY_MILLI_SECONDS/1000));
		LogUtil.logIdipSensitivity(player, request, 0, diamond);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}


