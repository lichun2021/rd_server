package com.hawk.game.idipscript.roleexchange;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisKey;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.player.roleexchange.RoleExchangeService;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeFailReason;
import com.hawk.game.player.roleexchange.XinyueConst.XinyueRoleExchangeState;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;

/**
 * 修改角色交易状态(心悦角色交易)请求 -- 10282175
 * 
 * 1. 要求接⼝实现幂等性。如果当前用户下，该角色已经处于目标状态，那么返回成功。
 * 2. 要求在状态转换前，必须检查当前状态是否允许转换⾄⽬标状态。即检查⻆⾊交易状态流转合法性。
 * 3. 对于某些特定⽬标状态转换，需要额外判断该⻆⾊是否能进⼊该状态。例如将⻆⾊状态修改为审核中之前，需要同时判断该⻆⾊是否可登记出售。
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4499")
public class ModifyRoleExchangeStateHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			HawkLog.errPrintln("idip/4499 ModifyRoleExchangeState error, resultCode: {}, msg: {}", result.getBody().get("Result"), result.getBody().get("RetMsg"));
			result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1005);
			return result;
		}
		
		String serialID = request.getJSONObject("body").getString("Serial");
		String key = RedisKey.IDIP_SERIAL_ID + ":" + serialID;
		String info = RedisProxy.getInstance().getRedisSession().getString(key);
		if (!HawkOSOperator.isEmptyString(info)) {
			result.getBody().put("Result", 0);
			result.getBody().put("RetMsg", "");
			return result;
		}
		
		int nextStatus = request.getJSONObject("body").getIntValue("NextStatus");
		if (!conditionCheck(player, result, nextStatus)) {
			return result;
		}
		
		RedisProxy.getInstance().saveIdipSerialID(serialID);
		RoleExchangeService.getInstance().roleExchangeStateSwitch(player, nextStatus);
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
	
	/**
	 * 条件判断
	 * @param player
	 * @param result
	 * @param nextStatus
	 * @return
	 */
	public boolean conditionCheck(Player player, IdipResult result, int nextStatus) {
		if (nextStatus == XinyueRoleExchangeState.EXCHANGE_REVIEW
				|| nextStatus == XinyueRoleExchangeState.EXCHANGE_NOTICE
				|| nextStatus == XinyueRoleExchangeState.EXCHANGE_LAUNCH) {
			int resultCode = RoleExchangeService.getInstance().sellerRoleLaunchCheck(player);
			if (resultCode > 0) {
				result.getBody().put("Result", resultCode);
				return false;
			}
		}
		
		String redisKey = RoleExchangeService.getInstance().getRoleExchangeStatusKey(player.getOpenId(), player.getId());
		String status = RedisProxy.getInstance().getRedisSession().getString(redisKey);
		int statusVal = HawkOSOperator.isEmptyString(status) ? 0 : Integer.parseInt(status);
		if (statusVal == nextStatus) {
			if (nextStatus == XinyueRoleExchangeState.EXCHANGE_LAUNCH) { 
				result.getBody().put("Result", XinyueRoleExchangeFailReason.ERROR_1006); //这个角色已经上架了，玩家操作再次上架
				return false;
			} 
			//如果当前用户下，该角色已经处于目标状态，那么返回成功
			result.getBody().put("Result", 0);
			return false;
		}
		
		return true;
	}
	
}
