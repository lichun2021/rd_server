package com.hawk.game.player;

import java.util.List;

import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.entity.*;
import com.hawk.game.invoker.xhjz.XHJZWarRefreshPowerInvoker;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.type.impl.monthcard.cfg.MonthCardActivityCfg;
import com.hawk.activity.type.impl.monthcard.entity.ActivityMonthCardEntity;
import com.hawk.activity.type.impl.monthcard.entity.MonthCardItem;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.CrossTechCfg;
import com.hawk.game.config.EquipResearchLevelCfg;
import com.hawk.game.config.PlayerLevelExpCfg;
import com.hawk.game.config.TechnologyCfg;
import com.hawk.game.invoker.GuildMemberRefreshPowerInvoker;
import com.hawk.game.module.plantfactory.tech.PlantTech;
import com.hawk.game.player.equip.CommanderObject;
import com.hawk.game.player.equip.EquipSlot;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.ArmyService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.guildtask.event.PowerUpTaskEvent;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventPowerCreate;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.gamelib.player.PowerChangeData;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.LogConst.PowerChangeReason;

public class PowerElectric {
	/**
	 * 日志对象
	 */
	final static Logger logger = LoggerFactory.getLogger("Server");
	
	/**
	 * 玩家数据对象
	 */
	private PlayerData playerData;
	/**
	 * 战力数据
	 */
	private PowerData powerData;
	
	/**
	 * 战力变化事件
	 */
	private BattlePointChangeEvent changeEvent = null;
	
	public PowerElectric(PlayerData playerData) {
		this.playerData = playerData;
		powerData = new PowerData();
		
		PlayerEntity playerEntity = playerData.getPlayerEntity();
		if (playerEntity != null) {
			powerData.setBattlePoint(playerEntity.getBattlePoint());
		}
	}
	public PowerData getPowerData() {
		return powerData;
	}
	
	/**
	 * 战斗力\电力刷新，如果有变化推送前台
	 * @param player
	 * @param needSync
	 * @param
	 */
	public void refreshPowerElectric(Player player, boolean needSync, PowerChangeReason reason) {
		refreshPowerElectric(player, false, needSync, reason);
	}
	
