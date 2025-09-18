package com.hawk.game.module.spacemecha;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.impl.spaceguard.SpaceGuardActivity;
import com.hawk.activity.type.impl.spaceguard.cfg.SpaceGuardTimeCfg;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpaceMechaGrid;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaCabinCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaSubcabinCfg;
import com.hawk.game.module.spacemecha.stage.SpacePrepareStage;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Activity.SpaceMachineGuardActivityInfoPB;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SpaceMecha.MechaSpaceQuarterInfoResp;
import com.hawk.game.protocol.SpaceMecha.MechaSpaceQuarterMarch;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.WorldUtil;
import com.hawk.game.util.GsConst.GuildOffice;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.object.AreaObject;
import com.hawk.game.world.object.Point;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelog.GameLog;
import com.hawk.gamelog.LogParam;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 星甲召唤功能service
 *  
 * @author lating
 *
 */
public class SpaceMechaService {
	/**
	 * 所有的联盟机甲舱体信息
	 */
	private Map<String, MechaSpaceInfo> guildSpaceMap = new ConcurrentHashMap<>();
	/**
	 * 本期活动内各联盟放置的所有舱体信息
	 */
	private Map<String, BlockingDeque<GuardRecordPB.Builder>> guildSpaceRecordMap = new ConcurrentHashMap<>();
	/**
	 * 活动状态：-1初始状态，0活动关闭状态，1活动开启状态，2停服处理
	 */
	private AtomicInteger activityState = new AtomicInteger(MechaSpaceConst.ACTIVITY_STATE_INIT);
	/**
	 * enmeyId对应的 npcplayer信息
	 */
	private Map<Integer, NpcPlayer> enemyNpcPlayerMap = new ConcurrentHashMap<>();
	
	private static SpaceMechaService instance;
	
	private SpaceMechaService() {
	}
	
