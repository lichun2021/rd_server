package com.hawk.game.idipscript.recharge;

import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsConfig;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;


/**
 * 取消单个玩家首充双倍状态 -- 10282196
 *
 * localhost:8081/idip/4541
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4541")
public class ChargeShowControlHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		String key = GsConst.CHARGE_SHOW + ":" + player.getId();
		RedisProxy.getInstance().getRedisSession().setString(key, "1");
		if (player.isActiveOnline()) {
			CustomDataEntity entity = player.getData().getCustomDataEntity(GsConst.CHARGE_SHOW);
			if (entity == null) {
				entity = player.getData().createCustomDataEntity(GsConst.CHARGE_SHOW, 1, "");
			} else {
				entity.setValue(1);
			}
			player.getPush().syncCustomData();
		}
		
		HawkLog.logPrintln("idip chargeShowControl playerId: {}, local server: {}", player.getId(), GsConfig.getInstance().getServerId());
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		return result;
	}
}


