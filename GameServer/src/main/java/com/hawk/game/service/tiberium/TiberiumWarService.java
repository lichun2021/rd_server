package com.hawk.game.service.tiberium;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hawk.game.invoker.guildBackActivity.GuildBackDropInvoker;
import com.hawk.game.service.ActivityService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventTiberiumWar;
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
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.TWScoreEvent;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.config.TiberiumAppointMatchCfg;
import com.hawk.game.config.TiberiumConstCfg;
import com.hawk.game.config.TiberiumGuildAwardCfg;
import com.hawk.game.config.TiberiumPersonAwardCfg;
import com.hawk.game.config.TiberiumSeasonGuildAwardCfg;
import com.hawk.game.config.TiberiumSeasonPersonAwardCfg;
import com.hawk.game.config.TiberiumTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.tiberium.TWMemberQuitInvoker;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.lianmengtaiboliya.TBLYExtraParam;
import com.hawk.game.module.lianmengtaiboliya.TBLYRoomManager;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg;
import com.hawk.game.module.lianmengtaiboliya.msg.TBLYBilingInformationMsg.PlayerGameRecord;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.SoldierType;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.TiberiumWar;
import com.hawk.game.protocol.TiberiumWar.TLWGetMatchInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGetOBRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TLWGuildBaseInfo;
import com.hawk.game.protocol.TiberiumWar.TLWRoomInfo;
import com.hawk.game.protocol.TiberiumWar.TWBattleLog;
import com.hawk.game.protocol.TiberiumWar.TWGetTeamSummaryResp;
import com.hawk.game.protocol.TiberiumWar.TWGuildInfo;
import com.hawk.game.protocol.TiberiumWar.TWGuildTeamInfo;
import com.hawk.game.protocol.TiberiumWar.TWMemberMangeType;
import com.hawk.game.protocol.TiberiumWar.TWPageInfo;
import com.hawk.game.protocol.TiberiumWar.TWPlayerInfo;
import com.hawk.game.protocol.TiberiumWar.TWPlayerList;
import com.hawk.game.protocol.TiberiumWar.TWPlayerManage;
import com.hawk.game.protocol.TiberiumWar.TWState;
import com.hawk.game.protocol.TiberiumWar.TWStateInfo;
import com.hawk.game.protocol.TiberiumWar.TWTeamInfo;
import com.hawk.game.protocol.TiberiumWar.TWTeamManageInfoResp;
import com.hawk.game.protocol.TiberiumWar.TWTeamMember;
import com.hawk.game.protocol.TiberiumWar.TeamSummary;
import com.hawk.game.protocol.TiberiumWar.WarTimeChoose;
import com.hawk.game.rank.RankObject;
import com.hawk.game.rank.RankService;
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
import com.hawk.game.service.tiberium.TiberiumConst.TLWGroupType;
import com.hawk.game.service.tiberium.TiberiumConst.TWActivityState;
import com.hawk.game.service.tiberium.logunit.TWEloScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWGuildScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWLogUtil;
import com.hawk.game.service.tiberium.logunit.TWPlayerScoreLogUnit;
import com.hawk.game.service.tiberium.logunit.TWSelfRewardLogUnit;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.LogConst.LogMsgType;

/**
 * 泰伯利亚联赛服务类
 * @author Jesse
 */
public class TiberiumWarService extends HawkAppObj {
	
	/**
	 * 全局实例对象
	 */
	private static TiberiumWarService instance = null;
	
	/**
	 * 活动时间信息数据
	 */
	public static TWActivityData activityInfo = new TWActivityData();
	
	/**
	 * 匹配状态信息
	 */
	public static TWMatchState matchServerInfo = new TWMatchState();
	/**
	 * 战斗阶段状态信息
	 */
	public static TWFightState fightState = new TWFightState();
	/**
	 * 已报名联盟信息
	 */
	public static Map<String, Integer> signGuild = new ConcurrentHashMap<>();
	
	/**
	 * 联盟小组构建信息
	 */
	public static Map<String, TWGuildTeamInfo> tiberiumGuildTeams = new ConcurrentHashMap<>(); 
	
	/**
	 * 联盟小组信息
	 */
	public static Map<String, TWGuildTeamData> guildTeamDatas = new ConcurrentHashMap<>();
	
	/**
	 * 待更新联盟列表
	 */
	public static Set<String> needUpdateGuildIds = new HashSet<>();
	
	/**
	 * 联盟小组构建后信息
	 */
	public static Map<String, TWGuildTeamInfo> guildTeamInfos = new ConcurrentHashMap<>();
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static TiberiumWarService getInstance() {
		return instance;
	}

