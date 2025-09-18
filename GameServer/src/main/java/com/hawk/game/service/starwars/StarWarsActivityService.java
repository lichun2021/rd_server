package com.hawk.game.service.starwars;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.game.protocol.Activity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple3;
import org.hawk.xid.HawkXID;

import com.alibaba.fastjson.JSON;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.SWScoreEvent;
import com.hawk.game.GsConfig;
import com.hawk.game.config.SWHonorRankAwardCfg;
import com.hawk.game.config.SWKillAwardCfg;
import com.hawk.game.config.StarWarsConstCfg;
import com.hawk.game.config.StarWarsPartCfg;
import com.hawk.game.config.StarWarsTimeCfg;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.lianmengstarwars.SWExtraParam;
import com.hawk.game.lianmengstarwars.SWRoomManager;
import com.hawk.game.lianmengstarwars.msg.SWBilingInformationMsg;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Chat.ChatMsg;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.SW.PBSWGuildInfo;
import com.hawk.game.protocol.SW.PBSWPlayerInfo;
import com.hawk.game.protocol.SW.SWPageInfo;
import com.hawk.game.protocol.SW.SWState;
import com.hawk.game.protocol.SW.SWStateInfo;
import com.hawk.game.protocol.StarWarsOfficer.StarWarsOfficerStruct;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankObject;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.MailService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.starwars.StarWarsConst.SWActivityState;
import com.hawk.game.service.starwars.StarWarsConst.SWFightState;
import com.hawk.game.service.starwars.StarWarsConst.SWGroupType;
import com.hawk.game.service.starwars.StarWarsConst.SWRoomState;
import com.hawk.game.service.starwars.StarWarsConst.SWWarType;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.LogUtil;

/**
 * 泰伯利亚联赛服务类
 * @author Jesse
 */
public class StarWarsActivityService extends HawkAppObj {
	
	/**
	 * 全局实例对象
	 */
	private static StarWarsActivityService instance = null;
	
	/**
	 * 活动时间信息数据
	 */
	public static SWActivityData activityInfo = new SWActivityData();
	
	/**
	 * 匹配状态信息
	 */
	public static SWMatchState matchServerInfo = new SWMatchState();
	/**
	 * 战斗阶段状态信息
	 */
	public static SWFightData fightData = new SWFightData();
	
	/**
	 * 参与联盟信息
	 */
	public static Map<String, SWGroupType> joinGuilds = new ConcurrentHashMap<>();	
	
	/**
	 * 记录每个区服进入的人数.
	 */
	private Map<String, Set<String>> guildPlayerSet = new ConcurrentHashMap<>();	
	
	
	/**
	 * 当前大区结算服
	 */
	private static String calServer;
	
	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static StarWarsActivityService getInstance() {
		return instance;
	}

