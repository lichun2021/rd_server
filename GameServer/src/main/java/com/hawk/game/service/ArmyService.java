package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.ArmyHurtDeathEvent;
import com.hawk.activity.event.impl.SoldierNumChangeEvent;
import com.hawk.activity.event.impl.TreatArmyEvent;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.BuildingBaseEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.CalcDeadArmy;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Army.ArmyChangeCause;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.Hero.PBHeroState;
import com.hawk.game.protocol.Mail.CureMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SuperSoldier.PBSuperSoldierState;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EvenntTreatArmy;
import com.hawk.game.service.mssion.event.EventSoldierAdd;
import com.hawk.game.strengthenguide.StrengthenGuideManager;
import com.hawk.game.strengthenguide.msg.SGPlayerSoldierNumChangeMsg;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MailBuilderUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;
import com.hawk.log.LogConst.ArmyChangeReason;
import com.hawk.log.LogConst.ArmySection;
import com.hawk.log.LogConst.PowerChangeReason;
import com.hawk.log.Source;

/**
 * 军队兵种操作类
 * 
 * @author lating
 */
public class ArmyService {

	static Logger logger = LoggerFactory.getLogger("Server");

	private static ArmyService instance = null;

	public static ArmyService getInstance() {
		if (instance == null) {
			instance = new ArmyService();
		}
		return instance;
	}

	/**
	 * 获取可防御的士兵（含英雄）信息
	 * @param player
	 * @return
	 */
	public List<ArmyInfo> getDefenceArmyReady(Player player) {
		List<ArmyInfo> armyList = getFreeArmyList(player);
		for (ArmyInfo armyInfo : armyList) {
			try {
				ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
				armyEntity.addFree(-armyInfo.getTotalCount());
				armyEntity.addMarch(armyInfo.getTotalCount());
				LogUtil.logArmyChange(player, armyEntity, armyInfo.getTotalCount(), ArmySection.MARCH, ArmyChangeReason.DEFENCE);
			} catch (Exception e) {
				HawkLog.logPrintln("getDefenceArmyReady error, playerId:{}, armyId:{}, armyCnt: {}", player.getId(), armyInfo.getArmyId(), armyInfo.getFreeCnt());
			}
		}
		armyList.addAll(getDefBuilding(player));
		return armyList;
	}

	/**
	 * 获取空闲兵力信息
	 * @param player
	 * @return
	 */
	public List<ArmyInfo> getFreeArmyList(Player player) {
		List<ArmyInfo> armys = new ArrayList<ArmyInfo>();
		player.getData().getArmyEntities().stream()
			.filter(e -> e.getFree() > 0)
			.forEach(e -> armys.add(new ArmyInfo(e.getArmyId(), e.getFree())));
		return armys;
	}
	
