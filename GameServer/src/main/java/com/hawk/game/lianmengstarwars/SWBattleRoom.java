package com.hawk.game.lianmengstarwars;

import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import com.google.common.collect.ImmutableMap;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.SWBattleCfg;
import com.hawk.game.config.SWHealCfg;
import com.hawk.game.config.StarWarsConstCfg;
import com.hawk.game.config.WorldMarchConstProperty;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.lianmengstarwars.SWConst.SWOverType;
import com.hawk.game.lianmengstarwars.SWConst.SWState;
import com.hawk.game.lianmengstarwars.entity.SWMarchEntity;
import com.hawk.game.lianmengstarwars.msg.SWQuitReason;
import com.hawk.game.lianmengstarwars.player.ISWPlayer;
import com.hawk.game.lianmengstarwars.player.SWVideoPlayer;
import com.hawk.game.lianmengstarwars.player.module.guildformation.SWGuildFormationObj;
import com.hawk.game.lianmengstarwars.roomstate.ISWBattleRoomState;
import com.hawk.game.lianmengstarwars.worldmarch.ISWWorldMarch;
import com.hawk.game.lianmengstarwars.worldmarch.SWAssistanceSingleMarch;
import com.hawk.game.lianmengstarwars.worldmarch.SWAttackPlayerMarch;
import com.hawk.game.lianmengstarwars.worldmarch.SWBuildingMarchMass;
import com.hawk.game.lianmengstarwars.worldmarch.SWBuildingMarchMassJoin;
import com.hawk.game.lianmengstarwars.worldmarch.SWBuildingMarchSingle;
import com.hawk.game.lianmengstarwars.worldmarch.SWMassJoinSingleMarch;
import com.hawk.game.lianmengstarwars.worldmarch.SWMassSingleMarch;
import com.hawk.game.lianmengstarwars.worldmarch.SWSpyMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWMassJoinMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWMassMarch;
import com.hawk.game.lianmengstarwars.worldmarch.submarch.ISWReportPushMarch;
import com.hawk.game.lianmengstarwars.worldpoint.ISWBuilding;
import com.hawk.game.lianmengstarwars.worldpoint.SWCommandCenter;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.msg.CalcSWDeadArmy;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SW.PBSWGameInfoSync;
import com.hawk.game.protocol.SW.PBSWGameOver;
import com.hawk.game.protocol.SW.PBSWGuildInfo;
import com.hawk.game.protocol.SW.PBSWPlayerInfo;
import com.hawk.game.protocol.SW.PBSWPlayerQuitRoom;
import com.hawk.game.protocol.SW.PBSWSecondMapResp;
import com.hawk.game.protocol.SW.PBSWVideoPackage;
import com.hawk.game.protocol.World.MarchEvent;
import com.hawk.game.protocol.World.WorldMarchRelation;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.protocol.World.WorldMarchType;
import com.hawk.game.protocol.World.WorldMoveCityResp;
import com.hawk.game.protocol.World.WorldPointSync;
import com.hawk.game.protocol.World.WorldPointType;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.starwars.SWGuildData;
import com.hawk.game.service.starwars.SWRoomData;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsConst.SWRoomState;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;

/**
 * 虎牢关
 * 
 * @author lwt
 * @date 2018年10月26日
 */
public class SWBattleRoom extends HawkAppObj {
	public boolean IS_GO_MODEL;
	final int MINITEMILLSECS = 66 * 1000;
	private SWThread thread;
	/** 结算信息 */
	private SWExtraParam extParm;
	private boolean gameOver;
	private int battleCfgId;
	private long curTimeMil;
	private String winGuild;
	private SWOverType overType = SWOverType.TIMEOVER;
	/** 游戏内玩家 不包含退出的 */
	private Map<String, ISWPlayer> playerMap = new ConcurrentHashMap<>();
	/** 退出战场的 */
	private Map<String, ISWPlayer> playerQuitMap = new ConcurrentHashMap<>();
	// /** 战场中的对象 玩家城点, 怪, npc建筑等等 */
	// private Map<Integer, ISWWorldPoint> viewPoints = new ConcurrentHashMap<>();

	private Map<String, ISWWorldMarch> worldMarches = new ConcurrentHashMap<>();
	private ISWBattleRoomState state;
	private long createTime;
	private long startTime;
	private long overTime;
	private long nextSaveVideow;
	private SWVideoPlayer videoPlayer;
	private int videoIndex;
	private PBSWVideoPackage.Builder videoPackage;
	private SWWorldPointService worldPointService;
	/** 下次主动推详情 */
	private long nextSyncToPlayer;
	private ImmutableMap<EffType, Integer> buff;
	/** 上次同步战场详情 */
	private PBSWGameInfoSync lastSyncpb;
	private PBSWGameInfoSync lastBilingpb;
	private long lastSyncMap;
	private PBSWSecondMapResp lastMappb;

