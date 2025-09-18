package com.hawk.game.module.lianmenxhjz.battleroom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZConst.XHJZState;
import com.hawk.game.module.lianmenxhjz.battleroom.XHJZRoomManager.XHJZ_CAMP;
import com.hawk.game.module.lianmenxhjz.battleroom.cfg.XHJZBattleCfg;
import com.hawk.game.module.lianmenxhjz.battleroom.entity.XHJZMarchEntity;
import com.hawk.game.module.lianmenxhjz.battleroom.msg.XHJZQuitReason;
import com.hawk.game.module.lianmenxhjz.battleroom.player.IXHJZPlayer;
import com.hawk.game.module.lianmenxhjz.battleroom.player.module.guildformation.XHJZGuildFormationObj;
import com.hawk.game.module.lianmenxhjz.battleroom.roomstate.IXHJZBattleRoomState;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.IXHJZWorldMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.XHJZBuildingMarchMass;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.XHJZBuildingMarchMassJoin;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.XHJZBuildingMarchSingle;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZMassJoinMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZMassMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldmarch.submarch.IXHJZReportPushMarch;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.IXHJZBuilding;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildState;
import com.hawk.game.module.lianmenxhjz.battleroom.worldpoint.XHJZBuildType;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.Position;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.XHJZ.PBXHJZGameInfoSync;
import com.hawk.game.protocol.XHJZ.PBXHJZGameOver;
import com.hawk.game.protocol.XHJZ.PBXHJZGuildInfo;
import com.hawk.game.protocol.XHJZ.PBXHJZPlayerInfo;
import com.hawk.game.protocol.XHJZ.PBXHJZPlayerQuitRoom;
import com.hawk.game.protocol.XHJZ.PBXHJZSecondMapResp;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumConst.RoomState;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.service.xhjzWar.XHJZWarPlayerData;
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
public class XHJZBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	private XHJZExtraParam extParm;
	private volatile boolean gameOver;
	private int overState;
	private int battleCfgId;
	private long curTimeMil;
	private long totalEnterPower;// 进入副本总战力

	private String nuclearReadGuild = "";
	private String nuclearReadLeader = "";
	// 超时空传送器
	private String chronoReadGuild = "";
	private String chronoReadLeader = "";
	/** 游戏内玩家 不包含退出的 */
	private Map<String, IXHJZPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, IXHJZPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	// private Map<Integer, IXHJZWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private XHJZWorldPointService worldPointService;
	private Map<String, IXHJZWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private List<IXHJZBuilding> buildingList = new CopyOnWriteArrayList<>();
	private IXHJZBattleRoomState state;
	private long createTime;
	private long startTime;
	private long overTime;

	private XHJZRandPointSeed pointSeed = new XHJZRandPointSeed();
	private XHJZGuildBaseInfo baseInfoA;
	private XHJZGuildBaseInfo baseInfoB;
	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBXHJZGameInfoSync lastSyncpb = PBXHJZGameInfoSync.getDefaultInstance();
	// private long lastSyncMap;
	// private PBXHJZSecondMapResp lastMappb;

	private long nextLoghonor;

	private Multimap<Class<? extends IXHJZBuilding>, ? super IXHJZBuilding> tblyBuildingMap = HashMultimap.create();

	private int campAGuildWarCount;
	private int campBGuildWarCount;

	public int campANuclearSendCount;
	public int campBNuclearSendCount;

	/**A击杀机甲数*/
	public int campANianKillCount;
	public int campBNianKillCount;
	/**首站积分*/
	public int firstControlHonorA;
	public int firstControlHonorB;

	/** 号令点数*/
	public int campAOrder;
	public int campBOrder;

	public String first5000Honor;// 首无5000
	public String firstKillNian; // 首杀nian
	public String firstControlHeXin; // 首控制核心

	private Map<String, IXHJZPlayer> anchorMap = new HashMap<>();
	private String csServerId = "";
	private Set<String> csPlayerids = new HashSet<>();

	private Map<String, XHJZGuildFormationObj> guildFormationObjmap = new HashMap<>();

	public XHJZBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		XHJZProtocol pro = (XHJZProtocol) protocol;
		IXHJZPlayer player = pro.getPlayer();
		player.onProtocol(pro.getSource());

		return true;
	}

	public XHJZGuildFormationObj getGuildFormation(String guildId) { // TODO 包装成自己的
		if (guildFormationObjmap.containsKey(guildId)) {
			XHJZGuildFormationObj result = guildFormationObjmap.get(guildId);
			return result;
		}
		XHJZGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new XHJZGuildFormationObj());
		obj.setParent(this);
		try {
			String serializ = RedisProxy.getInstance().getCsGuildFormation(guildId).serializ();
			obj.unSerializ(serializ);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		guildFormationObjmap.put(guildId, obj);
		return obj;
	}

	public void broadcastCrossProtocol(HawkProtocol protocol) {
		CrossProxy.getInstance().broadcastProtocolV2(csServerId, csPlayerids, protocol);
	}

	/** 跨服联盟id */
	public String csGuildId() {
		if (baseInfoA.getServerId().equals(csServerId)) {
			return baseInfoA.getGuildId();
		}
		return baseInfoB.getGuildId();
	}

	public int getCsGuildWarCount() {
		if (baseInfoA.getServerId().equals(csServerId)) {
			return campAGuildWarCount;
		}
		return campBGuildWarCount;
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - curTimeMil < 2000;
	}

	public void sync(IXHJZPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		PBXHJZGameInfoSync.Builder bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_GAME_SYNC, bul));

	}

	public boolean isHotBloodModel() {
		return false;
	}

	public PBXHJZGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBXHJZGameInfoSync.Builder bul = PBXHJZGameInfoSync.newBuilder();
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		PBXHJZGuildInfo.Builder aInfo = PBXHJZGuildInfo.newBuilder().setServerId(baseInfoA.getServerId()).setCamp(XHJZ_CAMP.A.intValue()).setGuildFlag(baseInfoA.getGuildFlag())
				.setGuildName(baseInfoA.getGuildName()).addAllLeaders(baseInfoA.getCommonder().stream().map(p -> p.name).collect(Collectors.toList()))
				.setGuildTag(baseInfoA.getGuildTag()).setGuildId(baseInfoA.getGuildId()).setXhjzGasoline((int) baseInfoA.getGasoline()).setTeamName(baseInfoA.getTeamName());
		PBXHJZGuildInfo.Builder bInfo = PBXHJZGuildInfo.newBuilder().setServerId(baseInfoB.getServerId()).setCamp(XHJZ_CAMP.B.intValue()).setGuildFlag(baseInfoB.getGuildFlag())
				.setGuildName(baseInfoB.getGuildName()).addAllLeaders(baseInfoB.getCommonder().stream().map(p -> p.name).collect(Collectors.toList()))
				.setGuildTag(baseInfoB.getGuildTag()).setGuildId(baseInfoB.getGuildId()).setXhjzGasoline((int) baseInfoB.getGasoline()).setTeamName(baseInfoB.getTeamName());

		int honorA = 0; // 当前积分
		double perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		long centerControlA = 0; // XHJZ_HEADQUARTERS 核心控制时间
		int buildControlHonorA = 0;
		int killHonorA = 0;
		int collectHonorA = 0;
		double playerFuelAddA = getCfg().getFuelRecoverySpeed();
		double alianceFuelAddA = getCfg().getFuelRecoverySpeedTeam();
		int coolDownReducePercentageA = 0;
		int occupyMarchFuelA = 0;
		int allianceScoreA = 0;
		Map<EffType, Integer> battleEffValA = new HashMap<>();
		Table<XHJZBuildType, EffType, Integer> specialEffectTableA = HashBasedTable.create();

		int honorB = 0; // 当前积分
		double perMinB = 0; // 每分增加
		int buildCountB = 0; // 占领建筑
		int playerCountB = 0; // 战场中人数
		long centerControlB = 0; // XHJZ_HEADQUARTERS 核心控制时间
		int buildControlHonorB = 0;
		int killHonorB = 0;
		int collectHonorB = 0;
		double playerFuelAddB = getCfg().getFuelRecoverySpeed();
		int monsterCount = 0;
		double alianceFuelAddB = getCfg().getFuelRecoverySpeedTeam();
		int coolDownReducePercentageB = 0;
		int occupyMarchFuelB = 0;
		int allianceScoreB = 0;
		Map<EffType, Integer> battleEffValB = new HashMap<>();
		Table<XHJZBuildType, EffType, Integer> specialEffectTableB = HashBasedTable.create();

		for (IXHJZWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof IXHJZBuilding) {
				IXHJZBuilding build = (IXHJZBuilding) viewp;
				honorA += build.getBuildingHonor(baseInfoA.getGuildId()).getGuildHonor();
				honorB += build.getBuildingHonor(baseInfoB.getGuildId()).getGuildHonor();
				buildControlHonorA += build.getBuildingHonor(baseInfoA.getGuildId()).getGuildHonor();
				buildControlHonorB += build.getBuildingHonor(baseInfoB.getGuildId()).getGuildHonor();
				if (build.getState() == XHJZBuildState.ZHAN_LING) {
					if (Objects.equals(baseInfoA.getGuildId(), build.getGuildId())) {
						playerFuelAddA += build.getBuildTypeCfg().getPlayerFuelAdd();
						alianceFuelAddA += build.getBuildTypeCfg().getAllianceFuelAdd();
						coolDownReducePercentageA += build.getBuildTypeCfg().getCoolDownReducePercentage();
						honorA += build.getBuildTypeCfg().getAllianceScore();
						allianceScoreA += build.getBuildTypeCfg().getAllianceScore();
						perMinA += build.getAllianceScoreAdd();
						buildCountA++;
						occupyMarchFuelA += build.getBuildTypeCfg().getOccupyMarchFuel();
						for (Entry<EffType, Integer> hw2 : build.getBuildTypeCfg().getControleBuffList().entrySet()) {
							battleEffValA.merge(hw2.getKey(), hw2.getValue(), (v1, v2) -> v1 + v2);
						}
						for (HawkTuple3<XHJZBuildType, EffType, Integer> tv : build.getBuildTypeCfg().getSpecialEffectTable()) {
							Integer v = specialEffectTableA.get(tv.first, tv.second);
							int value = Objects.isNull(v) ? tv.third : v + tv.third;
							specialEffectTableA.put(tv.first, tv.second, value);
						}
					} else if (Objects.equals(baseInfoB.getGuildId(), build.getGuildId())) {
						playerFuelAddB += build.getBuildTypeCfg().getPlayerFuelAdd();
						alianceFuelAddB += build.getBuildTypeCfg().getAllianceFuelAdd();
						coolDownReducePercentageB += build.getBuildTypeCfg().getCoolDownReducePercentage();
						honorB += build.getBuildTypeCfg().getAllianceScore();
						allianceScoreB += build.getBuildTypeCfg().getAllianceScore();
						perMinB += build.getAllianceScoreAdd();
						buildCountB++;
						occupyMarchFuelB += build.getBuildTypeCfg().getOccupyMarchFuel();
						for (Entry<EffType, Integer> hw2 : build.getBuildTypeCfg().getControleBuffList().entrySet()) {
							battleEffValB.merge(hw2.getKey(), hw2.getValue(), (v1, v2) -> v1 + v2);
						}
						for (HawkTuple3<XHJZBuildType, EffType, Integer> tv : build.getBuildTypeCfg().getSpecialEffectTable()) {
							Integer v = specialEffectTableB.get(tv.first, tv.second);
							int value = Objects.isNull(v) ? tv.third : v + tv.third;
							specialEffectTableB.put(tv.first, tv.second, value);
						}
					}
				}
			}
		}

		List<IXHJZPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (IXHJZPlayer p : all) {
			if (p.getCamp() == XHJZ_CAMP.A) {
				honorA += p.getGuildHonor();
				killHonorA += p.getKillHonor();
				collectHonorA += p.getCollectGuildHonor();
				playerCountA++;
			} else {
				honorB += p.getGuildHonor();
				killHonorB += p.getKillHonor();
				collectHonorB += p.getCollectGuildHonor();
				playerCountB++;
			}
			PBXHJZPlayerInfo.Builder prc = PBXHJZPlayerInfo.newBuilder();
			prc.setCamp(p.getCamp().intValue());
			prc.setName(p.getName());
			prc.setHonor(p.getHonor());
			prc.setGuildTag(p.getGuildTag());
			prc.setGuildHonor(p.getGuildHonor());
			prc.setPlayerId(p.getId());
			prc.setKillMonster(p.getKillMonster());
			prc.setIcon(p.getIcon());
			prc.setPfIcon(p.getPfIcon());
			prc.setXhjzGasoline((int) p.getGasoline());
			prc.setXhjzDistributeGasoline(p.getXhjzDistributeGasoline());
			prc.setIsMidwayQuit(p.getQuitReason() == XHJZQuitReason.LEAVE);
			bul.addPlayerInfo(prc);
		}

		baseInfoA.setPlayerFuelAdd(playerFuelAddA);
		baseInfoA.setAllianceFuelAdd(alianceFuelAddA);
		baseInfoA.setCoolDownReducePercentage(coolDownReducePercentageA);
		baseInfoA.battleEffVal = ImmutableMap.copyOf(battleEffValA);
		baseInfoA.setOccupyMarchFuel(occupyMarchFuelA);
		baseInfoA.specialEffectTable = specialEffectTableA;

		baseInfoB.setPlayerFuelAdd(playerFuelAddB);
		baseInfoB.setAllianceFuelAdd(alianceFuelAddB);
		baseInfoB.setCoolDownReducePercentage(coolDownReducePercentageB);
		baseInfoB.battleEffVal = ImmutableMap.copyOf(battleEffValB);
		baseInfoB.setOccupyMarchFuel(occupyMarchFuelB);
		baseInfoB.specialEffectTable = specialEffectTableB;
		
		aInfo.setHonor(honorA)
				.setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setCenterControl(centerControlA).setBuildControlHonor(buildControlHonorA)
				.setKillHonor(killHonorA).setCollectHonor(collectHonorA)
				.setNuclearCount(campANuclearSendCount)
				.setNianKillCnt(campANianKillCount)
				.setGuildOrder(campAOrder)
				.setPerMin((int) Math.ceil(perMinA * 60))
				.setXhjzGasolinePerSecond((int) Math.ceil(alianceFuelAddA * 60))
				.setXhjzPlayerGasolinePerSecond((int) Math.ceil(playerFuelAddA * 60))
				.setMarchFuelAdd(occupyMarchFuelA)
				.setAllianceScore(allianceScoreA);
		bInfo.setHonor(honorB)
				.setBuildCount(buildCountB).setPlayerCount(playerCountB)
				.setCenterControl(centerControlB).setBuildControlHonor(buildControlHonorB)
				.setKillHonor(killHonorB).setCollectHonor(collectHonorB)
				.setNuclearCount(campBNuclearSendCount)
				.setNianKillCnt(campBNianKillCount)
				.setGuildOrder(campBOrder)
				.setPerMin((int) Math.ceil(perMinB * 60))
				.setXhjzGasolinePerSecond((int) Math.round(alianceFuelAddB * 60))
				.setXhjzPlayerGasolinePerSecond((int) Math.round(playerFuelAddB * 60))
				.setMarchFuelAdd(occupyMarchFuelB)
				.setAllianceScore(allianceScoreB);

		bul.setMonsterCount(monsterCount);
		bul.addGuildInfo(aInfo);
		bul.addGuildInfo(bInfo);

		long campAScore = aInfo.getHonor();
		long campBScore = bInfo.getHonor();
		XHJZ_CAMP winCamp = campAScore > campBScore ? XHJZ_CAMP.A : XHJZ_CAMP.B;
		bul.setWinCamp(winCamp.intValue());

		lastSyncpb = bul.build();
		lastSyncGame = curTimeMil;

		if (StringUtils.isEmpty(first5000Honor)) {
			if (honorA >= 5000) {
				first5000Honor = baseInfoA.getGuildId();
			} else if (honorB >= 5000) {
				first5000Honor = baseInfoB.getGuildId();
			}
		}

		return bul;
	}

	public Collection<IXHJZWorldPoint> getViewPoints() {
		return worldPointService.getViewPoints().values();
	}

	public void getSecondMap(IXHJZPlayer player) {
		PBXHJZSecondMapResp.Builder bul = PBXHJZSecondMapResp.newBuilder();
		for (IXHJZWorldPoint point : getViewPoints()) {

			bul.addPoints(point.toBuilder(player));
		}
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_SECOND_MAP_S, bul));
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
			IXHJZPlayer pl = (IXHJZPlayer) ppp;
			int killPow = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				if (cfg.getSoldierType() == SoldierType.TANK_SOLDIER_1) {
					killPow += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			int killPow = 0;
			IXHJZPlayer pl = (IXHJZPlayer) ppp;
			{
				Map<Integer, Integer> killMap = battleKillMap.get(pl.getId());
				if (killMap != null) {
					for (Entry<Integer, Integer> ent : killMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
					}
				}
			}
			{
				Map<Integer, Integer> hurtMap = battleHurtMap.get(pl.getId());
				if (hurtMap != null) {
					for (Entry<Integer, Integer> ent : hurtMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
					}
				}
			}
			pl.setKillPower(pl.getKillPower() + killPow);
		}
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		baseInfoA = new XHJZGuildBaseInfo();
		baseInfoA.setCamp(XHJZ_CAMP.A);
		baseInfoA.setGuildId(extParm.getCampAGuild());
		baseInfoA.setGuildName(extParm.getCampAGuildName());
		baseInfoA.setGuildTag(extParm.getCampAGuildTag());
		baseInfoA.setServerId(extParm.getCampAServerId());
		baseInfoA.setGuildFlag(extParm.getCampAguildFlag());
		baseInfoA.setCommonder(extParm.getCampACommonder());
		baseInfoA.setTeamName(extParm.getTeamAName());
		baseInfoA.setGasoline(getCfg().getFuelBaseTeam());

		baseInfoB = new XHJZGuildBaseInfo();
		baseInfoB.setCamp(XHJZ_CAMP.B);
		baseInfoB.setGuildId(extParm.getCampBGuild());
		baseInfoB.setGuildName(extParm.getCampBGuildName());
		baseInfoB.setGuildTag(extParm.getCampBGuildTag());
		baseInfoB.setServerId(extParm.getCampBServerId());
		baseInfoB.setGuildFlag(extParm.getCampBguildFlag());
		baseInfoB.setCommonder(extParm.getCampBCommonder());
		baseInfoB.setTeamName(extParm.getTeamBName());
		baseInfoB.setGasoline(getCfg().getFuelBaseTeam());

		nextSyncToPlayer = createTime + 5000;
		XHJZBattleCfg cfg = getCfg();
		startTime = createTime + cfg.getPrepairTime() * 1000;
		this.nextLoghonor = startTime + XHJZConst.MINUTE_MICROS;

		worldPointService = new XHJZWorldPointService(this);
		worldPointService.init();

		getGuildFormation(baseInfoA.getGuildId());
		getGuildFormation(baseInfoB.getGuildId());

		long periodTime = TiberiumConstCfg.getInstance().getPerioTime() * 1L;
		this.addTickable(new HawkPeriodTickable(periodTime) {

			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});
	}

	public void onPlayerLogin(IXHJZPlayer gamer) {
		gamer.getPush().syncPlayerWorldInfo();
		gamer.getPush().syncPlayerInfo();
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<IXHJZWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (IXHJZWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				IXHJZWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof IXHJZPassiveAlarmTriggerMarch) {
				((IXHJZPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<IXHJZWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (IXHJZWorldMarch march : pointMs) {
			if (march instanceof IXHJZReportPushMarch) {
				((IXHJZReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

	}

	public void updateRoomActiveTime() {
		long curTime = HawkTime.getMillisecond();
		if (curTime > this.getCreateTime() && curTime < this.getOverTime()) {
			int termId = TiberiumWarService.getInstance().getTermId();
			if (extParm.isLeaguaWar()) {
				termId = TiberiumLeagueWarService.getInstance().getActivityInfo().getMark();
			}
			TWRoomData roomData = RedisProxy.getInstance().getTWRoomData(this.getXid().getUUID(), termId);
			if (roomData == null || roomData.getRoomState() != RoomState.INITED) {
				return;
			}

			roomData.setLastActiveTime(curTime);
			RedisProxy.getInstance().updateTWRoomData(roomData, termId);
		}
	}

	public IXHJZWorldMarch startMarch(IXHJZPlayer player, IXHJZWorldPoint fPoint, IXHJZWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams, int gasoline) {
		// gasoline = Math.max(effParams.getWorldmarchReq().getXhjzGasoline(), (int) player.getParent().getCfg().getFuelMarchNeed());
		// 生成行军
		IXHJZWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);

		player.setGasoline(player.getGasoline() - gasoline);
		player.getPush().syncXHJZPlayerInfo();
		march.setGasoline(gasoline);

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

		// 加入行军警报
		march.onMarchStart();

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE, march.toBuilder(WorldMarchRelation.SELF));
		player.sendProtocol(protocol);

		march.notifyMarchEvent(MarchEvent.MARCH_ADD); // 通知行军事件

		tPoint.onMarchCome(march);
		player.onMarchStart(march);

		return march;
	}

	public boolean isExtraSypMarchOccupied(Player player) {
		List<IXHJZWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
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
	public IXHJZWorldMarch genMarch(IXHJZPlayer player, IXHJZWorldPoint fPoint, IXHJZWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();
		XHJZMarchEntity march = new XHJZMarchEntity();
		march.setEffParams(effParams);
		// marchId设置放在前面
		march.setCreateTime(startTime);
		march.setMarchId(HawkOSOperator.randomUUID());
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
			march.setXhjzBianduiNum(effParams.getWorldmarchReq().getXhjzBianduiNum());
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
		IXHJZWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case XHJZ_BUILDING_SINGLE: // = 107; // 司令部
			iWorldMarch = new XHJZBuildingMarchSingle(player);
			((XHJZBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case XHJZ_BUILDING_MASS:// = 117; // 司令部
			iWorldMarch = new XHJZBuildingMarchMass(player);
			((XHJZBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.XHJZ_BUILDING_MASS);
			((XHJZBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.XHJZ_BUILDING_MASS_JOIN);
			break;
		case XHJZ_BUILDING_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new XHJZBuildingMarchMassJoin(player);
			break;
		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!! " + marchType);
		}
		march.getEffectParams().setImarch(iWorldMarch);
		iWorldMarch.setMarchEntity(march);

		List<Position.Builder> roadAllPoints = new LinkedList<>();
		roadAllPoints.add(Position.newBuilder().setX(fPoint.getX()).setY(fPoint.getY()));
		for (Position p : effParams.getWorldmarchReq().getXhjzPointsList()) {
			roadAllPoints.add(p.toBuilder());
		}

		// boolean fuelFree = false;
		// int speedUp = 0;
		// if (tPoint instanceof IXHJZBuilding && roadAllPoints.size() == 2) { // 从主基地到建筑 不拐弯
		// IXHJZBuilding build = (IXHJZBuilding) tPoint;
		// if (build.getBuildType() == XHJZBuildType.LIU && build.underGuildControl(player.getGuildId())) {
		// fuelFree = build.getBuildTypeCfg().getOccupyMarchFree() > 0;
		// speedUp = build.getBuildTypeCfg().getOccupyMarchSpeedUp();
		// }
		// }
		iWorldMarch.calFullpath(startTime, roadAllPoints);

		// 行军时间
		Position tPosition = iWorldMarch.getNextPoints().get(0);
		long needTime = tPosition.getReachTime() - tPosition.getStartTime();
		march.setEndTime(tPosition.getReachTime());
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
			iWorldMarch.calFullpath(march.getStartTime(), roadAllPoints);
		}

		return iWorldMarch;
	}

	public XHJZBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(XHJZBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public IXHJZWorldMarch getPlayerMarch(String playerId, String marchId) {
		IXHJZWorldMarch march = getMarch(marchId);
		if (march != null && Objects.equals(march.getPlayerId(), playerId)) {
			return march;
		}
		return null;
	}

	public List<IXHJZWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<IXHJZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<IXHJZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	@SuppressWarnings("unchecked")
	public <T extends IXHJZBuilding> List<T> getXHJZBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) tblyBuildingMap.get(type));
	}

	public List<IXHJZBuilding> getXHJZBuildingList() {
		return buildingList;
	}

	public List<IXHJZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IXHJZWorldMarch> result = new ArrayList<>();
		for (IXHJZWorldMarch ma : worldMarches.values()) {
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

	public IXHJZWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<IXHJZWorldMarch> getGuildWarMarch(String guildId) {
		List<IXHJZWorldMarch> result = new ArrayList<>();
		for (IXHJZWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof XHJZBuildingMarchMass) {
					IXHJZBuilding point = (IXHJZBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof XHJZBuildingMarchSingle) {
					IXHJZBuilding point = (IXHJZBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
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

				result.add(march);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return result;
	}

	public List<IXHJZWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<IXHJZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<IXHJZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<IXHJZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IXHJZWorldMarch> result = new LinkedList<>();
		for (IXHJZWorldMarch ma : worldMarches.values()) {
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
	public Optional<IXHJZWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(worldPointService.getViewPoints().get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<IXHJZWorldPoint> getWorldPoint(int pointId) {
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
			worldPointService.onTick();
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;

				baseInfoA.setGasoline(baseInfoA.getGasoline() + baseInfoA.getAllianceFuelAdd() * 3);
				baseInfoB.setGasoline(baseInfoB.getGasoline() + baseInfoB.getAllianceFuelAdd() * 3);

				PBXHJZGameInfoSync.Builder builder = buildSyncPB();
				// 夸服玩家和本服分别广播
				broadcastCrossProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_GAME_SYNC, builder));
				for (IXHJZPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}

				// 发送主播
				for (IXHJZPlayer anchor : getAnchors()) {
					sync(anchor);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		getGuildFormation(baseInfoA.getGuildId()).checkMarchIdRemove();
		getGuildFormation(baseInfoB.getGuildId()).checkMarchIdRemove();

		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(IXHJZPlayer player) {
		state.enterWorld(player);
	}

	public void joinRoom(IXHJZPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || isGameOver()) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}

		player.setCamp(Objects.equals(player.getGuildId(), baseInfoA.getGuildId()) ? XHJZ_CAMP.A : XHJZ_CAMP.B);
		if (player.getCamp() == XHJZ_CAMP.A) {
			player.setGuildId(baseInfoA.getGuildId());
			player.setGuildTag(baseInfoA.getGuildTag());
			player.setGuildFlag(baseInfoA.getGuildFlag());
			player.setGuildName(baseInfoA.getGuildName());
			for (XHJZWarPlayerData comder : baseInfoA.getCommonder()) {
				if (comder.id.equals(player.getId())) {
					comder.name = player.getName();
					player.setCommonder(1);
				}
			}
		} else {
			player.setGuildId(baseInfoB.getGuildId());
			player.setGuildTag(baseInfoB.getGuildTag());
			player.setGuildFlag(baseInfoB.getGuildFlag());
			player.setGuildName(baseInfoB.getGuildName());
			for (XHJZWarPlayerData comder : baseInfoB.getCommonder()) {
				if (comder.id.equals(player.getId())) {
					comder.name = player.getName();
					player.setCommonder(1);
				}
			}
		}

		int[] bornP = GameUtil.splitXAndY(worldPointService.getBaseByCamp(player.getCamp()).getPointId());

		player.setPos(bornP);

		player.init();
		XHJZRoomManager.getInstance().cache(player);
		player.setXhjzState(XHJZState.GAMEING);
		player.setGasoline((int) getCfg().getFuelBase());
		player.getPush().pushJoinGame();
		// this.addViewPoint(player);
		playerMap.put(player.getId(), player);
		if (player.isCsPlayer()) {
			csPlayerids.add(player.getId());
			csServerId = player.getMainServerId();
		}
		totalEnterPower += player.getPower();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {} guildId {}", getId(), player.getGuildId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public int[] randomPoint() {
		IXHJZBuilding build = HawkRand.randomObject(buildingList);
		return GameUtil.splitXAndY(build.getPointId());
	}

	public void quitWorld(IXHJZPlayer quitPlayer, XHJZQuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBXHJZGameOver.Builder builder = PBXHJZGameOver.newBuilder();
			if (reason == XHJZQuitReason.LEAVE) {
				builder.setQuitReson(reason.intValue());
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_GAME_OVER, builder));
		}

		if (quitPlayer.isAnchor()) { // 主播退了不用理他
			anchorMap.remove(quitPlayer.getId());
			quitPlayer.quitGame();
			return;
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		worldPointService.getWorldScene().leave(quitPlayer.getEye().getAoiObjId());
		// 删除行军
		cleanCityPointMarch(quitPlayer);

		for (IXHJZPlayer gamer : playerMap.values()) {
			PBXHJZPlayerQuitRoom.Builder bul = PBXHJZPlayerQuitRoom.newBuilder();
			bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code2.XHJZ_PLAYER_QUIT, bul));
		}

		quitPlayer.getData().getQueueEntities().clear();

		// if (reason == QuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_XHJZ).setKey(NoticeCfgId.XHJZ_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();
		csPlayerids.remove(quitPlayer.getId());

	}

	public void cleanCityPointMarch(IXHJZPlayer quitPlayer) {
		try {
			List<IXHJZWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (IXHJZWorldMarch march : quiterMarches) {
				if (march instanceof IXHJZMassMarch) {
					march.getMassJoinMarchs(true).forEach(IXHJZWorldMarch::onMarchCallback);
				}
				if (march instanceof IXHJZMassJoinMarch) {
					Optional<IXHJZMassMarch> massMarch = ((IXHJZMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						IXHJZMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(IXHJZWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<IXHJZWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (IXHJZWorldMarch march : pms) {
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

	public List<IXHJZPlayer> getPlayerList(XHJZState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<IXHJZPlayer> getPlayerList(XHJZState st1, XHJZState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<IXHJZPlayer> getPlayerList(XHJZState st1, XHJZState st2, XHJZState st3) {
		List<IXHJZPlayer> result = new ArrayList<>();
		for (IXHJZPlayer player : playerMap.values()) {
			XHJZState state = player.getXhjzState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public IXHJZPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public List<IXHJZPlayer> getCampPlayers(XHJZ_CAMP camp) {
		List<IXHJZPlayer> list = new ArrayList<>();
		for (IXHJZPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void setPlayerList(List<IXHJZPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	public void setViewPoints(List<IXHJZWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<IXHJZWorldMarch> getWorldMarchList() {
		ArrayList<IXHJZWorldMarch> result = new ArrayList<>(worldMarches.values());
		return result;
	}

	public void removeMarch(IXHJZWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(IXHJZWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<IXHJZWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public IXHJZBattleRoomState getState() {
		return state;
	}

	public void setState(IXHJZBattleRoomState state) {
		this.state = state;
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

	public XHJZExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(XHJZExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(IXHJZPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber()).setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo, null);

	}

	public void addWorldBroadcastMsg(ChatParames parames, XHJZ_CAMP camp) {
		broadcastChatMsg(parames.toPBMsg(), camp);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		broadcastChatMsg(parames.toPBMsg(), null);
	}

	private void broadcastChatMsg(ChatMsg pbMsg, XHJZ_CAMP camp) {
		Set<Player> tosend = new HashSet<>(getPlayerList(XHJZState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg) || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE || Objects.nonNull(camp);
		if (isGuildMsg) {
			String guildId = Objects.nonNull(camp) ? getCampGuild(camp) : pbMsg.getAllianceId();
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), guildId)).collect(Collectors.toSet());
		} else {
			for (IXHJZPlayer anchor : getAnchors()) {
				tosend.add(anchor);
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

	public long getCurTimeMil() {
		return curTimeMil;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<IXHJZPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public String getCsServerId() {
		return csServerId;
	}

	public Set<String> getCsPlayerids() {
		return csPlayerids;
	}

	public PBXHJZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBXHJZGameInfoSync lastSyncpb) {
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

	public List<IXHJZPlayer> getAnchors() {
		if (anchorMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<IXHJZPlayer> result = new ArrayList<>(anchorMap.size());
		for (IXHJZPlayer anchor : anchorMap.values()) {
			if (Objects.nonNull(anchor) && anchor.isActiveOnline()) {
				result.add(anchor);
			}
		}
		return result;
	}

	public boolean isAnchor(IXHJZPlayer player) {
		return anchorMap.containsKey(player.getId());
	}

	public long getTotalEnterPower() {
		return totalEnterPower;
	}

	public int getCampOrder(XHJZ_CAMP camp) {
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

	public String getCampGuild(XHJZ_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.getGuildId();
		case B:
			return baseInfoB.getGuildId();
		default:
			break;
		}

		return null;
	}

	public String getCampGuildTag(XHJZ_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.getGuildTag();
		case B:
			return baseInfoB.getGuildTag();
		default:
			break;
		}

		return null;
	}

	public String getCampGuildName(XHJZ_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.getGuildName();
		case B:
			return baseInfoB.getGuildName();
		default:
			break;
		}

		return null;
	}

	public XHJZ_CAMP getGuildCamp(String guildId) {
		if (Objects.equals(guildId, baseInfoA.getGuildId())) {
			return XHJZ_CAMP.A;
		}
		return XHJZ_CAMP.B;
	}

	public String getChronoReadGuild() {
		return chronoReadGuild;
	}

	public void setChronoReadGuild(String chronoReadGuild) {
		this.chronoReadGuild = chronoReadGuild;
	}

	public String getChronoReadLeader() {
		return chronoReadLeader;
	}

	public void setChronoReadLeader(String chronoReadLeader) {
		this.chronoReadLeader = chronoReadLeader;
	}

	public XHJZGuildBaseInfo getCampBase(String guildId) {
		if (Objects.equals(guildId, baseInfoA.getGuildId())) {
			return baseInfoA;
		}
		if (Objects.equals(guildId, baseInfoB.getGuildId())) {
			return baseInfoB;
		}
		return XHJZGuildBaseInfo.defaultInstance;
	}

	public XHJZGuildBaseInfo getCampBase(XHJZ_CAMP camp) {
		if (camp == baseInfoA.getCamp()) {
			return baseInfoA;
		}
		if (camp == baseInfoB.getCamp()) {
			return baseInfoB;
		}
		return XHJZGuildBaseInfo.defaultInstance;
	}

	public XHJZGuildBaseInfo getBaseInfoA() {
		return baseInfoA;
	}

	public void setBaseInfoA(XHJZGuildBaseInfo baseInfoA) {
		this.baseInfoA = baseInfoA;
	}

	public XHJZGuildBaseInfo getBaseInfoB() {
		return baseInfoB;
	}

	public void setBaseInfoB(XHJZGuildBaseInfo baseInfoB) {
		this.baseInfoB = baseInfoB;
	}

	public XHJZWorldPointService getWorldPointService() {
		return worldPointService;
	}

}
