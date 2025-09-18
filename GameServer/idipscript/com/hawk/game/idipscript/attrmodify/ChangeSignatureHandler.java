package com.hawk.game.idipscript.attrmodify;

import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.callback.HawkCallback;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;

import com.alibaba.fastjson.JSONObject;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst.ChangeContentType;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.tsssdk.util.UicNameDataInfo;


/**
 * 修改玩家基地签名信息 -- 10282830
 *
 * localhost:8080/script/idip/4351
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4351")
public class ChangeSignatureHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String encodedSignature = request.getJSONObject("body").getString("Sign");
		String signature = IdipUtil.decode(encodedSignature);
		
		AtomicInteger checkResult = new AtomicInteger(-1);
		int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
		GameTssService.getInstance().wordUicNameCheck(signature, new HawkCallback() {
			@Override
			public int invoke(Object args) {
				UicNameDataInfo dataInfo = (UicNameDataInfo) args;
				if (dataInfo.msg_result_flag == 0 && GameUtil.canSignatureUse(signature)) {
					checkResult.set(0);
					WorldPointService.getInstance().updatePlayerSignature(player.getId(), signature);
					GameUtil.notifyDressShow(player.getId());
					RedisProxy.getInstance().updateChangeContentTime(player.getId(), ChangeContentType.CHANGE_SIGNATURE, HawkApp.getInstance().getCurrentTime());
					HawkLog.logPrintln("changeSignature success, playerId: {}, signature: {}", player.getId(), signature);
				} else {
					checkResult.set(1);
					HawkLog.logPrintln("changeSignature failed, playerId: {}, signature: {}, result: {}", player.getId(), signature, dataInfo.msg_result_flag);
				}
				return 0;
			}
		}, threadIdx);
		
		IdipUtil.wait4AsyncProccess(result, checkResult, "change signature failed");
		
		return result;
	}
}