	private long nextLoghonor;

	private Map<String, GuildStaticInfo> guildStatisticMap = new HashMap<>();
	private boolean hasNotBrodcast_SW_182 = true;
	private boolean hasNotBrodcast_SW_183 = true;

	private int centerX, centerY;

	private Map<String, SWGuildFormationObj> guildFormationObjmap = new HashMap<>();
	private Map<String, Long> guildIdPower = new HashMap<>();
	public SWBattleRoom(HawkXID xid) {
		super(xid);
	}
	
	@Override
	public boolean onProtocol(HawkProtocol protocol) {
		SWProtocol pro = (SWProtocol) protocol;
		ISWPlayer player = pro.getPlayer();
		player.onProtocol(pro.getSource());

		return true;
	}

	/**
	 * 副本是否马上要结束了
	 * 
	 * @return
	 */
	public boolean maShangOver() {
		return getOverTime() - HawkTime.getMillisecond() < 3000;
	}

	public void sync(ISWPlayer player) {
		if (Objects.nonNull(lastSyncpb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_GAME_SYNC, lastSyncpb.toBuilder()));
			return;
		}

		PBSWGameInfoSync bul = buildSyncPB();
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_GAME_SYNC, bul.toBuilder()));

	}

	public int getGuildMemberCount(String guildId) {
		if (guildStatisticMap.containsKey(guildId)) {
			return guildStatisticMap.get(guildId).playerCountA;
		}
		return 0;
	}

	public PBSWGameInfoSync buildSyncPB() {
		try {
			PBSWGameInfoSync.Builder bul = PBSWGameInfoSync.newBuilder();
			bul.setGameStartTime(startTime);
			bul.setGameOverTime(overTime);

			for (ISWBuilding build : getSWBuildingList()) {
				bul.addBuilddetail(build.toDetailBuilder(videoPlayer));
			}
			Map<String, GuildStaticInfo> gsMap = new HashMap<>();

			List<ISWPlayer> all = new ArrayList<>(playerMap.values());
			all.addAll(playerQuitMap.values());
			for (ISWPlayer p : all) {
				try {
					if (p.getGuildId() == null) {
						continue;
					}
					GuildStaticInfo staInfo;
					if (gsMap.containsKey(p.getGuildId())) {
						staInfo = gsMap.get(p.getGuildId());
					} else {
						staInfo = new GuildStaticInfo();
						staInfo.campAGuild = p.getGuildId();
						staInfo.campAGuildName = p.getGuildName();
						staInfo.campAGuildTag = p.getGuildTag();
						staInfo.campAServerId = p.getMainServerId();
						staInfo.campAguildFlag = p.getGuildFlag();
						staInfo.campAGuildWarCount = getGuildWarMarch(staInfo.campAGuild).size();
						if (p.isCsPlayer()) {
							staInfo.csServerId = p.getMainServerId();
						}
						gsMap.put(p.getGuildId(), staInfo);
					}

					staInfo.honorA += p.getGuildHonor();
					staInfo.killHonorA += p.getKillHonor();
					if (!playerQuitMap.containsKey(p.getId())) { // 玩家 没有退出
						staInfo.playerCountA++;
						staInfo.playerIds.add(p.getId());
					}
					PBSWPlayerInfo.Builder prc = PBSWPlayerInfo.newBuilder();
					prc.setGuildId(p.getGuildId());
					prc.setName(p.getName());
					prc.setHonor(p.getHonor());
					prc.setGuildTag(p.getGuildTag());
					prc.setGuildHonor(p.getGuildHonor());
					prc.setPlayerId(p.getId());
					prc.setKillHonor((int) (p.getKillPower() + p.getDeadPower()));
					prc.setKillPower((int)p.getKillPower());
					prc.setDeadPower(p.getDeadPower());
					bul.addPlayerInfo(prc);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}

			for (GuildStaticInfo staInfo : gsMap.values()) {
				try {
					for (ISWWorldPoint viewp : getViewPoints()) {
						if (viewp instanceof ISWBuilding) {
							ISWBuilding build = (ISWBuilding) viewp;
							staInfo.honorA += build.getControlGuildHonorMap().getOrDefault(staInfo.campAGuild, 0D);
							staInfo.buildControlHonorA += build.getControlGuildHonorMap().getOrDefault(staInfo.campAGuild, 0D);
							if (build.getPointType() == WorldPointType.SW_HEADQUARTERS) {
								staInfo.centerControlA = build.getControlGuildTimeMap().get(staInfo.campAGuild);
							}
							if (build.underGuildControl(staInfo.campAGuild)) {
								staInfo.perMinA += build.getGuildHonorPerSecond() * 60 + 0.1;
								staInfo.buildCountA++;
								if (viewp instanceof SWCommandCenter) {
									staInfo.towerCnt++;
								}
							}
						}
					}
					int honorRank = honorRank(staInfo.campAGuild);
					PBSWGuildInfo.Builder aInfo = PBSWGuildInfo.newBuilder().setServerId(staInfo.campAServerId)
							.setGuildFlag(staInfo.campAguildFlag).setGuildName(staInfo.campAGuildName)
							.setGuildTag(staInfo.campAGuildTag).setGuildId(staInfo.campAGuild).setHonor(staInfo.honorA)
							.setPerMin(staInfo.perMinA).setBuildCount(staInfo.buildCountA).setPlayerCount(staInfo.playerCountA)
							.setCenterControl(staInfo.centerControlA).setBuildControlHonor(staInfo.buildControlHonorA)
							.setKillHonor(staInfo.killHonorA).setHonorRank(honorRank);
					if (staInfo.towerCnt == 1) {
						aInfo.setAtkPresidentBuff(SWCommandCenter.getCfg().getControlCountBuff1());
					} else if (staInfo.towerCnt > 1) {
						aInfo.setAtkPresidentBuff(SWCommandCenter.getCfg().getControlCountBuff2());
					}

					bul.addGuildInfo(aInfo);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			this.guildStatisticMap = gsMap;
			lastBilingpb = bul.build();
			lastSyncpb = bul.clearPlayerInfo().build();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return lastSyncpb;
	}

	private int honorRank(String guildId) {
		// 同分的按联盟战力排
		List<PBSWGuildInfo> guildInfoList = lastBilingpb.getGuildInfoList().stream()
				.sorted(Comparator.comparingLong(PBSWGuildInfo::getHonor).thenComparingLong(ginfo -> guildIdPower.getOrDefault(ginfo.getGuildId(), 0L)).reversed())
				.collect(Collectors.toList());

		int honorRank = Integer.MAX_VALUE;
		for (int i = 0; i < guildInfoList.size(); i++) {
			PBSWGuildInfo ginfo = guildInfoList.get(i);
			if (ginfo.getGuildId().equals(guildId)) {
				honorRank = i + 1;
			}
		}
		return honorRank;
	}

	public void getSecondMap(ISWPlayer player) {
		if (curTimeMil - lastSyncMap < 3000 && Objects.nonNull(lastMappb)) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_SECOND_MAP_S, lastMappb.toBuilder()));
			return;
		}
		PBSWSecondMapResp.Builder bul = PBSWSecondMapResp.newBuilder();
		for (ISWWorldPoint point : getViewPoints()) {
			bul.addPoints(point.toBuilder(player));
		}
		bul.setGameStartTime(startTime);
		bul.setGameOverTime(overTime);

		bul.addAllGuildInfo(lastSyncpb.getGuildInfoList());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_SECOND_MAP_S, bul));

		lastMappb = bul.build();
		lastSyncMap = curTimeMil;
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

		// 计算国家医院
		calcNationalHospital(atkPlayers, battleOutcome.getBattleArmyMapAtk());
		// 防守方剩余兵力
		// 计算损失兵力
		calcNationalHospital(defPlayers, battleOutcome.getBattleArmyMapDef());
	}

	/**计算国家医院收治*/
	private void calcNationalHospital(List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
		for (Player ppp : battlePlayers) {
			ISWPlayer pl = (ISWPlayer) ppp;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			final int dead = pl.getDeadCnt();
			int healRate = HawkConfigManager.getInstance().getConfigIterator(SWHealCfg.class).stream()
					.filter(cfg -> cfg.getDeadNumMin() <= dead && cfg.getDeadNumMax() >= dead)
					.mapToInt(cfg -> cfg.getHealRate())
					.findAny()
					.orElse(10000);

			double savePct = healRate * GsConst.EFF_PER;
			for (ArmyInfo army : leftList) {// 进医院的数量
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				if (cfg.getSoldierType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE) { // 兵种1 -8
					army.setTszzNationalHospital((int) (army.getDeadCount() * savePct));
				}
			}
			HawkApp.getInstance().postMsg(ppp, CalcSWDeadArmy.valueOf(leftList));
		}
	}

	private void calcSelfLosePower(List<Player> battlePlayers, Map<String, List<ArmyInfo>> leftArmyMap) {
		for (Player ppp : battlePlayers) {
			ISWPlayer pl = (ISWPlayer) ppp;
			int killPow = 0;
			int deadCnt = 0;
			int deadPower = 0;
			List<ArmyInfo> leftList = leftArmyMap.get(pl.getId());
			if (leftList == null) {
				continue;
			}
			for (ArmyInfo army : leftList) {
				BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, army.getArmyId());
				if (cfg.getSoldierType() == SoldierType.TANK_SOLDIER_1) {
					killPow += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
				}
				if (cfg.getSoldierType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE) {
					deadPower += cfg.getPower() * (army.getDeadCount() + army.getWoundedCount());
				}
				if (cfg.getLevel() > getCfg().getHospitalMinLevel() && cfg.getSoldierType().getNumber() <= SoldierType.CANNON_SOLDIER_8_VALUE) { // 兵种1 -8
					deadCnt += army.getDeadCount();
				}
			}
			pl.setHurtTankPower(pl.getHurtTankPower() + killPow);
			pl.setDeadCnt(pl.getDeadCnt() + deadCnt);
			pl.setDeadPower(pl.getDeadPower() + deadPower);
		}
	}

	/** 自己的击杀,击伤 */
	private void calcKillAndHurtPower(List<Player> battlePlayers, Map<String, Map<Integer, Integer>> battleKillMap, Map<String, Map<Integer, Integer>> battleHurtMap) {
		for (Player ppp : battlePlayers) {
			ISWPlayer pl = (ISWPlayer) ppp;
			double killPow = 0;
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
			pl.setKillPower((int) (pl.getKillPower() + Math.ceil(killPow)));
		}
	}

	/** 初始化, 创建npc等 */
	public void init() {
		HawkAssert.notNull(extParm);
		HawkLog.logPrintln("SW GAME CREATE parm: {}", extParm.toString());
		IS_GO_MODEL = extParm.isDebug();
		nextSyncToPlayer = createTime + 5000;

		worldPointService = new SWWorldPointService(this);
		worldPointService.init();

		videoPlayer = new SWVideoPlayer(HawkXID.nullXid());
		videoPlayer.setParent(this);

		buff = getCfg().getEffectMap(getExtParm().getWarType());
		startTime = createTime + getCfg().getPrepairTime() * 1000;
		this.nextLoghonor = startTime + SWConst.MINUTE_MICROS;
		long periodTime = StarWarsConstCfg.getInstance().getPerioTime() * 1L;
		this.addTickable(new HawkPeriodTickable(periodTime) {

			@Override
			public void onPeriodTick() {
				updateRoomActiveTime();
			}
		});

		createNextVideoPackage();
		
		initGuildIdPower();

	}

	private void initGuildIdPower() {
		try {
			SWRoomData roomData = StarWarsActivityService.getInstance().getLocalRoomData(extParm.getWarType());
			if (roomData == null) {
				return;
			}

			int termId = StarWarsActivityService.getInstance().getTermId();
			Map<String, SWGuildData> dataMap = RedisProxy.getInstance().getSWGuildDatas(termId, roomData.getGuildList());
			this.guildIdPower = dataMap.values().stream().collect(Collectors.toMap(SWGuildData::getId, SWGuildData::getPower));

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	public long battleTime() {
		return overTime - startTime;
	}

	/** 创建下一个录像包 */
	private void createNextVideoPackage() {
		try {
			// System.out.println("createNextVideoPackage");
			PBSWVideoPackage.Builder vpack = PBSWVideoPackage.newBuilder();
			vpack.setBattleId(getId());
			vpack.setIslast(false);
			vpack.setIndex(videoIndex);

			videoPackage = vpack;
			videoPlayer.sendHeadVideoProtocol(HawkProtocol.valueOf(HP.code.SW_ENTER_GAME_SUCCESS));
			// 录像观看需求取消. 先注掉. 注意. 代码是新测可用的.
			// {
			// WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			// for (ISWWorldPoint point : getViewPoints()) {
			// builder.addPoints(point.toBuilder(videoPlayer));
			// }
			// HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			// videoPlayer.sendHeadVideoProtocol(protocol);
			// }
			// {
			// // 自己的行军不走这种同步模式
			// MarchEventSync.Builder builder = MarchEventSync.newBuilder();
			// builder.setEventType(MarchEvent.MARCH_UPDATE.getNumber());
			// for (ISWWorldMarch march : getWorldMarchList()) {
			// WorldMarchRelation relation = WorldMarchRelation.NONE;
			// MarchData.Builder dataBuilder = MarchData.newBuilder();
			// dataBuilder.setMarchId(march.getMarchId());
			// dataBuilder.setMarchPB(march.toBuilder(relation));
			// builder.addMarchData(dataBuilder);
			// }
			//
			// videoPlayer.sendHeadVideoProtocol(HawkProtocol.valueOf(HP.code.WORLD_MARCH_EVENT_SYNC_VALUE, builder));
			// }

			videoIndex++;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		nextSaveVideow = HawkTime.getMillisecond() + MINITEMILLSECS;
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
		long curTime = HawkTime.getMillisecond();
		if (curTime > this.getCreateTime() && curTime < this.getOverTime()) {
			int termId = StarWarsActivityService.getInstance().getActivityData().getTermId();
			SWWarType warType = extParm.getWarType();
			SWRoomData roomData = RedisProxy.getInstance().getSWRoomData(this.getXid().getUUID(), termId, warType.getNumber());
			if (roomData == null || roomData.getRoomState() != SWRoomState.INITED) {
				return;
			}

			roomData.setLastActiveTime(curTime);
			RedisProxy.getInstance().updateSWRoomData(roomData, termId, warType.getNumber());
		}
	}

	public void doMoveCitySuccess(ISWPlayer player, int[] targetPoint) {
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
		for (ISWWorldMarch march : worldMarches.values()) {
			if (march instanceof ISWReportPushMarch) {
				((ISWReportPushMarch) march).removeAttackReport(player.getId());
			}
		}

		worldPointService.removeViewPoint(player);
		player.setPos(targetPoint);
		worldPointService.addViewPoint(player);
		player.getPush().syncPlayerWorldInfo();
		player.moveCityCDSync();
	}

	public ISWWorldMarch startMarch(ISWPlayer player, ISWWorldPoint fPoint, ISWWorldPoint tPoint,
			WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		// 生成行军
		ISWWorldMarch march = genMarch(player, fPoint, tPoint, marchType, targetId, waitTime, effParams);
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

		return march;
	}

	/** 生成一个行军对象 */
	public ISWWorldMarch genMarch(ISWPlayer player, ISWWorldPoint fPoint, ISWWorldPoint tPoint,
			WorldMarchType marchType, String targetId, int waitTime, EffectParams effParams) {
		long startTime = HawkTime.getMillisecond();

		SWMarchEntity march = new SWMarchEntity();
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
		ISWWorldMarch iWorldMarch;
		// TODO 有时间封装一下 组装
		switch (marchType) {
		case ATTACK_PLAYER:
			iWorldMarch = new SWAttackPlayerMarch(player);
			break;
		case SPY:
			iWorldMarch = new SWSpyMarch(player);
			break;
		case ASSISTANCE:
			iWorldMarch = new SWAssistanceSingleMarch(player);
			break;
		case MASS:
			iWorldMarch = new SWMassSingleMarch(player);
			break;
		case MASS_JOIN:
			iWorldMarch = new SWMassJoinSingleMarch(player);
			break;
		case SW_HEADQUARTERS_SINGLE: // = 107; // 司令部
		case SW_COMMAND_CENTER_SINGLE: // = 105; // 指挥部
			iWorldMarch = new SWBuildingMarchSingle(player);
			((SWBuildingMarchSingle) iWorldMarch).setMarchType(marchType);
			break;

		case SW_HEADQUARTERS_MASS:// = 117; // 司令部
			iWorldMarch = new SWBuildingMarchMass(player);
			((SWBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.SW_HEADQUARTERS_MASS);
			((SWBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.SW_HEADQUARTERS_MASS_JOIN);
			break;
		case SW_COMMAND_CENTER_MASS:// = 115; // 指挥部
			iWorldMarch = new SWBuildingMarchMass(player);
			((SWBuildingMarchMass) iWorldMarch).setMarchType(WorldMarchType.SW_COMMAND_CENTER_MASS);
			((SWBuildingMarchMass) iWorldMarch).setJoinMassType(WorldMarchType.SW_COMMAND_CENTER_MASS_JOIN);
			break;

		case SW_HEADQUARTERS_MASS_JOIN:// = 127; // 司令部
		case SW_COMMAND_CENTER_MASS_JOIN:// = 125; // 指挥部
			iWorldMarch = new SWBuildingMarchMassJoin(player);
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

		return iWorldMarch;
	}

	public SWBattleCfg getCfg() {
		return HawkConfigManager.getInstance().getKVInstance(SWBattleCfg.class);
	}

	public long getPlayerMarchCount(String playerId) {
		return worldMarches.values().stream().filter(m -> Objects.equals(m.getPlayerId(), playerId)).count();
	}

	public ISWWorldMarch getPlayerMarch(String playerId, String marchId) {
		return worldMarches.values().stream()
				.filter(m -> Objects.equals(m.getPlayerId(), playerId) && Objects.equals(m.getMarchId(), marchId))
				.findAny().orElse(null);
	}

	public List<ISWWorldMarch> getPlayerMarches(String playerId, WorldMarchType... types) {
		return getPlayerMarches(playerId, null, null, null, types);
	}

	public List<ISWWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, null, null, types);
	}

	public List<ISWWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchType... types) {
		return getPlayerMarches(playerId, status1, status2, null, types);
	}

	public <T extends ISWBuilding> List<T> getSWBuildingByClass(Class<T> type) {
		return worldPointService.getSWBuildingByClass(type);
	}

	public List<ISWBuilding> getSWBuildingList() {
		return worldPointService.getBuildingList();
	}

	public List<ISWWorldMarch> getPlayerMarches(String playerId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ISWWorldMarch> result = new ArrayList<>();
		for (ISWWorldMarch ma : worldMarches.values()) {
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

	public ISWWorldMarch getMarch(String marchId) {
		return worldMarches.get(marchId);
	}

	/** 联盟战争展示行军 */
	public List<ISWWorldMarch> getGuildWarMarch(String guildId) {
		List<ISWWorldMarch> result = new ArrayList<>();
		for (ISWWorldMarch march : getWorldMarchList()) {
			try {
				boolean bfalse = march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE
						|| march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE;
				if (!bfalse) {
					continue;
				}
				if (!march.needShowInGuildWar()) {
					continue;
				}
				ISWWorldPoint point = march.getTerminalWorldPoint();
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

				if (march instanceof SWBuildingMarchSingle) {
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

	public List<ISWWorldMarch> getPointMarches(int pointId, WorldMarchType... types) {
		return getPointMarches(pointId, null, null, null, types);
	}

	public List<ISWWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchType... types) {
		return getPointMarches(pointId, status1, null, null, types);
	}

	public List<ISWWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchType... types) {
		return getPointMarches(pointId, status1, status2, null, types);
	}

	/** 取得 终点在该点行军 */
	public List<ISWWorldMarch> getPointMarches(int pointId, WorldMarchStatus status1, WorldMarchStatus status2,
			WorldMarchStatus status3, WorldMarchType... types) {
		boolean free = Objects.isNull(status1) && Objects.isNull(status2) && Objects.isNull(status3);
		int[] xy = GameUtil.splitXAndY(pointId);
		int x = xy[0];
		int y = xy[1];
		List<WorldMarchType> typeList = Arrays.asList(types);
		List<ISWWorldMarch> result = new LinkedList<>();
		for (ISWWorldMarch ma : worldMarches.values()) {
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
	public Optional<ISWWorldPoint> getWorldPoint(int x, int y) {
		return Optional.ofNullable(worldPointService.getViewPoints().get(GameUtil.combineXAndY(x, y)));
	}

	public Optional<ISWWorldPoint> getWorldPoint(int pointId) {
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
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		try {
			if (curTimeMil > nextSyncToPlayer) {
				nextSyncToPlayer = curTimeMil + 3000;
				PBSWGameInfoSync builder = buildSyncPB();
				// 夸服玩家和本服分别广播
				for (GuildStaticInfo gstic : getGuildStatisticMap().values()) {
					if (StringUtils.isEmpty(gstic.getCsServerId())) {
						continue;
					}
					CrossProxy.getInstance().broadcastProtocolV2(gstic.getCsServerId(), gstic.getPlayerIds(),
							HawkProtocol.valueOf(HP.code.SW_GAME_SYNC, builder.toBuilder()));
				}

				for (ISWPlayer p : playerMap.values()) {
					if (!p.isCsPlayer()) {
						sync(p);
					}
				}
				videoPlayer.sendBroadVideoProtocol(HawkProtocol.valueOf(HP.code.SW_GAME_SYNC, builder.toBuilder()));
				if (IS_GO_MODEL) {
					HawkLog.logPrintln("行军总数量  " + worldMarches.size());
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (getCurTimeMil() > nextLoghonor) {
				this.nextLoghonor += SWConst.MINUTE_MICROS;
				List<PBSWGuildInfo> guildList = lastSyncpb.getGuildInfoList();
				for (PBSWGuildInfo ginfo : guildList) {
					String logGuild = ginfo.getGuildId();
					HawkLog.logPrintln("SW GUILD HONOR battleId: {} guildId:{} guildName:{} honor:{}", getXid().getUUID(), logGuild, ginfo.getGuildName(),
							ginfo.getCenterControl() / 1000);
				}
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}

		try {
			if (getCurTimeMil() > nextSaveVideow) {
				SWRoomManager.getInstance().saveSWVideo(videoPackage.build());
				createNextVideoPackage();
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return super.onTick();
	}

	/** 玩家进入世界 */
	public void enterWorld(ISWPlayer player) {
		state.enterWorld(player);
	}

	/**
	 * 副本中玩家更新点
	 * 
	 * @param point
	 */
	public void worldPointUpdate(ISWWorldPoint point) {
		for (ISWPlayer pla : getPlayerList(SWState.GAMEING)) {
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(pla));
			HawkProtocol protocol = HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder);
			pla.sendProtocol(protocol);
		}
		{
			WorldPointSync.Builder builder = WorldPointSync.newBuilder();
			builder.addPoints(point.toBuilder(videoPlayer));
			videoPlayer.sendBroadVideoProtocol(HawkProtocol.valueOf(HP.code.WORLD_POINT_SYNC_VALUE, builder));
		}
	}

	public void joinRoom(ISWPlayer player) {
		if (Objects.isNull(player) || player.getParent() != this) {
			return;
		}
		if (playerMap.containsKey(player.getId())) {
			// 进入地图成功
			player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_ENTER_GAME_SUCCESS));
			// enterWorld(player);
			return;
		}
		int[] bornP = null;
		while (bornP == null) {
			bornP = worldPointService.randomFreePoint(randomPoint(), player.getWorldPointRadius());
		}

		player.setPos(bornP);

		player.init();
		SWRoomManager.getInstance().cache(player);
		player.setSwState(SWState.GAMEING);
		player.getPush().pushJoinGame();
		worldPointService.addViewPoint(player);
		playerMap.put(player.getId(), player);
//		buildSyncPB();
		// 进入地图成功
		player.sendProtocol(HawkProtocol.valueOf(HP.code.SW_ENTER_GAME_SUCCESS));

		DungeonRedisLog.log(player.getId(), "roomId {} guildId:{}", getId(), player.getGuildId());
		DungeonRedisLog.log(getId(), "player:{} guildId:{} serverId:{}", player.getId(), player.getGuildId(), player.getMainServerId());
	}

	public void quitWorld(ISWPlayer quitPlayer, SWQuitReason reason) {
		quitPlayer.setSWQuitReason(reason);
		{ // 弹窗
			PBSWGameOver.Builder builder = PBSWGameOver.newBuilder();
			if (reason == SWQuitReason.LEAVE) {
				builder.setQuitReson(1);
			}
			quitPlayer.sendProtocol(HawkProtocol.valueOf(HP.code.SW_GAME_OVER, builder));
		}

		playerMap.remove(quitPlayer.getId());
		playerQuitMap.put(quitPlayer.getId(), quitPlayer);
		worldPointService.getWorldScene().leave(quitPlayer.getEye().getAoiObjId());
		boolean inWorld = worldPointService.removeViewPoint(quitPlayer);
		if (inWorld) {
			// 删除行军
			cleanCityPointMarch(quitPlayer);

			for (ISWPlayer gamer : playerMap.values()) {
				PBSWPlayerQuitRoom.Builder bul = PBSWPlayerQuitRoom.newBuilder();
				bul.setQuiter(BuilderUtil.buildSnapshotData(quitPlayer));
				gamer.sendProtocol(HawkProtocol.valueOf(HP.code.SW_PLAYER_QUIT, bul));
			}
		}

		// if (reason == SWQuitReason.LEAVE) {
		// ChatParames parames =
		// ChatParames.newBuilder().setPlayer(quitPlayer).setChatType(ChatType.CHAT_SW).setKey(NoticeCfgId.SW_PLAYER_QUIT).addParms(quitPlayer.getName())
		// .build();
		// addWorldBroadcastMsg(parames);
		// }
		quitPlayer.getPush().pushGameOver();

	}

	public void cleanCityPointMarch(ISWPlayer quitPlayer) {
		try {
			List<ISWWorldMarch> quiterMarches = getPlayerMarches(quitPlayer.getId());
			for (ISWWorldMarch march : quiterMarches) {
				if (march instanceof ISWMassMarch) {
					march.getMassJoinMarchs(true).forEach(ISWWorldMarch::onMarchCallback);
				}
				if (march instanceof ISWMassJoinMarch) {
					Optional<ISWMassMarch> massMarch = ((ISWMassJoinMarch) march).leaderMarch();
					if (massMarch.isPresent()) {
						ISWMassMarch leadermarch = massMarch.get();
						if (leadermarch.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_MARCH_VALUE) {
							leadermarch.getMassJoinMarchs(true).forEach(ISWWorldMarch::onMarchCallback);
							leadermarch.onMarchCallback();
						}
					}
				}
				march.onMarchBack();
				march.remove();
			}

			List<ISWWorldMarch> pms = getPointMarches(quitPlayer.getPointId());
			for (ISWWorldMarch march : pms) {
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

	public List<ISWPlayer> getPlayerList(SWState st1) {
		return getPlayerList(st1, null, null);
	}

	public List<ISWPlayer> getPlayerList(SWState st1, SWState st2) {
		return getPlayerList(st1, st2, null);
	}

	public List<ISWPlayer> getPlayerList(SWState st1, SWState st2, SWState st3) {
		List<ISWPlayer> result = new ArrayList<>();
		for (ISWPlayer player : playerMap.values()) {
			SWState state = player.getSwState();
			if (state == st1 || state == st2 || state == st3) {
				result.add(player);
			}
		}
		return result;
	}

	public ISWPlayer getPlayer(String id) {
		return playerMap.get(id);
	}

	public void setPlayerList(List<ISWPlayer> playerList) {
		throw new UnsupportedOperationException();
	}

	public List<ISWWorldPoint> getViewPoints() {
		return new ArrayList<>(worldPointService.getViewPoints().values());
	}

	public void setViewPoints(List<ISWWorldPoint> viewPoints) {
		throw new UnsupportedOperationException();
	}

	public List<ISWWorldMarch> getWorldMarchList() {
		ArrayList<ISWWorldMarch> result = new ArrayList<>(worldMarches.values());
		// Collections.sort(result, Comparator.comparingLong(ISWWorldMarch::getStartTime));
		return result;
	}

	public int getWorldMarchCount() {
		return worldMarches.size();
	}

	public void removeMarch(ISWWorldMarch march) {
		worldMarches.remove(march.getMarchId());
	}

	public void addMarch(ISWWorldMarch march) {
		worldMarches.put(march.getMarchId(), march);
	}

	public void setWorldMarchList(List<ISWWorldMarch> worldMarchList) {
		throw new UnsupportedOperationException();
	}

	public ISWBattleRoomState getState() {
		return state;
	}

	public void setState(ISWBattleRoomState state) {
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

	public SWExtraParam getExtParm() {
		return extParm;
	}

	public void setExtParm(SWExtraParam extParm) {
		this.extParm = extParm;
	}

	public void sendChatMsg(ISWPlayer player, String chatMsg, String voiceId, int voiceLength, ChatType type) {
		ChatMsg chatMsgInfo = ChatService.getInstance().createMsgObj(player).setType(type.getNumber())
				.setChatMsg(chatMsg).setVoiceId(voiceId).setVoiceLength(voiceLength).build();
		broadcastChatMsg(chatMsgInfo);
	}

	public void addWorldBroadcastMsg(ChatParames parames) {
		ChatMsg pbMsg = parames.toPBMsg();
		broadcastChatMsg(pbMsg);
	}

	private void broadcastChatMsg(ChatMsg pbMsg) {
		Set<Player> tosend = new HashSet<>(getPlayerList(SWState.GAMEING));
		boolean isGuildMsg = ChatService.getInstance().isGuildMsg(pbMsg);
		if (isGuildMsg || pbMsg.getType() == ChatType.CHAT_FUBEN_TEAM_VALUE) {
			tosend = tosend.stream().filter(p -> Objects.equals(p.getGuildId(), pbMsg.getAllianceId())).collect(Collectors.toSet());
		}
		ChatService.getInstance().sendChatMsg(Arrays.asList(pbMsg), tosend);
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

	public List<ISWPlayer> getPlayerQuitList() {
		return new ArrayList<>(playerQuitMap.values());
	}

	public PBSWGameInfoSync getLastSyncpb() {
		return lastSyncpb;
	}

	public void setLastSyncpb(PBSWGameInfoSync lastSyncpb) {
		this.lastSyncpb = lastSyncpb;
	}

	public Map<String, GuildStaticInfo> getGuildStatisticMap() {
		return guildStatisticMap;
	}

	public PBSWVideoPackage.Builder getVideoPackage() {
		return videoPackage;
	}

	public SWVideoPlayer getVideoPlayer() {
		return videoPlayer;
	}

	public String getWinGuild() {
		return winGuild;
	}

	public void setWinGuild(String winGuild) {
		this.winGuild = winGuild;
	}

	public SWOverType getOverType() {
		return overType;
	}

	public void setOverType(SWOverType overType) {
		this.overType = overType;
	}

	public boolean isHasNotBrodcast_SW_182() {
		return hasNotBrodcast_SW_182;
	}

	public void setHasNotBrodcast_SW_182(boolean hasNotBrodcast_SW_182) {
		this.hasNotBrodcast_SW_182 = hasNotBrodcast_SW_182;
	}

	public boolean isHasNotBrodcast_SW_183() {
		return hasNotBrodcast_SW_183;
	}

	public void setHasNotBrodcast_SW_183(boolean hasNotBrodcast_SW_183) {
		this.hasNotBrodcast_SW_183 = hasNotBrodcast_SW_183;
	}

	public ImmutableMap<EffType, Integer> getBuff() {
		return buff;
	}

	public SWThread getThread() {
		return thread;
	}

	public void setThread(SWThread thread) {
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

	public SWWorldPointService getWorldPointService() {
		return worldPointService;
	}

	public GuildFormationObj getGuildFormation(String guildId) {
		if (guildFormationObjmap.containsKey(guildId)) {
			SWGuildFormationObj result = guildFormationObjmap.get(guildId);
			if (getCurTimeMil() - result.getLastCheckUpdate() < 5000) {
				return result;
			}
		}
		SWGuildFormationObj obj = guildFormationObjmap.getOrDefault(guildId, new SWGuildFormationObj());
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

	public PBSWGameInfoSync getLastBilingpb() {
		return lastBilingpb;
	}

	public void setLastBilingpb(PBSWGameInfoSync lastBilingpb) {
		this.lastBilingpb = lastBilingpb;
	}

}