	public StarWarsActivityService(HawkXID xid) {
		super(xid);
		instance = this;
	}
	
	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		try {
			//计算大区结算服
			calServer = getCalServer();
			// 读取活动阶段数据
			activityInfo = RedisProxy.getInstance().getSWActivityInfo();
			// 进行阶段检测
			checkStateChange();
			// 拉取入选联盟信息
			loadJoinGuilds();
			// 阶段轮询
			addTickable(new HawkPeriodTickable(1000) {
				@Override
				public void onPeriodTick() {
					// 活动阶段轮询
					stateTick();
					// 匹配轮询检测
					if (activityInfo.getState() == SWActivityState.MATCH) {
						matchTick();
					}
					// 比赛阶段轮询检测
					else if( activityInfo.getState().getNumber() > SWActivityState.MATCH.getNumber() && activityInfo.getState().getNumber() < SWActivityState.END.getNumber()){
						try {
							fightTick();
							fightCalTick();
						} catch (Exception e) {
							HawkException.catchException(e);
						}
					}
				}
			});
			
			// 联盟总战力排行榜
			addTickable(new HawkPeriodTickable(60000) {
				@Override
				public void onPeriodTick() {
					updateServerGuildInfo();
				}
			});
			
			boolean rlt = StarWarsOfficerService.getInstance().init();
			if (!rlt) {
				return false;
			}
			
			//10秒tick一次.
			this.addTickable(new HawkPeriodTickable(10000) {
				
				@Override
				public void onPeriodTick() {
					StarWarsOfficerService.getInstance().onTick();
					
				}
			});
			
			int termId = activityInfo.getTermId();
			String areaId = GsConfig.getInstance().getAreaId();
			HawkLog.logPrintln("StarWarsActivityService calServer choose! termId: {},areaId:{},serverId:{}", termId,areaId,calServer);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
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
			if (activityInfo.getState() != SWActivityState.MATCH) {
				return;
			}
			
			// 非战场服刷入本服司令盟/战力第一盟信息
			if (!activityInfo.isPrepareFinish()) {
				flushJoinGuild();
				activityInfo.setPrepareFinish(true);
				RedisProxy.getInstance().updateSWActivityInfo(activityInfo);
			}
			
			String matchKey = RedisProxy.getInstance().SWACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
			String matchLockKey = RedisProxy.getInstance().SWACTIVITY_MATCH_LOCK + ":" + activityInfo.getTermId();
			
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
			
			long prepareEndTime = activityInfo.getTimeCfg().getSignEndTimeValue() + StarWarsConstCfg.getInstance().getMatchPrepareTime();
			// 匹配准备时间未结束,不进行其他处理
			if (HawkTime.getMillisecond() <= prepareEndTime) {
				return;
			}
			
			String serverId = GsConfig.getInstance().getServerId();
			long lock = RedisProxy.getInstance().getMatchLock(matchLockKey);
			boolean needSync = false;
			// 获取到匹配权限,设置有效期并进行匹配
			if (lock > 0) {
				RedisProxy.getInstance().getRedisSession().expire(matchLockKey, StarWarsConstCfg.getInstance().getMatchLockExpire());
				pickJoiner();
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
					syncStateInfo(player);
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 刷新入选联盟信息
	 */
	private void flushJoinGuild() {
		int termId = activityInfo.getTermId();
		if (termId == 0) {
			return;
		}
		try {
			StarWarsPartCfg cfg = getLocalServerPartCfg();
			String serverId = GsConfig.getInstance().getServerId();
			// 本服未分配到赛区
			if (cfg == null) {
				return;
			}
			String prsientId = PresidentFightService.getInstance().getPresidentPlayerId();
			String pickGuildId = null;
			// 存在本服司令,则本服司令所在的盟入围 
			if (!HawkOSOperator.isEmptyString(prsientId) && GlobalData.getInstance().isLocalPlayer(prsientId)) {
				String kingGuild = GuildService.getInstance().getPlayerGuildId(prsientId);
				if (!HawkOSOperator.isEmptyString(kingGuild)) {
					pickGuildId = kingGuild;
				}
			}
			// 没有本服司令联盟,则取当前战力第一联盟入围
			if (HawkOSOperator.isEmptyString(pickGuildId)) {
				RankObject rankObject = RankService.getInstance().getRankObject(RankType.ALLIANCE_FIGHT_KEY);
				List<RankInfo> rankList = new ArrayList<>(rankObject.getSortedRank());
				if (!CollectionUtils.isEmpty(rankList)) {
					RankInfo rankInfo = rankList.get(0);
					pickGuildId = rankInfo.getId();
				}
			}
			
			if (HawkOSOperator.isEmptyString(pickGuildId)) {
				return;
			}
			updateSWGuildData(serverId, cfg, pickGuildId);
			RedisProxy.getInstance().updateSWServerGuild(termId, serverId, pickGuildId);
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(pickGuildId);
			if (guildObj == null) {
				return;
			}
			//把盟主刷新进去.
			Player leaderPlayer = GlobalData.getInstance().makesurePlayer(guildObj.getLeaderId());
			if (leaderPlayer != null) {
				CrossPlayerStruct.Builder structBuilder = BuilderUtil.buildCrossPlayer(leaderPlayer);
				RedisProxy.getInstance().addStarWarsJoinGuildLeader(GsConfig.getInstance().getServerId(), structBuilder.build());
			}		
			
			DungeonRedisLog.log("StarWarsActivityService", "join guild:{} guildName:{} leaderId:{} leadername:{}",pickGuildId, guildObj.getName(),leaderPlayer.getId(),leaderPlayer.getName());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 更新入围联盟信息
	 * @param serverId
	 * @param cfg
	 * @param guildId
	 */
	public void updateSWGuildData(String serverId, StarWarsPartCfg cfg, String guildId) {
		int termId = getTermId();
		GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if (guildObj == null) {
			return;
		}
		long power = GuildService.getInstance().getGuildBattlePoint(guildId);
		SWGuildData guildData = new SWGuildData();
		guildData.setTermId(termId);
		guildData.setId(guildId);
		guildData.setName(guildObj.getName());
		guildData.setTag(guildObj.getTag());
		guildData.setLeaderId(guildObj.getLeaderId());
		guildData.setLeaderName(guildObj.getLeaderName());
		guildData.setFlag(guildObj.getFlagId());
		guildData.setServerId(serverId);
		guildData.setZoneId(cfg.getZone());
		guildData.setTeamId(cfg.getTeam());
		guildData.setPower(power);
		RedisProxy.getInstance().updateSWGuildData(guildData, termId);
	}

	/**
	 * 战斗阶段状态轮询
	 */
	protected void fightTick() {
		if (!fightData.isHasInit()) {
			fightData = RedisProxy.getInstance().getSWFightData();
			if (fightData == null) {
				fightData = new SWFightData();
				RedisProxy.getInstance().updateSWFightData(fightData);
			}
			fightData.setHasInit(true);
		}
		if (fightData.getTermId() != activityInfo.getTermId()) {
			fightData = new SWFightData();
			fightData.setTermId(activityInfo.getTermId());
			fightData.setHasInit(true);
			RedisProxy.getInstance().updateSWFightData(fightData);
		}
		long curTime = HawkTime.getMillisecond();
	
		boolean needUpdate = false;
		
		StarWarsTimeCfg timeCfg = activityInfo.getTimeCfg();
		if(timeCfg == null){
			return;
		}
		int termId = activityInfo.getTermId();
		StarWarsPartCfg localCfg = getLocalServerPartCfg();
		int zoneId = -1;
		int teamId = -1;
		if (localCfg != null) {
			zoneId = localCfg.getZone();
			teamId = localCfg.getTeam();
		}
		long openTime = StarWarsConstCfg.getInstance().getWarOpenTime();
		
		long matchEndTime = timeCfg.getMatchEndTimeValue();
		long warMangeEndTimeOne = timeCfg.getMangeEndTimeOneValue();
		long warStartTimeOne = timeCfg.getWarStartTimeOneValue();
		long warFinishTimeOne = warStartTimeOne + openTime;
		long warEndTimeOne = timeCfg.getWarEndTimeOneValue();
		long warMangeEndTimeTwo = timeCfg.getMangeEndTimeTwoValue();
		long warStartTimeTwo = timeCfg.getWarStartTimeTwoValue();
		long warFinishTimeTwo = warStartTimeTwo + openTime;
		long warEndTimeTwo = timeCfg.getWarEndTimeTwoValue();
		long warMangeEndTimeThree = timeCfg.getMangeEndTimeThreeValue();
		long warStartTimeThree = timeCfg.getWarStartTimeThreeValue();
		long warFinishTimeThree = warStartTimeThree + openTime;
		long warEndTimeThree = timeCfg.getWarEndTimeThreeValue();
		
		SWFightState state = SWFightState.NOT_OPEN;
		boolean stageChange = false;
		if (curTime < matchEndTime) {
			state = SWFightState.NOT_OPEN;
		} else if (curTime >= matchEndTime && curTime < warMangeEndTimeOne) {
			state = SWFightState.FIRST_MANAGE;
		} else if (curTime >= warMangeEndTimeOne && curTime < warStartTimeOne) {
			state = SWFightState.FIRST_WAIT;
		} else if (curTime >= warStartTimeOne && curTime < warFinishTimeOne) {
			state = SWFightState.FIRST_OPEN;
			if (!fightData.isFirstWarFinish()) {
				SWRoomData roomData = getFirstWarRoom(termId, zoneId, teamId);
				// 本赛区比赛已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.SECOND_MANGE;
					fightData.setFirstWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.SECOND_MANGE;
			}
		} else if (curTime >= warFinishTimeOne && curTime < warEndTimeOne) {
			state = SWFightState.FIRST_CALC;
			if (!fightData.isFirstWarFinish()) {
				SWRoomData roomData = getFirstWarRoom(termId, zoneId, teamId);
				// 本赛区比赛已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.SECOND_MANGE;
					fightData.setFirstWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.SECOND_MANGE;
			}
		} else if (curTime >= warEndTimeOne && curTime < warMangeEndTimeTwo) {
			state = SWFightState.SECOND_MANGE;
		} else if (curTime >= warMangeEndTimeTwo && curTime < warStartTimeTwo) {
			state = SWFightState.SECOND_WAIT;
		} else if (curTime >= warStartTimeTwo && curTime < warFinishTimeTwo) {
			state = SWFightState.SECOND_OPEN;
			if (!fightData.isSecondWarFinish()) {
				SWRoomData roomData = getSecondWarRoom(termId, zoneId);
				// 第二场已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.THIRD_MANGE;
					fightData.setSecondWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.THIRD_MANGE;
			}
		} else if (curTime >= warFinishTimeTwo && curTime < warEndTimeTwo) {
			state = SWFightState.SECOND_CALC;
			if (!fightData.isSecondWarFinish()) {
				SWRoomData roomData = getSecondWarRoom(termId, zoneId);
				// 本赛区比赛已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.THIRD_MANGE;
					fightData.setSecondWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.THIRD_MANGE;
			}
		} else if (curTime >= warEndTimeTwo && curTime < warMangeEndTimeThree) {
			state = SWFightState.THIRD_MANGE;
		} else if (curTime >= warMangeEndTimeThree && curTime < warStartTimeThree) {
			state = SWFightState.THIRD_WAIT;
		} else if (curTime >= warStartTimeThree && curTime < warFinishTimeThree) {
			state = SWFightState.THIRD_OPEN;
			if (!fightData.isThirdWarFinish()) {
				SWRoomData roomData = getThirdWarRoom(termId);
				// 第三场已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.FINISH;
					fightData.setThirdWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.FINISH;
			}
		} else if (curTime >= warFinishTimeThree && curTime < warEndTimeThree) {
			state = SWFightState.THIRD_CALC;
			if (!fightData.isThirdWarFinish()) {
				SWRoomData roomData = getThirdWarRoom(termId);
				// 第三场已经结束
				if (roomData != null && !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
					state = SWFightState.FINISH;
					fightData.setThirdWarFinish(true);
					stageChange = true;
				}
			} else {
				state = SWFightState.FINISH;
			}
		} else if (curTime >= warEndTimeThree) {
			state = SWFightState.FINISH;
		}
		int i = 0;
		while (fightData.getState() != state && i <= 30) {
			if (fightData.getState() == SWFightState.NOT_OPEN) {
				// 进入第一场可管理阶段
				fightData.setState(SWFightState.FIRST_MANAGE);
				clearAllGuildJoinPlayer();				
				HawkLog.logPrintln("StarWarsActivityService onFightOpen! ");
			} else if (fightData.getState() == SWFightState.FIRST_MANAGE) {
				// 战场服在初赛关节阶段结束后,清空上一期官职信息
				flushJoinPlayerInfo(SWWarType.FIRST_WAR);
				fightData.setState(SWFightState.FIRST_WAIT);
			} else if (fightData.getState() == SWFightState.FIRST_WAIT) {
				// 进入第一场开战阶段
				fightData.setState(SWFightState.FIRST_OPEN);
			} else if (fightData.getState() == SWFightState.FIRST_OPEN) {
				// 进入第一场结果整理阶段
				fightData.setState(SWFightState.FIRST_CALC);
			} else if (fightData.getState() == SWFightState.FIRST_CALC) {
				// 进入第二场可管理阶段
				fightData.setState(SWFightState.SECOND_MANGE);
				onWarFinish(SWWarType.FIRST_WAR);
			} else if (fightData.getState() == SWFightState.SECOND_MANGE) {	
				flushJoinPlayerInfo(SWWarType.SECOND_WAR);
				// 进入第二场准备阶段
				fightData.setState(SWFightState.SECOND_WAIT);
			} else if (fightData.getState() == SWFightState.SECOND_WAIT) {
				// 进入第二场开战阶段
				fightData.setState(SWFightState.SECOND_OPEN);
			} else if (fightData.getState() == SWFightState.SECOND_OPEN) {
				// 进入第二场结果整理阶段
				fightData.setState(SWFightState.SECOND_CALC);
			} else if (fightData.getState() == SWFightState.SECOND_CALC) {
				// 战斗阶段结束
				fightData.setState(SWFightState.THIRD_MANGE);
				onWarFinish(SWWarType.SECOND_WAR);
			} else if (fightData.getState() == SWFightState.THIRD_MANGE) {
				flushJoinPlayerInfo(SWWarType.THIRD_WAR);
				// 进入第三场准备阶段
				fightData.setState(SWFightState.THIRD_WAIT);
			} else if (fightData.getState() == SWFightState.THIRD_WAIT) {
				// 进入第三场开战阶段
				fightData.setState(SWFightState.THIRD_OPEN);
			} else if (fightData.getState() == SWFightState.THIRD_OPEN) {
				// 进入第三场结果整理阶段
				fightData.setState(SWFightState.THIRD_CALC);
			} else if (fightData.getState() == SWFightState.THIRD_CALC) {
				// 战斗阶段结束
				fightData.setState(SWFightState.FINISH);
				onWarFinish(SWWarType.THIRD_WAR);
			}
			needUpdate = true;
			stageChange = true;
			i++;
		}
		
		// 非战场服轮训检测战场开启推送邮件
		warOpenNoticeCheck();
		
		if (stageChange || needUpdate) {
			RedisProxy.getInstance().updateSWFightData(fightData);
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
		}
	}
	
	
	public void fightCalTick(){
		if(!this.isCalServer()){
			return;
		}
		StarWarsTimeCfg timeCfg = activityInfo.getTimeCfg();
		if(timeCfg == null){
			return;
		}
		long openTime = StarWarsConstCfg.getInstance().getWarOpenTime();
		long warStartTimeOne = timeCfg.getWarStartTimeOneValue();
		long warFinishTimeOne = warStartTimeOne + openTime;
		long warEndTimeOne = timeCfg.getWarEndTimeOneValue();
		long warStartTimeTwo = timeCfg.getWarStartTimeTwoValue();
		long warFinishTimeTwo = warStartTimeTwo + openTime;
		long warEndTimeTwo = timeCfg.getWarEndTimeTwoValue();
		long curTime = HawkTime.getMillisecond();
		//第一阶段战斗完成，匹配第二阶段战斗
		if (curTime >= warFinishTimeOne && curTime < warEndTimeOne) {
			if(!matchServerInfo.isFirstWarFinsh()){
				String matchKey = RedisProxy.getInstance().SWACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
				String finishStr = RedisProxy.getInstance().getRedisSession().hGet(matchKey, "isFirstWarFinsh", 0);
				if (!HawkOSOperator.isEmptyString(finishStr)) {
					matchServerInfo.setFirstWarFinsh(true);
				}else{
					boolean isFinish = firstWarStatusCheck();
					if(isFinish){
						matchServerInfo.setFirstWarFinsh(true);
						RedisProxy.getInstance().getRedisSession().hSet(matchKey, "isFirstWarFinsh", String.valueOf(true));
					}
				}
				
			}
		}
		//第二阶段战斗完成，匹配第三阶段战斗
		if (curTime >= warFinishTimeTwo && curTime < warEndTimeTwo) {
			if(!matchServerInfo.isSecondWarFinsh()){
				String matchKey = RedisProxy.getInstance().SWACTIVITY_MATCH_STATE + ":" + activityInfo.getTermId();
				String finishStr = RedisProxy.getInstance().getRedisSession().hGet(matchKey, "isSecondWarFinsh", 0);
				if (!HawkOSOperator.isEmptyString(finishStr)) {
					matchServerInfo.setSecondWarFinsh(true);
				}else{
					boolean isFinish = secondWarStatusCheck();
					if(isFinish){
						matchServerInfo.setSecondWarFinsh(true);
						RedisProxy.getInstance().getRedisSession().hSet(matchKey, "isSecondWarFinsh", String.valueOf(true));
					}
				}
				
			}
		}
		
	}
	
	/**
	 * 战场开启前推送邮件轮询检测
	 */
	private void warOpenNoticeCheck() {
		int termId = activityInfo.getTermId();
		String guildId = RedisProxy.getInstance().getSWServerGuild(termId, GsConfig.getInstance().getServerId());
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		SWGroupType groupType = joinGuilds.get(guildId);
		StarWarsTimeCfg timeCfg = activityInfo.getTimeCfg();
		
		boolean needUpdate = false;
		long lastCheckTime = fightData.getLastCheckTime();
		long currTime = HawkTime.getMillisecond();
		int currStateNum = fightData.getState().getNumber();
		List<Long> timeGapList = StarWarsConstCfg.getInstance().getWarOpenNoticeTimeList();
		List<Long> realTimeList = new ArrayList<>();
		boolean needPush = false;
		long openGape = 0;
		MailId noticeMail;
		if (currStateNum >= SWFightState.FIRST_WAIT.getNumber() && currStateNum <= SWFightState.FIRST_OPEN.getNumber()) {
			if(groupType!= SWGroupType.PARTNER){
				return;
			}
			noticeMail = MailId.SW_WAR_ONE_OPEN;
			if(lastCheckTime < timeCfg.getMatchEndTimeValue()){
				fightData.setNoticeCnt(0);
				needUpdate = true;
			}
			for(Long timeGap : timeGapList){
				realTimeList.add(timeCfg.getWarStartTimeOneValue() - timeGap);
			}
		} else if (currStateNum >= SWFightState.SECOND_WAIT.getNumber() && currStateNum <= SWFightState.SECOND_OPEN.getNumber()) {
			if(groupType!= SWGroupType.FIRST_WINNER){
				return;
			}
			noticeMail = MailId.SW_WAR_TWO_OPEN;
			if(lastCheckTime < timeCfg.getWarEndTimeOneValue()){
				fightData.setNoticeCnt(0);
				needUpdate = true;
			}
			for(Long timeGap : timeGapList){
				realTimeList.add(timeCfg.getWarStartTimeTwoValue() - timeGap);
			}

		} else if (currStateNum >= SWFightState.THIRD_WAIT.getNumber() && currStateNum <= SWFightState.THIRD_OPEN.getNumber()) {
			if(groupType!= SWGroupType.SECOND_WINNER){
				return;
			}
			noticeMail = MailId.SW_WAR_THREE_OPEN;
			if(lastCheckTime < timeCfg.getWarEndTimeTwoValue()){
				fightData.setNoticeCnt(0);
				needUpdate = true;
			}
			for(Long timeGap : timeGapList){
				realTimeList.add(timeCfg.getWarStartTimeThreeValue() - timeGap);
			}
		}
		else{
			return;
		}
		
		for (int i = 0; i < realTimeList.size(); i++) {
			long time = realTimeList.get(i);
			if (currTime >= time && fightData.getNoticeCnt() < i + 1) {
				needPush = true;
				needUpdate = true;
				openGape = timeGapList.get(i) / 1000;
				break;
			}
		}
		if (needPush) {
			GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
					.setMailId(noticeMail)
					.addContents(openGape));
			fightData.setNoticeCnt(fightData.getNoticeCnt() + 1);
		}
		if (needUpdate) {
			fightData.setLastCheckTime(currTime);
			RedisProxy.getInstance().updateSWFightData(fightData);
		}
		
	}

	/**
	 * 本服战斗结束
	 * @param firstWar
	 */
	private void onWarFinish(SWWarType warType) {
		sendResultMail(warType);
		sendRankMail(warType);
		sendKingMail(warType);
		clearAllGuildJoinPlayer();
	}
	
	private void sendRankMail(SWWarType warType) {
		
		switch (warType) {
		case FIRST_WAR:
			if(fightData.isFirstRankRewarded()){
				return;
			}
			break;
		case SECOND_WAR:
			if(fightData.isSecondRankRewarded()){
				return;
			}
			break;
		case THIRD_WAR:
			if(fightData.isThirdRankRewarded()){
				return;
			}
			break;
		default:
			return;
		}
		
		String serverId = GsConfig.getInstance().getServerId();
		int termId = getTermId();
		String guildId = RedisProxy.getInstance().getSWServerGuild(termId, serverId);
		SWRoomData roomData = getLocalRoomData(warType);
		if (roomData == null) {
			return;
		}
		

		SWBilingInformationMsg msg = new SWBilingInformationMsg();
		msg.mergeFrom(roomData.getBilingInforStr());
		// 同分的按联盟战力排
		List<PBSWGuildInfo> guildInfoList = msg.getLastSyncpb().getGuildInfoList();
		
		PBSWGuildInfo bilingInfo = null;
		for (int i = 0; i < guildInfoList.size(); i++) {
			PBSWGuildInfo ginfo = guildInfoList.get(i);
			if (ginfo.getGuildId().equals(guildId)) {
				bilingInfo = ginfo;
			}
		}
		if (bilingInfo == null) {
			return;
		}
		
		// TODO
		// SW_HONOR_RANK_REWARD = 2020062327; // 荣誉点排名奖励
		// SW_KILL_RANK_REWARD = 2020062328; // 消灭战力目标奖励
		String honorRankReward = "";
		final int honorRankF = bilingInfo.getHonorRank();
		SWHonorRankAwardCfg hraCfg = HawkConfigManager.getInstance().getConfigIterator(SWHonorRankAwardCfg.class).stream()
				.filter(cfg -> cfg.getRound() == warType.getNumber())
				.filter(cfg -> cfg.getRankMin() <= honorRankF && cfg.getRankMax() >= honorRankF)
				.findFirst().orElse(null);
		if(Objects.nonNull(hraCfg)){
			honorRankReward = hraCfg.getAward();
		}
		
		
		List<PBSWPlayerInfo> playerInfoList = msg.getLastSyncpb().getPlayerInfoList().stream().filter(joinRoom->joinRoom.getGuildId().equals(guildId) ).collect(Collectors.toList());
		for (PBSWPlayerInfo joinRoom : playerInfoList) {
			if (StringUtils.isNotEmpty(honorRankReward)) {
				MailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(joinRoom.getPlayerId())
						.setMailId(MailId.SW_HONOR_RANK_REWARD)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(bilingInfo.getHonor(), honorRankF)
						.addRewards(ItemInfo.valueListOf(honorRankReward))
						.build());
			}
			
			String killReward = "";
			final HawkTuple3<Integer, Integer, Integer> killHonor = msg.getKillHonor(joinRoom.getPlayerId());
			SWKillAwardCfg killCfg = HawkConfigManager.getInstance().getConfigIterator(SWKillAwardCfg.class).stream()
					.filter(cfg -> cfg.getTarget() <= killHonor.first)
					.filter(cfg -> cfg.getRound() == warType.getNumber())
					.sorted(Comparator.comparingInt(SWKillAwardCfg::getTarget).reversed())
					.findFirst().orElse(null);
			if (Objects.nonNull(killCfg)) {
				killReward = killCfg.getAward();
			}
			if (StringUtils.isNotEmpty(killReward)) {
				MailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(joinRoom.getPlayerId())
						.setMailId(MailId.SW_KILL_RANK_REWARD)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addContents(killHonor)
						.addRewards(ItemInfo.valueListOf(killReward))
						.build());
			}
			ActivityManager.getInstance().postEvent(new SWScoreEvent(joinRoom.getPlayerId(), killHonor.second, killHonor.third, 1,true));
			DungeonRedisLog.log("StarWarsActivityService", "send rank mail playerId:{} 2020062327 rank:{} reward:{}, 2020062328 kill:{} reward:{}",
					joinRoom.getPlayerId(), honorRankF, honorRankReward, killHonor, killReward);
		}
		
		switch (warType) {
		case FIRST_WAR:
			fightData.setFirstRankRewarded(true);
			break;
		case SECOND_WAR:
			fightData.setSecondRankRewarded(true);
			break;
		case THIRD_WAR:
			fightData.setThirdRankRewarded(true);
			break;
		default:
			break;
		}
		
		RedisProxy.getInstance().updateSWFightData(fightData);
		try {
			if (warType== SWWarType.THIRD_WAR && GuildService.getInstance().isGuildExist(guildId)) {
				Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
				if (opActivity.isPresent()) {
					SeasonActivity activity = opActivity.get();
					if(honorRankF>=2 && honorRankF<=4){
						if(activity.matchSeasonVerify(Activity.SeasonMatchType.S_SW, termId)){
							activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_SW, guildId, honorRankF);
						}
					}
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
	}

	/**
	 * 比赛结束,推送结果及奖励邮件 TODO 日志完善
	 * @param warType
	 */
	private void sendResultMail(SWWarType warType) {
		String serverId = GsConfig.getInstance().getServerId();
		int termId = getTermId();
		String guildId = RedisProxy.getInstance().getSWServerGuild(termId, serverId);
		MailId endMailId = null;
		MailId rewardMailId = null;
		List<ItemInfo> rewardList = null;
		SWRoomData roomData = getLocalRoomData(warType);
		if (roomData == null) {
			return;
		}
		String winnerGuild = roomData.getWinnerId();
		List<String> guildList = roomData.getGuildList();
		// 本服联盟未参与
		if (!guildList.contains(guildId)) {
			return;
		}
		SWGuildData winnerData = RedisProxy.getInstance().getSWGuildData(winnerGuild, termId);
		boolean isWinner = guildId.equals(winnerGuild);

		switch (warType) {
		case FIRST_WAR:
			if(fightData.isFirstWarRewarded()){
				return;
			}
			endMailId = MailId.SW_WAR_ONE_END;
			if (isWinner) {
				rewardMailId = MailId.SW_WAR_ONE_WIN;
				rewardList = StarWarsConstCfg.getInstance().getSwWinReward1List();
			} else {
				rewardMailId = MailId.SW_WAR_ONE_LOSE;
				rewardList = StarWarsConstCfg.getInstance().getSwLostReward1List();
			}
			break;
		case SECOND_WAR:
			if(fightData.isSecondWarRewarded()){
				return;
			}
			endMailId = MailId.SW_WAR_TWO_END;
			if (isWinner) {
				rewardMailId = MailId.SW_WAR_TWO_WIN;
				rewardList = StarWarsConstCfg.getInstance().getSwWinReward2List();
			} else {
				rewardMailId = MailId.SW_WAR_TWO_LOSE;
				rewardList = StarWarsConstCfg.getInstance().getSwLostReward2List();
			}
			break;
		case THIRD_WAR:
			if(fightData.isThirdWarRewarded()){
				return;
			}
			endMailId = MailId.SW_WAR_THREE_END;
			if (isWinner) {
				rewardMailId = MailId.SW_WAR_THREE_WIN;
				rewardList = StarWarsConstCfg.getInstance().getSwWinReward3List();
			} else {
				rewardMailId = MailId.SW_WAR_THREE_LOSE;
				rewardList = StarWarsConstCfg.getInstance().getSwLostReward3List();
			}
			break;
		default:
			return;
		}
		// 战场结束邮件
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
				.setMailId(endMailId)
				.addContents(winnerData.getServerId(), winnerData.getName()));
		Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
		for (String playerId : memberIds) {
			try {
				SWPlayerData playerDate = RedisProxy.getInstance().getSWPlayerData(playerId, termId, warType.getNumber());
				if (playerDate == null) {
					continue;
				}
				// 未进入
				if (playerDate.getEnterTime() <= 0) {
					continue;
				}
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(rewardMailId)
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.addRewards(rewardList)
						.build());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		try {
			if (GuildService.getInstance().isGuildExist(guildId)) {
				Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY.intValue());
				if (opActivity.isPresent()) {
					SeasonActivity activity = opActivity.get();
					if(rewardMailId == MailId.SW_WAR_TWO_LOSE){
						if(activity.matchSeasonVerify(Activity.SeasonMatchType.S_SW, termId)){
							activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_SW, guildId, 16);
						}
					}
					if(rewardMailId == MailId.SW_WAR_THREE_WIN){
						if(activity.matchSeasonVerify(Activity.SeasonMatchType.S_SW, termId)){
							activity.addGuildGradeExpFromMatchRank(Activity.SeasonMatchType.S_SW, guildId, 1);
						}
					}
				}
			}
		}catch (Exception e){
			HawkException.catchException(e);
		}
		switch (warType) {
		case FIRST_WAR:
			fightData.setFirstWarRewarded(true);
			break;
		case SECOND_WAR:
			fightData.setSecondWarRewarded(true);
			break;
		case THIRD_WAR:
			fightData.setThirdWarRewarded(true);
			break;
		}
		RedisProxy.getInstance().updateSWFightData(fightData);
	}

	/**
	 * 霸主产生,发送全服邮件 
	 * @param warType
	 */
	private void sendKingMail(SWWarType warType) {
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
		long currTime = HawkTime.getMillisecond();
		switch (warType) {
		case FIRST_WAR:			
			break;
		case SECOND_WAR:
			break;
		case THIRD_WAR:
			StarWarsOfficerStruct worldKing = StarWarsOfficerService.getInstance().getWorldKing();
			if(worldKing == null){
				return;
			} 
			CrossPlayerStruct worldKingInfo = worldKing.getPlayerInfo();								
			SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
					.setMailId(MailId.SW_WORLD_KING_NOTICE)
					.addContents(new Object[] {GlobalData.getInstance().getMainServerId(worldKingInfo.getServerId()), worldKingInfo.getGuildTag(), worldKingInfo.getName() })
					.build()
					, currTime, currTime + 24 * 3600 * 1000L);
			ChatParames.Builder chatBuilder = ChatParames.newBuilder();
			chatBuilder.setKey(NoticeCfgId.CHANGE_STAR_WARS_KING);
			chatBuilder.setChatType(ChatType.SPECIAL_BROADCAST);
			chatBuilder.addParms(GlobalData.getInstance().getMainServerId(worldKingInfo.getServerId()));
			chatBuilder.addParms(worldKingInfo.getGuildTag());
			chatBuilder.addParms(worldKingInfo.getName());
			ChatService.getInstance().addWorldBroadcastMsg(chatBuilder.build());	
			break;
		default:
			break;
		}
	}

