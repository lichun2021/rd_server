package com.hawk.game.service.tiberium;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.protocol.*;
import com.hawk.game.service.ActivityService;
import com.hawk.gamelib.GameConst;
import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuple3;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.activity.event.impl.TblyGuessSendRewardEvent;
import com.hawk.activity.type.impl.tiberiumGuess.cfg.TblyGuessActiviytKVCfg;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumGuildAwardCfg;
import com.hawk.game.config.TiberiumPersonAwardCfg;
import com.hawk.game.config.TiberiumSeasonGuildAwardCfg;
import com.hawk.game.config.TiberiumSeasonPersonAwardCfg;
import com.hawk.game.config.TiberiumSeasonRankAwardCfg;
import com.hawk.game.config.TiberiumSeasonTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg.PlayerGameRecord;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.TiberiumWar.GetTLWGuildRankResp;
import com.hawk.game.protocol.TiberiumWar.PBTLWEliminationGroup;
import com.hawk.game.protocol.TiberiumWar.TLWBattle;
import com.hawk.game.protocol.TiberiumWar.TLWFinalMatchMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWFinalMatchStageInfo;
import com.hawk.game.protocol.TiberiumWar.TLWFreeRoom;
import com.hawk.game.protocol.TiberiumWar.TLWGetFinalMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoReq;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetOBRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetScoreInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGetTeamGuildInfoResp;
import com.hawk.game.protocol.TiberiumWar.TLWGroup;
import com.hawk.game.protocol.TiberiumWar.TLWGuildBaseInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGuildRank;
import com.hawk.game.protocol.TiberiumWar.TLWPageInfo;
import com.hawk.game.protocol.TiberiumWar.TLWRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWScoreInfo;
import com.hawk.game.protocol.TiberiumWar.TLWSelfMatchList;
import com.hawk.game.protocol.TiberiumWar.TLWState;
import com.hawk.game.protocol.TiberiumWar.TLWStateInfo;
import com.hawk.game.protocol.TiberiumWar.TLWTeamGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TWBattleLog;
import com.hawk.game.protocol.TiberiumWar.TWGuildInfo;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventTiberiumWar;
import com.hawk.game.service.tiberium.TiberiumConst.EloReason;
import com.hawk.game.service.tiberium.TiberiumConst.FightState;
import com.hawk.game.service.tiberium.TiberiumConst.RoomState;
import com.hawk.game.service.tiberium.TiberiumConst.TLWActivityState;
import com.hawk.game.service.tiberium.TiberiumConst.TLWBattleType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWEliminationGroupType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.service.tiberium.TiberiumConst.TLWWarType;
import com.hawk.game.service.tiberium.comparetor.TLWGuildInitPowerRankComparator;
import com.hawk.game.service.tiberium.comparetor.TLWGuildJoinInfoSeanRankComparator;
import com.hawk.game.service.tiberium.comparetor.TLWGuildJoinInfoTeamGroupComparator;
import com.hawk.game.service.tiberium.logunit.TLWLeaguaGuildInfoUnit;
import com.hawk.game.service.tiberium.logunit.TLWWarResultLogUnit;
import com.hawk.game.service.tiberium.logunit.TWEloScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWGuildScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWLogUtil;
import com.hawk.game.service.tiberium.logunit.TWPlayerScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWPlayerSeasonScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWSelfRewardLogUnit;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;

import redis.clients.jedis.Tuple;

/**
 * 泰伯利亚之战服务类
 * @author Jesse
 * @since 2018年4月23日
 */
public class TiberiumLeagueWarService extends HawkAppObj {
	
	/**
	 * 全局实例对象
	 */
	private static TiberiumLeagueWarService instance = null;
	
	/**
	 * 活动时间信息数据
	 */
	public static TLWActivityData activityInfo = new TLWActivityData();
	
	/**
	 * 匹配状态信息
	 */
	public static TLWMatchState matchServerInfo = new TLWMatchState();
	
	/**
	 * 战斗阶段状态信息
	 */
	public static TLWFightState fightState = new TLWFightState();
	
	/**
	 * 赛季参与联盟当前组别信息
	 */
	public static Map<String, TLWGroupType> currGuildGroup = new ConcurrentHashMap<>();
	
	/**
	 * 匹配结果列表
	 */
	public static List<TWRoomData> roomList = new ArrayList<>();
	
	/**
	 * 当前已完成匹配的房间信息
	 */
	public static Map<Integer, List<TWRoomData>> matchedRoomMap = new ConcurrentHashMap<>();
	
	/**
	 * 赛季出战联盟当前排行
	 */
	public static Map<String, Integer> currRankMap = new ConcurrentHashMap<>();
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static TiberiumLeagueWarService getInstance() {
		return instance;
	}