	public static SpaceMechaService getInstance() {
		if (instance == null) {
			instance = new SpaceMechaService();
		}
		return instance;
	}
	
	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		// 加载舱体记录数据
		loadSpaceRecordData();
		//
		WorldThreadScheduler.getInstance().addWorldTickable(new HawkPeriodTickable(2000) {
			@Override
			public void onPeriodTick() {
				try {
					tick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		});
		
		return true;
	}
	
	/**
	 * 对已放置的舱体进行tick
	 */
	private void tick() {
		// 停服状态下不做任何处理
		if (activityState.get() == MechaSpaceConst.ACTIVITY_STATE_SHUTDOWN) {
			return;
		}
		
		if (!isActivityOpen()) {
			activityClose();
			return;
		}
		
		// 处于初始化状态，或关闭状态
		if (activityState.get() != MechaSpaceConst.ACTIVITY_STATE_OPEN) {
			// 从关闭状态切换到开启状态
			if (!activityState.compareAndSet(MechaSpaceConst.ACTIVITY_STATE_CLOSE, MechaSpaceConst.ACTIVITY_STATE_OPEN)) {
				// 从初始化状态切换到开启状态
				activityState.compareAndSet(MechaSpaceConst.ACTIVITY_STATE_INIT, MechaSpaceConst.ACTIVITY_STATE_OPEN);
			}
			// 这里不直接设置成 ACTIVITY_STATE_OPEN 状态，是避免把 ACTIVITY_STATE_SHUTDOWN 状态也改掉了
		}
		
		for (MechaSpaceInfo spaceObj : guildSpaceMap.values()) {
			// 停服状态下不做任何处理
			if (activityState.get() == MechaSpaceConst.ACTIVITY_STATE_SHUTDOWN) {
				return;
			}
			
			if (spaceObj.getStage() != null) {
				try {
					spaceObj.getStage().onTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 活动状态变更
	 */
	private void activityClose() {
		// 起服后第一次tick
		activityState.compareAndSet(MechaSpaceConst.ACTIVITY_STATE_INIT, MechaSpaceConst.ACTIVITY_STATE_CLOSE);
		// 从开启状态转到关闭状态
		activityState.compareAndSet(MechaSpaceConst.ACTIVITY_STATE_OPEN, MechaSpaceConst.ACTIVITY_STATE_CLOSE);
		
		// 这里不直接设置成 ACTIVITY_STATE_CLOSE 状态，是避免把 ACTIVITY_STATE_SHUTDOWN 状态也改掉了
		
		// 进入停服处理状态就不再往下走了 
		if (activityState.get() == MechaSpaceConst.ACTIVITY_STATE_SHUTDOWN) {
			return;
		}
		
		for (MechaSpaceInfo spaceObj : guildSpaceMap.values()) {
			// 进入停服过程就不再处理了
			if (activityState.get() == MechaSpaceConst.ACTIVITY_STATE_SHUTDOWN) {
				return;
			}
			
			if (spaceObj.getStage() != null) {
				try {
					spaceObj.setMaxLevel(Math.max(spaceObj.getMaxLevel(), spaceObj.getNewMaxLevel()));
					spaceObj.setStage(null);
					spaceObj.syncSpaceMechaInfo();
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		
		// 活动关闭了，清理一下
		if (!guildSpaceRecordMap.isEmpty()) {
			List<String> guildList = new LinkedList<>(guildSpaceRecordMap.keySet());
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					RedisProxy.getInstance().getRedisSession().del(getRecordGuildKey(GsConfig.getInstance().getServerId()));
					for (String guildId : guildList) {
						RedisProxy.getInstance().getRedisSession().del(getRecordsKey(guildId).getBytes());
					}
					return null;
				}
			});
		}
		
		guildSpaceRecordMap.clear();
		enemyNpcPlayerMap.clear();
		guildSpaceMap.clear();
	}
	
	/**
	 * 放置舱体时间判断
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean placeSpaceTimeCheck() {
		SpaceGuardActivity activity = getActivityObject();
		if (activity == null) {
			return false;
		}
		SpaceGuardTimeCfg timeCfg = activity.getTimeCfg();
		return timeCfg.getStopTimeValue() > HawkTime.getMillisecond();
	}
	
	/**
	 * 放置联盟机甲舱体
	 * 
	 * @param player
	 * @param posX
	 * @param posY
	 * @param level 难度等级
	 */
	public boolean placeGuildSpace(Player player, int posX, int posY, int level) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		
		MechaSpaceInfo spaceObj = getGuildSpace(guildId);
		if (spaceObj != null && spaceObj.getStage() != null) {
			return false;
		}
		
		int x0 = posX;
		int y0 = posY - 1;
		SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(level);
		// 创建点
		AreaObject areaObj0 = WorldPointService.getInstance().getArea(x0, y0);
		int zoneId0 = WorldUtil.getPointResourceZone(x0, y0);
		SpaceWorldPoint spacePoint0 = new SpaceWorldPoint(x0, y0, areaObj0.getId(), zoneId0, WorldPointType.SPACE_MECHA_MAIN_VALUE);
		spacePoint0.setGuildId(guildId);
		spacePoint0.setOwnerId(guildId);
		spacePoint0.setPlaceTime(HawkTime.getMillisecond());
		spacePoint0.setSpaceLevel(level);
		spacePoint0.setSpaceIndex(SpacePointIndex.MAIN_SPACE);
		spacePoint0.setSpaceBlood(cfg.getBlood());
		WorldPointService.getInstance().addPoint(spacePoint0);
		
		SpaceMechaSubcabinCfg slaveSpaceCfg = SpaceMechaSubcabinCfg.getCfg(level);
		//子舱1
		int slaveSpace1X = posX - 1;
		int slaveSpace1Y = posY + 1;
		AreaObject areaObj1 = WorldPointService.getInstance().getArea(slaveSpace1X, slaveSpace1Y);
		int zoneId1 = WorldUtil.getPointResourceZone(slaveSpace1X, slaveSpace1Y);
		SpaceWorldPoint spacePoint1 = new SpaceWorldPoint(slaveSpace1X, slaveSpace1Y, areaObj1.getId(), zoneId1, WorldPointType.SPACE_MECHA_SLAVE_VALUE);
		spacePoint1.setGuildId(guildId);
		spacePoint1.setOwnerId(guildId);
		spacePoint1.setPlaceTime(spacePoint0.getPlaceTime());
		spacePoint1.setSpaceLevel(level);
		spacePoint1.setSpaceIndex(SpacePointIndex.SUB_SPACE_1);
		spacePoint1.setSpaceBlood(slaveSpaceCfg.getBlood());
		WorldPointService.getInstance().addPoint(spacePoint1);
		
		//子舱2
		int slaveSpace2X = posX + 1;
		int slaveSpace2Y = posY + 1;
		AreaObject areaObj2 = WorldPointService.getInstance().getArea(slaveSpace2X, slaveSpace2Y);
		int zoneId2 = WorldUtil.getPointResourceZone(slaveSpace2X, slaveSpace2Y);
		SpaceWorldPoint spacePoint2 = new SpaceWorldPoint(slaveSpace2X, slaveSpace2Y, areaObj2.getId(), zoneId2, WorldPointType.SPACE_MECHA_SLAVE_VALUE);
		spacePoint2.setGuildId(guildId);
		spacePoint2.setOwnerId(guildId);
		spacePoint2.setPlaceTime(spacePoint0.getPlaceTime());
		spacePoint2.setSpaceLevel(level);
		spacePoint2.setSpaceIndex(SpacePointIndex.SUB_SPACE_2);
		spacePoint2.setSpaceBlood(slaveSpaceCfg.getBlood());
		WorldPointService.getInstance().addPoint(spacePoint2);
		
		spaceObj = new MechaSpaceInfo(guildId);
		spaceObj.setStage(new SpacePrepareStage(guildId));
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		int maxLevel = guildInfoObj.getSpaceMaxLv();
		if (maxLevel > spaceObj.getMaxLevel()) {
			spaceObj.setMaxLevel(maxLevel);
		} else {
			int maxLv = Math.min(spaceObj.getMaxLevel(), SpaceMechaLevelCfg.getMaxLevel());
			guildInfoObj.setSpaceMaxLv(maxLv);
		}
		
		Map<Integer, SpaceWorldPoint> spacePointMap = spaceObj.getSpacePointMap();
		spacePointMap.put(SpacePointIndex.MAIN_SPACE, spacePoint0);
		spacePointMap.put(SpacePointIndex.SUB_SPACE_1, spacePoint1);
		spacePointMap.put(SpacePointIndex.SUB_SPACE_2, spacePoint2);
		
		guildSpaceMap.put(guildId, spaceObj);
		
		GuardRecordPB.Builder builder = GuardRecordPB.newBuilder();
		builder.setPlaceTime(spaceObj.getPlaceTime());
		builder.setPlayerName(player.getName());
		builder.setSpaceLevel(spaceObj.getLevel());
		builder.setGuardResult(MechaSpaceGuardResult.SPACE_GUARD_SUCC_VALUE);
		addGuildSpaceRecord(guildId, builder);
		
		HawkLog.logPrintln("spaceMecha place space, playerId: {}, guildId: {}, posX: {}, posY: {}, level: {}", player.getId(), player.getGuildId(), posX, posY, level);
		return true;
	}
	
	/**
	 * 获取联盟机甲舱体信息
	 * 
	 * @param guildId
	 * @return
	 */
	public MechaSpaceInfo getGuildSpace(String guildId) {
		MechaSpaceInfo obj = guildSpaceMap.get(guildId);
		return obj;
	}
	
	/**
	 * 获取联盟代币点数
	 * @param guildId
	 * @return
	 */
	public long getGuildPointCount(String guildId) {
		SpaceGuardActivity activity = getActivityObject();
		if (activity == null) {
			return 0L;
		}
		
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfoObj != null) {
			if (guildInfoObj.getSpaceMechaTermId() == activity.getActivityTermId()) {
				return guildInfoObj.getSpaceMechaGuildPoint();
			}
			guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
		}
		
		return 0;
	}
	
	/**
	 * 消耗代币
	 * @param guildId
	 * @param count
	 */
	public void consumeGuildPoint(String guildId, int count) {
		addGuildPoint(guildId, 0 - count);
	}
	
	/**
	 * 添加星币 
	 * @param guildId
	 * @param count
	 */
	public void addGuildPoint(String guildId, int count) {
		SpaceGuardActivity activity = getActivityObject();
		if (activity == null) {
			return;
		}
		
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfoObj != null) {
			if (guildInfoObj.getSpaceMechaTermId() != activity.getActivityTermId()) {
				guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
			}
			guildInfoObj.addSpaceMechaGuildPoint(count);
		}
	}
	
	/**
	 * 判断是否是星甲召唤相关的点
	 * 
	 * @param point
	 * @return
	 */
	public boolean isSpaceMechaPoint(WorldPoint point) {
		if (point == null) {
			return false;
		}
		
		int pointType = point.getPointType();
		if (pointType == WorldPointType.SPACE_MECHA_MAIN_VALUE 
				|| pointType == WorldPointType.SPACE_MECHA_SLAVE_VALUE 
				|| pointType == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE
				|| pointType == WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * 获取活动所需的舱体相关信息
	 * 
	 * @param guildId
	 * @return
	 */
	public SpaceMachineGuardActivityInfoPB.Builder getSpaceMechaInfo(String guildId) {
		GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildInfoObj == null) {
			return null;
		}
		
		MechaSpaceInfo spaceInfo = getGuildSpace(guildId);
		SpaceMachineGuardActivityInfoPB.Builder builder = SpaceMachineGuardActivityInfoPB.newBuilder();
		builder.setSpaceLevelMax(Math.max(1, guildInfoObj.getSpaceMaxLv()));    // 目前可挑战的舱体最高等级
		if (spaceInfo != null) {
			builder.setCurSpaceLevel(spaceInfo.getLevel());  // 当前选择的舱体等级
			if (spaceInfo.getStage() != null) {
				builder.setStage(spaceInfo.getStageVal().getNumber()); // 当前所处阶段
				builder.setStageEndTime((int)(spaceInfo.getStage().getEndTime() / 1000));   // 当前阶段的结束时间
			} else {
				builder.setStage(SpaceMechaStage.SPACE_END_VALUE);
			}
		} else {
			builder.setStage(SpaceMechaStage.SPACE_END_VALUE);
		}
		
		SpaceGuardActivity activity = getActivityObject(true);
		if (guildInfoObj.getSpaceMechaTermId() == activity.getActivityTermId()) {
			builder.setSelectLevel(guildInfoObj.getSpaceSelectedLv());
			builder.setSpaceSetTimes(guildInfoObj.getSpaceSetTimes());
		} else {
			guildInfoObj.resetSpaceMechaData(activity.getActivityTermId());
			builder.setSelectLevel(0);
			builder.setSpaceSetTimes(0);
		}
		
		return builder;
	}
	
	/**
	 * 获取主舱体的加成作用号
	 * 
	 * @param playerId
	 * @param effId
	 * @param effParams
	 * @return
	 */
	public int getPlayerEffVal(String playerId, int effId, EffectParams effParams) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		try {
			String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if (HawkOSOperator.isEmptyString(guildId)) {
				return 0;
			}
			
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(effParams.getBattlePoint());
			if (point == null || (point.getPointType() !=  WorldPointType.SPACE_MECHA_MAIN_VALUE 
					&& point.getPointType() != WorldPointType.SPACE_MECHA_SLAVE_VALUE
					&& point.getPointType() != WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE)) {
				return 0;
			}
			
			MechaSpaceInfo space = getGuildSpace(guildId);
			if (space == null) {
				return 0;
			}
			
			int curbuff = 0;
			if (effParams.getMarch() != null) {
				// 据点取防守敌军的克制buff
				if (point.getPointType() == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
					StrongHoldWorldPoint strongHoldPoint = (StrongHoldWorldPoint) point;
					SpaceMechaStrongholdCfg cfg = strongHoldPoint.getStrongHoldCfg();
					return cfg == null ? 0 : cfg.getCurbBuff(effId);
				}
				
				// 舱体点取进攻敌军的克制buff
				int enemyId = space.getMarchEnemyMap().getOrDefault(effParams.getMarch().getMarchId(), 0);
				SpaceMechaEnemyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
				if (cfg != null) {
					curbuff = cfg.getCurbBuff(effId);
				}
			}
			
			// 主舱取加成buff + 克制buff
			if (point.getPointType() ==  WorldPointType.SPACE_MECHA_MAIN_VALUE) {
				return space.getSpaceEffVal(effId) + curbuff;
			} else if (point.getPointType() == WorldPointType.SPACE_MECHA_SLAVE_VALUE) {
				// 子舱只取克制buff
				return curbuff; 
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		return 0;
	}
	
	/**
	 * 获取npc的加成作用号
	 * 
	 * @param effId
	 * @param effParams
	 * @return
	 */
	public int getNpcEffVal(String guildId, int effId, EffectParams effParams) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(effParams.getBattlePoint());
		if (point == null || point.getPointType() !=  WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
			return 0;
		}
		
		MechaSpaceInfo space = getGuildSpace(guildId);
		if (space == null || space.getStageVal() != SpaceMechaStage.SPACE_GUARD_3) {
			return 0;
		}
		
		return space.getBossEffVal(effId);
	}
	
	/**
	 * 获取本期活动内联盟放置的所有舱体信息
	 * 
	 * @param guildId
	 * @return
	 */
	public List<GuardRecordPB.Builder> getGuildSpaceRecord(String guildId) {
		if (!guildSpaceRecordMap.containsKey(guildId)) {
			return Collections.emptyList();
		}
		
		List<GuardRecordPB.Builder> list = new ArrayList<>();
		BlockingDeque<GuardRecordPB.Builder> records = guildSpaceRecordMap.get(guildId);
		MechaSpaceInfo obj = getGuildSpace(guildId);
		// 没有正在进行中的
		if (obj == null || obj.getStage() == null) {
			list.addAll(records);
			return list;
		}
		
		GuardRecordPB.Builder builder = records.getLast();
		for (GuardRecordPB.Builder record : records) {
			// 当前正在进行的阶段不展示
			if (record != builder) {
				list.add(record);
			}
		}
		
		return list;
	}
	
	/**
	 * 添加舱体记录
	 * 
	 * @param guildId
	 * @param record
	 */
	public void addGuildSpaceRecord(String guildId, GuardRecordPB.Builder record) {
		BlockingDeque<GuardRecordPB.Builder> records =  guildSpaceRecordMap.putIfAbsent(guildId, new LinkedBlockingDeque<GuardRecordPB.Builder>());
		if (records == null) {
			records = guildSpaceRecordMap.get(guildId);
		}
		records.add(record);
	}
	
	/**
	 * 获取最近的一个记录
	 * @param guildId
	 * @return
	 */
	public GuardRecordPB.Builder getLatestSpaceRecord(String guildId) {
		if (!guildSpaceRecordMap.containsKey(guildId)) {
			return null;
		}
		
		return guildSpaceRecordMap.get(guildId).getLast();
	}
	
	/**
	 * 将舱体记录数据刷到redis中
	 * @param guildId
	 */
	public void flushSpaceRecordToRedis(String guildId) {
		GuardRecordPB.Builder recordBuilder = getLatestSpaceRecord(guildId);
		if (recordBuilder == null) {
			return;
		}
		
		int expireSeconds = getRedisExpireSeconds();
		RedisProxy.getInstance().getRedisSession().sAdd(getRecordGuildKey(GsConfig.getInstance().getServerId()), expireSeconds, guildId);
		String placeTime = String.valueOf(recordBuilder.getPlaceTime());
		byte[] bytes = recordBuilder.build().toByteArray();
		RedisProxy.getInstance().getRedisSession().hSetBytes(getRecordsKey(guildId), placeTime, bytes, expireSeconds);
	}
	
	/**
	 * 获取redis数据存储过期时间
	 * 
	 * @return
	 */
	private int getRedisExpireSeconds() {
		SpaceGuardActivity activity = getActivityObject(true);
		long remainTime = activity.getTimeCfg().getHiddenTimeValue() - HawkTime.getMillisecond();
		int second = (int) (remainTime / 1000) + 60;
		return second;
	}
	
	/**
	 * 加载舱体记录数据
	 */
	private void loadSpaceRecordData() {
		if (!isActivityOpen()) {
			return;
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		Set<String> guildIds = RedisProxy.getInstance().getRedisSession().sMembers(getRecordGuildKey(serverId));
		// 针对合服的处理
		List<String> serverList = AssembleDataManager.getInstance().getMergedServerList(serverId);
		if (serverList != null && !serverList.isEmpty()) {
			int expireSeconds = getRedisExpireSeconds();
			for (String server : serverList) {
				if (serverId.equals(server)) {
					continue;
				}
				Set<String> serverGuildIds = RedisProxy.getInstance().getRedisSession().sMembers(getRecordGuildKey(server));
				if (!serverGuildIds.isEmpty()) {
					guildIds.addAll(serverGuildIds);
					RedisProxy.getInstance().getRedisSession().del(getRecordGuildKey(server));
					RedisProxy.getInstance().getRedisSession().sAdd(getRecordGuildKey(serverId), expireSeconds, serverGuildIds.toArray(new String[serverGuildIds.size()]));
				}
			}
		}
		
		if (guildIds.isEmpty()) {
			return;
		}
		
		for (String guildId : guildIds) {
			String key = getRecordsKey(guildId);
			Map<byte[], byte[]> records = RedisProxy.getInstance().getRedisSession().hGetAllBytes(key.getBytes());
			for (Entry<byte[], byte[]> entry : records.entrySet()) {
				try {
					GuardRecordPB.Builder builder = GuardRecordPB.newBuilder().mergeFrom(entry.getValue());
					addGuildSpaceRecord(guildId, builder);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 判断玩家当天是否发起过联盟号召
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean spaceMechaGuildCall(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.GUILD_OFFICER_NOTICE);
		if (customData  == null) {
			return false;
		}
		
		return HawkTime.isSameDay(customData.getValue() * 1000L, HawkTime.getMillisecond());
	}
	
	/**
	 * 获取当日攻击据点奖励次数
	 * 
	 * @param player
	 * @return
	 */
	public int getAtkStrongHoldAwardTimesToday(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.ATK_STRONG_AWARD);
		if (customData == null) {
			player.getData().createCustomDataEntity(MechaSpaceConst.ATK_STRONG_AWARD, 0, "0");
			return 0;
		}
		
		String oldId = customData.getArg();
		MechaSpaceInfo obj = getGuildSpace(player.getGuildId());
		String newId = obj == null ? "" : obj.getId();
		if (newId.equals(oldId)) {
			return customData.getValue();
		} 
		
		return 0;
	}
	
	/**
	 * 添加进攻据点获奖次数
	 * 
	 * @param player
	 */
	public void addAtkStrongHoldAwardTimes(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.ATK_STRONG_AWARD);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(MechaSpaceConst.ATK_STRONG_AWARD, 0, "0");
		}
		
		String oldId = customData.getArg();
		String newId = getGuildSpace(player.getGuildId()).getId();
		customData.setArg(newId);
		if (newId.equals(oldId)) {
			customData.setValue(customData.getValue() + 1);
		} else {
			customData.setValue(1);
		}
	}
	
	/**
	 * 获取当日攻击据点次数
	 * 
	 * @param player
	 * @return
	 */
	public int getAtkStrongHoldTimesToday(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.ATK_STRONG_TIMES);
		if (customData == null) {
			player.getData().createCustomDataEntity(MechaSpaceConst.ATK_STRONG_TIMES, 0, "0");
			return 0;
		}
		
		String oldId = customData.getArg();
		MechaSpaceInfo obj = getGuildSpace(player.getGuildId());
		String newId = obj == null ? "" : obj.getId();
		if (newId.equals(oldId)) {
			return customData.getValue();
		} 
		
		return 0;
	}
	
	/**
	 * 添加进攻据点次数
	 * 
	 * @param player
	 */
	public void addAtkStrongHoldTimes(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(MechaSpaceConst.ATK_STRONG_TIMES);
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(MechaSpaceConst.ATK_STRONG_TIMES, 0, "0");
		}
		
		String oldId = customData.getArg();
		String newId = getGuildSpace(player.getGuildId()).getId();
		customData.setArg(newId);
		if (newId.equals(oldId)) {
			customData.setValue(customData.getValue() + 1);
		} else {
			customData.setValue(1);
		}
	}
	
	/**
	 * 根据野怪配置获取npc player
	 * @param enemyCfg
	 * @return
	 */
	public NpcPlayer getNpcPlayer(SpaceMechaEnemyCfg enemyCfg) {
		NpcPlayer npcPlayer = enemyNpcPlayerMap.get(enemyCfg.getId());
		if (npcPlayer != null) {
			return npcPlayer;
		}
		
		npcPlayer = new NpcPlayer(HawkXID.nullXid());
		npcPlayer.setPlayerId(String.valueOf(enemyCfg.getId()));
		List<PlayerHero> heros = NPCHeroFactory.getInstance().get(enemyCfg.getHeroIdList());
		npcPlayer.setHeros(heros);
		enemyNpcPlayerMap.put(enemyCfg.getId(), npcPlayer);
		return npcPlayer;
	}
	
	/**
	 * 遣返行军
	 * 
	 * @param player
	 * @param targetPlayerId
	 * @param spacePoint
	 * @return
	 */
	public boolean repatriateSpaceDefMarch(Player player, String targetPlayerId, SpaceWorldPoint spacePoint) {
		MechaSpaceInfo spaceObj = SpaceMechaService.getInstance().getGuildSpace(player.getGuildId());
		if (spaceObj == null || spaceObj.getStage() == null) {
			return false;
		}
		
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		Player leader = spacePoint.getLeader();
		if (!guildAuthority && (leader == null || !player.getId().equals(leader.getId()))) {
			return false;
		}
		
		spacePoint.forceMarchBack(targetPlayerId);
		syncSpaceQuarterMarchInfo(player, spacePoint);
		return true;
	}
	
	/**
	 * 更换队长
	 * 
	 * @param player
	 * @param worldPoint
	 * @param targetPlayerId
	 * @return
	 */
	public int changeLeader(Player player, WorldPoint worldPoint, String targetPlayerId) {
		// 没有联盟或者不是本联盟的舱体
		if (!player.hasGuild() || !player.getGuildId().equals(worldPoint.getGuildId())) {
			return Status.Error.SPACE_MECHA_CHANGE_LEADER_AUTH_EER_VALUE;
		}
		
		MechaSpaceInfo spaceInfo = SpaceMechaService.getInstance().getGuildSpace(worldPoint.getGuildId());
		if (spaceInfo == null || spaceInfo.getStage() == null || spaceInfo.getStageVal() == SpaceMechaStage.SPACE_GUARD_4) {
			return Status.Error.SPACE_MECHA_CHANGE_LEADER_TIME_ERR_VALUE;
		}
		SpaceWorldPoint spacePoint = (SpaceWorldPoint) worldPoint;
		Player leader = spacePoint.getLeader();
		boolean guildAuthority = GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_MANOR_SET);
		boolean isLeader = player.getId().equals(leader.getId());
		if (!guildAuthority && !isLeader) {
			return Status.Error.SPACE_MECHA_CHANGE_LEADER_AUTH_EER_VALUE;
		}
		
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.CHANGE_SPACE_MECHA_LEADER) {
			@Override
			public boolean onInvoke() {
				spacePoint.changeMarchLeader(targetPlayerId);
				SpaceMechaService.getInstance().syncSpaceQuarterMarchInfo(player, spacePoint);
				return true;
			}
		});
		
