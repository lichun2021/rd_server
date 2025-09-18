package com.hawk.game.lianmengcyb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.CYBORGBattleCfg;
import com.hawk.game.config.CYBORGBuildBuffCfg;
import com.hawk.game.config.CYBORGBuildBuffLevelCfg;
import com.hawk.game.config.CYBORGBuildBuffLevelExtraCfg;
import com.hawk.game.config.CYBORGChronoSphereCfg;
import com.hawk.game.config.CYBORGCommandCenterCfg;
import com.hawk.game.config.CYBORGHeadQuartersCfg;
import com.hawk.game.config.CYBORGIronCurtainDeviceCfg;
import com.hawk.game.config.CYBORGMilitaryBaseCfg;
import com.hawk.game.config.CYBORGNuclearMissileSiloCfg;
import com.hawk.game.config.CYBORGWeatherControllerCfg;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.lianmengcyb.CYBORGConst.CYBORGState;
import com.hawk.game.lianmengcyb.CYBORGRoomManager.CYBORG_CAMP;
import com.hawk.game.lianmengcyb.entity.CYBORGMarchEntity;
import com.hawk.game.lianmengcyb.module.CYBORGArmyModule;
import com.hawk.game.lianmengcyb.module.CYBORGMarchModule;
import com.hawk.game.lianmengcyb.module.CYBORGWorldModule;
import com.hawk.game.lianmengcyb.msg.CYBORGQuitReason;
import com.hawk.game.lianmengcyb.order.CYBORGOrderCollection;
import com.hawk.game.lianmengcyb.player.ICYBORGPlayer;
import com.hawk.game.lianmengcyb.roomstate.ICYBORGBattleRoomState;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGAssistanceSingleMarch;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGAttackMonsterMarch;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGAttackPlayerMarch;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGBuildingMarchMass;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGBuildingMarchMassJoin;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGBuildingMarchSingle;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGMassJoinSingleMarch;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGMassSingleMarch;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGNianMarchMass;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGNianMarchMassJoin;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGNianMarchSingle;
import com.hawk.game.lianmengcyb.worldmarch.CYBORGSpyMarch;
import com.hawk.game.lianmengcyb.worldmarch.ICYBORGWorldMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassJoinMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGMassMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGPassiveAlarmTriggerMarch;
import com.hawk.game.lianmengcyb.worldmarch.submarch.ICYBORGReportPushMarch;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGBuildState;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGChronoSphere;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGCommandCenter;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGHeadQuarters;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGIronCurtainDevice;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGMilitaryBase;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGMonster;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNian;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGNuclearMissileSilo;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGPointUtil;
import com.hawk.game.lianmengcyb.worldpoint.CYBORGWeatherController;
import com.hawk.game.lianmengcyb.worldpoint.ICYBORGBuilding;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.CYBORG.PBCYBORGGameInfoSync;
import com.hawk.game.protocol.CYBORG.PBCYBORGGameOver;
import com.hawk.game.protocol.CYBORG.PBCYBORGGuildInfo;
import com.hawk.game.protocol.CYBORG.PBCYBORGPlayerInfo;
import com.hawk.game.protocol.CYBORG.PBCYBORGPlayerQuitRoom;
import com.hawk.game.protocol.CYBORG.PBCYBORGSecondMapResp;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.TiberiumWar.TWGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TWPageInfo;
import com.hawk.game.protocol.TiberiumWar.TWState;
import com.hawk.game.protocol.TiberiumWar.TWStateInfo;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointPB;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CWConst;
import com.hawk.game.service.cyborgWar.CWRoomData;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 虎牢关
 * 
 * @author lwt
 * @date 2018年10月26日
 */
public class CYBORGBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	/** 结算信息 */
	private CYBORGExtraParam extParm;
	private boolean gameOver;
	private int overState;
	private int battleCfgId;
	private long curTimeMil;

	/** 游戏内玩家 不包含退出的 */
	private Map<String, ICYBORGPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, ICYBORGPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, ICYBORGWorldPoint> viewPoints = new ConcurrentHashMap<>();

	private Map<String, ICYBORGWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private List<ICYBORGBuilding> buildingList = new CopyOnWriteArrayList<>();
	private final List<CYBORGNuclearMissileSilo> nuclearBuildList = new ArrayList<>();
	private ICYBORGBattleRoomState state;
	private List<CYBORGMonster> monsterList = new CopyOnWriteArrayList<>();
	private long createTime;
	private long startTime;
	private long overTime;
	private CYBORGBornPointRing bornPointAList;
	private CYBORGBornPointRing bornPointBList;
	private CYBORGBornPointRing bornPointCList;
	private CYBORGBornPointRing bornPointDList;

	private List<Integer> nianRefreshTimes = new LinkedList<>();
	private CYBORGRandPointSeed pointSeed = new CYBORGRandPointSeed();

	private CYBORGGuildBaseInfo campA = new CYBORGGuildBaseInfo();
	private CYBORGGuildBaseInfo campB = new CYBORGGuildBaseInfo();
	private CYBORGGuildBaseInfo campC = new CYBORGGuildBaseInfo();
	private CYBORGGuildBaseInfo campD = new CYBORGGuildBaseInfo();

	private List<CYBORGGuildBaseInfo> battleCamps = new ArrayList<>();

	private long nextRefreshNian;

	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBCYBORGGameInfoSync lastSyncpb = PBCYBORGGameInfoSync.getDefaultInstance();

