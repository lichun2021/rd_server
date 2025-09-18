package com.hawk.game.module.lianmengtaiboliya;

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
import com.hawk.game.module.lianmengtaiboliya.TBLYConst.TBLYState;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager.CAMP;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYBattleCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYBuffCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYChronoSphereCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYCommandCenterCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYCommandPostCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYFuelBankCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYHeadQuartersCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYIronCurtainDeviceCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYMilitaryBaseCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYNuclearMissileSiloCfg;
import com.hawk.game.module.lianmengtaiboliya.cfg.TBLYWeatherControllerCfg;
import com.hawk.game.module.lianmengtaiboliya.entity.TBLYMarchEntity;
import com.hawk.game.module.lianmengtaiboliya.msg.QuitReason;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrder10003;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderChrono;
import com.hawk.game.module.lianmengtaiboliya.order.TBLYOrderCollection;
import com.hawk.game.module.lianmengtaiboliya.player.ITBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.TBLYPlayer;
import com.hawk.game.module.lianmengtaiboliya.player.module.guildformation.TBLYGuildFormationObj;
import com.hawk.game.module.lianmengtaiboliya.roomstate.ITBLYBattleRoomState;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.ITBLYWorldMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYAssistanceSingleMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYAttackMonsterMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYAttackPlayerMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYBuildingMarchMass;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYBuildingMarchMassJoin;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYBuildingMarchSingle;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYBuildingMarchSingleNpc;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYCollectFuelMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYMassJoinSingleMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYMassSingleMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYNianMarchMass;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYNianMarchMassJoin;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYNianMarchSingle;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYNotifyMarchEventFunc;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.TBLYSpyMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYMassJoinMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYMassMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengtaiboliya.worldmarch.submarch.ITBLYReportPushMarch;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.ITBLYBuilding;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYBuildState;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYChronoSphere;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYCommandCenter;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYCommandPost;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYFuelBank;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYHeadQuarters;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYIronCurtainDevice;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYMilitaryBase;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYMonster;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYNuclearMissileSilo;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYPointUtil;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYTechnologyLab;
import com.hawk.game.module.lianmengtaiboliya.worldpoint.TBLYWeatherController;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.TBLY.PBGuildInfo;
import com.hawk.game.protocol.TBLY.PBPlayerInfo;
import com.hawk.game.protocol.TBLY.PBTBLYBuff;
import com.hawk.game.protocol.TBLY.PBTBLYGameInfoSync;
import com.hawk.game.protocol.TBLY.PBTBLYGameOver;
import com.hawk.game.protocol.TBLY.PBTBLYPlayerQuitRoom;
import com.hawk.game.protocol.TBLY.PBTBLYSecondMapResp;
import com.hawk.game.protocol.TBLY.PBTBLYTechonolgyLabEffect;
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
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
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
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;

/**
 * 虎牢关
 * 
 * @author lwt
 * @date 2018年10月26日
 */
