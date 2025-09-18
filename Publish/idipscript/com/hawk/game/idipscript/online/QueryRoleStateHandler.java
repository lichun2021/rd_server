package com.hawk.game.idipscript.online;

import org.hawk.app.HawkApp;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 查询状态（福袋活动）
 *
 * localhost:8081/idip/4329
 *
 * @param OpenId     
 * @param PlatId     
 * @param Partition  
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4329")
public class QueryRoleStateHandler extends IdipScriptHandler {
	// 未注册用户
	public static final int UNREG_USER = 1;
	// 7天内有登录的用户
	public static final int LOGIN_WITHIN_7_DAY = 2;
	// 9天流失付费用户
	public static final int LOSE_USER_PAY_9_DAY = 3;
	// 9天流失非付费用户
	public static final int LOSE_USER_9_DAY = 4;
	// 其它
	public static final int OTHER = 5;
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			result.getBody().put("State", UNREG_USER);
			return result;
		}
		
		long offlineTimeLong = 0;
		if (player.getLogoutTime() > player.getLoginTime() && !player.isActiveOnline()) {
			offlineTimeLong = HawkApp.getInstance().getCurrentTime() - player.getLogoutTime();
		}
		
		if (offlineTimeLong < 7L * GsConst.DAY_MILLI_SECONDS) {
			result.getBody().put("State", LOGIN_WITHIN_7_DAY);
			return result;
		}
		
		if (offlineTimeLong >= 9L * GsConst.DAY_MILLI_SECONDS) {
			int rechargeTotal = player.getData().getRechargeTotal();
			result.getBody().put("State", rechargeTotal > 0 ? LOSE_USER_PAY_9_DAY : LOSE_USER_9_DAY);
			return result;
		}
		
		result.getBody().put("State", OTHER);
		return result;
	}
}


