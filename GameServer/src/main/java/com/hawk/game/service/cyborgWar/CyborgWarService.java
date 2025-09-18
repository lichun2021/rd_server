package com.hawk.game.service.cyborgWar;

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
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuple4;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSONObject;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CWScoreEvent;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsConfig;
import com.hawk.game.config.CyborgConstCfg;
import com.hawk.game.config.CyborgPersonAwardCfg;
import com.hawk.game.config.CyborgSeasonConstCfg;
import com.hawk.game.config.CyborgSeasonDivisionCfg;
import com.hawk.game.config.CyborgSeasonGuildAwardCfg;
import com.hawk.game.config.CyborgSeasonInitCfg;
import com.hawk.game.config.CyborgSeasonTimeCfg;
import com.hawk.game.config.CyborgWarTimeCfg;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengcyb.CYBORGExtraParam;
import com.hawk.game.lianmengcyb.CYBORGRoomManager;
import com.hawk.game.lianmengcyb.msg.CYBORGBilingInformationMsg;
import com.hawk.game.lianmengcyb.msg.CYBORGBilingInformationMsg.PlayerGameRecord;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.schedule.ScheduleInfo;
import com.hawk.game.module.schedule.ScheduleService;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.CyborgWar.CSWStateInfo;
import com.hawk.game.protocol.CyborgWar.CWBattleLog;
import com.hawk.game.protocol.CyborgWar.CWGetPlayerListResp;
import com.hawk.game.protocol.CyborgWar.CWPageInfo;
import com.hawk.game.protocol.CyborgWar.CWPlayerInfo;
import com.hawk.game.protocol.CyborgWar.CWPlayerManageReq;
import com.hawk.game.protocol.CyborgWar.CWState;
import com.hawk.game.protocol.CyborgWar.CWStateInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamInfo;
import com.hawk.game.protocol.CyborgWar.CWTeamList;
import com.hawk.game.protocol.CyborgWar.CWTeamRank;
import com.hawk.game.protocol.CyborgWar.CWTeamStateInfo;
import com.hawk.game.protocol.CyborgWar.CWTimeChoose;
import com.hawk.game.protocol.CyborgWar.GetCWTeamRankResp;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Mail.PBCyborgContributionMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.MsgCategory;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Schedule.ScheduleType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CWConst.CLWStarReason;
import com.hawk.game.service.cyborgWar.CWConst.CWActivityState;
import com.hawk.game.service.cyborgWar.CWConst.CWMemverMangeType;
import com.hawk.game.service.cyborgWar.CWConst.FightState;
import com.hawk.game.service.cyborgWar.CWConst.RoomState;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventCyborgWar;
import com.hawk.game.tsssdk.GameMsgCategory;
import com.hawk.game.tsssdk.GameTssService;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.LogConst.LogMsgType;

import redis.clients.jedis.Tuple;

/**
 * 赛博之战
 * 
 * @author Jesse
 */
public class CyborgWarService extends HawkAppObj {

	/**
	 * 全局实例对象
	 */
	private static CyborgWarService instance = null;

	/**
	 * 活动时间信息数据
	 */
	public static CWActivityData activityInfo = new CWActivityData();

	/**
	 * 本服所有联盟对应战队信息(仅限本服遍历使用)
	 */
	public static Map<String, List<String>> guildTeams = new ConcurrentHashMap<>();
	/**
	 * 匹配状态信息
	 */
	public static CWMatchState matchServerInfo = new CWMatchState();
	/**
	 * 战斗阶段状态信息
	 */
	public static CWFightState fightState = new CWFightState();
	/**
	 * 已报名战队信息
	 */
	public static Map<String, Integer> signTeams = new ConcurrentHashMap<>();

	/**
	 * 战力排行榜
	 */
	public static CyborgWarPowerRank cyborgWarPowerRank = new CyborgWarPowerRank();
	
	/**
	 * 缓存一些数据
	 */
	public static CyborgWarCacheData cyborgWarCacheData = new CyborgWarCacheData();
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static CyborgWarService getInstance() {
		return instance;
	}