	/**
	 * 获取指定赛区指定分片的第一场比赛房间信息
	 * @param termId
	 * @param zoneId
	 * @return
	 */
	public SWRoomData getFirstWarRoom(int termId, int zoneId, int teamId) {
		List<SWRoomData> roomList = RedisProxy.getInstance().getAllSWRoomData(termId, SWWarType.FIRST_WAR.getNumber());
		for(SWRoomData roomData : roomList){
			if(roomData.getZoneId() == zoneId && roomData.getTeamId() == teamId){
				return roomData;
			}
		}
		return null;
	}
	
	/**
	 * 获取指定赛区的第二场比赛房间信息
	 * @param termId
	 * @param zoneId
	 * @return
	 */
	public SWRoomData getSecondWarRoom(int termId, int zoneId) {
		List<SWRoomData> roomList = RedisProxy.getInstance().getAllSWRoomData(termId, SWWarType.SECOND_WAR.getNumber());
		for(SWRoomData roomData : roomList){
			if(roomData.getZoneId() == zoneId){
				return roomData;
			}
		}
		return null;
	}
	
	/**
	 * 获取第三场比赛房间信息
	 * @param termId
	 * @param zoneId
	 * @return
	 */
	public SWRoomData getThirdWarRoom(int termId) {
		List<SWRoomData> roomList = RedisProxy.getInstance().getAllSWRoomData(termId, SWWarType.THIRD_WAR.getNumber());
		if(roomList.isEmpty()){
			return null;
		}
		return roomList.get(0);
	}

