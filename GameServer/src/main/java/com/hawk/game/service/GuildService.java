package com.hawk.game.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.hawk.game.invoker.guildTeam.GuildTeamDismissGuildInvoker;
import com.hawk.game.invoker.guildTeam.GuildTeamQuitGuildInvoker;
import com.hawk.game.invoker.xhjz.XHJZWarDismissGuildInvoker;
import com.hawk.game.invoker.xhjz.XHJZWarQuitGuildInvoker;
import com.hawk.game.protocol.*;
import com.hawk.game.service.guildTeam.GuildTeamService;
import com.hawk.game.service.tblyTeam.TBLYSeasonService;
import com.hawk.game.service.tblyTeam.TBLYWarService;
import com.hawk.game.service.xhjzWar.XHJZGuildInfoObject;
import com.hawk.game.service.xhjzWar.XHJZWarGuildData;
import com.hawk.game.service.xhjzWar.XHJZWarService;
import com.hawk.game.service.xqhxWar.XQHXGuildInfoObject;
import com.hawk.game.service.xqhxWar.XQHXWarService;
import com.hawk.game.service.xqhxWar.model.XQHXWarGuildData;
import org.apache.commons.lang.StringUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.collection.ConcurrentHashSet;
import org.hawk.collection.ConcurrentHashTable;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.delay.HawkDelayAction;
import org.hawk.helper.HawkAssert;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.tuple.HawkTuple2;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GuildHelpEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.common.AccountRoleInfo;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.activity.impl.yurirevenge.YuriRevengeService;
import com.hawk.game.config.AllianceNewAuthorityCfg;
import com.hawk.game.config.AllianceOfficialCfg;
import com.hawk.game.config.AllianceSignatureCfg;
import com.hawk.game.config.AllianceTaskCfg;
import com.hawk.game.config.AllianceTaskGroupCfg;
import com.hawk.game.config.AllianceTaskRewardCfg;
import com.hawk.game.config.BuildingCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.config.GameConstCfg;
import com.hawk.game.config.GuildConstProperty;
import com.hawk.game.config.GuildFlagCfg;
import com.hawk.game.config.GuildScienceFloorCfg;
import com.hawk.game.config.GuildScienceLevelCfg;
import com.hawk.game.config.GuildScienceMainCfg;
import com.hawk.game.config.GuildShopCfg;
import com.hawk.game.config.XZQConstCfg;
import com.hawk.game.crossactivity.CActivityInfo;
import com.hawk.game.crossactivity.CrossActivityService;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.model.CsGuildInfoObject;
import com.hawk.game.crossproxy.model.CsPlayer;
import com.hawk.game.entity.DailyDataEntity;
import com.hawk.game.entity.GuildBigGiftEntity;
import com.hawk.game.entity.GuildCounterattackEntity;
import com.hawk.game.entity.GuildInfoEntity;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.GuildMemberEntity;
import com.hawk.game.entity.GuildMemberObject;
import com.hawk.game.entity.GuildScienceEntity;
import com.hawk.game.entity.QueueEntity;
import com.hawk.game.entity.StorehouseBaseEntity;
import com.hawk.game.entity.StorehouseEntity;
import com.hawk.game.entity.StorehouseHelpEntity;
import com.hawk.game.entity.item.GuildFormationObj;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.LocalRedis;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.guild.Counterattack;
import com.hawk.game.guild.GuildBigGift;
import com.hawk.game.guild.GuildCreateObj;
import com.hawk.game.guild.GuildDonateRank;
import com.hawk.game.guild.GuildHelpInfo;
import com.hawk.game.guild.GuildRankObj;
import com.hawk.game.guild.championship.ChampionshipService;
import com.hawk.game.guild.guildrank.GuildRankMgr;
import com.hawk.game.guild.voice.VoiceRoomManager;
import com.hawk.game.invoker.GuildChangeMemberKillCntInvoker;
import com.hawk.game.invoker.GuildClearMemberKillCntInvoker;
import com.hawk.game.invoker.GuildClearMemberPowerInvoker;
import com.hawk.game.invoker.GuildDissmiseRemoveManorMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.log.BehaviorLogger;
import com.hawk.game.log.DungeonRedisLog;
import com.hawk.game.log.BehaviorLogger.Params;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGuildData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZGuildInfoObject;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZRedisData;
import com.hawk.game.module.dayazhizhan.playerteam.service.DYZZService;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZGuildInfoObject;
import com.hawk.game.module.lianmengyqzz.march.data.global.YQZZJoinGuild;
import com.hawk.game.module.lianmengyqzz.march.service.YQZZMatchService;
import com.hawk.game.msg.AcceptGuildApplyMsg;
import com.hawk.game.msg.GuildJoinMsg;
import com.hawk.game.msg.GuildQuitMsg;
import com.hawk.game.msg.GuildTaskMsg;
import com.hawk.game.msg.MigrateOutPlayerMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg;
import com.hawk.game.msg.PlayerUnlockImageMsg.PLAYERSTAT_PARAM;
import com.hawk.game.msg.PlayerUnlockImageMsg.UnlockType;
import com.hawk.game.msg.QueueBeHelpedMsg;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.protocol.Const.BuildingStatus;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.GuildAuthority;
import com.hawk.game.protocol.Const.GuildPositon;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.Const.QueueStatus;
import com.hawk.game.protocol.Const.QueueType;
import com.hawk.game.protocol.Cross.CrossType;
import com.hawk.game.protocol.CrossActivity.CGuildInfo;
import com.hawk.game.protocol.CrossActivity.CPlayerInfo;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.GuildManager.AuthId;
import com.hawk.game.protocol.GuildManager.BeGuildHelpedRes;
import com.hawk.game.protocol.GuildManager.DonateRankType;
import com.hawk.game.protocol.GuildManager.GetGuildInfoResp;
import com.hawk.game.protocol.GuildManager.GetGuildMemeberInfoResp;
import com.hawk.game.protocol.GuildManager.GetOfficerApplyListResp;
import com.hawk.game.protocol.GuildManager.GetOtherGuildResp;
import com.hawk.game.protocol.GuildManager.GetRecommendGuildListResp;
import com.hawk.game.protocol.GuildManager.GetSearchGuildListResp;
import com.hawk.game.protocol.GuildManager.GuildApplyInfo;
import com.hawk.game.protocol.GuildManager.GuildAuthInfo;
import com.hawk.game.protocol.GuildManager.GuildBBSMessage;
import com.hawk.game.protocol.GuildManager.GuildDonateRankInfo;
import com.hawk.game.protocol.GuildManager.GuildGetDonateRankResp;
import com.hawk.game.protocol.GuildManager.GuildHelpQueue;
import com.hawk.game.protocol.GuildManager.GuildLogType;
import com.hawk.game.protocol.GuildManager.GuildMemeberInfo;
import com.hawk.game.protocol.GuildManager.GuildRecommendType;
import com.hawk.game.protocol.GuildManager.GuildShopLog;
import com.hawk.game.protocol.GuildManager.GuildSign;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopInfoResp;
import com.hawk.game.protocol.GuildManager.HPGetGuildShopLogResp;
import com.hawk.game.protocol.GuildManager.HPGuildApplyNumSync;
import com.hawk.game.protocol.GuildManager.HPGuildInfoSync;
import com.hawk.game.protocol.GuildManager.HPGuildLog;
import com.hawk.game.protocol.GuildManager.HPGuildShopItem;
import com.hawk.game.protocol.GuildManager.InvitationLetter;
import com.hawk.game.protocol.GuildManager.RefreshGuildHelpQueueNumRes;
import com.hawk.game.protocol.GuildManager.VoiceRoomModel;
import com.hawk.game.protocol.GuildScience.DonateType;
import com.hawk.game.protocol.GuildScience.GetGuildScienceInfoResp;
import com.hawk.game.protocol.GuildScience.GuildScienceDonateResp;
import com.hawk.game.protocol.GuildScience.GuildScienceInfo;
import com.hawk.game.protocol.GuildScience.GuildScienceInfoSync;
import com.hawk.game.protocol.GuildTask.GuildSignInfo;
import com.hawk.game.protocol.GuildTask.GuildTaskInfo;
import com.hawk.game.protocol.GuildTask.TashInfo;
import com.hawk.game.protocol.GuildTask.TaskStatus;
import com.hawk.game.protocol.GuildTask.TaskType;
import com.hawk.game.protocol.GuildWar.HPGuildWarCountPush;
import com.hawk.game.protocol.GuildWar.PushNewGuildWarRecord;
import com.hawk.game.protocol.Mail.GuildInviteMail;
import com.hawk.game.protocol.Mail.InviteState;
import com.hawk.game.protocol.Mail.MoveCityInviteMail;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.World.WorldMarchStatus;
import com.hawk.game.queryentity.AccountInfo;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.cyborgWar.CWGuildData;
import com.hawk.game.service.cyborgWar.CWGuildInfoObject;
import com.hawk.game.service.cyborgWar.CyborgWarRedis;
import com.hawk.game.service.cyborgWar.CyborgWarService;
import com.hawk.game.service.guildtask.GuildTaskCfgItem;
import com.hawk.game.service.guildtask.GuildTaskContext;
import com.hawk.game.service.guildtask.GuildTaskEvent;
import com.hawk.game.service.guildtask.GuildTaskItem;
import com.hawk.game.service.guildtask.GuildTaskType;
import com.hawk.game.service.guildtask.event.GuildHelpTaskEvent;
import com.hawk.game.service.guildtask.event.MemberLoginTaskEvent;
import com.hawk.game.service.guildtask.impl.IGuildTask;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mssion.MissionManager;
import com.hawk.game.service.mssion.event.EventGuildHelp;
import com.hawk.game.service.mssion.event.EventGuildMemberChange;
import com.hawk.game.service.starwars.SWGuildData;
import com.hawk.game.service.starwars.SWGuildInfoObject;
import com.hawk.game.service.starwars.StarWarsActivityService;
import com.hawk.game.service.starwars.StarWarsOfficerService;
import com.hawk.game.service.tiberium.TWGuildData;
import com.hawk.game.service.tiberium.TWGuildInfoObject;
import com.hawk.game.service.tiberium.TiberiumWarService;
import com.hawk.game.util.BuilderUtil;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.GuildActiveType;
import com.hawk.game.util.GsConst.GuildConst;
import com.hawk.game.util.GsConst.GuildOffice;
import com.hawk.game.util.GsConst.ScoreType;
import com.hawk.game.util.GsConst.TimerEventEnum;
import com.hawk.game.util.GuildUtil;
import com.hawk.game.util.LogUtil;
import com.hawk.game.util.MapUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.WorldPoint;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldPlayerService;
import com.hawk.game.world.service.WorldPointService;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.GuildAction;
import com.hawk.log.LogConst.GuildOperType;
import com.hawk.log.LogConst.GuildTechOperType;
import com.hawk.log.Source;

/**
 *
 * 联盟服务管理器
 * 
 * @author shadow
 *
 */
public class GuildService extends HawkAppObj {
	static Logger logger = LoggerFactory.getLogger("Server");
	/**
	 * 服务器联盟信息
	 */
	private Map<String, GuildInfoObject> guildData;
	private Map<String, GuildBigGift> guildGift;
	/**
	 * 玩家与联盟映射
	 */
	private Map<String, GuildMemberObject> playerGuild;
	/**
	 * 服务器联盟成员信息
	 */
	private Map<String, Set<String>> guildMemberData;
	
	/**
	 * 跨服联盟成员信息
	 */
	private Map<String, Set<String>> cGuildMemberData;
	
	/**
	 * 跨服玩家与联盟映射
	 */
	private Map<String, String> cPlayerGuild;
	
	/**
	 * 跨服玩家联盟阶级
	 */
	private Map<String, Integer> cPlayerAuth;
	
	/**
	 * 跨服服务器联盟信息
	 */
	private Map<String, GuildInfoObject> cGuildData;
	
	/**
	 * 联盟科技列表
	 */
	private Map<String, List<GuildScienceEntity>> guildScienceData;
	/**
	 * 科技研究列表
	 */
	private Map<String, List<GuildScienceEntity>> researchData;
	/**
	 * 联盟帮助信息
	 */
	private Table<String,String,GuildHelpInfo> guildHelpInfoTable;
	/**
	 * 联盟科技层级
	 */
	private Map<String, Integer> guildScienceFloor;
	/**
	 * 联盟科技作用号
	 */
	private Map<String, Map<Integer, Integer>> effectGuildTech;
	/**
	 * 跨服科技作用号.
	 */
	private Map<String, Map<Integer, Integer>> csEffectGuildTech;

	/**
	 * 联盟捐献排名
	 */
	private Map<String, GuildRankObj> guildRankObjs;

	/**
	 * 联盟反击列表 guildId,uuid, db
	 */
	private Table<String, String, Counterattack> guildCounterAttackData;

	/**
	 * 联盟标记列表
	 */
	private Map<String, Map<Integer, GuildSign>> guildSignMaps;
	
	/**
	 * 联盟任务
	 */
	private Map<String, List<GuildTaskItem>> guildTaskMap;
	
	/**
	 * 今日登录成员信息
	 */
	private Map<String, Set<String>> dailyLoginMembers;

	/**
	 * 被联盟邀请时间
	 */
	private Map<String, Long> beInviteTime = new ConcurrentHashMap<>();
	
	/**
	 * 上次捐献排行检测时间
	 */
	private long lastCrossCheckTime = 0;

	/**
	 * 单例
	 */
	private static GuildService instance;

	private GuildFormationObj emptyfo;
	/**
	 * 
	 * @return 单例
	 */
	public static GuildService getInstance() {
		return instance;
	}

	/**
	 * 联盟服务
	 * 
	 * @param xid
	 */
	public GuildService(HawkXID xid) {
		super(xid);
		if (instance == null) {
			instance = this;
		}
	}

	/**
	 * 是否联盟反击目标
	 * 
	 * @param viewerId
	 *            观察着id
	 * @param attackerId
	 * @return
	 */
	public boolean isCounterattacker(String viewerId, String attackerId) {
		if (Objects.isNull(viewerId) || Objects.isNull(attackerId)) {
			return false;
		}
		if (isInTheSameGuild(viewerId, attackerId)) {
			return false;
		}
		String guildid = getPlayerGuildId(viewerId);
		if (Objects.isNull(guildid)) {
			return false;
		}
		long counterCount = guildCounterAttackData.row(guildid).values().stream()
				.filter(en -> Objects.equals(attackerId, en.getAtkerId()))
				.count();
		return counterCount > 0;
	}

