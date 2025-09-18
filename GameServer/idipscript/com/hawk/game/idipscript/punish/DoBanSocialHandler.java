package com.hawk.game.idipscript.punish;

import org.hawk.os.HawkTime;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.common.IDIPBanInfo;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.IDIPBanType;
import com.hawk.idip.IdipConst;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 禁止社交操作（AQ） -- 10282831
 *
 * localhost:8081/idip/4433
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4433")
public class DoBanSocialHandler extends IdipScriptHandler {
	
	static final IDIPBanType[] ALL_TYPE = {IDIPBanType.BAN_ADD_FRIEND, IDIPBanType.BAN_ASK_DRESS, IDIPBanType.BAN_SEND_DRESS, IDIPBanType.BAN_SEND_MAIL, IDIPBanType.BAN_GUILD_INVITE};
	static final int all = 99;
	static final int cancel = 0; 
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String reason = request.getJSONObject("body").getString("BanReason"); 
		int seconds = request.getJSONObject("body").getIntValue("Time");  // 禁止时长（秒）
		int type = request.getJSONObject("body").getIntValue("Type");  // 禁止类型（1为加好友，2索要装扮,3赠送装扮,4发送邮件,5联盟邀请,99全部,0取消处罚）
		reason = IdipUtil.decode(reason);
		
		if (type == all) {
			long now = HawkTime.getMillisecond();
			long endTime = now + 1000L * seconds;
			IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason + "（解封时间：" + HawkTime.formatTime(endTime) + "）", now, endTime, seconds);
			for (IDIPBanType banType : ALL_TYPE) {
				RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, banType);
			}
		} else if (type == cancel) {
			for (IDIPBanType banType : ALL_TYPE) {
				RedisProxy.getInstance().removeIDIPBanInfo(player.getId(), banType);
			}
		} else if (type > 0 && type <= ALL_TYPE.length) {
			long now = HawkTime.getMillisecond();
			long endTime = now + 1000L * seconds;
			IDIPBanInfo banInfo = new IDIPBanInfo(player.getId(), reason + "（解封时间：" + HawkTime.formatTime(endTime) + "）", now, endTime, seconds);
			RedisProxy.getInstance().addIDIPBanInfo(player.getId(), banInfo, ALL_TYPE[type - 1]);
		} else {
			return sendResult(result, seconds, IdipConst.SysError.PARAM_ERROR, "not support type");
		}
		
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		
		return sendResult(result, seconds, 0, "");
	}
	
	private IdipResult sendResult(IdipResult result, int seconds, int resultCode, String msg) {
		int timeNow = HawkTime.getSeconds();
		result.getBody().put("Result", resultCode);
		result.getBody().put("RetMsg", msg);
		result.getBody().put("OperTime", timeNow);     // 封停时间
		result.getBody().put("ExpireTime", timeNow + seconds);   // 解封时间
		result.getBody().put("BanTerm", seconds); // 封停时长
		return result;
	} 
	
}


