package com.hawk.game.guild.championship;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.app.HawkAppCfg;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;

import com.hawk.game.GsConfig;
import com.hawk.game.battle.BattleOutcome;
import com.hawk.game.battle.BattleService;
import com.hawk.game.battle.TemporaryMarch;
import com.hawk.game.battle.battleIncome.impl.PvpBattleIncome;
import com.hawk.game.battle.effect.BattleConst;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ChampionshipAwardCfg;
import com.hawk.game.config.ChampionshipConstCfg;
import com.hawk.game.config.ChampionshipDebuffCfg;
import com.hawk.game.config.ChampionshipTimeCfg;
import com.hawk.game.entity.ArmyEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.championship.GCConst.GCBattleStage;
import com.hawk.game.guild.championship.GCConst.GCBattleState;
import com.hawk.game.guild.championship.GCConst.GCGradeRankType;
import com.hawk.game.guild.championship.GCConst.GCGuildGrade;
import com.hawk.game.guild.championship.member.CHAMPlayer;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.march.ArmyInfo;
import com.hawk.game.module.PlayerMarchModule;
import com.hawk.game.player.Player;
import com.hawk.game.player.hero.PlayerHero;
import com.hawk.game.player.supersoldier.SuperSoldier;
import com.hawk.game.protocol.Armour.ArmourBriefInfo;
import com.hawk.game.protocol.Armour.ArmourSuitType;
import com.hawk.game.protocol.Army.ArmySoldierPB;
import com.hawk.game.protocol.Const.BattleSkillType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.GuildChampionship.GCBattelInfo;
import com.hawk.game.protocol.GuildChampionship.GCGetGroupRankResp;
import com.hawk.game.protocol.GuildChampionship.GCGetMemberMarchResp;
import com.hawk.game.protocol.GuildChampionship.GCGetRankResp;
import com.hawk.game.protocol.GuildChampionship.GCGuildBattle;
import com.hawk.game.protocol.GuildChampionship.GCGuildInfo;
import com.hawk.game.protocol.GuildChampionship.GCPageInfo;
import com.hawk.game.protocol.GuildChampionship.GCPlayerInfo;
import com.hawk.game.protocol.GuildChampionship.GCRankInfo;
import com.hawk.game.protocol.GuildChampionship.GCRankType;
import com.hawk.game.protocol.GuildChampionship.GCRewardState;
import com.hawk.game.protocol.GuildChampionship.GCSelfRank;
import com.hawk.game.protocol.GuildChampionship.GCStageBattle;
import com.hawk.game.protocol.GuildChampionship.GCState;
import com.hawk.game.protocol.GuildChampionship.GCStateInfo;
import com.hawk.game.protocol.GuildChampionship.GetHistoryBattleResp;
import com.hawk.game.protocol.GuildChampionship.PBChampionEff;
import com.hawk.game.protocol.GuildChampionship.PBChampionPlayer;
import com.hawk.game.protocol.GuildChampionship.PBChampionSoldier;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Hero.PBHeroInfo;
import com.hawk.game.protocol.MechaCore.MechaCoreSuitType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.World.WorldMarchReq;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.mail.DungeonMailType;
import com.hawk.game.service.mail.FightMailService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.EffectParams;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

/**
 * 锦标赛
 */
public class ChampionshipService extends HawkAppObj {
	private static ChampionshipService instance = null;

	/**
	 * 活动时间信息数据
	 */
	public static GCActivityData activityInfo = new GCActivityData();
	
	/**
	 * 匹配状态信息
	 */
	public static GCMatchState matchServerInfo = new GCMatchState();
	
	/**
	 * 战斗阶段状态信息
	 */
	public static GCWarState fightState = new GCWarState();
	
	/**
	 * 报名出战成员信息
	 */
	public static Map<String, Set<String>> signMemberMap = new ConcurrentHashMap<String, Set<String>>();
	
	/**
	 * 联盟-小组 映射信息
	 */
	public static Map<String, String> groupMapping = new ConcurrentHashMap<>();
	
	/**
	 * 匹配阶段小组对阵信息<小组id,对阵信息>
	 */
	public static Map<String, GCStageBattle> matchStageGroupInfo = new ConcurrentHashMap<>();
	
	/**
	 * 战斗阶段小组对战信息
	 */
	public static Map<String, Map<GCBattleStage,GCStageBattle>> warStageGroupInfo = new ConcurrentHashMap<>();
	
	/**
	 * 个人排行信息
	 */
	public static Map<GCGradeRankType, List<GCRankInfo>> selfRankMap = new ConcurrentHashMap<>();
	
	/**
	 * 联盟段位排行信息
	 */
	public static Map<GCGuildGrade, List<GCRankInfo>> guildRankMap = new ConcurrentHashMap<>();
	
	public static List<GCGroupData> groupList = new ArrayList<>();

	public ChampionshipService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	public static ChampionshipService getInstance() {
		return instance;
	}