public class TBLYBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	private TBLYExtraParam extParm;
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
	private Map<String, ITBLYPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, ITBLYPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	private Map<Integer, ITBLYWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private List<TBLYFuBankRefesh> fubankrefreshList = new ArrayList<>();
	private TBLYMonsterRefesh monsterrefresh;
	private TBLYNianRefesh nianrefresh;
	private Map<String, ITBLYWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private List<ITBLYBuilding> buildingList = new CopyOnWriteArrayList<>();
	private List<TBLYMonster> monsterList = new CopyOnWriteArrayList<>();
	private ITBLYBattleRoomState state;
	private long createTime;
	private long startTime;
	private long overTime;
	private TBLYBornPointRing bornPointAList;
	private TBLYBornPointRing bornPointBList;

	private TBLYOrderCollection campAorderCollection;
	private TBLYOrderCollection campBorderCollection;
	private TBLYOrderCollection campNoneorderCollection;

	private TBLYRandPointSeed pointSeed = new TBLYRandPointSeed();
	private String campAGuild = "";
	private String campAGuildName = "";
	private String campAGuildTag = "";
	private String campAServerId = "";
	private int campAguildFlag;
	private String campBGuild = "";
	private String campBGuildName = "";
	private String campBGuildTag = "";
	private String campBServerId = "";
	private int campBguildFlag;

	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBTBLYGameInfoSync lastSyncpb = PBTBLYGameInfoSync.getDefaultInstance();
	private Map<Long, TBLYBuffCfg> buff530Map = new HashMap<>();
	private TBLYBuffCfg curBuff530;
	// private long lastSyncMap;
	// private PBTBLYSecondMapResp lastMappb;

	private long nextLoghonor;

	private Multimap<Class<? extends ITBLYBuilding>, ? super ITBLYBuilding> tblyBuildingMap = HashMultimap.create();

	/** 地图上占用点 */
	private Set<Integer> occupationPointSet = new HashSet<>();

	private int campAGuildWarCount;
	private int campBGuildWarCount;

	public int campANuclearSendCount;
	public int campBNuclearSendCount;

	/**A击杀机甲数*/
	public int campANianKillCount;
	public int campBNianKillCount;
	/**额外悬赏分*/
	public int extryHonorA;
	public int extryHonorB;
	/**首站积分*/
	public int firstControlHonorA;
	public int firstControlHonorB;

	/** 号令点数*/
	public int campAOrder;
	public int campBOrder;

	public String first5000Honor;// 首无5000
	public String firstKillNian; // 首杀nian
	public String firstControlHeXin; // 首控制核心

	private Map<String, ITBLYPlayer> anchorMap = new HashMap<>();
	private String csServerId = "";
	private Set<String> csPlayerids = new HashSet<>();

	private Map<String, TBLYGuildFormationObj> guildFormationObjmap = new HashMap<>();
	private Map<String, PBPlayerInfo> notJoinPlayers = new HashMap<>();
	private ConcurrentSkipListMap<String, TBLYNotifyMarchEventFunc> noticyMarchEnentMap = new ConcurrentSkipListMap<>(); 
	public TBLYBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		TBLYProtocol pro = (TBLYProtocol) protocol;
		ITBLYPlayer player = pro.getPlayer();
		player.onProtocol(pro.getSource());

		return true;
	}

	public TBLYGuildFormationObj getGuildFormation(String guildId) { // TODO 包装成自己的
		if (guildFormationObjmap.containsKey(guildId)) {
			TBLYGuildFormationObj result = guildFormationObjmap.get(guildId);
			return result;
		}
		TBLYGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new TBLYGuildFormationObj());
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
		if (campAServerId.equals(csServerId)) {
			return campAGuild;
		}
		return campBGuild;
	}

	public int getCsGuildWarCount() {
		if (campAServerId.equals(csServerId)) {
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

	public void sync(ITBLYPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		PBTBLYGameInfoSync.Builder bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_GAME_SYNC, bul));

	}

	public boolean isHotBloodModel() {
		return overTime - curTimeMil < getCfg().getHotBloodModel() * 1000;
	}

	public PBTBLYGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBTBLYGameInfoSync.Builder bul = PBTBLYGameInfoSync.newBuilder();
		bul.setGameCreateTime(createTime);
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		PBGuildInfo.Builder aInfo = PBGuildInfo.newBuilder().setServerId(campAServerId).setCamp(CAMP.A.intValue()).setGuildFlag(campAguildFlag).setGuildName(campAGuildName)
				.setGuildTag(campAGuildTag).setGuildId(campAGuild).setExtryHonor(extryHonorA);
		PBGuildInfo.Builder bInfo = PBGuildInfo.newBuilder().setServerId(campBServerId).setCamp(CAMP.B.intValue()).setGuildFlag(campBguildFlag).setGuildName(campBGuildName)
				.setGuildTag(campBGuildTag).setGuildId(campBGuild).setExtryHonor(extryHonorB);

		for (Entry<Long, TBLYBuffCfg> ent : buff530Map.entrySet()) {
			bul.addTblyBuff(PBTBLYBuff.newBuilder().setBuffId(ent.getValue().getId()).setStartTime(ent.getKey()));
		}

		int honorA = extryHonorA; // 当前积分
		int perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		long centerControlA = 0; // TBLY_HEADQUARTERS 核心控制时间
		int buildControlHonorA = 0;
		int killHonorA = 0;
		int collectHonorA = 0;

		int honorB = extryHonorB; // 当前积分
		int perMinB = 0; // 每分增加
		int buildCountB = 0; // 占领建筑
		int playerCountB = 0; // 战场中人数
		long centerControlB = 0; // TBLY_HEADQUARTERS 核心控制时间
		int buildControlHonorB = 0;
		int killHonorB = 0;
		int collectHonorB = 0;

		int monsterCount = 0;
		for (ITBLYWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof ITBLYBuilding) {
				ITBLYBuilding build = (ITBLYBuilding) viewp;
				honorA += build.getControlGuildHonorMap().getOrDefault(campAGuild, 0D);
				honorB += build.getControlGuildHonorMap().getOrDefault(campBGuild, 0D);
				buildControlHonorA += build.getControlGuildHonorMap().getOrDefault(campAGuild, 0D);
				buildControlHonorB += build.getControlGuildHonorMap().getOrDefault(campBGuild, 0D);
				if (build.getPointType() == WorldPointType.TBLY_HEADQUARTERS) {
					centerControlA = build.getControlGuildTimeMap().get(campAGuild);
					centerControlB = build.getControlGuildTimeMap().get(campBGuild);
				}
				if (build.getState() == TBLYBuildState.ZHAN_LING) {
					if (Objects.equals(campAGuild, build.getGuildId())) {
						perMinA += Math.ceil(build.getGuildHonorPerSecond() * 60);
						buildCountA++;
					} else if (Objects.equals(campBGuild, build.getGuildId())) {
						perMinB += Math.ceil(build.getGuildHonorPerSecond() * 60);
						buildCountB++;
					}
				}
			}
			if (viewp instanceof TBLYFuelBank) {
				TBLYFuelBank build = (TBLYFuelBank) viewp;
				if (build.getMarch() != null) {
					double honorRate = build.getCfg().getGuildHonorRate();
					if (Objects.equals(campAGuild, build.getGuildId())) {
						perMinA += build.getMarch().getCollectSpeed() * honorRate * 60;
					} else if (Objects.equals(campBGuild, build.getGuildId())) {
						perMinB += build.getMarch().getCollectSpeed() * honorRate * 60;
					}
				}
			}
			if (viewp instanceof TBLYNuclearMissileSilo) {
				TBLYNuclearMissileSilo nucl = (TBLYNuclearMissileSilo) viewp;
				aInfo.setNuclearControlTime(nucl.getNuclearControlTime(campAGuild));
				aInfo.setNuclearReady(Objects.equals(campAGuild, nuclearReadGuild));
				aInfo.setNuclearBuildLeaderid(nuclearReadLeader);
				aInfo.setNuclearReadyTime(nucl.getReadyTime(campAGuild)); // 核弹发射OK时间
				aInfo.setNuclearStartTime(nucl.getStartTime());

				bInfo.setNuclearControlTime(nucl.getNuclearControlTime(campBGuild));
				bInfo.setNuclearReady(Objects.equals(campBGuild, nuclearReadGuild));
				bInfo.setNuclearBuildLeaderid(nuclearReadLeader);
				bInfo.setNuclearReadyTime(nucl.getReadyTime(campBGuild)); // 核弹发射OK时间
				bInfo.setNuclearStartTime(nucl.getStartTime());
			}

			if (viewp instanceof TBLYChronoSphere) {
				TBLYChronoSphere chrono = (TBLYChronoSphere) viewp;
				TBLYOrderChrono guildAorder = chrono.getGuildAorder();
				aInfo.setChronoControlTime(guildAorder.getControlTime());
				aInfo.setChronoReady(Objects.equals(campAGuild, chronoReadGuild));
				aInfo.setChronoBuildLeaderid(chronoReadLeader);
				aInfo.setChronoReadyTime(guildAorder.getReadyTime()); // 核弹发射OK时间
				aInfo.setChronoStartTime(guildAorder.getStartTime());

				TBLYOrderChrono guilBAorder = chrono.getGuildBorder();
				bInfo.setChronoControlTime(guilBAorder.getControlTime());
				bInfo.setChronoReady(Objects.equals(campBGuild, chronoReadGuild));
				bInfo.setChronoBuildLeaderid(chronoReadLeader);
				bInfo.setChronoReadyTime(guilBAorder.getReadyTime()); // 核弹发射OK时间
				bInfo.setChronoStartTime(guilBAorder.getStartTime());
			}
			if (viewp instanceof TBLYTechnologyLab) {
				for (PBTBLYTechonolgyLabEffect.Builder buf : ((TBLYTechnologyLab) viewp).getTechnologyLabBuffList()) {
					bul.addLabBuff(buf);
				}
			}

			if (viewp instanceof TBLYMonster) {
				monsterCount++;
			}
		}

		List<ITBLYPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (ITBLYPlayer p : all) {
			if (p.getCamp() == CAMP.A) {
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
			PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
			prc.setCamp(p.getCamp().intValue());
			prc.setName(p.getName());
			prc.setHonor(p.getHonor());
			prc.setGuildTag(p.getGuildTag());
			prc.setGuildHonor(p.getGuildHonor());
			prc.setPlayerId(p.getId());
			prc.setKillhonor(p.getKillHonor());
			prc.setHurthonor(p.getHurtHonor());
			prc.setBuildhonor(p.getBuildHonor());
			prc.setCollectGuildHonor(p.getCollectGuildHonor());
			prc.setKillMonster(p.getKillMonster());
			prc.setCityLevel(p.getCityLevel());
			prc.addAllDressShow(p.getShowDress());
			prc.setIcon(p.getIcon());
			prc.setPfIcon(p.getPfIcon());
			bul.addPlayerInfo(prc);
			notJoinPlayers.remove(p.getId());
		}
		
		bul.addAllPlayerNotJoin(notJoinPlayers.values());
		
		aInfo.setHonor(honorA).setPerMin(perMinA).setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setCenterControl(centerControlA).setBuildControlHonor(buildControlHonorA).setFirstControlHonor(firstControlHonorA)
				.setKillHonor(killHonorA).setCollectHonor(collectHonorA)
				.setNuclearCount(campANuclearSendCount)
				.setNianKillCnt(campANianKillCount)
				.setGuildOrder(campAOrder);
		bInfo.setHonor(honorB).setPerMin(perMinB).setBuildCount(buildCountB).setPlayerCount(playerCountB)
				.setCenterControl(centerControlB).setBuildControlHonor(buildControlHonorB).setFirstControlHonor(firstControlHonorB)
				.setKillHonor(killHonorB).setCollectHonor(collectHonorB)
				.setNuclearCount(campBNuclearSendCount)
				.setNianKillCnt(campBNianKillCount)
				.setGuildOrder(campBOrder);

		bul.setMonsterCount(monsterCount);
		bul.addGuildInfo(aInfo);
		bul.addGuildInfo(bInfo);
		
		long campAScore = aInfo.getHonor();
		long campBScore = bInfo.getHonor();
		CAMP winCamp = campAScore > campBScore ? CAMP.A : CAMP.B;
		bul.setWinCamp(winCamp.intValue());
		
		lastSyncpb = bul.build();
		lastSyncGame = curTimeMil;

		if (StringUtils.isEmpty(first5000Honor)) {
			if (honorA >= 5000) {
				first5000Honor = campAGuild;
			} else if (honorB >= 5000) {
				first5000Honor = campBGuild;
			}
		}

		return bul;
	}

	public void getSecondMap(ITBLYPlayer player) {
		PBTBLYSecondMapResp.Builder bul = PBTBLYSecondMapResp.newBuilder();
		for (ITBLYWorldPoint point : getViewPoints()) {
			if (point instanceof TBLYMonster) {
				continue;
			}
			if (point instanceof ITBLYPlayer && player.getCamp() != ((ITBLYPlayer) point).getCamp()) {
				continue;
			}

			if (point instanceof ITBLYBuilding) {
				ITBLYBuilding build = (ITBLYBuilding) point;
				bul.addAllOrderBuffs(build.getShowOrder().values());
			}

			bul.addPoints(point.toBuilder(player));
		}
		bul.setMonsterCount(worldMonsterCount());
		bul.setNextMonsterRefresh(monsterrefresh.nextRefreshTime());
		bul.setMonsterTurn(monsterrefresh.getRefreshTurn());
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_SECOND_MAP_S, bul));
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
			ITBLYPlayer pl = (ITBLYPlayer) ppp;
			double killPow = 0;
			int killHonor = 0;
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
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
			pl.setHurtTankHonor(pl.getHurtTankHonor() + killHonor);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			int killPow = 0;
			ITBLYPlayer pl = (ITBLYPlayer) ppp;
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

	/**主播请求小地图*/
	public void getAnchorSecondMap(ITBLYPlayer player) {
		PBTBLYSecondMapResp.Builder bul = PBTBLYSecondMapResp.newBuilder();
		for (ITBLYWorldPoint point : getViewPoints()) {
			if (point instanceof TBLYMonster) {
				continue;
			}
			bul.addPoints(point.toBuilder(player));
		}
		bul.setMonsterCount(worldMonsterCount());
		bul.setNextMonsterRefresh(monsterrefresh.nextRefreshTime());
		bul.setMonsterTurn(monsterrefresh.getRefreshTurn());
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_SECOND_MAP_S, bul));
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		campAGuild = extParm.getCampAGuild();
		campAGuildName = extParm.getCampAGuildName();
		campAGuildTag = extParm.getCampAGuildTag();
		campAServerId = extParm.getCampAServerId();
		campAguildFlag = extParm.getCampAguildFlag();

		campBGuild = extParm.getCampBGuild();
		campBGuildName = extParm.getCampBGuildName();
		campBGuildTag = extParm.getCampBGuildTag();
		campBServerId = extParm.getCampBServerId();
		campBguildFlag = extParm.getCampBguildFlag();

		nextSyncToPlayer = createTime + 5000;
		TBLYBattleCfg cfg = getCfg();
		startTime = createTime + cfg.getPrepairTime() * 1000;
		this.nextLoghonor = startTime + TBLYConst.MINUTE_MICROS;

		List<int[]> bornlist = cfg.copyOfbornPointAList();
		bornPointAList = new TBLYBornPointRing(bornlist.get(0), bornlist.get(1));

		bornlist = cfg.copyOfbornPointBList();
		bornPointBList = new TBLYBornPointRing(bornlist.get(0), bornlist.get(1));

		campAorderCollection = new TBLYOrderCollection(this, CAMP.A);
		campBorderCollection = new TBLYOrderCollection(this, CAMP.B);
		campNoneorderCollection = new TBLYOrderCollection(this, CAMP.NONE);
		getGuildFormation(campAGuild);
		getGuildFormation(campBGuild);

		int buildIndex = 1;
		{
			TBLYChronoSphereCfg buildcfg = TBLYChronoSphere.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYChronoSphere icd = new TBLYChronoSphere(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYCommandCenterCfg buildcfg = TBLYCommandCenter.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYCommandCenter icd = new TBLYCommandCenter(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYHeadQuartersCfg buildcfg = TBLYHeadQuarters.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYHeadQuarters icd = new TBLYHeadQuarters(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYIronCurtainDeviceCfg ironCfg = TBLYIronCurtainDevice.getCfg();
			for (HawkTuple2<Integer, Integer> rp : ironCfg.getRefreshPointList()) {
				TBLYIronCurtainDevice icd = new TBLYIronCurtainDevice(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYMilitaryBaseCfg buildcfg = TBLYMilitaryBase.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYMilitaryBase icd = new TBLYMilitaryBase(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYNuclearMissileSiloCfg buildcfg = TBLYNuclearMissileSilo.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYNuclearMissileSilo icd = new TBLYNuclearMissileSilo(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		{
			TBLYWeatherControllerCfg buildcfg = TBLYWeatherController.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYWeatherController icd = new TBLYWeatherController(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}
//		{
//			TBLYTechnologyLabCfg buildcfg = TBLYTechnologyLab.getCfg();
//			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
//				TBLYTechnologyLab icd = new TBLYTechnologyLab(this);
//				icd.setIndex(buildIndex);
//				icd.setX(rp.first);
//				icd.setY(rp.second);
//				icd.init();
//				viewPoints.put(icd.getPointId(), icd);
//				buildingList.add(icd);
//				tblyBuildingMap.put(icd.getClass(), icd);
//				buildIndex++;
//			}
//		}
		
		{
			TBLYCommandPostCfg buildcfg = TBLYCommandPost.getCfg();
			for (HawkTuple2<Integer, Integer> rp : buildcfg.getRefreshPointList()) {
				TBLYCommandPost icd = new TBLYCommandPost(this);
				icd.setIndex(buildIndex);
				icd.setX(rp.first);
				icd.setY(rp.second);
				icd.setNextMarch(getCreateTime() + TBLYCommandPost.getCfg().getNpcMarchFirst() * 1000);
				viewPoints.put(icd.getPointId(), icd);
				buildingList.add(icd);
				tblyBuildingMap.put(icd.getClass(), icd);
				buildIndex++;
			}
		}

		ConfigIterator<TBLYFuelBankCfg> fubankit = HawkConfigManager.getInstance().getConfigIterator(TBLYFuelBankCfg.class);
		for (TBLYFuelBankCfg fcfg : fubankit) {
			fubankrefreshList.add(TBLYFuBankRefesh.create(this, fcfg));
		}

		monsterrefresh = TBLYMonsterRefesh.create(this);

		nianrefresh = TBLYNianRefesh.create(this);

		{
			ConfigIterator<TBLYBuffCfg> bit = HawkConfigManager.getInstance().getConfigIterator(TBLYBuffCfg.class);
			Multimap<Integer, TBLYBuffCfg> buffMap = HashMultimap.create();
			for (TBLYBuffCfg bcfg : bit) {
				for (Integer t : bcfg.getRefreshTimeList()) {
					buffMap.put(t, bcfg);
				}
			}

			for (Entry<Integer, Collection<TBLYBuffCfg>> ent : buffMap.asMap().entrySet()) {
				List<TBLYBuffCfg> rlist = new LinkedList<>(ent.getValue());
				rlist.removeAll(buff530Map.values());
				buff530Map.put(createTime + ent.getKey() * 1000, HawkRand.randomWeightObject(rlist));
			}

		}
		
		for (TWPlayerData tdp : extParm.getCampAPlayers()) {
			try {
				PBPlayerInfo.Builder prc = PBPlayerInfo.newBuilder();
				prc.setCamp(CAMP.A.intValue());
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
				prc.setCamp(CAMP.B.intValue());
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

	public void onPlayerLogin(ITBLYPlayer gamer) {
		gamer.getPush().syncPlayerWorldInfo();
		gamer.getPush().syncPlayerInfo();
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<ITBLYWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (ITBLYWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				ITBLYWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof ITBLYPassiveAlarmTriggerMarch) {
				((ITBLYPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<ITBLYWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (ITBLYWorldMarch march : pointMs) {
			if (march instanceof ITBLYReportPushMarch) {
				((ITBLYReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

		for (ITBLYBuilding build : getTBLYBuildingList()) {
			build.onPlayerLogin(gamer);
		}
		// 号令同步
		syncOrder(gamer);
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

	public void doMoveCitySuccess(ITBLYPlayer player, int[] targetPoint) {
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
		for (ITBLYWorldMarch march : worldMarches.values()) {
			if (march instanceof ITBLYReportPushMarch) {
				((ITBLYReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		removeViewPoint(player);
		player.setPos(targetPoint);
		addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();
	}

	public ITBLYWorldMarch startMarch(ITBLYPlayer player, ITBLYWorldPoint fPoint, ITBLYWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		// 生成行军
		ITBLYWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
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
		List<ITBLYWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
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
	public ITBLYWorldMarch genMarch(ITBLYPlayer player, ITBLYWorldPoint fPoint, ITBLYWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		TBLYMarchEntity march = new TBLYMarchEntity();
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
		ITBLYWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new TBLYAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new TBLYSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new TBLYAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new TBLYMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new TBLYMassJoinSingleMarch(player);
			break;
		case COLLECT_RESOURCE:
			iWorldMarch = new TBLYCollectFuelMarch(player);
			break;
		case TBLY_HEADQUARTERS_SINGLE: // = 107; // 司令部
			if (player instanceof TBLYPlayer) {
				iWorldMarch = new TBLYBuildingMarchSingle(player);
				((TBLYBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			} else  {
				iWorldMarch = new TBLYBuildingMarchSingleNpc(player);
				((TBLYBuildingMarchSingleNpc) iWorldMarch).setMarchType(marchType);
			}
			break;

		case TBLY_HEADQUARTERS_MASS:// = 117; // 司令部
			iWorldMarch = new TBLYBuildingMarchMass(player);
			((TBLYBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.TBLY_HEADQUARTERS_MASS);
			((TBLYBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.TBLY_HEADQUARTERS_MASS_JOIN);
			break;

		case TBLY_HEADQUARTERS_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new TBLYBuildingMarchMassJoin(player);
			break;
		case NIAN_SINGLE:
			iWorldMarch = new TBLYNianMarchSingle(player);
			break;
		case NIAN_MASS:
			iWorldMarch = new TBLYNianMarchMass(player);
			break;
		case NIAN_MASS_JOIN:
			iWorldMarch = new TBLYNianMarchMassJoin(player);
			break;
		case ATTACK_MONSTER:
			iWorldMarch = new TBLYAttackMonsterMarch(player);
			break;
		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!!");
		}
		iWorldMarch.setMarchEntity(march);
		// marchId设置放在前面
		march.setMarchId(HawkOSOperator.randomUUID());

		// 行军时间
		long needTime = iWorldMarch.getMarchNeedTime();
		TBLYOrder10003 order10003 = getTBLYOrderCollection(player.getCamp()).getOrderById(TBLYOrderCollection.Order10003);
		if (order10003 != null && order10003.inEffect() && order10003.getTarget() == tPoint) {
			needTime = 3000;
		}
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
		if (iWorldMarch instanceof ITBLYMassJoinMarch) {
			iWorldMarch.setHashThread(((ITBLYMassJoinMarch) iWorldMarch).leaderMarch().get().getHashThread());
		} else {
			final int threadNum = HawkTaskManager.getInstance().getThreadNum();
			iWorldMarch.setHashThread(tPoint.getHashThread(threadNum));
		}
		return iWorldMarch;
	}

	public TBLYBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(TBLYBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public ITBLYWorldMarch getPlayerMarch(String playerId, String marchId) {
		ITBLYWorldMarch march = getMarch(marchId);
		if (march != null && Objects.equals(march.getPlayerId(), playerId)) {
			return march;
		}
		return null;
	}

	public List<ITBLYWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<ITBLYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<ITBLYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	@SuppressWarnings("unchecked")
	public <T extends ITBLYBuilding> List<T> getTBLYBuildingByClass(Class<T> type) {
		return new ArrayList<>((Collection<T>) tblyBuildingMap.get(type));
	}

	public List<ITBLYBuilding> getTBLYBuildingList() {
		return buildingList;
	}

	public List<ITBLYWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ITBLYWorldMarch> result = new ArrayList<>();
		for (ITBLYWorldMarch ma : worldMarches.values()) {
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

	public ITBLYWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<ITBLYWorldMarch> getGuildWarMarch(String guildId) {
		List<ITBLYWorldMarch> result = new ArrayList<>();
		for (ITBLYWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof TBLYBuildingMarchMass) {
					ITBLYBuilding point = (ITBLYBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof TBLYBuildingMarchSingle || march instanceof TBLYBuildingMarchSingleNpc) {
					ITBLYBuilding point = (ITBLYBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
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
				if (march instanceof TBLYNianMarchMass && !Objects.equals(march.getParent().getGuildId(), guildId)) {
					continue;
				}

				result.add(march);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return result;
	}

	public List<ITBLYWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<ITBLYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<ITBLYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<ITBLYWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ITBLYWorldMarch> result = new LinkedList<>();
		for (ITBLYWorldMarch ma : worldMarches.values()) {
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
	public Optional<ITBLYWorldPoint> getWorldPoint(int x, int y) {
		return getWorldPoint(GameUtil.combineXAndY(x, y));
	}

	public Optional<ITBLYWorldPoint> getWorldPoint(int pointId) {
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
				TBLYNotifyMarchEventFunc func = noticyMarchEnentMap.pollFirstEntry().getValue();
				func.apply(null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;
				PBTBLYGameInfoSync.Builder builder = buildSyncPB();
				// 夸服玩家和本服分别广播
				broadcastCrossProtocol(HawkProtocol.valueOf(HP.code.TBLY_GAME_SYNC, builder));
				for (ITBLYPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}

				// 发送主播
				for (ITBLYPlayer anchor : getAnchors()) {
					sync(anchor);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		fubankrefreshList.forEach(TBLYFuBankRefesh::onTick);
		monsterrefresh.onTick();
		nianrefresh.onTick();

		try {
			if (getCurTimeMil() > nextLoghonor) {
				this.nextLoghonor += TBLYConst.MINUTE_MICROS;
				List<PBGuildInfo> guildList = lastSyncpb.getGuildInfoList();
				for (PBGuildInfo ginfo : guildList) {
					String logGuild = ginfo.getCamp() == CAMP.A.intValue() ? campAGuild : campBGuild;
					LogUtil.logTBLYGuildHonor(getXid().getUUID(), logGuild, ginfo.getGuildName(), ginfo.getHonor());
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		campAorderCollection.onTick();
		campBorderCollection.onTick();
		getGuildFormation(campAGuild).checkMarchIdRemove();
		getGuildFormation(campBGuild).checkMarchIdRemove();

		try {
			for (Entry<Long, TBLYBuffCfg> ent : buff530Map.entrySet()) {
				long start = ent.getKey();
				TBLYBuffCfg bcfg = ent.getValue();
				if (start < curTimeMil && start + bcfg.getEffectTime() * 1000 > curTimeMil) {
					curBuff530 = bcfg;
					break;
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(ITBLYPlayer player) {
		state.enterWorld(player);
	}

	/**
	 * 副本中玩家更新点
	 * 
	 * @param point
	 */
	public void worldPointUpdate(ITBLYWorldPoint point) {
		for (ITBLYPlayer pla : getPlayerList(TBLYState.GAMEING)) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(pla));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}

		for (ITBLYPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(anchor));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(protocol);
		}
	}

	public void anchorJoinRoom(ITBLYPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || getPlayer(player.getId()) != null || isGameOver()) {
			return;
		}

		player.setCamp(CAMP.A);

		int[] bornP = new int[] { 75, 152 };
		player.setGuildId(campAGuild);
		player.setGuildTag(campAGuildTag);
		player.setGuildFlag(campAguildFlag);
		player.setGuildName(campAGuildName);

		player.setPos(bornP);

		player.init();
		player.getPush().syncGuildInfo();

		TBLYRoomManager.getInstance().cache(player);
		player.setTBLYState(TBLYState.GAMEING);

		{
			// 模拟 TiberiumWarService.getInstance().syncStateInfo(player);
			synAnchorPageInfo(player, true);
		}

		anchorMap.put(player.getId(), player);
		player.getPush().pushJoinGame();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_ENTER_GAME_SUCCESS));

		for (ITBLYBuilding build : getTBLYBuildingList()) {
			build.anchorJoin(player);
		}
	}

	public void synAnchorPageInfo(ITBLYPlayer anchor, boolean join) {
		try {
			TWPageInfo.Builder builder = TWPageInfo.newBuilder();
			TWStateInfo.Builder stateInfo = genStateInfo(join);

			builder.setIsSignUp(true);
			builder.setStateInfo(stateInfo);

			{
				TWGuildInfo.Builder selfbuilder = TWGuildInfo.newBuilder();
				selfbuilder.setId(campAGuild);
				selfbuilder.setName(campAGuildName);
				selfbuilder.setTag(campAGuildTag);
				selfbuilder.setGuildFlag(campAguildFlag);
				selfbuilder.setServerId(campAServerId);
				selfbuilder.setBattlePoint(9999);
				selfbuilder.setMemberCnt(50);

				builder.setSelfGuild(selfbuilder);
			}

			{
				TWGuildInfo.Builder oobuilder = TWGuildInfo.newBuilder();
				oobuilder.setId(campBGuild);
				oobuilder.setName(campBGuildName);
				oobuilder.setTag(campBGuildTag);
				oobuilder.setGuildFlag(campBguildFlag);
				oobuilder.setServerId(campBServerId);
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

	public void joinRoom(ITBLYPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || isGameOver()) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}
		notJoinPlayers.remove(player.getId());

		player.setCamp(Objects.equals(player.getGuildId(), campAGuild) ? CAMP.A : CAMP.B);

		int[] bornP = null;
		if (player.getCamp() == CAMP.A) {
			bornP = randomBornPoint(bornPointAList);
			player.setGuildId(campAGuild);
			player.setGuildTag(campAGuildTag);
			player.setGuildFlag(campAguildFlag);
			player.setGuildName(campAGuildName);
		} else {
			bornP = randomBornPoint(bornPointBList);
			player.setGuildId(campBGuild);
			player.setGuildTag(campBGuildTag);
			player.setGuildFlag(campBguildFlag);
			player.setGuildName(campBGuildName);
		}

		while (Objects.isNull(bornP)) {
			bornP = randomFreePoint(randomPoint(), WorldPointType.PLAYER);
		}

		player.setPos(bornP);

		player.init();
		TBLYRoomManager.getInstance().cache(player);
		player.setTBLYState(TBLYState.GAMEING);
		player.getPush().pushJoinGame();
		this.syncOrder(player);
		this.addViewPoint(player);
		playerMap.put(player.getId(), player);
		if (player.isCsPlayer()) {
			csPlayerids.add(player.getId());
			csServerId = player.getMainServerId();
		}
		totalEnterPower += player.getPower();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {} guildId {}", getId(), player.getGuildId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public int[] randomPoint() {
		ITBLYBuilding build = HawkRand.randomObject(buildingList);
		return GameUtil.splitXAndY(build.getPointId());
	}

	public void quitWorld(ITBLYPlayer quitPlayer, QuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBTBLYGameOver.Builder builder = PBTBLYGameOver.newBuilder();
			if (reason == QuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_GAME_OVER, builder));
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

			for (ITBLYPlayer gamer : playerMap.values()) {
				PBTBLYPlayerQuitRoom.Builder bul = PBTBLYPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_PLAYER_QUIT, bul));
			}

			quitPlayer.getData().getQueueEntities().clear();
		}

		// if (reason == QuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_TBLY).setKey(NoticeCfgId.TBLY_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();
		csPlayerids.remove(quitPlayer.getId());

	}

	public void cleanCityPointMarch(ITBLYPlayer quitPlayer) {
		try {
			List<ITBLYWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (ITBLYWorldMarch march : quiterMarches) {
				if (march instanceof ITBLYMassMarch) {
					march.getMassJoinMarchs(true).forEach(ITBLYWorldMarch::onMarchCallback);
				}
				if (march instanceof ITBLYMassJoinMarch) {
					Optional<ITBLYMassMarch> massMarch = ((ITBLYMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						ITBLYMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(ITBLYWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<ITBLYWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (ITBLYWorldMarch march : pms) {
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

	public List<ITBLYPlayer> getPlayerList(TBLYState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<ITBLYPlayer> getPlayerList(TBLYState st1, TBLYState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<ITBLYPlayer> getPlayerList(TBLYState st1, TBLYState st2, TBLYState st3) {
		List<ITBLYPlayer> result = new ArrayList<>();
		for (ITBLYPlayer player : playerMap.values()) {
			TBLYState state = player.getTBLYState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public ITBLYPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public List<ITBLYPlayer> getCampPlayers(CAMP camp) {
		List<ITBLYPlayer> list = new ArrayList<>();
		for (ITBLYPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void setPlayerList(List<ITBLYPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	/** 添加世界点 */
	public void addViewPoint(ITBLYWorldPoint... vp) {
		List<ITBLYWorldPoint> list = new ArrayList<>();
		for (ITBLYWorldPoint point : vp) {
			if (point != null) {
				list.add(point);
			}
		}

		for (ITBLYWorldPoint point : list) {
			if (point instanceof TBLYMonster) {
				monsterList.add((TBLYMonster) point);
			}
			viewPoints.put(point.getPointId(), point);
		}

		resetOccupationPoint();

		for (ITBLYPlayer player : playerMap.values()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ITBLYWorldPoint point : list) {
				builder.addPoints(point.toBuilder(player));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			player.sendProtocol(pp);
		}
		for (ITBLYPlayer anchor : getAnchors()) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			for (ITBLYWorldPoint point : list) {
				builder.addPoints(point.toBuilder(anchor));
			}
			HawkProtocol pp = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			anchor.sendProtocol(pp);
		}
	}

	public boolean removeViewPoint(ITBLYWorldPoint vp) {
		boolean result = Objects.nonNull(viewPoints.remove(vp.getPointId()));
		if (vp instanceof TBLYMonster) {
			monsterList.remove(vp);
		}
		for (ITBLYPlayer gamer : playerMap.values()) {
			// 删除点
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.setIsRemove(true);
			builder.addPoints(vp.toBuilder(gamer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
		for (ITBLYPlayer anchor : getAnchors()) {
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
		for (ITBLYWorldPoint viewp : viewPoints.values()) {
			viewp.fillWithOcuPointId(set);
		}
		occupationPointSet = set;
	}

	public List<ITBLYWorldPoint> getViewPoints() {
		return new ArrayList<>(viewPoints.values());
	}

	public void setViewPoints(List<ITBLYWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<ITBLYWorldMarch> getWorldMarchList() {
		ArrayList<ITBLYWorldMarch> result = new ArrayList<>(worldMarches.values());
		return result;
	}

	public void removeMarch(ITBLYWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(ITBLYWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<ITBLYWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public ITBLYBattleRoomState getState() {
		return state;
	}

	public void setState(ITBLYBattleRoomState state) {
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

	public TBLYExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(TBLYExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(ITBLYPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber()).setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo, null);

	}

	public void addWorldBroadcastMsg(ChatParames parames, CAMP camp) {
		broadcastChatMsg(parames.toPBMsg(), camp);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		broadcastChatMsg(parames.toPBMsg(), null);
	}

	private void broadcastChatMsg(ChatMsg pbMsg, CAMP camp) {
		Set<Player> tosend = new HashSet<>(getPlayerList(TBLYState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg) || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE || Objects.nonNull(camp);
		if (isGuildMsg) {
			String guildId = Objects.nonNull(camp) ? getCampGuild(camp) : pbMsg.getAllianceId();
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), guildId)).collect(Collectors.toSet());
		} else {
			for (ITBLYPlayer anchor : getAnchors()) {
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

	public int[] randomFreePoint(int[] popBornPointA, WorldPointType pointType) {
		TBLYBattleCfg bcfg = getCfg();
		int x;
		int y;
		for (int i = 0; i < pointSeed.size(); i++) {
			int[] pp = pointSeed.nextPoint();
			x = popBornPointA[0] + pp[0];
			y = popBornPointA[1] + pp[1];

			int pointRedis = TBLYPointUtil.pointRedis(pointType);
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

			Set<Integer> set = TBLYPointUtil.getOcuPointId(x, y, pointRedis);
			set.retainAll(occupationPointSet);
			if (!set.isEmpty()) {
				continue;
			}
			return new int[] { x, y };
		}
		return null;
	}

	public int[] randomBornPoint(TBLYBornPointRing pointRing) {
		int x;
		int y;
		for (int i = 0; i < pointRing.size(); i++) {
			int[] pp = pointRing.nextPoint();
			x = pp[0];
			y = pp[1];

			int pointRedis = TBLYPointUtil.pointRedis(WorldPointType.PLAYER);
			if ((x + y) % 2 == pointRedis % 2) {
				x += 1;
			}

			Set<Integer> set = TBLYPointUtil.getOcuPointId(x, y, pointRedis);
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

	public boolean checkPlayerCanOccupy(ITBLYPlayer player, int x, int y) {
		int pointRedis = TBLYPointUtil.pointRedis(WorldPointType.PLAYER);
		Set<Integer> set = TBLYPointUtil.getOcuPointId(x, y, pointRedis);
		set.removeAll(TBLYPointUtil.getOcuPointId(player.getX(), player.getY(), pointRedis));

		set.retainAll(occupationPointSet);
		if (!set.isEmpty()) {
			return false;
		}

		return true;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public String getCampAGuild() {
		return campAGuild;
	}

	public void setCampAGuild(String campAGuild) {
		this.campAGuild = campAGuild;
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

	public List<ITBLYPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public String getCsServerId() {
		return csServerId;
	}

	public Set<String> getCsPlayerids() {
		return csPlayerids;
	}

	public PBTBLYGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBTBLYGameInfoSync lastSyncpb) {
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

	public List<ITBLYPlayer> getAnchors() {
		if (anchorMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<ITBLYPlayer> result = new ArrayList<>(anchorMap.size());
		for (ITBLYPlayer anchor : anchorMap.values()) {
			if (Objects.nonNull(anchor) && anchor.isActiveOnline()) {
				result.add(anchor);
			}
		}
		return result;
	}

	public boolean isAnchor(ITBLYPlayer player) {
		return anchorMap.containsKey(player.getId());
	}

	public long getTotalEnterPower() {
		return totalEnterPower;
	}

	public TBLYOrderCollection getTBLYOrderCollection(CAMP camp) {
		if (CAMP.A == camp) {
			return this.campAorderCollection;
		}
		if (CAMP.B == camp) {
			return this.campBorderCollection;
		}
		return campNoneorderCollection;
	}

	public int getOrderEffect(CAMP camp, EffType type) {
		TBLYOrderCollection collection = this.getTBLYOrderCollection(camp);
		return collection.getEffectVal(type);
	}

	public void syncOrder(ITBLYPlayer player) {
		TBLYOrderCollection orderCollection = this.getTBLYOrderCollection(player.getCamp());
		if (orderCollection != null) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TBLY_ORDER_SYNC_S_VALUE,
					orderCollection.genPBTBLYOrderSyncRespBuilder()));
		}
	}

	public int worldMonsterCount() {
		return monsterList.size();
	}

	public int getCampOrder(CAMP camp) {
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

	public String getCampGuild(CAMP camp) {
		switch (camp) {
		case A:
			return campAGuild;
		case B:
			return campBGuild;
		default:
			break;
		}

		return null;
	}

	public String getCampGuildTag(CAMP camp) {
		switch (camp) {
		case A:
			return campAGuildTag;
		case B:
			return campBGuildTag;
		default:
			break;
		}

		return null;
	}

	public String getCampGuildName(CAMP camp) {
		switch (camp) {
		case A:
			return campAGuildName;
		case B:
			return campBGuildName;
		default:
			break;
		}

		return null;
	}

	public CAMP getGuildCamp(String guildId) {
		if (Objects.equals(guildId, campAGuild)) {
			return CAMP.A;
		}
		return CAMP.B;
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

	public String getCampAGuildName() {
		return campAGuildName;
	}

	public String getCampBGuildName() {
		return campBGuildName;
	}

	public int getCurBuff530Val(EffType eff) {
		if (curBuff530 == null) {
			return 0;
		}
		return curBuff530.getEffectList().getOrDefault(eff, 0);
	}
	
	public void notifyMarchEventAsync(TBLYNotifyMarchEventFunc func){
		noticyMarchEnentMap.put(func.getMarch().getMarchId(), func);
	}
}
