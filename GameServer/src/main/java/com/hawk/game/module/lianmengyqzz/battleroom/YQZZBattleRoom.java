package com.hawk.game.module.lianmengyqzz.battleroom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
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
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.module.lianmengyqzz.battleroom.YQZZConst.YQZZState;
import com.hawk.game.module.lianmengyqzz.battleroom.cfg.YQZZBattleCfg;
import com.hawk.game.module.lianmengyqzz.battleroom.entity.YQZZMarchEntity;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZExtraParam;
import com.hawk.game.module.lianmengyqzz.battleroom.extra.YQZZNation;
import com.hawk.game.module.lianmengyqzz.battleroom.msg.YQZZQuitReason;
import com.hawk.game.module.lianmengyqzz.battleroom.order.YQZZOrderCollection;
import com.hawk.game.module.lianmengyqzz.battleroom.player.IYQZZPlayer;
import com.hawk.game.module.lianmengyqzz.battleroom.player.according.YQZZBattleStatics;
import com.hawk.game.module.lianmengyqzz.battleroom.player.module.guildformation.YQZZGuildFormationObj;
import com.hawk.game.module.lianmengyqzz.battleroom.roomstate.IYQZZBattleRoomState;
import com.hawk.game.module.lianmengyqzz.battleroom.secondmap.YQZZSecondMap;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.IYQZZWorldMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZAssistanceSingleMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZAttackMonsterMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZAttackPlayerMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZBuildingMarchMass;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZBuildingMarchMassJoin;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZBuildingMarchSingle;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZCollectResMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZMassJoinSingleMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZMassSingleMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZPylonMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.YQZZSpyMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy.YQZZNianMarchMass;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy.YQZZNianMarchMassJoin;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.foggy.YQZZNianMarchSingle;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZMassJoinMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZMassMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldmarch.submarch.IYQZZReportPushMarch;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.IYQZZBuilding;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBase;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildState;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildType;
import com.hawk.game.module.lianmengyqzz.battleroom.worldpoint.YQZZBuildingHonor;
import com.hawk.game.module.lianmengyqzz.march.cfg.YQZZWarConstCfg;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Chat.HPChatState;
import com.hawk.game.protocol.Common.RedType;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.YQZZ.PBYQZZGameInfoSync;
import com.hawk.game.protocol.YQZZ.PBYQZZGameOver;
import com.hawk.game.protocol.YQZZ.PBYQZZGuildInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZNationInfo;
import com.hawk.game.protocol.YQZZ.PBYQZZPlayerQuitRoom;
import com.hawk.game.protocol.YQZZ.PBYQZZSecondMapResp;
import com.hawk.game.protocol.YQZZ.YQZZDeclareWarUseResp;
import com.hawk.game.protocol.YQZZ.YQZZGaiLanResp;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarch;
import com.hawk.game.world.WorldMarchService;

/**
 * 虎牢关
 * 
 * @author lwt
 * @date 2018年10月26日
 */
public class YQZZBattleRoom extends HawkAppObj {
	public boolean IS_GO_MODEL;
	final int MINITEMILLSECS = 66 * 1000;
	private YQZZThread thread;
	/** 结算信息 */
	private YQZZExtraParam extParm;
	private transient boolean gameOver;
	private int battleCfgId;
	private long curTimeMil;
	/** 上次同步战场详情 */
	private long lastSyncGame;
	private String winGuild;
	/** 游戏内玩家 不包含退出的 */
	private Map<String, IYQZZPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, IYQZZPlayer> playerQuitMap = new ConcurrentHashMap<>();
	// /** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	// private Map<Integer, IYQZZWorldPoint> viewPoints = new ConcurrentHashMap<>();

	private Map<String, IYQZZWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private IYQZZBattleRoomState state;
	private long createTime;
	private long gameStartTime;
	private long overTime;
	private YQZZWorldPointService worldPointService;
	private YQZZBattleStageTime battleStageTime;
	/** 下次主动推详情 */
	private long nextSyncToPlayer;
	/** 上次同步战场详情 */
	private PBYQZZGameInfoSync lastSyncpb;
	private PBYQZZGameInfoSync.Builder lastSyncpbPlayer;

	// private long lastSyncMap;
	// private PBYQZZSecondMapResp lastMappb;

	private YQZZSecondMap secondMap;
	private long nextLoghonor;

	private Map<String, YQZZGuildBaseInfo> guildStatisticMap = new HashMap<>();
	public Deque<ChatMsg> roomMsgCache = new ConcurrentLinkedDeque<>();
	
	private boolean hasNotBrodcast_YQZZ_182 = true;
	private boolean hasNotBrodcast_YQZZ_183 = true;
	private Map<String, YQZZGuildFormationObj> guildFormationObjmap = new HashMap<>();
	private int centerX, centerY;

	public YQZZBattleRoom(HawkXID xid) {
		super(xid);
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - curTimeMil < 3000;
	}