		return 0;
	}
	
	/**
	 * 同步舱体驻扎行军信息
	 * 
	 * @param spacePoint
	 * @param player
	 */
	public void syncSpaceQuarterMarchInfo(Player player, SpaceWorldPoint spacePoint) {
		MechaSpaceQuarterInfoResp.Builder builder = MechaSpaceQuarterInfoResp.newBuilder();
		BlockingDeque<String> marchs = spacePoint.getDefMarchs();
		for (String marchId : marchs) {
			IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
			if (march != null) {
				builder.addQuarterMarch(buildQuarterMarch(marchId));
			}
		}
		
		String leaderMarchId = spacePoint.getLeaderMarchId();
		IWorldMarch leaderMarch = WorldMarchService.getInstance().getMarch(leaderMarchId);
		if (leaderMarch != null) {
			int maxMassJoinSoldierNum = leaderMarch.getMaxMassJoinSoldierNum(leaderMarch.getPlayer());
			builder.setMassSoldierNum(maxMassJoinSoldierNum);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.SPACE_MECHA_QUARTER_RESP, builder));
	}
	
	/**
	 * 构建驻扎行军信息
	 * 
	 * @param marchId
	 * @return
	 */
	private MechaSpaceQuarterMarch.Builder buildQuarterMarch(String marchId) {
		MechaSpaceQuarterMarch.Builder builder = MechaSpaceQuarterMarch.newBuilder();
		
		IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
		String playerId = march.getPlayerId();
		Player snapshot = GlobalData.getInstance().makesurePlayer(playerId);
		
		builder.setPlayerId(snapshot.getId());
		builder.setName(snapshot.getName());
		builder.setIcon(snapshot.getIcon());
		builder.setPfIcon(snapshot.getPfIcon());
		builder.setGuildTag(snapshot.getGuildTag());
		builder.setMarchId(marchId);
		
		List<ArmyInfo> armys = march.getMarchEntity().getArmys();
		for (ArmyInfo army : armys) {
			builder.addArmy(army.toArmySoldierPB(snapshot).build());
		}
		for (PlayerHero hero : march.getHeros()) {
			builder.addHeroId(hero.getCfgId());
		}
		List<PlayerHero> heroList = snapshot.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : heroList) {
			builder.addHero(hero.toPBobj());
		}
		SuperSoldier ssoldier = snapshot.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId()).orElse(null);
		if(Objects.nonNull(ssoldier)){
			builder.setSsoldier(ssoldier.toPBobj());
		}
		
		return builder;
	}
	
	/**
	 * 获取 CustomDataEntity 数据
	 * @param player
	 * @param key
	 * @return
	 */
	public CustomDataEntity getCustomDataEntity(Player player, String key) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(key);
		SpaceGuardActivity activity = getActivityObject(true);
		int termId = activity.getTimeCfg().getTermId();
		if (customData == null) {
			customData = player.getData().createCustomDataEntity(key, 0, String.valueOf(termId));
		} else if (Integer.parseInt(customData.getArg()) != termId) {
			customData.setArg(String.valueOf(termId));
			customData.setValue(0);
		}
		
		return customData;
	}
	
	/**
	 * 活动是否开放
	 */
	public boolean isActivityOpen() {
		return getActivityObject() != null;
	}
	
	/**
	 * 获取活动对象
	 * 
	 * @param ignore 是否忽略活动状态
	 * 
	 * @return
	 */
	public SpaceGuardActivity getActivityObject(boolean ignore) {
		Optional<ActivityBase> activityOp = ActivityManager.getInstance().getGameActivityByType(ActivityType.SPACE_MACHINE_GUARD_VALUE);
		if(!activityOp.isPresent()) {
			return null;
		}
		
		ActivityBase activity = activityOp.get();
		if (!ignore && (activity.isInvalid() || activity.getActivityEntity().getActivityState() != ActivityState.OPEN)) {
			return null;
		}
		
		return (SpaceGuardActivity) activity;
	}
	
	/**
	 * 获取活动对象
	 * 
	 * @return
	 */
	public SpaceGuardActivity getActivityObject() {
		return getActivityObject(false);
	}
	
	/**
	 * 停服处理
	 */
	public void onShutdown() {
		if (!isActivityOpen()) {
			return;
		}
		
		activityState.set(MechaSpaceConst.ACTIVITY_STATE_SHUTDOWN);
		
		for (MechaSpaceInfo spaceObj : guildSpaceMap.values()) {
			if (spaceObj.getStage() == null) {
				continue;
			}
			
			try {
				SpaceMechaCabinCfg cfg = SpaceMechaCabinCfg.getCfgByLevel(spaceObj.getLevel());
				// 补星币
				addGuildPoint(spaceObj.getGuildId(), cfg.getCost());
				GuildInfoObject guildInfoObj = GuildService.getInstance().getGuildInfoObject(spaceObj.getGuildId());
				int oldCount = guildInfoObj.getSpaceSetTimes();
				// 补放置次数
				guildInfoObj.setSpaceSetTimes(oldCount - 1);
				
				SpaceWorldPoint spacePoint = spaceObj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE);
				// 发联盟消息
				Object[] objects = new Object[] { spacePoint.getX(), spacePoint.getY(), cfg.getId(), cfg.getCost() };
				ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_SHUTDOWN_NOTICE).setGuildId(spaceObj.getGuildId()).addParms(objects).build());
				
				Object[] object = new Object[] { spaceObj.getLevel(), cfg.getCost() };
				// 给盟主和官员发告知邮件
				for (String playerId : GuildService.getInstance().getGuildMembers(spaceObj.getGuildId())) {
					GuildMemberObject memberObj = GuildService.getInstance().getGuildMemberObject(playerId);
					if (memberObj != null && memberObj.getOfficeId() != GuildOffice.NONE.value()) {
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(playerId)
								.setMailId(MailId.SPACE_MECHA_SHUTDOWN_ISSUE)
								.addContents(object)
								.setAwardStatus(MailRewardStatus.GET)
								.build());
					}
				}
				
				// 把部队遣返
				spaceObj.forceAllSpaceMarchBack();
				// 清除世界上的相关点
				spaceObj.clearPoint();
				// 将舱体记录信息更新并刷到redis
				spaceObj.getStage().updateAndRecord(true, MechaSpaceGuardResult.SPACE_GUARD_BREAK);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 触发世界生成野怪(需要抛到世界线程处理)
	 */
	public List<WorldPoint> generateMonster(String guildId, int monsterType, int count, Collection<Integer> enemyIds) {
		MechaSpaceInfo spaceObj = getGuildSpace(guildId);
		int[] xy = GameUtil.splitXAndY(spaceObj.getSpacePointId(SpacePointIndex.MAIN_SPACE));
		List<WorldPoint> createPoints = new ArrayList<>();
		Iterator<Integer> iter = enemyIds.iterator();
		for (int i = 0; i < count; i++) {
			int enemyId = iter.hasNext() ? iter.next() : 0;
			WorldPoint point = createMonster(xy[0], xy[1], guildId, monsterType, 0, WorldPointType.SPACE_MECHA_MONSTER_VALUE, enemyId);
			if (point == null) {
				continue;
			}
			createPoints.add(point);
			spaceObj.addEnemyPointId(point.getId());
		}
		
		return createPoints;
	}
	
	/**
	 * 刷出据点
	 * @param guildId
	 * @param count
	 * @param strongHoldId
	 */
	public List<Integer> generateStrongHold(String guildId, int count, int strongHoldId) {
		List<Integer> pointList = new ArrayList<>();
		MechaSpaceInfo spaceObj = getGuildSpace(guildId);
		int[] xy = GameUtil.splitXAndY(spaceObj.getSpacePointId(SpacePointIndex.MAIN_SPACE));
		List<WorldPoint> createPoints = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			WorldPoint worldPoint = createMonster(xy[0], xy[1], guildId, MonsterType.TYPE_14_VALUE, strongHoldId, WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE);
			if (worldPoint == null) {
				continue;
			}
			
			StrongHoldWorldPoint point = (StrongHoldWorldPoint) worldPoint;
			spaceObj.setSpStrongHoldPoint(point);
			createPoints.add(point);
			spaceObj.addEnemyPointId(point.getId());
			pointList.add(point.getId());
		}
		
		return pointList;
	}
	
	/**
	 * 刷出野怪点
	 * @return
	 */
	public WorldPoint createMonster(int centerX, int centerY, String guildId, int monsterType, int strongHoldId, int pointType) {
		return createMonster(centerX, centerY, guildId, monsterType, strongHoldId, pointType, 0);
	}
	
	/**
	 * 刷出野怪点
	 */
	public WorldPoint createMonster(int centerX, int centerY, String guildId, int monsterType, int strongHoldId, int pointType, int enemyId) {
		int grid = SpaceMechaGrid.MONSTER_GRID;
		if (pointType == WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			grid = SpaceMechaGrid.STRONG_HOLD_GRID;
		}
		final int radiusAddDelta = 5, roundMax = 5;
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		int randomRadius = cfg.getMinRefreshDistance();
		int round = 0;
		do {
			round++;
			List<Point> pointList = WorldPointService.getInstance().getRhoAroundPointsFree(centerX, centerY, randomRadius);
			randomRadius += radiusAddDelta;
			List<Point> points = WorldPointService.getInstance().getRhoAroundPointsFree(centerX, centerY, randomRadius);
			points.removeAll(pointList);
			Collections.shuffle(points);
			
			for (Point point : points) {
	            if (!point.canSpaceMechaSeat(pointType)) {
	            	continue;
	            }
	            
	            AreaObject area = WorldPointService.getInstance().getArea(point.getAreaId());
				if (!WorldPointService.getInstance().tryOccupied(area, point, grid)) {
					continue;
				}
				if (WorldPointService.getInstance().getWorldPoint(point.getId()) != null) {
					continue;
				}
				
				int monsterId = 0;
				MechaSpaceInfo spaceObj = getGuildSpace(guildId);
				SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, spaceObj.getLevel());
				// 普通怪
				if (monsterType == MonsterType.TYPE_12_VALUE) {
					monsterId = enemyId > 0 ? enemyId : HawkRand.randomWeightObject(levelCfg.getStage1EnemyIdList(), levelCfg.getStage1EnemyWeightList());
				} else if(monsterType == MonsterType.TYPE_13_VALUE) {
					// 精英怪
					monsterId = enemyId > 0 ? enemyId : HawkRand.randomWeightObject(levelCfg.getStage1SpEnemyIdList(), levelCfg.getStage1SpEnemyWeightList());
				} else if(monsterType == MonsterType.TYPE_14_VALUE) {
					// 据点
					SpaceMechaStrongholdCfg holdCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, strongHoldId);
					if (holdCfg == null) {
						break;
					}
					
					monsterId = strongHoldId;
					StrongHoldWorldPoint worldPoint = new StrongHoldWorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), pointType);
					worldPoint.setGuildId(guildId);
					worldPoint.setMonsterId(monsterId);
					worldPoint.setStrongHoldId(strongHoldId);
					worldPoint.setLifeStartTime(HawkTime.getMillisecond());
					
					enemyId = HawkRand.randomWeightObject(holdCfg.getAtkEnemyIdList(), holdCfg.getAtkEnemyWeightList());
					worldPoint.setStrongHoldId(strongHoldId);
					worldPoint.storeEnemyId(enemyId);
					worldPoint.setHpNum(1);
					worldPoint.setDefArmyList(holdCfg.getArmyList());
					worldPoint.setRemainBlood(holdCfg.getBlood());
					// 特殊据点
					if (holdCfg.getIsSpecial() > 0) {
						worldPoint.setSpecial(1);
					}
					WorldPointService.getInstance().addPoint(worldPoint);
					
					HawkLog.debugPrintln("spaceMecha create stronghold, guildId: {}, monsterType: {}, posX: {}, posY: {}, strongHoldId: {}", guildId, monsterType, worldPoint.getX(), worldPoint.getY(), strongHoldId);
					return worldPoint;
					
				} else if (monsterType == MonsterType.TYPE_15_VALUE) {
					// boss
					monsterId = levelCfg.getStage3Boss();
				}
				
				WorldPoint worldPoint = new WorldPoint(point.getX(), point.getY(), point.getAreaId(), point.getZoneId(), pointType);
				worldPoint.setGuildId(guildId);
				worldPoint.setMonsterId(monsterId);
				worldPoint.setLifeStartTime(HawkTime.getMillisecond());
				WorldPointService.getInstance().addPoint(worldPoint);
				
				HawkLog.debugPrintln("spaceMecha create monster, guildId: {}, monsterType: {}, posX: {}, posY: {}, monsterId: {}", guildId, monsterType, worldPoint.getX(), worldPoint.getY(), monsterId);
				
				return worldPoint;
			} 
		} while (round < roundMax);
		
		return null;
	}
	
	/**
	 * 获取活动期数
	 * 
	 * @return
	 */
	private int getTermId() {
		SpaceGuardActivity activity = getActivityObject(true);
		int termId = activity.getTimeCfg().getTermId();
		return termId;
	}
	
	/**
	 * 获得星币打点
	 * @param player
	 * @param termId
	 * @param taskId  任务id
	 * @param pointCount  获得星币数量
	 * @param guildPointCount  联盟星币数量
	 * @param taskLimitGap  该任务上限余量
	 */
	public void logSpaceMechaPointGet(Player player, int termId, int taskId, int pointCount, long guildPointCount, int taskLimitGap) {
		// 1. 玩家获得星币时，记录玩家信息、任务id、获得星币数量、该任务上限余量、联盟星币数量
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.space_mecha_point);
		    logParam.put("guildId", player.getGuildId())
		    		.put("termId", termId)
					.put("taskId", taskId)
					.put("pointCount", pointCount)
					.put("taskLimitGap", taskLimitGap)
					.put("guildPointCount", guildPointCount);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 放置舱体打点
	 * @param player
	 * @param spaceObj
	 * @param posX
	 * @param posY
	 */
	public void logSpaceMechaPlace(Player player, MechaSpaceInfo spaceObj, int posX, int posY, int times) {
		// 2. 联盟放置太空舱时，记录联盟id、联盟人数、玩家id、消耗星币数量、放置的时间、坐标、难度等级、剩余放置次数
		SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		try {
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.space_mecha_place);
		    logParam.put("guildId", spaceObj.getGuildId())
		    		.put("termId", getTermId())
					.put("memberCount", GuildService.getInstance().getGuildMembers(spaceObj.getGuildId()).size())
					.put("consumeCnt", spaceObj.getCost())
					.put("posX", posX)
					.put("posY", posY)
					.put("spaceLevel", spaceObj.getLevel())
					.put("setTimes", times)
					.put("remainTimes", constCfg.getSetLimitNum() - times);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录舱体防守打点
	 * 
	 * @param spaceObj
	 * @param spacePoint
	 * @param reduceBlood
	 */
	public void logSpaceMechaDefWar(MechaSpaceInfo spaceObj, SpaceWorldPoint spacePoint, int reduceBlood) {
		// 4. 玩法在主舱、子舱中发生战斗时，记录联盟id、难度等级、玩法阶段、波次、战斗建筑id、时间、对应建筑的剩余血量、扣血量、建筑内防守人数、可挑战的最高等级
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.space_mecha_def_war);
			logParam.put("guildId", spaceObj.getGuildId())
					.put("termId", getTermId())
					.put("spaceLevel", spaceObj.getLevel())
					.put("stage", spaceObj.getStageVal().getNumber())
					.put("wave", spaceObj.getStage().getRound())
					.put("buildId", spacePoint.getSpaceCfgId())
					.put("remainBlood", spacePoint.getSpaceBlood())
					.put("reduceBlood", reduceBlood)
					.put("defMemberCnt", spacePoint.getDefenceMembers().size())
					.put("maxLevel", spaceObj.getMaxLevel())
					.put("spaceIndex", spacePoint.getSpaceIndex());
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录进攻据点打点日志
	 * @param player
	 * @param spaceObj
	 * @param strongHoldPoint
	 */
	public void logAtkStrongHold(Player player, StrongHoldWorldPoint strongHoldPoint) {
		// 5. 玩家进攻据点时，记录玩家信息、难度等级、联盟id、据点id、据点剩余血量（管+数量，即第几管血还剩多少）、奖励可领取剩余次数、时间、是否为特殊据点、加成buffid
		try {
			MechaSpaceInfo spaceObj = getGuildSpace(player.getGuildId());
			int awardTimes = SpaceMechaService.getInstance().getAtkStrongHoldAwardTimesToday(player);
			SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
			int remainRewardTimes = constCfg.getStrongholdAwardLimit() - awardTimes;
			
			HawkTuple2<Integer, Integer> tuple = strongHoldPoint.getEffectTuple();
			int buffId = tuple == null ? 0 : tuple.first;
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.space_mecha_atk_stronghold);
		    logParam.put("guildId", spaceObj.getGuildId())
		    		.put("termId", getTermId())
		    		.put("spaceLevel", spaceObj.getLevel())
					.put("strongHoldId", strongHoldPoint.getStrongHoldId())
					.put("hpNum", strongHoldPoint.getHpNum())
					.put("remainBlood", strongHoldPoint.getRemainBlood())
					.put("special", strongHoldPoint.getSpecial())
					.put("buffId", buffId)
					.put("remainRewardTimes", Math.max(remainRewardTimes, 0));
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 记录采集宝箱打点
	 * @param player
	 * @param boxId
	 */
	public void logSpaceMechaCollect(Player player, int boxId) {
		// 6. 玩家采集宝箱时，记录玩家信息、难度等级、联盟id、宝箱id、采集开始时间
		try {
			MechaSpaceInfo spaceObj = getGuildSpace(player.getGuildId());
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.space_mecha_collect);
		    logParam.put("guildId", player.getGuildId())
		    		.put("termId", getTermId())
		    		.put("spaceLevel", spaceObj.getLevel())
		    		.put("boxId", boxId);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 阶段结束打点记录
	 * @param spaceObj
	 * @param timeLong 阶段持续时长
	 */
	public void logSpaceMechaStageEnd(MechaSpaceInfo spaceObj, long timeLong) {
		// 7. 阶段变化时，记录联盟id、难度等级、变化阶段（取结束的阶段）、变化时间、主舱和子舱剩余血量
		try {
			LogParam logParam = LogUtil.getNonPersonalLogParam(LogInfoType.space_mecha_stage_end);
			logParam.put("guildId", spaceObj.getGuildId())
					.put("termId", getTermId())
					.put("spaceLevel", spaceObj.getLevel())
					.put("stage", spaceObj.getStageVal().getNumber())
					.put("mainSpaceBlood", spaceObj.getSpaceBlood(SpacePointIndex.MAIN_SPACE))
					.put("space1Blood", spaceObj.getSpaceBlood(SpacePointIndex.SUB_SPACE_1))
					.put("space2Blood", spaceObj.getSpaceBlood(SpacePointIndex.SUB_SPACE_2))
					.put("continueTime", timeLong / 1000);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 玩家进入或离开舱体的记录
	 * 
	 * @param player
	 * @param spacePoint
	 * @param joinSpace  1 进入舱体，0离开舱体
	 */
	public void logSpaceMechaDefChange(Player player, SpaceWorldPoint spacePoint, boolean joinSpace) {
		// 9. 玩家进出舱体建筑时，记录玩家信息、联盟id、难度等级、建筑id、时间、阶段、对应建筑的剩余血量
		try {
			MechaSpaceInfo spaceObj = getGuildSpace(spacePoint.getGuildId());
			LogParam logParam = LogUtil.getPersonalLogParam(player, LogInfoType.space_mecha_def_change);
		    logParam.put("guildId", player.getGuildId())
		    		.put("termId", getTermId())
		    		.put("spaceLevel", spaceObj.getLevel())
		    		.put("buildId", spacePoint.getSpaceCfgId())
		    		.put("stage", spaceObj.getStageVal().getNumber())
		    		.put("remainBlood", spacePoint.getSpaceBlood())
		    		.put("spaceIndex", spacePoint.getSpaceIndex())
		    		.put("joinSpace", joinSpace ? 1 : 0);
			GameLog.getInstance().info(logParam);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 获取存储舱体记录的redis key
	 * @return
	 */
	private String getRecordsKey(String guildId) {
		return "MechaSpaceRecord:" + guildId;
	}
	
	/**
	 * 联盟ID存储key
	 * 
	 * @return
	 */
	private String getRecordGuildKey(String serverId) {
		return "MechaSpaceRecordGuild:" + serverId;
	}
	
}
