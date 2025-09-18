package com.hawk.game.module.spacemecha.stage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.xid.HawkXID;

import com.hawk.game.module.spacemecha.MechaSpaceConst;
import com.hawk.game.module.spacemecha.MechaSpaceConst.SpacePointIndex;
import com.hawk.game.battle.NpcPlayer;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.module.spacemecha.MechaSpaceInfo;
import com.hawk.game.module.spacemecha.SpaceMechaService;
import com.hawk.game.module.spacemecha.config.SpaceMechaConstCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaEnemyCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaLevelCfg;
import com.hawk.game.module.spacemecha.config.SpaceMechaSubcabinCfg;
import com.hawk.game.module.spacemecha.worldmarch.SpaceMechaMonsterAttackMarch;
import com.hawk.game.module.spacemecha.worldpoint.SpaceWorldPoint;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.NPCHeroFactory;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.protocol.Activity.GuardRecordPB;
import com.hawk.game.protocol.Activity.GuardRecordStagePB;
import com.hawk.game.protocol.Activity.GuildSubSpace;
import com.hawk.game.protocol.Activity.MechaSpaceGuardResult;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.SpaceMecha.EnemyAtkInfo;
import com.hawk.game.protocol.SpaceMecha.MechaSpacePB;
import com.hawk.game.protocol.SpaceMecha.MechaSpaceType;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaInfoPB;
import com.hawk.game.protocol.SpaceMecha.SpaceMechaStage;
import com.hawk.game.protocol.SpaceMecha.Stage1DetailPB;
import com.hawk.game.protocol.SpaceMecha.WaveEnemyAtkInfo;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.service.WorldPointService;

/**
 * 防守阶段1
 * 
 * 1.在防守点附近，刷出普通敌军（后面波次，首波敌军之前已经刷出了），进攻主舱
 * 2.我方成员可继续向主舱进行增援驻守，但最多只可驻守1支部队
 * 3.在防守点附近，刷出精英敌军，进攻子舱
 * 4.子舱防守仅在1阶段进行，1阶段结束时，如果子舱防守成功，存在收益
 * 5.倒计时结束，且主舱血条未被清空，则继续进入防守阶段2
 * 
 * 
 * @author lating
 *
 */
public class SpaceGuard1Stage extends ISpaceMechaStage {
	/** 
	 * 下一次发起野怪行军的时间 
	 */
	private long nextPushMarchTime;  
	/** 
	 * 下一次创建野怪点的时间 
	 */
	private long createMonsterNextTime; 
	/** 
	 * 当前波次 
	 */
	private int round;
	/** 
	 * 总的波次 
	 */
	private int totalWave;  
	/** 
	 * 野怪点发行军的时间间隔, 相对于基础时间点（生成怪物点的时间）来说  
	 */
	private long wavePushMarchCd; 
	/** 
	 * 创建野怪点的时间间隔，相对于当前波次野怪行军到达目的地的时间点来说 
	 */
	private long createMonsterCd;
	/** 
	 * 首波野怪行军发起的时间 
	 */
	private long firstWaveMarchPushTime;
	/** 
	 * 首次进攻子舱的野怪行军发起时间
	 */
	private long firstSubSpaceMarchPushTime;
	/** 
	 * 野怪行军时长 
	 */
	private long monsterMarchTime;
	/** 
	 * 一个波次的总时长 
	 */
	private long roundWholeTime;
	/**
	 * 子舱结束标识
	 */
	private boolean subSpace1Over;
	private boolean subSpace2Over;
	
	/**
	 * 每波次各个怪物点发出的敌军信息，还没随出来的点，map->key=0
	 */
	private List<Map<Integer, Integer>> pointEnemyList = new ArrayList<>();
	private List<Map<Integer, Integer>> pointSp1EnemyList = new ArrayList<>();
	private List<Map<Integer, Integer>> pointSp2EnemyList = new ArrayList<>();
	/**
	 * 记录信息
	 */
	GuardRecordStagePB.Builder stageBuilder;
	GuildSubSpace.Builder subSpace1Builder;
	GuildSubSpace.Builder subSpace2Builder;
	/** 
	 * 子舱1，子舱2被打爆的波次 
	 */
	int subSpace1BrokenWave, subSpace2BrokenWave;
	
	
	/**
	 * 构造函数
	 * 
	 * @param guildId
	 */
	public SpaceGuard1Stage(String guildId) {
		super(guildId, SpaceMechaStage.SPACE_GUARD_1_VALUE);
	}

