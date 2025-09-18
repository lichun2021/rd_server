package com.hawk.game.module.dayazhizhan.battleroom;

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

import com.hawk.game.module.dayazhizhan.battleroom.cfg.*;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.DYZZHighTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.highTower.state.DYZZHighTowerStateProtected;
import com.hawk.game.module.dayazhizhan.playerteam.season.DYZZSeasonService;
import com.hawk.game.protocol.DYZZWar;
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
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AtomicLongMap;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZConst.DYZZState;
import com.hawk.game.module.dayazhizhan.battleroom.DYZZRoomManager.DYZZCAMP;
import com.hawk.game.module.dayazhizhan.battleroom.entity.DYZZMarchEntity;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZExtraParam;
import com.hawk.game.module.dayazhizhan.battleroom.extry.DYZZGamer;
import com.hawk.game.module.dayazhizhan.battleroom.module.DYZZArmyModule;
import com.hawk.game.module.dayazhizhan.battleroom.module.DYZZMarchModule;
import com.hawk.game.module.dayazhizhan.battleroom.module.DYZZWorldModule;
import com.hawk.game.module.dayazhizhan.battleroom.msg.DYZZQuitReason;
import com.hawk.game.module.dayazhizhan.battleroom.order.DYZZOrderCollection;
import com.hawk.game.module.dayazhizhan.battleroom.player.IDYZZPlayer;
import com.hawk.game.module.dayazhizhan.battleroom.player.rogue.DYZZRogueType;
import com.hawk.game.module.dayazhizhan.battleroom.roomstate.IDYZZBattleRoomState;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZAssistanceSingleMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZBuildingMarchMass;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZBuildingMarchMassJoin;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZBuildingMarchSingle;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZCollectFuelMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZMassJoinSingleMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZMassSingleMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.DYZZSpyMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.IDYZZWorldMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZMassJoinMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZMassMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldmarch.submarch.IDYZZReportPushMarch;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZBase;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFameHall;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFuelBankBig;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZFuelBankSmall;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.DYZZPointUtil;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZBuilding;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.IDYZZFuelBank;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.DYZZEnergyWell;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.enertywell.state.DYZZEnergyWellStateProtected;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZInTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.DYZZOutTower;
import com.hawk.game.module.dayazhizhan.battleroom.worldpoint.tower.state.DYZZTowerStateZhanLing;
import com.hawk.game.module.dayazhizhan.playerteam.cfg.DYZZWarCfg;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGameRoomData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.DYZZ.DYZZEnegerWellOpen;
import com.hawk.game.protocol.DYZZ.PBDYZZGameInfoSync;
import com.hawk.game.protocol.DYZZ.PBDYZZGameOver;
import com.hawk.game.protocol.DYZZ.PBDYZZPlayerQuitRoom;
import com.hawk.game.protocol.DYZZ.PBDYZZSecondMapResp;
import com.hawk.game.protocol.DYZZ.PBGuildInfo;
import com.hawk.game.protocol.DYZZ.PBPlayerInfo;
import com.hawk.game.protocol.DYZZWar.PBDYZZGameRoomState;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
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
public class DYZZBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	private DYZZExtraParam extParm;
	private boolean gameOver;
	private DYZZCAMP winCamp;
	private int battleCfgId;
	private long curTimeMil;
	private long totalEnterPower;// 进入副本总战力

	private String nuclearReadGuild = "";
	private String nuclearReadLeader = "";
	/** 游戏内玩家 不包含退出的 */
	private Map<String, IDYZZPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, IDYZZPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, IDYZZWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private DYZZFuBankSmallRefesh fubankSmallrefresh;
	private DYZZFuBankBigRefesh fubankBigrefresh;
	private Map<String, IDYZZWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private List<IDYZZBuilding> buildingList = new CopyOnWriteArrayList<>();
	private IDYZZBattleRoomState state;
	private long createTime;
	private long collectStartTime;
	private long battleStartTime;
	private long hotBattleStartTime;
	private long overTime;

	private DYZZRandPointSeed pointSeed = new DYZZRandPointSeed();

	private DYZZOrderCollection campAorderCollection;
	private DYZZBornPointRing bornPointAList;
	private String campAGuild = "";
	private String campAGuildName = "";
	private String campAGuildTag = "";
	private int campAguildFlag;

	private DYZZOrderCollection campBorderCollection;
	private DYZZBornPointRing bornPointBList;
	private String campBGuild = "";
	private String campBGuildName = "";
	private String campBGuildTag = "";
	private int campBguildFlag;

	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBDYZZGameInfoSync lastSyncpb;