	/**
	 * 刷新本服参与联盟的联盟信息
	 */
	protected void updateServerGuildInfo() {
		int termId = activityInfo.getTermId();
		if (termId == 0) {
			return;
		}
		int stateNum = activityInfo.getState().getNumber();
		HawkLog.logPrintln("StarWarsActivityService-updateServerGuildInfo,term:{},state:{},calserver:{}",termId,stateNum,this.getCalServer());
		// 仅在匹配完成后到本期比赛完全结束前,定期刷新出战联盟信息
		if (stateNum <= SWActivityState.MATCH.getNumber() || stateNum >= SWActivityState.END.getNumber()) {
			return;
		}
		try {

			StarWarsPartCfg localCfg = getLocalServerPartCfg();
			// 本服未分配到赛区
			if (localCfg == null) {
				return;
			}
			String serverId = GsConfig.getInstance().getServerId();
			String guildId = RedisProxy.getInstance().getSWServerGuild(termId, serverId);
			if(HawkOSOperator.isEmptyString(guildId)){
				return;
			}
			updateSWGuildData(serverId, localCfg, guildId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}


	/**
	 * 推送入围邮件
	 */
	private void sendPickedMail() {
		int termId = activityInfo.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		String guildId = RedisProxy.getInstance().getSWServerGuild(termId, serverId);
		if(HawkOSOperator.isEmptyString(guildId)){
			return;
		}
		GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
		if(guildObj == null){
			return;
		}
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
				.setMailId(MailId.SW_MATCH_JOINER_NOTICE)
				.addContents(guildObj.getName(), guildObj.getTag()));
	}

	/**
	 *  创建第一场战斗房间信息
	 */
	private void pickJoiner() {
		int termId = activityInfo.getTermId();
		DungeonRedisLog.log("StarWarsActivityService", "start pickJoinGuild! termId: {}", termId);
		RedisProxy.getInstance().removeSWJoinGuild(termId);
		String areaId = GsConfig.getInstance().getAreaId();

		ConfigIterator<StarWarsPartCfg> partIts = HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class);
		List<SWRoomData> roomList = new ArrayList<>();
		
		Map<String, SWGuildData> guildDatas = RedisProxy.getInstance().getAllSWGuildData(termId);
		if(guildDatas.isEmpty()){
			return;
		}
		Set<String> roomServerSet = new HashSet<>();
		// 拉取各赛区排行,存入联盟信息
		for (StarWarsPartCfg partCfg : partIts) {
			if (!partCfg.getAreaId().equals(areaId)) {
				continue;
			}
			int zoneId = partCfg.getZone();
			int teamId = partCfg.getTeam();
			if (zoneId == 0) {
				continue;
			}
			if(teamId == 0){
				continue;
			}
			List<String> guildIds = guildDatas.values().stream().filter(t -> t.getZoneId() == partCfg.getZone() && t.getTeamId() == partCfg.getTeam()).map(t -> t.getId())
					.collect(Collectors.toList());
			if(guildIds.isEmpty()){
				DungeonRedisLog.log("StarWarsActivityService", "pickJoiner guildIds empty, zoneId: {},temId:{}", zoneId,teamId);
				continue;
			}
			String roomServer = getRoomServer(guildDatas, roomServerSet, guildIds);
			SWRoomData roomData = new SWRoomData();
			String roomId = String.valueOf(zoneId * 1000 + teamId);
			List<String> guildList = new ArrayList<>();
			roomData.setId(roomId);
			roomData.setRoomServerId(roomServer);
			roomData.setRoomState(SWRoomState.NOT_INIT);
			List<SWGuildJoinInfo> joinList = new ArrayList<>();
			for (String guildId : guildIds) {
				SWGuildData guildData = guildDatas.get(guildId);
				SWGuildJoinInfo info = new SWGuildJoinInfo();
				info.setGroup(SWGroupType.PARTNER);
				info.setZone(zoneId);
				info.setTeam(teamId);
				info.setServerId(guildData.getServerId());
				info.setId(guildId);
				joinList.add(info);
				guildList.add(guildId);
				LogUtil.logSWMatchInfo(roomId, roomServer, termId, SWWarType.FIRST_WAR, guildId, guildData.getServerId());
				DungeonRedisLog.log("StarWarsActivityService", "battle march guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
						guildId, guildData.getName(), SWWarType.FIRST_WAR, zoneId, teamId, roomId,roomServer);
			}
			RedisProxy.getInstance().updateSWJoinGuild(joinList, termId);
			roomData.setGuildList(guildList);
			roomData.setZoneId(zoneId);
			roomData.setTeamId(teamId);
			roomList.add(roomData);
			roomServerSet.add(roomServer);
		}
		RedisProxy.getInstance().updateSWRoomData(roomList, termId, SWWarType.FIRST_WAR.getNumber());

	}
	
	
	public String getRoomServer(Map<String, SWGuildData> guildDatas,Set<String> roomServerSet,List<String> joinGuilds){
		List<String> guildIds =new ArrayList<>();
		guildIds.addAll(joinGuilds);
		List<String> serverId = new ArrayList<>();
		for(String guildId : guildIds){
			SWGuildData guild = guildDatas.get(guildId);
			if(roomServerSet.contains(guild.getServerId())){
				continue;
			}
			serverId.add(guild.getServerId());
		}
		Collections.sort(serverId);
		return serverId.get(0);
	}
	
	/**
	 * 系统是否关闭
	 * @return
	 */
	public boolean isClose() {
		boolean isSystemClose = StarWarsConstCfg.getInstance().isSystemClose();
		return isSystemClose && activityInfo.getState() == SWActivityState.CLOSE;
	}