	/** 通知联盟成员反击有变化 */
	public void notifycounterattarkChang(String guildId, String atkPlayerId) {
		broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.COUNTERATTACK_CHANGED_S));
		WorldPoint worldPoint = WorldPlayerService.getInstance().getPlayerWorldPoint(atkPlayerId);
		if (Objects.nonNull(worldPoint)) {
			WorldPointService.getInstance().getWorldScene().update(worldPoint.getAoiObjId());
		}
		List<WorldPoint> list = WorldPointService.getInstance().getOccupyPoints(atkPlayerId);
		for (WorldPoint wp : list) {
			WorldPointService.getInstance().getWorldScene().update(wp.getAoiObjId());
		}
	}

	/**
	 * 取得单条联盟反击记录
	 * 
	 * @return
	 */
	public Counterattack counterAttack(String guildId, String uuid) {
		Counterattack entity = guildCounterAttackData.get(guildId, uuid);
		if (Objects.isNull(entity)) {
			return null;
		}
		return entity;
	}

	/**
	 * 联盟大礼包
	 */
	public GuildBigGift bigGift(String guildId) {
		HawkAssert.notNull(guildId);
		if (!guildGift.containsKey(guildId)) {
			createBigGift(guildId);
		}
		return guildGift.get(guildId);
	}

	/**
	 * 全联盟反击记录
	 * 
	 * @param guildId
	 * @return
	 */
	public List<Counterattack> guildCounterAttack(String guildId) {
		if (Objects.isNull(guildId)) {
			return Collections.emptyList();
		}
		List<Counterattack> result = guildCounterAttackData.row(guildId).values().stream()
				.collect(Collectors.toList());
		return result;
	}
	
	/** 联盟反击对象 */
	public Set<String> guildCounterAttacker(String guildId) {
		if (Objects.isNull(guildId)) {
			return Collections.emptySet();
		}
		Set<String> result = guildCounterAttackData.row(guildId).values().stream()
				.map(Counterattack::getAtkerId)
				.collect(Collectors.toSet());
		return result;
	}

	/** 移除反击记录 */
	public void counterattackRemove(Counterattack counter) {
		Counterattack toRemove = guildCounterAttackData.remove(counter.getDbObj().getGuildId(), counter.getDbObj().getId());
		if (Objects.nonNull(toRemove)) {
			notifycounterattarkChang(counter.getDbObj().getGuildId(), counter.getDbObj().getAtkerId());
			counter.getDbObj().delete();
		}
	}

	public void counterattackAdd(Counterattack counter) {
		GuildCounterattackEntity dbObj = counter.getDbObj();
		guildCounterAttackData.put(dbObj.getGuildId(), dbObj.getId(), counter);
		notifycounterattarkChang(counter.getDbObj().getGuildId(), counter.getDbObj().getAtkerId());
	}

	/**
	 * 加载联盟信息
	 * 
	 * @return
	 */
	public boolean initGuildData() {
		guildData = new ConcurrentHashMap<>();
		guildMemberData = new ConcurrentHashMap<>();
		guildScienceData = new ConcurrentHashMap<>();
		researchData = new ConcurrentHashMap<>();
		guildScienceFloor = new HashMap<>();
		effectGuildTech = new HashMap<>();
		playerGuild = new ConcurrentHashMap<>();
		guildRankObjs = new ConcurrentHashMap<>();
		guildCounterAttackData = ConcurrentHashTable.create();
		guildGift = new ConcurrentHashMap<>();
		guildSignMaps = new HashMap<>();
		guildTaskMap = new HashMap<>();
		GuildTaskContext.getInstance().init();
		
		cGuildMemberData = new ConcurrentHashMap<>();
		cPlayerGuild = new ConcurrentHashMap<>();
		cPlayerAuth = new ConcurrentHashMap<>();
		cGuildData = new ConcurrentHashMap<>();
		
		//跨服的联盟科技.
		csEffectGuildTech = new ConcurrentHashMap<>();
		
		List<GuildBigGiftEntity> bigGiftList = HawkDBManager.getInstance().query("from GuildBigGiftEntity where invalid = 0");
		bigGiftList.forEach(entity -> guildGift.put(entity.getGuildId(), entity.getGiftObj()));

		List<GuildCounterattackEntity> counterList = HawkDBManager.getInstance().query("from GuildCounterattackEntity where invalid = 0");
		counterList.forEach(entity -> guildCounterAttackData.put(entity.getGuildId(), entity.getId(), entity.getCounter()));

		List<String> leaderEmptyGuildIds = new ArrayList<>();
		List<GuildInfoEntity> guildEntities = HawkDBManager.getInstance().query("from GuildInfoEntity where invalid = 0");
		for (GuildInfoEntity guildInfoEntity : guildEntities) {
			String guildId = guildInfoEntity.getId();
			guildData.put(guildId, new GuildInfoObject(guildInfoEntity));
			
			if (GameConstCfg.getInstance().guildLeaderCheck()) {
				String leaderId = guildInfoEntity.getLeaderId();
				try {
					Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
					if (leader == null) {
						leaderEmptyGuildIds.add(guildId);
					} else {
						guildInfoEntity.setLeaderPlatform(leader.getPlatform());
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			} else if (HawkOSOperator.isEmptyString(guildInfoEntity.getLeaderPlatform())) {
				try {
					Player leader = GlobalData.getInstance().makesurePlayer(guildInfoEntity.getLeaderId());
					if (leader != null) {
						guildInfoEntity.setLeaderPlatform(leader.getPlatform());
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
			
			SearchService.getInstance().addGuildInfo(guildInfoEntity.getName(), guildId);
			SearchService.getInstance().addGuildTag(guildInfoEntity.getTag(), guildId);
			Map<String, String> donateDailyRank = LocalRedis.getInstance().getGuildDonateInfo(guildId, DonateRankType.DAILY_RANK);
			Map<String, String> donateDailyWeek = LocalRedis.getInstance().getGuildDonateInfo(guildId, DonateRankType.WEEK_RANK);
			Map<String, String> donateDailyTotal = LocalRedis.getInstance().getGuildDonateInfo(guildId, DonateRankType.TOTAL_RANK);
			GuildRankObj rankObj = new GuildRankObj(guildId, donateDailyRank, donateDailyWeek, donateDailyTotal);
			guildRankObjs.put(guildId, rankObj);
			Map<Integer, GuildSign> signMap = LocalRedis.getInstance().getGuildSigns(guildId);
			guildSignMaps.put(guildId, signMap);
			// 加载联盟任务
			List<GuildTaskItem> taskList = LocalRedis.getInstance().getGuildTaskList(guildId);
			// 起服时如果没有联盟任务,则进行初始化
			if (taskList.isEmpty()) {
				refreshGuildTask(guildData.get(guildId));
			}else{
				guildTaskMap.put(guildId, taskList);
			}
		}
		// 加载联盟科技
		loadGuildTechnologyEntities();
		loadGuildHelpInfoTable();
		loadGuildDailyLogin();
		List<GuildMemberEntity> guildMemberEntities = HawkDBManager.getInstance().query("from GuildMemberEntity");
		for (GuildMemberEntity memberData : guildMemberEntities) {
			if (memberData.getGuildId() == null) {
				playerGuild.put(memberData.getPlayerId(), new GuildMemberObject(memberData));
				continue;
			}
			if (!GlobalData.getInstance().isLocalPlayer(memberData.getPlayerId())) {
				continue;
			}
			if (guildMemberData.containsKey(memberData.getGuildId())) {
				guildMemberData.get(memberData.getGuildId()).add(memberData.getPlayerId());
				playerGuild.put(memberData.getPlayerId(), new GuildMemberObject(memberData));
			} else {
				Set<String> set = new ConcurrentHashSet<String>();
				guildMemberData.put(memberData.getGuildId(), set);
				set.add(memberData.getPlayerId());
				playerGuild.put(memberData.getPlayerId(), new GuildMemberObject(memberData));
			}
		}
		
		if (!leaderEmptyGuildIds.isEmpty()) {
			Iterator<String> iterator = leaderEmptyGuildIds.iterator();
			while (iterator.hasNext()) {
				String guildId = iterator.next();
				try {
					boolean exist = checkLeaderExistV2(guildId);
					if (!exist) {
						iterator.remove();
					}
				} catch (Exception e) {
					HawkException.catchException(e);
				}
			}
		}
		
		// 清除空联盟
		if (!leaderEmptyGuildIds.isEmpty()) {
			HawkTaskManager.getInstance().postExtraTask(new HawkDelayTask(60000, 300000, 1) {
				@Override
				public Object run() {
					try {
						for (String guildId : leaderEmptyGuildIds) {
							onDissmiseGuild(guildId, null);
							HawkLog.logPrintln("guild leader not exist, dismiss guild, guildId: {}", guildId);
						}
					} catch (Exception e) {
						HawkException.catchException(e);
					}
					return null;
				}
			});
		}

		// 注册周期性更新检测
		int upatePeriod = GameConstCfg.getInstance().getGuildTickPeriod();
		addTickable(new GuildPeriodTick(upatePeriod, upatePeriod));
		/** 添加死盟tick业务  **/
		int deadGuildTickTime = GuildConstProperty.getInstance().getDeadGuildCheckInterval();
		int delayTime = GuildConstProperty.getInstance().getDeadGuildCheckDelay();
		long serverOpenTime = GameUtil.getServerOpenTime();
		long curTime = HawkTime.getMillisecond();
		long timeInterval = curTime - serverOpenTime; //开服多少毫秒了
		if(timeInterval >= delayTime){
			delayTime = 0;
		}else{
			delayTime = (int)(delayTime - timeInterval);
		}
		if(deadGuildTickTime > 0){
			addTickable(new HawkPeriodTickable(deadGuildTickTime, delayTime) {				
				@Override
				public void onPeriodTick() {
					for(String guildId : guildData.keySet()){
						HawkTaskManager.getInstance().postExtraTask(new HawkTask() {							
							@Override
							public Object run() {
								checkDeadGuild(guildId);
								return null;
							}
						});
					}
					
				}
			});
		}else{
			throw new RuntimeException("GameConstCfg field deadGuildTickTime value is:" + deadGuildTickTime + ", delayTime is:" + delayTime);
		}
		/** 英雄试炼tick **/
		addTickable(new PlotBattle(PlotBattleService.getInstance().getTickPeriod(), PlotBattleService.getInstance().getTickPeriod()));
		loadCrossGuildInfo();
		
		emptyfo = new GuildFormationObj();
		emptyfo.unSerializ("");
		return true;
	}
	
	/**
	 * 起服加载全部联盟成员日登陆信息
	 */
	private void loadGuildDailyLogin() {
		dailyLoginMembers = new ConcurrentHashMap<>();
		Set<String> guildIdSet = new HashSet<>(guildData.keySet());
		for (String guildId : guildIdSet) {
			Set<String> memberSet = LocalRedis.getInstance().getGuildLoginMembers(guildId);
			if (memberSet != null && !memberSet.isEmpty()) {
				dailyLoginMembers.put(guildId, new HashSet<>(memberSet));
			}
		}

	}

	/**启服加载全部联盟帮助信息*/
	private void loadGuildHelpInfoTable() {
		guildHelpInfoTable = ConcurrentHashTable.create();
		Set<String> guildIdSet = new HashSet<>(guildData.keySet());
		for(String guildId:guildIdSet){
			Map<String, GuildHelpInfo> map = LocalRedis.getInstance().loadAllGuildHelpInfo(guildId);
			map.forEach((qid,info) -> guildHelpInfoTable.put(guildId, qid, info));
		}
	}
	
	/**停服入redis*/
	public void saveGuildHelpInfoTable() {
		try {
			Map<String, Map<String, GuildHelpInfo>> rowMap = guildHelpInfoTable.rowMap();
			for (Entry<String, Map<String, GuildHelpInfo>> ent : rowMap.entrySet()) {
				LocalRedis.getInstance().saveAllGuildHelpInfo(ent.getKey(), ent.getValue());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/** 删除联盟帮助信息*/
	public int deleteGuildHelpInfo(String guildId, String ... queueId) {
		int result = 0;
		for(String qid: queueId){
			GuildHelpInfo info = guildHelpInfoTable.remove(guildId, qid);
			if(Objects.nonNull(info)){
				result++;
			}
		}
		return result;
	}

	/**删除一个联盟的所有联盟帮助信息 */
	public void removeAllGuildHelpInfo(String guildId) {
		Set<String> qidSet = guildHelpInfoTable.row(guildId).keySet();
		deleteGuildHelpInfo(guildId, qidSet.toArray(new String[qidSet.size()]));
	}
	
	/** 获取某联盟的所有帮助信息 */
	public Map<String, GuildHelpInfo> getAllGuildHelpInfo(String guildId) {
		return guildHelpInfoTable.row(guildId);
	}
	
	/** 获取联盟帮助信息 */
	public GuildHelpInfo getGuildHelpInfo(String guildId, String queueId) {
		return guildHelpInfoTable.get(guildId, queueId);
	}
	
	/** 添加联盟帮助信息*/
	public void saveOrUpdateGuildHelpInfo(String guildId, String queueId, GuildHelpInfo queueInfo) {
		guildHelpInfoTable.put(guildId, queueId, queueInfo);
	}
	
	

	/** 周期性更新检测 */
	class GuildPeriodTick extends HawkPeriodTickable {
		public GuildPeriodTick(long tickPeriod, long delayTime) {
			super(tickPeriod, delayTime);
		}

		long lastTickCheckCounterattack;

		@Override
		public void onPeriodTick() {
			long start = HawkTime.getMillisecond();
			GuildService.this.checkScienceResearch();
			long time1 = HawkTime.getMillisecond();
			GuildService.this.crossDayCheck();
			long time2 = HawkTime.getMillisecond();
			GuildService.this.checkGuildTaskRefresh();

			long time3 = HawkTime.getMillisecond();
			GuildRankMgr.getInstance().onTick();
			long time4 = HawkTime.getMillisecond();

			if (start - lastTickCheckCounterattack > GsConst.MINUTE_MILLI_SECONDS) {
				GuildService.this.checkCounterattack(start);
				lastTickCheckCounterattack = start;
			}
			long time5 = HawkTime.getMillisecond();
			if (time5 - start > 1000) {
				HawkLog.logPrintln("GuildPeriodTick timeout, costtime:{}, scienceTick:{}, crossDayCheck:{}, taskRefresh:{}, guildRank:{}, counterattack:{}", time5 - start,
						time1 - start, time2 - time1, time3 - time2, time4 - time3, time5 - time4);
			}

		}

	}
	
	private class PlotBattle extends HawkPeriodTickable{

		public PlotBattle(long tickPeriod, long delayTime) {
			super(tickPeriod, delayTime);
		}

		@Override
		public void onPeriodTick() {
			PlotBattleService.getInstance().onTick();
		}
		
	}

	/** 联盟反击清理 */
	private void checkCounterattack(long overTime) {
		Table<String, String, Counterattack> dataCopy = ConcurrentHashTable.create();
		dataCopy.putAll(guildCounterAttackData);

		List<Collection<Counterattack>> guildList = dataCopy.rowMap().values().stream()
				.map(Map::values)
				.collect(Collectors.toList());
		for (Collection<Counterattack> guildCounters : guildList) {
			// 1 找出应该保留的
			Set<Counterattack> keepSet = guildCounters.stream()
					.filter(counter -> counter.getOverTime() > overTime) // 还没有过期的
					.sorted(Comparator.comparingLong(Counterattack::getCreateTime).reversed()) // 时间倒序
					.limit(ConstProperty.getInstance().getAllianceCareHelpQuantityUpLimit()) // 最后50条
					.collect(Collectors.toSet());

			if (keepSet.size() == guildCounters.size()) {
				continue;
			}

			Set<Counterattack> toRemove = guildCounters.stream()
					.filter(c -> !keepSet.contains(c))
					.collect(Collectors.toSet());

			for (Counterattack del : toRemove) {
				del.sendBackAllBounty(MailId.COUNTERATTACK_REWARD_CHEXIAO_BACK);
				counterattackRemove(del);
			}
		}

	}

	/**
	 * 加载联盟科技相关数据
	 * 
	 * @return
	 */
	private void loadGuildTechnologyEntities() {
		List<GuildScienceEntity> all = HawkDBManager.getInstance().query("from GuildScienceEntity where invalid = 0");
		for (GuildScienceEntity entity : all) {
			if (!guildScienceData.containsKey(entity.getGuildId())) {
				guildScienceData.put(entity.getGuildId(), new ArrayList<>());
			}
			guildScienceData.get(entity.getGuildId()).add(entity);
			if (entity.getFinishTime() <= 0) {
				continue;
			}
			// 加载研究中的科技
			if (!researchData.containsKey(entity.getGuildId())) {
				researchData.put(entity.getGuildId(), new ArrayList<>());
			}
			researchData.get(entity.getGuildId()).add(entity);
		}
		// 计算联盟科技层级
		for (Entry<String, List<GuildScienceEntity>> entry : guildScienceData.entrySet()) {
			int lvl = 0;
			String guildId = entry.getKey();
			Set<Integer> set = new HashSet<>();
			for (GuildScienceEntity guildScienceEntity : entry.getValue()) {
				lvl += guildScienceEntity.getLevel();
				set.add(guildScienceEntity.getScienceId());
			}
			int floor = calcGuildFloor(lvl);
			guildScienceFloor.put(guildId, floor);
			List<GuildScienceEntity> nlist = this.checkGuildScienceNew(floor, set, guildId);
			if(!nlist.isEmpty()){
				entry.getValue().addAll(nlist);
			}
			// 加载联盟科技作用号
			if (lvl > 0) {
				calcTechEffec(guildId);
			}
		}
	}
	
	public List<GuildScienceEntity> checkGuildScienceNew(int floor,Set<Integer> ids,String guildId){
		ConfigIterator<GuildScienceMainCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(GuildScienceMainCfg.class);
		List<GuildScienceEntity> nlist = new ArrayList<>();
		for (GuildScienceMainCfg guildScienceMainCfg : cfgs) {
			if(ids.contains(guildScienceMainCfg.getId())){
				continue;
			}
			if(XZQConstCfg.getInstance().isLimitGuildScience(guildScienceMainCfg.getId())){
				continue;
			}
			if (guildScienceMainCfg.getFloor() <= floor) {
				GuildScienceEntity entity = new GuildScienceEntity();
				entity.setGuildId(guildId);
				entity.setScienceId(guildScienceMainCfg.getId());
				long openLimitTime = this.calGuildScienceOpenLimitTime(entity.getScienceId(), entity.getLevel());
				entity.setOpenLimitTime(openLimitTime);
				if (!HawkDBManager.getInstance().create(entity)) {
					logger.error("manor create guild science failed, guildId: {}, scienceId: {}", guildId, guildScienceMainCfg.getId());
					continue;
				}
				nlist.add(entity);
			}
		}
		return nlist;
	}

	/**
	 * 计算联盟科技作用属性
	 * 
	 * @param value
	 * @return
	 */
	public void calcTechEffec(String guildId) {
		Map<Integer, Integer> effMap = new HashMap<>();
		List<GuildScienceEntity> scienceList = getGuildScienceList(guildId);
		for (GuildScienceEntity science : scienceList) {
			if (science.getLevel() == 0) {
				continue;
			}
			GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class,
					science.getLevel(), science.getScienceId());
			List<int[]> effects = cfg.getEffects();
			for (int[] effect : effects) {
				int type = effect[0];
				int val = effect[1];
				if (effMap.containsKey(type)) {
					effMap.put(type, effMap.get(type) + val);
				} else {
					effMap.put(type, val);
				}
			}
		}
		effectGuildTech.put(guildId, effMap);
	}

	/**
	 * 创建联盟
	 * 
	 * @param name
	 * @param tag
	 * @param lang
	 * @param announcement
	 * @param leaderId
	 * @param leaderName
	 * @param power
	 * @return
	 */
	public HawkTuple2<Integer, String> onCreateGuild(Player leader, GuildCreateObj creatObj) {
		String name = creatObj.getName();
		String tag = creatObj.getTag();
		String announcement = creatObj.getAnnounce();
		int error = GuildUtil.checkGuildName(name);
		if (error != Status.SysError.SUCCESS_OK_VALUE) {
			return new HawkTuple2<Integer, String>(error, null);
		}

		error = GuildUtil.checkGuildTag(tag);
		if (error != Status.SysError.SUCCESS_OK_VALUE) {
			return new HawkTuple2<Integer, String>(error, null);
		}
		
		//因金币扣除接口可能返回异常,会导致中途阻断创建联盟流程,因此此处特殊处理,在联盟线程调用消耗接口,扣除完成后再进行后续建盟流程
		if (leader.getCityLv() < GuildConstProperty.getInstance().getCreateGuildCostGoldLevel()) {
			ConsumeItems consume = creatObj.getConsume();
			if (!consume.checkConsume(leader, creatObj.getProtoType())) {
				error = Status.Error.GOLD_NOT_ENOUGH_VALUE;
				return new HawkTuple2<Integer, String>(error, null);
			}
			consume.consumeAndPush(leader, Action.GUILD_CREATE);
		}
		
		GuildInfoObject guild = new GuildInfoObject(null);
		if (guild.create(name, tag, getRandomFlag(), leader, announcement)) {
			guildData.put(guild.getId(), guild);
		} else {
			error = Status.Error.GUILD_CREATE_FAILE_VALUE;
			return new HawkTuple2<Integer, String>(error, null);
		}
		
		int joinResult = onJoinGuild(leader.getId(), guild.getId(), leader.getName(), leader.getPower(), Const.GuildAuthority.L5_VALUE);
		
		// 加入联盟失败,则本次创建联盟失败,删除已创建GuildEntity
		if (joinResult != Status.SysError.SUCCESS_OK_VALUE) {
			guildData.remove(guild.getId());
			guild.delete();
			error = Status.Error.GUILD_CREATE_FAILE_VALUE;
			return new HawkTuple2<Integer, String>(error, null);
		}
		GlobalData.getInstance().changeGuildName(null, name);
		GlobalData.getInstance().changeGuildTag(null, tag);
		guildRankObjs.put(guild.getId(), new GuildRankObj(guild.getId()));
		checkScienceOpen(guild.getId());
		onCreateGuildFinish(guild, leader);
		createBigGift(guild.getId());
		// 刷新联盟任务
		refreshGuildTask(guild);

		return new HawkTuple2<Integer, String>(Status.SysError.SUCCESS_OK_VALUE, guild.getId());
	}

	private synchronized void createBigGift(String guildId) {
		if (guildGift.containsKey(guildId)) {
			return;
		}

		GuildBigGiftEntity giftEntity = new GuildBigGiftEntity();
		giftEntity.setGuildId(guildId);
		GuildBigGift bigGift = GuildBigGift.create(giftEntity);
		giftEntity.setGiftObj(bigGift);
		bigGift.randomBigGift();
		HawkDBManager.getInstance().create(giftEntity);
		guildGift.put(guildId, bigGift);
		bigGift.notifyChanged();
	}

	/**
	 * 获得随机旗帜
	 * 
	 * @return
	 */
	private int getRandomFlag() {
		ConfigIterator<GuildFlagCfg> cfg = HawkConfigManager.getInstance().getConfigIterator(GuildFlagCfg.class);
		Random random = new Random();
		int size = 0;
		for (GuildFlagCfg o : cfg) {
			if(o.getType() == GuildFlagCfg.NORMAL){
				size++;
			}
		}
		int time = random.nextInt(size);
		int flag = 0;
		int temp = 0;
		cfg = HawkConfigManager.getInstance().getConfigIterator(GuildFlagCfg.class);
		for (GuildFlagCfg o : cfg) {
			if(o.getType() == GuildFlagCfg.REWARD){
				continue;
			}
			if (temp == time) {
				flag = o.getId();
				break;
			}
			temp++;
		}
		if(flag == 0){
			flag = 1;
		}
		return flag;
	}

	/**
	 * 创建联盟结束
	 * 
	 * @param info
	 */
	private void onCreateGuildFinish(final GuildInfoObject guild, Player leader) {
		// 初始化联盟领地, 机器人单独初始化
		if (leader.isRobot()) {
			GuildManorService.getInstance().initNewRobotGuildManor(guild.getId());
		} else {
			GuildManorService.getInstance().initNewGuildManor(guild.getId());
		}
		// 通知搜索服务
		SearchService.getInstance().addGuildInfo(guild.getName(), guild.getId());
		SearchService.getInstance().addGuildTag(guild.getTag(), guild.getId());

		HPGuildLog.Builder log = HPGuildLog.newBuilder();
		log.setLogType(GuildLogType.CREATE);
		log.setParam(guild.getLeaderName());
		log.setTime(HawkTime.getMillisecond());
		LocalRedis.getInstance().addGuildLog(guild.getId(), log);
		// 记录联盟基础信息
		if(CrossActivityService.getInstance().isOpen()){
			CGuildInfo.Builder builder = CGuildInfo.newBuilder();
			builder.setId(guild.getId());
			builder.setName(guild.getName());
			builder.setTag(guild.getTag());
			builder.setServerId(guild.getServerId());
			builder.setGuildFlag(guild.getFlagId());
			RedisProxy.getInstance().addCrossGuildInfo(builder, CrossActivityService.getInstance().getTermId());
		}
	}
	
	/**
	 * 加入联盟
	 * 
	 * @param playerId
	 * @param guildId
	 */
	private int onJoinGuild(final String playerId, final String guildId, final String playerName, long power, final int authority) {
		int error = Status.SysError.SUCCESS_OK_VALUE;
		GuildMemberObject member = playerGuild.get(playerId);
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		long noArmyPower = player != null ? player.getNoArmyPower() : 0;
		if (member != null) {
			if (!member.joinGuild(guildId, playerName, power, noArmyPower, authority)) {
				return Status.Error.GUILD_ALREADYJOIN_VALUE;
			}
		} else {
			member = new GuildMemberObject(null);
			if (member.create(playerId, guildId, playerName, authority, power, noArmyPower)) {
				playerGuild.put(playerId, member);
			} else {
				return Status.Error.GUILD_JOIN_FAILE_VALUE;
			}
		}
		if (authority == Const.GuildAuthority.L5_VALUE) {
			guildMemberData.put(guildId, new ConcurrentHashSet<String>());
			member.updateOfficeId(GuildOffice.LEADER.value());
		}
		guildMemberData.get(guildId).add(playerId);
		if (player != null && player.isActiveOnline()) {
			// 同步联盟科技作用号信息
			syncGuildTechEffect(guildId, player);
			// 刷新联盟领地相关作用号
			player.getEffect().initEffectManor();
			player.getPush().syncPlayerEffect(AssembleDataManager.getInstance().getManorEffectTypes());
			// 刷新联盟帮助
			refreshGuildHelpNum(guildId, null, null);
			// 刷超级武器作用号
			player.getEffect().resetEffectSuperWeapon(player);
			memberDailyLoginCheck(playerId, guildId);
			// 同步联盟任务信息
			syncPlayerGuildTaskInfo(player);
		}
		// 更新跨服基础数据
		CrossActivityService.getInstance().updatePlayerInfo(player, player.getGuildId());

		GlobalData.getInstance().removeNoGuildPlayer(playerId);
		WorldPlayerService.getInstance().noticeAllianceChange(playerId);
		// 发送邮件---加入联盟
		// 异步发送邮件,记录日志,推送消息
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				removeAllGuildApply(playerId);
				if (authority != GuildAuthority.L5_VALUE) {
					int icon = getGuildFlag(guildId);
					String guildName = getGuildName(guildId);
					GuildMailService.getInstance().sendMail(MailParames.newBuilder()
							.setPlayerId(playerId)
							.setMailId(MailId.JOIN_GUILD)
							.addSubTitles(guildName)
							.addContents(GameUtil.guild4MailContents(guildId))
							.setIcon(icon)
							.build());
					HPGuildLog.Builder log = HPGuildLog.newBuilder();
					log.setLogType(GuildLogType.JOIN);
					log.setParam(playerName);
					log.setTime(HawkTime.getMillisecond());
					LocalRedis.getInstance().addGuildLog(guildId, log);
					LogUtil.logGuildAction(GuildAction.GUILD_MEMBER_JOIN, guildId);
				}
				return null;
			}
		});

		HawkApp.getInstance().postMsg(player.getXid(), GuildJoinMsg.valueOf(guildId));

		joinGuildScoreBatch(player, guildId, authority);

		return error;
	}
	
	/**
	 * 机器人加入联盟
	 * 
	 * @param playerId
	 * @param guildId
	 * @param playerName
	 * @param power
	 * @param authority
	 * @return
	 */
	public int onRobotJoinGuild(final String playerId, final String guildId, final String playerName, long power, final int authority) {
		AccountRoleInfo roleInfo = GlobalData.getInstance().getAccountRoleInfo(playerId);
		if (roleInfo == null || !roleInfo.getOpenId().startsWith("robot")) {
			return -1;
		}
		
		return onJoinGuild(playerId, guildId, playerName, power, authority);
	}

	/**
	 * 加入联盟相关手Q成就上报
	 * 
	 * @param player
	 * @param guildId
	 * @param isCreate
	 */
	private void joinGuildScoreBatch(Player player, String guildId, int authority) {
		if (GameUtil.isScoreBatchEnable(player) && !player.isActiveOnline()) {
			int[] keys = { ScoreType.GUILD_ID.intValue(), ScoreType.GUILD_NAME.intValue(),
					ScoreType.GUILD_MEMBER_CHANGE.intValue(), ScoreType.GUILD_MEMBER_LEVEL_CHANGE.intValue() };
			String[] playerIds = { player.getId() }, vals = { player.getGuildId(), player.getGuildName(), "1", String.valueOf(authority) };
			LocalRedis.getInstance().addScoreBatchFlag(playerIds, keys, vals);
		} else {
			GameUtil.scoreBatch(player, ScoreType.GUILD_ID, player.getGuildId()); // 上报公会ID
			GameUtil.scoreBatch(player, ScoreType.GUILD_NAME, player.getGuildName()); // 上报公会名称
			GameUtil.scoreBatch(player, ScoreType.GUILD_MEMBER_CHANGE, 1); // 加入公会上报
			GameUtil.scoreBatch(player, ScoreType.GUILD_MEMBER_LEVEL_CHANGE, authority); // 公会成员身份上报
		}

		Set<String> guildMembers = guildMemberData.get(guildId);
		if (GsConfig.getInstance().isScoreBatchEnable() && guildMembers != null) {
			int count = guildMembers.size();
			int[] keys = { ScoreType.GUILD_MEMBER_COUNT.intValue() };
			String[] vals = { String.valueOf(count) };
			List<String> playerIds = new ArrayList<>();

			for (String playerId : guildMembers) {
				Player member = GlobalData.getInstance().makesurePlayer(playerId);
				if (member == null) {
					continue;
				}
				
				if (GameUtil.isScoreBatchEnable(member) && !member.isActiveOnline()) {
					playerIds.add(playerId);
				} else {
					GameUtil.scoreBatch(member, ScoreType.GUILD_MEMBER_COUNT, count);
				}
			}

			if (!playerIds.isEmpty()) {
				LocalRedis.getInstance().addScoreBatchFlag(playerIds.toArray(new String[playerIds.size()]), keys, vals);
			}
		}

	}

	/**
	 * 移除玩家其他联盟申请
	 * 
	 * @param playerId
	 */
	private void removeAllGuildApply(String playerId) {
		Set<String> guilds = LocalRedis.getInstance().getPlayerGuildApply(playerId);
		for (String guildId : guilds) {
			LocalRedis.getInstance().removeGuildPlayerApply(guildId, playerId);
			pushApplayNum(guildId);
		}
		LocalRedis.getInstance().removeAllPlayerGuildApply(playerId);
	}

	/**
	 * 修改联盟名称
	 */
	public int onChangeGuildName(String name, String guildId) {
		int errorCode = GuildUtil.checkGuildName(name);
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			GuildInfoObject guild = guildData.get(guildId);
			String oriName = guild.getName();
			if (!guild.updateGuildName(oriName, name)) {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}

			// 如果是国王所在的联盟, 更新王国信息
			if (PresidentFightService.getInstance().isPresidentGuild(guildId)) {
				PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
			}

			for (String playerId : guildMemberData.get(guildId)) {
				WorldPlayerService.getInstance().noticeAllianceChange(playerId);
			}

			// 领地堡垒广播
			GuildManorService.getInstance().broadcastManorPonit(guildId);

			Set<String> guildMembers = guildMemberData.get(guildId);
			if (GsConfig.getInstance().isScoreBatchEnable() && guildMembers != null) {
				int[] keys = { ScoreType.GUILD_NAME.intValue() };
				String[] vals = { name };
				List<String> playerIds = new ArrayList<>();

				for (String playerId : guildMembers) {
					Player player = GlobalData.getInstance().makesurePlayer(playerId);
					if (player == null) {
						continue;
					}
					
					if (GameUtil.isScoreBatchEnable(player) && !player.isActiveOnline()) {
						playerIds.add(playerId);
					} else {
						GameUtil.scoreBatch(player, ScoreType.GUILD_NAME, name);
					}
				}

				if (!playerIds.isEmpty()) {
					LocalRedis.getInstance().addScoreBatchFlag(playerIds.toArray(new String[playerIds.size()]), keys, vals);
				}
			}
		}

		return errorCode;
	}

	/**
	 * 修改联盟简称
	 * 
	 * @param player
	 * @param tag
	 * @return
	 */
	public int onChangeGuildTag(String tag, String guildId) {
		int errorCode = GuildUtil.checkGuildTag(tag);
		if (errorCode == Status.SysError.SUCCESS_OK_VALUE) {
			GuildInfoObject guild = guildData.get(guildId);
			String oriTag = guild.getTag();
			if (!guild.updateGuildTag(oriTag, tag)) {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}

			for (String playerId : guildMemberData.get(guildId)) {
				WorldPlayerService.getInstance().noticeAllianceChange(playerId);
			}

			// 国王所在盟的变更广播
			if (PresidentFightService.getInstance().isPresidentGuild(guildId)) {
				PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
			}
			// 领地堡垒广播
			GuildManorService.getInstance().broadcastManorPonit(guildId);
		}

		return errorCode;
	}

	/**
	 * 修改联盟旗帜
	 * 
	 * @param player
	 * @param flag
	 * @return
	 */
	public int onChangeGuildFlag(int flag, String guildId) {
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildFlag(flag);

		for (String playerId : guildMemberData.get(guildId)) {
			WorldPlayerService.getInstance().noticeAllianceChange(playerId);
		}

		// 国王所在盟的变更广播
		if (PresidentFightService.getInstance().isPresidentGuild(guildId)) {
			PresidentFightService.getInstance().getPresidentCity().broadcastPresidentInfo(null);
		}

		return errorCode;
	}

	/**
	 * 修改联盟语言
	 * 
	 * @param lang
	 * @param guildId
	 * @return
	 */
	public int onChangeGuildLanguage(String lang, String guildId) {
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildLanguage(lang);

		return errorCode;
	}

	/**
	 * 改变联盟申请权限
	 * 
	 * @param guildId
	 * @param req
	 * @return
	 */
	public int onChangeGuildApplyPermition(String guildId, boolean isOpen, int buildingLvl, int power, int commonderLvl, String lang) {
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildPermiton(isOpen, buildingLvl, power, commonderLvl, lang);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 改变联盟等级名字
	 * 
	 * @param guildId
	 * @param info
	 * @return
	 */
	public int onChangeLevelName(String guildId, String[] names) {
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildLevelName(names);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	public int getEffectGuildOfficer(String playerId, int effId) {
		int officeVal = 0;
		GuildMemberObject member = getGuildMemberObject(playerId);
		if (member != null && member.getOfficeId() != GuildOffice.NONE.value()) {
			AllianceOfficialCfg officialCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, member.getOfficeId());
			List<int[]> effects = officialCfg.getEffects();
			for (int[] effect : effects) {
				if (effect[0] == effId) {
					officeVal += effect[1];
				}
			}
		}
		
		return officeVal;
	}
	/**
	 * 获取联盟作用号
	 * 
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int getEffectGuild(String playerId, int effId) {
		if (!GsApp.getInstance().isInitOK()) {
			return 0;
		}
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		
		String guildId = null;
		GuildMemberObject guildMemberObj = playerGuild.get(playerId);
		if (guildMemberObj == null) {
			guildId = cPlayerGuild.get(playerId);
		} else {
			guildId = guildMemberObj.getGuildId();
		}

		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}

		int techVal = getEffectGuildTech(guildId, effId);

		// 联盟官员作用号
		int officeVal = getEffectGuildOfficer(playerId, effId);

		// 在跨服中读取工会的联盟科技.
		int crossEffect = getCsEffectGuildTech(guildId, effId);
		int effectVal = techVal + officeVal + crossEffect;
		return effectVal;
	}

	/**
	 * 
	 * 申请加入联盟
	 */
	public HawkTuple2<Integer, Boolean> onApplyGuild(GuildApplyInfo.Builder info, String guildId, boolean needPermition) {
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null || !guildMemberData.containsKey(guildId)) {
			return new HawkTuple2<Integer, Boolean>(Status.Error.GUILD_NOT_EXIST_VALUE, false);
		}
		if (guildMemberData.get(guildId).size() >= getGuildMemberMaxNum(guildId)) {
			return new HawkTuple2<Integer, Boolean>(Status.Error.GUILD_FULL_VALUE, false);
		}
		
		// 星球大战参与联盟特定阶段不能进行人员操作
		if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
			return new HawkTuple2<Integer, Boolean>(Status.Error.SW_GUILD_OPS_LIMIT_VALUE, false);
		}
		
		if (needPermition && guild.isNeedPermition()) {
			if (info.getBuildingLevel() >= guild.getNeedBuildingLevel() && info.getPower() >= guild.getNeedPower()) {
				addGuildApplyInfo(info, guildId);
				return new HawkTuple2<Integer, Boolean>(Status.SysError.SUCCESS_OK_VALUE, false);
			} else {
				return new HawkTuple2<Integer, Boolean>(Status.Error.GUILD_APPLY_NOTMATCH_VALUE, false);
			}
		}
		int result = onJoinGuild(info.getPlayerId(), guildId, info.getPlayerName(), info.getPower(), Const.GuildAuthority.L1_VALUE);
		return new HawkTuple2<Integer, Boolean>(result, true);
	}
	
	/***
	 * 无盟玩家响应联盟推荐
	 * @param info
	 * @param guildId
	 * @param needPermition
	 * @return <操作码, 寻找到的联盟ID>
	 */
	public HawkTuple2<Integer, String> onApplyGuildRecommend(GuildApplyInfo.Builder info, String guildId, boolean needPermition){
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null || !guildMemberData.containsKey(guildId)) {
			return new HawkTuple2<Integer, String>(Status.Error.GUILD_NOT_EXIST_VALUE, null);
		}
		if (needPermition && guild.isNeedPermition()) {
			if (info.getBuildingLevel() >= guild.getNeedBuildingLevel() && info.getPower() >= guild.getNeedPower()) {
				addGuildApplyInfo(info, guildId);
				return new HawkTuple2<Integer, String>(Status.SysError.SUCCESS_OK_VALUE, null);
			} else {
				return new HawkTuple2<Integer, String>(Status.Error.GUILD_APPLY_NOTMATCH_VALUE, null);
			}
		}
		
		if (guildMemberData.get(guildId).size() >= getGuildMemberMaxNum(guildId)) {
			List<GuildInfoObject> guildList = new ArrayList<>(guildData.values());
			Collections.shuffle(guildList);
			Optional<GuildInfoObject> opGuild = noGuildMemberGuildChoise(guildList);
			if(!opGuild.isPresent()){
				return new HawkTuple2<Integer, String>(Status.Error.GUILD_FULL_AND_CANNOT_ENTER_VALUE, null);
			}else{
				// 星球大战参与联盟特定阶段不能进行人员操作
				if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
					return new HawkTuple2<Integer, String>(Status.Error.SW_GUILD_OPS_LIMIT_VALUE, null);
				}
				String findGuildId = opGuild.get().getId();
				int result = onJoinGuild(info.getPlayerId(), findGuildId, info.getPlayerName(), info.getPower(), Const.GuildAuthority.L1_VALUE);
				return new HawkTuple2<Integer, String>(result, findGuildId);
			}
		}
		int result = onJoinGuild(info.getPlayerId(), guildId, info.getPlayerName(), info.getPower(), Const.GuildAuthority.L1_VALUE);
		return new HawkTuple2<Integer, String>(result, guildId);
	}

	/**
	 * 一键入盟
	 * 
	 * @param player
	 * @return
	 */
	public HawkTuple2<Integer, String> onQuickJoinGuild(Player player) {
		String joinGuildId = "";
		for (GuildInfoObject guild : guildData.values()) {
			if (guild.isNeedPermition()) {
				continue;
			}
			String guildId = guild.getId();
			if (guildMemberData.get(guildId).size() >= getGuildMemberMaxNum(guildId)) {
				continue;
			}
			// 星球大战参与联盟特定阶段不能进行人员操作
			if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
				continue;
			}
			joinGuildId = guildId;
			break;
		}
		if (!HawkOSOperator.isEmptyString(joinGuildId)) {
			int result = onJoinGuild(player.getId(), joinGuildId, player.getName(), player.getPower(), Const.GuildAuthority.L1_VALUE);
			return new HawkTuple2<Integer, String>(result, joinGuildId);
		}
		return new HawkTuple2<Integer, String>(Status.Error.GUILD_NOT_SUIT_VALUE, "");
	}

	/**
	 * 添加联盟申请信息
	 * 
	 * @param info
	 * @param guildId
	 */
	private void addGuildApplyInfo(GuildApplyInfo.Builder info, String guildId) {
		LocalRedis.getInstance().addGuildPlayerApply(guildId, info);
		pushApplayNum(guildId);
	}

	/**
	 * 推送联盟成员消息
	 * 
	 * @param guildId
	 * @param protocol
	 */
	public void broadcastProtocol(String guildId, HawkProtocol protocol) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		if (guildMemberData.containsKey(guildId)) {
			for (String playerId : guildMemberData.get(guildId)) {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player != null && player.isActiveOnline()) {
					player.sendProtocol(protocol);
				}
			}
		}
		
		if (cGuildMemberData.containsKey(guildId)) {
			for (String playerId : cGuildMemberData.get(guildId)) {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player != null && player.isActiveOnline()) {
					player.sendProtocol(protocol);
				}
			}
		}
	}

	/**
	 * 同步推送联盟成员联盟信息
	 * 
	 * @param guildId
	 */
	public void broadcastGuildInfo(String guildId) {
		if (guildMemberData.containsKey(guildId)) {
			for (String playerId : guildMemberData.get(guildId)) {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player != null && player.isActiveOnline()) {
					player.getPush().syncGuildInfo();
				}
			}
		}
	}

	/**
	 * 同意联盟申请
	 * 
	 * @param player
	 * @param playerId
	 * @return
	 */
	public int onAcceptGuildApply(String guildId, String playerId) {
		GuildInfoObject entity = guildData.get(guildId);
		if (!entity.containApply(playerId)) {
			return Status.Error.GUILD_APPLY_NOTEXIST_VALUE;
		}

		if (guildMemberData.get(guildId).size() >= getGuildMemberMaxNum(guildId)) {
			return Status.Error.GUILD_FULL_VALUE;
		}

		LocalRedis.getInstance().removeGuildPlayerApply(guildId, playerId);
		pushApplayNum(guildId);
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		
		
		if (targetPlayer != null) {
			// 对方是跨服玩家
			if (CrossService.getInstance().isCrossPlayer(playerId)) {
				return Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE;
			}
			
			// 星球大战参与联盟特定阶段不能进行人员操作
			if (!StarWarsActivityService.getInstance().checkGuildMemberOps(guildId)) {
				return Status.Error.SW_GUILD_OPS_LIMIT_VALUE;
			}
			
			if (targetPlayer.hasGuild()) {
				return Status.Error.GUILD_ALREADYJOIN_VALUE;
			} else {
				int result = onJoinGuild(playerId, guildId, targetPlayer.getName(), targetPlayer.getPower(), Const.GuildAuthority.L1_VALUE);
				if (result != Status.SysError.SUCCESS_OK_VALUE) {
					return result;
				}
				HawkApp.getInstance().postMsg(targetPlayer.getXid(), AcceptGuildApplyMsg.valueOf(entity.getId()));
				return Status.SysError.SUCCESS_OK_VALUE;
			}
		} else {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
	}

	/**
	 * 接受邀请
	 * 
	 * @param player
	 * @param guildId
	 * @return
	 */
	public int onAcceptInvite(String playerId, String playerName, long power, String guildId) {
		GuildApplyInfo.Builder info = GuildApplyInfo.newBuilder();
		info.setPlayerId(playerId);
		info.setPlayerName(playerName);
		info.setPower(power);
		HawkTuple2<Integer, Boolean> result = onApplyGuild(info, guildId, false);
		return result.first;
	}

	/**
	 * 发表联盟宣言
	 * 
	 * @param player
	 * @param announcement
	 * @return
	 */
	public int onPostGuildAnnouncement(String announcement, String guildId) {
		announcement = GuildUtil.filterEmoji(announcement);
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildAnnouncement(announcement);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 发表联盟通告
	 * 
	 * @param player
	 * @param notice
	 * @return
	 */
	public int onPostGuildNotice(String notice, String guildId) {
		notice = GuildUtil.filterEmoji(notice);
		GuildInfoObject guild = guildData.get(guildId);
		int errorCode = Status.SysError.SUCCESS_OK_VALUE;
		if (guild == null) {
			errorCode = Status.Error.GUILD_NOT_EXIST_VALUE;
			return errorCode;
		}
		guild.updateGuildNotice(notice);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 发表联盟留言
	 * 
	 * @param player
	 * @param message
	 * @return
	 */
	public int onPostGuildMessage(String guildId, GuildBBSMessage.Builder info) {
		LocalRedis.getInstance().addGuildBBS(guildId, info);
		for (String playerId : guildMemberData.get(guildId)) {
			int cnt = LocalRedis.getInstance().getGuildNum(playerId, "MESSAGE");
			if (cnt == 20) {
				continue;
			}
			LocalRedis.getInstance().setGuildNum(playerId, "MESSAGE", cnt + 1);
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 修改联盟成员权限
	 * 
	 * @param player
	 * @param playerId
	 * @return
	 */
	public HawkTuple2<Integer, Integer> onChangeGuildMemLevel(String guildId, String sourceId, String targetId,
			int level) {

		GuildMemberObject source = playerGuild.get(sourceId);
		GuildMemberObject target = playerGuild.get(targetId);
		int oldAuth = target.getAuthority();
		if (!source.getGuildId().equals(target.getGuildId())) {
			return new HawkTuple2<Integer, Integer>(Status.Error.GUILD_NOT_MEMBER_VALUE, 0);
		}
		
		if (!canChangeAuth(source.getAuthority(), target.getAuthority(), level)) {
			return new HawkTuple2<Integer, Integer>(Status.Error.GUILD_LOW_AUTHORITY_VALUE, 0);
		}
		target.updateMemberAuthority(level);
		
		HawkTuple2<Integer, Integer> result = new HawkTuple2<Integer, Integer>(Status.SysError.SUCCESS_OK_VALUE, oldAuth);

		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if (GameUtil.isScoreBatchEnable(targetPlayer) && !targetPlayer.isActiveOnline()) {
			LocalRedis.getInstance().addScoreBatchFlag(targetId, ScoreType.GUILD_MEMBER_LEVEL_CHANGE.intValue(), String.valueOf(level));
		} else {
			GameUtil.scoreBatch(targetPlayer, ScoreType.GUILD_MEMBER_LEVEL_CHANGE, level);
		}

		return result;
	}

	
	/**
	 * 踢出联盟成员
	 * 
	 * @param player
	 * @param playerId
	 * @return
	 */
	public int onKickMember(String guildId, Player player, String targetId) {
		String sourceId = player.getId();
		GuildMemberObject source = playerGuild.get(sourceId);
		GuildMemberObject target = playerGuild.get(targetId);
		if (target == null) {
			return Status.Error.GUILD_NOT_MEMBER_VALUE;
		}
		if (!Objects.equals(target.getGuildId(), guildId)) {
			return Status.Error.GUILD_NOT_MEMBER_VALUE;
		}
		
		// 只能踢出1-4阶成员,且己方阶级须高于被踢者阶级
		if (target.getAuthority() > GuildAuthority.L4_VALUE || target.getAuthority() < GuildAuthority.L1_VALUE
				|| source.getAuthority() <= target.getAuthority()) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if (targetPlayer != null && guildId.equals(targetPlayer.getGuildId())) {
			removeGuildMemberEntity(target);
			HawkApp.getInstance().postMsg(targetPlayer.getXid(), GuildQuitMsg.valueOf(true, guildId));

			ActivityManager.getInstance().postEvent(new GuildQuiteEvent(targetPlayer.getId(), guildId, target.getJoinGuildTime()));
			//星海激战
			XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_QUIT_GUILD, new XHJZWarQuitGuildInvoker(targetId));
			GuildTeamService.getInstance().dealMsg(MsgId.GUILD_TEAM_QUIT_GUILD, new GuildTeamQuitGuildInvoker(targetId));
			//联盟当前人数
			int guildMemberNum = getGuildMemberNum(guildId);
			MissionManager.getInstance().postMsg(targetPlayer, new EventGuildMemberChange(guildId, guildMemberNum));

			HPGuildLog.Builder log = HPGuildLog.newBuilder();
			log.setLogType(GuildLogType.KICK_OUT);
			log.setParam(targetPlayer.getName());
			log.setTime(HawkTime.getMillisecond());
			log.addParams(targetPlayer.getName()).addParams(player.getName());
			LocalRedis.getInstance().addGuildLog(guildId, log);

			return Status.SysError.SUCCESS_OK_VALUE;
		} else {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
	}

	/**
	 * 退出联盟
	 * 
	 * @param player
	 * @return
	 */
	public int onQuitGuild(String guildId, String playerId) {
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(playerId);
		GuildMemberObject entity = playerGuild.get(playerId);
		if (entity == null || HawkOSOperator.isEmptyString(entity.getGuildId())) {
			logger.error("quit guild error, playerId: {}, guildId: {}", playerId, guildId);
			return Status.Error.GUILD_PLAYER_HASNOT_GUILD_VALUE;
		}
		removeGuildMemberEntity(entity);
		// 宝藏变化
		List<StorehouseEntity> storehouseEntities = targetPlayer.getData().getStorehouseEntities();
		HawkDBEntity.batchDelete(storehouseEntities);
		storehouseEntities.clear();
		List<StorehouseHelpEntity> storehouseHelpEntities = targetPlayer.getData().getStorehouseHelpEntities();
		HawkDBEntity.batchDelete(storehouseHelpEntities);
		storehouseHelpEntities.clear();
		//死盟推荐最高次数变化
		DailyDataEntity dailyEntity = targetPlayer.getData().getDailyDataEntity();
		dailyEntity.setDeadGuildRefuseRecommendCnt(0);
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.STORE_HOUSE_UPDATE_S));
		// 回复消息
		HPGuildLog.Builder log = HPGuildLog.newBuilder();
		log.setLogType(GuildLogType.QUIT);
		log.setParam(targetPlayer.getName());
		log.setTime(HawkTime.getMillisecond());
		LocalRedis.getInstance().addGuildLog(guildId, log);
		// 联盟编队
		getGuildFormation(guildId).quitGuild(playerId);
		LogUtil.logGuildAction(GuildAction.GUILD_MEMBER_QUIT, guildId);
		HawkApp.getInstance().postMsg(targetPlayer.getXid(), GuildQuitMsg.valueOf(false, guildId));
		ActivityManager.getInstance().postEvent(new GuildQuiteEvent(playerId, guildId, entity.getJoinGuildTime()));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 转让联盟盟主
	 * 
	 * @param player
	 * @param playerId
	 * @return
	 */
	public int onDemiseLeader(String guildId, String sourceId, String targetId) {
		GuildMemberObject source = playerGuild.get(sourceId);
		GuildMemberObject target = playerGuild.get(targetId);

		if (!source.getGuildId().equals(target.getGuildId())) {
			return Status.Error.GUILD_NOT_MEMBER_VALUE;
		}

		if (target.getAuthority() == Const.GuildAuthority.L5_VALUE) {
			return Status.Error.GUILD_LEADER_CANNOT_DEMISE_VALUE;
		}

		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if (targetPlayer != null && targetPlayer.getGuildId().equals(guildId)) {
			GuildInfoObject guild = guildData.get(guildId);
			if (!guild.updateGuildLeader(guild.getLeaderId(), targetPlayer.getId(), targetPlayer.getName())) {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}

			source.updateMemberAuthority(Const.GuildAuthority.L1_VALUE);
			source.updateOfficeId(GuildOffice.NONE.value());
			
			target.updateMemberAuthority(Const.GuildAuthority.L5_VALUE);
			target.updateOfficeId(GuildOffice.LEADER.value());
			
			LocalRedis.getInstance().removeGuildOfficeApply(guildId, targetId);
			
			if (targetPlayer.isActiveOnline()) {
				targetPlayer.getPush().syncGuildInfo();
			}
		}
		
		this.notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_MEMBER, Const.GuildAuthority.L5_VALUE);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 解散联盟
	 * 
	 * @param player
	 * @return
	 */
	public int onDissmiseGuild(final String guildId, Player player) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		if (guildMemberData.containsKey(guildId) && guildMemberData.get(guildId).size() > 1) {
			return Status.Error.GUILD_MEMBERNUM_ILLEGAL_VALUE;
		}

		// 移除联盟实体对象
		removeGuildEntity(guildId);

		// 删除聊天缓存
		GlobalData.getInstance().deleteGuildMsgCache(guildId);

		// 删除联盟帮助数据
		removeAllGuildHelpInfo(guildId);

		// 联盟解散删除联盟排行
		LocalRedis.getInstance().deleteGuildRank(guildId);

		// 删除联盟商店信息及购买记录
		LocalRedis.getInstance().removeGuildShopLog(guildId);
		
		// 删除联盟商店商品信息
		RedisProxy.getInstance().removeGuildShopInfo(guildId);

		// 删除联盟标记信息
		LocalRedis.getInstance().removeAllGuildSign(guildId);
		
		// 删除所有联盟官员申请
		LocalRedis.getInstance().removeGuildOfficeApply(guildId);
		
		// 删除联盟成员登录信息
		LocalRedis.getInstance().removeGuildLogin(guildId);
		
		
		// 删除联盟成员分享信息
		LocalRedis.getInstance().removeGuildShare(guildId);
		
		// 删除联盟任务
		LocalRedis.getInstance().removeGuildTask(guildId);

		// 删除联盟相关行军
		WorldMarchService.getInstance().removeGuildAllMarch(guildId);

		// 尤里复仇活动相关处理
		YuriRevengeService.getInstance().onDismissGuild(guildId);


		//星海激战
		XHJZWarService.getInstance().dealMsg(MsgId.XHJZ_DISMISS_GUILD, new XHJZWarDismissGuildInvoker(guildId));

		GuildTeamService.getInstance().dealMsg(MsgId.GUILD_TEAM_DISMISS_GUILD, new GuildTeamDismissGuildInvoker(guildId));

		// 泰伯利亚之战相关处理
		TiberiumWarService.getInstance().onGuildDismiss(guildId);
		
		// 赛博之战相关处理
		CyborgWarService.getInstance().onGuildDismiss(guildId);
		
		// 联盟锦标赛相关处理
		ChampionshipService.getInstance().onGuildDismiss(guildId);
		
		// 移除联盟领地
		GuildManorService.getInstance().dealMsg(MsgId.REMOVE_MANOR_ON_GUILD_DISSMISE, new GuildDissmiseRemoveManorMsgInvoker(guildId, player));

		// 移除联盟大礼包
		GuildBigGift bigGift = guildGift.remove(guildId);
		if (Objects.nonNull(bigGift)) {
			bigGift.getEntity().delete();
		}
		
		// 联盟战旗
		WarFlagService.getInstance().onDismissGuild(guildId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 取代联盟盟主 
	 * 
	 * @param player
	 * @return
	 */
	public int onReplaceLeader(String guildId, String playerId, String playerName) {
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NOT_EXIST_VALUE;
		}
		GuildMemberObject source = playerGuild.get(playerId);
		if(source == null){
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		GuildMemberObject target = playerGuild.get(guild.getLeaderId());
		int checkResult = GuildService.getInstance().canImpeachLeader(guild, source);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			return checkResult;
		}

		String oldLeaderName = guild.getLeaderName();
		guild.updateGuildLeader(guild.getLeaderId(), playerId, playerName);
		source.updateMemberAuthority(GuildAuthority.L5_VALUE);
		source.updateOfficeId(GuildOffice.LEADER.value());
		guild.updateGuildLeaderOfflineTime();
		boolean need = GameConstCfg.getInstance().guildLeaderCheck() && target == null;
		if (!need) {
			oldLeaderName = target.getPlayerName();
			target.updateMemberAuthority(GuildAuthority.L4_VALUE);
			target.updateOfficeId(GuildOffice.NONE.value());
		}

		// 移除该玩家的官员申请信息
		LocalRedis.getInstance().removeGuildOfficeApply(guildId, playerId);

		// 同步联盟信息
		broadcastGuildInfo(guildId);

		// 发送盟主取代邮件
		GuildMailService.getInstance().sendGuildMail(guildId, MailParames.newBuilder()
						.setMailId(MailId.GUILD_MEM_IMPEACH_LEADER)
						.addSubTitles(oldLeaderName, source.getPlayerName())
						.addContents(oldLeaderName, source.getPlayerName())
						.setIcon(getGuildFlag(guildId)));
		// 记录打点日志
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		LogUtil.logGuildFlow(player, GuildOperType.GUILD_IMPEACHMENT, guildId, playerId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 目标玩家是否为联盟成员
	 * 
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public boolean isPlayerInGuild(String guildId, String playerId) {
		if (HawkOSOperator.isEmptyString(guildId) || HawkOSOperator.isEmptyString(playerId)) {
			return false;
		}
		String playerGuildId = getPlayerGuildId(playerId);
		return guildId.equals(playerGuildId);
	}

	/**
	 * 玩家在同一个盟的判断
	 * 
	 * @param playerIds
	 * @return
	 */
	public boolean isInTheSameGuild(String... playerIds) {
		String guildId = null;
		for (String playerId : playerIds) {
			String currGuildId = GuildService.getInstance().getPlayerGuildId(playerId);
			if (HawkOSOperator.isEmptyString(currGuildId)) {
				currGuildId = UUID.randomUUID().toString();
			}

			if (guildId == null) {
				guildId = currGuildId;
			}

			if (!guildId.equals(currGuildId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 移除联盟成员信息
	 * 
	 * @param entity
	 */
	private void removeGuildMemberEntity(GuildMemberObject entity) {
		// 刷新联盟建筑
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(entity.getPlayerId());
		GameUtil.changeBuildingStatus(targetPlayer, BuildingType.EMBASSY_VALUE, BuildingStatus.COMMON);
		// 移除联盟行军
		WorldMarchService.getInstance().doQuitGuild(entity.getPlayerId(), entity.getGuildId());
		// 移除联盟领地相关
		GuildManorService.getInstance().doQuitGuild(entity.getPlayerId(), entity.getGuildId());
		// 尤里复仇相关处理
		YuriRevengeService.getInstance().onQuitGuild(entity.getPlayerId(), entity.getGuildId());
		// 移除联盟帮助信息
		removeMemberGuildHelp(targetPlayer, entity.getGuildId());
		// 移除联盟礼物信息
		LocalRedis.getInstance().removeGuildGiftLog(entity.getPlayerId());
		// 移除联盟官员申请信息
		LocalRedis.getInstance().removeGuildOfficeApply(entity.getGuildId(), entity.getPlayerId());
		// 移除捐献排行相关信息
		if (guildRankObjs.containsKey(entity.getGuildId())) {
			guildRankObjs.get(entity.getGuildId()).onMemberRemove(entity.getPlayerId());
		}
		//更新联盟语音数量
		VoiceRoomManager.getInstance().onPlayerQuit(entity.getPlayerId());
		String guildId = entity.getGuildId();
		Set<String> memberSet = guildMemberData.get(guildId);
		if (memberSet != null) {
			memberSet.remove(entity.getPlayerId());
			if (memberSet.size() == 0) {
				guildMemberData.remove(guildId);
			}
		}
		entity.quitGuild();
		BlockingQueue<IWorldMarch> marchs = WorldMarchService.getInstance().getPlayerMarch(targetPlayer.getId());
		for (IWorldMarch march : marchs) {
			march.updateMarch();
		}
		// 更新跨服基础数据
		CrossActivityService.getInstance().updatePlayerInfo(targetPlayer, targetPlayer.getGuildId());
		
	}

	/**
	 * 根据guildId获取 GuildInfoObject
	 * 
	 * @param guildId
	 * @return
	 */
	public GuildInfoObject getGuildInfoObject(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		return guildData.get(guildId);
	}

	/**
	 * 根据playerId获取GuildMemberObject
	 * 
	 * @param playerId
	 * @return
	 */
	public GuildMemberObject getGuildMemberObject(String playerId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return null;
		}
		if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
			return null;
		}
		return playerGuild.get(playerId);
	}

	/**
	 * 获取成员官职
	 * @param playerId
	 * @return
	 */
	public int getGuildMemberOfficer(String playerId) {
		GuildMemberObject member = getGuildMemberObject(playerId);
		if (member == null) {
			return 0;
		}
		return member.getOfficeId();
	}
	
	/**
	 * 获得玩家联盟简称
	 * 
	 * @param playerId
	 * @return
	 */
	public String getPlayerGuildTag(String playerId) {
		GuildMemberObject member = playerGuild.get(playerId);
		if (member == null) {
			return null;
		}
		if (HawkOSOperator.isEmptyString(member.getGuildId())) {
			return null;
		}
		return getGuildTag(member.getGuildId());
	}

	/**
	 * 获得玩家联盟Id
	 * 
	 * @param playerId
	 * @return
	 */
	public String getPlayerGuildId(String playerId) {
		if (playerGuild != null && playerGuild.containsKey(playerId)) {
			return playerGuild.get(playerId).getGuildId();
		}
		if (cPlayerGuild != null && cPlayerGuild.containsKey(playerId)) {
			return cPlayerGuild.get(playerId);
		}
		return null;
	}

	/**
	 * 移除联盟
	 * 
	 * @param guildId
	 */
	private void removeGuildEntity(String guildId) {
		if (guildData.containsKey(guildId)) {
			GuildInfoObject guild = guildData.get(guildId);
			LocalRedis.getInstance().removeGuildLog(guildId);
			SearchService.getInstance().removeGuildInfo(guild.getName());
			SearchService.getInstance().removeGuildTag(guild.getTag());

			// 移除联盟捐献排行信息
			guildRankObjs.get(guildId).onGuildDismiss();
			guildRankObjs.remove(guildId);

			// 移除科技相关信息
			guildScienceFloor.remove(guildId);
			researchData.remove(guildId);
			if (guildScienceData.containsKey(guildId)) {
				for (GuildScienceEntity science : guildScienceData.get(guildId)) {
					science.delete();
				}
				guildScienceData.remove(guildId);
			}
			effectGuildTech.remove(guildId);
			syncGuildTechEffect(guildId);

			guildData.remove(guildId);
			GlobalData.getInstance().onDismissGuild(guild.getName(), guild.getTag());
			if (!guild.isInvalid()) {
				guild.delete();
			}
		}

		if (guildMemberData.containsKey(guildId)) {
			for (String playerId : guildMemberData.get(guildId)) {
				removeGuildMemberEntity(playerGuild.get(playerId));
			}
		}
	}

	/**
	 * 查看联盟帮助队列
	 * 
	 * @param guildId
	 *            : 联盟ID
	 * @param playerId
	 *            : 查看者ID
	 */

	public List<GuildHelpQueue> getGuildHelpQueues(String guildId, Player player) {
		String playerId = player.getId();
		List<GuildHelpQueue> list = new ArrayList<GuildHelpQueue>();

		// 获取申请帮助信息
		Map<String, GuildHelpInfo> queueInfos = getAllGuildHelpInfo(guildId);
		if (queueInfos == null || queueInfos.size() == 0) {
			return list;
		}

		for (Entry<String, GuildHelpInfo> e : queueInfos.entrySet()) {
			String queueId = e.getKey();
			GuildHelpInfo queue = e.getValue();

			String pid = queue.getPlayerId();
			int count = queue.getCount();
			List<String> helpers = queue.getHelpers();
			if (pid.equals(playerId)) {
				QueueEntity queueEntity = player.getData().getQueueEntity(queueId);
				if(Objects.isNull(queueEntity)){
					deleteGuildHelpInfo(guildId, queueId);
					continue;
				}
				int queueType = queue.getQueueType().getNumber();
				if (queueEntity.getQueueType() != queueType) {
					deleteGuildHelpInfo(guildId, queueId);
					HawkLog.errPrintln(" del guild help failed, playerId: {}, queueId: {}, queueType: {}", playerId, queueId, queueType);
					continue;
				}
			} else {
				// 非自己的帮助队列已完成
				if (queue.getCurHelpCount() >= count) {
					continue;
				}
			}

			// 已经帮助过该队列
			if (helpers.contains(playerId)) {
				continue;
			}

			Player snapshot = GlobalData.getInstance().makesurePlayer(pid);
			// 理论上不该为null,添加打印
			if(snapshot == null){
				// 删除异常帮助数据
				deleteGuildHelpInfo(guildId, queueId);
				HawkLog.logPrintln("getGuildHelpQueues targetPlayer not exist, targetId: {}, queueId: {}", pid, queueId);
				continue;
			}
			
			// 跳过跨服玩家的信息
			if (CrossService.getInstance().isCrossPlayer(pid)) {
				HawkLog.logPrintln("getGuildHelpQueues targetPlayer isCrosser, targetId: {}, queueId: {}", pid, queueId);
				continue;
			}
			
			GuildHelpQueue.Builder builder = GuildHelpQueue.newBuilder();
			if (playerId.equals(pid)) {
				builder.setTotalDisTime((int) queue.getTotalDisTime());
			}
			builder.setQueueId(queueId);
			builder.setApplyId(pid);
			builder.setApplyName(snapshot.getName());
			builder.setApplyIcon(queue.getPlayerIcon());
			if (!HawkOSOperator.isEmptyString(snapshot.getPfIcon())) {
				builder.setPfIcon(snapshot.getPfIcon());
			}
			builder.setCurCount(queue.getCurHelpCount());
			builder.setTotalCount(count);
			builder.setQueueType(queue.getQueueType().getNumber());
			builder.setItemId(queue.getItemId());
			builder.setQueueStatus(queue.getQueueStatus().getNumber());
			builder.addAllParams(queue.getParames());
			list.add(builder.build());
		}

		return list;
	}

	/**
	 * 申请联盟帮助
	 *
	 * @param guildId
	 *            : 联盟ID
	 * @param playerId
	 *            : 申请者ID
	 * @param queueId
	 *            : 队列ID
	 * @param count
	 *            : 申请帮助次数
	 */
	public boolean applyGuildHelp(String guildId, Player player, QueueEntity queue, String... parames) {
		// 获取联盟大厦
		BuildingCfg conf = player.getData().getBuildingCfgByType(BuildingType.EMBASSY);
		if (conf == null) {
			return false;
		}
		// 最大帮助次数
		int helpCnt = conf.getAssistLimit();
		helpCnt += player.getData().getEffVal(EffType.GUILD_HELP_NUM);
		helpCnt += player.getData().getEffVal(EffType.GUILD_HELP_CNT_ADD);

		// 计算队列总时间（毫秒）和加成（百分比）
		long buildTime = queue.getEndTime() - queue.getStartTime();
		int itemCfgId = 0;
		int multiply = 1;
		switch (queue.getQueueType()) {

		case QueueType.BUILDING_QUEUE_VALUE:
			// 建筑队列
			multiply = queue.getMultiply();
			itemCfgId = player.getData().getBuildingEntityIgnoreStatus(queue.getItemId()).getBuildingCfgId();
			BuildingCfg curBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, itemCfgId);
			//一键升X级
			if(multiply >1){
				for(int i=1;i<= multiply;i++){
					int nextId = curBuildCfg.getPostStage();
					curBuildCfg = HawkConfigManager.getInstance().getConfigByKey(BuildingCfg.class, nextId);
					itemCfgId = nextId;
				}
				helpCnt *= multiply;
			}
			break;

		case QueueType.BUILDING_DEFENER_VALUE:
			// 防御建筑队列
			itemCfgId = player.getData().getBuildingBaseEntity(queue.getItemId()).getBuildingCfgId();
			break;

		case QueueType.SCIENCE_QUEUE_VALUE:
			// 科技研究
			itemCfgId = Integer.valueOf(queue.getItemId());
			break;
		case QueueType.PLANT_SCIENCE_QUEUE_VALUE:
			// 泰能科技研究
			itemCfgId = Integer.valueOf(queue.getItemId());
			break;
		case QueueType.CURE_QUEUE_VALUE:
			// 治疗
			break;
		case QueueType.CURE_PLANT_QUEUE_VALUE:
			// 治疗泰能
			break;
		case QueueType.GUILD_HOSPICE_QUEUE_VALUE:
			helpCnt = ConstProperty.getInstance().getAllianceCareHelpNumber();
			break;

		default:
			return false;
		}

		GuildHelpInfo json = new GuildHelpInfo();
		json.setPlayerId(player.getId());
		json.setPlayerIcon(player.getIcon());
		json.setQueueType(QueueType.valueOf(queue.getQueueType()));
		json.setItemId(itemCfgId);
		json.setCount(helpCnt);
		json.setBuildTime(buildTime);
		json.setQueueStatus(QueueStatus.valueOf(queue.getStatus()));
		json.getParames().addAll(Arrays.asList(parames));
		json.setMultiply(multiply);
		// 申请帮助
		saveOrUpdateGuildHelpInfo(guildId, queue.getId(), json);

		GuildHelpQueue.Builder builder = GuildHelpQueue.newBuilder();
		builder.setQueueId(queue.getId());
		builder.setApplyId(player.getId());
		builder.setApplyName(player.getName());
		builder.setApplyIcon(player.getIcon());
		builder.setCurCount(0);
		builder.setTotalCount(helpCnt);
		builder.setQueueType(queue.getQueueType());
		builder.setItemId(itemCfgId);
		builder.setQueueStatus(queue.getStatus());
		String pfIcon = player.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			builder.setPfIcon(player.getPfIcon());
		}

		// 刷新联盟帮助队列数目
		refreshGuildHelpNum(guildId, builder, null);
		
		return true;
	}

	public void refreshGuildHelpNum(String guildId, GuildHelpQueue.Builder addHelpQueue, String removeQueueId) {

		// 获取所有队列
		Map<String, GuildHelpInfo> queues = getAllGuildHelpInfo(guildId);
		// 获取所有在线联盟成员
		Collection<String> mems = this.getGuildMembers(guildId);
		Map<Player, Integer> players = new HashMap<Player, Integer>();
		for (String playerId : mems) {
			Player memPlayer = GlobalData.getInstance().getActivePlayer(playerId);
			if (memPlayer != null) {
				players.put(memPlayer, 0);
			}
		}
		if (queues != null && queues.size() > 0) {
			for (GuildHelpInfo helpInfo : queues.values()) {
				List<String> helpers = helpInfo.getHelpers();
				int helpCount = helpInfo.getCount();
				int curHelpCount = helpInfo.getCurHelpCount();
				for (Player player : players.keySet()) {
					if (!helpInfo.getPlayerId().equals(player.getId()) && !helpers.contains(player.getId()) && curHelpCount < helpCount
							&& !CrossService.getInstance().isCrossPlayer(helpInfo.getPlayerId())) {
						players.put(player, players.get(player) + 1);
					}
				}
			}
		}
		for (Entry<Player, Integer> e : players.entrySet()) {
			RefreshGuildHelpQueueNumRes.Builder builder = RefreshGuildHelpQueueNumRes.newBuilder();
			Player member = e.getKey();
			Integer number = e.getValue();
			boolean bfalse = false;
			if (member.lastRefreshGuildHelpNum != number) {
				bfalse = true;
				member.lastRefreshGuildHelpNum = number;
			}
			builder.setNum(number);
			if (addHelpQueue != null) {
				bfalse = true;
				builder.setAddHelpQueue(addHelpQueue);
			}
			if (!HawkOSOperator.isEmptyString(removeQueueId)) {
				bfalse = true;
				builder.setRemoveQueueId(removeQueueId);
			}
			if (bfalse) { // 有变化才推送
				member.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_REFRESH_HELPQUEUE_NUM_S, builder));
			}
		}
	}

	/**
	 * 帮助一个联盟队列
	 * 
	 * @param guildId
	 *            : 联盟ID
	 * @param playerId
	 *            : 帮助者玩家ID
	 * @param queueId
	 *            : 队列ID
	 * @param assistTime
	 *            : 加成值
	 */
	public void helpOneGuildQueue(String guildId, Player helper, String queueId) {

		// 获取申请帮助信息
		GuildHelpInfo helpInfo = this.getGuildHelpInfo(guildId, queueId);
		if (Objects.isNull(helpInfo)) {
			return;
		}

		helpGuildQueue(guildId, queueId, helpInfo, helper);
		
		// 重置上次推送帮助次数,确保联盟帮助数目推送成功
		helper.lastRefreshGuildHelpNum = -1;
		// 刷新联盟帮助队列数目
		refreshGuildHelpNum(guildId, null, null);
		// 排行
		GuildRankMgr.getInstance().onPlayerHelp(helper.getId(), guildId, 1);
	}

	/**
	 * 帮助所有联盟队列
	 * 
	 * @param guildId
	 *            : 联盟ID
	 * @param playerId
	 *            : 帮助者玩家ID
	 * @param value
	 *            : 加成值
	 */
	public void helpAllGuildQueues(String guildId, Player helper) {
		// 获取申请帮助信息
		Map<String, GuildHelpInfo> helpInfos = getAllGuildHelpInfo(guildId);

		int successHelpCount = 0;
		for (Entry<String, GuildHelpInfo> e : helpInfos.entrySet()) {

			String queueId = e.getKey();
			GuildHelpInfo helpInfo = e.getValue();

			boolean isSuccessed = helpGuildQueue(guildId, queueId, helpInfo, helper);
			if(isSuccessed){
				successHelpCount++;
			}
		}
		// 重置上次推送帮助次数,确保联盟帮助数目推送成功
		helper.lastRefreshGuildHelpNum = -1;
		// 刷新联盟帮助队列数目
		refreshGuildHelpNum(guildId, null, null);
		
		GuildRankMgr.getInstance().onPlayerHelp(helper.getId(), guildId, successHelpCount );
	}

	/**
	 * 实际的帮助联盟队列处理
	 * 
	 * @param guildId
	 *            : 联盟ID
	 * @param queueId
	 *            : 队列ID
	 * @param helpInfo
	 *            : 帮助队列信息
	 * @param helperId
	 *            : 帮助者玩家ID
	 * @param assistTime
	 *            : 加成值
	 */
	private boolean helpGuildQueue(String guildId, String queueId, GuildHelpInfo helpInfo, Player helper) {
		List<String> helpers = helpInfo.getHelpers();

		// 自己的队列
		String playerId = helpInfo.getPlayerId();
		if (playerId.equals(helper.getId())) {
			return false;
		}
		int maxTimes = helpInfo.getCount();
		if (helpers.size() >= maxTimes) {
			return false;
		}

		// 已经帮助过该队列
		if (helpers.contains(helper.getId())) {
			return false;
		}

		// 申请帮助者
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		// 理论上该玩家不可能为null,添加日志
		if(player == null){
			HawkLog.logPrintln("helpGuildQueue targetPlayer not exist, targetId: {}, helpInfo: {}", helpInfo.getPlayerId(), helpInfo.serializ());
			return false;
		}
		
		// 不帮助跨服玩家
		if (CrossService.getInstance().isCrossPlayer(playerId)) {
			return false;
		}
		QueueEntity entity = player.getData().getQueueEntity(queueId);
		if (entity == null) {
			// 队列已经不存在，删除帮助数据
			deleteGuildHelpInfo(guildId, queueId);
			return false;
		}
		if (Objects.isNull(player)) {
			return false;
		}
		BuildingCfg conf = player.getData().getBuildingCfgByType(BuildingType.EMBASSY);
		// 计算减少的时间（毫秒）
		long assistTime = conf.getAssistTime() * 1000 + player.getEffect().getEffVal(EffType.GUILD_HELP_TIME) * 1000;
		//一键升X级的情况
		assistTime *= entity.getMultiply();
		// 玩家在线
		// 减少队列时间
		if (entity.getQueueType() != QueueType.GUILD_HOSPICE_QUEUE_VALUE) {
			// 在被帮助者线程进行队列修改
			GsApp.getInstance().postMsg(player.getXid(), QueueBeHelpedMsg.valueOf(queueId, assistTime));
			helpInfo.setTotalDisTime(helpInfo.getTotalDisTime() + assistTime);
		}
		// 推送被帮助
		BeGuildHelpedRes.Builder builder = BeGuildHelpedRes.newBuilder();
		builder.setHelperName(helper.getName());
		builder.setQueueType(helpInfo.getQueueType().getNumber());
		builder.setItemId(helpInfo.getItemId());
		builder.setQueueStatus(helpInfo.getQueueStatus().getNumber());
		builder.setDisTime(assistTime);
		builder.setQueueId(queueId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILDMANAGER_BEHELPED_S, builder));

		// 活动事件
		ActivityManager.getInstance().postEvent(new GuildHelpEvent(helper.getId()));
		// 任务事件
		MissionManager.getInstance().postMsg(helper, new EventGuildHelp());
		// 联盟任务-帮助
		postGuildTaskMsg(new GuildHelpTaskEvent(guildId));

		// 添加到帮助队列
		helpers.add(helper.getId());

		// 联盟关怀, 达到最大次数直接结束队列
		if (entity.getQueueType() == QueueType.GUILD_HOSPICE_QUEUE_VALUE && helpers.size() == maxTimes) {
			// 在被帮助者线程进行队列修改
			GsApp.getInstance().postMsg(player.getXid(), QueueBeHelpedMsg.valueOf(queueId, Long.MAX_VALUE));
		}

		// 更新队列
		this.saveOrUpdateGuildHelpInfo(guildId, queueId, helpInfo);
		return true;
	}

	private void removeMemberGuildHelp(Player player, String guildId) {
		List<QueueEntity> queues = player.getData().getQueueEntities();
		List<String> queueIds = queues.stream()
				.filter(e -> e.getQueueType() > 0 && e.getHelpTimes() > 0)
				.map(QueueEntity::getId)
				.collect(Collectors.toList());
		if (!queueIds.isEmpty()) {
			long ret = deleteGuildHelpInfo(guildId, queueIds.toArray(new String[queueIds.size()]));
			if (ret > 0) {
				refreshGuildHelpNum(guildId, null, null);
			}
		}
	}

	/**
	 * 删除联盟帮助队列
	 */
	public void removeGuildHelp(String guildId, String queueId) {
		// 删除队列
		long ret = deleteGuildHelpInfo(guildId, queueId);
		if (ret > 0) {
			// 刷新联盟帮助队列数目
			refreshGuildHelpNum(guildId, null, queueId);
		}
	}

	/**
	 * 判断联盟是否存在
	 * 
	 * @param guildId
	 * @return
	 */
	public boolean isGuildExist(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		return guildData.containsKey(guildId) || cGuildData.containsKey(guildId);
	}

	/**
	 * 清除玩家对联盟排行榜的贡献
	 * 
	 * @param playerId
	 */
	public void clearGuildMemberPower(String playerId) {
		String guildId = getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		playerGuild.get(playerId).updateMemberPower(0);
		msgCall(MsgId.GUILD_FIGHT_RANK_REFRESH, RankService.getInstance(), new GuildClearMemberPowerInvoker(guildId));
	}

	/**
	 * 刷新联盟击杀
	 * 
	 * @param guildId
	 * @param battlePoint
	 * @param newPoint
	 */
	public void changeGuildMemeberKillCount(String guildId, String playerId, long newKillCount) {
		if (!playerGuild.containsKey(playerId) || HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		playerGuild.get(playerId).updateMemberKillCount(newKillCount);
		msgCall(MsgId.GUILD_KILL_RANK_REFRESH, RankService.getInstance(), new GuildChangeMemberKillCntInvoker(guildId));
	}

	/**
	 * 清除玩家对联盟排行榜的贡献
	 * 
	 * @param playerId
	 */
	public void clearGuildMemberKillCount(String playerId) {
		String guildId = getPlayerGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		playerGuild.get(playerId).updateMemberKillCount(0);
		msgCall(MsgId.GUILD_KILL_RANK_REFRESH, RankService.getInstance(), new GuildClearMemberKillCntInvoker(guildId));
	}

	/**
	 * 获取玩家联盟权限
	 * 
	 * @param playerId
	 * @return
	 */
	public int getPlayerGuildAuthority(String playerId) {
		if (playerGuild.containsKey(playerId)) {
			return playerGuild.get(playerId).getAuthority();
		}
		// 跨服联盟成员权限
		else if (cPlayerGuild.containsKey(playerId)) {
			if (cPlayerAuth.containsKey(playerId)) {
				return cPlayerAuth.get(playerId);
			} else {
				return GuildAuthority.L3_VALUE;
			}
		}
		return GuildAuthority.L0_VALUE;
	}

	/**
	 * 是否是工会
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isGuildLeader(String playerId) {
		return this.getPlayerGuildAuthority(playerId) == Const.GuildAuthority.L5_VALUE;
	}

	/**
	 * 获取玩家入盟时间
	 * 
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public long getJoinGuildTime(String playerId) {
		if (!playerGuild.containsKey(playerId)) {
			return 0;
		}
		return playerGuild.get(playerId).getJoinGuildTime();
	}
	
	/**
	 * 构建联盟成员信息
	 * @param playerId
	 * @return
	 */
	public GuildMemeberInfo.Builder genGuildMemberInfo(String playerId){
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		GuildMemberObject entity = playerGuild.get(playerId);
		if(player == null || entity == null){
			return null;
		}
		GuildMemeberInfo.Builder info = GuildMemeberInfo.newBuilder();
		info.setAuthority(entity.getAuthority());
		info.setPlayerName(entity.getPlayerName());
		info.setPlayerId(entity.getPlayerId());
		info.setPower(entity.getPower());

		info.setIcon(player.getIcon());
		if (!HawkOSOperator.isEmptyString(player.getPfIcon())) {
			info.setPfIcon(player.getPfIcon());
		}
		info.setVipLvl(player.getVipLevel());
		info.setCommon(BuilderUtil.genPlayerCommonBuilder(player));
		if (player.isActiveOnline()) {
			info.setOnline(true);
			info.setOfflineTime(0);
		} else {
			info.setOnline(false);
			info.setOfflineTime(player.getLogoutTime());
		}
		int pos[] = WorldPlayerService.getInstance().getPlayerPosXY(playerId);
		info.setX(pos[0]);
		info.setY(pos[1]);
		info.setBuildingLevel(player.getCityLv());
		info.setOfficer(GameUtil.getOfficerId(playerId));
		info.setIsSendGift(false);
		info.setGuildOfficer(entity.getOfficeId());
		info.setMtpremarch(player.getData().getCommanderEntity().getMtpremarch());
		return info;
	}

	public List<String> getGuildMemberIdsByAuthority(String guildId, int authority) {
		List<String> playerIds = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return playerIds;
		}
				
		Set<String> ids = guildMemberData.get(guildId);
		if (ids == null || ids.isEmpty()) {
			return playerIds;
		}
		
		for (String playerId : ids) {
			GuildMemberObject entity = playerGuild.get(playerId);
			if (entity.getAuthority() == authority) {
				playerIds.add(playerId);
			}
		} 
		
		return playerIds;
	}
	
	
	//获取有权限的玩家
	public List<String> getGuildMemberIdsHasAuthority(String guildId , AuthId aId ) {
		List<String> playerIds = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return playerIds;
		}
				
		Set<String> ids = guildMemberData.get(guildId);
		if (ids == null || ids.isEmpty()) {
			return playerIds;
		}
		
		for (String playerId : ids) {
			if(checkGuildAuthority(playerId,aId)){
				playerIds.add(playerId);
			}
		} 
		
		return playerIds;
	}
	
	/**
	 * 获取联盟成员信息
	 * 
	 * @param guildId
	 * @param needName
	 * @return
	 */
	public GetGuildMemeberInfoResp.Builder getGuildMemberInfo(String guildId, boolean needName) {
		GetGuildMemeberInfoResp.Builder builder = GetGuildMemeberInfoResp.newBuilder();
		if (guildMemberData.get(guildId) == null) {
			return builder;
		}
		for (String playerId : guildMemberData.get(guildId)) {
			GuildMemeberInfo.Builder info = genGuildMemberInfo(playerId);
			if (info != null) {
				builder.addInfo(info);
			}
		}
		if (needName) {
			GuildInfoObject guild = guildData.get(guildId);
			if (!HawkOSOperator.isEmptyString(guild.getL1Name())) {
				builder.setL1Name(guild.getL1Name());
			}
			if (!HawkOSOperator.isEmptyString(guild.getL2Name())) {
				builder.setL2Name(guild.getL2Name());
			}
			if (!HawkOSOperator.isEmptyString(guild.getL3Name())) {
				builder.setL3Name(guild.getL3Name());
			}
			if (!HawkOSOperator.isEmptyString(guild.getL4Name())) {
				builder.setL4Name(guild.getL4Name());
			}
			if (!HawkOSOperator.isEmptyString(guild.getL5Name())) {
				builder.setL5Name(guild.getL5Name());
			}
		}
		return builder;
	}

	/**
	 * 根据联盟成员权限等级获取对应的名称
	 * 
	 * @param guildId
	 * @param authLevel
	 * @return
	 */
	public String getLevelName(String guildId, int authLevel) {
		String name = "";
		if (!HawkOSOperator.isEmptyString(guildId)) {
			GuildInfoObject guild = guildData.get(guildId);
			if (guild != null) {
				switch (authLevel) {
				case Const.GuildAuthority.L1_VALUE:
					name = guild.getL1Name();
					break;
				case Const.GuildAuthority.L2_VALUE:
					name = guild.getL2Name();
					break;
				case Const.GuildAuthority.L3_VALUE:
					name = guild.getL3Name();
					break;
				case Const.GuildAuthority.L4_VALUE:
					name = guild.getL4Name();
					break;
				case Const.GuildAuthority.L5_VALUE:
					name = guild.getL5Name();
					break;
				default:
					break;
				}
			}
		}
		if (name == null) {
			name = "";
		}
		return name;
	}

	/**
	 * 修改玩家名字
	 * 
	 * @param guildId
	 * @param playerId
	 * @param playerName
	 */
	public void changePlayerName(String guildId, String playerId, String playerName) {
		if (!HawkOSOperator.isEmptyString(guildId)) {
			GuildInfoObject guild = guildData.get(guildId);
			if (guild != null && guild.getLeaderId().equals(playerId)) {
				guild.updateGuildLeader(playerId, playerId, playerName);
			}
		}
		GuildMemberObject memberObj = playerGuild.get(playerId);
		if (memberObj != null) {
			memberObj.updateMemberPlayerName(playerName);
		}
	}

	/**
	 * 当前联盟科技作用号映射
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<Integer, Integer> getEffectsGuildTech(String guildId) {
		return effectGuildTech.get(guildId);
	}
	
	public Map<Integer, Integer> getCsEffectsGuildTech(String guildId) {
		return csEffectGuildTech.get(guildId);
	}
	
	public int getCsEffectGuildTech(String guildId, int effectType) {
		Map<Integer, Integer> csEffectGuildTechMap = getCsEffectsGuildTech(guildId);
		if (csEffectGuildTechMap == null || csEffectGuildTechMap.isEmpty()) {
			return 0;
		}
		
		return MapUtil.getIntValue(csEffectGuildTechMap, effectType);
	}

	/**
	 * 获取指定联盟科技作用号的作用值
	 * 
	 * @param guildId
	 * @param effId
	 * @return
	 */
	public int getEffectGuildTech(String guildId, int effId) {
		Map<Integer, Integer> effects = getEffectsGuildTech(guildId);
		if (effects == null) {
			return 0;
		}
		Integer val = effects.get(effId);
		return val == null ? 0 : val;
	}

	/**
	 * 是否可以取代盟主
	 * 
	 * @param guildId
	 * @param playerId
	 * @return
	 */
	public int canImpeachLeader(GuildInfoObject guild, GuildMemberObject source) {
		String playerId = source.getPlayerId();
		String leaderId = guild.getLeaderId();
		long leaderOfflineTime = guild.getLeaderOfflineTime();
		// 盟主本人不能取代自己
		if (playerId.equals(leaderId)) {
			HawkLog.logPrintln("canImpeachLeader check failed, cannot replace self, playerId: {}, guildId: {}, leaderId: {}, offlineTime: {}", playerId, guild.getId(), leaderId,
					leaderOfflineTime);
			return Status.Error.GUILD_CANNOT_REPLACE_SELF_VALUE;
		}
		
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		if (leader == null) {
			HawkLog.errPrintln("guildservice canImpeachLeader, leader null, leaderId: {}", leaderId);
		} else {
			leaderOfflineTime = leader.getEntity().getLogoutTime();
		}
		// 盟主在线则不能取代
		if (leader != null && leader.isActiveOnline()) {
			HawkLog.logPrintln("canImpeachLeader check failed, leaderIsOnline, playerId: {}, guildId: {}, leaderId: {}, offlineTime: {}", playerId, guild.getId(), leaderId,
					leaderOfflineTime);
			return Status.Error.GUILD_LEADER_CANNOT_REPLACE_VALUE;
		}
		
		if (GameConstCfg.getInstance().guildLeaderCheck() && leader == null) {
			leaderOfflineTime = leaderOfflineTime - HawkTime.DAY_MILLI_SECONDS * 360;
		}
		
		long offlineTime = HawkTime.getMillisecond() - leaderOfflineTime;
		// 时间未到
		if (offlineTime < GuildConstProperty.getInstance().getLeaderReplaceTime1()) {
			HawkLog.logPrintln("canImpeachLeader check failed, timeCheck failed, playerId: {}, guildId: {}, leaderId: {}, offlineTimeGap: {}, offlineTime: {}", playerId,
					guild.getId(), leaderId, offlineTime, leaderOfflineTime);
			return Status.Error.GUILD_CANNOT_REPLACE_TIME_LESS_VALUE;
		}
		if (offlineTime >= GuildConstProperty.getInstance().getLeaderReplaceTime1() && offlineTime < GuildConstProperty.getInstance().getLeaderReplaceTime2()) {
			if (!checkGuildAuthority(playerId, AuthId.REPLACE_LEADER_ADVANCE)) {
				HawkLog.logPrintln("canImpeachLeader check failed, checkGuildAuthority1 failed, playerId: {}, guildId: {}, leaderId: {}, offlineTimeGap: {}, offlineTime: {}",
						playerId, guild.getId(), leaderId, offlineTime, leaderOfflineTime);
				// 当前阶段权限不足
				return Status.Error.GUILD_CANNOT_REPLACE_STAGE_AUTH_LESS_VALUE;
			}
		} else {
			if (!checkGuildAuthority(playerId, AuthId.REPLACE_LEADER)) {
				HawkLog.logPrintln("canImpeachLeader check failed, checkGuildAuthority2 failed, playerId: {}, guildId: {}, leaderId: {}, offlineTimeGap: {}, offlineTime: {}",
						playerId, guild.getId(), leaderId, offlineTime, leaderOfflineTime);
				// 权限不足不能取代
				return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
			}
		}
		// 霸主和统帅不能被取代
		if (StarWarsOfficerService.getInstance().isKing(leaderId)) {
			HawkLog.logPrintln("canImpeachLeader check failed, StarWarsOfficerService is king, playerId: {}, guildId: {}, leaderId: {}", playerId, guild.getId(), leaderId);
			return Status.Error.SW_KING_CANNOT_IMPEACHEMENT_VALUE;
		}
		HawkLog.logPrintln("canImpeachLeader check success, playerId: {}, guildId: {}, leaderId: {}, offlineTimeGap: {}, offlineTime: {}", playerId, guild.getId(), leaderId,
				offlineTime, leaderOfflineTime);
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 更新盟主离线时间
	 * 
	 * @param guildId
	 * @param id
	 */
	public void updateGuildLeaderLogoutTime(String guildId, String playerId) {
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null) {
			return;
		}
		if (guild.getLeaderId().equals(playerId)) {
			guild.updateGuildLeaderOfflineTime();
		}
	}

	public void addGuildLog(String guildId, GuildLogType type, String param) {
		HPGuildLog.Builder log = HPGuildLog.newBuilder();
		log.setLogType(type);
		log.setParam(param);
		log.setTime(HawkTime.getMillisecond());
		LocalRedis.getInstance().addGuildLog(guildId, log);
	}

	/**
	 * 获取联盟积分
	 */
	public long getGuildScore(String guildId) {
		GuildInfoObject guild = guildData.get(guildId);
		if (guild == null) {
			return 0;
		}
		return guild.getScore();
	}

	/**
	 * 添加联盟积分
	 * 
	 * @param playerId
	 * @return
	 */
	public void incGuildScore(String guildId, int score) {
		GuildInfoObject guild = getGuildInfoObject(guildId);
		if (guild == null) {
			return;
		}
		guild.incGuildScore(score);
	}

	/**
	 * 获得玩家的退出联盟的时间
	 * 
	 * @param playerId
	 * @return
	 */
	public long getPlayerQuitGuildTime(String playerId) {
		GuildMemberObject entity = playerGuild.get(playerId);
		if (entity != null) {
			return entity.getQuitGuildTime();
		}
		return 0;
	}

	/**
	 * 设置玩家的退出联盟的时间
	 * 
	 * @param playerId
	 * @return
	 */
	public void setPlayerQuitGuildTime(String playerId, long time) {
		GuildMemberObject entity = playerGuild.get(playerId);
		if (entity != null) {
			entity.updateMemberQuitGuildTime(time);
		}
	}

	/**
	 * 获得推荐联盟信息
	 * 
	 * @param player
	 * @param pageNum
	 * @return
	 */
	public GetRecommendGuildListResp.Builder onGetRecommendGuild(int pageNum, String lang) {
		List<GuildInfoObject> list = getSortGuilds((pageNum - 1) * GsConst.RECOMMOND_GUILD_MAX,
				GsConst.RECOMMOND_GUILD_MAX, lang, true);
		GetRecommendGuildListResp.Builder builder = GetRecommendGuildListResp.newBuilder();
		for (GuildInfoObject guild : list) {
			try {
				builder.addInfo(getGuildInfo(guild, false));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		return builder;
	}

	/**
	 * 获得推荐联盟信息
	 * 
	 * @param player
	 * @param pageNum
	 * @return
	 */
	public GetOtherGuildResp.Builder onGetOtherGuild(Player player, int pageNum) {
		List<GuildInfoObject> list = getSortGuilds((pageNum - 1) * GsConst.RECOMMOND_GUILD_MAX,
				GsConst.RECOMMOND_GUILD_MAX, player.getLanguage(), false);
		String selfGuildId = player.getGuildId();
		GetOtherGuildResp.Builder builder = GetOtherGuildResp.newBuilder();
		for (GuildInfoObject guild : list) {
			if (guild.getId().equals(selfGuildId)) {
				continue;
			}
			builder.addInfo(getGuildInfo(guild, false));
		}
		return builder;
	}

	/**
	 * 根据名称获得联盟信息
	 * 
	 * @param name
	 * @return
	 */
	public GetSearchGuildListResp.Builder onSearchGuild(String name, boolean precise) {
		int maxCnt = GuildConstProperty.getInstance().getSearchAllianceNumber();
		List<String> guildsByName = SearchService.getInstance().matchingGuildInfo(name);
		List<String> guildsByTag = SearchService.getInstance().matchingGuildTag(name);
		Set<String> guildIds = new HashSet<>();
		guildIds.addAll(guildsByName);
		guildIds.addAll(guildsByTag);
		GetSearchGuildListResp.Builder builder = GetSearchGuildListResp.newBuilder();
		int count = 0;
		for (String guildId : guildIds) {
			if (count >= maxCnt) {
				break;
			}
			GuildInfoObject guild = guildData.get(guildId);
			if (guild == null) {
				logger.error(" onSearchGuild error, guildId: {}", guildId);
				continue;
			}
			
			if (precise && !guild.getName().equals(name)) {
				continue;
			}
			builder.addInfo(getGuildInfo(guild, false));
			count++;
		}
		return builder;
	}

	/**
	 * 获得排序后的联盟信息
	 * 
	 * @param startIndex
	 * @param num
	 * @return
	 */
	private List<GuildInfoObject> getSortGuilds(int startIndex, int num, final String lang, final boolean recommand) {
		List<GuildInfoObject> info = new ArrayList<GuildInfoObject>();
		info.addAll(guildData.values());
		for(GuildInfoObject obj : info){ //保证每次排序之前重新计算
			if(obj == null){
				continue;
			}
			obj.setGuildWeightScore(getGuildSortWeight(obj));
		}
		Collections.sort(info, new Comparator<GuildInfoObject>() {
			@Override
			public int compare(GuildInfoObject left, GuildInfoObject right) {
				if(left == null && right == null){
					return 0;
				}
				if(left == null){
					return -1;
				}
				if(right == null){
					return 1;
				}
				if(left.getGuildWeightScore() > right.getGuildWeightScore()){
					return -1;
				}else if(left.getGuildWeightScore() < right.getGuildWeightScore()){
					return 1;
				}
				return 0;
			}
		});
		
		List<GuildInfoObject> finalList = new ArrayList<GuildInfoObject>();
		for (int i = startIndex; i < startIndex + num; i++) {
			if (info.size() <= i) {
				break;
			}
			GuildInfoObject entity = info.get(i);
			if (entity != null) {
				finalList.add(entity);
			}
		}
		return finalList;
	}

	// 计算联盟排序权值
//	private int getOtherSortWeight(GuildInfoObject entity, String lang) {
//		int weight = 0;
//		if (entity.getLangId().equals(lang)) {
//			weight += GsConst.GuildSortWeight.MEMBER_NUM;
//		}
//		if (entity.getLangId().equals("en")) {
//			weight += GsConst.GuildSortWeight.NEED_PERMITION;
//		}
//		if (entity.getLangId().equals("all")) {
//			weight += GsConst.GuildSortWeight.LEADER_ONLINE;
//		}
//		return weight;
//	}

	// 计算联盟排序权值
//	private int getGuildSortWeight(GuildInfoObject entity, String lang) {
//		int weight = 0;
//		if (!guildMemberData.containsKey(entity.getId())) {
//			return 0;
//		}
//		int memberNum = guildMemberData.get(entity.getId()).size();
//		if (memberNum < getGuildMemberMaxNum(entity.getId()) && !entity.isNeedPermition()) {
//			weight += GsConst.GuildSortWeight.MEMBER_NUM;
//		}
//		if (GlobalData.getInstance().getActivePlayer(entity.getLeaderId()) != null) {
//			weight += GsConst.GuildSortWeight.LEADER_ONLINE;
//		}
//		return weight;
//	}
	
	/***
	 * 策划排序规则：人数比例低于80%的活跃联盟，按联盟当前人数降序排列，人数在80%到99%的活跃联盟，
	 * 人数降序排列，后面是满员联盟，紧跟着是死盟
	 * 
	 * 设计规则：1.人数比例低于80%的，返回当前联盟人数加上一个联盟极限人数的常量
	 * 2.人数在80%到99%的联盟，返回联盟极限人数减去当前联盟人数
	 * 3.满员联盟，直接返回满员人数 - 初始人数
	 * 4.死盟返回，返回当前人数 - 联盟极限人数
	 * @param entity
	 * @return
	 */
	private int getGuildSortWeight(GuildInfoObject entity){
		final float constPersent = GuildConstProperty.getInstance().getGuildRecommendPersent(); //分割线常量
		final int nomalMaxGuildMember = 200; //计算默认值
		int guildMemberNormalMaxNum = GuildConstProperty.getInstance().getGuildMemberNormalMaxNum();
		if(guildMemberNormalMaxNum == 0 || guildMemberNormalMaxNum < nomalMaxGuildMember){
			guildMemberNormalMaxNum = nomalMaxGuildMember; //防止策划配置遗漏或者配置过低影响计算
		}
		Collection<String> members = getGuildMembers(entity.getId());
		int memberCnt = members.size();
		if(entity.deadGuild()){
			return memberCnt - guildMemberNormalMaxNum;
		}
		int maxCnt = getGuildMemberMaxNum(entity.getId());
		if(memberCnt == maxCnt){
			return maxCnt - GuildConstProperty.getInstance().getInitAlliancePeople();
		}
		float persent = (float)memberCnt / (float)maxCnt;
		if(persent < constPersent){
			return guildMemberNormalMaxNum + memberCnt;
		}else{
			return memberCnt;
		}
	}

	/**
	 * 获得联盟成员id
	 * 
	 * @param id
	 * @return
	 */
	public Collection<String> getGuildMembers(String guildId) {
		if (Objects.isNull(guildId)) {
			return Collections.emptyList();
		}
		if (guildMemberData.containsKey(guildId)){
			return new HashSet<String>(guildMemberData.get(guildId));
		}
		
		if(cGuildMemberData.containsKey(guildId)){
			return new HashSet<String>(cGuildMemberData.get(guildId));
		}
		return Collections.emptyList();
	}

	/**
	 * 获得联盟成员及联盟成员的权限
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<String, Integer> getGuildMemberAndPos(String guildId) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		if (!guildMemberData.containsKey(guildId)) {
			return map;
		}
		GuildInfoObject guild = guildData.get(guildId);
		for (String playerId : guildMemberData.get(guildId)) {
			if (playerId.equals(guild.getLeaderId())) {
				map.put(playerId, GuildPositon.LEADER_VALUE);
				continue;
			}
			if (!HawkOSOperator.isEmptyString(guild.getColeaderId()) && playerId.equals(guild.getColeaderId())) {
				map.put(playerId, GuildPositon.COLEADER_VALUE);
				continue;
			}
			map.put(playerId, GuildPositon.MEMBER_VALUE);
		}
		return map;
	}

	/**
	 * 获得联盟名字
	 * 
	 * @param guildId
	 * @return
	 */
	public String getGuildName(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return "";
		}
		GuildInfoObject guild = guildData.get(guildId);
		if (guild != null) {
			return guild.getName();
		}
		// 跨服联盟处理
		if(cGuildData.containsKey(guildId)){
			return cGuildData.get(guildId).getName();
		}
		return "";
	}

	/**
	 * 获取指定盟的盟主id
	 * 
	 * @param guildId
	 * @return
	 */
	public String getGuildLeaderId(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return "";
		}

		GuildInfoObject guild = guildData.get(guildId);
		if (guild != null) {
			return guild.getLeaderId();
		}
		// 跨服联盟处理
		if(cGuildData.containsKey(guildId)){
			return cGuildData.get(guildId).getLeaderId();
		}
		return "";
	}

	/**
	 * 获取制定盟的盟主名
	 * 
	 * @param guildId
	 * @return
	 */
	public String getGuildLeaderName(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return "";
		}

		GuildInfoObject guild = guildData.get(guildId);
		if (guild != null) {
			return guild.getLeaderName();
		}
		// 跨服联盟处理
		if(cGuildData.containsKey(guildId)){
			return cGuildData.get(guildId).getLeaderName();
		}
		return "";
	}

	/**
	 * 获得联盟旗帜
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildFlag(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		if (guildData.containsKey(guildId)) {
			return guildData.get(guildId).getFlagId();
		}
		if (cGuildData.containsKey(guildId)){
			return cGuildData.get(guildId).getFlagId();
		}
		return 0;
	}

	/**
	 * 获得联盟旗帜
	 * 
	 * @param playerId
	 * @return
	 */
	public int getGuildFlagByPlayerId(String playerId) {
		return getGuildFlag(getPlayerGuildId(playerId));
	}

	/**
	 * 获得联盟战力
	 * 
	 * @param guildId
	 * @return
	 */
	public long getGuildBattlePoint(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		long battlePoint = 0;
		Set<String> playerIds = guildMemberData.get(guildId);
		if (playerIds != null && playerIds.size() > 0) {
			for (String pid : playerIds) {
				battlePoint += playerGuild.get(pid).getPower();
			}
		}
		return battlePoint;
	}
	
	/**
	 * 获取联盟的去兵战力
	 * @param guildId
	 * @return
	 */
	public long getGuildNoArmyPower(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		long battlePoint = 0;
		Set<String> playerIds = guildMemberData.get(guildId);
		if (playerIds != null && playerIds.size() > 0) {
			for (String pid : playerIds) {
				battlePoint += playerGuild.get(pid).getNoArmyPower();
			}
		}
		return battlePoint;
	}

	/**
	 * 联盟击杀数量
	 * 
	 * @param guildId
	 * @return
	 */
	public long getGuildKillCount(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return 0;
		}
		long killCount = 0;
		Set<String> playerIds = guildMemberData.get(guildId);
		if (playerIds != null && playerIds.size() > 0) {
			for (String pid : playerIds) {
				killCount += playerGuild.get(pid).getKillCount();
			}
		}
		return killCount;
	}

	/**
	 * 获得联盟简称
	 * 
	 * @param guildId
	 * @return
	 */
	public String getGuildTag(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return "";
		}
		if (guildData.containsKey(guildId)) {
			return guildData.get(guildId).getTag();
		}
		if (cGuildData.containsKey(guildId)) {
			return cGuildData.get(guildId).getTag();
		}
		return "";
	}

	/**
	 * 获得联盟等级(当前版本取消等级概念,均为1级)
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildLevel(String guildId) {
		return 1;
	}

	/**
	 * 获取本服的联盟数据列表
	 * 
	 * @param entities
	 * @return
	 */
	public int getGuildEntities(List<GuildInfoObject> entities) {
		entities.addAll(guildData.values());
		return entities.size();
	}

	/**
	 * 获取本服联盟个数
	 * 
	 * @return
	 */
	public int getGuildCount() {
		return guildData.size();
	}

	/**
	 * 获取所有联盟id
	 * 
	 * @return
	 */
	public List<String> getGuildIds() {
		List<String> guildIds = new ArrayList<>(guildData.size());
		guildIds.addAll(guildData.keySet());
		return guildIds;
	}

	/**
	 * 获得联盟信息
	 * 
	 * @param guildId
	 * @return
	 */
	public GetGuildInfoResp.Builder getGuildInfo(String guildId, boolean self) {
		if (guildData.containsKey(guildId)) {
			return getGuildInfo(guildData.get(guildId), self);
		}
		return null;
	}

	/**
	 * 获得联盟不可变信息
	 * 
	 * @param guildId
	 * @return
	 */
	public GetGuildInfoResp.Builder getGuildInfo(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		if (!guildData.containsKey(guildId)) {
			return null;
		}
		return getGuildInfo(guildData.get(guildId), false);
	}

	/**
	 * 联盟最大人数
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildMemberMaxNum(String guildId) {
		int maxCount = GuildConstProperty.getInstance().getInitAlliancePeople();
		maxCount += getEffectGuildTech(guildId, EffType.GUILD_MEMBER_NUM_VALUE);
		return maxCount;
	}

	/**
	 * 获取联盟当前人数
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildMemberNum(String guildId) {
		return getGuildMembers(guildId).size();
	}

	/**
	 * 获得联盟信息
	 * 
	 * @param entity
	 * @param self
	 *            是否为自己联盟信息
	 * @return
	 */
	private GetGuildInfoResp.Builder getGuildInfo(GuildInfoObject entity, boolean self) {
		String guildId = entity.getId();
		GetGuildInfoResp.Builder builder = GetGuildInfoResp.newBuilder();
		builder.setId(guildId);
		builder.setNumTypeGuildId(entity.getNumTypeId());
		builder.setName(entity.getName());
		builder.setTag(entity.getTag());
		builder.setLevel(entity.getLevel());
		builder.setLeaderName(entity.getLeaderName());
		builder.setLeaderId(entity.getLeaderId());
		Player leader = GlobalData.getInstance().makesurePlayer(entity.getLeaderId());
		if (leader == null) {
			HawkLog.errPrintln("guildservice getGuildInfo, leader null, leaderId: {}", entity.getLeaderId());
		}
		builder.setLeaderIcon(leader.getIcon());
		builder.setLeaderVipLvl(leader.getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(leader));
		if (!HawkOSOperator.isEmptyString(leader.getPfIcon())) {
			builder.setLeaderPfIcon(leader.getPfIcon());
		}
		
		builder.setPower(getGuildBattlePoint(guildId));
		builder.setMemberNum(getGuildMembers(guildId).size());
		builder.setMemberMaxNum(getGuildMemberMaxNum(guildId));
		builder.setLanguage(entity.getLangId());
		builder.setFlag(entity.getFlagId());
		builder.setOpenRecurit(!entity.isNeedPermition());
		builder.setNeedBuildingLevel(entity.getNeedBuildingLevel());
		builder.setNeedCommonderLevel(entity.getNeedCommanderLevel());
		builder.setNeedPower(entity.getNeedPower());
		builder.setNeedLanguage(entity.getNeedLang());
		builder.setHasChangeTag(entity.isHasChangeTag());
		if (!HawkOSOperator.isEmptyString(entity.getNotice())) {
			builder.setNotice(entity.getNotice());
		}
		if (!HawkOSOperator.isEmptyString(entity.getAnnouncement())) {
			builder.setAnnouncement(entity.getAnnouncement());
		}
		
		if (self) {
			if (!HawkOSOperator.isEmptyString(entity.getL1Name())) {
				builder.setL1Name(entity.getL1Name());
			}
			if (!HawkOSOperator.isEmptyString(entity.getL2Name())) {
				builder.setL2Name(entity.getL2Name());
			}
			if (!HawkOSOperator.isEmptyString(entity.getL3Name())) {
				builder.setL3Name(entity.getL3Name());
			}
			if (!HawkOSOperator.isEmptyString(entity.getL4Name())) {
				builder.setL4Name(entity.getL4Name());
			}
			if (!HawkOSOperator.isEmptyString(entity.getL5Name())) {
				builder.setL5Name(entity.getL5Name());
			}
		}
		builder.setXzqTickets(entity.getXZQTickets());
		return builder;
	}

	/**
	 * 获取联盟帮助个数
	 */
	public int getGuildHelpNum(String guildId, String playerId) {

		Map<String, GuildHelpInfo> queues = getAllGuildHelpInfo(guildId);
		if (queues == null || queues.size() == 0) {
			return 0;
		}

		int num = 0;
		for (GuildHelpInfo helpInfo : queues.values()) {
			List<String> helpers = helpInfo.getHelpers();
			int helpCount = helpInfo.getCount();
			int curHelpCount = helpInfo.getCurHelpCount();
			if (!playerId.equals(helpInfo.getPlayerId()) && !helpers.contains(playerId) && curHelpCount < helpCount) {
				num++;
			}
		}
		return num;
	}

	/**
	 * 获取联盟商店信息
	 * 
	 * @param builder
	 * @param guildId
	 */
	public HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder> onGetGuildShopInfo(Player player) {
		GuildInfoObject guildObject = guildData.get(player.getGuildId());
		GuildMemberObject member = playerGuild.get(player.getId());
		if (guildObject == null) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.Error.GUILD_NOT_EXIST_VALUE, null);
		}
		if (member == null) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.Error.GUILD_NOT_MEMBER_VALUE, null);
		}
		HPGetGuildShopInfoResp.Builder builder = HPGetGuildShopInfoResp.newBuilder();
		builder.setGuildScore(guildObject.getScore());
		builder.setContribution(player.getGuildContribution());
		ConfigIterator<GuildShopCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(GuildShopCfg.class);
		List<GuildShopCfg> cfgList = new ArrayList<>(iterator.toList());
		Collections.sort(cfgList);
		Map<String, String> shopInfo = RedisProxy.getInstance().getGuildShopInfo(player.getGuildId());
		for (GuildShopCfg cfg : cfgList) {
			int id = cfg.getId();
			String idStr = String.valueOf(id);
			int count = 0;
			if (shopInfo.containsKey(idStr)) {
				count = Integer.parseInt(shopInfo.get(idStr));
			}
			HPGuildShopItem.Builder itemBuilder = HPGuildShopItem.newBuilder();
			itemBuilder.setItemId(id);
			itemBuilder.setCount(count);
			itemBuilder.setPrice(cfg.getPrice());
			builder.addShopItem(itemBuilder);
		}
		return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 联盟商店补货
	 * 
	 * @param player
	 * @param req
	 * @return
	 */
	public HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder> addGuildShopItem(Player player, int itemId, int count) {
		if (count <= 0) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.SysError.PARAMS_INVALID_VALUE, null);
		}

		// 权限不足
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_STORE_REPLENISH)) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.Error.GUILD_LOW_AUTHORITY_VALUE, null);
		}

		GuildShopCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildShopCfg.class, itemId);
		// 商品信息不存在
		if (cfg == null) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.Error.GUILD_SHOP_ITEM_NOT_EXIST_VALUE, null);
		}
		long total = 1l * count * cfg.getPrice();
		// 参数异常
		if (total >= Integer.MAX_VALUE) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.SysError.PARAMS_INVALID_VALUE, null);
		}
		int totalPrice = (int) total;
		GuildInfoObject guildObject = guildData.get(player.getGuildId());

		// 联盟积分不足
		if (guildObject.getScore() < totalPrice) {
			return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.Error.GUILD_CONTRIBUTION_NOT_ENOUGH_VALUE, null);
		}
		long scoreBef = guildObject.getScore();
		// 扣除积分,补充联盟商店商品
		guildObject.disGuildScore(totalPrice);
		guildObject.updateGuildShopItem(itemId, count);

		// 记录商店日志
		addGuildShopLog(player, itemId, count, totalPrice, GuildConst.SHOP_LOG_TYPE_ADD);

		// 返回商品信息
		HPGetGuildShopInfoResp.Builder builder = buildShopInfo(player, cfg);
		HawkLog.logPrintln("addGuildShopItem , guildId:{}, playerId:{}, itemId:{}, cnt:{}, cost:{}, scoreBef:{}, scoreAft:{}", guildObject.getId(), player.getId(), itemId, count,
				totalPrice, scoreBef, guildObject.getScore());
		LogUtil.logGuildShopAdd(player, guildObject.getId(), itemId, count, totalPrice, scoreBef, guildObject.getScore());
		return new HawkTuple2<Integer, HPGetGuildShopInfoResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 添加联盟商店日志记录
	 * 
	 * @param player
	 * @param itemId
	 * @param count
	 * @param totalPrice
	 * @param logType
	 */
	public void addGuildShopLog(Player player, int itemId, int count, int totalPrice, int logType) {
		GuildShopLog.Builder shopLog = GuildShopLog.newBuilder();
		shopLog.setCount(count);
		shopLog.setItemId(itemId);
		shopLog.setName(player.getName());
		shopLog.setTime(HawkTime.getMillisecond());
		shopLog.setCost(totalPrice);
		LocalRedis.getInstance().addGuildShopLog(player.getGuildId(), shopLog, logType);
	}

	/**
	 * 构建商品信息
	 * 
	 * @param player
	 * @param cfg
	 * @return
	 */
	public HPGetGuildShopInfoResp.Builder buildShopInfo(Player player, GuildShopCfg cfg) {
		HPGuildShopItem.Builder itemInfo = HPGuildShopItem.newBuilder();
		itemInfo.setCount(RedisProxy.getInstance().getGuildShopItemCount(player.getGuildId(), cfg.getId()));
		itemInfo.setItemId(cfg.getId());
		itemInfo.setPrice(cfg.getPrice());

		HPGetGuildShopInfoResp.Builder builder = HPGetGuildShopInfoResp.newBuilder();
		builder.setContribution(player.getGuildContribution());
		builder.setGuildScore(getGuildScore(player.getGuildId()));
		builder.addShopItem(itemInfo);
		return builder;
	}

	/**
	 * 获取联盟商店记录
	 * 
	 * @param player
	 * @param logType
	 *            记录类型:购买/补货
	 * @return
	 */
	public HawkTuple2<Integer, HPGetGuildShopLogResp.Builder> getGuildShopLog(Player player, int logType) {
		GuildInfoObject entity = guildData.get(player.getGuildId());
		if (entity == null) {
			return new HawkTuple2<Integer, HPGetGuildShopLogResp.Builder>(Status.Error.GUILD_NOT_EXIST_VALUE, null);
		}
		HPGetGuildShopLogResp.Builder builder = HPGetGuildShopLogResp.newBuilder();
		Set<String> shopLogs = LocalRedis.getInstance().getGuildShopLog(player.getGuildId(), logType);
		if (shopLogs != null) {
			try {
				for (String string : shopLogs) {
					GuildShopLog.Builder shopLog = GuildShopLog.newBuilder();
					JsonFormat.merge(string, shopLog);
					builder.addShopLog(shopLog);
				}
			} catch (ParseException e) {
				HawkException.catchException(e);
			}
		}
		return new HawkTuple2<Integer, HPGetGuildShopLogResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 获取联盟科技作用号ids
	 * 
	 * @param guildId
	 * @return
	 */
	private EffType[] getGuildTechEffectTypes(String guildId) {
		Map<Integer, Integer> effects = effectGuildTech.get(guildId);
		if (effects == null || effects.size() == 0) {
			return null;
		}
		Set<Integer> typeSet = effects.keySet();
		EffType[] effTypes = new EffType[typeSet.size()];
		int index = 0;
		for (Integer type : typeSet) {
			effTypes[index] = EffType.valueOf(type);
			index++;
		}
		return effTypes;
	}

	/**
	 * 同步推送联盟科技作用号
	 * 
	 * @param guildId
	 */
	public void syncGuildTechEffect(String guildId) {
		EffType[] effTypes = getGuildTechEffectTypes(guildId);
		if (effTypes == null || effTypes.length == 0) {
			return;
		}
		if (!guildMemberData.containsKey(guildId)) {
			return;
		}
		for (String playerId : guildMemberData.get(guildId)) {
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null && player.isActiveOnline()) {
				player.getPush().syncPlayerEffect(effTypes);
			}
		}
	}

	/**
	 * 推送单个玩家联盟科技作用号信息
	 * 
	 * @param guildId
	 * @param player
	 */
	public void syncGuildTechEffect(String guildId, Player player) {
		if (player == null || !player.isActiveOnline()) {
			return;
		}
		EffType[] effTypes = getGuildTechEffectTypes(guildId);
		if (effTypes == null || effTypes.length == 0) {
			return;
		}
		player.getPush().syncPlayerEffect(effTypes);
	}

	/**
	 * 推送联盟申请数量
	 * 
	 * @param guildId
	 */
	public void pushApplayNum(String guildId) {
		HPGuildApplyNumSync.Builder sync = HPGuildApplyNumSync.newBuilder();
		sync.setApplyNum(LocalRedis.getInstance().getGuildPlayerApplyNum(guildId));
		GuildService.getInstance().broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_APPLYNUM_SYNC_S, sync));
	}

	/**
	 * 跨天检测
	 */
	private void crossDayCheck() {
		try {
			long currTime = HawkTime.getMillisecond();
			if (!HawkTime.isSameDay(lastCrossCheckTime, currTime)) {
				dailyLoginMembers = new ConcurrentHashMap<>();
				HawkTaskManager.getInstance().postExtraTask(new HawkTask() {							
					@Override
					public Object run() {
						for (GuildRankObj rankObj : guildRankObjs.values()) {
							rankObj.crossCheck();
						}
						return null;
					}
				});
			}
			lastCrossCheckTime = currTime;
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 构建联盟排行信息
	 * 
	 * @param player
	 * @param rankType
	 * @return
	 */
	public GuildGetDonateRankResp.Builder onGetContributionRankInfo(Player player, DonateRankType rankType) {
		GuildGetDonateRankResp.Builder builder = GuildGetDonateRankResp.newBuilder();
		builder.setNextRefreshTime(getNextRefreshTime(rankType));
		builder.addAllRankInfo(buildContributionRankInfo(rankType, player.getGuildId()));
		builder.setRankType(rankType);
		return builder;
	}

	/**
	 * 构建排行列表
	 * 
	 * @param rankType
	 * @param guildId
	 * @return
	 */
	public List<GuildDonateRankInfo> buildContributionRankInfo(DonateRankType rankType, String guildId) {
		List<GuildDonateRankInfo> rankList = new ArrayList<>();
		List<GuildDonateRank> rankInfos = guildRankObjs.get(guildId).getRankList(rankType);
		for (GuildDonateRank donateRank : rankInfos) {
			String playerId = donateRank.getPlayerId();
			GuildMemberObject member = playerGuild.get(playerId);
			if (member == null) {
				continue;
			}
			GuildDonateRankInfo.Builder rankInfo = GuildDonateRankInfo.newBuilder();
			Player rplayer = GlobalData.getInstance().makesurePlayer(member.getPlayerId());
			if (rplayer.isZeroEarningState()) {
				continue;
			}
			rankInfo.setPlayerId(member.getPlayerId());
			rankInfo.setPlayerName(member.getPlayerName());
			rankInfo.setIcon(rplayer.getIcon());
			if (!HawkOSOperator.isEmptyString(rplayer.getPfIcon())) {
				rankInfo.setPfIcon(rplayer.getPfIcon());
			}
			rankInfo.setAuthority(member.getAuthority());
			rankInfo.setDonate(donateRank.getDonate());
			rankInfo.setContribution(donateRank.getContribution());
			rankList.add(rankInfo.build());
		}
		return rankList;
	}

	/**
	 * 获取下次排行刷新时间
	 * 
	 * @param rankType
	 * @return
	 */
	private long getNextRefreshTime(DonateRankType rankType) {
		switch (rankType) {
		case DAILY_RANK:
			return HawkTime.getNextAM0Date();
		case WEEK_RANK:
			java.util.Date date = HawkTime.getFirstDayCalendarOfCurWeek();
			return date.getTime() + GsConst.DAY_MILLI_SECONDS * 7;
		default:
			return 0;
		}
	}

	/**
	 * 邀请联盟成员迁城
	 * 
	 * @param player
	 * @param inviteeId
	 * @return
	 */
	public int onInviteToMoveCity(Player player, String inviteeId) {
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(inviteeId);
		// 玩家不存在
		if (targetPlayer == null) {
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}

		// 操作涉及到跨服玩家,不支持
		if (CrossService.getInstance().isCrossPlayer(inviteeId)) {
			return Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE;
		}

		if (!isInTheSameGuild(player.getId(), inviteeId)) {
			return Status.Error.GUILD_NOT_MEMBER_VALUE;
		}
		
		long inviteCd = GuildConstProperty.getInstance().getInviteMailCd();
		// 对该玩家的联盟迁城邀请处于cd中
		if (HawkTime.getMillisecond() - LocalRedis.getInstance().getGuildCitymoveMailCd(player.getId(), inviteeId) < inviteCd * 1000l) {
			return Status.Error.GUILD_INVITE_MOVE_CITY_CD_VALUE;
		}
		int[] pos = WorldPlayerService.getInstance().getPlayerPosXY(player.getId());

		// 发送邮件---邀请迁城
		int icon = this.getGuildFlag(player.getGuildId());
		MoveCityInviteMail.Builder content = MoveCityInviteMail.newBuilder();
		content.setPlayerId(player.getId())
				.setName(player.getName())
				.setIcon(player.getIcon())
				.setPfIcon(player.getPfIcon())
				.setGuildTag(player.getGuildTag())
				.setGuildId(player.getGuildId())
				.setX(pos[0])
				.setY(pos[1]);
		GuildMailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(inviteeId)
				.setMailId(MailId.INVITE_MOVE_CITY)
				.addSubTitles(player.getName(), pos[0], pos[1])
				.addContents(content)
				.setIcon(icon)
				.build());
		LocalRedis.getInstance().addGuildCitymoveMailCd(player.getId(), inviteeId);

		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 是否包含联盟申请
	 * 
	 * @param guildId
	 */
	public boolean containGuildApply(String playerId, String guildId) {
		return LocalRedis.getInstance().containPlayerGuildApply(playerId, guildId);
	}

	/**
	 * 联盟科技研究完成检测
	 */
	private void checkScienceResearch() {
		if (researchData.size() == 0) {
			return;
		}
		Iterator<Entry<String, List<GuildScienceEntity>>> it = researchData.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, List<GuildScienceEntity>> entry = it.next();
			String guildId = entry.getKey();
			List<GuildScienceEntity> list = entry.getValue();
			long currTime = HawkTime.getMillisecond();
			List<GuildScienceEntity> needRemove = new ArrayList<>();
			List<GuildScienceEntity> lvlUpList = new ArrayList<>();
			for (GuildScienceEntity science : list) {
				if (science.getFinishTime() == 0) {
					needRemove.add(science);
					continue;
				}
				GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(
						GuildScienceLevelCfg.class, science.getLevel() + 1, science.getScienceId());
				if (cfg == null) {
					needRemove.add(science);
					continue;
				}
				if (currTime >= science.getFinishTime()) {
					science.setLevel(science.getLevel() + 1);
					science.setDonate(0);
					science.setStar(0);
					science.setFinishTime(0);
					// 研究至满级的科技,取消推荐状态
					if (science.isRecommend() && isScienceMaxLvl(science)) {
						science.setRecommend(false);
					}
					long openLimitTime = this.calGuildScienceOpenLimitTime(science.getScienceId(), science.getLevel());
					science.setOpenLimitTime(openLimitTime);
					
					needRemove.add(science);
					lvlUpList.add(science);
				}
			}
			if (!needRemove.isEmpty()) {
				list.removeAll(needRemove);
			}
			if (list.isEmpty()) {
				it.remove();
			}
			// 同步联盟科技信息
			if (!lvlUpList.isEmpty()) {
				// 解锁检测
				checkScienceOpen(guildId);
				// 作用号刷新同步
				calcTechEffec(guildId);
				syncGuildTechEffect(guildId);
				// 科技信息同步
				GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
				for (GuildScienceEntity entity : lvlUpList) {
					builder.addScienceInfo(buildGuildScienceInfo(entity));
				}
				builder.setScienceFloor(guildScienceFloor.get(guildId));
				broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
				broadcastGuildInfo(guildId);
			}
		}

	}

	/**
	 * 脚本指令调用，联盟科技一键满级
	 */
	public void scienceLvUp(String guildId){
		List<GuildScienceEntity> scienceList = getGuildScienceList(guildId);
		List<GuildScienceEntity> lvlUpList = new ArrayList<>();
		for (GuildScienceEntity science: scienceList){
			if (isScienceMaxLvl(science)){
				continue;
			}
			lvlUpList.add(science);
			science.setLevel(science.getLevel() + 1);
			science.setDonate(0);
			science.setStar(0);
			science.setFinishTime(0);
			// 研究至满级的科技,取消推荐状态
			if (science.isRecommend() && isScienceMaxLvl(science)) {
				science.setRecommend(false);
			}
		}
		// 同步联盟科技信息
		if (!lvlUpList.isEmpty()) {
			// 解锁检测
			checkScienceOpen(guildId);
			// 作用号刷新同步
			calcTechEffec(guildId);
			syncGuildTechEffect(guildId);
			// 科技信息同步
			GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
			for (GuildScienceEntity entity : lvlUpList) {
				builder.addScienceInfo(buildGuildScienceInfo(entity));
			}
			builder.setScienceFloor(guildScienceFloor.get(guildId));
			broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
			broadcastGuildInfo(guildId);
			scienceLvUp(guildId);
		}
	}


	/**
	 * 检查联盟科技开启
	 */
	private void checkScienceOpen(String guildId) {
		int floor = calcGuildFloor(getGuildScienceVal(guildId));
		// 如果大于当前的联盟的层级，则执行开启新技能操作
		Integer curFloor = guildScienceFloor.get(guildId);
		if (curFloor != null && curFloor >= floor) {
			return;
		}
		// 先更新当前层级
		guildScienceFloor.put(guildId, floor);
		// 检查开启技能，并进行添加
		List<GuildScienceEntity> scienceList = guildScienceData.get(guildId);
		if (scienceList == null) { // 之前没有任何科技，初始化
			scienceList = new ArrayList<GuildScienceEntity>();
			guildScienceData.put(guildId, scienceList);
		}

		GuildScienceInfoSync.Builder syncInfo = GuildScienceInfoSync.newBuilder();
		ConfigIterator<GuildScienceMainCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(GuildScienceMainCfg.class);
		for (GuildScienceMainCfg guildScienceMainCfg : cfgs) {
			if (guildScienceMainCfg.getFloor() == floor) {// 此处判断等于，而不是大于等于，因为等级逐级提升，仅需要当前层级即可，这样可以省去排重的计算量
				if(XZQConstCfg.getInstance().isLimitGuildScience(guildScienceMainCfg.getId())){
					continue;
				}
				GuildScienceEntity entity = new GuildScienceEntity();
				entity.setGuildId(guildId);
				entity.setScienceId(guildScienceMainCfg.getId());
				long openLimitTime = this.calGuildScienceOpenLimitTime(entity.getScienceId(), entity.getLevel());
				entity.setOpenLimitTime(openLimitTime);
				if (!HawkDBManager.getInstance().create(entity)) {
					logger.error("manor create guild science failed, guildId: {}, scienceId: {}", guildId, guildScienceMainCfg.getId());
					continue;
				}

				scienceList.add(entity);
				syncInfo.addScienceInfo(buildGuildScienceInfo(entity));
			}
		}

		// 广播推送联盟解锁的新科技
		if (!syncInfo.getScienceInfoList().isEmpty()) {
			broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, syncInfo));
		}
	}

	/**
	 * 增加科技值
	 * 
	 * @param science
	 * @param addVal
	 */
	private void addGuildScienceDonate(GuildScienceEntity science, int addVal) {
		int starOrg = science.getStar();
		String guildId = science.getGuildId();
		int donate = science.getDonate() + addVal;
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(
				GuildScienceLevelCfg.class, science.getLevel() + 1, science.getScienceId());
		List<Integer> starVals = cfg.getStarValList();
		int starAft = 0;
		int starVal = 0;
		for (int i = 0; i < starVals.size(); i++) {
			starVal += starVals.get(i);
			if (donate < starVal) {
				break;
			}
			starAft = i + 1;
			if (i == starVals.size() - 1) {
				donate = starVal;
			}
		}
		science.setDonate(donate);
		if (starAft > starOrg) {
			science.setStar(starAft);
			// 同步科技信息
			GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
			builder.addScienceInfo(buildGuildScienceInfo(science));
			broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
		}
	}

	/**
	 * 根据联盟科技等级获取当前层级
	 * 
	 * @return
	 */
	private int calcGuildFloor(int lvl) {
		int floor = 1; // 最低为1
		ConfigIterator<GuildScienceFloorCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(GuildScienceFloorCfg.class);
		for (GuildScienceFloorCfg guildScienceFloorCfg : cfgs) {
			if (lvl >= guildScienceFloorCfg.getUnlockLevel() && guildScienceFloorCfg.getFloor() > floor) {
				floor = guildScienceFloorCfg.getFloor();
			}
		}
		return floor;
	}

	/**
	 * 获取联盟科技层级
	 * 
	 * @param guildId
	 * @return
	 */
	private int getGuildScienceFloor(String guildId) {
		if (guildScienceFloor.containsKey(guildId)) {
			return guildScienceFloor.get(guildId);
		}
		return 0;
	}

	/**
	 * 获取联盟科技列表
	 * 
	 * @param guildId
	 * @return
	 */
	public List<GuildScienceEntity> getGuildScienceList(String guildId) {
		List<GuildScienceEntity> scienceList = new ArrayList<>();
		if (HawkOSOperator.isEmptyString(guildId) || !guildScienceData.containsKey(guildId)) {
			return scienceList;
		}
		return guildScienceData.get(guildId);
	}

	/**
	 * 获取联盟科技
	 * 
	 * @param guildId
	 * @param scienceId
	 * @return
	 */
	public GuildScienceEntity getGuildScience(String guildId, int scienceId) {
		GuildScienceEntity entity = getGuildScienceList(guildId).stream()
				.filter(g -> g.getScienceId() == scienceId)
				.findFirst()
				.orElse(null);
		return entity;
	}

	/**
	 * 获取联盟总科技值
	 * 
	 * @param guildId
	 * @return
	 */
	public int getGuildScienceVal(String guildId) {
		return getGuildScienceList(guildId).stream()
				.mapToInt(s -> s.getLevel())
				.sum();
	}

	/**
	 * 获取联盟科技信息
	 * 
	 * @param guildId
	 * @return
	 */
	public HawkTuple2<Integer, GetGuildScienceInfoResp.Builder> onGetguildScienceInfo(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return new HawkTuple2<Integer, GetGuildScienceInfoResp.Builder>(Status.Error.GUILD_NO_JOIN_VALUE, null);
		}
		GuildMemberObject member = getGuildMemberObject(player.getId());
		List<GuildScienceEntity> scienceList = getGuildScienceList(guildId);
		if (scienceList.isEmpty()) {
			checkScienceOpen(guildId);
			scienceList = getGuildScienceList(guildId);
		}
		GetGuildScienceInfoResp.Builder builder = buildMemberScienceInfo(player, member, scienceList);
		return new HawkTuple2<Integer, GetGuildScienceInfoResp.Builder>(Status.SysError.SUCCESS_OK_VALUE, builder);
	}

	/**
	 * 设置联盟推荐科技
	 * 
	 * @param player
	 * @param recommendIds
	 * @param cancleIds
	 * @return
	 */
	public int onGuildScienceRecommend(Player player, List<Integer> recommendIds, List<Integer> cancleIds) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_SCIENCE_RESEARCH)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		int result = checkRecommendData(recommendIds, cancleIds, guildId);
		if (result != Status.SysError.SUCCESS_OK_VALUE) {
			return result;
		}
		List<GuildScienceEntity> syncList = new ArrayList<>();
		for (int scienceId : cancleIds) {
			GuildScienceEntity science = getGuildScience(guildId, scienceId);
			science.setRecommend(false);
			syncList.add(science);
		}
		for (int scienceId : recommendIds) {
			GuildScienceEntity science = getGuildScience(guildId, scienceId);
			science.setRecommend(true);
			syncList.add(science);
		}

		// 同步联盟科技信息
		GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
		for (GuildScienceEntity science : syncList) {
			builder.addScienceInfo(buildGuildScienceInfo(science));
		}
		broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
		return result;
	}

	/**
	 * 联盟科技研究
	 * 
	 * @param player
	 * @param scienceId
	 * @return
	 */
	public int onGuildScienceResearch(Player player, int scienceId) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		if (!GuildService.getInstance().checkGuildAuthority(player.getId(), AuthId.ALLIANCE_SCIENCE_RESEARCH)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		GuildScienceEntity science = getGuildScience(guildId, scienceId);
		// 科技未解锁
		if (science == null) {
			return Status.Error.GUILD_SCIENCE_UNLOCK_VALUE;
		}
		// 科技值未满
		if (!isScienceEnough(science)) {
			return Status.Error.GUILD_SCIENCE_DONATE_NOT_ENOUGH_VALUE;
		}
		// 科技正在研究
		if (science.getFinishTime() > 0) {
			return Status.Error.GUILD_SCIENCE_ON_RESEARCHING_VALUE;
		}
		// 当前有联盟科技正在研究
		if (getResearchingCount(guildId) > 1) {
			return Status.Error.GUILD_SCIENCE_OTHER_RESEARCHING_VALUE;
		}

		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class,
				science.getLevel() + 1, science.getScienceId());
		int needTime = cfg.getCostTime() * 1000;
		needTime /= 1d + player.getData().getEffVal(EffType.GUILD_TECH_RESEARH_SPD) * GsConst.EFF_PER;
		science.setFinishTime(HawkTime.getMillisecond() + needTime);
		if (!researchData.containsKey(guildId)) {
			researchData.put(guildId, new ArrayList<>());
		}

		researchData.get(guildId).add(science);
		// 记录联盟科技数据打点
		LogUtil.logGuildTechFlow(player, science.getScienceId(), GuildTechOperType.RESEARCH, 0, 0);

		// 同步联盟科技信息
		GuildScienceInfoSync.Builder builder = GuildScienceInfoSync.newBuilder();
		builder.addScienceInfo(buildGuildScienceInfo(science));
		broadcastProtocol(guildId, HawkProtocol.valueOf(HP.code.GUILD_SCIENCE_INFO_SYNC_S, builder));
		return 0;
	}

	/**
	 * 联盟捐献
	 * 
	 * @param player
	 * @param type
	 * @param scienceId
	 * @param awardItems
	 * @return
	 */
	public GuildScienceDonateResp.Builder onGuildScienceDonate(Player player, DonateType type, int scienceId, int scienceDonate, int guildScore, int contribution, int crit) {
		String guildId = player.getGuildId();
		List<GuildScienceEntity> scienceList = new ArrayList<>();
		GuildScienceEntity science = getGuildScience(guildId, scienceId);
		if (science == null) {
			logger.error("GuildScience Donate error, science is null, guildId: {}, scienceId: {}", guildId, scienceId);
			return null;
		} else {
			addGuildScienceDonate(science, scienceDonate);
			scienceList.add(science);
		}

		if (!HawkOSOperator.isEmptyString(guildId)) {
			// 捐献排行统计
			guildRankObjs.get(guildId).onMemberDonate(player.getId(), scienceDonate, contribution);
			incGuildScore(guildId, guildScore);
			GuildRankMgr.getInstance().onPlayerContri( player.getId(), guildId, contribution );
		}
		GuildMemberObject member = GuildService.getInstance().getGuildMemberObject(player.getId());
		GuildScienceDonateResp.Builder builder = GuildScienceDonateResp.newBuilder();
		builder.setScienceInfo(buildMemberScienceInfo(player, member, scienceList));
		builder.setCrit(crit);
		builder.setDonate(scienceDonate);
		builder.setContribution(contribution);
		builder.setScore(guildScore);
		return builder;
	}

	/**
	 * 构建联盟科技信息
	 * 
	 * @param science
	 * @return
	 */
	private GuildScienceInfo.Builder buildGuildScienceInfo(GuildScienceEntity science) {
		GuildScienceInfo.Builder scienceInfo = GuildScienceInfo.newBuilder();
		scienceInfo.setScienceId(science.getScienceId());
		scienceInfo.setLevel(science.getLevel());
		scienceInfo.setStar(science.getStar());
		scienceInfo.setDonate(science.getDonate());
		scienceInfo.setRecommend(science.isRecommend());
		if (science.getFinishTime() > 0) {
			scienceInfo.setFinishTime(science.getFinishTime());
		}
		scienceInfo.setLimitOpenTime(science.getOpenLimitTime());
		return scienceInfo;
	}

	/**
	 * 构建联盟成员科技界面信息
	 * 
	 * @param member
	 * @param scienceList
	 * @return
	 */
	private GetGuildScienceInfoResp.Builder buildMemberScienceInfo(Player player, GuildMemberObject member, List<GuildScienceEntity> scienceList) {
		String guildId = member.getGuildId();
		GetGuildScienceInfoResp.Builder builder = GetGuildScienceInfoResp.newBuilder();
		if (scienceList != null) {
			for (GuildScienceEntity science : scienceList) {
				builder.addScienceInfo(buildGuildScienceInfo(science));
			}
		}
		builder.setScienceFloor(getGuildScienceFloor(guildId));
		builder.setContribution(player.getGuildContribution());
		builder.setNormalLeftTimes(GuildConstProperty.getInstance().getResourceDonateNumber() - member.getNormalDonateTimes());
		builder.setCrystalTimes(member.getCrystalDonateTimes());
		builder.setResetTimes(member.getDonateResetTimes());
		builder.setNextAddTime(member.getNextDonateAddTime());
		return builder;
	}

	/**
	 * 计算捐献相关消耗
	 * 
	 * @param player
	 * @param type
	 * @param scienceId
	 * @param consume
	 * @return
	 */
	public int getDonateConsume(Player player, DonateType type, int scienceId, ConsumeItems consume) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}

		GuildScienceEntity science = getGuildScience(guildId, scienceId);
		// 科技未解锁
		if (science == null) {
			return Status.Error.GUILD_SCIENCE_UNLOCK_VALUE;
		}
		//时间限制
		if(science.getOpenLimitTime() > HawkTime.getMillisecond()){
			return Status.Error.GUILD_SCIENCE_UNLOCK_VALUE;
		}
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class, science.getLevel() + 1, science.getScienceId());
		// 联盟科技等级达到上限
		if (cfg == null) {
			return Status.Error.GUILD_SCIENCE_LVL_MAX_VALUE;
		}
		// 科技值已满
		if (isScienceEnough(science)) {
			return Status.Error.GUILD_SCIENCE_DONATE_FULL_VALUE;
		}
		// 科技正在研究
		if (science.getFinishTime() > 0) {
			return Status.Error.GUILD_SCIENCE_ON_RESEARCHING_VALUE;
		}
		GuildConstProperty property = GuildConstProperty.getInstance();
		GuildMemberObject member = getGuildMemberObject(player.getId());
		switch (type) {
		case NORMAL:
			// 捐献次数超出上限
			if (member.getNormalDonateTimes() >= property.getResourceDonateNumber()) {
				return Status.Error.GUILD_DONATE_TIMES_OVER_LIMIT_VALUE;
			}
			consume.addConsumeInfo(cfg.getResCost(science.getStar()), false);
			break;

		case CRYSTAL:
			float param0 = property.getDonateParameter0();
			float param1 = property.getDonateParameter1();
			float param2 = property.getDonateParameter2();
			int times = member.getCrystalDonateTimes() + 1;
			int cost = (int) Math.floor(param0 + (times - 1) * param1 + (times - 1) * (times - 2) * param2 / 2);
			consume.addConsumeInfo(PlayerAttr.GOLD, cost);
			break;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 联盟捐献次数重置
	 * 
	 * @param player
	 */
	public GetGuildScienceInfoResp.Builder onDonateTimesReset(Player player) {
		GuildMemberObject member = getGuildMemberObject(player.getId());
		member.setNormalDonateTimes(0);
		member.setDonateResetTimes(member.getDonateResetTimes() + 1);
		member.setNextDonateAddTime(0);
		return buildMemberScienceInfo(player, member, null);

	}

	/**
	 * 重置联盟捐献次数检测
	 * 
	 * @param player
	 * @return
	 */
	public int checkResetDonate(Player player) {
		String guildId = player.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		GuildMemberObject member = getGuildMemberObject(player.getId());
		// 超出重置次数上限
		if (member.getDonateResetTimes() >= GameUtil.getDonateResetTimes(player)) {
			return Status.Error.GUILD_DONATE_RESET_TIMES_OVER_LIMIT_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 检测联盟捐献跨天重置相关信息
	 * 
	 * @param player
	 */
	public boolean checkCrossDay(Player player) {
		GuildMemberObject member = getGuildMemberObject(player.getId());
		int refreshHour = GuildConstProperty.getInstance().getDonateRefreshTime();

		int dayOfYear = HawkTime.getYearDay();
		int hourOfDay = HawkTime.getHour();
		boolean crossHour = member.getDonateDayOfYear() != dayOfYear && hourOfDay >= refreshHour;
		boolean crossDay = Math.abs(dayOfYear - member.getDonateDayOfYear()) > 1;
		// 跨天重置联盟捐献相关信息
		boolean isCorssDay = crossHour || crossDay;
		if (isCorssDay) {
			member.setDonateDayOfYear(dayOfYear);
			member.setDiamondDonateTimes(0);
			member.setDonateResetTimes(0);

			// 联盟宝藏
			StorehouseBaseEntity storehouseBase = player.getData().getStorehouseBase();
			storehouseBase.setExc(GuildConstProperty.getInstance().getExcavateNumber());
			storehouseBase.setLastExcRecover(Long.MAX_VALUE);
			storehouseBase.setHelp(GuildConstProperty.getInstance().getHelpNumber());
			storehouseBase.setLastHelpRecover(Long.MAX_VALUE);
			storehouseBase.setRefrashCount(0);
		}
		return isCorssDay;
	}

	/**
	 * 普通捐献次数恢复检测
	 * 
	 * @param player
	 */
	public void checkDonateTimesAdd(Player player) {
		GuildMemberObject member = getGuildMemberObject(player.getId());
		long nextAddTime = member.getNextDonateAddTime();
		if (nextAddTime == 0) {
			return;
		}
		long currTime = HawkTime.getMillisecond();
		int gap = GuildConstProperty.getInstance().getResourceDonateTime() * 1000;
		int disEff = player.getData().getEffVal(EffType.GUILD_DONATE_SPEED);
		gap = (int) Math.ceil(GsConst.EFF_RATE * gap / (GsConst.RANDOM_MYRIABIT_BASE + disEff));
		if (currTime < nextAddTime) {
			return;
		}
		long canAdd = 1 + (currTime - nextAddTime) / gap;
		if (canAdd >= member.getNormalDonateTimes()) {
			member.setNormalDonateTimes(0);
			member.setNextDonateAddTime(0);
		} else {
			member.setNormalDonateTimes((int) (member.getNormalDonateTimes() - canAdd));
			member.setNextDonateAddTime(nextAddTime + canAdd * gap);
		}
	}

	/**
	 * 联盟科技值是否已满
	 * 
	 * @param scienceEntity
	 * @return
	 */
	private boolean isScienceEnough(GuildScienceEntity scienceEntity) {
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class,
				scienceEntity.getLevel() + 1, scienceEntity.getScienceId());
		return scienceEntity.getDonate() >= cfg.getFullDonate();
	}

	/**
	 * 联盟科技是否满级
	 * 
	 * @param scienceEntity
	 * @return
	 */
	public boolean isScienceMaxLvl(GuildScienceEntity scienceEntity) {
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(GuildScienceLevelCfg.class, scienceEntity.getLevel() + 1, scienceEntity.getScienceId());
		return cfg == null;
	}

	/**
	 * 检测联盟科技推荐信息
	 * 
	 * @param recommendIds
	 * @param cancleIds
	 * @param guildId
	 * @return
	 */
	private int checkRecommendData(List<Integer> recommendIds, List<Integer> cancleIds, String guildId) {
		for (int cancleId : cancleIds) {
			GuildScienceEntity science = getGuildScience(guildId, cancleId);
			if (science == null) {
				return Status.Error.GUILD_SCIENCE_UNLOCK_VALUE;
			}
			if (!science.isRecommend()) {
				return Status.SysError.PARAMS_INVALID_VALUE;
			}
		}

		for (int recommendId : recommendIds) {
			GuildScienceEntity science = getGuildScience(guildId, recommendId);
			if (science == null) {
				return Status.Error.GUILD_SCIENCE_UNLOCK_VALUE;
			}
		}
		int recommendCnt = getRecommendCount(guildId);
		if (recommendCnt + recommendIds.size() - cancleIds.size() > GsConst.GUILD_SCIENCE_MAX_RECOMMEND) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取当前联盟推荐科技数量
	 * 
	 * @param guildId
	 * @return
	 */
	private int getRecommendCount(String guildId) {
		List<GuildScienceEntity> sciences = getGuildScienceList(guildId);
		if (sciences == null) {
			return 0;
		}
		return (int) sciences.stream().filter(o -> o.isRecommend()).count();
	}

	/**
	 * 获取当前正在研究的科技的数量
	 * 
	 * @param guildId
	 * @return
	 */
	private int getResearchingCount(String guildId) {
		List<GuildScienceEntity> sciences = getGuildScienceList(guildId);
		if (sciences == null) {
			return 0;
		}
		long curTime = HawkTime.getMillisecond();
		return (int) sciences.stream().filter(o -> o.getFinishTime() > curTime).count();
	}

	/**
	 * 推送联盟战争条数
	 */
	public void pushGuildWarCount(String guildId) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				if (HawkOSOperator.isEmptyString(guildId)) {
					return null;
				}

				HPGuildWarCountPush.Builder builder = HPGuildWarCountPush.newBuilder();
				Collection<IWorldMarch> mCloection = WorldMarchService.getInstance().getGuildMarchs(guildId);
				for(IWorldMarch march : mCloection){
					if (march.getMarchStatus() == WorldMarchStatus.MARCH_STATUS_WAITING_VALUE) {
						builder.addMassEndTime(march.getStartTime());
					}
				}
				builder.setCount(mCloection.size());

				Collection<String> guildMembers = getGuildMembers(guildId);
				for (String playerId : guildMembers) {
					Player player = GlobalData.getInstance().getActivePlayer(playerId);
					if (player != null && StringUtils.isEmpty(player.getDungeonMap())) {
						player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_GUILD_WAR_COUNT_VALUE, builder));
					}
				}
				return null;
			}
		});
	}

	/**
	 * 联盟权限检查
	 * 
	 * @param playerId
	 * @param authId
	 * @return
	 */
	public boolean checkGuildAuthority(String playerId, AuthId authId) {
		int authLvl = getPlayerGuildAuthority(playerId);
		List<Integer> lvlList = null;
		String guildId = getPlayerGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			GuildInfoObject guild = getGuildInfoObject(guildId);
			if (guild != null) {
				lvlList = guild.getAuthMap().get(authId.getNumber());
			}
		}

		if (lvlList == null) {
			AllianceNewAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceNewAuthorityCfg.class, authId.getNumber());
			lvlList = cfg == null ? Collections.emptyList() : cfg.getInitList();
		}

		return lvlList.contains(authLvl);
	}

	/**
	 * 修改联盟权限信息
	 * 
	 * @param player
	 * @param authInfos
	 * @return
	 */
	public int onChangeAuthInfo(Player player, List<GuildAuthInfo> authInfos) {
		if (!checkGuildAuthority(player.getId(), AuthId.AMEND_MEMBER_AUTHORITY)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		Map<Integer, List<Integer>> editMap = new HashMap<>();
		for (GuildAuthInfo authInfo : authInfos) {
			int authId = authInfo.getId();
			List<Integer> authLvls = authInfo.getAuthLvlList();
			AllianceNewAuthorityCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceNewAuthorityCfg.class, authId);
			List<Integer> canEditList = cfg.getCanEditList();
			// 权限不支持修改
			if (canEditList.isEmpty()) {
				return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
			}
			// 初始化等级列表
			List<Integer> initList = cfg.getInitList();
			// 初始化列表和修改后列表并集
			List<Integer> totalList = new ArrayList<>();
			totalList.addAll(initList);
			totalList.addAll(authLvls);
			// 初始化列表和修改后列表交集
			initList.retainAll(authLvls);
			// 涉及变更的列表
			totalList.removeAll(initList);
			// 权限修改不支持
			if (!canEditList.containsAll(totalList)) {
				return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
			}
			editMap.put(authId, authLvls);
		}
		GuildInfoObject guild = getGuildInfoObject(player.getGuildId());
		if (guild == null) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		for (Entry<Integer, List<Integer>> entry : editMap.entrySet()) {
			guild.updateAuthMap(entry.getKey(), entry.getValue());
		}
		// 同步联盟信息
		broadcastGuildInfo(guild.getId());
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 推送新加联盟战争记录条数(联盟战争记录小红点儿)
	 */
	public void pushNewGuildWarRecordCount(String atkGuildId, String defGuildId) {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				PushNewGuildWarRecord.Builder builder = PushNewGuildWarRecord.newBuilder();
				builder.setCount(1);
				if (!HawkOSOperator.isEmptyString(atkGuildId)) {
					Collection<String> atkGuildMembers = getGuildMembers(atkGuildId);
					for (String playerId : atkGuildMembers) {
						Player player = GlobalData.getInstance().getActivePlayer(playerId);
						if (player == null) {
							continue;
						}
						player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_NEW_GUILD_WAR_RECORD_COUNT, builder));
					}
				}
				if (!HawkOSOperator.isEmptyString(defGuildId)) {
					Collection<String> defGuildMembers = getGuildMembers(defGuildId);
					for (String playerId : defGuildMembers) {
						Player player = GlobalData.getInstance().getActivePlayer(playerId);
						if (player == null) {
							continue;
						}
						player.sendProtocol(HawkProtocol.valueOf(HP.code.PUSH_NEW_GUILD_WAR_RECORD_COUNT, builder));
					}
				}
				return null;
			}
		});
	}

	/**
	 * 重置清理资源点次数
	 */
	public void resetClearResNum() {
		for (GuildInfoObject info : guildData.values()) {
			info.getEntity().setClearResNum(0);
		}
	}

	public HPGuildInfoSync.Builder buildGuildSyncInfo(Player player) {
		if (player.isCsPlayer()) {
			return buildCGuildSyncInfo(player);
		}
		return buildLocalGuildInfo(player);
	}
	
	private HPGuildInfoSync.Builder buildLocalGuildInfo(Player player){
		HPGuildInfoSync.Builder builder = HPGuildInfoSync.newBuilder();
		if (player.getData().getStatisticsEntity().getJoinGuildCnt() == 0) {
			builder.setJoinedGuild(false);
		} else {
			builder.setJoinedGuild(true);
		}
		String guildId = player.getGuildId();
		GuildInfoObject guild = null;
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guild = getGuildInfoObject(guildId);
		}
		if (guild != null) {
			builder.setGuildId(guildId);
			builder.setServerId(GameUtil.strUUID2ServerId(guildId));
			builder.setGuildFlag(guild.getFlagId());
			builder.setGuildTag(guild.getTag());
			builder.setGuildLevel(guild.getLevel());
			builder.setGuildAuthority(player.getGuildAuthority());
			builder.setApplyNum(LocalRedis.getInstance().getGuildPlayerApplyNum(guildId));
			builder.setHelpNum(getGuildHelpNum(guildId, player.getId()));
			builder.setGuildName(getGuildName(guildId));
			builder.setJoinTime(getJoinGuildTime(player.getId()));
			builder.setNumTypeGuildId(guild.getNumTypeId());
			// 联盟权限信息
			Map<Integer, List<Integer>> authMap = guild.getAuthMap();
			for (Entry<Integer, List<Integer>> entry : authMap.entrySet()) {
				GuildAuthInfo.Builder authInfo = GuildAuthInfo.newBuilder();
				authInfo.setId(entry.getKey());
				for (int lvl : entry.getValue()) {
					authInfo.addAuthLvl(lvl);
				}
				builder.addAuthInfo(authInfo);
			}
			// 联盟标记信息
			Map<Integer, GuildSign> signMap = GuildService.getInstance().getGuildSignMap(guildId);
			for (Entry<Integer, GuildSign> entry : signMap.entrySet()) {
				builder.addSignInfo(entry.getValue());
			}
			
			// 玩家申请的联盟官员id
			int applyOfficeId = getSelfApplyOfficeId(player.getId(), guildId);
			if (applyOfficeId != GuildOffice.NONE.value()) {
				builder.setApplyOfficerId(applyOfficeId);
			}
			GuildMemberObject member = getGuildMemberObject(player.getId());
			if(member!=null && member.getOfficeId() != GuildOffice.NONE.value()){
				builder.setGuildOfficeId(member.getOfficeId());
			}

			builder.setGuildScience(getGuildScienceVal(guildId));
			builder.setXzqTickets(guild.getXZQTickets());
		} else {
			builder.setQuitGuildTime(getPlayerQuitGuildTime(player.getId()));
		}
		return builder;
	}

	/**
	 * 邀请玩家加入联盟
	 * 
	 * @param player
	 * @param targetId
	 */
	@SuppressWarnings("deprecation")
	public int invitePlayer(Player player, String targetId) {
		String guildId = player.getGuildId();
		GuildInfoObject guild = getGuildInfoObject(guildId);
		if (guild == null) {
			return Status.Error.GUILD_NOT_EXIST_VALUE;
		}
		
		Player targetPlayer = GlobalData.getInstance().makesurePlayer(targetId);
		if(targetPlayer == null){
			return Status.SysError.ACCOUNT_NOT_EXIST_VALUE;
		}
		// 操作涉及到跨服玩家,不支持
		if (CrossService.getInstance().isCrossPlayer(targetId)) {
			return Status.CrossServerError.CROSS_OPERATION_NOT_SUPPOERT_VALUE;
		}
		
		long inviteCd = GuildConstProperty.getInstance().getInviteMailCd();
		// 对该玩家的邀请入盟邮件处于cd状态
		if (HawkTime.getMillisecond() - LocalRedis.getInstance().getGuildInviteMailCd(player.getId(), targetId) < inviteCd * 1000l) {
			return Status.Error.GUILD_INVITE_IN_CD_VALUE;
		}
		//如果对方已经把自己加入了黑名单则直接返回一个成功迷惑对方.
		if (RelationService.getInstance().isBlacklist(targetId, player.getId())) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}

		String leaderId = guild.getLeaderId();
		Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
		GuildInviteMail.Builder mailBuilder = GuildInviteMail.newBuilder()
				.setGuildId(guildId)
				.setPlayerId(leaderId)
				.setName(leader.getName())
				.setIcon(leader.getIcon())
				.setBattlePoint(getGuildBattlePoint(guildId))
				.setMemberNum(getGuildMembers(guildId).size())
				.setMemberMaxNum(getGuildMemberMaxNum(guildId))
				.setGuildTag(guild.getTag())
				.setGuildName(guild.getName())
				.setGuildFlag(guild.getFlagId())
				.setInviteState(InviteState.NOTDEAL);
		String pfIcon = leader.getPfIcon();
		if (!HawkOSOperator.isEmptyString(pfIcon)) {
			mailBuilder.setPfIcon(pfIcon);
		}
		
		if (targetPlayer != null) {
			ChatService.getInstance().addWorldBroadcastMsg(Const.ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.INVITE_JOIN_GUILD, player, targetPlayer.getName());
		}
		
		MailService.getInstance().sendMail(MailParames.newBuilder()
				.setPlayerId(targetId)
				.setMailId(MailId.GUILD_INVITE)
				.addTitles(guild.getName(), guild.getTag())
				.addContents(mailBuilder)
				.build());

		LocalRedis.getInstance().addGuildInviteMailCd(player.getId(), targetId);
		BehaviorLogger.log4Service(player, Source.GUILD_OPRATION, Action.GUILD_INVITE,
				Params.valueOf("guildId", player.getGuildId()),
				Params.valueOf("targetPlayer", targetId));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 推送联盟邀请函
	 * 
	 * @param player
	 */
	public void pushInvitationLetter(Player player) {
		// 玩家在副本中,不推送
		if(player.isInDungeonMap()){
			return;
		}
		
		if (!player.isActiveOnline()) {
			return;
		}
		if (player.getCityLv() < GuildConstProperty.getInstance().getAllianceInvitationCityLevel()) {
			return;
		}
		long currTime = HawkTime.getMillisecond();
		DailyDataEntity dailyEntity = player.getData().getDailyDataEntity();
		int countLimit = GuildConstProperty.getInstance().getAllianceInvitationNum();
		if (dailyEntity.getGuildPushTimes() >= countLimit) {
			return;
		}
		long timeLimit = GuildConstProperty.getInstance().getAllianceInvitationCD() * 1000l;
		if (currTime < timeLimit + dailyEntity.getLastPushTime()) {
			return;
		}
		List<GuildInfoObject> guildList = new ArrayList<>(guildData.values());
		Collections.shuffle(guildList);
		Optional<GuildInfoObject> opGuild = null;
		if(!player.hasGuild()){
			opGuild = noGuildMemberGuildChoise(guildList);
		}else{
			//判断死盟推荐次数是否达到上限
			int deadRecommendCnt = dailyEntity.getDeadGuildRefuseRecommendCnt();
			if(deadRecommendCnt >= GuildConstProperty.getInstance().getDeadGuildMemberRecommendMaxCnt()){
				return; //再也不推荐
			}
			GuildInfoObject guild = guildData.get(player.getGuildId());
			if(guild.isLeader(player.getId())){
				return; //盟主不推荐
			}
			GuildActiveType activeType = guild.getActiveType();
			if(activeType != null && activeType != GuildActiveType.NONE){
				opGuild = deadGuildMemberGuildChoise(guildList);
			}else{
				return;
			}
		}
		if(opGuild == null || !opGuild.isPresent()){ //实在是找不到推荐的联盟了
			return;
		}
		GuildInfoObject guild = opGuild.get();
		Player snapshotPB = GlobalData.getInstance().makesurePlayer(guild.getLeaderId());
		if (snapshotPB == null) {
			HawkLog.errPrintln("guildservice pushInvitationLetter, leader null, playerId: {}, leaderId: {}", player.getId(), guild.getLeaderId());
			return;
		}
		
		InvitationLetter.Builder builder = InvitationLetter.newBuilder();
		builder.setGuildId(guild.getId());
		builder.setGuildTag(guild.getTag());
		builder.setGuildName(guild.getName());
		builder.setGuildFlag(guild.getFlagId());
		builder.setCreateTime(currTime);
		
		builder.setIcon(snapshotPB.getIcon());
		if (!HawkOSOperator.isEmptyString(snapshotPB.getPfIcon())) {
			builder.setPfIcon(snapshotPB.getPfIcon());
		}
		builder.setVipLvl(snapshotPB.getVipLevel());
		builder.setCommon(BuilderUtil.genPlayerCommonBuilder(snapshotPB));
		if(player.hasGuild()){
			GuildInfoObject curGuild = guildData.get(player.getGuildId());
			if(curGuild.getActiveType() == GuildActiveType.DEADA){
				builder.setRecommendType(GuildRecommendType.DEADGUILDA);
			}else if(curGuild.getActiveType() == GuildActiveType.DEADB){
				builder.setRecommendType(GuildRecommendType.DEADGUILDB);
			}else if(curGuild.getActiveType() == GuildActiveType.DEADAB){
				builder.setRecommendType(GuildRecommendType.DEADGUILDA);
			}
		}else{
			builder.setRecommendType(GuildRecommendType.NOGUILD);
		}
		dailyEntity.setLastPushTime(currTime);
		dailyEntity.setGuildPushTimes(dailyEntity.getGuildPushTimes() + 1);
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_INVITATION_LETTER_PUSH_S, builder));
	}

	@MessageHandler
	public void migrateOutPlayer(MigrateOutPlayerMsg msg) {
		msg.setResult(Boolean.FALSE);

		String playerId = msg.getPlayer().getId();
		String guildId = this.getPlayerGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			this.onQuitGuild(guildId, playerId);
		}

		msg.setResult(Boolean.TRUE);
	}

	/**
	 * 发送联盟坐标指引奖励
	 * 
	 * @param player
	 */
	public void sendGuildPointGuideAward(Player player) {
		List<ItemInfo> guildPointGuideAwardList = GuildConstProperty.getInstance().getGuildPointGuideAwardList();
		if (guildPointGuideAwardList.isEmpty()) {
			return;
		}
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(guildPointGuideAwardList);
		awardItems.rewardTakeAffectAndPush(player, Action.GUILD_POINT_GUIDE_REWARD, false, null);
	}

	/**
	 * 获取联盟坐标信息
	 * 
	 * @param guildId
	 * @return
	 */
	public Map<Integer, GuildSign> getGuildSignMap(String guildId) {
		if (guildSignMaps.containsKey(guildId)) {
			return guildSignMaps.get(guildId);
		} else {
			return Collections.emptyMap();
		}
	}

	/**
	 * 添加联盟标记
	 * 
	 * @param player
	 * @param guildSign
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public int onAddGuildSign(Player player, GuildSign guildSign) {
		String guildId = player.getGuildId();

		Map<Integer, GuildSign> signMap = guildSignMaps.get(guildId);
		if (signMap == null) {
			signMap = new HashMap<>();
			guildSignMaps.put(guildId, signMap);
		}
		// 放置位置检测
		for (GuildSign sign : signMap.values()) {
			if (sign.getPosX() == guildSign.getPosX() && sign.getPosY() == guildSign.getPosY()) {
				return Status.Error.GUILD_SIGN_POINT_REPEAT_VALUE;
			}
		}
		signMap.put(guildSign.getId(), guildSign);
		LocalRedis.getInstance().addGuildSign(guildId, guildSign);
		// 同步联盟信息
		broadcastGuildInfo(guildId);
		//联盟红点
		notifyGuildFavouriteRedPoint(guildId, GsConst.GuildFavourite.TYPE_GUILD_SIGN, 0);
		// 发送联盟消息
		ChatService.getInstance().addWorldBroadcastMsg(ChatType.CHAT_ALLIANCE, Const.NoticeCfgId.ALLIANCE_SIGN_NOTICE,
				player, guildSign.getPosX(), guildSign.getPosY(), guildSign.getId(), guildSign.getInfo());
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 
	 * @param guildId
	 * @param type
	 * @param param type不一样 param代表的含义不一样
	 */
	public void notifyGuildFavouriteRedPoint(String guildId, int type, int param) {
		try {
			Collection<String> memberIds = GuildService.getInstance().getGuildMembers(guildId);
			for (String memberId : memberIds) {
				Player player = GlobalData.getInstance().getActivePlayer(memberId);
				if (player != null) {
					player.getPush().notifyGuildFavouriteRedPoint(type, param);
				}
			}
		} catch(Exception e) {
			HawkException.catchException(e);
		}		
	}
	/**
	 * 移除联盟标记
	 * 
	 * @param player
	 * @param signId
	 * @return
	 */
	public int onRemoveGuildSign(Player player, int signId) {
		String guildId = player.getGuildId();

		if (!checkGuildAuthority(player.getId(), AuthId.ALLIANCE_SIGN)) {
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}

		Map<Integer, GuildSign> signMap = guildSignMaps.get(guildId);
		if (signMap != null) {
			signMap.remove(signId);
		}
		LocalRedis.getInstance().removeGuildSign(guildId, signId);
		// 同步联盟信息
		broadcastGuildInfo(guildId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/***
	 * 野人玩家查找推荐联盟
	 * @param list
	 * @return
	 */
	private Optional<GuildInfoObject> noGuildMemberGuildChoise(List<GuildInfoObject> list){
		Optional<GuildInfoObject> opGuild = null;
		for(int i = 0 ; i < 4 ; i ++){
			switch (i) {
			case 0:
				opGuild = find80PersentAndHaveMemberGuild(list);
				break;
			case 1:
				opGuild = find80PersentGuild(list);
				break;
			case 2:
				opGuild = findCanEnterGuild(list);
				break;
			case 3:
				opGuild = findCanEnterDeadGuild(list);
				break;
			default:
				break;
			}
			if(opGuild != null && opGuild.isPresent()){
				break;
			}
		}
		return opGuild;
	}
	
	/***
	 * 死盟所属玩家联盟推荐
	 * @param list
	 * @return
	 */
	private Optional<GuildInfoObject> deadGuildMemberGuildChoise(List<GuildInfoObject> list){
		Optional<GuildInfoObject> opGuild = null;
		for(int i = 0 ; i < 3 ; i ++){
			switch (i) {
			case 0:
				opGuild = find80PersentAndHaveMemberGuild(list);
				break;
			case 1:
				opGuild = find80PersentGuild(list);
				break;
			case 2:
				opGuild = findCanEnterGuild(list);
				break;
			default:
				break;
			}
			if(opGuild != null && opGuild.isPresent()){
				break;
			}
		}
		return opGuild;
	}
	
	/***
	 * 查找公开招募低于80%且至少有10人的联盟(80%是配置的，具体看配置)
	 * @param list
	 * @return
	 */
	private Optional<GuildInfoObject> find80PersentAndHaveMemberGuild(List<GuildInfoObject> list){
		Predicate<GuildInfoObject> predicate = new Predicate<GuildInfoObject>() {
			@Override
			public boolean test(GuildInfoObject guild) {
				if (guild.isNeedPermition()) {
					return false;
				}				
				int memNum = getGuildMemberNum(guild.getId());
				int memMaxNum = getGuildMemberMaxNum(guild.getId());
				if (memNum >= memMaxNum) {
					return false;
				}
				GuildActiveType activeType = guild.getActiveType();
				if(activeType != null && activeType != GuildActiveType.NONE){
					return false; //死盟不要
				}
				float per = (float)memNum / (float)memMaxNum;
				if(per > GuildConstProperty.getInstance().getGuildRecommendPersent()){
					return false;
				}
				final int minMemberCnt = GuildConstProperty.getInstance().getRecommendMinGuildMember(); //至少有10人才可以加入
				if(memNum < minMemberCnt){
					return false;
				}
				return true;
			}
		};
		return list.parallelStream().filter(predicate).findAny();
	}
	
	/***
	 * 查找公开招募低于80%的联盟(80%是配置的，具体看配置)
	 * @return
	 */
	private Optional<GuildInfoObject> find80PersentGuild(List<GuildInfoObject> list){
		Predicate<GuildInfoObject> predicate = new Predicate<GuildInfoObject>() {
			@Override
			public boolean test(GuildInfoObject guild) {
				if (guild.isNeedPermition()) {
					return false;
				}				
				int memNum = getGuildMemberNum(guild.getId());
				int memMaxNum = getGuildMemberMaxNum(guild.getId());
				if (memNum >= memMaxNum) {
					return false;
				}
				GuildActiveType activeType = guild.getActiveType();
				if(activeType != null && activeType != GuildActiveType.NONE){
					return false; //死盟不要
				}
				float per = (float)memNum / (float)memMaxNum;
				if(per > GuildConstProperty.getInstance().getGuildRecommendPersent()){
					return false;
				}
				return true;
			}
		};
		return list.parallelStream().filter(predicate).findAny();
	}
	
	/***
	 * 寻找公开招募未满员的联盟
	 * @param guild
	 * @return
	 */
	private Optional<GuildInfoObject> findCanEnterGuild(List<GuildInfoObject> list){
		Predicate<GuildInfoObject> predicate = new Predicate<GuildInfoObject>() {
			@Override
			public boolean test(GuildInfoObject guild) {
				if (guild.isNeedPermition()) {
					return false;
				}
				GuildActiveType activeType = guild.getActiveType();
				if(activeType != null && activeType != GuildActiveType.NONE){
					return false;
				}
				int memNum = getGuildMemberNum(guild.getId());
				int memMaxNum = getGuildMemberMaxNum(guild.getId());
				if (memNum >= memMaxNum) {
					return false;
				}
				return true;
			}
		};
		return list.parallelStream().filter(predicate).findAny();
	}
	
	/***
	 * 寻找死盟未满员的联盟
	 * @param guild
	 * @return
	 */
	private Optional<GuildInfoObject> findCanEnterDeadGuild(List<GuildInfoObject> list){
		Predicate<GuildInfoObject> predicate = new Predicate<GuildInfoObject>() {
			@Override
			public boolean test(GuildInfoObject guild) {
				if (guild.isNeedPermition()) {
					return false;
				}
				int memNum = getGuildMemberNum(guild.getId());
				int memMaxNum = getGuildMemberMaxNum(guild.getId());
				if (memNum >= memMaxNum) {
					return false;
				}
				return true;
			}
		};
		return list.parallelStream().filter(predicate).findAny();
	}
	
	public void checkDeadGuild(String guildId){
		GuildInfoObject guildInfo = guildData.get(guildId);
		boolean deadA = deadGuildA(guildId);
		boolean deadB = deadGuildB(guildId);
		if(deadA && deadB){
			guildInfo.setActiveType(GuildActiveType.DEADAB);
			logger.info("find deadGuild, GuildName:" + guildInfo.getEntity().getName() + "is DeadGuildAB");
		}else if(deadA){
			guildInfo.setActiveType(GuildActiveType.DEADA);
			logger.info("find deadGuild, GuildName:" + guildInfo.getEntity().getName() + "is DeadGuildA");
		}else if(deadB){
			guildInfo.setActiveType(GuildActiveType.DEADB);
			logger.info("find deadGuild, GuildName:" + guildInfo.getEntity().getName() + "is DeadGuildB");
		}else{
			guildInfo.setActiveType(GuildActiveType.NONE);
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(guildInfo.getLeaderId());
			if(account !=null && !Objects.equals(account.getPlayerName(), guildInfo.getLeaderName())){
				guildInfo.updateGuildLeader(guildInfo.getLeaderId(), guildInfo.getLeaderId(), account.getPlayerName());
			}
		}
	}

	/***
	 * A类死盟(盟主离线72小时)
	 * @return true : A类死盟
	 */
	public boolean deadGuildA(String guildId){
		GuildInfoObject guildInfo = guildData.get(guildId);
		if(guildInfo == null){
			return true;
		}
		String leaderId = guildInfo.getLeaderId();
		boolean online = GlobalData.getInstance().isOnline(leaderId);
		if (online) {
			return false;
		}
		AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(leaderId);
		if (account == null) {
			return false;
		}
		int logoutHours = HawkTime.getHoursInterval(HawkTime.getMillisecond(), account.getLogoutTime());
		if(logoutHours >= GuildConstProperty.getInstance().getGuildLeaderLogoutTime()){
			return true;
		}
		return false;
	}
	
	/***
	 * B类死盟(70%玩家死号)
	 * @return true : B类死盟
	 */
	public boolean deadGuildB(String guildId){
		Collection<String> members = getGuildMembers(guildId);
		int memMax = getGuildMemberMaxNum(guildId);
		int deadMembers = 0;
		for(String playerId : members){
			if(GlobalData.getInstance().isOnline(playerId)){
				continue;
			}
			GuildMemberObject guildMember = playerGuild.get(playerId);
			if(guildMember == null){
				continue;
			}
			if(guildMember.getLogoutTime() != 0){
				int hours = HawkTime.getHoursInterval(HawkTime.getMillisecond(), guildMember.getLogoutTime());
				if(hours >= GuildConstProperty.getInstance().getGuildMemberLogoutTime()){
					deadMembers ++;
				}
			}else{
				deadMembers ++;
			}
		}
		float deadPersent = (float)deadMembers / (float)memMax;
		if(deadPersent >= GuildConstProperty.getInstance().getGuildDeadMemberPersent()){
			return true;
		}
		return false;
	}
	
	/**
	 * 申请联盟官员
	 * @param player
	 * @param officeId
	 * @return
	 */
	public int onApplyGuildOfficer(Player player, int officeId) {
		AllianceOfficialCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, officeId);
		//  联盟官职id错误
		if (cfg == null || !cfg.canAppoint()) {
			return Status.Error.GUILD_OFFICEID_ERROR_VALUE;
		}
		GuildMemberObject member = getGuildMemberObject(player.getId());
		// 已拥有官职
		if(member.getOfficeId()!=GuildOffice.NONE.value()){
			return Status.Error.GUILD_ALREADY_IS_OFFICER_VALUE;
		}
		// 添加联盟官员申请信息
		LocalRedis.getInstance().removeGuildOfficeApply(player.getGuildId(), player.getId());
		LocalRedis.getInstance().addGuildOfficeApply(player.getGuildId(), officeId, player.getId());
		player.getPush().syncGuildInfo();
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取联盟官员申请列表
	 * @param officeId
	 * @return
	 */
	public GetOfficerApplyListResp.Builder getOfficerApplyList(String guildId, int officeId) {
		GetOfficerApplyListResp.Builder builder = GetOfficerApplyListResp.newBuilder();
		Map<String, String> applyMap = LocalRedis.getInstance().getGuildOfficeApply(guildId, officeId);
		if (applyMap != null) {
			for (String playerId : applyMap.keySet()) {
				GuildMemeberInfo.Builder info = genGuildMemberInfo(playerId);
				if (info != null) {
					builder.addApplicant(info);
				}
			}
		}
		return builder;
	}

	/**
	 * 任命联盟官员
	 * @param player
	 * @param tarPlayerId
	 * @param officeId
	 * @return
	 */
	public int onAppointGuildOfficer(Player player, String tarPlayerId, int officeId) {
		AllianceOfficialCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceOfficialCfg.class, officeId);
		// 联盟官职id错误
		if(cfg == null || !cfg.canAppoint()){
			return Status.Error.GUILD_OFFICEID_ERROR_VALUE;
		}
		// 不在同一个联盟
		if(!isInTheSameGuild(player.getId(), tarPlayerId)){
			return Status.Error.GUILD_NOT_SAME_VALUE;
		}
		GuildMemberObject target = getGuildMemberObject(tarPlayerId);

		if (target.getAuthority() == GuildAuthority.L5_VALUE){
			return Status.Error.GUILD_LOW_AUTHORITY_VALUE;
		}
		GuildMemberObject officer = getGuildOfficer(target.getPlayerId(), officeId);
		// 已存在官员
		if(officer != null && !officer.getPlayerId().equals(tarPlayerId)){
			return Status.Error.GUILD_OFFICE_ALREADY_APPOINT_VALUE;
		}
		// 提升官员的权限等级为4级
		if (target.getAuthority() != Const.GuildAuthority.L4_VALUE) {
			target.updateMemberAuthority(Const.GuildAuthority.L4_VALUE);
		}
		
		target.updateOfficeId(officeId);
		String guildId = player.getGuildId();
		
		// 移除该成员以及该官职的申请信息
		LocalRedis.getInstance().removeGuildOfficeApply(guildId, tarPlayerId);
		LocalRedis.getInstance().removeGuildOfficeApply(guildId, officeId);
		// 同步联盟信息
		broadcastGuildInfo(guildId);
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	
	/**
	 * 解除联盟官员
	 * @param player
	 * @param tarPlayerId
	 * @return
	 */
	public int onDismissGuildOfficer(Player player, String tarPlayerId) {
		// 不在同一个联盟
		if (!isInTheSameGuild(player.getId(), tarPlayerId)) {
			return Status.Error.GUILD_NOT_SAME_VALUE;
		}
		GuildMemberObject target = getGuildMemberObject(tarPlayerId);
		if (target == null || target.getOfficeId() == GuildOffice.NONE.value()) {
			return Status.SysError.SUCCESS_OK_VALUE;
		}
		if (target.getOfficeId() == GuildOffice.LEADER.value() || !target.getGuildId().equals(player.getGuildId())) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		target.updateOfficeId(GuildOffice.NONE.value());
		
		// 同步联盟信息
		Player targetPlayer = GlobalData.getInstance().getActivePlayer(tarPlayerId);
		if (targetPlayer != null) {
			targetPlayer.getPush().syncGuildInfo();
		}
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 根据官职id获取联盟官员
	 * @param guildId
	 * @param officeId
	 * @return
	 */
	public GuildMemberObject getGuildOfficer(String guildId, int officeId) {
		
		if (officeId == GuildOffice.NONE.value()) {
			return null;
		}
		
		Collection<String> ids = getGuildMembers(guildId);
		if (ids == null) {
			return null;
		}
	
		for (String id : ids) {
			GuildMemberObject member = getGuildMemberObject(id);
			if (member != null && member.getOfficeId() == officeId) {
				return member;
			}
		}
	
		return null;
	}
	
	/**
	 * 获取玩家申请的官职id
	 * @param playerId
	 * @param guildId
	 * @return
	 */
	public int getSelfApplyOfficeId(String playerId, String guildId) {
		for (GuildOffice office : GuildOffice.values()) {
			if (office == GuildOffice.NONE || office == GuildOffice.LEADER) {
				continue;
			}
			Map<String, String> applyMap = LocalRedis.getInstance().getGuildOfficeApply(guildId, office.value());
			if (applyMap != null && applyMap.containsKey(playerId)) {
				return office.value();
			}
		}
		return GuildOffice.NONE.value();
	}
	
	/**
	 * 获取联盟的创建时间
	 * @param guildId
	 * @return 
	 */
	public long getGuildCreateTime(String guildId){
		if (!HawkOSOperator.isEmptyString(guildId)) {
			if (guildData.containsKey(guildId)) {
				return guildData.get(guildId).getCreateTime();
			}
		}

		return HawkTime.getMillisecond();
	}
	
	/***
	 * 设置联盟成员离线时间
	 * @param playerId
	 * @param now
	 */
	public void onPlayerLogout(String playerId, long now){
		GuildMemberObject guildMember = playerGuild.get(playerId);
		if(guildMember != null){
			guildMember.setLogoutTime(now);
		}
	}
	
	
	/**
	 * 判定联盟成员权限修改是否合法
	 * @param operateAuth
	 * @param oldAuth
	 * @param newAuth
	 * @return
	 */
	public boolean canChangeAuth(int operateAuth, int oldAuth, int newAuth) {
		// 权限无变动
		if(oldAuth == newAuth){
			return false;
		}
		
		// 只能对1-4阶玩家进行阶级调整
		if (oldAuth < GuildAuthority.L1_VALUE || oldAuth > GuildAuthority.L4_VALUE) {
			return false;
		}
		
		// 只能将目标在1-4阶中进行阶级调整
		if (newAuth < GuildAuthority.L1_VALUE || newAuth > GuildAuthority.L4_VALUE) {
			return false;
		}
		
		// 修改者权限须高于被修改者修改前后的权限
		if (operateAuth <= oldAuth || operateAuth <= newAuth) {
			return false;
		}
		return true;
	}
	
	/**
	 * 获取联盟任务列表
	 * @param guildId
	 * @return
	 */
	public List<GuildTaskItem> getGuildTaskList(String guildId) {
		List<GuildTaskItem> taskList = guildTaskMap.get(guildId);
		if (taskList == null) {
			return Collections.emptyList();
		}
		return taskList;
	}
	
	/**
	 * 联盟任务数据落地
	 * @param guildId
	 */
	public void updateGuildTaskList() {
		for (Entry<String, List<GuildTaskItem>> entry : guildTaskMap.entrySet()) {
			LocalRedis.getInstance().updateGuildTask(entry.getKey(), entry.getValue());
		}
	}
	
	/**
	 * 任务事件处理
	 * 
	 * @param event
	 */
	public void postGuildTaskMsg(GuildTaskEvent event) {
		if (!HawkOSOperator.isEmptyString(event.getGuildId())) {
			HawkApp.getInstance().postMsg(this, GuildTaskMsg.valueOf(event));
		}
	}
	
	/**
	 * 同步全盟成员联盟任务信息
	 * @param guildId
	 */
	public void syncGuildTaskInfo(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		if (guildMemberData.containsKey(guildId)) {
			for (String playerId : guildMemberData.get(guildId)) {
				Player player = GlobalData.getInstance().getActivePlayer(playerId);
				if (player != null && player.isActiveOnline()) {
					syncPlayerGuildTaskInfo(player);
				}
			}
		}
	}
	
	/**
	 * 同步玩家联盟任务信息
	 * @param player
	 */
	public void syncPlayerGuildTaskInfo(Player player) {
		GuildTaskInfo.Builder builder = genGuildTaskInfo(player.getId());
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_TASK_INFO_ALL_SYNC, builder));
	}
	
	/**
	 * 回流玩家联盟任务刷新检测
	 * @param player
	 */
	public void checkAndResetTask(Player player) {
		GuildMemberObject guildMember = getGuildMemberObject(player.getId());
		if (guildMember == null) {
			return;
		}
		// 距离上次刷新时间超过1天的,表明是回流玩家,刷新任务进度
		if (HawkTime.getMillisecond() - guildMember.getTaskResetTime() > GsConst.DAY_MILLI_SECONDS) {
			guildMember.resetRewardedTask();
			HawkLog.logPrintln("GuildService checkAndResetTask, playerId:{}", player.getId());
		}

	}
	
	/**
	 * 同步玩家联盟任务信息变更
	 * @param player
	 * @param taskIds
	 */
	public void syncPlayerGuildTaskChange(Player player, List<Integer> taskIds) {
		GuildTaskInfo.Builder builder = GuildTaskInfo.newBuilder();
		GuildMemberObject member = getGuildMemberObject(player.getId());
		// 联盟任务列表
		List<GuildTaskItem> taskItems = getGuildTaskList(member.getGuildId());
		// 玩家已领奖列表
		List<Integer> rewaredList = member.getRewardedTaskList();

		for (GuildTaskItem task : taskItems) {
			int taskId = task.getCfgId();
			if (taskIds != null && !taskIds.contains(taskId)) {
				continue;
			}
			TashInfo.Builder taskInfo = TashInfo.newBuilder();
			taskInfo.setTaskId(task.getCfgId());
			taskInfo.setTaskValue(task.getValue());
			TaskStatus status = TaskStatus.valueOf(task.getState());
			if (status == TaskStatus.NOT_REWARD && rewaredList.contains(task.getCfgId())) {
				status = TaskStatus.TOOKEN;
			}
			taskInfo.setStatus(status);
			builder.addTaskInfo(taskInfo);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_TASK_INFO_CHANGE_PUSH, builder));
	}
	
	/**
	 * 构建玩家联盟任务信息
	 * 
	 * @param player
	 * @return
	 */
	public GuildTaskInfo.Builder genGuildTaskInfo(String playerId) {
		GuildMemberObject member = getGuildMemberObject(playerId);
		GuildTaskInfo.Builder builder = GuildTaskInfo.newBuilder();
		// 联盟任务列表
		List<GuildTaskItem> taskItems = getGuildTaskList(member.getGuildId());
		// 玩家已领奖列表
		List<Integer> rewaredList = member.getRewardedTaskList();

		for (GuildTaskItem task : taskItems) {
			TashInfo.Builder taskInfo = TashInfo.newBuilder();
			taskInfo.setTaskId(task.getCfgId());
			taskInfo.setTaskValue(task.getValue());
			TaskStatus status = TaskStatus.valueOf(task.getState());
			if (status == TaskStatus.NOT_REWARD && rewaredList.contains(task.getCfgId())) {
				status = TaskStatus.TOOKEN;
			}
			taskInfo.setStatus(status);
			builder.addTaskInfo(taskInfo);
		}

		return builder;
	}

	/**
	 * 领取联盟任务奖励
	 * 
	 * @param player
	 * @param taskIds
	 *            领奖任务id列表
	 * @return
	 */
	public Integer getGuildTaskReward(Player player, List<Integer> taskIds, AwardItems awardItems) {
		String guildId = player.getGuildId();
		GuildMemberObject member = getGuildMemberObject(player.getId());
		if (HawkOSOperator.isEmptyString(guildId) || member == null) {
			return Status.Error.GUILD_PLAYER_HASNOT_GUILD_VALUE;

		}
		// 退盟冷却时长判定
//		if (HawkTime.getMillisecond() - member.getQuitGuildTime() < GuildConstProperty.getInstance().getAllianceTaskCD()) {
//			return Status.Error.GUILD_QUITTIME_NOT_ENOUGH_VALUE;
//		}

		// 联盟任务列表
		List<GuildTaskItem> taskItems = getGuildTaskList(guildId);
		// 玩家已领奖列表
		List<Integer> rewaredList = member.getRewardedTaskList();
		// 可领奖列表
		List<Integer> toRewardList = new ArrayList<>();
		StringBuilder sb = new StringBuilder();
		for (GuildTaskItem guildTask : taskItems) {
			int cfgId = guildTask.getCfgId();
			// 本次领取的任务条件已完成,且该玩家今日未领取
			if (taskIds.contains(cfgId) && guildTask.getState() == TaskStatus.NOT_REWARD_VALUE && !rewaredList.contains(cfgId)) {
				toRewardList.add(cfgId);
				sb.append(cfgId).append(",");
			}
		}

		// 没有可领取的奖励
		if (toRewardList.isEmpty()) {
			return Status.Error.GUILD_NO_TASK_REWARD_VALUE;
		}

		// 记录已领取奖励id
		member.addRewardedTask(toRewardList);
		// 同步玩家联盟任务信息
		syncPlayerGuildTaskChange(player, toRewardList);
		List<ItemInfo> rewardItems = new ArrayList<>();
		for (Integer cfgId : toRewardList) {
			AllianceTaskRewardCfg rewardCfg = getTaskRewardCfg(cfgId, player.getCityLv());
			if (rewardCfg != null) {
				rewardItems.addAll(rewardCfg.getRewardItem());
			}
		}
		awardItems.addItemInfos(rewardItems);
		sb.deleteCharAt(sb.length() - 1);

		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.GUILD_TASK_AWARD, Params.valueOf("taskIds", sb.toString()));

		return Status.SysError.SUCCESS_OK_VALUE;

	}

	/**
	 * 刷新联盟任务进度
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onRefreshMission(GuildTaskMsg msg) {
		GuildTaskEvent event = msg.getEvent();
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}

		// 事件触发任务列表
		List<GuildTaskType> touchTasks = event.touchTasks();
		if (touchTasks == null || touchTasks.isEmpty()) {
			return;
		}

		// 联盟任务数据
		List<GuildTaskItem> taskList = getGuildTaskList(guildId);
		boolean hasFinish = false;
		for (GuildTaskItem taskItem : taskList) {
			// 任务实体
			if (taskItem == null) {
				continue;
			}

			if (taskItem.getState() != GuildTask.TaskStatus.NOT_REACH_VALUE) {
				continue;
			}

			AllianceTaskCfg taskCfg = HawkConfigManager.getInstance().getConfigByKey(AllianceTaskCfg.class, taskItem.getCfgId());
			GuildTaskCfgItem cfgItem = taskCfg.getTaskItem();
			// 任务类型
			GuildTaskType taskType = cfgItem.getType();

			// 不触发此类型任务
			if (!touchTasks.contains(taskType)) {
				continue;
			}

			// 刷新任务
			IGuildTask itask = GuildTaskContext.getInstance().getTask(taskType);
			boolean update = itask.refreshTask(guildId, event, taskItem, cfgItem);
			if (update) {
				hasFinish = itask.checkTaskFinish(taskItem, cfgItem);
			}

			// 联盟数据同步,有任务达成时,同步全部数据
			if (hasFinish) {
				syncGuildTaskInfo(guildId);
			}
		}
	}

	/**
	 * 联盟签到
	 * 
	 * @param player
	 * @return
	 */
	public int onGuildSignature(Player player) {
		GuildMemberObject member = getGuildMemberObject(player.getId());
		// 没有联盟
		if (member == null || HawkOSOperator.isEmptyString(member.getGuildId())) {
			return Status.Error.GUILD_NO_JOIN_VALUE;
		}
		long currTime = HawkTime.getMillisecond();
		long lastSignTime = member.getLastSingTime();

		// 已签到
		if (HawkTime.isSameDay(currTime, lastSignTime)) {
			return Status.Error.GUILD_ALREADY_SIGN_VALUE;
		}
		int signTimes = member.getSignTimes() + 1;
		member.setSignTimes(signTimes);
		member.setLastSingTime(currTime);
		AllianceSignatureCfg cfg = getSignCfg(signTimes);
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItemInfos(cfg.getRewardItems());
		awardItems.rewardTakeAffectAndPush(player, Action.GUILD_SIGN, true);
		syncGuildSignatureInfo(player);
		BehaviorLogger.log4Service(player, Source.USER_OPERATION, Action.GUILD_SIGN, Params.valueOf("signTimes", member.getSignTimes()));
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 获取联盟签到奖励配置
	 * 
	 * @param signTimes
	 * @return
	 */
	public AllianceSignatureCfg getSignCfg(int signTimes) {
		ConfigIterator<AllianceSignatureCfg> it = HawkConfigManager.getInstance().getConfigIterator(AllianceSignatureCfg.class);
		for (AllianceSignatureCfg cfg : it) {
			if (signTimes >= cfg.getDayMin() && signTimes <= cfg.getDayMax()) {
				return cfg;
			}
		}
		return null;
	}

	/**
	 * 同步联盟签到信息
	 * 
	 * @param player
	 */
	public void syncGuildSignatureInfo(Player player) {
		GuildMemberObject member = getGuildMemberObject(player.getId());
		if (member == null) {
			return;
		}
		GuildSignInfo.Builder builder = GuildSignInfo.newBuilder();
		builder.setSignTimes(member.getSignTimes());
		builder.setSigned(HawkTime.isSameDay(HawkTime.getMillisecond(), member.getLastSingTime()));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.GUILD_SIGN_INFO_SYNC, builder));

	}

	/**
	 * 根据群组类型获取任务群组配置
	 * 
	 * @return
	 */
	private List<AllianceTaskGroupCfg> getTaskGroupCfgs(TaskType type) {
		long serverOpenAm0 = HawkTime.getAM0Date(new Date(GameUtil.getServerOpenTime())).getTime();
		int openDays = HawkTime.getCrossDay(HawkTime.getMillisecond(), serverOpenAm0, TimerEventEnum.ZERO_CLOCK.getClock());
		List<AllianceTaskGroupCfg> cfgList = new ArrayList<>();
		for (AllianceTaskGroupCfg cfg : HawkConfigManager.getInstance().getConfigIterator(AllianceTaskGroupCfg.class)) {
			if (cfg.getType() == type.getNumber() && openDays >= cfg.getOpenDay() && (cfg.getCloseDay() == 0 || openDays <= cfg.getCloseDay())) {
				cfgList.add(cfg);
			}
		}
		return cfgList;
	}

	/**
	 * 检查联盟任务刷新
	 */
	private void checkGuildTaskRefresh() {
		try {
			if(guildData.isEmpty()){
				return;
			}
			long currTime = HawkTime.getMillisecond();
			// 联盟战力从高到低排序  排除今天已经刷新的. 一次刷新 GsConst.GUILD_TASK_REFRESH_CNT_PER 条
			List<GuildInfoObject> guildList = guildData.values()
					.stream().filter(guildObj -> !HawkTime.isSameDay(currTime, guildObj.getTaskRefreshTime())) // 今天刷过的排除
					.sorted(Comparator.comparingLong(GuildInfoObject::getLastRankPower).reversed()) // 战力倒序
					.limit(GsConst.GUILD_TASK_REFRESH_CNT_PER)
					.collect(Collectors.toList());

			int refreshCnt = 0;
			for (GuildInfoObject guild : guildList) {
				if (refreshGuildTask(guild)) {
					refreshCnt++;
				}
				if (refreshCnt >= GsConst.GUILD_TASK_REFRESH_CNT_PER) {
					break;
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 刷新联盟任务
	 * 
	 * @param guildObj
	 */
	private boolean refreshGuildTask(GuildInfoObject guildObj) {
		String guildId = guildObj.getId();
		long currTime = HawkTime.getMillisecond();
		List<GuildTaskItem> taskList = getGuildTaskList(guildId);
		boolean empty = taskList.isEmpty();
		// 跨天或者任务列表为空时,重新刷新联盟任务
		if (!HawkTime.isSameDay(currTime, guildObj.getTaskRefreshTime()) || empty) {
			// 随机并更新联盟任务
			taskList = randomGuildTaskItems();
			List<Integer> taskIds = new ArrayList<>();
			for (GuildTaskItem item : taskList) {
				taskIds.add(item.getCfgId());
			}
			guildTaskMap.put(guildId, taskList);
			// 刷新联盟任务刷新时间
			guildObj.updateTaskRefreshTime(HawkTime.getMillisecond());

			// 清空昨日登录列表
			LocalRedis.getInstance().removeGuildLogin(guildId);

			// 清空昨日分享列表
			LocalRedis.getInstance().removeGuildShare(guildId);

			// 清空联盟成员任务完成列表
			clearMemberTaskInfo(guildId, empty);
			// 将在线成员加入今日登录列表-延时离散推送
			List<String> memberIds = getOnlineMembers(guildId);
			long delayTime = 5000 + HawkRand.randInt(10000);
			if (!memberIds.isEmpty()) {
				if (!dailyLoginMembers.containsKey(guildId)) {
					dailyLoginMembers.put(guildId, new HashSet<>(memberIds));
				} else {
					dailyLoginMembers.get(guildId).addAll(memberIds);
				}
				addDelayAction(delayTime, new HawkDelayAction() {
					@Override
					protected void doAction() {
						LocalRedis.getInstance().addGuildLoginMember(guildId, memberIds.toArray(new String[memberIds.size()]));
					}
				});
				// 联盟任务-成员登录跨天时刷新一波在线信息()
				postGuildTaskMsg(new MemberLoginTaskEvent(guildId));
			}
			// 推送联盟任务信息
			syncGuildTaskInfo(guildId);
			logger.info("guildtask refresh guildId: {}, taskIds: {}", guildId, taskIds);
			return true;
		}
		return false;
	}

	/**
	 * 重置联盟成员任务领奖信息
	 * 
	 * @param guildId
	 * @param forced 强制刷新
	 */
	private void clearMemberTaskInfo(String guildId, boolean forced) {
		long currTime = HawkTime.getMillisecond();
		Collection<String> memberIds = getGuildMembers(guildId);
		for (String playerId : memberIds) {
			GuildMemberObject member = getGuildMemberObject(playerId);
			if(member == null){
				continue;
			}
			// 超过N天未上线的,不进行重置,减少无用的dbqps
			if (currTime - member.getLogoutTime() > 1l * GuildConstProperty.getInstance().getTaskNoRefreshDay()* GsConst.DAY_MILLI_SECONDS) {
				continue;
			}
			
			// 今日未刷新的玩家进行任务领奖重置
			if (!HawkTime.isSameDay(currTime, member.getTaskResetTime()) || forced) {
				member.resetRewardedTask();
			}
		}

	}

	/**
	 * 获取联盟当前在线玩家列表
	 * 
	 * @param guildId
	 * @return
	 */
	public List<String> getOnlineMembers(String guildId) {
		List<String> memberIds = new ArrayList<>();
		for (String playerId : getGuildMembers(guildId)) {
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null && player.isActiveOnline()) {
				memberIds.add(player.getId());
			}
		}
		return memberIds;
	}

	/**
	 * 随机联盟任务
	 * 
	 * @return
	 */
	public List<GuildTaskItem> randomGuildTaskItems() {
		List<GuildTaskItem> taskList = new ArrayList<>();
		List<Integer> groupIds = new ArrayList<>();
		for (AllianceTaskGroupCfg cfg : getTaskGroupCfgs(TaskType.DAILY)) {
			groupIds.add(cfg.getGroupId());
		}
//		List<AllianceTaskGroupCfg> randomList = getTaskGroupCfgs(TaskType.STOCHASTIC);
//		List<Integer> wightList = new ArrayList<>();
//		for (AllianceTaskGroupCfg cfg : randomList) {
//			wightList.add(cfg.getWeight());
//		}
//		// 随机任务
//		int randCnt = GuildConstProperty.getInstance().getAllianceRandomTaskNo();
//		if (randCnt > 0) {
//			List<AllianceTaskGroupCfg> resultList = HawkRand.randomWeightObject(randomList, wightList, randCnt);
//			for (AllianceTaskGroupCfg cfg : resultList) {
//				groupIds.add(cfg.getGroupId());
//			}
//		}

		for (AllianceTaskCfg cfg : HawkConfigManager.getInstance().getConfigIterator(AllianceTaskCfg.class)) {
			if (groupIds.contains(cfg.getGroupId())) {
				GuildTaskItem taskItem = new GuildTaskItem(cfg.getId(), 0, TaskStatus.NOT_REACH_VALUE);
				taskList.add(taskItem);
			}
		}
		return taskList;
	}

	/**
	 * 获取联盟任务奖励
	 * 
	 * @param taskId
	 *            任务id
	 * @param cityLvl
	 *            大本等级
	 * @return
	 */
	public AllianceTaskRewardCfg getTaskRewardCfg(int taskId, int cityLvl) {
		for (AllianceTaskRewardCfg cfg : HawkConfigManager.getInstance().getConfigIterator(AllianceTaskRewardCfg.class)) {
			if (cfg.getTaskId() == taskId && cityLvl >= cfg.getCityLvlMin() && cityLvl <= cfg.getCityLvlMax()) {
				return cfg;
			}
		}
		return null;
	}
	
	/**
	 * 获取本服全盟基础数据(供跨服活动使用)
	 * @return
	 */
	public Map<byte[], byte[]> getAllGuildInfo() {
		Map<byte[], byte[]> infoMap = new HashMap<>();
		String serverId = GsConfig.getInstance().getServerId();
		for (GuildInfoObject guild : guildData.values()) {
			CGuildInfo.Builder builder = CGuildInfo.newBuilder();
			builder.setId(guild.getId());
			builder.setName(guild.getName());
			builder.setTag(guild.getTag());
			builder.setServerId(serverId);
			builder.setGuildFlag(guild.getFlagId());
			infoMap.put(guild.getId().getBytes(), builder.build().toByteArray());
		}
		return infoMap;
	}
	
	/**
	 * 获取跨服玩家guildId
	 * @param playerId
	 * @return
	 */
	public String getCPlayerGuildId(String playerId){
		if (!cPlayerGuild.containsKey(playerId)) {
			return null;
		}
		return cPlayerGuild.get(playerId);
	}
	
	/**
	 * 获取跨服联盟成员
	 * @param guildId
	 * @return
	 */
	public Collection<String> getCGuildMembers(String guildId) {
		if (Objects.isNull(guildId) || !cGuildMemberData.containsKey(guildId)) {
			return Collections.emptyList();
		}
		return new HashSet<String>(cGuildMemberData.get(guildId));
	}
	
	/**
	 * 玩家跨入其他服务器,加入/初始化其联盟信息
	 * @param player
	 * @param guildId
	 */
	@SuppressWarnings("deprecation")
	public boolean onCsPlayerEnter(Player player, String guildId, int guildAuth) {

		try {
			String playerId = player.getId();
			int termId = CrossActivityService.getInstance().getTermId();
			// 记录跨服玩家id
			RedisProxy.getInstance().addCrossPlayerId(playerId, termId);

			if (HawkOSOperator.isEmptyString(guildId)) {
				return false;
			}
			cPlayerGuild.put(player.getId(), guildId);
			cPlayerAuth.put(player.getId(), guildAuth);
			if (!cGuildData.containsKey(guildId)) {
				CsPlayer csPlayer = (CsPlayer) player;
				if (csPlayer.isCrossType(CrossType.TIBERIUM_VALUE)) {
					boolean isInLeaguaWar = TBLYSeasonService.getInstance().isInSeason(player);
					int twTermId = TBLYWarService.getInstance().getTermId();
					if (isInLeaguaWar) {
						int season = TBLYSeasonService.getInstance().getSeason();
						int term = TBLYSeasonService.getInstance().getTermId();
						twTermId = 10000 * season + term;
					}
					TWGuildData twGuildData = RedisProxy.getInstance().getTWGuildData(guildId, twTermId);
					if (twGuildData != null) {
						TWGuildInfoObject guildObj = new TWGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(twGuildData);
						cGuildData.put(guildId, guildObj);
					} else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter error, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, isInLeagua: {}, twTermId: {}",
								player.getId(), player.getName(), player.getServerId(), guildId, isInLeaguaWar, twTermId);
					}
				} else if (csPlayer.isCrossType(CrossType.STAR_WARS_VALUE)) {
					int twTermId = StarWarsActivityService.getInstance().getTermId();
					SWGuildData swGuildData = RedisProxy.getInstance().getSWGuildData(guildId, twTermId);
					if (swGuildData != null) {
						SWGuildInfoObject guildObj = new SWGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(swGuildData);
						cGuildData.put(guildId, guildObj);
					} else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter error, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, twTermId: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, twTermId);
					}
				} else if (csPlayer.isCrossType(CrossType.CYBORG_VALUE)) {
					int cwTermId = CyborgWarService.getInstance().getTermId();
					CWGuildData cwGuildData = CyborgWarRedis.getInstance().getCWGuildData(guildId, cwTermId);
					if (cwGuildData != null) {
						CWGuildInfoObject guildObj = new CWGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(cwGuildData);
						cGuildData.put(guildId, guildObj);
					} else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter error, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, cwTermId: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, cwTermId);
					}
				} else if (csPlayer.isCrossType(CrossType.CROSS_VALUE)) {
					CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, CrossActivityService.getInstance().getTermId());
					// 创建影子联盟信息
					if (guildInfo != null) {
						CsGuildInfoObject guildObj = new CsGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(guildInfo);
						cGuildData.put(guildId, guildObj);
						// 记录跨服联盟id
						RedisProxy.getInstance().addCrossGuild(guildId, termId);
					}
				}else if (csPlayer.isCrossType(CrossType.DYZZ_VALUE)) {
					int dyzzTerm = DYZZService.getInstance().getDYZZWarTerm();
					DYZZGuildData guildInfo = DYZZRedisData.getInstance().getDYZZGuildData(guildId, dyzzTerm);
					// 创建影子联盟信息
					if (guildInfo != null) {
						DYZZGuildInfoObject guildObj = new DYZZGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(guildInfo);
						cGuildData.put(guildId, guildObj);
					}else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, cwTermId: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, dyzzTerm);
					}
				}else if (csPlayer.isCrossType(CrossType.YQZZ_VALUE)) {
					int yqzzTerm = YQZZMatchService.getInstance().getDataManger().getStateData().getTermId();
					YQZZJoinGuild guildInfo = YQZZJoinGuild.loadData(yqzzTerm, guildId);
					// 创建影子联盟信息
					if (guildInfo != null) {
						YQZZGuildInfoObject guildObj = new YQZZGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(guildInfo);
						cGuildData.put(guildId, guildObj);
						DungeonRedisLog.log("YQZZLeaderName", "{} {} {} {}", guildObj.getId(), guildObj.getName(), guildObj.getLeaderId(), guildObj.getLeaderName());
					}else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, cwTermId: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, yqzzTerm);
					}
				}else if (csPlayer.isCrossType(CrossType.XHJZ_VALUE)) {
					int xhjzTerm = XHJZWarService.getInstance().getTermId();
					XHJZWarGuildData guildInfo = XHJZWarService.getInstance().loadGuildData(guildId);
					// 创建影子联盟信息
					if (guildInfo != null) {
						XHJZGuildInfoObject guildObj = new XHJZGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(guildInfo);
						cGuildData.put(guildId, guildObj);
						DungeonRedisLog.log("XHJZLeaderName", "{} {} {} {}", guildObj.getId(), guildObj.getName(), guildObj.getLeaderId(), guildObj.getLeaderName());
					}else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, xhjzTerm: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, xhjzTerm);
					}
				}else if (csPlayer.isCrossType(CrossType.XQHX_VALUE)) {
					int xqhxTerm = XQHXWarService.getInstance().getTermId();
					XQHXWarGuildData guildInfo = XQHXWarService.getInstance().loadGuildData(guildId);
					// 创建影子联盟信息
					if (guildInfo != null) {
						XQHXGuildInfoObject guildObj = new XQHXGuildInfoObject(new GuildInfoEntity());
						guildObj.setGuildInfo(guildInfo);
						cGuildData.put(guildId, guildObj);
						DungeonRedisLog.log("XQHXLeaderName", "{} {} {} {}", guildObj.getId(), guildObj.getName(), guildObj.getLeaderId(), guildObj.getLeaderName());
					}else {
						HawkLog.logPrintln("GuildService onCsPlayerEnter, playerId:{}, playerName:{}, playerServer:{}, guildId:{}, xqhxTerm: {}", player.getId(),
								player.getName(), player.getServerId(), guildId, xqhxTerm);
					}
				}
			}
			if (!cGuildMemberData.containsKey(guildId)) {
				cGuildMemberData.put(guildId, new ConcurrentHashSet<>());
			}
			cGuildMemberData.get(guildId).add(player.getId());
			// 更新跨服玩家基础数据
			CrossActivityService.getInstance().updatePlayerInfo(player, guildId);

			this.loadGuild4Cross(guildId);
			HawkLog.logPrintln("GuildService onCsPlayerEnter success, playerId:{}, playerName:{}, playerServer:{}, guildId:{}", player.getId(), player.getName(),
					player.getServerId(), guildId);

		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("GuildService onCsPlayerEnter error, playerId:{}, playerName:{}, playerServer:{}, guildId:{}", player.getId(), player.getName(),
					player.getServerId(), guildId);
		}
		return true;
	}
	
	/**
	 * 玩家迁出跨服服务器,将其从联盟信息中移除
	 * @param player
	 * @param guildId
	 */
	@SuppressWarnings("deprecation")
	public boolean onCsPlayerOut(Player player) {
		String playerId = player.getId();
		VoiceRoomManager.getInstance().onPlayerQuit(playerId);
		int termId = CrossActivityService.getInstance().getTermId();
		// 记录跨服玩家id
		RedisProxy.getInstance().removeCrossPlayerId(playerId, termId);
		String guildId = cPlayerGuild.get(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return true;
		}
		cPlayerGuild.remove(playerId);
		cPlayerAuth.remove(playerId);
		if (cGuildMemberData.containsKey(guildId)) {
			cGuildMemberData.get(guildId).remove(playerId);
		}
		// 移除官职缓存数据
		RedisProxy.getInstance().removePlayerOfficerId(playerId);
		RedisProxy.getInstance().removePlayerOfficerIdSet(playerId);
		HawkLog.logPrintln("GuildService onCsPlayerOut, playerId:{}, playerName:{}, playerServer:{}, guildId:{}", player.getId(), player.getName(), player.getServerId(), guildId);
		return true;

	}
	
	/**
	 * 构建跨服玩家联盟信息
	 * @param player
	 * @return
	 */
	public HPGuildInfoSync.Builder buildCGuildSyncInfo(Player player) {
		HPGuildInfoSync.Builder builder = HPGuildInfoSync.newBuilder();
		if (player.getData().getStatisticsEntity().getJoinGuildCnt() == 0) {
			builder.setJoinedGuild(false);
		} else {
			builder.setJoinedGuild(true);
		}
		String guildId = player.getGuildId();
		
		// 如果跨服玩家联盟ID为空,则进行兼容处理,取玩家的联盟数据
		if (HawkOSOperator.isEmptyString(guildId)) {
			try {
				int termId = CrossActivityService.getInstance().getTermId();
				CPlayerInfo.Builder playerInfo = RedisProxy.getInstance().getCrossPlayerInfo(player.getId(), termId);
				if (playerInfo != null && !HawkOSOperator.isEmptyString(playerInfo.getGuildId())) {
					guildId = playerInfo.getGuildId();
					// 记录跨服玩家id
					RedisProxy.getInstance().addCrossPlayerId(player.getId(), termId);
					cPlayerGuild.put(player.getId(), guildId);
					// 判定是否需要创建影子联盟
					if (!cGuildData.containsKey(guildId)) {
						CGuildInfo.Builder guildInfo = RedisProxy.getInstance().getCrossGuildInfo(guildId, CrossActivityService.getInstance().getTermId());
						// 创建影子联盟信息
						if (guildInfo != null) {
							CsGuildInfoObject guildObj = new CsGuildInfoObject(new GuildInfoEntity());
							guildObj.setGuildInfo(guildInfo);
							cGuildData.put(guildId, guildObj);
							// 记录跨服联盟id
							RedisProxy.getInstance().addCrossGuild(guildId, termId);
						}
					}
					if (!cGuildMemberData.containsKey(guildId)) {
						cGuildMemberData.put(guildId, new ConcurrentHashSet<>());
					}
					cGuildMemberData.get(guildId).add(player.getId());
				}
				HawkLog.logPrintln(" player cross guildinfo error deal, playerId: {}, name: {}, guildId: {}", player.getId(), player.getName(), guildId);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!HawkOSOperator.isEmptyString(guildId) && cGuildData.containsKey(guildId)) {
			GuildInfoObject guildObj = cGuildData.get(guildId);
			builder.setGuildId(guildId);
			builder.setServerId(GameUtil.strUUID2ServerId(guildId));
			builder.setGuildFlag(guildObj.getFlagId());
			builder.setGuildTag(guildObj.getTag());
			builder.setGuildLevel(1);
			builder.setGuildAuthority(player.getGuildAuthority());
			builder.setApplyNum(0);
			builder.setHelpNum(0);
			builder.setGuildName(guildObj.getName());
			builder.setJoinTime(getJoinGuildTime(player.getId()));
		} else {
			builder.setQuitGuildTime(getPlayerQuitGuildTime(player.getId()));
			HawkLog.logPrintln("buildCGuildSyncInfo no guild player, playerId: {}, name: {}, guildId: {}, guildExist:{}", player.getId(), player.getName(), guildId,
					!HawkOSOperator.isEmptyString(guildId) && cGuildData.containsKey(guildId));
		}
		return builder;
	}
	
	/**
	 * 加载跨服联盟玩家信息
	 * @param termId
	 */
	public void loadCrossGuildInfo() {
		try {
			CActivityInfo activityInfo = RedisProxy.getInstance().getCActivityInfo();
			int termId = activityInfo.getTermId();
			// 活动如果处于开启阶段,则加载跨服联盟数据
			if (termId == 0 || activityInfo.getState() != CrossActivityState.C_OPEN) {
				return;
			}
			Collection<String> guildids = RedisProxy.getInstance().getCrossGuilds(termId);
			Map<String, CGuildInfo.Builder> guildInfoMap = RedisProxy.getInstance().getCrossGuildInfo(termId, new ArrayList<>(guildids));
			for (CGuildInfo.Builder guildInfo : guildInfoMap.values()) {
				CsGuildInfoObject guildObj = new CsGuildInfoObject(new GuildInfoEntity());
				guildObj.setGuildInfo(guildInfo);
				cGuildData.put(guildInfo.getId(), guildObj);
			}

			Collection<String> playerIds = RedisProxy.getInstance().getCrossPlayerIds(termId);
			Map<String, CPlayerInfo.Builder> playerInfoMap = RedisProxy.getInstance().getCrossPlayerInfo(termId, new ArrayList<>(playerIds));
			for (CPlayerInfo.Builder playerInfo : playerInfoMap.values()) {
				if (!playerInfo.hasGuildId()) {
					continue;
				}
				String guildId = playerInfo.getGuildId();
				String playerId = playerInfo.getId();
				cPlayerGuild.put(playerId, guildId);
				if (playerInfo.hasGuildAuth()) {
					cPlayerAuth.put(playerId, playerInfo.getGuildAuth());
				}
				if (!cGuildMemberData.containsKey(guildId)) {
					cGuildMemberData.put(guildId, new ConcurrentHashSet<>());
				}
				cGuildMemberData.get(guildId).add(playerId);
			}

		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 跨服活动关闭时,清除本服的跨服联盟相关数据
	 */
	public void clearCrossGuildInfo(){
		cGuildMemberData = new ConcurrentHashMap<>();
		cPlayerGuild = new ConcurrentHashMap<>();
		cGuildData = new ConcurrentHashMap<>();
	}
	
	
	/**
	 * 修改联盟语音聊天室模式
	 * @param player
	 * @param model
	 */
	public void changeVoiceRoomModel(Player player, VoiceRoomModel model) {
		if( VoiceRoomModel.DICTATE == model || VoiceRoomModel.LIBERTY == model){
			VoiceRoomManager.getInstance().onChangeRoomType(player, model);
		}
	}
	
	/**
	 * 序列化联盟的科技数据给跨服用.
	 * @param guildId
	 */
	public void serializeGuild4Cross(String guildId) {
		this.serializeGuildTech4Cross(guildId);
	}
	
	/**
	 * 序列化联盟的科技数据给跨服用.
	 * @param guildId
	 */
	public void serializeGuildTech4Cross(String guildId) {
		//此map为非线程安全的,但这个时候应该不会有修改.
		Map<Integer, Integer> map = effectGuildTech.get(guildId);
		if (map != null && !map.isEmpty()) {
			String key = RedisProxy.CS_GUILD_TECH + ":" + guildId;
			Map<String, String> redisMap = MapUtil.toStringString(map);
			RedisProxy.getInstance().getRedisSession().hmSet(key, redisMap, CrossActivityService.getInstance().getCrossKeyExpireTime());
		}		
	}
	
	/**
	 * 跨服加载联盟相关.
	 * @param guildId
	 */
	public void loadGuild4Cross(String guildId) {
		this.loadGuildTech4Cross(guildId);
	}
	
	/**
	 * 跨服加载联盟科技
	 * @param guildId
	 */
	public void loadGuildTech4Cross(String guildId) {		
		String key = RedisProxy.CS_GUILD_TECH + ":" + guildId;		
		Map<String, String> map = RedisProxy.getInstance().getRedisSession().hGetAll(key);
		Map<Integer, Integer> effectMap = MapUtil.toIntegerInteger(map);
		
		csEffectGuildTech.put(guildId, effectMap);
	}
	
	/**
	 * 获取联盟今日登录成员数量
	 * @param guildId
	 */
	public int getDailyLoginCnt(String guildId) {
		if (dailyLoginMembers.containsKey(guildId)) {
			return dailyLoginMembers.get(guildId).size();
		}
		return 0;
	}
	
	/**
	 * 联盟成员跨天登录检测
	 */
	public void memberDailyLoginCheck(String playerId, String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		Set<String> memberSet = dailyLoginMembers.get(guildId);
		if (memberSet == null) {
			memberSet = new HashSet<>();
			dailyLoginMembers.put(guildId, memberSet);
		}
		// 今日首次登录的玩家计入redis,并推送联盟登录事件
		if (!memberSet.contains(playerId)) {
			memberSet.add(playerId);
			LocalRedis.getInstance().addGuildLoginMember(guildId, playerId);
			// 联盟任务-联盟成员登录
			GuildService.getInstance().postGuildTaskMsg(new MemberLoginTaskEvent(guildId));
		}
	}
	
	
	private long calGuildScienceOpenLimitTime(int scienceId,int level){
		GuildScienceLevelCfg cfg = HawkConfigManager.getInstance().getCombineConfig(
				GuildScienceLevelCfg.class, level + 1,scienceId);
		if(cfg == null){
			return 0;
		}
		if(cfg.getOpenLimitTime() <= 0){
			return 0;
		}
		long openTime = GsApp.getInstance().getServerOpenAM0Time();
		openTime += cfg.getOpenLimitTime() * 1000;
		return openTime;
	}

	/**
	 * 获取联盟serverId
	 * @param guildId
	 * @return
	 */
	public String getGuildServerId(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		if (guildData.containsKey(guildId)) {
			return guildData.get(guildId).getServerId();
		}
		if (cGuildData.containsKey(guildId)) {
			return cGuildData.get(guildId).getServerId();
		}
		return null;		
	}
	
	/**
	 * 是否是同阵营
	 * @param player
	 * @param guildId
	 */
	public boolean isSameCamp(Player player, String guildId) {
//		boolean open = CrossActivityService.getInstance().isOpen();
		// 跨服王战
//		if (open) {
//			int camp1 = CrossActivityService.getInstance().getCamp(player.getMainServerId());
//			int camp2 = CrossActivityService.getInstance().getCamp(GuildService.getInstance().getGuildServerId(guildId));
//			return camp1 == camp2;
//		} else {
//			if (!player.hasGuild()) {
//				return false;
//			}
//			return player.getGuildId().equals(guildId);
//		}
		if (!player.hasGuild()) {
			return false;
		}
		return player.getGuildId().equals(guildId);
	}
	
	/**
	 * 获取上次被联盟邀请的时间
	 * @param playerId
	 * @return
	 */
	public long getBeInviteTime(String playerId) {
		return beInviteTime.getOrDefault(playerId, 0L);
	}
	
	/**
	 * 更新上次被联盟邀请的时间
	 * @param playerId
	 */
	public void updateBeInviteTime(String playerId) {
		beInviteTime.put(playerId, HawkTime.getMillisecond());
	}
	
	/**
	 * 检查盟主是本服玩家
	 * 
	 */
	public void checkLeaderExist(String guildId) {
		String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		// leader还在
		AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(leaderId);
		if (account != null) {
			return;
		}
		GuildInfoObject guild = guildData.get(guildId);

		try {
			// leader不在了,选一个新leader
			leaderId = sysAppointLeader(guildId);
			account = GlobalData.getInstance().getAccountInfoByPlayerId(leaderId);
			
			guild.updateGuildLeader(leaderId, account.getPlayerId(), account.getPlayerName());
			
			GuildMemberObject target = playerGuild.get(leaderId);
			target.updateMemberAuthority(Const.GuildAuthority.L5_VALUE);
			target.updateOfficeId(GuildOffice.LEADER.value());
			
			// 解锁头像
			Player leader = GlobalData.getInstance().makesurePlayer(leaderId);
			HawkTaskManager.getInstance().postMsg(leader.getXid(), PlayerUnlockImageMsg.valueOf(UnlockType.PLAYERSTAT, PLAYERSTAT_PARAM.MENGZHU));
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 检查盟主是本服玩家
	 * 
	 */
	public boolean checkLeaderExistV2(String guildId) {
		String leaderId = GuildService.getInstance().getGuildLeaderId(guildId);
		// leader还在
		AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(leaderId);
		if (account != null) {
			return true;
		}
		checkLeaderExist(guildId);
		return false;
	}
	
	/**
	 * 系统任命盟主
	 */
	private String sysAppointLeader(String guildId) {
		String leaderId = null;
		long lastLoginTime = 0L;
		
		// 按照权限从高到低选
		int[] authArray = new int[]{
				Const.GuildAuthority.L5_VALUE,
                Const.GuildAuthority.L14_VALUE,
                Const.GuildAuthority.L4_VALUE,
                Const.GuildAuthority.L3_VALUE,
                Const.GuildAuthority.L2_VALUE,
                Const.GuildAuthority.L1_VALUE
		};
		
		// 权限最高的联盟成员集合
		List<String> members = new ArrayList<>();
		for (int i = 0; i < authArray.length; i++) {
			members = getGuildMemberIdsByAuthority(guildId, authArray[i]);
			if (!members.isEmpty()) {
				break;
			} 
		}
		
		// 选成员集合里最后登录的玩家
		for (String memberId : members) {
			AccountInfo account = GlobalData.getInstance().getAccountInfoByPlayerId(memberId);
			if (account == null) {
				continue;
			}
			if (account.getLogoutTime() > lastLoginTime) {
				leaderId = memberId;
			}
		}
		
		return leaderId;
	}
	
	/**
	 * 获取联盟编队信息
	 * @param guildId
	 * @return
	 */
	public GuildFormationObj getGuildFormation(String guildId) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return null;
		}
		if (guildData.containsKey(guildId)) {
			return guildData.get(guildId).getFormation();
		}
//		if (cGuildData.containsKey(guildId)) {
//			return cGuildData.get(guildId).getFormation();
//		}
//		return null;	
		// 大世界暂不支持
		return emptyfo;
	}

	public boolean addRewardFlag(String guildId, int flagId){
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		GuildFlagCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GuildFlagCfg.class, flagId);
		if(cfg == null){
			return false;
		}
		if(cfg.getType() != GuildFlagCfg.REWARD){
			return false;
		}
		if (guildData.containsKey(guildId)) {
			boolean rlt = guildData.get(guildId).addRewardFlag(flagId);
			if(rlt){
				syncGuildRewardFlagInfo(guildId);
			}
			return rlt;
		}
		return false;
	}

	public boolean checkRewardFlag(String guildId, int flagId){
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		if (!guildData.containsKey(guildId)) {
			return false;
		}
		return guildData.get(guildId).getRewardFlagSet().contains(flagId);
	}

	public void syncGuildRewardFlagInfo(String guildId) {
		if(!guildData.containsKey(guildId)){
			return;
		}
		if(!guildMemberData.containsKey(guildId)){
			return;
		}
		Set<Integer> rewardFlagSet = guildData.get(guildId).getRewardFlagSet();
		GuildManager.GuildRewardFlag.Builder builder = GuildManager.GuildRewardFlag.newBuilder();
		builder.addAllFlagIds(rewardFlagSet);
		for (String playerId : guildMemberData.get(guildId)) {
			Player player = GlobalData.getInstance().getActivePlayer(playerId);
			if (player != null && player.isActiveOnline()) {
				player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_REWARD_FLAG_SYNC, builder));
			}
		}
	}

	public void syncGuildRewardFlagInfo(Player player) {
		if(!player.hasGuild()){
			return;
		}
		String guildId = player.getGuildId();
		if(!guildData.containsKey(guildId)){
			return;
		}
		Set<Integer> rewardFlagSet = guildData.get(guildId).getRewardFlagSet();
		GuildManager.GuildRewardFlag.Builder builder = GuildManager.GuildRewardFlag.newBuilder();
		builder.addAllFlagIds(rewardFlagSet);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.GUILD_REWARD_FLAG_SYNC, builder));
	}
}
