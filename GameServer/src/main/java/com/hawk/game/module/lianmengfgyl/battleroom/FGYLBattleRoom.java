package com.hawk.game.module.lianmengfgyl.battleroom;

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
import java.util.concurrent.ConcurrentSkipListMap;
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
import org.hawk.xid.HawkXID;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.march.AutoMonsterMarchParam;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLConst.FGYLState;
import com.hawk.game.module.lianmengfgyl.battleroom.FGYLRoomManager.FGYL_CAMP;
import com.hawk.game.module.lianmengfgyl.battleroom.cfg.FGYLBattleCfg;
import com.hawk.game.module.lianmengfgyl.battleroom.entity.FGYLMarchEntity;
import com.hawk.game.module.lianmengfgyl.battleroom.msg.FGYLQuitReason;
import com.hawk.game.module.lianmengfgyl.battleroom.player.IFGYLPlayer;
import com.hawk.game.module.lianmengfgyl.battleroom.player.module.guildformation.FGYLGuildFormationObj;
import com.hawk.game.module.lianmengfgyl.battleroom.roomstate.IFGYLBattleRoomState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLAttackMonsterMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLBuildingMarchMass;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLBuildingMarchMassJoin;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLBuildingMarchSingle;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.FGYLNotifyMarchEventFunc;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.IFGYLWorldMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassJoinMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLMassMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLPassiveAlarmTriggerMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldmarch.submarch.IFGYLReportPushMarch;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildHurtRecord;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLBuildState;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLHeadQuarter;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonster;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.FGYLMonsterKillRecord;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.IFGYLBuilding;
import com.hawk.game.module.lianmengfgyl.battleroom.worldpoint.refresh.FGYLMonsterRefesh;
import com.hawk.game.module.lianmengfgyl.march.cfg.FGYLConstCfg;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.FGYL.PBFGYLGameInfoSync;
import com.hawk.game.protocol.FGYL.PBFGYLGameOver;
import com.hawk.game.protocol.FGYL.PBFGYLGuildInfo;
import com.hawk.game.protocol.FGYL.PBFGYLPlayerInfo;
import com.hawk.game.protocol.FGYL.PBFGYLPlayerQuitRoom;
import com.hawk.game.protocol.FGYL.PBFGYLSecondMapResp;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.PBFGYLBuildHurtRank;
import com.hawk.game.protocol.World.PBFGYLMonsterKillRank;
import com.hawk.game.protocol.World.Position;
import com.hawk.game.protocol.World.WorldMarchLoginPush;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
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
public class FGYLBattleRoom extends HawkAppObj {
	public final boolean IS_GO_MODEL;
	private FGYLExtraParam extParm;
	private int difficult;
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
	private Map<String, IFGYLPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, IFGYLPlayer> playerQuitMap = new ConcurrentHashMap<>();
	/** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	// private Map<Integer, IFGYLWorldPoint> viewPoints = new ConcurrentHashMap<>();
	private FGYLWorldPointService worldPointService;
	private Map<String, IFGYLWorldMarch> worldMarches = new ConcurrentHashMap<>();

	private IFGYLBattleRoomState state;
	private long createTime;
	private long startTime;
	private long overTime;

	private FGYLRandPointSeed pointSeed = new FGYLRandPointSeed();
	private FGYLGuildBaseInfo baseInfoA;
	private FGYLGuildBaseInfo baseInfoB;
	/** 下次主动推详情 */
	private long nextSyncToPlayer;

	/** 上次同步战场详情 */
	private long lastSyncGame;
	private PBFGYLGameInfoSync lastSyncpb = PBFGYLGameInfoSync.getDefaultInstance();
	// private long lastSyncMap;
	// private PBFGYLSecondMapResp lastMappb;

	private long nextLoghonor;

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

	private Map<String, IFGYLPlayer> anchorMap = new HashMap<>();
	private String csServerId = "";
	private Set<String> csPlayerids = new HashSet<>();

	private Map<String, FGYLGuildFormationObj> guildFormationObjmap = new HashMap<>();
	private FGYLMonsterRefesh monsterrefresh;
	private Map<String, FGYLMonsterKillRecord> monsterKillMap = new HashMap<>();
	private FGYL_CAMP winCamp = FGYL_CAMP.B;
	private ConcurrentSkipListMap<String, FGYLNotifyMarchEventFunc> noticyMarchEnentMap = new ConcurrentSkipListMap<>(); 
	public FGYLBattleRoom(HawkXID xid) {
		super(xid);
		IS_GO_MODEL = GsConfig.getInstance().getServerId().equals("60004");
	}

	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		FGYLProtocol pro = (FGYLProtocol) protocol;
		IFGYLPlayer player = pro.getPlayer();
		player.onProtocol(pro.getSource());