	@Override
	public boolean onTick() {
		if (checkMainSpaceDefenceFailed()) {
			return false;
		}
		
		long timeNow = HawkTime.getMillisecond();
		if (timeNow >= this.getEndTime()) {
			stageEnd(timeNow);
		} else {
			round(timeNow);
		}
		
		return true;
	}

	@Override
	public SpaceMechaStage getStageVal() {
		return SpaceMechaStage.SPACE_GUARD_1;
	}
	
	/**
	 * 阶段初始化
	 * @param timeNow
	 */
	public void init(long timeNow) {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage init, guildId: {}, enemy points: {}", getGuildId(), obj.getEnemyPointIds());
		stageChangeSync(NoticeCfgId.SPACE_MECHA_STAGE_1_START);
		
		SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, obj.getLevel());
		totalWave = levelCfg.getStage1Wave();
		wavePushMarchCd = levelCfg.getStage1WaveCd();
		createMonsterCd = levelCfg.getStage1RefreshCd();
		
		Map<Integer, Integer> pointEnemyMap = new HashMap<>();
		pointEnemyList.add(pointEnemyMap);
		for (int pointId : obj.getEnemyPointIds()) {
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			if (point != null) {
				pointEnemyMap.put(pointId, point.getMonsterId());
			} 
		}
		
		pointSp1EnemyList.add(Collections.emptyMap());
		pointSp2EnemyList.add(Collections.emptyMap());
		for (int i = 2; i <= totalWave; i++) {
			pointEnemyMap = new HashMap<>();
			pointEnemyList.add(pointEnemyMap);
			int count = levelCfg.getEnemyNumByWave(i);
			for (int j = 0; j < count; j ++) {
				int enemyId = HawkRand.randomWeightObject(levelCfg.getStage1EnemyIdList(), levelCfg.getStage1EnemyWeightList());
				pointEnemyMap.put(0-j, enemyId);
				HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate enemyId, guildId: {}, wave: {}, enemyId: {}", getGuildId(), i, enemyId);
			}
			
			initSubSpaceEnemy(i, SpacePointIndex.SUB_SPACE_1, levelCfg, pointSp1EnemyList);
			initSubSpaceEnemy(i, SpacePointIndex.SUB_SPACE_2, levelCfg, pointSp2EnemyList);
		}
		
		recordInfo(obj);
		
		// 首波敌人从阶段1的0秒开始行军
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		monsterMarchTime = cfg.getEnemyMarchTime();
		roundWholeTime = wavePushMarchCd + monsterMarchTime + createMonsterCd; // 刷出野怪后等待发起行军的时间差值 + 野怪行军时长 + 行军到达目的地后等待刷出下一波野怪点的时间差值
		firstWaveMarchPushTime = this.getStartTime() + cfg.getCabinFirstWaveTime();
		firstSubSpaceMarchPushTime = this.getStartTime() + cfg.getSubcabinFirstWaveTime();
		if (levelCfg.getSpEnemyWaveMin() > 0 && cfg.getSubcabinFirstWaveTime() <= 0) {
			firstSubSpaceMarchPushTime = this.getStartTime() + (levelCfg.getSpEnemyWaveMin() - 1) * roundWholeTime + wavePushMarchCd;
		}
		
