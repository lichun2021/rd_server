package com.hawk.game.gmscript;

import java.util.Map;

import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;

import com.hawk.game.activity.impl.backflow.BackFlowService;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.global.GlobalData;
import com.hawk.game.protocol.Const.PushMsgType;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.service.PushService;

/**
 * 平台好友推送
 *
 * localhost:8080/script/sendFriendLossPush?playerId=7py-4uwfp-1
 *
 * @param playerId
 * @param friendOpenid
 * @param serverId 
 * @author RickMei 
 */

public class SendFriendLossPushHandler extends HawkScript {

	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		try {
			String playerId = params.get("playerId");
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(playerId);
			if(account == null){
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			if(account.getLogoutTime() == 0){
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			long curTime = HawkTime.getMillisecond();
			int configLimitDay = ConstProperty.getInstance().getPlayerFriendLossDaysPush();
			long lossBegin = BackFlowService.getInstance().getLossBeginTime(account.getLogoutTime());
			if(curTime - lossBegin  < configLimitDay * HawkTime.DAY_MILLI_SECONDS){
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
			}
			PushService.getInstance().pushMsg(playerId, PushMsgType.LOSS_PLATFORM_FRIEND_PUSH_VALUE);
			return HawkScript.successResponse("");
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, null);
	}
}
