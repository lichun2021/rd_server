package com.hawk.game.service;

import java.util.List;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TrainSoldierStartEvent;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.NewlyDataCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.PlayerMonsterEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.PlayerHeroModule;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Building.BuildingUpdateOperation;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventSoldierAdd;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.GsConst.NewlyType;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;

/**
 * 新手服务类
 * 
 * @author golden
 *
 */
public class NewlyService {

	/**
	 * 实例
	 */
	private static NewlyService instance = new NewlyService();

	/**
	 * 私有化默认构造
	 */
	private NewlyService() {

	}

	/**
	 * 获取实例
	 * 
	 * @return
	 */
	public static NewlyService getInstance() {
		return instance;
	}

	/**
	 * 初始化新手数据
	 * 
	 * @param player
	 */
	public void initNewlyData(Player player) {
		ConfigIterator<NewlyDataCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(NewlyDataCfg.class);

		while (cfgIterator.hasNext()) {
			NewlyDataCfg newlyCfg = cfgIterator.next();

			// 参数
			String[] params = newlyCfg.getValue().split("_");

			switch (newlyCfg.getType()) {

			// 新手建筑
			case NewlyType.NEWLY_BUILDING:
				newlyBuilding(player, Integer.parseInt(params[0]), Integer.parseInt(params[1]), Integer.parseInt(params[2]));
				break;

			// 新手奖励
			case NewlyType.NEWLY_REWARD:
				newlyReward(player, newlyCfg.getValue());
				break;

			// 新手部队
			case NewlyType.NEWLY_SODLIER:
				newlySoldier(player, Integer.parseInt(params[0]), Integer.parseInt(params[1]));
				break;

			// 新手英雄
			case NewlyType.NEWLY_HERO:
				newlyHero(player, Integer.parseInt(params[0]));
				break;

			// 新手消耗
			case NewlyType.NEWLY_CONSUME:
				newlyConsume(player, newlyCfg.getValue());
				break;

			default:
				break;

			}
		}

		// 刷新新手章节任务
		newlyStoryMission(player);

		// 刷新新手野怪击杀等级
		newlyMonsterKilled(player);

		player.responseSuccess(HP.code.NEWLY_DATA_INIT_SUCCESS_SYNC_VALUE);
	}

	/**
	 * 新手建筑
	 * 
	 * @param player
	 * @param budildId
	 * @param level
	 * @param index
	 */
	private void newlyBuilding(Player player, int type, int level, int index) {
		BuildingCfg buildingCfg = AssembleDataManager.getInstance().getBuildingCfg(BuildingType.valueOf(type), 1);
		List<BuildingBaseEntity> entities = player.getData().getBuildingListByType(BuildingType.valueOf(type));

		BuildingBaseEntity buildingEntity = null;
		if (entities == null || entities.isEmpty()) {
			buildingEntity = player.getData().createBuildingEntity(buildingCfg, String.valueOf(index), false);
			BuildingService.getInstance().createBuildingFinish(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY, HP.code.BUILDING_CREATE_PUSH_VALUE);
		} else {
			buildingEntity = entities.get(0);
		}

		for (int i = 0; i < level - 1; i++) {
			BuildingService.getInstance().buildingUpgrade(player, buildingEntity, BuildingUpdateOperation.BUILDING_UPDATE_IMMIDIATELY);
			player.refreshPowerElectric(PowerChangeReason.BUILD_LVUP);
		}
	}

	/**
	 * 新手士兵
	 * 
	 * @param player
	 * @param armyId
	 * @param count
	 */
	private void newlySoldier(Player player, int armyId, int count) {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
		if (armyEntity == null) {
			armyEntity = new ArmyEntity();
			armyEntity.setPlayerId(player.getId());
			armyEntity.setArmyId(armyCfg.getId());
			armyEntity.setId(HawkOSOperator.randomUUID());
			if (HawkDBManager.getInstance().create(armyEntity)) {
				player.getData().addArmyEntity(armyEntity);
			}
		}
		armyEntity.addFree(count);
		player.refreshPowerElectric(PowerChangeReason.INIT_SOLDIER);

		MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, 0, count));
		ActivityManager.getInstance().postEvent(new TrainSoldierStartEvent(player.getId(), armyCfg.getType(), armyId, count));
		
		LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.AWARD);
	}

	/**
	 * 新手奖励
	 * 
	 * @param player
	 */
	private void newlyReward(Player player, String reward) {
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(ItemInfo.valueListOf(reward));
		awardItems.rewardTakeAffectAndPush(player, Action.STORY_MISSION_BONUS, false);
	}

	/**
	 * 新手英雄
	 * 
	 * @param player
	 * @param heroId
	 */
	private void newlyHero(Player player, int heroId) {
		PlayerHeroModule heroModule = player.getModule(GsConst.ModuleType.HERO);
		heroModule.unLockHero(heroId);
	}

	/**
	 * 新手消耗
	 * 
	 * @param player
	 * @param consume
	 */
	private void newlyConsume(Player player, String param) {
		ConsumeItems consume = ConsumeItems.valueOf();
		consume.addConsumeInfo(ItemInfo.valueListOf(param), false);
		if (!consume.checkConsume(player)) {
			return;
		}
		consume.consumeAndPush(player, Action.NEWBIE_GUIDE);
	}

	/**
	 * 新手章节任务
	 * 
	 * @param player
	 */
	private void newlyStoryMission(Player player) {
		StoryMissionService.getInstance().refreshChapterMission(player);
		player.getPush().syncStoryMissionInfo();
	}

	/**
	 * 新手击杀野怪等级
	 * 
	 * @param player
	 */
	private void newlyMonsterKilled(Player player) {
		PlayerMonsterEntity monsterEntity = player.getData().getMonsterEntity();
		monsterEntity.setMaxLevel(1);
		player.getPush().syncMonsterKilled(0, true);
	}
}
