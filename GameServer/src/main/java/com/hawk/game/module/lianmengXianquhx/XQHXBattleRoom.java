package com.hawk.game.module.lianmengXianquhx;

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
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.helper.HawkAssert;
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
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
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
import com.hawk.game.module.lianmengXianquhx.XQHXConst.XQHXState;
import com.hawk.game.module.lianmengXianquhx.XQHXRoomManager.XQHX_CAMP;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBattleCfg;
import com.hawk.game.module.lianmengXianquhx.cfg.XQHXBuildCfg;
import com.hawk.game.module.lianmengXianquhx.entity.XQHXMarchEntity;
import com.hawk.game.module.lianmengXianquhx.msg.XQHXQuitReason;
import com.hawk.game.module.lianmengXianquhx.player.IXQHXPlayer;
import com.hawk.game.module.lianmengXianquhx.player.module.guildformation.XQHXGuildFormationObj;
import com.hawk.game.module.lianmengXianquhx.roomstate.IXQHXBattleRoomState;
import com.hawk.game.module.lianmengXianquhx.worldmarch.IXQHXWorldMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXAssistanceSingleMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXAttackMonsterMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXAttackPlayerMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXBuildingMarchMass;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXBuildingMarchMassJoin;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXBuildingMarchSingle;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXMassJoinSingleMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXMassSingleMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXNotifyMarchEventFunc;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXPylonMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.XQHXSpyMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXMassJoinMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXMassMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengXianquhx.worldmarch.submarch.IXQHXReportPushMarch;
import com.hawk.game.module.lianmengXianquhx.worldpoint.IXQHXBuilding;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXBuildState;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXCommandCenter;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXMonster;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXPointUtil;
import com.hawk.game.module.lianmengXianquhx.worldpoint.XQHXPylon;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.TiberiumWar.TWState;
import com.hawk.game.protocol.TiberiumWar.TWStateInfo;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.XQHX.PBGuildInfo;
import com.hawk.game.protocol.XQHX.PBPlayerInfo;
import com.hawk.game.protocol.XQHX.PBXQHXGameInfoSync;
import com.hawk.game.protocol.XQHX.PBXQHXGameOver;
import com.hawk.game.protocol.XQHX.PBXQHXPlayerQuitRoom;
import com.hawk.game.protocol.XQHX.PBXQHXSecondMapResp;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.tiberium.TWPlayerData;
import com.hawk.game.service.tiberium.TWRoomData;
import com.hawk.game.service.tiberium.TiberiumConst.RoomState;
import com.hawk.game.service.tiberium.TiberiumLeagueWarService;
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
public class XQHXBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	private XQHXExtraParam extParm;
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
	private Map<String, IXQHXPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, IXQHXPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, IXQHXWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private XQHXMonsterRefesh monsterrefresh;
	private XQHXPylonRefesh pylonRefesh;
	private Map<String, IXQHXWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private List<IXQHXBuilding> buildingList = new CopyOnWriteArrayList<>();
	private List<XQHXMonster> monsterList = new CopyOnWriteArrayList<>();
	private IXQHXBattleRoomState state;
	private long createTime;
	private long startTime;
	private long overTime;

	private XQHXRandPointSeed pointSeed = new XQHXRandPointSeed();

	private XQHXGuildBaseInfo baseInfoA;
	private XQHXGuildBaseInfo baseInfoB;
	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBXQHXGameInfoSync lastSyncpb = PBXQHXGameInfoSync.getDefaultInstance();
	// private long lastSyncMap;
	// private PBXQHXSecondMapResp lastMappb;

	private long nextLoghonor;

	private Multimap<Class<? extends IXQHXBuilding>, ? super IXQHXBuilding> tblyBuildingMap = HashMultimap.create();

	/** 地图上占用点 */
	private Set<Integer> occupationPointSet = new HashSet<>();

	public String first5000Honor;// 首无5000
	public String firstKillNian; // 首杀nian
	public String firstControlHeXin; // 首控制核心

	private Map<String, IXQHXPlayer> anchorMap = new HashMap<>();
	private String csServerId = "";
	private Set<String> csPlayerids = new HashSet<>();

	private Map<String, XQHXGuildFormationObj> guildFormationObjmap = new HashMap<>();
	private Map<String, PBPlayerInfo> notJoinPlayers = new HashMap<>();
	private ConcurrentSkipListMap<String, XQHXNotifyMarchEventFunc> noticyMarchEnentMap = new ConcurrentSkipListMap<>();

	public XQHXBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		XQHXProtocol pro = (XQHXProtocol) protocol;
		IXQHXPlayer player = pro.getPlayer();
		player.onProtocol(pro.getSource());

		return true;
	}

	public XQHXGuildFormationObj getGuildFormation(String guildId) { // TODO 包装成自己的
		if (guildFormationObjmap.containsKey(guildId)) {
			XQHXGuildFormationObj result = guildFormationObjmap.get(guildId);
			return result;
		}
		XQHXGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new XQHXGuildFormationObj());
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
		if (baseInfoA.campServerId.equals(csServerId)) {
			return baseInfoA.campGuild;
		}
		return baseInfoB.campGuild;
	}

	public int getCsGuildWarCount() {
		if (baseInfoA.campServerId.equals(csServerId)) {
			return baseInfoA.campGuildWarCount;
		}
		return baseInfoB.campGuildWarCount;
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - curTimeMil < 2000;
	}

	public void sync(IXQHXPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		PBXQHXGameInfoSync.Builder bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_GAME_SYNC, bul));

	}

	public boolean isHotBloodModel() {
		return overTime - curTimeMil < getCfg().getHotBloodModel() * 1000;
	}

	public PBXQHXGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBXQHXGameInfoSync.Builder bul = PBXQHXGameInfoSync.newBuilder();
		bul.setGameCreateTime(createTime);
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		PBGuildInfo.Builder aInfo = PBGuildInfo.newBuilder().setServerId(baseInfoA.campServerId).setCamp(XQHX_CAMP.A.intValue()).setGuildFlag(baseInfoA.campguildFlag)
				.setGuildName(baseInfoA.campGuildName)
				.setGuildTag(baseInfoA.campGuildTag).setGuildId(baseInfoA.campGuild);
		PBGuildInfo.Builder bInfo = PBGuildInfo.newBuilder().setServerId(baseInfoB.campServerId).setCamp(XQHX_CAMP.B.intValue()).setGuildFlag(baseInfoB.campguildFlag)
				.setGuildName(baseInfoB.campGuildName)
				.setGuildTag(baseInfoB.campGuildTag).setGuildId(baseInfoB.campGuild);

		double allianceScoreControlA = 0;
		int perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		int killHonorA = 0;
		Map<EffType, Integer> battleEffValA = new HashMap<>();

		double allianceScoreControlB = 0;
		int perMinB = 0; // 每分增加
		int buildCountB = 0; // 占领建筑
		int playerCountB = 0; // 战场中人数
		int killHonorB = 0;
		Map<EffType, Integer> battleEffValB = new HashMap<>();

		int monsterCount = 0;
		int pylongCount = 0;
		for (IXQHXWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof IXQHXBuilding) {
				IXQHXBuilding build = (IXQHXBuilding) viewp;
				if (build.getState() == XQHXBuildState.ZHAN_LING) {
					if (Objects.equals(baseInfoA.campGuild, build.getGuildId())) {
						perMinA += Math.round(build.getGuildHonorPerSecond() * 60);
						allianceScoreControlA += build.getBuildTypeCfg().getAllianceScoreControl();
						for (HawkTuple2<EffType, Integer> hw2 : build.getBuildTypeCfg().getControleBuffList()) {
							battleEffValA.merge(hw2.first, hw2.second, (v1, v2) -> v1 + v2);
						}
						buildCountA++;
					} else if (Objects.equals(baseInfoB.campGuild, build.getGuildId())) {
						perMinB += Math.round(build.getGuildHonorPerSecond() * 60);
						allianceScoreControlB += build.getBuildTypeCfg().getAllianceScoreControl();
						for (HawkTuple2<EffType, Integer> hw2 : build.getBuildTypeCfg().getControleBuffList()) {
							battleEffValB.merge(hw2.first, hw2.second, (v1, v2) -> v1 + v2);
						}
						buildCountB++;
					}
				}
			}

			if (viewp instanceof XQHXMonster) {
				monsterCount++;
			}
			if (viewp instanceof XQHXPylon) {
				pylongCount++;
			}
			
		}

		List<IXQHXPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (IXQHXPlayer p : all) {
			if (p.getCamp() == XQHX_CAMP.A) {
				killHonorA += p.getKillHonor();
				playerCountA++;
			} else {
				killHonorB += p.getKillHonor();
				playerCountB++;
			}
			PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
			prc.setCamp(p.getCamp().intValue());
			prc.setName(p.getName());
			prc.setHonor(p.getHonor());
			prc.setGuildTag(p.getGuildTag());
			prc.setPlayerId(p.getId());
			prc.setKillhonor(p.getKillHonor());
			prc.setHurthonor(p.getHurtHonor());
			prc.setBuildControlHonor(p.getBuildHonor());
			prc.setPylonHonor(p.getPylonHonor());
			prc.setPylonCnt(p.getPylonCnt());
			prc.setMonsterHonor(p.getMonsterHonor());
			prc.setKillMonster(p.getKillMonster());
			prc.setCityLevel(p.getCityLevel());
			prc.addAllDressShow(p.getShowDress());
			prc.setIcon(p.getIcon());
			prc.setPfIcon(p.getPfIcon());
			bul.addPlayerInfo(prc);
			notJoinPlayers.remove(p.getId());
		}

		bul.addAllPlayerNotJoin(notJoinPlayers.values());

		int honorA = (int) (allianceScoreControlA + baseInfoA.buildControlHonor) + (int) (baseInfoA.pylonHonor) + (int) (baseInfoA.monsterHonor);
		aInfo.setHonor(honorA).setPerMin(perMinA).setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setPylonHonor((int) (baseInfoA.pylonHonor))
				.setMonsterHonor((int) (baseInfoA.monsterHonor))
				.setBuildControlHonor((int) (allianceScoreControlA + baseInfoA.buildControlHonor))
				.setGuildOrder(baseInfoA.campOrder);

		int honorB = (int) (allianceScoreControlB + baseInfoB.buildControlHonor) + (int) (baseInfoB.pylonHonor) + (int) (baseInfoB.monsterHonor);
		bInfo.setHonor(honorB).setPerMin(perMinB).setBuildCount(buildCountB).setPlayerCount(playerCountB)
				.setPylonHonor((int) (baseInfoB.pylonHonor))
				.setMonsterHonor((int) (baseInfoB.monsterHonor))
				.setBuildControlHonor((int) (allianceScoreControlB + baseInfoB.buildControlHonor))
				.setGuildOrder(baseInfoB.campOrder);

		bul.setMonsterCount(monsterCount);
		bul.setPylonCount(pylongCount);
		bul.addGuildInfo(aInfo);
		bul.addGuildInfo(bInfo);

		long campAScore = aInfo.getHonor();
		long campBScore = bInfo.getHonor();
		XQHX_CAMP winCamp = campAScore > campBScore ? XQHX_CAMP.A : XQHX_CAMP.B;
		bul.setWinCamp(winCamp.intValue());

		lastSyncpb = bul.build();
		lastSyncGame = curTimeMil;

		baseInfoA.battleEffVal = ImmutableMap.copyOf(battleEffValA);
		baseInfoA.campHonor = honorA;

		baseInfoB.battleEffVal = ImmutableMap.copyOf(battleEffValB);
		baseInfoB.campHonor = honorB;

		if (StringUtils.isEmpty(first5000Honor)) {
			if (honorA >= 5000) {
				first5000Honor = baseInfoA.campGuild;
			} else if (honorB >= 5000) {
				first5000Honor = baseInfoB.campGuild;
			}
		}

		return bul;
	}

	public void getSecondMap(IXQHXPlayer player) {
		PBXQHXSecondMapResp.Builder bul = PBXQHXSecondMapResp.newBuilder();
		int pylongCnt = 0;
		for (IXQHXWorldPoint point : getViewPoints()) {
			if(point instanceof XQHXPylon){
				pylongCnt++;
			}
			if (point instanceof XQHXMonster) {
				continue;
			}
			if (point instanceof IXQHXPlayer && player.getCamp() != ((IXQHXPlayer) point).getCamp()) {
				continue;
			}

			bul.addPoints(point.toBuilder(player));
		}
		bul.setMonsterCount(worldMonsterCount());
		bul.setNextMonsterRefresh(monsterrefresh.nextRefreshTime());
		bul.setMonsterTurn(monsterrefresh.getRefreshTurn());
		bul.setPylonCount(pylongCnt);
		bul.setNextPylonRefresh(pylonRefesh.nextRefreshTime());
		bul.setPylonTurn(pylonRefesh.getRefreshTurn());
		
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_SECOND_MAP_S, bul));
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
			IXQHXPlayer pl = (IXQHXPlayer) ppp;
			double killHonor = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
			
				int scoreForDefense = getCfg().getScoreForDefense(cfg.getSoldierType());
				if (scoreForDefense > 0) {
					killHonor = killHonor + cfg.getPower() * (army.getDeadCount() + army.getWoundedCount()) / scoreForDefense;
				}
			}
			pl.setHurtTankHonor(pl.getHurtTankHonor() + killHonor);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			int killPow = 0;
			IXQHXPlayer pl = (IXQHXPlayer) ppp;
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
		baseInfoA = new XQHXGuildBaseInfo();
		baseInfoA.campGuild = extParm.getCampAGuild();
		baseInfoA.campGuildName = extParm.getCampAGuildName();
		baseInfoA.campGuildTag = extParm.getCampAGuildTag();
		baseInfoA.campServerId = extParm.getCampAServerId();
		baseInfoA.campguildFlag = extParm.getCampAguildFlag();
		baseInfoA.camp = XQHX_CAMP.A;

		baseInfoB = new XQHXGuildBaseInfo();
		baseInfoB.campGuild = extParm.getCampBGuild();
		baseInfoB.campGuildName = extParm.getCampBGuildName();
		baseInfoB.campGuildTag = extParm.getCampBGuildTag();
		baseInfoB.campServerId = extParm.getCampBServerId();
		baseInfoB.campguildFlag = extParm.getCampBguildFlag();
		baseInfoB.camp = XQHX_CAMP.B;

		nextSyncToPlayer = createTime + 5000;
		XQHXBattleCfg cfg = getCfg();
		startTime = createTime + cfg.getPrepairTime() * 1000;
		this.nextLoghonor = startTime + XQHXConst.MINUTE_MICROS;

		List<int[]> bornlist = cfg.copyOfbornPointAList();
		baseInfoA.bornPointList = new XQHXBornPointRing(bornlist.get(0), bornlist.get(1));

		bornlist = cfg.copyOfbornPointBList();
		baseInfoB.bornPointList = new XQHXBornPointRing(bornlist.get(0), bornlist.get(1));

		getGuildFormation(baseInfoA.campGuild);
		getGuildFormation(baseInfoB.campGuild);

		{
			ConfigIterator<XQHXBuildCfg> buildRefreshIt = HawkConfigManager.getInstance().getConfigIterator(XQHXBuildCfg.class);
			int index = 0;
			for (XQHXBuildCfg bcfg : buildRefreshIt) {
				XQHXCommandCenter icd = new XQHXCommandCenter(this);
				icd.setCfgId(bcfg.getBuildId());
				icd.setIndex(index);
				icd.setX(bcfg.getCoordinateX());
				icd.setY(bcfg.getCoordinateY());
				icd.setBuildTypeId(bcfg.getBuildTypeId());
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				index++;
			}
		}

		monsterrefresh = XQHXMonsterRefesh.create(this);
		pylonRefesh = XQHXPylonRefesh.create(this);
		for (TWPlayerData tdp : extParm.getCampAPlayers()) {
			try {
				PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
				prc.setCamp(XQHX_CAMP.A.intValue());
				prc.setName(tdp.getName());
				prc.setPlayerId(tdp.getId());
				prc.setCityLevel(tdp.getCityLevel());
				prc.setIcon(tdp.getIcon());
				prc.setPfIcon(tdp.getPfIcon());
				notJoinPlayers.put(tdp.getId(), prc.build());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		for (TWPlayerData tdp : extParm.getCampBPlayers()) {
			try {
				PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
				prc.setCamp(XQHX_CAMP.B.intValue());
				prc.setName(tdp.getName());
				prc.setPlayerId(tdp.getId());
				prc.setCityLevel(tdp.getCityLevel());
				prc.setIcon(tdp.getIcon());
				prc.setPfIcon(tdp.getPfIcon());
				notJoinPlayers.put(tdp.getId(), prc.build());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		long periodTime = TiberiumConstCfg.getInstance().getPerioTime() * 1L;
		this.addTickable(new HawkPeriodTickable(periodTime) {

			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});
	}

	public void onPlayerLogin(IXQHXPlayer gamer) {
		gamer.getPush().syncPlayerWorldInfo();
		gamer.getPush().syncPlayerInfo();
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<IXQHXWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (IXQHXWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				IXQHXWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof IXQHXPassiveAlarmTriggerMarch) {
				((IXQHXPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<IXQHXWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (IXQHXWorldMarch march : pointMs) {
			if (march instanceof IXQHXReportPushMarch) {
				((IXQHXReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

		for (IXQHXBuilding build : getXQHXBuildingList()) {
			build.onPlayerLogin(gamer);
		}
		// 号令同步
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

	public void doMoveCitySuccess(IXQHXPlayer player, int[] targetPoint) {
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
		for (IXQHXWorldMarch march : worldMarches.values()) {
			if (march instanceof IXQHXReportPushMarch) {
				((IXQHXReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		removeViewPoint(player);
		player.setPos(targetPoint);
		addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();
	}

	public IXQHXWorldMarch startMarch(IXQHXPlayer player, IXQHXWorldPoint fPoint, IXQHXWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		// 生成行军
		IXQHXWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
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
		List<IXQHXWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
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
	public IXQHXWorldMarch genMarch(IXQHXPlayer player, IXQHXWorldPoint fPoint, IXQHXWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		XQHXMarchEntity march = new XQHXMarchEntity();
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
		IXQHXWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new XQHXAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new XQHXSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new XQHXAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new XQHXMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new XQHXMassJoinSingleMarch(player);
			break;
		case XQHX_BUILDING_SINGLE: // = 107; // 司令部
			iWorldMarch = new XQHXBuildingMarchSingle(player);
			((XQHXBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case XQHX_BUILDING_MASS:// = 111; // 集结铁幕装置行军
			iWorldMarch = new XQHXBuildingMarchMass(player);
			((XQHXBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.XQHX_BUILDING_MASS);
			((XQHXBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.XQHX_BUILDING_MASS_JOIN);
			break;

		case XQHX_BUILDING_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new XQHXBuildingMarchMassJoin(player);
			break;
		case ATTACK_MONSTER:
			iWorldMarch = new XQHXAttackMonsterMarch(player);
			break;
		case PYLON_MARCH:
			iWorldMarch = new XQHXPylonMarch(player);
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
		if (iWorldMarch instanceof IXQHXMassJoinMarch) {
			iWorldMarch.setHashThread(((IXQHXMassJoinMarch) iWorldMarch).leaderMarch().get().getHashThread());
		} else {
			final int threadNum = HawkTaskManager.getInstance().getThreadNum();
			iWorldMarch.setHashThread(tPoint.getHashThread(threadNum));
		}
		return iWorldMarch;
	}

	public XQHXBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(XQHXBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public IXQHXWorldMarch getPlayerMarch(String playerId, String marchId) {
		IXQHXWorldMarch march = getMarch(marchId);
		if (march != null && Objects.equals(march.getPlayerId(), playerId)) {
			return march;
		}
		return null;
	}

	public List<IXQHXWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<IXQHXWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<IXQHXWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	@SuppressWarnings("unchecked")
	public <T extends IXQHXBuilding> List<T> getXQHXBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) tblyBuildingMap.get(type));
	}

	public List<IXQHXBuilding> getXQHXBuildingList() {
		return buildingList;
	}

	public List<IXQHXWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IXQHXWorldMarch> result = new ArrayList<>();
		for (IXQHXWorldMarch ma : worldMarches.values()) {
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

	public IXQHXWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<IXQHXWorldMarch> getGuildWarMarch(String guildId) {
		List<IXQHXWorldMarch> result = new ArrayList<>();
		for (IXQHXWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof XQHXBuildingMarchMass) {
					IXQHXBuilding point = (IXQHXBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof XQHXBuildingMarchSingle) {
					IXQHXBuilding point = (IXQHXBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
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

	public List<IXQHXWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<IXQHXWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<IXQHXWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<IXQHXWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IXQHXWorldMarch> result = new LinkedList<>();
		for (IXQHXWorldMarch ma : worldMarches.values()) {
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
	public Optional<IXQHXWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(viewPoints.get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<IXQHXWorldPoint> getWorldPoint(int pointId) {
		return Optional.ofNullable(viewPoints.get(pointId));
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

		try { // 广播行军变化
			while (!noticyMarchEnentMap.isEmpty()) {
				// 移除当前时间节点集合
				XQHXNotifyMarchEventFunc func = noticyMarchEnentMap.pollFirstEntry().getValue();
				func.apply(null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;
				PBXQHXGameInfoSync.Builder builder = buildSyncPB();
				// 夸服玩家和本服分别广播
				broadcastCrossProtocol(HawkProtocol.valueOf(HP.code2.XQHX_GAME_SYNC, builder));
				for (IXQHXPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}

				// 发送主播
				for (IXQHXPlayer anchor : getAnchors()) {
					sync(anchor);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		monsterrefresh.onTick();
		pylonRefesh.onTick();
		baseInfoA.tick();
		baseInfoB.tick();
		getGuildFormation(baseInfoA.campGuild).checkMarchIdRemove();
		getGuildFormation(baseInfoB.campGuild).checkMarchIdRemove();

		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(IXQHXPlayer player) {
		state.enterWorld(player);
	}

	/**
	 * 副本中玩家更新点
	 * 
	 * @param point
	 */
	public void worldPointUpdate(IXQHXWorldPoint point) {
		for (IXQHXPlayer pla : getPlayerList(XQHXState.GAMEING)) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(pla));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}

		for (IXQHXPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(anchor));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(protocol);
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

	public void joinRoom(IXQHXPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || isGameOver()) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}
		notJoinPlayers.remove(player.getId());

		player.setCamp(Objects.equals(player.getGuildId(), baseInfoA.campGuild) ? XQHX_CAMP.A : XQHX_CAMP.B);

		int[] bornP = null;
		if (player.getCamp() == XQHX_CAMP.A) {
			bornP = randomBornPoint(baseInfoA.bornPointList);
			player.setGuildId(baseInfoA.campGuild);
			player.setGuildTag(baseInfoA.campGuildTag);
			player.setGuildFlag(baseInfoA.campguildFlag);
			player.setGuildName(baseInfoA.campGuildName);
		} else {
			bornP = randomBornPoint(baseInfoB.bornPointList);
			player.setGuildId(baseInfoB.campGuild);
			player.setGuildTag(baseInfoB.campGuildTag);
			player.setGuildFlag(baseInfoB.campguildFlag);
			player.setGuildName(baseInfoB.campGuildName);
		}

		while (Objects.isNull(bornP)) {
			bornP = randomFreePoint(randomPoint(), player.getGridCnt());
		}

		player.setPos(bornP);

		player.init();
		XQHXRoomManager.getInstance().cache(player);
		player.setXQHXState(XQHXState.GAMEING);
		player.getPush().pushJoinGame();
		this.addViewPoint(player);
		playerMap.put(player.getId(), player);
		if (player.isCsPlayer()) {
			csPlayerids.add(player.getId());
			csServerId = player.getMainServerId();
		}
		totalEnterPower += player.getPower();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {} guildId {}", getId(), player.getGuildId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public int[] randomPoint() {
		IXQHXBuilding build = HawkRand.randomObject(buildingList);
		return GameUtil.splitXAndY(build.getPointId());
	}

	public void quitWorld(IXQHXPlayer quitPlayer, XQHXQuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBXQHXGameOver.Builder builder = PBXQHXGameOver.newBuilder();
			if (reason == XQHXQuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_GAME_OVER, builder));
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

			for (IXQHXPlayer gamer : playerMap.values()) {
				PBXQHXPlayerQuitRoom.Builder bul = PBXQHXPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code2.XQHX_PLAYER_QUIT, bul));
			}

			quitPlayer.getData().getQueueEntities().clear();
		}

		// if (reason == QuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_XQHX).setKey(NoticeCfgId.XQHX_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();
		csPlayerids.remove(quitPlayer.getId());

	}

	public void cleanCityPointMarch(IXQHXPlayer quitPlayer) {
		try {
			List<IXQHXWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (IXQHXWorldMarch march : quiterMarches) {
				if (march instanceof IXQHXMassMarch) {
					march.getMassJoinMarchs(true).forEach(IXQHXWorldMarch::onMarchCallback);
				}
				if (march instanceof IXQHXMassJoinMarch) {
					Optional<IXQHXMassMarch> massMarch = ((IXQHXMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						IXQHXMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(IXQHXWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<IXQHXWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (IXQHXWorldMarch march : pms) {
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

	public List<IXQHXPlayer> getPlayerList(XQHXState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<IXQHXPlayer> getPlayerList(XQHXState st1, XQHXState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<IXQHXPlayer> getPlayerList(XQHXState st1, XQHXState st2, XQHXState st3) {
		List<IXQHXPlayer> result = new ArrayList<>();
		for (IXQHXPlayer player : playerMap.values()) {
			XQHXState state = player.getXQHXState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public IXQHXPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public List<IXQHXPlayer> getCampPlayers(XQHX_CAMP camp) {
		List<IXQHXPlayer> list = new ArrayList<>();
		for (IXQHXPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void setPlayerList(List<IXQHXPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	/** 添加世界点 */
	public void addViewPoint(IXQHXWorldPoint... vp) {
		List<IXQHXWorldPoint> list = new ArrayList<>();
		for (IXQHXWorldPoint point : vp) {
			if (point != null) {
				list.add(point);
			}
		}

		for (IXQHXWorldPoint point : list) {
			if (point instanceof XQHXMonster) {
				monsterList.add((XQHXMonster) point);
			}
			viewPoints.put(point.getPointId(), point);
		}

		resetOccupationPoint();

		for (IXQHXPlayer player : playerMap.values()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IXQHXWorldPoint point : list) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(pp);
		}
		for (IXQHXPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (IXQHXWorldPoint point : list) {
				builder.addPoints(point.toBuilder(anchor));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(pp);
		}
	}

	public boolean removeViewPoint(IXQHXWorldPoint vp) {
		boolean result = Objects.nonNull(viewPoints.remove(vp.getPointId()));
		if (vp instanceof XQHXMonster) {
			monsterList.remove(vp);
		}
		for (IXQHXPlayer gamer : playerMap.values()) {
			// 删除点
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(gamer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		for (IXQHXPlayer anchor : getAnchors()) {
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
		for (IXQHXWorldPoint viewp : viewPoints.values()) {
			viewp.fillWithOcuPointId(set);
		}
		occupationPointSet = set;
	}

	public List<IXQHXWorldPoint> getViewPoints() {
		return new ArrayList<>(viewPoints.values());
	}

	public void setViewPoints(List<IXQHXWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<IXQHXWorldMarch> getWorldMarchList() {
		ArrayList<IXQHXWorldMarch> result = new ArrayList<>(worldMarches.values());
		return result;
	}

	public void removeMarch(IXQHXWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(IXQHXWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<IXQHXWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public IXQHXBattleRoomState getState() {
		return state;
	}

	public void setState(IXQHXBattleRoomState state) {
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

	public XQHXExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(XQHXExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(IXQHXPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber()).setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo, null);

	}

	public void addWorldBroadcastMsg(ChatParames parames, XQHX_CAMP camp) {
		broadcastChatMsg(parames.toPBMsg(), camp);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		broadcastChatMsg(parames.toPBMsg(), null);
	}

	private void broadcastChatMsg(ChatMsg pbMsg, XQHX_CAMP camp) {
		Set<Player> tosend = new HashSet<>(getPlayerList(XQHXState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg) || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE || Objects.nonNull(camp);
		if (isGuildMsg) {
			String guildId = Objects.nonNull(camp) ? getCampGuild(camp) : pbMsg.getAllianceId();
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), guildId)).collect(Collectors.toSet());
		} else {
			for (IXQHXPlayer anchor : getAnchors()) {
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

	public int[] randomFreePoint(int[] popBornPointA, int gridCnt) {
		XQHXBattleCfg bcfg = getCfg();
		int x;
		int y;
		for (int i = 0; i < pointSeed.size(); i++) {
			int[] pp = pointSeed.nextPoint();
			x = popBornPointA[0] + pp[0];
			y = popBornPointA[1] + pp[1];

			if ((x + y) % 2 == gridCnt % 2) {
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

			Set<Integer> set = XQHXPointUtil.getOcuPointId(x, y, gridCnt);
			set.retainAll(occupationPointSet);
			if (!set.isEmpty()) {
				continue;
			}
			return new int[] { x, y };
		}
		return null;
	}

	public int[] randomBornPoint(XQHXBornPointRing pointRing) {
		int x;
		int y;
		for (int i = 0; i < pointRing.size(); i++) {
			int[] pp = pointRing.nextPoint();
			x = pp[0];
			y = pp[1];

			int pointRedis = 2;
			if ((x + y) % 2 == pointRedis % 2) {
				x += 1;
			}

			Set<Integer> set = XQHXPointUtil.getOcuPointId(x, y, pointRedis);
			set.retainAll(occupationPointSet);
			if (!set.isEmpty()) {
				continue;
			}

			return new int[] { x, y };
		}
		return null;
	}

	public long getCurTimeMil() {
		return curTimeMil;
	}

	public boolean checkPlayerCanOccupy(IXQHXPlayer player, int x, int y) {
		int pointRedis = 2;
		Set<Integer> set = XQHXPointUtil.getOcuPointId(x, y, pointRedis);
		set.removeAll(XQHXPointUtil.getOcuPointId(player.getX(), player.getY(), pointRedis));

		set.retainAll(occupationPointSet);
		if (!set.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public List<IXQHXPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public String getCsServerId() {
		return csServerId;
	}

	public Set<String> getCsPlayerids() {
		return csPlayerids;
	}

	public PBXQHXGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBXQHXGameInfoSync lastSyncpb) {
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

	public List<IXQHXPlayer> getAnchors() {
		if (anchorMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<IXQHXPlayer> result = new ArrayList<>(anchorMap.size());
		for (IXQHXPlayer anchor : anchorMap.values()) {
			if (Objects.nonNull(anchor) && anchor.isActiveOnline()) {
				result.add(anchor);
			}
		}
		return result;
	}

	public boolean isAnchor(IXQHXPlayer player) {
		return anchorMap.containsKey(player.getId());
	}

	public long getTotalEnterPower() {
		return totalEnterPower;
	}

	public int worldMonsterCount() {
		return monsterList.size();
	}

	public String getCampGuild(XQHX_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.campGuild;
		case B:
			return baseInfoB.campGuild;
		default:
			break;
		}

		return null;
	}

	public String getCampGuildTag(XQHX_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.campGuildTag;
		case B:
			return baseInfoB.campGuildTag;
		default:
			break;
		}

		return null;
	}

	public String getCampGuildName(XQHX_CAMP camp) {
		switch (camp) {
		case A:
			return baseInfoA.campGuildName;
		case B:
			return baseInfoB.campGuildName;
		default:
			break;
		}

		return null;
	}

	public XQHX_CAMP getGuildCamp(String guildId) {
		if (Objects.equals(guildId, baseInfoA.campGuild)) {
			return XQHX_CAMP.A;
		}
		return XQHX_CAMP.B;
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

	public void notifyMarchEventAsync(XQHXNotifyMarchEventFunc func) {
		noticyMarchEnentMap.put(func.getMarch().getMarchId(), func);
	}

	public XQHXGuildBaseInfo getCampBase(String guildId) {
		if (Objects.equals(guildId, baseInfoA.campGuild)) {
			return baseInfoA;
		}
		return baseInfoB;
	}

	public XQHXGuildBaseInfo getCampBase(XQHX_CAMP camp) {
		if (camp == baseInfoA.camp) {
			return baseInfoA;
		}
		return baseInfoB;
	}

	public XQHXGuildBaseInfo getBaseInfoA() {
		return baseInfoA;
	}

	public void setBaseInfoA(XQHXGuildBaseInfo baseInfoA) {
		this.baseInfoA = baseInfoA;
	}

	public XQHXGuildBaseInfo getBaseInfoB() {
		return baseInfoB;
	}

	public void setBaseInfoB(XQHXGuildBaseInfo baseInfoB) {
		this.baseInfoB = baseInfoB;
	}

}
