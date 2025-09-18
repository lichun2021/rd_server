package com.hawk.game.module.spacemecha.stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaStrongholdCfg;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMonsterAttackMarch;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.module.spacemecha.worldpoint.StrongHoldWorldPoint;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.GuardRecordStagePB;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.SpaceMecha.EnemyAtkInfo;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.SpaceMecha.Stage2DetailPB;
import com.hawk.game.protocol.SpaceMecha.StrongHoldPB;
import com.hawk.game.protocol.SpaceMecha.StrongHoldStatus;
import com.hawk.game.protocol.SpaceMecha.WaveEnemyAtkInfo;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPointService;

/**
 * 防守阶段2
 * 
 * 1.主舱附近将刷出N个敌军据点
 * 2.敌军据点将在阶段2内持续刷出普通敌军进攻主舱
 * 3.敌军据点可被我方进攻（需要标识一个特殊据点）
 * 4.击破据点可获得效果
 * 5.阶段2倒计时结束时，未被击破的据点，将在阶段3消失并生成1支普通敌军进攻主舱
 * 6.阶段2倒计时结束，且主舱血条未被清空，则继续进入防守阶段3
 * 
 * @author lating
 *
 */
public class SpaceGuard2Stage extends ISpaceMechaStage {
	
	/** 下一轮开始时间 */
	private long nextPushMarchTime;
	
	/** 当前轮次 */
	private int round;
	/**
	 * 据点信息<worldPoint, strongHoldCfgId>
	 */
	private Map<Integer, Integer> pointHoldMap = new HashMap<>();
	/**
	 * 各个据点发出的敌军信息
	 */
	private Map<Integer, Integer> pointEnemyMap = new HashMap<>();
	/**
	 * 总的轮次和轮次间隔时间
	 */
	private int wave;
	private long waveCd;
	private long firstWaveTime;
	
	/**
	 * 记录信息
	 */
	GuardRecordStagePB.Builder stageBuilder;
	
	public SpaceGuard2Stage(String guildId) {
		super(guildId, SpaceMechaStage.SPACE_GUARD_2_VALUE);
	}