	/**
	 * 获取可参战防御建筑信息
	 * @param player
	 * @return
	 */
	private List<ArmyInfo> getDefBuilding(Player player) {
		List<ArmyInfo> armys = new ArrayList<ArmyInfo>();
		List<BuildingBaseEntity> entitys = player.getData().getBuildingListByType(BuildingType.PRISM_TOWER);
		entitys.forEach(e -> {
			BuildingCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, e.getBuildingCfgId());
			armys.add(new ArmyInfo(cfg.getBattleSoldierId(), 1));
		});
		return armys;
	}

	/**
	 * 获取伤兵总数
	 * @param player
	 * @return
	 */
	public int getWoundedCount(Player player) {
		int woundedCount = 0;
		for (ArmyEntity army : player.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if(!cfg.isPlantSoldier()){
				woundedCount += army.getWoundedCount();
			}
		}
		return woundedCount;
	}
	
	
	/**
	 * 获取伤兵总数
	 * @param player
	 * @return
	 */
	public int getPlantWoundedCount(Player player) {
		int woundedCount = 0;
		for (ArmyEntity army : player.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if(cfg.isPlantSoldier()){
				woundedCount += army.getWoundedCount();
			}
		}
		return woundedCount;
	}

	/**
	 * 获取士兵总数
	 * @param player
	 * @return
	 */
	public int getArmysCount(List<ArmyInfo> armys) {
		int count = 0;
		for (ArmyInfo army : armys) {
			count += army.getTotalCount();
		}
		return count;
	}
	
	/**
	 * 获取已治疗完待领取的士兵总数
	 * @param player
	 * @return
	 */
	public int getCureFinishCount(Player player) {
		int cureFinishCount = 0;
		for (ArmyEntity army : player.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if(!cfg.isPlantSoldier()){
				cureFinishCount += army.getCureFinishCount();
			}
		}
		return cureFinishCount;
	}
	
	public int getPlantCureFinishCount(Player player){
		int cureFinishCount = 0;
		for (ArmyEntity army : player.getData().getArmyEntities()) {
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			if(cfg.isPlantSoldier()){
				cureFinishCount += army.getCureFinishCount();
			}
		}
		return cureFinishCount;
	}
	
	/**
	 * 获取某种造兵建筑身上待领取兵的数量
	 * 
	 * @param player
	 * @param buildingType
	 * @return
	 */
	public int getTrainFinishCount(Player player, int buildingType) {
		int trainFinishCount = 0;
		for (ArmyEntity entity : player.getData().getArmyEntities()) {
			BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, entity.getArmyId());
			if (armyCfg.getBuilding() == buildingType && entity.getTrainFinishCount() > 0) {
				trainFinishCount ++;
			}
		}
		
		return trainFinishCount;
	}

	/**
	 * 将自由状态士兵改变为行军
	 * @param player
	 * @param armyList 兵种信息
	 */
	public boolean checkArmyAndMarch(Player player, List<ArmyInfo> armyList, List<Integer> heroIdList,int superSoldierId) {
		
		for (ArmyInfo armyInfo : armyList) {
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			if (armyEntity == null || armyInfo.getTotalCount() <= 0 || armyInfo.getTotalCount() > armyEntity.getFree()) {
				logger.error("check army and march error, playerId: {}, armyId: {}, marchCount: {}, freeCount: {}", player.getId(), armyInfo.getArmyId(), armyInfo.getTotalCount(), armyEntity == null ? 0 : armyEntity.getFree());
				return false;
			}
		}
		
		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyInfo armyInfo : armyList) {
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			armyIds.add(armyInfo.getArmyId());
			armyEntity.addFree(-armyInfo.getTotalCount());
			armyEntity.addMarch(armyInfo.getTotalCount());
			LogUtil.logArmyChange(player, armyEntity, armyInfo.getTotalCount(), ArmySection.MARCH, ArmyChangeReason.MARCH);
		}
		
		player.getPush().syncArmyInfo(ArmyChangeCause.MARCH, armyIds.toArray(new Integer[armyIds.size()]));
		
		return true;
	}
	
	/**
	 * 检测部队信息
	 * 
	 * @param player
	 * @param armyList
	 * @param heroIdList
	 * @param superSoldierId
	 */
	public List<ArmyInfo> checkArmyInfo(Player player, List<ArmyInfo> armyList, List<Integer> heroIdList,int superSoldierId) {
		List<ArmyInfo> armyInfoList = new ArrayList<ArmyInfo>();
		for (ArmyInfo armyInfo : armyList) {
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			if (armyEntity != null && armyEntity.getFree() > 0) {
				int count = Math.min(armyInfo.getTotalCount(), armyEntity.getFree());
				armyInfoList.add(new ArmyInfo(armyInfo.getArmyId(), count));
			}
		}
		
		if (armyInfoList.isEmpty()) {
			return armyInfoList;
		}
		
		List<Integer> armyIds = new ArrayList<Integer>();
		for (ArmyInfo armyInfo : armyInfoList) {
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			armyIds.add(armyInfo.getArmyId());
			armyEntity.addFree(-armyInfo.getTotalCount());
			armyEntity.addMarch(armyInfo.getTotalCount());
			LogUtil.logArmyChange(player, armyEntity, armyInfo.getTotalCount(), ArmySection.MARCH, ArmyChangeReason.MARCH);
		}
		
		player.getPush().syncArmyInfo(ArmyChangeCause.MARCH, armyIds.toArray(new Integer[armyIds.size()]));
		
		return armyInfoList;
	}
	
	/**
	 * 检测英雄出征
	 * @param heroId
	 * @return
	 */
	public boolean heroCanMarch(Player player, List<Integer>  heroId) {
		List<PlayerHero> heros = player.getHeroByCfgId(heroId);
		if(heros.size() != heroId.size()){
			return false;
		}
		for(PlayerHero hero : heros){
			if(hero.getState() != PBHeroState.HERO_STATE_FREE){
				return false;
			}
			if(heroId.contains(hero.getConfig().getProhibitedHero())){
				return false;
			}
		}
		return true;
	}
	
	public boolean superSoldierCsnMarch(Player player, int superSoldierId){
		if (superSoldierId > 0) {
			Optional<SuperSoldier> ssOP = player.getSuperSoldierByCfgId(superSoldierId);
			if (!ssOP.isPresent()) {
				return false;
			}
			SuperSoldier soldier = ssOP.get();
			if (soldier.getState() != PBSuperSoldierState.SUPER_SOLDIER_STATE_FREE) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 部队回城(包含伤兵计算)
	 * 
	 * IMP : 单个行军的部队返还不会出现多次
	 * 
	 * @param armyList
	 * @param heroList 
	 */
	public boolean onArmyBack(final Player player, List<ArmyInfo> armyList, List<Integer> heros, int supersoldierId, IWorldMarch march) {
		WorldMarch worldMarch = null;
		if (march != null) {
			worldMarch = march.getMarchEntity();
		}
		if(worldMarch != null && (worldMarch.getMarchProcMask() & GsConst.MarchProcMask.ARMY_PROC) > 0){
			logger.error("army back failed, armyList is already processing, playerId: {}", player.getId());
			return false;
		}
		if (Objects.nonNull(worldMarch)) {
			armyList = worldMarch.getArmys();
			heros = worldMarch.getHeroIdList();
			supersoldierId = worldMarch.getSuperSoldierId();
		}
		
		if (armyList == null) {
			logger.error("army back failed, armyList and heroList is null, playerId: {}", player.getId());
			return false;
		}
		
		for (int hid : heros) {
			Optional<PlayerHero> heroOp = player.getHeroByCfgId(hid);
			if (heroOp.isPresent()) {
				heroOp.get().backFromMarch(march);
			}
		}

		Optional<SuperSoldier> marchSsOp = player.getSuperSoldierByCfgId(supersoldierId);
		if(marchSsOp.isPresent()){
			marchSsOp.get().backFromMarch(march);
		}
		
		if (armyList != null && armyList.size() > 0) {
			//普通士兵
			List<ArmyInfo> commSoldierList = armySoldierBack(player, armyList, worldMarch);
			//泰能士兵
			List<ArmyInfo> plantSoldierList = armyPlantSoldierBack(player, armyList, worldMarch);
			List<Integer> changes = new ArrayList<>();
			List<ArmyInfo> allArmys = new ArrayList<>();
			if(commSoldierList != null){
				commSoldierList.forEach(a->changes.add(a.getArmyId()));
				allArmys.addAll(commSoldierList);
			}
			if(plantSoldierList != null){
				plantSoldierList.forEach(a->changes.add(a.getArmyId()));
				allArmys.addAll(plantSoldierList);
			}
			//士兵变化推送
			player.getPush().syncArmyInfo(ArmyChangeCause.MARCH_BACK, changes.toArray(new Integer[changes.size()]));
			// 只要有死兵就触发，不管是战斗中直接死亡的兵，还是行军回到城点因医院容量不足而死的兵
			boolean powerChange = false;
			for(ArmyInfo army : allArmys){
				if (army.getDeadCount() > 0 || army.getWoundedCount() > 0) {
					powerChange = true;
				}
				if (army.getDeadCount() > 0) {
					HawkApp.getInstance().postMsg(player, CalcDeadArmy.valueOf(allArmys, true));
					// 我要变强士兵数量变更
			        StrengthenGuideManager.getInstance().postMsg(new SGPlayerSoldierNumChangeMsg(player) );
					break;
				}
			}
			if (powerChange) {
				player.refreshPowerElectric(PowerChangeReason.ARMY_BACK);
			}
		}
		
		if(worldMarch != null && !worldMarch.isInvalid()){
			worldMarch.addWorldMarchProcMask(GsConst.MarchProcMask.ARMY_PROC);
		}
		return true;
	}

	private List<ArmyInfo> armySoldierBack(final Player player, List<ArmyInfo> armys, WorldMarch worldMarch) {
		List<Integer> armyIds = new ArrayList<Integer>();

		int deadSoldierCnt = 0, woundedSoldierCnt = 0, totalWound2DeadCount = 0;
		List<ArmyInfo> wound2DeadList = new ArrayList<>();
		// 医院可容纳人口上限
		int cap = player.getRemainDisabledCap();
		int cannonCap = player.getCannonCap();

		// 部队信息过滤,排序
		List<ArmyInfo> armyList = armys.stream().filter(new Predicate<ArmyInfo>() {
			@Override
			public boolean test(ArmyInfo e) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, e.getArmyId());
				// 箭塔不参与结算
				return cfg.getType() != SoldierType.BARTIZAN_100_VALUE && !cfg.isPlantSoldier();
			}
		}).sorted(new Comparator<ArmyInfo>() {
			@Override
			public int compare(ArmyInfo arg0, ArmyInfo arg1) {
				BattleSoldierCfg cfg0 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg0.getArmyId());
				BattleSoldierCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg1.getArmyId());
				if (cfg0.getLevel() != cfg1.getLevel()) {
					return cfg1.getLevel() - cfg0.getLevel();
				} else {
					return cfg1.getId() - cfg0.getId();
				}
			}
		}).collect(Collectors.toList());
		
		ArmyHurtDeathEvent armyHurtDeathEvent = new ArmyHurtDeathEvent(player.getId(),player.getDungeonMap(), player.isCsPlayer());
		StringBuffer armyLogSb = new StringBuffer();
		for (ArmyInfo armyInfo : armyList) {
			ArmyInfo mailArmy = armyInfo.getCopy();
			mailArmy.setWoundedCount(0);
			mailArmy.setDeadCount(0);
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			if (armyEntity == null) {
				HawkLog.errPrintln("armyEntity null on armyBack, playerId: {}, armyId: {}", player.getId(), armyInfo.getArmyId());
				continue;
			}

			if (armyInfo.getDeadCount() > 0) {
				HawkLog.logPrintln("on army back, before calculate, playerId: {}, armyInfo: {}", player.getId(), armyInfo.toString());
			}
			
			try {
				ArmySection armySection = ArmySection.FREE;
				int diedCount = 0;
				int changeCount = 0;
				if (armyInfo.getFreeCnt() > 0 ) {
					int cnt = Math.min(armyEntity.getMarch(), armyInfo.getFreeCnt());
					if (armyInfo.getSave1634() > 0) {
						HawkLog.logPrintln("ArmyBack Save1634 soldierUID:{} count:{}", player.getId() + "_" + armyInfo.getArmyId(), armyInfo.getSave1634());
					}
					if (cnt > 0) {
						armyEntity.addMarch(-cnt);
						armyEntity.addFree(cnt);
						changeCount = cnt;
					}
				}
				
				armyInfo.setDirectDeadCount(armyInfo.getDeadCount());
				int wound2DeadCount = 0;
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyInfo.getArmyId());
				// 伤兵收治
				if (armyInfo.getWoundedCount() > 0) {
					// 可治疗伤兵数量
					int canCureCount = 0;
					// 是否是采矿车,并且有剩余采矿车保护额度
					boolean isCannonProtect = cfg.getSoldierType() == SoldierType.CANNON_SOLDIER_8 && cannonCap >0;
					if (isCannonProtect) {
						canCureCount = Math.min(cap + cannonCap, armyInfo.getWoundedCount());
					}else{
						canCureCount = Math.min(cap, armyInfo.getWoundedCount());
					}
					woundedSoldierCnt += canCureCount;
					mailArmy.setWoundedCount(canCureCount);
					// 如果需要进行采矿车保护计算
					if (isCannonProtect) {
						// 本次采矿车保护收纳的数量
						int protectCnt = Math.min(cannonCap, canCureCount);
						cannonCap -= protectCnt;
						// 保护额度不足,则占用医院额度
						if (canCureCount > protectCnt) {
							cap -= (canCureCount - protectCnt);
						}
					}else{
						cap -= canCureCount;
					}
					
					armyEntity.addMarch(-canCureCount);
					armyEntity.addWoundedCount(canCureCount);
					changeCount = canCureCount;
					if (canCureCount > 0) {
						armySection = ArmySection.WOUNDED;
					}
					// 超过医院收治上限,超出伤兵死亡
					if (canCureCount < armyInfo.getWoundedCount()) {
						wound2DeadCount = armyInfo.getWoundedCount() - canCureCount;						
						totalWound2DeadCount += wound2DeadCount;
						armyInfo.setDeadCount(armyInfo.getDeadCount() + wound2DeadCount);
					}
					armyInfo.setWoundedCount(canCureCount);
					armyHurtDeathEvent.addHurt(armyInfo.getArmyId(), canCureCount);
				}

				int deadCnt = armyInfo.getDeadCount();
				if (deadCnt > 0) {					
					final int armyId = armyEntity.getArmyId();
					armyEntity.addMarch(-armyInfo.getDeadCount());
					deadSoldierCnt += armyInfo.getDeadCount();
					mailArmy.setDeadCount(armyInfo.getDeadCount());
					diedCount += armyInfo.getDeadCount();
					MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, armyEntity.getMarch() + deadCnt, armyEntity.getMarch()));
					ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, deadCnt));
					armyHurtDeathEvent.addDeath(armyId, deadCnt);
			    }
				armyIds.add(armyInfo.getArmyId());
				armyLogSb.append(armyInfo).append(",");
				if (diedCount > 0 && armySection == ArmySection.FREE) {
					armySection = ArmySection.TARALABS;
					changeCount = diedCount;
				}
				if (mailArmy.getWoundedCount() > 0 || mailArmy.getDeadCount() > 0) {
					wound2DeadList.add(mailArmy);
				}
				LogUtil.logArmyChange(player, armyEntity, changeCount, diedCount, armySection, ArmyChangeReason.MARCH_BACK);
			} catch (Exception e) {
				logger.error("on army back error, playerId:{}, armyInfo:{}, entityMarch:{}, entityFree:{}", player.getId(), armyInfo.toString(), armyEntity.getMarch(), armyEntity.getFree());
				HawkException.catchException(e);
			}
		}
		
		// 当医务所存在治疗完未领取的士兵时，不推送变化，建筑依然处于伤兵治疗完未领取状态
		if (woundedSoldierCnt > 0 && ArmyService.getInstance().getCureFinishCount(player) <= 0) {
			GameUtil.changeBuildingStatus(player, Const.BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.SOLDIER_WOUNDED);
		}

		// 发送邮件---收治伤兵邮件 && 统计损失数量
		if (totalWound2DeadCount > 0) {
			player.getData().getStatisticsEntity().addArmyLoseCnt(totalWound2DeadCount);
			//TODO 治疗邮件修改
			CureMail.Builder builder = MailBuilderUtil.createCureMail(woundedSoldierCnt, deadSoldierCnt, wound2DeadList);
			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.WOUND_SOLDIER_DEAD).addContents(builder).build());
		}
		//抛事件
		ActivityManager.getInstance().postEvent(armyHurtDeathEvent);
		//进攻的兵力计算，记录日志, 防守方没有marchId
		logger.info("World march comm Army back, playerId: {}, marchId: {}, dead: {}, wound: {}, wound2dead:{}, ArmyData: {}", player.getId(),
				worldMarch != null ? worldMarch.getMarchId() : "", deadSoldierCnt, woundedSoldierCnt, totalWound2DeadCount, armyLogSb);
		return armyList;
	}
	
	
	
	private List<ArmyInfo> armyPlantSoldierBack(final Player player, List<ArmyInfo> armys, WorldMarch worldMarch) {
		List<Integer> armyIds = new ArrayList<Integer>();
		List<ArmyInfo> wound2DeadList = new ArrayList<>();
		int deadSoldierCnt = 0, woundedSoldierCnt = 0, totalWound2DeadCount = 0;
		// 医院可容纳人口上限
		int cap = player.getPlantRemainDisabledCap();

		// 部队信息过滤,排序
		List<ArmyInfo> armyList = armys.stream().filter(new Predicate<ArmyInfo>() {
			@Override
			public boolean test(ArmyInfo e) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, e.getArmyId());
				// 箭塔不参与结算
				return cfg.getType() != SoldierType.BARTIZAN_100_VALUE && cfg.isPlantSoldier();
			}
		}).sorted(new Comparator<ArmyInfo>() {
			@Override
			public int compare(ArmyInfo arg0, ArmyInfo arg1) {
				BattleSoldierCfg cfg0 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg0.getArmyId());
				BattleSoldierCfg cfg1 = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, arg1.getArmyId());
				if (cfg0.getLevel() != cfg1.getLevel()) {
					return cfg1.getLevel() - cfg0.getLevel();
				} else {
					return cfg1.getId() - cfg0.getId();
				}
			}
		}).collect(Collectors.toList());
		
		ArmyHurtDeathEvent armyHurtDeathEvent = new ArmyHurtDeathEvent(player.getId(),player.getDungeonMap(), player.isCsPlayer());
		StringBuffer armyLogSb = new StringBuffer();
		for (ArmyInfo armyInfo : armyList) {
			ArmyInfo mailArmy = armyInfo.getCopy();
			mailArmy.setWoundedCount(0);
			mailArmy.setDeadCount(0);
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyInfo.getArmyId());
			if (armyEntity == null) {
				HawkLog.errPrintln("armyEntity null on armyBack, playerId: {}, armyId: {}", player.getId(), armyInfo.getArmyId());
				continue;
			}
			
			if (armyInfo.getDeadCount() > 0) {
				HawkLog.logPrintln("on army back, before calculate, playerId: {}, plant armyInfo: {}", player.getId(), armyInfo.toString());
			}

			try {
				ArmySection armySection = ArmySection.FREE;
				int diedCount = 0;
				int changeCount = 0;
				if (armyInfo.getFreeCnt() > 0 ) {
					int cnt = Math.min(armyEntity.getMarch(), armyInfo.getFreeCnt());
					if (armyInfo.getSave1634() > 0) {
						HawkLog.logPrintln("ArmyBack Save1634 soldierUID:{} count:{}", player.getId() + "_" + armyInfo.getArmyId(), armyInfo.getSave1634());
					}
					if (cnt > 0) {
						armyEntity.addMarch(-cnt);
						armyEntity.addFree(cnt);
						changeCount = cnt;
					}
				}

				armyInfo.setDirectDeadCount(armyInfo.getDeadCount());
				int wound2DeadCount = 0;
				// 伤兵收治
				if (armyInfo.getWoundedCount() > 0) {
					// 可治疗伤兵数量
					int canCureCount = Math.min(cap, armyInfo.getWoundedCount());
					cap -= canCureCount;
					woundedSoldierCnt += canCureCount;
					mailArmy.setWoundedCount(canCureCount);
					armyEntity.addMarch(-canCureCount);
					armyEntity.addWoundedCount(canCureCount);
					changeCount = canCureCount;
					if (canCureCount > 0) {
						armySection = ArmySection.WOUNDED;
					}
					// 超过医院收治上限,超出伤兵死亡
					if (canCureCount < armyInfo.getWoundedCount()) {
						wound2DeadCount = armyInfo.getWoundedCount() - canCureCount;						
						totalWound2DeadCount += wound2DeadCount;
						armyInfo.setDeadCount(armyInfo.getDeadCount() + wound2DeadCount);
					}
					armyInfo.setWoundedCount(canCureCount);
					
					armyHurtDeathEvent.addHurt(armyInfo.getArmyId(), canCureCount);
				}

				int deadCnt = armyInfo.getDeadCount();
				if (deadCnt > 0) {					
					final int armyId = armyEntity.getArmyId();
					armyEntity.addMarch(-armyInfo.getDeadCount());
					deadSoldierCnt += armyInfo.getDeadCount();
					mailArmy.setDeadCount(armyInfo.getDeadCount());
					diedCount += armyInfo.getDeadCount();
					MissionManager.getInstance().postMsg(player, new EventSoldierAdd(armyId, armyEntity.getMarch() + deadCnt, armyEntity.getMarch()));
					ActivityManager.getInstance().postEvent(new SoldierNumChangeEvent(player.getId(), armyId, deadCnt));
					armyHurtDeathEvent.addDeath(armyId, deadCnt);
			    }
				armyIds.add(armyInfo.getArmyId());
				
				armyLogSb.append(armyInfo).append(",");
				
				if (diedCount > 0 && armySection == ArmySection.FREE) {
					armySection = ArmySection.TARALABS;
					changeCount = diedCount;
				}
				if (mailArmy.getWoundedCount() > 0 || mailArmy.getDeadCount() > 0) {
					wound2DeadList.add(mailArmy);
				}
				LogUtil.logArmyChange(player, armyEntity, changeCount, diedCount, armySection, ArmyChangeReason.MARCH_BACK);
			} catch (Exception e) {
				logger.error("on army back error, playerId:{}, armyInfo:{}, entityMarch:{}, entityFree:{}", player.getId(), armyInfo.toString(), armyEntity.getMarch(), armyEntity.getFree());
				HawkException.catchException(e);
			}
		}
		
		// 当医务所存在治疗完未领取的士兵时，不推送变化，建筑依然处于伤兵治疗完未领取状态
		if (woundedSoldierCnt > 0 && ArmyService.getInstance().getPlantCureFinishCount(player) <= 0) {
			GameUtil.changeBuildingStatus(player, Const.BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.PLANT_SOLDIER_WOUNDED);
		}
		// 发送邮件---收治伤兵邮件 && 统计损失数量
		if (totalWound2DeadCount > 0) {
			player.getData().getStatisticsEntity().addArmyLoseCnt(totalWound2DeadCount);
			//TODO 治疗邮件修改
			CureMail.Builder builder = MailBuilderUtil.createCureMail(woundedSoldierCnt, deadSoldierCnt, wound2DeadList);
			FightMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(player.getId()).setMailId(MailId.PLANT_WOUND_SOLDIER_DEAD).addContents(builder).build());
		}
		//抛事件
		ActivityManager.getInstance().postEvent(armyHurtDeathEvent);
		//进攻的兵力计算，记录日志, 防守方没有marchId
		logger.info("World march plant Army back, playerId: {}, marchId: {}, dead: {}, wound: {}, wound2dead:{}, ArmyData: {}", player.getId(),
				worldMarch != null ? worldMarch.getMarchId() : "", deadSoldierCnt, woundedSoldierCnt, totalWound2DeadCount, armyLogSb);
		return armyList;
	}

	/**
	 * 发放陷阱
	 * @param trapId
	 * @param count
	 */
	public void awardTrap(Player player, int trapId, int trapCount) {
		int trapCapacity = player.getData().getTrapCapacity();
        int trapTotalCount = player.getData().getTrapCount();
        // 已造出的陷阱数量已达陷阱容量上限
        if(trapTotalCount >= trapCapacity) {
        	return;
        }
        
        trapCount = trapTotalCount + trapCount > trapCapacity ? trapCapacity - trapTotalCount : trapCount;
		ArmyEntity trapEntity = player.getData().getArmyEntity(trapId);
        if (trapEntity == null) {
        	trapEntity = new ArmyEntity();
        	trapEntity.setPlayerId(player.getId());
        	trapEntity.setArmyId(trapId);
        	trapEntity.setId(HawkOSOperator.randomUUID());
            if (!HawkDBManager.getInstance().create(trapEntity)) {
                return;
            }
            player.getData().addArmyEntity(trapEntity);
        }
        
    	trapEntity.addFree(trapCount);
    	// 同步陷阱数据
    	Map<Integer, Integer> map = new HashMap<Integer, Integer>();
     	map.put(trapId, trapCount);
     	player.getPush().syncArmyInfo(ArmyChangeCause.SOLDIER_COLLECT, map);
     	player.refreshPowerElectric(PowerChangeReason.AWARD_TRAP);
     	
     	LogUtil.logArmyChange(player, trapEntity, trapCount, ArmySection.FREE, ArmyChangeReason.AWARD);
	}
	
	/**
	 * 获取已解锁的最高等级的陷阱
	 * @param player
	 * @return
	 */
	public int getUnlockedMaxLevelTrap(Player player) {
		BuildingBaseEntity warFortsBuilding = player.getData().getBuildingEntityByType(BuildingType.WAR_FORTS);
        // 不存在战争堡垒建筑
        if(warFortsBuilding == null) {
        	return 0;
        }
        // 配置不存在
        BuildingCfg buildingCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, warFortsBuilding.getBuildingCfgId());
        if(buildingCfg == null) {
        	return 0;
        }
        
        List<Integer> unlockedTraps = buildingCfg.getUnlockedSoldierIds();
        List<BattleSoldierCfg> maxLevelTrap = new ArrayList<>();
        for(Integer trapId : unlockedTraps) {
        	BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, trapId);
        	if(cfg == null) {
        		continue;
        	}
        	if(maxLevelTrap.size() == 0 || cfg.getLevel() == maxLevelTrap.get(0).getLevel()) {
        		maxLevelTrap.add(cfg);
        	} else if(cfg.getLevel() > maxLevelTrap.get(0).getLevel()) {
        		maxLevelTrap.clear();
        		maxLevelTrap.add(cfg);
        	}
        }
        
        if(maxLevelTrap.size() > 0) {
        	return maxLevelTrap.get(HawkRand.randInt(maxLevelTrap.size() - 1)).getId();
        }
        
		return 0;
	}
	
	/**
	 * 战斗伤兵转化
	 * @param armyList
	 * @param svrCnt 可转化数量
	 */
	public void convertTest(List<ArmyInfo> armyList, int svrCnt) {
		Table<Integer, Integer, ArmyInfo> table = HashBasedTable.create();
		for (ArmyInfo army : armyList) {
			BattleSoldierCfg soldierCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			table.put(soldierCfg.getLevel(), army.getArmyId(), army);
		}
		List<Integer> lvlList = new ArrayList<>(table.rowKeySet());
		lvlList.sort(Comparator.reverseOrder());
		for (int lvl : lvlList) {
			Map<Integer, ArmyInfo> lvlArmyMap = table.row(lvl);
			int sum = lvlArmyMap.values().stream().mapToInt(e -> e.getDeadCount()).sum();
			if (sum <= svrCnt) {
				lvlArmyMap.values().stream().forEach(e -> {
					e.setWoundedCount(e.getWoundedCount() + e.getDeadCount());
					e.setDeadCount(0);
				});
				svrCnt -= sum;
			} else {
				hurtAverage(lvlArmyMap, svrCnt);
				svrCnt = 0;
			}
			if (svrCnt == 0) {
				break;
			}
		}
	}
	
	/**
	 * 同级兵种伤兵均摊
	 * @param armyMap
	 * @param svrCnt 可转化数量
	 */
	private void hurtAverage(Map<Integer, ArmyInfo> armyMap, int svrCnt) {
		int totalDead = armyMap.values().stream().mapToInt(e -> e.getDeadCount()).sum();
		int leftSvr = svrCnt;
		for (Entry<Integer, ArmyInfo> entry : armyMap.entrySet()) {
			ArmyInfo army = entry.getValue();
			if (army.getDeadCount() == 0) {
				continue;
			}
			// 根据死兵比例分摊伤兵转化数量
			int hurtCnt = (int) (1l * svrCnt * army.getDeadCount() / totalDead);

			// 分摊伤兵数量数据修正
			hurtCnt = Math.min(hurtCnt, army.getDeadCount());
			hurtCnt = Math.max(hurtCnt, 0);

			army.setWoundedCount(army.getWoundedCount() + hurtCnt);
			army.setDeadCount(army.getDeadCount() - hurtCnt);
			leftSvr -= hurtCnt;
			if (leftSvr <= 0) {
				return;
			}
		}

		for (Entry<Integer, ArmyInfo> entry : armyMap.entrySet()) {
			ArmyInfo army = entry.getValue();
			if (army.getDeadCount() == 0) {
				continue;
			}
			army.setDeadCount(army.getDeadCount() - 1);
			army.setWoundedCount(army.getWoundedCount() + 1);
			leftSvr--;
			if (leftSvr == 0) {
				return;
			}
		}
	}
	
	/**
	 * 判断兵种是否是陷阱
	 * @param type 兵种类型
	 * @return
	 */
	public boolean isTrap(int type) {
		return type == SoldierType.WEAPON_LANDMINE_101_VALUE || type == SoldierType.WEAPON_ACKACK_102_VALUE || type == SoldierType.WEAPON_ANTI_TANK_103_VALUE;
	}
	
	/**
	 * 移除最后一个医院建筑：有治疗完成的兵，直接收兵；否则，不管是正在治疗中或还没治疗，都直接死兵、
	 * 
	 * @param player
	 */
	public void removeLastHospital(Player player) {
		if (player.getData().getBuildCount(BuildingType.HOSPITAL_STATION) > 0) {
			return;
		}
		
		 Map<String, QueueEntity> queueEntities = player.getData().getQueueEntitiesByType(QueueType.CURE_QUEUE_VALUE);
        // 没有正在治疗的伤兵
        if (queueEntities.size() == 0) {
        	collectCureFinishSoldier(player);
        } else {
        	for (Entry<String, QueueEntity> entry : queueEntities.entrySet()) {
        		QueueEntity queue = entry.getValue();
        		queue.remove();
        	}
        }
        
        for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
        	if(armyEntity.isPlantSoldier()){
        		continue;
        	}
        	int count = armyEntity.getCureCount();
        	ArmySection section = ArmySection.CURE;
        	if (count > 0) {
        		armyEntity.immSetCureCountWithoutSync(0);
        	}
        	
        	if (armyEntity.getWoundedCount() > 0) {
        		count = armyEntity.getWoundedCount();
        		armyEntity.immSetWoundedCountWithoutSync(0);
        		section = ArmySection.WOUNDED;
        	}
        	
        	if (count > 0) {
        		LogUtil.logArmyChange(player, armyEntity, count, section, ArmyChangeReason.REMOVE_BUILDING);
        	}
        }
	}
	
	/**
	 * 收兵（治疗完成的伤兵）
	 * @param player
	 * @return
	 */
	public boolean collectCureFinishSoldier(Player player) {
		boolean collectSuccess = false;
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
        	if(armyEntity.isPlantSoldier()){
        		continue;
        	}
            // 有正在治疗中的兵但有不存在治疗队列，这时候直接完成治疗
            if (armyEntity.getCureCount() > 0) {
                armyEntity.setCureFinishCount(armyEntity.getCureCount());
                armyEntity.immSetCureCountWithoutSync(0);
            }

            // 存在治疗完成未领取的兵，领取士兵
            if (armyEntity.getCureFinishCount() > 0) {
            	int count = armyEntity.getCureFinishCount();
                armyIds.put(armyEntity.getArmyId(), count);
                armyEntity.addFree(count);
                armyEntity.setCureFinishCount(0);
                collectSuccess = true;
                LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.CURE_COLLECT);
            }
        }

        if (ArmyService.getInstance().getWoundedCount(player) <= 0) {
        	GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.COMMON);
        } else {
        	GameUtil.changeBuildingStatus(player, BuildingType.HOSPITAL_STATION_VALUE, Const.BuildingStatus.SOLDIER_WOUNDED);
        }
        // 无兵可领取，领取失败
        if (!collectSuccess) {
            logger.error("there is none cure finish soldier, playerId: {}", player.getId());
            return false;
        }


        // 异步推送消息
		player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
		player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        
        int totalCount = 0;
        for (int count : armyIds.values()) {
        	totalCount += count;
		}
        
        ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), totalCount));
        MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(totalCount));
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.COLLECT_SOLDIER_TREATMENT,
     				Params.valueOf("armys", armyIds));
        
        return true;
	}
	
	
	
	/**
	 * 收兵（治疗完成的伤兵）
	 * @param player
	 * @return
	 */
	public boolean collectCurePlantFinishSoldier(Player player) {
		boolean collectSuccess = false;
        Map<Integer, Integer> armyIds = new HashMap<Integer, Integer>();
        for (ArmyEntity armyEntity : player.getData().getArmyEntities()) {
        	if(!armyEntity.isPlantSoldier()){
        		continue;
        	}
            // 有正在治疗中的兵但有不存在治疗队列，这时候直接完成治疗
            if (armyEntity.getCureCount() > 0) {
                armyEntity.setCureFinishCount(armyEntity.getCureCount());
                armyEntity.immSetCureCountWithoutSync(0);
            }

            // 存在治疗完成未领取的兵，领取士兵
            if (armyEntity.getCureFinishCount() > 0) {
            	int count = armyEntity.getCureFinishCount();
                armyIds.put(armyEntity.getArmyId(), count);
                armyEntity.addFree(count);
                armyEntity.setCureFinishCount(0);
                collectSuccess = true;
                LogUtil.logArmyChange(player, armyEntity, count, ArmySection.FREE, ArmyChangeReason.CURE_COLLECT);
            }
        }


        if (ArmyService.getInstance().getPlantWoundedCount(player) <= 0) {
            GameUtil.changeBuildingStatus(player, BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.COMMON);
        } else {
            GameUtil.changeBuildingStatus(player, BuildingType.PLANT_HOSPITAL_VALUE, Const.BuildingStatus.PLANT_SOLDIER_WOUNDED);
        }
        // 无兵可领取，领取失败
        if (!collectSuccess) {
        	logger.error("there is none cure finish soldier, playerId: {}", player.getId());
        	return false;
        }

        // 异步推送消息
		player.getPush().syncArmyInfo(ArmyChangeCause.CURE_FINISH_COLLECT, armyIds);
		player.refreshPowerElectric(true, PowerChangeReason.CURE_SOLDIER);
        
        int totalCount = 0;
        for (int count : armyIds.values()) {
        	totalCount += count;
		}
        
        ActivityManager.getInstance().postEvent(new TreatArmyEvent(player.getId(), totalCount));
        MissionManager.getInstance().postMsg(player, new EvenntTreatArmy(totalCount));
        // 行为日志
        BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.COLLECT_SOLDIER_TREATMENT,
     				Params.valueOf("armys", armyIds));
        
        return true;
	}
	
	/**
	 * 获取超时空急救站剩余容量
	 * @param playerId
	 * @return
	 */
	public int getTaraLabsLeftCnt(String playerId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		if (player == null) {
			throw new RuntimeException("get player null");
		}
		
		return 0;
	}
	
	/**
	 * 获取兵种类型
	 * @param armyId
	 * @return
	 */
	public SoldierType getArmyType(int armyId) {
		BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		if (cfg == null) {
			return null;
		}
		return cfg.getSoldierType();
	}
}