	/**
	 * 战斗力\电力刷新，如果有变化推送前台
	 * @param player
	 * @param isSoliderCure 是否伤兵治疗恢复
	 * @param needSync
	 */
	public void refreshPowerElectric(Player player, boolean isSoliderCure, boolean needSync, PowerChangeReason reason) {
		// 变更前战力数据
		PowerData oldPower = this.powerData.getCopy();

		// 计算当前战力属性
		calcElectric();
		
		// 叠加属性
		long afterPoint = powerData.getTotalPoint();
		long battlePoint = powerData.getBattlePoint();
		
		// 只有战力变化处理数据更新
		if (afterPoint == battlePoint) {
			return;
		}

		// 保存战力值
		playerData.getPlayerEntity().setBattlePoint(afterPoint);
		powerData.setBattlePoint(afterPoint);
		
		if (player == null) {
			return;
		}
		
		// 属性同步
		if (player.isActiveOnline() && needSync) {
			player.getPush().syncPlayerInfo();
		}
		
		if (player.isInDungeonMap()) { // 在副本中 就不投递消息了.
			return;
		}
		
		// 保存最高战力值
		long maxPoint = playerData.getPlayerEntity().getMaxBattlePoint();
		if (maxPoint < afterPoint){
			playerData.getPlayerEntity().setMaxbattlePoint(afterPoint);
		}
		PowerChangeData changeData = new PowerChangeData(oldPower, powerData);
		if (changeData.getBuildBattleChange() > 100000 || changeData.getTechBattleChange() > 100000) {
			logger.error("PowerChangeData to large playerId: {} , build: {}, tech: {}",player.getId(), changeData.getBuildBattleChange(), changeData.getTechBattleChange());
			//DungeonRedisLog.log("PowerChangeData", "PowerChangeData too large playerId: {}, build: {}, tech: {}", player.getId(), changeData.getBuildBattleChange(), changeData.getTechBattleChange());
			return;
		}
		
		// 如果是伤兵治疗/进入军演,实时抛出战力变化事件
		if (isSoliderCure || reason == PowerChangeReason.LMJY_INIT) {
			postPowerChangeEvent();
			BattlePointChangeEvent event = new BattlePointChangeEvent(playerData.getPlayerEntity().getId(), powerData, changeData, isSoliderCure, reason);
			ActivityManager.getInstance().postEvent(event);
			postGuildTaskEvent(event);
		} else if (changeData.isPowerReduce()) {
			// 如果存在单项战力下降,实时抛出战力变化事件
			postPowerChangeEvent();
			ActivityManager.getInstance().postEvent(new BattlePointChangeEvent(playerData.getPlayerEntity().getId(), powerData, changeData, isSoliderCure, reason));
		} else {
			// 进行战力变更记录
			if(changeEvent == null){
				changeEvent = new BattlePointChangeEvent(playerData.getPlayerEntity().getId(), powerData, changeData, isSoliderCure, reason);
			}else{
				changeEvent.summation(powerData, changeData);
			}

		}

		// 	记录战力变化
		LogUtil.logPowerFlow(player, changeData.getBattleChange(), changeData.getChangeType(), reason);
		
		// 任务操作
		MissionManager.getInstance().postMsg(player, new EventPowerCreate(battlePoint, afterPoint));
		long noArmyPower = Math.max(afterPoint - powerData.getArmyBattlePoint() - powerData.getTrapBattlePoint(), 0);
		boolean beBan = RankService.getInstance().isBan(player.getId(), RankType.PLAYER_FIGHT_RANK);
		if (!beBan && !player.isZeroEarningState()) {
			RankService.getInstance().updatePlayerPower(player.getId(), afterPoint);
			RankService.getInstance().updatePlayerNoArmyPower(player.getId(), noArmyPower); //刷新去兵战力
		}
		
		if(player.hasGuild()){
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(player.getId());
			if (member != null) {
				member.updateMemberNoArmyPower(noArmyPower); //这里需要在抛事件之前先设置一下，因为活动那边可能在下面的事件处理之前，就需要拉取联盟去兵战力
			}
			GuildService.getInstance().dealMsg(MsgId.GUILDMEMBER_POWER_REFRESH, new GuildMemberRefreshPowerInvoker(player.getId(), afterPoint, noArmyPower));
			XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_POWER_REFRESH, new XHJZWarRefreshPowerInvoker(player.getId(), afterPoint));
		}
		// 记录战力变化
		logger.info(
				"player battle point changed, playerId: {}, before: {}, after: {}, playerPoint: {}, armyPoint: {}, techPoint: {}, buildPoint: {}, trapPoint: {}, heroPoint: {}",
				playerData.getPlayerId(), battlePoint, afterPoint,
				powerData.getPlayerBattlePoint(),
				powerData.getArmyBattlePoint(),
				powerData.getTechBattlePoint(),
				powerData.getBuildBattlePoint(),
				powerData.getTrapBattlePoint(),
				powerData.getHeroBattlePoint());
	}
	
	/**
	 * 部队返还等处在属性变化前提前调用
	 */
	public void calcElectricBeforeChange() {
		synchronized (this.powerData) {
			// 对未初始化过全量战力数据进行计算
			if (!powerData.isInited()) {
				calcElectric();
			}
		}
	}
	
	/**
	 * 计算玩家战力
	 */
	protected void calcElectric() {
		try {
			refreshPlayerBattlePoint();
			refreshArmyAndTrapPowerElectric();
			refreshTechBattlePoint();
			refreshCrossTechBattlePoint();
			refreshBuildPowerElectric();
			refreshHeroPowerPoint();
			refreshSuperSoldierPowerPoint();
			refreshMonthCardPowerPoint();
			refreshArmourPowerPoint();
			refreshEquipResearchPoint();
			refreshPlantTechPowerPoint();
			refreshPlantSciencePowerPoint();
			refreshPlantSchoolPowerPoint();
			refreshStarExplorePowerPoint();
			refreshManhattanBasePower();
			refreshManhattanSWPower();
			refreshMechaCorePower();
			refreshHomeLandPower();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		powerData.setInited(true);
	}
	
	/**
	 * 总战斗力
	 */
	public long getBattlePoint() {
		return powerData.setBattlePoint(playerData.getPlayerEntity().getBattlePoint());
	}

	/**
	 * @return 玩家战斗力
	 */
	public int getPlayerBattlePoint() {
		return powerData.getPlayerBattlePoint();
	}

	/**
	 * @return 部队战斗力
	 */
	public long getArmyBattlePoint() {
		return powerData.getArmyBattlePoint();
	}

	/**
	 * @return 建筑战斗力
	 */
	public int getBuildBattlePoint() {
		return powerData.getBuildBattlePoint();
	}
	
	/**
	 * 获取玩家科技战斗力
	 * @return
	 */
	public int getTechBattlePoint() {
		return powerData.getTechBattlePoint();
	}
	
	/**
	 * 获取玩家远征科技战斗力
	 * @return
	 */
	public int getCrossTechBattlePoint() {
		return powerData.getCrossTechBattlePoint();
	}
	
	/**
	 * 获取陷阱战斗力
	 * @return
	 */
	public int getTrapBattlePoint() {
		return powerData.getTrapBattlePoint();
	}
	
	/**
	 * 英雄战力
	 * @return
	 */
	public int getHeroBattlePoint(){
		return powerData.getHeroBattlePoint();
	}
	
	public int getSuperSoldierBattlePoint(){
		return powerData.getSuperSoldierBattlePoint();
	}
	
	public int getArmourBattlePoint() {
		return powerData.getArmourBattlePoint();
	}
	
	public int getEquipResearchPoint() {
		return powerData.getEquipResearchPoint();
	}
	
	public int getPlantTechBattlePoint(){
		return powerData.getPlantTechBattlePoint();
	}
	
	public int getPlantSchoolBattlePoint() {
		return powerData.getPlantSchoolBattlePoint();
	}

	public int getPlantScienceBattlePoint(){
		return powerData.getPlantScienceBattlePoint();
	}

	public int getStarExploreBattlePoint(){
		return powerData.getStarExploreBattlePoint();
	}
	
	public int getManhattanBaseBattlePoint() {
		return powerData.getManhattanBaseBattlePoint();
	}
	
	public int getManhattanSWBattlePoint() {
		return powerData.getManhattanSWBattlePoint();
	}
	
	public int getMechacoreTechPower() {
		return powerData.getMechaCoreTechPower();
	}
	
	public int getMechacoreModulePower() {
		return powerData.getMechaCoreModulePower();
	}
	public int getHomeLandPower() {
		return powerData.getHomeLandModulePower();
	}

	/**
	 * 获得装备战力值
	 * 
	 * @return
	 */
	public int getEquipBattlePoint() {
		int equipBattlePoint = 0;
		CommanderObject commander = playerData.getCommanderObject();
		List<EquipSlot> equipSlots = commander.getEquipSlots();
		for (EquipSlot slot : equipSlots) {
			equipBattlePoint += slot.getEquipPower();
		}
		
		return equipBattlePoint;
	}

	/**
	 * @return 玩家战斗力
	 */
	private int refreshPlayerBattlePoint() {
		try {
			int commanderBattlePoint = 0;
			PlayerLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(PlayerLevelExpCfg.class, playerData.getPlayerBaseEntity().getLevel());
			if (cfg != null) {
				powerData.setPlayerBattlePoint(0);;
				commanderBattlePoint+=cfg.getBattlePoint();
			}
			CommanderObject commander = playerData.getCommanderObject();
			List<EquipSlot> equipSlots = commander.getEquipSlots();
			for (EquipSlot slot : equipSlots) {
				commanderBattlePoint += slot.getEquipPower();
			}
			//角色战斗力
			return powerData.setPlayerBattlePoint(commanderBattlePoint);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 刷新部队&陷阱战斗力
	 */
	private void refreshArmyAndTrapPowerElectric() {
		try {
			double armyBattlePoint = 0;
			double trapBattlePoint = 0;
			List<ArmyEntity> armyEntities = playerData.getArmyEntities();
			for (ArmyEntity armyEntity : armyEntities) {
				int liveSoldiers = armyEntity.getFree() + armyEntity.getMarch();
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyEntity.getArmyId());
				if (cfg == null) {
					continue;
				}
				if (ArmyService.getInstance().isTrap(cfg.getType())) {
					trapBattlePoint += cfg.getPower() * liveSoldiers;
				} else {
					armyBattlePoint += (cfg.getPower() * liveSoldiers + armyEntity.getAdvancePower());
				}
			}
			powerData.setArmyBattlePoint((long) Math.ceil(armyBattlePoint));
			powerData.setTrapBattlePoint((int) Math.ceil(trapBattlePoint));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 刷新科技战斗力
	 * @return 
	 */
	private int refreshTechBattlePoint() {
		int techBattlePoint = 0;
		List<TechnologyEntity> technologyEntities = playerData.getTechnologyEntities();
		for (TechnologyEntity technologyEntity : technologyEntities) {
			if (technologyEntity.getLevel() > 0) {
				TechnologyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TechnologyCfg.class, technologyEntity.getCfgId());
				if (cfg != null) {
					techBattlePoint += cfg.getBattlePoint();
				}
			}
		}
		powerData.setTechBattlePoint(techBattlePoint);
		return techBattlePoint;
	}
	

	/**
	 * 刷新远征科技战斗力
	 * @return 
	 */
	private int refreshCrossTechBattlePoint() {
		int crossTechBattlePoint = 0;
		List<CrossTechEntity> techEntities = playerData.getCrossTechEntities();
		for (CrossTechEntity techEntity : techEntities) {
			if (techEntity.getLevel() > 0) {
				CrossTechCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTechCfg.class, techEntity.getCfgId());
				if (cfg != null) {
					crossTechBattlePoint += cfg.getBattlePoint();
				}
			}
		}
		powerData.setCrossTechBattlePoint(crossTechBattlePoint);
		return crossTechBattlePoint;
	}
	
	/**
	 * 刷新装备科技战力
	 */
	private int refreshEquipResearchPoint() {
		int point = 0;
		List<EquipResearchEntity> entities = playerData.getEquipResearchEntityList();
		for (EquipResearchEntity entity : entities) {
			ConfigIterator<EquipResearchLevelCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(EquipResearchLevelCfg.class);
			while (cfgIter.hasNext()) {
				EquipResearchLevelCfg cfg = cfgIter.next();
				if (cfg.getResearchId() != entity.getResearchId() || cfg.getLevel() > entity.getResearchLevel()) {
					continue;
				}
				point += cfg.getPower();
			}
		}
		powerData.setEquipResearchPoint(point);
		return point;
	}
	
	/**
	 * 建筑战斗力（包含防御设施）
	 */
	private void refreshBuildPowerElectric() {
		try {
			int buildBattlePoint = 0;
			for (BuildingBaseEntity buildingEntity : playerData.getBuildingEntities()) {
				int cfgId = buildingEntity.getBuildingCfgId();
				BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, cfgId);
				if (buildingCfg == null) {
					continue;
				}
				
				buildBattlePoint += buildingCfg.getBattlePoint();	
			}
			powerData.setBuildBattlePoint(buildBattlePoint);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 英雄战斗力
	 */
	private void refreshHeroPowerPoint() {
		int power = playerData.getHeroEntityList().stream()
				.map(HeroEntity::getHeroObj)
				.mapToInt(PlayerHero::power)
				.sum();
		powerData.setHeroBattlePoint(power);
	}
	
	/**
	 * 英雄战斗力
	 */
	private void refreshPlantTechPowerPoint() {
		int power = playerData.getPlantTechEntities().stream()
				.map(PlantTechEntity::getTechObj)
				.mapToInt(PlantTech::getTechPower)
				.sum();
		powerData.setPlantTechBattlePoint(power);
	}
	
	private void refreshPlantSchoolPowerPoint(){
		int power = playerData.getPlantSoldierSchoolEntity().getPlantSchoolObj().getPower();
		powerData.setPlantSchoolBattlePoint(power);
	}
	
	
	/**
	 * 泰能兵科技树战斗力
	 */
	private void refreshPlantSciencePowerPoint(){
		int power = playerData.getPlantScienceEntity().getSciencObj().getTechPower();
		powerData.setPlantScienceBattlePoint(power);
	}
	/**
	 * 英雄战斗力
	 */
	private void refreshSuperSoldierPowerPoint() {
		int power = playerData.getSuperSoldierEntityList().stream()
				.map(SuperSoldierEntity::getSoldierObj)
				.mapToInt(SuperSoldier::power)
				.sum();
		powerData.setSuperSoldierBattlePoint(power);
	}
	
	/**
	 * 刷新超武底座战力
	 */
	private void refreshManhattanBasePower() {
		int basePower = playerData.getManhattanEntityList().stream()
				.map(e -> e.getManhattanObj())
				.filter(e -> e.isBase())
				.mapToInt(e -> e.getPower())
				.sum();
		powerData.setManhattanBaseBattlePoint(basePower);
	}
	
	/**
	 * 刷新超武战力
	 */
	private void refreshManhattanSWPower() {
		int swPower = playerData.getManhattanEntityList().stream()
				.map(e -> e.getManhattanObj())
				.filter(e -> !e.isBase())
				.mapToInt(e -> e.getPower())
				.sum();
		powerData.setManhattanSWBattlePoint(swPower);
	}
	private void refreshHomeLandPower() {
		try {
			int modulePower = playerData.getHomeLandEntity().getComponent().getMapBuildComp().getPower();
			powerData.setHomeLandModulePower(modulePower);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	private void refreshMechaCorePower() {
		try {
			int techPower = playerData.getMechaCoreEntity().getMechaCoreObj().getTechPower();
			powerData.setMechaCoreTechPower(techPower);
			int modulePower = playerData.getMechaCoreEntity().getMechaCoreObj().getModulePower();
			powerData.setMechaCoreModulePower(modulePower);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 铠甲战斗力
	 */
	private void refreshArmourPowerPoint() {
		int power = playerData.getArmourSuitPower();
		powerData.setArmourBattlePoint(power);
	}
	
	/**
	 * 月卡战斗力
	 */
	private void refreshMonthCardPowerPoint() {
		ActivityMonthCardEntity entity = GameUtil.getMonthCardEntity(playerData.getPlayerEntity().getId());
		if (entity == null) {
			return;
		}
		
		int battlePoint = 0;
		List<MonthCardItem> monthCardItems = entity.getEfficientCardList();
		for (MonthCardItem monthCard : monthCardItems) {
			MonthCardActivityCfg cardCfg = HawkConfigManager.getInstance().getConfigByKey(MonthCardActivityCfg.class, monthCard.getCardId());
			if (cardCfg != null) {
				battlePoint += cardCfg.getPower();
			}
		}
		
		powerData.setMonthCardBattlePoint(battlePoint);
	}

	private void refreshStarExplorePowerPoint() {
		CommanderEntity entity = playerData.getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		int power = starExplores.power();
		powerData.setStarExploreBattlePoint(power);
	}
	
	/**
	 * 抛出玩家战力变化活动事件
	 */
	public void postPowerChangeEvent() {
		if (changeEvent != null) {
			BattlePointChangeEvent postEvent = changeEvent;
			changeEvent = null;
			ActivityManager.getInstance().postEvent(postEvent);
			postGuildTaskEvent(postEvent);
		}
	}
	
	/**
	 * 抛出联盟成员战力增长事件
	 * @param changeEvent
	 */
	private void postGuildTaskEvent(BattlePointChangeEvent changeEvent) {
		String guildId = GuildService.getInstance().getPlayerGuildId(changeEvent.getPlayerId());
		int upPower = changeEvent.getChangeData().getBattleChange();
		// 玩家有联盟,且战力增长
		if (!HawkOSOperator.isEmptyString(guildId) && upPower > 0) {
			// 联盟任务-战力增长
			GuildService.getInstance().postGuildTaskMsg(new PowerUpTaskEvent(guildId, upPower));
		}
	}

}