	public TiberiumLeagueWarService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		try {
			// 读取活动阶段数据
			activityInfo = RedisProxy.getInstance().getTLWActivityInfo();
			// 进行阶段检测
			checkStateChange();
			// 拉取参与的联盟信息
			loadJoinGuildsInfo();
			if(activityInfo.getState().getNumber() > TLWActivityState.TLW_MATCH.getNumber()){
				loadMatchResult();
			}
			if (activityInfo.getTermId() > 0) {
				loadMatchedRoom();
			}
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();

					// 匹配轮询检测
					if (activityInfo.state == TLWActivityState.TLW_MATCH) {
						matchTick();
					}
					// 战斗阶段轮询检测
					else if (activityInfo.state == TLWActivityState.TLW_WAR_OPEN) {
						try {
							fightTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			});

		} catch (Exception e) {
					HawkException.catchException(e);
					return false;
				}
				return true;
	}
	
	/**
	 * 全量加载本赛季已匹配完成的房间信息
	 */
	private void loadMatchedRoom() {
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		Map<Integer, List<TWRoomData>> roomMap = new ConcurrentHashMap<>();
		int maxTerm = termId < TiberiumConstCfg.getInstance().getEliminationStartTermId() ? TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1 : termId;
		for (int i = 1; i <= maxTerm; i++) {
			int markId = combineSeasonTerm(season, i);
			List<TWRoomData> list = RedisProxy.getInstance().getAllTWRoomData(markId);
			roomMap.put(i, list);
		}
		matchedRoomMap = roomMap;
	}
	
	/**
	 * 更新本期已匹配房间信息
	 */
	private void updateCurrMatchedRoom(){
		int termId = activityInfo.getTermId();
		int markId = activityInfo.getMark();
		List<TWRoomData> list = RedisProxy.getInstance().getAllTWRoomData(markId);
		matchedRoomMap.put(termId, list);
	}

	/**
	 * 加载匹配结果
	 */
	private void loadMatchResult() {
		int mark = activityInfo.getMark();
		List<TWRoomData> rooms = RedisProxy.getInstance().getAllTWRoomData(mark);
		roomList = rooms;
	}

	public TLWActivityData getActivityInfo() {
		return activityInfo;
	}
	
	public Map<String, TLWGroupType> getJoinGuilds(){
		return currGuildGroup;
	}
	
	public Map<String, Integer> getCurrRanMap(){
		return currRankMap;
	}

	/**
	 * 匹配阶段轮询
	 */
	protected void matchTick() {
		try {
			if (activityInfo.state != TLWActivityState.TLW_MATCH) {
				return;
			}

			String matchKey = RedisProxy.getInstance().TLWACTIVITY_MATCH_STATE + ":" + activityInfo.getSeason() + ":" + activityInfo.getTermId();
			String matchLockKey = RedisProxy.getInstance().TLWACTIVITY_MATCH_LOCK + ":" + activityInfo.getSeason() + ":" + activityInfo.getTermId();

			// 初始化匹配阶段信息
			if (!matchServerInfo.hasInit || activityInfo.getSeason() != matchServerInfo.getSeason() || activityInfo.getTermId() != matchServerInfo.getTermId()) {
				matchServerInfo.setSeason(activityInfo.getSeason());
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
				// 赛季第一期,检测推送入围邮件
				if (activityInfo.getTermId() == 1 && !activityInfo.isSendPickedMail()) {
					loadJoinGuildsInfo();
					sendPickedMail();
					activityInfo.setSendPickedMail(true);
					RedisProxy.getInstance().updateTLWActivityInfo(activityInfo);
					// 检测普通泰伯比赛报名情况
					TiberiumWarService.getInstance().checkTWSignGuild(currGuildGroup.keySet());
					// 在线玩家推送活动状态
					for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
						syncStateInfo(player);
					}
				}
				return;
			}

			String serverId = GsConfig.getInstance().getServerId();
			long lock = RedisProxy.getInstance().getMatchLock(matchLockKey);
			boolean needSync = false;
			// 获取到匹配权限,设置有效期并进行匹配
			if (lock > 0) {
				RedisProxy.getInstance().getRedisSession().expire(matchLockKey, TiberiumConstCfg.getInstance().getMatchLockExpire());
				HawkLog.logPrintln("TiberiumLeagueWarService doMatch, serverId:{}", serverId);
				doMatch();
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
			if (needSync) {
				loadJoinGuildsInfo();
				// 在线玩家推送活动状态
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					syncStateInfo(player);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 推送入围邮件
	 */
	private void sendPickedMail() {
		int season = activityInfo.getSeason();
		Map<String, TLWGroupType> groupMap = currGuildGroup;
		for (String guildId : groupMap.keySet()) {
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj == null) {
				continue;
			}
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder().setMailId(MailId.TIBERIUM_SEASON_PICKED));
			TLWGuildJoinInfo joinInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
			TLWGuildData guildData = RedisProxy.getInstance().getTLWGuildData(guildId, season);
			// 入围时记录参赛联盟信息
			TLWLeaguaGuildInfoUnit logUnit = new TLWLeaguaGuildInfoUnit(activityInfo.getTermId(), joinInfo, guildData);
			TWLogUtil.logTimberiumLeaguaGuildInfo(logUnit);
		}
	}

	/**
	 * 计算当前活动周期
	 * 
	 * @return
	 */
	private TLWActivityData calcStateInfo() {
		TLWActivityData info = new TLWActivityData();
		if (TiberiumConstCfg.getInstance().isSystemClose()) {
			info.setState(TLWActivityState.TLW_CLOSE);
			return info;
		}
		long now = HawkTime.getMillisecond();


		// 游客服不开启该活动
		if (GsConfig.getInstance().getServerType() == ServerType.GUEST) {
			return info;
		}
		TiberiumSeasonTimeCfg cfg = calcTimeCfg();

		int season = 0;
		int termId = 0;
		TLWActivityState state = TLWActivityState.TLW_NOT_OPEN;
		// 没有可供开启的配置
		if (cfg != null) {
			season = cfg.getSeason();
			termId = cfg.getTermId();
			long matchStartTime = cfg.getMatchStartTimeValue();
			long matchEndTime = cfg.getMatchEndTimeValue();
			long manageEndTime = cfg.getManageEndTimeValue();
			long warStartTime = cfg.getWarStartTimeValue();
			long warEndTime = cfg.getWarEndTimeValue();
			long seasonEndShowTime = cfg.getSeasonEndShowTimeValue();
			if (now < matchStartTime) {
				state = TLWActivityState.TLW_PEACE;
			}
			if (now >= matchStartTime && now < matchEndTime) {
				state = TLWActivityState.TLW_MATCH;
			}
			if (now >= matchEndTime && now < manageEndTime) {
				state = TLWActivityState.TLW_WAR_MANGE;
			}
			if (now >= manageEndTime && now < warStartTime) {
				state = TLWActivityState.TLW_WAR_WAIT;
			}
			if (now >= warStartTime && now < warEndTime) {
				state = TLWActivityState.TLW_WAR_OPEN;
			}
			if (now >= warEndTime) {
				state = TLWActivityState.TLW_PEACE;
			}
			if(cfg.getSeasonEndTimeValue()>0 && now>= seasonEndShowTime){
				state = TLWActivityState.TLW_END_SHOW;
			}
		}
		info.setSeason(season);
		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	private void doMatch() {
		// 赛季首次参赛联盟
		if (activityInfo.getTermId() == 1) {
			int season = activityInfo.getSeason();
			RedisProxy.getInstance().removeTLWJoinGuild(season);
//			for(int termId = 1; termId < TiberiumConst.ELIMINATION_START_TERMID; termId++){
//				int mark = combineSeasonTerm(season, termId);
//				RedisProxy.getInstance().removeTWRoomData(mark);
//			}
			Map<Integer, List<TLWGuildJoinInfo>> teamMap = pickJoinGuild(TiberiumWar.TLWServer.TLW_OLD_SERVER);
			Map<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> matchTuple = calcTeamWarMatchTuple(teamMap);
			createTeamRoom(matchTuple,TiberiumWar.TLWServer.TLW_OLD_SERVER);
			Map<Integer, List<TLWGuildJoinInfo>> teamMapNew = pickJoinGuild(TiberiumWar.TLWServer.TLW_NEW_SERVER);
			Map<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> matchTupleNew = calcTeamWarMatchTuple(teamMapNew);
			createTeamRoom(matchTupleNew,TiberiumWar.TLWServer.TLW_NEW_SERVER);
		}
		TiberiumSeasonTimeCfg timeCfg = calcTimeCfg();
		TLWWarType warType = timeCfg.getWarType();
		switch (warType) {
		case TEAM_WAR:
			//S3开始新赛制,小组赛11长比赛匹配在首场已完成
			break;
		case FINAL_WAR:
			this.doEliminationMatch();
			break;
		default:
			break;
		}
	}
	
	

	/**
	 * 小组赛匹配
	 * @param teamMap
	 */
	private Map<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> calcTeamWarMatchTuple(Map<Integer, List<TLWGuildJoinInfo>> teamMap) {
		int season = activityInfo.getSeason();
		Map<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> matchMap = new HashMap<>();
		ConfigIterator<TiberiumSeasonTimeCfg> cfgIts = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		Map<Integer, List<HawkTuple2<Integer, Integer>>> matchTemp = calcSingleCycleMap(TiberiumConst.TEAM_MEMBER_CNT);
		for (TiberiumSeasonTimeCfg cfg : cfgIts) {
			int termId = cfg.getTermId();
			if (cfg.getSeason() != season || cfg.getType() != TiberiumConst.TLWWarType.TEAM_WAR.getNumber()) {
				continue;
			}
			List<HawkTuple2<Integer, Integer>> listTemp = matchTemp.get(termId);
			List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>> matchList = new ArrayList<>();
			// 各小组匹配结果
			for (Entry<Integer, List<TLWGuildJoinInfo>> entry : teamMap.entrySet()) {
				List<TLWGuildJoinInfo> teamList = entry.getValue();
				for (HawkTuple2<Integer, Integer> temp : listTemp) {
					HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo> matchTuple = new HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>(teamList.get(temp.first),
							teamList.get(temp.second));
					matchList.add(matchTuple);
				}
			}
			matchMap.put(termId, matchList);
		}
		return matchMap;
	}

	/**
	 * 创建小组赛循环赛房间
	 * @param matchTuple
	 */
	private void createTeamRoom(Map<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> matchTuple, TiberiumWar.TLWServer serverType) {
		Random random = new Random();
		int season = activityInfo.getSeason();
		for (Entry<Integer, List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>>> entry : matchTuple.entrySet()) {
			List<TWRoomData> roomList = new ArrayList<>();
			List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>> tupleList = entry.getValue();
			int termId = entry.getKey();
			TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
			int mark = combineSeasonTerm(season, termId);
			// 根据匹配信息生成房间信息
			for (HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo> tuple : tupleList) {
				TWRoomData roomData = new TWRoomData();
				String server1 = tuple.first.getServerId();
				String server2 = tuple.second.getServerId();
				TLWGuildJoinInfo guildA;
				TLWGuildJoinInfo guildB;
				String roomServer = "";
				boolean result = server1.compareTo(server2) < 0;
				if (TiberiumConstCfg.getInstance().isRoomServerRandomOpen()) {
					result = random.nextInt(2) == 0;
				}
				if (result) {
					roomServer = server1;
					roomData.setGuildA(tuple.first.getId());
					roomData.setGuildB(tuple.second.getId());
					guildA = tuple.first;
					guildB = tuple.second;
				} else {
					roomServer = server2;
					roomData.setGuildA(tuple.second.getId());
					roomData.setGuildB(tuple.first.getId());
					guildA = tuple.second;
					guildB = tuple.first;
				}
				roomData.setTimeIndex(0);
				roomData.setId(HawkOSOperator.randomUUID());
				roomData.setRoomServerId(roomServer);
				roomData.setGroup(TLWGroupType.TEAM_GROUP);
				roomData.setBattleType(TLWBattleType.TEAM_GROUP_BATTLE);
				roomData.setTeamId(tuple.first.getTeamId());
				roomData.setServerType(serverType.getNumber());
				roomList.add(roomData);
				LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, guildA.getId(), guildA.getServerId(), guildB.getId(),
						guildB.getServerId(), timeCfg.getWarStartTimeValue(),TLWBattleType.TEAM_GROUP_BATTLE.getValue(), serverType.getNumber());
				HawkLog.logPrintln("TiberiumLeaguaWarService createTeamRoom, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
						roomData.getId(), roomData.getRoomServerId(), season, termId, guildA.getId(), guildA.getServerId(), guildB.getId(), guildB.getServerId());
			}
			RedisProxy.getInstance().updateTWRoomData(roomList, mark);
		}
	}
	
	/**
	 * 淘汰赛初始化数据
	 */
	private void eliminationMatchInit(){
		int season = activityInfo.getSeason();
		Map<String, TLWGuildJoinInfo> joinMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		Map<Integer, List<TLWGuildJoinInfo>> teamMap = new HashMap<>();
		Map<Integer, List<TLWGuildJoinInfo>> teamMapNew = new HashMap<>();
		for (Entry<String, TLWGuildJoinInfo> entry : joinMap.entrySet()) {
			TLWGuildJoinInfo joinInfo = entry.getValue();
			if(joinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				int teamId = joinInfo.getTeamId();
				if (!teamMapNew.containsKey(teamId)) {
					teamMapNew.put(teamId, new ArrayList<>());
				}
				teamMapNew.get(teamId).add(joinInfo);
			}else {
				int teamId = joinInfo.getTeamId();
				if (!teamMap.containsKey(teamId)) {
					teamMap.put(teamId, new ArrayList<>());
				}
				teamMap.get(teamId).add(joinInfo);
			}

		}
		Map<Integer,List<TLWGuildJoinInfo>> rankGroupMap = new HashMap<>();
		for(Entry<Integer, List<TLWGuildJoinInfo>> entry : teamMap.entrySet()){
			List<TLWGuildJoinInfo> teamList = entry.getValue();
			Collections.sort(teamList, new TLWGuildJoinInfoTeamGroupComparator());
			int rank = 1;
			for (TLWGuildJoinInfo joinInfo : teamList) {
				joinInfo.setTeamRank(rank);
				if (!rankGroupMap.containsKey(rank)) {
					rankGroupMap.put(rank, new ArrayList<>());
				}
				rankGroupMap.get(rank).add(joinInfo);
				rank++;
			}
		}

		Map<Integer,List<TLWGuildJoinInfo>> rankGroupMapNew = new HashMap<>();
		for(Entry<Integer, List<TLWGuildJoinInfo>> entry : teamMapNew.entrySet()){
			List<TLWGuildJoinInfo> teamList = entry.getValue();
			Collections.sort(teamList, new TLWGuildJoinInfoTeamGroupComparator());
			int rank = 1;
			for (TLWGuildJoinInfo joinInfo : teamList) {
				joinInfo.setTeamRank(rank);
				if (!rankGroupMapNew.containsKey(rank)) {
					rankGroupMapNew.put(rank, new ArrayList<>());
				}
				rankGroupMapNew.get(rank).add(joinInfo);
				rank++;
			}
		}
		//组内在排序
		for(Entry<Integer, List<TLWGuildJoinInfo>> entry : rankGroupMap.entrySet()){
			List<TLWGuildJoinInfo> teamList = entry.getValue();
			Collections.sort(teamList, new TLWGuildJoinInfoTeamGroupComparator());		
		}
		//组内在排序
		for(Entry<Integer, List<TLWGuildJoinInfo>> entry : rankGroupMapNew.entrySet()){
			List<TLWGuildJoinInfo> teamList = entry.getValue();
			Collections.sort(teamList, new TLWGuildJoinInfoTeamGroupComparator());
		}
		//分配S,A,B组
		Map<TLWGroupType, List<TLWGuildJoinInfo>> groupMap = new HashMap<>();
		for(int rankGroup=1;rankGroup<=TiberiumConst.TEAM_MEMBER_CNT;rankGroup++){
			List<TLWGuildJoinInfo> teamList = rankGroupMap.get(rankGroup);
			int rank = 1;
			for (TLWGuildJoinInfo joinInfo : teamList) {
				joinInfo.setPositionGroupRank(rank);
				TLWGroupType groupType = calcEliminationGroupType(rankGroup, rank);
				if(groupType!= null){
					joinInfo.setGroup(groupType);
					joinInfo.setInitGroup(groupType);
					joinInfo.setEliminationGroup(TLWEliminationGroupType.ELIMINATION_WIN);
					if (!groupMap.containsKey(groupType)) {
						groupMap.put(groupType, new ArrayList<>());
					}
					groupMap.get(groupType).add(joinInfo);
				}
				rank ++;
			}
		}
		for(int rankGroup=1;rankGroup<=TiberiumConst.TEAM_MEMBER_CNT;rankGroup++){
			List<TLWGuildJoinInfo> teamList = rankGroupMapNew.get(rankGroup);
			int rank = 1;
			for (TLWGuildJoinInfo joinInfo : teamList) {
				joinInfo.setPositionGroupRank(rank);
				TLWGroupType groupType = calcEliminationGroupType(rankGroup, rank);
				if(groupType!= null){
					joinInfo.setGroup(groupType);
					joinInfo.setInitGroup(groupType);
					joinInfo.setEliminationGroup(TLWEliminationGroupType.ELIMINATION_WIN);
					if (!groupMap.containsKey(groupType)) {
						groupMap.put(groupType, new ArrayList<>());
					}
					groupMap.get(groupType).add(joinInfo);
				}
				rank ++;
			}
		}
		// 清空本期的房间信息,存入新创建房间
		RedisProxy.getInstance().removeTWRoomData(activityInfo.getMark());
		for (Entry<TLWGroupType, List<TLWGuildJoinInfo>> entry : groupMap.entrySet()) {
			eliminationGroupInit(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 双淘汰赛匹配
	 */
	private void doEliminationMatch(){
		int termId = activityInfo.getTermId();
		if(termId == TiberiumConstCfg.getInstance().getEliminationStartTermId()){
			//第一期，淘汰赛初始化
			this.eliminationMatchInit();
		}else if(termId == TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
			//最后一期，大决赛
			this.eliminationMatchFinal();
		}else{
			//中间的淘汰赛匹配
			this.eliminationMatchCalc();
		}
		
		
	} 
	
	
	
	/**
	 * 淘汰赛初始化
	 * @param groupType
	 * @param joinList
	 */
	private void eliminationGroupInit(TLWGroupType groupType, List<TLWGuildJoinInfo> joinList) {
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		int mark = activityInfo.getMark();
		TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
		List<TLWGuildJoinInfo> joinListNew = new ArrayList<>();
		List<TLWGuildJoinInfo> joinListOld = new ArrayList<>();
		for(TLWGuildJoinInfo joinInfo : joinList){
			if(joinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				joinListNew.add(joinInfo);
			}else {
				joinListOld.add(joinInfo);
			}
		}

		List<TLWGuildJoinInfo> leftListNew = new ArrayList<>();
		List<TLWGuildJoinInfo> rightListNew = new ArrayList<>();
		List<TLWGuildJoinInfo> leftListOld = new ArrayList<>();
		List<TLWGuildJoinInfo> rightListOld = new ArrayList<>();
		int halfGapNew = joinListNew.size() / 2;
		int halfGapOld = joinListOld.size() / 2;
		leftListNew.addAll(joinListNew.subList(0, halfGapNew));
		rightListNew.addAll(joinListNew.subList(halfGapNew, joinListNew.size()));
		leftListOld.addAll(joinListOld.subList(0, halfGapOld));
		rightListOld.addAll(joinListOld.subList(halfGapOld, joinListOld.size()));
		
		List<HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>> resultList = new ArrayList<>();
		for (int i = 0; i < halfGapNew; i++) {
			TLWGuildJoinInfo join1 = leftListNew.get(i);
			TLWGuildJoinInfo join2 = rightListNew.get(i);
			resultList.add(new HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>(join1, join2));
		}
		for (int i = 0; i < halfGapOld; i++) {
			TLWGuildJoinInfo join1 = leftListOld.get(i);
			TLWGuildJoinInfo join2 = rightListOld.get(i);
			resultList.add(new HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo>(join1, join2));
		}
		List<String> guildIds = new ArrayList<>();
		List<TWRoomData> roomList = new ArrayList<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple2<TLWGuildJoinInfo, TLWGuildJoinInfo> tuple : resultList) {
			TWRoomData roomData = new TWRoomData();
			String server1 = tuple.first.getServerId();
			String roomServer = server1;
			roomData.setGuildA(tuple.first.getId());
			roomData.setGuildB(tuple.second.getId());
			roomData.setTimeIndex(0);
			roomData.setId(HawkOSOperator.randomUUID());
			roomData.setRoomServerId(roomServer);
			roomData.setGroup(groupType);
			roomData.setBattleType(TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE);
			int serverType = 0;
			if(tuple.first.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				serverType = TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE;
			}else {
				serverType = TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE;
			}
			roomData.setServerType(serverType);
			roomList.add(roomData);
			guildIds.add(roomData.getGuildA());
			guildIds.add(roomData.getGuildB());

			LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, tuple.first.getId(), tuple.first.getServerId(), tuple.second.getId(),
					tuple.second.getServerId(), timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE.getValue(),serverType);

			HawkLog.logPrintln("TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
					roomData.getId(), roomData.getRoomServerId(), season, termId, tuple.first.getId(), tuple.first.getServerId(), tuple.second.getId(), tuple.second.getServerId());
		}
		RedisProxy.getInstance().updateTWRoomData(roomList, mark);
		TLWEliminationGroup groupInfo = new TLWEliminationGroup();
		groupInfo.setSeason(season);
		groupInfo.setTermId(termId);
		groupInfo.setGuildIds(guildIds);
		groupInfo.setWinGuildGroup(guildIds);
		groupInfo.setGroupType(groupType);
		Map<Integer, List<TLWBattleData>> warMap = new HashMap<>();
		List<TLWBattleData> battleList = new ArrayList<>();
		for (int i = 0; i < roomList.size(); i++) {
			TWRoomData roomData = roomList.get(i);
			TLWBattleData battleData = new TLWBattleData();
			battleData.setServerType(roomData.getServerType());
			battleData.setTermId(termId);
			battleData.setRoomId(roomData.getId());
			battleData.setPosIndex(i+1);
			battleData.setGuildA(roomData.getGuildA());
			battleData.setGuildB(roomData.getGuildB());
			battleData.setBattleType(TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE);
			battleList.add(battleData);
		}
		warMap.put(termId, battleList);
		groupInfo.setBattleMap(warMap);
		RedisProxy.getInstance().updateTLWEliminationGroupInfo(season, groupInfo);
		RedisProxy.getInstance().updateTLWJoinGuild(joinList, season);
	}
		

	
	public TLWGroupType calcEliminationGroupType(int rankGroup,int rank){
		if(rankGroup>=1 && rankGroup<=4){
			return TLWGroupType.S_GROUP;
		}
		if(rankGroup>=5 && rankGroup<=8){
			return TLWGroupType.A_GROUP;
		}


//		if(rankGroup>=TiberiumConst.S_RANGE.first && rankGroup<=TiberiumConst.S_RANGE.second){
//			return TLWGroupType.S_GROUP;
//		}
//		if(rankGroup>=TiberiumConst.A_RANGE.first && rankGroup<=TiberiumConst.A_RANGE.second){
//			return TLWGroupType.A_GROUP;
//		}
//		if(rankGroup>=TiberiumConst.B_RANGE.first && rankGroup<=TiberiumConst.B_RANGE.second){
//			return TLWGroupType.B_GROUP;
//		}
//
//		if(rankGroup == TiberiumConst.S_RANGE_EXT.first &&
//				rank >=TiberiumConst.S_RANGE_EXT.second && rank<=TiberiumConst.S_RANGE_EXT.third){
//			return TLWGroupType.S_GROUP;
//		}
//		if(rankGroup == TiberiumConst.A_RANGE_EXT1.first &&
//				rank >=TiberiumConst.A_RANGE_EXT1.second && rank<=TiberiumConst.A_RANGE_EXT1.third){
//			return TLWGroupType.A_GROUP;
//		}
//		if(rankGroup == TiberiumConst.A_RANGE_EXT2.first &&
//				rank >=TiberiumConst.A_RANGE_EXT2.second && rank<=TiberiumConst.A_RANGE_EXT2.third){
//			return TLWGroupType.A_GROUP;
//		}
//		if(rankGroup == TiberiumConst.B_RANGE_EXT.first &&
//				rank >=TiberiumConst.B_RANGE_EXT.second && rank<=TiberiumConst.B_RANGE_EXT.third){
//			return TLWGroupType.B_GROUP;
//		}
		return null;
	}
	
	
  
	
	
	/**
	 * 决赛匹配
	 */
	private void eliminationMatchFinal(){
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		int mark = activityInfo.getMark();
		// 清空本期的房间信息
		RedisProxy.getInstance().removeTWRoomData(activityInfo.getMark());
		for (TLWGroupType groupType : TiberiumConst.FINAL_WAR_GROUPS) {
			TLWEliminationGroup groupInfo = RedisProxy.getInstance().getTLWEliminationGroupInfo(season, groupType);
			TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
			Map<Integer, List<TLWBattleData>> stageMap = groupInfo.getBattleMap();
			int oldTermId = termId - 1;
			int oldMark = combineSeasonTerm(season, oldTermId);
			//获取上期的对战胜负信息
			List<TWRoomData> oldRoomList = RedisProxy.getInstance().getAllTWRoomData(oldMark);
			Map<String, String> winnerMap = new HashMap<>();
			for (TWRoomData roomData : oldRoomList) {
				winnerMap.put(roomData.getId(), roomData.getWinnerId());
			}
			List<TLWBattleData> oldBattleList = stageMap.get(oldTermId);
			// 拉取参赛联盟信息
			Map<String, TLWGuildJoinInfo> joinMaps = RedisProxy.getInstance().getAllTLWJoinGuild(season);
			//设置对战结果
			for (TLWBattleData battleData : oldBattleList) {
				String winnerId = winnerMap.get(battleData.getRoomId());
				battleData.setWinnerGuild(winnerId);
				String lossGuild = battleData.getLossGuild();
				if(groupInfo.inWinGuildGroup(lossGuild)){
					//如果是胜者组的,从胜者组删除，加入到败者组
					groupInfo.removeFromWinGuildGroup(lossGuild);
					groupInfo.addToLossGuildGroup(lossGuild);
				}else if(groupInfo.inLossGuildGroup(lossGuild)){
					//如果是败者组，从败者组删除，被淘汰
					groupInfo.removeFromLossGuildGroup(lossGuild);
				}
			}
			//对战列表
			List<TWRoomData> roomList = new ArrayList<>();
			if(true){
				//决赛匹配。胜者组最后一位，败者组最后一位，火拼一下
				List<String> winList = groupInfo.getWinGuildGroup();
				List<String> lossList = groupInfo.getLossGuildGroup();
				String winGuild = winList.get(0);
				for(String guildId : winList){
					TLWGuildJoinInfo guildJoinInfo = joinMaps.get(guildId);
					if(guildJoinInfo.getServerType() == TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE){
						winGuild = guildId;
					}
				}
				String lossGuild = lossList.get(0);
				for(String guildId : lossList){
					TLWGuildJoinInfo guildJoinInfo = joinMaps.get(guildId);
					if(guildJoinInfo.getServerType() == TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE){
						lossGuild = guildId;
					}
				}
				TLWGuildJoinInfo guildA = joinMaps.get(winGuild);
				TLWGuildJoinInfo guildB = joinMaps.get(lossGuild);
				TWRoomData roomData = new TWRoomData();
				String serverA = guildA.getServerId();
				String serverB = guildB.getServerId();
				String roomServer = serverA;
				roomData.setGuildA(guildA.getId());
				roomData.setGuildB(guildB.getId());
				roomData.setTimeIndex(0);
				roomData.setId(HawkOSOperator.randomUUID());
				roomData.setRoomServerId(roomServer);
				roomData.setGroup(groupType);
				roomData.setBattleType(TLWBattleType.ELIMINATION_FINAL_GROUP_BATTLE);
				roomData.setServerType(TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);
				roomList.add(roomData);
				LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
						timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_FINAL_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);

				HawkLog.logPrintln(
						"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
						roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
			}

			if(true){
				//决赛匹配。胜者组最后一位，败者组最后一位，火拼一下
				List<String> winList = groupInfo.getWinGuildGroup();
				List<String> lossList = groupInfo.getLossGuildGroup();
				String winGuild = winList.get(0);
				for(String guildId : winList){
					TLWGuildJoinInfo guildJoinInfo = joinMaps.get(guildId);
					if(guildJoinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
						winGuild = guildId;
					}
				}
				String lossGuild = lossList.get(0);
				for(String guildId : lossList){
					TLWGuildJoinInfo guildJoinInfo = joinMaps.get(guildId);
					if(guildJoinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
						lossGuild = guildId;
					}
				}
				TLWGuildJoinInfo guildA = joinMaps.get(winGuild);
				TLWGuildJoinInfo guildB = joinMaps.get(lossGuild);
				TWRoomData roomData = new TWRoomData();
				String serverA = guildA.getServerId();
				String serverB = guildB.getServerId();
				String roomServer = serverA;
				roomData.setGuildA(guildA.getId());
				roomData.setGuildB(guildB.getId());
				roomData.setTimeIndex(0);
				roomData.setId(HawkOSOperator.randomUUID());
				roomData.setRoomServerId(roomServer);
				roomData.setGroup(groupType);
				roomData.setBattleType(TLWBattleType.ELIMINATION_FINAL_GROUP_BATTLE);
				roomData.setServerType(TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);
				roomList.add(roomData);
				LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
						timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_FINAL_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);

				HawkLog.logPrintln(
						"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
						roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
			}
			//更新对战信息
			RedisProxy.getInstance().updateTWRoomData(roomList, mark);
			List<TLWBattleData> battleList = new ArrayList<>();
			for (int i = 0; i < roomList.size(); i++) {
				TWRoomData roomData = roomList.get(i);
				TLWBattleData battleData = new TLWBattleData();
				battleData.setServerType(roomData.getServerType());
				battleData.setTermId(termId);
				battleData.setRoomId(roomData.getId());
				battleData.setPosIndex(i+1);
				battleData.setGuildA(roomData.getGuildA());
				battleData.setGuildB(roomData.getGuildB());
				battleData.setBattleType(roomData.getBattleType());
				battleList.add(battleData);
			}
			stageMap.put(termId, battleList);
			RedisProxy.getInstance().updateTLWEliminationGroupInfo(season, groupInfo);
		}
	}
	
	/**
	 * 决赛数据处理
	 * @param season
	 * @param termId
	 * @param mark
	 * @param stage
	 * @param groupInfo
	 */
	private void eliminationMatchCalc() {
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		int mark = activityInfo.getMark();
		// 清空本期的房间信息
		RedisProxy.getInstance().removeTWRoomData(activityInfo.getMark());
		for (TLWGroupType groupType : TiberiumConst.FINAL_WAR_GROUPS) {
			TLWEliminationGroup groupInfo = RedisProxy.getInstance().getTLWEliminationGroupInfo(season, groupType);
			TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
			Map<Integer, List<TLWBattleData>> stageMap = groupInfo.getBattleMap();
			int oldTermId = termId - 1;
			int oldMark = combineSeasonTerm(season, oldTermId);
			//获取上期的对战胜负信息
			List<TWRoomData> oldRoomList = RedisProxy.getInstance().getAllTWRoomData(oldMark);
			Map<String, String> winnerMap = new HashMap<>();
			for (TWRoomData roomData : oldRoomList) {
				winnerMap.put(roomData.getId(), roomData.getWinnerId());
			}
			List<TLWBattleData> oldBattleList = stageMap.get(oldTermId);
			// 拉取参赛联盟信息
			Map<String, TLWGuildJoinInfo> joinMaps = RedisProxy.getInstance().getAllTLWJoinGuild(season);
			//设置对战结果
			for (TLWBattleData battleData : oldBattleList) {
				String winnerId = winnerMap.get(battleData.getRoomId());
				battleData.setWinnerGuild(winnerId);
				String lossGuild = battleData.getLossGuild();
				if(groupInfo.inWinGuildGroup(lossGuild)){
					//如果是胜者组的,从胜者组删除，加入到败者组
					groupInfo.removeFromWinGuildGroup(lossGuild);
					groupInfo.addToLossGuildGroup(lossGuild);
				}else if(groupInfo.inLossGuildGroup(lossGuild)){
					//如果是败者组，从败者组删除，被淘汰
					groupInfo.removeFromLossGuildGroup(lossGuild);
				}
			}
			//对战列表
			List<TWRoomData> roomList = new ArrayList<>();
			//胜者组匹配对战
			if(!timeCfg.winGroupFree()){
				List<TLWGuildJoinInfo> leftList = new ArrayList<>();
				List<TLWGuildJoinInfo> rightList = new ArrayList<>();
				List<String> winList = groupInfo.getWinGuildGroup();
				List<String> tmp = new ArrayList<>();
				for(String winGuild : winList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(winGuild);
					if(guildInfo.getServerType() == TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE){
						tmp.add(winGuild);
					}
				}
				winList = tmp;
				HawkRand.randomOrder(winList);
				int halfGap = winList.size() / 2;
				for(String winGuild : winList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(winGuild);
					if(leftList.size() < halfGap){
						leftList.add(guildInfo);
					}else{
						rightList.add(guildInfo);
					}
				}
				for (int i = 0; i < halfGap; i++) {
					TLWGuildJoinInfo guildA = leftList.get(i);
					TLWGuildJoinInfo guildB = rightList.get(i);
					TWRoomData roomData = new TWRoomData();
					String serverA = guildA.getServerId();
					String serverB = guildB.getServerId();
					String roomServer = serverA;
					roomData.setGuildA(guildA.getId());
					roomData.setGuildB(guildB.getId());
					roomData.setTimeIndex(0);
					roomData.setId(HawkOSOperator.randomUUID());
					roomData.setRoomServerId(roomServer);
					roomData.setGroup(groupType);
					roomData.setBattleType(TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE);
					roomData.setServerType(TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);
					roomList.add(roomData);
					LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
							timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);

					HawkLog.logPrintln(
							"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
							roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
				}
			}
			if(!timeCfg.winGroupFree()){
				List<TLWGuildJoinInfo> leftList = new ArrayList<>();
				List<TLWGuildJoinInfo> rightList = new ArrayList<>();
				List<String> winList = groupInfo.getWinGuildGroup();
				List<String> tmp = new ArrayList<>();
				for(String winGuild : winList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(winGuild);
					if(guildInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
						tmp.add(winGuild);
					}
				}
				winList = tmp;
				HawkRand.randomOrder(winList);
				int halfGap = winList.size() / 2;
				for(String winGuild : winList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(winGuild);
					if(leftList.size() < halfGap){
						leftList.add(guildInfo);
					}else{
						rightList.add(guildInfo);
					}
				}
				for (int i = 0; i < halfGap; i++) {
					TLWGuildJoinInfo guildA = leftList.get(i);
					TLWGuildJoinInfo guildB = rightList.get(i);
					TWRoomData roomData = new TWRoomData();
					String serverA = guildA.getServerId();
					String serverB = guildB.getServerId();
					String roomServer = serverA;
					roomData.setGuildA(guildA.getId());
					roomData.setGuildB(guildB.getId());
					roomData.setTimeIndex(0);
					roomData.setId(HawkOSOperator.randomUUID());
					roomData.setRoomServerId(roomServer);
					roomData.setGroup(groupType);
					roomData.setBattleType(TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE);
					roomData.setServerType(TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);
					roomList.add(roomData);
					LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
							timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_WIN_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);

					HawkLog.logPrintln(
							"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
							roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
				}
			}
			//败者组匹配对战
			if(true){
				List<TLWGuildJoinInfo> leftList = new ArrayList<>();
				List<TLWGuildJoinInfo> rightList = new ArrayList<>();
				List<String> lossList = groupInfo.getLossGuildGroup();
				List<String> tmp = new ArrayList<>();
				for(String  lossGuild : lossList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(lossGuild);
					if(guildInfo.getServerType() == TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE){
						tmp.add(lossGuild);
					}
				}
				lossList = tmp;
				HawkRand.randomOrder(lossList);
				int halfGap = lossList.size() / 2;
				for(String lossGuild : lossList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(lossGuild);
					if(leftList.size() < halfGap){
						leftList.add(guildInfo);
					}else{
						rightList.add(guildInfo);
					}
				}
				for (int i = 0; i < halfGap; i++) {
					TLWGuildJoinInfo guildA = leftList.get(i);
					TLWGuildJoinInfo guildB = rightList.get(i);
					TWRoomData roomData = new TWRoomData();
					String serverA = guildA.getServerId();
					String serverB = guildB.getServerId();
					String roomServer = serverA;
					roomData.setGuildA(guildA.getId());
					roomData.setGuildB(guildB.getId());
					roomData.setTimeIndex(0);
					roomData.setId(HawkOSOperator.randomUUID());
					roomData.setRoomServerId(roomServer);
					roomData.setGroup(groupType);
					roomData.setBattleType(TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE);
					roomData.setServerType(TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);
					roomList.add(roomData);
					LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
							timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE);
					HawkLog.logPrintln(
							"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
							roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
				}
			}
			//败者组匹配对战
			if(true){
				List<TLWGuildJoinInfo> leftList = new ArrayList<>();
				List<TLWGuildJoinInfo> rightList = new ArrayList<>();
				List<String> lossList = groupInfo.getLossGuildGroup();
				List<String> tmp = new ArrayList<>();
				for(String  lossGuild : lossList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(lossGuild);
					if(guildInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
						tmp.add(lossGuild);
					}
				}
				lossList = tmp;
				HawkRand.randomOrder(lossList);
				int halfGap = lossList.size() / 2;
				for(String lossGuild : lossList){
					TLWGuildJoinInfo guildInfo = joinMaps.get(lossGuild);
					if(leftList.size() < halfGap){
						leftList.add(guildInfo);
					}else{
						rightList.add(guildInfo);
					}
				}
				for (int i = 0; i < halfGap; i++) {
					TLWGuildJoinInfo guildA = leftList.get(i);
					TLWGuildJoinInfo guildB = rightList.get(i);
					TWRoomData roomData = new TWRoomData();
					String serverA = guildA.getServerId();
					String serverB = guildB.getServerId();
					String roomServer = serverA;
					roomData.setGuildA(guildA.getId());
					roomData.setGuildB(guildB.getId());
					roomData.setTimeIndex(0);
					roomData.setId(HawkOSOperator.randomUUID());
					roomData.setRoomServerId(roomServer);
					roomData.setGroup(groupType);
					roomData.setBattleType(TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE);
					roomData.setServerType(TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);
					roomList.add(roomData);
					LogUtil.logTimberiumLeaguaMatchInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB,
							timeCfg.getWarStartTimeValue(),TLWBattleType.ELIMINATION_LOSS_GROUP_BATTLE.getValue(),TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE);
					HawkLog.logPrintln(
							"TiberiumLeaguaWarService do finalMatchInit, roomId: {}, roomServer: {}, season: {}, termId: {}, guildA: {}, serverA: {}, guildB: {}, serverB: {} ",
							roomData.getId(), roomData.getRoomServerId(), season, termId, roomData.getGuildA(), serverA, roomData.getGuildB(), serverB);
				}
			}
			RedisProxy.getInstance().updateTWRoomData(roomList, mark);
			List<TLWBattleData> battleList = new ArrayList<>();
			for (int i = 0; i < roomList.size(); i++) {
				TWRoomData roomData = roomList.get(i);
				TLWBattleData battleData = new TLWBattleData();
				battleData.setServerType(roomData.getServerType());
				battleData.setTermId(termId);
				battleData.setRoomId(roomData.getId());
				battleData.setPosIndex(i+1);
				battleData.setGuildA(roomData.getGuildA());
				battleData.setGuildB(roomData.getGuildB());
				battleData.setBattleType(roomData.getBattleType());
				battleList.add(battleData);
			}
			stageMap.put(termId, battleList);
			groupInfo.setBattleMap(stageMap);
			RedisProxy.getInstance().updateTLWEliminationGroupInfo(season, groupInfo);
		}
	}
	


	/**
	 * 筛选参赛联盟
	 */
	private Map<Integer, List<TLWGuildJoinInfo>> pickJoinGuild(TiberiumWar.TLWServer serverType) {
		int season = activityInfo.getSeason();
		HawkLog.logPrintln("TiberiumLeagueWarService start pickJoinGuild! season: {}", season);
		//RedisProxy.getInstance().removeTLWJoinGuild(season);
		// 拉取联盟排行榜数据
		Set<Tuple> tuples = null;
		if(serverType == TiberiumWar.TLWServer.TLW_NEW_SERVER){
			tuples = RedisProxy.getInstance().getTLWGuildPowerRanksNew(season, 0, TiberiumConstCfg.getInstance().getGuildPickCnt() - 1);
		}else {
			tuples = RedisProxy.getInstance().getTLWGuildPowerRanks(season, 0, TiberiumConstCfg.getInstance().getGuildPickCnt() - 1);
		}
		List<String> guildIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, TLWGuildData> guildDatas = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		List<TLWGuildJoinInfo> joinList = new ArrayList<>();
		int rank = 1;
		// 联盟分组
		Map<Integer, List<TLWGuildJoinInfo>> teamMap = new HashMap<>();
		List<TLWGuildJoinInfo> normalList = new ArrayList<>();
		for (Tuple tuple : tuples) {
			String guildId = tuple.getElement();
			long initPower = Math.round(tuple.getScore());
			TLWGuildData guildData = guildDatas.get(guildId);
			if(serverType == TiberiumWar.TLWServer.TLW_OLD_SERVER && guildData.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				RedisProxy.getInstance().removeTLWGuildPowerRankNew(season, guildId);
				HawkLog.logPrintln("TiberiumLeagueWarService pickJoinGuild pick newToOldguild, season: {}, guildId: {}, guildName: {}, guildServer: {}, initPower: {}, serverType: {}", season, guildId,
						guildData.getName(), guildData.getServerId(), initPower, serverType.getNumber());
			}
			TLWGuildJoinInfo info = new TLWGuildJoinInfo();
			info.setGroup(TLWGroupType.TEAM_GROUP);
			info.setInitGroup(TLWGroupType.TEAM_GROUP);
			info.setServerId(guildData.getServerId());
			info.setId(guildId);
			info.setCreateTime(guildData.getCreateTime());
			// 种子选手,创建小组
			if (rank <= TiberiumConstCfg.getInstance().getTeamCnt()) {
				List<TLWGuildJoinInfo> teamList = new ArrayList<>();
				info.setSeed(true);
				info.setTeamId(rank);
				teamList.add(info);
				teamMap.put(rank, teamList);
			} else {
				normalList.add(info);
			}
			info.setScore(0);
			info.setWinCnt(0);
			info.setLoseCnt(0);
			info.setInitPower(initPower);
			info.setInitPowerRank(rank);
			info.setServerType(serverType.getNumber());
			joinList.add(info);
			HawkLog.logPrintln("TiberiumLeagueWarService pickJoinGuild pick guild, season: {}, guildId: {}, guildName: {}, guildServer: {}, initPower: {}, serverType: {}", season, guildId,
					guildData.getName(), guildData.getServerId(), initPower, serverType.getNumber());
			rank++;
		}
		Collections.shuffle(normalList);

		// 填充剩余小组成员
		for (int i = 0; i < normalList.size(); i++) {
			TLWGuildJoinInfo joinInfo = normalList.get(i);
			int teamId = i / (TiberiumConst.TEAM_MEMBER_CNT - 1) + 1;
			List<TLWGuildJoinInfo> teamList = teamMap.get(teamId);
			joinInfo.setTeamId(teamId);
			teamList.add(joinInfo);
		}

		// 小组列表进行排序
		for (int i = 1; i <= TiberiumConstCfg.getInstance().getTeamCnt(); i++) {
			List<TLWGuildJoinInfo> teamList = teamMap.get(i);
			Collections.sort(teamList, new TLWGuildInitPowerRankComparator());
		}
		RedisProxy.getInstance().updateTLWJoinGuild(joinList, season);
		HawkLog.logPrintln("TiberiumLeagueWarService pickJoinGuild zone finish , season: {}, zoneId :{}", season);
		return teamMap;
	}

	/**
	 * 出战管理阶段结束时刷新记录出战联盟数据
	 * @return
	 */
	private boolean flushGuildWarInfo() {
		String serverId = GsConfig.getInstance().getServerId();
		int season = activityInfo.season;
		int termId = activityInfo.termId;
		int mark = activityInfo.getMark();
		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
		Map<String, String> roomMapping = new HashMap<>();
		for(TWRoomData roomData : roomList){
			roomMapping.put(roomData.getGuildA(), roomData.getId());
			roomMapping.put(roomData.getGuildB(), roomData.getId());
		}
		try {
			Map<String, TLWGroupType> joinMap = currGuildGroup;
			for (Entry<String, TLWGroupType> entry : joinMap.entrySet()) {
				String guildId = entry.getKey();
				TLWGroupType group = entry.getValue();
				// 被淘汰的联盟不参与
				if(group == TLWGroupType.NORMAL || group == TLWGroupType.KICK_OUT){
					continue;
				}
				
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guild == null) {
					continue;
				}
				Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
				int memberCnt = 0;
				long totalPower = 0;
				for (String playerId : idList) {
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					// 非本盟玩家
					if (member == null || !guildId.equals(member.getGuildId())) {
						continue;
					}
					memberCnt++;
					totalPower += member.getPower();
					TWPlayerData playerData = new TWPlayerData();
					playerData.setId(playerId);
					playerData.setGuildAuth(member.getAuthority());
					playerData.setGuildId(guildId);
					playerData.setGuildOfficer(member.getOfficeId());
					playerData.setServerId(serverId);
					Player mplayer = GlobalData.getInstance().makesurePlayer(playerId);
					if (mplayer != null) {
						playerData.setCityLevel(mplayer.getCityLevel());
						playerData.setName(mplayer.getName());
						playerData.setIcon(mplayer.getIcon());
						playerData.setPfIcon(mplayer.getPfIcon());
					}
					RedisProxy.getInstance().updateTWPlayerData(playerData, mark);
					HawkLog.logPrintln("TiberiumLeaguaWarService flushSignerPlayerInfo, playerId:{}, guildId: {}, serverId: {}, selfPowar: {}, season: {}, termId: {}, group: {}",
							playerId, guildId, serverId, member.getPower(), season, termId, group.getNumber());
					LogUtil.logTimberiumLeaguaPlayerWarInfo(playerId, guildId, serverId, member.getPower(), season, termId, group.getNumber());
				}
				
				// ELO积分数据
				TWGuildEloData eloData = RedisProxy.getInstance().getTWGuildElo(guildId);
				if (eloData == null) {
					eloData = new TWGuildEloData();
					eloData.setId(guildId);
					eloData.setServerId(serverId);
					int initScore = TiberiumConstCfg.getInstance().getInitEloScore();
					eloData.setScore(initScore);
					GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
							.setMailId(MailId.TLW_INIT_ELO_SCORE)
							.addContents(initScore));
					TWEloScoreLogUnit eloScoreUnit = new TWEloScoreLogUnit(guildId, activityInfo.getTermId(), 0, initScore, initScore, EloReason.INIT);
					TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnit);
					
				}
				eloData.setLastActiveTerm(TiberiumWarService.getInstance().getProximateTermId());
				String roomId = roomMapping.get(guildId);
				TWGuildData guildData = new TWGuildData();
	
				guildData.setId(guildId);
				guildData.setServerId(serverId);
				guildData.setName(guild.getName());
				guildData.setTag(guild.getTag());
				guildData.setFlag(guild.getFlagId());
				guildData.setMemberCnt(memberCnt);
				guildData.setTotalPower(totalPower);
				guildData.setRoomId(roomId);
				guildData.setEloScore(eloData.getScore());
				guildData.setGuildPower(GuildService.getInstance().getGuildBattlePoint(guildId));
				RedisProxy.getInstance().updateTWGuildData(guildData, mark);
				RedisProxy.getInstance().updateTWGuildElo(eloData);
				TLWGuildJoinInfo joinInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
				joinInfo.setLastestPower(totalPower);
				RedisProxy.getInstance().updateTLWJoinGuild(joinInfo, season);
				try {
					HawkLog.logPrintln("TiberiumLeaguaWarService flushSignerGuildInfo, guildId: {}, guildName: {} , serverId: {}, memberCnt: {}, totalPowar: {}, season: {}, termId: {}", guildId, guild.getName(), serverId, memberCnt, totalPower, termId, entry.getValue());
					LogUtil.logTimberiumLeaguaGuildWarInfo(guildId, guild.getName(), serverId, memberCnt, totalPower, season, termId, group.getNumber(), joinInfo.getServerType());
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
	
			return true;
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
	}
	
	/**
	 * 获取当前轮次标识
	 * @param season
	 * @param termId
	 * @return
	 */
	public int combineSeasonTerm(int season, int termId) {
		return season * TiberiumConst.SEASON_OFFSET + termId;
	}
	
	/**
	 * 根据标识获取赛季&期数
	 * @param mark
	 * @return
	 */
	public HawkTuple2<Integer, Integer> splitTWLMark(int mark) {
		int season = mark / TiberiumConst.SEASON_OFFSET;
		int termId = mark % TiberiumConst.SEASON_OFFSET;
		return new HawkTuple2<Integer, Integer>(season, termId);
	}
	
	/**
	 * 战斗阶段轮询
	 */
	protected void fightTick() {
		if (activityInfo.state != TLWActivityState.TLW_WAR_OPEN) {
			return;
		}
		if (!fightState.isHasInit()) {
			fightState = RedisProxy.getInstance().getTLWFightInfo();
			if (fightState == null) {
				fightState = new TLWFightState();
				RedisProxy.getInstance().updateTLWFightInfo(fightState);
			}
			fightState.setHasInit(true);
		}
		if (fightState.getTermId() != activityInfo.termId) {
			fightState = new TLWFightState();
			fightState.setTermId(activityInfo.termId);
			fightState.setHasInit(true);
			RedisProxy.getInstance().updateTLWFightInfo(fightState);
		}
		long curTime = HawkTime.getMillisecond();

		boolean needUpdate = false;
		
		TiberiumSeasonTimeCfg timeCfg = calcTimeCfg();
		if(timeCfg == null){
			return;
		}
		
		long startTime = timeCfg.getWarStartTimeValue();
		long endTime = startTime + TiberiumConstCfg.getInstance().getWarOpenTime();
		long awardTime = endTime + TiberiumConstCfg.getInstance().getAwardDelayTime();
		FightState state;
		if (curTime < startTime) {
			state = FightState.NOT_OPEN;
		} else if (curTime >= startTime && curTime < endTime) {
			state = FightState.OPEN;
		} else if (curTime >= endTime && curTime < awardTime) {
			state = FightState.FINISH;
		} else {
			state = FightState.AWARDED;
		}
		boolean stageChange = false;
		while (fightState.getState() != state) {
			if (fightState.getState() == FightState.NOT_OPEN) {
				fightState.setState(FightState.OPEN);
				HawkLog.logPrintln("TiberiumLeagueWarService onFightOpen! ");
				createBattle();
			} else if (fightState.getState() == FightState.OPEN) {
				fightState.setState(FightState.FINISH);
			} else if (fightState.getState() == FightState.FINISH) {
				loadMatchedRoom();
				loadJoinGuildsInfo();
				sendAward();
				updateCurrMatchedRoom();
				// 决赛阶段结束,记录所有参与正赛的联盟当前联盟成员的id
				if(activityInfo.termId == TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
					recordJoinGuildMembers();
				}
				fightState.setState(FightState.AWARDED);
			} else {
				fightState.setState(FightState.NOT_OPEN);
			}
			needUpdate = true;
			stageChange = true;
		}

		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		int mark = combineSeasonTerm(season, termId);
		// 阶段变更为开启,则需要推送跑马灯
		if (fightState.state == FightState.OPEN && stageChange) {
			List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
			List<String> guildIds = new ArrayList<>();
			for (TWRoomData roomData : roomList) {
				guildIds.add(roomData.getGuildA());
				guildIds.add(roomData.getGuildB());
			}
			Set<Player> onlineMembers = new HashSet<>();
			for (String guildId : guildIds) {
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj != null) {
					Set<String> memberIds = RedisProxy.getInstance().getTWPlayerIds(guildId);
					if (memberIds == null || memberIds.isEmpty()) {
						continue;
					}
					for (String memberId : memberIds) {
						Player member = GlobalData.getInstance().getActivePlayer(memberId);
						if (member != null) {
							onlineMembers.add(member);
						}
					}
				}
			}
			List<ChatMsg> msgList = new ArrayList<>();
			ChatMsg msg = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.TBLY_123).build().toPBMsg();
			msgList.add(msg);
			ChatService.getInstance().sendChatMsg(msgList, onlineMembers);
		}

		if (needUpdate) {
			RedisProxy.getInstance().updateTLWFightInfo(fightState);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
		}

	}
	
	/**
	 * 存储泰伯利亚联赛入围正赛的联盟成员列表
	 */
	private void recordJoinGuildMembers() {
		// 合服Map<主服,从服> 
		Map<String, String> mergerMap = new HashMap<>();
		
		ConfigIterator<MergeServerTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		long currTime = HawkTime.getMillisecond();
		for (MergeServerTimeCfg cfg : its) {
			// 还未合服以及2020/3/24之前合服的服务器不作处理
			if (cfg.getMergeTimeValue() > currTime || cfg.getMergeTimeValue() <= 1584979200000l) {
				continue;
			}
			List<String> mergeServerList = cfg.getMergeServerList();
			if (mergeServerList == null) {
				HawkLog.logPrintln("recordJoinGuildMembers mergeInfoNull, id:{}", cfg.getId());
				continue;
			} else if (mergeServerList.size() == 2) {
				mergerMap.put(mergeServerList.get(0), mergeServerList.get(1));
			} else if (mergeServerList.size() == 4) {
				mergerMap.put(mergeServerList.get(0), mergeServerList.get(2));
			} else {
				HawkLog.logPrintln("recordJoinGuildMembers mergeInfoError, id:{}", cfg.getId());
				continue;
			}
		}
		int season = activityInfo.getSeason();
		Map<String, TLWGuildJoinInfo> guildMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		String serverId = GsConfig.getInstance().getServerId();
		String followServer = serverId;
		if(mergerMap.containsKey(serverId)){
			followServer = mergerMap.get(serverId);
		}
		for (Entry<String, TLWGuildJoinInfo> entry : guildMap.entrySet()) {
			String guildId = entry.getKey();
			TLWGuildJoinInfo joinGuild = entry.getValue();
			// 非本服或合并入的从服的联盟,不做处理
			if (!joinGuild.getServerId().equals(serverId) && !joinGuild.getServerId().equals(followServer)) {
				continue;
			}
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			if (memberIds.isEmpty()) {
				HawkLog.logPrintln("recordJoinGuildMembersFailed ,member is empty, serverId: {}, guildId: {}", serverId, guildId);
				continue;
			}
			Map<String, String> dataMap = new HashMap<>();
			for (String memberId : memberIds) {
				dataMap.put(memberId, String.valueOf(false));
			}
			RedisProxy.getInstance().updateTLWMemberIds(guildId, dataMap, season);
			HawkLog.logPrintln("TiberiumLeaguaWarService recordJoinGuildMembers, guildId: {}, memberIds: {}", guildId, memberIds);
		}
		
	}
	
	/**
	 * 赛季结束结算积分
	 */
	private void calcEloScoreOnSeasonEnd() {
		int season = activityInfo.getSeason();
		Map<String, TWGuildEloData> guildMap = RedisProxy.getInstance().getAllEloData();
		String serverId = GsConfig.getInstance().getServerId();
		for (Entry<String, TWGuildEloData> entry : guildMap.entrySet()) {
			String guildId = entry.getKey();
			TWGuildEloData eloData = entry.getValue();
			if (!eloData.getServerId().equals(serverId)) {
				continue;
			}
			GuildInfoObject guildInfo = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildInfo == null) {
				continue;
			}
			if (eloData.getCalcedSeason() >= season) {
				continue;
			}
			int oldScore = eloData.getScore();
			int initScore = TiberiumConstCfg.getInstance().getInitEloScore();
			int calcPer = TiberiumConstCfg.getInstance().getEloSeasonCalcPer();
			int scoreGap = oldScore - initScore;
			int leftScore = (int) Math.floor(scoreGap * GsConst.EFF_PER * calcPer);
			int newScore = initScore + leftScore;
			eloData.setScore(newScore);
			eloData.setCalcedSeason(season);
			RedisProxy.getInstance().updateTWGuildElo(eloData);
			GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
					.setMailId(MailId.TLW_ELO_SCORE_SEASON_CALC)
					.addContents(oldScore, scoreGap, calcPer, newScore));
			
			TWEloScoreLogUnit eloScoreUnit = new TWEloScoreLogUnit(guildId, activityInfo.getTermId(), oldScore, newScore, newScore - oldScore, EloReason.SEASON_END_CALC);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnit);
			HawkLog.logPrintln("calcEloScoreOnSeasonEnd success, guildId: {}, oldScore: {}, newScore: {}, season: {}", guildId, oldScore, newScore, season);
		}
	}

	//发奖
	private void sendAward() {
		int mark = activityInfo.getMark();
		int termId = activityInfo.getTermId();
		//本轮结果事件推送给Activity模块
		Set<String> winGuilds = new HashSet<>();
		int season = activityInfo.getSeason();
		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
		List<String> guildIds = new ArrayList<>();
		for (TWRoomData roomData : roomList) {
			guildIds.add(roomData.getGuildA());
			guildIds.add(roomData.getGuildB());
		};
		if(guildIds == null || guildIds.isEmpty()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		for (String guildId : guildIds) {
			TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, mark);
			if (guildData == null) {
				continue;
			}
			TWRoomData roomData = RedisProxy.getInstance().getTWRoomData(guildData.getRoomId(), mark);
			//胜利的联盟
			if (guildData.isWin()) {
				winGuilds.add(guildId);
			}
			if (!serverId.equals(guildData.getServerId()) || guildData.matchFailed || guildData.isAwarded()) {
				continue;
			}
			boolean isWin = guildData.isWin();
			boolean isComplete = guildData.isComplete();
			// 战场异常中断导致未正常结算,记为胜利
			if (!isComplete) {
				isWin = true;
			}
			MailId guildMailId = isWin ? MailId.TBLY_GUILD_WIN_MAIL : MailId.TBLY_GUILD_LOSE_MAIL;
			TiberiumGuildAwardCfg guildCfg = TiberiumWarService.getInstance().getGuildAwardCfg(isWin);
			TLWScoreData guildScoreData = RedisProxy.getInstance().getTLWGuildScoreInfo(season, guildId);
			calcGuildAddReward(guildScoreData);
			Set<Integer> guildRewards = guildScoreData.getRewardedList();
			// 联盟奖励
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			List<TWSelfRewardLogUnit> selfRewardLogUnitList = new ArrayList<>();
			List<TWPlayerSeasonScoreLogUnit> pSeasonScorgLogUnitList = new ArrayList<>();
			for (String memberId : memberIds) {
				boolean needUpdate = false;
				TLWScoreData selfScoreData = RedisProxy.getInstance().getTLWPlayerScoreInfo(season, memberId);
				Set<Integer> rewardeds = selfScoreData.getRewardedList();
				Set<Integer> guildRewardeds = selfScoreData.getGuildRewardeds();
				List<Integer> gNeedRewards = new ArrayList<>();
				for(int grId : guildRewards){
					if(!guildRewardeds.contains(grId)){
						gNeedRewards.add(grId);
					}
				}
				if(!gNeedRewards.isEmpty()){
					for(int gnId : gNeedRewards){
						TiberiumSeasonGuildAwardCfg grCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumSeasonGuildAwardCfg.class, gnId);
						if(grCfg == null){
							HawkLog.logPrintln("TiberiumLeaguaWarService send guildSeasonReward err,cfg is null, playerId: {}, guildId: {}, score: {}, cfgId: {}", memberId, guildId,
									guildScoreData.getScore(), gnId);
							continue;
						}
						// 赛季联盟积分奖励
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(memberId)
								.setMailId(MailId.TIBERIUM_SEASON_GUILD_SCORE_REWARD)
								.addContents(grCfg.getScore())
								.setAwardStatus(MailRewardStatus.NOT_GET)
								.addRewards(grCfg.getRewardItem())
								.build());
						guildRewardeds.add(gnId);
						LogUtil.logTimberiumLeaguaGuildReward(memberId, guildId, serverId, guildScoreData.getScore(), selfScoreData.getScore(), season, termId, gnId, true);
						HawkLog.logPrintln(
								"TiberiumLeagueWarService sendSeasonGuildScoreReward, memberId: {}, guildId: {}, serverId: {}, guildScore: {}, selfScore: {}, season: {}, termId: {}, gnId: {}",
								memberId, guildId, serverId, guildScoreData.getScore(), selfScoreData.getScore(), season, termId, gnId);
					}
					needUpdate = true;
				}
				Player player = GlobalData.getInstance().makesurePlayer(memberId);
				if(player == null){
					continue;
				}
				// 记录个人积分信息
				pSeasonScorgLogUnitList.add(new TWPlayerSeasonScoreLogUnit(memberId,  guildData.getRoomId(), roomData.getRoomServerId(), guildId, guildData.getName(), selfScoreData.getScore(), isWin));
				List<Integer> pNeedReward = calcSelfAddReward(selfScoreData);
				if(!pNeedReward.isEmpty()){
					for(int pnId : pNeedReward){
						TiberiumSeasonPersonAwardCfg prCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumSeasonPersonAwardCfg.class, pnId);
						if (prCfg == null) {
							HawkLog.logPrintln("TiberiumLeaguaWarService send selfSeasonReward err,cfg is null, playerId: {}, guildId: {}, score: {}, cfgId: {}", memberId, guildId,
									selfScoreData.getScore(), pnId);
							continue;
						}
						//赛季个人积分奖励
						SystemMailService.getInstance().sendMail(MailParames.newBuilder()
								.setPlayerId(memberId)
								.setMailId(MailId.TIBERIUM_SEASON_PERSON_SCORE_REWARD)
								.addContents(prCfg.getScore())
								.setAwardStatus(MailRewardStatus.NOT_GET)
								.addRewards(prCfg.getRewardItem())
								.build());
						rewardeds.add(pnId);
						selfRewardLogUnitList.add(new TWSelfRewardLogUnit(memberId, guildId, serverId, selfScoreData.getScore(), season, termId, pnId, true)); 
						HawkLog.logPrintln(
								"TiberiumLeagueWarService sendSeasonSelfScoreReward, memberId: {}, guildId: {}, serverId: {}, selfScore: {}, season: {}, termId: {}, gnId: {}",
								memberId, guildId, serverId, selfScoreData.getScore(), season, termId, pnId);
					}
					needUpdate = true;
				}
				
				if(needUpdate){
					RedisProxy.getInstance().updateTLWPlayerScoreInfo(season, selfScoreData);
				}
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(memberId)
						.setMailId(guildMailId)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addRewards(guildCfg.getRewardItem())
						.build());
			}
			RedisProxy.getInstance().updateTLWGuildScoreInfo(season, guildScoreData);
			guildData.setAwarded(true);
			HawkLog.logPrintln("TiberiumWarService send guild reward, guildId: {}, guildName: {} ,guildTag: {}, score: {}, cfgId: {}, isComplete: {}", guildId, guildData.getName(),
					guildData.getTag(), guildData.getScore(), guildCfg.getId(), isComplete);
			RedisProxy.getInstance().updateTWGuildData(guildData, mark);
			// 参赛个人奖励
			Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
			HawkLog.logPrintln("TiberiumLeaguaWarService start send self reward, guildId: {}, guildName: {} ,guildTag: {}, joinIds: {}", guildId, guildData.getName(),
					guildData.getTag(), idList);
			MailId selfMailId = isWin ? MailId.TBLY_SELF_WIN_MAIL : MailId.TBLY_SELF_LOSE_MAIL;
			for (String playerId : idList) {
				TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(playerId, mark);
				if (twPlayerData == null) {
					HawkLog.logPrintln("TiberiumLeaguaWarService ignore self reward, twPlayerData is null, playerId: {}", playerId);
					continue;
				}
				if (twPlayerData.isAwarded()) {
					HawkLog.logPrintln("TiberiumLeaguaWarService ignore self reward,already awarded, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}, isAwarded",
							playerId, twPlayerData.getEnterTime(), twPlayerData.getQuitTime(), twPlayerData.isMidwayQuit(), twPlayerData.isAwarded());
					continue;
				}
				long selfScore = twPlayerData.getScore();
				// 战场异常中断导致未正常结算,按照最高档次奖励发放
				if (!isComplete) {
					selfScore = Integer.MAX_VALUE;
					selfMailId = MailId.TBLY_SELF_PAUSE_MAIL;
				}
				
				ActivityManager.getInstance().postEvent(new TWScoreEvent(playerId, selfScore, true,twPlayerData.getEnterTime()));
				MissionManager.getInstance().postMsg(playerId, new EventTiberiumWar());

				if (twPlayerData.getEnterTime() == 0 || twPlayerData.isMidwayQuit()) {
					HawkLog.logPrintln("TiberiumLeaguaWarService ignore self reward, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}", playerId,
							twPlayerData.getEnterTime(), twPlayerData.getQuitTime(), twPlayerData.isMidwayQuit());
					continue;
				}
				TiberiumPersonAwardCfg selfCfg = TiberiumWarService.getInstance().getPersonAwardCfg(selfScore, isWin);
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(selfMailId)
						.addContents(selfScore)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addRewards(selfCfg.getRewardItem())
						.build());
				twPlayerData.setAwarded(true);
				RedisProxy.getInstance().updateTWPlayerData(twPlayerData, mark);
				HawkLog.logPrintln("TiberiumLeaguaWarService send self reward, playerId: {}, score: {}, cfgId: {}, isComplete: {}", playerId, selfScore, selfCfg.getId(), isComplete);
			}
			TWLogUtil.logTimberiumLeaguaSelfReward(selfRewardLogUnitList);
			TWLogUtil.logTimberiumLeaguaPlayerScore(pSeasonScorgLogUnitList);
			try {
				ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(memberIds)));
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
		//通知活动模块
		TblyGuessActiviytKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(TblyGuessActiviytKVCfg.class);
		if (termId >= kvCfg.getStartRound()) {
			HawkLog.logPrintln("TiberiumLeaguaWarService TblyGuessSendRewardEvent  mark: {}, winGuilds: {}", mark, winGuilds.toString());
			ActivityManager.getInstance().postEvent(new TblyGuessSendRewardEvent(mark, winGuilds), true);
		}
		
	}

	/**
	 * 批量创建
	 * @param timeIndex
	 */
	private void createBattle() {
		int mark = activityInfo.getMark();
		TiberiumSeasonTimeCfg timeCfg = calcTimeCfg();
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		long startTime = timeCfg.getWarStartTimeValue();
		long overTime = startTime + TiberiumConstCfg.getInstance().getWarOpenTime();
		List<TWRoomData> dataList = RedisProxy.getInstance().getAllTWRoomData(mark);
		String serverId = GsConfig.getInstance().getServerId();
		List<TWRoomData> createList = dataList.stream().filter(t -> t.getRoomServerId().equals(serverId) ).collect(Collectors.toList());
		if(createList == null || createList.isEmpty()){
			return;
		}
		for (TWRoomData roomData : createList) {
			String guildA = roomData.getGuildA();
			String guildB = roomData.getGuildB();
			TWGuildData guildDataA = RedisProxy.getInstance().getTWGuildData(guildA, mark);
			TWGuildData guildDataB = RedisProxy.getInstance().getTWGuildData(guildB, mark);
			if (guildDataA == null || guildDataB == null) {
				roomData.setRoomState(RoomState.INITED_FAILED);
			} else {
				TWGuildData campA = null;
				TWGuildData campB = null;
				if (!guildDataA.getServerId().equals(roomData.getRoomServerId())) {
					campA = guildDataA;
					campB = guildDataB;
				} else {
					campA = guildDataB;
					campB = guildDataA;
				}

				TBLYExtraParam param = new TBLYExtraParam();
				param.setCampAGuild(campA.getId());
				param.setCampAguildFlag(campA.getFlag());
				param.setCampAGuildName(campA.getName());
				param.setCampAGuildTag(campA.getTag());
				param.setCampAServerId(campA.getServerId());
				param.setCampAPlayers(RedisProxy.getInstance().getAllTWPlayerData(campA.getId(), termId));
				param.setCampBGuild(campB.getId());
				param.setCampBguildFlag(campB.getFlag());
				param.setCampBGuildName(campB.getName());
				param.setCampBGuildTag(campB.getTag());
				param.setCampBServerId(campB.getServerId());
				param.setCampBPlayers(RedisProxy.getInstance().getAllTWPlayerData(campB.getId(), termId));
				param.setLeaguaWar(true);
				param.setSeason(activityInfo.getSeason());
				boolean result = TBLYRoomManager.getInstance().creatNewBattle(startTime, overTime, roomData.getId(), param);
				if (result) {
					roomData.setRoomState(RoomState.INITED);
					LogUtil.logTimberiumLeaguaManageEndInfo(roomData.getId(), roomData.getRoomServerId(), season, termId, guildA, guildDataA.getServerId(), guildDataA.getName(),
							guildDataA.getTotalPower(), guildB, guildDataB.getServerId(), guildDataB.getName(), guildDataB.getTotalPower());
				} else {
					roomData.setRoomState(RoomState.INITED_FAILED);
				}
			}
		}
		RedisProxy.getInstance().updateTWRoomData(createList, mark);
	}
	
	/**
	 * 战场结束
	 * @param msg
	 */
	@MessageHandler
	private void onBattleFinish(TBLYBilingInformationMsg msg) {
		String roomId = msg.getRoomId();
		int mark = activityInfo.getMark();
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		TWRoomData roomData = RedisProxy.getInstance().getTWRoomData(roomId, mark);
		if (roomData == null) {
			HawkLog.logPrintln("TiberiumLeaguaWarService onBattleFinish error, room data null, mark: {}, roomId: {}", mark, roomId);
			return;
		}
		roomData.setRoomState(RoomState.CLOSE);
		RedisProxy.getInstance().updateTWRoomData(roomData, mark);
		String guildA = roomData.getGuildA();
		long scoreA = msg.getGuildHonor(guildA);
		String guildB = roomData.getGuildB();
		long scoreB = msg.getGuildHonor(guildB);
		TLWGuildJoinInfo joinInfoA = RedisProxy.getInstance().getTLWJoinGuild(season, guildA);
		TLWGuildJoinInfo joinInfoB = RedisProxy.getInstance().getTLWJoinGuild(season, guildB);
		TLWGroupType groupType = joinInfoA.getGroup();
		
		// 记录联盟积分数据
		TWGuildData guildDataA = RedisProxy.getInstance().getTWGuildData(guildA, mark);
		TWGuildData guildDataB = RedisProxy.getInstance().getTWGuildData(guildB, mark);
		// 联盟ELO数据
		TWGuildEloData eloDataA = RedisProxy.getInstance().getTWGuildElo(guildA);
		TWGuildEloData eloDataB = RedisProxy.getInstance().getTWGuildElo(guildB);
		int eloBefA = eloDataA.getScore();
		int eloBefB = eloDataB.getScore();
		guildDataA.setScore(scoreA);
		guildDataA.setComplete(true);
		guildDataB.setScore(scoreB);
		guildDataB.setComplete(true);
		long guildScoreLimit = TiberiumConstCfg.getInstance().getSeasonGuildScoreLimit();
		//赛季开启,记录玩家和联盟积分
		if (season > 0) {
			TLWScoreData guildAScore = RedisProxy.getInstance().getTLWGuildScoreInfo(season, guildA);
			long scoreAIncrease = Math.min(scoreA, guildScoreLimit);
			guildAScore.setScore(guildAScore.getScore() + scoreAIncrease);
			TLWScoreData guildBScore = RedisProxy.getInstance().getTLWGuildScoreInfo(season, guildB);
			long scoreBIncrease = Math.min(scoreB, guildScoreLimit);
			guildBScore.setScore(guildBScore.getScore() + scoreBIncrease);
			RedisProxy.getInstance().updateTLWGuildScoreInfo(season, guildAScore);
			RedisProxy.getInstance().updateTLWGuildScoreInfo(season, guildBScore);
		}
		List<PlayerGameRecord> recods = msg.getPlayerRecords();
		
		long personScoreLimit = TiberiumConstCfg.getInstance().getSeasonPersonScoreLimit();
		List<TWPlayerScoreLogUnit> playerScoreLogList = new ArrayList<>();
		// 记录玩家积分数据
		for (PlayerGameRecord recod : recods) {
			try {
				String playerId = recod.getPlayerId();
				TWPlayerData playerDate = RedisProxy.getInstance().getTWPlayerData(playerId, mark);
				if (playerDate != null) {
					playerDate.setScore(recod.getHonor());
					RedisProxy.getInstance().updateTWPlayerData(playerDate, mark);
					playerScoreLogList.add(new TWPlayerScoreLogUnit(playerDate.getId(), mark, playerDate.getGuildId(), recod.getHonor()));
					//赛季开启,记录玩家和联盟积分
					if (season > 0) {
						TLWScoreData playerScore = RedisProxy.getInstance().getTLWPlayerScoreInfo(season, playerId);
						long scoreBef = playerScore.getScore();
						long warScore = recod.getHonor();
						long addScore = Math.min(personScoreLimit, warScore);
						playerScore.setScore(playerScore.getScore() + addScore);
						RedisProxy.getInstance().updateTLWPlayerScoreInfo(season, playerScore);
						HawkLog.logPrintln(
								"TiberiumLeaguaWarService  playerSeasonScoreSdd, playerId: {}, guildId: {}, season: {}, mark:{}, serverId: {}, roomId: {}, roomServer: {}, scoreBef: {}, warScore: {}, addScore: {}, scoreAft: {}",
								playerDate.getId(), playerDate.getGuildId(), season, mark, playerDate.getServerId(), roomId, roomData.getRoomServerId(), scoreBef, warScore,
								addScore, playerScore.getScore());
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				continue;
			}
		}
		// Tlog 延时记录玩家个人积分,避免拥堵
		TWLogUtil.logTimberiumPlayerScoreInfo(playerScoreLogList);
		
		String winGuild = msg.getWinGuild();
		// 以战场传来的胜负为准,只有在战场传来的胜利者为空时,根据分数和战力结算胜负
		if(HawkOSOperator.isEmptyString(winGuild)){
			if (scoreA != scoreB) {
				winGuild = scoreA > scoreB ? guildA : guildB;
			} else {
				winGuild = guildDataA.getTotalPower() > guildDataB.getTotalPower() ? guildA : guildB;
			}
		}
		// 处理elo积分
		HawkTuple2<Integer, Integer> changeTuple = TiberiumWarService.getInstance().calcEloChangeValue(eloDataA.getScore(), eloDataB.getScore());
		int eloChange = 0;
		if (eloBefA >= eloBefB) {
			if (winGuild.equals(guildA)) {
				eloChange = changeTuple.first;
			} else {
				eloChange = changeTuple.second;
			}
		} else {
			if (winGuild.equals(guildA)) {
				eloChange = changeTuple.second;
			} else {
				eloChange = changeTuple.first;
			}
		}
		
		if (winGuild.equals(guildA)) {
			eloDataA.setScore(eloDataA.getScore() + eloChange);
			eloDataB.setScore(eloDataB.getScore() - eloChange);
		} else {
			eloDataA.setScore(eloDataA.getScore() - eloChange);
			eloDataB.setScore(eloDataB.getScore() + eloChange);
		}
		
		roomData.setWinnerId(winGuild);
		roomData.setScoreA(scoreA);
		roomData.setScoreB(scoreB);
		guildDataA.setWin(winGuild.equals(guildA));
		guildDataB.setWin(winGuild.equals(guildB));
		RedisProxy.getInstance().updateTWGuildData(guildDataA, mark);
		RedisProxy.getInstance().updateTWGuildData(guildDataB, mark);
		RedisProxy.getInstance().updateTWGuildElo(eloDataA);
		RedisProxy.getInstance().updateTWGuildElo(eloDataB);
		RedisProxy.getInstance().updateTWRoomData(roomData, mark);
		
		dealWithJoinGuild(guildDataA,joinInfoA);
		dealWithJoinGuild(guildDataB,joinInfoB);
		
		// 记录战斗记录
		TWBattleLog.Builder builder = TWBattleLog.newBuilder();
		builder.setTermId(activityInfo.getMark());
		builder.setRoomId(roomId);
		builder.setWinGuild(winGuild);
		long startTime = getCurrTimeCfg().getWarStartTimeValue();
		builder.setTime(startTime);
		TWGuildInfo.Builder guildInfoA = guildDataA.build();
		guildInfoA.setEloScoreBef(eloBefA);
		guildInfoA.setCurrEloScore(eloDataA.getScore());
		builder.addGuildInfo(guildInfoA);
		TWGuildInfo.Builder guildInfoB = guildDataB.build();
		guildInfoB.setEloScoreBef(eloBefB);
		guildInfoB.setCurrEloScore(eloDataB.getScore());
		builder.addGuildInfo(guildInfoB);
		TWBattleLog battleLog = builder.build();
		RedisProxy.getInstance().addTWBattleLog(battleLog, guildA);
		RedisProxy.getInstance().addTWBattleLog(battleLog, guildB);
			
		//决赛,战斗完刷新决赛小组信息
		if(activityInfo.termId == TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
			TLWEliminationGroup groupInfo = RedisProxy.getInstance().getTLWEliminationGroupInfo(season, groupType);
			Map<Integer, List<TLWBattleData>> stageMap = groupInfo.getBattleMap();
			List<TLWBattleData> battleDatas = stageMap.get(TiberiumConstCfg.getInstance().getEliminationFinalTermId());
			TLWBattleData battleData = battleDatas.get(0);
			battleData.setWinnerGuild(winGuild);
			RedisProxy.getInstance().updateTLWEliminationGroupInfo(season, groupInfo);
			// 刷新排行
			loadAndRefreshRank();
		}
		try {
			// 联盟战场积分
			TWGuildScoreLogUnit gScoreLogUnitA = new TWGuildScoreLogUnit(guildDataA.getId(), guildDataA.getName(), mark, guildDataA.getServerId(), roomId, roomData.getRoomServerId(), guildDataA.getScore(),
					guildDataA.getMemberCnt(), guildDataA.getTotalPower(), guildDataA.isWin());
			TWGuildScoreLogUnit gScoreLogUnitB = new TWGuildScoreLogUnit(guildDataB.getId(), guildDataB.getName(), mark, guildDataB.getServerId(), roomId, roomData.getRoomServerId(), guildDataB.getScore(),
					guildDataB.getMemberCnt(), guildDataB.getTotalPower(), guildDataB.isWin());
			TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitA);
			TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitB);
			
			// 联赛战斗结果
			TLWWarResultLogUnit resultLogUnit = new TLWWarResultLogUnit(roomId, season, termId, guildA, scoreA, guildB, scoreB, winGuild, msg.getFirstKillNian(), msg.getFirst5000Honor(), msg.getFirstControlHeXin(), roomData.getBattleType().getValue(), roomData.getServerType());
			TWLogUtil.logTimberiumLeaguaWarResult(resultLogUnit);
			
			// elo积分流水
			TWEloScoreLogUnit eloScoreUnitA = new TWEloScoreLogUnit(guildA, activityInfo.getMark(), eloBefA, eloDataA.getScore(), eloDataA.getScore() - eloBefA, EloReason.WAR_CALC);
			TWEloScoreLogUnit eloScoreUnitB = new TWEloScoreLogUnit(guildB, activityInfo.getMark(), eloBefB, eloDataB.getScore(), eloDataB.getScore() - eloBefB, EloReason.WAR_CALC);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnitA);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnitB);
			HawkLog.logPrintln(
					"TiberiumWarService  guildScoreInfo, guildId: {}, guildName: {} , termId: {}, serverId: {}, roomId: {}, roomServer: {}, score: {}, memberCnt: {}, totalPower: {}, isWin: {}",
					guildDataA.getId(), guildDataA.getName(), mark, guildDataA.getServerId(), roomId, roomData.getRoomServerId(), guildDataA.getScore(),
					guildDataA.getMemberCnt(), guildDataA.getTotalPower(), guildDataA.isWin());
			HawkLog.logPrintln(
					"TiberiumWarService  guildScoreInfo, guildId: {}, guildName: {} , termId: {}, serverId: {}, roomId: {}, roomServer: {}, score: {}, memberCnt: {}, totalPower: {}, isWin: {}",
					guildDataB.getId(), guildDataB.getName(), mark, guildDataB.getServerId(), roomId, roomData.getRoomServerId(), guildDataB.getScore(),
					guildDataB.getMemberCnt(), guildDataB.getTotalPower(), guildDataB.isWin());
			
			// 入围时记录参赛联盟信息
			TLWGuildData tlwGuildA = RedisProxy.getInstance().getTLWGuildData(guildA, season);
			TLWGuildData tlwGuildB = RedisProxy.getInstance().getTLWGuildData(guildB, season);
			TLWLeaguaGuildInfoUnit guildUnitA = new TLWLeaguaGuildInfoUnit(activityInfo.getTermId(), joinInfoA, tlwGuildA);
			TLWLeaguaGuildInfoUnit guildUnitB = new TLWLeaguaGuildInfoUnit(activityInfo.getTermId(), joinInfoB, tlwGuildB);
			TWLogUtil.logTimberiumLeaguaGuildInfo(guildUnitA);
			TWLogUtil.logTimberiumLeaguaGuildInfo(guildUnitB);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 处理战后参与联盟信息
	 * @param guildData
	 */
	private void dealWithJoinGuild(TWGuildData guildData,TLWGuildJoinInfo guildInfo) {
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		this.calTLWEliminationGroupType(guildInfo, termId, guildData.isWin);
		if (!guildData.isWin) {
			guildInfo.setLoseCnt(guildInfo.getLoseCnt() + 1);
		} else {
			guildInfo.setWinCnt(guildInfo.getWinCnt() + 1);
		}
		long guildScoreLimit = TiberiumConstCfg.getInstance().getSeasonGuildScoreLimit();
		long addScore = Math.min(guildScoreLimit, guildData.getScore());
		guildInfo.setScore(guildInfo.getScore() + addScore);
		RedisProxy.getInstance().updateTLWJoinGuild(guildInfo, season);
	}
	
	
	/**
	 * 计算淘汰赛组别
	 * @param guildInfo
	 * @param termId
	 * @param win
	 */
	private void calTLWEliminationGroupType(TLWGuildJoinInfo guildInfo,int termId,boolean win){
		//淘汰赛开始期数
		int startEliminationTerm = TiberiumConstCfg.getInstance().getEliminationStartTermId();
		int finalTermId = TiberiumConstCfg.getInstance().getEliminationFinalTermId();
		if (termId >= startEliminationTerm) {
			//淘汰赛,胜者组战败，进入败者组，败者组战败，被淘汰
			if(win){
				//如果胜利保持当前组
				return;
			}else{
				//胜者组战败，进入败者组，败者组战败，被淘汰
				if(guildInfo.getEliminationGroup() == TLWEliminationGroupType.ELIMINATION_WIN){
					//进入败者组
					guildInfo.setEliminationGroup(TLWEliminationGroupType.ELIMINATION_LOSS);
				}else if(guildInfo.getEliminationGroup() == TLWEliminationGroupType.ELIMINATION_LOSS){
					//被淘汰
					guildInfo.setGroup(TLWGroupType.KICK_OUT);
				}
				if(termId >= finalTermId){
					guildInfo.setGroup(TLWGroupType.KICK_OUT);
				}
			}
		}
		// 记录被淘汰轮次
		if (guildInfo.getGroup() == TLWGroupType.KICK_OUT) {
			guildInfo.setKickOutTerm(termId);
		}
	}

	/**
	 * 加载本赛季参与的联盟信息
	 */
	private void loadJoinGuildsInfo() {
		int season = activityInfo.getSeason();
		Map<String, TLWGuildJoinInfo> joinInfos = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		List<TLWGuildJoinInfo> joinList = new ArrayList<>(joinInfos.values());
		Collections.sort(joinList,new TLWGuildJoinInfoSeanRankComparator());
		Map<String, TLWGroupType> currGroupMap = new ConcurrentHashMap<>();
		Map<String, TLWGroupType> initGroupMap = new ConcurrentHashMap<>();
		Map<String, Integer> rankMap = new ConcurrentHashMap<>();
		int rank = 1;
		int newRank = 1;
		for (TLWGuildJoinInfo joinInfo : joinList) {
			currGroupMap.put(joinInfo.getId(), joinInfo.getGroup());
			initGroupMap.put(joinInfo.getId(), joinInfo.getInitGroup());
			if(joinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				rankMap.put(joinInfo.getId(), newRank);
				newRank++;
			}else {
				rankMap.put(joinInfo.getId(), rank);
				rank++;
			}

		}
		currGuildGroup = currGroupMap;
		currRankMap = rankMap;
	}
	
	/**
	 * 阶段轮询
	 */
	private void stateTick() {
		try {
			checkStateChange();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 活动阶段检测
	 */
	private void checkStateChange() {
		TLWActivityData newInfo = calcStateInfo();
		int old_season = activityInfo.getSeason();
		int new_serson = newInfo.getSeason();

		// 如果当前赛季和当前实赛季数不一致,且当前活动未开启,则推送活动状态,且刷新状态信息
		if (old_season != new_serson && new_serson == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateTLWActivityInfo(activityInfo);
			return;
		}
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();
		TLWActivityState old_state = activityInfo.getState();
		TLWActivityState stateBef = activityInfo.getState();
		TLWActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 赛季/期数不一致,重置活动状态,从当前期数未开启阶段开始轮询
		if(old_season != new_serson || old_term != new_term){
			activityInfo.setTermId(newInfo.getTermId());
			activityInfo.setSeason(newInfo.getSeason());
			// 赛季开启时发送全服邮件通知
			if(new_term == 1){
				sendOpenMail();
			}
			old_state = TLWActivityState.TLW_NOT_OPEN;
			needUpdate = true;
		}
		
		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == TLWActivityState.TLW_NOT_OPEN) {
				old_state = TLWActivityState.TLW_PEACE;
				activityInfo.setState(old_state);
				onPeaceStart();
			} else if (old_state == TLWActivityState.TLW_PEACE ) {
				if(new_state == TLWActivityState.TLW_END_SHOW){
					old_state = TLWActivityState.TLW_END_SHOW;
					activityInfo.setState(new_state);
					onSeasonEndShow();
				}
				else{
					old_state = TLWActivityState.TLW_MATCH;
					activityInfo.setState(old_state);
				}
			} else if (old_state == TLWActivityState.TLW_MATCH) {
				old_state = TLWActivityState.TLW_WAR_MANGE;
				activityInfo.setState(old_state);
				onMatchFinish();
			} else if (old_state == TLWActivityState.TLW_WAR_MANGE) {
				old_state = TLWActivityState.TLW_WAR_WAIT;
				activityInfo.setState(old_state);
				onWarMangeFinish();
			} else if (old_state == TLWActivityState.TLW_WAR_WAIT) {
				old_state = TLWActivityState.TLW_WAR_OPEN;
				activityInfo.setState(old_state);
			} else if (old_state == TLWActivityState.TLW_WAR_OPEN) {
				old_state = TLWActivityState.TLW_PEACE;
				activityInfo.setState(old_state);
				onWarEnd();
			}
		}
	
		if (needUpdate) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateTLWActivityInfo(activityInfo);
			HawkLog.logPrintln(
					"TiberiumLeagueWarService state change, old_season: {}, oldState: {}, stateBef:{} ,new_serson: {}, newState: {}",
					old_season, old_state, stateBef, activityInfo.getSeason(), activityInfo.getState());
		}
	
		
	}
	
	/**
	 * 赛季开启邮件通知
	 */
	private void sendOpenMail() {
		int season = activityInfo.getSeason();
		TiberiumSeasonTimeCfg cfg = getTimeCfgBySeasonAndTermId(season, 1);
		if(cfg == null){
			return;
		}
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.TIBERIUM_SEASON_OPEN)
				.build()
				, HawkTime.getMillisecond(), cfg.getMatchStartTimeValue());
	}

	/**
	 * 发放积分排名奖励,结算赛季积分
	 */
	private void onSeasonEndShow() {
		// 发送赛季排行奖励
		sendTLWRankReward();
		// 结算赛季积分
		calcEloScoreOnSeasonEnd();
		
	}

	/**
	 * 进入战前准备阶段
	 */
	private void onMatchFinish() {
		loadJoinGuildsInfo();
		loadMatchResult();
		if(activityInfo.getTermId() == 1){
			loadMatchedRoom();
		}else{
			updateCurrMatchedRoom();
		}
		if (activityInfo.getTermId() == 1 || activityInfo.getTermId() == TiberiumConstCfg.getInstance().getEliminationStartTermId()) {
			sendGroupMail();
		}
	}
	
	/**
	 *  推送分组邮件
	 */
	private void sendGroupMail() {
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		Map<String, TLWGroupType> groupMap = currGuildGroup;
		for (Entry<String, TLWGroupType> entry : groupMap.entrySet()) {
			String guildId = entry.getKey();
			TLWGroupType group = entry.getValue();
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj == null) {
				continue;
			}
			if (termId == 1) {
				// 小组赛分组邮件
				TLWGuildJoinInfo guildInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
				if (guildInfo == null) {
					HawkLog.logPrintln("TiberiumLeagueWarService sendGroupMail error, guild:{}", guildId);
					continue;
				}
				GuildMailService.getInstance().sendGuildMail(guildId,
						MailParames.newBuilder()
						.setMailId(MailId.TIBERIUM_SEASON_TEAM_GROUP)
						.addContents(guildInfo.getTeamId()));
			}else if(termId == TiberiumConstCfg.getInstance().getEliminationStartTermId()){
				TLWGuildJoinInfo joinInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
				TLWGuildData guildData = RedisProxy.getInstance().getTLWGuildData(guildId, season);
				//季后赛分组结束时记录参赛联盟信息
				TLWLeaguaGuildInfoUnit logUnit = new TLWLeaguaGuildInfoUnit(activityInfo.getTermId(), joinInfo, guildData);
				TWLogUtil.logTimberiumLeaguaGuildInfo(logUnit);
				// 季后赛分组邮件
				GuildMailService.getInstance().sendGuildMail(guildId,
						MailParames.newBuilder()
						.setMailId(MailId.TIBERIUM_SEASON_FINAL_GROUP)
						.addContents(group.getNumber()));
			}
		}
		
	}

	/**
	 * 进入和平等待阶段
	 */
	private void onPeaceStart() {
		loadJoinGuildsInfo();
	}
	
	/**
	 * 战前管理阶段结束
	 */
	private void onWarMangeFinish() {
		loadJoinGuildsInfo();
		flushGuildWarInfo();
	}
	
	/**
	 * 战斗结束
	 */
	private void onWarEnd(){
		loadJoinGuildsInfo();
	}

	/**
	 * 同步活动信息
	 * @param player
	 */
	public void syncStateInfo(Player player) {
		// 同步活动信息
		TLWPageInfo.Builder pageInfo = genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_INFO_SYNC, pageInfo));
	}
	
	public GetTLWGuildRankResp.Builder getGuildPowerRank(Player player, TiberiumWar.TLWServer serverType) {
		if (activityInfo.getTermId() == 1 && activityInfo.getState().getNumber() <= TLWActivityState.TLW_MATCH.getNumber()) {
			return getGuildPowerRankBefPick(player, serverType);
		} else {
			return getGuildPowerRankAftPick(player, serverType);
		}
	}
	
	/**
	 * 海选阶段联盟战力排行列表
	 * @param areaId
	 * @param player
	 * @return
	 */
	public GetTLWGuildRankResp.Builder getGuildPowerRankBefPick(Player player, TiberiumWar.TLWServer serverType) {
		int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
		int rankLimit = TiberiumConstCfg.getInstance().getPowerRankSize();
		String selfGuildId = player.getGuildId();
		GetTLWGuildRankResp.Builder builder = GetTLWGuildRankResp.newBuilder();
		Set<Tuple> tuples = null;
		if(serverType == TiberiumWar.TLWServer.TLW_NEW_SERVER){
			tuples = RedisProxy.getInstance().getTLWGuildPowerRanksNew(season, 0, rankLimit - 1);
		}else {
			tuples = RedisProxy.getInstance().getTLWGuildPowerRanks(season, 0, rankLimit - 1);
		}
		List<String> guildIds = tuples.stream().map(t -> t.getElement()).collect(Collectors.toList());
		Map<String, TLWGuildData> guildDatas = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		TLWGuildRank.Builder selfRank = null;
		int rank = 1;
		for (Tuple tuple : tuples) {
			String guildId = tuple.getElement();
			long power = (long) tuple.getScore();
			TLWGuildData guildData = guildDatas.get(guildId);
			if (guildData == null) {
				continue;
			}
			TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
			TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
			guildBuilder.setBaseInfo(guildData.genBaseInfo());
			guildBuilder.setBattlePoint(power);
			rankInfo.setGuildInfo(guildBuilder);
			rankInfo.setRank(rank);
			builder.addRankInfo(rankInfo);
			if (guildId.equals(selfGuildId)) {
				selfRank = rankInfo;
			}
			rank++;
		}
		if (!HawkOSOperator.isEmptyString(selfGuildId) && !player.isCsPlayer()) {
			if (selfRank == null) {
				selfRank = TLWGuildRank.newBuilder();
				GuildInfoObject guildData = GuildService.getInstance().getGuildInfoObject(selfGuildId);
				HawkTuple2<Integer, Long> joinInfo = TiberiumWarService.getInstance().getJoinMemberPower(selfGuildId);
				long power = joinInfo.second;
				TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();

				TLWGuildBaseInfo.Builder baseInfo = TLWGuildBaseInfo.newBuilder();
				baseInfo.setId(guildData.getId());
				baseInfo.setName(guildData.getName());
				baseInfo.setTag(guildData.getTag());
				baseInfo.setGuildFlag(guildData.getFlagId());
				baseInfo.setLeaderName(guildData.getLeaderName());
				baseInfo.setServerId(guildData.getServerId());
				guildBuilder.setBaseInfo(baseInfo);
				guildBuilder.setBattlePoint(power);
				selfRank.setGuildInfo(guildBuilder);
				selfRank.setRank(-1);
			}
			builder.setSelfRank(selfRank);
		}
		return builder;
	}
	
	/**
	 * 获取入围联盟战力排行榜信息
	 * @param areaId
	 * @param player
	 * @return
	 */
	public GetTLWGuildRankResp.Builder getGuildPowerRankAftPick(Player player, TiberiumWar.TLWServer serverType) {
		int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
		String selfGuildId = player.getGuildId();
		GetTLWGuildRankResp.Builder builder = GetTLWGuildRankResp.newBuilder();
		if(season <= 0){
			return builder;
		}
		Map<String, TLWGuildJoinInfo> joimMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		List<String> guildIds = joimMap.values().stream().filter(e -> e.getServerType() == serverType.getNumber()).map(e -> e.getId()).collect(Collectors.toList());
		List<TLWGuildJoinInfo> joinList = new ArrayList<>(joimMap.values());
		Collections.sort(joinList,new TLWGuildJoinInfoSeanRankComparator());
		Map<String, TLWGroupType> groupMap = new ConcurrentHashMap<>();
		Map<String, TLWEliminationGroupType> eliminationGroupMap = new ConcurrentHashMap<>();
		Map<String, Integer> seasonRankMap = new ConcurrentHashMap<>();
		int seasonRank = 1;
		for (TLWGuildJoinInfo joinInfo : joinList) {
			if(joinInfo.getServerType() != serverType.getNumber()){
				continue;
			}
			groupMap.put(joinInfo.getId(), joinInfo.getGroup());
			seasonRankMap.put(joinInfo.getId(), seasonRank);
			TLWEliminationGroupType egroupType = joinInfo.getEliminationGroup();
			if(egroupType != null){
				eliminationGroupMap.put(joinInfo.getId(), egroupType);
			}
			seasonRank++;
		}
		// 联盟入围后,排行榜依照入围时的排行排序
		Collections.sort(joinList,new TLWGuildInitPowerRankComparator());
		Map<String, TLWGuildData> guildDatas = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		TLWGuildRank.Builder selfRank = null;
		TiberiumSeasonTimeCfg finalCfg = getTimeCfgBySeasonAndTermId(season, TiberiumConstCfg.getInstance().getEliminationFinalTermId());
		long currTime = HawkTime.getMillisecond();
		boolean finalWarFinish = currTime > (finalCfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
		int rank = 1;
		for (TLWGuildJoinInfo joinInfo : joinList) {
			String guildId = joinInfo.getId();
			TLWGuildData guildData = guildDatas.get(guildId);
			if (guildData == null) {
				continue;
			}
			long power = guildData.getPower();
			TLWGuildRank.Builder rankInfo = TLWGuildRank.newBuilder();
			TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();
			guildBuilder.setBaseInfo(guildData.genBaseInfo());
			guildBuilder.setBattlePoint(power);
			TLWGroupType groupType = groupMap.get(guildId);
			TLWEliminationGroupType eGroupType = eliminationGroupMap.get(guildId);
			int currRank = seasonRankMap.get(guildId);
			if (groupType != null) {
				guildBuilder.setGroup(TLWGroup.valueOf(groupType.getNumber()));
			}
			if(eGroupType != null){
				guildBuilder.setEliminationGroup(PBTLWEliminationGroup.valueOf(eGroupType.getValue()));
			}
			if (groupType == TLWGroupType.KICK_OUT || finalWarFinish) {
				guildBuilder.setCurrRank(currRank);
			}
			rankInfo.setGuildInfo(guildBuilder);
			rankInfo.setRank(rank);
			builder.addRankInfo(rankInfo);
			if (guildId.equals(selfGuildId)) {
				selfRank = rankInfo;
			}
			rank++;
		}
		if (!HawkOSOperator.isEmptyString(selfGuildId) && !player.isCsPlayer()) {
			if (selfRank == null) {
				selfRank = TLWGuildRank.newBuilder();
				GuildInfoObject guildData = GuildService.getInstance().getGuildInfoObject(selfGuildId);
				HawkTuple2<Integer, Long> joinInfo = TiberiumWarService.getInstance().getJoinMemberPower(selfGuildId);
				long power = joinInfo.second;
				TLWGuildInfo.Builder guildBuilder = TLWGuildInfo.newBuilder();

				TLWGuildBaseInfo.Builder baseInfo = TLWGuildBaseInfo.newBuilder();
				baseInfo.setId(guildData.getId());
				baseInfo.setName(guildData.getName());
				baseInfo.setTag(guildData.getTag());
				baseInfo.setGuildFlag(guildData.getFlagId());
				baseInfo.setLeaderName(guildData.getLeaderName());
				baseInfo.setServerId(guildData.getServerId());
				guildBuilder.setBaseInfo(baseInfo);
				guildBuilder.setBattlePoint(power);
				selfRank.setGuildInfo(guildBuilder);
				guildBuilder.setGroup(TLWGroup.NOMAL_GROUP);
				selfRank.setRank(-1);
			}
			builder.setSelfRank(selfRank);
		}
		return builder;
	}
	/**
	 * 获取联盟的分组
	 * @param guildId
	 * @return
	 */
	public int getGuildTeamId(String guildId) {
		TLWGuildJoinInfo joinInfo = RedisProxy.getInstance().getTLWJoinGuild(activityInfo.getSeason(), guildId);
		if (joinInfo != null) {
			return joinInfo.getTeamId();
		}
		return 0;
	}


	public TiberiumWar.TLWServer getServerType(){
		TiberiumWar.TLWServer type = TiberiumWar.TLWServer.TLW_NEW_SERVER;
		String serverId = GsConfig.getInstance().getServerId();
		List<String> serverList = GlobalData.getInstance().getMergeServerList(serverId);
		//没有合服 或者不在合服列表里面. 取本服.
		if (CollectionUtils.isEmpty(serverList)) {
			return type;
		}
		if(serverList.size() > 8){
			type = TiberiumWar.TLWServer.TLW_OLD_SERVER;
		}
		return type;
	}

	public TiberiumWar.TLWServer getGuildType(Player player){
		TiberiumWar.TLWServer type = getServerType();
		if(!player.hasGuild()){
			return type;
		}
		int season = activityInfo.getSeason();
		Set<String> signupOldGuilds = RedisProxy.getInstance().getTLWNewSignupOldServer(season);
		String guildId = player.getGuildId();
		if (activityInfo.getTermId() == 1 && activityInfo.getState().getNumber() <= TLWActivityState.TLW_MATCH.getNumber()) {
			if(type == TiberiumWar.TLWServer.TLW_NEW_SERVER && signupOldGuilds.contains(guildId)){
				type = TiberiumWar.TLWServer.TLW_OLD_SERVER;
			}
		}else {
			TLWGuildJoinInfo guildData = RedisProxy.getInstance().getTLWJoinGuild(season,guildId);
			if(guildData != null){
				type = TiberiumWar.TLWServer.valueOf(guildData.getServerType());
			}
		}
		return type;
	}

	public TLWPageInfo.Builder genPageInfo(Player player) {
		TiberiumWar.TLWServer type = getGuildType(player);
		return genPageInfo(player, type);
	}
	/**
	 * 构建主界面信息
	 * @param player
	 * @return
	 */
	public TLWPageInfo.Builder genPageInfo(Player player, TiberiumWar.TLWServer serverType) {
		int season = activityInfo.getSeason();
		int mark = activityInfo.getMark();
		TLWPageInfo.Builder builder = TLWPageInfo.newBuilder();
		TLWStateInfo.Builder stateInfo = genStateInfo();
		String guildId = player.getGuildId();
		int teamId = getGuildTeamId(guildId);
		if (teamId != 0) {
			builder.setTeamId(teamId);
		}
		builder.setGuildType(serverType);
		boolean inSeasonWar = false;
		TLWGroupType currGroup = TLWGroupType.NORMAL;
		if (stateInfo.getState() != TLWState.TLW_NOT_OPEN && !HawkOSOperator.isEmptyString(guildId) && currGuildGroup.containsKey(guildId)) {
			TLWGuildJoinInfo guildAInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
			if(guildAInfo != null && guildAInfo.getServerType() == serverType.getNumber()){
				currGroup = currGuildGroup.get(guildId);
				if (currGroup == TLWGroupType.NORMAL || currGroup == TLWGroupType.KICK_OUT) {
					inSeasonWar = false;
				} else {
					inSeasonWar = true;
				}
			}
		}
		builder.setInSeasonWar(inSeasonWar);
		if(serverType == TiberiumWar.TLWServer.TLW_NEW_SERVER && activityInfo.getTermId() == 1 && activityInfo.getState().getNumber() < TLWActivityState.TLW_MATCH.getNumber() && currGroup == TLWGroupType.NORMAL){
			Set<String> signupOldGuilds = RedisProxy.getInstance().getTLWNewSignupOldServer(season);
			if(!signupOldGuilds.contains(guildId)){
				builder.setGroup(TLWGroup.NOT_SIGNUP);
			}else {
				builder.setGroup(TLWGroup.valueOf(currGroup.getNumber()));
			}
		}else {
			builder.setGroup(TLWGroup.valueOf(currGroup.getNumber()));
		}
		// 参与本期战斗,且已匹配完成,则显示对手信息
		if (inSeasonWar && stateInfo.getState().getNumber() > TLWState.TLW_MATCH_VALUE && stateInfo.getState().getNumber() <= TLWState.TLW_WAR_OPEN_VALUE) {
			String oppGuild = null;
			for (TWRoomData room : roomList) {
				if (room.getGuildA().equals(guildId) || room.getGuildB().equals(guildId)) {
					oppGuild = room.getOppGuildId(guildId);
					break;
				}
			}
			if (!HawkOSOperator.isEmptyString(oppGuild)) {
				long startTime = getCurrTimeCfg().getWarStartTimeValue();
				if (activityInfo.getState().getNumber() > TLWActivityState.TLW_MATCH.getNumber()
						&& activityInfo.getState().getNumber() < TLWActivityState.TLW_WAR_WAIT.getNumber()) {

					// 锁定阵容前,拉取对手联盟基础数据,不显示人数及战力
					TLWGuildData guildData = RedisProxy.getInstance().getTLWGuildData(oppGuild, season);
					if (guildData != null) {
						TWGuildInfo.Builder oppInfo = TWGuildInfo.newBuilder();
						oppInfo.setId(guildData.getId());
						oppInfo.setName(guildData.getName());
						oppInfo.setTag(guildData.getTag());
						oppInfo.setGuildFlag(guildData.getFlag());
						oppInfo.setServerId(guildData.getServerId());
						oppInfo.setBattlePoint(0);
						oppInfo.setMemberCnt(0);
						oppInfo.setWarStartTime(startTime);
						builder.setOppGuild(oppInfo);
					}
				} else {
					// 锁定阵容后,拉取对手联盟出战数据,显示出战人数及战力
					TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(oppGuild, mark);
					if (guildData != null) {
						TWGuildInfo.Builder oppInfo = guildData.build();
						oppInfo.setWarStartTime(startTime);
						builder.setOppGuild(oppInfo);
					}
				}
			}
		}
		builder.setStateInfo(stateInfo);
		builder.setBigStateInfo(genBigStateInfo());
		builder.setServerType(getServerType());
		return builder;
	}
	
	public TLWStateInfo.Builder genStateInfo(){
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		HawkTuple2<Long, Long> timeInfo = AssembleDataManager.getInstance().getTiberiumSeasonTime(season);
		TLWStateInfo.Builder builder = TLWStateInfo.newBuilder();
		TLWActivityState state = activityInfo.getState();
		if(state == TLWActivityState.TLW_CLOSE){
			builder.setState(TLWState.TLW_NOT_OPEN);
		}
		else{
			builder.setState(TLWState.valueOf(activityInfo.getState().getNumber()));
		}
		builder.setSeason(season);
		builder.setTermId(termId);
		TiberiumSeasonTimeCfg cfg = calcTimeCfg();
		TiberiumSeasonTimeCfg fistCfg = getTimeCfgBySeasonAndTermId(season, 1);
		if(timeInfo!=null && cfg != null && fistCfg!=null){
			builder.setSeasonStartTime(timeInfo.first);
			builder.setSignEndTime(fistCfg.getMatchStartTimeValue());
			builder.setMatchStartTime(cfg.getMatchStartTimeValue());
			builder.setMatchEndTime(cfg.getMatchEndTimeValue());
			builder.setMangeEndTime(cfg.getMatchEndTimeValue());
			builder.setWarStartTime(cfg.getWarStartTimeValue());
			builder.setWarFinishTime(cfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
			builder.setWarEndTime(cfg.getWarEndTimeValue());
			builder.setSeasonEndTime(timeInfo.second);
		}
		builder.setTeamNum(TiberiumConstCfg.getInstance().getTeamCnt());
		builder.setMaxNormalTermNum(TiberiumConstCfg.getInstance().getEliminationStartTermId() - 1);
		builder.setMaxTermNum(TiberiumConstCfg.getInstance().getEliminationFinalTermId());
		return builder;
	}

	public TiberiumWar.TLWBigStateInfo.Builder genBigStateInfo(){
		TiberiumWar.TLWBigState state = TiberiumWar.TLWBigState.TLW_BIG_NOT_OPEN;
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		HawkTuple2<Long, Long> timeInfo = AssembleDataManager.getInstance().getTiberiumSeasonTime(season);
		long now = HawkTime.getMillisecond();
		if(timeInfo != null && now >= timeInfo.first && now <= timeInfo.second){
			TiberiumSeasonTimeCfg fistCfg = getTimeCfgBySeasonAndTermId(season, 1);
			TiberiumSeasonTimeCfg cfg = getTimeCfgBySeasonAndTermId(season, termId);
			if(fistCfg != null && now < fistCfg.getMatchStartTimeValue()){
				state = TiberiumWar.TLWBigState.TLW_BIG_SIGNUP;
			}else if(cfg != null && cfg.getWarType() == TLWWarType.TEAM_WAR){
				state = TiberiumWar.TLWBigState.TLW_BIG_GROUP;
			}else if(cfg != null && cfg.getWarType() == TLWWarType.FINAL_WAR){
				if(termId < TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
					state = TiberiumWar.TLWBigState.TLW_BIG_KICKOUT;
				}else {
					state = TiberiumWar.TLWBigState.TLW_BIG_FINAL;
				}
			}
		}
		TiberiumWar.TLWBigStateInfo.Builder builder = TiberiumWar.TLWBigStateInfo.newBuilder();
		builder.setState(state);
		return builder;
	}
	
	/**
	 * 构建小组联盟列表
	 * @param teamId
	 * @param player
	 * @return
	 */
	public TLWGetTeamGuildInfoResp.Builder getTeamGuildInfo(int teamId, Player player, TiberiumWar.TLWServer serverType) {
		int season = activityInfo.getSeason();
		TLWGetTeamGuildInfoResp.Builder builder = TLWGetTeamGuildInfoResp.newBuilder();
		builder.setTeamId(teamId);
		Map<String, TLWGuildJoinInfo> infoMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		if (infoMap.isEmpty()) {
			return builder;
		}
		if (activityInfo.state == TLWActivityState.TLW_CLOSE || activityInfo.state == TLWActivityState.TLW_NOT_OPEN) {
			player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_GET_ZONE_GUILD_INFO_C_VALUE, Status.Error.TIBERIUM_LEAGUA_SEASON_NOT_OPEN, 0);
			return null;
		}
		List<TLWGuildJoinInfo> guildList = infoMap.values().stream().filter(t -> t.getTeamId() == teamId && t.getServerType() == serverType.getNumber()).collect(Collectors.toList());
		Collections.sort(guildList, new TLWGuildJoinInfoTeamGroupComparator());
		List<String> guildIds = new ArrayList<>();
		for(TLWGuildJoinInfo info : guildList){
			guildIds.add(info.getId());
		}
		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		int rank = 1;
		for (TLWGuildJoinInfo joinInfo : guildList) {
			String guildId = joinInfo.getId();
			TLWGuildData guildData = dataMap.get(guildId);
			TLWGuildBaseInfo.Builder baseInfo = guildData.genBaseInfo();
			TLWTeamGuildInfo.Builder infoBuilder = TLWTeamGuildInfo.newBuilder();
			infoBuilder.setBaseInfo(baseInfo);
			infoBuilder.setTeamId(joinInfo.getTeamId());
			infoBuilder.setBattlePoint(guildData.getPower());
			infoBuilder.setWinCnt(joinInfo.getWinCnt());
			infoBuilder.setLoseCnt(joinInfo.getLoseCnt());
			infoBuilder.setScore((int) joinInfo.getScore());
			infoBuilder.setGroup(TLWGroup.valueOf(joinInfo.getGroup().getNumber()));
			infoBuilder.setRank(rank);
			infoBuilder.setIsSeed(joinInfo.isSeed());
			builder.addGuildInfo(infoBuilder);
			rank++;
		}
		builder.setTeamId(teamId);
		return builder;
	}
	
	/**
	 * 拉取赛程信息 
	 * @param teamId
	 * @param termId
	 * @param player
	 * @return
	 */
	public TLWGetMatchInfoResp.Builder getTLWMatchInfo(TLWGetMatchInfoReq req, Player player) {
		int termId = req.getTermId();
		int season = activityInfo.getSeason();
		int mark = combineSeasonTerm(season, termId);
		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(mark);
		TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
		if (timeCfg == null) {
			player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_GET_MATCH_INFO_C_VALUE, Status.Error.TIBERIUM_LEAGUA_TERMID_ERROR, 0);
			return null;
		}
		TiberiumWar.TLWServer serverType = req.getServer();
		TLWGetMatchInfoResp.Builder builder = TLWGetMatchInfoResp.newBuilder();
		builder.setTermId(termId);
		builder.setWarTime(timeCfg.getWarStartTimeValue());
		List<TWRoomData> filterList = new ArrayList<>();
		if (termId >= TiberiumConstCfg.getInstance().getEliminationStartTermId()) {
			filterList = roomList.stream().filter(e -> e.getGroup().getNumber() == req.getGroup().getNumber() && e.getServerType() == serverType.getNumber()).collect(Collectors.toList());
			builder.setGroup(req.getGroup());
		} else {
			filterList = roomList.stream().filter(e -> e.getTeamId() == req.getTeamId() && e.getServerType() == serverType.getNumber()).collect(Collectors.toList());
			builder.setTeamId(req.getTeamId());
			builder.setGroup(TLWGroup.TEAM_GROUP);
		}
		// 淘汰赛阶段,若是请求当前期数的匹配阶段的对战数据,屏蔽不显示
		if (termId >= TiberiumConstCfg.getInstance().getEliminationStartTermId() &&
				termId == activityInfo.getTermId() && 
				activityInfo.getState() == TLWActivityState.TLW_MATCH) {
			return builder;
		}
		// 赛季第一期,匹配阶段结束前不显示赛程信息
		if(termId == 1 && HawkTime.getMillisecond()<= timeCfg.getMatchEndTimeValue()){
			return builder;
		}
		
		List<String> guildIds = new ArrayList<>();
		for (TWRoomData roomData : filterList) {
			guildIds.add(roomData.getGuildA());
			guildIds.add(roomData.getGuildB());
		}
		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		for (TWRoomData roomData : filterList) {
			TLWGuildData guildA = dataMap.get(roomData.getGuildA());
			TLWGuildData guildB = dataMap.get(roomData.getGuildB());
			TLWBattleType battleType = roomData.getBattleType();
			TLWGetMatchInfo.Builder matchInfo = TLWGetMatchInfo.newBuilder();
			matchInfo.setRoomId(roomData.getId());
			matchInfo.setGuildA(guildA.genBaseInfo());
			matchInfo.setGuildB(guildB.genBaseInfo());
			matchInfo.setBattleType(TLWBattle.valueOf(battleType.getValue()));
			if (!HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
				matchInfo.setWinnerId(roomData.getWinnerId());
			}
			builder.addMatchInfo(matchInfo);
		}
		
		//如果是最后一期
		if(termId >= TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
			int lastMark = combineSeasonTerm(season, termId -1);
			List<TWRoomData> lastRoomList = RedisProxy.getInstance().getAllTWRoomData(lastMark);
			this.genFinalGuilds(season,termId, roomList,lastRoomList,TLWGroup.S_GROUP_VALUE,builder, serverType);
			this.genFinalGuilds(season,termId, roomList,lastRoomList,TLWGroup.A_GROUP_VALUE,builder, serverType);
			//this.genFinalGuilds(season,termId, roomList,lastRoomList,TLWGroup.B_GROUP_VALUE,builder, serverType);
			
		}
		return builder;
	}
	
	
	private void genFinalGuilds(int season,int termId,List<TWRoomData> roomList,List<TWRoomData> lastRoomList,int group,TLWGetMatchInfoResp.Builder builder, TiberiumWar.TLWServer serverType){
		if(termId != TiberiumConstCfg.getInstance().getEliminationFinalTermId()){
			return;
		}
		if(roomList.isEmpty()){
			return;
		}
		if(lastRoomList.isEmpty()){
			return;
		}
		List<TWRoomData> curFilterList = roomList.stream()
				.filter(e -> e.getGroup().getNumber() == group && e.getServerType() == serverType.getNumber()).collect(Collectors.toList());
		if(curFilterList.size() != 1){
			return;
		}
		List<TWRoomData> lastFilterList = lastRoomList.stream()
				.filter(e -> e.getGroup().getNumber() == group && e.getServerType() == serverType.getNumber()).collect(Collectors.toList());
		if(lastFilterList.size() != 1){
			return;
		}
		
		TWRoomData finalRoomData = curFilterList.get(0);
		String winnerId = finalRoomData.getWinnerId();
		//上一期
		TWRoomData lastRoomData = lastFilterList.get(0);
		String lastWinnerId = lastRoomData.getWinnerId();
		//三个联盟
		List<String> guildIds = new ArrayList<>();
		String guildA = finalRoomData.getGuildA();
		String guildB = finalRoomData.getGuildB();
		String guildC = lastRoomData.getOppGuildId(lastWinnerId);
		guildIds.add(guildA);
		guildIds.add(guildB);
		guildIds.add(guildC);
		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		TLWGuildData guildDataA = dataMap.get(guildA);
		TLWGuildData guildDataB = dataMap.get(guildB);
		TLWGuildData guildDataC = dataMap.get(guildC);
		
		TLWFinalMatchMatchInfo.Builder fbuilder = TLWFinalMatchMatchInfo.newBuilder();
		fbuilder.setRoomId(finalRoomData.getId());
		fbuilder.setGuildA(guildDataA.genBaseInfo());
		fbuilder.setGuildB(guildDataB.genBaseInfo());
		fbuilder.setGuildC(guildDataC.genBaseInfo());
		fbuilder.setBattleType(TLWBattle.valueOf(finalRoomData.getBattleType().getValue()));
		if (!HawkOSOperator.isEmptyString(winnerId)) {
			fbuilder.setWinnerId(winnerId);
		}
		fbuilder.setGroup(TLWGroup.valueOf(group));
		builder.addFinalMatchInfo(fbuilder);
	}
	
	
	/**
	 * 获取决赛对阵信息
	 * @param player
	 * @return
	 */
	public TLWGetFinalMatchInfoResp.Builder getTLWFinalInfo(Player player, TLWGroup group, TiberiumWar.TLWServer serverType) {
		TLWGetFinalMatchInfoResp.Builder builder = TLWGetFinalMatchInfoResp.newBuilder();
		int season = activityInfo.getSeason();
		TLWGroupType groupType = TLWGroupType.getType(group.getNumber());
		TLWEliminationGroup groupData = RedisProxy.getInstance().getTLWEliminationGroupInfo(season, groupType);
		builder.setGroup(group);
		if (groupData == null) {
			return builder;
		}
		List<String> guildIds = groupData.getGuildIds();
		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		Map<Integer, List<TLWBattleData>> battleMap = groupData.getBattleMap();
		for (Entry<Integer, List<TLWBattleData>> entry : battleMap.entrySet()) {
			TLWFinalMatchStageInfo.Builder stageBuilder = TLWFinalMatchStageInfo.newBuilder();
			int stage = entry.getKey();
			// 若是请求当前期数的匹配阶段的对战数据,屏蔽不显示
			if (stage == activityInfo.getTermId() && activityInfo.getState() == TLWActivityState.TLW_MATCH) {
				continue;
			}
			
			List<TLWBattleData> dataList = entry.getValue();
			stageBuilder.setTermId(stage);
			for (TLWBattleData battleData : dataList) {
				if(battleData.getServerType() != serverType.getNumber()){
					continue;
				}
				TLWGetMatchInfo.Builder matchBuilder = TLWGetMatchInfo.newBuilder();
				TLWGuildData guildA = dataMap.get(battleData.getGuildA());
				TLWGuildData guildB = dataMap.get(battleData.getGuildB());
				TLWBattleType battleType = battleData.getBattleType();
				matchBuilder.setGuildA(guildA.genBaseInfo());
				matchBuilder.setGuildB(guildB.genBaseInfo());
				matchBuilder.setPos(battleData.getPosIndex());
				matchBuilder.setRoomId(battleData.getRoomId());
				matchBuilder.setBattleType(TLWBattle.valueOf(battleType.getValue()));
				String winnerId = battleData.getWinnerGuild();
				if (!HawkOSOperator.isEmptyString(winnerId)) {
					matchBuilder.setWinnerId(winnerId);
				}
				stageBuilder.addMatchInfo(matchBuilder);
			}
			builder.addStageInfo(stageBuilder);
		}
		return builder;
	}
	
	/**
	 * 获取联赛积分和奖励信息
	 * @param player
	 * @return
	 */
	public TLWGetScoreInfoResp.Builder getTLWScoreRewardInfo(Player player) {

		TLWGetScoreInfoResp.Builder builder = TLWGetScoreInfoResp.newBuilder();
		int season = activityInfo.getSeason();
		if (season <= 0) {
			return builder;
		}
		TLWScoreData selfData = RedisProxy.getInstance().getTLWPlayerScoreInfo(season, player.getId());
		TLWScoreInfo.Builder selfBuilder = TLWScoreInfo.newBuilder();
		Set<Integer> selfRewarded = selfData.getRewardedList();
		Set<Integer> guildRewarded = selfData.getGuildRewardeds();
		selfBuilder.setScore(selfData.getScore());
		if(!selfRewarded.isEmpty()){
			selfBuilder.addAllRewardedId(selfRewarded);
		}
		builder.setSelfScore(selfBuilder);
		if (player.hasGuild()) {
			TLWScoreData guildData = RedisProxy.getInstance().getTLWGuildScoreInfo(season, player.getGuildId());
			TLWScoreInfo.Builder guildBuilder = TLWScoreInfo.newBuilder();
			guildBuilder.setScore(guildData.getScore());
			if (!guildRewarded.isEmpty()) {
				guildBuilder.addAllRewardedId(guildRewarded);
			}
			builder.setGuildScore(guildBuilder);
		}
		return builder;
	}

	/**
	 * 获取当前所在赛季
	 * @return
	 */
	private int calcCurrSeason() {
		long now = HawkTime.getMillisecond();
		int season = -1;
		ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		for (TiberiumSeasonTimeCfg cfg : its) {
			long startTime = cfg.getSeasonStartTimeValue();
			long endTime = cfg.getSeasonEndTimeValue();
			if (startTime > 0 && now >= startTime) {
				season = cfg.getSeason();
			}
			if (endTime > 0 && now >= endTime) {
				season = -1;
			}
		}
		return season;
	}
	
	/**
	 * 获取当前时间配置
	 * @return
	 */
	private TiberiumSeasonTimeCfg calcTimeCfg() {
		long now = HawkTime.getMillisecond();
		int season = calcCurrSeason();
		if (season == -1) {
			return null;
		}
		ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		for (TiberiumSeasonTimeCfg cfg : its) {
			if (cfg.getSeason() != season) {
				continue;
			}
			long warEndTime = cfg.getWarEndTimeValue();
			if (now < warEndTime) {
				return cfg;
			}
			long endTime = cfg.getSeasonEndTimeValue();
			if (endTime > 0 && now < endTime) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 获取当前阶段的联赛时间配置
	 * @return
	 */
	public TiberiumSeasonTimeCfg getCurrTimeCfg(){
		return getTimeCfgBySeasonAndTermId(activityInfo.season, activityInfo.termId);
	}
	
	/**
	 * 获取指定赛季和期数的时间配置
	 * @return
	 */
	public TiberiumSeasonTimeCfg getTimeCfgBySeasonAndTermId(int season, int termId) {
		ConfigIterator<TiberiumSeasonTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonTimeCfg.class);
		for (TiberiumSeasonTimeCfg cfg : its) {
			if (cfg.getSeason() == season && cfg.getTermId() == termId) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 计算联盟可领取奖励
	 * @param scoreData
	 */
	public void calcGuildAddReward(TLWScoreData scoreData){
		long score = scoreData.getScore();
		Set<Integer> rewardedList = scoreData.getRewardedList();
		ConfigIterator<TiberiumSeasonGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonGuildAwardCfg.class);
		for(TiberiumSeasonGuildAwardCfg cfg : its){
			int id=cfg.getId();
			if(score>=cfg.getScore()){
				rewardedList.add(id);
			}
		}
	}
	
	/**
	 * 获取玩家能领取的个人奖励列表
	 * @param scoreData
	 * @return
	 */
	public List<Integer> calcSelfAddReward(TLWScoreData scoreData) {
		long score = scoreData.getScore();
		List<Integer> canGetList = new ArrayList<>();
		Set<Integer> rewardedList = scoreData.getRewardedList();
		ConfigIterator<TiberiumSeasonPersonAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonPersonAwardCfg.class);
		for (TiberiumSeasonPersonAwardCfg cfg : its) {
			int id = cfg.getId();
			if (score >= cfg.getScore() && !rewardedList.contains(id)) {
				canGetList.add(id);
			}
		}
		return canGetList;
	}
	
	/**
	 * 判定玩家是否在参与泰伯利亚联赛
	 * @param player
	 * @return
	 */
	public boolean isJointLeaguaWar(Player player) {
		if (activityInfo.season <= 0) {
			return false;
		}
		if (!player.hasGuild()) {
			return false;
		}
		String guildId = player.getGuildId();
		return isJointLeaguaWar(guildId);
	}
	
	/**
	 * 判定玩家是否在参与泰伯利亚联赛
	 * @param player
	 * @return
	 */
	public boolean isJointLeaguaWar(String guildId) {
		if(HawkOSOperator.isEmptyString(guildId)){
			return false;
		}
		
		if (activityInfo.season <= 0) {
			return false;
		}
		if (!currGuildGroup.containsKey(guildId)) {
			return false;
		}
		TLWGroupType type = currGuildGroup.get(guildId);
		if (type == TLWGroupType.NORMAL || type == TLWGroupType.KICK_OUT) {
			return false;
		}
		return true;
	}

	public TLWGetOBRoomInfo.Builder getTLWRoomInfo() {
		TLWGetOBRoomInfo.Builder builder = TLWGetOBRoomInfo.newBuilder();
		int season = activityInfo.getSeason();
		int termId = activityInfo.getTermId();
		builder.setSeason(season);
		builder.setTermId(termId);
		TiberiumSeasonTimeCfg cfg = getCurrTimeCfg();
		if(cfg!=null){
			builder.setWarStartTime(cfg.getWarStartTimeValue());
			builder.setWarFinishTime(cfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
		}
		for (TLWGroupType groupType : TiberiumConst.FINAL_WAR_GROUPS) {
			TLWEliminationGroup groupData = RedisProxy.getInstance().getTLWEliminationGroupInfo(season, groupType);
			if (groupData == null) {
				continue;
			}
			List<String> guildIds = groupData.getGuildIds();
			Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
			Map<Integer, List<TLWBattleData>> battleMap = groupData.getBattleMap();
			for (Entry<Integer, List<TLWBattleData>> entry : battleMap.entrySet()) {
				int stage = entry.getKey();

				// 只显示当前期数匹配完成后的对战列表
				if (stage != activityInfo.getTermId() || fightState.getState() != FightState.OPEN) {
					continue;
				}

				List<TLWBattleData> dataList = entry.getValue();
				for (TLWBattleData battleData : dataList) {
					TLWGetMatchInfo.Builder matchBuilder = TLWGetMatchInfo.newBuilder();
					TLWGuildData guildA = dataMap.get(battleData.getGuildA());
					TLWGuildData guildB = dataMap.get(battleData.getGuildB());
					matchBuilder.setGuildA(guildA.genBaseInfo());
					matchBuilder.setGuildB(guildB.genBaseInfo());
					TLWRoomInfo.Builder obRoomInfo = TLWRoomInfo.newBuilder();
					obRoomInfo.setRoomId(battleData.getRoomId());
					obRoomInfo.setRoomServer(guildA.getServerId());
					obRoomInfo.setMatchInfo(matchBuilder);
					builder.addRoomInfo(obRoomInfo);
				}
			}
		}
		return builder;
	}
	
	/**获取联盟的参战成员数量和总战力
	 * @param guildId
	 * @param termId
	 * @return
	 */
	public HawkTuple3<Long, Integer, String> getTWGuildData(String guildId){
		TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, activityInfo.getMark());
		if (guildData == null) {
			return null;
		}
		HawkTuple3<Long, Integer, String> data = new HawkTuple3<Long, Integer, String>(guildData.getTotalPower(), guildData.getMemberCnt(), guildData.getName());
		return data;
	}
	
	/**判断两队是否是一组
	 * @param guildIdA
	 * @param guildIdB
	 * @return
	 */
	public boolean isSameRoom(String guildIdA, String guildIdB){
		if (roomList == null) {
			return false;
		}
		for (TWRoomData twRoomData : roomList) {
			String idA = twRoomData.getGuildA();
			String idB = twRoomData.getGuildB();
			if ((guildIdA.equals(idA) && guildIdB.equals(idB)) || (guildIdA.equals(idB) && guildIdB.equals(idA)) ) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 拉取刷新泰伯利亚联赛正赛联盟排行信息
	 */
	private void loadAndRefreshRank() {
		int season = activityInfo.season;
		Map<String, TLWGuildJoinInfo> guildMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		List<TLWGuildJoinInfo> guildList = new ArrayList<>(guildMap.values());
		List<String> guildIds = new ArrayList<>(guildMap.keySet());
		Map<String, TLWGuildData> dataMap = RedisProxy.getInstance().getTLWGuildDatas(season, guildIds);
		Collections.sort(guildList, new TLWGuildJoinInfoSeanRankComparator());
		/******************************总排行*********************************/
		int rank = 1;
		int newRank = 1;
		Map<String, String> rankMap = new HashMap<>();
		List<String> rankInfoList = new ArrayList<>();
		for(TLWGuildJoinInfo joinInfo : guildList){
			String guildId = joinInfo.getId();
			TLWGuildData guildData = dataMap.get(guildId);
			long guildIdNum = HawkUUIDGenerator.strUUID2Long(guildId);
			if(joinInfo.getServerType() == TiberiumWar.TLWServer.TLW_NEW_SERVER_VALUE){
				rankMap.put(guildId, String.valueOf(newRank));
				StringBuilder sb = new StringBuilder();
				sb.append(newRank).append("\t")
						.append(guildIdNum).append("\t")
						.append(guildId).append("\t")
						.append(guildData.getName()).append("\t")
						.append(guildData.getLeaderName()).append("\t")
						.append(guildData.getServerId()).append("\t")
						.append(joinInfo.getTeamId()).append("\t")
						.append(joinInfo.getKickOutTerm()).append("\t")
						.append(joinInfo.getLastestPower()).append("\t")
						.append("\r\n");
				rankInfoList.add(sb.toString());
				newRank++;
			}else {
				rankMap.put(guildId, String.valueOf(rank));
				StringBuilder sb = new StringBuilder();
				sb.append(rank).append("\t")
						.append(guildIdNum).append("\t")
						.append(guildId).append("\t")
						.append(guildData.getName()).append("\t")
						.append(guildData.getLeaderName()).append("\t")
						.append(guildData.getServerId()).append("\t")
						.append(joinInfo.getTeamId()).append("\t")
						.append(joinInfo.getKickOutTerm()).append("\t")
						.append(joinInfo.getLastestPower()).append("\t")
						.append("\r\n");
				rankInfoList.add(sb.toString());
				rank++;
			}
		}
		/******************************总排行*********************************/
		// 存储联赛所有参与正赛联盟的总排行信息
		RedisProxy.getInstance().updateTLWGuildTotalRank(rankMap, season);
		for(String rankInfo : rankInfoList){
			HawkLog.logPrintln("TiberiumLeaguaWarService loadAndRefreshRank, info:{}", rankInfo);
		}
	}
	
	/**
	 * 发送泰伯利亚联赛正赛联盟排行奖励
	 */
	private void sendTLWRankReward() {
		// 合服Map<主服,从服> 
		Map<String, String> mergerMap = new HashMap<>();
		
		ConfigIterator<MergeServerTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		long currTime = HawkTime.getMillisecond();
		for (MergeServerTimeCfg cfg : its) {
			// 还未合服以及2020/3/24之前合服的服务器不作处理
			if (cfg.getMergeTimeValue() > currTime || cfg.getMergeTimeValue() <= 1584979200000l) {
				continue;
			}
			List<String> mergeServerList = cfg.getMergeServerList();
			if(mergeServerList == null){
				HawkLog.logPrintln("sendTLWRankRewardError mergeInfoNull, id:{}", cfg.getId());
				continue;
			}
			else if(mergeServerList.size() == 2){
				mergerMap.put(mergeServerList.get(0), mergeServerList.get(1));
			}
			else if(mergeServerList.size() == 4){
				mergerMap.put(mergeServerList.get(0), mergeServerList.get(2));
			}
			else{
				HawkLog.logPrintln("sendTLWRankRewardError mergeInfoError, id:{}", cfg.getId());
				continue;
			}
		}
		int season = activityInfo.getSeason();
		String serverId = GsConfig.getInstance().getServerId();
		Map<String, TLWGuildJoinInfo> guildMap = RedisProxy.getInstance().getAllTLWJoinGuild(season);
		List<TLWGuildJoinInfo> serverGuildList = guildMap.values().stream().filter(t -> t.getServerId().equals(serverId)).collect(Collectors.toList());
		if (mergerMap.containsKey(serverId)) {
			String followServer = mergerMap.get(serverId);
			List<TLWGuildJoinInfo> followList = guildMap.values().stream().filter(t -> t.getServerId().equals(followServer)).collect(Collectors.toList());
			if (followList != null && !followList.isEmpty()) {
				serverGuildList.addAll(followList);
			}
		}
		Map<String, String> totalRankMap = RedisProxy.getInstance().getTLWGuildTotalRank(season);
		for(TLWGuildJoinInfo joinInfo : serverGuildList){
			Map<String, String> rewardMap = new HashMap<>();
			String guildId = joinInfo.getId();
			Map<String, String> members = RedisProxy.getInstance().getTLWMemberIds(season, guildId);
			if (members.isEmpty()) {
				HawkLog.logPrintln("sendTLWRankRewardFailed, members not exist, guildId: {}", guildId);
				continue;
			}
			String totalRankStr = totalRankMap.get(guildId);
			
			// 排行信息异常 
			if(HawkOSOperator.isEmptyString(totalRankStr)){
				HawkLog.logPrintln("sendTLWRankRewardFailed, rankError, guildId: {}, totalRank: {}", guildId, totalRankStr);
				continue;
			}
			int totalRank = Integer.valueOf(totalRankStr);
			TiberiumSeasonRankAwardCfg cfg = getTLWRankAwardCfg(totalRank, joinInfo.getServerType());
			
			for (Entry<String, String> entry : members.entrySet()) {
				String playerId = entry.getKey();
				boolean hasSend = Boolean.valueOf(entry.getValue());
				if (hasSend) {
					HawkLog.logPrintln("sendTLWRankRewardFailed, already send, playerId: {}, guildId: {}, totalRank: {}", playerId, guildId, totalRankStr);
					continue;
				}
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.TIBERIUM_LEAGUA_RANK_REWARD)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(totalRankStr)
						.setRewards(cfg.getRewardList())
						.build());
				rewardMap.put(playerId, String.valueOf(true));
				HawkLog.logPrintln("sendTLWRankRewardSuccess, playerId: {}, guildId: {}, totalRank: {}", playerId, guildId, totalRank);
			}
			if (!rewardMap.isEmpty()) {
				RedisProxy.getInstance().updateTLWMemberIds(guildId, rewardMap, season);
			}
			GuildService.getInstance().addRewardFlag(guildId, cfg.getAllianceFlagId());
		}
		Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
		if (!opActivity.isPresent()) {
			return;
		}
		SeasonActivity activity = opActivity.get();
		for(TLWGuildJoinInfo joinInfo : serverGuildList){
			String guildId = joinInfo.getId();
			if (GuildService.getInstance().getGuildInfoObject(guildId) == null) {
				continue;
			}
			String totalRankStr = totalRankMap.get(guildId);
			// 排行信息异常
			if(HawkOSOperator.isEmptyString(totalRankStr)){
				HawkLog.logPrintln("sendTLWRankRewardFailed, rankError, guildId: {}, totalRank: {}", guildId, totalRankStr);
				continue;
			}
			int totalRank = Integer.valueOf(totalRankStr);
			if(joinInfo.getServerType() == TiberiumWar.TLWServer.TLW_OLD_SERVER_VALUE){
				activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_TBLY, guildId, totalRank);
			}
		}
	}

	/**
	 * 根据排行获取泰伯利亚联赛奖励配置
	 * @param rank
	 * @return
	 */
	private TiberiumSeasonRankAwardCfg getTLWRankAwardCfg(int rank, int type) {
		ConfigIterator<TiberiumSeasonRankAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumSeasonRankAwardCfg.class);
		for (TiberiumSeasonRankAwardCfg cfg : its) {
			if(cfg.getType() != type){
				continue;
			}
			int rankLow = cfg.getRankRange().first;
			int rankHight = cfg.getRankRange().second;
			if (rank >= rankLow && rank <= rankHight) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 小组单循环赛算法
	 * @param teamNum
	 * @return
	 */
	public Map<Integer, List<HawkTuple2<Integer, Integer>>> calcSingleCycleMap(int teamNum) {
		Map<Integer, List<HawkTuple2<Integer, Integer>>> matchMap = new HashMap<>();
		List<List<HawkTuple2<Integer, Integer>>> shuffleList = new ArrayList<>();
		int[] teams = new int[teamNum];
		for (int i = 0; i < teams.length; i++) {
			teams[i] = i;
		}
		for (int i = 1; i < teamNum; i++) {
			List<HawkTuple2<Integer, Integer>> matchList = new ArrayList<>();
			for (int j = 0; j < teamNum / 2; j++) {
				matchList.add(new HawkTuple2<Integer, Integer>(teams[j], teams[teamNum - 1 - j]));
			}
			shuffleList.add(matchList);
			int temp = teams[teamNum - 1];
			for (int k = teamNum - 1; k > 0; k--) {
				teams[k] = teams[k - 1];
			}
			teams[1] = temp;
		}
		Collections.shuffle(shuffleList);
		for (int i = 0; i < teamNum-1; i++) {
			matchMap.put(i + 1, shuffleList.get(i));
		}
		return matchMap;
	}
	
	/**
	 * 获取本盟的对战列表
	 * @param player
	 * @return
	 */
	public TLWSelfMatchList.Builder getSelfMatchInfo(Player player) {
		TLWSelfMatchList.Builder builder = TLWSelfMatchList.newBuilder();
		if (!player.hasGuild()) {
			return builder;
		}
		String guildId = player.getGuildId();
		if(!currGuildGroup.containsKey(guildId)){
			return builder;
		}
		int season = activityInfo.getSeason();
		int curTermId = activityInfo.getTermId();
		TLWGuildJoinInfo guildInfo = RedisProxy.getInstance().getTLWJoinGuild(season, guildId);
		if(guildInfo == null){
			return builder;
		}
		int endTermId = curTermId;
		if(endTermId < TiberiumConstCfg.getInstance().getEliminationStartTermId()){
			endTermId = TiberiumConstCfg.getInstance().getEliminationStartTermId() -1;
		}
		for(int termId=1;termId<= endTermId;termId++){
			TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
			List<TWRoomData> roomList = matchedRoomMap.get(termId);
			//如果没有房间信息，说明是当前期还没有完成匹配
			if(roomList == null){
				continue;
			}
			//找联盟对战
			TWRoomData roomData = null;
			for(TWRoomData room : roomList){
				if(room.getGuildA().equals(guildId) || room.getGuildB().equals(guildId)){
					roomData = room;
					break;
				}
			}
			if(roomData != null){
				TLWRoomInfo.Builder roomInfo = TLWRoomInfo.newBuilder();
				roomInfo.setRoomId(roomData.getId());
				roomInfo.setRoomServer(roomData.getRoomServerId());
				roomInfo.setTermId(termId);
				roomInfo.setWarStartTime(timeCfg.getWarStartTimeValue());
				roomInfo.setWarFinishTime(timeCfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
				TLWGetMatchInfo.Builder matchInfo = TLWGetMatchInfo.newBuilder();
				matchInfo.setRoomId(roomData.getId());
				matchInfo.setBattleType(TLWBattle.valueOf(roomData.getBattleType().getValue()));
				TLWGuildData guildA = RedisProxy.getInstance().getTLWGuildData(roomData.getGuildA(), season);
				TLWGuildData guildB = RedisProxy.getInstance().getTLWGuildData(roomData.getGuildB(), season);
				matchInfo.setGuildA(guildA.genBaseInfo());
				matchInfo.setGuildB(guildB.genBaseInfo());
				if(!HawkOSOperator.isEmptyString(roomData.getWinnerId())){
					matchInfo.setWinnerId(roomData.getWinnerId());
				}
				roomInfo.setMatchInfo(matchInfo);
				builder.addRoomInfo(roomInfo);
				continue;
			}
			//如果没有对战信息,胜者组免战或者被淘汰
			int kickOutTerm = guildInfo.getKickOutTerm();
			if(kickOutTerm > termId){
				TLWFreeRoom.Builder freeBuilder = TLWFreeRoom.newBuilder();
				freeBuilder.setTermId(termId);
				freeBuilder.setWarStartTime(timeCfg.getWarStartTimeValue());
				freeBuilder.setWarFinishTime(timeCfg.getWarStartTimeValue() + TiberiumConstCfg.getInstance().getWarOpenTime());
				builder.addFreeInfos(freeBuilder);
			}
		}
		return builder;
	}
	
	public boolean checkMemberJoin(String playerId, String guildId){
		try {
			if(activityInfo == null){
				return true;
			}
			int season = activityInfo.getSeason();
			int termId = activityInfo.getTermId();
			TiberiumSeasonTimeCfg timeCfg = getTimeCfgBySeasonAndTermId(season, termId);
			if(timeCfg == null){
				return true;
			}
			if(!timeCfg.winGroupFree()){
				return true;
			}
			int mark = TiberiumLeagueWarService.getInstance().combineSeasonTerm(season, termId - 1);
			TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(playerId, mark);
			if(twPlayerData == null){
				return true;
			}
			if(twPlayerData.getEnterTime() <= 0){
				return true;
			}
			TLWGroupType group = currGuildGroup.get(twPlayerData.getGuildId());
			if(group == null || group == TLWGroupType.KICK_OUT){
				return true;
			}
			if(guildId.equals(twPlayerData.getGuildId())){
				return true;
			}
			return false;
		}catch (Exception e){
			HawkException.catchException(e);
			return true;
		}
	}

	public void newSignupOld(Player player){
		String guildId = player.getGuildId();
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		GuildInfoObject guildInfoObject = GuildService.getInstance().getGuildInfoObject(guildId);
		if(guildInfoObject == null){
			return;
		}
		if(!guildInfoObject.isLeader(player.getId())){
			player.sendError(HP.code.TIBERIUM_LEAGUA_WAR_SIGN_UP_REQ_VALUE, Status.Error.TIBERIUM_LEAGUA_LEADER_SIGNUP_OLD_VALUE, 0);
			return;
		}
		TiberiumWar.TLWServer type = getServerType();
		if(type != TiberiumWar.TLWServer.TLW_NEW_SERVER){
			return;
		}
		int season = activityInfo.getSeason();
		RedisProxy.getInstance().addTLWNewSignupOld(season, guildId);
		RedisProxy.getInstance().addTLWNewSignupOldServer(season, guildId);
		// 同步活动信息
		TLWPageInfo.Builder pageInfo = genPageInfo(player, TiberiumWar.TLWServer.TLW_OLD_SERVER);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_LEAGUA_WAR_INFO_SYNC, pageInfo));
		LogUtil.logTimberiumLeaguaNewSignupOld(player.getId(), guildId);
	}
}