//	private long lastSyncMap;
//	private PBCYBORGSecondMapResp lastMappb;
	private CYBORGMonsterRefesh monsterrefresh;
	private long nextLoghonor;

	private int nianCount;

	private Multimap<Class<? extends ICYBORGBuilding>, ? super ICYBORGBuilding> CYBORGBuildingMap = HashMultimap.create();

	/** 地图上占用点 */
	private Set<Integer> occupationPointSet = new HashSet<>();

	private ICYBORGPlayer anchor;

	public CYBORGBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	public int worldMonsterCount() {
		return monsterList.size();
	}

	public List<ICYBORGPlayer> getCampPlayers(CYBORG_CAMP camp) {
		List<ICYBORGPlayer> list = new ArrayList<>();
		for (ICYBORGPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void syncOrder(ICYBORGPlayer player) {
		campA.orderCollection.syncOrder(player);
		campB.orderCollection.syncOrder(player);
		campC.orderCollection.syncOrder(player);
		campD.orderCollection.syncOrder(player);
	}

	public CYBORGGuildBaseInfo getCampBase(CYBORG_CAMP camp) {
		switch (camp) {
		case A:
			return campA;
		case B:
			return campB;
		case C:
			return campC;
		case D:
			return campD;

		default:
			throw new RuntimeException();
		}
	}

	public CYBORGGuildBaseInfo getCampBase(String guildId) {

		if (Objects.equals(guildId, campA.campGuild)) {
			return campA;
		}
		if (Objects.equals(guildId, campB.campGuild)) {
			return campB;
		}

		if (Objects.equals(guildId, campC.campGuild)) {
			return campC;
		}
		if (Objects.equals(guildId, campD.campGuild)) {
			return campD;
		}
		throw new RuntimeException();
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - HawkTime.getMillisecond() < 3000;
	}

	public void sync(ICYBORGPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			buildSyncPB();
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_GAME_SYNC, lastSyncpb.toBuilder()));
	}

	public boolean isHotBloodModel() {
		return overTime - curTimeMil < getCfg().getHotBloodModel() * 1000;
	}

	public PBCYBORGGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBCYBORGGameInfoSync.Builder bul = PBCYBORGGameInfoSync.newBuilder();
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		bul.setMonsterCount(worldMonsterCount());
		List<ICYBORGPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (ICYBORGPlayer p : all) {
			bul.addPlayerInfo(p.genPBCYBORGPlayerInfo());
		}

		List<PBCYBORGGuildInfo.Builder> allGildInfo = new ArrayList<>();
		allGildInfo.add(buildGuildStaticInfo(campA));
		allGildInfo.add(buildGuildStaticInfo(campB));
		allGildInfo.add(buildGuildStaticInfo(campC));
		allGildInfo.add(buildGuildStaticInfo(campD));
		Collections.sort(allGildInfo, Comparator.comparingInt(PBCYBORGGuildInfo.Builder::getHonor));

		int allHonor = 0;
		for (PBCYBORGGuildInfo.Builder ginfo : allGildInfo) {
			allHonor += ginfo.getHonor();
		}

		int leftRate = (int) GsConst.EFF_RATE;
		int leftItem = CyborgConstCfg.getInstance().getCyborgItemTotal();
		for (int i = 0; i < allGildInfo.size(); i++) {
			PBCYBORGGuildInfo.Builder ginfo = allGildInfo.get(i);
			if (ginfo.getHonor() <= 0) {
				continue;
			}
			if (i == 3) { // 剩下的全给第一
				ginfo.setHonorRate(leftRate);
				ginfo.setCyborgItemTotal(leftItem);

				break;
			}

			int rate = (int) (Math.round(ginfo.getHonor() * 1000D / allHonor)) * 10;
			int itemc = (int) (CyborgConstCfg.getInstance().getCyborgItemTotal() * GsConst.EFF_PER * rate);
			leftRate -= rate;
			leftItem -= itemc;
			ginfo.setHonorRate(rate);
			ginfo.setCyborgItemTotal(itemc);
		}
		for (PBCYBORGGuildInfo.Builder ginfo : allGildInfo) {
			bul.addGuildInfo(ginfo);
			CYBORGGuildBaseInfo buildcamp = getCampBase(ginfo.getGuildId());
			buildcamp.honorRate = ginfo.getHonorRate();
			buildcamp.cyborgItemTotal = ginfo.getCyborgItemTotal();
		}

		lastSyncpb = bul.build();
		lastSyncGame = curTimeMil;
		return bul;
	}

	private PBCYBORGGuildInfo.Builder buildGuildStaticInfo(CYBORGGuildBaseInfo buildcamp) {
		PBCYBORGGuildInfo.Builder aInfo = PBCYBORGGuildInfo.newBuilder()
				.setServerId(buildcamp.campServerId)
				.setCamp(buildcamp.camp.intValue())
				.setGuildFlag(buildcamp.campguildFlag)
				.setGuildName(buildcamp.campGuildName)
				.setGuildTag(buildcamp.campGuildTag)
				.setGuildId(buildcamp.campGuild)
				.setTeamName(buildcamp.campTeamName)
				.setTeamPower(buildcamp.campTeamPower);

		int honorA = buildcamp.campNianATKHonor; // 当前积分
		int perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		long centerControlA = 0; // CYBORG_HEADQUARTERS 核心控制时间
		int buildControlHonorA = 0;
		int killHonorA = 0;
		int collectHonorA = 0;

		int lostBuild = 0;
		int killMonster = 0;

		for (ICYBORGWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof ICYBORGBuilding) {
				ICYBORGBuilding build = (ICYBORGBuilding) viewp;
				honorA += build.getControlGuildHonorMap().getOrDefault(buildcamp.campGuild, 0D);
				buildControlHonorA += build.getControlGuildHonorMap().getOrDefault(buildcamp.campGuild, 0D);
				if (build.getPointType() == WorldPointType.CYBORG_HEADQUARTERS) {
					centerControlA = build.getControlGuildTimeMap().get(buildcamp.campGuild);
				}
				if (build.getState() == CYBORGBuildState.ZHAN_LING && build.underGuildControl(buildcamp.campGuild)) {
					if (!build.isRoot()) {
						buildCountA++;
						if (build.canBeAttack(buildcamp.camp)) {
							perMinA += build.getGuildHonorPerSecond() * 60 + 0.1;
						}
					}
				}
				
				if (Objects.nonNull(build.getTreeCfg()) 
						&& build.getTreeCfg().getCamp() == buildcamp.camp.intValue() 
						&& build.getState() == CYBORGBuildState.ZHAN_LING
						&& !build.underGuildControl(buildcamp.campGuild)) {
					lostBuild++;
				}
			}
			if (viewp instanceof CYBORGNuclearMissileSilo) {
				CYBORGNuclearMissileSilo nucl = (CYBORGNuclearMissileSilo) viewp;
				if (!aInfo.getNuclearReady() && Objects.equals(buildcamp.campGuild, nucl.getNuclearReadyGuild())) {
					aInfo.setNuclearReady(true);
				}
				aInfo.addNuclearBuildLeaderid(nucl.getNuclearReadyLeader());
			}
		}

		List<ICYBORGPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		buildcamp.playerIds.clear();
		PBCYBORGPlayerInfo killPowerMax = null; // 本队击杀战力最高玩家
		PBCYBORGPlayerInfo lostPowerMax = null; // 本队损失战力最高玩家
		PBCYBORGPlayerInfo killMonsterMax = null; // 本队击杀野怪最高玩家
		for (ICYBORGPlayer p : all) {
			if (p.getCamp() != buildcamp.camp) {
				continue;
			}
			honorA += p.getGuildHonor();
			killHonorA += p.getKillHonor();
			collectHonorA += p.getGuildHonor();
			killMonster += p.getKillMonster();
			playerCountA++;
			buildcamp.playerIds.add(p.getId());
			buildcamp.isCsGuild = p.isCsPlayer();
			if (Objects.isNull(killPowerMax) || killPowerMax.getKillPower() < p.getKillPower()) {
				killPowerMax = p.genPBCYBORGPlayerInfo();
			}
			if (Objects.isNull(lostPowerMax) || lostPowerMax.getLostPower() < p.getHurtTankPower()) {
				lostPowerMax = p.genPBCYBORGPlayerInfo();
			}
			if (Objects.isNull(killMonsterMax) || killMonsterMax.getKillMonster() < p.getKillMonster()) {
				killMonsterMax = p.genPBCYBORGPlayerInfo();
			}
		}
		aInfo.setHonor(honorA).setPerMin(perMinA).setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setCenterControl(centerControlA).setBuildControlHonor(buildControlHonorA)
				.setKillHonor(killHonorA).setCollectHonor(collectHonorA)
				.setNuclearCount(buildcamp.campNuclearSendCount)
				.setNianKillCnt(buildcamp.campNianKillCount)
				.setKillMonster(killMonster);

		if (Objects.nonNull(killPowerMax) && killPowerMax.getKillPower() > 0) {
			aInfo.setKillPowerMax(killPowerMax);
		}
		if (Objects.nonNull(lostPowerMax) && lostPowerMax.getLostPower() > 0) {
			aInfo.setLostPowerMax(lostPowerMax);
		}
		if (Objects.nonNull(killMonsterMax) && killMonsterMax.getKillMonster() > 0) {
			aInfo.setKillMonsterMax(killMonsterMax);
		}

		int bblevel = 0;
		for (CYBORGBuildBuffLevelCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CYBORGBuildBuffLevelCfg.class)) {
			if (cfg.getExp() <= killMonster) {
				bblevel = Math.max(bblevel, cfg.getLevel());
			}
		}

		int bblevelextra = 0;
		for (CYBORGBuildBuffLevelExtraCfg cfg : HawkConfigManager.getInstance().getConfigIterator(CYBORGBuildBuffLevelExtraCfg.class)) {
			if (cfg.getLostBuild() <= lostBuild) {
				bblevelextra = Math.max(bblevelextra, cfg.getLevel());
			}
		}
		buildcamp.buildBuffLevel = bblevel;
		buildcamp.buildBuffLevelExtra = bblevelextra;
		buildcamp.buildBuffCfgLevel = Math.min(bblevel + bblevelextra , CYBORGBuildBuffCfg.maxLevel);
		aInfo.setBuildbuffLevel(bblevel);
		aInfo.setBuildbuffLevelExtra(bblevelextra);
		return aInfo;
	}

	public void getSecondMap(ICYBORGPlayer player) {
		PBCYBORGSecondMapResp.Builder bul = PBCYBORGSecondMapResp.newBuilder();
		for (ICYBORGWorldPoint point : getViewPoints()) {
			bul.addPoints(point.toBuilder(player));
		}
		bul.setMonsterCount(worldMonsterCount());
		bul.setNextMonsterRefresh(monsterrefresh.nextRefreshTime());
		bul.setMonsterTurn(monsterrefresh.getRefreshTurn());
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_SECOND_MAP_S, bul));

	}

	public void calcKillAndHurtPower(BattleOutcome battleOutcome, List<Player> atkPlayers, List<Player> defPlayers) {
		Map<String, Map<Integer, Integer>> atkKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> atkHurtMap = new HashMap<>();
		BattleService.getInstance().calcKillAndHurtInfo(atkKillMap, atkHurtMap, battleOutcome.getBattleArmyMapAtk(), battleOutcome.getBattleArmyMapDef());
		calcKillAndHurtPower(atkPlayers, atkKillMap, atkHurtMap);

		Map<String, Map<Integer, Integer>> defKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> defHurtMap = new HashMap<>();
		BattleService.getInstance().calcKillAndHurtInfo(defKillMap, defHurtMap, battleOutcome.getBattleArmyMapDef(), battleOutcome.getBattleArmyMapAtk());
		calcKillAndHurtPower(defPlayers, defKillMap, defHurtMap);

		// 攻击方剩余兵力
		// 计算损失兵力
		calcSelfLosePower(atkPlayers, battleOutcome.getBattleArmyMapAtk());
		// 防守方剩余兵力
		// 计算损失兵力
		calcSelfLosePower(defPlayers, battleOutcome.getBattleArmyMapDef());
	}

	private void calcSelfLosePower(List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
		for (Player ppp : battlePlayers) {
			ICYBORGPlayer pl = (ICYBORGPlayer) ppp;
			int killPow = 0;
			int killHonor = 0;
			int killCnt = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				int scoreForDefense = getCfg().getScoreForDefense(cfg.getSoldierType());
				if (scoreForDefense > 0) {
					int count = army.getDeadCount() + army.getWoundedCount();
					float power = cfg.getPower() * count;
					killPow += power;
					killHonor += power/ scoreForDefense;
					killCnt += count;
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
			pl.setHurtTankHonor(pl.getHurtTankHonor() + killHonor);
			pl.setHurtTankCount(pl.getHurtTankCount() + killCnt);
		}
	}
	
	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			int killPow = 0;
			int killCnt = 0;
			ICYBORGPlayer pl = (ICYBORGPlayer) ppp;
			{
				Map<Integer, Integer> killMap = battleKillMap.get(pl.getId());
				if (killMap != null) {
					for (Entry<Integer, Integer> ent : killMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
						killCnt += ent.getValue();
					}
				}
			}
			{
				Map<Integer, Integer> hurtMap = battleHurtMap.get(pl.getId());
				if (hurtMap != null) {
					for (Entry<Integer, Integer> ent : hurtMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
						killCnt += ent.getValue();
					}
				}
			}
			pl.setKillPower(pl.getKillPower() + killPow);
			pl.setKillCount(pl.getKillCount() + killCnt);
		}
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		HawkLog.logPrintln("CYBORG GAME CREATE parm: {}", extParm.toString());
		campA.campGuild = extParm.getCampAGuild();
		campA.campGuildName = extParm.getCampAGuildName();
		campA.campGuildTag = extParm.getCampAGuildTag();
		campA.campServerId = extParm.getCampAServerId();
		campA.campguildFlag = extParm.getCampAguildFlag();
		campA.campTeamName = extParm.getCampATeamName();
		campA.campTeamPower = extParm.getCampATeamPower();
		campA.camp = CYBORG_CAMP.A;
		campA.orderCollection = new CYBORGOrderCollection(this, CYBORG_CAMP.A);
		battleCamps.add(campA);

		campB.campGuild = extParm.getCampBGuild();
		campB.campGuildName = extParm.getCampBGuildName();
		campB.campGuildTag = extParm.getCampBGuildTag();
		campB.campServerId = extParm.getCampBServerId();
		campB.campguildFlag = extParm.getCampBguildFlag();
		campB.campTeamName = extParm.getCampBTeamName();
		campB.campTeamPower = extParm.getCampBTeamPower();
		campB.camp = CYBORG_CAMP.B;
		campB.orderCollection = new CYBORGOrderCollection(this, CYBORG_CAMP.B);
		battleCamps.add(campB);

		campC.campGuild = extParm.getCampCGuild();
		campC.campGuildName = extParm.getCampCGuildName();
		campC.campGuildTag = extParm.getCampCGuildTag();
		campC.campServerId = extParm.getCampCServerId();
		campC.campguildFlag = extParm.getCampCguildFlag();
		campC.campTeamName = extParm.getCampCTeamName();
		campC.campTeamPower = extParm.getCampCTeamPower();
		campC.camp = CYBORG_CAMP.C;
		campC.orderCollection = new CYBORGOrderCollection(this, CYBORG_CAMP.C);
		battleCamps.add(campC);

		campD.campGuild = extParm.getCampDGuild();
		campD.campGuildName = extParm.getCampDGuildName();
		campD.campGuildTag = extParm.getCampDGuildTag();
		campD.campServerId = extParm.getCampDServerId();
		campD.campguildFlag = extParm.getCampDguildFlag();
		campD.campTeamName = extParm.getCampDTeamName();
		campD.campTeamPower = extParm.getCampDTeamPower();
		campD.camp = CYBORG_CAMP.D;
		campD.orderCollection = new CYBORGOrderCollection(this, CYBORG_CAMP.D);
		battleCamps.add(campD);

		nextSyncToPlayer = createTime + 5000;
		registerModule(CYBORGConst.ModuleType.CYBORGWorld, new CYBORGWorldModule(this));
		registerModule(CYBORGConst.ModuleType.CYBORGMarch, new CYBORGMarchModule(this));
		registerModule(CYBORGConst.ModuleType.CYBORGArmy, new CYBORGArmyModule(this));

		CYBORGBattleCfg cfg = getCfg();
		List<int[]> bornlist = cfg.copyOfbornPointAList();
		bornPointAList = new CYBORGBornPointRing(bornlist.get(0), bornlist.get(1));

		bornlist = cfg.copyOfbornPointBList();
		bornPointBList = new CYBORGBornPointRing(bornlist.get(0), bornlist.get(1));

		bornlist = cfg.copyOfbornPointCList();
		bornPointCList = new CYBORGBornPointRing(bornlist.get(0), bornlist.get(1));

		bornlist = cfg.copyOfbornPointDList();
		bornPointDList = new CYBORGBornPointRing(bornlist.get(0), bornlist.get(1));

		{
			CYBORGChronoSphereCfg buildcfg = CYBORGChronoSphere.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				CYBORGChronoSphere icd = new CYBORGChronoSphere(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{
			ConfigIterator<CYBORGCommandCenterCfg> cmdit = HawkConfigManager.getInstance().getConfigIterator(CYBORGCommandCenterCfg.class);
			for (CYBORGCommandCenterCfg buildcfg : cmdit) {
				int index = 0;
				for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
					CYBORGCommandCenter icd = new CYBORGCommandCenter(this);
					icd.setCfgId(buildcfg.getId());
					icd.setIndex(index);
					icd.setX(rp.first);
					icd.setY(rp.second);
					viewPoints.put(icd.getPointId(), icd);
					buildingList.add(icd);
					CYBORGBuildingMap.put(icd.getClass(), icd);
					index++;
				}
			}

		}

		{
			CYBORGHeadQuartersCfg buildcfg = CYBORGHeadQuarters.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				CYBORGHeadQuarters icd = new CYBORGHeadQuarters(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{
			CYBORGIronCurtainDeviceCfg ironCfg = CYBORGIronCurtainDevice.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : ironCfg.getRefreshPointList()) {
				CYBORGIronCurtainDevice icd = new CYBORGIronCurtainDevice(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{
			CYBORGMilitaryBaseCfg buildcfg = CYBORGMilitaryBase.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				CYBORGMilitaryBase icd = new CYBORGMilitaryBase(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{
			CYBORGNuclearMissileSiloCfg buildcfg = CYBORGNuclearMissileSilo.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				CYBORGNuclearMissileSilo icd = new CYBORGNuclearMissileSilo(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				nuclearBuildList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{
			CYBORGWeatherControllerCfg buildcfg = CYBORGWeatherController.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				CYBORGWeatherController icd = new CYBORGWeatherController(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				CYBORGBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}
		for (ICYBORGBuilding build : getCYBORGBuildingList()) {
			if (build.isRoot()) {
				build.setState(CYBORGBuildState.ZHAN_LING);
				CYBORG_CAMP rootCamp = CYBORG_CAMP.valueOf(build.getTreeCfg().getCamp());
				getCampBase(rootCamp).campRootId = build.getPointId();
			}
		}
		for (ICYBORGBuilding build : getCYBORGBuildingList()) {
			build.checkAttackCampChanged();
		}

		startTime = createTime + cfg.getPrepairTime() * 1000;
		this.nextLoghonor = startTime + CYBORGConst.MINUTE_MICROS;

		nianRefreshTimes.addAll(CYBORGNian.getCfg().getRefreshTimeList());
		nextRefreshNian = createTime + nianRefreshTimes.remove(0) * 1000;

		monsterrefresh = CYBORGMonsterRefesh.create(this);

		resetOccupationPoint();

		long periodTime = TiberiumConstCfg.getInstance().getPerioTime() * 1L;
		this.addTickable(new HawkPeriodTickable(periodTime) {

			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});
		DungeonRedisLog.log(getId(), "init");
	}

	public void onPlayerLogin(ICYBORGPlayer gamer) {
		gamer.getPush().syncPlayerWorldInfo();
		gamer.getPush().syncPlayerInfo();
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<ICYBORGWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (ICYBORGWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				ICYBORGWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof ICYBORGPassiveAlarmTriggerMarch) {
				((ICYBORGPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<ICYBORGWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (ICYBORGWorldMarch march : pointMs) {
			if (march instanceof ICYBORGReportPushMarch) {
				((ICYBORGReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

		for (ICYBORGBuilding build : getCYBORGBuildingList()) {
			build.onPlayerLogin(gamer);
		}

		// 号令同步
		syncOrder(gamer);
	}

	public void updateRoomActiveTime() {
		long curTime = HawkTime.getMillisecond();
		if (curTime > this.getCreateTime() && curTime < this.getOverTime()) {
			int termId = CyborgWarService.getInstance().getTermId();
			CWRoomData roomData = CyborgWarRedis.getInstance().getCWRoomData(this.getId(), termId);
			if (roomData == null || roomData.getRoomState() != CWConst.RoomState.INITED) {
				return;
			}

			roomData.setLastActiveTime(curTime);
			CyborgWarRedis.getInstance().updateCWRoomData(roomData, termId);
		}
	}

	public void doMoveCitySuccess(ICYBORGPlayer player, int[] targetPoint) {
		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(targetPoint[0]);
		builder.setY(targetPoint[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));
		// 恢复城墙状态，投递回玩家线程执行
		player.setOnFireEndTime(0);
		player.setCityDefNextRepairTime(0);
		player.setCityDefVal(Integer.MAX_VALUE);
		player.getPush().syncCityDef(false);

		// 重推警报
		cleanCityPointMarch(player);
		for (ICYBORGWorldMarch march : worldMarches.values()) {
			if (march instanceof ICYBORGReportPushMarch) {
				((ICYBORGReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		removeViewPoint(player);
		player.setPos(targetPoint);
		addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();
	}

	public ICYBORGWorldMarch startMarch(ICYBORGPlayer player, ICYBORGWorldPoint fPoint, ICYBORGWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		// 生成行军
		ICYBORGWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
		List<PlayerHero> OpHero = player.getHeroByCfgId(march.getMarchEntity().getHeroIdList());
		for (PlayerHero hero : OpHero) {
			hero.goMarch(march);
		}
		
		Optional<SuperSoldier> sso = player.getSuperSoldierByCfgId(march.getMarchEntity().getSuperSoldierId());
		if (sso.isPresent()) {
			sso.get().goMarch(march);
		}
		// 行军上需要显示的作用号
		List<Integer> marchShowEffList = new ArrayList<>();
		int[] marchShowEffs = WorldMarchConstProperty.getInstance().getMarchShowEffArray();
		if (marchShowEffs != null) {
			for (int i = 0; i < marchShowEffs.length; i++) {
				int effVal = player.getEffect().getEffVal(EffType.valueOf(marchShowEffs[i]));
				if (effVal > 0) {
					marchShowEffList.add(marchShowEffs[i]);
				}
			}
		}

		if (!marchShowEffList.isEmpty()) {
			march.getMarchEntity().resetEffect(marchShowEffList);
		}

		// 保存行军并推送给对应的玩家
		addMarch(march);

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.toBuilder(WorldMarchRelation.SELF));
		player.sendProtocol(protocol);

		march.notifyMarchEvent(MarchEvent.MARCH_ADD); // 通知行军事件

		// 加入行军警报
		march.onMarchStart();

		tPoint.onMarchCome(march);
		player.onMarchStart(march);

		return march;
	}

	public boolean isExtraSypMarchOccupied(Player player) {
		List<ICYBORGWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
		for (IWorldMarch march : spyMarchs) {
			if (march.isExtraSpyMarch()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 判断额外侦查队列是否可以使用了
	 * 
	 * @param player
	 * @return
	 */
	public boolean isExtraSpyMarchOpen(Player player) {
		return WorldMarchService.getInstance().isExtraSpyMarchOpen(player);
	}

	/** 生成一个行军对象 */
	public ICYBORGWorldMarch genMarch(ICYBORGPlayer player, ICYBORGWorldPoint fPoint, ICYBORGWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		CYBORGMarchEntity march = new CYBORGMarchEntity();
		march.setArmys(effParams.getArmys());
		march.setStartTime(startTime);
		march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_MARCH_VALUE);
		march.setOrigionId(fPoint.getPointId());
		march.setTerminalId(tPoint.getPointId());
		march.setAlarmPointId(tPoint.getPointId());
		march.setTargetId(targetId);
		march.setMarchType(marchType.getNumber());
		march.setSuperSoldierId(effParams.getSuperSoliderId());
		march.setPlayerId(player.getId());
		march.setPlayerName(player.getName());
		if (Objects.nonNull(effParams.getHeroIds())) {
			march.setHeroIdList(effParams.getHeroIds());
		}

		// 使用额外侦查行军队列
		if (marchType == WorldMarchType.SPY && !isExtraSypMarchOccupied(player) && isExtraSpyMarchOpen(player)) {
			march.setExtraSpyMarch(true);
		}

		if (effParams.getArmourSuit() != null) {
			int armourSuit = effParams.getArmourSuit().getNumber();
			if (armourSuit > 0 && armourSuit <= player.getEntity().getArmourSuitCount()) {
				march.setArmourSuit(armourSuit);
			}
		}
		MechaCoreSuitType mechaSuit = effParams.getMechacoreSuit();
		if (mechaSuit != null && player.getPlayerMechaCore().isSuitUnlocked(mechaSuit.getNumber())) {
			march.setMechacoreSuit(mechaSuit.getNumber());
		} else {
			march.setMechacoreSuit(player.getPlayerMechaCore().getWorkSuit());
		}
		
		if (effParams.getDressList().size() > 0) {
			march.setDressList(effParams.getDressList());
		}
		march.setTalentType(effParams.getTalent());
		march.setSuperLab(effParams.getSuperLab());

		march.setTargetPointType(tPoint.getPointType().getNumber());
		// 把个人编队记录下来
		if (effParams.getWorldmarchReq() != null) {
			march.setFormation(effParams.getWorldmarchReq().getFormation());
		}
		// 超武
		int atkId = effParams.getManhattanAtkSwId();
		if (atkId > 0) {
			march.setManhattanAtkSwId(atkId);
		}
		int defId = effParams.getManhattanDefSwId();
		if (defId > 0) {
			march.setManhattanDefSwId(defId);
		}
		ICYBORGWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new CYBORGAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new CYBORGSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new CYBORGAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new CYBORGMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new CYBORGMassJoinSingleMarch(player);
			break;
		case CYBORG_IRON_CRUTAIN_DIVICE_SINGLE:// = 101; // 单人铁幕装置行军
		case CYBORG_NUCLEAR_MISSILE_SILO_SINGLE: // = 102; // 单人核弹发射井行军
		case CYBORG_WEATHER_CONTROLLER_SINGLE: // = 103; // 天气控制器
		case CYBORG_CHRONO_SPHERE_SINGLE: // = 104; // 超时空传送器
		case CYBORG_COMMAND_CENTER_SINGLE: // = 105; // 指挥部
		case CYBORG_MILITARY_BASE_SINGLE: // = 106; // 军事基地
		case CYBORG_HEADQUARTERS_SINGLE: // = 107; // 司令部
			iWorldMarch = new CYBORGBuildingMarchSingle(player);
			((CYBORGBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case CYBORG_IRON_CRUTAIN_DIVICE_MASS:// = 111; // 集结铁幕装置行军
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_IRON_CRUTAIN_DIVICE_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_IRON_CRUTAIN_DIVICE_MASS_JOIN);
			break;
		case CYBORG_NUCLEAR_MISSILE_SILO_MASS:// = 112; // 集结核弹发射井行军
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_NUCLEAR_MISSILE_SILO_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_NUCLEAR_MISSILE_SILO_MASS_JOIN);
			break;
		case CYBORG_WEATHER_CONTROLLER_MASS:// = 113; // 天气控制器
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_WEATHER_CONTROLLER_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_WEATHER_CONTROLLER_MASS_JOIN);
			break;
		case CYBORG_CHRONO_SPHERE_MASS:// = 114; // 超时空传送器
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_CHRONO_SPHERE_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_CHRONO_SPHERE_MASS_JOIN);
			break;
		case CYBORG_COMMAND_CENTER_MASS:// = 115; // 指挥部
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_COMMAND_CENTER_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_COMMAND_CENTER_MASS_JOIN);
			break;
		case CYBORG_MILITARY_BASE_MASS:// = 116; // 军事基地
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_MILITARY_BASE_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_MILITARY_BASE_MASS_JOIN);
			break;
		case CYBORG_HEADQUARTERS_MASS:// = 117; // 司令部
			iWorldMarch = new CYBORGBuildingMarchMass(player);
			((CYBORGBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.CYBORG_HEADQUARTERS_MASS);
			((CYBORGBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.CYBORG_HEADQUARTERS_MASS_JOIN);
			break;

		case CYBORG_IRON_CRUTAIN_DIVICE_MASS_JOIN:// = 121; // 加入集结铁幕装置行军
		case CYBORG_NUCLEAR_MISSILE_SILO_MASS_JOIN:// = 122; // 加入集结核弹发射井行军
		case CYBORG_WEATHER_CONTROLLER_MASS_JOIN:// = 123; // 天气控制器
		case CYBORG_CHRONO_SPHERE_MASS_JOIN:// = 124; // 超时空传送器
		case CYBORG_COMMAND_CENTER_MASS_JOIN:// = 125; // 指挥部
		case CYBORG_MILITARY_BASE_MASS_JOIN:// = 126; // 军事基地
		case CYBORG_HEADQUARTERS_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new CYBORGBuildingMarchMassJoin(player);
			break;
		case NIAN_SINGLE:
			iWorldMarch = new CYBORGNianMarchSingle(player);
			break;
		case NIAN_MASS:
			iWorldMarch = new CYBORGNianMarchMass(player);
			break;
		case NIAN_MASS_JOIN:
			iWorldMarch = new CYBORGNianMarchMassJoin(player);
			break;
		case ATTACK_MONSTER:
			iWorldMarch = new CYBORGAttackMonsterMarch(player);
			break;
		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!! " + marchType);
		}
		iWorldMarch.setMarchEntity(march);
		// marchId设置放在前面
		march.setMarchId(HawkOSOperator.randomUUID());

		// 行军时间
		long needTime = iWorldMarch.getMarchNeedTime();
		march.setEndTime(startTime + needTime);
		march.setMarchJourneyTime((int) needTime);

		// 集结等待时间
		if (waitTime > 0) {
			waitTime *= 1000;

			// 作用号618：集结所需时间减少 -> 实际集结时间 = 基础集结时间 * （1 - 作用值/10000）；向上取整；不得小于0
			waitTime *= 1 - player.getEffect().getEffVal(EffType.GUILD_MASS_TIME_REDUCE_PER, effParams) * GsConst.EFF_PER;
			waitTime = waitTime > 0 ? waitTime : 0;

			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE);
			march.setMassReadyTime(march.getStartTime());
			march.setStartTime(march.getStartTime() + waitTime);
			march.setEndTime(march.getEndTime() + waitTime);
		}
		if (iWorldMarch instanceof ICYBORGMassJoinMarch) {
			iWorldMarch.setHashThread(((ICYBORGMassJoinMarch) iWorldMarch).leaderMarch().get().getHashThread());
		} else {
			final int threadNum = HawkTaskManager.getInstance().getThreadNum();
			iWorldMarch.setHashThread(tPoint.getHashThread(threadNum));
		}
		return iWorldMarch;
	}

	public int[] randomPoint() {
		ICYBORGBuilding build = HawkRand.randomObject(buildingList);
		return GameUtil.splitXAndY(build.getPointId());
	}

	public CYBORGBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(CYBORGBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public ICYBORGWorldMarch getPlayerMarch(String playerId, String marchId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId) && Objects.equals(m.getMarchId(), marchId)).findAny().orElse(null);
	}

	public List<ICYBORGWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<ICYBORGWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<ICYBORGWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	@SuppressWarnings("unchecked")
	public <T extends ICYBORGBuilding> List<T> getCYBORGBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) CYBORGBuildingMap.get(type));
	}

	/** 被占领出生属于已方建筑数*/
	public int lostBornBuildCnt(CYBORG_CAMP camp) {
		long result = buildingList.stream()
				.filter(build -> build.bornCamp() == camp)
				.filter(build -> build.getState() == CYBORGBuildState.ZHAN_LING)
				.filter(build -> !Objects.equals(build.getGuildId(), build.bornGuild()))
				.count();
		return (int) result;
	}

	public List<ICYBORGBuilding> getCYBORGBuildingList() {
		return buildingList;
	}

	public List<ICYBORGWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ICYBORGWorldMarch> result = new ArrayList<>();
		for (ICYBORGWorldMarch ma : worldMarches.values()) {
			if (!Objects.equals(playerId, ma.getPlayerId())) {
				continue;
			}
			if (!typeList.isEmpty() && !typeList.contains(ma.getMarchType())) {
				continue;
			}
			boolean a = Objects.nonNull(status1) && ma.getMarchStatus() == status1.getNumber();
			boolean b = Objects.nonNull(status2) && ma.getMarchStatus() == status2.getNumber();
			boolean c = Objects.nonNull(status3) && ma.getMarchStatus() == status3.getNumber();
			if (free || a || b || c) {
				result.add(ma);
			}
		}
		return result;

	}

	public ICYBORGWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<ICYBORGWorldMarch> getGuildWarMarch(String guildId) {
		List<ICYBORGWorldMarch> result = new ArrayList<>();
		for (ICYBORGWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof CYBORGBuildingMarchMass) {
					ICYBORGBuilding point = (ICYBORGBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof CYBORGBuildingMarchSingle) {
					ICYBORGBuilding point = (ICYBORGBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					} else {
						if (Objects.equals(point.getGuildId(), guildId)) {// 如果已被已方控制
							continue;
						}
					}
				}
				if (march instanceof CYBORGNianMarchMass && !Objects.equals(march.getParent().getGuildId(), guildId)) {
					continue;
				}

				if (march instanceof CYBORGAttackPlayerMarch || march instanceof CYBORGMassSingleMarch) {
					ICYBORGWorldPoint point = getWorldPoint(march.getTerminalX(), march.getTerminalY()).orElse(null);
					// 路点为空
					if (point == null || !(point instanceof ICYBORGPlayer)) {
						continue;
					}
					ICYBORGPlayer defplayer = (ICYBORGPlayer) point;
					if (!Objects.equals(guildId, defplayer.getGuildId()) && !Objects.equals(guildId, march.getPlayer().getGuildId())) {
						continue;
					}
				}

				result.add(march);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return result;
	}

	public List<ICYBORGWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<ICYBORGWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<ICYBORGWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<ICYBORGWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ICYBORGWorldMarch> result = new LinkedList<>();
		for (ICYBORGWorldMarch ma : worldMarches.values()) {
			// boolean isNotO = ma.getOrigionX() != x || ma.getOrigionY() != y;
			// // 不是起点行军
			boolean isNotT = ma.getTerminalX() != x || ma.getTerminalY() != y;
			if (isNotT) {
				continue;
			}
			if (!typeList.isEmpty() && !typeList.contains(ma.getMarchType())) {
				continue;
			}
			boolean a = Objects.nonNull(status1) && ma.getMarchStatus() == status1.getNumber();
			boolean b = Objects.nonNull(status2) && ma.getMarchStatus() == status2.getNumber();
			boolean c = Objects.nonNull(status3) && ma.getMarchStatus() == status3.getNumber();
			if (free || a || b || c) {
				result.add(ma);
			}
		}
		return result;

	}

	/** 根据坐标获得世界点信息，若是未被占用的点就返回空 */
	public Optional<ICYBORGWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(viewPoints.get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<ICYBORGWorldPoint> getWorldPoint(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getWorldPoint(pos[0], pos[1]);
	}

	@Override
	public boolean onTick() {
		if (Objects.isNull(state)) {
			return false;
		}

		curTimeMil = HawkTime.getMillisecond();
		
		try {
			state.onTick();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		if (curTimeMil > nextSyncToPlayer) {
			nextSyncToPlayer = curTimeMil + 3000;
			PBCYBORGGameInfoSync.Builder builder = buildSyncPB();
			// 夸服玩家和本服分别广播
			for (CYBORGGuildBaseInfo campBase : getBattleCamps()) {
				if (campBase.isCsGuild) {
					CrossProxy.getInstance().broadcastProtocol(campBase.campServerId, campBase.playerIds,
							HawkProtocol.valueOf(HP.code.CYBORG_GAME_SYNC, builder));

				}
			}

			for (ICYBORGPlayer p : playerMap.values()) {
				if (!p.isCsPlayer()) {
					sync(p);
				}
			}

			// 发送主播
			if (hasAnchor()) {
				sync(anchor);
			}
		}

		// 刷机甲
		if (curTimeMil > nextRefreshNian) {
			if (!nianRefreshTimes.isEmpty()) {
				nextRefreshNian = nianRefreshTimes.remove(0) * 1000 + createTime;
			} else {
				nextRefreshNian = Long.MAX_VALUE;
			}

			ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.CYBORG_NIAN_CHUXIAN).build();
			addWorldBroadcastMsg(parames);
			nianCount += CYBORGNian.getCfg().getRefreshCount();
		}

		if (nianCount > 0) {
			nianCount--;
			try {
				CYBORGNian res = new CYBORGNian(this);
				int[] xy = randomFreePoint(CYBORGNian.getCfg().randomBoinPoint(), res.getPointType());
				if (xy != null) {
					res.setX(xy[0]);
					res.setY(xy[1]);
					addViewPoint(res);
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		monsterrefresh.onTick();

		campA.orderCollection.onTick();
		campB.orderCollection.onTick();
		campC.orderCollection.onTick();
		campD.orderCollection.onTick();

		try {
			if (getCurTimeMil() > nextLoghonor) {
				this.nextLoghonor += CYBORGConst.MINUTE_MICROS;
				// List<PBCYBORGGuildInfo> guildList = lastSyncpb.getGuildInfoList();
				// for (PBCYBORGGuildInfo ginfo : guildList) {
				// String logGuild = ginfo.getCamp() == CAMP.A.intValue() ? campAGuild : campB.campGuild;
				// LogUtil.logCYBORGGuildHonor(getXid().getUUID(), logGuild, ginfo.getGuildName(), ginfo.getHonor());
				// }
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(ICYBORGPlayer player) {
		state.enterWorld(player);
	}

	/**
	 * 副本中玩家更新点
	 * 
	 * @param point
	 */
	public void worldPointUpdate(ICYBORGWorldPoint point) {
		List<ICYBORGWorldPoint> tosend = new LinkedList<>();
		tosend.add(point);
		if (point instanceof ICYBORGBuilding) {
			for (ICYBORGBuilding build : getCYBORGBuildingList()) {
				if (build.checkAttackCampChanged() && point != build) {
					tosend.add(build);
				}
			}
		}

		for (ICYBORGPlayer pla : getPlayerList(CYBORGState.GAMEING)) {
			List<WorldPointPB> list = tosend.stream().map(p -> p.toBuilder(pla).build()).collect(Collectors.toList());
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addAllPoints(list);
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}

		if (hasAnchor()) {
			List<WorldPointPB> list = tosend.stream().map(p -> p.toBuilder(anchor).build()).collect(Collectors.toList());
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addAllPoints(list);
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(protocol);
		}
	}

	/**当前是否有主播在*/
	public boolean hasAnchor() {
		return Objects.nonNull(anchor) && anchor.isActiveOnline();
	}

	public void anchorJoinRoom(ICYBORGPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || getPlayer(player.getId()) != null) {
			return;
		}
		if (hasAnchor() && !Objects.equals(player.getId(), getAnchor().getId())) {
			getAnchor().quitGame();
		}

		player.setCamp(CYBORG_CAMP.A);

		int[] bornP = new int[] { 75, 152 };
		player.setGuildId(campA.campGuild);
		player.setGuildTag(campA.campGuildTag);
		player.setGuildFlag(campA.campguildFlag);
		player.setGuildName(campA.campGuildName);

		player.setPos(bornP);

		player.init();
		player.getPush().syncGuildInfo();

		CYBORGRoomManager.getInstance().cache(player);
		player.setCYBORGState(CYBORGState.GAMEING);

		{
			// 模拟 TiberiumWarService.getInstance().syncStateInfo(player);
			synAnchorPageInfo(player, true);
		}

		anchor = player;
		player.getPush().pushJoinGame();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ENTER_GAME_SUCCESS));

		for (ICYBORGBuilding build : getCYBORGBuildingList()) {
			build.anchorJoin(player);
		}
	}

	public void synAnchorPageInfo(ICYBORGPlayer anchor, boolean join) {
		try {
			TWPageInfo.Builder builder = TWPageInfo.newBuilder();
			TWStateInfo.Builder stateInfo = genStateInfo(join);

			builder.setIsSignUp(true);
			builder.setStateInfo(stateInfo);

			{
				TWGuildInfo.Builder oobuilder = TWGuildInfo.newBuilder();
				oobuilder.setId(campB.campGuild);
				oobuilder.setName(campB.campGuildName);
				oobuilder.setTag(campB.campGuildTag);
				oobuilder.setGuildFlag(campB.campguildFlag);
				oobuilder.setServerId(campB.campServerId);
				oobuilder.setBattlePoint(9999);
				oobuilder.setMemberCnt(50);

				builder.setOppGuild(oobuilder);
			}

			anchor.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_INFO_SYNC, builder));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private TWStateInfo.Builder genStateInfo(boolean join) {
		TWStateInfo.Builder builder = TWStateInfo.newBuilder();
		int termId = TiberiumWarService.getInstance().getTermId();
		builder.setStage(termId);
		builder.setState(join ? TWState.WAR_OPEN : TWState.NOT_OPEN);
		builder.setWarStartTime(createTime);
		builder.setWarEndTime(overTime);
		return builder;
	}

	public void joinRoom(ICYBORGPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}

		CYBORG_CAMP camp = null;
		if (Objects.equals(player.getGuildId(), campA.campGuild)) {
			camp = CYBORG_CAMP.A;
		} else if (Objects.equals(player.getGuildId(), campB.campGuild)) {
			camp = CYBORG_CAMP.B;
		} else if (Objects.equals(player.getGuildId(), campC.campGuild)) {
			camp = CYBORG_CAMP.C;
		} else if (Objects.equals(player.getGuildId(), campD.campGuild)) {
			camp = CYBORG_CAMP.D;
		}
		if (camp == null) {
			throw new RuntimeException("can not find player camp gameid = " + getId() + " playerId=" + player.getId());
		}
		player.setCamp(camp);

		int[] bornP = null;
		if (player.getCamp() == CYBORG_CAMP.A) {
			bornP = randomBornPoint(bornPointAList);
			player.setGuildId(campA.campGuild);
			player.setGuildTag(campA.campGuildTag);
			player.setGuildFlag(campA.campguildFlag);
			player.setGuildName(campA.campGuildName);
		} else if (player.getCamp() == CYBORG_CAMP.B) {
			bornP = randomBornPoint(bornPointBList);
			player.setGuildId(campB.campGuild);
			player.setGuildTag(campB.campGuildTag);
			player.setGuildFlag(campB.campguildFlag);
			player.setGuildName(campB.campGuildName);
		} else if (player.getCamp() == CYBORG_CAMP.C) {
			bornP = randomBornPoint(bornPointCList);
			player.setGuildId(campC.campGuild);
			player.setGuildTag(campC.campGuildTag);
			player.setGuildFlag(campC.campguildFlag);
			player.setGuildName(campC.campGuildName);
		} else if (player.getCamp() == CYBORG_CAMP.D) {
			bornP = randomBornPoint(bornPointDList);
			player.setGuildId(campD.campGuild);
			player.setGuildTag(campD.campGuildTag);
			player.setGuildFlag(campD.campguildFlag);
			player.setGuildName(campD.campGuildName);
		}

		while (Objects.isNull(bornP)) {
			bornP = randomFreePoint(randomPoint(), WorldPointType.PLAYER);
		}

		player.setPos(bornP);

		player.init();
		CYBORGRoomManager.getInstance().cache(player);
		player.setCYBORGState(CYBORGState.GAMEING);
		player.getPush().pushJoinGame();
		this.syncOrder(player);
		this.addViewPoint(player);
		playerMap.put(player.getId(), player);
		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_ENTER_GAME_SUCCESS));
	
		DungeonRedisLog.log(player.getId(), "roomId {}", getId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public void quitWorld(ICYBORGPlayer quitPlayer, CYBORGQuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBCYBORGGameOver.Builder builder = PBCYBORGGameOver.newBuilder();
			if (reason == CYBORGQuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_GAME_OVER, builder));
		}

		if (quitPlayer == anchor) { // 主播退了不用理他
			quitPlayer.quitGame();
			return;
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		boolean inWorld = removeViewPoint(quitPlayer);
		if (inWorld) {
			// 删除行军
			cleanCityPointMarch(quitPlayer);

			for (ICYBORGPlayer gamer : playerMap.values()) {
				PBCYBORGPlayerQuitRoom.Builder bul = PBCYBORGPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_PLAYER_QUIT, bul));
			}

			quitPlayer.getData().getQueueEntities().clear();
		}

		// if (reason == CYBORGQuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_CYBORG).setKey(NoticeCfgId.CYBORG_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();

	}

	public void cleanCityPointMarch(ICYBORGPlayer quitPlayer) {
		try {
			List<ICYBORGWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (ICYBORGWorldMarch march : quiterMarches) {
				if (march instanceof ICYBORGMassMarch) {
					march.getMassJoinMarchs(true).forEach(ICYBORGWorldMarch::onMarchCallback);
				}
				if (march instanceof ICYBORGMassJoinMarch) {
					Optional<ICYBORGMassMarch> massMarch = ((ICYBORGMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						ICYBORGMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(ICYBORGWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<ICYBORGWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (ICYBORGWorldMarch march : pms) {
				if (march.isMassMarch() && march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
					march.getMassJoinMarchs(true).forEach(jm -> jm.onMarchCallback());
					march.onMarchBack();
				} else {
					march.onMarchCallback();
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public boolean isHasAssistanceMarch(String viewerId, int pointId) {
		boolean bfalse = getPointMarches(pointId, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE).stream()
				.filter(march -> march.getPlayerId().equals(viewerId)).count() > 0;
		return bfalse;
	}

	public List<ICYBORGPlayer> getPlayerList(CYBORGState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<ICYBORGPlayer> getPlayerList(CYBORGState st1, CYBORGState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<ICYBORGPlayer> getPlayerList(CYBORGState st1, CYBORGState st2, CYBORGState st3) {
		List<ICYBORGPlayer> result = new ArrayList<>();
		for (ICYBORGPlayer player : playerMap.values()) {
			CYBORGState state = player.getCYBORGState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public ICYBORGPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public void setPlayerList(List<ICYBORGPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	/** 添加世界点 */
	public void addViewPoint(ICYBORGWorldPoint... vp) {
		List<ICYBORGWorldPoint> list = new ArrayList<>();
		for (ICYBORGWorldPoint point : vp) {
			if (point != null && !viewPoints.containsKey(point.getPointId())) {
				list.add(point);
			}
		}

		for (ICYBORGWorldPoint point : list) {
			if (point instanceof CYBORGMonster) {
				monsterList.add((CYBORGMonster) point);
			}
			viewPoints.put(point.getPointId(), point);
		}

		resetOccupationPoint();

		for (ICYBORGPlayer player : playerMap.values()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ICYBORGWorldPoint point : list) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(pp);
		}
		if (hasAnchor()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ICYBORGWorldPoint point : list) {
				builder.addPoints(point.toBuilder(getAnchor()));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			getAnchor().sendProtocol(pp);
		}
	}

	public boolean removeViewPoint(ICYBORGWorldPoint vp) {
		boolean result = Objects.nonNull(viewPoints.remove(vp.getPointId()));
		if (vp instanceof CYBORGMonster) {
			monsterList.remove(vp);
		}

		for (ICYBORGPlayer gamer : playerMap.values()) {
			// 删除点
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(gamer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		if (hasAnchor()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(getAnchor()));
			getAnchor().sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		resetOccupationPoint();
		return result;
	}

	/** 计算已占用点 */
	private void resetOccupationPoint() {
		Set<Integer> set = new HashSet<>();
		for (ICYBORGWorldPoint viewp : viewPoints.values()) {
			viewp.fillWithOcuPointId(set);
		}
		occupationPointSet = set;
	}

	public List<ICYBORGWorldPoint> getViewPoints() {
		return new ArrayList<>(viewPoints.values());
	}

	public void setViewPoints(List<ICYBORGWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<ICYBORGWorldMarch> getWorldMarchList() {
		ArrayList<ICYBORGWorldMarch> result = new ArrayList<>(worldMarches.values());
		return result;
	}

	public void removeMarch(ICYBORGWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(ICYBORGWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<ICYBORGWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public ICYBORGBattleRoomState getState() {
		return state;
	}

	public void setState(ICYBORGBattleRoomState state) {
		this.state = state;
		DungeonRedisLog.log(getId(), "{}", state.getClass().getSimpleName());
	}

	public long getCreateTime() {
		return createTime;
	}

	public int getBattleCfgId() {
		return battleCfgId;
	}

	public void setBattleCfgId(int battleCfgId) {
		this.battleCfgId = battleCfgId;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public CYBORGExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(CYBORGExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(ICYBORGPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber()).setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		ChatMsg pbMsg = parames.toPBMsg();
		broadcastChatMsg(pbMsg);
	}

	private void broadcastChatMsg(ChatMsg pbMsg) {
		Set<Player> tosend = new HashSet<>(getPlayerList(CYBORGState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg);
		if (isGuildMsg || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE) {
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), pbMsg.getAllianceId())).collect(Collectors.toSet());
		} else {
			if (hasAnchor()) {
				tosend.add(getAnchor());
			}
		}
		ChatService.getInstance().sendChatMsg(Arrays.asList(pbMsg), tosend);
	}

	public int getOverState() {
		return overState;
	}

	/** 1 胜利 2 被npc击败 3 时间结束 */
	public void setOverState(int overState) {
		this.overState = overState;
	}

	public String getId() {
		return getXid().getUUID();
	}

	public int[] randomFreePoint(int[] popBornPointA, WorldPointType pointType) {
		CYBORGBattleCfg bcfg = getCfg();
		int x;
		int y;
		for (int i = 0; i < pointSeed.size(); i++) {
			int[] pp = pointSeed.nextPoint();
			x = popBornPointA[0] + pp[0];
			y = popBornPointA[1] + pp[1];

			if ((x + y) % 2 == CYBORGPointUtil.pointRedis(pointType) % 2) {
				x += 1;
			}
			if (x < 3) {
				continue;
			}
			if (y < 3) {
				continue;
			}
			if (x > bcfg.getMapX() - 3) {
				continue;
			}
			if (y > bcfg.getMapY() - 3) {
				continue;
			}
			if (occupationPointSet.contains(GameUtil.combineXAndY(x, y))) {
				continue;
			}
			return new int[] { x, y };
		}
		return null;
	}

	public int[] randomBornPoint(CYBORGBornPointRing pointRing) {
		int x;
		int y;
		for (int i = 0; i < pointRing.size(); i++) {
			int[] pp = pointRing.nextPoint();
			x = pp[0];
			y = pp[1];

			if ((x + y) % 2 == CYBORGPointUtil.pointRedis(WorldPointType.PLAYER) % 2) {
				x += 1;
			}

			if (occupationPointSet.contains(GameUtil.combineXAndY(x, y))) {
				continue;
			}
			return new int[] { x, y };
		}
		return null;
	}

	public long getCurTimeMil() {
		return curTimeMil;
	}

	public boolean checkPlayerCanOccupy(ICYBORGPlayer player, int x, int y) {
		int pointRedis = CYBORGPointUtil.pointRedis(WorldPointType.PLAYER);
		Set<Integer> set = CYBORGPointUtil.getOcuPointId(x, y, pointRedis);
		set.removeAll(CYBORGPointUtil.getOcuPointId(player.getX(), player.getY(), pointRedis));

		set.retainAll(occupationPointSet);
		if (!set.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<ICYBORGPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public PBCYBORGGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBCYBORGGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public ICYBORGPlayer getAnchor() {
		return anchor;
	}

	public void setAnchor(ICYBORGPlayer anchor) {
		this.anchor = anchor;
	}

	public List<CYBORGGuildBaseInfo> getBattleCamps() {
		return battleCamps;
	}

	public List<CYBORGNuclearMissileSilo> getNuclearBuildList() {
		return nuclearBuildList;
	}

	private GuildFormationObj guildFormationObj;

	public GuildFormationObj getGuildFormation(String guildId2) {
		if (Objects.isNull(guildFormationObj)) {
			guildFormationObj = new GuildFormationObj();
			guildFormationObj.unSerializ("");
		}

		return guildFormationObj;
	}

}
