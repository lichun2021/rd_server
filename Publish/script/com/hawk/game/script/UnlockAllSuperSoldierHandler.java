package com.hawk.game.script;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

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
import com.hawk.game.config.SuperSoldierCfg;
import com.hawk.game.config.SuperSoldierStarLevelCfg;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.player.Player;
import com.hawk.game.player.supersoldier.PlayerSuperSoldierModule;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.player.supersoldier.SuperSoldierSkillSlot;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Mail.PBGundamStartUp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierListPush;
import com.hawk.game.service.BuildingService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGainItem;
import com.hawk.game.service.mssion.event.EventMechaAdvance;
import com.hawk.game.service.mssion.event.EventMechaPartLvUp;
import com.hawk.game.service.mssion.event.EventMechaPartRepair;
import com.hawk.game.util.GsConst;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 解锁所有二代机甲
 * 
 * http://localhost:8080/script/unlockAllSuperSoldier?playerId=1aat-2awl7h-1
 * 
 * @author lating
 *
 */
public class UnlockAllSuperSoldierHandler extends HawkScript {
	
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
			
			if (player.isActiveOnline()) {
				int threadIdx = player.getXid().getHashThread(HawkTaskManager.getInstance().getThreadNum());
				HawkTaskManager.getInstance().postTask(new HawkTask() {
					@Override
					public Object run() {
						if (player.getAllSuperSoldier().isEmpty()) {
							unlockFirstSuperSoldier(player);
						}
						upgradeAllPreSuperSoldier(player);
						return null;
					}
				}, threadIdx);
			} else {
				if (player.getAllSuperSoldier().isEmpty()) {
					unlockFirstSuperSoldier(player);
				}
				upgradeAllPreSuperSoldier(player);
			}
			
			
			return HawkScript.successResponse("ok");
		} catch (Exception e) {
			HawkException.catchException(e);
			return HawkException.formatStackMsg(e);
		}
	}
	
	/**
	 * 解锁所有的一代机甲
	 * 
	 * @param player
	 */
	private static void upgradeAllPreSuperSoldier(Player player) {
		PlayerSuperSoldierModule module = player.getModule(GsConst.ModuleType.SUPER_SOLDIER);
		ConfigIterator<SuperSoldierCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		while (iterator.hasNext()) {
			SuperSoldierCfg cfg = iterator.next();
			if (cfg.getPreSupersoldierId() > 0) {
				continue;
			}
			
			int count = 0;
			while (true) {
				if (!superSoldierUnlockAndStarUp(player, cfg.getSupersoldierId(), module)) {
					break;
				}
				
				count++;
				if (count > 10) {
					break;
				}
			}
		}
		
		ConfigIterator<SuperSoldierCfg> iterator2 = HawkConfigManager.getInstance().getConfigIterator(SuperSoldierCfg.class);
		while (iterator2.hasNext()) {
			SuperSoldierCfg cfg = iterator2.next();
			if (cfg.getPreSupersoldierId() <= 0) {
				continue;
			}
			
			int superSoldierId = cfg.getSupersoldierId();
			Optional<SuperSoldier> ssoldier = player.getSuperSoldierByCfgId(superSoldierId);
			SuperSoldier superSoldier = ssoldier.orElse(null);
			if (!ssoldier.isPresent()) {
				superSoldier = module.unLockSuperSoldier(superSoldierId);
			}
			int count = 0;
			while (true) {
				if (!superSoldierUnlockAndStarUp(player, cfg.getSupersoldierId(), module)) {
					break;
				}
				
				count++;
				if (count > 10) {
					break;
				}
			}
			superSoldier.notifyChange();
			MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(superSoldierId));
		}
	}
	
	/**
	 * 机甲解锁和进阶
	 * 
	 * @param player
	 * @param soldierId
	 * @param module
	 */
	private static boolean superSoldierUnlockAndStarUp(Player player, int soldierId, PlayerSuperSoldierModule module) {
		int toStep = 0;
		SuperSoldier superSoldier = null;
		Optional<SuperSoldier> ssoldier = player.getSuperSoldierByCfgId(soldierId);
		if (!ssoldier.isPresent()) {
			superSoldier = module.unLockSuperSoldier(soldierId);
			superSoldier.notifyChange();
			MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
		} else {
			superSoldier = ssoldier.get();
		}
		
		int toStar = superSoldier.getStar() + 1;
		SuperSoldierStarLevelCfg toStarLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, toStar, toStep);
		if (Objects.isNull(toStarLevelCfg)) {
			return false;
		}
		SuperSoldierStarLevelCfg starLevelCfg = HawkConfigManager.getInstance().getCombineConfig(SuperSoldierStarLevelCfg.class, soldierId, superSoldier.getStar(), superSoldier.getStep());
		if (toStarLevelCfg.getId() <= starLevelCfg.getId()) {
			return false;
		}
		
		for (SuperSoldierSkillSlot skillSlot : superSoldier.getSkillSlots()) {
			if (skillSlot.getSkill().isMaxLevel()) {
				continue;
			}
			
			skillSlot.getSkill().addExp(9999999);
			superSoldier.notifyChange();
			MissionManager.getInstance().postMsg(player, new EventMechaPartLvUp(soldierId));
		}
		
		superSoldier.starUp(toStar, toStep);
		MissionManager.getInstance().postMsg(player, new EventMechaAdvance(soldierId, toStep));
		GuildMailService.getInstance().sendMail(MailParames.newBuilder()
                .setPlayerId(player.getId())
                .setMailId(MailId.GUNDAM_START_UP)
                .addContents(PBGundamStartUp.newBuilder().setSsoldier(superSoldier.toPBobj()))
                .addSubTitles(soldierId,superSoldier.getStar())
                .addTips(soldierId,superSoldier.getStar())
                .build());
		return true;
	}
	
	/**
	 * 解锁第一个机甲
	 * 
	 * @param player
	 */
	private static void unlockFirstSuperSoldier(Player player) {
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