		if (cfg.getCabinFirstWaveTime() <= 0) {
			round(timeNow);
		}
	}
	
	public void stageEnd() {
		stageEnd(HawkTime.getMillisecond());
	}
	
	/**
	 * 阶段结束处理
	 * @param timeNow
	 */
	private void stageEnd(long timeNow) {
		updateAndRecord(false);
		MechaSpaceInfo spaceObj = getGuildSpaceInfoObj();
		// 记录阶段变化
		SpaceMechaService.getInstance().logSpaceMechaStageEnd(spaceObj, timeNow - this.getStartTime());
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage end, guildId: {}", getGuildId());
		
		// 阶段结束是时，对子舱防守结果进行检测、结算
		slaveSpaceDefResultCheck(spaceObj);
		
		SpaceGuard2Stage stage = new SpaceGuard2Stage(spaceObj.getGuildId());
		spaceObj.setStage(stage);
		stage.init(timeNow);
	}
	
	public int getRound() {
		return round;
	}
	
	/**
	 * 轮次
	 * 
	 * @param timeNow
	 */
	public void round(long timeNow) {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		// 刷出怪物点的时间判断
		if (timeNow > createMonsterNextTime) {
			this.round++;
			if(this.round >= totalWave){
				this.createMonsterNextTime = Long.MAX_VALUE - wavePushMarchCd;
			} else {
				// 生成野怪点后，隔 waveCd发起行军，野怪行军时长 monsterMarchTime，行军到达目的地后，隔 createPointCd再生成下一波野怪点，如此循环
				this.createMonsterNextTime = timeNow + roundWholeTime;
			}
			
			// 刷出怪物点：第一波野怪点在放置舱体的时候就已将生成了，所以这里跳过第一波
			if (this.round > 1) {
				nextPushMarchTime = timeNow + wavePushMarchCd;
				createMonsterPoint(obj);
			} else {
				nextPushMarchTime = Math.max(firstWaveMarchPushTime, timeNow);
			}
		}
		
		// 普通怪进攻主舱行军
		if (timeNow >= nextPushMarchTime) {
			nextPushMarchTime = createMonsterNextTime + wavePushMarchCd;
			this.pushMainSpaceMonsterMarch(obj);
			// 精英怪进攻子舱行军
			if (timeNow >= firstSubSpaceMarchPushTime) {
				this.pushSubSpaceMonstermMarch(obj, pointSp1EnemyList.get(this.round - 1), SpacePointIndex.SUB_SPACE_1);
				this.pushSubSpaceMonstermMarch(obj, pointSp2EnemyList.get(this.round - 1), SpacePointIndex.SUB_SPACE_2);
			}
		}
		
		// 判断子舱是否结束
		subSpaceCheck(obj);
	}
	
	/**
	 * 判断子舱防守失败或最终防守成功
	 * 
	 * @param spaceObj
	 */
	private void subSpaceCheck(MechaSpaceInfo spaceObj) {
		if (!subSpace1Over) {
			SpaceWorldPoint spacePoint1 = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_1);
			if (spacePoint1.getSpaceBlood() <= 0 || (this.round >= totalWave && nextPushMarchTime == Long.MAX_VALUE && spacePoint1.getEnemyMarchCount() <= 0)) {
				subSpace1Over = true;
				spaceObj.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_1);
			}
		}
		
		if (!subSpace2Over) {
			SpaceWorldPoint spacePoint2 = spaceObj.getSpaceWorldPoint(SpacePointIndex.SUB_SPACE_2);
			if (spacePoint2.getSpaceBlood() <= 0 || (this.round >= totalWave && nextPushMarchTime == Long.MAX_VALUE  && spacePoint2.getEnemyMarchCount() <= 0)) {
				subSpace2Over = true;
				spaceObj.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_2);
			}
		}
	}
	
	/**
	 * 给子舱生成初始化野怪点
	 * 
	 * @param waveNum
	 * @param levelCfg
	 */
	private void initSubSpaceEnemy(int waveNum, int spaceIndex, SpaceMechaLevelCfg levelCfg, List<Map<Integer, Integer>> pointSpEnemyList) {
		Map<Integer, Integer> pointSpEnemyMap = new HashMap<>();
		pointSpEnemyList.add(pointSpEnemyMap);
		if (waveNum >= levelCfg.getSpEnemyWaveMin()) {
			int spCount = levelCfg.getSpEnemyNumByWave(waveNum);
			for (int j = 0; j < spCount; j ++) {
				int enemyId = HawkRand.randomWeightObject(levelCfg.getStage1SpEnemyIdList(), levelCfg.getStage1SpEnemyWeightList());
				pointSpEnemyMap.put(0-j, enemyId);
				HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate sp enemyId, guildId: {}, spaceIndex: {}, wave: {}, enemyId: {}", getGuildId(), spaceIndex, waveNum, enemyId);
			}
		}
	}
	
	/**
	 * 刷出野怪点
	 * 
	 * @param obj
	 */
	private void createMonsterPoint(MechaSpaceInfo obj) {
		// 刷出新一波的野怪点之前，先将老的可能遗留下来的野怪点先清理一下
		if (this.round - 2 >= 0) {
			clearPoint(obj, pointEnemyList.get(this.round - 2));
			clearPoint(obj, pointSp1EnemyList.get(this.round - 2));
			clearPoint(obj, pointSp2EnemyList.get(this.round - 2));
		}
		Map<Integer, Integer> pointEnemyMap = pointEnemyList.get(this.round - 1);
		HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate enemyId point before, guildId: {}, wave: {}, enemyId: {}", getGuildId(), round, pointEnemyMap);
		SpaceMechaLevelCfg levelCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaLevelCfg.class, obj.getLevel());
		List<WorldPoint> pointList = SpaceMechaService.getInstance().generateMonster(getGuildId(), MonsterType.TYPE_12_VALUE, levelCfg.getEnemyNumByWave(this.round), pointEnemyMap.values());
		updatePointEnemyInfo(pointList, pointEnemyMap);
		HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate enemyId point after, guildId: {}, wave: {}, enemyId: {}", getGuildId(), round, pointEnemyMap);
		// 给子舱刷出野怪点
		if (this.round >= levelCfg.getSpEnemyWaveMin()) {
			subSpace1BrokenWave = createMonsterPointToSubSpace(obj, SpacePointIndex.SUB_SPACE_1, subSpace1BrokenWave, levelCfg, pointSp1EnemyList);
			subSpace2BrokenWave = createMonsterPointToSubSpace(obj, SpacePointIndex.SUB_SPACE_2, subSpace2BrokenWave, levelCfg, pointSp2EnemyList);
		}
	}
	
	/**
	 * 给子舱刷野怪点
	 */
	private int createMonsterPointToSubSpace(MechaSpaceInfo obj, int spaceIndex, int brokenWave, SpaceMechaLevelCfg levelCfg, List<Map<Integer, Integer>> pointSpEnemyList) {
		if (brokenWave > 0) {
			return brokenWave;
		}
		
		if (!spaceBroken(obj, spaceIndex)) {
			Map<Integer, Integer> pointSpEnemyMap = pointSpEnemyList.get(this.round - 1);
			HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate sp enemyId point before, guildId: {}, spaceIndex: {}, wave: {}, enemyId: {}", getGuildId(), spaceIndex, round, pointSpEnemyMap);
			List<WorldPoint> spPointList = SpaceMechaService.getInstance().generateMonster(getGuildId(), MonsterType.TYPE_13_VALUE, levelCfg.getSpEnemyNumByWave(this.round), pointSpEnemyMap.values());
			updatePointEnemyInfo(spPointList, pointSpEnemyMap);
			HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage generate sp enemyId point after, guildId: {}, spaceIndex: {}, wave: {}, enemyId: {}", getGuildId(), spaceIndex, round, pointSpEnemyMap);
		} else {
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage generate sp enemyId point failed, space broken, guildId: {}, spaceIndex: {}, wave: {}", getGuildId(), spaceIndex, round);
			return round - 1;
		}
		
		return 0;
	}
	
	/**
	 * 清除上一波次遗留下来的怪物点
	 * 
	 * @param obj
	 * @param lastRoundPEenemyMap
	 */
	private void clearPoint(MechaSpaceInfo obj, Map<Integer, Integer> lastRoundPEenemyMap) {
		if (lastRoundPEenemyMap == null) {
			return;
		}
		
		for (int pointId : lastRoundPEenemyMap.keySet()) {
			if (obj.getEnemyPointIds().contains(pointId)) {
				WorldPointService.getInstance().removeWorldPoint(pointId, true);
				obj.getEnemyPointIds().remove(pointId);
			}
		}
	}
	
	/**
	 * 更新点-敌军对应信息
	 * 
	 * @param pointList
	 * @param pointEnemyMap
	 */
	private void updatePointEnemyInfo(List<WorldPoint> pointList, Map<Integer, Integer> pointEnemyMap) {
		Map<Integer, Integer> map = new HashMap<>();
		for (WorldPoint point : pointList) {
			map.put(point.getId(), point.getMonsterId());
		}
		pointEnemyMap.clear();
		pointEnemyMap.putAll(map);
	}
	
	/**
	 * 发起进攻主舱的普通怪行军
	 */
	private void pushMainSpaceMonsterMarch(MechaSpaceInfo obj){
		Map<Integer, Integer> pointEnemyMap = pointEnemyList.get(this.round - 1);
		for (int pointId : pointEnemyMap.keySet()) {
			if (pointId < 0) {
				continue;
			}
			
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			if (point == null || point.getPointType() != WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
				HawkLog.errPrintln("spaceMecha SPACE_GUARD_1 stage push monster to mainspace, pointType error, guildId: {}, wave: {}, pointId: {}, pointType: {}", obj.getGuildId(), round, pointId, point == null ? 0 : point.getPointType());
				continue;
			}
			
			SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, point.getMonsterId());
			if(monsterCfg == null || monsterCfg.getAttackAim() != MechaSpaceType.MAIN_SPACE_VALUE){
				HawkLog.errPrintln("spaceMecha SPACE_GUARD_1 stage push monster to mainspace, monsterCfg error, guildId: {}, wave: {}, pointId: {}, point monsterId: {}", obj.getGuildId(), round, pointId, point.getMonsterId());
				continue;
			}
			
			try {
				//发起行军开始
				NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
				npcPlayer.setPlayerId(SpaceMechaStage.SPACE_GUARD_1_VALUE + "_" + round + "_" + point.getX() + "_" + point.getY());
				npcPlayer.setPfIcon(monsterCfg.getIcon());
				npcPlayer.setName(monsterCfg.getName());
				List<PlayerHero> heroList = new ArrayList<>();
				monsterCfg.getHeroIdList().stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
				npcPlayer.setHeros(heroList);
				obj.getSpaceWorldPoint(SpacePointIndex.MAIN_SPACE).addNpcPlayer(pointId, npcPlayer);
				
				SpaceMechaMonsterAttackMarch march = (SpaceMechaMonsterAttackMarch) WorldMarchService.getInstance().startMonsterMarch(obj.getGuildId(), WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE, 
						pointId, obj.getSpacePointId(SpacePointIndex.MAIN_SPACE), null, obj.getGuildId(), monsterCfg.getArmyList(), monsterCfg.getHeroIdList(), false);
				WorldMarchService.getInstance().addGuildMarch(march);
				HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage push monster to mainspace, guildId: {}, wave: {}, enemyId: {}, original posX: {}, posY: {}, marchId: {}, npcPlayer: {}, army: {}, hero: {}", 
						obj.getGuildId(), round, monsterCfg.getId(), march.getOrigionX(), march.getOrigionY(), march.getMarchId(), npcPlayer.getId(), monsterCfg.getArmyList(), monsterCfg.getHeroIdList());
			} catch (Exception e) {
				HawkException.catchException(e);
			} 
			
			obj.getEnemyPointIds().remove(pointId);
			WorldPointService.getInstance().removeWorldPoint(pointId, true);
		}
		
		HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage push monster to mainspace after, guildId: {},  wave: {}, remained enemy pointIds: {}", obj.getGuildId(), round, obj.getEnemyPointIds());
	}
	
	/**
	 * 发起进攻子舱的精英怪行军
	 */
	private void pushSubSpaceMonstermMarch(MechaSpaceInfo obj, Map<Integer, Integer> pointSpEnemyMap, int spaceIndex) {
		if (spaceBroken(obj, spaceIndex)) {
			HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage push monster to subspace-{}, space broken, guildId: {}, wave: {}", spaceIndex, obj.getGuildId(), round);
			return;
		}
		
		for (int pointId : pointSpEnemyMap.keySet()) {
			if (pointId < 0) {
				continue;
			}
			WorldPoint point = WorldPointService.getInstance().getWorldPoint(pointId);
			if (point == null || point.getPointType() != WorldPointType.SPACE_MECHA_MONSTER_VALUE) {
				HawkLog.errPrintln("spaceMecha SPACE_GUARD_1 stage push monster to subspace, pointType error, guildId: {}, wave: {}, pointId: {}, pointType: {}", obj.getGuildId(), round, pointId, point == null ? 0 : point.getPointType());
				continue;
			}
			
			SpaceMechaEnemyCfg monsterCfg = HawkConfigManager.getInstance().getConfigByKey(SpaceMechaEnemyCfg.class, point.getMonsterId());
			if(monsterCfg == null || monsterCfg.getAttackAim() != MechaSpaceType.SLAVE_SPACE_VALUE){
				HawkLog.errPrintln("spaceMecha SPACE_GUARD_1 stage push monster to subspace, monsterCfg error, guildId: {}, wave: {}, pointId: {}, point monsterId: {}", obj.getGuildId(), round, pointId, point.getMonsterId());
				continue;
			}
			
			try {
				SpaceWorldPoint spacePoint = obj.getSpaceWorldPoint(spaceIndex);
				NpcPlayer npcPlayer = new NpcPlayer(HawkXID.nullXid());
				npcPlayer.setPlayerId(SpaceMechaStage.SPACE_GUARD_1_VALUE + "_" + round + "_" + point.getX() + "_" + point.getY());
				npcPlayer.setPfIcon(monsterCfg.getIcon());
				npcPlayer.setName(monsterCfg.getName());
				List<PlayerHero> heroList = new ArrayList<>();
				monsterCfg.getHeroIdList().stream().forEach(heroId -> heroList.add(NPCHeroFactory.getInstance().get(heroId)));
				npcPlayer.setHeros(heroList);
				spacePoint.addNpcPlayer(pointId, npcPlayer);
				
				//发起行军开始
				SpaceMechaMonsterAttackMarch march = (SpaceMechaMonsterAttackMarch) WorldMarchService.getInstance().startMonsterMarch(obj.getGuildId(), WorldMarchType.SPACE_MECHA_MONSTER_ATTACK_MARCH_VALUE, 
						pointId, spacePoint.getId(), null, obj.getGuildId(), monsterCfg.getArmyList(), monsterCfg.getHeroIdList(), false);
				WorldMarchService.getInstance().addGuildMarch(march);
				HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage push monster to subspace, guildId: {}, wave: {}, enemyId: {}, original posX: {}, posY: {}, terminal posX: {}, posY: {}, marchId: {}, npcPlayer: {}, army: {}, hero: {}", 
						obj.getGuildId(), round, monsterCfg.getId(), march.getOrigionX(), march.getOrigionY(), march.getTerminalX(), march.getTerminalY(), march.getMarchId(), npcPlayer.getId(), 
						monsterCfg.getArmyList(), monsterCfg.getHeroIdList());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
			
			obj.getEnemyPointIds().remove(pointId);
			WorldPointService.getInstance().removeWorldPoint(pointId, true);
		}
	}
	
	/**
	 * 子舱防守结果处理
	 * 
	 * @param obj
	 */
	private void slaveSpaceDefResultCheck(MechaSpaceInfo obj) {
		try {
			slaveSpaceDefWin(obj, SpacePointIndex.SUB_SPACE_1);
			slaveSpaceDefWin(obj, SpacePointIndex.SUB_SPACE_2);
			// 子舱防守行军遣返
			obj.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_1);
			obj.forceSpaceMarchBack(SpacePointIndex.SUB_SPACE_2);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 子舱防守成功
	 * 
	 * @param obj
	 */
	private void slaveSpaceDefWin(MechaSpaceInfo obj, int spaceIndex) {
		SpaceWorldPoint spacePoint = obj.getSpaceWorldPoint(spaceIndex);
		HawkLog.logPrintln("spaceMecha SPACE_GUARD_1 stage subspace defence result, guildId: {}, spaceIndex: {}, blood: {}, wave: {}, remained enemy pointIds: {}", obj.getGuildId(), spaceIndex, spacePoint.getSpaceBlood(), round, obj.getEnemyPointIds());
		if (spaceBroken(obj, spaceIndex)) {
			return;
		}
		
		SpaceMechaSubcabinCfg cfg = SpaceMechaSubcabinCfg.getCfg(obj.getLevel());
		Object[] objects = new Object[] { spacePoint.getX(), spacePoint.getY(), cfg.getId() };
		ChatService.getInstance().addWorldBroadcastMsg(ChatParames.newBuilder().setChatType(ChatType.CHAT_ALLIANCE).setKey(NoticeCfgId.SPACE_MECHA_SLAVE_DEF_WIN).setGuildId(obj.getGuildId()).addParms(objects).build());
		// 获得得子舱对主舱防守的伤害提升加成
		for (Entry<Integer, Integer> entry : cfg.getWinEffectMap().entrySet()) {
			obj.addEffectToMainSpace(entry.getKey(), entry.getValue());
		}
		
		SpaceMechaConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		Object[] object = new Object[] { obj.getLevel() };
		// 联盟全员获取子舱防守奖励邮件（每个子舱各1份）
		for (String playerId : spacePoint.getDefenceMembers()) {
			HawkLog.debugPrintln("spaceMecha SPACE_GUARD_1 stage subspace defence win reward, guildId: {}, spaceIndex: {}, playerId: {}", obj.getGuildId(), spaceIndex, playerId);
			Player player = GlobalData.getInstance().makesurePlayer(playerId);
			if (player != null) {
				CustomDataEntity customData = SpaceMechaService.getInstance().getCustomDataEntity(player, MechaSpaceConst.PERSONAL_SUBSPACE_AWARD_TOTAL);
				if (customData.getValue() >= constCfg.getSubcabinPartiAwardPersonLimit()) {
					continue;
				}
				customData.setValue(customData.getValue() + 1);
			}
			
			SystemMailService.getInstance().sendMail(MailParames.newBuilder()
			        .setPlayerId(playerId)
			        .setMailId(MailId.SPACE_MECHA_SLAVE_WIN_REWARD_MEMBER)
			        .setAwardStatus(MailRewardStatus.NOT_GET)
			        .setRewards(cfg.getWinAward())
			        .addContents(object)
			        .build());
		}
	}
	
	/**
	 * 判断自仓是否被击破
	 * 
	 * @param obj
	 * @param spaceIndex
	 * @return
	 */
	private boolean spaceBroken(MechaSpaceInfo obj, int spaceIndex) {
		SpaceWorldPoint spacePoint = obj.getSpaceWorldPoint(spaceIndex);
		return spacePoint.getSpaceBlood() <= 0;
	}
	
	
	@Override
	public void buildStageInfo(SpaceMechaInfoPB.Builder builder) {
		Stage1DetailPB.Builder detailBuilder = Stage1DetailPB.newBuilder();
		detailBuilder.setWave(round);
		
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long spaceMainStartTime = this.getStartTime() + cfg.getCabinFirstWaveTime();
		long marchTime = cfg.getEnemyMarchTime();
		for (int roundIndex = 0; roundIndex < pointEnemyList.size(); roundIndex++) {
			int waveNum = roundIndex + 1;
			Map<Integer, Integer> pointEnemyMap = pointEnemyList.get(roundIndex);
			long reachTime = spaceMainStartTime + roundIndex * roundWholeTime + wavePushMarchCd + marchTime;
			WaveEnemyAtkInfo.Builder waveEnemyBuilder = WaveEnemyAtkInfo.newBuilder();
			waveEnemyBuilder.setWave(waveNum);
			waveEnemyBuilder.setReachTime(reachTime);
			for (Entry<Integer, Integer> entry : pointEnemyMap.entrySet()) {
				EnemyAtkInfo.Builder enemyBuilder = buildEnemyAtkInfo(entry.getKey(), entry.getValue(), reachTime);
				waveEnemyBuilder.addMainSpaceEnemy(enemyBuilder);
			}
			
			// 子舱打爆之后就不传了
			if (subSpace1BrokenWave == 0 || waveNum <= subSpace1BrokenWave) {
				buildSubSpaceEnemyInfo(roundIndex, reachTime, pointSp1EnemyList, waveEnemyBuilder);
			}
			
			// 子舱打爆之后就不传了
			if (subSpace2BrokenWave == 0 || waveNum <= subSpace2BrokenWave) {
				buildSubSpaceEnemyInfo(roundIndex, reachTime, pointSp2EnemyList, waveEnemyBuilder);
			}
			
			detailBuilder.addWaveEnemy(waveEnemyBuilder);
		}
		
		builder.setStage1Detail(detailBuilder);
	}
	
	/**
	 * 拼子舱的信息
	 * 
	 * @param roundIndex
	 * @param reachTime
	 * @param pointSpEnemyList
	 * @param waveEnemyBuilder
	 */
	private void buildSubSpaceEnemyInfo(int roundIndex, long reachTime, List<Map<Integer, Integer>> pointSpEnemyList, WaveEnemyAtkInfo.Builder waveEnemyBuilder) {
		Map<Integer, Integer> pointSpEnemyMap = pointSpEnemyList.get(roundIndex);
		if (pointSpEnemyMap.isEmpty()) {
			return;
		}
		
		for (Entry<Integer, Integer> entry : pointSpEnemyMap.entrySet()) {
			EnemyAtkInfo.Builder enemyBuilder = buildEnemyAtkInfo(entry.getKey(), entry.getValue(), reachTime);
			waveEnemyBuilder.addSpace1Enemy(enemyBuilder);
		}
	}
	
	
	private void recordInfo(MechaSpaceInfo obj) {
		stageBuilder = GuardRecordStagePB.newBuilder();
		stageBuilder.setStage(SpaceMechaStage.SPACE_GUARD_1_VALUE);
		stageBuilder.setGuardResult(0);
		stageBuilder.setMainSpaceVal(obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE));
		
		subSpace1Builder = GuildSubSpace.newBuilder();
		subSpace1Builder.setId(SpacePointIndex.SUB_SPACE_1);
		subSpace1Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_1));
		
		subSpace2Builder = GuildSubSpace.newBuilder();
		subSpace2Builder.setId(SpacePointIndex.SUB_SPACE_2);
		subSpace2Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_2));
	}
	
	public void updateRecord() {
		MechaSpaceInfo obj = getGuildSpaceInfoObj();
		int mainSpaceBlood = obj.getSpaceBlood(SpacePointIndex.MAIN_SPACE);
		stageBuilder.setGuardResult(mainSpaceBlood > 0 ? 0 : 1);
		stageBuilder.setMainSpaceVal(mainSpaceBlood);
		subSpace1Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_1));
		subSpace2Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_2));
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
			subSpace1Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_1));
			subSpace2Builder.setSpaceVal(obj.getSpaceBlood(SpacePointIndex.SUB_SPACE_2));
			stageBuilder.addSubSpaceVal(subSpace1Builder);
			stageBuilder.addSubSpaceVal(subSpace2Builder);
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
	
	@Override
	public void buildSpacePointInfo(SpaceWorldPoint spacePoint, MechaSpacePB.Builder builder) {
		SpaceMechaConstCfg cfg = HawkConfigManager.getInstance().getKVInstance(SpaceMechaConstCfg.class);
		long spaceMainStartTime = this.getStartTime() + cfg.getCabinFirstWaveTime();
		long marchTime = cfg.getEnemyMarchTime();
		long reachTime = spaceMainStartTime + (round - 1) * roundWholeTime + wavePushMarchCd + marchTime;
		if (spacePoint.getPointType() == WorldPointType.SPACE_MECHA_MAIN_VALUE) {
			buildSpacePointInfo(builder, reachTime, spaceMainStartTime, marchTime);
		} else if (spacePoint.getSpaceBlood() > 0) {
			buildSubSpacePoint(builder, reachTime, spaceMainStartTime, marchTime);
		}
	}
	
	/**
	 * 子舱独有逻辑
	 */
	private void buildSubSpacePoint(MechaSpacePB.Builder builder, long reachTime, long spaceMainStartTime, long marchTime) {
		if (HawkTime.getMillisecond() > firstSubSpaceMarchPushTime) {
			buildSpacePointInfo(builder, reachTime, spaceMainStartTime, marchTime);
		} else {
			reachTime = firstSubSpaceMarchPushTime + marchTime;
			builder.setNearestTime(reachTime);
		}
	}
	
	/**
	 * 主舱、子舱相同逻辑
	 */
	private void buildSpacePointInfo(MechaSpacePB.Builder builder, long reachTime, long spaceMainStartTime, long marchTime) {
		// 最后一波
		if(this.round >= totalWave){
			if (reachTime > HawkTime.getMillisecond()) {
				builder.setNearestTime(reachTime);
			}
		} else {
			// 这一波还没有过
			if (reachTime > HawkTime.getMillisecond()) {
				builder.setNearestTime(reachTime);
			} else {
				// 当前这一波过了，取下一波的到达时间
				reachTime = spaceMainStartTime + round * roundWholeTime + wavePushMarchCd + marchTime;
				builder.setNearestTime(reachTime);
			}
		}
	}
	
}