	/**
	 * 当前阶段状态计算,仅供状态检测调用
	 * @return
	 */
	private SWActivityData calcInfo() {
		SWActivityData info = new SWActivityData();
		if (StarWarsConstCfg.getInstance().isSystemClose()) {
			info.setState(SWActivityState.CLOSE);
			return info;
		}
		StarWarsTimeCfg cfg = null;
		long now = HawkTime.getMillisecond();

		ConfigIterator<StarWarsTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(StarWarsTimeCfg.class);
		for (StarWarsTimeCfg timeCfg : its) {
			long starTime = timeCfg.getSignStartTimeValue();
			long endTime = timeCfg.getEndTimeValue();
			if (now > starTime && now < endTime) {
				cfg = timeCfg;
				break;
			}
		}
		StarWarsPartCfg partCfg = getLocalServerPartCfg();
		int termId = 0;
		SWActivityState state = SWActivityState.NOT_OPEN;
		// 没有可供开启的配置
		if (partCfg != null && cfg != null) {
			termId = cfg.getTermId();
			long signStartTime = cfg.getSignStartTimeValue();
			long signEndTime = cfg.getSignEndTimeValue();
			long matchEndTime = cfg.getMatchEndTimeValue();
			long warStartTimeOne = cfg.getWarStartTimeOneValue();
			long warEndTimeOne = cfg.getWarEndTimeOneValue();
			long warStartTimeTwo = cfg.getWarStartTimeTwoValue();
			long warEndTimeTwo = cfg.getWarEndTimeTwoValue();
			long warStartTimeThree = cfg.getWarStartTimeThreeValue();
			long warEndTimeThree = cfg.getWarEndTimeThreeValue();
			long endTime = cfg.getEndTimeValue();
			if (now >= signStartTime && now < signEndTime) {
				state = SWActivityState.SIGN_UP;
			}
			if (now >= signEndTime && now < matchEndTime) {
				state = SWActivityState.MATCH;
			}
			if (now >= matchEndTime && now < warStartTimeOne) {
				state = SWActivityState.WAR_ONE_WAIT;
			}
			if (now >= warStartTimeOne && now < warEndTimeOne) {
				state = SWActivityState.WAR_ONE_OPEN;
			}
			if (now >= warEndTimeOne && now < warStartTimeTwo) {
				state = SWActivityState.WAR_TWO_WAIT;
			}
			if (now >= warStartTimeTwo && now < warEndTimeTwo) {
				state = SWActivityState.WAR_TWO_OPEN;
			}
			if (now >= warEndTimeTwo && now < warStartTimeThree) {
				state = SWActivityState.WAR_THREE_WAIT;
			}
			if (now >= warStartTimeThree && now < warEndTimeThree) {
				state = SWActivityState.WAR_THREE_OPEN;
			}
			if (now >= warEndTimeThree && now < endTime) {
				state = SWActivityState.END;
			}
		}
		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	private void checkStateChange() {
		SWActivityData newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();
	
		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateSWActivityInfo(activityInfo);
		}
		SWActivityState old_state = activityInfo.getState();
		SWActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从未开启阶段开始轮询
		if (new_term != old_term) {
			old_state = SWActivityState.NOT_OPEN;
			activityInfo.setTermId(new_term);
			joinGuilds = new ConcurrentHashMap<>();
			needUpdate = true;
		}
	
		for (int i = 0; i < 30; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			if (old_state == SWActivityState.NOT_OPEN) {
				old_state = SWActivityState.SIGN_UP;
				activityInfo.setState(old_state);
				onSignOpen();
			} else if (old_state == SWActivityState.SIGN_UP) {
				old_state = SWActivityState.MATCH;
				activityInfo.setState(old_state);
			} else if (old_state == SWActivityState.MATCH) {
				old_state = SWActivityState.WAR_ONE_WAIT;
				activityInfo.setState(old_state);
				onMatchFinish();
			} else if (old_state == SWActivityState.WAR_ONE_WAIT) {
				old_state = SWActivityState.WAR_ONE_OPEN;
				activityInfo.setState(old_state);
				onFirstWarOpen();
			} else if (old_state == SWActivityState.WAR_ONE_OPEN) {
				old_state = SWActivityState.WAR_TWO_WAIT;
				activityInfo.setState(old_state);
				onSecondWarWait();
			} else if (old_state == SWActivityState.WAR_TWO_WAIT) {
				old_state = SWActivityState.WAR_TWO_OPEN;
				activityInfo.setState(old_state);
				onSecondWarOpen();
			} else if (old_state == SWActivityState.WAR_TWO_OPEN) {
				old_state = SWActivityState.WAR_THREE_WAIT;
				activityInfo.setState(old_state);
				onThirdWarWait();
			} else if (old_state == SWActivityState.WAR_THREE_WAIT) {
				old_state = SWActivityState.WAR_THREE_OPEN;
				activityInfo.setState(old_state);
				onThirdWarOpen();
			} else if (old_state == SWActivityState.WAR_THREE_OPEN) {
				old_state = SWActivityState.END;
				activityInfo.setState(old_state);
			}  
		}
	
		if (needUpdate) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateSWActivityInfo(activityInfo);
			HawkLog.logPrintln("StarWarsActivityService state change, oldTerm: {}, oldState: {} ,newTerm: {}, newState: {}", old_term, old_state, activityInfo.getTermId(),
					activityInfo.getState());
		}
	
	}

	/**
	 * 批量创建
	 * @param timeIndex
	 */
	private void createBattle() {
		int termId = activityInfo.getTermId();
		StarWarsTimeCfg timeCfg = activityInfo.getTimeCfg();
		if (timeCfg == null) {
			HawkLog.logPrintln("StarWarsActivityService creatBattle error ,timeCfg not exist, termId:{}, state:{}", termId, activityInfo.getState());
			return;
		}
		long startTime = 0;
		SWWarType warType = null;
		if (activityInfo.getState() == SWActivityState.WAR_ONE_OPEN) {
			startTime = timeCfg.getWarStartTimeOneValue();
			warType = SWWarType.FIRST_WAR;
		} else if (activityInfo.getState() == SWActivityState.WAR_TWO_OPEN) {
			startTime = timeCfg.getWarStartTimeTwoValue();
			warType = SWWarType.SECOND_WAR;
		} else if (activityInfo.getState() == SWActivityState.WAR_THREE_OPEN) {
			startTime = timeCfg.getWarStartTimeThreeValue();
			warType = SWWarType.THIRD_WAR;
		} else {
			return;
		}
		long overTime = startTime + StarWarsConstCfg.getInstance().getWarOpenTime();
		String serverId = GsConfig.getInstance().getServerId();
		List<SWRoomData> dataList = RedisProxy.getInstance().getAllSWRoomData(termId, warType.getNumber());
		List<SWRoomData> roomList = dataList.stream().filter(t -> t.getRoomServerId().equals(serverId)).collect(Collectors.toList());
		for (SWRoomData roomData : roomList) {
			SWExtraParam extParm = new SWExtraParam();
			extParm.setWarType(warType);
			boolean result = SWRoomManager.getInstance().creatNewBattle(startTime, overTime, roomData.getId(), extParm);
			if (result) {
				roomData.setRoomState(SWRoomState.INITED);
			} else {
				roomData.setRoomState(SWRoomState.INITED_FAILED);
			}
		}
		RedisProxy.getInstance().updateSWRoomData(roomList, termId, warType.getNumber());
	}

	/**
	 * 活动开启
	 */
	private void onSignOpen() {
		joinGuilds = new ConcurrentHashMap<>();
		StarWarsTimeCfg cfg = getActivityData().getTimeCfg();
		// 活动开启邮件
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.SW_ACTIVITY_OPEN)
				.build()
				, HawkTime.getMillisecond(), cfg.getSignEndTimeValue());
		try {
			if (isCalServer()) {
				StarWarsOfficerService.getInstance().onClearEvent();
			}
			StarWarsOfficerService.getInstance().onClearMemInfo();
			StarWarsOfficerService.getInstance().reset();				
			RedisProxy.getInstance().deleteStarWarsLoginingPlayer(SWWarType.FIRST_WAR.toString());
			RedisProxy.getInstance().deleteStarWarsLoginingPlayer(SWWarType.SECOND_WAR.toString());
			RedisProxy.getInstance().deleteStarWarsLoginingPlayer(SWWarType.THIRD_WAR.toString());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
	}

	/**
	 * 匹配结束,进入战斗准备阶段
	 */
	private void onMatchFinish() {
		loadJoinGuilds();
		sendPickedMail();
	}
	
	/**
	 * 初赛开启
	 */
	private void onFirstWarOpen() {
		createBattle();
		pushWarOpenBanner(SWWarType.FIRST_WAR);
	}

	/**
	 * 第一场比赛完成,第二场比赛进入等待阶段
	 */
	private void onSecondWarWait() {
		loadJoinGuilds();
		// 再加载一次官职.
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
	}
	
	/**
	 * 第二场比赛开启
	 */
	private void onSecondWarOpen() {
		createBattle();
		pushWarOpenBanner(SWWarType.SECOND_WAR);
	}
	
	/**
	 * 第二场完成,第三场比赛进入等待阶段
	 */
	private void onThirdWarWait() {
		loadJoinGuilds();
		// 再加载一次官职.
		StarWarsOfficerService.getInstance().loadOrReloadOfficer();
	}

	/**
	 * 决赛开启
	 */
	private void onThirdWarOpen() {
		createBattle();
		pushWarOpenBanner(SWWarType.THIRD_WAR);
	}
	
	/**
	 * 推送开战
	 * @param warType
	 */
	private void pushWarOpenBanner(SWWarType warType) {
		try {
			Set<Player> onlineMembers = new HashSet<>();
			SWGroupType limitType = null;
			NoticeCfgId noticeId = null;
			switch (warType) {
			case FIRST_WAR:
				limitType = SWGroupType.PARTNER;
				noticeId = NoticeCfgId.SW_185;
				break;
			case SECOND_WAR:
				limitType = SWGroupType.FIRST_WINNER;
				noticeId = NoticeCfgId.SW_180;
				break;
			case THIRD_WAR:
				limitType = SWGroupType.SECOND_WINNER;
				noticeId = NoticeCfgId.SW_181;
				break;
			}
			for (Entry<String, SWGroupType> entry : joinGuilds.entrySet()) {
				SWGroupType type = entry.getValue();
				if (type != limitType) {
					continue;
				}
				String guildId = entry.getKey();
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				if (guildObj == null) {
					continue;
				}
				Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
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
			ChatMsg msg = ChatParames.newBuilder().setChatType(Const.ChatType.SPECIAL_BROADCAST).setKey(noticeId).build().toPBMsg();
			msgList.add(msg);
			ChatService.getInstance().sendChatMsg(msgList, onlineMembers);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 加载本赛季参与的联盟信息
	 */
	private void loadJoinGuilds() {
		int termId = activityInfo.getTermId();
		
		Map<String, SWGuildJoinInfo> joinInfos = RedisProxy.getInstance().getAllSWJoinGuild(termId);
		Map<String, SWGroupType> joinMap = new ConcurrentHashMap<>();
		for (Entry<String, SWGuildJoinInfo> entry : joinInfos.entrySet()) {
			SWGuildJoinInfo joinInfo = entry.getValue();
			joinMap.put(joinInfo.getId(), joinInfo.getGroup());
		}
		joinGuilds = joinMap;
	}
	
	private void flushJoinPlayerInfo(SWWarType warType) {
		StarWarsPartCfg localCfg = getLocalServerPartCfg();
		int zoneId = localCfg.getZone();
		int teamId = localCfg.getTeam();
		int termId = activityInfo.getTermId();
		String serverId = GsConfig.getInstance().getServerId();
		SWRoomData roomData = null;
		switch (warType) {
		case FIRST_WAR:
			roomData = getFirstWarRoom(termId, zoneId, teamId);
			break;
		case SECOND_WAR:
			roomData = getSecondWarRoom(termId, zoneId);
			break;
		case THIRD_WAR:
			roomData = getThirdWarRoom(termId);
			break;

		default:
			break;
		}
		if (roomData == null || !HawkOSOperator.isEmptyString(roomData.getWinnerId())) {
			return;
		}
		List<String> guildList = roomData.getGuildList();
		if (guildList == null || guildList.isEmpty()) {
			return;
		}
		for (String guildId : guildList) {
			GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
			if (guildObj == null) {
				continue;
			}
			String leaderId = guildObj.getLeaderId();
			Collection<String> members = GuildService.getInstance().getGuildMembers(guildId);
			for (String playerId : members) {
				GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(playerId);
				// 非本盟玩家
				if (member == null || !guildId.equals(member.getGuildId())) {
					continue;
				}
				SWPlayerData playerData = new SWPlayerData();
				playerData.setId(playerId);
				playerData.setGuildAuth(member.getAuthority());
				playerData.setGuildId(guildId);
				playerData.setGuildOfficer(member.getOfficeId());
				playerData.setServerId(serverId);
				RedisProxy.getInstance().updateSWPlayerData(playerData, termId, warType.getNumber());
				// 刷入盟主的信息,结算司令时使用
				if (leaderId.equals(playerId)) {
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					CrossPlayerStruct.Builder crossBuilder = BuilderUtil.buildCrossPlayer(player);
					RedisProxy.getInstance().updateCrossPlayerStruct(playerId, crossBuilder.build(), 3600 * 24 * 2);
				}
			}
		}
	}
	
	/*********************************活动数据信息*****************************************************************/
	
	/**
	 * 构建活动时间信息
	 * @return
	 */
	public SWStateInfo.Builder genStateInfo(String guildId) {
		SWStateInfo.Builder builder = SWStateInfo.newBuilder();
		int termId = activityInfo.getTermId();
		SWActivityState state = activityInfo.getState();
		if (state == SWActivityState.CLOSE) {
			builder.setState(SWState.SW_CLOSE);
		} else {
			builder.setState(SWState.valueOf(activityInfo.getState().getNumber()));
		}
		
		// 活动未开启,获取下一次开启的时间
		if(state == SWActivityState.NOT_OPEN){
			long nextStartTime = getNextStartTime();
			builder.setSignStartTime(nextStartTime);
		}
		if (state == SWActivityState.WAR_ONE_OPEN) {
			if (fightData.getState() == SWFightState.FIRST_CALC) {
				builder.setState(SWState.valueOf(SWActivityState.WAR_ONE_CALC.getNumber()));
			}
			else if(fightData.getState() == SWFightState.SECOND_MANGE){
				builder.setState(SWState.valueOf(SWActivityState.WAR_TWO_WAIT.getNumber()));
			}
		} else if (state == SWActivityState.WAR_TWO_OPEN) {
			if (fightData.getState() == SWFightState.SECOND_CALC) {
				builder.setState(SWState.valueOf(SWActivityState.WAR_TWO_CALC.getNumber()));
			}
			else if (fightData.getState() == SWFightState.THIRD_MANGE) {
				builder.setState(SWState.valueOf(SWActivityState.WAR_THREE_WAIT.getNumber()));
			}
		} else if (state == SWActivityState.WAR_THREE_OPEN) {
			if (fightData.getState() == SWFightState.THIRD_CALC) {
				builder.setState(SWState.valueOf(SWActivityState.WAR_THREE_CALC.getNumber()));
			}
			else if (fightData.getState() == SWFightState.FINISH) {
				builder.setState(SWState.valueOf(SWActivityState.END.getNumber()));
			}
		}
		
		builder.setTermId(termId);
		StarWarsTimeCfg timeCfg = activityInfo.getTimeCfg();
		if (timeCfg != null) {
			builder.setSignStartTime(timeCfg.getSignStartTimeValue());
			builder.setSignEndTime(timeCfg.getSignEndTimeValue());
			builder.setMatchEndTime(timeCfg.getMatchEndTimeValue());
			builder.setWarOneMangeEndTime(timeCfg.getMangeEndTimeOneValue());
			builder.setWarOneStartTime(timeCfg.getWarStartTimeOneValue());
			builder.setWarOneEndTime(timeCfg.getWarEndTimeOneValue());
			builder.setWarTwoMangeEndTime(timeCfg.getMangeEndTimeTwoValue());
			builder.setWarTwoStartTime(timeCfg.getWarStartTimeTwoValue());
			builder.setWarTwoEndTime(timeCfg.getWarEndTimeTwoValue());
			builder.setWarThreeMangeEndTime(timeCfg.getMangeEndTimeThreeValue());
			builder.setWarThreeStartTime(timeCfg.getWarStartTimeThreeValue());
			builder.setWarThreeEndTime(timeCfg.getWarEndTimeThreeValue());
			builder.setEndTime(timeCfg.getEndTimeValue());
		}
		return builder;
	}
	
	/**
	 * 获取下一期活动开启时间
	 * @return
	 */
	private long getNextStartTime() {
		long now = HawkTime.getMillisecond();
		ConfigIterator<StarWarsTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(StarWarsTimeCfg.class);
		for(StarWarsTimeCfg cfg : its){
			if(now < cfg.getSignStartTimeValue()){
				return cfg.getSignStartTimeValue();
			}
		}
		return Long.MAX_VALUE;
	}

	/**
	 * 构建活动界面信息
	 * @param player
	 * @return
	 */
	public SWPageInfo.Builder genPageInfo(Player player) {
		String guildId = player.getGuildId();
		SWPageInfo.Builder builder = SWPageInfo.newBuilder();
		SWStateInfo.Builder stateInfo = genStateInfo(guildId);
		// 是否出战
		boolean isSign = isJoinCurrWar(guildId);
		builder.setIsJoin(isSign);
		builder.setStateInfo(stateInfo);
		String serverId = player.getMainServerId();
		int zoneId = AssembleDataManager.getInstance().getServerPart(serverId);
		if (zoneId != -1) {
			builder.setZoneId(zoneId);
		}
		return builder;
	}
	
	/**
	 * 联盟是否参与当前战斗
	 * @param guildId
	 * @return
	 */
	public boolean isJoinCurrWar(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		SWGroupType groupType = joinGuilds.get(guildId);
		if (groupType == null) {
			return false;
		}
		if (activityInfo.getState().getNumber() >= SWActivityState.WAR_ONE_WAIT.getNumber() 
				&& activityInfo.getState().getNumber() <= SWActivityState.WAR_ONE_CALC.getNumber() && groupType == SWGroupType.PARTNER) {
			return true;
		} else if (activityInfo.getState().getNumber() >= SWActivityState.WAR_TWO_WAIT.getNumber()
				&& activityInfo.getState().getNumber() <= SWActivityState.WAR_TWO_CALC.getNumber() && groupType == SWGroupType.FIRST_WINNER) {
			return true;
		} else if (activityInfo.getState().getNumber() >= SWActivityState.WAR_THREE_WAIT.getNumber()
				&& activityInfo.getState().getNumber() <= SWActivityState.WAR_THREE_CALC.getNumber() && groupType == SWGroupType.SECOND_WINNER) {
			return true;
		}
		return false;
	}

	/**
	 * 同步跨服活动状态
	 * @param player
	 */
	public void syncStateInfo(Player player){
		SWPageInfo.Builder pageInfo = genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WAR_INFO_SYNC, pageInfo));
	}

	/**
	 * 同步跨服活动页面状态
	 * @param player
	 */
	public void syncPageInfo(Player player){
		SWPageInfo.Builder pageInfo = genPageInfo(player);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.STAR_WAR_PAGE_INFO_SYNC, pageInfo));
	}

	/**
	 * 进入联盟军演房间
	 * @param player
	 * @return
	 */
	public boolean enterRoom(Player player) {
		int termId = activityInfo.getTermId();
		SWActivityState state = activityInfo.getState();
		SWRoomData roomData = null;
		String serverId = player.getMainServerId();
		StarWarsPartCfg partCfg = AssembleDataManager.getInstance().getServerPartCfg(serverId);
		int zoneId = partCfg.getZone();
		int teamId = partCfg.getTeam();
		SWWarType warType = null;
		if (state == SWActivityState.WAR_ONE_OPEN) {
			roomData = getFirstWarRoom(termId, zoneId, teamId);
			warType = SWWarType.FIRST_WAR;
		} else if (state == SWActivityState.WAR_TWO_OPEN) {
			roomData = getSecondWarRoom(termId, zoneId);
			warType = SWWarType.SECOND_WAR;
		} else if (state == SWActivityState.WAR_THREE_OPEN) {
			roomData = getThirdWarRoom(termId);
			warType = SWWarType.THIRD_WAR;
		}

		if (roomData == null) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, room not esixt, playerId: {}, termId: {}", player.getId(), activityInfo.getTermId());
			return false;
		}
		String roomId = roomData.getId();

		if (!SWRoomManager.getInstance().hasGame(roomId)) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, game not esixt, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId());
			return false;
		}
		SWPlayerData swPlayerData = RedisProxy.getInstance().getSWPlayerData(player.getId(), termId, warType.getNumber());
		if (swPlayerData == null) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, twPlayerData is null, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), player.getGuildId());
			return false;
		}
		
		String guildId = player.getGuildId();
		if (!swPlayerData.getGuildId().equals(guildId)) {
			HawkLog.logPrintln(
					"StarWarsActivityService enter room error, guildIds not match, playerId: {}, termId: {}, roomId: {}, roomServer: {}, twDataGuildId: {}, playerGuildId: {}",
					player.getId(), activityInfo.getTermId(), roomId, roomData.getRoomServerId(), guildId, player.getGuildId());
			return false;
		}
		int innerCnt = SWRoomManager.getInstance().guildMemberInBattle(roomId, guildId);
		int limitCnt = StarWarsConstCfg.getInstance().getMemberCntLimit();
		// 战场内同时进入成员数已满
		if (innerCnt >= limitCnt) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, memberCnt over limit, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}, innerCnt: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), swPlayerData.getGuildId(), innerCnt);
			return false;
		}

		if (swPlayerData.getQuitTime() > 0) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, has entered, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), swPlayerData.getGuildId());
			return false;
		}
		if (!SWRoomManager.getInstance().joinGame(roomId, player)) {
			HawkLog.logPrintln("StarWarsActivityService enter room error, joinGame failed, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), swPlayerData.getGuildId());
			return false;
		}
		swPlayerData.setEnterTime(HawkTime.getMillisecond());
		RedisProxy.getInstance().updateSWPlayerData(swPlayerData, termId, warType.getNumber());
		LogUtil.logSWEnterInfo(player.getId(), guildId, serverId, termId, roomId, roomData.getRoomServerId(), warType);
		HawkLog.logPrintln("StarWarsActivityService enter room, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(), termId, roomId, roomId, swPlayerData.getGuildId());
		return true;
	}
	
	/**
	 * 退出房间
	 * @param player
	 * @param isMidwayQuit 是否中途退出
	 * @return
	 */
	public boolean quitRoom(Player player, boolean isMidwayQuit) {
		int termId = activityInfo.getTermId();
		SWWarType warType = getCurrWarType();
		if (warType == null) {
			HawkLog.logPrintln("StarWarsActivityService quitRoom error, warType is null, playerId: {}, isMidwayQuit: {}", player.getId(), isMidwayQuit);
			return false;
		}
		
		SWPlayerData swPlayerData = RedisProxy.getInstance().getSWPlayerData(player.getId(), termId, warType.getNumber());
		if (swPlayerData == null) {
			HawkLog.logPrintln("StarWarsActivityService quitRoom error, twPlayerData is null, playerId: {}, isMidwayQuit: {}", player.getId(), isMidwayQuit);
			return false;
		}
		
		if (swPlayerData.getEnterTime() == 0) {
			HawkLog.logPrintln("StarWarsActivityService quitRoom error, has not entered, playerId: {}, guildId: {}, isMidwayQuit: {}", swPlayerData.getId(),
					swPlayerData.getGuildId(), isMidwayQuit);
			return false;
		}
		if (isMidwayQuit) {
			swPlayerData.setMidwayQuit(isMidwayQuit);
			swPlayerData.setQuitTime(HawkTime.getMillisecond());
			RedisProxy.getInstance().updateSWPlayerData(swPlayerData, termId, warType.getNumber());
		}
		LogUtil.logSWQuitInfo(player.getId(), swPlayerData.getGuildId(), swPlayerData.getServerId(), termId, warType, isMidwayQuit);
		HawkLog.logPrintln("StarWarsActivityService quitRoom, playerId: {}, guildId: {}, termId:{}, isMidwayQuit: {}", swPlayerData.getId(), swPlayerData.getGuildId(), termId,
				isMidwayQuit);
		return true;
	}

	public SWWarType getCurrWarType() {
		SWWarType warType = null;
		SWActivityState state = activityInfo.getState();
		if (state == SWActivityState.WAR_ONE_OPEN) {
			warType = SWWarType.FIRST_WAR;
		} else if (state == SWActivityState.WAR_TWO_OPEN) {
			warType = SWWarType.SECOND_WAR;
		} else if (state == SWActivityState.WAR_THREE_OPEN) {
			warType = SWWarType.THIRD_WAR;
		}
		return warType;
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
	public SWActivityData getActivityData(){
		return activityInfo;
	}
	
	
	
	/**
	 * 联盟解散
	 * @param guildId
	 */
	public void onGuildDismiss(String guildId) {
		try {
			int termId = activityInfo.getTermId();
			// 移除联赛相关信息
			RedisProxy.getInstance().removeSWGuildData(guildId, termId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}
	
	/**
	 * 成员退出联盟
	 * @param player
	 * @param guildId
	 */
	public void onQuitGuild(Player player, String guildId){
	}
	
	/**
	 * 战场结束
	 * @param msg
	 */
	@MessageHandler
	private void onBattleFinish(SWBilingInformationMsg msg) {
		String bstr = msg.serializ();
		SWBilingInformationMsg msssg = new SWBilingInformationMsg();
		msssg.mergeFrom(bstr);
		System.out.println("@@@@@@@@@@@");
		for(PBSWPlayerInfo pinfo :  msssg.getLastSyncpb().getPlayerInfoList()){
			System.out.println(pinfo.getName() +"   "+ pinfo.getGuildId());
		}
		HawkLog.errPrintln(bstr.length()+"  这么长");
		HawkLog.errPrintln(bstr);
		
		String winGuild = msg.getWinGuild();
		String roomId = msg.getRoomId();
		SWWarType warType = msg.getWarType();
		if (warType == SWWarType.FIRST_WAR) {
			dealWithFirstWarResult(winGuild, roomId, warType);
		} else if (warType == SWWarType.SECOND_WAR) {
			dealWithSecondWarResult(winGuild, roomId, warType);
		} else if (warType == SWWarType.THIRD_WAR) {
			dealWithThirdWarResult(winGuild, roomId, warType);
		}
		
		saveBilingInfoMsg(msg);
	}
	
	private void saveBilingInfoMsg(SWBilingInformationMsg msg) {
		String roomId = msg.getRoomId();
		SWWarType warType = msg.getWarType();
		int termId = activityInfo.getTermId();
		SWRoomData roomData = RedisProxy.getInstance().getSWRoomData(roomId, termId, warType.getNumber());
		if(roomData == null){
			throw new RuntimeException("roomData not exist");
		}
		roomData.setBilingInforStr(msg.serializ());
		
		RedisProxy.getInstance().updateSWRoomData(roomData, termId, warType.getNumber());
		DungeonRedisLog.log("StarWarsActivityService", JSON.toJSONString(roomData));
	}

	/**
	 * 处理第一场比赛比赛结果
	 * @param winGuild
	 * @param roomId
	 * @param warType
	 */
	private void dealWithFirstWarResult(String winGuild, String roomId, SWWarType warType) {
		int termId = activityInfo.getTermId();
		SWRoomData roomData = RedisProxy.getInstance().getSWRoomData(roomId, termId, warType.getNumber());
		if(roomData == null){
			throw new RuntimeException("roomData not exist");
		}
		int zoneId = roomData.getZoneId();
		int teamId = roomData.getTeamId();
		if (HawkOSOperator.isEmptyString(winGuild)) {
			winGuild = calcEmptyWinner(roomData);
		}
		roomData.setWinnerId(winGuild);
		roomData.setRoomState(SWRoomState.CLOSE);
		RedisProxy.getInstance().updateSWRoomData(roomData, termId, warType.getNumber());
		if (!HawkOSOperator.isEmptyString(winGuild)) {
			SWGuildData guildData = RedisProxy.getInstance().getSWGuildData(winGuild, termId);
			String leaderId = guildData.getLeaderId();
			StarWarsOfficerService.getInstance().onFighterOver(leaderId, termId, zoneId, teamId);
			SWGuildJoinInfo joinInfo = RedisProxy.getInstance().getSWJoinGuild(termId, winGuild);
			joinInfo.setGroup(SWGroupType.FIRST_WINNER);
			RedisProxy.getInstance().updateSWJoinGuild(joinInfo, termId);
		}
		DungeonRedisLog.log("StarWarsActivityService", "win guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
				winGuild, GuildService.getInstance().getGuildName(winGuild), warType, zoneId, teamId, roomId, roomData.getRoomServerId());
	}
	
	/**
	 * 处理第二场比赛比赛结果 //TODO jm
	 * @param winGuild
	 * @param roomId
	 * @param warType
	 */
	private void dealWithSecondWarResult(String winGuild, String roomId, SWWarType warType) {
		int termId = activityInfo.getTermId();
		SWRoomData roomData = RedisProxy.getInstance().getSWRoomData(roomId, termId, warType.getNumber());
		if (roomData == null) {
			throw new RuntimeException("roomData not exist");
		}
		int zoneId = roomData.getZoneId();
		int teamId = roomData.getTeamId();
		if (HawkOSOperator.isEmptyString(winGuild)) {
			winGuild = calcEmptyWinner(roomData);
		}
		roomData.setWinnerId(winGuild);
		roomData.setRoomState(SWRoomState.CLOSE);
		RedisProxy.getInstance().updateSWRoomData(roomData, termId, warType.getNumber());
		if (!HawkOSOperator.isEmptyString(winGuild)) {
			SWGuildData guildData = RedisProxy.getInstance().getSWGuildData(winGuild, termId);
			String leaderId = guildData.getLeaderId();
			StarWarsOfficerService.getInstance().onFighterOver(leaderId, termId, zoneId, teamId);
			SWGuildJoinInfo joinInfo = RedisProxy.getInstance().getSWJoinGuild(termId, winGuild);
			joinInfo.setGroup(SWGroupType.SECOND_WINNER);
			RedisProxy.getInstance().updateSWJoinGuild(joinInfo, termId);
		}
		DungeonRedisLog.log("StarWarsActivityService", "win guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
				winGuild, GuildService.getInstance().getGuildName(winGuild), warType, zoneId, teamId, roomId, roomData.getRoomServerId());
	}
	
	/**
	 * 处理第三场比赛战斗结果 TODO jm
	 * @param winGuild
	 * @param roomId
	 * @param warType
	 */
	private void dealWithThirdWarResult(String winGuild, String roomId, SWWarType warType) {
		int termId = activityInfo.getTermId();
		SWRoomData roomData = RedisProxy.getInstance().getSWRoomData(roomId, termId, warType.getNumber());
		if (roomData == null) {
			throw new RuntimeException("roomData not exist");
		}
		// 战斗未产生胜利者,则从出战联盟中筛选一个作为胜利方
		if (HawkOSOperator.isEmptyString(winGuild)) {
			winGuild = calcEmptyWinner(roomData);
		}
		roomData.setWinnerId(winGuild);
		roomData.setRoomState(SWRoomState.CLOSE);
		RedisProxy.getInstance().updateSWRoomData(roomData, termId, warType.getNumber());
		if (!HawkOSOperator.isEmptyString(winGuild)) {
			SWGuildData guildData = RedisProxy.getInstance().getSWGuildData(winGuild, termId);
			String leaderId = guildData.getLeaderId();
			StarWarsOfficerService.getInstance().onFighterOver(leaderId, termId, StarWarsConst.WORLD_PART, GsConst.StarWarsConst.TEAM_NONE);
			SWGuildJoinInfo joinInfo = RedisProxy.getInstance().getSWJoinGuild(termId, winGuild);
			joinInfo.setGroup(SWGroupType.THIRD_WINNER);
			RedisProxy.getInstance().updateSWJoinGuild(joinInfo, termId);
		}
		DungeonRedisLog.log("StarWarsActivityService", "win guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
				winGuild, GuildService.getInstance().getGuildName(winGuild), warType, 0, 0, roomId, roomData.getRoomServerId());
	}

	/**
	 *  战场结束状态检测,所有第一场比赛结束后,创建第二场比赛房间  TODO 日志
	 */
	private boolean firstWarStatusCheck() {
		int termId = activityInfo.getTermId();
		List<SWRoomData> roomList = RedisProxy.getInstance().getAllSWRoomData(termId, SWWarType.FIRST_WAR.getNumber());
		Map<Integer, List<String>> zoneWinner = new HashMap<>();
		boolean isFinish = true;
		for (SWRoomData room : roomList) {
			String winner = room.getWinnerId();
			List<String> roomGuilds = room.getGuildList();
			if (!roomGuilds.isEmpty() && HawkOSOperator.isEmptyString(winner)) {
				isFinish = false;
				break;
			}
			int zoneId = room.getZoneId();
			if(!zoneWinner.containsKey(zoneId)){
				zoneWinner.put(zoneId, new ArrayList<>());
			}
			if (!HawkOSOperator.isEmptyString(winner)) {
				zoneWinner.get(zoneId).add(winner);
			}
		}
		Map<String, SWGuildData> guildDatas = RedisProxy.getInstance().getAllSWGuildData(termId);
		if (isFinish) {
			List<SWRoomData> secondRoomList = new ArrayList<>();
			Set<String> roomServerSet = new HashSet<>();
			for(Entry<Integer, List<String>> entry : zoneWinner.entrySet()){
				int zoneId = entry.getKey();
				List<String> winnerIds = entry.getValue();
				SWRoomData secondRoom = new SWRoomData();
				String terminalRoomId = String.valueOf(zoneId);
				if(winnerIds.isEmpty()){
					HawkLog.logPrintln("StarWarsActivityService firstWarStatusCheck winnerIds empty, zoneId: {}", zoneId);
					continue;
				}
				String roomServer = getRoomServer(guildDatas, roomServerSet, winnerIds);
				secondRoom.setId(terminalRoomId);
				secondRoom.setRoomServerId(roomServer);
				secondRoom.setGuildList(winnerIds);
				secondRoom.setRoomState(SWRoomState.NOT_INIT);
				secondRoom.setZoneId(zoneId);
				secondRoomList.add(secondRoom);
				roomServerSet.add(roomServer);
				for (String guildId : winnerIds) {
					SWGuildData guildData = guildDatas.get(guildId);
					if (guildData == null) {
						HawkLog.logPrintln("StarWarsActivityService firstWarStatusCheck guildDataIsNull, guildId: {}", guildId);
						continue;
					}
					LogUtil.logSWMatchInfo(terminalRoomId, roomServer, termId, SWWarType.SECOND_WAR, guildId, guildData.getServerId());
					DungeonRedisLog.log("StarWarsActivityService", "battle march guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
							guildId, guildData.getName(), SWWarType.SECOND_WAR, zoneId, 0, terminalRoomId,roomServer);
				}
			}
			RedisProxy.getInstance().updateSWRoomData(secondRoomList, termId, SWWarType.SECOND_WAR.getNumber());
		}
		return isFinish;
	}

	/**
	 * 第二场战场结束状态检测,所有第二场比赛结束后,创建第三场比赛房间  TODO 日志
	 */
	private boolean secondWarStatusCheck() {
		int termId = activityInfo.getTermId();
		List<SWRoomData> roomList = RedisProxy.getInstance().getAllSWRoomData(termId, SWWarType.SECOND_WAR.getNumber());
		List<String> winnerIds = new ArrayList<>();
		boolean isFinish = true;
		for (SWRoomData room : roomList) {
			String winner = room.getWinnerId();
			List<String> roomGuilds = room.getGuildList();
			if (!roomGuilds.isEmpty() && HawkOSOperator.isEmptyString(winner)) {
				isFinish = false;
				break;
			}
			if (!HawkOSOperator.isEmptyString(winner)) {
				winnerIds.add(winner);
			}
		}
		if (isFinish) {
			Map<String, SWGuildData> guildDatas = RedisProxy.getInstance().getSWGuildDatas(termId, winnerIds);
			String roomServer = getRoomServer(guildDatas, new HashSet<String>(), winnerIds);
			SWRoomData thirdRoom = new SWRoomData();
			String thirdRoomId = String.valueOf(0);
			thirdRoom.setId(thirdRoomId);
			thirdRoom.setRoomServerId(roomServer);
			thirdRoom.setGuildList(winnerIds);
			thirdRoom.setRoomState(SWRoomState.NOT_INIT);
			thirdRoom.setZoneId(StarWarsConst.WORLD_PART);
			RedisProxy.getInstance().updateSWRoomData(thirdRoom, termId, SWWarType.THIRD_WAR.getNumber());
			for (String guildId : winnerIds) {
				SWGuildData guildData = guildDatas.get(guildId);
				if (guildData == null) {
					HawkLog.logPrintln("StarWarsActivityService secondWarStatusCheck guildDataIsNull, guildId: {}", guildId);
					continue;
				}
				LogUtil.logSWMatchInfo(thirdRoomId, roomServer, termId, SWWarType.THIRD_WAR, guildId, guildData.getServerId());
				DungeonRedisLog.log("StarWarsActivityService", "battle march guildId:{} guildName:{} warType:{}, zoneId: {},teamId:{} , roomId:{},roomServer:{}",
						guildId, guildData.getName(), SWWarType.THIRD_WAR, 0, 0, thirdRoomId,roomServer);
			}
		}
		return isFinish;
	}
	
	/**
	 * 战场内未产生胜利者,从参战者中
	 * @param guildData
	 * @return
	 */
	private String calcEmptyWinner(SWRoomData roomData) {
		int termId = activityInfo.getTermId();
		List<String> guildIds = roomData.getGuildList();
		if(guildIds.isEmpty()){
			return "";
		}
		Map<String, SWGuildData> dataMap = RedisProxy.getInstance().getSWGuildDatas(termId, guildIds);
		if(dataMap.isEmpty()){
			return "";
		}
		List<SWGuildData> guildList = new ArrayList<>(dataMap.values());
		Collections.sort(guildList);
		SWGuildData winner = guildList.get(0);
		return winner.getId();
	}

	/**
	 * 解散联盟检测
	 * @param guildId
	 * @return
	 */
	public int canDismiss(String guildId) {
		// 还未到匹配阶段,可以解散
		if (activityInfo.getState().getNumber() < SWActivityState.MATCH.getNumber()) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		
		// 匹配阶段不允许解散
		if(activityInfo.getState() == SWActivityState.MATCH){
			return Status.Error.SW_MATCH_CANNOT_DISMISS_VALUE;
		}
		
		// 未入选本次比赛,可以解散
		if (joinGuilds.containsKey(guildId)) {
			return Status.Error.SW_JOIN_GUILD_CANNOT_DISMISS_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 判断当前联盟可否进行人员变更操作
	 * @param guildId
	 * @return
	 */
	public boolean checkGuildMemberOps(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return true;
		}
		SWGroupType groupType = joinGuilds.get(guildId);
		if (groupType == null) {
			return true;
		}
		// 第一场比赛参与者在第一场管理阶段结束后不能进行人员变更
		if (groupType == SWGroupType.PARTNER && fightData.getState().getNumber() > SWFightState.FIRST_MANAGE.getNumber()
				&& fightData.getState().getNumber() <= SWFightState.FIRST_CALC.getNumber()) {
			return false;
		}

		// 第二场比赛参与者在第二场管理阶段结束后不能进行人员变更
		if (groupType == SWGroupType.FIRST_WINNER && fightData.getState().getNumber() > SWFightState.SECOND_MANGE.getNumber()
				&& fightData.getState().getNumber() <= SWFightState.SECOND_CALC.getNumber()) {
			return false;
		}
		
		// 第三场比赛参与者在第三场管理阶段结束后不能进行人员变更
		if (groupType == SWGroupType.SECOND_WINNER && fightData.getState().getNumber() > SWFightState.THIRD_MANGE.getNumber()
				&& fightData.getState().getNumber() <= SWFightState.THIRD_CALC.getNumber()) {
			return false;
		}

		return true;
	}
	
	/**
	 * 获取本服赛区
	 * @return
	 */
	public int getLocalServerZone(){
		String serverId = GsConfig.getInstance().getServerId();
		int zoneId = AssembleDataManager.getInstance().getServerPart(serverId);
		return zoneId;
	}
	
	public int getLocalServerTeamId() {
		String serverId = GsConfig.getInstance().getServerId();
		int teamId = AssembleDataManager.getInstance().getTeamId(serverId);
		return teamId;
	}
	
	/**
	 * 获取本服的分区配置,未开启则配null
	 * @return
	 */
	public StarWarsPartCfg getLocalServerPartCfg(){
		String serverId = GsConfig.getInstance().getServerId();
		ConfigIterator<StarWarsPartCfg> its = HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class);
		for (StarWarsPartCfg cfg : its) {
			if(cfg.getZone() == GsConst.StarWarsConst.WORLD_PART){
				continue;
			}
			if(cfg.getTeam() == GsConst.StarWarsConst.TEAM_NONE){
				continue;
			}
			if (cfg.getServerList().contains(serverId)) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 本服是否是战场服
	 * @return
	 */
	public boolean isBattleServer(){
		String areaId = GsConfig.getInstance().getAreaId();
		String serverId = GsConfig.getInstance().getServerId();
		String roomServer = StarWarsConstCfg.getInstance().getRoomServer(areaId);
		return serverId.equals(roomServer);
	}
	
	
	
	/**
	 * 是否是结果结算服
	 * @return
	 */
	public boolean isCalServer(){
		String serverId = GsConfig.getInstance().getServerId();
		String roomServer = this.getCalServer();
		return serverId.equals(roomServer);
	}
	
	
	public String getCalServer(){
//		if(!HawkOSOperator.isEmptyString(calServer)){
//			return calServer;
//		}
		String areaId = GsConfig.getInstance().getAreaId();
		String roomServer = StarWarsConstCfg.getInstance().getCalServer(areaId);
		if(HawkOSOperator.isEmptyString(roomServer) ||
				!GlobalData.getInstance().isMainServer(roomServer)){
			ConfigIterator<StarWarsPartCfg> its = HawkConfigManager.getInstance().getConfigIterator(StarWarsPartCfg.class);
			for (StarWarsPartCfg cfg : its) {
				if(cfg.getAreaId().equals(areaId) &&
						cfg.getZone() == 1 && cfg.getTeam()==1){
					roomServer = cfg.getServerList().get(0);
					break;
				}
			}
		}
		return roomServer;
	}
	
	/**
	 * 只有在战争阶段才能拿到玩家数据.
	 * @param player
	 * @return
	 */
	public SWPlayerData getCurrentPlayerData(Player player) {
		if (!player.hasGuild()) {
			return null;
		}
		if (!isJoinCurrWar(player.getGuildId())) {
			return null;
		}
		SWWarType warType = getCurrWarType();
		if (warType == null) {
			return null;
		}
		int termId = activityInfo.getTermId();
		
		
		return RedisProxy.getInstance().getSWPlayerData(player.getId(), termId, warType.getNumber());		
	}
	/**
	 * 获取玩家当前可进入的战场房间信息
	 * @param player
	 * @return
	 */
	public SWRoomData getCurrRoomData(Player player) {
		if (!player.hasGuild()) {
			return null;
		}
		if (!isJoinCurrWar(player.getGuildId())) {
			return null;
		}
		SWWarType warType = getCurrWarType();
		if (warType == null) {
			return null;
		}
		int termId = activityInfo.getTermId();
		String serverId = player.getMainServerId();
		StarWarsPartCfg partCfg = AssembleDataManager.getInstance().getServerPartCfg(serverId);
		int zoneId = partCfg.getZone();
		int teamId = partCfg.getTeam();
		if (warType == SWWarType.FIRST_WAR) {
			return getFirstWarRoom(termId, zoneId, teamId);
		} else if (warType == SWWarType.SECOND_WAR) {
			return getSecondWarRoom(termId, zoneId);
		} else if (warType == SWWarType.THIRD_WAR) {
			return getThirdWarRoom(termId);
		}
		return null;
	}
	
	/**
	 * 获取对应阶段本服对应赛区的房间信息
	 * @param player
	 * @return
	 */
	public SWRoomData getLocalRoomData(SWWarType warType) {
		int termId = activityInfo.getTermId();
		StarWarsPartCfg partCfg = getLocalServerPartCfg();
		int zoneId = partCfg.getZone();
		int teamId = partCfg.getTeam();
		if (warType == SWWarType.FIRST_WAR) {
			return getFirstWarRoom(termId, zoneId, teamId);
		} else if (warType == SWWarType.SECOND_WAR) {
			return getSecondWarRoom(termId, zoneId);
		} else if (warType == SWWarType.THIRD_WAR) {
			return getThirdWarRoom(termId);
		}
		return null;
	}
		
	public String getRedisStateName() {
		try {
			SWActivityState state = activityInfo.getState();
			if (state.getNumber() <= SWActivityState.WAR_TWO_WAIT.getNumber()) {
				return  SWWarType.FIRST_WAR.toString();
			}  else if ( state.getNumber() <= SWActivityState.WAR_THREE_WAIT.getNumber()) {
				return SWWarType.SECOND_WAR.toString();
			} else {
				return SWWarType.THIRD_WAR.toString();
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return "excpetion";
	}
	/**
	 * 战斗的剩余时间.
	 * @return
	 */
	public long getWarLeftTime() {
		SWActivityState state = activityInfo.getState();
		long curTime = HawkTime.getMillisecond();
		if (state == SWActivityState.WAR_ONE_OPEN) {
			return this.getActivityData().getTimeCfg().getWarEndTimeOneValue() - curTime + 900_000;
		} else if (state == SWActivityState.WAR_TWO_OPEN) {
			return this.getActivityData().getTimeCfg().getWarEndTimeTwoValue() - curTime + 900_000;
		} else if (state == SWActivityState.WAR_THREE_OPEN) {
			return this.getActivityData().getTimeCfg().getWarEndTimeThreeValue() - curTime + 900_000;
		} else {
			return 0;
		}
	}
	/**
	 * 跨服前检测进入战场条件
	 * @param player
	 * @return
	 */
	public int checkEnterCondition(Player player) {
		int termId = activityInfo.getTermId();
		SWActivityState state = activityInfo.getState();
		SWRoomData roomData = null;
		StarWarsPartCfg localCfg = getLocalServerPartCfg();
		int zoneId = localCfg.getZone();
		int teamId = localCfg.getTeam();
		SWWarType warType = null;
		if (state == SWActivityState.WAR_ONE_OPEN) {
			roomData = getFirstWarRoom(termId, zoneId, teamId);
			warType = SWWarType.FIRST_WAR;
		} else if (state == SWActivityState.WAR_TWO_OPEN) {
			roomData = getSecondWarRoom(termId, zoneId);
			warType = SWWarType.SECOND_WAR;
		} else if (state == SWActivityState.WAR_THREE_OPEN) {
			roomData = getThirdWarRoom(termId);
			warType = SWWarType.THIRD_WAR;
		}else{
			return Status.Error.SW_NOT_OPEN_STATE_VALUE;
		}

		// 战场信息不存在
		if (roomData == null) {
			HawkLog.logPrintln("StarWarsActivityService enter room check error, room not esixt, playerId: {}, termId: {}", player.getId(), activityInfo.getTermId());
			return Status.Error.SW_ROOM_NOT_EXIST_VALUE;
		}
		String roomId = roomData.getId();
		
		SWPlayerData swPlayerData = RedisProxy.getInstance().getSWPlayerData(player.getId(), termId, warType.getNumber());
		if (swPlayerData == null) {
			HawkLog.logPrintln("StarWarsActivityService enter room check error, twPlayerData is null, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), player.getGuildId());
			return Status.Error.SW_ROOM_NOT_EXIST_VALUE;
		}
		
		String guildId = player.getGuildId();
		if (!swPlayerData.getGuildId().equals(guildId)) {
			HawkLog.logPrintln(
					"StarWarsActivityService enter room check error, guildIds not match, playerId: {}, termId: {}, roomId: {}, roomServer: {}, twDataGuildId: {}, playerGuildId: {}",
					player.getId(), activityInfo.getTermId(), roomId, roomData.getRoomServerId(), guildId, player.getGuildId());
			return Status.Error.SW_GUILD_NOT_JOIN_VALUE;
		}
		
		if (swPlayerData.getQuitTime() > 0) {
			HawkLog.logPrintln("StarWarsActivityService enter room check error, has entered, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(),
					activityInfo.getTermId(), roomId, roomData.getRoomServerId(), swPlayerData.getGuildId());
			return Status.Error.SW_HAS_ENTERED_VALUE;
		}
		
		//这个判断一定要放在最后否则有问题.
		boolean result = StarWarsActivityService.getInstance().tryAddJoinPlayer(player.getGuildId(), player.getId());
		if (!result) {
			return Status.Error.SW_ENTER_CNT_OVER_LIMIT_VALUE;
		}
		HawkLog.logPrintln("StarWarsActivityService enter room check, playerId: {}, termId: {}, roomId: {}, roomServer: {}, guildId: {}", player.getId(), termId, roomId, roomId, swPlayerData.getGuildId());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 如果可以加入则把玩家加入,不能则直接返回了.
	 * @param GuildId
	 * @param playerId
	 * @return
	 */
	public boolean tryAddJoinPlayer(String guildId, String playerId) {
		Set<String> playerIdSet = guildPlayerSet.get(guildId);
		if (playerIdSet == null) {
			playerIdSet = new HashSet<>();
			Set<String> oldSet = guildPlayerSet.putIfAbsent(guildId, playerIdSet);
			if (oldSet != null) {
				playerIdSet = oldSet;
			}
		}
		StarWarsConstCfg constCfg = StarWarsConstCfg.getInstance();
		synchronized (playerIdSet) {
			//已经在里面就不管他了.
			if (playerIdSet.contains(playerId)) {
				return true;
			}
			//判断人数.
			if (playerIdSet.size() >= constCfg.getMemberCntLimit()) {
				return false;
			}
			playerIdSet.add(playerId);
		}
		
		return true;
	}
	
	/**
	 * 删除进入的人员信息.
	 * @param guildId
	 * @param playerId
	 */
	public void removeJoinPlayer(String guildId, String playerId) {		
		Set<String> playerIdSet = guildPlayerSet.get(guildId);
		if (playerIdSet == null) {
			return;
		}
		
		synchronized (playerIdSet) {
			playerIdSet.remove(playerId);
		} 
	}
	
	/**
	 * 清理所有的人数信息.
	 */
	public void clearAllGuildJoinPlayer() {
		this.guildPlayerSet = new ConcurrentHashMap<>();
	}
	
	/**
	 * 尝试计数,减少登录玩家.
	 */
	public void tryDeincreseLoginingPlayer() {
		try {
			StarWarsConstCfg starWarsConstCfg = StarWarsConstCfg.getInstance();
			if (starWarsConstCfg.getMaxWaitLoginingPlayer() != 0) {
				long decreaseNum = RedisProxy.getInstance().decreaseStarWarsLogininPlayer(StarWarsActivityService.getInstance().getRedisStateName());	
				HawkLog.logPrintln("starWarsLoginingPlayer number:{}", decreaseNum);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}		
	}
}