	/************************* 活动控制 ******************************/

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		try {
			 // 读取活动阶段数据
			 activityInfo = RedisProxy.getInstance().getGCActivityData();

			// 进行阶段检测
			checkStateChange();
			
			// 活动如果处于匹配完成状态,则拉取匹配数据
			if (activityInfo.getState() != GCState.GC_HIDDEN && activityInfo.getState() != GCState.SHOW) {
				 loadSignInfo();
			}
			
			// 战斗阶段,加载需要本服计算的战斗小组数据
			if(activityInfo.getState() == GCState.WAR || activityInfo.getState() == GCState.END ){
				loadGroupInfo();
			}
			
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();

					// 匹配轮询检测
					if (activityInfo.state == GCState.MATCH) {
						matchTick();
					}
					// 战斗阶段轮询检测
					else if (activityInfo.state == GCState.WAR) {
						warTick();
					}
				}
			});
			long rankTickPeriod = 30000l;
			if(GsConfig.getInstance().isDebug()){
				rankTickPeriod = 10000;
			}
			// 阶段轮询
			addTickable(new HawkPeriodTickable(rankTickPeriod) {
				@Override
				public void onPeriodTick() {
					// 匹配轮询检测
					if (activityInfo.state == GCState.WAR && fightState.state == GCBattleState.ADVANCE_CALC) {
						battleTick();
					}
				}
			});
			
			// 排行拉取
			addTickable(new HawkPeriodTickable(60000) {
				@Override
				public void onPeriodTick() {
					// 匹配轮询检测
					if (activityInfo.state != GCState.GC_HIDDEN) {
						rankTick();
					}
				}
			});

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	protected void rankTick() {
		int termId = activityInfo.getTermId();
		GCState state = activityInfo.getState();
		// 这三个阶段展示上一期排行数据
		if (state == GCState.SHOW || state == GCState.SIGN_UP || state == GCState.MATCH || (state == GCState.WAR && fightState.state != GCBattleState.OPEN_SHOW)) {
			termId = termId - 1;
		}
		loadGuildRank(termId);
		loadSelfRank(termId);
	}
	
	
	private void loadSelfRank(int termId) {
		Map<GCGradeRankType, List<GCRankInfo>> _selfRankMap = new ConcurrentHashMap<>();
		Set<String> playerIds = new HashSet<>();
		Map<GCGradeRankType, Set<Tuple>> tuplesMap = new HashMap<>();
		for (GCGradeRankType rankType : GCGradeRankType.values()) {
			if (rankType == GCGradeRankType.G_KILL) {
				continue;
			}
			String key = getRankKey(termId, rankType, null);
			Set<Tuple> rankSet = RedisProxy.getInstance().getGCRankInfo(key, 100);
			tuplesMap.put(rankType, rankSet);
			for (Tuple tuple : rankSet) {
				playerIds.add(tuple.getElement());
			}
		}
		Map<String, GCPlayerInfo> playerInfoMap = new HashMap<>();
		for (String playerId : playerIds) {
			GCPlayerInfo playerInfo = RedisProxy.getInstance().getGCPlayerData(termId, playerId);
			if (playerInfo != null) {
				playerInfoMap.put(playerId, playerInfo);
			}
		}
		for (Entry<GCGradeRankType, Set<Tuple>> entry : tuplesMap.entrySet()) {
			List<GCRankInfo> rankList = new ArrayList<>();
			GCGradeRankType rankType = entry.getKey();
			int rank = 1;
			for (Tuple tuple : entry.getValue()) {
				String playerId = tuple.getElement();
				long score = (long) tuple.getScore();
				GCPlayerInfo playerInfo = playerInfoMap.get(playerId);
				if (playerInfo == null) {
					continue;
				}
				GCRankInfo.Builder builder = GCRankInfo.newBuilder();
				builder.setId(playerId);
				
				builder.setServerId(playerInfo.getServerId());
				builder.setScore(score);
				builder.setRank(rank);
				builder.setTag(playerInfo.getGuildTag());
				builder.setName(playerInfo.getName());
				builder.setIcon(playerInfo.getIcon());
				if (playerInfo.hasPfIcon()) {
					builder.setPfIcon(playerInfo.getPfIcon());
				}
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				if (player != null) {
					builder.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
				}
				rankList.add(builder.build());
				rank++;
			}
			_selfRankMap.put(rankType, rankList);
		}
		selfRankMap = _selfRankMap;
	}

	private void loadGuildRank(int termId) {
		Map<GCGuildGrade, List<GCRankInfo>> _guildRankMap = new ConcurrentHashMap<>();
		Set<String> guildIds = new HashSet<>();
		Map<GCGuildGrade, Set<Tuple>> tuplesMap = new HashMap<>();
		for (GCGuildGrade grade : GCGuildGrade.values()) {
			String key = getRankKey(termId, GCGradeRankType.G_KILL, grade);
			Set<Tuple> rankSet = RedisProxy.getInstance().getGCRankInfo(key, 100);
			tuplesMap.put(grade, rankSet);
			for (Tuple tuple : rankSet) {
				guildIds.add(tuple.getElement());
			}
		}
		Map<String, GCGuildData> guildDataMap = RedisProxy.getInstance().getGCGuildsData(new ArrayList<>(guildIds));
		for (Entry<GCGuildGrade, Set<Tuple>> entry : tuplesMap.entrySet()) {
			List<GCRankInfo> rankList = new ArrayList<>();
			GCGuildGrade grade = entry.getKey();
			int rank = 1;
			for (Tuple tuple : entry.getValue()) {
				String guildId = tuple.getElement();
				long score = (long) tuple.getScore();
				GCGuildData guildData = guildDataMap.get(guildId);
				if (guildData == null) {
					continue;
				}
				GCRankInfo.Builder builder = GCRankInfo.newBuilder();
				builder.setId(guildId);
				builder.setServerId(guildData.getServerId());
				builder.setScore(score);
				builder.setRank(rank);
				builder.setTag(guildData.getTag());
				builder.setName(guildData.getName());
				builder.setFlag(guildData.getFlag());
				rankList.add(builder.build());
				rank++;
			}
			_guildRankMap.put(grade, rankList);
		}
		guildRankMap = _guildRankMap;
	}

	protected void matchTick() {
		try {
			if (activityInfo.state != GCState.MATCH) {
				return;
			}
			
			// 准备加载参与匹配联盟列表
			if (!activityInfo.isPrepareFinish()) {
				flushSignerInfo();
				activityInfo.setPrepareFinish(true);
				RedisProxy.getInstance().updateGCActivityInfo(activityInfo);
			}
			
			
			String matchKey = RedisProxy.getInstance().GCACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
			String matchLockKey = RedisProxy.getInstance().GCACTIVITY_MATCH_LOCK + ":" + activityInfo.getTermId();
			
			// 初始化匹配阶段信息
			if (!matchServerInfo.hasInit || activityInfo.getTermId() != matchServerInfo.getTermId()) {
				matchServerInfo.setTermId(activityInfo.getTermId());
				String finishStr = RedisProxy.getInstance().getRedisSession().hGet(matchKey, "isFinish", 0);
				if (!HawkOSOperator.isEmptyString(finishStr)) {
					matchServerInfo.setFinish(true);
				} else {
					matchServerInfo.setFinish(false);
				}
				matchServerInfo.setHasInit(true);
			}
			
			// 本期匹配已完成
			if (matchServerInfo.isFinish()) {
				return;
			}
			
			long prepareEndTime = activityInfo.getTimeCfg().getMatchStartTimeValue() + ChampionshipConstCfg.getInstance().getMatchPrepareTime();
			// 匹配准备时间未结束,不进行其他处理
			if (HawkTime.getMillisecond() <= prepareEndTime) {
				return;
			}
			
			String serverId = GsConfig.getInstance().getServerId();
			long lock = RedisProxy.getInstance().getMatchLock(matchLockKey);
			boolean needSync = false;
			// 获取到匹配权限,设置有效期并进行匹配
			if (lock > 0) {
				RedisProxy.getInstance().getRedisSession().expire(matchLockKey, ChampionshipConstCfg.getInstance().getMatchLockExpire());
				toMatchGuild();
				matchServerInfo.setFinish(true);
				RedisProxy.getInstance().getRedisSession().hSet(matchKey, "isFinish", String.valueOf(true));
				RedisProxy.getInstance().getRedisSession().hSet(matchKey, "matchServer", serverId);
				needSync = true;
			} else {
				String finishStr = RedisProxy.getInstance().getRedisSession().hGet(matchKey, "isFinish", 0);
				if (!HawkOSOperator.isEmptyString(finishStr)) {
					matchServerInfo.setFinish(true);
					needSync = true;
				}
			}
			if(needSync){
				// 在线玩家推送活动状态 
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					syncPageInfo(player);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	
	/**
	 * 战斗阶段轮询
	 */
	protected void warTick() {
		if (activityInfo.state != GCState.WAR) {
			return;
		}

		if (!fightState.isHasInit()) {
			fightState = RedisProxy.getInstance().getGCWarInfo();
			if (fightState == null) {
				fightState = new GCWarState();
				RedisProxy.getInstance().updateGCWarInfo(fightState);
			}
			fightState.setHasInit(true);
		}

		if (fightState.getTermId() != activityInfo.termId) {
			fightState = new GCWarState();
			fightState.setTermId(activityInfo.termId);
			fightState.setHasInit(true);
			RedisProxy.getInstance().updateGCWarInfo(fightState);
		}
		long curTime = HawkTime.getMillisecond();

		ChampionshipTimeCfg cfg = activityInfo.getTimeCfg();
		long warStartTime = cfg.getWarStartTimeValue();
		long warCalcTime = cfg.getWarCalcTimeValue();
		long to8Time = cfg.getWar16to8TimeValue();
		GCBattleState state;
		if (curTime < warStartTime) {
			state = GCBattleState.WAIT;
		} else if (curTime >= warStartTime && curTime < warCalcTime) {
			state = GCBattleState.WAIT;
		} else if (curTime >= warCalcTime && curTime < to8Time) {
			state = GCBattleState.ADVANCE_CALC;
		} else if (curTime >= to8Time) {
			state = GCBattleState.OPEN_SHOW;
		} else {
			state = GCBattleState.OPEN_SHOW;
		}

		boolean needUpdate = false;
		boolean stageChange = false;

		if (fightState.state != state) {
			needUpdate = true;
			fightState.state = state;
			if (fightState.state == GCBattleState.OPEN_SHOW) {
				flushRankData(GCBattleStage.TO_8);
				stageChange = true;
			}
		}

		GCBattleStage old_stage = fightState.getStage();
		GCBattleStage new_stage = calcWarStage(curTime, cfg);
		while (old_stage != new_stage) {
			needUpdate = true;
			stageChange = true;
			if (old_stage.isTop()) {
				break;
			}
			old_stage = old_stage.getNestStage();
			flushRankData(old_stage);
			fightState.setStage(old_stage);
		}

		if (fightState.stage != new_stage) {
			needUpdate = true;
			stageChange = true;
			fightState.stage = new_stage;
		}
		// 阶段变更为开启,则需要推送跑马灯
		if (fightState.state == GCBattleState.OPEN_SHOW && stageChange) {

		}

		if (needUpdate) {
			RedisProxy.getInstance().updateGCWarInfo(fightState);
			// 在线玩家推送活动状态,只同步参与联盟的成员
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				if (!player.hasGuild()) {
					continue;
				}
				if (!signMemberMap.containsKey(player.getGuildId())) {
					continue;
				}
				syncPageInfo(player);
			}
		}

	}

	/**
	 * 战斗预算轮询
	 */
	protected void battleTick() {
		int termId = activityInfo.getTermId();
		int index = fightState.getCalcIndex();
		// 当前所需计算小组已全部计算完成
		if (index > groupList.size() - 1) {
			return;
		}
		GCGroupData gcGroupData = groupList.get(index);
		// 该组计算完成,进行下一组计算
		if (gcGroupData.isCalcFinish()) {
			index += 1;
			fightState.setCalcIndex(index);
			RedisProxy.getInstance().updateGCWarInfo(fightState);
			return;
		}
		GCBattleStage calcStage = gcGroupData.getCalcStage();
		int calcIndex = gcGroupData.getCalcIndex();
		Map<GCBattleStage, List<GCGuildBattleData>> battleMap = gcGroupData.getBattleMap();
		List<GCGuildBattleData> gcGBattlelist = battleMap.get(calcStage);
		if (calcIndex >= gcGBattlelist.size()) {
			if (calcStage.isTop()) {
				index += 1;
				fightState.setCalcIndex(index);
			} else {
				calcStage = calcStage.getNestStage();
				calcIndex = 0;
				gcGroupData.setCalcStage(calcStage);
				gcGroupData.setCalcIndex(calcIndex);
			}
			RedisProxy.getInstance().updateGCWarInfo(fightState);
			return;
		}
		GCGuildBattleData gBattleData = gcGBattlelist.get(calcIndex);
		String guildA = gBattleData.getGuildA();
		String guildB = gBattleData.getGuildB();
		// A联盟轮空
		if (HawkOSOperator.isEmptyString(guildB)) {
			gBattleData.setWinnerGuild(guildA);
			GCGuildData guildAData = RedisProxy.getInstance().getGCGuildData(guildA);
			guildAData.setStage(calcStage.getWinner());
			RedisProxy.getInstance().updateGCGuildData(guildAData);
		} else {
			GCScoreData gcScoreData = gcGroupData.getScoreDataMap().get(calcStage);
			GCGuildBattle.Builder battleBuilder = doGuildBattle(termId, gBattleData, gcScoreData, calcStage);
			RedisProxy.getInstance().updateGCGuildBattle(termId, battleBuilder);
		}
		// 本阶段最后一组比试结束,处理联盟信息及阶段信息
		if (calcIndex == gcGBattlelist.size() - 1) {
			// 本组比试结束,开始计算下一组比赛
			if (calcStage.isTop()) {
				gcGroupData.setCalcFinish(true);
				calcResult(termId, gcGroupData);
				fightState.setCalcIndex(index + 1);
				RedisProxy.getInstance().updateGCWarInfo(fightState);
			} else {
				// 开始下一阶段比赛
				gcGroupData.setCalcStage(calcStage.getNestStage());
				gcGroupData.setCalcIndex(0);
				initNextStage(termId, gcGroupData, calcStage);
			}
		} else {
			gcGroupData.setCalcIndex(calcIndex + 1);
		}
		RedisProxy.getInstance().updateGCGroupData(termId, gcGroupData);
	}

	/**
	 * 加载报名出战玩家信息
	 */
	private void loadSignInfo() {
		int termId = activityInfo.termId;
		if(termId == 0){
			return;
		}
		Map<String, Set<String>> memberMap = new ConcurrentHashMap<>();
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for(String guildId : guildIds){
			Set<String> memberIds = RedisProxy.getInstance().getGCPlayerIds(termId, guildId);
			if(memberIds==null || memberIds.isEmpty()){
				continue;
			}
			memberMap.put(guildId, new HashSet<>(memberIds));
		}
		signMemberMap = memberMap;
	}

	/**
	 * 统计整理出战信息
	 */
	private void flushSignerInfo() {
		int termId = activityInfo.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		List<GCGuildData> guildDataList = new ArrayList<>();
		for (String guildId : guildIds) {
			Set<Tuple> memberIds = RedisProxy.getInstance().getGCPlayerIdAndPower(termId, guildId);
			if (memberIds == null || memberIds.isEmpty()) {
				continue;
			}
			// 出战人数不足
			if (memberIds.size() < ChampionshipConstCfg.getInstance().getWarMemberMinCnt()) {
				HawkLog.logPrintln("GuildChampionshipService flushSignerInfo failed, memberCnt not enough, guildId: {}, cnt: {}", guildId, memberIds.size());
				;
				continue;
			}
			GCGuildData guildData = RedisProxy.getInstance().getGCGuildData(guildId);
			if (guildData == null) {
				HawkLog.logPrintln("GuildChampionshipService flushSignerInfo failed, guildData null, guildId: {}", guildId);
				continue;
			}
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			GCGuildGrade grade = guildData.getGrade();
			guildData.setTermId(termId);
			guildData.setLastBattleGrade(guildData.getLastGrade());
			guildData.setLastGrade(grade);
			guildData.setKickCnt(0);
			guildData.setName(guildObj.getName());
			guildData.setTag(guildObj.getTag());
			guildData.setFlag(guildObj.getFlagId());
			guildData.setServerId(serverId);
			int joinCntLimit = ChampionshipConstCfg.getInstance().getGradeMemberLimit(grade.getValue());
			List<String> playerIds = new ArrayList<>();
			long totalPower = 0;
			List<GCPlayerInfo> playerInfos = new ArrayList<>();
			for (Tuple tuple : memberIds) {
				String playerId = tuple.getElement();
				if (playerIds.size() >= joinCntLimit) {
					break;
				}
				playerIds.add(tuple.getElement());
				totalPower += tuple.getScore();
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				GCPlayerInfo.Builder playerInfo = GCPlayerInfo.newBuilder();
				playerInfo.setId(playerId);
				playerInfo.setName(player.getName());
				playerInfo.setServerId(serverId);
				playerInfo.setIcon(player.getIcon());
				String pfIcon = player.getPfIcon();
				if(!HawkOSOperator.isEmptyString(pfIcon)){
					playerInfo.setPfIcon(pfIcon);
				}
				playerInfo.setGuildId(guildId);
				playerInfo.setGuildTag(guildObj.getTag());
				playerInfo.setBattlePoint((long) tuple.getScore());
				RedisProxy.getInstance().updateGCPlayerData(termId, playerInfo.build());
				playerInfos.add(playerInfo.build());
			}
			guildData.setTotalPower(totalPower);
			guildData.setMemberCnt(playerInfos.size());
			RedisProxy.getInstance().updateGCJoinPlayerInfos(termId, guildId, playerInfos);
			RedisProxy.getInstance().addGCJoinGuildId(termId, guildId, grade);
			guildDataList.add(guildData);
			Map<String, String> canRewardMap = new HashMap<>();
			for (Tuple tuple : memberIds) {
				canRewardMap.put(tuple.getElement(), String.valueOf(0));
			}
			RedisProxy.getInstance().updateGCRewardInfo(termId, guildId, canRewardMap);
			LogUtil.logGCGuildInfo(guildId, guildData.getName(), termId, guildData.getGrade(), memberIds.size(), playerInfos.size());
		}
		RedisProxy.getInstance().updateGCGuildData(guildDataList);
	
	}
	
	/**
	 * 匹配分组
	 * @return
	 */
	private boolean toMatchGuild() {
		try {
			long startTime = HawkTime.getMillisecond();
			int termId = activityInfo.termId;
			// 清空本期的小组信息
			RedisProxy.getInstance().removeGCGroupData(termId);
			Map<String, String> signMap = RedisProxy.getInstance().getGCJoinGuildIds(termId);
			Map<GCGuildGrade, List<String>> gradeMap = new HashMap<>();
			for(Entry<String, String> entry : signMap.entrySet()){
				GCGuildGrade grade = GCGuildGrade.valueOf(entry.getValue());
				String guildId = entry.getKey();
				if(!gradeMap.containsKey(grade)){
					gradeMap.put(grade, new ArrayList<>());
				}
				gradeMap.get(grade).add(guildId);
				
			}
			doMatch(gradeMap);
			HawkLog.logPrintln("GuildChampionshipService toMatchGuild finish, serverId: {}, costTime: {}", GsConfig.getInstance().getServerId(), HawkTime.getMillisecond() - startTime);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
		
	}
	
	/**
	 * 匹配分组
	 * @param gradeMap
	 * @return
	 */
	private boolean doMatch(Map<GCGuildGrade, List<String>> gradeMap) {
		int termId = activityInfo.termId;
		Random random = new Random();
		Map<String, GCGuildData> guildDataMap = RedisProxy.getInstance().getAllGCGuildData();
		List<GCGroupData> groupList = new ArrayList<>();
		List<GCGuildData> guildList = new ArrayList<>();
		for (Entry<GCGuildGrade, List<String>> entry : gradeMap.entrySet()) {
			GCGuildGrade grade = entry.getKey();
			List<String> guildIds = entry.getValue();
			if (guildIds.isEmpty()) {
				continue;
			}
			Collections.shuffle(guildIds);
			Map<Integer, String> groupIdsMap = new HashMap<>();
			Map<String, GCGroupData> groupMap = new HashMap<>();
			for (int i = 0; i < guildIds.size(); i++) {
				String guildId = guildIds.get(i);
				int groupIndex = i / 16;
				int posIndex = i % 16;
				String groupId;
				if (!groupIdsMap.containsKey(groupIndex)) {
					groupId = HawkOSOperator.randomUUID();
					groupIdsMap.put(groupIndex, groupId);
					GCGroupData groupData = new GCGroupData();
					groupData.setId(groupId);
					groupData.setGrade(grade);
					groupData.setGuildIds(new ArrayList<>());
					groupMap.put(groupId, groupData);
					groupData.getGuildIds().add(guildId);
				} else {
					groupId = groupIdsMap.get(groupIndex);
					GCGroupData groupData = groupMap.get(groupId);
					groupData.getGuildIds().add(guildId);
				}
				GCGuildData guildData = guildDataMap.get(guildId);
				guildData.setGroupId(groupId);
				guildData.setgPosIndex(posIndex);
				guildList.add(guildData);
			}
			if (groupMap.size() > 0) {
				groupList.addAll(groupMap.values());
			}
		}

		for (GCGroupData groupData : groupList) {
			List<String> guildIds = groupData.getGuildIds();
			String selectGuild = guildIds.get(random.nextInt(guildIds.size()));
			GCGuildData guildData = guildDataMap.get(selectGuild);
			groupData.setServerId(guildData.getServerId());
			// 初始化小组对战列表
			init16to8Stage(groupData);

		}

		RedisProxy.getInstance().updateGCGroupData(termId, groupList);
		RedisProxy.getInstance().updateGCGuildData(guildList);
		return true;
	}
	
	/**
	 * 计算当前战斗阶段
	 * @param curTime
	 * @param cfg
	 * @return
	 */
	private GCBattleStage calcWarStage(long curTime, ChampionshipTimeCfg cfg) {
		long to8Time = cfg.getWar16to8TimeValue();
		long to4Time = cfg.getWar8to4TimeValue();
		long to2Time = cfg.getWar4to2TimeValue();
		long to1Time = cfg.getWar2to1TimeValue();
		GCBattleStage new_stage;
		if (curTime < to8Time) {
			new_stage = GCBattleStage.TO_8;
		} else if (curTime >= to8Time && curTime < to4Time) {
			new_stage = GCBattleStage.TO_8;
		} else if (curTime >= to4Time && curTime < to2Time) {
			new_stage = GCBattleStage.TO_4;
		} else if (curTime >= to2Time && curTime < to1Time) {
			new_stage = GCBattleStage.TO_2;
		} else {
			new_stage = GCBattleStage.TO_1;
		}
		return new_stage;
	}
	
	/**
	 * 刷新各小组指定阶段的排行积分
	 * @param battleStage
	 */
	private void flushRankData(GCBattleStage battleStage) {
		int termId = activityInfo.getTermId();
		if (groupList == null || groupList.isEmpty()) {
			return;
		}
		for (GCGroupData gcGroupData : groupList) {
			GCBattleStage lastBattleStage = battleStage.getLastStage();
			GCScoreData lastScoreData = null;
			if (battleStage != lastBattleStage) {
				lastScoreData = gcGroupData.getScoreDataMap().get(lastBattleStage);
			}
			GCScoreData gcScoreData = gcGroupData.getScoreDataMap().get(battleStage);
			if (gcScoreData == null) {
				continue;
			}
			GCGuildGrade grade = gcGroupData.getGrade();
			// 获取需要更新的积分信息
			Map<String, Double> guildKill = gcScoreData.getGuildKillChangeMap(lastScoreData);
			Map<String, Double> selfKill = gcScoreData.getSelfKillChangeMap(lastScoreData);
			Map<String, Double> selfSuccessive = gcScoreData.getSelfContinueKillChangeMap(lastScoreData);
			Map<String, Double> selfBeat = gcScoreData.getSelfBeatChangeMap(lastScoreData);

			// 联盟杀敌
			if (!guildKill.isEmpty()) {
				String key = getRankKey(termId, GCGradeRankType.G_KILL, grade);
				RedisProxy.getInstance().updateGCRankInfo(key, guildKill);
			}

			// 个人杀敌
			if (!selfKill.isEmpty()) {
				String key = getRankKey(termId, GCGradeRankType.S_KILL, grade);
				RedisProxy.getInstance().updateGCRankInfo(key, selfKill);
			}

			// 个人连胜
			if (!selfSuccessive.isEmpty()) {
				String key = getRankKey(termId, GCGradeRankType.S_CWIN, grade);
				RedisProxy.getInstance().updateGCRankInfo(key, selfSuccessive);
			}

			// 个人击败
			if (!selfBeat.isEmpty()) {
				String key = getRankKey(termId, GCGradeRankType.S_BEAT, grade);
				RedisProxy.getInstance().updateGCRankInfo(key, selfBeat);
			}

			gcScoreData.setIsflushed(true);
			RedisProxy.getInstance().updateGCGroupData(termId, gcGroupData);
		}

	}
	
	/**
	 * 获取排行redis的key
	 * @param termId
	 * @param rankType
	 * @param grade
	 * @return
	 */
	private String getRankKey(int termId, GCGradeRankType rankType, GCGuildGrade grade) {
		switch (rankType) {
		case G_KILL:
			return rankType.getSkey() + ":" + termId + ":" + grade.getValue();
		case S_KILL:
		case S_CWIN:
		case S_BEAT:
			return rankType.getSkey() + ":" + termId;
		default:
			return "";
		}
	}

	/**
	 * 小组战斗完成结算
	 * @param termId
	 * @param gcGroupData
	 */
	private void calcResult(int termId, GCGroupData gcGroupData) {
		List<String> guildIds = gcGroupData.getGuildIds();
		Map<String, GCGuildData> dataMap = RedisProxy.getInstance().getGCGuildsData(guildIds);
		List<GCGuildData> guildList = new ArrayList<>(dataMap.values());
		Collections.sort(guildList);
		int rank = 1;
		for (GCGuildData guildData : guildList) {
			GCGuildGrade grade = guildData.getGrade();
			guildData.setGroupRank(rank);
			guildData.setLastGrade(grade);
			// 升降级别
			if (rank >= 1 && rank <= 4) {
				guildData.setGrade(grade.getHighGrade());
			} else if (rank > 12 && rank <= 16) {
				guildData.setGrade(grade.getLowGrade());
			}
			rank++;
		}
		RedisProxy.getInstance().updateGCGuildData(guildList);

	}

	/**
	 * 初始化16进8阶段列表
	 * @param groupData
	 */
	private void init16to8Stage(GCGroupData groupData) {
		GCBattleStage battleStage = GCBattleStage.TO_8;
		List<String> guildIds = groupData.getGuildIds();
		int guildSize = guildIds.size();
		List<GCGuildBattleData> battleDataList = new ArrayList<>();
		int index = 0;
		for (int i = 0; i <= guildSize / 2; i++) {
			GCGuildBattleData battleData = new GCGuildBattleData();
			int indexA = i * 2;
			if(indexA >= guildSize){
				continue;
			}
			String guildA = guildIds.get(indexA);
			int indexB = i * 2 + 1;
			String guildB = "";
			if (indexB < guildSize) {
				guildB = guildIds.get(indexB);
			}
			battleData.setBattleStage(battleStage);
			battleData.setgBattleId(HawkOSOperator.randomUUID());
			battleData.setGuildA(guildA);
			battleData.setGuildB(guildB);
			battleData.setPosIndex(index);
			battleDataList.add(battleData);
			index++;
		}
		groupData.getBattleMap().put(battleStage, battleDataList);
		groupData.getScoreDataMap().put(battleStage, new GCScoreData());
	
	}

	/**
	 * 计算下一阶段分组
	 * @param termId
	 * @param gcGroupData
	 * @param calcStage
	 */
	private void initNextStage(int termId, GCGroupData gcGroupData, GCBattleStage calcStage) {
		Map<GCBattleStage, List<GCGuildBattleData>> battleMap = gcGroupData.getBattleMap();
		Map<GCBattleStage, GCScoreData> scoreDataMap = gcGroupData.getScoreDataMap();
		List<GCGuildBattleData> gcGBattlelist = battleMap.get(calcStage);
		List<String> winnerList = new ArrayList<>();
		for (GCGuildBattleData battleDate : gcGBattlelist) {
			String guildId = battleDate.getWinnerGuild();
			winnerList.add(guildId);
		}
		List<GCGuildBattleData> nextStageBattleList = new ArrayList<>();
		GCBattleStage nextStage = calcStage.getNestStage();
		int guildSize = winnerList.size();
		int index = 0;
		for (int i = 0; i <= guildSize / 2; i++) {
			GCGuildBattleData battleData = new GCGuildBattleData();
			int indexA = i * 2;
			if (indexA >= guildSize) {
				continue;
			}
			String guildA = winnerList.get(indexA);
			int indexB = i * 2 + 1;
			String guildB = "";
			if (indexB < guildSize) {
				guildB = winnerList.get(indexB);
			}
			battleData.setBattleStage(nextStage);
			battleData.setgBattleId(HawkOSOperator.randomUUID());
			battleData.setGuildA(guildA);
			battleData.setGuildB(guildB);
			battleData.setPosIndex(index);
			nextStageBattleList.add(battleData);
			index++;
		}
		battleMap.put(nextStage, nextStageBattleList);
		
		// 继承上一阶段的积分数据
		GCScoreData scoreData = gcGroupData.getScoreDataMap().get(calcStage);
		if (scoreData == null) {
			scoreDataMap.put(nextStage, new GCScoreData());
		} else {
			scoreDataMap.put(nextStage, scoreData.getCopy());
		}
	}

	/**
	 * 联盟战斗计算
	 * @param termId
	 * @param gBattleData
	 * @param gcScoreData
	 * @param calcStage
	 * @return
	 */
	private GCGuildBattle.Builder doGuildBattle(int termId, GCGuildBattleData gBattleData, GCScoreData gcScoreData, GCBattleStage calcStage) {
		String guildA = gBattleData.getGuildA();
		String guildB = gBattleData.getGuildB();
		GCGuildData guildAData = RedisProxy.getInstance().getGCGuildData(guildA);
		GCGuildData guildBData = RedisProxy.getInstance().getGCGuildData(guildB);
		List<GCPlayerInfo.Builder> playerAInfos = RedisProxy.getInstance().getGCJoinPlayerIds(termId, guildA);
		List<GCPlayerInfo.Builder> playerBInfos = RedisProxy.getInstance().getGCJoinPlayerIds(termId, guildB);
		Map<String, PBChampionPlayer.Builder> battleMapA = new HashMap<>();
		Map<String, PBChampionPlayer.Builder> battleMapB = new HashMap<>();
		for (GCPlayerInfo.Builder player : playerAInfos) {
			String playerId = player.getId();
			PBChampionPlayer.Builder battle = RedisProxy.getInstance().getGCPbattleData(termId, playerId);
			battleMapA.put(playerId, battle);
		}
		for (GCPlayerInfo.Builder player : playerBInfos) {
			String playerId = player.getId();
			PBChampionPlayer.Builder battle = RedisProxy.getInstance().getGCPbattleData(termId, playerId);
			battleMapB.put(playerId, battle);
		}

		String winGuild = "";
		int kickCntA = 0;
		int kickCntB = 0;
		List<GCBattelInfo> list = new ArrayList<>();
		HawkLog.logPrintln("ChampionshipService doFight start, gBattleId: {}, gBattlePos: {}, gidA: {}, gnameA: {}, gidB: {}, gnameB: {}", gBattleData.getgBattleId(),
				gBattleData.getPosIndex(), guildAData.getId(), guildAData.getName(), guildBData.getId(), guildBData.getName());
		// a组出战角标
		int aIndex = 0;
		// B组出战角标
		int bIndex = 0;
		// 车轮战
		for (int a = 0; a < playerAInfos.size(); a++) {
			GCPlayerInfo.Builder playerA = playerAInfos.get(a);
			for (int b = bIndex; b < playerBInfos.size(); b++) {
				GCPlayerInfo.Builder playerB = playerBInfos.get(b);
				bIndex = b;
				GCBattelInfo.Builder battleInfo = GCBattelInfo.newBuilder();
				String battleId = HawkOSOperator.randomUUID();
				battleInfo.setPBattleId(battleId);
				PBChampionPlayer.Builder battleA = battleMapA.get(playerA.getId());
				PBChampionPlayer.Builder battleB = battleMapB.get(playerB.getId());
				
				// 连胜debuff
				checkAndInitDebuff(playerA, battleA);
				checkAndInitDebuff(playerB, battleB);
				
				int armyA = calcArmyCnt(battleA);
				int armyB = calcArmyCnt(battleB);
				BattleOutcome battleOutcome = doFight(termId, battleId, battleA, battleB, playerA, playerB);
				boolean isAtkWin = battleOutcome.isAtkWin();

				// 计算杀兵积分
				long scoreA = calcKillArmyScore(battleOutcome.getBattleArmyMapAtk().get(BattleService.NPC_ID + playerA.getId()));
				long scoreB = calcKillArmyScore(battleOutcome.getBattleArmyMapDef().get(BattleService.NPC_ID + playerB.getId()));

				int armyAftA = calcArmyCnt(battleA);
				int armyAftB = calcArmyCnt(battleB);
				HawkLog.logPrintln(
						"ChampionshipService doFight, gBattleId: {}, gidA: {}, pidA: {}, pnameA: {}, pArmyCntBefA: {}, pArmyCntAftA: {}, gidB: {}, pidB: {}, pnameB: {}, pArmyCntBefB: {}, pArmyCntAftB: {}, isatkWin: {}",
						gBattleData.getgBattleId(), guildAData.getId(), playerA.getId(), playerA.getName(), armyA, armyAftA, guildBData.getId(), playerB.getId(), playerB.getName(),
						armyB, armyAftB, isAtkWin);

				playerA.setArmyCnt(armyA);
				playerA.setDisCnt(armyA - armyAftA);

				playerB.setArmyCnt(armyB);
				playerB.setDisCnt(armyB - armyAftB);

				// 记录联盟及个人击杀积分
				gcScoreData.addGuildKillScore(guildA, scoreA);
				gcScoreData.addGuildKillScore(guildB, scoreB);
				gcScoreData.addSelfKillScore(playerA.getId(), scoreA);
				gcScoreData.addSelfKillScore(playerB.getId(), scoreB);

				if (isAtkWin) {
					playerA.setSuccessive(playerA.getSuccessive() + 1);
					playerB.setSuccessive(0);
					battleInfo.setWinnerId(playerA.getId());
					winGuild = guildA;
					guildAData.setStage(calcStage.getWinner());
					guildBData.setStage(calcStage.getLoser());
					kickCntB++;
					battleInfo.setPlayerA(playerA);
					battleInfo.setPlayerB(playerB);
					list.add(battleInfo.build());
					// 记录连胜/击败数据
					gcScoreData.addSelfBeat(playerA.getId());
					gcScoreData.updateSlefSuccessive(playerA.getId(), playerA.getSuccessive());
					bIndex++;
				} else {
					playerA.setSuccessive(0);
					playerB.setSuccessive(playerB.getSuccessive() + 1);
					battleInfo.setWinnerId(playerB.getId());
					winGuild = guildB;
					guildAData.setStage(calcStage.getLoser());
					guildBData.setStage(calcStage.getWinner());
					kickCntA++;
					battleInfo.setPlayerA(playerA);
					battleInfo.setPlayerB(playerB);
					list.add(battleInfo.build());
					// 记录连胜/击败数据
					gcScoreData.addSelfBeat(playerB.getId());
					gcScoreData.updateSlefSuccessive(playerB.getId(), playerB.getSuccessive());
					aIndex ++;
					break;
				}
			}
		}
		// 玩家未全部出战
		if (aIndex < playerAInfos.size() - 1) {
			addEmptyBattle(playerAInfos, list, aIndex + 1, true);
		} else if (bIndex < playerBInfos.size() - 1) {
			addEmptyBattle(playerBInfos, list, bIndex + 1, false);
		}
		
		guildAData.setKickCnt(kickCntA);
		guildBData.setKickCnt(kickCntB);
		HawkLog.logPrintln("ChampionshipService doFight finish, gBattleId: {}, gBattlePos: {}, gidA: {}, gnameA: {}, gidB: {}, gnameB: {}, winner: {}", gBattleData.getgBattleId(),
				gBattleData.getPosIndex(), guildAData.getId(), guildAData.getName(), guildBData.getId(), guildBData.getName(), winGuild);
		GCGuildBattle.Builder guildBattle = GCGuildBattle.newBuilder();
		guildBattle.setGBattleId(gBattleData.getgBattleId());
		guildBattle.setBPosIndex(gBattleData.getPosIndex());
		guildBattle.setGuildA(guildAData.build());
		guildBattle.setGuildB(guildBData.build());
		guildBattle.setWinnerGuild(winGuild);
		guildBattle.addAllBattleInfo(list);
		gBattleData.setWinnerGuild(winGuild);
		RedisProxy.getInstance().updateGCGuildData(guildAData);
		RedisProxy.getInstance().updateGCGuildData(guildBData);
		return guildBattle;
	}

	private void addEmptyBattle(List<GCPlayerInfo.Builder> playerAInfos, List<GCBattelInfo> list, int aIndex, boolean isAtker) {
		for (int i = aIndex; i < playerAInfos.size(); i++) {
			GCBattelInfo.Builder battleInfo = GCBattelInfo.newBuilder();
			GCPlayerInfo.Builder playerBuilder = playerAInfos.get(i);
			String battleId = HawkOSOperator.randomUUID();
			battleInfo.setPBattleId(battleId);
			if (isAtker) {
				battleInfo.setPlayerA(playerBuilder);
			} else {
				battleInfo.setPlayerB(playerBuilder);
			}
			battleInfo.setWinnerId(playerBuilder.getId());
			list.add(battleInfo.build());
		}
	}
	
	/**
	 * 添加连胜debuff
	 * @param playerA
	 * @param battleA
	 */
	private void checkAndInitDebuff(GCPlayerInfo.Builder player, PBChampionPlayer.Builder battle) {
		int successive = player.getSuccessive();
		Map<Integer, Integer> effMap = null;
		ConfigIterator<ChampionshipDebuffCfg> its = HawkConfigManager.getInstance().getConfigIterator(ChampionshipDebuffCfg.class);
		for (ChampionshipDebuffCfg config : its) {
			int min = config.getRangeTuple().first;
			int max = config.getRangeTuple().second;
			if (successive >= min && successive <= max) {
				effMap = config.getEffMap();
				break;
			}
		}
		// 无减益效果
		if (effMap == null || effMap.isEmpty()) {
			return;
		}
		List<PBChampionEff.Builder> effBuilderList = battle.getEffsBuilderList();
		for (PBChampionEff.Builder effBuilder : effBuilderList) {
			int effId = effBuilder.getEffectId();
			Integer effVal = effMap.get(effId);
			if (effVal != null) {
				effBuilder.setValue(effVal);
			}
		}

	}

	/**
	 * 阶段轮询
	 */
	public void stateTick() {
		try {
			checkStateChange();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	private void checkStateChange() {
		GCActivityData newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncPageInfo(player);
			}
			RedisProxy.getInstance().updateGCActivityInfo(activityInfo);
		}
		GCState old_state = activityInfo.getState();
		GCState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_term != old_term) {
			old_state = GCState.NOT_OPEN;
			activityInfo.setTermId(new_term);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == GCState.NOT_OPEN) {
				old_state = GCState.SHOW;
				activityInfo.setState(old_state);
				onShowStart();
			} else if (old_state == GCState.SHOW) {
				old_state = GCState.SIGN_UP;
				activityInfo.setState(old_state);
				onSignStart();
			} else if (old_state == GCState.SIGN_UP) {
				old_state = GCState.MATCH;
				activityInfo.setState(old_state);
				onMatchStart();
			} else if (old_state == GCState.MATCH) {
				old_state = GCState.WAR;
				activityInfo.setState(old_state);
				onWarStart();
			} else if (old_state == GCState.WAR) {
				old_state = GCState.END;
				activityInfo.setState(old_state);
				onEndStart();
			} else if (old_state == GCState.END) {
				old_state = GCState.GC_HIDDEN;
				activityInfo.setState(old_state);
				onHidden();
			} 
		}

		if (needUpdate) {
			activityInfo = newInfo;
			RedisProxy.getInstance().updateGCActivityInfo(activityInfo);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncPageInfo(player);
			}
			HawkLog.logPrintln("GuildChampionshipService state change, oldTerm: {}, oldState: {} ,newTerm: {}, newState: {}", old_term, old_state, activityInfo.getTermId(),
					activityInfo.getState());
		}

	}

	private void onShowStart() {
		signMemberMap = new ConcurrentHashMap<>();
	
	}

	private void onSignStart() {
		signMemberMap = new ConcurrentHashMap<>();
	
	}

	private void onMatchStart() {
		loadSignInfo();
	
	}

	private void onWarStart() {
		loadGroupInfo();
	}
	
	/**
	 * 加载本服计算的小组及联盟和小组映射
	 * @return
	 */
	private void loadGroupInfo() {
		int termId = activityInfo.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		// 加载在本服运算的分组信息
		List<GCGroupData> dataList = RedisProxy.getInstance().getAllGCGroupData(termId);
		List<GCGroupData> sortedList = dataList.stream().filter(e -> serverId.equals(e.getServerId())).sorted(new Comparator<GCGroupData>() {
			@Override
			public int compare(GCGroupData arg0, GCGroupData arg1) {
				return arg0.getId().compareTo(arg1.getId());
			}
		}).collect(Collectors.toList());
		groupList = sortedList;
		Map<String, String> groupMap = new ConcurrentHashMap<>();
		for(GCGroupData groupData : dataList){
			String groupId = groupData.getId();
			List<String> guildIds = groupData.getGuildIds();
			for(String guildId : guildIds){
				groupMap.put(guildId, groupId);
			}
		}
		groupMapping = groupMap;
	}

	private void onEndStart() {
		// 计算结果
	
	}


	private void onHidden() {
		signMemberMap = new ConcurrentHashMap<>();
		matchStageGroupInfo = new ConcurrentHashMap<>();

	}

	/**
	 * 当前阶段状态计算,仅供状态检测调用 
	 * 
	 * @return
	 */
	private GCActivityData calcInfo() {
		GCActivityData info = new GCActivityData();
		if (ChampionshipConstCfg.getInstance().isSystemClose()) {
			info.setState(GCState.GC_HIDDEN);
			return info;
		}
		ConfigIterator<ChampionshipTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(ChampionshipTimeCfg.class);
		long now = HawkTime.getMillisecond();

		String serverId = GsConfig.getInstance().getServerId();
		Long mergeTime =  AssembleDataManager.getInstance().getServerMergeTime(serverId);

		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = ChampionshipConstCfg.getInstance().getServerDelay();
		ChampionshipTimeCfg cfg = null;
		for (ChampionshipTimeCfg timeCfg : its) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			
			// 开服时间不满足
			if (serverOpenAm0 + serverDelay > timeCfg.getSignStartTimeValue()) {
				continue;
			}
			
			// 合服时间在活动期间内,则不参与本次锦标赛
			if (mergeTime != null && mergeTime >= timeCfg.getShowStartTimeValue() && mergeTime <= timeCfg.getHiddenTimeValue()) {
				continue;
			}
			
			// 开启判定,如果没有开启区服限制,或者本期允许本服所在区组开放
			if ((limitServerLimit.isEmpty() || limitServerLimit.contains(serverId)) && (forbidServerLimit == null || !forbidServerLimit.contains(serverId))) {
				
				if (now > timeCfg.getShowStartTimeValue()) {
					cfg = timeCfg;
				}
			}
		}

		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}

		int termId = 0;
		GCState state = GCState.GC_HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showStartTime = cfg.getShowStartTimeValue();
			long signStartTime = cfg.getSignStartTimeValue();
			long matchStartTime = cfg.getMatchStartTimeValue();
			long warStartTime = cfg.getWarStartTimeValue();
			long endStartTime = cfg.getEndStartTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showStartTime) {
				state = GCState.GC_HIDDEN;
			}
			if (now >= showStartTime && now < signStartTime) {
				state = GCState.SHOW;
			}
			if (now >= signStartTime && now < matchStartTime) {
				state = GCState.SIGN_UP;
			}
			if (now >= matchStartTime && now < warStartTime) {
				state = GCState.MATCH;
			}
			if (now >= warStartTime && now < endStartTime) {
				state = GCState.WAR;
			}
			if (now >= endStartTime && now < hiddenTime) {
				state = GCState.END;
			}
			if (now >= hiddenTime) {
				state = GCState.GC_HIDDEN;
			}
		}

		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	/**
	 * 同步锦标赛活动状态
	 * 
	 * @param player
	 */
	public void syncPageInfo(Player player) {
		GCPageInfo.Builder stateInfo = genPageInfo(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_INFO_SYNC, stateInfo));
	}

	/**
	 * 构建活动界面信息
	 * 
	 * @param playerId
	 * @return
	 */
	public GCPageInfo.Builder genPageInfo(String playerId) {
		int termId = activityInfo.getTermId();
		GCPageInfo.Builder builder = GCPageInfo.newBuilder();
		GCStateInfo.Builder stateInfo = genStateInfo(playerId);
		String guildId = GuildService.getInstance().getPlayerGuildId(playerId);
		boolean hasGuild = !HawkOSOperator.isEmptyString(guildId);
		if (!hasGuild && stateInfo.getState() != GCState.GC_HIDDEN) {
			stateInfo.setState(GCState.NOT_OPEN);
		}
		boolean isSign = !HawkOSOperator.isEmptyString(guildId) && signMemberMap.containsKey(guildId) && signMemberMap.get(guildId).contains(playerId);
		builder.setIsSigned(isSign);
		GCGuildData guildData = null;

		GCGuildGrade grade = GCGuildGrade.GRADE_3;
		if (hasGuild) {
			guildData = RedisProxy.getInstance().getGCGuildData(guildId);
			if (guildData != null) {
				grade = guildData.getGrade();
			}
		}
		builder.setGrade(grade.getValue());
		List<GCPlayerInfo> memberList = null;
		switch (stateInfo.getState()) {
		case NOT_OPEN:
			break;
		case SHOW:
			break;
		case SIGN_UP:
			memberList = genMemberList(guildId);
			if (memberList != null && !memberList.isEmpty()) {
				builder.addAllJoinPlayers(memberList);
			}
			break;
		case MATCH:
			// 本期已匹配完成
			if (matchServerInfo.isFinish() && matchServerInfo.getTermId() == activityInfo.getTermId()) {
				if (hasGuild) {
					guildData = RedisProxy.getInstance().getGCGuildData(guildId);
					if (guildData != null) {
						grade = guildData.getGrade();
					}
				}
				builder.setGrade(grade.getValue());
				// 未参与本次锦标赛
				if (guildData == null || guildData.getTermId() != termId || HawkOSOperator.isEmptyString(guildData.getGroupId())) {
					stateInfo.setState(GCState.NOT_OPEN);
					break;
				} else {
					genMatchStateInfo(termId, builder, guildData);

				}
			} else {
				if (hasGuild) {
					guildData = RedisProxy.getInstance().getGCGuildData(guildId);
					if (guildData != null) {
						grade = guildData.getGrade();
					}
				}
				builder.setGrade(grade.getValue());
				memberList = genMemberList(guildId);
				if (memberList != null && !memberList.isEmpty()) {
					builder.addAllJoinPlayers(memberList);
				}
				builder.setIfMatchFinish(false);
			}
			break;
		case WAR:
			// 未参与本次锦标赛
			if (guildData == null || guildData.getTermId() != termId) {
				stateInfo.setState(GCState.NOT_OPEN);
				break;
			} else {
				genWarStateInfo(termId, builder, guildData);
			}
			break;
		case END:
			// 未参与本次锦标赛
			if (guildData == null || guildData.getTermId() != termId) {
				stateInfo.setState(GCState.NOT_OPEN);
				break;
			} else {
				builder.setGroupRank(guildData.getGroupRank());
				builder.setLastGrade(guildData.getLastGrade().getValue());
				builder.setRewardState(getRewardState(playerId, termId, guildId));
			}
			break;
		default:
			break;
		}
		builder.setStateInfo(stateInfo);
		return builder;
	}
	
	/**
	 * 获取玩家的领奖状态
	 * @param playerId
	 * @param termId
	 * @param guildId
	 * @return
	 */
	private GCRewardState getRewardState(String playerId, int termId, String guildId) {
		String rewardStatus = RedisProxy.getInstance().getGCRewardInfo(termId, guildId, playerId);
		GCRewardState rewardState = GCRewardState.NO_REWARD;
		if(!HawkOSOperator.isEmptyString(rewardStatus)){
			int state = Integer.valueOf(rewardStatus);
			if(state == 0){
				rewardState = GCRewardState.CAN_TAKE;
			}else{
				rewardState = GCRewardState.TOOK;
			}
		}
		return rewardState;
	}
	
	/**
	 * 构建出战联盟匹配完成阶段界面信息
	 * @param termId
	 * @param builder
	 * @param guildData
	 */
	private void genMatchStateInfo(int termId, GCPageInfo.Builder builder, GCGuildData guildData) {
		String groupId = guildData.getGroupId();
		GCGroupData gcGroupData = RedisProxy.getInstance().getGCGroupData(groupId, termId);
		GCStageBattle stageBattle = matchStageGroupInfo.get(groupId);
		if (stageBattle == null) {
			stageBattle = getMatchStageInfo(gcGroupData);
			if (stageBattle != null) {
				matchStageGroupInfo.put(groupId, stageBattle);
			}
		}
		if (stageBattle != null) {
			builder.addStageBattle(stageBattle);
		}
		builder.setIfMatchFinish(true);
	}
	
	/**
	 * 构建出战联盟战斗阶段界面信息
	 * @param termId
	 * @param builder
	 * @param guildData
	 */
	private void genWarStateInfo(int termId, GCPageInfo.Builder builder, GCGuildData guildData) {
		String groupId = guildData.getGroupId();
		GCGroupData groupData = RedisProxy.getInstance().getGCGroupData(groupId, termId);
		GCBattleState state = fightState.getState();
		groupId = guildData.getGroupId();
		// 非战况展示阶段,显示信息与匹配阶段相同
		if (state != GCBattleState.OPEN_SHOW) {
			GCStageBattle stageBattle = matchStageGroupInfo.get(groupId);
			if (stageBattle == null) {
				stageBattle = getMatchStageInfo(groupData);
				matchStageGroupInfo.put(groupId, stageBattle);
			}
			if (stageBattle != null) {
				builder.addStageBattle(stageBattle);
			}
		} else {
			GCBattleStage stage = fightState.getStage();
			while (true){
				Map<GCBattleStage, GCStageBattle> battleMap = warStageGroupInfo.get(groupId);
				if (battleMap == null) {
					battleMap = new HashMap<>();
					warStageGroupInfo.put(groupId, battleMap);
				}
				GCStageBattle stageBattle = battleMap.get(stage);
				if (stageBattle == null) {
					stageBattle = getWarStageInfo(groupData, stage);
					if (stageBattle != null) {
						battleMap.put(stage, stageBattle);
					}
				}
				if (stageBattle != null) {
					builder.addStageBattle(stageBattle);
				}
				if(stage == stage.getLastStage()){
					break;
				}
				stage = stage.getLastStage();
			}
		}
		if (groupData.isCalcFinish()) {
			builder.setGrade(guildData.getLastGrade().getValue());
		}else{
			builder.setGrade(guildData.getGrade().getValue());
			
		}
	}
	
	/**
	 * 构建匹配阶小组对阵信息
	 * @param termId
	 * @param groupId
	 * @return
	 */
	private GCStageBattle getMatchStageInfo(GCGroupData groupData) {
		List<GCGuildBattleData> guildBattleDate = groupData.getBattleMap().get(GCBattleStage.TO_8);
		List<String> guildIds = groupData.getGuildIds();
		Map<String, GCGuildData> guildMap = RedisProxy.getInstance().getGCGuildsData(guildIds);
		int index = 0;
		GCStageBattle.Builder stageBattleBuilder = GCStageBattle.newBuilder();
		for(GCGuildBattleData battleDate : guildBattleDate){
			GCGuildBattle.Builder battleBuilder = GCGuildBattle.newBuilder();
			battleBuilder.setGBattleId(battleDate.getgBattleId());
			battleBuilder.setBPosIndex(index);
			
			String guildIdA = battleDate.getGuildA();
			GCGuildData guildA = guildMap.get(guildIdA);
			GCGuildInfo.Builder guildABuilder = GCGuildInfo.newBuilder();
			guildABuilder.setId(guildA.getId());
			guildABuilder.setName(guildA.getName());
			guildABuilder.setTag(guildA.getTag());
			guildABuilder.setGuildFlag(guildA.getFlag());
			guildABuilder.setServerId(guildA.getServerId());
			battleBuilder.setGuildA(guildABuilder);
			
			String guildIdB = battleDate.getGuildB();
			if (!HawkOSOperator.isEmptyString(guildIdB)) {
				GCGuildData guildB = guildMap.get(guildIdB);
				GCGuildInfo.Builder guildBBuilder = GCGuildInfo.newBuilder();
				guildBBuilder.setId(guildB.getId());
				guildBBuilder.setName(guildB.getName());
				guildBBuilder.setTag(guildB.getTag());
				guildBBuilder.setGuildFlag(guildB.getFlag());
				guildBBuilder.setServerId(guildB.getServerId());
				battleBuilder.setGuildB(guildBBuilder);
			}else{
				GCGuildInfo.Builder guildBBuilder = GCGuildInfo.newBuilder();
				guildBBuilder.setId("");
				guildBBuilder.setName("");
				guildBBuilder.setTag("");
				guildBBuilder.setGuildFlag(guildA.getFlag());
				guildBBuilder.setServerId(guildA.getServerId());
				battleBuilder.setGuildB(guildBBuilder);
			}
			stageBattleBuilder.addBattle(battleBuilder);
			index++;
		}
		stageBattleBuilder.setBattleStage(GCBattleStage.TO_8.getValue());
		return stageBattleBuilder.build();
	}

	/**
	 * 构造小组阶段对战数据
	 * @param termId
	 * @param gcGroupData
	 * @param stage
	 * @return
	 */
	private GCStageBattle getWarStageInfo(GCGroupData groupData, GCBattleStage stage) {
		List<GCGuildBattleData> guildBattleDate = groupData.getBattleMap().get(stage);
		List<String> guildIds = groupData.getGuildIds();
		Map<String, GCGuildData> guildMap = RedisProxy.getInstance().getGCGuildsData(guildIds);
		int index = 0;
		GCStageBattle.Builder stageBattleBuilder = GCStageBattle.newBuilder();
		for (GCGuildBattleData battleDate : guildBattleDate) {
			GCGuildBattle.Builder battleBuilder = GCGuildBattle.newBuilder();
			battleBuilder.setGBattleId(battleDate.getgBattleId());
			battleBuilder.setBPosIndex(index);

			String guildIdA = battleDate.getGuildA();
			GCGuildData guildA = guildMap.get(guildIdA);
			GCGuildInfo.Builder guildABuilder = GCGuildInfo.newBuilder();
			guildABuilder.setId(guildA.getId());
			guildABuilder.setName(guildA.getName());
			guildABuilder.setTag(guildA.getTag());
			guildABuilder.setGuildFlag(guildA.getFlag());
			guildABuilder.setServerId(guildA.getServerId());
			battleBuilder.setGuildA(guildABuilder);

			String guildIdB = battleDate.getGuildB();
			if (!HawkOSOperator.isEmptyString(guildIdB)) {
				GCGuildData guildB = guildMap.get(guildIdB);
				GCGuildInfo.Builder guildBBuilder = GCGuildInfo.newBuilder();
				guildBBuilder.setId(guildB.getId());
				guildBBuilder.setName(guildB.getName());
				guildBBuilder.setTag(guildB.getTag());
				guildBBuilder.setGuildFlag(guildB.getFlag());
				guildBBuilder.setServerId(guildB.getServerId());
				battleBuilder.setGuildB(guildBBuilder);
			}else{
				GCGuildInfo.Builder guildBBuilder = GCGuildInfo.newBuilder();
				guildBBuilder.setId("");
				guildBBuilder.setName("");
				guildBBuilder.setTag("");
				guildBBuilder.setGuildFlag(guildA.getFlag());
				guildBBuilder.setServerId(guildA.getServerId());
				battleBuilder.setGuildB(guildBBuilder);
			}

			battleBuilder.setWinnerGuild(battleDate.getWinnerGuild());
			stageBattleBuilder.addBattle(battleBuilder);
			index++;
		}
		stageBattleBuilder.setBattleStage(stage.getValue());
		return stageBattleBuilder.build();
	}
	
	/**
	 * 获取小组排行信息
	 * @param player
	 * @return
	 */
	public int onGetGroupRank(Player player) {
		if (activityInfo.getState() != GCState.END) {
			return Status.Error.CHAMPIONSHIP_STATE_ERROR_VALUE;
		}
		if (!player.hasGuild()) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		String guildId = player.getGuildId();
		String groupId = groupMapping.get(guildId);
		if (HawkOSOperator.isEmptyString(groupId)) {
			return Status.Error.CHAMPIONSHIP_GUILD_NOT_JOIN_VALUE;
		}
		GCGroupData gcGroupData = RedisProxy.getInstance().getGCGroupData(groupId, activityInfo.getTermId());
		if (gcGroupData == null) {
			return Status.Error.CHAMPIONSHIP_GUILD_NOT_JOIN_VALUE;
		}
		List<String> guildIds = gcGroupData.getGuildIds();
		Map<String, GCGuildData> dataMap = RedisProxy.getInstance().getGCGuildsData(guildIds);
		List<GCGuildData> guildList = new ArrayList<>(dataMap.values());
		GCGetGroupRankResp.Builder builder = GCGetGroupRankResp.newBuilder();
		for (GCGuildData guildData : guildList) {
			GCGuildInfo.Builder guildBuilder = guildData.build();
			guildBuilder.setBattleRank(guildData.getGroupRank());
			guildBuilder.setTotalPower(guildData.getTotalPower());
			builder.addGuildInfo(guildBuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_GROUP_RANK_S_VALUE, builder));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 构建活动时间信息
	 * @return
	 */
	public GCStateInfo.Builder genStateInfo(String playerId) {
		GCStateInfo.Builder builder = GCStateInfo.newBuilder();
		GCState state = activityInfo.state;
		int termId = activityInfo.termId;
		builder.setStage(termId);
		builder.setState(state);
		ChampionshipTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg != null) {
			builder.setShowStartTime(cfg.getShowStartTimeValue());
			builder.setSignStartTime(cfg.getSignStartTimeValue());
			builder.setMatchStartTime(cfg.getMatchStartTimeValue());
			builder.setWarStartTime(cfg.getWarStartTimeValue());
			builder.setWar16To8Time(cfg.getWar16to8TimeValue());
			builder.setWar8To4Time(cfg.getWar8to4TimeValue());
			builder.setWar4To2Time(cfg.getWar4to2TimeValue());
			builder.setWar2To1Time(cfg.getWar2to1TimeValue());
			builder.setEndStartTime(cfg.getEndStartTimeValue());
			builder.setHiddenTime(cfg.getHiddenTimeValue());
			builder.setNewlyTime(HawkTime.getAM0Date(new Date(cfg.getShowStartTimeValue())).getTime() + GsConst.DAY_MILLI_SECONDS);
		}
		return builder;
	}

	private  List<GCPlayerInfo> genMemberList(String guildId) {
		int termId = activityInfo.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		List<GCPlayerInfo> list = new ArrayList<>();
		Set<Tuple> memberIds = RedisProxy.getInstance().getGCPlayerIdAndPower(termId, guildId);
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if(guild == null){ // 夸服
			return list;
		}
		String tag = guild.getTag();
		int rank = 1;
		for(Tuple tuple : memberIds){
			GCPlayerInfo.Builder playerInfo = GCPlayerInfo.newBuilder();
			String id = tuple.getElement();
			long battlePoint = (long) tuple.getScore();
			Player memberPlayer = GlobalData.getInstance().makesurePlayer(id);
			playerInfo.setId(id);
			playerInfo.setName(memberPlayer.getName());
			playerInfo.setServerId(serverId);
			playerInfo.setIcon(memberPlayer.getIcon());
			String pfIcon = memberPlayer.getPfIcon();
			if (!HawkOSOperator.isEmptyString(pfIcon)) {
				playerInfo.setPfIcon(pfIcon);
			}
			playerInfo.setGuildId(guildId);
			playerInfo.setGuildTag(tag);
			playerInfo.setBattlePoint(battlePoint);
			playerInfo.setBattleRank(rank);
			list.add(playerInfo.build());
			rank++;
		}
		return list;
		
	}

	/************************* 活动控制 ******************************/

	/**
	 * 保存出战数据. 把请求接口放到PlayermarchModule里边. 就可以用原来行军的checkMarchReq方法了.
	 */
	public int saveBattlePlayer(Player player, WorldMarchReq req) {
		if (activityInfo.getState() != GCState.SIGN_UP) {
			return Status.Error.CHAMPIONSHIP_STATE_NOT_SIGN_VALUE;
		}

		if (player.getCityLv() < ChampionshipConstCfg.getInstance().getCityLvlLimit()) {
			return Status.Error.CITY_LEVEL_NOT_ENOUGH_VALUE;
		}
		
		try {
			PlayerMarchModule marchModule = player.getModule(GsConst.ModuleType.WORLD_MARCH_MODULE);
			if (!marchModule.checkMarchReq(req, HP.code.CHAMPIONSHIP_SIGN_UP_C_VALUE, new ArrayList<>(), null, false)) {
				return Status.Error.WORLD_MARCH_ARMY_TOTALCOUNT_VALUE;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		int termId = activityInfo.getTermId();
		double battlePoint = 0;
		String guildId = player.getGuildId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		guildInfoCheck(guildId);
		List<Integer> heroIdList = req.getHeroIdList();
		int superSoldierId = req.getSuperSoldierId();
		PBChampionPlayer.Builder data = PBChampionPlayer.newBuilder();
		data.setPlayerInfo(BuilderUtil.buildSnapshotData(player));
		int totalCnt = 0;
		for (ArmySoldierPB pbarmy : req.getArmyInfoList()) {
			PBChampionSoldier.Builder army = PBChampionSoldier.newBuilder().setArmyId(pbarmy.getArmyId()).setCount(pbarmy.getCount())
					.setStar(player.getSoldierStar(pbarmy.getArmyId()))
					.setPlantStep(player.getSoldierStep(pbarmy.getArmyId()))
					.setPlantSkillLevel(player.getSoldierPlantSkillLevel(pbarmy.getArmyId()))
					.setPlantMilitaryLevel(player.getSoldierPlantMilitaryLevel(pbarmy.getArmyId()));
			data.addSoldiers(army);
			int armyId = pbarmy.getArmyId();
			int armyCnt = pbarmy.getCount();
			ArmyEntity armyEntity = player.getData().getArmyEntity(armyId);
			if (armyEntity == null || armyEntity.getTotal() < armyCnt) {
				return Status.Error.CHAMPIONSHIP_ARMY_CNT_NOT_ENOUGH_VALUE;
			}
			BattleSoldierCfg cfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
			battlePoint += 1d * armyCnt * cfg.getPower();
			totalCnt += armyCnt;
		}

		if (totalCnt <= 0) {
			return Status.Error.CHAMPIONSHIP_ARMY_EMPTY_VALUE;
		}

		for (int heroId : heroIdList) {
			Optional<PlayerHero> heroOp = player.getHeroByCfgId(heroId);
			if (heroOp.isPresent()) {
				// 检查英雄出征
				if (heroIdList.contains(heroOp.get().getConfig().getProhibitedHero())) {
					return Status.Error.PLAYER_HERO_MARCH_TYPE_ERROR_VALUE;
				}
				data.addHeros(heroOp.get().toPBobj());
				battlePoint += heroOp.get().power();
			}
		}
		Optional<SuperSoldier> ssoldierOp = player.getSuperSoldierByCfgId(superSoldierId);
		if (ssoldierOp.isPresent()) {
			data.setSuperSoldier(ssoldierOp.get().toPBobj());
			battlePoint += ssoldierOp.get().power();
		}

		ArmourBriefInfo armour = player.genArmourBriefInfo(req.getArmourSuit());
		data.setArmourBrief(armour);

		for (EffType eff : EffType.values()) {
			PBChampionEff pbef = PBChampionEff.newBuilder().setEffectId(eff.getNumber())
					.setValue(player.getEffect().getEffVal(eff, new EffectParams(req, new ArrayList<>()))).build();
			data.addEffs(pbef);
		}
		
		for (int dressId : req.getMarchDressList()) {
			data.addDressId(dressId);
		}
		data.setManhattanFuncUnlock(player.checkManhattanFuncUnlock());
		data.setManhattanInfo(player.buildManhattanInfo(req.getManhattan()));
		data.setMechacoreFuncUnlock(player.checkMechacoreFuncUnlock());
		data.setMechacoreInfo(player.buildMechacoreInfo(req.getMechacoreSuit()));
		
		RedisProxy.getInstance().updateGCPbattleData(termId, player.getId(), data);
		// 保存参战玩家数据
		RedisProxy.getInstance().updateGCPlayerId(termId, guildId, player.getId(), (long) Math.ceil(battlePoint));
		syncPageInfo(player);

		LogUtil.logGCPlayerInfo(player, termId);
		return Status.SysError.SUCCESS_OK_VALUE;

	}
	
	
	/**
	 * 获取玩家部队总数
	 * @param championPlayerPb
	 * @return
	 */
	private int calcArmyCnt(PBChampionPlayer.Builder championPlayerPb) {
		int sum = 0;
		List<PBChampionSoldier> list = championPlayerPb.getSoldiersList();
		if (list == null || list.isEmpty()) {
			return sum;
		}
		for (PBChampionSoldier soldier : list) {
			sum += soldier.getCount();
		}
		return sum;
	}

	/**
	 * 联盟信息检测
	 * @param guildId
	 */
	private void guildInfoCheck(String guildId) {
		String serverId = GsConfig.getInstance().getServerId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		GCGuildData guildDate = RedisProxy.getInstance().getGCGuildData(guildId);
		boolean needUpdate = false;
		// 不存在锦标赛信息
		if(guildDate == null){
			guildDate = new GCGuildData();
			guildDate.setId(guildId);
			guildDate.setServerId(serverId);
			guildDate.setName(guild.getName());
			guildDate.setFlag(guild.getFlagId());
			guildDate.setTag(guild.getTag());
			guildDate.setGrade(GCGuildGrade.GRADE_3);
			needUpdate = true;
		}
		// 区服信息不一致
		if(!serverId.equals(guildDate.getServerId())){
			guildDate.setServerId(serverId);
			needUpdate = true;
		}
		if(needUpdate){
			RedisProxy.getInstance().updateGCGuildData(guildDate);
		}
		
	}

	/**
	 * 联盟解散
	 * @param guildId
	 */
	public void onGuildDismiss(String guildId) {
		if (activityInfo.state == GCState.NOT_OPEN) {
			return;
		}
		try {
			if(activityInfo.state == GCState.SIGN_UP){
				RedisProxy.getInstance().removeGCPlayerIds(activityInfo.termId, guildId);
				RedisProxy.getInstance().removeGCGuildData(guildId);
			}
			signMemberMap.remove(guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	/**
	 * 成员退出联盟
	 * @param player
	 * @param guildId
	 */
	public void onQuitGuild(Player player, String guildId) {
		if (activityInfo.state == GCState.NOT_OPEN) {
			return;
		}
		try {
			if(activityInfo.state == GCState.SIGN_UP){
				RedisProxy.getInstance().removeGCPlayerId(activityInfo.getTermId(), guildId, player.getId());
				RedisProxy.getInstance().removeGCPbattleData(activityInfo.termId, player.getId());
			}
			syncPageInfo(player);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 战斗
	 * @param termId
	 * @param battleId
	 * @param atkData
	 * @param defData
	 * @param playerB 
	 * @param playerA 
	 * @return
	 */
	public BattleOutcome doFight(int termId, String battleId, PBChampionPlayer.Builder atkData, PBChampionPlayer.Builder defData, GCPlayerInfo.Builder playerA, GCPlayerInfo.Builder playerB) {
		IWorldMarch atkMarch = buildMarch(atkData.build());

		IWorldMarch defMarch = buildMarch(defData.build());

		List<Player> atkPlayers = new ArrayList<>();
		atkPlayers.add(atkMarch.getPlayer());

		// 防守方玩家
		List<Player> defPlayers = new ArrayList<>();
		defPlayers.add(defMarch.getPlayer());

		// 进攻方行军
		List<IWorldMarch> atkMarchs = new ArrayList<>();
		atkMarchs.add(atkMarch);
		// 防守方行军
		List<IWorldMarch> defMarchs = new ArrayList<>();
		defMarchs.add(defMarch);

		// 战斗数据输入
		PvpBattleIncome battleIncome = BattleService.getInstance().initPVPBattleData(BattleConst.BattleType.GUILD_CHAMPIONSHIP, 0, atkPlayers, defPlayers, atkMarchs, defMarchs,
				BattleSkillType.BATTLE_SKILL_NONE);
		battleIncome.getBattle().setDuntype(DungeonMailType.TBLY);
		// 战斗数据输出
		BattleOutcome battleOutcome = BattleService.getInstance().doBattle(battleIncome);
		FightMailService.getInstance().recordGcMail(termId, battleId, battleIncome, battleOutcome, playerA, playerB);
		updateArmyData(atkData, battleOutcome.getAftArmyMapAtk().get(atkMarch.getPlayerId()));
		updateArmyData(defData, battleOutcome.getAftArmyMapDef().get(defMarch.getPlayerId()));
		if (battleIncome.getBattle().isSaveDebugLog()) {
			String filename = HawkAppCfg.getInstance().getLogPath() + File.separator + atkData.getPlayerInfo().getName() + "VS" + defData.getPlayerInfo().getName();
			HawkOSOperator.saveAsFile(battleIncome.getBattle().getDebugLog(), filename);
		}
		return battleOutcome;
	}
	
	/**
	 * 计算杀敌分数
	 * @param armyList
	 * @return
	 */
	private long calcKillArmyScore(List<ArmyInfo> armyList) {
		ChampionshipConstCfg constCfg = HawkConfigManager.getInstance().getKVInstance(ChampionshipConstCfg.class);
		long score = 0;
		for (ArmyInfo armyInfo : armyList) {
			Map<Integer, Integer> killMap = armyInfo.getKillInfo();
			for (Entry<Integer, Integer> killEntry : killMap.entrySet()) {

				int armyId = killEntry.getKey();
				BattleSoldierCfg config = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
				if (config == null) {
					continue;
				}
				int cnt = killEntry.getValue();
				score += 1l * cnt * constCfg.getScore(config.getLevel());
			}
		}
		return score;
	}
	
	private IWorldMarch buildMarch(PBChampionPlayer sourceData) {
		TemporaryMarch atkMarch = new TemporaryMarch();
		List<ArmyInfo> armys = new ArrayList<>(sourceData.getSoldiersCount());
		for (PBChampionSoldier sd : sourceData.getSoldiersList()) {
			if (sd.getCount() > 0) {
				armys.add(new ArmyInfo(sd.getArmyId(), sd.getCount()));
			}
		}

		CHAMPlayer player = new CHAMPlayer(HawkXID.nullXid(), sourceData);
		atkMarch.setArmys(armys);
		atkMarch.setPlayer(player);
		atkMarch.getMarchEntity().setArmourSuit(ArmourSuitType.ONE_VALUE);
		atkMarch.getMarchEntity().setMechacoreSuit(MechaCoreSuitType.MECHA_ONE_VALUE);
		atkMarch.getMarchEntity().setHeroIdList(sourceData.getHerosList().stream().map(PBHeroInfo::getHeroId).collect(Collectors.toList()));
		atkMarch.getMarchEntity().setSuperSoldierId(sourceData.getSuperSoldier().getSuperSoldierId());
		atkMarch.setHeros(player.getHeroByCfgId(null));
		atkMarch.getMarchEntity().setDressList(sourceData.getDressIdList());
		return atkMarch;
	}

	/**
	 * 更新胜利方玩家数据,以便进行下一场战斗
	 * @param atkData
	 * @param list
	 */
	private void updateArmyData(PBChampionPlayer.Builder atkData, List<ArmyInfo> armyList) {
		List<PBChampionSoldier.Builder> soldierList = atkData.getSoldiersBuilderList();
		for(PBChampionSoldier.Builder builder : soldierList){
			int armyId = builder.getArmyId();
			for(ArmyInfo army : armyList){
				if(army.getArmyId() == armyId){
					builder.setCount(army.getFreeCnt());
				}
			}
		}
	}

	/**
	 * 获取成员出战部队信息
	 * @param player
	 * @param targetId
	 */
	public void onGetMemberMarchInfo(Player player, String targetId) {
		if (activityInfo.getState() != GCState.SIGN_UP && activityInfo.getState() != GCState.MATCH) {
			player.sendError(HP.code.CHAMPIONSHIP_GET_MARCH_INFO_C_VALUE, Status.Error.CHAMPIONSHIP_STATE_ERROR_VALUE, 0);
			return;
		}
		PBChampionPlayer.Builder targetInfo = RedisProxy.getInstance().getGCPbattleData(activityInfo.getTermId(), targetId);
		if (targetInfo == null) {
			player.sendError(HP.code.CHAMPIONSHIP_GET_MARCH_INFO_C_VALUE, Status.Error.CHAMPIONSHIP_PLAYER_BATTLE_DATA_ERROR_VALUE, 0);
			return;
		}
		GCGetMemberMarchResp.Builder builder = GCGetMemberMarchResp.newBuilder();
		builder.setInfo(targetInfo);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_MARCH_INFO_S, builder));

	}
	
	
	public int getReward(Player player) {
		if (!player.hasGuild()) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (activityInfo.state != GCState.END) {
			return Status.Error.CHAMPIONSHIP_STATE_ERROR_VALUE;
		}
		int termId = activityInfo.getTermId();
		String playerId = player.getId();
		String guildId = player.getGuildId();
		GCRewardState state = getRewardState(playerId, termId, guildId);
		switch (state) {
		case NO_REWARD:
			return Status.Error.CHAMPIONSHIP_NO_REWARD_VALUE;
		case TOOK:
			return Status.Error.CHAMPIONSHIP_ALREADY_REWARDED_VALUE;
		case CAN_TAKE:
			GCGuildData gcGuildData = RedisProxy.getInstance().getGCGuildData(guildId);
			RedisProxy.getInstance().updateGCRewardInfo(termId, guildId, playerId, String.valueOf(1));
			ChampionshipAwardCfg cfg = getAwardCfg(gcGuildData.getLastGrade().getValue(), gcGuildData.getGroupRank());
			AwardItems awardItems = AwardItems.valueOf();
			awardItems.addItemInfos(cfg.getRewardList());
			awardItems.rewardTakeAffectAndPush(player, Action.GUILD_ACCEPTAPPLY, true);
			syncPageInfo(player);
			break;
		default:
			break;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 获取奖励配置
	 * @param grade
	 * @param groupRank
	 * @return
	 */
	private ChampionshipAwardCfg getAwardCfg(int grade, int groupRank) {
		ConfigIterator<ChampionshipAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(ChampionshipAwardCfg.class);
		for (ChampionshipAwardCfg cfg : its) {
			int rankLow = cfg.getRankRange().first;
			int rankHight = cfg.getRankRange().second;
			if (cfg.getSection() == grade && groupRank >= rankLow && groupRank <= rankHight) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 获取小组历史战斗阶段数据
	 * @param player
	 * @return
	 */
	public int onGetHistoryBattle(Player player) {
		if (!player.hasGuild()) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (activityInfo.state != GCState.END) {
			return Status.Error.CHAMPIONSHIP_STATE_ERROR_VALUE;
		}

		String guildId = player.getGuildId();
		String groupId = groupMapping.get(guildId);
		if (HawkOSOperator.isEmptyString(groupId)) {
			return Status.Error.CHAMPIONSHIP_GUILD_NOT_JOIN_VALUE;
		}
		int termId = activityInfo.getTermId();
		GCGroupData gcGroupData = RedisProxy.getInstance().getGCGroupData(groupId, termId);
		if (gcGroupData == null) {
			return Status.Error.CHAMPIONSHIP_GUILD_NOT_JOIN_VALUE;
		}
		GetHistoryBattleResp.Builder builder = GetHistoryBattleResp.newBuilder();
		GCBattleStage stage = GCBattleStage.TO_1;
		while (true) {
			Map<GCBattleStage, GCStageBattle> battleMap = warStageGroupInfo.get(groupId);
			if (battleMap == null) {
				battleMap = new HashMap<>();
				warStageGroupInfo.put(groupId, battleMap);
			}
			GCStageBattle stageBattle = battleMap.get(stage);
			if (stageBattle == null) {
				stageBattle = getWarStageInfo(gcGroupData, stage);
				if (stageBattle != null) {
					battleMap.put(stage, stageBattle);
				}
			}
			if (stageBattle != null) {
				builder.addStageBattle(stageBattle);
			}
			if (stage == stage.getLastStage()) {
				break;
			}
			stage = stage.getLastStage();
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CHAMPIONSHIP_GET_HISTORY_BATTLE_S, builder));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取段位排行信息
	 * @param req
	 * @param player
	 * @return
	 */
	public GCGetRankResp.Builder getRankInfoBuilder(GCRankType rtype, GCGuildGrade grade, Player player) {
		GCGetRankResp.Builder builder = GCGetRankResp.newBuilder();
		GCState state = activityInfo.getState();
		if (state == GCState.SHOW || state == GCState.SIGN_UP || state == GCState.MATCH || (state == GCState.WAR && fightState.state != GCBattleState.OPEN_SHOW)) {
			builder.setIsLastStage(true);
		}else{
			builder.setIsLastStage(false);
		}
		builder.setRankType(rtype);
		GCGradeRankType rankType = GCGradeRankType.valueOf(rtype.getNumber());
		if (rankType == null) {
			return builder;
		}
		List<GCRankInfo> rankList = null;
		if (rankType == GCGradeRankType.G_KILL) {
			if (grade == null) {
				return builder;
			}
			builder.setGrade(grade.getValue());
			rankList = guildRankMap.get(grade);
			if (rankList != null && !rankList.isEmpty()) {
				builder.addAllRankInfo(rankList);
			}
			if (!player.hasGuild()) {
				return builder;
			}
			GCSelfRank.Builder selfRank = null;
			if (rankList != null && !rankList.isEmpty()) {
				for (GCRankInfo rankInfo : rankList) {
					if (rankInfo.getId().equals(player.getGuildId())) {
						selfRank = GCSelfRank.newBuilder();
						selfRank.setRankType(rtype);
						selfRank.setRankInfo(rankInfo);
						break;
					}
				}
			}
			if (selfRank == null) {
				selfRank = getSelfRank(rankType, grade, player);
			}
			if (selfRank != null) {
				builder.addSelfRank(selfRank);
			}
		} else {
			rankList = selfRankMap.get(rankType);
			if (rankList == null || rankList.isEmpty()) {
				return builder;
			}
			builder.addAllRankInfo(rankList);
			for (GCGradeRankType type : GCGradeRankType.values()) {
				if (type.isGuildRank()) {
					continue;
				}
				List<GCRankInfo> list = selfRankMap.get(type);
				if (list == null || list.isEmpty()) {
					continue;
				}
				GCSelfRank.Builder selfRank = null;
				for (GCRankInfo rankInfo : list) {
					if (rankInfo.getId().equals(player.getId())) {
						selfRank = GCSelfRank.newBuilder();
						selfRank.setRankType(GCRankType.valueOf(type.getValue()));
						selfRank.setRankInfo(rankInfo);
						builder.addSelfRank(selfRank);
						break;
					}
				}
				if (selfRank == null) {
					selfRank = getSelfRank(type, grade, player);
				}
				if (selfRank != null) {
					builder.addSelfRank(selfRank);
				}
			}
		}
		
		return builder;
	}
	
	/**
	 * 获取未上榜玩家的个人排行
	 * @param rankType
	 * @param grade
	 * @param player
	 * @return
	 */
	private GCSelfRank.Builder getSelfRank(GCGradeRankType rankType, GCGuildGrade grade, Player player) {
		int termId = activityInfo.getTermId();
		GCState state = activityInfo.getState();
		// 这三个阶段展示上一期排行数据
		if (state == GCState.SHOW || state == GCState.SIGN_UP || state == GCState.MATCH || (state == GCState.WAR && fightState.state != GCBattleState.OPEN_SHOW)) {
			termId = termId - 1;
		}
		String key = getRankKey(termId, rankType, grade);
		String memberId = player.getId();
		if (rankType.isGuildRank()) {
			if (!player.hasGuild()) {
				return null;
			}
			memberId = player.getGuildId();
		}
		GCSelfRank.Builder builder = GCSelfRank.newBuilder();
		builder.setRankType(GCRankType.valueOf(rankType.getValue()));
		GCRankInfo.Builder rankInfo = GCRankInfo.newBuilder();
		long score = RedisProxy.getInstance().getGCRankInfo(key, memberId);
		rankInfo.setId(memberId);
		rankInfo.setScore(score);
		rankInfo.setRank(-1);
		if (rankType.isGuildRank()) {
			GCGuildData guildData = RedisProxy.getInstance().getGCGuildData(memberId);
			if (guildData == null) {
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(memberId);
				rankInfo.setServerId(guildObj.getServerId());
				rankInfo.setTag(guildObj.getTag());
				rankInfo.setName(guildObj.getName());
				rankInfo.setFlag(guildObj.getFlagId());
			} else {
				rankInfo.setServerId(guildData.getServerId());
				rankInfo.setTag(guildData.getTag());
				rankInfo.setName(guildData.getName());
				rankInfo.setFlag(guildData.getFlag());
			}
		} else {
			GCPlayerInfo playerInfo = RedisProxy.getInstance().getGCPlayerData(termId, memberId);
			if (playerInfo == null) {
				rankInfo.setServerId(GsConfig.getInstance().getServerId());
				rankInfo.setTag(player.getGuildTag());
				rankInfo.setName(player.getName());
				rankInfo.setIcon(player.getIcon());
				String pfIcon = player.getPfIcon();
				if (HawkOSOperator.isEmptyString(pfIcon)) {
					rankInfo.setPfIcon(pfIcon);
				}
			} else {
				rankInfo.setServerId(playerInfo.getServerId());
				rankInfo.setTag(playerInfo.getGuildTag());
				rankInfo.setName(playerInfo.getName());
				rankInfo.setIcon(playerInfo.getIcon());
				if (playerInfo.hasPfIcon()) {
					rankInfo.setPfIcon(playerInfo.getPfIcon());
				}
			}
			rankInfo.addAllPersonalProtectSwitch(player.getData().getPersonalProtectListVals());
		}
		builder.setRankInfo(rankInfo);
		return builder;
	}

	/**
	 * 获取联盟排行界面信息
	 * @param player
	 * @return
	 */
	public GCGetRankResp.Builder getGRankPageInfo(Player player) {
		GCGuildGrade grade = GCGuildGrade.GRADE_3;
		GCState state = activityInfo.getState();
		GCGuildData guildData = RedisProxy.getInstance().getGCGuildData(player.getGuildId());
		if (guildData != null) {
			if (guildData.getTermId() != activityInfo.getTermId()) {
				grade = guildData.getLastGrade();
			} else {

				if (state == GCState.MATCH || (state == GCState.WAR && fightState.state != GCBattleState.OPEN_SHOW)) {
					grade = guildData.getLastBattleGrade();
				} else {
					grade = guildData.getLastGrade();
				}
			}
		}

		GCGetRankResp.Builder builder = getRankInfoBuilder(GCRankType.G_KILL, grade, player);
		builder.setSelfGrade(grade.getValue());
		return builder;
	}

}