//	private long lastSyncMap;
//	private PBDYZZSecondMapResp lastMappb;

	private Multimap<Class<? extends IDYZZBuilding>, ? super IDYZZBuilding> DYZZBuildingMap = HashMultimap.create();

	/** 地图上占用点 */
	private Set<Integer> occupationPointSet = new HashSet<>();

	private int campAGuildWarCount;
	private int campBGuildWarCount;

	/** 号令点数*/
	public int campAOrder;
	public int campBOrder;

	private Map<String, IDYZZPlayer> anchorMap = new HashMap<>();
	boolean less10 = true; // 双方主基地血量低于10
	private DYZZRogueRefesh rogueRefesh;
	private Set<Integer> noBuffArea = new HashSet<>();

	private Map<Integer, DYZZRoguePoolCfg> rogueMap = new HashMap<>();
	private AtomicLongMap<Integer> rogueOutCnt = AtomicLongMap.create();
	private Set<Integer> exclusions = new HashSet<>();

	public DYZZBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	public void addNobuffArea(int areaId) {
		noBuffArea.add(areaId);
	}

	public boolean isNoBuffArea(int areaId) {
		return noBuffArea.contains(areaId);
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - getCurTimeMil() < 3000;
	}

	public DYZZRoguePoolCfg randomRoguePool(DYZZRogueType type, int param) {
		int key = type.intValue() * 10000000 + param % 1000000;
		if (rogueMap.containsKey(key)) {
			return rogueMap.get(key);
		}
		ConfigIterator<DYZZRoguePoolCfg> pit = HawkConfigManager.getInstance().getConfigIterator(DYZZRoguePoolCfg.class);
		List<DYZZRoguePoolCfg> plist = new ArrayList<>();
		for (DYZZRoguePoolCfg pcfg : pit) {
			if (exclusions.contains(pcfg.getId())) {
				continue;
			}
			if (rogueOutCnt.get(pcfg.getId()) >= pcfg.getMaxRogue()) {
				continue;
			}
			plist.add(pcfg);
		}
		DYZZRoguePoolCfg pcfg = HawkRand.randomWeightObject(plist);
		rogueMap.put(key, pcfg);
		exclusions.addAll(pcfg.getExclusionList());
		rogueOutCnt.incrementAndGet(pcfg.getId());
		return pcfg;
	}

	public void sync(IDYZZPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		player.getPush().syncPlayerEffect(EffType.WAR_CITYDEF_VAL, EffType.TROOP_STRENGTH_NUM, EffType.GUILD_TEAM_ARMY_NUM, EffType.MARCH_TROOP_NUM);

		PBDYZZGameInfoSync.Builder bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_GAME_SYNC, bul));

	}

	public boolean isHotBloodModel() {
		return curTimeMil > hotBattleStartTime;
	}

	public PBDYZZGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 1500 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBDYZZGameInfoSync.Builder bul = PBDYZZGameInfoSync.newBuilder();
		bul.setCreateTime(createTime);
		bul.setCollectStartTime(collectStartTime);
		bul.setBattleStartTime(battleStartTime);
		bul.setHotBattleStartTime(hotBattleStartTime);
		bul.setGameOverTime(overTime);
		if (Objects.nonNull(winCamp)) {
			bul.setWinCamp(winCamp.intValue());
		}
		int baseHPA = 0;
		int perMinA = 0; // 每分增加
		int playerCountA = 0; // 战场中人数

		int baseHPB = 0;
		int perMinB = 0; // 每分增加
		int playerCountB = 0; // 战场中人数

		for (IDYZZWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof DYZZBase) {
				DYZZBase build = (DYZZBase) viewp;
				if (build.getBornCamp() == DYZZCAMP.A) {
					baseHPA = build.getHp();
				} else {
					baseHPB = build.getHp();
				}
			}

			if (viewp instanceof IDYZZFuelBank) {
				IDYZZFuelBank build = (IDYZZFuelBank) viewp;
				if (build.getMarch() != null) {
					if (Objects.equals(campAGuild, build.getGuildId())) {
						perMinA += build.getMarch().getCollectSpeed() * 60;
					} else if (Objects.equals(campBGuild, build.getGuildId())) {
						perMinB += build.getMarch().getCollectSpeed() * 60;
					}
				}
			}
			if (viewp instanceof DYZZEnergyWell) {
				DYZZEnergyWell build = (DYZZEnergyWell) viewp;
				bul.addWellOpen(DYZZEnegerWellOpen.newBuilder().setX(build.getX()).setY(build.getY()).setProtectedEndTime(build.getProtectedEndTime()));
			}
			//这里写法并不好，但是为了不改老代码，先这么写
			if (viewp instanceof DYZZHighTower) {
				DYZZHighTower build = (DYZZHighTower) viewp;
				bul.addWellOpen(DYZZEnegerWellOpen.newBuilder().setX(build.getX()).setY(build.getY()).setProtectedEndTime(build.getProtectedEndTime()));
			}
		}

		List<IDYZZPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (IDYZZPlayer p : all) {
			if (p.getCamp() == DYZZCAMP.A) {
				playerCountA++;
			} else {
				playerCountB++;
			}
			PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
			prc.setCamp(p.getCamp().intValue());
			prc.setName(p.getName());
			prc.setGuildTag(p.getGuildTag());
			prc.setPlayerId(p.getId());
			prc.setIcon(p.getIcon());
			prc.setPfIcon(p.getPfIcon());
			prc.setServerId(p.getServerId());
			prc.setCollectOrder((int) p.getCollectHonor());
			prc.setKillCount(p.getKillCount());
			prc.setMvp(p.getMvp());
			prc.setKda(p.getKda());
			prc.setRewardCount(p.getRewardCount());
			prc.setHurtCount(p.getHurtTankCount());
			prc.addAllRogueselected(p.getRogueCollec().getAllRogueSelected());
			//赛季
			prc.setSeasonScore(p.getSeasonScore());
			prc.setSeasonScoreAdd(p.getSeasonScoreAdd());
			prc.setFirstWinReward(p.getSeasonFirstReward());
			
			bul.addPlayerInfo(prc);
		}

		PBGuildInfo.Builder aInfo = PBGuildInfo.newBuilder().setCamp(DYZZCAMP.A.intValue()).setGuildFlag(campAguildFlag).setGuildName(campAGuildName)
				.setGuildTag(campAGuildTag).setGuildId(campAGuild).setPlayerCount(playerCountA)
				.setGuildOrder(campAOrder).setBaseHP(baseHPA);
		PBGuildInfo.Builder bInfo = PBGuildInfo.newBuilder().setCamp(DYZZCAMP.B.intValue()).setGuildFlag(campBguildFlag).setGuildName(campBGuildName)
				.setGuildTag(campBGuildTag).setGuildId(campBGuild).setPlayerCount(playerCountB)
				.setGuildOrder(campBOrder).setBaseHP(baseHPB);

		bul.addGuildInfo(aInfo);
		bul.addGuildInfo(bInfo);
		int season = DYZZSeasonService.getInstance().getDYZZSeasonTerm();
		DYZZWar.PBDYZZSeasonState state = DYZZSeasonService.getInstance().getDYZZSeasonState();
		if(season >= 0 && state == DYZZWar.PBDYZZSeasonState.DYZZ_SEASON_OPEN){
			bul.setSeason(DYZZSeasonService.getInstance().getDYZZSeasonTerm());
		}

		lastSyncpb = bul.build();
		lastSyncGame = curTimeMil;

		return bul;
	}

	public void getSecondMap(IDYZZPlayer player) {
		PBDYZZSecondMapResp.Builder bul = PBDYZZSecondMapResp.newBuilder();
		for (IDYZZWorldPoint point : getViewPoints()) {
			bul.addPoints(point.toBuilder(player));
		}
		bul.setCreateTime(createTime);
		bul.setCollectStartTime(collectStartTime);
		bul.setBattleStartTime(battleStartTime);
		bul.setHotBattleStartTime(hotBattleStartTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SECOND_MAP_S, bul));

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
			IDYZZPlayer pl = (IDYZZPlayer) ppp;
			int killPow = 0;
			int killCnt = 0;
			int killPowAll = 0;
			int killCntAll = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				killPowAll += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
				killCntAll += army.getDeadCount() + army.getWoundedCount();
				if (cfg.getSoldierType() == SoldierType.TANK_SOLDIER_1) {
					killPow += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
					killCnt += army.getDeadCount() + army.getWoundedCount();
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
			pl.setHurtTankCount(pl.getHurtTankCount() + killCnt);

			pl.setHurtPower(pl.getHurtPower() + killPowAll);
			pl.setHurtCount(pl.getHurtCount() + killCntAll);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			int killPow = 0;
			int killCnt = 0;
			IDYZZPlayer pl = (IDYZZPlayer) ppp;
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

	/**主播请求小地图*/
	public void getAnchorSecondMap(IDYZZPlayer player) {
		PBDYZZSecondMapResp.Builder bul = PBDYZZSecondMapResp.newBuilder();
		for (IDYZZWorldPoint point : getViewPoints()) {
			bul.addPoints(point.toBuilder(player));
		}
		bul.setCreateTime(createTime);
		bul.setCollectStartTime(collectStartTime);
		bul.setBattleStartTime(battleStartTime);
		bul.setHotBattleStartTime(hotBattleStartTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_SECOND_MAP_S, bul));
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		HawkLog.logPrintln("DYZZ GAME CREATE parm: {}", extParm.toString());
		curTimeMil = HawkTime.getMillisecond();

		campAGuild = "campA";
		campAGuildName = "防卫军";
		campAGuildTag = "防卫军";
		campAguildFlag = 0;

		campBGuild = "campB";
		campBGuildName = "兄弟会";
		campBGuildTag = "兄弟会";
		campBguildFlag = 0;

		nextSyncToPlayer = createTime + 5000;
		registerModule(DYZZConst.ModuleType.DYZZWorld, new DYZZWorldModule(this));
		registerModule(DYZZConst.ModuleType.DYZZMarch, new DYZZMarchModule(this));
		registerModule(DYZZConst.ModuleType.DYZZArmy, new DYZZArmyModule(this));

		DYZZBattleCfg cfg = getCfg();
		collectStartTime = createTime + cfg.getPrepairTime() * 1000;
		battleStartTime = collectStartTime + cfg.getCollectTime() * 1000;
		hotBattleStartTime = battleStartTime + cfg.getBattleTime() * 1000;

		bornPointAList = new DYZZBornPointRing(cfg.copyOfbornPointAList());

		bornPointBList = new DYZZBornPointRing(cfg.copyOfbornPointBList());

		campAorderCollection = new DYZZOrderCollection(this, DYZZCAMP.A);
		campBorderCollection = new DYZZOrderCollection(this, DYZZCAMP.B);

		{ // 基地
			DYZZBaseCfg buildcfg = DYZZBase.getCfg();
			int index = 0;
			{
				HawkTuple2<Integer, Integer> rp = buildcfg.getBaseAPos();
				DYZZBase icd = new DYZZBase(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setHp(buildcfg.getInitBlood());
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.A);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
			{
				HawkTuple2<Integer, Integer> rp = buildcfg.getBaseBPos();
				DYZZBase icd = new DYZZBase(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setHp(buildcfg.getInitBlood());
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.B);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{ // 能量井
			DYZZEnergyWellCfg buildcfg = DYZZEnergyWell.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				DYZZEnergyWell icd = new DYZZEnergyWell(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setStateObj(new DYZZEnergyWellStateProtected(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{ //
			DYZZHighTowerCfg buildcfg = DYZZHighTower.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				DYZZHighTower icd = new DYZZHighTower(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setStateObj(new DYZZHighTowerStateProtected(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{ // 外塔
			DYZZOutTowerCfg buildcfg = DYZZOutTower.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointAList()) {
				DYZZOutTower icd = new DYZZOutTower(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.A);
				icd.setOnwerCamp(DYZZCAMP.A);
				icd.setStateObj(new DYZZTowerStateZhanLing(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointBList()) {
				DYZZOutTower icd = new DYZZOutTower(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.B);
				icd.setOnwerCamp(DYZZCAMP.B);
				icd.setStateObj(new DYZZTowerStateZhanLing(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{ // 内塔
			DYZZInTowerCfg buildcfg = DYZZInTower.getCfg();
			int index = 0;
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointAList()) {
				DYZZInTower icd = new DYZZInTower(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.A);
				icd.setOnwerCamp(DYZZCAMP.A);
				icd.setStateObj(new DYZZTowerStateZhanLing(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointBList()) {
				DYZZInTower icd = new DYZZInTower(this);
				icd.setIndex(index);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				icd.setBornCamp(DYZZCAMP.B);
				icd.setOnwerCamp(DYZZCAMP.B);
				icd.setStateObj(new DYZZTowerStateZhanLing(icd));
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				DYZZBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		{ // 资源矿小
			DYZZFuelBankSmallCfg buildcfg = DYZZFuelBankSmall.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				DYZZFuelBankSmall icd = DYZZFuelBankSmall.create(this);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
			}
		}

		{ // 资源矿大
			DYZZFuelBankBigCfg buildcfg = DYZZFuelBankBig.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				DYZZFuelBankBig icd = DYZZFuelBankBig.create(this);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
			}
		}
		{ // 名人堂建筑
			DYZZFameHallCfg buildcfg = DYZZFameHall.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				DYZZFameHall icd = DYZZFameHall.create(this);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setRedis(buildcfg.getRedis());
				viewPoints.put(icd.getPointId(), icd);
			}
		}
		
		fubankSmallrefresh = DYZZFuBankSmallRefesh.create(this);
		fubankBigrefresh = DYZZFuBankBigRefesh.create(this);
		rogueRefesh = DYZZRogueRefesh.create(this);
		DYZZWarCfg dyzzWarCfg = HawkConfigManager.getInstance().getKVInstance(DYZZWarCfg.class);
		int periodTime = dyzzWarCfg.getPerioTime();
		this.addTickable(new HawkPeriodTickable(periodTime) {
			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});

		DungeonRedisLog.log(getId(), "init");
	}

	public void onPlayerLogin(IDYZZPlayer gamer) {
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<IDYZZWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (IDYZZWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				IDYZZWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof IDYZZPassiveAlarmTriggerMarch) {
				((IDYZZPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<IDYZZWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (IDYZZWorldMarch march : pointMs) {
			if (march instanceof IDYZZReportPushMarch) {
				((IDYZZReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

		for (IDYZZBuilding build : getDYZZBuildingList()) {
			build.onPlayerLogin(gamer);
		}
		// 号令同步
		syncOrder(gamer);
		gamer.getRogueCollec().sync();
	}

	public void updateRoomActiveTime() {
		long curTime = HawkTime.getMillisecond();
		if (curTime > this.getCreateTime() && curTime < this.getOverTime()) {
			int termId = DYZZService.getInstance().getDYZZWarTerm();
			DYZZGameRoomData roomData = DYZZRedisData.getInstance().getDYZZGameData(termId, this.getId());
			if (roomData == null || roomData.getState() != PBDYZZGameRoomState.DYZZ_GAME_GAMING) {
				return;
			}
			roomData.setLastActiveTime(curTime);
			DYZZRedisData.getInstance().saveDYZZGameData(termId, roomData);
		}
	}

	public void doMoveCitySuccess(IDYZZPlayer player, int[] targetPoint) {
		// 恢复城墙状态，投递回玩家线程执行
		player.setOnFireEndTime(0);
		player.setCityDefNextRepairTime(0);
		player.setCityDefVal(Integer.MAX_VALUE);
		player.getPush().syncCityDef(false);

		// 重推警报
		cleanCityPointMarch(player);
		for (IDYZZWorldMarch march : worldMarches.values()) {
			if (march instanceof IDYZZReportPushMarch) {
				((IDYZZReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		removeViewPoint(player);
		player.setPos(targetPoint);
		addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();

		// 回复协议
		WorldMoveCityResp.Builder builder = WorldMoveCityResp.newBuilder();
		builder.setResult(true);
		builder.setX(targetPoint[0]);
		builder.setY(targetPoint[1]);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MOVE_CITY_S_VALUE, builder));
	}

	public IDYZZWorldMarch startMarch(IDYZZPlayer player, IDYZZWorldPoint fPoint, IDYZZWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		// 生成行军
		IDYZZWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
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
		List<IDYZZWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
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
	public IDYZZWorldMarch genMarch(IDYZZPlayer player, IDYZZWorldPoint fPoint, IDYZZWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = getCurTimeMil();

		DYZZMarchEntity march = new DYZZMarchEntity();
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
		IDYZZWorldMarch iWorldMarch = null;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			// iWorldMarch = new DYZZAttackPlayerMarch(player); // 当前版本不能打玩家城点
			break;
		case SPY:
			iWorldMarch = new DYZZSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new DYZZAssistanceSingleMarch(player);// 当前版本不能打玩家城点
			break;
		case MASS:
			iWorldMarch = new DYZZMassSingleMarch(player);// 当前版本不能打玩家城点
			break;
		case MASS_JOIN:
			iWorldMarch = new DYZZMassJoinSingleMarch(player);// 当前版本不能打玩家城点
			break;
		case COLLECT_RESOURCE:
			iWorldMarch = new DYZZCollectFuelMarch(player);
			break;
		case DYZZ_TOWER_SINGLE:// = 101; // 单人铁幕装置行军
		case DYZZ_ENERGY_WELL_SINGLE: // = 102; // 单人核弹发射井行军
		case DYZZ_HIGH_TOWER_SINGLE:
			iWorldMarch = new DYZZBuildingMarchSingle(player);
			((DYZZBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case DYZZ_TOWER_MASS:// = 111; // 集结铁幕装置行军
			iWorldMarch = new DYZZBuildingMarchMass(player);
			((DYZZBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.DYZZ_TOWER_MASS);
			((DYZZBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.DYZZ_TOWER_MASS_JOIN);
			break;
		case DYZZ_ENERGY_WELL_MASS:// = 112; // 集结核弹发射井行军
			iWorldMarch = new DYZZBuildingMarchMass(player);
			((DYZZBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.DYZZ_ENERGY_WELL_MASS);
			((DYZZBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.DYZZ_ENERGY_WELL_MASS_JOIN);
			break;
		case DYZZ_HIGH_TOWER_MASS:
			iWorldMarch = new DYZZBuildingMarchMass(player);
			((DYZZBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.DYZZ_HIGH_TOWER_MASS);
			((DYZZBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.DYZZ_HIGH_TOWER_MASS_JOIN);
			break;
		case DYZZ_TOWER_MASS_JOIN:// = 121; // 加入集结铁幕装置行军
		case DYZZ_ENERGY_WELL_MASS_JOIN:// = 122; // 加入集结核弹发射井行军
		case DYZZ_HIGH_TOWER_MASS_JOIN:
			iWorldMarch = new DYZZBuildingMarchMassJoin(player);
			break;

		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!!");
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

		return iWorldMarch;
	}

	public DYZZBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(DYZZBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public IDYZZWorldMarch getPlayerMarch(String playerId, String marchId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId) && Objects.equals(m.getMarchId(), marchId)).findAny().orElse(null);
	}

	public List<IDYZZWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<IDYZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<IDYZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	@SuppressWarnings("unchecked")
	public <T extends IDYZZBuilding> List<T> getDYZZBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) DYZZBuildingMap.get(type));
	}

	public List<IDYZZBuilding> getDYZZBuildingList() {
		return buildingList;
	}

	public List<IDYZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IDYZZWorldMarch> result = new ArrayList<>();
		for (IDYZZWorldMarch ma : worldMarches.values()) {
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

	public IDYZZWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<IDYZZWorldMarch> getGuildWarMarch(String guildId) {
		List<IDYZZWorldMarch> result = new ArrayList<>();
		for (IDYZZWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof DYZZBuildingMarchMass) {
					IDYZZBuilding point = (IDYZZBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getDYZZGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof DYZZBuildingMarchSingle) {
					IDYZZBuilding point = (IDYZZBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getDYZZGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					} else {
						if (Objects.equals(point.getGuildId(), guildId)) {// 如果已被已方控制
							continue;
						}
					}
				}

				result.add(march);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return result;
	}

	public List<IDYZZWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<IDYZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<IDYZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<IDYZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IDYZZWorldMarch> result = new LinkedList<>();
		for (IDYZZWorldMarch ma : worldMarches.values()) {
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
	public Optional<IDYZZWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(viewPoints.get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<IDYZZWorldPoint> getWorldPoint(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getWorldPoint(pos[0], pos[1]);
	}

	@Override
	public boolean onTick() {
		if (Objects.isNull(state)) {
			return false;
		}

		if (less10) {
			List<DYZZBase> bases = getDYZZBuildingByClass(DYZZBase.class);
			Optional<DYZZBase> bop = bases.stream().filter(base -> 1D * base.getHp() / DYZZBase.getCfg().getInitBlood() > 0.1).findAny();
			if (!bop.isPresent()) {
				less10 = false;
				// 广播战场
				ChatParames paramesBroad = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.DYZZ_344)
						.build();
				addWorldBroadcastMsg(paramesBroad);
			}
		}

		curTimeMil = HawkTime.getMillisecond();
		try {
			state.onTick();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 1500;
				buildSyncPB();
				for (IDYZZPlayer p : playerMap.values()) {
					sync(p);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		fubankSmallrefresh.onTick();
		fubankBigrefresh.onTick();
		rogueRefesh.onTick();

		campAorderCollection.onTick();
		campBorderCollection.onTick();
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(IDYZZPlayer player) {
		state.enterWorld(player);
	}

	/**
	 * 副本中玩家更新点
	 * 
	 * @param point
	 */
	public void worldPointUpdate(IDYZZWorldPoint point) {
		for (IDYZZPlayer pla : getPlayerList(DYZZState.GAMEING)) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(pla));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}

		for (IDYZZPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(anchor));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(protocol);
		}
	}

	public void joinRoom(IDYZZPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || isGameOver()) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}
		
		DYZZGamer gamer = extParm.getGamer(player.getId());
		player.setCamp(gamer.getCamp());

		int[] bornP = null;
		if (player.getCamp() == DYZZCAMP.A) {
			bornP = bornPointAList.nextPoint();
			player.setGuildId(campAGuild);
			player.setGuildTag(campAGuildTag);
			player.setGuildFlag(campAguildFlag);
			player.setGuildName(campAGuildName);
		} else {
			bornP = bornPointBList.nextPoint();
			player.setGuildId(campBGuild);
			player.setGuildTag(campBGuildTag);
			player.setGuildFlag(campBguildFlag);
			player.setGuildName(campBGuildName);
		}

		while (Objects.isNull(bornP)) {
			bornP = randomFreePoint(randomPoint(), WorldPointType.PLAYER, player.getRedis());
		}

		player.setPos(bornP);
		player.setRewardCount(gamer.getRewardCount());
		player.setSeasonScore(gamer.getSeasonScore());
		player.setWinCount(gamer.getWinCount());
		player.init();
		DYZZRoomManager.getInstance().cache(player);
		player.setDYZZState(DYZZState.GAMEING);
		player.getPush().pushJoinGame();
		this.syncOrder(player);
		this.addViewPoint(player);
		player.getRogueCollec().sync();
		playerMap.put(player.getId(), player);
		totalEnterPower += player.getPower();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ENTER_GAME_SUCCESS));

		NoticeCfgId wellcome = player.getCamp() == DYZZCAMP.A ? NoticeCfgId.DYZZ_336 : NoticeCfgId.DYZZ_338;
		ChatParames parames = ChatParames.newBuilder().setChatType(ChatType.CHAT_FUBEN_SPECIAL_BROADCAST).setKey(wellcome)
				.addParms(player.getCamp().intValue())
				.addParms(player.getGuildTag())
				.addParms(player.getName())
				.build();
		Set<Player> tosend = new HashSet<>(1);
		tosend.add(player);
		ChatService.getInstance().sendChatMsg(Arrays.asList(parames.toPBMsg()), tosend);
		// addWorldBroadcastMsg(parames);

		DungeonRedisLog.log(player.getId(), "roomId {}", getId());
		DungeonRedisLog.log(getId(), "player{}", player.getId());
	}

	public int[] randomPoint() {
		IDYZZBuilding build = HawkRand.randomObject(buildingList);
		return GameUtil.splitXAndY(build.getPointId());
	}

	public void quitWorld(IDYZZPlayer quitPlayer, DYZZQuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBDYZZGameOver.Builder builder = PBDYZZGameOver.newBuilder();
			if (reason == DYZZQuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_GAME_OVER, builder));
		}

		if (quitPlayer.isAnchor()) { // 主播退了不用理他
			anchorMap.remove(quitPlayer.getId());
			quitPlayer.quitGame();
			return;
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		boolean inWorld = removeViewPoint(quitPlayer);
		if (inWorld) {
			// 删除行军
			cleanCityPointMarch(quitPlayer);

			for (IDYZZPlayer gamer : playerMap.values()) {
				PBDYZZPlayerQuitRoom.Builder bul = PBDYZZPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_PLAYER_QUIT, bul));
			}

			quitPlayer.getData().getQueueEntities().clear();
		}

		// if (reason == DYZZQuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_DYZZ).setKey(NoticeCfgId.DYZZ_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();
	}

	public void cleanCityPointMarch(IDYZZPlayer quitPlayer) {
		try {
			List<IDYZZWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (IDYZZWorldMarch march : quiterMarches) {
				if (march instanceof IDYZZMassMarch) {
					march.getMassJoinMarchs(true).forEach(IDYZZWorldMarch::onMarchCallback);
				}
				if (march instanceof IDYZZMassJoinMarch) {
					Optional<IDYZZMassMarch> massMarch = ((IDYZZMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						IDYZZMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(IDYZZWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<IDYZZWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (IDYZZWorldMarch march : pms) {
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

	public List<IDYZZPlayer> getPlayerList(DYZZState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<IDYZZPlayer> getPlayerList(DYZZState st1, DYZZState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<IDYZZPlayer> getPlayerList(DYZZState st1, DYZZState st2, DYZZState st3) {
		List<IDYZZPlayer> result = new ArrayList<>();
		for (IDYZZPlayer player : playerMap.values()) {
			DYZZState state = player.getDYZZState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public IDYZZPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public List<IDYZZPlayer> getCampPlayers(DYZZCAMP camp) {
		List<IDYZZPlayer> list = new ArrayList<>();
		for (IDYZZPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void setPlayerList(List<IDYZZPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	/** 添加世界点 */
	public void addViewPoint(IDYZZWorldPoint... vp) {
		List<IDYZZWorldPoint> list = new ArrayList<>();
		for (IDYZZWorldPoint point : vp) {
			if (point != null) {
				list.add(point);
			}
		}

		for (IDYZZWorldPoint point : list) {
			viewPoints.put(point.getPointId(), point);
		}

		resetOccupationPoint();

		for (IDYZZPlayer player : playerMap.values()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IDYZZWorldPoint point : list) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(pp);
		}
		for (IDYZZPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IDYZZWorldPoint point : list) {
				builder.addPoints(point.toBuilder(anchor));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(pp);
		}
	}

	public boolean removeViewPoint(IDYZZWorldPoint vp) {
		boolean result = Objects.nonNull(viewPoints.remove(vp.getPointId()));
		for (IDYZZPlayer gamer : playerMap.values()) {
			// 删除点
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(gamer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		for (IDYZZPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(anchor));
			anchor.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		resetOccupationPoint();
		return result;
	}

	/** 计算已占用点 */
	private void resetOccupationPoint() {
		Set<Integer> set = new HashSet<>();
		for (IDYZZWorldPoint viewp : viewPoints.values()) {
			viewp.fillWithOcuPointId(set);
		}
		occupationPointSet = set;
	}

	public List<IDYZZWorldPoint> getViewPoints() {
		return new ArrayList<>(viewPoints.values());
	}

	public void setViewPoints(List<IDYZZWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<IDYZZWorldMarch> getWorldMarchList() {
		ArrayList<IDYZZWorldMarch> result = new ArrayList<>(worldMarches.values());
		Collections.sort(result, Comparator.comparingLong(IDYZZWorldMarch::getStartTime));
		return result;
	}

	public void removeMarch(IDYZZWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(IDYZZWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<IDYZZWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public IDYZZBattleRoomState getState() {
		return state;
	}

	public void setState(IDYZZBattleRoomState state) {
		this.state = state;
		this.state.init();
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

	public long getCollectStartTime() {
		return collectStartTime;
	}

	public long getBattleStartTime() {
		return battleStartTime;
	}

	public long getHotBattleStartTime() {
		return hotBattleStartTime;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public DYZZExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(DYZZExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(IDYZZPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setAllianceId(player.getDYZZGuildId()).setType(type.getNumber()).setChatMsg(chatMsg)
				.setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo, null);

	}

	public void addWorldBroadcastMsg(ChatParames parames, DYZZCAMP camp) {
		broadcastChatMsg(parames.toPBMsg(), camp);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		broadcastChatMsg(parames.toPBMsg(), null);
	}

	private void broadcastChatMsg(ChatMsg pbMsg, DYZZCAMP camp) {
		IDYZZPlayer sender = getPlayer(pbMsg.toBuilder().getPlayerId());
		if (Objects.nonNull(sender)) {
			pbMsg = pbMsg.toBuilder().setAllianceId(sender.getDYZZGuildId()).build();
		}
		Set<IDYZZPlayer> tosend = new HashSet<>(getPlayerList(DYZZState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg) || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE || Objects.nonNull(camp);
		if (isGuildMsg) {
			String guildId = Objects.nonNull(camp) ? getCampGuild(camp) : pbMsg.getAllianceId();
			tosend = tosend.stream().filter(p -> Objects.equals(p.getDYZZGuildId(), guildId)).collect(Collectors.toSet());
		} else {
			for (IDYZZPlayer anchor : getAnchors()) {
				tosend.add(anchor);
			}
		}
		ChatService.getInstance().sendChatMsg(Arrays.asList(pbMsg), tosend);
	}

	public String getId() {
		return getXid().getUUID();
	}

	public int[] randomFreePoint(int[] popBornPointA, WorldPointType pointType, int redis) {
		DYZZBattleCfg bcfg = getCfg();
		int x;
		int y;
		for (int i = 0; i < pointSeed.size(); i++) {
			int[] pp = pointSeed.nextPoint();
			x = popBornPointA[0] + pp[0];
			y = popBornPointA[1] + pp[1];

			if (DYZZMapBlock.getInstance().isStopPoint(GameUtil.combineXAndY(x, y))) {
				continue;
			}
			int pointRedis = redis;
			if ((x + y) % 2 == pointRedis % 2) {
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

			Set<Integer> set = DYZZPointUtil.getOcuPointId(x, y, pointRedis);
			if (!checkAllFreePoint(set)) {
				continue;
			}

			return new int[] { x, y };
		}
		return null;
	}

	private boolean checkAllFreePoint(Set<Integer> set) {
		for (Integer pid : set) {
			if (DYZZMapBlock.getInstance().isStopPoint(pid)) {
				return false;
			}
		}
		set.retainAll(occupationPointSet);
		if (!set.isEmpty()) {
			return false;
		}
		return true;
	}

	public long getCurTimeMil() {
		return curTimeMil;
	}

	public boolean checkPlayerCanOccupy(IDYZZPlayer player, int x, int y) {
		if (DYZZMapBlock.getInstance().isStopPoint(GameUtil.combineXAndY(x, y))) {
			return false;
		}
		int pointRedis = player.getRedis();
		Set<Integer> set = DYZZPointUtil.getOcuPointId(x, y, pointRedis);
		set.removeAll(DYZZPointUtil.getOcuPointId(player.getX(), player.getY(), pointRedis));

		if (!checkAllFreePoint(set)) {
			return false;
		}

		return true;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<IDYZZPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public PBDYZZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBDYZZGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public String getNuclearReadGuild() {
		return nuclearReadGuild;
	}

	public void setNuclearReadyGuild(String nuclearReadGuild) {
		this.nuclearReadGuild = nuclearReadGuild;
	}

	public String getNuclearReadLeader() {
		return nuclearReadLeader;
	}

	public void setNuclearReadLeader(String nuclearReadLeader) {
		this.nuclearReadLeader = nuclearReadLeader;
	}

	public int getCampAGuildWarCount() {
		return campAGuildWarCount;
	}

	public void setCampAGuildWarCount(int campAGuildWarCount) {
		this.campAGuildWarCount = campAGuildWarCount;
	}

	public int getCampBGuildWarCount() {
		return campBGuildWarCount;
	}

	public void setCampBGuildWarCount(int campBGuildWarCount) {
		this.campBGuildWarCount = campBGuildWarCount;
	}

	public List<IDYZZPlayer> getAnchors() {
		if (anchorMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<IDYZZPlayer> result = new ArrayList<>(anchorMap.size());
		for (IDYZZPlayer anchor : anchorMap.values()) {
			if (Objects.nonNull(anchor) && anchor.isActiveOnline()) {
				result.add(anchor);
			}
		}
		return result;
	}

	public boolean isAnchor(IDYZZPlayer player) {
		return anchorMap.containsKey(player.getId());
	}

	public long getTotalEnterPower() {
		return totalEnterPower;
	}

	public DYZZOrderCollection getDYZZOrderCollection(DYZZCAMP camp) {
		if (DYZZCAMP.A == camp) {
			return this.campAorderCollection;
		}
		if (DYZZCAMP.B == camp) {
			return this.campBorderCollection;
		}
		return null;
	}

	public int getOrderEffect(DYZZCAMP camp, EffType type) {
		DYZZOrderCollection collection = this.getDYZZOrderCollection(camp);
		return collection.getEffectVal(type);
	}

	public void syncOrder(IDYZZPlayer player) {
		DYZZOrderCollection orderCollection = this.getDYZZOrderCollection(player.getCamp());
		if (orderCollection != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.DYZZ_ORDER_SYNC_S_VALUE,
					orderCollection.genPBDYZZOrderSyncRespBuilder()));
		}
	}

	public int getCampOrder(DYZZCAMP camp) {
		switch (camp) {
		case A:
			return campAOrder;
		case B:
			return campBOrder;
		default:
			break;
		}
		return 0;
	}

	public String getCampGuild(DYZZCAMP camp) {
		switch (camp) {
		case A:
			return campAGuild;
		case B:
			return campBGuild;
		default:
			break;
		}

		return "";
	}

	public String getCampGuildTag(DYZZCAMP camp) {
		switch (camp) {
		case A:
			return campAGuildTag;
		case B:
			return campBGuildTag;
		default:
			break;
		}

		return "";
	}

	public int getCampGuildFlag(DYZZCAMP camp) {
		switch (camp) {
		case A:
			return campAguildFlag;
		case B:
			return campBguildFlag;
		default:
			break;
		}

		return 0;
	}

	public DYZZCAMP getGuildCamp(String guildId) {
		if (Objects.equals(guildId, campAGuild)) {
			return DYZZCAMP.A;
		} else {
			return DYZZCAMP.B;
		}
	}

	public String getCampAGuild() {
		return campAGuild;
	}

	public void setCampAGuild(String campAGuild) {
		this.campAGuild = campAGuild;
	}

	public String getCampAGuildName() {
		return campAGuildName;
	}

	public void setCampAGuildName(String campAGuildName) {
		this.campAGuildName = campAGuildName;
	}

	public String getCampAGuildTag() {
		return campAGuildTag;
	}

	public void setCampAGuildTag(String campAGuildTag) {
		this.campAGuildTag = campAGuildTag;
	}

	public int getCampAguildFlag() {
		return campAguildFlag;
	}

	public void setCampAguildFlag(int campAguildFlag) {
		this.campAguildFlag = campAguildFlag;
	}

	public String getCampBGuild() {
		return campBGuild;
	}

	public void setCampBGuild(String campBGuild) {
		this.campBGuild = campBGuild;
	}

	public String getCampBGuildName() {
		return campBGuildName;
	}

	public void setCampBGuildName(String campBGuildName) {
		this.campBGuildName = campBGuildName;
	}

	public String getCampBGuildTag() {
		return campBGuildTag;
	}

	public void setCampBGuildTag(String campBGuildTag) {
		this.campBGuildTag = campBGuildTag;
	}

	public int getCampBguildFlag() {
		return campBguildFlag;
	}

	public void setCampBguildFlag(int campBguildFlag) {
		this.campBguildFlag = campBguildFlag;
	}

	public DYZZFuBankSmallRefesh getFubankSmallrefresh() {
		return fubankSmallrefresh;
	}

	public DYZZFuBankBigRefesh getFubankBigrefresh() {
		return fubankBigrefresh;
	}

	public DYZZCAMP getWinCamp() {
		return winCamp;
	}

	public void setWinCamp(DYZZCAMP winCamp) {
		this.winCamp = winCamp;
	}

	public int getInTowerAtkAdd(String guildid){
		if(guildid == null){
			return 0;
		}
		int atk = 0;
		for(DYZZHighTower highTower : getDYZZBuildingByClass(DYZZHighTower.class)){
			atk += highTower.getAtk(guildid);
		}
		return atk;
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