		return true;
	}

	public FGYLGuildFormationObj getGuildFormation(String guildId) { // TODO 包装成自己的
		if (guildFormationObjmap.containsKey(guildId)) {
			FGYLGuildFormationObj result = guildFormationObjmap.get(guildId);
			return result;
		}
		FGYLGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new FGYLGuildFormationObj());
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

	public void sync(IFGYLPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		PBFGYLGameInfoSync.Builder bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_GAME_SYNC, bul));

	}

	public boolean isHotBloodModel() {
		return false;
	}

	public PBFGYLGameInfoSync.Builder buildSyncPB() {
		if (curTimeMil - lastSyncGame < 3000 && Objects.nonNull(lastSyncpb)) {
			return lastSyncpb.toBuilder();
		}
		PBFGYLGameInfoSync.Builder bul = PBFGYLGameInfoSync.newBuilder();
		bul.setLevel(difficult);
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);
		bul.setHotBloodMod(isHotBloodModel());
		PBFGYLGuildInfo.Builder aInfo = PBFGYLGuildInfo.newBuilder().setServerId(baseInfoA.getServerId()).setCamp(FGYL_CAMP.A.intValue()).setGuildFlag(baseInfoA.getGuildFlag())
				.setGuildName(baseInfoA.getGuildName())
				.setGuildTag(baseInfoA.getGuildTag()).setGuildId(baseInfoA.getGuildId());
		PBFGYLGuildInfo.Builder bInfo = PBFGYLGuildInfo.newBuilder().setServerId(baseInfoB.getServerId()).setCamp(FGYL_CAMP.B.intValue()).setGuildFlag(baseInfoB.getGuildFlag())
				.setGuildName(baseInfoB.getGuildName())
				.setGuildTag(baseInfoB.getGuildTag()).setGuildId(baseInfoB.getGuildId());

		int honorA = 0; // 当前积分
		double perMinA = 0; // 每分增加
		int buildCountA = 0; // 占领建筑
		int playerCountA = 0; // 战场中人数
		long centerControlA = 0; // FGYL_HEADQUARTERS 核心控制时间
		int buildControlHonorA = 0;
		int killHonorA = 0;
		int collectHonorA = 0;
		Map<EffType, Integer> battleEffValA = new HashMap<>();

		int honorB = 0; // 当前积分
		double perMinB = 0; // 每分增加
		int buildCountB = 0; // 占领建筑
		int playerCountB = 0; // 战场中人数
		long centerControlB = 0; // FGYL_HEADQUARTERS 核心控制时间
		int buildControlHonorB = 0;
		int killHonorB = 0;
		int collectHonorB = 0;
		int monsterCount = 0;
		Map<EffType, Integer> battleEffValB = new HashMap<>();

		for (IFGYLWorldPoint viewp : getViewPoints()) {
			if (viewp instanceof IFGYLBuilding) {
				IFGYLBuilding build = (IFGYLBuilding) viewp;
				if (build.getState() == FGYLBuildState.ZHAN_LING) {
					if (Objects.equals(baseInfoA.getGuildId(), build.getGuildId())) {
						buildCountA++;
					} else if (Objects.equals(baseInfoB.getGuildId(), build.getGuildId())) {
						buildCountB++;
					}
				}
			}
			if (viewp instanceof FGYLMonster) {
				monsterCount++;
			}
		}

		List<IFGYLPlayer> all = new ArrayList<>(playerMap.values());
		all.addAll(playerQuitMap.values());
		for (IFGYLPlayer p : all) {
			if (p.getCamp() == FGYL_CAMP.A) {
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
			PBFGYLPlayerInfo.Builder prc = PBFGYLPlayerInfo.newBuilder();
			prc.setCamp(p.getCamp().intValue());
			prc.setName(p.getName());
			prc.setHonor(p.getHonor());
			prc.setGuildTag(p.getGuildTag());
			prc.setGuildHonor(p.getGuildHonor());
			prc.setPlayerId(p.getId());
			prc.setKillMonster(p.getKillMonster());
			prc.setIcon(p.getIcon());
			prc.setPfIcon(p.getPfIcon());
			prc.setIsMidwayQuit(p.getQuitReason() == FGYLQuitReason.LEAVE);
			bul.addPlayerInfo(prc);
		}

		baseInfoA.battleEffVal = ImmutableMap.copyOf(battleEffValA);

		baseInfoB.battleEffVal = ImmutableMap.copyOf(battleEffValB);

		aInfo.setHonor(honorA)
				.setBuildCount(buildCountA).setPlayerCount(playerCountA)
				.setCenterControl(centerControlA).setBuildControlHonor(buildControlHonorA)
				.setKillHonor(killHonorA).setCollectHonor(collectHonorA)
				.setNuclearCount(campANuclearSendCount)
				.setNianKillCnt(campANianKillCount)
				.setPerMin((int) Math.ceil(perMinA * 60));
		bInfo.setHonor(honorB)
				.setBuildCount(buildCountB).setPlayerCount(playerCountB)
				.setCenterControl(centerControlB).setBuildControlHonor(buildControlHonorB)
				.setKillHonor(killHonorB).setCollectHonor(collectHonorB)
				.setNuclearCount(campBNuclearSendCount)
				.setNianKillCnt(campBNianKillCount)
				.setPerMin((int) Math.ceil(perMinB * 60));

		bul.setMonsterCount(monsterCount);
		bul.addGuildInfo(aInfo);
		bul.addGuildInfo(bInfo);

		long campAScore = aInfo.getHonor();
		long campBScore = bInfo.getHonor();
//		FGYL_CAMP winCamp = campAScore > campBScore ? FGYL_CAMP.A : FGYL_CAMP.B;
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

	public Collection<IFGYLWorldPoint> getViewPoints() {
		return worldPointService.getViewPointsList();
	}

	public void getSecondMap(IFGYLPlayer player) {
		PBFGYLSecondMapResp.Builder bul = PBFGYLSecondMapResp.newBuilder();
		for (IFGYLWorldPoint point : getViewPoints()) {

			bul.addPoints(point.toBuilder(player));
		}
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_SECOND_MAP_S, bul));
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
			IFGYLPlayer pl = (IFGYLPlayer) ppp;
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
			IFGYLPlayer pl = (IFGYLPlayer) ppp;
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
		this.difficult = extParm.getDifficult();

		baseInfoA = new FGYLGuildBaseInfo();
		baseInfoA.setCamp(FGYL_CAMP.A);
		baseInfoA.setGuildId(extParm.getCampAGuild());
		baseInfoA.setGuildName(extParm.getCampAGuildName());
		baseInfoA.setGuildTag(extParm.getCampAGuildTag());
		baseInfoA.setServerId(extParm.getCampAServerId());
		baseInfoA.setGuildFlag(extParm.getCampAguildFlag());

		baseInfoB = new FGYLGuildBaseInfo();
		baseInfoB.setCamp(FGYL_CAMP.B);
		baseInfoB.setGuildId(extParm.getCampBGuild());
		baseInfoB.setGuildName(extParm.getCampBGuildName());
		baseInfoB.setGuildTag(extParm.getCampBGuildTag());
		baseInfoB.setServerId(extParm.getCampBServerId());
		baseInfoB.setGuildFlag(extParm.getCampBguildFlag());

		nextSyncToPlayer = createTime + 5000;
		FGYLBattleCfg cfg = getCfg();
		startTime = createTime + cfg.getPrepairTime() * 1000;
		this.nextLoghonor = startTime + FGYLConst.MINUTE_MICROS;

		worldPointService = new FGYLWorldPointService(this);
		worldPointService.init();

		getGuildFormation(baseInfoA.getGuildId());
		getGuildFormation(baseInfoB.getGuildId());

		monsterrefresh = FGYLMonsterRefesh.create(this);
	}

	public void onPlayerLogin(IFGYLPlayer gamer) {
		gamer.getPush().syncPlayerWorldInfo();
		gamer.getPush().syncPlayerInfo();
		// 组装玩家自己的行军PB数据
		WorldMarchLoginPush.Builder builder = WorldMarchLoginPush.newBuilder();
		List<IFGYLWorldMarch> marchs = this.getPlayerMarches(gamer.getId());
		for (IFGYLWorldMarch worldMarch : marchs) {
			builder.addMarchs(worldMarch.toBuilder(WorldMarchRelation.SELF).build());

			WorldMarch march = worldMarch.getMarchEntity();
			if (march.getMarchType() == WorldMarchType.MASS_JOIN_VALUE) {
				IFGYLWorldMarch massMach = this.getMarch(march.getTargetId());
				// 这里添加个空判断。 存在队长解散后，队员找不到队长行军。所以这里可能问空。
				if (massMach != null) {
					builder.addMarchs(massMach.toBuilder(WorldMarchRelation.TEAM_LEADER).build());
				}
			}

			if (worldMarch instanceof IFGYLPassiveAlarmTriggerMarch) {
				((IFGYLPassiveAlarmTriggerMarch) worldMarch).pullAttackReport(gamer.getId());
			}
		}

		List<IFGYLWorldMarch> pointMs = this.getPointMarches(gamer.getPointId());
		for (IFGYLWorldMarch march : pointMs) {
			if (march instanceof IFGYLReportPushMarch) {
				((IFGYLReportPushMarch) march).pushAttackReport(gamer.getId());
			}
		}
		// 通知客户端
		gamer.sendProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCHS_PUSH, builder));
		gamer.moveCityCDSync();

	}

	public IFGYLWorldMarch startMarch(IFGYLPlayer player, IFGYLWorldPoint fPoint, IFGYLWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		// gasoline = Math.max(effParams.getWorldmarchReq().getFgylGasoline(), (int) player.getParent().getCfg().getFuelMarchNeed());
		// 生成行军
		IFGYLWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);

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
		List<IFGYLWorldMarch> spyMarchs = getPlayerMarches(player.getId(), WorldMarchType.SPY);
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
	public IFGYLWorldMarch genMarch(IFGYLPlayer player, IFGYLWorldPoint fPoint, IFGYLWorldPoint tPoint, WorldMarchType marchType, String targetId, int waitTime,
			EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();
		FGYLMarchEntity march = new FGYLMarchEntity();
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
		IFGYLWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_MONSTER:
			iWorldMarch = new FGYLAttackMonsterMarch(player);
			break;
		case FGYL_BUILDING_SINGLE: // = 107; // 司令部
			iWorldMarch = new FGYLBuildingMarchSingle(player);
			((FGYLBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case FGYL_BUILDING_MASS:// = 117; // 司令部
			iWorldMarch = new FGYLBuildingMarchMass(player);
			((FGYLBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.FGYL_BUILDING_MASS);
			((FGYLBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.FGYL_BUILDING_MASS_JOIN);
			break;
		case FGYL_BUILDING_MASS_JOIN:// = 127; // 司令部
			iWorldMarch = new FGYLBuildingMarchMassJoin(player);
			break;

		default:
			throw new UnsupportedOperationException("dont know what march it is!!!!!!!");
		}
		march.getEffectParams().setImarch(iWorldMarch);
		iWorldMarch.setMarchEntity(march);

		List<Position.Builder> roadAllPoints = new LinkedList<>();
		roadAllPoints.add(Position.newBuilder().setX(fPoint.getX()).setY(fPoint.getY()));

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

	public FGYLBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(FGYLBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		try {
			return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	public IFGYLWorldMarch getPlayerMarch(String playerId, String marchId) {
		try {
			IFGYLWorldMarch march = worldMarches.get(marchId);
			if (march != null && Objects.equals(march.getMarchId(), marchId)) {
				return march;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return null;
	}

	public List<IFGYLWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<IFGYLWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<IFGYLWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	public <T extends IFGYLBuilding> List<T> getFGYLBuildingByClass(Class<T> type) {
		return worldPointService.getFGYLBuildingByClass(type);
	}

	public List<IFGYLWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		try {
			boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
			List<WorldMarchType> typeList = Arrays.asList(types);
			List<IFGYLWorldMarch> result = new ArrayList<>();
			for (IFGYLWorldMarch ma : worldMarches.values()) {
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
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Collections.emptyList();
	}

	public IFGYLWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<IFGYLWorldMarch> getGuildWarMarch(String guildId) {
		List<IFGYLWorldMarch> result = new ArrayList<>();
		for (IFGYLWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE || march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				if (march instanceof FGYLBuildingMarchMass) {
					IFGYLBuilding point = (IFGYLBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
					if (!Objects.equals(guildId, march.getParent().getGuildId())) { // 如果不是自己阵营的行军
						if (!Objects.equals(point.getGuildId(), guildId)) {
							continue;
						}
					}
				}
				if (march instanceof FGYLBuildingMarchSingle) {
					IFGYLBuilding point = (IFGYLBuilding) getWorldPoint(march.getMarchEntity().getTerminalX(), march.getMarchEntity().getTerminalY()).orElse(null);
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

	public List<IFGYLWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<IFGYLWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<IFGYLWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<IFGYLWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2, WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<IFGYLWorldMarch> result = new LinkedList<>();
		for (IFGYLWorldMarch ma : worldMarches.values()) {
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
	public Optional<IFGYLWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(worldPointService.getWorldPoint(x, y));
	}

	public Optional<IFGYLWorldPoint> getWorldPoint(int pointId) {
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

		try { // 广播行军变化
			while (!noticyMarchEnentMap.isEmpty()) {
				// 移除当前时间节点集合
				FGYLNotifyMarchEventFunc func = noticyMarchEnentMap.pollFirstEntry().getValue();
				func.apply(null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		

		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;

				PBFGYLGameInfoSync.Builder builder = buildSyncPB();
				// 夸服玩家和本服分别广播
				broadcastCrossProtocol(HawkProtocol.valueOf(HP.code2.FGYL_GAME_SYNC, builder));
				for (IFGYLPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}

				// 发送主播
				for (IFGYLPlayer anchor : getAnchors()) {
					sync(anchor);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		getGuildFormation(baseInfoA.getGuildId()).checkMarchIdRemove();
		getGuildFormation(baseInfoB.getGuildId()).checkMarchIdRemove();

		monsterrefresh.onTick();
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(IFGYLPlayer player) {
		state.enterWorld(player);
	}

	public void joinRoom(IFGYLPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this || isGameOver()) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}

		player.setCamp(Objects.equals(player.getGuildId(), baseInfoA.getGuildId()) ? FGYL_CAMP.A : FGYL_CAMP.B);
		if (player.getCamp() == FGYL_CAMP.A) {
			player.setGuildId(baseInfoA.getGuildId());
			player.setGuildTag(baseInfoA.getGuildTag());
			player.setGuildFlag(baseInfoA.getGuildFlag());
			player.setGuildName(baseInfoA.getGuildName());
		} else {
			player.setGuildId(baseInfoB.getGuildId());
			player.setGuildTag(baseInfoB.getGuildTag());
			player.setGuildFlag(baseInfoB.getGuildFlag());
			player.setGuildName(baseInfoB.getGuildName());
		}

		int[] bornP = null;
		while (bornP == null) {
			bornP = worldPointService.randomFreePoint(randomPoint(), player.getGridCnt());
		}
		FGYLConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(FGYLConstCfg.class);
		player.setPos(bornP);

		player.init();
		FGYLRoomManager.getInstance().cache(player);
		player.setFgylState(FGYLState.GAMEING);
		player.getPush().pushJoinGame();
		player.setSkillOrder(constCfg.getStartHonor());
		worldPointService.addViewPoint(player);
		playerMap.put(player.getId(), player);
		if (player.isCsPlayer()) {
			csPlayerids.add(player.getId());
			csServerId = player.getMainServerId();
		}
		totalEnterPower += player.getPower();

		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {} guildId {}", getId(), player.getGuildId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public int[] randomPoint() {
		IFGYLBuilding build = HawkRand.randomObject(worldPointService.getBuildingList());
		return GameUtil.splitXAndY(build.getPointId());
	}

	public void quitWorld(IFGYLPlayer quitPlayer, FGYLQuitReason reason) {
		quitPlayer.setQuitReason(reason);
		{ // 弹窗
			PBFGYLGameOver.Builder builder = PBFGYLGameOver.newBuilder();
			if (reason == FGYLQuitReason.LEAVE) {
				builder.setQuitReson(reason.intValue());
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_GAME_OVER, builder));
		}

		if (quitPlayer.isAnchor()) { // 主播退了不用理他
			anchorMap.remove(quitPlayer.getId());
			quitPlayer.quitGame();
			return;
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		worldPointService.getWorldScene().leave(quitPlayer.getEye().getAoiObjId());
		worldPointService.removeViewPoint(quitPlayer);
		// 删除行军
		cleanCityPointMarch(quitPlayer);

		for (IFGYLPlayer gamer : playerMap.values()) {
			PBFGYLPlayerQuitRoom.Builder bul = PBFGYLPlayerQuitRoom.newBuilder();
			bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
			gamer.sendProtocol(HawkProtocol.valueOf(HP.code2.FGYL_PLAYER_QUIT, bul));
		}

		quitPlayer.getData().getQueueEntities().clear();

		// if (reason == QuitReason.LEAVE) {
		// ChatParames parames = ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_FGYL).setKey(NoticeCfgId.FGYL_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();
		csPlayerids.remove(quitPlayer.getId());

	}

	public void cleanCityPointMarch(IFGYLPlayer quitPlayer) {
		try {
			List<IFGYLWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (IFGYLWorldMarch march : quiterMarches) {
				if (march instanceof IFGYLMassMarch) {
					march.getMassJoinMarchs(true).forEach(IFGYLWorldMarch::onMarchCallback);
				}
				if (march instanceof IFGYLMassJoinMarch) {
					Optional<IFGYLMassMarch> massMarch = ((IFGYLMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						IFGYLMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(IFGYLWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<IFGYLWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (IFGYLWorldMarch march : pms) {
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

	public List<IFGYLPlayer> getPlayerList(FGYLState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<IFGYLPlayer> getPlayerList(FGYLState st1, FGYLState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<IFGYLPlayer> getPlayerList(FGYLState st1, FGYLState st2, FGYLState st3) {
		List<IFGYLPlayer> result = new ArrayList<>();
		for (IFGYLPlayer player : playerMap.values()) {
			FGYLState state = player.getFgylState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public IFGYLPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public List<IFGYLPlayer> getCampPlayers(FGYL_CAMP camp) {
		List<IFGYLPlayer> list = new ArrayList<>();
		for (IFGYLPlayer gamer : playerMap.values()) {
			if (gamer.getCamp() == camp) {
				list.add(gamer);
			}
		}
		return list;
	}

	public void setPlayerList(List<IFGYLPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	public void setViewPoints(List<IFGYLWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<IFGYLWorldMarch> getWorldMarchList() {
		return new ArrayList<>(worldMarches.values());
	}

	public void removeMarch(IFGYLWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(IFGYLWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<IFGYLWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public IFGYLBattleRoomState getState() {
		return state;
	}

	public void setState(IFGYLBattleRoomState state) {
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

	public FGYLExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(FGYLExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(IFGYLPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber()).setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo, null);

	}

	public void addWorldBroadcastMsg(ChatParames parames, FGYL_CAMP camp) {
		broadcastChatMsg(parames.toPBMsg(), camp);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		broadcastChatMsg(parames.toPBMsg(), null);
	}

	private void broadcastChatMsg(ChatMsg pbMsg, FGYL_CAMP camp) {
		Set<Player> tosend = new HashSet<>(getPlayerList(FGYLState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg) || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE || Objects.nonNull(camp);
		if (isGuildMsg) {
			String guildId = Objects.nonNull(camp) ? getCampGuild(camp) : pbMsg.getAllianceId();
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), guildId)).collect(Collectors.toSet());
		} else {
			for (IFGYLPlayer anchor : getAnchors()) {
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

	public List<IFGYLPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public String getCsServerId() {
		return csServerId;
	}

	public Set<String> getCsPlayerids() {
		return csPlayerids;
	}

	public PBFGYLGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBFGYLGameInfoSync lastSyncpb) {
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

	public List<IFGYLPlayer> getAnchors() {
		if (anchorMap.isEmpty()) {
			return Collections.emptyList();
		}
		List<IFGYLPlayer> result = new ArrayList<>(anchorMap.size());
		for (IFGYLPlayer anchor : anchorMap.values()) {
			if (Objects.nonNull(anchor) && anchor.isActiveOnline()) {
				result.add(anchor);
			}
		}
		return result;
	}

	public boolean isAnchor(IFGYLPlayer player) {
		return anchorMap.containsKey(player.getId());
	}

	public long getTotalEnterPower() {
		return totalEnterPower;
	}

	public int getCampOrder(FGYL_CAMP camp) {
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

	public String getCampGuild(FGYL_CAMP camp) {
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

	public String getCampGuildTag(FGYL_CAMP camp) {
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

	public String getCampGuildName(FGYL_CAMP camp) {
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

	public FGYL_CAMP getGuildCamp(String guildId) {
		if (Objects.equals(guildId, baseInfoA.getGuildId())) {
			return FGYL_CAMP.A;
		}
		return FGYL_CAMP.B;
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

	public FGYLGuildBaseInfo getCampBase(String guildId) {
		if (Objects.equals(guildId, baseInfoA.getGuildId())) {
			return baseInfoA;
		}
		if (Objects.equals(guildId, baseInfoB.getGuildId())) {
			return baseInfoB;
		}
		return FGYLGuildBaseInfo.defaultInstance;
	}

	public FGYLGuildBaseInfo getCampBase(FGYL_CAMP camp) {
		if (camp == baseInfoA.getCamp()) {
			return baseInfoA;
		}
		if (camp == baseInfoB.getCamp()) {
			return baseInfoB;
		}
		return FGYLGuildBaseInfo.defaultInstance;
	}

	public FGYLGuildBaseInfo getBaseInfoA() {
		return baseInfoA;
	}

	public void setBaseInfoA(FGYLGuildBaseInfo baseInfoA) {
		this.baseInfoA = baseInfoA;
	}

	public FGYLGuildBaseInfo getBaseInfoB() {
		return baseInfoB;
	}

	public void setBaseInfoB(FGYLGuildBaseInfo baseInfoB) {
		this.baseInfoB = baseInfoB;
	}

	public FGYLWorldPointService getWorldPointService() {
		return worldPointService;
	}

	public void doMoveCitySuccess(IFGYLPlayer player, int[] targetPoint) {
		AutoMonsterMarchParam autoMarchParam = WorldMarchService.getInstance().getAutoMarchParam(player.getId());
		if (Objects.nonNull(autoMarchParam)) {
			// 结束自动打野
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
		for (IFGYLWorldMarch march : getWorldMarchList()) {
			if (march instanceof IFGYLReportPushMarch) {
				((IFGYLReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		worldPointService.removeViewPoint(player);
		player.setPos(targetPoint);
		worldPointService.addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();

	}

	public int getDifficult() {
		return difficult;
	}

	public void setDifficult(int difficult) {
		this.difficult = difficult;
	}

	public List<PBFGYLBuildHurtRank> foggyHurtRank() {
//		List<IFGYLBuilding> buildList = worldPointService.getBuildingList();
		List<FGYLHeadQuarter> buildList =worldPointService.getFGYLBuildingByClass(FGYLHeadQuarter.class);
		Map<String, FGYLBuildHurtRecord> foggyHurtMap = new HashMap<>();
		for (IFGYLBuilding build : buildList) {
			for (FGYLBuildHurtRecord value : build.getFoggyHurtMap().values()) {
				FGYLBuildHurtRecord record = foggyHurtMap.get(value.getPlayerId());
				if (record == null) {
					record = new FGYLBuildHurtRecord();
					record.setName(value.getName());
					record.setPlayerId(value.getPlayerId());
					foggyHurtMap.put(value.getPlayerId(), record);
				}
				record.addKill(value.getKill());
			}
		}

		return foggyHurtMap.values().stream()
				.sorted(Comparator.comparingInt(FGYLBuildHurtRecord::getKill).reversed().thenComparing(Comparator.comparingLong(FGYLBuildHurtRecord::getLastUpdate)))
				.map(FGYLBuildHurtRecord::toPBObj)
				.collect(Collectors.toList());
	}

	public void addMonsterKill(IFGYLPlayer player, int kill) {
		FGYLMonsterKillRecord record = monsterKillMap.get(player.getId());
		if (record == null) {
			record = new FGYLMonsterKillRecord();
			record.setName(player.getName());
			record.setPlayerId(player.getId());
			monsterKillMap.put(player.getId(), record);
		}
		record.addKill(kill);
	}

	public List<PBFGYLMonsterKillRank> monsterKillRank() {
		return monsterKillMap.values().stream()
				.sorted(Comparator.comparingInt(FGYLMonsterKillRecord::getKill).reversed().thenComparing(Comparator.comparingLong(FGYLMonsterKillRecord::getLastUpdate)))
				.map(FGYLMonsterKillRecord::toPBObj)
				.collect(Collectors.toList());
	}

	public long getGametime() {
		return getCurTimeMil() - getCreateTime();
	}

	public FGYL_CAMP getWinCamp() {
		return winCamp;
	}

	public void setWinCamp(FGYL_CAMP winCamp) {
		this.winCamp = winCamp;
	}
	
	public void notifyMarchEventAsync(FGYLNotifyMarchEventFunc func){
		noticyMarchEnentMap.put(func.getMarch().getMarchId(), func);
	}
}