	public TiberiumWarService(HawkXID xid) {
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
			activityInfo = RedisProxy.getInstance().getTWActivityInfo();
			
			// 进行阶段检测
			checkStateChange();
			// 活动如果处于匹配完成状态,则拉取匹配数据
			if (activityInfo.getState() != TWActivityState.NOT_OPEN) {
				loadSignGuild();
			}
			loadGuildTeamInfo();
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();
					
					// 匹配轮询检测
					if (activityInfo.state == TWActivityState.MATCH) {
						matchTick();
					}
					// 战斗阶段轮询检测
					else if( activityInfo.state == TWActivityState.WAR){
						try {
							fightTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			});
			// 阶段轮询
			addTickable(new HawkPeriodTickable(3000) {
				@Override
				public void onPeriodTick() {
					updateGuildTeamChanges();
				}
			});
			
			// 联盟出战战力排行榜
			addTickable(new HawkPeriodTickable(60000) {
				@Override
				public void onPeriodTick() {
					updateGuildPowerRank();
				}
			});

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	/**
	 * 更新存储本服联盟信息(前X名)
	 */
	protected void updateGuildPowerRank() {
		try {
			int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
			if (season <= 0) {
				return;
			}
			TLWActivityData activityData = TiberiumLeagueWarService.getInstance().getActivityInfo();
			List<String> guildIds = new ArrayList<>();
			//
			if (activityData.getTermId() == 1 && activityData.getState().getNumber() < TLWActivityState.TLW_MATCH.getNumber()) {
				// 预选结束前,刷新前X名联盟数据
				if (activityData.getState().getNumber() <= TLWActivityState.TLW_MATCH.getNumber()) {
					guildIds = getNeedUpdateGuildListBefPick();
				}
				// 海选开始后,只刷新入围联盟
				else {
					guildIds = getNeedUpdateGuildListAftPick();

				}
			} else {
				// 其他阶段,只刷新入围联盟信息
				guildIds = getNeedUpdateGuildListAftPick();
			}
			TiberiumWar.TLWServer serverType = TiberiumLeagueWarService.getInstance().getServerType();
			Set<String> signupOldGuilds = RedisProxy.getInstance().getTLWNewSignupOldServer(season);
			List<TLWGuildData> needAddGuilds = new ArrayList<>();
			Map<String, Double> powerMap = new HashMap<>();
			Map<String, Double> signupOldPowerMap = new HashMap<>();
			for (String guildId : guildIds) {
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					continue;
				}
				HawkTuple2<Integer, Long> joinInfo = getJoinMemberPower(guildId);
				int joinCnt = joinInfo.first;
				long power = joinInfo.second;
				TLWGuildData guildData = new TLWGuildData();
				Player leader = GlobalData.getInstance().makesurePlayer(guildObj.getLeaderId());
				guildData.setSeason(season);
				guildData.setId(guildId);
				guildData.setName(guildObj.getName());
				guildData.setTag(guildObj.getTag());
				guildData.setLeaderName(guildObj.getLeaderName());
				guildData.setLeaderId(guildObj.getLeaderId());
				guildData.setLeadegOpenid(leader.getOpenId());
				guildData.setFlag(guildObj.getFlagId());
				guildData.setServerId(GsConfig.getInstance().getServerId());
				guildData.setPower(power);
				guildData.setJoinMemberCnt(joinCnt);
				guildData.setTotalMemberCnt(GuildService.getInstance().getGuildMemberNum(guildId));
				guildData.setCreateTime(guildObj.getCreateTime());
				guildData.setServerType(serverType.getNumber());
				needAddGuilds.add(guildData);
				powerMap.put(guildId, (double) power);
				if(signupOldGuilds.contains(guildId)){
					signupOldPowerMap.put(guildId, (double) power);
				}
			}
			if (!needAddGuilds.isEmpty()) {
				RedisProxy.getInstance().updateTLWGuildData(needAddGuilds, season);
			}
			String serverId = GsConfig.getInstance().getServerId();
			if(serverId.startsWith("1999") || serverId.startsWith("2999") ){
				return;
			}
			if (activityData.getTermId() == 1 && activityData.getState().getNumber() < TLWActivityState.TLW_MATCH.getNumber()) {
				if(serverType == TiberiumWar.TLWServer.TLW_NEW_SERVER){
					if(!powerMap.isEmpty()){
						RedisProxy.getInstance().addTLWGuildPowerRanksNew(season, powerMap);
					}
					if(!signupOldPowerMap.isEmpty()){
						RedisProxy.getInstance().addTLWGuildPowerRanks(season, signupOldPowerMap);
					}
				}else {
					if(!powerMap.isEmpty()){
						RedisProxy.getInstance().addTLWGuildPowerRanks(season, powerMap);
					}
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 海选结束前,批量更新本服战力排行前x名的联盟数据
	 * @return
	 */
	public List<String> getNeedUpdateGuildListBefPick() {
		List<String> guildIds = new ArrayList<>();
		int rankLimit = TiberiumConstCfg.getInstance().getUpdateRankLimit();
		// debug 模式,刷新联盟上限扩大为榜单容量
		if (GsConfig.getInstance().isDebug()) {
			rankLimit = TiberiumConstCfg.getInstance().getPowerRankSize();
			return GuildService.getInstance().getGuildIds();
		}

		// 刷新本服前30的联盟的战力
		RankObject rankObject = RankService.getInstance().getRankObject(RankType.ALLIANCE_FIGHT_KEY);
		List<RankInfo> rankList = new ArrayList<>(rankObject.getSortedRank());
		int size = Math.min(rankLimit, rankList.size());
		for (int i = 0; i < size; i++) {
			RankInfo rankInfo = rankList.get(i);
			guildIds.add(rankInfo.getId());
		}
		return guildIds;
	}
	
	/**
	 * 海选结束后,只更新入围的本服联盟信息
	 * @return
	 */
	public List<String> getNeedUpdateGuildListAftPick() {
		Map<String, Integer> joinMap = TiberiumLeagueWarService.getInstance().getCurrRanMap();
		List<String> localGuilds = new ArrayList<>();
		for (String guildId : joinMap.keySet()) {
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj != null) {
				localGuilds.add(guildId);
			}
		}
		return localGuilds;
	}
	
	/**
	 * 获取报名出战的联盟战力
	 * @param guildId
	 * @return
	 */
	public HawkTuple2<Integer, Long> getJoinMemberPower(String guildId) {
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return null;
		}
		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		long totalPower = 0;
		for (String playerId : idList) {
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
			// 非本盟玩家
			if (member == null || !guildId.equals(member.getGuildId())) {
				continue;
			}
			totalPower += member.getPower();
		}
		int joinCnt = idList.size();
		return new HawkTuple2<Integer, Long>(joinCnt, totalPower);
	}

	/**
	 * 加载本服联盟小队信息
	 */
	private void loadGuildTeamInfo() {
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		Map<String, TWGuildTeamData> dataMap  = new ConcurrentHashMap<>();
		for(String guildId : guildIds){
			TWGuildTeamData teamData = RedisProxy.getInstance().getTWGuildTeamData(guildId);
			if(teamData!=null){
				dataMap.put(guildId, teamData);
			}
		}
		guildTeamDatas = dataMap;
	}

	/**
	 * 加载报名联盟
	 */
	private void loadSignGuild() {
		int termId = activityInfo.getTermId();
		if (termId == 0) {
			return;
		}
		Map<String, Integer> signGuildMap = new HashMap<>();
		List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			List<String> guildIds = RedisProxy.getInstance().getTWSignInfo(termId, i);
			for (String guildId : guildIds) {
				signGuildMap.put(guildId, i);
			}
		}
		signGuild = signGuildMap;
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

	/**
	 * 匹配阶段轮询
	 */
	protected void matchTick() {
		try {
			if (activityInfo.state != TWActivityState.MATCH) {
				return;
			}
			
			// 拉取加载成员列表
			if (!activityInfo.isPrepareFinish()) {
				flushSignerInfo();
				activityInfo.setPrepareFinish(true);
				RedisProxy.getInstance().updateTWActivityInfo(activityInfo);
				// 结算连续缺席积分
				calcAbsentEloScore();
			}
			
			
			String matchKey = RedisProxy.getInstance().TWACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
			String matchLockKey = RedisProxy.getInstance().TWACTIVITY_MATCH_LOCK + ":" + activityInfo.getTermId();
			
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
			
			long prepareEndTime = activityInfo.getTimeCfg().getSignEndTimeValue() + TiberiumConstCfg.getInstance().getMatchPrepareTime();
			// 匹配准备时间未结束,不进行其他处理
			if (HawkTime.getMillisecond() <= prepareEndTime) {
				return;
			}
			
			String serverId = GsConfig.getInstance().getServerId();
			long lock = RedisProxy.getInstance().getMatchLock(matchLockKey);
			boolean needSync = false;
			// 获取到匹配权限,设置有效期并进行匹配
			if (lock > 0) {
				RedisProxy.getInstance().getRedisSession().expire(matchLockKey, TiberiumConstCfg.getInstance().getMatchLockExpire());
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
				// 邮件通知匹配失败的联盟
				sendMatchFailedMail();
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
	 * 结算连续缺席泰伯利亚活动elo积分
	 */
	private void calcAbsentEloScore() {
		int termId = activityInfo.getTermId();
		int initScore = TiberiumConstCfg.getInstance().getInitEloScore();
		int disScore = TiberiumConstCfg.getInstance().getEloAbsentDissScore();
		int absemtLimit = TiberiumConstCfg.getInstance().getEloAbsentTerms();
		Map<String, TWGuildEloData> guildMap = RedisProxy.getInstance().getAllEloData();
		List<TWGuildEloData> updateList = new ArrayList<>();
		for(Entry<String, TWGuildEloData> entry : guildMap.entrySet()){
			String guildId = entry.getKey();
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj == null) {
				continue;
			}
			TWGuildEloData eloData = entry.getValue();
			int lastActiveTerm = eloData.getLastActiveTerm();
			if (lastActiveTerm == 0) {
				HawkLog.logPrintln("calcAbsentEloScore failed, lastActiveTerm is 0, termId: {}, guildId: {}, eloData: {}", termId, guildId, eloData);
				continue;
			}
			int absentTerm = termId - lastActiveTerm;
			if (absentTerm < absemtLimit) {
				continue;
			}
			int oldScore = eloData.getScore();
			int newScore = 0;
			if (oldScore < initScore) {
				newScore = oldScore;
			} else {
				newScore = oldScore - disScore;
				newScore = newScore >= initScore ? newScore : initScore;
			}
			int realDisScore = oldScore - newScore;
			eloData.setScore(newScore);
			updateList.add(eloData);
			GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
					.setMailId(MailId.TLW_ELO_ABSENT_DISS_SCORE)
					.addContents(absentTerm, realDisScore, newScore));
			TWEloScoreLogUnit eloScoreUnit = new TWEloScoreLogUnit(guildId, activityInfo.getTermId(), oldScore, newScore, newScore - oldScore, EloReason.ABSENT_CALC);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnit);
		}
		if (!updateList.isEmpty()) {
			RedisProxy.getInstance().updateTWGuildElo(updateList);
		}
		
	}

	private void sendMatchFailedMail() {
		int termId = activityInfo.termId;
		List<String> guildIds = new ArrayList<>(signGuild.keySet());
		if(guildIds == null || guildIds.isEmpty()){
			return;
		}
		Map<String, TWGuildData> dataMap = RedisProxy.getInstance().getAllTWGuildData(termId); 
		String serverId = GsConfig.getInstance().getServerId();
		for (Entry<String, TWGuildData> entry : dataMap.entrySet()) {
			TWGuildData guildData = entry.getValue();
			if (!serverId.equals(guildData.getServerId())) {
				continue;
			}
			final String guildId = entry.getKey();
			if(guildData.isMatchFailed()){
				HawkLog.logPrintln("TiberiumWarService match failed guild mail, termId: {}, guildId: {}", termId, guildData.getId());
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
								.setMailId(MailId.TBLY_GUILD_MATCH_FAILED));
						return null;
					}
				});
			}
		}
		
	}

	/**
	 * 战斗阶段状态轮询
	 */
	protected void fightTick() {
		if (activityInfo.state != TWActivityState.WAR) {
			return;
		}
		if (!fightState.isHasInit()) {
			fightState = RedisProxy.getInstance().getTWFightInfo();
			if (fightState == null) {
				fightState = new TWFightState();
				RedisProxy.getInstance().updateTWFightInfo(fightState);
			}
			fightState.setHasInit(true);
		}
		if (fightState.getTermId() != activityInfo.termId) {
			fightState = new TWFightState();
			fightState.setTermId(activityInfo.termId);
			fightState.setHasInit(true);
			RedisProxy.getInstance().updateTWFightInfo(fightState);
		}
		long curTime = HawkTime.getMillisecond();

		boolean needUpdate = false;
		for (TWFightUnit unit : fightState.getUnitList()) {

			WarTimeChoose timeChoose = getWarTimeChoose(unit.getTimeIndex());
			long startTime = timeChoose.getTime();
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
			while (unit.getState() != state) {
				if (unit.getState() == FightState.NOT_OPEN) {
					unit.setState(FightState.OPEN);
					createBattle(unit.getTimeIndex());
				} else if (unit.getState() == FightState.OPEN) {
					unit.setState(FightState.FINISH);
				} else if (unit.getState() == FightState.FINISH) {
					sendAward(unit.getTimeIndex());
					unit.setState(FightState.AWARDED);
				} else {
					unit.setState(FightState.NOT_OPEN);
				}
				needUpdate = true;
				stageChange = true;
			}
			// 阶段变更为开启,则需要推送跑马灯
			if (unit.state == FightState.OPEN && stageChange) {
				//
				Set<Player> onlineMembers = new HashSet<>();
				for (Entry<String, Integer> entry : signGuild.entrySet()) {
					String guildId = entry.getKey();
					if (entry.getValue() != unit.getTimeIndex()) {
						continue;
					}
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
		}

		if (needUpdate) {
			RedisProxy.getInstance().updateTWFightInfo(fightState);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
		}
	}
	
	public boolean isClose() {
		boolean isSystemClose = TiberiumConstCfg.getInstance().isSystemClose();
		return isSystemClose && activityInfo.state == TWActivityState.CLOSE;
	}

	/**
	 * 获取选项
	 * @return
	 */
	private List<WarTimeChoose> getChooses() {
		List<WarTimeChoose> chooseList = new ArrayList<>();
		TiberiumTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg == null) {
			return chooseList;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(cfg.getWarStartTimeValue());
		List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			HawkTuple2<Integer, Integer> timeTuple = timeList.get(i);
			calendar.set(Calendar.HOUR_OF_DAY, timeTuple.first);
			calendar.set(Calendar.MINUTE, timeTuple.second);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			WarTimeChoose.Builder builder = WarTimeChoose.newBuilder();
			builder.setIndex(i);
			builder.setTime(calendar.getTimeInMillis());
			chooseList.add(builder.build());
		}
		return chooseList;
	}

	/**
	 * 当前阶段状态计算,仅供状态检测调用
	 * @return
	 */
	private TWActivityData calcInfo() {
		TWActivityData info = new TWActivityData();
		if(TiberiumConstCfg.getInstance().isSystemClose()){
			info.setState(TWActivityState.CLOSE);
			return info;
		}
		ConfigIterator<TiberiumTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumTimeCfg.class);
		long now = HawkTime.getMillisecond();
	
		String serverId = GsConfig.getInstance().getServerId();
	
		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = TiberiumConstCfg.getInstance().getServerDelay();
		TiberiumTimeCfg cfg = null;
		
		//游客服不开启该活动
		if(GsConfig.getInstance().getServerType() == ServerType.GUEST){
			return info;
		}
		for (TiberiumTimeCfg timeCfg : its) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			if (serverOpenAm0 + serverDelay > timeCfg.getSignStartTimeValue()) {
				continue;
			}
			// 开启判定,如果没有开启区服限制,或者本期允许本服所在区组开放
			if ((limitServerLimit.isEmpty() || limitServerLimit.contains(serverId)) && (forbidServerLimit == null || !forbidServerLimit.contains(serverId))) {
				if (now > timeCfg.getSignStartTimeValue()) {
					cfg = timeCfg;
				}
			}
		}
	
		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}
	
		int termId = 0;
		TWActivityState state = TWActivityState.NOT_OPEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long signStartTime = cfg.getSignStartTimeValue();
			long signEndTime = cfg.getSignEndTimeValue();
			long matchEndTime = cfg.getMatchEndTimeValue();
			long warEndTime = cfg.getWarEndTimeValue();
			if (now < signStartTime) {
				state = TWActivityState.NOT_OPEN;
			}
			if (now >= signStartTime && now < signEndTime) {
				state = TWActivityState.SIGN;
			}
			if (now >= signEndTime && now < matchEndTime) {
				state = TWActivityState.MATCH;
			}
			if (now >= matchEndTime && now < warEndTime) {
				state = TWActivityState.WAR;
			}
			if (now >= warEndTime) {
				state = TWActivityState.NOT_OPEN;
			}
		}
		
		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	private void checkStateChange() {
		TWActivityData newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();
	
		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateTWActivityInfo(activityInfo);
		}
		TWActivityState old_state = activityInfo.getState();
		TWActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_term != old_term) {
			old_state = TWActivityState.NOT_OPEN;
			activityInfo.setTermId(new_term);
			needUpdate = true;
		}
	
		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == TWActivityState.NOT_OPEN) {
				old_state = TWActivityState.SIGN;
				activityInfo.setState(old_state);
				onSignOpen();
			} else if (old_state == TWActivityState.SIGN) {
				old_state = TWActivityState.MATCH;
				activityInfo.setState(old_state);
				onMatchStart();
			} else if (old_state == TWActivityState.MATCH) {
				old_state = TWActivityState.WAR;
				activityInfo.setState(old_state);
				onMatchFinish();
			} else if (old_state == TWActivityState.WAR) {
				old_state = TWActivityState.NOT_OPEN;
				activityInfo.setState(old_state);
				onHidden();
			}
		}
	
		if (needUpdate) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateTWActivityInfo(activityInfo);
			HawkLog.logPrintln("TiberiumWar state change, oldTerm: {}, oldState: {} ,newTerm: {}, newState: {}", old_term, old_state, activityInfo.getTermId(),
					activityInfo.getState());
		}
	
	}

	/**
	 * 进行联盟匹配
	 */
	private boolean toMatchGuild() {
		try {
			long startTime = HawkTime.getMillisecond();
			int termId = activityInfo.termId;
			// 清空本期的房间信息
			RedisProxy.getInstance().removeTWRoomData(termId);
			Map<String, TWGuildData> dataMap = RedisProxy.getInstance().getAllTWGuildData(termId);
			Map<Integer, List<TWGuildData>> matchGuildMap = new HashMap<>();
			for (Entry<String, TWGuildData> entry : dataMap.entrySet()) {
				TWGuildData data = entry.getValue();
				int timeIndex = data.getTimeIndex();
				if (!matchGuildMap.containsKey(timeIndex)) {
					matchGuildMap.put(timeIndex, new ArrayList<>());
				}
				matchGuildMap.get(timeIndex).add(data);
			}
			for (Entry<Integer, List<TWGuildData>> entry : matchGuildMap.entrySet()) {
				int timeIndex = entry.getKey();
				List<TWGuildData> dataList = entry.getValue();
				doMatch(timeIndex, dataList);
			}
			HawkLog.logPrintln("TiberiumWarService toMatchGuild finish, serverId: {}, costTime: {}", GsConfig.getInstance().getServerId(), HawkTime.getMillisecond() - startTime);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	/**
	 * 匹配
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public boolean doMatch(int timeIndex, List<TWGuildData> dataList) {
		int termId = activityInfo.termId;
		List<TWGuildData> matchList = new ArrayList<>(dataList);
		List<HawkTuple2<TWGuildData, TWGuildData>> resultList = new ArrayList<>();
		/************* 暗箱操作匹配 ***************/
		Map<String, TWGuildData> dataMap = new HashMap<>();
		for (TWGuildData guildData : matchList) {
			dataMap.put(guildData.getId(), guildData);
		}
		TiberiumAppointMatchCfg appointCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumAppointMatchCfg.class, termId);
		if (appointCfg != null) {
			List<HawkTuple2<String, String>> appointList = appointCfg.getAppointList();

			for (HawkTuple2<String, String> tuple : appointList) {
				TWGuildData guildDataA = dataMap.get(tuple.first);
				TWGuildData guildDataB = dataMap.get(tuple.second);
				// 内定联盟未在同一个时间区间
				if (guildDataA == null || guildDataB == null) {
					continue;
				}
				resultList.add(new HawkTuple2<TWGuildData, TWGuildData>(guildDataA, guildDataB));
				HawkLog.logPrintln("TiberiumWarService appoint match, termId: {}, timeIndex: {}, guild1: {}, guild2: {}", termId, timeIndex, tuple.first, tuple.second);
				matchList.remove(guildDataA);
				matchList.remove(guildDataB);
			}
		}
		/************* 暗箱操作匹配 ***************/

		Random random = new Random();
		do {
			if (matchList.size() <= 1) {
				break;
			}
			
			List<TWGuildData> sortList = matchList.stream().sorted().collect(Collectors.toList());
			if (sortList.size() <= 1) {
				break;
			}
			TWGuildData selectGuild = sortList.get(0);
			List<TWGuildData> filterList = matchList.stream().filter(e -> !e.serverId.equals(selectGuild.serverId) || e.id.equals(selectGuild.id)).sorted()
					.collect(Collectors.toList());
			int paramN = filterList.size();
			if (paramN <= 1) {
				break;
			}
			int paramR = filterList.indexOf(selectGuild) + 1;
			int minIndex = Math.max(1, paramR - (int) Math.ceil(1d * paramN / 20)) - 1;
			int maxIndex = Math.min(paramN, paramR + (int) Math.ceil(1d * paramN / 20)) - 1;
			List<TWGuildData> selects = new ArrayList<>();
			for (int i = minIndex; i <= maxIndex; i++) {
				TWGuildData select = filterList.get(i);
				if (!select.getId().equals(selectGuild.getId())) {
					selects.add(select);
				}
			}
			Collections.shuffle(selects);
			TWGuildData matchGuild = selects.get(0);
			resultList.add(new HawkTuple2<TWGuildData, TWGuildData>(selectGuild, matchGuild));
			matchList.remove(selectGuild);
			matchList.remove(matchGuild);
		} while (true);
		// 存在未能成功匹配的联盟
		if (!matchList.isEmpty()) {
			for (TWGuildData guildData : matchList) {
				guildData.setMatchFailed(true);
				guildData.setRoomId("");
			}
			RedisProxy.getInstance().updateTWGuildData(matchList, termId);
		}
		List<TWGuildData> updateList = new ArrayList<>();
		List<TWRoomData> roomList = new ArrayList<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple2<TWGuildData, TWGuildData> tuple : resultList) {
			TWRoomData roomData = new TWRoomData();
			String server1 = tuple.first.getServerId();
			String server2 = tuple.second.getServerId();
			TWGuildData guildA;
			TWGuildData guildB;
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
			roomData.setTimeIndex(timeIndex);
			roomData.setId(HawkOSOperator.randomUUID());
			roomData.setRoomServerId(roomServer);
			roomData.setGroup(TLWGroupType.NORMAL);
			roomData.setBattleType(TLWBattleType.NORMAL);
			roomList.add(roomData);

			tuple.first.setRoomId(roomData.getId());
			tuple.first.setOppGuildId(tuple.second.getId());
			tuple.first.setMatchFailed(false);

			tuple.second.setRoomId(roomData.getId());
			tuple.second.setOppGuildId(tuple.first.getId());
			tuple.second.setMatchFailed(false);
			updateList.add(tuple.first);
			updateList.add(tuple.second);
			LogUtil.logTimberiumMatchInfo(roomData.getId(), roomData.getRoomServerId(), termId, timeIndex, guildA.getId(), guildA.getGuildStrength(), guildA.getServerId(), guildB.getId(),
					guildB.getGuildStrength(),guildB.getServerId());
			
			
			HawkLog.logPrintln("TiberiumWarService do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}, guildA: {}, guildStrengthA:{}, serverA: {}, guildB: {},  guildStrengthB:{}, serverB: {} ",
					roomData.getId(), roomData.getRoomServerId(), termId, timeIndex, guildA.getId(), guildA.getGuildStrength(), guildA.getServerId(), guildB.getId(), guildA.getGuildStrength(), guildB.getServerId());
		}
		RedisProxy.getInstance().updateTWGuildData(updateList, termId);
		RedisProxy.getInstance().updateTWRoomData(roomList, termId);
		return true;
	}
	
	/**
	 * 报名阶段结束,记录参与数据
	 * @return
	 */
	private boolean flushSignerInfo() {
		String serverId = GsConfig.getInstance().getServerId();
		int eloBeginTermId = TiberiumConstCfg.getInstance().getEloBeginTermId();
		int termId = activityInfo.termId;
		try {
			for (Entry<String, Integer> entry : signGuild.entrySet()) {
				String guildId = entry.getKey();
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guild == null) {
					continue;
				}
				Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
				int memberCnt = 0;
				long totalPower = 0;
				List<HawkTuple2<String, Long>> powerList = new ArrayList<>();
				for (String playerId : idList) {
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					// 非本盟玩家
					if (member == null || !guildId.equals(member.getGuildId())) {
						continue;
					}
					memberCnt++;
					totalPower += member.getPower();
					//获取玩家对象
					long playerStrength = 0;
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					if(player != null){
						playerStrength = player.getStrength();
						//获取玩家实力值
						powerList.add(new HawkTuple2<String, Long>(playerId,playerStrength));
					}
					//玩家泰伯数据
					TWPlayerData playerData = new TWPlayerData();
					playerData.setId(playerId);
					playerData.setGuildAuth(member.getAuthority());
					playerData.setGuildId(guildId);
					playerData.setGuildOfficer(member.getOfficeId());
					playerData.setServerId(serverId);
					playerData.setPlayerStrength(playerStrength);
					Player mplayer = GlobalData.getInstance().makesurePlayer(playerId);
					if (mplayer != null) {
						playerData.setCityLevel(mplayer.getCityLevel());
						playerData.setName(mplayer.getName());
						playerData.setIcon(mplayer.getIcon());
						playerData.setPfIcon(mplayer.getPfIcon());
					}
					RedisProxy.getInstance().updateTWPlayerData(playerData, termId);
					LogUtil.logTimberiumPlayerWarInfo(playerId, guildId, playerStrength, termId);
				}
				
				int cntLimit = TiberiumConstCfg.getInstance().getWarMemberMinCnt();
				// 参战人数不达标
				if (memberCnt < cntLimit) {
					final int cnt = memberCnt;
					HawkLog.logPrintln("flushSignerInfo failed, memberCnt not enough, guildId: {}, memberCnt: {}", guildId, memberCnt);
					HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
						@Override
						public Object run() {
							GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
									.setMailId(MailId.TBLY_GUILD_MEMBER_NOT_ENOUGH)
									.addContents(cnt));
							return null;
						}
					});
					continue;
				}
				TWGuildEloData eloData = RedisProxy.getInstance().getTWGuildElo(guildId);
				if (eloData == null) {
					eloData = new TWGuildEloData();
					eloData.setId(guildId);
					eloData.setServerId(serverId);
					int initScore = TiberiumConstCfg.getInstance().getInitEloScore();
					// 本期首次开放elo机制,计算战力额外积分
					if (termId == eloBeginTermId) {
						int addScore = (int) Math.floor(1d * totalPower / TiberiumConst.ELO_EXT_SCORE_PARAM * TiberiumConstCfg.getInstance().getExtPowerScore());
						initScore += addScore;
						eloData.setScore(initScore);
						GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
								.setMailId(MailId.TLW_INIT_ELO_SCORE_WITH_POWER)
								.addContents(totalPower, addScore, initScore));
						TWEloScoreLogUnit eloScoreUnit = new TWEloScoreLogUnit(guildId, activityInfo.getTermId(), 0, initScore, initScore, EloReason.INIT_WITH_POWER);
						TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnit);
					} else {
						eloData.setScore(initScore);
						GuildMailService.getInstance().sendGuildMail(guildId,  MailParames.newBuilder()
								.setMailId(MailId.TLW_INIT_ELO_SCORE)
								.addContents(initScore));
						TWEloScoreLogUnit eloScoreUnit = new TWEloScoreLogUnit(guildId, activityInfo.getTermId(), 0, initScore, initScore, EloReason.INIT);
						TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnit);
					}
				}
				// 更新elo信息
				if(!serverId.equals(eloData.getServerId())){
					eloData.setServerId(serverId);
				}
				eloData.setLastActiveTerm(termId);
				
				TWGuildData guildData = new TWGuildData();
	
				guildData.setId(guildId);
				guildData.setServerId(serverId);
				guildData.setName(guild.getName());
				guildData.setTag(guild.getTag());
				guildData.setFlag(guild.getFlagId());
				guildData.setMemberCnt(memberCnt);
				guildData.setTotalPower(totalPower);
				guildData.setTimeIndex(entry.getValue());
				guildData.setEloScore(eloData.getScore());
				guildData.setGuildPower(GuildService.getInstance().getGuildBattlePoint(guildId));
				//如果开启新的战力匹配，则赋值
				if(CyborgConstCfg.getInstance().getCyborgMatchSwitch() > 0){
					long guildStrength = this.getMatchPower(guildId, powerList);
					guildData.setGuildStrength(guildStrength);
				}
				
				RedisProxy.getInstance().updateTWGuildData(guildData, termId);
				RedisProxy.getInstance().updateTWGuildElo(eloData);
				try {
					HawkLog.logPrintln("TiberiumWarService flushSignerInfo, guildId: {}, guildName: {} , serverId: {}, memberCnt: {}, totalPowar: {}, termId: {}, timeIndex: {}", guildId, guild.getName(), serverId, memberCnt, totalPower, termId, entry.getValue());
					LogUtil.logTimberiumMatcherInfo(guildId, guild.getName(), serverId, memberCnt, totalPower, guildData.getGuildStrength(), termId, entry.getValue());
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
	 * 新得匹配战力
	 * @param teamId
	 * @param powerList
	 * @return
	 */
	public long getMatchPower(String guildId,List<HawkTuple2<String, Long>> powerList){
		try {
			Collections.sort(powerList, new Comparator<HawkTuple2<String, Long>>() {
				@Override
				public int compare(HawkTuple2<String, Long> o1, HawkTuple2<String, Long> o2) {
					long power1 = o1.second;
					long power2 = o2.second;
					if(power1 == power2){
						return 0;
					}
					if(power1 > power2){
						return -1;
					}
					return 1;
				};
			});
			double memberPower = 0;
			for(int i =0;i<powerList.size();i++){
				HawkTuple2<String, Long> tuple = powerList.get(i);
				int rank = i+1;
				long power = tuple.second;
				double powerWeight = this.getPowerWeight(rank);
				memberPower += (power * powerWeight);
				HawkLog.logPrintln("TiberiumWarService match power,guildId:{},playerId:{},power:{},powerWeight:{},memberPower:{},", 
						guildId,tuple.first,power,powerWeight, power * powerWeight);
			}
			double teamParam = this.getTeamMatchParam(guildId);
			double openServerParam = TiberiumConstCfg.getInstance().getMatchPowerParam();
			long matchPower = (long) (teamParam * memberPower * openServerParam);
			HawkLog.logPrintln("TiberiumWarService match power,guildId:{},memberPower:{},teamParam:{},openServerParam:{},matchPower:{}", guildId,memberPower,teamParam,openServerParam,matchPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(20);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}
	
	/**
	 * 队伍磨合参数
	 * @param teamId
	 * @return
	 */
	private double getTeamMatchParam(String guildId){
		int count = TiberiumConstCfg.getInstance().getTiberiumMatchTimesLimit() -1;
		count = Math.max(count, 0);
		List<TWBattleLog> logList = RedisProxy.getInstance().getTWBattleLog(guildId,count);
		double param = 0;
		for(TWBattleLog log : logList){
			double addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultLoss();
			int win = 0;
			if(log.getWinGuild().equals(guildId)){
				addParam = TiberiumConstCfg.getInstance().getTiberiumMatchBattleResultWin();
				win = 1;
			}
			param += addParam;
			HawkLog.logPrintln("TiberiumWarService match power, getTeamMatchParam,guildId:{},termId:{},win:{},param:{}", 
					guildId,log.getTermId(),win,addParam);
		}
		param = Math.min(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMaxValue());
		param = Math.max(param, TiberiumConstCfg.getInstance().getTiberiumMatchCofMinValue());
		HawkLog.logPrintln("TiberiumWarService match power, getTeamMatchParam, result,guildId:{},param:{}", 
				guildId,param);
		return param + 1;
	}

	private TWGuildTeamInfo genGuildTeamInfo(String guildId) {
		TWGuildTeamData teamData = guildTeamDatas.get(guildId);
		// 无联盟小队信息
		if(teamData == null){
			return null;
		}
		Map<Integer, TWTeamData> teamInfos = teamData.getTeamInfos();
		if(teamInfos.isEmpty()){
			return null;
		}
		TWGuildTeamInfo.Builder builder = TWGuildTeamInfo.newBuilder();
		for(Entry<Integer, TWTeamData> entry: teamInfos.entrySet()){
			HawkTuple2<Integer, TWTeamInfo.Builder> tuple = genTeamInfo(guildId, entry.getKey());
			if(tuple.first == Status.SysError.SUCCESS_OK_VALUE){
				builder.addTeamInfo(tuple.second);
			}
		}
		
		return builder.build();
	}

	/**
	 * 批量创建
	 * @param timeIndex
	 */
	private void createBattle(int timeIndex) {
		WarTimeChoose timechoose = getWarTimeChoose(timeIndex);
		long startTime = timechoose.getTime();
		long overTime = startTime + TiberiumConstCfg.getInstance().getWarOpenTime();
		List<TWRoomData> dataList = RedisProxy.getInstance().getAllTWRoomData(activityInfo.termId);
		String serverId = GsConfig.getInstance().getServerId();
		List<TWRoomData> createList = dataList.stream().filter(t -> t.getRoomServerId().equals(serverId) && t.getTimeIndex() == timeIndex).collect(Collectors.toList());
		for (TWRoomData roomData : createList) {
			String guildA = roomData.getGuildA();
			String guildB = roomData.getGuildB();
			TWGuildData guildDataA = RedisProxy.getInstance().getTWGuildData(guildA, activityInfo.termId);
			TWGuildData guildDataB = RedisProxy.getInstance().getTWGuildData(guildB, activityInfo.termId);
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
				param.setCampAPlayers(RedisProxy.getInstance().getAllTWPlayerData(campA.getId(), activityInfo.termId));
				param.setCampBGuild(campB.getId());
				param.setCampBguildFlag(campB.getFlag());
				param.setCampBGuildName(campB.getName());
				param.setCampBGuildTag(campB.getTag());
				param.setCampBServerId(campB.getServerId());
				param.setCampBPlayers(RedisProxy.getInstance().getAllTWPlayerData(campB.getId(), activityInfo.termId));
				param.setLeaguaWar(false);
				boolean result = TBLYRoomManager.getInstance().creatNewBattle(startTime, overTime, roomData.getId(), param);
				if (result) {
					roomData.setRoomState(RoomState.INITED);
				} else {
					roomData.setRoomState(RoomState.INITED_FAILED);
				}
			}
		}
		RedisProxy.getInstance().updateTWRoomData(createList, activityInfo.termId);
	}

	/**
	 * 发奖
	 */
	private void sendAward(int timeIndex) {
		int termId = activityInfo.termId;
		int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
		List<String> guildIds = RedisProxy.getInstance().getTWSignInfo(termId, timeIndex);
		if(guildIds == null || guildIds.isEmpty()){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		for (String guildId : guildIds) {
			TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
			if (guildData == null) {
				continue;
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
			TiberiumGuildAwardCfg guildCfg = getGuildAwardCfg(isWin);
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			List<TWSelfRewardLogUnit> selfRewardLogUnitList = new ArrayList<>();
			// 赛季积分奖励
			if (season > 0) {
				TLWScoreData guildScoreData = RedisProxy.getInstance().getTLWGuildScoreInfo(season, guildId);
				TiberiumLeagueWarService.getInstance().calcGuildAddReward(guildScoreData);
				Set<Integer> guildRewards = guildScoreData.getRewardedList();
				// 联盟奖励
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
					// 赛季联盟积分奖励
					if(!gNeedRewards.isEmpty()){
						for(int gnId : gNeedRewards){
							TiberiumSeasonGuildAwardCfg grCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumSeasonGuildAwardCfg.class, gnId);
							if(grCfg == null){
								HawkLog.logPrintln("TiberiumWarService send guildSeasonReward err,cfg is null, playerId: {}, guildId: {}, score: {}, cfgId: {}", memberId, guildId,
										guildScoreData.getScore(), gnId);
								continue;
							}
							SystemMailService.getInstance().sendMail(MailParames.newBuilder()
									.setPlayerId(memberId)
									.setMailId(MailId.TIBERIUM_SEASON_GUILD_SCORE_REWARD)
									.addContents(grCfg.getScore())
									.setAwardStatus(MailRewardStatus.NOT_GET)
									.addRewards(grCfg.getRewardItem())
									.build());
							guildRewardeds.add(gnId);
							LogUtil.logTimberiumLeaguaGuildReward(memberId, guildId, serverId, guildScoreData.getScore(), selfScoreData.getScore(), season, termId, gnId, false);
							HawkLog.logPrintln(
									"TiberiumWarService sendSeasonGuildScoreReward, memberId: {}, guildId: {}, serverId: {}, guildScore: {}, selfScore: {}, season: {}, termId: {}, gnId: {}",
									memberId, guildId, serverId, guildScoreData.getScore(), selfScoreData.getScore(), season, termId, gnId);
						}
						needUpdate = true;
					}
					List<Integer> pNeedReward = TiberiumLeagueWarService.getInstance().calcSelfAddReward(selfScoreData);
					if(!pNeedReward.isEmpty()){
						for(int pnId : pNeedReward){
							TiberiumSeasonPersonAwardCfg prCfg = HawkConfigManager.getInstance().getConfigByKey(TiberiumSeasonPersonAwardCfg.class, pnId);
							if(prCfg == null){
								HawkLog.logPrintln("TiberiumWarService send selfSeasonReward err,cfg is null, playerId: {}, guildId: {}, score: {}, cfgId: {}", memberId, guildId,
										guildScoreData.getScore(), pnId);
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
							selfRewardLogUnitList.add(new TWSelfRewardLogUnit(memberId, guildId, serverId, selfScoreData.getScore(), season, termId, pnId, false)); 
							HawkLog.logPrintln(
									"TiberiumWarService sendSeasonSelfScoreReward, memberId: {}, guildId: {}, serverId: {}, selfScore: {}, season: {}, termId: {}, gnId: {}",
									memberId, guildId, serverId, selfScoreData.getScore(), season, termId, pnId);
						}
						needUpdate = true;
					}
					if(needUpdate){
						RedisProxy.getInstance().updateTLWPlayerScoreInfo(season, selfScoreData);
					}
				}
			}
			for (String memberId : memberIds) {
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(memberId)
						.setMailId(guildMailId)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addRewards(guildCfg.getRewardItem())
						.build());
			}
			guildData.setAwarded(true);
			HawkLog.logPrintln("TiberiumWarService send guild reward, guildId: {}, guildName: {} ,guildTag: {}, score: {}, cfgId: {}, isComplete: {}", guildId, guildData.getName(),
					guildData.getTag(), guildData.getScore(), guildCfg.getId(), isComplete);
			RedisProxy.getInstance().updateTWGuildData(guildData, termId);
			// 参赛个人奖励
			Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
			HawkLog.logPrintln("TiberiumWarService start send self reward, guildId: {}, guildName: {} ,guildTag: {}, joinIds: {}", guildId, guildData.getName(),
					guildData.getTag(), idList);
			MailId selfMailId = isWin ? MailId.TBLY_SELF_WIN_MAIL : MailId.TBLY_SELF_LOSE_MAIL;
			for (String playerId : idList) {
				TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(playerId, termId);
				if (twPlayerData == null) {
					HawkLog.logPrintln("TiberiumWarService ignore self reward, twPlayerData is null, playerId: {}", playerId);
					continue;
				}
				if (twPlayerData.isAwarded()) {
					HawkLog.logPrintln("TiberiumWarService ignore self reward,already awarded, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}, isAwarded",
							playerId, twPlayerData.getEnterTime(), twPlayerData.getQuitTime(), twPlayerData.isMidwayQuit(), twPlayerData.isAwarded());
					continue;
				}
				long selfScore = twPlayerData.getScore();
				// 战场异常中断导致未正常结算,按照最高档次奖励发放
				if (!isComplete) {
					selfScore = Integer.MAX_VALUE;
					selfMailId = MailId.TBLY_SELF_PAUSE_MAIL;
				}
				ActivityManager.getInstance().postEvent(new TWScoreEvent(playerId, selfScore, false,twPlayerData.getEnterTime()));
				MissionManager.getInstance().postMsg(playerId, new EventTiberiumWar());

				if (twPlayerData.getEnterTime() == 0 || twPlayerData.isMidwayQuit()) {
					HawkLog.logPrintln("TiberiumWarService ignore self reward, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}", playerId, twPlayerData.getEnterTime(),
							twPlayerData.getQuitTime(), twPlayerData.isMidwayQuit());
					continue;
				}
				TiberiumPersonAwardCfg selfCfg = getPersonAwardCfg(selfScore, isWin);
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(selfMailId)
						.addContents(selfScore)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addRewards(selfCfg.getRewardItem())
						.build());
				twPlayerData.setAwarded(true);
				RedisProxy.getInstance().updateTWPlayerData(twPlayerData, termId);
				HawkLog.logPrintln("TiberiumWarService send self reward, playerId: {}, score: {}, cfgId: {}, isComplete: {}", playerId, selfScore, selfCfg.getId(), isComplete);
			}
			TWLogUtil.logTimberiumLeaguaSelfReward(selfRewardLogUnitList);
			try {
				ActivityService.getInstance().dealMsg(GameConst.MsgId.ON_GUILD_BACK_DROP, new GuildBackDropInvoker(new ArrayList<>(memberIds)));
			}catch (Exception e){
				HawkException.catchException(e);
			}
		}
		
	}

	

	public TiberiumPersonAwardCfg getPersonAwardCfg(long selfScore, boolean isWin) {
		ConfigIterator<TiberiumPersonAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumPersonAwardCfg.class);
		for(TiberiumPersonAwardCfg cfg : its){
			if(isWin != cfg.isWin()){
				continue;
			}
			HawkTuple2<Long, Long> rang = cfg.getScoreRange();
			if (selfScore >= rang.first && selfScore <= rang.second) {
				return cfg;
			}
		}
		return null;
	}
	
	public TiberiumGuildAwardCfg getGuildAwardCfg(boolean isWin) {
		ConfigIterator<TiberiumGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(TiberiumGuildAwardCfg.class);
		for(TiberiumGuildAwardCfg cfg : its){
			if(isWin == cfg.isWin()){
				return cfg;
			}
		}
		return null;
	}

	private void onSignOpen() {
		signGuild = new ConcurrentHashMap<>();
		
	}

	private void onMatchStart() {
		// 加载全部报名的联盟
		loadSignGuild();
	}
	
	/**
	 * 活动关闭
	 */
	private void onHidden() {
		signGuild = new ConcurrentHashMap<>();
		guildTeamInfos = new ConcurrentHashMap<>();
	}

	/**
	 * 匹配结束,进入战斗准备阶段
	 */
	private void onMatchFinish() {
		// 构建联盟小组静态数据
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		for (String guildId : guildIds) {
			TWGuildTeamInfo guildTeamBuilder = genGuildTeamInfo(guildId);
			if (guildTeamBuilder != null) {
				RedisProxy.getInstance().updateTWGuildTeamBuild(guildId, guildTeamBuilder);
				guildTeamInfos.put(guildId, guildTeamBuilder);
			}
		}
	}
	
	/*********************************活动数据信息*****************************************************************/
	
	/**
	 * 构建活动时间信息
	 * @return
	 */
	public TWStateInfo.Builder genStateInfo(String guildId) {
		TWStateInfo.Builder builder = TWStateInfo.newBuilder();
		TWActivityState state = activityInfo.state;
		int termId = activityInfo.termId;
		builder.setStage(termId);
		TiberiumTimeCfg cfg = activityInfo.getTimeCfg();
		List<WarTimeChoose> timeList = getChooses();
		long warStartTime = 0;
		if (cfg != null) {
			builder.setSignStartTime(cfg.getSignStartTimeValue());
			builder.setSignEndTime(cfg.getSignEndTimeValue());
			builder.setShowEndTime(cfg.getWarEndTimeValue());
		}
		boolean isSign = !HawkOSOperator.isEmptyString(guildId) && signGuild.containsKey(guildId);
		if (isSign) {
			int index = signGuild.get(guildId);
			WarTimeChoose choose = timeList.get(index);
			warStartTime = choose.getTime();
			builder.setWarEndTime(warStartTime + TiberiumConstCfg.getInstance().getWarOpenTime());
		}
		switch (state) {
		case NOT_OPEN:
			builder.setState(TWState.NOT_OPEN);
			break;
		case SIGN:
			builder.setState(TWState.SIGN_UP);
			if (isSign) {
				if (warStartTime != 0) {
					builder.setWarStartTime(warStartTime);
				}
			} else {
				if (!timeList.isEmpty()) {
					List<WarTimeChoose> chooseList = new ArrayList<>();
					for (WarTimeChoose choose : timeList) {
						int index = choose.getIndex();
						List<String> signGuilds = RedisProxy.getInstance().getTWSignInfo(termId, index);
						WarTimeChoose.Builder timeChoose = WarTimeChoose.newBuilder();
						timeChoose.setIndex(index);
						timeChoose.setTime(choose.getTime());
						int maxSignNum = TiberiumConstCfg.getInstance().getMaxSignNum();
						int guildCnt = signGuilds == null ? 0 : signGuilds.size();
						timeChoose.setGuildCnt(Math.min(maxSignNum, guildCnt));
						chooseList.add(timeChoose.build());
					}
					builder.addAllChoose(chooseList);
				}
			}
			break;
		case MATCH:
			if (isSign) {
				builder.setState(TWState.MATCH);
				if (warStartTime != 0) {
					builder.setWarStartTime(warStartTime);
				}
			} else {
				builder.setState(TWState.NOT_OPEN);
			}
			break;
		case WAR:
			if (isSign) {
				long now = HawkTime.getMillisecond();
				long warOpenTime = TiberiumConstCfg.getInstance().getWarOpenTime();
				if (now < warStartTime) {
					builder.setState(TWState.WAR_WAIT);
				} else if (now >= warStartTime && now < warStartTime + warOpenTime) {
					builder.setState(TWState.WAR_OPEN);
				} else {
					builder.setState(TWState.WAR_FINISH);
				}
				builder.setWarStartTime(warStartTime);
			} else {
				builder.setState(TWState.NOT_OPEN);
			}
			break;
	
		default:
			break;
		}
		return builder;
	}

	/**
	 * 构建活动界面信息
	 * @param player
	 * @return
	 */
	public TWPageInfo.Builder genPageInfo(Player player) {
		String guildId = player.getGuildId();
		int termId = activityInfo.getTermId();
		TWPageInfo.Builder builder = TWPageInfo.newBuilder();
		TWStateInfo.Builder stateInfo = genStateInfo(guildId);
		boolean isSign = signGuild.containsKey(guildId);
		builder.setIsSignUp(isSign);
		builder.setIsGuildJoin(IsGuildJoin(player));
		builder.setIsPlayerJoin(isPlayerJoin(player));
		switch (activityInfo.state) {
		case NOT_OPEN:
			break;

		case SIGN:
			break;

		case MATCH:
			if (matchServerInfo.isFinish()) {
				TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
				if (guildData == null) {
					stateInfo.setState(TWState.NOT_OPEN);
					break;
				} else {
					// 未匹配成功
					if (guildData.isMatchFailed()) {
						stateInfo.setState(TWState.NOT_OPEN);
						break;
					}
					String oppGuildId = guildData.getOppGuildId();
					// 匹配房间id为空
					if (HawkOSOperator.isEmptyString(oppGuildId)) {
						HawkLog.logPrintln("genPageInfo error on MATCH state, oppGuildId is empty, termId: {}, guildId: {}", termId, guildId);
						stateInfo.setState(TWState.NOT_OPEN);
						break;
					}
					builder.setSelfGuild(guildData.build());
					TWGuildData oppGuildData = RedisProxy.getInstance().getTWGuildData(oppGuildId, termId);
					if (oppGuildData != null) {
						builder.setOppGuild(oppGuildData.build());
					} else {
						HawkLog.logPrintln("genPageInfo error on MATCH state, oppGuild data null, termId: {}, guildId: {}, oppGuildId: {}", termId, guildId, oppGuildId);
					}
				}
			}
			break;
		case WAR:
			TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
			if (guildData == null) {
				break;
			} else {
				// 未匹配成功
				if (guildData.isMatchFailed()) {
					stateInfo.setState(TWState.NOT_OPEN);
					break;
				}
				String oppGuildId = guildData.getOppGuildId();
				// 匹配房间id为空
				if (HawkOSOperator.isEmptyString(oppGuildId)) {
					HawkLog.logPrintln("genPageInfo error on WAR state, oppGuildId is empty, termId: {}, guildId: {}", termId, guildId);
					stateInfo.setState(TWState.NOT_OPEN);
					break;
				}
				builder.setSelfGuild(guildData.build());
				if(guildData.isComplete() && stateInfo.getState() == TWState.WAR_OPEN){
					stateInfo.setState(TWState.WAR_FINISH);
				}
				TWGuildData oppGuildData = RedisProxy.getInstance().getTWGuildData(oppGuildId, termId);
				if (oppGuildData != null) {
					builder.setOppGuild(oppGuildData.build());
				} else {
					HawkLog.logPrintln("genPageInfo error on WAR state, oppGuild data null, termId: {}, guildId: {}, oppGuildId: {}", termId, guildId, oppGuildId);
				}
			}
			break;

		default:
			break;
		}
		builder.setStateInfo(stateInfo);
		return builder;
	}

	/**
	 * 同步跨服活动状态
	 * @param player
	 */
	public void syncStateInfo(Player player){
		TWPageInfo.Builder stateInfo = genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_INFO_SYNC, stateInfo));
	}

	/**
	 * 进入联盟军演房间
	 * @param player
	 * @return
	 */
	public boolean joinRoom(Player player) {
		String guildId = player.getGuildId();
		boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
		int termId = 0;
		int season = 0;
		int tlwTermId = 0;
		if (!isInLeaguaWar) {
			termId = activityInfo.getTermId();
		} else {
			TLWActivityData tlwActivityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
			termId = tlwActivityInfo.getMark();
			season = tlwActivityInfo.getSeason();
			tlwTermId = tlwActivityInfo.getTermId();
		}
		TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
		if (guildData == null) {
			return false;
		}
		HawkTuple2<TWRoomData, Integer> tuple = getPlayerRoomData(player);
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			HawkLog.logPrintln("TiberiumWarService enter room error, errorCode: {}, playerId: {}, termId: {}", status, player.getId(), activityInfo.termId);
			return false;
		}
		TWRoomData roomData = tuple.first;
		if (roomData == null) {
			HawkLog.logPrintln("TiberiumWarService enter room error, room not esixt, playerId: {}, termId: {}", player.getId(), activityInfo.termId);
			return false;
		}
		String roomId = roomData.getId();

		if (!TBLYRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("TiberiumWarService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId());
			return false;
		}
		TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(player.getId(), termId);
		if (twPlayerData == null) {
			HawkLog.logPrintln("TiberiumWarService enter room error, twPlayerData is null, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), player.getGuildId());
			return false;
		}

		if (!twPlayerData.getGuildId().equals(player.getGuildId())) {
			HawkLog.logPrintln(
					"TiberiumWarService enter room error, guildIds not match, playerId: {}, termId: {}, roomId: {}, roomServer: {}, twDataGuildId: {}, playerGuildId: {}",
					player.getId(), activityInfo.termId, roomId, roomData.getRoomServerId(), twPlayerData.getGuildId(), player.getGuildId());
			return false;
		}

		if (twPlayerData.getQuitTime()> 0) {
			HawkLog.logPrintln("TiberiumWarService enter room error, has entered, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), twPlayerData.getGuildId());
			return false;
		}
		if (!TBLYRoomManager.getInstance().joinGame(roomData.getId(), player)) {
			HawkLog.logPrintln("TiberiumWarService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), twPlayerData.getGuildId());
			return false;
		}
		twPlayerData.setEnterTime(HawkTime.getMillisecond());
		RedisProxy.getInstance().updateTWPlayerData(twPlayerData, termId);
		HawkLog.logPrintln("TiberiumWarService enter room, playerId: {}, season:{}, termId: {}, roomId: {}, roomServer: {}, guildId: {}, isLeagus: {}", player.getId(), season,
				isInLeaguaWar ? tlwTermId : termId, roomId, roomData.getRoomServerId(), twPlayerData.getGuildId(), isInLeaguaWar);
		if (isInLeaguaWar) {
			LogUtil.logTimberiumLeaguaEnterInfo(player.getId(), season, tlwTermId, roomId, roomData.getRoomServerId(), twPlayerData.getGuildId(), twPlayerData.getServerId(),
					player.getPower());
		} else {
			LogUtil.logTimberiumEnterInfo(player.getId(), activityInfo.getTermId(), roomId, roomData.getRoomServerId(), twPlayerData.getGuildId(), twPlayerData.getServerId(),
					player.getPower(),guildData.getGuildStrength());
		}
		return true;
	}
	
	/**
	 * 退出房间
	 * @param player
	 * @param isMidwayQuit 是否中途退出
	 * @return
	 */
	public boolean quitRoom(Player player, boolean isMidwayQuit) {
		String guildId = player.getGuildId();
		boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
		int termId = 0;
		int season = 0;
		int tlwTermId = 0;
		if (!isInLeaguaWar) {
			termId = activityInfo.getTermId();
		} else {
			TLWActivityData tlwActivityInfo = TiberiumLeagueWarService.getInstance().getActivityInfo();
			termId = tlwActivityInfo.getMark();
			season = tlwActivityInfo.getSeason();
			tlwTermId = tlwActivityInfo.getTermId();
		}
		TWPlayerData twPlayerData = RedisProxy.getInstance().getTWPlayerData(player.getId(), termId);
		if (twPlayerData == null) {
			HawkLog.logPrintln("TiberiumWarService quitRoom error, twPlayerData is null, playerId: {}, isMidwayQuit: {}", player.getId(), isMidwayQuit);
			return false;
		}
		if (twPlayerData.getEnterTime() == 0) {
			HawkLog.logPrintln("TiberiumWarService quitRoom error, has not entered, playerId: {}, guildId: {}, isMidwayQuit: {}", twPlayerData.getId(), twPlayerData.getGuildId(),
					isMidwayQuit);
			return false;
		}
		if (isMidwayQuit) {
			twPlayerData.setMidwayQuit(isMidwayQuit);
			twPlayerData.setQuitTime(HawkTime.getMillisecond());
			RedisProxy.getInstance().updateTWPlayerData(twPlayerData, termId);
		}
		HawkLog.logPrintln("TiberiumWarService quitRoom, playerId: {}, guildId: {}, season:{}, termId:{}, isMidwayQuit: {}, isLeagua: {}", twPlayerData.getId(),
				twPlayerData.getGuildId(), season, isInLeaguaWar ? tlwTermId : termId, isMidwayQuit, isInLeaguaWar);
		if (isInLeaguaWar) {
			LogUtil.logTimberiumLeaguaQuitInfo(player.getId(), season, tlwTermId, twPlayerData.getGuildId(), isMidwayQuit);
		} else {
			LogUtil.logTimberiumQuitInfo(player.getId(), termId, twPlayerData.getGuildId(), isMidwayQuit);
		}
		return true;
	}

	/**
	 * 获取期数信息
	 * @return
	 */
	public int getTermId() {
		return activityInfo.getTermId();
	}
	
	/**
	 * 获取活动信息
	 * @return
	 */
	public TWActivityData getActivityData(){
		return activityInfo;
	}
	
	/**
	 * 根据时间角标获取报名时间
	 * @param timeIndex
	 * @return
	 */
	public WarTimeChoose getWarTimeChoose( int timeIndex){
		List<WarTimeChoose> chooseList = getChooses();
		return chooseList.get(timeIndex);
	}

	/**
	 * 报名
	 * @param player
	 * @return
	 */
	public int signUp(Player player, int index) {
		
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		// 非报名阶段
		if (activityInfo.state != TWActivityState.SIGN) {
			return Status.Error.TIBERIUM_NOT_SIGN_STATE_VALUE;
		}

		if (signGuild.containsKey(guildId)) {
			return Status.Error.TIBERIUM_SIGNED_VALUE;
		}
		
		// 已参与联赛正赛,不能进行报名
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player)){
			return Status.Error.TIBERIUM_LEAGUA_CANNOT_SIGNUP_VALUE;
		}
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		
		if(HawkTime.getMillisecond() - guild.getCreateTime() < TiberiumConstCfg.getInstance().getGuildCreateTimeLimit()){
			return Status.Error.TIBERIUM_GUILD_NEW_VALUE;
		}
		int rankLimit = TiberiumConstCfg.getInstance().getSignRankLimit();
		RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, guildId);
		// 联盟战力排名限制
		if (rankInfo == null || rankInfo.getRank() > rankLimit) {
			return Status.Error.TIBERIUM_RANK_NOT_ENOUGH_VALUE;
		}
		int maxSignNum = TiberiumConstCfg.getInstance().getMaxSignNum();
		List<String> signGuilds = RedisProxy.getInstance().getTWSignInfo(activityInfo.termId, index);
		// 该时段报名联盟数量过多
		if (signGuilds != null && signGuilds.size() >= maxSignNum) {
			return Status.Error.TIBERIUM_TIME_FIERY_VALUE;
		}
		RedisProxy.getInstance().addTWSignInfo(guildId, activityInfo.termId, index);
		signGuild.put(guildId, index);
		HawkLog.logPrintln("TiberiumWar sign success,termId: {}, guildId: {}, timeIndex: {}, guildRank: {}, playerId:{}", activityInfo.termId, guildId, index, rankInfo.getRank(),
				player.getId());
		TWPageInfo.Builder stateInfo = genPageInfo(player);
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_INFO_SYNC, stateInfo));
		int memberCnt = GuildService.getInstance().getGuildMemberNum(guildId);
		long totalPowar = GuildService.getInstance().getGuildBattlePoint(guildId);
		LogUtil.logTimberiumSignup(guildId, guild.getName(), memberCnt, totalPowar, GsConfig.getInstance().getServerId(), activityInfo.termId, index, player.getId());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 构建联盟成员列表
	 * @param player
	 * @return
	 */
	public TWPlayerList.Builder getMemberList(String guildId) {
		String serverId = GsConfig.getInstance().getServerId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return null;
		}
		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		TWPlayerList.Builder builder = TWPlayerList.newBuilder();
		for (String memberId : memberIds) {
			Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(memberId);
			if (memberPlayer == null || member == null) {
				continue;
			}
			TWPlayerInfo.Builder playerInfo = TWPlayerInfo.newBuilder();
			playerInfo.setId(memberId);
			playerInfo.setName(memberPlayer.getName());
			playerInfo.setServerId(serverId);
			playerInfo.setIcon(memberPlayer.getIcon());
			String pfIcon = memberPlayer.getPfIcon();
			if (!HawkOSOperator.isEmptyString(pfIcon)) {
				playerInfo.setPfIcon(pfIcon);
			}
			playerInfo.setGuildId(guildId);
			playerInfo.setGuildTag(guild.getTag());
			playerInfo.setAuth(member.getAuthority());
			playerInfo.setBattlePoint(member.getPower());
			playerInfo.setIsJoin(idList.contains(memberId));
			playerInfo.setGuildOfficer(member.getOfficeId());
			builder.addPlayerInfo(playerInfo);
		}
		return builder;
	}
	
	/**
	 * 更新参战玩家列表
	 * @param player
	 * @param idList
	 * @return
	 */
	public int updateMemberList(Player player, TWPlayerManage req) {

		String targetId = req.getPlayerId();
		int type = req.getType();
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		String guildId = player.getGuildId();
		if (!GuildService.getInstance().isInTheSameGuild(player.getId(), targetId)) {
			return Status.Error.GUILD_NOT_SAME_VALUE;
		}

		TLWActivityData tlwActivityData = TiberiumLeagueWarService.getInstance().getActivityInfo();
		Map<String, TLWGroupType> joindGuilds = TiberiumLeagueWarService.getInstance().getJoinGuilds();
		TLWGroupType group = joindGuilds.get(guildId);
		if (group == null) {
			group = TLWGroupType.NORMAL;
		}
		boolean inSeasonWar = false;
		if (tlwActivityData.getState() != TLWActivityState.TLW_NOT_OPEN && tlwActivityData.getState() != TLWActivityState.TLW_CLOSE) {
			if (group == TLWGroupType.NORMAL || group == TLWGroupType.KICK_OUT) {
				inSeasonWar = false;
			} else {
				inSeasonWar = true;
			}

		}
		// 若在本次联赛战斗序列中
		if (inSeasonWar) {
			if (tlwActivityData.getState().getNumber() >= TLWActivityState.TLW_WAR_WAIT.getNumber()) {
				return Status.Error.TIBERIUM_LEAGUA_CANNOT_MANAGE_STATE_VALUE;
			}
			if(!TiberiumLeagueWarService.getInstance().checkMemberJoin(targetId, guildId)){
				return Status.Error.TIBERIUM_LEAGUA_JOIN_WIN_VALUE;
			}
		} else {
			// 当前阶段不能进行出战人员变更
			if (activityInfo.state == TWActivityState.MATCH || activityInfo.state == TWActivityState.WAR) {
				return Status.Error.TIBERIUM_CANNOT_MANAGE_STATE_VALUE;
			}
		}

		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		// 添加出战
		if (type == 1) {
			if (idList.contains(targetId)) {
				return Status.SysError.SUCCESS_OK_VALUE;
			}
			// 超过出战人数上限
			if (idList.size() + 1 > TiberiumConstCfg.getInstance().getWarMemberLimit()) {
				return Status.Error.TIBERIUM_PLAYER_OVER_LIMIT_VALUE;
			}
		}
		RedisProxy.getInstance().updateTWPlayerIds(guildId, type, targetId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 联盟解散
	 * @param guildId
	 */
	public void onGuildDismiss(String guildId) {
		try {
			Integer index = signGuild.get(guildId);
			if (index != null) {
				signGuild.remove(guildId);
				guildTeamDatas.remove(guildId);
				RedisProxy.getInstance().removeTWSignInfo(guildId, activityInfo.termId, index);
				RedisProxy.getInstance().removeTWPlayerIds(guildId);
				RedisProxy.getInstance().removeTWBattleLog(guildId);
				RedisProxy.getInstance().removeTWGuildTeamData(guildId);
				RedisProxy.getInstance().removeTWGuildElo(guildId);

			}
			// 移除联赛相关信息
			int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
			RedisProxy.getInstance().removeTLWGuildData(guildId, season);
			RedisProxy.getInstance().removeTLWGuildPowerRank(season, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 联赛入围联盟产生后,检测并移除普同泰伯利亚比赛的报名
	 */
	public void checkTWSignGuild(Collection<String> guildIds) {
		if (activityInfo.getState() != TWActivityState.SIGN) {
			return;
		}
		if (CollectionUtils.isEmpty(guildIds)) {
			return;
		}
		for (String guildId : guildIds) {
			if (!signGuild.containsKey(guildId)) {
				continue;
			}
			int index = signGuild.get(guildId);
			RedisProxy.getInstance().removeTWSignInfo(guildId, activityInfo.termId, index);
			signGuild.remove(guildId);
			HawkLog.logPrintln("TiberiumWarService chackTWSignGuild, guildId:{}", guildId);
		}
	}
	
	/**
	 * 成员退出联盟
	 * @param player
	 * @param guildId
	 */
	public void onQuitGuild(Player player, String guildId){
		try {
			RedisProxy.getInstance().removeTWPlayerId(guildId, player.getId());
			syncStateInfo(player);
			player.msgCall(MsgId.TIBERIUM_MEMBER_QUIT, TiberiumWarService.getInstance(),
					new TWMemberQuitInvoker(player, guildId));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 根据玩家获取匹配房间信息
	 * @param player
	 * @return
	 */
	public HawkTuple2<TWRoomData, Integer> getPlayerRoomData(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<TWRoomData, Integer>(null, Status.Error.GUILD_NO_JOIN_VALUE);
		}
		
		boolean isInLeaguaWar = TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId);
		int termId = 0;
		if (isInLeaguaWar) {
			termId = TiberiumLeagueWarService.getInstance().getActivityInfo().getMark();
		} else {
			termId = activityInfo.getTermId();
		}

		TWPlayerData twPlayer = RedisProxy.getInstance().getTWPlayerData(player.getId(), termId);
		if (twPlayer == null) {
			return new HawkTuple2<TWRoomData, Integer>(null, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE);
		}
		TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, termId);
		if (guildData == null) {
			return new HawkTuple2<TWRoomData, Integer>(null, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE);
		}
		String roomId = guildData.getRoomId();
		if (HawkOSOperator.isEmptyString(guildData.getRoomId())) {
			return new HawkTuple2<TWRoomData, Integer>(null, Status.Error.TIBERIUM_HAS_NO_MATCH_INFO_VALUE);
		}
		TWRoomData roomData = RedisProxy.getInstance().getTWRoomData(roomId, termId);
		return new HawkTuple2<TWRoomData, Integer>(roomData, Status.SysError.SUCCESS_OK_VALUE);
	}
	
	/**
	 * 战场结束
	 * @param msg
	 */
	@MessageHandler
	private void onBattleFinish(TBLYBilingInformationMsg msg) {
		String roomId = msg.getRoomId();
		int season = TiberiumLeagueWarService.getInstance().getActivityInfo().getSeason();
		int termId = activityInfo.termId;
		TWRoomData roomData = RedisProxy.getInstance().getTWRoomData(roomId, termId);
		if (roomData == null) {
			HawkLog.logPrintln("TiberiumWarService onBattleFinish error, room data null, termId: {}, roomId: {}", termId, roomId);
			return;
		}
		roomData.setRoomState(RoomState.CLOSE);
		RedisProxy.getInstance().updateTWRoomData(roomData, termId);
		String guildA = roomData.getGuildA();
		long scoreA = msg.getGuildHonor(guildA);
		String guildB = roomData.getGuildB();
		long scoreB = msg.getGuildHonor(guildB);
		// 记录联盟积分数据
		TWGuildData guildDataA = RedisProxy.getInstance().getTWGuildData(guildA, termId);
		TWGuildData guildDataB = RedisProxy.getInstance().getTWGuildData(guildB, termId);
		// 联盟ELO数据
		TWGuildEloData eloDataA = RedisProxy.getInstance().getTWGuildElo(guildA);
		TWGuildEloData eloDataB = RedisProxy.getInstance().getTWGuildElo(guildB);
		int eloBefA = eloDataA.getScore();
		int eloBefB = eloDataB.getScore();
		
		guildDataA.setScore(scoreA);
		guildDataA.setComplete(true);
		guildDataB.setScore(scoreB);
		guildDataB.setComplete(true);
		//赛季开启,记录玩家和联盟积分
		if (season > 0) {
			long guildScoreLimit = TiberiumConstCfg.getInstance().getSeasonGuildScoreLimit();
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
				TWPlayerData playerDate = RedisProxy.getInstance().getTWPlayerData(recod.getPlayerId(), termId);
				if (playerDate != null) {
					playerDate.setScore(recod.getHonor());
					RedisProxy.getInstance().updateTWPlayerData(playerDate, termId);
					playerScoreLogList.add(new TWPlayerScoreLogUnit(playerDate.getId(), termId, playerDate.getGuildId(), recod.getHonor()));
					//赛季开启,记录玩家和联盟积分
					if (season > 0) {
						TLWScoreData playerScore = RedisProxy.getInstance().getTLWPlayerScoreInfo(season, playerId);
						long scoreBef = playerScore.getScore();
						long warScore = recod.getHonor();
						long addScore = Math.min(personScoreLimit, warScore);
						playerScore.setScore(playerScore.getScore() + addScore);
						RedisProxy.getInstance().updateTLWPlayerScoreInfo(season, playerScore);
						HawkLog.logPrintln(
								"TiberiumWarService  playerSeasonScoreSdd, playerId: {}, guildId: {}, season: {}, termId:{}, serverId: {}, roomId: {}, roomServer: {}, scoreBef: {}, warScore: {}, addScore: {}, scoreAft: {}",
								playerDate.getId(), playerDate.getGuildId(), season, termId, playerDate.getServerId(), roomId, roomData.getRoomServerId(), scoreBef, warScore,
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
		if (HawkOSOperator.isEmptyString(winGuild)) {
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
			guildDataA.setEloChange(eloChange);
			guildDataB.setEloChange(-eloChange);
		} else {
			eloDataA.setScore(eloDataA.getScore() - eloChange);
			eloDataB.setScore(eloDataB.getScore() + eloChange);
			guildDataA.setEloChange(-eloChange);
			guildDataB.setEloChange(eloChange);
		}
		
		roomData.setWinnerId(winGuild);
		roomData.setScoreA(scoreA);
		roomData.setScoreB(scoreB);
		guildDataA.setWin(winGuild.equals(guildA));
		guildDataB.setWin(winGuild.equals(guildB));
		RedisProxy.getInstance().updateTWGuildData(guildDataA, termId);
		RedisProxy.getInstance().updateTWGuildData(guildDataB, termId);
		RedisProxy.getInstance().updateTWGuildElo(eloDataA);
		RedisProxy.getInstance().updateTWGuildElo(eloDataB);
		RedisProxy.getInstance().updateTWRoomData(roomData, termId);
		try {
			TWGuildScoreLogUnit gScoreLogUnitA = new TWGuildScoreLogUnit(guildDataA.getId(), guildDataA.getName(), termId, guildDataA.getServerId(), roomId, roomData.getRoomServerId(), guildDataA.getScore(),
					guildDataA.getMemberCnt(), guildDataA.getTotalPower(), guildDataA.isWin());
			TWGuildScoreLogUnit gScoreLogUnitB = new TWGuildScoreLogUnit(guildDataB.getId(), guildDataB.getName(), termId, guildDataB.getServerId(), roomId, roomData.getRoomServerId(), guildDataB.getScore(),
					guildDataB.getMemberCnt(), guildDataB.getTotalPower(), guildDataB.isWin());
			TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitA);
			TWLogUtil.logTimberiumGuildScoreInfo(gScoreLogUnitB);
			TWEloScoreLogUnit eloScoreUnitA = new TWEloScoreLogUnit(guildA, activityInfo.getTermId(), eloBefA, eloDataA.getScore(), eloDataA.getScore() - eloBefA, EloReason.WAR_CALC);
			TWEloScoreLogUnit eloScoreUnitB = new TWEloScoreLogUnit(guildB, activityInfo.getTermId(), eloBefB, eloDataB.getScore(), eloDataB.getScore() - eloBefB, EloReason.WAR_CALC);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnitA);
			TWLogUtil.logTimberiumEloScoreInfo(eloScoreUnitB);
			
			HawkLog.logPrintln(
					"TiberiumWarService  guildScoreInfo, guildId: {}, guildName: {} , termId: {}, serverId: {}, roomId: {}, roomServer: {}, score: {}, memberCnt: {}, totalPower: {}, isWin: {}",
					guildDataA.getId(), guildDataA.getName(), termId, guildDataA.getServerId(), roomId, roomData.getRoomServerId(), guildDataA.getScore(),
					guildDataA.getMemberCnt(), guildDataA.getTotalPower(), guildDataA.isWin());
			HawkLog.logPrintln(
					"TiberiumWarService  guildScoreInfo, guildId: {}, guildName: {} , termId: {}, serverId: {}, roomId: {}, roomServer: {}, score: {}, memberCnt: {}, totalPower: {}, isWin: {}",
					guildDataB.getId(), guildDataB.getName(), termId, guildDataB.getServerId(), roomId, roomData.getRoomServerId(), guildDataB.getScore(),
					guildDataB.getMemberCnt(), guildDataB.getTotalPower(), guildDataB.isWin());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 记录战斗记录
		TWBattleLog.Builder builder = TWBattleLog.newBuilder();
		builder.setTermId(termId);
		builder.setRoomId(roomId);
		builder.setWinGuild(winGuild);
		WarTimeChoose timeChose = getWarTimeChoose(roomData.getTimeIndex());
		builder.setTime(timeChose.getTime());
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
	}
	
	/**
	 * 判断当前联盟可否进行部分操作
	 * @param guildId
	 * @return
	 */
	public boolean checkGuildOperation(String guildId) {

		if (TiberiumLeagueWarService.getInstance().isJointLeaguaWar(guildId)) {
			// 正在参与泰伯利亚联赛,且当前阶段过了管理阶段
			if (TiberiumLeagueWarService.getInstance().getActivityInfo().getState().getNumber() > TLWActivityState.TLW_WAR_MANGE.getNumber()) {
				return false;
			} else {
				return true;
			}
		} else {
			if (activityInfo.state == TWActivityState.CLOSE || activityInfo.state == TWActivityState.SIGN) {
				return true;
			}
			Integer timeIndex = signGuild.get(guildId);
			if (timeIndex == null) {
				return true;
			}
			if (activityInfo.state == TWActivityState.WAR) {
				WarTimeChoose timeChoose = getWarTimeChoose(timeIndex);
				if (timeChoose == null) {
					return true;
				}
			}
			TWGuildData guildData = RedisProxy.getInstance().getTWGuildData(guildId, activityInfo.termId);
			// 匹配失败
			if (guildData != null && guildData.isMatchFailed()) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * 刷新缓存中的联盟小队变更信息
	 */
	public void updateGuildTeamChanges() {
		try {
			if (needUpdateGuildIds.isEmpty()) {
				return;
			}
			int totalCnt = needUpdateGuildIds.size();
			int failCnt = 0;
			for (String guildId : needUpdateGuildIds) {
				try {
					TWGuildTeamData guildTeamData = guildTeamDatas.get(guildId);
					if (guildTeamData != null) {
						RedisProxy.getInstance().updateTWGuildTeamData(guildTeamData);
					}
				} catch (Exception e) {
					HawkException.catchException(e);
					failCnt ++;
				}
			}
			HawkLog.logPrintln("TiberiumWarService updateGuildTeamChanges, totalCnt: {}, failedCnt: {}", totalCnt, failCnt);
			needUpdateGuildIds = new HashSet<>();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取联盟小队信息
	 * @param guildId
	 * @return
	 */
	private Optional<TWGuildTeamData> getGuildTeamData(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Optional.empty();
		}
		TWGuildTeamData guildTeamData = guildTeamDatas.get(guildId);
		if(guildTeamData == null){
			guildTeamData = new TWGuildTeamData();
			guildTeamData.setGuildId(guildId);
			guildTeamDatas.put(guildId, guildTeamData);
			RedisProxy.getInstance().updateTWGuildTeamData(guildTeamData);
		}
		return Optional.ofNullable(guildTeamData);
	}

	
	/**
	 * 修改小队名字
	 * @param player
	 * @param teamIndex
	 * @param name
	 * @param hpCode
	 */
	public int onEditTeamName(Player player, int teamIndex, String name, int hpCode) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			return Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE;
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		if (teamData == null) {
			teamData = new TWTeamData();
			teamData.setTeamIndex(teamIndex);
			guildTeamData.updataTwTeamData(teamData);
		}
		teamData.setName(name);
		needUpdateGuildIds.add(guildId);

		HawkTuple2<Integer, TWTeamInfo.Builder> tuple = genTeamInfo(guildId, teamIndex);
		if(tuple.first == Status.SysError.SUCCESS_OK_VALUE){
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_EDIT_TEAM_NAME_S, tuple.second));
			LogUtil.logSecTalkFlow(player, null, LogMsgType.TIBER_TEAM_NAME, guildId + "_" + teamIndex, name);
		}else{
			HawkLog.logPrintln("TiberiumWarService onEditTeamName response failed, playerId: {}, name: {}, errorCode: {}", player.getId(), player.getName(), tuple.first);
		}
		return Status.SysError.SUCCESS_OK_VALUE;

	}
	
	/**
	 * 修改小组目标
	 * @param player
	 * @param teamIndex
	 * @param targets
	 * @param hpCode
	 */
	public int onEditTeamTarget(Player player, int teamIndex, List<Integer> targets, int hpCode) {
		int teamLimit = TiberiumConstCfg.getInstance().getTeamNumLimit();
		// 小组id错误
		if (teamIndex > teamLimit || teamIndex <= 0) {
			return Status.Error.TIBERIUM_TEAMINDEX_ERROR_VALUE;
		}
		int targetLimit = TiberiumConstCfg.getInstance().getTeamTargetLimit();
		// 小组目标数量超标
		if (targets.size() > targetLimit) {
			return Status.Error.TIBERIUM_TEAM_TARGET_OVER_LIMIT_VALUE;
		}
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		// 信息异常
		if (!opData.isPresent()) {
			return Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE;
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		if (teamData == null) {
			teamData = new TWTeamData();
			teamData.setTeamIndex(teamIndex);
			guildTeamData.updataTwTeamData(teamData);
		}
		List<Integer> targetList = new ArrayList<>();
		if (!targets.isEmpty()) {
			targetList.addAll(targets);
		}
		teamData.setTargetList(targetList);
		needUpdateGuildIds.add(guildId);

		HawkTuple2<Integer, TWTeamInfo.Builder> tuple = genTeamInfo(guildId, teamIndex);
		if (tuple.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_EDIT_TEAM_TARGET_S, tuple.second));
		} else {
			HawkLog.logPrintln("TiberiumWarService onEditTeamTarget response failed, playerId: {}, name: {}, errorCode: {}", player.getId(), player.getName(), tuple.first);
		}
		return Status.SysError.SUCCESS_OK_VALUE;

	}
	
	/**
	 * 修改小组成员策略
	 * @param player
	 * @param teamIndex
	 * @param memberId
	 * @param targets
	 * @param hpCode
	 * @return
	 */
	public int onEditMemberTarget(Player player, int teamIndex, String memberId, List<Integer> targets, int hpCode) {
		int teamLimit = TiberiumConstCfg.getInstance().getTeamNumLimit();
		// 小组id错误
		if (teamIndex > teamLimit || teamIndex <= 0) {
			return Status.Error.TIBERIUM_TEAMINDEX_ERROR_VALUE;
		}
		int targetLimit = TiberiumConstCfg.getInstance().getTeamMemberStrategyLimit();
		// 成员目标数量超标
		if (targets.size() > targetLimit) {
			return Status.Error.TIBERIUM_MEMBER_TARGET_OVER_LIMIT_VALUE;
		}
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			return Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE;
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		// 小组信息不存在
		if (teamData == null) {
			return Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE;
		}
		
		TWTeaMemberData memberData = teamData.getMemberMap().get(memberId);
		// 非本小组成员
		if(memberData == null){
			return Status.Error.TIBERIUM_NOT_MEMBER_VALUE;
		}
		
		List<Integer> targetList = new ArrayList<>();
		if (!targets.isEmpty()) {
			targetList.addAll(targets);
		}
		memberData.setTargetList(targetList);
		needUpdateGuildIds.add(guildId);
		HawkTuple2<Integer, TWTeamInfo.Builder> tuple = genTeamInfo(guildId, teamIndex);
		if(tuple.first == Status.SysError.SUCCESS_OK_VALUE){
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_EDIT_MEMBER_TARGET_S_VALUE, tuple.second));
		}else{
			HawkLog.logPrintln("TiberiumWarService onEditMemberTarget response failed, playerId: {}, name: {}, errorCode: {}", player.getId(), player.getName(), tuple.first);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 获取小队信息
	 * @param player
	 * @param teamIndex
	 * @return
	 */
	public HawkTuple2<Integer, TWTeamInfo.Builder> getTeamInfo(Player player, int teamIndex) {
		int teamLimit = TiberiumConstCfg.getInstance().getTeamNumLimit();
		// 小组id错误
		if (teamIndex > teamLimit || teamIndex <= 0) {
			return new HawkTuple2<Integer, TWTeamInfo.Builder>(Status.Error.TIBERIUM_TEAMINDEX_ERROR_VALUE, null);
		}

		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, TWTeamInfo.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		if (activityInfo.getState() == TWActivityState.WAR) {
			TWGuildTeamInfo info = guildTeamInfos.get(guildId);
			if (info == null) {
				info = RedisProxy.getInstance().getTWGuildTeamBuild(guildId);
				if (info != null) {
					guildTeamInfos.put(guildId, info);
				}
			}
			TWTeamInfo.Builder builder = null;
			if (info != null) {
				for (TWTeamInfo teamInfo : info.getTeamInfoList()) {
					if (teamInfo.getIndex() == teamIndex) {
						builder = teamInfo.toBuilder();
					}
				}
			}
			if (builder == null) {
				builder = TWTeamInfo.newBuilder();
				builder.setTeamName("");
				builder.setIndex(teamIndex);
			}
			return new HawkTuple2<Integer, TWTeamInfo.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
		} else {
			// 构建数据
			return genTeamInfo(guildId, teamIndex);
		}
	}
	
	/**
	 * 获取小队信息
	 * @param player
	 * @param teamIndex
	 * @return
	 */
	public HawkTuple2<Integer, TWGetTeamSummaryResp.Builder> getTeamSummaryInfo(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		if (activityInfo.getState() == TWActivityState.WAR) {
			TWGuildTeamInfo info = guildTeamInfos.get(guildId);
			if (info == null) {
				info = RedisProxy.getInstance().getTWGuildTeamBuild(guildId);
				if (info != null) {
					guildTeamInfos.put(guildId, info);
				}
			}
			TWGetTeamSummaryResp.Builder builder = TWGetTeamSummaryResp.newBuilder();
			if (info != null) {
				return genSummary(player, info);
			}
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
		} else {
			// 构建数据
			return genSummary(player);
		}
	}
	
	/**
	 * 根据构建信息,组装玩家小队任务数据
	 * @param player
	 * @param info
	 * @return
	 */
	private HawkTuple2<Integer, TWGetTeamSummaryResp.Builder> genSummary(Player player, TWGuildTeamInfo info) {
		TWGetTeamSummaryResp.Builder builder = TWGetTeamSummaryResp.newBuilder();
		String playerId = player.getId();
		List<TWTeamInfo> teamInfos = info.getTeamInfoList();
		if (teamInfos.isEmpty()) {
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
		}

		for (TWTeamInfo teamInfo : teamInfos) {
			List<TWTeamMember> members = teamInfo.getMemberInfoList();
			if (members.isEmpty()) {
				continue;
			}
			TWTeamMember self = null;
			TWTeamMember captain = null;
			for (TWTeamMember member : members) {
				if (member.getIsCaptain()) {
					captain = member;
				}
				if (member.getId().equals(playerId)) {
					self = member;
				}
			}
			TeamSummary.Builder summaryBuilder = TeamSummary.newBuilder();
			summaryBuilder.setTeamIndex(teamInfo.getIndex());
			summaryBuilder.setTeamName(teamInfo.getTeamName());
			if (captain != null) {
				summaryBuilder.setLeaderName(captain.getName());
			}
			List<Integer> teamTarget = teamInfo.getTeamTargetList();
			if (!teamTarget.isEmpty()) {
				summaryBuilder.addAllTeamTarget(teamTarget);
			}
			List<Integer> memberTarget = self.getMemberTargetList();
			if (!memberTarget.isEmpty()) {
				summaryBuilder.addAllMemberTarget(memberTarget);
			}
			builder.addSummary(summaryBuilder);
		}
		return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 通过本服数据构建个人小队任务列表
	 * @param guildId
	 * @return
	 */
	private HawkTuple2<Integer, TWGetTeamSummaryResp.Builder> genSummary(Player player) {
		String guildId = player.getGuildId();
		String playerId = player.getId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE, null);
		}
		TWGetTeamSummaryResp.Builder builder = TWGetTeamSummaryResp.newBuilder();
		TWGuildTeamData guildTeamData = opData.get();
		Map<Integer, TWTeamData> map = guildTeamData.getTeamInfos();
		if (map.isEmpty()) {
			return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
		}
		for (Entry<Integer, TWTeamData> entry : map.entrySet()) {
			int teamIndex = entry.getKey();
			TWTeamData teamData = entry.getValue();
			Map<String, TWTeaMemberData> members = teamData.getMemberMap();
			TWTeaMemberData selfData = members.get(playerId);
			if (selfData == null) {
				continue;
			}
			TeamSummary.Builder summaryBuilder = TeamSummary.newBuilder();
			List<Integer> memberTarget = selfData.getTargetList();
			summaryBuilder.setTeamIndex(teamIndex);
			summaryBuilder.setTeamName(teamData.getName());
			List<Integer> teamTarget = teamData.getTargetList();
			if (!teamTarget.isEmpty()) {
				summaryBuilder.addAllTeamTarget(teamTarget);
			}
			if (!memberTarget.isEmpty()) {
				summaryBuilder.addAllMemberTarget(memberTarget);
			}
			TWTeaMemberData captain = null;
			for (TWTeaMemberData memberData : members.values()) {
				if (memberData.isCaptain()) {
					captain = memberData;
					break;
				}
			}
			if (captain != null) {
				String captainId = captain.getId();
				Player captainPlayer = GlobalData.getInstance().makesurePlayer(captainId);
				if (captainPlayer != null) {
					summaryBuilder.setLeaderName(captainPlayer.getName());
				}
			}
			builder.addSummary(summaryBuilder);
		}
		return new HawkTuple2<Integer, TWGetTeamSummaryResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 获取小组管理界面信息
	 * @param player
	 * @param teamIndex
	 * @return
	 */
	public HawkTuple2<Integer, TWTeamManageInfoResp.Builder> getTeamMangeInfo(Player player, int teamIndex) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, TWTeamManageInfoResp.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			return new HawkTuple2<Integer, TWTeamManageInfoResp.Builder>(Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE, null);
		}
		TWTeamManageInfoResp.Builder builder = TWTeamManageInfoResp.newBuilder();
		builder.setTeamIndex(teamIndex);
		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		// 无人出战
		if (idList == null || idList.isEmpty()) {
			return new HawkTuple2<Integer, TWTeamManageInfoResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		Map<String, TWTeaMemberData> memberMap = new HashMap<>();
		if (teamData != null) {
			memberMap = teamData.getMemberMap();
		}
		List<TWTeamMember> memberList = new ArrayList<>();
		for (String memberId : idList) {
			TWTeaMemberData memberData = memberMap.get(memberId);
			TWTeamMember.Builder memberBuilder = genMemberInfo(memberId, memberData);
			if (memberBuilder == null) {
				continue;
			}
			memberList.add(memberBuilder.build());
		}
		if (!memberList.isEmpty()) {
			builder.addAllMember(memberList);
		}
		return new HawkTuple2<Integer, TWTeamManageInfoResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}
	
	/**
	 * 本服构建小组信息数据
	 * @param player
	 * @param teamIndex
	 * @return
	 */
	private HawkTuple2<Integer, TWTeamInfo.Builder> genTeamInfo(String guildId, int teamIndex) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, TWTeamInfo.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			return new HawkTuple2<Integer, TWTeamInfo.Builder>(Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE, null);
		}
		TWTeamInfo.Builder builder = TWTeamInfo.newBuilder();
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		if (teamData == null) {
			builder.setIndex(teamIndex);
			builder.setTeamName("");
		} else {
			List<TWTeamMember> memberInfos = new ArrayList<>();
			for (Entry<String, TWTeaMemberData> entry : teamData.getMemberMap().entrySet()) {
				String id = entry.getKey();
				TWTeaMemberData memberData = entry.getValue();
				TWTeamMember.Builder memberBuilder = genMemberInfo(id, memberData);
				if (memberBuilder == null) {
					continue;
				}
				memberInfos.add(memberBuilder.build());
			}

			if (!memberInfos.isEmpty()) {
				builder.addAllMemberInfo(memberInfos);
			}

			builder.setTeamName(teamData.getName());
			builder.setIndex(teamData.getTeamIndex());
			List<Integer> targetList = teamData.getTargetList();
			if (!targetList.isEmpty()) {
				builder.addAllTeamTarget(targetList);
			}
		}
		return new HawkTuple2<Integer, TiberiumWar.TWTeamInfo.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	
	}

	/**
	 * 构建成员信息
	 * @param memberId
	 * @param memberData
	 * @return
	 */
	private TWTeamMember.Builder genMemberInfo(String memberId, TWTeaMemberData memberData) {
		TWTeamMember.Builder memberBuilder = TWTeamMember.newBuilder();
		GuildMemberObject memberObj = GuildService.getInstance().getGuildMemberObject(memberId);
		Player member = GlobalData.getInstance().makesurePlayer(memberId);
		if (memberObj == null || member == null) {
			HawkLog.errPrintln("TiberiumWarService genMemberInfo failed, memberId: {}, memberObjIsNull: {}, memberIsNull", memberId, memberObj == null, member == null);
			return null;
		}
		memberBuilder.setId(memberId);
		memberBuilder.setName(memberObj.getPlayerName());
		memberBuilder.setIcon(member.getIcon());
		String pfIcon = member.getPfIcon();
		if (HawkOSOperator.isEmptyString(pfIcon)) {
			memberBuilder.setPfIcon(pfIcon);
		}
		memberBuilder.setAuth(memberObj.getAuthority());
		memberBuilder.setGuildOfficer(memberObj.getOfficeId());
		memberBuilder.setBattlePoint(member.getPower());
		Set<SoldierType> mainForce = member.getMainForce();
		if (!mainForce.isEmpty()) {
			memberBuilder.addAllMainSoldier(mainForce);
		}
		if (memberData != null) {

			List<Integer> targetList = memberData.getTargetList();
			if (!targetList.isEmpty()) {
				memberBuilder.addAllMemberTarget(targetList);
			}
			memberBuilder.setIsCaptain(memberData.isCaptain());
			memberBuilder.setIsTeamMember(true);
		} else {
			memberBuilder.setIsTeamMember(false);
		}
		return memberBuilder;
	}

	public void onMemberManage(Player player, int teamIndex, TWMemberMangeType type, String memberId, int hpCode) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			player.sendError(hpCode, Status.Error.GUILD_NO_JOIN_VALUE, 0);
			return ;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		if (!opData.isPresent()) {
			player.sendError(hpCode, Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE, 0);
			return ;
		}
		TWTeamManageInfoResp.Builder builder = TWTeamManageInfoResp.newBuilder();
		builder.setTeamIndex(teamIndex);
		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		// 无人出战
		if (idList == null || idList.isEmpty()) {
			player.sendError(hpCode, Status.Error.TIBERIUM_NO_MEMBER_JOIN_VALUE, 0);
			return ;
		}
		// 成员没有出战
		if (!idList.contains(memberId)) {
			player.sendError(hpCode, Status.Error.TIBERIUM_NOT_IN_THIS_WAR_VALUE, 0);
			return ;
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		Map<String, TWTeaMemberData> memberMap = new HashMap<>();
		TWTeaMemberData captain = null;
		TWTeaMemberData targetMember = memberMap.get(memberId);
		if (targetMember == null) {
			targetMember = new TWTeaMemberData();
			targetMember.setId(memberId);
			targetMember.setTeamIndex(teamIndex);
		}
		if (teamData != null) {
			memberMap = teamData.getMemberMap();
			for (TWTeaMemberData member : memberMap.values()) {
				if (member.isCaptain()) {
					captain = member;
				}
			}
		} else {
			teamData = new TWTeamData();
			teamData.setTeamIndex(teamIndex);
			teamData.setName("");
			teamData.setMemberMap(memberMap);
			guildTeamData.updataTwTeamData(teamData);
		}
		
		TWTeamMember.Builder memberBuilder = null;
		switch (type) {
		case CANCEL:
			memberMap.remove(memberId);
			memberBuilder = genMemberInfo(memberId, null);
			break;
		case MIX_IN:
			int memberLimit = TiberiumConstCfg.getInstance().getTeamMemberLimit();
			// 超出上限
			if (memberMap.size() >= memberLimit && !memberMap.containsKey(memberId)) {
				player.sendError(hpCode, Status.Error.TIBERIUM_MEMBER_OVER_LIMIT_VALUE, 0);
				return ;
			}
			memberMap.put(memberId, targetMember);
			memberBuilder = genMemberInfo(memberId, targetMember);
			break;
		case SET_CAPTAIN:
			// 已存在队长
			if (captain != null && !captain.getId().equals(memberId)) {
				player.sendError(hpCode, Status.Error.TIBERIUM_CAPTAIN_EXIST_VALUE, 0);
				return ;
			}
			targetMember.setCaptain(true);
			memberMap.put(memberId, targetMember);
			memberBuilder = genMemberInfo(memberId, targetMember);
			break;
		}
		needUpdateGuildIds.add(guildId);
		if(type == TWMemberMangeType.CANCEL){
			memberBuilder.setIsTeamMember(false);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_TEAM_MEMBER_MANAGE_S_VALUE, memberBuilder));
	}
	
	/**
	 * 重置小组成员
	 * @param player
	 * @param teamIndex
	 * @param hpCode
	 */
	public int onTeamMemberReset(Player player, int teamIndex, int hpCode) {
		int teamLimit = TiberiumConstCfg.getInstance().getTeamNumLimit();
		// 小组id错误
		if (teamIndex > teamLimit || teamIndex <= 0) {
			return Status.Error.TIBERIUM_TEAMINDEX_ERROR_VALUE;
		}
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		// 信息异常
		if (!opData.isPresent()) {
			return Status.Error.TIBERIUM_TEAMDATA_ERROR_VALUE;
		}
		TWGuildTeamData guildTeamData = opData.get();
		TWTeamData teamData = guildTeamData.getTwTeamData(teamIndex);
		if (teamData == null) {
			teamData = new TWTeamData();
			teamData.setTeamIndex(teamIndex);
			guildTeamData.updataTwTeamData(teamData);
		}
		teamData.resetMemberMap();
		needUpdateGuildIds.add(guildId);
	
		HawkTuple2<Integer, TWTeamManageInfoResp.Builder> tuple = getTeamMangeInfo(player, teamIndex);
		if (tuple.first == Status.SysError.SUCCESS_OK_VALUE) {
			player.sendProtocol(HawkProtocol.valueOf(HP.code.TIBERIUM_WAR_TEAM_MEMBER_RESET_S, tuple.second));
		} else {
			HawkLog.logPrintln("TiberiumWarService onTeamMemberReset response failed, playerId: {}, name: {}, errorCode: {}", player.getId(), player.getName(), tuple.first);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
		
	}

	/**
	 * 小组成员退出联盟处理
	 * @param player
	 * @param guildId
	 */
	public void onMemberQuit(Player player, String guildId) {
		String playerId = player.getId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return ;
		}
		Optional<TWGuildTeamData> opData = getGuildTeamData(guildId);
		// 信息异常
		if (!opData.isPresent()) {
			return;
		}
		TWGuildTeamData guildTeamData = opData.get();
		Map<Integer, TWTeamData> teamDataMap = guildTeamData.getTeamInfos();
		if(teamDataMap.isEmpty()){
			return;
		}
		boolean needUpdate = false;
		for(Entry<Integer, TWTeamData> entry : teamDataMap.entrySet()){
			TWTeamData teamData = entry.getValue();
			Map<String, TWTeaMemberData> memberMap = teamData.getMemberMap();
			if(memberMap.containsKey(playerId)){
				memberMap.remove(playerId);
				needUpdate = true;
			}
		}
		if(needUpdate){
			needUpdateGuildIds.add(guildId);
		}
		
	}
	
	
	
	/**
	 * 表演赛模式获取当前进行中的比赛列表
	 * @return
	 */
	public TLWGetOBRoomInfo.Builder getTWRoomInfo() {
		TLWGetOBRoomInfo.Builder builder = TLWGetOBRoomInfo.newBuilder();
		int termId = activityInfo.getTermId();
		builder.setSeason(0);
		builder.setTermId(termId);
		if(activityInfo.getState() != TWActivityState.WAR){
			return builder;
		}
		
		List<HawkTuple2<Integer, Integer>> timeList = TiberiumConstCfg.getInstance().getTimeList();
		int currTimeIndex = -1;
		long currTime = HawkTime.getMillisecond();
		for(int i=0;i<timeList.size();i++){
			WarTimeChoose timeChoose = getWarTimeChoose(i);
			long startTime = timeChoose.getTime();
			long endTime = startTime + TiberiumConstCfg.getInstance().getWarOpenTime();
			if(currTime>=startTime && currTime< endTime){
				currTimeIndex = i;
				builder.setWarStartTime(startTime);
				builder.setWarFinishTime(endTime);
				break;
			}
		}
		// 当前接段没有开启的房间
		if(currTimeIndex == -1){
			return builder;
		}
		
		List<TWRoomData> roomList = RedisProxy.getInstance().getAllTWRoomData(termId);
		if(roomList == null || roomList.isEmpty()){
			return builder;
		}
		
		Map<String, TWGuildData> guildDataMap = RedisProxy.getInstance().getAllTWGuildData(termId);
		for(TWRoomData roomData : roomList){
			if(roomData.getTimeIndex() != currTimeIndex){
				continue;
			}
			
			TLWRoomInfo.Builder obRoomInfo = TLWRoomInfo.newBuilder();
			obRoomInfo.setRoomId(roomData.getId());
			obRoomInfo.setRoomServer(roomData.getRoomServerId());
			TLWGetMatchInfo.Builder matchBuilder = TLWGetMatchInfo.newBuilder();
			TWGuildData guildA = guildDataMap.get(roomData.getGuildA());
			TWGuildData guildB = guildDataMap.get(roomData.getGuildB());
			if(guildA == null || guildB==null){
				continue;
			}
			
			TLWGuildBaseInfo.Builder guildBuilderA = TLWGuildBaseInfo.newBuilder();
			guildBuilderA.setId(guildA.getId());
			guildBuilderA.setName(guildA.name);
			guildBuilderA.setTag(guildA.tag);
			guildBuilderA.setGuildFlag(guildA.flag);
			guildBuilderA.setLeaderName(guildA.getName());
			guildBuilderA.setServerId(guildA.getServerId());
			matchBuilder.setGuildA(guildBuilderA);
			
			TLWGuildBaseInfo.Builder guildBuilderB = TLWGuildBaseInfo.newBuilder();
			guildBuilderB.setId(guildB.getId());
			guildBuilderB.setName(guildB.name);
			guildBuilderB.setTag(guildB.tag);
			guildBuilderB.setGuildFlag(guildB.flag);
			guildBuilderB.setLeaderName(guildB.getName());
			guildBuilderB.setServerId(guildB.getServerId());
			matchBuilder.setGuildB(guildBuilderB);
			obRoomInfo.setMatchInfo(matchBuilder);
			builder.addRoomInfo(obRoomInfo);
		}
		
		return builder;
	}
	
	/**
	 * 根据当前elo积分计算本场变更分值
	 * @param scoreA
	 * @param scoreB
	 * @return
	 */
	public HawkTuple2<Integer, Integer> calcEloChangeValue(int scoreA, int scoreB) {
		double paramL = 400d;
		int paramK = TiberiumConstCfg.getInstance().getEloParamK();
		int d = Math.abs(scoreA - scoreB);
		double totalRate = 1.0d;
		double rateA = totalRate / (1 + Math.pow(10, -d / paramL));
		double scoreChangeW = paramK * (1 - rateA);
		double scoreChangeL = paramK * rateA;
		return new HawkTuple2<Integer, Integer>((int) Math.round(scoreChangeW), (int) Math.round(scoreChangeL));
	}
	
	/**
	 * 获取当前之间及以前最临近的一期泰伯利亚活动期数
	 * @return
	 */
	public int getProximateTermId() {
		long now = HawkTime.getMillisecond();
		int proximateTermId = activityInfo.getTermId();
		if(proximateTermId > 0){
			return proximateTermId;
		}
		ConfigIterator<TiberiumTimeCfg> timeCfgs = HawkConfigManager.getInstance().getConfigIterator(TiberiumTimeCfg.class);
		for(TiberiumTimeCfg cfg : timeCfgs){
			if(now >= cfg.getSignStartTimeValue() && now< cfg.getWarEndTimeValue()){
				proximateTermId = cfg.getTermId();
			}
			else if(now < cfg.getSignStartTimeValue()){
				break;
			}
		}
		return 0;
	}


	public boolean IsGuildJoin(Player player){
		if(player.isCsPlayer()){
			return true;
		}
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
//		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
//			return false;
//		}
		// 非报名阶段
//		if (activityInfo.state != TWActivityState.SIGN) {
//			return false;
//		}
		if (signGuild.containsKey(guildId)) {
			return true;
		}
		// 已参与联赛正赛,不能进行报名
		if(TiberiumLeagueWarService.getInstance().isJointLeaguaWar(player)){
			return false;
		}
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return false;
		}
		if(HawkTime.getMillisecond() - guild.getCreateTime() < TiberiumConstCfg.getInstance().getGuildCreateTimeLimit()){
			return false;
		}
		int rankLimit = TiberiumConstCfg.getInstance().getSignRankLimit();
		RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, guildId);
		// 联盟战力排名限制
		if (rankInfo == null || rankInfo.getRank() > rankLimit) {
			return false;
		}
		return true;
	}

	public boolean isPlayerJoin(Player player){
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		Set<String> idList = RedisProxy.getInstance().getTWPlayerIds(guildId);
		if(idList.contains(player.getId())){
			return true;
		}
		return false;
	}
}
