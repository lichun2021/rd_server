package com.hawk.game.script;

import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.script.HawkScript;
import org.hawk.script.HawkScriptHttpInfo;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.hawk.game.GsConfig;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.SuperSoldierBuildTaskCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.PlayerSuperSoldierModule;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierListPush;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGainItem;
import com.hawk.game.service.mssion.event.EventMechaPartRepair;
import com.hawk.game.util.GsConst;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 自动解锁第一个机甲
 * 
 * http://localhost:8080/script/unlockFirstSuperSoldier?playerId=1aat-2awl7h-1&playerName=111
 * 
 * @author lating
 *
 */
public class FirstSuperSoldierUnlockHandler extends HawkScript {
	
	@Override
	public String action(Map<String, String> params, HawkScriptHttpInfo httpInfo) {
		return doAction(params);
	}
	
	public static String doAction(Map<String, String> params) {
		if (!GsConfig.getInstance().isDebug()) {
			return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "is not debug");
		}
		
		try {
			Player player = GlobalData.getInstance().scriptMakesurePlayer(params);
			if (player == null) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player not exist");
			}
			
			if (!player.getAllSuperSoldier().isEmpty()) {
				return HawkScript.failedResponse(HawkScript.SCRIPT_ERROR, "player mecha not empty");
			}
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						unlockSuperSoldier(player);
						return null;
					}
				}, threadIdx);
			} else {
				unlockSuperSoldier(player);
			}
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	private static void unlockSuperSoldier(Player player) {
		BuildingBaseEntity building = player.getData().getBuildingEntityByType(BuildingType.SUER_SOLDIER_BUILD);
		if (building == null) {
			BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, (BuildingType.SUER_SOLDIER_BUILD_VALUE * 100) + 1);
			building = player.getData().createBuildingEntity(buildingCfg, "1", false);
			BuildingService.getInstance().createBuildingFinish(player, building, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		}
		
		PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
		module.unLockSuperSoldier(GameConstCfg.getInstance().getSuperSoldierId());
		updateCustomData(player);
		
		// 关联的任务自动完成：任务类型 = 270（获取机甲图纸）
		for (int itemId : ConstProperty.getInstance().getRecordItemList()) {
			RedisProxy.getInstance().getRedisSession().hIncrBy("GainItemTotal:" + player.getId(), String.valueOf(itemId), 100);
			MissionManager.getInstance().postMsg(player, new EventGainItem(itemId, 100));
		}
		
		ConfigIterator<SuperSoldierBuildTaskCfg> taskCfgInterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierBuildTaskCfg.class);
		while (taskCfgInterator.hasNext()) {
			SuperSoldierBuildTaskCfg taskCfg = taskCfgInterator.next();
			MissionManager.getInstance().postMsg(player, new EventMechaPartRepair(taskCfg.getId()));
		}
		
		player.getPush().syncCustomData();
		
		List<SuperSoldier> soldiers = player.getAllSuperSoldier();
		PBSuperSoldierListPush.Builder resp = PBSuperSoldierListPush.newBuilder();
		soldiers.forEach(sd -> resp.addSuperSoldiers(sd.toPBobj()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_ALL_SUPER_SOLDIER, resp));
	}
	
	private static void updateCustomData(Player player) {
		String customKey = GameConstCfg.getInstance().getSuperSoldierTutorialKey();
		CustomDataEntity entity = player.getData().getCustomDataEntity(customKey);
		if (entity == null) {
			// 这里不调player.getData().createCustomDataEntity接口，是为了防止db容错的一个缺陷（数据落地失败但又触发了容错机制，导致内存跟db不一致）而客户端又强依赖这个数据
			entity = new CustomDataEntity();
			entity.setPlayerId(player.getId());
			entity.setType(customKey);
			entity.setValue(1);
			entity.setArg("");
			entity.setId(HawkOSOperator.randomUUID());
			entity.create(true);
			player.getData().getCustomDataEntities().add(entity);
		} else {
			entity.setValue(1);
		}
		
		// 和前端对齐后，两种情况下调用这个接口都不用同步了
		//player.getPush().syncCustomData();
	}
	
}