	public CyborgWarService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public boolean init() {
		try {
			// 加载战队信息
			loadGuildTeams();
			// 读取活动阶段数据
			activityInfo = CyborgWarRedis.getInstance().getCWActivityInfo();

			// 进行阶段检测
			checkStateChange();
			// 活动如果处于匹配完成状态,则拉取匹配数据
			if (activityInfo.getState() != CWActivityState.NOT_OPEN) {
				loadSignTeams();
			}
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();

					// 匹配轮询检测
					if (activityInfo.state == CWActivityState.MATCH) {
						matchTick();
					}
					// 战斗阶段轮询检测
					else if (activityInfo.state == CWActivityState.WAR) {
						try {
							fightTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			});

			// 刷新战队的战力排行
			addTickable(new HawkPeriodTickable(300000) {
				@Override
				public void onPeriodTick() {
					updateTeamPowerRank();
					cyborgWarPowerRank.updateRank();
				}
			});

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	/**
	 * 加载本服所有联盟战队映射
	 */
	private void loadGuildTeams() {
		List<String> guildList = GuildService.getInstance().getGuildIds();
		if (guildList.isEmpty()) {
			return;
		}
		Map<String, List<String>> gTeams = new ConcurrentHashMap<>();
		for (String guildId : guildList) {
			List<String> teams = CyborgWarRedis.getInstance().getCWGuildTeams(guildId);
			if (teams == null) {
				continue;
			}
			gTeams.put(guildId, teams);
		}
		guildTeams = gTeams;

	}

	/**
	 * 刷新战队的战力排行
	 */
	protected void updateTeamPowerRank() {
		try {
			Map<String, Double> powerMap = new HashMap<>();
			String serverId = GsConfig.getInstance().getServerId();
			List<CWTeamData> teamNeedUpdateList = new ArrayList<>();
			List<String> teamIds = new ArrayList<>();
			for (Entry<String, List<String>> entry : guildTeams.entrySet()) {
				String guildId = entry.getKey();
				RankInfo rankInfo = RankService.getInstance().getRankInfo(RankType.ALLIANCE_FIGHT_KEY, guildId);
				// 联盟战力排名限制
				if (rankInfo == null || rankInfo.getRank() > 123) {
					continue;
				}
				for (String teamId : entry.getValue()) {
					HawkTuple2<Integer, Long> joinInfo = getTeamMemberPower(guildId, teamId, serverId);
					long power = joinInfo.second;
					powerMap.put(teamId, (double) power);
					teamIds.add(teamId);
				}
			}
			HawkLog.logPrintln("CyborgWarService updateTeamPowerRank guild has team:{} real update team power:{}", guildTeams.size(), teamIds.size());
			if (!powerMap.isEmpty()) {
				cyborgWarPowerRank.addCWTeamPowerRanks(powerMap, serverId);
			}
			// 判定是否有区服信息发生变化的战队
			Map<String, CWTeamData> teamMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
			for (Entry<String, CWTeamData> entry : teamMap.entrySet()) {
				CWTeamData teamData = entry.getValue();
				String guildId = teamData.getGuildId();
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				boolean needUpdate = false;
				if (guildObj == null) {
					HawkLog.logPrintln("CyborgWarService updateTeamPowerRank error, guild not exist, teamId:{}, guildId:{}", teamData.getId(), teamData.getGuildId());
					continue;
				}
				if (!serverId.equals(teamData.getServerId())) {
					teamData.setServerId(serverId);
					needUpdate = true;
				}
				if (!guildObj.getName().equals(teamData.getGuildName()) || !guildObj.getTag().equals(teamData.getGuildTag()) || guildObj.getFlagId() != teamData.getGuildFlag()) {
					teamData.setGuildName(guildObj.getName());
					teamData.setGuildTag(guildObj.getTag());
					teamData.setGuildFlag(guildObj.getFlagId());
					needUpdate = true;
				}
				if (needUpdate) {
					teamNeedUpdateList.add(teamData);
				}
			}
			if (!teamNeedUpdateList.isEmpty()) {
				CyborgWarRedis.getInstance().updateCWTeamData(teamNeedUpdateList);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/**
	 * 获取报名出战的联盟战力
	 * 
	 * @param guildId
	 * @return
	 */
	private HawkTuple2<Integer, Long> getTeamMemberPower(String guildId, String teamId, String serverId) {
		Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
		int joinCnt = idList.size();
		long totalPower = 0;
		if (!serverId.equals(GsConfig.getInstance().getServerId())) {
			totalPower = cyborgWarPowerRank.getCWTeamPower(teamId, serverId);
		} else {
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guild == null) {
				return new HawkTuple2<Integer, Long>(0, 0l);
			}
			for (String playerId : idList) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
				// 非本盟玩家
				if (member == null || !guildId.equals(member.getGuildId())) {
					continue;
				}
				totalPower += member.getPower();
			}
		}
		return new HawkTuple2<Integer, Long>(joinCnt, totalPower);
	}

	/**
	 * 加载报名联盟
	 */
	private void loadSignTeams() {
		int termId = activityInfo.getTermId();
		if (termId == 0) {
			return;
		}
		Map<String, Integer> signTeamMap = new HashMap<>();
		List<HawkTuple2<Integer, Integer>> timeList = CyborgConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			List<String> teamIds = CyborgWarRedis.getInstance().getCWSignInfo(termId, i);
			for (String teamId : teamIds) {
				signTeamMap.put(teamId, i);
			}
		}
		signTeams = signTeamMap;
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
			if (activityInfo.state != CWActivityState.MATCH) {
				return;
			}

			// 拉取加载成员列表
			if (!activityInfo.isPrepareFinish()) {
				flushSignerInfo();
				activityInfo.setPrepareFinish(true);
				CyborgWarRedis.getInstance().updateCWActivityInfo(activityInfo);
			}

			String matchKey = CyborgWarRedis.getInstance().CWACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
			String matchLockKey = CyborgWarRedis.getInstance().CWACTIVITY_MATCH_LOCK + ":" + activityInfo.getTermId();

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

			long prepareEndTime = activityInfo.getTimeCfg().getSignEndTimeValue() + CyborgConstCfg.getInstance().getMatchPrepareTime();
			// 匹配准备时间未结束,不进行其他处理
			if (HawkTime.getMillisecond() <= prepareEndTime) {
				return;
			}

			String serverId = GsConfig.getInstance().getServerId();
			long lock = RedisProxy.getInstance().getMatchLock(matchLockKey);
			boolean needSync = false;
			// 获取到匹配权限,设置有效期并进行匹配
			if (lock > 0) {
				RedisProxy.getInstance().getRedisSession().expire(matchLockKey, CyborgConstCfg.getInstance().getMatchLockExpire());
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
			if (needSync) {
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
	 * 邮件通知联盟匹配信息
	 */
	private void sendMatchMail() {
		int termId = activityInfo.termId;
		List<String> teamIds = new ArrayList<>(signTeams.keySet());
		if (teamIds == null || teamIds.isEmpty()) {
			return;
		}
		Map<String, CWTeamJoinData> dataMap = CyborgWarRedis.getInstance().getAllCWJoinTeamData(termId);
		String serverId = GsConfig.getInstance().getServerId();
		for (Entry<String, CWTeamJoinData> entry : dataMap.entrySet()) {
			CWTeamJoinData teamData = entry.getValue();
			if (!serverId.equals(teamData.getServerId())) {
				continue;
			}
			final String teamId = entry.getKey();
			Set<String> playerIds = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
			if (teamData.isMatchFailed()) {
				HawkLog.logPrintln("CyborgWarService match failed guild mail, termId: {}, teamId: {}, guildId: {}", termId, teamId, teamData.getGuildId());
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						// 匹配失败邮件
						for (String playerId : playerIds) {
							SystemMailService.getInstance().sendMail(
									MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CYBORG_MATCH_FAILED).setAwardStatus(MailRewardStatus.NOT_GET).build());
						}
						return null;
					}
				});
			} else {
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
					@Override
					public Object run() {
						// 匹配成功邮件
						for (String playerId : playerIds) {
							SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CYBORG_MATCH_NOTICE).build());
						}
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
		if (activityInfo.state != CWActivityState.WAR) {
			return;
		}
		if (!fightState.isHasInit()) {
			fightState = CyborgWarRedis.getInstance().getCWFightInfo();
			if (fightState == null) {
				fightState = new CWFightState();
				CyborgWarRedis.getInstance().updateCWFightInfo(fightState);
			}
			fightState.setHasInit(true);
		}
		if (fightState.getTermId() != activityInfo.termId) {
			fightState = new CWFightState();
			fightState.setTermId(activityInfo.termId);
			fightState.setHasInit(true);
			CyborgWarRedis.getInstance().updateCWFightInfo(fightState);
		}
		long curTime = HawkTime.getMillisecond();

		boolean needUpdate = false;
		for (CWFightUnit unit : fightState.getUnitList()) {

			CWTimeChoose timeChoose = getWarTimeChoose(unit.getTimeIndex());
			long startTime = timeChoose.getTime();
			long endTime = startTime + CyborgConstCfg.getInstance().getWarOpenTime();
			long awardTime = endTime + CyborgConstCfg.getInstance().getAwardDelayTime();
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
				for (Entry<String, Integer> entry : signTeams.entrySet()) {
					String teamId = entry.getKey();
					if (entry.getValue() != unit.getTimeIndex()) {
						continue;
					}
					Set<String> memberIds = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
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
				List<ChatMsg> msgList = new ArrayList<>();
				ChatMsg msg = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(Const.NoticeCfgId.CYBORG_123).build().toPBMsg();
				msgList.add(msg);
				ChatService.getInstance().sendChatMsg(msgList, onlineMembers);
			}
		}

		if (needUpdate) {
			long startTime = HawkTime.getMillisecond();
			CyborgWarRedis.getInstance().updateCWFightInfo(fightState);
			int count = 0;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				count++;
				syncPageInfo(player);
			}
			HawkLog.logPrintln("CyborgWarService fightTick update sync, costtime: {}, playerCount: {}", HawkTime.getMillisecond() - startTime, count);
		}
	}

	public boolean isClose() {
		boolean isSystemClose = CyborgConstCfg.getInstance().isSystemClose();
		return isSystemClose && activityInfo.state == CWActivityState.CLOSE;
	}

	/**
	 * 获取选项
	 * 
	 * @return
	 */
	private List<CWTimeChoose> getChooses(boolean isNeedCnt) {
		List<CWTimeChoose> chooseList = new ArrayList<>();
		CyborgWarTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg == null) {
			return chooseList;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(cfg.getWarStartTimeValue());
		int signLimit = CyborgConstCfg.getInstance().getMaxSignNum();
		List<HawkTuple2<Integer, Integer>> timeList = CyborgConstCfg.getInstance().getTimeList();
		for (int i = 0; i < timeList.size(); i++) {
			HawkTuple2<Integer, Integer> timeTuple = timeList.get(i);
			calendar.set(Calendar.HOUR_OF_DAY, timeTuple.first);
			calendar.set(Calendar.MINUTE, timeTuple.second);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			CWTimeChoose.Builder builder = CWTimeChoose.newBuilder();
			builder.setIndex(i);
			builder.setTime(calendar.getTimeInMillis());
			if (isNeedCnt) {
				List<String> signList = CyborgWarRedis.getInstance().getCWSignInfo(cfg.getTermId(), i);
				int signCnt = 0;
				if (!CollectionUtils.isEmpty(signList)) {
					signCnt = signList.size();
				}
				signCnt = Math.min(signCnt, signLimit);
				builder.setTeamCnt(signCnt);
			}
			chooseList.add(builder.build());
		}
		return chooseList;
	}

	/**
	 * 当前阶段状态计算,仅供状态检测调用
	 * 
	 * @return
	 */
	private CWActivityData calcInfo() {
		CWActivityData info = new CWActivityData();
		if (CyborgConstCfg.getInstance().isSystemClose()) {
			info.setState(CWActivityState.CLOSE);
			return info;
		}
		ConfigIterator<CyborgWarTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgWarTimeCfg.class);
		long now = HawkTime.getMillisecond();

		String serverId = GsConfig.getInstance().getServerId();

		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		long serverDelay = CyborgConstCfg.getInstance().getServerDelay();
		CyborgWarTimeCfg cfg = null;

		// 游客服不开启该活动
		if (GsConfig.getInstance().getServerType() == ServerType.GUEST) {
			return info;
		}
		for (CyborgWarTimeCfg timeCfg : its) {
			List<String> limitServerLimit = timeCfg.getLimitServerList();
			List<String> forbidServerLimit = timeCfg.getForbidServerList();
			if (serverOpenAm0 + serverDelay > timeCfg.getSignStartTimeValue()) {
				continue;
			}
			// 开启判定,如果没有开启区服限制,或者本期允许本服所在区组开放
			if ((limitServerLimit.isEmpty() || limitServerLimit.contains(serverId)) && (forbidServerLimit == null || !forbidServerLimit.contains(serverId))) {
				if (now > timeCfg.getOpenTimeValue()) {
					cfg = timeCfg;
				}
			}
		}

		// 没有可供开启的配置
		if (cfg == null) {
			return info;
		}

		// 本期活动和合服时间重叠,本期不开启
		if (AssembleDataManager.getInstance().isCrossOverMergeServerCfg(cfg.getOpenTimeValue(), cfg.getWarEndTimeValue(), serverId)) {
			return info;
		}

		int termId = 0;
		CWActivityState state = CWActivityState.NOT_OPEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long openTime = cfg.getOpenTimeValue();
			long signStartTime = cfg.getSignStartTimeValue();
			long signEndTime = cfg.getSignEndTimeValue();
			long matchEndTime = cfg.getMatchEndTimeValue();
			long warEndTime = cfg.getWarEndTimeValue();
			if (now < openTime) {
				state = CWActivityState.NOT_OPEN;
			} else if (now >= openTime && now < signStartTime) {
				state = CWActivityState.OPEN;
			} else if (now >= signStartTime && now < signEndTime) {
				state = CWActivityState.SIGN;
			} else if (now >= signEndTime && now < matchEndTime) {
				state = CWActivityState.MATCH;
			} else if (now >= matchEndTime && now < warEndTime) {
				state = CWActivityState.WAR;
			} else if (now >= warEndTime) {
				state = CWActivityState.NOT_OPEN;
			}
		}

		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	private void checkStateChange() {
		CWActivityData newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncPageInfo(player);
			}
			CyborgWarRedis.getInstance().updateCWActivityInfo(activityInfo);
		}
		CWActivityState old_state = activityInfo.getState();
		CWActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_term != old_term) {
			old_state = CWActivityState.NOT_OPEN;
			activityInfo.setTermId(new_term);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == CWActivityState.NOT_OPEN) {
				old_state = CWActivityState.OPEN;
				activityInfo.setState(old_state);
			} else if (old_state == CWActivityState.OPEN) {
				old_state = CWActivityState.SIGN;
				activityInfo.setState(old_state);
				onSignOpen();
			} else if (old_state == CWActivityState.SIGN) {
				old_state = CWActivityState.MATCH;
				activityInfo.setState(old_state);
				onMatchStart();
			} else if (old_state == CWActivityState.MATCH) {
				old_state = CWActivityState.WAR;
				activityInfo.setState(old_state);
				onMatchFinish();
			} else if (old_state == CWActivityState.WAR) {
				old_state = CWActivityState.NOT_OPEN;
				activityInfo.setState(old_state);
				onHidden();
			}
		}

		if (needUpdate) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncPageInfo(player);
			}
			CyborgWarRedis.getInstance().updateCWActivityInfo(activityInfo);
			HawkLog.logPrintln("CyborgWar state change, oldTerm: {}, oldState: {} ,newTerm: {}, newState: {}", old_term, old_state, activityInfo.getTermId(),
					activityInfo.getState());
		}

	}

	/**
	 * 进行联盟匹配
	 */
	public boolean toMatchGuild() {
		try {
			long startTime = HawkTime.getMillisecond();
			int termId = activityInfo.termId;
			// 清空本期的房间信息
			CyborgWarRedis.getInstance().removeCWRoomData(termId);
			Map<String, CWTeamJoinData> dataMap = CyborgWarRedis.getInstance().getAllCWJoinTeamData(termId);
			// 检测赛季初始化
			seasonCheckInit(dataMap);
			Map<Integer, List<CWTeamJoinData>> matchGuildMap = new HashMap<>();
			for (Entry<String, CWTeamJoinData> entry : dataMap.entrySet()) {
				CWTeamJoinData data = entry.getValue();
				int timeIndex = data.getTimeIndex();
				if (!matchGuildMap.containsKey(timeIndex)) {
					matchGuildMap.put(timeIndex, new ArrayList<>());
				}
				matchGuildMap.get(timeIndex).add(data);
			}
			for (Entry<Integer, List<CWTeamJoinData>> entry : matchGuildMap.entrySet()) {
				int timeIndex = entry.getKey();
				List<CWTeamJoinData> dataList = entry.getValue();
				doMatch(timeIndex, dataList);
			}
			HawkLog.logPrintln("CyborgWarService toMatchGuild finish, serverId: {}, costTime: {}", GsConfig.getInstance().getServerId(), HawkTime.getMillisecond() - startTime);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}

	/**
	 * 赛季段位初始化检测
	 */
	private void seasonCheckInit(Map<String, CWTeamJoinData> dataMap) {
		if (dataMap == null || dataMap.isEmpty()) {
			return;
		}
		int termId = getTermId();
		CLWActivityData clwData = CyborgLeaguaWarService.getInstance().getActivityData();
		int season = clwData.getSeason();

		// 非排位阶段
		if (!CyborgLeaguaWarService.getInstance().isSeasonOpen()) {
			return;
		}

		// 仅赛季首期赛博之战进行初始化
		CyborgSeasonTimeCfg timeCfg = clwData.getTimeCfg();
		if (timeCfg == null || timeCfg.getBeginTerm() != termId) {
			return;
		}

		long currTime = HawkTime.getMillisecond();
		HawkLog.logPrintln("seasonCheckInit start init! season: {}", clwData.getSeason());
		// 仅在赛季初始赛博期数时,进行段位初始化
		List<String> teamIds = new ArrayList<>(dataMap.keySet());
		List<CWTeamJoinData> joinTeams = new ArrayList<>(dataMap.values());
		Map<String, CWTeamData> teamDataMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		List<CWTeamJoinData> sortList = joinTeams.stream().sorted().collect(Collectors.toList());
		int size = sortList.size();
		List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> tupleList = getInitRange(size);
		for (int i = 0; i < sortList.size(); i++) {
			CWTeamJoinData joinData = sortList.get(i);
			CWTeamData teamData = teamDataMap.get(joinData.getId());
			CyborgSeasonInitCfg cfg = getCyborgSeasonInitCfgByRank(i, tupleList);
			int topRate = 0;
			int addStar = 0;
			if (cfg != null) {
				addStar = cfg.getExtraStar();
				topRate = cfg.getTopeRate();
			}
			teamData.setStar(1 + addStar);
			teamData.setInitSeason(season);
			teamData.setInitPercent(topRate);
			teamData.setInitExtStar(addStar);
			teamData.setInitRank(i + 1);
			teamData.setSeasonScore(0);
			joinData.setStarBef(teamData.getStar());
		}
		CyborgWarRedis.getInstance().updateCWJoinTeamData(sortList, termId);
		CyborgWarRedis.getInstance().updateCWTeamData(new ArrayList<>(teamDataMap.values()));
		HawkLog.logPrintln("seasonCheckInit init finish! season: {}, costTime: {}", clwData.getSeason(), HawkTime.getMillisecond() - currTime);
	}

	public List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> getInitRange(int size) {
		ConfigIterator<CyborgSeasonInitCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonInitCfg.class);
		List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> rangeList = new ArrayList<>();
		long endIndex = 0;
		for (CyborgSeasonInitCfg cfg : its) {
			int topRate = cfg.getTopeRate();
			long indexCnt = (long) Math.floor(1d * size * topRate / GsConst.RANDOM_MYRIABIT_BASE);
			if (endIndex != 0 && indexCnt < endIndex) {
				HawkLog.logPrintln("CyborgWarService getInitRange pass, cfgId:{}, size:{}, endIndex:{}, indexCnt:{}", cfg.getId(), size, indexCnt, endIndex);
				continue;
			}
			HawkTuple3<CyborgSeasonInitCfg, Long, Long> tuple = new HawkTuple3<CyborgSeasonInitCfg, Long, Long>(cfg, endIndex - 1, indexCnt - 1);
			rangeList.add(tuple);
			HawkLog.logPrintln("CyborgWarService getInitRange, cfgId:{}, size:{}, endIndex:{}, indexCnt:{}", cfg.getId(), size, indexCnt, endIndex);
			endIndex = indexCnt + 1;
		}
		return rangeList;
	}
	
	public CyborgSeasonInitCfg getCyborgSeasonInitCfgByRank(int index, List<HawkTuple3<CyborgSeasonInitCfg, Long, Long>> tupleList) {
		for (HawkTuple3<CyborgSeasonInitCfg, Long, Long> tuple : tupleList) {
			if (index >= tuple.second && index <= tuple.third) {
				return tuple.first;
			}
		}
		return null;
	}

	/**
	 * 匹配
	 * 
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public boolean doMatch(int timeIndex, List<CWTeamJoinData> dataList) {
		int termId = activityInfo.termId;
		Map<Integer,List<CWTeamJoinData>> matchMap = new HashMap<>();
		List<CWTeamJoinData> extList = new ArrayList<>();
		//填充匹配池
		this.fillMatchPool(dataList, matchMap, extList);
		//匹配池数量
		int poolSize =  CyborgConstCfg.getInstance().getMatchOpenDaysPoolSize();
		//没有匹配上的队伍
		List<CWTeamJoinData> lastList = new ArrayList<>();
		for(int i=0;i<poolSize;i++){
			List<CWTeamJoinData> pool = matchMap.get(i);
			if(Objects.isNull(pool)){
				continue;
			}
			//上次剩余的添加进来
			pool.addAll(lastList);
			lastList = this.doMatchPool(termId,timeIndex,pool);
		}
		//额外池匹配
		if(extList.size() > 0){
			extList.addAll(lastList);
			lastList = this.doMatchPool(termId,timeIndex,extList);
		}
		// 未匹配成功的战队及剩余不足4队的战队
		if (!lastList.isEmpty()) {
			for (CWTeamJoinData guildData : lastList) {
				guildData.setMatchFailed(true);
				guildData.setRoomId("");
			}
			CyborgWarRedis.getInstance().updateCWJoinTeamData(lastList, termId);
		}
		return true;
	}
	
	/**
	 * 分配匹配池
	 * @param dataList
	 * @param matchMap
	 * @param extList
	 */
	public void fillMatchPool(List<CWTeamJoinData> dataList,Map<Integer,List<CWTeamJoinData>> matchMap,List<CWTeamJoinData> extList){
		for(CWTeamJoinData data : dataList){
			int poolIndex = CyborgConstCfg.getInstance().getMatchOpenDaysPoolIndex(data.getServerOpenDays());
			if(poolIndex < 0){
				extList.add(data);
				continue;
			}
			List<CWTeamJoinData> matchList = matchMap.get(poolIndex);
			if(Objects.isNull(matchList)){
				matchList = new ArrayList<>();
				matchMap.put(poolIndex, matchList);
			}
			matchList.add(data);
		}
	}
	
	
	/**
	 * 匹配
	 * @param termId
	 * @param timeIndex
	 * @param dataList
	 * @return
	 */
	public List<CWTeamJoinData> doMatchPool(int termId,int timeIndex,List<CWTeamJoinData> dataList){
		List<CWTeamJoinData> matchList = new ArrayList<>(dataList);
		List<HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData>> resultList = new ArrayList<>();
		// 匹配不到对手的联盟列表
		List<CWTeamJoinData> noMatchList = new ArrayList<>();
		do {
			// 不足四队,终止匹配
			if (matchList.size() < 4) {
				break;
			}
			List<CWTeamJoinData> sortList = matchList.stream().sorted(new Comparator<CWTeamJoinData>() {
				@Override
				public int compare(CWTeamJoinData arg0, CWTeamJoinData arg1) {
					if (arg0.getMatchPower() > arg1.getMatchPower()) {
						return -1;
					} else if (arg0.getMatchPower() < arg1.getMatchPower()) {
						return 1;
					} else {
						return arg0.getId().compareTo(arg1.getId());
					}
				}
			}).collect(Collectors.toList());
			CWTeamJoinData selectGuild = sortList.get(0);
			List<CWTeamJoinData> matchedList = new ArrayList<>();
			matchedList.add(selectGuild);
			sortList.remove(selectGuild);
			List<String> matchedGuild = new ArrayList<>();
			matchedGuild.add(selectGuild.getGuildId());
			int totalSize = sortList.size();
			int priorityIndex = (int) (1l * totalSize * CyborgConstCfg.getInstance().getMatchTopRate() / 10000);
			List<CWTeamJoinData> priorityList = sortList.subList(0, priorityIndex);
			// 前5%的战队顺序打散
			Collections.shuffle(priorityList);
			for (int i = 0; i < 3; i++) {
				for (CWTeamJoinData tData : sortList) {
					// 该战队联盟未匹配入本房间,符合入围标准
					if (!matchedGuild.contains(tData.getGuildId())) {
						matchedList.add(tData);
						matchedGuild.add(tData.getGuildId());
						break;
					}
				}
			}
			// 该战队匹配不满3个对手,则该战队匹配失败
			if (matchedList.size() != 4) {
				matchList.remove(selectGuild);
				noMatchList.add(selectGuild);
			} else {
				resultList.add(new HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData>(matchedList.get(0), matchedList.get(1), matchedList.get(2),
						matchedList.get(3)));
				matchList.removeAll(matchedList);
			}
		} while (true);
		
		List<CWTeamJoinData> updateList = new ArrayList<>();
		List<CWRoomData> roomList = new ArrayList<>();
		Map<String, Integer> roomServerMap = new HashMap<>();
		// 根据匹配信息生成房间信息
		for (HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData> tuple : resultList) {
			CWRoomData roomData = new CWRoomData();
			Map<String, String> guildTeamMap = new HashMap<>();
			guildTeamMap.put(tuple.first.getGuildId(), tuple.first.getId());
			guildTeamMap.put(tuple.second.getGuildId(), tuple.second.getId());
			guildTeamMap.put(tuple.third.getGuildId(), tuple.third.getId());
			guildTeamMap.put(tuple.fourth.getGuildId(), tuple.fourth.getId());
			roomData.setGtMaps(guildTeamMap);
			roomData.setTimeIndex(timeIndex);
			roomData.setId(HawkOSOperator.randomUUID());
			// 筛选当前负载最低的服作为房间服
			String roomServer = selectRoomServer(roomServerMap, tuple);
			roomData.setRoomServerId(roomServer);
			if (roomServerMap.containsKey(roomServer)) {
				roomServerMap.put(roomServer, roomServerMap.get(roomServer) + 1);
			} else {
				roomServerMap.put(roomServer, 1);
			}
			roomList.add(roomData);
			List<String> teamIdList = new ArrayList<>(guildTeamMap.values());
			Collections.sort(teamIdList);
			tuple.first.setRoomId(roomData.getId());
			tuple.first.setRoomTeams(teamIdList);
			tuple.first.setMatchFailed(false);

			tuple.second.setRoomId(roomData.getId());
			tuple.second.setRoomTeams(teamIdList);
			tuple.second.setMatchFailed(false);

			tuple.third.setRoomId(roomData.getId());
			tuple.third.setRoomTeams(teamIdList);
			tuple.third.setMatchFailed(false);

			tuple.fourth.setRoomId(roomData.getId());
			tuple.fourth.setRoomTeams(teamIdList);
			tuple.fourth.setMatchFailed(false);

			updateList.add(tuple.first);
			updateList.add(tuple.second);
			updateList.add(tuple.third);
			updateList.add(tuple.fourth);
			LogUtil.logCyborgMatchRoom(roomData.getId(), roomData.getRoomServerId(), termId, timeIndex, tuple.first.getId(), tuple.first.getMatchPower(), tuple.first.getGuildId(), tuple.first.getServerId(),
					tuple.second.getId(), tuple.second.getMatchPower(),tuple.second.getGuildId(), tuple.second.getServerId(), tuple.third.getId(), tuple.third.getMatchPower(), tuple.third.getGuildId(), tuple.third.getServerId(),
					tuple.fourth.getId(), tuple.fourth.getMatchPower(),tuple.fourth.getGuildId(), tuple.fourth.getServerId());
			HawkLog.logPrintln(
					"CyborgWarService do match, roomId: {}, roomServer: {}, termId: {}, timeIndex: {}," + "teamA: {}, matchPowerA:{}, guildA: {}, serverA: {}, teamB: {}, matchPowerB:{}, guildB: {}, serverB: {},"
							+ "teamC: {}, matchPowerC:{}, guildC: {}, serverC: {}, teamD: {},matchPowerD:{}, guildD: {}, serverD: {}",
					roomData.getId(), roomData.getRoomServerId(), termId, timeIndex, tuple.first.getId(), tuple.first.getMatchPower(),tuple.first.getGuildId(), tuple.first.getServerId(), tuple.second.getId(),tuple.second.getMatchPower(),
					tuple.second.getGuildId(), tuple.second.getServerId(), tuple.third.getId(),tuple.third.getMatchPower(), tuple.third.getGuildId(), tuple.third.getServerId(), tuple.fourth.getId(),tuple.fourth.getMatchPower(),
					tuple.fourth.getGuildId(), tuple.fourth.getServerId());
		}
		CyborgWarRedis.getInstance().updateCWJoinTeamData(updateList, termId);
		CyborgWarRedis.getInstance().updateCWRoomData(roomList, termId);
		
		List<CWTeamJoinData> lastList = new ArrayList<>();
		//剩下不足的
		lastList.addAll(matchList);
		//没找到合适队伍的
		lastList.addAll(noMatchList);
		return lastList;
	}
	
	

	/**
	 * 筛选四个战队中负载最低的服作为战斗服
	 * 
	 * @param roomServerMap
	 * @param tuple
	 * @return
	 */
	public String selectRoomServer(Map<String, Integer> roomServerMap, HawkTuple4<CWTeamJoinData, CWTeamJoinData, CWTeamJoinData, CWTeamJoinData> tuple) {
		List<HawkTuple3<String, Integer, String>> serverInfos = new ArrayList<>();
		String serverA = tuple.first.getServerId();
		int cntA = roomServerMap.containsKey(serverA) ? roomServerMap.get(serverA) : 0;
		serverInfos.add(new HawkTuple3<String, Integer, String>(serverA, cntA, tuple.first.getId()));
		String serverB = tuple.second.getServerId();
		int cntB = roomServerMap.containsKey(serverB) ? roomServerMap.get(serverB) : 0;
		serverInfos.add(new HawkTuple3<String, Integer, String>(serverB, cntB, tuple.second.getId()));
		String serverC = tuple.third.getServerId();
		int cntC = roomServerMap.containsKey(serverC) ? roomServerMap.get(serverC) : 0;
		serverInfos.add(new HawkTuple3<String, Integer, String>(serverC, cntC, tuple.third.getId()));
		String serverD = tuple.fourth.getServerId();
		int cntD = roomServerMap.containsKey(serverD) ? roomServerMap.get(serverD) : 0;
		serverInfos.add(new HawkTuple3<String, Integer, String>(serverD, cntD, tuple.fourth.getId()));
		serverInfos.sort(new Comparator<HawkTuple3<String, Integer, String>>() {
			@Override
			public int compare(HawkTuple3<String, Integer, String> arg0, HawkTuple3<String, Integer, String> arg1) {
				if (arg0.second != arg1.second) {
					return arg0.second - arg1.second;
				} else {
					return arg0.third.compareTo(arg1.third);
				}
			}
		});
		HawkTuple3<String, Integer, String> mainTeam = serverInfos.get(0);
		String roomServer = mainTeam.first;
		return roomServer;
	}

	/**
	 * 报名阶段结束,记录参与数据
	 * 
	 * @return
	 */
	private boolean flushSignerInfo() {
		String serverId = GsConfig.getInstance().getServerId();
		int serverOpenDays = GlobalData.getInstance().getServerOpenDays();
		int termId = activityInfo.termId;
		Set<String> signGuilds = new HashSet<>();
		try {
			for (Entry<String, Integer> entry : signTeams.entrySet()) {
				String teamId = entry.getKey();
				CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
				if (teamData == null) {
					continue;
				}
				String guildId = teamData.getGuildId();
				GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guild == null) {
					continue;
				}
				Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
				long param1 = CyborgConstCfg.getInstance().getMatchParam1();
				long param2 = CyborgConstCfg.getInstance().getMatchParam2();
				long param3 = CyborgConstCfg.getInstance().getMatchParam3();
				long param4 = CyborgConstCfg.getInstance().getMatchParam4();
				int memberCnt = 0;
				long totalPower = 0;
				long matchPower = 0;
				List<HawkTuple2<String, Long>> powerList = new ArrayList<>();
				for (String playerId : idList) {
					GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
					// 非本盟玩家
					if (member == null || !guildId.equals(member.getGuildId())) {
						continue;
					}
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					PowerData powerData = player.getData().getPowerElectric().getPowerData();
					int cityLvl = player.getCityLevel();
					long armyPower = powerData.getArmyBattlePoint();
					long heroPower = powerData.getHeroBattlePoint();
					long superSoldierPower = powerData.getSuperSoldierBattlePoint();
					matchPower += Math.ceil(GsConst.EFF_PER * (param1 * cityLvl + param2 * armyPower + param3 * heroPower + param4 * superSoldierPower));
					memberCnt++;
					totalPower += member.getPower();
					powerList.add(new HawkTuple2<String, Long>(playerId,player.getStrength()));
					CWPlayerData playerData = new CWPlayerData();
					playerData.setId(playerId);
					playerData.setGuildAuth(member.getAuthority());
					playerData.setGuildId(guildId);
					playerData.setTeamId(teamId);
					playerData.setGuildOfficer(member.getOfficeId());
					playerData.setServerId(serverId);
					CyborgWarRedis.getInstance().updateCWPlayerData(playerData, termId);
					LogUtil.logCyborgMatchPlayer(playerId, termId, teamId, guildId, serverId, member.getPower());
				}

				CWTeamJoinData teamJoinData = new CWTeamJoinData();
				teamJoinData.setId(teamId);
				teamJoinData.setGuildId(guildId);
				teamJoinData.setServerId(serverId);
				teamJoinData.setServerOpenDays(serverOpenDays);
				teamJoinData.setName(teamData.getName());
				teamJoinData.setTag(guild.getTag());
				teamJoinData.setFlag(guild.getFlagId());
				teamJoinData.setMemberCnt(memberCnt);
				teamJoinData.setTotalPower(totalPower);
				teamJoinData.setMatchPower(matchPower);
				teamJoinData.setTimeIndex(entry.getValue());
				if(CyborgLeaguaWarService.getInstance().isSeasonOpen()){
					teamJoinData.setStarBef(teamData.getStar());
				}
				if(CyborgConstCfg.getInstance().getCyborgMatchSwitch() > 0){
					long newMatchPower = this.getMatchPower(teamId,powerList);
					teamJoinData.setMatchPower(newMatchPower);
				}
				CyborgWarRedis.getInstance().updateCWJoinTeamData(teamJoinData, termId);
				signGuilds.add(guildId);
				try {
					LogUtil.logCyborgMatchTeam(teamId, teamData.getName(), guildId, guild.getName(), serverId, memberCnt, totalPower, termId, teamJoinData.getTimeIndex(),teamJoinData.getMatchPower());
					HawkLog.logPrintln(
							"CyborgWarService flushSignerInfo, guildId: {}, guildName: {}, teamId:{}, teamName:{} , serverId: {}, memberCnt: {}, totalPowar: {}, termId: {}, timeIndex: {}, playerIds:{}",
							guildId, guild.getName(), teamId, teamData.getName(), serverId, memberCnt, totalPower, termId, entry.getValue(), idList);
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			List<CWGuildData> addList = new ArrayList<>();
			for (String guildId : signGuilds) {
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					continue;
				}
				long power = GuildService.getInstance().getGuildBattlePoint(guildId);
				CWGuildData guildData = new CWGuildData();
				guildData.setTermId(termId);
				guildData.setId(guildId);
				guildData.setName(guildObj.getName());
				guildData.setTag(guildObj.getTag());
				guildData.setLeaderId(guildObj.getLeaderId());
				guildData.setLeaderName(guildObj.getLeaderName());
				guildData.setFlag(guildObj.getFlagId());
				guildData.setServerId(serverId);
				guildData.setPower(power);
				addList.add(guildData);
			}

			// 刷入出战联盟信息,供跨服查询
			if (!addList.isEmpty()) {
				CyborgWarRedis.getInstance().updateCWGuildData(addList, termId);
			}
			// 记录当前所有战队出战列表
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					recordJoinList();
					return null;
				}
			});

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
	public long getMatchPower(String teamId,List<HawkTuple2<String, Long>> powerList){
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
				HawkLog.logPrintln("CyborgWarService match power,teamId:{},playerId:{},power:{},powerWeight:{},memberPower:{},", 
						teamId,tuple.first,power,powerWeight, power * powerWeight);
			}
			double teamParam = this.getTeamMatchParam(teamId);
			double openServerParam = CyborgConstCfg.getInstance().getMatchPowerParam();
			long matchPower = (long) (teamParam * memberPower *openServerParam);
			HawkLog.logPrintln("CyborgWarService match power,teamId:{},memberPower:{},teamParam:{},openServerParam:{},matchPower:{}", teamId, memberPower,teamParam,openServerParam,matchPower);
			return matchPower;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}
	
	/**
	 * 队伍磨合参数
	 * @param teamId
	 * @return
	 */
	private double getTeamMatchParam(String teamId){
		int count = CyborgConstCfg.getInstance().getCyborgMatchTimesLimit() -1;
		count = Math.max(count, 0);
		List<CWBattleLog> logList = CyborgWarRedis.getInstance().getCWBattleLog(teamId,count);
		double param = 0;
		for(CWBattleLog log : logList){
			for(CWTeamInfo team : log.getTeamInfoList()){
				if(team.getId().equals(teamId)){
					int rank = team.getRank();
					double rankParam = CyborgConstCfg.getInstance().getCyborgMatchBattleResultValue(rank);
					param += rankParam;
					HawkLog.logPrintln("CyborgWarService match power, getTeamMatchParam,teamId:{},termId:{},rank:{},param:{}", 
							teamId,log.getTermId(),rank,rankParam);
				}
			} 
		}
		param = Math.min(param, CyborgConstCfg.getInstance().getCyborgMatchCofMaxValue());
		param = Math.max(param, CyborgConstCfg.getInstance().getCyborgMatchCofMinValue());
		HawkLog.logPrintln("CyborgWarService match power, getTeamMatchParam, result,teamId:{},param:{}", 
				teamId,param);
		return param + 1;
	}
	
	
	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(10);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}
	/**
	 * 刷新本服所有战队出战人员列表快照
	 */
	private void recordJoinList() {
		int termId = activityInfo.getTermId();
		List<String> guildIds = GuildService.getInstance().getGuildIds();
		List<CWTeamLastJoins> joinLists = new ArrayList<>();
		for (String guildId : guildIds) {
			List<String> teamIds = CyborgWarRedis.getInstance().getCWGuildTeams(guildId);
			if (CollectionUtils.isEmpty(teamIds)) {
				continue;
			}
			for (String teamId : teamIds) {
				Set<String> joinPlayers = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
				CWTeamLastJoins joinInfo = new CWTeamLastJoins();
				joinInfo.setId(teamId);
				List<String> playerList = new ArrayList<>();
				if (!CollectionUtils.isEmpty(joinPlayers)) {
					playerList.addAll(joinPlayers);
				}
				joinInfo.setJoinList(playerList);
				joinInfo.setTermId(termId);
				joinLists.add(joinInfo);
				HawkLog.logPrintln("CyborgWarService recordJoinList, teamInfo:{}", joinInfo);
			}
		}
		if (!CollectionUtils.isEmpty(joinLists)) {
			CyborgWarRedis.getInstance().updateCWTeamLastJoins(joinLists);
		}

	}

	/**
	 * 批量创建
	 * 
	 * @param timeIndex
	 */
	private void createBattle(int timeIndex) {
		int termId = activityInfo.getTermId();
		CWTimeChoose timechoose = getWarTimeChoose(timeIndex);
		long startTime = timechoose.getTime();
		long overTime = startTime + CyborgConstCfg.getInstance().getWarOpenTime();
		List<CWRoomData> dataList = CyborgWarRedis.getInstance().getAllCWRoomData(activityInfo.termId);
		String serverId = GsConfig.getInstance().getServerId();
		List<CWRoomData> createList = dataList.stream().filter(t -> t.getRoomServerId().equals(serverId) && t.getTimeIndex() == timeIndex).collect(Collectors.toList());
		for (CWRoomData roomData : createList) {
			Map<String, String> gtMap = roomData.getGtMaps();
			List<String> teamIds = new ArrayList<>(gtMap.values());
			if (teamIds.size() != 4) {
				HawkLog.logPrintln("CyborgWarService createBattleFailed, cnt error! roomId:{}, roomTeams:{}", roomData.getId(), roomData.getGtMaps().values());
				;
				continue;
			}
			String teamA = teamIds.get(0);
			String teamB = teamIds.get(1);
			String teamC = teamIds.get(2);
			String teamD = teamIds.get(3);
			Collections.sort(teamIds);

			Map<String, CWTeamJoinData> teamMap = CyborgWarRedis.getInstance().getCWJoinTeamDatas(teamIds, termId);
			CWTeamJoinData teamDataA = teamMap.get(teamA);
			CWTeamJoinData teamDataB = teamMap.get(teamB);
			CWTeamJoinData teamDataC = teamMap.get(teamC);
			CWTeamJoinData teamDataD = teamMap.get(teamD);
			if (teamDataA == null || teamDataB == null || teamDataC == null || teamDataD == null) {
				roomData.setRoomState(RoomState.INITED_FAILED);
			} else {

				CYBORGExtraParam param = new CYBORGExtraParam();
				param.setCampAGuild(teamDataA.getGuildId());
				param.setCampAguildFlag(teamDataA.getFlag());
				param.setCampAGuildName(teamDataA.getName());
				param.setCampAGuildTag(teamDataA.getTag());
				param.setCampAServerId(teamDataA.getServerId());
				param.setCampATeamName(teamDataA.getName());
				param.setCampATeamPower(teamDataA.getTotalPower());
				param.setCampBGuild(teamDataB.getGuildId());
				param.setCampBguildFlag(teamDataB.getFlag());
				param.setCampBGuildName(teamDataB.getName());
				param.setCampBGuildTag(teamDataB.getTag());
				param.setCampBServerId(teamDataB.getServerId());
				param.setCampBTeamName(teamDataB.getName());
				param.setCampBTeamPower(teamDataB.getTotalPower());
				param.setCampCGuild(teamDataC.getGuildId());
				param.setCampCguildFlag(teamDataC.getFlag());
				param.setCampCGuildName(teamDataC.getName());
				param.setCampCGuildTag(teamDataC.getTag());
				param.setCampCServerId(teamDataC.getServerId());
				param.setCampCTeamName(teamDataC.getName());
				param.setCampCTeamPower(teamDataC.getTotalPower());
				param.setCampDGuild(teamDataD.getGuildId());
				param.setCampDguildFlag(teamDataD.getFlag());
				param.setCampDGuildName(teamDataD.getName());
				param.setCampDGuildTag(teamDataD.getTag());
				param.setCampDServerId(teamDataD.getServerId());
				param.setCampDTeamName(teamDataD.getName());
				param.setCampDTeamPower(teamDataD.getTotalPower());
				boolean result = CYBORGRoomManager.getInstance().creatNewBattle(startTime, overTime, roomData.getId(), param);
				if (result) {
					roomData.setRoomState(RoomState.INITED);
				} else {
					roomData.setRoomState(RoomState.INITED_FAILED);
				}
			}
		}
		CyborgWarRedis.getInstance().updateCWRoomData(createList, activityInfo.termId);
	}

	/**
	 * 发奖
	 */
	private void sendAward(int timeIndex) {
		int termId = activityInfo.termId;
		List<String> teamIds = CyborgWarRedis.getInstance().getCWSignInfo(termId, timeIndex);
		if (teamIds == null || teamIds.isEmpty()) {
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		// 本轮涉及出战的联盟
		Set<String> guildIds = new HashSet<>();
		// 是否处在赛季开启阶段
		boolean isSeasonOpen = CyborgLeaguaWarService.getInstance().isSeasonOpen();
		for (String teamId : teamIds) {
			CWTeamJoinData teamJoinData = CyborgWarRedis.getInstance().getCWJoinTeamData(teamId, termId);
			if (teamJoinData == null) {
				continue;
			}
			if (!serverId.equals(teamJoinData.getServerId()) || teamJoinData.matchFailed || teamJoinData.isAwarded()) {
				continue;
			}
			boolean isComplete = teamJoinData.isComplete();
			int rank = teamJoinData.getRank();
			// 战场异常中断导致未正常结算,按照最高档次奖励发放
			if (!isComplete) {
				rank = 1;
			}
			guildIds.add(teamJoinData.getGuildId());
			List<ItemInfo> extraReward = null;
			int star = 0;
			if (isSeasonOpen) {
				CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
				star = teamData.getStar();
				CyborgSeasonDivisionCfg divisionCfg = CyborgLeaguaWarService.getInstance().getDivisionCfgByStar(star);
				if (divisionCfg != null) {
					extraReward = divisionCfg.getExtraRewardListByRank(rank);
				}
			}
			CyborgWarRedis.getInstance().updateCWJoinTeamData(teamJoinData, termId);
			// 参赛个人奖励
			Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
			HawkLog.logPrintln("CyborgWarService start send self reward, teamId: {}, guildId: {}, guildName: {} ,guildTag: {}, joinIds: {}", teamId, teamJoinData.getGuildId(),
					teamJoinData.getName(), teamJoinData.getTag(), idList);
			// 个人奖励邮件
			MailId selfMailId = MailId.CYBORG_SELF_REWARD;
			//重大贡献邮件ID
			MailId contributeMailId = MailId.CYBORG_CONTRIBUTE_NOTICE;
			for (String playerId : idList) {
				CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(playerId, termId);
				if (cwPlayerData == null) {
					HawkLog.logPrintln("CyborgWarService ignore self reward, cwPlayerData is null, playerId: {}", playerId);
					continue;
				}
				if (cwPlayerData.isAwarded()) {
					HawkLog.logPrintln("CyborgWarService ignore self reward,already awarded, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}, isAwarded", playerId,
							cwPlayerData.getEnterTime(), cwPlayerData.getQuitTime(), cwPlayerData.isMidwayQuit(), cwPlayerData.isAwarded());
					continue;
				}
				long selfScore = cwPlayerData.getScore();
				// 战场异常中断导致未正常结算,按照最高档次奖励发放
				if (!isComplete) {
					selfScore = Integer.MAX_VALUE;
				}
				ActivityManager.getInstance().postEvent(new CWScoreEvent(playerId, selfScore,cwPlayerData.getEnterTime() > 0, isSeasonOpen));
				MissionManager.getInstance().postMsg(playerId, new EventCyborgWar());

				if (cwPlayerData.getEnterTime() == 0 || cwPlayerData.isMidwayQuit()) {
					HawkLog.logPrintln("CyborgWarService ignore self reward, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}", playerId, cwPlayerData.getEnterTime(),
							cwPlayerData.getQuitTime(), cwPlayerData.isMidwayQuit());
					continue;
				}
				List<ItemInfo> rewardList = new ArrayList<>();
				CyborgPersonAwardCfg selfCfg = getPersonAwardCfg(selfScore, rank);
				//个人积分奖励
				rewardList.addAll(selfCfg.getRewardItem());
				//重大贡献奖励
				rewardList.addAll(teamJoinData.getContributeRewards(playerId));
				//积分占比奖励
				rewardList.addAll(teamJoinData.getScorePerRewards());
				//奖励邮件
				SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(selfMailId).addContents(teamJoinData.getScore(),
						teamJoinData.getScorePer(),teamJoinData.getCyborgItemTotal(),rank, selfScore,selfCfg.getId(),teamJoinData.contributePlayer(playerId)).
						setAwardStatus(MailRewardStatus.NOT_GET).addRewards(rewardList).build());
				//重大贡献邮件
				PBCyborgContributionMail.Builder contributeMail = teamJoinData.buildContributeMail();
				if(contributeMail != null){
					SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(contributeMailId).addContents(contributeMail).build());
				}
				// 赛季排位阶段,发送段位额外奖励
				if (isSeasonOpen && !CollectionUtils.isEmpty(extraReward)) {
					// 发送段位额外奖励
					SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_EXTRA_REWARD)
							.addContents(teamJoinData.getStarBef(), teamJoinData.getStarAft()).setAwardStatus(MailRewardStatus.NOT_GET).addRewards(extraReward).build());
				}
				cwPlayerData.setAwarded(true);
				CyborgWarRedis.getInstance().updateCWPlayerData(cwPlayerData, termId);
				HawkLog.logPrintln("CyborgWarService send self reward, playerId: {}, score: {}, cfgId: {}, isComplete: {}", playerId, selfScore, selfCfg.getId(), isComplete);
			}
		}
		// 赛季排位阶段,给联盟成员发送联盟积分奖励
		if (isSeasonOpen && !guildIds.isEmpty()) {
			int season = CyborgLeaguaWarService.getInstance().getSeason();
			for (String guildId : guildIds) {
				Collection<String> playerIds = GuildService.getInstance().getGuildMembers(guildId);
				if (CollectionUtils.isEmpty(playerIds)) {
					continue;
				}
				long score = CyborgWarRedis.getInstance().getCLWGuildScore(guildId, season);
				List<CyborgSeasonGuildAwardCfg> cfgList = calcGuildScoreReward(score);
				if (cfgList.isEmpty()) {
					continue;
				}
				for (String playerId : playerIds) {
					List<Integer> rewardedList = CyborgWarRedis.getInstance().getCLWRewardedList(playerId, season);
					List<String> addList = new ArrayList<>();
					for (CyborgSeasonGuildAwardCfg cfg : cfgList) {
						int cfgId = cfg.getId();
						if (rewardedList.contains(cfgId)) {
							continue;
						}
						// 赛季联盟积分奖励
						SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_GUILD_SCORE_REWARD)
								.addContents(score, cfg.getScore()).setAwardStatus(MailRewardStatus.NOT_GET).addRewards(cfg.getRewardItem()).build());
						addList.add(String.valueOf(cfg.getId()));
					}
					if (!addList.isEmpty()) {
						CyborgWarRedis.getInstance().addCLWRewardedIds(playerId, season, addList);
					}
				}
				HawkLog.logPrintln("CyborgWarService send guild score reward, guildId: {}, season: {}, score: {}, memberCount: {}", guildId, season, score, playerIds.size());
			}
		}

	}
	
	/**
	 * 计算可领取的奖励配置列表
	 * @param score
	 * @return
	 */
	private List<CyborgSeasonGuildAwardCfg> calcGuildScoreReward(long score) {
		List<CyborgSeasonGuildAwardCfg> cfgList = new ArrayList<>();
		ConfigIterator<CyborgSeasonGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonGuildAwardCfg.class);
		for(CyborgSeasonGuildAwardCfg cfg : its) {
			if(score>=cfg.getScore()){
				cfgList.add(cfg);
			}
		}
		return cfgList;
	}

	public CyborgPersonAwardCfg getPersonAwardCfg(long selfScore, int rank) {
		ConfigIterator<CyborgPersonAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgPersonAwardCfg.class);
		for (CyborgPersonAwardCfg cfg : its) {
			if (rank != cfg.getRank()) {
				continue;
			}
			HawkTuple2<Long, Long> rang = cfg.getScoreRange();
			if (selfScore >= rang.first && selfScore <= rang.second) {
				return cfg;
			}
		}
		return null;
	}

	private void onSignOpen() {
		signTeams = new ConcurrentHashMap<>();
		CyborgWarTimeCfg timeCfg = activityInfo.getTimeCfg();
		// 全服通知邮件
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder().setMailId(MailId.CYBORG_SIGN_NOTICE).build(), timeCfg.getSignStartTimeValue(),
				timeCfg.getSignEndTimeValue());
	}

	private void onMatchStart() {
		// 加载全部报名的联盟
		loadSignTeams();
	}

	/**
	 * 活动关闭
	 */
	private void onHidden() {
		signTeams = new ConcurrentHashMap<>();
	}

	/**
	 * 匹配结束,进入战斗准备阶段
	 */
	private void onMatchFinish() {
		// 邮件通知匹配结果
		sendMatchMail();
		// 邮件通知初始化段位信息
		sendSeasonStarMail();
		// 未匹配成功星级奖励
		machFailedStarChange();
		// 未出战导致掉星
		noSignStarChange();
	}
	
	/**
	 * 未出战导致掉星
	 */
	public void noSignStarChange() {
		// 非排位阶段
		if (!CyborgLeaguaWarService.getInstance().isSeasonOpen()) {
			return;
		}
		int season = CyborgLeaguaWarService.getInstance().getSeason();
		List<CWTeamData> teamList = getServerTeam();
		List<CWTeamData> updateList = new ArrayList<>();
		// 待更新段位积分排名信息
		Map<String, Double> starScoreMap = new HashMap<>();
		int termId = getTermId();
		for (CWTeamData teamData : teamList) {
			// 当前赛季已出战,不进行掉段处理
			if (teamData.getTermId() >= termId) {
				continue;
			}
			// 当前赛季未初始化的战队,不处理
			if (teamData.getInitSeason() != season) {
				continue;
			}
			int starChange = CyborgLeaguaWarService.getInstance().getStarChange(teamData.getStar(), 4);
			// 不掉星的不处理
			if (starChange == 0) {
				continue;
			}
			int starBef = teamData.getStar();
			teamData.setStar(teamData.getStar() + starChange);
			
			// 战队段位变化流水
			LogUtil.logCyborgSeasonStarFlow(teamData.getGuildId(), teamData.getId(), season, termId, teamData.getServerId(), starChange, teamData.getStar(),
					CLWStarReason.NO_SIGN.getNumber(), 0);
			updateList.add(teamData);
			starScoreMap.put(teamData.getId(), (double) calcStarScore(teamData.getStar(), teamData.getSeasonScore()));
			// 邮件告知怠战掉星
			Set<String> playerIds = CyborgWarRedis.getInstance().getCWPlayerIds(teamData.getId());
			for (String playerId : playerIds) {
				SystemMailService.getInstance().sendMail(
						MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_NO_SIGN).addContents(starChange, starBef, teamData.getStar()).build());
			}
		}

		if (!updateList.isEmpty()) {
			CyborgWarRedis.getInstance().updateCWTeamData(updateList);
			CyborgWarRedis.getInstance().addCLWTeamStarRanks(starScoreMap, season);
		}

	}

	/**
	 * 匹配失败的战队星级处理
	 */
	private void machFailedStarChange() {
		try {
			if (!CyborgLeaguaWarService.getInstance().isSeasonOpen()) {
				return;
			}
			int season = CyborgLeaguaWarService.getInstance().getSeason();
			int termId = getTermId();
			List<String> teamIds = new ArrayList<>(signTeams.keySet());
			if (teamIds == null || teamIds.isEmpty()) {
				return;
			}
			String serverId = GsConfig.getInstance().getServerId();
			Map<String, CWTeamJoinData> joinMap = CyborgWarRedis.getInstance().getAllCWJoinTeamData(termId);
			Map<String, CWTeamData> dataMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
			List<CWTeamData> needUpdateList = new ArrayList<>();
			// 待更新段位积分排名信息
			Map<String, Double> starScoreMap = new HashMap<>();
			for (Entry<String, CWTeamJoinData> entry : joinMap.entrySet()) {
				CWTeamJoinData joinData = entry.getValue();
				if (!serverId.equals(joinData.getServerId())) {
					continue;
				}
				if (!joinData.isMatchFailed()) {
					continue;
				}
				CWTeamData teamData = dataMap.get(joinData.getId());
				int starBef = teamData.getStar();
				int starChange = CyborgLeaguaWarService.getInstance().getStarChange(teamData.getStar(), 1);
				//teamData.setStar(teamData.getStar() + starChange);
				
				// 战队段位变化流水
				LogUtil.logCyborgSeasonStarFlow(teamData.getGuildId(), teamData.getId(), season, termId, serverId, starChange, teamData.getStar(),
						CLWStarReason.MATCH_FAILED.getNumber(), 0);
				CyborgPersonAwardCfg selfCfg = getPersonAwardCfg(1200, 1);

				// 发送匹配失败段位提升邮件
				Set<String> playerIds = CyborgWarRedis.getInstance().getCWPlayerIds(teamData.getId());
				for (String playerId : playerIds) {
					try {
						CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(playerId, termId);
						if (cwPlayerData == null) {
							HawkLog.logPrintln("CyborgWarService ignore self reward, cwPlayerData is null, playerId: {}", playerId);
							continue;
						}
						if (cwPlayerData.isAwarded()) {
							HawkLog.logPrintln("CyborgWarService ignore self reward,already awarded, playerId: {}, enterTime: {}, quitTime: {}, isMidwayQuit: {}, isAwarded", playerId,
									cwPlayerData.getEnterTime(), cwPlayerData.getQuitTime(), cwPlayerData.isMidwayQuit(), cwPlayerData.isAwarded());
							continue;
						}
						SystemMailService.getInstance().sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_MATCH_FAILED)
								.addContents(starChange, starBef, teamData.getStar()).setRewards(selfCfg.getRewardItem()).setAwardStatus(MailRewardStatus.NOT_GET).build());
						cwPlayerData.setAwarded(true);
						CyborgWarRedis.getInstance().updateCWPlayerData(cwPlayerData, termId);
						HawkLog.logPrintln("CyborgWarService send self reward, playerId: {}, score: {}, cfgId: {}, isComplete: {}", playerId, 1200, selfCfg.getId(), false);
					}catch (Exception e){
						HawkException.catchException(e);
					}
				}
				needUpdateList.add(teamData);
				starScoreMap.put(teamData.getId(), (double) calcStarScore(teamData.getStar(), teamData.getSeasonScore()));
			}
			if (!needUpdateList.isEmpty()) {
				CyborgWarRedis.getInstance().updateCWTeamData(needUpdateList);
				CyborgWarRedis.getInstance().addCLWTeamStarRanks(starScoreMap, season);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 赛季初始化邮件推送
	 */
	private void sendSeasonStarMail() {
		try {
			CLWActivityData clwData = CyborgLeaguaWarService.getInstance().getActivityData();
			int season = clwData.getSeason();
			if (!CyborgLeaguaWarService.getInstance().isSeasonOpen()) {
				return;
			}
			CyborgSeasonTimeCfg timeCfg = clwData.getTimeCfg();
			if (timeCfg == null) {
				return;
			}
			int termId = getTermId();
			if (termId != timeCfg.getBeginTerm()) {
				return;
			}
			List<String> teamIds = new ArrayList<>(signTeams.keySet());
			Map<String, CWTeamData> dataMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
			// 待更新段位积分排名信息
			Map<String, Double> starScoreMap = new HashMap<>();
			String serverId = GsConfig.getInstance().getServerId();
			for (Entry<String, CWTeamData> entry : dataMap.entrySet()) {
				CWTeamData teamData = entry.getValue();
				// 只发送本服的定级邮件
				if (!serverId.equals(teamData.getServerId())) {
					continue;
				}
				Set<String> playerIds = CyborgWarRedis.getInstance().getCWPlayerIds(teamData.getId());
				int addStar = teamData.getInitExtStar();
				int per = teamData.getInitPercent();
				// 战队段位变化流水
				LogUtil.logCyborgSeasonStarFlow(teamData.getGuildId(), teamData.getId(), season, termId, teamData.getServerId(), 1 + addStar, teamData.getStar(),
						CLWStarReason.BEGIN_INIT.getNumber(), teamData.getInitRank());

				if (teamData.getInitExtStar() > 0) {
					// 发送初始化N星邮件
					for (String playerId : playerIds) {
						SystemMailService.getInstance().sendMail(
								MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_INIT_HIGH).addContents(per, addStar, teamData.getStar()).build());
						HawkLog.logPrintln("CyborgWarService sendSeasonStarMail with extra star, teamId:{}, playerId:{}, extraStar:{} ", termId, playerId, addStar);
					}
				} else {
					// 发送初始化1星邮件
					for (String playerId : playerIds) {
						SystemMailService.getInstance()
								.sendMail(MailParames.newBuilder().setPlayerId(playerId).setMailId(MailId.CLW_DIVISION_INIT_NORMAL).addContents(teamData.getStar()).build());
						HawkLog.logPrintln("CyborgWarService sendSeasonStarMail, teamId:{}, playerId:{}", termId, playerId);
					}
				}
				starScoreMap.put(teamData.getId(), (double) calcStarScore(teamData.getStar(), teamData.getSeasonScore()));
			}
			CyborgWarRedis.getInstance().addCLWTeamStarRanks(starScoreMap, season);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 构建活动界面信息
	 * 
	 * @param player
	 * @return
	 */
	public CWPageInfo.Builder genPageInfo(Player player) {
		String guildId = player.getGuildId();
		CWPageInfo.Builder builder = CWPageInfo.newBuilder();
		CWStateInfo.Builder stateInfo = genStateInfo(guildId);
		builder.setStateInfo(stateInfo);
		CWTeamList.Builder listBuilder = CWTeamList.newBuilder();
		String playerServer = player.getMainServerId();
		List<CWTeamInfo> guildTeamList = genGuildTeamList(guildId, playerServer);
		listBuilder.addAllTeamList(guildTeamList);
		listBuilder.setSelfTeam(getSelfTeamId(player));
		builder.setTeamList(listBuilder);

		// 赛季期间同步赛季状态信息
		CSWStateInfo.Builder cswStateInfo = CyborgLeaguaWarService.getInstance().genCSWStateInfo();
		builder.setCswStateInfo(cswStateInfo);
		return builder;
	}

	/********************************* 活动数据信息 *****************************************************************/

	/**
	 * 构建活动时间信息
	 * 
	 * @return
	 */
	public CWStateInfo.Builder genStateInfo(String guildId) {
		CWStateInfo.Builder builder = CWStateInfo.newBuilder();
		CWActivityState state = activityInfo.state;
		int termId = activityInfo.termId;
		builder.setStage(termId);
		CyborgWarTimeCfg cfg = activityInfo.getTimeCfg();
		if (cfg != null) {
			builder.setOpenTime(cfg.getOpenTimeValue());
			builder.setSignStartTime(cfg.getSignStartTimeValue());
			builder.setMatchStartTime(cfg.getSignEndTimeValue());
		}
		boolean canEnter = false;
		switch (state) {
		case NOT_OPEN:
			builder.setState(CWState.NOT_OPEN);
			break;
		case OPEN:
			builder.setState(CWState.OPEN);
			break;
		case SIGN:
			builder.setState(CWState.SIGN);
			List<CWTimeChoose> chooseList = getChooses(true);
			builder.addAllChoose(chooseList);
			break;
		case MATCH:
			builder.setState(CWState.MATCH);
			break;
		case WAR:
			builder.setState(CWState.WAR);
			break;
		default:
			break;
		}
		List<CWTeamStateInfo> teamStateList = genGuildTeamStateList(guildId, builder.getState());
		if (!teamStateList.isEmpty()) {
			builder.addAllTeamState(teamStateList);
		}
		builder.setCanEnter(canEnter);
		return builder;
	}

	private List<CWTeamStateInfo> genGuildTeamStateList(String guildId, CWState state) {
		List<CWTeamStateInfo> stateList = new ArrayList<>();
		if (state.getNumber() < CWState.SIGN_VALUE) {
			return stateList;
		}
		int termId = activityInfo.getTermId();
		List<String> teamIds = getGuildTeams(guildId);
		Map<String, CWTeamData> teamMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		Map<String, CWTeamJoinData> joinTeamMap = new HashMap<>();
		// 匹配阶段结束,批量拉取本盟的出战数据
		if (state.getNumber() > CWState.MATCH_VALUE) {
			joinTeamMap = CyborgWarRedis.getInstance().getCWJoinTeamDatas(teamIds, termId);
		}
		for (String teamId : teamIds) {
			CWTeamData team = teamMap.get(teamId);
			CWTeamStateInfo.Builder builder = CWTeamStateInfo.newBuilder();
			builder.setTeamId(teamId);
			if (team == null) {
				stateList.add(builder.build());
				continue;
			}
			// 本期没有报名
			if (team.getTermId() == 0 || team.getTermId() != termId) {
				continue;
			}
			builder.setIsSignUp(true);
			CWTimeChoose timeChoose = getWarTimeChoose(team.getTimeIndex());
			long startTime = timeChoose.getTime();
			long endTime = startTime + CyborgConstCfg.getInstance().getWarOpenTime();
			builder.setWarStartTime(startTime);
			builder.setWarEndTime(endTime);
			// 已经过了匹配阶段,显示对手信息
			if (state.getNumber() > CWState.MATCH_VALUE) {
				CWTeamJoinData joinData = joinTeamMap.get(team.getId());
				// 本期没有出战
				if (joinData == null) {
					continue;
				}
				List<String> roomTeamIds = joinData.getRoomTeams();
				Map<String, CWTeamJoinData> roomTeams = CyborgWarRedis.getInstance().getCWJoinTeamDatas(roomTeamIds, termId);
				for (CWTeamJoinData roomTeam : roomTeams.values()) {
					if (roomTeam.getId().equals(team.getId())) {
						continue;
					}
					builder.addOppTeams(roomTeam.build());
				}
			}
			stateList.add(builder.build());
		}
		return stateList;
	}

	/**
	 * 构建联盟战队列表(跨服状态下只拉取)
	 * 
	 * @param guildId
	 * @return
	 */
	public List<CWTeamInfo> genGuildTeamList(String guildId, String serverId) {
		List<CWTeamInfo> builderList = new ArrayList<>();
		List<String> teamIds = getGuildTeams(guildId);
		Map<String, CWTeamData> dataMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		
		boolean isSeasonAftOpen = CyborgLeaguaWarService.getInstance().isSeasonAfterOpen();
		int season = CyborgLeaguaWarService.getInstance().getSeason();
		for (Entry<String, CWTeamData> entry : dataMap.entrySet()) {
			CWTeamData data = entry.getValue();
			CWTeamInfo.Builder teamBuilder = CWTeamInfo.newBuilder();
			HawkTuple2<Integer, Long> tuple = getTeamMemberPower(guildId, data.getId(), serverId);
			teamBuilder.setId(data.getId());
			teamBuilder.setName(data.getName());
			teamBuilder.setBattlePoint(tuple.second);
			teamBuilder.setMemberCnt(tuple.first);
			teamBuilder.setCreatTime(data.getCreateTime());
			//int rank = CyborgWarRedis.getInstance().getCWTeamPowerRank(data.getId(), serverId);
			int rank = cyborgWarPowerRank.getCWTeamPowerRank(data.getId(),serverId);
			teamBuilder.setRank(rank);
			if(isSeasonAftOpen && data.getInitSeason() == season){
				teamBuilder.setSeasonStar(data.getStar());
			}
			builderList.add(teamBuilder.build());
		}
		return builderList;
	}

	/**
	 * 联盟成员同步界面信息
	 * 
	 * @param guildId
	 */
	public void guildSync(String guildId) {
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		if (memberIds == null || memberIds.isEmpty()) {
			return;
		}
		for (String memberId : memberIds) {
			Player member = GlobalData.getInstance().getActivePlayer(memberId);
			if (member != null) {
				syncPageInfo(member);
			}
		}
	}

	/**
	 * 同步活动状态
	 * 
	 * @param player
	 */
	public void syncPageInfo(Player player) {
		CWPageInfo.Builder stateInfo = genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_INFO_SYNC, stateInfo));
	}

	/**
	 * 同步队伍信息
	 * 
	 * @param guildId
	 */
	public void syncTeamInfo(String guildId, String serverId) {
		List<CWTeamInfo> teamList = genGuildTeamList(guildId, serverId);
		Map<String, String> ptMap = getMemberTeamMap(guildId);
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		for (String memberId : memberIds) {
			Player member = GlobalData.getInstance().getActivePlayer(memberId);
			if (member == null) {
				continue;
			}
			CWTeamList.Builder builder = CWTeamList.newBuilder();
			builder.addAllTeamList(teamList);
			String teamId = ptMap.get(memberId);
			if (!HawkOSOperator.isEmptyString(teamId)) {
				builder.setSelfTeam(teamId);
			}
			member.sendProtocol(HawkProtocol.valueOf(HP.code.CYBORG_WAR_GET_TEAM_LIST_S, builder));
		}
	}

	/**
	 * 进入四国之战房间
	 * 
	 * @param player
	 * @return
	 */
	public boolean joinRoom(Player player) {
		int termId = activityInfo.getTermId();
		HawkTuple2<CWRoomData, Integer> tuple = getPlayerRoomData(player);
		int status = tuple.second;
		if (status != Status.SysError.SUCCESS_OK_VALUE) {
			HawkLog.logPrintln("CyborgWarService enter room error, errorCode: {}, playerId: {}, termId: {}", status, player.getId(), activityInfo.termId);
			return false;
		}
		CWRoomData roomData = tuple.first;
		if (roomData == null) {
			HawkLog.logPrintln("CyborgWarService enter room error, room not esixt, playerId: {}, termId: {}", player.getId(), activityInfo.termId);
			return false;
		}
		String roomId = roomData.getId();

		if (!CYBORGRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("CyborgWarService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId());
			return false;
		}
		CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(player.getId(), termId);
		if (cwPlayerData == null) {
			HawkLog.logPrintln("CyborgWarService enter room error, cwPlayerData is null, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), player.getGuildId());
			return false;
		}

		if (!cwPlayerData.getGuildId().equals(player.getGuildId())) {
			HawkLog.logPrintln("CyborgWarService enter room error, guildIds not match, playerId: {}, termId: {}, roomId: {}, roomServer: {}, cwDataGuildId: {}, playerGuildId: {}",
					player.getId(), activityInfo.termId, roomId, roomData.getRoomServerId(), cwPlayerData.getGuildId(), player.getGuildId());
			return false;
		}

		if (cwPlayerData.getQuitTime() > 0) {
			HawkLog.logPrintln("CyborgWarService enter room error, has entered, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), cwPlayerData.getGuildId());
			return false;
		}
		if (!CYBORGRoomManager.getInstance().joinGame(roomData.getId(), player)) {
			HawkLog.logPrintln("CyborgWarService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.termId, roomId, roomData.getRoomServerId(), cwPlayerData.getGuildId());
			return false;
		}
		cwPlayerData.setEnterTime(HawkTime.getMillisecond());
		CyborgWarRedis.getInstance().updateCWPlayerData(cwPlayerData, termId);
		
		long matchPower = 0;
		CWTeamJoinData teamData = CyborgWarRedis.getInstance().getCWJoinTeamData(cwPlayerData.getTeamId(), termId);
		if (teamData != null) {
			matchPower = teamData.getMatchPower();
		}
		LogUtil.logCyborgEnterRoom(player.getId(), termId, roomId, roomData.getRoomServerId(), cwPlayerData.getTeamId(), cwPlayerData.getGuildId(), cwPlayerData.getServerId(),
				player.getPower(),matchPower);
		HawkLog.logPrintln("CyborgWarService enter room, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(), termId, roomId,
				roomData.getRoomServerId(), cwPlayerData.getGuildId());
		return true;
	}

	/**
	 * 退出房间
	 * 
	 * @param player
	 * @param isMidwayQuit
	 *            是否中途退出
	 * @return
	 */
	public boolean quitRoom(Player player, boolean isMidwayQuit) {
		int termId = activityInfo.getTermId();
		CWPlayerData cwPlayerData = CyborgWarRedis.getInstance().getCWPlayerData(player.getId(), termId);
		if (cwPlayerData == null) {
			HawkLog.logPrintln("CyborgWarService quitRoom error, cwPlayerData is null, playerId: {}, isMidwayQuit: {}", player.getId(), isMidwayQuit);
			return false;
		}
		if (cwPlayerData.getEnterTime() == 0) {
			HawkLog.logPrintln("CyborgWarService quitRoom error, has not entered, playerId: {}, guildId: {}, isMidwayQuit: {}", cwPlayerData.getId(), cwPlayerData.getGuildId(),
					isMidwayQuit);
			return false;
		}
		if (isMidwayQuit) {
			cwPlayerData.setMidwayQuit(isMidwayQuit);
			cwPlayerData.setQuitTime(HawkTime.getMillisecond());
			CyborgWarRedis.getInstance().updateCWPlayerData(cwPlayerData, termId);
		}
		LogUtil.logCyborgQuitRoom(player.getId(), termId, cwPlayerData.getTeamId(), cwPlayerData.getGuildId(), cwPlayerData.isMidwayQuit());
		HawkLog.logPrintln("CyborgWarService quitRoom, playerId: {}, guildId: {}, termId:{}, isMidwayQuit: {}", cwPlayerData.getId(), cwPlayerData.getGuildId(), termId,
				isMidwayQuit);
		return true;
	}

	/**
	 * 获取期数信息
	 * 
	 * @return
	 */
	public int getTermId() {
		return activityInfo.getTermId();
	}

	/**
	 * 获取活动信息
	 * 
	 * @return
	 */
	public CWActivityData getActivityData() {
		return activityInfo;
	}

	/**
	 * 根据时间角标获取报名时间
	 * 
	 * @param timeIndex
	 * @return
	 */
	public CWTimeChoose getWarTimeChoose(int timeIndex) {
		List<CWTimeChoose> chooseList = getChooses(false);
		return chooseList.get(timeIndex);
	}

	/**
	 * 报名
	 * 
	 * @param player
	 * @return
	 */
	public int signUp(Player player, String teamId, int index) {
		int termId = activityInfo.getTermId();
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		// 非报名阶段
		if (activityInfo.state != CWActivityState.SIGN) {
			return Status.Error.CYBORG_NOT_SIGN_STAGE_VALUE;
		}

		if (signTeams.containsKey(teamId)) {
			return Status.Error.CYBORG_TEAM_HAS_JOINED_VALUE;
		}

		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}

		CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
		// 队伍不存在
		if (teamData == null) {
			return Status.Error.CYBORG_TEAM_NOT_EXIST_VALUE;
		}

		// 战队创建时间(指定期数之前的报名不受限制)
		if (termId > CyborgConstCfg.getInstance().getTeamTimeLimitTermId()
				&& HawkTime.getMillisecond() - teamData.getCreateTime() < CyborgConstCfg.getInstance().getTeamCreateTimeLimit()) {
			HawkLog.logPrintln("CyborgWar sign failed, creatTime not enough ,termId: {}, guildId: {}, teamId:{}, teamName:{}, timeIndex: {}, playerId:{}, rank:{}",
					activityInfo.termId, guildId, teamId, teamData.getName(), index, player.getId(), teamData.getCreateTime());
			return Status.Error.CYBORG_TEAM_NEW_VALUE;
		}

		// 本期已报名
		if (teamData.getTermId() != 0 && teamData.getTermId() == termId) {
			return Status.Error.CYBORG_TEAM_HAS_JOINED_VALUE;
		}

		int rankLimit = CyborgConstCfg.getInstance().getSignRankLimit();
		//int rank = CyborgWarRedis.getInstance().getCWTeamPowerRank(teamId, player.getMainServerId());
		int rank = cyborgWarPowerRank.getCWTeamPowerRank(teamId, player.getMainServerId());
		// 战队战力排名限制
		if (rank == -1 || rank > rankLimit) {
			HawkLog.logPrintln("CyborgWar sign failed, rank not enough ,termId: {}, guildId: {}, teamId:{}, teamName:{}, timeIndex: {}, playerId:{}, rank:{}", activityInfo.termId,
					guildId, teamId, teamData.getName(), index, player.getId(), rank);
			return Status.Error.CYBORG_RANK_NOT_ENOUGH_VALUE;
		}
		Set<String> joinMembers = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
		// 出战人数不满足限制
		if (joinMembers == null || joinMembers.size() < CyborgConstCfg.getInstance().getWarMemberMinCnt()) {
			HawkLog.logPrintln("CyborgWar sign failed, memberCnt not enough ,termId: {}, teamName:{}, guildId: {}, teamId:{}, timeIndex: {}, playerId:{}, memberCnt:{}",
					activityInfo.termId, guildId, teamId, teamData.getName(), index, player.getId(), joinMembers == null ? 0 : joinMembers.size());
			return Status.Error.CYBORG_MEMBER_CNT_MIN_VALUE;

		}
		CWTeamLastJoins joinInfo = CyborgWarRedis.getInstance().getCWTeamLastJoins(teamId);
		// 非首次出战
		if (!HawkOSOperator.isEmptyString(joinInfo.getId()) && teamData.getTermId() != 0) {
			List<String> lastList = joinInfo.getJoinList();
			int newCnt = 0;
			for (String memberId : joinMembers) {
				if (!lastList.contains(memberId)) {
					newCnt++;
				}
			}
			// 本期新增出战人员数量超上限
			if (newCnt > CyborgConstCfg.getInstance().getWarNewMemberLimit()) {
				HawkLog.logPrintln("CyborgWar sign failed, newCnt over limit ,termId: {}, guildId: {}, teamId:{}, teamName:{}, timeIndex: {}, playerId:{}, newCnt:{}, memberIds:{}",
						activityInfo.termId, guildId, teamId, teamData.getName(), index, player.getId(), newCnt, joinMembers);
				return Status.Error.CYBORG_NEW_MEMBER_TOO_MUCH_VALUE;
			}
		}
		int maxSignNum = CyborgConstCfg.getInstance().getMaxSignNum();
		List<String> signList = CyborgWarRedis.getInstance().getCWSignInfo(termId, index);
		// 该时段报名联盟战队过多
		if (signList != null && signList.size() >= maxSignNum) {
			HawkLog.logPrintln("CyborgWar sign failed, timeIndexFull ,termId: {}, guildId: {}, teamId:{}, teamName:{}, timeIndex: {}, playerId:{}, signedNum:{}",
					activityInfo.termId, guildId, teamId, teamData.getName(), index, player.getId(), signList.size());
			return Status.Error.CYBORG_SIGN_TOO_MUCH_VALUE;
		}

		CyborgWarRedis.getInstance().addCWSignInfo(teamId, termId, index);
		signTeams.put(teamId, index);
		CWPageInfo.Builder stateInfo = genPageInfo(player);
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.CYBORG_WAR_INFO_SYNC, stateInfo));
		teamData.setTermId(termId);
		teamData.setTimeIndex(index);
		boolean isSeasonOpen = CyborgLeaguaWarService.getInstance().isSeasonOpen();
		// 若赛季开启,则初始化段位
		if (isSeasonOpen) {
			int season = CyborgLeaguaWarService.getInstance().getSeason();
			int beginTeamId = CyborgLeaguaWarService.getInstance().getActivityData().getTimeCfg().getBeginTerm();
			if (termId != beginTeamId && teamData.getInitSeason() != season) {
				teamData.setInitSeason(season);
				teamData.setStar(1);
				teamData.setSeasonScore(0);

				// 战队段位变化流水
				LogUtil.logCyborgSeasonStarFlow(teamData.getGuildId(), teamData.getId(), season, termId, teamData.getServerId(), 1, teamData.getStar(),
						CLWStarReason.MID_INIT.getNumber(), 0);
				for (String memberId : joinMembers) {
					SystemMailService.getInstance()
							.sendMail(MailParames.newBuilder().setPlayerId(memberId).setMailId(MailId.CLW_DIVISION_INIT_NORMAL).addContents(teamData.getStar()).build());
				}
				HawkLog.logPrintln("CyborgWarService initDivision on sign, teamId:{}, teamName:{}, guildId:{}, season:{}, termId:{}, star:{}", teamId, teamData.getName(), guildId,
						season, termId, teamData.getStar());
			}
		}
		CyborgWarRedis.getInstance().updateCWTeamData(teamData);
		String serverId = GsConfig.getInstance().getServerId();
		HawkTuple2<Integer, Long> powerInfo = getTeamMemberPower(guildId, teamId, serverId);
		guildSync(guildId);
		
		CWTimeChoose timeChoose = getWarTimeChoose(index);
		long startTime = timeChoose.getTime();
		ScheduleInfo schedule = ScheduleInfo.createNewSchedule(ScheduleType.SCHEDULE_TYPE_5_VALUE, guildId, startTime, 0, 0,teamId);
		ScheduleService.getInstance().addSystemSchedule(schedule);
			
		LogUtil.logCyborgSignUp(teamId, teamData.getName(), guildId, guild.getName(), joinMembers.size(), powerInfo.second, serverId, termId, index, player.getId());
		HawkLog.logPrintln("CyborgWar sign success,termId: {}, guildId: {}, teamId:{}, teamName:{}, timeIndex: {}, playerId:{}", activityInfo.termId, guildId, teamId,
				teamData.getName(), index, player.getId());
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 构建联盟成员列表
	 * 
	 * @param player
	 * @return
	 */
	public CWGetPlayerListResp.Builder getMemberList(String guildId, String teamId) {
		CWGetPlayerListResp.Builder builder = CWGetPlayerListResp.newBuilder();
		builder.setTeamId(teamId);
		String serverId = GsConfig.getInstance().getServerId();
		GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guild == null) {
			return null;
		}
		Map<String, String> ptMap = getMemberTeamMap(guildId);
		CWTeamLastJoins joinInfo = CyborgWarRedis.getInstance().getCWTeamLastJoins(teamId);
		List<String> lastList = joinInfo.getJoinList();
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		for (String memberId : memberIds) {
			Player memberPlayer = GlobalData.getInstance().makesurePlayer(memberId);
			GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(memberId);
			if (memberPlayer == null || member == null) {
				continue;
			}
			CWPlayerInfo.Builder playerInfo = CWPlayerInfo.newBuilder();
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
			playerInfo.setGuildOfficer(member.getOfficeId());
			playerInfo.setBattlePoint(member.getPower());
			if (ptMap.containsKey(memberId)) {
				String memberTeam = ptMap.get(memberId);
				playerInfo.setTeamId(memberTeam);
			}
			// 是否本期新加入玩家
			boolean isNew = !lastList.contains(memberId);
			playerInfo.setIsNew(isNew);
			builder.addPlayerInfo(playerInfo);
		}
		return builder;
	}

	/**
	 * 获取联盟成员战队映射
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<String, String> getMemberTeamMap(String guildId) {
		Map<String, String> ptMap = new HashMap<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return ptMap;
		}
		List<String> teamIds = CyborgWarRedis.getInstance().getCWGuildTeams(guildId);
		if (teamIds != null && !teamIds.isEmpty()) {
			for (String _teamId : teamIds) {
				Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(_teamId);
				if (idList.isEmpty()) {
					continue;
				}
				for (String id : idList) {
					ptMap.put(id, _teamId);
				}
			}
		}
		return ptMap;
	}

	/**
	 * 获取玩家当前所在战队的id
	 * 
	 * @param player
	 * @return
	 */
	public String getSelfTeamId(Player player) {
		if (player == null) {
			return "";
		}
		String guildId = player.getGuildId();
		String playerId = player.getId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return "";
		}
		Map<String, String> ptMap = getMemberTeamMap(guildId);
		if (ptMap.containsKey(playerId)) {
			return ptMap.get(playerId);
		} else {
			return "";
		}
	}

	/**
	 * 更新参战玩家列表
	 * 
	 * @param player
	 * @param idList
	 * @return
	 */
	public int updateMemberList(Player player, CWPlayerManageReq req) {

		String teamId = req.getTeamId();
		String targetId = req.getPlayerId();
		int termId = activityInfo.getTermId();
		int type = req.getType();
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		String guildId = player.getGuildId();
		if (!GuildService.getInstance().isInTheSameGuild(player.getId(), targetId)) {
			return Status.Error.GUILD_NOT_SAME_VALUE;
		}
		CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
		if (activityInfo.state == CWActivityState.SIGN && teamData.getTermId() != 0 && teamData.getTermId() == termId) {
			return Status.Error.CYBORG_TEAM_HAS_JOINED_VALUE;
		}

		// 当前阶段不能进行出战人员变更
		if (activityInfo.state == CWActivityState.MATCH || activityInfo.state == CWActivityState.WAR) {
			return Status.Error.CYBORG_CANNOT_MANAGE_STATE_VALUE;
		}

		Map<String, String> ptMap = getMemberTeamMap(guildId);
		// 玩家原来所在战队
		String orgTeamId = "";
		if (ptMap.containsKey(targetId)) {
			orgTeamId = ptMap.get(targetId);
		}
		Set<String> idList = CyborgWarRedis.getInstance().getCWPlayerIds(teamId);
		// 添加出战
		if (type == 1) {
			if (idList.contains(targetId)) {
				return Status.SysError.SUCCESS_OK_VALUE;
			}
			// 超过出战人数上限
			if (idList.size() + 1 > CyborgConstCfg.getInstance().getTeamMemberLimit()) {
				return Status.Error.CYBORG_PLAYER_OVER_LIMIT_VALUE;
			}
			// 转队操作
			if (!HawkOSOperator.isEmptyString(orgTeamId) && !orgTeamId.equals(teamId)) {
				CWTeamData orgTeamData = CyborgWarRedis.getInstance().getCWTeamData(orgTeamId);
				// 判定原队伍状态能否改变
				if (orgTeamData != null && orgTeamData.getTermId() != 0 && orgTeamData.getTermId() == termId && activityInfo.state == CWActivityState.SIGN) {
					return Status.Error.CYBORG_ORG_TEAM_JOINED_VALUE;
				}
				CyborgWarRedis.getInstance().updateCWPlayerIds(orgTeamId, CWMemverMangeType.KICK, targetId);
			}
			CyborgWarRedis.getInstance().updateCWPlayerIds(teamId, CWMemverMangeType.JOIN, targetId);
		} else {
			// 取消出战
			// 队伍id不一致,无效操作
			if (!orgTeamId.equals(teamId)) {
				return Status.Error.CYBORG_TEAM_NOT_MATCH_VALUE;
			}

			CyborgWarRedis.getInstance().updateCWPlayerIds(teamId, CWMemverMangeType.KICK, targetId);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 联盟解散
	 * 
	 * @param guildId
	 */
	public void onGuildDismiss(String guildId) {
		List<String> teams = getGuildTeams(guildId);
		if (teams.isEmpty()) {
			return;
		}
		try {
			for (String teamId : teams) {
				CyborgWarRedis.getInstance().removeCWPlayerIds(teamId);
				CyborgWarRedis.getInstance().removeCWTeamData(teamId);
				CyborgWarRedis.getInstance().removeCWGuildTeam(guildId, teamId);
				CyborgWarRedis.getInstance().removeCWBattleLog(teamId);
			}
			CyborgWarRedis.getInstance().removeCWTeamLastJoins(teams);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 成员退出联盟
	 * 
	 * @param player
	 * @param guildId
	 */
	public void onQuitGuild(Player player, String guildId) {
		try {
			String playerId = player.getId();
			Map<String, String> ptMap = getMemberTeamMap(guildId);
			if (ptMap.containsKey(playerId)) {
				String teamId = ptMap.get(playerId);
				CyborgWarRedis.getInstance().removeCWPlayerId(teamId, playerId);
			}
			syncPageInfo(player);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 根据玩家获取匹配房间信息
	 * 
	 * @param player
	 * @return
	 */
	public HawkTuple2<CWRoomData, Integer> getPlayerRoomData(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<CWRoomData, Integer>(null, Status.Error.GUILD_NO_JOIN_VALUE);
		}

		int termId = activityInfo.getTermId();

		CWPlayerData cwPlayer = CyborgWarRedis.getInstance().getCWPlayerData(player.getId(), termId);
		if (cwPlayer == null) {
			return new HawkTuple2<CWRoomData, Integer>(null, Status.Error.CYBORG_NOT_IN_THIS_WAR_VALUE);
		}
		String teamId = cwPlayer.getTeamId();
		// 没有出战
		if (HawkOSOperator.isEmptyString(teamId)) {
			return new HawkTuple2<CWRoomData, Integer>(null, Status.Error.CYBORG_NOT_IN_THIS_WAR_VALUE);
		}
		CWTeamJoinData teamData = CyborgWarRedis.getInstance().getCWJoinTeamData(teamId, termId);
		if (teamData == null) {
			return new HawkTuple2<CWRoomData, Integer>(null, Status.Error.CYBORG_NOT_IN_THIS_WAR_VALUE);
		}
		String roomId = teamData.getRoomId();
		if (HawkOSOperator.isEmptyString(teamData.getRoomId())) {
			return new HawkTuple2<CWRoomData, Integer>(null, Status.Error.CYBORG_HAS_NO_MATCH_INFO_VALUE);
		}
		CWRoomData roomData = CyborgWarRedis.getInstance().getCWRoomData(roomId, termId);
		return new HawkTuple2<CWRoomData, Integer>(roomData, Status.SysError.SUCCESS_OK_VALUE);
	}

	/**
	 * 战场结束
	 * 
	 * @param msg
	 */
	/**
	 * @param msg
	 */
	@MessageHandler
	private void onBattleFinish(CYBORGBilingInformationMsg msg) {
		String roomId = msg.getRoomId();
		int termId = activityInfo.termId;
		CWRoomData roomData = CyborgWarRedis.getInstance().getCWRoomData(roomId, termId);
		if (roomData == null) {
			HawkLog.logPrintln("CyborgWarService onBattleFinish error, room data null, termId: {}, roomId: {}", termId, roomId);
			return;
		}
		roomData.setRoomState(RoomState.CLOSE);
		CyborgWarRedis.getInstance().updateCWRoomData(roomData, termId);
		Map<String, String> gtMap = roomData.getGtMaps();
		Map<String, Long> scoreMap = new HashMap<>();
		Map<String, Integer> rankMap = new HashMap<>();
		List<CWTeamJoinData> teamJoinList = new ArrayList<>();
		Map<String, CWTeamData> teamDataMap = new HashMap<>();
		
		// 待更新段位积分排名信息
		Map<String, Double> starScoreMap = new HashMap<>();
		// 是否处于赛季排位阶段
		boolean isSeasonOpen = CyborgLeaguaWarService.getInstance().isSeasonOpen();
		int season = CyborgLeaguaWarService.getInstance().getSeason();
		for (Entry<String, String> entry : gtMap.entrySet()) {
			String guildId = entry.getKey();
			String teamId = entry.getValue();
			long score = msg.getGuildHonor(guildId);
			scoreMap.put(teamId, score);
			// 记录联盟积分数据
			CWTeamJoinData teamJoinData = CyborgWarRedis.getInstance().getCWJoinTeamData(teamId, termId);
			CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
			teamJoinData.setScore(score);
			//添加记分占比
			HawkTuple2<Integer, Integer> rate = msg.getHonorRateAndReward(guildId);
			if(rate != null){
				teamJoinData.setScorePer(rate.first);
				teamJoinData.setCyborgItemTotal(rate.second);
			}
			//击杀最多
			HawkTuple2<String, String> killMax = msg.getKillPowerMaxPlayer(guildId);
			if(killMax != null){
				teamJoinData.setKillMaxPlayer(killMax.first);
				teamJoinData.setKillMaxPlayerInfo(killMax.second);
				DungeonRedisLog.log(killMax.first, "onBattleFinish killMax");
			}
			//承受伤害最多
			HawkTuple2<String, String> damagedMax = msg.getDamagedMaxPlayer(guildId);
			if(damagedMax != null){
				teamJoinData.setDamagedMaxPlayer(damagedMax.first);
				teamJoinData.setDamagedMaxPlayerInfo(damagedMax.second);
				DungeonRedisLog.log(damagedMax.first, "onBattleFinish damagedMax");
			}
			//击杀最多
			HawkTuple2<String, String> monsterMax = msg.getKillMonsterMaxPlayer(guildId);
			if(monsterMax != null){
				teamJoinData.setMonsterMaxPlayer(monsterMax.first);
				teamJoinData.setMonsterMaxPlayerInfo(monsterMax.second);
				DungeonRedisLog.log(monsterMax.first, "onBattleFinish monsterMax");
			}
			// 记录赛季战队总积分
			if (isSeasonOpen) {
				teamData.setSeasonScore(teamData.getSeasonScore() + score);
			}
			teamJoinList.add(teamJoinData);
			teamDataMap.put(teamId, teamData);
		}
		teamJoinList.sort(new Comparator<CWTeamJoinData>() {
			@Override
			public int compare(CWTeamJoinData arg0, CWTeamJoinData arg1) {
				long gap = arg1.getScore() - arg0.getScore();
				if (gap > 0) {
					return 1;
				} else if (gap < 0) {
					return -1;
				}
				gap = arg0.getTotalPower() - arg1.getTotalPower();
				if (gap > 0) {
					return -1;
				} else if (gap < 0) {
					return 1;
				}
				return arg0.getId().compareTo(arg1.getId());
			}
		});
		// 记录排行名次及战斗状态
		int rank = 1;
		List<CWTeamInfo> teamList = new ArrayList<>();
		for (CWTeamJoinData teamJoinData : teamJoinList) {
			String teamId = teamJoinData.getId();
			rankMap.put(teamId, rank);
			teamJoinData.setRank(rank);
			teamJoinData.setComplete(true);
			CWTeamInfo.Builder teamInfo = teamJoinData.build();
			// 段位变化
			if (isSeasonOpen) {	
				CWTeamData teamData = teamDataMap.get(teamId);
				int starBef = teamData.getStar();
				int starChange = CyborgLeaguaWarService.getInstance().getStarChange(starBef, rank);
				int starAft = starBef + starChange;
				teamData.setStar(starAft);
				teamJoinData.setStarAft(starAft);
				CyborgSeasonDivisionCfg cfgAft = CyborgLeaguaWarService.getInstance().getDivisionCfgByStar(starAft);
				int guildScoreBase = cfgAft.getSeasonGuildScoreByRank(rank);
				int scoreTarget = CyborgSeasonConstCfg.getInstance().getPointsTarget();
				int scoreCount = msg.getScoreOverPlayerCount(teamJoinData.getGuildId(), scoreTarget);
				int battlePlayerCount = msg.getBattlePlayerCount(teamJoinData.getGuildId());
				double k1 = CyborgSeasonConstCfg.getInstance().getJoinCoefficient();
				double k2 = CyborgSeasonConstCfg.getInstance().getPointsCoefficient();
				double k3 = CyborgSeasonConstCfg.getInstance().getBaseCoefficient();
				//联盟积分增量=段位名次分数*(小队参战人数*K1/10000+达成个人积分目标人数*K2/10000+K3)
				int guildScore = (int) (guildScoreBase *((battlePlayerCount * k1 /10000) + (scoreCount * k2 /10000) + k3));
				DungeonRedisLog.log(teamId, "onBattleFinish guildScore add:battlePlayerCount:{},battlePlayerCount:{},scoreCount:{},guildScore:{}",
						guildScoreBase,battlePlayerCount,scoreCount,guildScore);
				// 战队段位变化流水
				LogUtil.logCyborgSeasonStarFlow(teamData.getGuildId(), teamData.getId(), season, termId, teamData.getServerId(), starChange, teamData.getStar(),
						CLWStarReason.WAR.getNumber(), rank);
				// 添加战队的联盟积分
				long score = CyborgWarRedis.getInstance().addCLWGuildScore(teamData.getGuildId(), season, guildScore);
				teamInfo.setStarChange(starChange);
				teamInfo.setSeasonStar(starAft);
				starScoreMap.put(teamId, (double) calcStarScore(starAft, teamData.getSeasonScore()));
				
				
				// 记录联盟积分变化
				LogUtil.logCyborgSeasonGuildScore(teamData.getGuildId(), teamId, season, termId, teamData.getServerId(), guildScore, score);
			}
			teamInfo.setScore(teamJoinData.getScore());
			teamList.add(teamInfo.build());
			rank++;
			LogUtil.logCyborgTeamScore(teamId, teamJoinData.getName(), teamJoinData.getGuildId(), termId, teamJoinData.getServerId(), roomId, roomData.getRoomServerId(),
					teamJoinData.getScore(), teamJoinData.getRank());
		}
		CyborgWarRedis.getInstance().updateCWJoinTeamData(teamJoinList, termId);
		
		// 刷新战队信息及段位排行
		if(isSeasonOpen){
			CyborgWarRedis.getInstance().updateCWTeamData(new ArrayList<>(teamDataMap.values()));
			CyborgWarRedis.getInstance().addCLWTeamStarRanks(starScoreMap, season);
		}
		List<PlayerGameRecord> recods = msg.getPlayerRecords();
		
		// 记录玩家积分数据
		for (PlayerGameRecord recod : recods) {
			try {
				String playerId = recod.getPlayerId();
				CWPlayerData playerDate = CyborgWarRedis.getInstance().getCWPlayerData(playerId, termId);
				if (playerDate != null) {
					playerDate.setScore(recod.getHonor());
					CyborgWarRedis.getInstance().updateCWPlayerData(playerDate, termId);
					LogUtil.logCyborgPlayerScore(playerId, termId, playerDate.getTeamId(), playerDate.getGuildId(), playerDate.getScore(), rankMap.get(playerDate.getTeamId()));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
				continue;
			}
		}
		roomData.setSocreMap(scoreMap);
		roomData.setRankMap(rankMap);
		CyborgWarRedis.getInstance().updateCWRoomData(roomData, termId);

		// 记录战斗记录
		CWBattleLog.Builder builder = CWBattleLog.newBuilder();
		builder.setTermId(termId);
		builder.setRoomId(roomId);
		CWTimeChoose timeChose = getWarTimeChoose(roomData.getTimeIndex());
		builder.setTime(timeChose.getTime());
		builder.addAllTeamInfo(teamList);
		CWBattleLog battleLog = builder.build();
		for (String teamId : scoreMap.keySet()) {
			CyborgWarRedis.getInstance().addCWBattleLog(battleLog, teamId);
		}
	}

	/**
	 * 获取联盟的战队列表
	 * 
	 * @param guildId
	 * @return
	 */
	public List<String> getGuildTeams(String guildId) {
		List<String> teams = CyborgWarRedis.getInstance().getCWGuildTeams(guildId);
		return teams;
	}

	/**
	 * 判断当前联盟可否进行部分操作
	 * 
	 * @param guildId
	 * @return
	 */
	public int checkGuildOperation(String guildId) {
		List<String> teams = getGuildTeams(guildId);
		// 联盟没有战队,可以做所有操作
		if (teams.isEmpty()) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		// 匹配/战斗阶段,战队成员不能解散联盟
		if (activityInfo.state == CWActivityState.MATCH || activityInfo.state == CWActivityState.WAR) {
			return Status.Error.CYBORG_STATE_CANNOT_DISMISS_VALUE;
		}
		if (activityInfo.state == CWActivityState.SIGN) {
			for (String teamId : teams) {
				Integer timeIndex = signTeams.get(teamId);
				// 已报名
				if (timeIndex != null) {
					return Status.Error.CYBORG_SIGNED_CANNOT_DISMISS_VALUE;
				}
			}
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 检查目标玩家是否能退出联盟
	 * 
	 * @param guildId
	 * @param targetId
	 * @param isKick
	 *            是否被踢出联盟
	 * @return
	 */
	public int checkGuildMemberOps(String guildId, String targetId, boolean isKick) {
		Player player = GlobalData.getInstance().makesurePlayer(targetId);
		String teamId = getSelfTeamId(player);
		if (HawkOSOperator.isEmptyString(teamId)) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		// 匹配/战斗阶段,战队成员不能离开联盟
		if (activityInfo.state == CWActivityState.MATCH || activityInfo.state == CWActivityState.WAR) {
			return Status.Error.CYBORG_MEMBER_CANNOT_QUIT_VALUE;
		}
		if (activityInfo.state == CWActivityState.SIGN) {
			Integer timeIndex = signTeams.get(teamId);
			// 已报名
			if (timeIndex != null) {
				if (isKick) {
					return Status.Error.CYBORG_SIGNED_CANNOT_KICK_OUT_VALUE;
				} else {
					return Status.Error.CYBORG_SIGNED_CANNOT_QUIT_VALUE;
				}
			}
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取联盟战力排行榜信息
	 * 
	 * @param areaId
	 * @param player
	 * @return
	 */
	public GetCWTeamRankResp.Builder getGuildPowerRank(Player player) {
		String selfGuildId = player.getGuildId();
		String selfTeamId = getSelfTeamId(player);
		GetCWTeamRankResp.Builder builder = GetCWTeamRankResp.newBuilder();
		CWTeamRank.Builder selfRank = null;
		
		List<CWTeamRank.Builder> rlist = cyborgWarPowerRank.getGuildPowerRanks();
		for (CWTeamRank.Builder rank :rlist) {
			builder.addRankInfo(rank);
			if (rank.getTeamInfo().getId().equals(selfTeamId)) {
				selfRank = rank;
			}
		}
		if (!HawkOSOperator.isEmptyString(selfTeamId)) {
			if (selfRank == null) {
				selfRank = CWTeamRank.newBuilder();
				CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(selfTeamId);
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(selfGuildId);
				HawkTuple2<Integer, Long> joinInfo = getTeamMemberPower(selfGuildId, selfTeamId, player.getMainServerId());
				long power = joinInfo.second;
				CWTeamInfo.Builder teamBuilder = CWTeamInfo.newBuilder();
				teamBuilder.setId(teamData.getId());
				teamBuilder.setGuildId(selfGuildId);
				teamBuilder.setGuildName(guildObj.getName());
				teamBuilder.setGuildTag(guildObj.getTag());
				teamBuilder.setGuildFlag(guildObj.getFlagId());
				teamBuilder.setName(teamData.getName());
				teamBuilder.setBattlePoint(power);
				selfRank.setTeamInfo(teamBuilder);
				selfRank.setRank(-1);
				builder.addRankInfo(selfRank);
			}
			builder.setSelfRank(selfRank);
		}
		return builder;
	}

	/**
	 * 创建战队
	 * 
	 * @param player
	 * @param name
	 * @return
	 */
	public int onCreateTeam(Player player, String name) {
		String guildId = player.getGuildId();

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}

		// 战队数量超标
		List<String> teamIds = getGuildTeams(player.getGuildId());
		if (teamIds.size() >= CyborgConstCfg.getInstance().getTeamNumLimit()) {
			return Status.Error.CYBORG_TEAM_NUM_OVER_LIMIT_VALUE;
		}

		if (activityInfo.getState() == CWActivityState.MATCH || activityInfo.getState() == CWActivityState.WAR) {
			return Status.Error.CYBORG_CANNOT_DISMISS_STATE_VALUE;
		}

		Map<String, CWTeamData> teamMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		boolean nameExist = false;
		for (Entry<String, CWTeamData> entry : teamMap.entrySet()) {
			CWTeamData team = entry.getValue();
			if (team.getName().equals(name)) {
				nameExist = true;
			}
		}
		if (nameExist) {
			return Status.Error.CYBORG_TEAM_NAME_EXIST_VALUE;
		}
		GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
		CWTeamData teamData = new CWTeamData();
		String id = HawkOSOperator.randomUUID();
		teamData.setId(id);
		teamData.setGuildId(guildId);
		teamData.setName(name);
		teamData.setCreateTime(HawkTime.getMillisecond());
		teamData.setServerId(GsConfig.getInstance().getServerId());
		teamData.setGuildName(guildObj.getName());
		teamData.setGuildTag(guildObj.getTag());
		teamData.setGuildFlag(guildObj.getFlagId());
		CyborgWarRedis.getInstance().updateCWTeamData(teamData);
		CyborgWarRedis.getInstance().addCWGuildTeam(guildId, id);
		if (!guildTeams.containsKey(guildId)) {
			guildTeams.put(guildId, new ArrayList<>());
		}
		guildTeams.get(guildId).add(id);

		syncTeamInfo(guildId, player.getMainServerId());
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CYBOR_TEAM_NAME, id, name);
		
		// 队伍创建出来后，补发一条上报，因为创建前不知道其ID，没法上报相关参数
		JSONObject json = new JSONObject();
		json.put("msg_type", 0);
		json.put("post_id", name);
		json.put("alliance_id", player.hasGuild() ? player.getGuildId() : "");
		json.put("param_id", id);
		GameTssService.getInstance().wordUicChatFilter(player, name, 
				MsgCategory.CYBOR_TEAM_NAME.getNumber(), 0 - GameMsgCategory.CREATE_CYBOR_TEAM, 
				"", json, 0);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 解散战队
	 * 
	 * @param player
	 * @param teamId
	 * @return
	 */
	public int onDismissTeam(Player player, String teamId) {
		// 联盟权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.TIBERIUM_WAR_MANGER)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		String guildId = player.getGuildId();
		if (!guildTeams.containsKey(guildId)) {
			return Status.Error.CYBORG_TEAM_NOT_EXIST_VALUE;
		}
		CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
		if (teamData == null) {
			return Status.Error.CYBORG_TEAM_NOT_EXIST_VALUE;
		}

		// 开启阶段,已报名的战队不能进行相关操作
		if (teamData.getTermId() != 0 && teamData.getTermId() == activityInfo.getTermId()
				&& (activityInfo.getState() != CWActivityState.NOT_OPEN && activityInfo.getState() != CWActivityState.CLOSE)) {
			return Status.Error.CYBORG_TEAM_HAS_JOINED_VALUE;
		}

		if (activityInfo.getState() == CWActivityState.MATCH || activityInfo.getState() == CWActivityState.WAR) {
			return Status.Error.CYBORG_CANNOT_DISMISS_STATE_VALUE;
		}

		CyborgWarRedis.getInstance().removeCWGuildTeam(guildId, teamId);
		CyborgWarRedis.getInstance().removeCWTeamData(teamId);
		CyborgWarRedis.getInstance().removeCWPlayerIds(teamId);
		cyborgWarPowerRank.removeCWTeamPowerRank(teamId);
		CyborgWarRedis.getInstance().removeCWTeamLastJoins(teamId);
		guildTeams.get(guildId).remove(teamId);
		syncTeamInfo(guildId, player.getMainServerId());
		if(CyborgLeaguaWarService.getInstance().isInSeason()){
			int season = CyborgLeaguaWarService.getInstance().getSeason();
			CyborgWarRedis.getInstance().removeCLWTeamStarRank(teamId, season);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 修改战队名称
	 * 
	 * @param player
	 * @param teamId
	 * @param name
	 * @return
	 */
	public int onEditTeamName(Player player, String teamId, String name) {

		int regexType = GsConst.RegexType.ALLLETTER + GsConst.RegexType.NUM + GsConst.RegexType.CHINESELETTER;
		if (!GameUtil.stringOnlyContain(name, regexType, "-_")) {
			return Status.NameError.CONTAIN_ILLEGAL_CHART_VALUE;
		}

		CWTeamData teamData = CyborgWarRedis.getInstance().getCWTeamData(teamId);
		if (teamData == null) {
			return Status.Error.CYBORG_TEAM_NOT_EXIST_VALUE;
		}

		// 开启阶段,已报名的战队不能进行相关操作
		if (teamData.getTermId() != 0 && teamData.getTermId() == activityInfo.getTermId()
				&& (activityInfo.getState() != CWActivityState.NOT_OPEN && activityInfo.getState() != CWActivityState.CLOSE)) {
			return Status.Error.CYBORG_TEAM_HAS_JOINED_VALUE;
		}

		if (activityInfo.getState() == CWActivityState.MATCH || activityInfo.getState() == CWActivityState.WAR) {
			return Status.Error.CYBORG_CANNOT_DISMISS_STATE_VALUE;
		}
		List<String> guildTeams = getGuildTeams(player.getGuildId());
		Map<String, CWTeamData> teamMap = CyborgWarRedis.getInstance().getCWTeamData(guildTeams);
		boolean nameExist = false;
		for (Entry<String, CWTeamData> entry : teamMap.entrySet()) {
			CWTeamData team = entry.getValue();
			if (team.getName().equals(name)) {
				nameExist = true;
			}
		}
		if (nameExist) {
			return Status.Error.CYBORG_TEAM_NAME_EXIST_VALUE;
		}

		teamData.setName(name);
		CyborgWarRedis.getInstance().updateCWTeamData(teamData);
		syncTeamInfo(teamData.getGuildId(), player.getMainServerId());
		LogUtil.logSecTalkFlow(player, null, LogMsgType.CYBOR_TEAM_NAME, teamId, name);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 计算联盟可领取奖励
	 * 
	 * @param scoreData
	 */
	public List<Integer> calcGuildAddReward(long score) {
		List<Integer> rewardedList = new ArrayList<>();
		ConfigIterator<CyborgSeasonGuildAwardCfg> its = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonGuildAwardCfg.class);
		for (CyborgSeasonGuildAwardCfg cfg : its) {
			int id = cfg.getId();
			if (score >= cfg.getScore()) {
				rewardedList.add(id);
			}
		}
		return rewardedList;
	}
	
	/**
	 * 获取本服所有的战队
	 * @return
	 */
	public List<CWTeamData> getServerTeam() {
		List<CWTeamData> teamList = new ArrayList<>();
		List<String> teamIds = new ArrayList<>();
		for (Entry<String, List<String>> entry : guildTeams.entrySet()) {
			List<String> teams = entry.getValue();
			if (!CollectionUtils.isEmpty(teams)) {
				teamIds.addAll(teams);
			}
		}
		Map<String, CWTeamData> teamMap = CyborgWarRedis.getInstance().getCWTeamData(teamIds);
		if (!teamMap.isEmpty()) {
			teamList.addAll(teamMap.values());
		}
		return teamList;
	}
	
	/**
	 * 合并计算星级积分
	 * @param star
	 * @param score
	 * @return
	 */
	public long calcStarScore(int star, long score) {
		return star * CWConst.STAR_SCORE_OFFSET + score;
	}
	
	/**
	 * 计算星级和赛季积分
	 * @param calcedScore
	 * @return
	 */
	public HawkTuple2<Integer, Long> splitStarScore(long calcedScore) {
		int star = (int) (calcedScore / CWConst.STAR_SCORE_OFFSET);
		long score = calcedScore % CWConst.STAR_SCORE_OFFSET;
		return new HawkTuple2<Integer, Long>(star, score);
	}
	
	
}
