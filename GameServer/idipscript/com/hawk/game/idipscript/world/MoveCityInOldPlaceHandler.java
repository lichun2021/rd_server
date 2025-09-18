package com.hawk.game.idipscript.world;

import java.util.ArrayList;
import java.util.List;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 基地原地高迁 -- 10282080
 *
 * localhost:8080/script/idip/4285
 * 
 * @param AreaId     大区：微信（1），手Q（2）
 * @param Partition  小区id
 * @param PlatId     平台:ios(0)，安卓（1）
 * @param OpenId     用户openId
 * @param RoleId     用户角色Id
 * @param Reason     操作原因
 *
 * @author lating
 */
@HawkScript.Declare(id = "idip/4285")
public class MoveCityInOldPlaceHandler extends IdipScriptHandler {
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.checkAccountAndPlayer(request, result);
		if (player == null) {
			return result;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.MOVE_CITY_IN_PLACE) {
			@Override
			public boolean onInvoke() {
				WorldPlayerService.getInstance().moveCity(player.getId(), false, true);
				GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
					@Override
					protected void doAction() {
						WorldMarchService.getInstance().armysCheckAndFix(player);
					}
				});
				return true;
			}
		});
		
		LogUtil.logIdipSensitivity(player, request, 0, 0);
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
}