	@Override
	public boolean onTick() {
		if (checkMainSpaceDefenceFailed()) {
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (timeNow >= this.getEndTime()) {
			stageEnd();
		} else {
			// 阶段2后T秒后，开始首波据点普通敌军的进攻行军
			round(timeNow);
		}
		
		return true;
	}
	
	@Override
	public SpaceMechaStage getStageVal() {
		return SpaceMechaStage.SPACE_GUARD_2;
	}
	
	/**
	 * 阶段初始化
	 * 
	 * @param timeNow
	 */
	public void init(long timeNow) {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage init, guildId: {}, exist enemy: {}", getGuildId(), obj.getEnemyPointIds());
		recordInfo();
		// 阶段2刷出敌军据点
		createStrongHoldPoint();
		// 联盟发送消息，提醒阶段2开始
		stageChangeSync(NoticeCfgId.SPACE_MECHA_STAGE_2_START);
		
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		firstWaveTime = this.getStartTime() + cfg.getStrongholdFirstWaveTime();
		if (cfg.getStrongholdFirstWaveTime() <= 0) {
			round(timeNow);
		}
	}
	
	/**
	 * 阶段结束处理
	 */
	public void stageEnd() {
		updateAndRecord(false);
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		// 记录阶段变化
		SpaceMechaService.getInstance().logSpaceMechaStageEnd(spaceObj, HawkTime.getMillisecond() - this.getStartTime());
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage end, guildId: {}, sp stronghold broken: {}, enemy points: {}", getGuildId(), spaceObj.getSpStrongHoldBroken(), spaceObj.getEnemyPointIds());
		dismissMassMarch(spaceObj);
		SpaceGuard3Stage stage = new SpaceGuard3Stage(spaceObj.getGuildId());
		spaceObj.setStage(stage);
		stage.init(this);
	}
	
	/**
	 * 阶段结束时解散集结行军
	 * 
	 * @param spaceObj
	 */
	private void dismissMassMarch(MechaSpaceInfo spaceObj) {
		for (int pointId : spaceObj.getEnemyPointIds()) {
			Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(pointId);
			for (IWorldMarch march : marchs) {	
				if (march.getMarchType() != WorldMarchType.SPACE_MECHA_ATK_STRONG_HOLD_MASS || march.getMarchStatus() != WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					continue;
				}
				
				try {
					Set<IWorldMarch> list = WorldMarchService.getInstance().getMassJoinMarchs(march, true);
					for (IWorldMarch march1 : list) {
						WorldMarchService.getInstance().onPlayerNoneAction(march1, HawkTime.getMillisecond());
					}
					
					WorldMarchService.getInstance().onMarchReturnImmediately(march, march.getArmys());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
	}
	
	/**
	 * 刷出敌军据点
	 */
	private void createStrongHoldPoint() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		obj.getEnemyPointIds().stream().forEach(e -> WorldPointService.getInstance().removeWorldPoint(e, true));
		obj.getEnemyPointIds().clear();
		
		SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, obj.getLevel());
		HawkTuple2<Integer, Integer> tuple1 = levelCfg.getStage2HoldNum();
		HawkTuple2<Integer, Integer> tuple2 = levelCfg.getStage2SpHoldNum();
		SpaceMechaStrongholdCfg holdCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, tuple1.first);
		wave = holdCfg.getAtkWave();
		waveCd = holdCfg.getAtkWaveCd();
		
		// 刷据点
		List<Integer> pointList = SpaceMechaService.getInstance().generateStrongHold(getGuildId(), tuple1.second, tuple1.first);
		pointList.stream().forEach(pointId -> pointHoldMap.put(pointId, tuple1.first));
		
		pointList = SpaceMechaService.getInstance().generateStrongHold(getGuildId(), tuple2.second, tuple2.first);
		pointList.stream().forEach(pointId -> pointHoldMap.put(pointId, tuple2.first));
		
		for (int pointId : pointHoldMap.keySet()) {
			StrongHoldWorldPoint point = (StrongHoldWorldPoint) WorldPointService.getInstance().getWorldPoint(pointId);
			pointEnemyMap.put(pointId, point.getEnemyId());
		} 
	}
	
	public int getRound() {
		return round;
	}
	
	/**
	 * 轮次
	 * @param timeNow
	 */
	public void round(long timeNow) {
		if(timeNow < firstWaveTime || timeNow < nextPushMarchTime){
			return;
		}
		
		this.round++;
		if(this.round >= wave){
			this.nextPushMarchTime = Long.MAX_VALUE;
		} else {
			this.nextPushMarchTime = timeNow + waveCd;
		}
		
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		// 所有据点都被清空了，提前结束当前阶段
		if (obj.getEnemyPointIds().isEmpty()) {
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage no stronghold remain, stage end now, guildId: {}, wave: {}", getGuildId(), round);
			stageEnd();
			return;
		}
		
		for (int pointId : obj.getEnemyPointIds()) {
			this.pushMonsterMarch(obj, pointId, false);
		}
	}
	
	/**
	 * 发起行军
	 */
	private long pushMonsterMarch(MechaSpaceInfo obj, int pointId, boolean remove){
		WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
		if (point == null || !getGuildId().equals(point.getGuildId())) {
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage push monster, point error, guildId: {}, wave: {}, pointId: {}", getGuildId(), round, pointId);
			return 0;
		}
		
		if (point.getPointType() != WorldPointType.SPACE_MECHA_STRONG_HOLD_VALUE) {
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage push monster, pointType error, guildId: {}, wave: {}, pointId: {}, pointType: {}", getGuildId(), round, pointId, point.getPointType());
			return 0;
		}
		
		StrongHoldWorldPoint strongholdPoint = (StrongHoldWorldPoint) point;
		int enemyId = strongholdPoint.getEnemyId();
		SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, enemyId);
		if(monsterCfg == null){
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage push monster, monsterCfg error, guildId: {}, wave: {}, pointId: {}, monsterId: {}", getGuildId(), round, pointId, point.getMonsterId());
			return 0;
		}
		
		long reachTime = 0;
		try {
			NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
			npcPlayer.setPlayerId(SpaceMechaStage.SPACE_GUARD_2_VALUE + "_" + round + "_" + point.getX() + "_" + point.getY());
			npcPlayer.setPfIcon(monsterCfg.getIcon());
			npcPlayer.setName(monsterCfg.getName());
			List<PlayerHero> heroList = new ArrayList<>();
			monsterCfg.getHeroIdList().stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
			npcPlayer.setHeros(heroList);
			obj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE).addNpcPlayer(pointId, npcPlayer);
			SpaceMechaMonsterAttackMarch march = (SpaceMechaMonsterAttackMarch) WorldMarchService.getInstance().startMonsterMarch(obj.getGuildId(), WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE, 
					pointId, obj.getSpacePointId(SpacePointIndex.MAIN_SPACE), null, obj.getGuildId(), monsterCfg.getArmyList(), monsterCfg.getHeroIdList(), false);
			reachTime = march.getEndTime();
			WorldMarchService.getInstance().addGuildMarch(march);
			strongholdPoint.syncEnemyStatus();
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage push monster, guildId: {}, wave: {}, enemyId: {}, marchId: {}, npcPlayer: {}, posX: {}, posY: {}, remove point: {}, army: {}, hero: {}", 
					getGuildId(), round, enemyId, march.getMarchId(), npcPlayer.getId(), march.getOrigionX(), march.getOrigionY(), remove, monsterCfg.getArmyList(), monsterCfg.getHeroIdList());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		if (remove) {
			WorldPointService.getInstance().removeWorldPoint(pointId, true);
		}
		