	public void onPlayerLogin(IYQZZPlayer gamer) {
		try {
			gamer.getPush().syncPlayerWorldInfo();
			gamer.getPush().syncGuildInfo();
			gamer.getPush().syncPlayerInfo();
			// 组装玩家自己的行军PB数据
			WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
			List<IYQZZWorldMarch> marchs = getPlayerMarches(gamer.getId());
			for (IYQZZWorldMarch worldMarch : marchs) {
				builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

				WorldMarch march = worldMarch.getMarchEntity();
				if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
					IYQZZWorldMarch massMach = getMarch(march.getTargetId());
					// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
					if (massMach != null) {
						builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
					}
				}

				if (worldMarch instanceof IYQZZPassiveAlarmTriggerMarch) {
					((IYQZZPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
				}
			}

			List<IYQZZWorldMarch> pointMs = getPointMarches(gamer.getPointId());
			for (IYQZZWorldMarch march : pointMs) {
				if (march instanceof IYQZZReportPushMarch) {
					((IYQZZReportPushMarch) march).pushAttackReport(gamer.getId());
				}
			}
			// 通知客户端
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
			gamer.moveCityCDSync();

			syncOrder(gamer);
			gamer.getPush().syncPlayerEffect(EffType.CITY_HURT_NUM, EffType.PLANT_SOLDIER_4101);
			
			sendChatCahce(gamer);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void sendChatCahce(IYQZZPlayer gamer) {
		try {
			Set<Player> tosend = new HashSet<>(1);
			tosend.add(gamer);
			HPChatState chatState = gamer.getChatState();
			gamer.setChatState(HPChatState.CHAT);
			ChatService.getInstance().sendChatMsg(getCampBase(gamer.getGuildId()).guldMsgCache.stream().collect(Collectors.toList()), tosend);
			ChatService.getInstance().sendChatMsg(getBaseByCamp(getNationInfo(gamer.getMainServerId()).getCamp()).nationMsgCache.stream().collect(Collectors.toList()), tosend);
			ChatService.getInstance().sendChatMsg(roomMsgCache.stream().collect(Collectors.toList()), tosend);
			gamer.setChatState(chatState);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		if (isGameOver()) {
			return false;
		}
		return super.onProtocol(protocol);
	}

	public void sync(IYQZZPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_GAME_SYNC, lastSyncpbPlayer));
			return;
		}

		buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_GAME_SYNC, lastSyncpbPlayer));

	}

	public void syncOrder(IYQZZPlayer player) {
		YQZZOrderCollection orderCollection = getWorldPointService().getBaseByCamp(player.getCamp()).getOrderCollection();
		if (orderCollection == null) {
			return;
		}
		orderCollection.syncOrder(player);
	}

	public int getGuildMemberCount(String guildId) {
		if (guildStatisticMap.containsKey(guildId)) {
			return guildStatisticMap.get(guildId).playerIds.size();
		}
		return 0;
	}

	public PBYQZZGameInfoSync buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb;
		}
		long beginTimeMs = HawkTime.getMillisecond();

		PBYQZZGameInfoSync.Builder bul = PBYQZZGameInfoSync.newBuilder();
		bul.setGameStartTime(gameStartTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		bul.setMonsterCount(worldMonsterCount());
		List<IYQZZPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (IYQZZPlayer p : all) {
			bul.addPlayerInfo(p.genPBYQZZPlayerInfo());
		}

		for (YQZZGuildBaseInfo gbi : guildStatisticMap.values()) {
			try {
				bul.addGuildInfo(buildGuildStaticInfo(gbi));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		for (String serverId : extParm.getServerCamp().keySet()) {
			try {
				bul.addNationInfo(buildNationStaticInfo(serverId));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		lastSyncpb = bul.build();

		PBYQZZGameInfoSync.Builder builder = PBYQZZGameInfoSync.newBuilder();
		builder.addAllNationInfo(lastSyncpb.getNationInfoList());
		builder.setGameStartTime(lastSyncpb.getGameStartTime());
		builder.setGameOverTime(lastSyncpb.getGameOverTime());
		for (PBYQZZGuildInfo ginfo : lastSyncpb.getGuildInfoList()) {
			builder.addGuildInfo(ginfo.toBuilder().clearBuildHonors());
		}
		lastSyncpbPlayer = builder;

		lastSyncGame = curTimeMil;

		long costTimeMs = HawkTime.getMillisecond() - beginTimeMs;
		if (costTimeMs > 50) {
			DungeonRedisLog.log(getId(), "process tick too much time, costtime: {}", costTimeMs);
		}
		return lastSyncpb;
	}

	public int worldMonsterCount() {
		return worldPointService.worldMonsterCount();
	}

	public int worldFoggyCount() {
		return worldPointService.worldFoggyCount();
	}

	private boolean isHotBloodModel() {
		// TODO Auto-generated method stub
		return false;
	}

	private PBYQZZNationInfo.Builder buildNationStaticInfo(String serverId) {
		PBYQZZNationInfo.Builder builder = PBYQZZNationInfo.newBuilder();
		builder.setServerId(serverId);
		YQZZNation nationInfo = getNationInfo(serverId);
		builder.setCamp(nationInfo.getCamp().intValue());
		builder.setNationLeaderId(nationInfo.getPresidentId());
		builder.setNationLeaderName(nationInfo.getPresidentName());
		int honorA = 0;
		int buildPlayerHonor = 0;
		int pylonCnt = 0;
		Set<YQZZBuildType> controlBuildTypes = new HashSet<>();
		for (YQZZGuildBaseInfo buildcamp : guildStatisticMap.values()) {
			if (Objects.equals(serverId, buildcamp.campServerId)) {
				honorA += buildcamp.buildNationHonor;
				honorA += buildcamp.playerNationHonor;
				buildPlayerHonor += buildcamp.buildPlayerHonor;
				pylonCnt += buildcamp.pylonCnt;
				controlBuildTypes.addAll(buildcamp.controlBuildTypes);
			}
		}
		YQZZBase baseByCamp = getBaseByCamp(nationInfo.getCamp());
		baseByCamp.setNationPlayerHonor(buildPlayerHonor);
		baseByCamp.controlBuildTypes = controlBuildTypes;
		baseByCamp.pylonCnt = pylonCnt;
		
		builder.setNationHonor(honorA);
		builder.setNationSpaceLevel(nationInfo.getNationLevel());

		IYQZZPlayer presient = getPlayer(nationInfo.getPresidentId());
		if (presient != null) {
			builder.setGiveupBuildCd(presient.getGiveupBuildCd());
		}
		builder.setPylonCnt(pylonCnt);
		builder.setNationTechValue(baseByCamp.getNationTechValue());
		return builder;
	}

	/**取得阵营国家飞船*/
	public YQZZBase getBaseByCamp(YQZZ_CAMP camp) {
		return worldPointService.getBaseByCamp(camp);
	}

	private PBYQZZGuildInfo.Builder buildGuildStaticInfo(YQZZGuildBaseInfo buildcamp) {
		PBYQZZGuildInfo.Builder aInfo = PBYQZZGuildInfo.newBuilder()
				.setServerId(buildcamp.campServerId)
				.setCamp(buildcamp.camp.intValue())
				.setGuildFlag(buildcamp.campguildFlag)
				.setGuildName(buildcamp.campGuildName)
				.setGuildTag(buildcamp.campGuildTag)
				.setGuildId(buildcamp.campGuild)
				.setLeaderName(buildcamp.guildLeaderName)
				.setLeaderId(buildcamp.guildLeaderId)
				.setTeamName(buildcamp.campTeamName)
				.setTeamPower(buildcamp.campTeamPower)
				.setGuildOrder(buildcamp.declareWarPoint)
				.setNextDeclareWarPoint(buildcamp.lastDeclareWarPoint + buildcamp.declareWarPointSpeed)
				.setDeclareWarPointSpeed(buildcamp.declareWarPointSpeed);

		int honorA = buildcamp.campNianATKHonor; // 当前积分
		int perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		long centerControlA = 0; // YQZZ_HEADQUARTERS 核心控制时间
		int buildControlHonorA = 0;
		int killHonorA = 0;
		int collectHonorA = 0;
		int pylonCnt = 0;
		int killMonster = 0;

		List<IYQZZPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		Set<String> playerIds = new HashSet<>();
		Set<Integer> controlBuildIds = new HashSet<>();
		Set<YQZZBuildType> controlBuildTypes = new HashSet<>();
		Map<EffType, Integer> battleEffVal = new HashMap<>();
		buildcamp.campGuildWarCount = getGuildWarMarch(buildcamp.campGuild).size();

		int declearWarRed = 0;
		int buildPlayerHonor = 0;
		int buildGuildHonor = 0;
		int buildNationHonor = 0;
		int playerNationHonor = 0;
		for (IYQZZBuilding build : getWorldPointService().getBuildingList()) {
			if (build.underGuildControl(buildcamp.campGuild)) {
				declearWarRed += build.getDeclareWar().size();
				buildCountA++;
				controlBuildIds.add(build.getCfgId());
				controlBuildTypes.add(build.getBuildType());
			} else if (build.getDeclareWarRecord(buildcamp.campGuild).isPresent()) {
				declearWarRed++;
			}
			if (build.getState() == YQZZBuildState.ZHAN_LING && build.underGuildControl(buildcamp.campGuild)) {
				perMinA += build.getGuildHonorPerSecond() * 60 + 0.1;
				for (HawkTuple2<EffType, Integer> hw2 : build.getBuildTypeCfg().getControlebuffList()) {
					battleEffVal.merge(hw2.first, hw2.second, (v1, v2) -> v1 + v2);
				}
			}
			if (build.underGuildControl(buildcamp.campGuild)) {
				buildNationHonor += build.getNationHonorLastScore();
			}
			YQZZBuildingHonor bhonor = build.getBuildingHonor(buildcamp.campGuild);
			if (Objects.nonNull(bhonor) && (bhonor.getGuildHonor() + bhonor.getFirstControlGuildHonor()) > 0) {
				aInfo.addBuildHonors(bhonor.toPBObj());
				buildPlayerHonor += bhonor.getPlayerHonor() + bhonor.getFirstControlPlayerHonor();
				buildGuildHonor += bhonor.getGuildHonor() + bhonor.getFirstControlGuildHonor();
				buildNationHonor += bhonor.getNationHonor() + bhonor.getFirstControlNationHonor();
			}
		}
		honorA += buildGuildHonor;
		buildControlHonorA += buildGuildHonor;
		buildcamp.buildPlayerHonor = buildPlayerHonor;
		buildcamp.buildGuildHonor = buildGuildHonor;
		buildcamp.buildNationHonor = buildNationHonor;

		for (IYQZZPlayer p : all) {
			if (!Objects.equals(p.getGuildId(), buildcamp.campGuild)) {
				continue;
			}
			playerNationHonor += p.getNationHonor();
			honorA += p.getGuildHonor();
			killHonorA += p.getKillHonor();
			// 采集分 collectHonorA += p.getGuildHonor();
			killMonster += p.getKillMonster();
			if (p.getYQZZState() == YQZZState.GAMEING) {
				playerCountA++;
				playerIds.add(p.getId());
			}
			pylonCnt += p.getCollPylon();
		}
		buildcamp.playerNationHonor = playerNationHonor;
		buildcamp.playerIds = playerIds;
		buildcamp.controlBuildIds = controlBuildIds;
		buildcamp.controlBuildTypes = controlBuildTypes;
		buildcamp.battleEffVal = ImmutableMap.copyOf(battleEffVal);
		buildcamp.pylonCnt = pylonCnt;
		aInfo.setHonor(honorA).setPerMin(perMinA).setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setCenterControl(centerControlA).setBuildControlHonor(buildControlHonorA)
				.setKillHonor(killHonorA).setCollectHonor(collectHonorA)
				.setNuclearCount(buildcamp.campNuclearSendCount)
				.setNianKillCnt(buildcamp.campNianKillCount)
				.setKillMonster(killMonster)
				.addAllControlBuildId(buildcamp.controlBuildIds)
				.setDeclearWarRed(declearWarRed)
				.setPylonCnt(pylonCnt);

		return aInfo;
	}

	public PBYQZZSecondMapResp.Builder getSecondMap(String guildId) {
		return secondMap.getSecondMap(guildId);

		// if (curTimeMil - lastSyncMap < 3000 && Objects.nonNull(lastMappb)) {
		// player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SECOND_MAP_S, lastMappb.toBuilder()));
		// return;
		// }
		// PBYQZZSecondMapResp.Builder bul = PBYQZZSecondMapResp.newBuilder();
		// for (IYQZZWorldPoint point : getViewPoints()) {
		// if(point instanceof IYQZZBuilding || point instanceof IYQZZPlayer){
		// bul.addPoints(point.toBuilder(player));
		// }
		// }
		// bul.setGameStartTime(gameStartTime);
		// bul.setGameOverTime(overTime);
		//
		// bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		// player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_SECOND_MAP_S, bul));
		//
		// lastMappb = bul.build();
		// lastSyncMap = curTimeMil;
	}

	public void calcKillAndHurtPower(BattleOutcome battleOutcome, List<Player> atkPlayers, List<Player> defPlayers) {
		Map<String, Map<Integer, Integer>> atkKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> atkHurtMap = new HashMap<>();
		BattleService.getInstance().calcKillAndHurtInfo(atkKillMap, atkHurtMap, battleOutcome.getBattleArmyMapAtk(), battleOutcome.getBattleArmyMapDef());
		calcKillAndHurtPower(atkPlayers, atkKillMap, atkHurtMap, defPlayers.get(0).getMainServerId());

		Map<String, Map<Integer, Integer>> defKillMap = new HashMap<>();
		Map<String, Map<Integer, Integer>> defHurtMap = new HashMap<>();
		BattleService.getInstance().calcKillAndHurtInfo(defKillMap, defHurtMap, battleOutcome.getBattleArmyMapDef(), battleOutcome.getBattleArmyMapAtk());
		calcKillAndHurtPower(defPlayers, defKillMap, defHurtMap, atkPlayers.get(0).getMainServerId());

		// 攻击方剩余兵力
		// 计算损失兵力
		calcSelfLosePower(atkPlayers, battleOutcome.getBattleArmyMapAtk());
		// 防守方剩余兵力
		// 计算损失兵力
		calcSelfLosePower(defPlayers, battleOutcome.getBattleArmyMapDef());

		// // 计算国家医院
		// calcNationalHospital(atkPlayers, battleOutcome.getBattleArmyMapAtk());
		// // 防守方剩余兵力
		// // 计算损失兵力
		// calcNationalHospital(defPlayers, battleOutcome.getBattleArmyMapDef());
	}

	// /**计算国家医院收治*/
	// private void calcNationalHospital(List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
	// for (Player ppp : battlePlayers) {
	// IYQZZPlayer pl = (IYQZZPlayer) ppp;
	// List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
	// if (leftList == null) {
	// continue;
	// }
	// final int dead = pl.getDeadCnt();
	// int healRate = 10000;
	//// int healRate = HawkConfigManager.getInstance().getConfigIterator(YQZZHealCfg.class).stream()
	//// .filter(cfg -> cfg.getDeadNumMin() <= dead && cfg.getDeadNumMax() >= dead)
	//// .mapToInt(cfg -> cfg.getHealRate())
	//// .findAny()
	//// .orElse(10000);
	//
	// double savePct = healRate * GsConst.EFF_PER;
	// for (ArmyInfo army : leftList) {// 进医院的数量
	// BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
	// if (cfg.getSoldierType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE) { // 兵种1 -8
	// army.setTszzNationalHospital((int) (army.getDeadCount() * savePct));
	// }
	// }
	// HawkApp.getInstance().postMsg(ppp, CalcSWDeadArmy.valueOf(leftList));
	// }
	// }

	private void calcSelfLosePower(List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
		for (Player ppp : battlePlayers) {
			IYQZZPlayer pl = (IYQZZPlayer) ppp;
			int killPow = 0;
			int deadCnt = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				if (cfg.getSoldierType() == SoldierType.TANK_SOLDIER_1) {
					killPow += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
				}
				if (cfg.getLevel() > getCfg().getHospitalMinLevel() && cfg.getSoldierType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE) { // 兵种1 -8
					deadCnt += army.getDeadCount();
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
			pl.setDeadCnt(pl.getDeadCnt() + deadCnt);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap,
			String tarServerId) {
		for (Player ppp : battlePlayers) {
			IYQZZPlayer pl = (IYQZZPlayer) ppp;
			int killPow = 0;
			{
				Map<Integer, Integer> killMap = battleKillMap.get(pl.getId());
				if (killMap != null) {
					for (Entry<Integer, Integer> ent : killMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
						YQZZBattleStatics bs = pl.getBattleStaticsStat(ent.getKey());
						bs.setKillCount(bs.getKillCount() + ent.getValue());
						if (!Objects.equals(pl.getMainServerId(), tarServerId)) {
							bs.setKillCrossCount(bs.getKillCrossCount() + ent.getValue());
						}
					}
				}
			}
			{
				Map<Integer, Integer> hurtMap = battleHurtMap.get(pl.getId());
				if (hurtMap != null) {
					for (Entry<Integer, Integer> ent : hurtMap.entrySet()) {
						BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, ent.getKey());
						killPow += cfg.getPower() * ent.getValue();
						YQZZBattleStatics bs = pl.getBattleStaticsStat(ent.getKey());
						bs.setHurtCount(bs.getHurtCount() + ent.getValue());
						if (!Objects.equals(pl.getMainServerId(), tarServerId)) {
							bs.setHurtCrossCount(bs.getHurtCrossCount() + ent.getValue());
						}
					}
				}
			}
			pl.setKillPower(pl.getKillPower() + killPow);
		}
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		DungeonRedisLog.log(getId(), "YQZZ GAME CREATE parm: {}", extParm.toString());
		IS_GO_MODEL = extParm.isDebug();
		nextSyncToPlayer = createTime + 5000;

		worldPointService = new YQZZWorldPointService(this);
		worldPointService.init();
		secondMap = YQZZSecondMap.create(this);
		battleStageTime = YQZZBattleStageTime.create(this);
		gameStartTime = createTime + getCfg().getPrepairTime() * 1000;
		this.nextLoghonor = gameStartTime + YQZZConst.MINUTE_MICROS;
		YQZZWarConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(YQZZWarConstCfg.class);
		long periodTime = constCfg.getPerioTime() * 1L;
		this.addTickable(new HawkPeriodTickable(periodTime) {

			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});

	}

	public long battleTime() {
		return overTime - gameStartTime;
	}

	public int[] randomPoint() {
		// int index = RandomUtils.nextInt(fuelBankPointList.size());
		// return fuelBankPointList.get(index);

		double r = 66 * Math.sqrt(Math.random()) + 16;
		double theta = Math.random() * 2 * 3.1415926;

		int x = (int) (centerX + r * Math.cos(theta));
		int y = (int) (centerY + r * Math.sin(theta));

		return new int[] { x, y };
	}

	public void updateRoomActiveTime() {
		YQZZMatchService.getInstance().getDataManger()
				.updateYQZZGameDataActiveTime(this.extParm.getBattleId());
	}

	public void doMoveCitySuccess(IYQZZPlayer player, int[] targetPoint) {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if(Objects.nonNull(autoMarchParam)){
			//结束自动打野  
			WorldMarchService.getInstance().breakAutoMarch(player, Status.Error.AUTO_ATK_MONSTER_MARCH_BACK_VALUE);
		}
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
		for (IYQZZWorldMarch march : worldMarches.values()) {
			if (march instanceof IYQZZReportPushMarch) {
				((IYQZZReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		worldPointService.removeViewPoint(player);
		player.setPos(targetPoint);
		worldPointService.addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();
	}

	public IYQZZWorldMarch startMarch(IYQZZPlayer player, IYQZZWorldPoint fPoint, IYQZZWorldPoint tPoint,
			WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		// 生成行军
		IYQZZWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
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

		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_MARCH_ADD_PUSH_VALUE,
				march.toBuilder(WorldMarchRelation.SELF));
		player.sendProtocol(protocol);

		march.notifyMarchEvent(MarchEvent.MARCH_ADD); // 通知行军事件

		// 加入行军警报
		march.onMarchStart();

		tPoint.onMarchCome(march);
		player.onMarchStart(march);
		DungeonRedisLog.log(player.getId(), "startMarch {}", march);
		return march;
	}

	/** 生成一个行军对象 */
	public IYQZZWorldMarch genMarch(IYQZZPlayer player, IYQZZWorldPoint fPoint, IYQZZWorldPoint tPoint,
			WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		YQZZMarchEntity march = new YQZZMarchEntity();
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
		march.setAutoMarchIdentify(effParams.getAutoMarchIdentify());
		if (Objects.nonNull(effParams.getHeroIds())) {
			march.setHeroIdList(effParams.getHeroIds());
		}

		// 使用额外侦查行军队列
		if (marchType == WorldMarchType.SPY && !player.isExtraSypMarchOccupied() && player.isExtraSpyMarchOpen()) {
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
		IYQZZWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new YQZZAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new YQZZSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new YQZZAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new YQZZMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new YQZZMassJoinSingleMarch(player);
			break;
		case YQZZ_BUILDING_SINGLE: // = 107; // 司令部
			iWorldMarch = new YQZZBuildingMarchSingle(player);
			((YQZZBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case YQZZ_BUILDING_MASS:// = 117; // 司令部
			iWorldMarch = new YQZZBuildingMarchMass(player);
			((YQZZBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.YQZZ_BUILDING_MASS);
			((YQZZBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.YQZZ_BUILDING_MASS_JOIN);
			break;
		case YQZZ_BUILDING_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new YQZZBuildingMarchMassJoin(player);
			break;
		case FOGGY_SINGLE: // = 107; // 司令部
			iWorldMarch = new YQZZNianMarchSingle(player);
			break;

		case FOGGY_FORTRESS_MASS:// = 117; // 司令部
			iWorldMarch = new YQZZNianMarchMass(player);
			break;
		case FOGGY_FORTRESS_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new YQZZNianMarchMassJoin(player);
			break;
		case ATTACK_MONSTER:
			iWorldMarch = new YQZZAttackMonsterMarch(player);
			break;
		case COLLECT_RESOURCE:
			iWorldMarch = new YQZZCollectResMarch(player);
			break;
		case PYLON_MARCH:
			iWorldMarch = new YQZZPylonMarch(player);
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
			waitTime *= 1
					- player.getEffect().getEffVal(EffType.GUILD_MASS_TIME_REDUCE_PER, effParams) * GsConst.EFF_PER;
			waitTime = waitTime > 0 ? waitTime : 0;

			march.setMarchStatus(WorldMarchStatus.MARCH_STATUS_WAITING_VALUE);
			march.setMassReadyTime(march.getStartTime());
			march.setStartTime(march.getStartTime() + waitTime);
			march.setEndTime(march.getEndTime() + waitTime);
		}
		if (iWorldMarch instanceof IYQZZMassJoinMarch) {
			iWorldMarch.setHashThread(((IYQZZMassJoinMarch) iWorldMarch).leaderMarch().get().getHashThread());
		} else {
			final int threadNum = HawkTaskManager.getInstance().getThreadNum();
			iWorldMarch.setHashThread(tPoint.getHashThread(threadNum));
		}
		return iWorldMarch;
	}

	public YQZZBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(YQZZBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public IYQZZWorldMarch getPlayerMarch(String playerId, String marchId) {
		IYQZZWorldMarch march = worldMarches.get(marchId);
		if (march != null && Objects.equals(march.getPlayerId(), playerId)) {
			return march;
		}
		return null;
	}

	public List<IYQZZWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<IYQZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<IYQZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}
	
	/**
	 * 获取自动打野行军
	 */
	public List<IYQZZWorldMarch> getAutoMonsterMarch(String playerId) {
		List<IYQZZWorldMarch> marchs = new ArrayList<IYQZZWorldMarch>();
		for (IYQZZWorldMarch march : getPlayerMarches(playerId)) {
			if (march.getMarchEntity().getAutoMarchIdentify() > 0) {
				marchs.add(march);
			}
		}
		
		return marchs;
	}

	public <T extends IYQZZBuilding> List<T> getYQZZBuildingByType(YQZZBuildType type) {
		return worldPointService.getBuildingByType(type);
	}

	public List<IYQZZBuilding> getYQZZBuildingList() {
		return worldPointService.getBuildingList();
	}

	public List<IYQZZWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IYQZZWorldMarch> result = new ArrayList<>();
		for (IYQZZWorldMarch ma : worldMarches.values()) {
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

	public IYQZZWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<IYQZZWorldMarch> getGuildWarMarch(String guildId) {
		List<IYQZZWorldMarch> result = new ArrayList<>();
		for (IYQZZWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
						|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				IYQZZWorldPoint point = march.getTerminalWorldPoint();
				if (Objects.isNull(point)) {
					continue;
				}
				// 不是自己盟的行军
				boolean notSelfGuildMarch = !Objects.equals(guildId, march.getParent().getGuildId());
				// 目标点不属于自己的联盟
				boolean notSelfGuildPoint = !Objects.equals(point.getGuildId(), guildId);
				if (notSelfGuildMarch && notSelfGuildPoint) { // 如果不是自己阵营的行军 也不是本方控制点
					continue;
				}

				if (march instanceof YQZZBuildingMarchSingle) {
					if (!notSelfGuildMarch && !notSelfGuildPoint) {// 如果已被已方控制 也是本本行军
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

	public List<IYQZZWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<IYQZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<IYQZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<IYQZZWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IYQZZWorldMarch> result = new LinkedList<>();
		for (IYQZZWorldMarch ma : worldMarches.values()) {
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
	public Optional<IYQZZWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(worldPointService.getViewPoints().get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<IYQZZWorldPoint> getWorldPoint(int pointId) {
		int[] pos = GameUtil.splitXAndY(pointId);
		return getWorldPoint(pos[0], pos[1]);
	}

	@Override
	public boolean onTick() {
		return true;
	}

	public boolean onBattleTick() {
		if (Objects.isNull(state)) {
			return false;
		}

		curTimeMil = HawkTime.getMillisecond();
		try {
			state.onTick();
			worldPointService.onTick();
			battleStageTime.onTick();
			for (YQZZGuildBaseInfo buildcamp : guildStatisticMap.values()) {
				buildcamp.onTick();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;
				buildSyncPB();
				// 夸服玩家和本服分别广播
				for (YQZZGuildBaseInfo gstic : getBattleCamps()) {
					if (gstic.isCsGuild) {
						CrossProxy.getInstance().broadcastProtocolV2(gstic.campServerId, gstic.playerIds,
								HawkProtocol.valueOf(HP.code2.YQZZ_GAME_SYNC, lastSyncpbPlayer));

					}
				}

				for (IYQZZPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}

				// 战场内外互通
				YQZZGaiLanResp.Builder gailan = YQZZGaiLanResp.newBuilder();
				gailan.setLastSyncpbPlayer(lastSyncpbPlayer);
				gailan.setSecondMap(getSecondMap(""));
				for (YQZZGuildBaseInfo buildcamp : guildStatisticMap.values()) {
					YQZZDeclareWarUseResp.Builder resp = YQZZDeclareWarUseResp.newBuilder();
					resp.addAllRecords(buildcamp.declareWarRecords);
					resp.setGuildId(buildcamp.campGuild);
					gailan.addDeclearWarRec(resp);
				}
				
				YQZZRoomManager.getInstance().saveGailan(gailan.build(), getId());
				if (IS_GO_MODEL) {
//					HawkLog.logPrintln("行军总数量  " + worldMarches.size());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (getCurTimeMil() > nextLoghonor) {
				this.nextLoghonor += YQZZConst.MINUTE_MICROS;
				LogUtil.logYQZZRoomInfo(lastSyncpb.getPlayerInfoCount(), playerMap.size(), guildStatisticMap.size(), getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(IYQZZPlayer player) {
		state.enterWorld(player);
	}

	public void joinRoom(IYQZZPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}
		if (playerQuitMap.containsKey(player.getId())) {
			player.mergerFrom(playerQuitMap.remove(player.getId()));
		}
		player.setCamp(getNationInfo(player.getMainServerId()).getCamp());
		int[] bornP = worldPointService.randomBornPoint(player);
		player.setPos(bornP);
		player.init();

		if (!guildStatisticMap.containsKey(player.getGuildId())) {
			YQZZGuildBaseInfo staInfo = new YQZZGuildBaseInfo(this);
			staInfo.camp = player.getCamp();
			staInfo.campGuild = player.getGuildId();
			staInfo.campGuildName = player.getGuildName();
			staInfo.campGuildTag = player.getGuildTag();
			staInfo.campServerId = player.getMainServerId();
			staInfo.campguildFlag = player.getGuildFlag();
			staInfo.guildLeaderName = player.getGuildLeaderName();
			staInfo.guildLeaderId = player.getGuildLeaderId();
			staInfo.isCsGuild = player.isCsPlayer();
			staInfo.declareWarPoint = getCfg().getDeclareWarOrder();
			staInfo.lastDeclareWarPoint = getCreateTime();
			guildStatisticMap.put(player.getGuildId(), staInfo);
			 
			DungeonRedisLog.log(getId(), "guildId:{} leader:{} serverId:{}", staInfo.campGuild, staInfo.guildLeaderName, staInfo.campServerId);
		}

		YQZZRoomManager.getInstance().cache(player);
		player.setYQZZState(YQZZState.GAMEING);
		player.getPush().pushJoinGame();
		this.syncOrder(player);
		worldPointService.addViewPoint(player);
		playerMap.put(player.getId(), player);
		// buildSyncPB();
		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {}", getId());
		DungeonRedisLog.log(getId(), "player:{} name:{} guildId:{} leader:{} serverId:{}", player.getId(), player.getName(), player.getGuildId(), player.getGuildLeaderName(),
				player.getMainServerId());
	}

	/**取得创建游戏的国家信息*/
	public YQZZNation getNationInfo(String serverId) {
		return extParm.getServerCamp().getOrDefault(serverId, YQZZNation.defaultInstance);
	}

	public YQZZNation getNationInfo(YQZZ_CAMP camp) {
		for (YQZZNation na : extParm.getServerCamp().values()) {
			if (na.getCamp() == camp) {
				return na;
			}
		}
		return YQZZNation.defaultInstance;
	}

	public String getCampServer(YQZZ_CAMP camp) {
		for (YQZZNation na : extParm.getServerCamp().values()) {
			if (na.getCamp() == camp) {
				return na.getServerId();
			}
		}
		return "";
	}
	

	public void quitWorld(IYQZZPlayer quitPlayer, YQZZQuitReason reason) {
		long time1 = HawkTime.getMillisecond();
		WorldMarchService.getInstance().closeAutoMarch(quitPlayer.getId());
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBYQZZGameOver.Builder builder = PBYQZZGameOver.newBuilder();
			if (reason == YQZZQuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_GAME_OVER, builder));
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		worldPointService.getWorldScene().leave(quitPlayer.getEye().getAoiObjId());
		boolean inWorld = worldPointService.removeViewPoint(quitPlayer);
		if (inWorld) {
			// 删除行军
			cleanCityPointMarch(quitPlayer);

//			for (IYQZZPlayer gamer : playerMap.values()) {
//				PBYQZZPlayerQuitRoom.Builder bul = PBYQZZPlayerQuitRoom.newBuilder();
//				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
//				gamer.sendProtocol(HawkProtocol.valueOf(HP.code2.YQZZ_PLAYER_QUIT, bul));
//			}
		}
		long time2 = HawkTime.getMillisecond();
		quitPlayer.getPush().pushGameOver();
		long time3 = HawkTime.getMillisecond();
		if (time3 - time1 > 500) {
			DungeonRedisLog.log(getId(), "{} quitWorld {} tick too much time, t1:{} t2:{} t3:{} t4:{} t5:{}", quitPlayer.getId(), time2 -time1, time3 -time2);
		}
	}

	public void cleanCityPointMarch(IYQZZPlayer quitPlayer) {
		try {
			List<IYQZZWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (IYQZZWorldMarch march : quiterMarches) {
				if (march instanceof IYQZZMassMarch) {
					march.getMassJoinMarchs(true).forEach(IYQZZWorldMarch::onMarchCallback);
				}
				if (march instanceof IYQZZMassJoinMarch) {
					Optional<IYQZZMassMarch> massMarch = ((IYQZZMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						IYQZZMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(IYQZZWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<IYQZZWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (IYQZZWorldMarch march : pms) {
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
		boolean bfalse = getPointMarches(pointId, WorldMarchStatus.MARCH_STATUS_MARCH_ASSIST, WorldMarchType.ASSISTANCE)
				.stream().filter(march -> march.getPlayerId().equals(viewerId)).count() > 0;
		return bfalse;
	}

	public List<IYQZZPlayer> getPlayerList(YQZZState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<IYQZZPlayer> getPlayerList(YQZZState st1, YQZZState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<IYQZZPlayer> getPlayerList(YQZZState st1, YQZZState st2, YQZZState st3) {
		List<IYQZZPlayer> result = new ArrayList<>();
		for (IYQZZPlayer player : playerMap.values()) {
			YQZZState state = player.getYQZZState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public IYQZZPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public void setPlayerList(List<IYQZZPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	public Collection<IYQZZWorldPoint> getViewPoints() {
		return worldPointService.getViewPoints().values();
	}

	public void setViewPoints(List<IYQZZWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<IYQZZWorldMarch> getWorldMarchList() {
		ArrayList<IYQZZWorldMarch> result = new ArrayList<>(worldMarches.values());
		// Collections.sort(result, Comparator.comparingLong(IYQZZWorldMarch::getStartTime));
		return result;
	}

	public int getWorldMarchCount() {
		return worldMarches.size();
	}

	public void removeMarch(IYQZZWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(IYQZZWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<IYQZZWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public IYQZZBattleRoomState getState() {
		return state;
	}

	public void setState(IYQZZBattleRoomState state) {
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

	public long getGameStartTime() {
		return gameStartTime;
	}

	public long getOverTime() {
		return overTime;
	}

	public void setOverTime(long overTime) {
		this.overTime = overTime;
	}

	public YQZZExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(YQZZExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(IYQZZPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber())
				.setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		ChatMsg pbMsg = parames.toPBMsg();
		broadcastChatMsg(pbMsg);
	}
	
	public YQZZGuildFormationObj getGuildFormation(String guildId) { // TODO 包装成自己的
		if (guildFormationObjmap.containsKey(guildId)) {
			YQZZGuildFormationObj result = guildFormationObjmap.get(guildId);
			return result;
		}
		YQZZGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new YQZZGuildFormationObj());
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

	private void broadcastChatMsg(ChatMsg pbMsg) {
		Set<Player> tosend = new HashSet<>(getPlayerList(YQZZState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg);
		if (isGuildMsg || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE
				|| pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_SPECIAL_BROADCAST_VALUE) {
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), pbMsg.getAllianceId())).collect(Collectors.toSet());
			YQZZGuildBaseInfo campBase = getCampBase(pbMsg.getAllianceId());
			if (Objects.nonNull(campBase)) {
				campBase.addGuldMsgCache(pbMsg);
			}
		} else if (pbMsg.getType() == ChatType.CHAT_FUBEN_NATION_VALUE) {
			tosend = tosend.stream().filter(p -> Objects.equals(p.getMainServerId(), pbMsg.getServerId())).collect(Collectors.toSet());
			getBaseByCamp(getNationInfo(pbMsg.getServerId()).getCamp()).addNationMsgCache(pbMsg);
		} else {
			addRoomMsgCache(pbMsg);
		}
		ChatService.getInstance().sendChatMsg(Arrays.asList(pbMsg), tosend);
	}
	
	public void addRoomMsgCache(ChatMsg msg) {
		try {
			roomMsgCache.addFirst(msg);
			if (roomMsgCache.size() > 50) {
				roomMsgCache.removeLast();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	public int onAddGuildSign(IYQZZPlayer player, GuildSign guildSign) {
		String guildId = player.getGuildId();

		Map<Integer, GuildSign> signMap = getCampBase(player.getGuildId()).getSignMap();
		// // 放置位置检测
		// for (GuildSign sign : signMap.values()) {
		// if (sign.getPosX() == guildSign.getPosX() && sign.getPosY() == guildSign.getPosY()) {
		// return Status.Error.GUILD_SIGN_POINT_REPEAT_VALUE;
		// }
		// }
		signMap.put(guildSign.getId(), guildSign);
		// LocalRedis.getInstance().addGuildSign(guildId, guildSign);

		Set<IYQZZPlayer> tosend = new HashSet<>(getPlayerList(YQZZState.GAMEING));
		tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), guildId)).collect(Collectors.toSet());
		// 同步联盟信息
		for (IYQZZPlayer member : tosend) {
			member.getPush().syncGuildInfo();
			// 联盟红点
			member.getPush().syncRedPoint(RedType.GUILD_FAVOURITE, "");
		}

		// 发送联盟消息
		ChatParames parames = ChatParames.newBuilder()
				.setPlayer(player)
				.setChatType(ChatType.CHAT_FUBEN_TEAM)
				.setKey(Const.NoticeCfgId.YQZZ_ALLIANCE_SIGN_NOTICE)
				.addParms(guildSign.getPosX(), guildSign.getPosY(), guildSign.getId(), guildSign.getInfo())
				.build();
		this.addWorldBroadcastMsg(parames);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	public void gameOver() {
		if (!isGameOver()) {
			return;
		}
		thread.close(false);
		DungeonRedisLog.log(getId(), "YQZZ close battle threadName:{} battleId:{}", thread.getName(), thread.getBattleRoom().getId());

		this.playerMap = null;
		this.playerQuitMap = null;
		this.worldMarches = null;
		this.worldPointService = null;
		this.thread = null;
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

	public List<IYQZZPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public PBYQZZGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public PBYQZZGameInfoSync.Builder getLastSyncpbPlayer() {
		return lastSyncpbPlayer;
	}

	public void setLastSyncpb(PBYQZZGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public String getWinGuild() {
		return winGuild;
	}

	public void setWinGuild(String winGuild) {
		this.winGuild = winGuild;
	}

	public boolean isHasNotBrodcast_YQZZ_182() {
		return hasNotBrodcast_YQZZ_182;
	}

	public void setHasNotBrodcast_YQZZ_182(boolean hasNotBrodcast_YQZZ_182) {
		this.hasNotBrodcast_YQZZ_182 = hasNotBrodcast_YQZZ_182;
	}

	public boolean isHasNotBrodcast_YQZZ_183() {
		return hasNotBrodcast_YQZZ_183;
	}

	public void setHasNotBrodcast_YQZZ_183(boolean hasNotBrodcast_YQZZ_183) {
		this.hasNotBrodcast_YQZZ_183 = hasNotBrodcast_YQZZ_183;
	}

	public YQZZThread getThread() {
		return thread;
	}

	public void setThread(YQZZThread thread) {
		this.thread = thread;
	}

	public int getCenterX() {
		return centerX;
	}

	public void setCenterX(int centerX) {
		this.centerX = centerX;
	}

	public int getCenterY() {
		return centerY;
	}

	public void setCenterY(int centerY) {
		this.centerY = centerY;
	}

	public YQZZWorldPointService getWorldPointService() {
		return worldPointService;
	}

	public YQZZGuildBaseInfo getCampBase(String guildId) {
		return guildStatisticMap.get(guildId);
	}

	public List<YQZZGuildBaseInfo> getBattleCamps() {
		return new ArrayList<>(guildStatisticMap.values());
	}

	public YQZZBattleStageTime getBattleStageTime() {
		return battleStageTime;
	}

}
