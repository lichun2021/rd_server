package com.hawk.game.idipscript.world;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.hawk.delay.HawkDelayAction;
import org.hawk.log.HawkLog;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import com.alibaba.fastjson.JSONObject;
import com.hawk.game.GsApp;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.idipscript.util.IdipUtil;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.idip.IdipResult;
import com.hawk.idip.IdipScriptHandler;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;

/**
 * 所有外出部队强制回城 -- 10282031
 *
 * localhost:8080/script/idip/4187
 * 
 * @author lating
 */
@HawkScript.Declare(id = "idip/4187")
public class PlayerMarchBackHandler extends IdipScriptHandler {
	
	@Override
	protected IdipResult onRequest(JSONObject request, HawkScriptHttpInfo httpInfo) {
		IdipResult result = IdipResult.fromRequest(request);
		Player player = IdipUtil.playerCheck(request, result);
		if (player == null) {
			return result;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.MARCH_BACK_FORCED) {
			@Override
			public boolean onInvoke() {
				BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(player.getId());
				if (marchs != null) {
					for (IWorldMarch march : marchs) {
						WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getMarchEntity().getArmys());
					}
					
					GsApp.getInstance().addDelayAction(2000, new HawkDelayAction() {
						@Override
						protected void doAction() {
							armysCheckAndFix(player);
						}
					});
				}
				return true;
			}
		});
		
		if (player.isActiveOnline()) {
			player.kickout(Status.IdipMsgCode.IDIP_NARCH_BACK_VALUE, true, null);
		}
		
		result.getBody().put("Result", 0);
		result.getBody().put("RetMsg", "");
		
		return result;
	}
	
	private void armysCheckAndFix(Player player) {
		// 玩家出征部队数量
		int marchCount = WorldMarchService.getInstance().getPlayerMarchCount(player.getId());
		if (marchCount > 0) {
			HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchCount: {}", player.getId(), marchCount);
			return;
		}

		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
			// 出征中的army数量
			int marchArmyCount = armyEntity.getMarch();
			if (marchArmyCount <= 0) {
				HawkLog.logPrintln("armysCheckAndFix, playerId: {}, marchArmyCount: {}", player.getId(), marchArmyCount);
				continue;
			}
			
			int armyId = armyEntity.getArmyId();
			armyIds.add(armyId);
			
			armyEntity.clearMarch();
			armyEntity.addFree(marchArmyCount);
			LogUtil.logArmyChange(player, armyEntity, marchArmyCount, ArmySection.FREE, ArmyChangeReason.MARCH_FIX);
			HawkLog.logPrintln("armysCheckAndFix, playerId:{}, armyId:{}, marchArmyCount:{}, armyFree:{}", armyEntity.getPlayerId(), armyEntity.getId(), marchArmyCount, armyEntity.getFree());
		}

		if (!armyIds.isEmpty()) {
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, armyIds.toArray(new Integer[armyIds.size()]));
		}
	}
}