		return reachTime;
	}
	
	/**
	 *  倒计时结束时，未被击破的据点，将在阶段3消失并生成1支普通敌军进攻主舱
	 *  
	 * @param obj
	 */
	public long pushLastWaveMonster(MechaSpaceInfo obj) {
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_2 stage push last monster, guildId: {}, sp stronghold broken: {}", getGuildId(), obj.getSpStrongHoldBroken());
		
		SpaceMechaStrongholdCfg holdCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, obj.getSpStrongHoldCfgId());
		// 特殊据点已被击破
		if (obj.getSpStrongHoldBroken() > 0) {
			// 特殊据点被击破，会对最终boss产生一次巨额伤害
			if (holdCfg != null) {
				for (Entry<Integer, Integer> entry : holdCfg.getSpWinEffectMap().entrySet()) {
					obj.addEffectToMainSpace(entry.getKey(), entry.getValue());
				}
			}
		} else {
			StrongHoldWorldPoint point = obj.getSpStrongHoldPoint();
			Object[] objects = new Object[] { point.getX(), point.getY(), holdCfg.getId() };
			ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_SPHOLD_NO_BROKEN).setGuildId(obj.getGuildId()).addParms(objects).build());
			// 特殊据点未被击破，会对最终boss产生较高额度的增益
			if (holdCfg != null) {
				for (Entry<Integer, Integer> entry : holdCfg.getSpLoseEffectMap().entrySet()) {
					obj.addEffectToBoss(entry.getKey(), entry.getValue());
				}
			}
		}
		
		long marchReachTime = 0;
		List<Integer> pointList = new ArrayList<>(obj.getEnemyPointIds());
		for (int point : pointList) {
			long reachTime = pushMonsterMarch(obj, point, true);
			marchReachTime = Math.max(marchReachTime, reachTime);
			obj.getEnemyPointIds().remove(point);
		}
		
		return marchReachTime;
	}
	

	@Override
	public void buildStageInfo(SpaceMechaInfoPB.Builder builder) {
		Stage2DetailPB.Builder detailBuilder = Stage2DetailPB.newBuilder();
		detailBuilder.setWave(round);
		for (Entry<Integer, Integer> entry : pointHoldMap.entrySet()) {
			try {
				detailBuilder.addStrongHold(buildStrongHold(entry.getKey(), entry.getValue()));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		} 
		
		builder.setStage2Detail(detailBuilder);
	}
	
	/**
	 * 构建据点信息
	 * 
	 * @param pointId
	 * @param cfgId
	 * @return
	 */
	private StrongHoldPB.Builder buildStrongHold(int pointId, int cfgId) {
		WorldPoint worldPoint = WorldPointService.getInstance().getWorldPoint(pointId);
		StrongHoldPB.Builder holdBuilder = StrongHoldPB.newBuilder();
		int remainBlood = 0;
		holdBuilder.setId(cfgId);
		if (worldPoint != null && worldPoint instanceof StrongHoldWorldPoint) {
			StrongHoldWorldPoint point = (StrongHoldWorldPoint) worldPoint;
			remainBlood = point.getRemainBlood();
			holdBuilder.setPosX(point.getX());
			holdBuilder.setPosY(point.getY());
			holdBuilder.setRemainBlood(point.getRemainBlood());
			holdBuilder.setHpNum(point.getHpNum());
			holdBuilder.setSpecial(point.getSpecial());
		} else {
			SpaceMechaStrongholdCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaStrongholdCfg.class, cfgId);
			int[] pos = GameUtil.splitXAndY(pointId);
			holdBuilder.setPosX(pos[0]);
			holdBuilder.setPosY(pos[1]);
			holdBuilder.setRemainBlood(0);
			holdBuilder.setHpNum(cfg.getHpNumber());
			holdBuilder.setSpecial(cfg.getIsSpecial());
		}
		
		if (remainBlood <= 0) {
			holdBuilder.setStatus(StrongHoldStatus.HOLD_BROKEN);
		} else {
			Collection<IWorldMarch> marchs = WorldMarchService.getInstance().getWorldPointMarch(pointId);
			Optional<IWorldMarch> optional = marchs.stream().filter(e -> e.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE && e.getTerminalId() == pointId).findAny();
			holdBuilder.setStatus(optional.isPresent() ? StrongHoldStatus.HOLD_BE_ATTACKING : StrongHoldStatus.HOLD_NO_ATTACK);
		}
		
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long startTime = this.getStartTime() + cfg.getStrongholdFirstWaveTime();
		long marchTime = cfg.getEnemyMarchTime();
		for (int i = 1; i <= wave; i++) {
			long reachTime = startTime + (i - 1) * waveCd + marchTime;
			WaveEnemyAtkInfo.Builder waveEnemyBuilder = WaveEnemyAtkInfo.newBuilder();
			waveEnemyBuilder.setWave(i);
			waveEnemyBuilder.setReachTime(reachTime);
			int enemyId = pointEnemyMap.get(pointId);
			
			EnemyAtkInfo.Builder enemyBuilder = buildEnemyAtkInfo(pointId, enemyId, reachTime);
			waveEnemyBuilder.addMainSpaceEnemy(enemyBuilder);
			holdBuilder.addWaveEnemy(waveEnemyBuilder);
		}
		
		return holdBuilder;
	}
	
	@Override
	public void buildSpacePointInfo(SpaceWorldPoint spacePoint, MechaSpacePB.Builder builder) {
		if (spacePoint.getSpaceIndex() != SpacePointIndex.MAIN_SPACE) {
			return;
		}
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long startTime = this.getStartTime() + cfg.getStrongholdFirstWaveTime();
		long marchTime = cfg.getEnemyMarchTime();
		long reachTime = startTime + (round - 1) * waveCd + marchTime;
		// 最后一波
		if(this.round >= wave){
			if (reachTime > HawkTime.getMillisecond()) {
				builder.setNearestTime(reachTime);
			}
		} else {
			// 这一波还没有过
			if (reachTime > HawkTime.getMillisecond()) {
				builder.setNearestTime(reachTime);
			} else {
				// 当前这一波过了，取下一波的到达时间
				reachTime = startTime + round * waveCd + marchTime;
				builder.setNearestTime(reachTime);
			}
		}
	}
	
	private void recordInfo() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		stageBuilder = GuardRecordStagePB.newBuilder();
		stageBuilder.setStage(SpaceMechaStage.SPACE_GUARD_2_VALUE);
		stageBuilder.setGuardResult(0);
		stageBuilder.setMainSpaceVal(obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE));
	}

	@Override
	public void updateRecord() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
		stageBuilder.setGuardResult(mainSpaceBlood > 0 ? 0 : 1);
		stageBuilder.setMainSpaceVal(mainSpaceBlood);
	}

	@Override
	public void updateAndRecord(boolean flush) {
		updateAndRecord(flush, null);
	}
	
	@Override
	public void updateAndRecord(boolean flush, MechaSpaceGuardResult result) {
		try {
			MechaSpaceInfo obj = getGuildSpaceInfoObj();
			int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
			if (result == null) {
				result = mainSpaceBlood > 0 ? MechaSpaceGuardResult.SPACE_GUARD_SUCC : MechaSpaceGuardResult.SPACE_GUARD_FAILED;
			}
			stageBuilder.setGuardResult(result.getNumber());
			stageBuilder.setMainSpaceVal(mainSpaceBlood);
			GuardRecordPB.Builder builder = SpaceMechaService.getInstance().getLatestSpaceRecord(obj.getGuildId());
			builder.addStageInfo(stageBuilder);
			if (flush) {
				builder.setGuardResult(stageBuilder.getGuardResult());
				SpaceMechaService.getInstance().flushSpaceRecordToRedis(obj.getGuildId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
}
