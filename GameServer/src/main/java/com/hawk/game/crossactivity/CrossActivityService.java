package com.hawk.game.crossactivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.collections4.CollectionUtils;
import org.hawk.annotation.MessageHandler;
import org.hawk.app.HawkApp;
import org.hawk.app.HawkAppObj;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tickable.HawkPeriodTickable;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.MergeServerTimeCfg;
import com.hawk.activity.event.impl.CrossScoreEvent;
import com.hawk.activity.event.speciality.CrossActivityEvent;
import com.hawk.activity.type.impl.seasonActivity.SeasonActivity;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.GsApp;
import com.hawk.game.GsConfig;
import com.hawk.game.cfgElement.EffectObject;
import com.hawk.game.config.CrossConstCfg;
import com.hawk.game.config.CrossIntegralCfg;
import com.hawk.game.config.CrossMissionCfg;
import com.hawk.game.config.CrossPylonBuffCfg;
import com.hawk.game.config.CrossRankRewardCfg;
import com.hawk.game.config.CrossRewardBuffCfg;
import com.hawk.game.config.CrossScoreRankBuffCfg;
import com.hawk.game.config.CrossServerListCfg;
import com.hawk.game.config.CrossTargetCfg;
import com.hawk.game.config.CrossTimeCfg;
import com.hawk.game.config.CrossTowerBuffCfg;
import com.hawk.game.config.TeamStrengthWeightCfg;
import com.hawk.game.crossactivity.rank.MatchStrengthRank;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxDelete;
import com.hawk.game.crossactivity.resourcespree.ResourceSpreeBoxRefersh;
import com.hawk.game.crossactivity.season.CrossActivitySeasonService;
import com.hawk.game.crossfortress.CrossFortressService;
import com.hawk.game.crossfortress.FortressOccupyItem;
import com.hawk.game.crossproxy.CrossProxy;
import com.hawk.game.crossproxy.CrossService;
import com.hawk.game.crossproxy.CrossSkillService;
import com.hawk.game.entity.GuildInfoObject;
import com.hawk.game.entity.ItemEntity;
import com.hawk.game.global.GlobalData;
import com.hawk.game.global.RedisProxy;
import com.hawk.game.invoker.RemoveItemMsgInvoker;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ConsumeItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.item.mission.MissionEntityItem;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.msg.CrossActivityMsg;
import com.hawk.game.nation.NationService;
import com.hawk.game.nation.wearhouse.NationWearhouse;
import com.hawk.game.player.Player;
import com.hawk.game.president.PresidentCity;
import com.hawk.game.president.PresidentFightService;
import com.hawk.game.president.model.President;
import com.hawk.game.president.model.PresidentCrossAccumulateInfo;
import com.hawk.game.protocol.Activity.AchieveItemPB;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.CHP;
import com.hawk.game.protocol.Common.KeyValuePairStr;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.MailRewardStatus;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.Const.PlayerAttr;
import com.hawk.game.protocol.CrossActivity.CAchieveInfo;
import com.hawk.game.protocol.CrossActivity.CAchieveInfoList;
import com.hawk.game.protocol.CrossActivity.CAchieveStatus;
import com.hawk.game.protocol.CrossActivity.CPlayerInfo;
import com.hawk.game.protocol.CrossActivity.CorssRecordType;
import com.hawk.game.protocol.CrossActivity.CrossActivityPylonRankResp;
import com.hawk.game.protocol.CrossActivity.CrossActivityState;
import com.hawk.game.protocol.CrossActivity.CrossChargePageInfo;
import com.hawk.game.protocol.CrossActivity.CrossFightPeriod;
import com.hawk.game.protocol.CrossActivity.CrossOccupyFortressCount;
import com.hawk.game.protocol.CrossActivity.CrossPylonData;
import com.hawk.game.protocol.CrossActivity.CrossPylonOccupyResp;
import com.hawk.game.protocol.CrossActivity.CrossRankInfo;
import com.hawk.game.protocol.CrossActivity.CrossRankType;
import com.hawk.game.protocol.CrossActivity.CrossResultSync;
import com.hawk.game.protocol.CrossActivity.CrossStateInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxInfo;
import com.hawk.game.protocol.CrossActivity.CrossTaxRecord;
import com.hawk.game.protocol.CrossActivity.CrossTaxRecordType;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendRecord;
import com.hawk.game.protocol.CrossActivity.CrossTaxSendReq;
import com.hawk.game.protocol.CrossActivity.FortressOccupyInfo;
import com.hawk.game.protocol.CrossActivity.GetCrossPageInfoResp;
import com.hawk.game.protocol.CrossActivity.GetCrossRankResp;
import com.hawk.game.protocol.CrossActivity.GuildOccupyAccumulateData;
import com.hawk.game.protocol.CrossActivity.GuildOccupyAccumulateDataResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.National.NationbuildingType;
import com.hawk.game.protocol.Player.CrossPlayerStruct;
import com.hawk.game.protocol.Rank.RankInfo;
import com.hawk.game.protocol.Rank.RankType;
import com.hawk.game.protocol.Status;
import com.hawk.game.rank.RankService;
import com.hawk.game.service.GuildService;
import com.hawk.game.service.chat.ChatParames;
import com.hawk.game.service.chat.ChatService;
import com.hawk.game.service.mail.GuildMailService;
import com.hawk.game.service.mail.MailParames;
import com.hawk.game.service.mail.SystemMailService;
import com.hawk.game.service.mssion.MissionType;
import com.hawk.game.util.GameUtil;
import com.hawk.game.util.GsConst;
import com.hawk.game.util.GsConst.PresidentTowerPointId;
import com.hawk.game.util.LogUtil;
import com.hawk.game.world.WorldMarchService;
import com.hawk.game.world.march.IWorldMarch;
import com.hawk.game.world.service.WorldResourceService;
import com.hawk.game.world.thread.WorldTask;
import com.hawk.game.world.thread.WorldThreadScheduler;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

/**
 * 跨服活动服务类
 */
public class CrossActivityService extends HawkAppObj {

	/**
	 * 日志记录器
	 */
	public static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 全局实例对象
	 */
	private static CrossActivityService instance = null;

	/**
	 * 活动时间信息数据
	 */
	public static CActivityInfo activityInfo;

	/** 上次排行刷新时间*/
	private long lastCheckTime = 0;

	/** 上次排行buff检测时间*/
	private long lastBuffTickTime = 0;

	/** 个人积分排行*/
	private CrossRankObject selfRank = new CrossRankObject(CrossRankType.C_SELF_RANK);

	/** 联盟积分排行*/
	private CrossRankObject guildRank = new CrossRankObject(CrossRankType.C_GUILD_RANK);

	/** 服务器积分排行*/
	private CrossRankObject serverRank = new CrossRankObject(CrossRankType.C_SERVER_RANK);

	/** 战略点个人排行*/
	private CrossRankObject talentPointSelfRank = new CrossRankObject(CrossRankType.C_TALENT_RANK);

	/** 战略点联盟排行*/
	private CrossRankObject talentPointGuildRank = new CrossRankObject(CrossRankType.C_TALENT_GUILD_RANK);
	
	/** 能量塔占领排行榜*/
	private CrossServerRank pylonRank = new CrossServerRank("PYLON");

	/** 跨服排行buff*/
	private Map<Integer, Integer> rankBuff;

	/** 开启王战的服务器*/
	private Map<Integer, String> presidentOpenServer = new HashMap<>();

	/** 交税*/
	private Map<Integer, AtomicLong> sendTax = new ConcurrentHashMap<>();

	/** 玩家任务*/
	private Map<String, Map<Integer, MissionEntityItem>> playerMissions = new ConcurrentHashMap<>();

	/** 上次交税时间*/
	private long lastTaxTime = 0L;

	/** 上次刷新9级矿时间*/
	private long lastRefresSpecialResTime;
	
	/** 能量塔占领数量(只有跨服盟总期间生效) */
	private Map<String, Integer> pylonCountMap = new ConcurrentHashMap<String, Integer>();
	
	
	/**
	 * 当前阶段 公告用
	 */
	private CrossFightPeriod noticePeriod; 
	/**
	 * 匹配信息
	 */
	private CrossMatchInfo crossMatchInfo;
	/**
	 * 跨服王战电塔归属服
	 */
	private Map<String, String> crossTowerOwner = new ConcurrentHashMap<>();
	/**
	 * 修改电塔归属时间
	 */
	private Map<String, Long> changeBelongTimeMap = new ConcurrentHashMap<>();
	
	/**
	 * 联盟占领盟总统计信息
	 */
	private PresidentCrossAccumulateInfo accumulateInfo = new PresidentCrossAccumulateInfo("");

	/**
	 * 获取实例对象
	 * 
	 * @return
	 */
	public static CrossActivityService getInstance() {
		return instance;
	}

	public CrossActivityService(HawkXID xid) {
		super(xid);
		instance = this;
	}

	/**
	 * 初始化
	 * @return
	 */
	public boolean init() {
		try {
			CrossTargetContext.initParser();			
			// 读取服务器跨服活动数据
			activityInfo = RedisProxy.getInstance().getCActivityInfo();
			// 进行阶段检测
			checkStateChange();
			rankBuff = new ConcurrentHashMap<>();
			// 初始化
			rankBuffTick();

			lastTaxTime = HawkTime.getMillisecond();

			// 设置开启王战的服务器
			String serverId = RedisProxy.getInstance().getCrossPresidentServer(getTermId());
			if (!HawkOSOperator.isEmptyString(serverId)) {
				presidentOpenServer.put(getTermId(), serverId);
			}
			
			//检测合服配置.
			boolean result = checkMergeServerTimeWithCrossTime();
			if (!result) {
				return false;
			}
			
			noticePeriod = getCurrentFightPeriod();
			
			loadPylonOccupyCount();
			
			if (isOpen()) {
				accumulateInfo = RedisProxy.getInstance().getPresidentCrossAccumulateInfo(serverId, activityInfo.getTermId());
			}
			
			// 阶段轮询
			addTickable(new HawkPeriodTickable(3000) {
				@Override
				public void onPeriodTick() {

					// 活动阶段轮询
					stateTick();

					// 排行buff奖励tick
					rankBuffTick();

					if (activityInfo.getState() != CrossActivityState.C_HIDDEN) {
						rankTick();
					}

					// 活动结束阶段 - 检测发奖
					if (activityInfo.getState() == CrossActivityState.C_END) {
						checkAndReward();
						//统计一下
						checkStatistics();
					}

					// 税收检测
					taxTick();

					// 检测开启王战的服务器
					checkPresidentOpenServer();

					// 检测周期公告
					checkPeriodNotice();

					// 刷9级矿检测
					checkSpecialResRefresh();
					
					//刷新能量塔占领数量
					if(getCurrentFightPeriod() == CrossFightPeriod.CROSS_FIGHT_SCORE){
						loadPylonOccupyCount();
					}
				}
			});

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		
		this.addTickable(new HawkPeriodTickable(10000) {
			
			@Override
			public void onPeriodTick() {
				try {
					autoMatchTick();
				} catch (Exception e) {
					HawkException.catchException(e);
				}								
			}
		});
		
		return true;
	}

	/**
	 * 初始化能量塔占领数量
	 */
	private void loadPylonOccupyCount() {
		Map<String,Integer> pylonCountMapTemp = new ConcurrentHashMap<>();
		if (isOpen()) {
			String thisServerId = GsConfig.getInstance().getServerId();
			CrossServerListCfg cfg = AssembleDataManager.getInstance().getCrossServerListCfg(thisServerId);
			if (cfg == null) {
				return;
			}
			List<String> serverList = cfg.getServerList();
			for (String serverId : serverList) {
				pylonCountMapTemp.put(serverId, RedisProxy.getInstance().getCrossPylonOccupyCount(serverId));
			}
		}
		this.pylonCountMap = pylonCountMapTemp;
	}
	
	
	
	
	/**
	 * 添加能量塔占领数量
	 */
	public void addPylonOccupyCount(Player player) {
		try {
			// 是否是积分比拼阶段
			if (!isFightPeriod(CrossFightPeriod.CROSS_FIGHT_SCORE)) {
				return;
			}
			RedisProxy.getInstance().addCrossPylonOccupyCount(player.getMainServerId(), 1);
			this.pylonRank.addScore(activityInfo.getTermId(), player, 1);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	protected void checkAndReward() {
		if (activityInfo.isRewarded()) {
			return;
		}
		long now = HawkTime.getMillisecond();
		// 未到发奖时间
		if (now < activityInfo.getAwardTime()) {
			return;
		}
		// 发奖
		sendRankReward();
		
		// 跨服道具移除
		Set<Player> onlinePlayers = GlobalData.getInstance().getOnlinePlayers();
		for (Player player : onlinePlayers) {
			try {
				if (player.isCsPlayer()) {
					continue;
				}
				player.dealMsg(MsgId.REMOVE_ITEM, new RemoveItemMsgInvoker(player));
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}

		// 强制踢回跨服玩家
		CrossService.getInstance().forceRepatriatePlayers();

		// 清除本服的跨服联盟相关数据
		GuildService.getInstance().clearCrossGuildInfo();
		
		//发赛季奖
		CrossActivitySeasonService.getInstance().sendCrossTermAward(activityInfo.getTermId());
	}
	
	/**
	 * 检查统计
	 */
	public void checkStatistics(){
		if (activityInfo.isStatistics()) {
			return;
		}
		long now = HawkTime.getMillisecond();
		// 未到发奖时间
		if (now < activityInfo.getAwardTime() + HawkTime.MINUTE_MILLI_SECONDS * 5) {
			return;
		}
		activityInfo.setStatistics(true);
		RedisProxy.getInstance().updateCActivityInfo(activityInfo);
		
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				//检查活动事件
				checkSendActivityEvent();
				return null;
			}
		});
	}
	
	

	/**
	 * 检查活动事件
	 */
	protected void checkSendActivityEvent() {
		HawkLog.logPrintln("CrossActivityService-checkSendActivityEvent-begin");
		Optional<SeasonActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.SEASON_ACTIVITY_VALUE);
        if (!opActivity.isPresent()) {
        	return;
        }
        SeasonActivity activity = opActivity.get();
		if(!activity.isOpening("")){
			return;
		}
		String serverId = GsConfig.getInstance().getServerId();
		CrossServerListCfg crossCfg = AssembleDataManager.getInstance().getCrossServerListCfg(serverId);
		if (crossCfg == null) {
			return;
		}
		int termId = this.getTermId();
		//取全量数据 玩家积分
		Set<Tuple> playerTuples = RedisProxy.getInstance().getCrossRanks(CrossRankType.C_SELF_RANK, termId, 0, -1);
		//取全量数据 联盟积分
		Set<Tuple> guildTuples = RedisProxy.getInstance().getCrossRanks(CrossRankType.C_GUILD_RANK, termId, 0, -1);
		//获取跨服走的玩家
		Set<String> outPlayers = RedisProxy.getInstance().getCrossActivityCrossPlayerRecord(termId,serverId);
		
		Map<String,CrossScoreEvent> scoreMap = new HashMap<>();
		for(Tuple guildTuple : guildTuples){
			String guildIdid = guildTuple.getElement();
			long guildScore = (long) guildTuple.getScore();
			GuildInfoObject guild = GuildService.getInstance().getGuildInfoObject(guildIdid);
			//不是本服联盟不管
			if(Objects.isNull(guild)){
				continue;
			}
			//联盟成员
			Collection<String> guildMember = GuildService.getInstance().getGuildMembers(guildIdid);
			for(String member : guildMember){
				if(!GlobalData.getInstance().isLocalPlayer(member)){
					continue;
				}
				CrossScoreEvent event = new CrossScoreEvent(member, false, 0, guildScore);
				scoreMap.put(event.getPlayerId(), event);
			}
		}
		
		//统计玩家积分
		for(Tuple playerTuple : playerTuples){
			String playerId = playerTuple.getElement();
			long playerScore = (long) playerTuple.getScore();
			if(!GlobalData.getInstance().isLocalPlayer(playerId)){
				continue;
			}
			CrossScoreEvent event = scoreMap.get(playerId);
			if(Objects.isNull(event)){
				event = new CrossScoreEvent(playerId, false, playerScore, 0);
				scoreMap.put(event.getPlayerId(), event);
			}else{
				event.setSelfScore(playerScore);
			}
		}
		//统计是否跨出
		for(String outer : outPlayers){
			if(!GlobalData.getInstance().isLocalPlayer(outer)){
				continue;
			}
			CrossScoreEvent event = scoreMap.get(outer);
			if(Objects.isNull(event)){
				event = new CrossScoreEvent(outer, true, 0, 0);
				scoreMap.put(event.getPlayerId(), event);
			}else{
				event.setCrossOut(true);
			}
		}
		//抛事件
		for(CrossScoreEvent event : scoreMap.values()){
			ActivityManager.getInstance().postEvent(event);
			HawkLog.logPrintln("CrossActivityService-checkSendActivityEvent-player-{},{},{},{}",
					event.getPlayerId(),event.isCrossOut(),event.getSelfScore(),event.getGuildScore());
		}
		HawkLog.logPrintln("CrossActivityService-checkSendActivityEvent-over");
	}

	/**
	 * 是否是发奖阶段
	 * @return
	 */
	public boolean isAwardTime() {
		if (!isState(CrossActivityState.C_END)) {
			return false;
		}
		long awardTime = activityInfo.getAwardTime();
		if (HawkTime.getMillisecond() < awardTime) {
			return false;
		}
		return true;
	}
	
	/**
	 * 发放排行奖励
	 */
	public void sendRankReward() {
		try {
			activityInfo.setRewarded(true);
			RedisProxy.getInstance().updateCActivityInfo(activityInfo);
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					sendSelfReward();
					sendGuildReward();
					sendServerReward();
					sendTalentPointRankReward();
					sendTalentPointGuildRankReward();
					return null;
				}
			});
		} catch (Exception e) {
			HawkException.catchException(e);
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

	/**
	 * 排行tick刷新
	 */
	public void rankTick() {
		try {
			long currTime = HawkTime.getMillisecond();
			int termId = activityInfo.getTermId();
			long hiddenTime = activityInfo.getHiddenTime();
			if (termId == 0 || currTime >= hiddenTime) {
				return;
			}
			long gap = CrossConstCfg.getInstance().getRankTickPeriod();

			if (currTime - lastCheckTime > gap) {
				selfRank.refreshRank(termId);
				guildRank.refreshRank(termId);
				serverRank.refreshRank(termId);
				talentPointSelfRank.refreshRank(termId);
				talentPointGuildRank.refreshRank(termId);
				pylonRank.refreshRank(termId);
				lastCheckTime = currTime;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 排行buff奖励tick
	 */
	public void rankBuffTick() {
		try {
			long currTime = HawkTime.getMillisecond();
			long gap = CrossConstCfg.getInstance().getRankBuffTickPeriod();
			if (currTime - lastBuffTickTime > gap) {
				Map<Integer, Integer> buffMap = new ConcurrentHashMap<>();
				CRankBuff cRankBuff = RedisProxy.getInstance().getCRankBuff();
				if (cRankBuff != null) {
					if (HawkTime.getMillisecond() <= cRankBuff.getEndTime()) {
						buffMap = cRankBuff.calcBuffMap();
					} else {
						RedisProxy.getInstance().removeCRankBuff();
					}
				}
				rankBuff = buffMap;
				lastBuffTickTime = currTime;
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 发送全服奖励
	 */
	private void sendServerReward() {
		String serverId = GsConfig.getInstance().getServerId();
		int rank = serverRank.getSelfRank(serverId);
		long score = serverRank.getSelfScore(serverId, activityInfo.getTermId());
		CrossRankType rankType = CrossRankType.C_SERVER_RANK;
		List<CrossRankRewardCfg> cfgList = getRankListReward(rankType);
		CrossRankRewardCfg cfg = getRankRewardCfg(cfgList, rank);
		if (cfg == null) {
			HawkLog.errPrintln("Cross Server rank error, CrossRankRewardCfg is null, rank: {}, score: {}", rank, score);
			return;
		}
		long currTime = HawkTime.getMillisecond();
		long experiTime = 1000l * CrossConstCfg.getInstance().getcActivityRedisExpire();

		// 全服邮件奖励
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.CROSS_ACTIVITY_SERVER_RANK)
				.addContents(serverId, score, rank)
				.setRewards(cfg.getRewardList())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build(), currTime, currTime + experiTime);

		LogUtil.logCrossActivityServerScoreRank(score, rank, cfg.getId());
		logger.info("cross send server rank reward, serverId: {}, rankType:{}, rank: {}, score: {}, cfgId: {}",
				serverId, rankType, rank, score, cfg.getId());

	}

	/**
	 * 发送个人排名奖励
	 */
	private void sendSelfReward() {
		Set<Tuple> tuples = selfRank.getRankTuples();
		CrossRankType rankType = CrossRankType.C_SELF_RANK;
		List<CrossRankRewardCfg> cfgList = getRankListReward(rankType);
		int rank = 0;
		for (Tuple tuple : tuples) {
			try {
				rank++;
				String playerId = tuple.getElement();
				if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
					continue;
				}
				long score = (long) tuple.getScore();
				CrossRankRewardCfg cfg = getRankRewardCfg(cfgList, rank);
				if (cfg == null) {
					return;
				}

				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.CROSS_ACTIVITY_SELF_RANK)
						.addContents(score, rank)
						.setRewards(cfg.getRewardList())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
				logger.info("cross send rank reward, playerId: {}, rankType:{}, rank: {}, score: {}, cfgId: {}",
						playerId, rankType, rank, score, cfg.getId());

				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				LogUtil.logCrossActivtySelfScoreRank(player, player.getGuildId(), score, rank, cfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 发送个人战略点排名奖励
	 */
	private void sendTalentPointRankReward() {
		Set<Tuple> tuples = talentPointSelfRank.getRankTuples();
		CrossRankType rankType = CrossRankType.C_TALENT_RANK;
		List<CrossRankRewardCfg> cfgList = getRankListReward(rankType);
		int rank = 0;
		for (Tuple tuple : tuples) {
			try {
				rank++;
				String playerId = tuple.getElement();
				if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
					logger.info("cross send talent rank reward error, player null, playerId: {}, rankType:{}, rank: {}",
							playerId, rankType, rank);
					continue;
				}
				long score = (long) tuple.getScore();
				CrossRankRewardCfg cfg = getRankRewardCfg(cfgList, rank);
				if (cfg == null) {
					logger.info("cross send talent rank reward error, cfg null, playerId: {}, rankType:{}, rank: {}, score: {}",
							playerId, rankType, rank, score);
					return;
				}

				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.CROSS_CHARGE_RANK_SELF_AWARD)
						.addContents(score, rank)
						.setRewards(cfg.getRewardList())
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
				
				Player player = GlobalData.getInstance().makesurePlayer(playerId);
				LogUtil.logCrossActivtySelfTalentRank(player, player.getGuildId(), score, rank, cfg.getId());
				
				logger.info("cross send talent rank reward, playerId: {}, rankType:{}, rank: {}, score: {}, cfgId: {}",
						playerId, rankType, rank, score, cfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 发送联盟排行奖励
	 */
	private void sendGuildReward() {
		Set<Tuple> tuples = guildRank.getRankTuples();
		CrossRankType rankType = CrossRankType.C_GUILD_RANK;
		List<CrossRankRewardCfg> cfgList = getRankListReward(rankType);
		int rank = 0;
		for (Tuple tuple : tuples) {
			try {
				rank++;
				String guildId = tuple.getElement();
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				// 不是本服联盟,直接跳过
				if (guildObj == null) {
					continue;
				}
				long score = (long) tuple.getScore();
				CrossRankRewardCfg cfg = getRankRewardCfg(cfgList, rank);
				if (cfg == null) {
					return;
				}
				AwardItems award = AwardItems.valueOf();
				award.addItemInfos(cfg.getRewardList());
				MailParames.Builder paramesBuilder = MailParames.newBuilder()
						.setMailId(MailId.CROSS_ACTIVITY_GUILD_RANK)
						.addContents(score, rank)
						.setRewards(award.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET);
				GuildMailService.getInstance().sendGuildMail(guildId, paramesBuilder);
				logger.info("cross send rank reward, guildId: {}, rankType:{}, rank: {}, score: {}, cfgId: {}",
						guildId, rankType, rank, score, cfg.getId());
				LogUtil.logCrossActivityGuildScoreRank(guildId, score, rank, cfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 发送战略点联盟排行奖励
	 */
	private void sendTalentPointGuildRankReward() {
		Set<Tuple> tuples = talentPointGuildRank.getRankTuples();
		CrossRankType rankType = CrossRankType.C_TALENT_GUILD_RANK;
		List<CrossRankRewardCfg> cfgList = getRankListReward(rankType);
		int rank = 0;
		for (Tuple tuple : tuples) {
			try {
				rank++;
				String guildId = tuple.getElement();
				GuildInfoObject guildObj = GuildService.getInstance().getGuildInfoObject(guildId);
				// 不是本服联盟,直接跳过
				if (guildObj == null) {
					logger.info("cross send guild talent rank reward error, guild null, guildId: {}, rankType:{}, rank: {}",
							guildId, rankType, rank);
					continue;
				}
				long score = (long) tuple.getScore();
				CrossRankRewardCfg cfg = getRankRewardCfg(cfgList, rank);
				if (cfg == null) {
					logger.info("cross send guild talent rank reward error, cfg null, guildId: {}, rankType:{}, rank: {}, score:{}",
							guildId, rankType, rank, score);
					return;
				}
				AwardItems award = AwardItems.valueOf();
				award.addItemInfos(cfg.getRewardList());
				MailParames.Builder paramesBuilder = MailParames.newBuilder()
						.setMailId(MailId.CROSS_CHARGE_RANK_GUILD_AWARD)
						.addContents(score, rank)
						.setRewards(award.getAwardItems())
						.setAwardStatus(MailRewardStatus.NOT_GET);
				GuildMailService.getInstance().sendGuildMail(guildId, paramesBuilder);
				
				LogUtil.logCrossActivityGuildTalentRank(guildId, score, rank, cfg.getId());
				logger.info("cross send guild talent rank reward, guildId: {}, rankType:{}, rank: {}, score:{}, cfgId:{}",
						guildId, rankType, rank, score, cfg.getId());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	
	/**
	 * 获取排行奖励配置列表
	 * @param rankType
	 * @return
	 */
	private List<CrossRankRewardCfg> getRankListReward(CrossRankType rankType) {
		List<CrossRankRewardCfg> list = new ArrayList<>();
		ConfigIterator<CrossRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CrossRankRewardCfg.class);
		while (configIterator.hasNext()) {
			CrossRankRewardCfg next = configIterator.next();
			if (next.getRankType() == rankType) {
				list.add(next);
			}
		}
		return list;
	}

	/**
	 * 获取排行奖励配置
	 * @param cfgList
	 * @param rank
	 * @return
	 */
	private CrossRankRewardCfg getRankRewardCfg(List<CrossRankRewardCfg> cfgList, int rank) {
		CrossRankRewardCfg cfg = null;
		for (CrossRankRewardCfg rankCfg : cfgList) {
			int rankUpper = rankCfg.getRankUpper();
			int rankLower = rankCfg.getRankLower();
			if (rank >= rankUpper && rank <= rankLower) {
				cfg = rankCfg;
			}
		}
		return cfg;
	}

	/*********************************活动数据信息*****************************************************************/

	/**
	 * 构建活动时间信息
	 * @return
	 */
	public CrossStateInfo.Builder genStateInfo() {
		CrossStateInfo.Builder builder = CrossStateInfo.newBuilder();
		builder.setStage(activityInfo.getTermId());
		builder.setState(activityInfo.getState());
		builder.setShowTime(activityInfo.getShowTime());
		builder.setStartTime(activityInfo.getOpenTime());
		builder.setEndTime(activityInfo.getEndTime());
		builder.setHiddenTime(activityInfo.getHiddenTime());
		return builder;
	}

	/**
	 * 同步跨服活动状态
	 * @param player
	 */
	public void syncStateInfo(Player player) {
		CrossStateInfo.Builder stateInfo = CrossActivityService.getInstance().genStateInfo();
		player.sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_STATE_INFO_SYNC, stateInfo));
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_CHARGE_STATE_INFO_S, stateInfo));
	}

	private void checkStateChange() {
		CActivityInfo newInfo = calcInfo();
		int old_term = activityInfo.getTermId();
		int new_term = newInfo.getTermId();

		// 如果当前期数和当前实际期数不一致,且当前活动强制关闭,则推送活动状态,且刷新状态信息
		if (old_term != new_term && new_term == 0) {
			activityInfo = newInfo;
			// 在线玩家推送活动状态
			for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
				syncStateInfo(player);
			}
			RedisProxy.getInstance().updateCActivityInfo(activityInfo);
		}
		CrossActivityState old_state = activityInfo.getState();
		CrossActivityState new_state = newInfo.getState();
		boolean needUpdate = false;
		// 期数不一致,则重置活动状态,从隐藏阶段开始轮询
		if (new_term != old_term) {
			logger.info("cross activity term change, oldTerm:{}, newTerm:{}", old_term, new_term);
			old_state = CrossActivityState.C_HIDDEN;
			activityInfo.setTermId(new_term);
			activityInfo.setRewarded(false);
			needUpdate = true;
		}

		for (int i = 0; i < 8; i++) {
			if (old_state == new_state) {
				break;
			}
			needUpdate = true;
			
			logger.info("cross activity status change, termId:{}, oldState:{}, newState:{}", activityInfo.getTermId(), old_state, new_state);
			
			if (old_state == CrossActivityState.C_HIDDEN) {
				old_state = CrossActivityState.C_SHOW;
				activityInfo.setState(old_state);
				onShow();
			} else if (old_state == CrossActivityState.C_SHOW) {
				old_state = CrossActivityState.C_OPEN;
				activityInfo.setState(old_state);
				onOpen();
			} else if (old_state == CrossActivityState.C_OPEN) {
				old_state = CrossActivityState.C_END;
				activityInfo.setState(old_state);
				onEnd();
			} else if (old_state == CrossActivityState.C_END) {
				old_state = CrossActivityState.C_HIDDEN;
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
			RedisProxy.getInstance().updateCActivityInfo(activityInfo);
		}

	}

	private void onShow() {
		// 清除本服的跨服联盟相关数据
		GuildService.getInstance().clearCrossGuildInfo();
		//在展示阶段重读一遍redis数据覆盖内存.
		this.loadCrossServerList();
		playerMissions.clear();
		this.pylonCountMap.clear();
		this.pylonRank.clearRank();
	}

	private void onOpen() {
		try {
			// 记录全服联盟信息
			Map<byte[], byte[]> infoMap = GuildService.getInstance().getAllGuildInfo();
			RedisProxy.getInstance().addCrossGuildInfo(infoMap, activityInfo.getTermId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}

		// 全服邮件奖励
		long currTime = HawkTime.getMillisecond();
		long experiTime = 1000l * CrossConstCfg.getInstance().getcActivityRedisExpire();
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.CROSS_ACTIVITY_OPEN_NOTICE)
				.setRewards(CrossConstCfg.getInstance().getRewardList())
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.build(), currTime, currTime + experiTime);

		try {
			// 活动开启时给本服添加一份积分数据
			RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_SERVER_RANK, GsConfig.getInstance().getServerId(), activityInfo.getTermId(), 0);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 活动开始初始化跨服战略点
		String serverId = GsConfig.getInstance().getServerId();
		int addTalent = CrossConstCfg.getInstance().getTalentInit();
		RedisProxy.getInstance().addCrossTalentPoint(serverId, addTalent);
		
		// 设置战时司令
		try {
			setFightKing();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//重新算一下匹配战力
		long matchPower = this.getMatchPower(activityInfo.getTermId());
		LogUtil.logCrossActivityStart(activityInfo.getTermId(), matchPower);
		
		// 初始化一下进度条
		this.accumulateInfo = new PresidentCrossAccumulateInfo("");
	}

	/**
	 * 设置战时司令
	 */
	public void setFightKing() {
		// 异常情况判断,联盟排行榜为空
		int count = CrossConstCfg.getInstance().getFightGuildCount();
		List<RankInfo> rankCache = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY, count);
		if (rankCache == null || rankCache.isEmpty()) {
			return;
		}
		
		// 司令
		President president = PresidentFightService.getInstance().getPresidentCity().getPresident();
		
		// 战时司令玩家id
		String fightKingId = "";
		
		// 盟主不是跨服玩家并且有联盟,才可以当选战时司令
		boolean cross = president != null && !president.getServerId().equals(GsConfig.getInstance().getServerId());
		boolean hasGuild = president != null && !HawkOSOperator.isEmptyString(GuildService.getInstance().getPlayerGuildId(president.getPlayerId()));
		if (president != null && !cross && hasGuild) {
			fightKingId = president.getPlayerId();
		} else {
			String guildId = rankCache.get(0).getId();
			fightKingId = GuildService.getInstance().getGuildLeaderId(guildId);
		}
		
		// 发邮件
		long currTime = HawkTime.getMillisecond();
		long experiTime = 1000l * CrossConstCfg.getInstance().getcActivityRedisExpire();
		String fightKingName = GlobalData.getInstance().getPlayerNameById(fightKingId);
		SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
				.setMailId(MailId.CROSS_ACTIVITY_MAIL_20190113)
				.addContents(fightKingName)
				.build(),
				currTime, currTime + experiTime);
		
		// 战时司令写到redis
		RedisProxy.getInstance().addCrossFightPresident(fightKingId);
		
		// 战时司令简要信息存起来
		Player leader = GlobalData.getInstance().makesurePlayer(fightKingId);
		String leaderGuildId = leader.getGuildId();
		CrossPlayerStruct.Builder builder = CrossPlayerStruct.newBuilder();
		builder.setPlayerId(fightKingId);
		builder.setName(leader.getName());
		builder.setIcon(leader.getIcon());
		builder.setPfIcon(leader.getPfIcon());
		builder.setGuildID(leaderGuildId);
		builder.setGuildName(GuildService.getInstance().getGuildName(leader.getGuildId()));
		builder.setGuildTag(GuildService.getInstance().getGuildTag(leader.getGuildId()));
		builder.setGuildFlag(GuildService.getInstance().getGuildFlag(leader.getGuildId()));
		builder.setServerId(leader.getMainServerId());
		RedisProxy.getInstance().setFightPresidentInfo(fightKingId, builder);
		
		// 添加出战联盟
		if (!HawkOSOperator.isEmptyString(leaderGuildId)) {
			RedisProxy.getInstance().addCrossFightGuild(leaderGuildId);
		}
		logger.info("cross activity add fight guild, guildId:{}", leaderGuildId);
		
		// 取前20个联盟
		Map<String, String> canFightGuilds = new HashMap<>();
		for (RankInfo rank : rankCache) {
			canFightGuilds.put(rank.getId(), "1");
		}
		RedisProxy.getInstance().setCrossCanFightGuilds(canFightGuilds);
		
		logger.info("cross activity add fight president, fightKingId:{}, fightKingName:{}", fightKingId, fightKingName);
	}
	
	private void onEnd() {
		int termId = activityInfo.getTermId();
		selfRank.refreshRank(termId);
		guildRank.refreshRank(termId);
		serverRank.refreshRank(termId);
		talentPointSelfRank.refreshRank(termId);
		talentPointGuildRank.refreshRank(termId);
		playerMissions.clear();
		pylonCountMap.clear();
		pushResult();
		addRankBuff();
		CrossService.getInstance().forceRemoveAllPlayerCity();
		removeResourceSpreeBox();
		recordCrossActivityInfo();
		//赛季计算结果
		CrossActivitySeasonService.getInstance().calBattleRlt(termId);
	}

	/**
	 * 添加排行buff奖励
	 */
	private void addRankBuff() {
		try {
			String serverId = GsConfig.getInstance().getServerId();
			int rank = serverRank.getSelfRank(serverId);
			CrossRewardBuffCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossRewardBuffCfg.class, rank);
			if (cfg == null) {
				HawkLog.errPrintln("cross server rank cfg is null, rank: {}", rank);
				return;
			}
			CRankBuff cRankBuff = new CRankBuff();
			cRankBuff.setBuff(cfg.getEffect());
			cRankBuff.setEndTime(activityInfo.getEndTime() + cfg.getTime());
			RedisProxy.getInstance().addCRankBuff(cRankBuff);

		} catch (Exception e) {
			HawkException.catchException(e);
		}

	}

	/**
	 * 活动关闭
	 */
	private void onHidden() {
		// 卸任战时总司令
		String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident();
		if (!HawkOSOperator.isEmptyString(crossFightPresident)) {
			try {
				RedisProxy.getInstance().delCrossFightPresident();
				long currentTime = HawkTime.getMillisecond();
				long experiTime = 1000l * CrossConstCfg.getInstance().getcActivityRedisExpire();
				SystemMailService.getInstance().addGlobalMail(MailParames.newBuilder()
						.setMailId(MailId.CROSS_ACTIVITY_MAIL_20190114)
						.addContents(GlobalData.getInstance().getPlayerNameById(crossFightPresident))
						.build(), currentTime, currentTime + experiTime);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}

	/**
	 * 推送跨服活动成绩
	 */
	public void pushResult() {
		CrossResultSync.Builder builder = CrossResultSync.newBuilder();
		builder.addAllServerRank(serverRank.getRankInfos());
		List<CrossRankInfo> selfRanks = selfRank.getRankInfos();
		for (int i = 0; i < Math.min(selfRanks.size(), 3); i++) {
			builder.addSelfRank(selfRanks.get(i));
		}
		List<CrossRankInfo> guildRanks = guildRank.getRankInfos();
		for (int i = 0; i < Math.min(guildRanks.size(), 3); i++) {
			builder.addGuildRank(guildRanks.get(i));
		}
		GetCrossPageInfoResp.Builder pageInfo = GetCrossPageInfoResp.newBuilder();
		CrossStateInfo.Builder stateInfo = genStateInfo();
		pageInfo.setStateInfo(stateInfo);
		pageInfo.addAllServerRank(serverRank.getRankInfos());
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			player.sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_RESULT_SYNC, builder));
		}

		// 要塞占领信息
		for (FortressOccupyItem occupyItem : CrossFortressService.getInstance().getAllOccupyInfo().values()) {
			FortressOccupyInfo.Builder occupyInfo = FortressOccupyInfo.newBuilder();
			occupyInfo.setHasNpc(false);
			occupyInfo.setFortressServerId(occupyItem.getServerId());
			occupyInfo.setOccupyServerId(occupyItem.getCurrServer());
			int[] pos = GameUtil.splitXAndY(occupyItem.getPointId());
			occupyInfo.setPosX(pos[0]);
			occupyInfo.setPosX(pos[1]);
			pageInfo.addOccupyInfo(occupyInfo);
		}
	}

	/**
	 * 获得活动的持续时间, 秒为单位.
	 * @return
	 */
	public int getCrossKeyExpireTime() {
		// 用默认时间.
		if (activityInfo == null || activityInfo.getHiddenTime() == activityInfo.getShowTime()) {
			return 3 * 86400;
		} else {
			// 最后的时间减去当前的时间,
			return (int) ((activityInfo.getHiddenTime() - HawkTime.getMillisecond()) / 1000) + 3 * 86400;
		}
	}

	/**
	 * 当前跨服状态计算,仅供状态检测调用
	 * @return
	 */
	private CActivityInfo calcInfo() {
		if (CrossConstCfg.getInstance().isDebugMode()) {
			CActivityInfo info = new CActivityInfo();
			info.setTermId(CrossConstCfg.getInstance().getDebugTermId());
			info.setState(CrossActivityState.valueOf(CrossConstCfg.getInstance().getDebugActivityState()));
			return info;
		}
		
		ConfigIterator<CrossTimeCfg> its = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		long now = HawkTime.getMillisecond();

		Integer crossId = AssembleDataManager.getInstance().getCrossServerCfgId(GsConfig.getInstance().getServerId());
		if (crossId == null) {
			return new CActivityInfo();
		}

		CrossTimeCfg cfg = null;
		for (CrossTimeCfg timeCfg : its) {
			List<Integer> groupLimit = timeCfg.getLimitGroupList();
			// 区组判定,如果没有区组限制,或者本期允许本服所在区组开放
			if (groupLimit.isEmpty() || groupLimit.contains(crossId)) {
				if (now > timeCfg.getShowTimeValue()) {
					cfg = timeCfg;
				}
			}
		}

		// 没有可供开启的配置
		if (cfg == null) {
			return new CActivityInfo();
		}

		int termId = 0;
		CrossActivityState state = CrossActivityState.C_HIDDEN;
		if (cfg != null) {
			termId = cfg.getTermId();
			long showTime = cfg.getShowTimeValue();
			long startTime = cfg.getStartTimeValue();
			long endTime = cfg.getEndTimeValue();
			long hiddenTime = cfg.getHiddenTimeValue();
			if (now < showTime) {
				state = CrossActivityState.C_HIDDEN;
			}
			if (now >= showTime && now < startTime) {
				state = CrossActivityState.C_SHOW;
			}
			if (now >= startTime && now < endTime) {
				state = CrossActivityState.C_OPEN;
			}
			if (now >= endTime && now < hiddenTime) {
				state = CrossActivityState.C_END;
			}
			if (now >= hiddenTime) {
				state = CrossActivityState.C_HIDDEN;
			}
		}
		CActivityInfo info = new CActivityInfo();
		info.setTermId(termId);
		info.setState(state);
		return info;
	}

	/**
	 * 获取期数信息
	 * @return
	 */
	public int getTermId() {
		return activityInfo.getTermId();
	}

	public GetCrossPageInfoResp.Builder getPageInfo(Player player) {
		String playerId = player.getId();
		GetCrossPageInfoResp.Builder builder = GetCrossPageInfoResp.newBuilder();
		builder.setStateInfo(genStateInfo());
		builder.setCrossBackTime(player.getCrossBackTime());
		switch (activityInfo.state) {
		case C_SHOW:
			break;
		case C_OPEN:
			CAchieveInfoList.Builder achieveInfo = CrossActivityService.getInstance().getAchieveInfoListBuilder(player);
			builder.addAllTaskInfo(achieveInfo.getTaskInfoList());
			builder.addAllServerRank(serverRank.getRankInfos());
			builder.setSelfRank(selfRank.getSelfRank(playerId));
			String guildId = player.getGuildId();
			if (!HawkOSOperator.isEmptyString(guildId)) {
				builder.setSelfGuildRank(guildRank.getSelfRank(guildId));
			}

			// 要塞占领信息
			for (FortressOccupyItem occupyItem : CrossFortressService.getInstance().getAllOccupyInfo().values()) {
				FortressOccupyInfo.Builder occupyInfo = FortressOccupyInfo.newBuilder();
				occupyInfo.setHasNpc(false);
				occupyInfo.setFortressServerId(occupyItem.getServerId());
				occupyInfo.setOccupyServerId(occupyItem.getCurrServer());
				int[] pos = GameUtil.splitXAndY(occupyItem.getPointId());
				occupyInfo.setPosX(pos[0]);
				occupyInfo.setPosY(pos[1]);
				builder.addOccupyInfo(occupyInfo);
			}
			break;
		case C_END:
			builder.addAllServerRank(serverRank.getRankInfos());
			break;
		case C_HIDDEN:
			break;

		default:
			break;
		}
		return builder;
	}

	/**
	 * 拉取榜单信息
	 * @param playerId
	 * @param rankType
	 */
	public GetCrossRankResp.Builder getRankInfo(Player player, CrossRankType rankType) {
		GetCrossRankResp.Builder builder = null;
		int termId = activityInfo.getTermId();
		switch (rankType) {

		case C_SELF_RANK:
			builder = selfRank.buildRankInfoResp(player, termId);
			break;

		case C_GUILD_RANK:
			builder = guildRank.buildRankInfoResp(player, termId);
			break;

		case C_SERVER_RANK:
			builder = serverRank.buildRankInfoResp(player, termId);
			break;

		case C_TALENT_RANK:
			builder = talentPointSelfRank.buildRankInfoResp(player, termId);
			break;

		case C_TALENT_GUILD_RANK:
			builder = talentPointGuildRank.buildRankInfoResp(player, termId);
			break;
			
		default:
			builder = GetCrossRankResp.newBuilder();
			break;

		}

		return builder;

	}

	/**
	 * 构建玩家活动任务信息
	 * @param player
	 * @return
	 */
	public CAchieveInfoList.Builder getAchieveInfoListBuilder(Player player) {
		CAchieveInfoList.Builder builder = CAchieveInfoList.newBuilder();
		String playerId = player.getId();
		List<Integer> rewerdedList = RedisProxy.getInstance().getCrossRewardedIds(activityInfo.getTermId(), playerId);
		Map<CrossRankType, Long> scoreInfo = new HashMap<>();
		long selfScore = selfRank.getSelfScore(playerId, activityInfo.getTermId());
		String guildId = player.getGuildId();
		long guildScore = 0;
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildScore = guildRank.getSelfScore(guildId, activityInfo.getTermId());
		}
		scoreInfo.put(CrossRankType.C_SELF_RANK, selfScore);
		scoreInfo.put(CrossRankType.C_GUILD_RANK, guildScore);
		ConfigIterator<CrossTargetCfg> its = HawkConfigManager.getInstance().getConfigIterator(CrossTargetCfg.class);
		for (CrossTargetCfg cfg : its) {
			CAchieveInfo.Builder info = CAchieveInfo.newBuilder();
			CrossRankType type = cfg.getRankType();
			long score = scoreInfo.get(type);
			long condition = cfg.getScoreValue();
			int achieveId = cfg.getId();
			CAchieveStatus status = null;
			if (score < condition) {
				status = CAchieveStatus.C_NOT_REACH;

			} else {
				if (rewerdedList.contains(achieveId)) {
					status = CAchieveStatus.C_TOOKEN;
				} else {
					status = CAchieveStatus.C_NOT_REWARD;
				}
			}
			info.setAchieveId(achieveId);
			info.setValue(score);
			info.setStatus(status);
			builder.addTaskInfo(info);
		}
		return builder;
	}

	/**
	 * 领取活动成就奖励
	 * @param player
	 * @param achieveId
	 * @return
	 */
	public int getCAchieveReward(Player player, int achieveId) {
		if (!isOpen()) {
			return Status.SysError.ACTIVITY_CLOSED_VALUE;
		}
		CrossTargetCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossTargetCfg.class, achieveId);
		if (cfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		// 部分箱子需要作用号解锁
		if (cfg.getUnlockBuff() > 0 && player.getEffect().getEffVal(EffType.valueOf(cfg.getUnlockBuff())) <= 0) {
			return Status.CrossServerError.CROSS_GET_BOX_AWARD_BUFF_VALUE;
		}
		List<Integer> rewerdedList = RedisProxy.getInstance().getCrossRewardedIds(activityInfo.getTermId(), player.getId());
		// 已领取
		if (rewerdedList.contains(achieveId)) {
			return Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE;
		}
		long score = 0;
		boolean isDouble = false;
		switch (cfg.getRankType()) {
		case C_SELF_RANK:
			score = selfRank.getSelfScore(player.getId(), activityInfo.getTermId());
			isDouble = player.getEffect().getEffVal(EffType.CROSS_SELF_SCORE_REWARD_DOUBLE) > 0;
			break;
		case C_GUILD_RANK:
			if (player.hasGuild()) {
				score = guildRank.getSelfScore(player.getGuildId(), activityInfo.getTermId());
				isDouble = player.getEffect().getEffVal(EffType.CROSS_GUILD_SCORE_REWARD_DOUBLE) > 0;
			}
			break;
		default:
			break;
		}
		// 分数不够
		if (score < cfg.getScoreValue()) {
			return Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE;
		}
		List<Integer> newList = new ArrayList<>();
		newList.addAll(rewerdedList);
		newList.add(achieveId);
		RedisProxy.getInstance().updateCrossRreardedIds(activityInfo.getTermId(), player.getId(), newList);
		AwardItems awardItems = AwardItems.valueOf();
		List<ItemInfo> awardList = cfg.getRewardItems();
		// 特殊作用号奖励翻倍
		if (isDouble) {
			for (ItemInfo item : awardList) {
				item.setCount(item.getCount() * 2);
			}
		}
		awardItems.addItemInfos(awardList);
		awardItems.rewardTakeAffectAndPush(player, Action.CROSS_SERVER_ACHIEVE_REWARD, true);
		syncTargetInfo(player);
		
		LogUtil.logCrossActivtyScoreBox(player, player.getGuildId(), achieveId, cfg.getRankType().getNumber());
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 根据区服id获得跨服分组id
	 * @param serverId
	 * @return
	 */
	public int getGroupId(String serverId) {
		Integer groupId = AssembleDataManager.getInstance().getCrossServerCfgId(serverId);
		return groupId == null ? -1 : groupId;
	}

	/**************************************************************************************************/
	public boolean isOpen() {
		if (activityInfo == null) {
			return false;
		}
		boolean isSystemOpen = CrossConstCfg.getInstance().isSystemOpen();
		return isSystemOpen && activityInfo.state == CrossActivityState.C_OPEN;
	}

	public long getHiddenTime() {
		return activityInfo.getHiddenTime();
	}

	/**
	 * 判断活动是否是某一种状态.
	 * @param state
	 * @return
	 */
	public boolean isState(CrossActivityState state) {
		boolean isSystemOpen = CrossConstCfg.getInstance().isSystemOpen();
		return isSystemOpen && activityInfo.state == state;
	}

	/**
	 * 是否可以进入跨服.
	 * @param player
	 * @return
	 */
	public boolean canCross() {

		// 活动结束前的多长时间内禁止跨服防止出现中间状态.
		CrossConstCfg crossConstCfg = CrossConstCfg.getInstance();
		if (activityInfo.getEndTime() - HawkTime.getMillisecond() <= crossConstCfg.getDeadTime()) {
			return false;
		}

		return true;

	}

	/**
	 * 跨服活动事件投递
	 * 
	 * @param event
	 */
	public void postEvent(CrossActivityEvent event) {
		try {
			if (isOpen()) {
				HawkApp.getInstance().postMsg(this, CrossActivityMsg.valueOf(event));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 监听活动事件消息
	 * 
	 * @param msg
	 */
	@MessageHandler
	private void onTargetMsg(CrossActivityMsg msg) {
		long now = HawkTime.getMillisecond();
		if (now < activityInfo.getOpenTime() || now > activityInfo.getEndTime()) {
			return;
		}

		CrossActivityEvent event = msg.getEvent();
		List<CrossTargetParser<?>> parsers = CrossTargetContext.getParser(event.getClass());
		if (parsers == null) {
			logger.error("CrossTargetParser not found, eventName: {}", event.getClass().getSimpleName());
			return;
		}
		boolean update = false;

		int termId = activityInfo.getTermId();
		for (CrossTargetParser<?> parser : parsers) {
			CrossIntegralCfg scoreCfg = HawkConfigManager.getInstance().getConfigByKey(CrossIntegralCfg.class,
					parser.getTargetType().getType());
			if (scoreCfg == null) {
				continue;
			}
			if (parser.calcScore(termId, scoreCfg, event.convert())) {
				update = true;
			}
		}

		Player player = GlobalData.getInstance().makesurePlayer(event.getPlayerId());
		if (update) {
			// 记录玩家数据
			updatePlayerInfo(player, player.getGuildId());
			syncTargetInfo(player);
		}
	}

	/**
	 * 更改玩家跨服基础数据
	 * @param player
	 */
	public void updatePlayerInfo(Player player, String guildId) {
		if (player == null) {
			return;
		}

		if (!isOpen()) {
			return;
		}
		try {
			CPlayerInfo.Builder builder = CPlayerInfo.newBuilder();
			builder.setId(player.getId());
			builder.setName(player.getName());
			builder.setIcon(player.getIcon());
			String pfIcon = player.getPfIcon();
			if(!HawkOSOperator.isEmptyString(pfIcon)){
				builder.setPfIcon(pfIcon);
			}
			String mainServerId = player.getMainServerId();
			builder.setServerId(mainServerId);
			if (!HawkOSOperator.isEmptyString(guildId)) {
				builder.setGuildId(player.getGuildId());
			}
			builder.setGuildAuth(player.getGuildAuthority());
			RedisProxy.getInstance().updateCrossPlayerInfo(builder, activityInfo.getTermId());
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 同步玩家活动任务数据
	 * @param player
	 */
	public void syncTargetInfo(Player player) {
		if (activityInfo.state == CrossActivityState.C_OPEN && player.isActiveOnline()) {
			CAchieveInfoList.Builder achieveInfo = CrossActivityService.getInstance().getAchieveInfoListBuilder(player);
			player.sendProtocol(HawkProtocol.valueOf(CHP.code.CROSS_ACHIEVE_INFO_SYNC, achieveInfo));
		}
	}

	/**
	 *  获取跨服相关buff
	 * @param playerId
	 * @param effId
	 * @return
	 */
	public int getCrossBuff(String playerId, int effId) {
		return getRankBuff(playerId, effId) + getServerBuff(effId);
	}

	/**
	 * 获取跨服期间全服加成
	 * @param effId
	 * @return
	 */
	private int getServerBuff(int effId) {
		try {
			if (activityInfo == null) {
				return 0;
			}
			if (activityInfo.getState() != CrossActivityState.C_OPEN) {
				return 0;
			}
			int val = 0;
			List<EffectObject> efftList = CrossConstCfg.getInstance().getEffectList();
			for (EffectObject eff : efftList) {
				if (eff.getEffectType() == effId) {
					val += eff.getEffectValue();
				}
			}
			return val;

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}

	/**
	 * 获取跨服活动全服排行加成
	 * @param playerId
	 * @param effId
	 * @return
	 */
	private int getRankBuff(String playerId, int effId) {
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		
		try {
			if (!GlobalData.getInstance().isLocalPlayer(playerId)) {
				return 0;
			}
			if (rankBuff.containsKey(effId)) {
				return rankBuff.get(effId);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return 0;
	}


	/**
	 * 活动信息
	 */
	public CActivityInfo getActivityInfo() {
		return activityInfo;
	}

	/**
	 * 推送航海远征界面信息
	 */
	public void pushCrossPageInfo(Player player) {
		CrossChargePageInfo.Builder builder = CrossChargePageInfo.newBuilder();

		CrossFightPeriod period = getCurrentFightPeriod();
		builder.setPeriod(period);
		builder.addAllServerScoreRankInfo(serverRank.getRankInfos());
		
		String openServer = presidentOpenServer.get(getTermId());
		if (!HawkOSOperator.isEmptyString(openServer)) {
			builder.setOpenPresidentServer(openServer);

			CrossPlayerStruct serverKing = RedisProxy.getInstance().getServerKing(openServer);
			if (serverKing != null) {
				builder.setPresidentName(serverKing.getName());
				builder.setPresidentTag(serverKing.getGuildTag());
				String serverId = GlobalData.getInstance().getMainServerId(serverKing.getServerId());
				builder.setPresidentServer(serverId);
				builder.setPresidentIcon(serverKing.getIcon());
				builder.setPresidentPfIcon(serverKing.getPfIcon());
				builder.setPresidentId(serverKing.getPlayerId());
			}
		}

		builder.setStateInfo(genStateInfo());

		Map<Integer, MissionEntityItem> missions = getCrossMission(player.getId());
		for (MissionEntityItem mission : missions.values()) {
			AchieveItemPB.Builder achieve = AchieveItemPB.newBuilder();
			achieve.setAchieveId(mission.getCfgId());
			
			CrossMissionCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossMissionCfg.class, mission.getCfgId());
			if (cfg != null && cfg.getType() == MissionType.OCCUPY_CROSS_FORTRESS_SECOND.intValue()) {
				achieve.setValue((int) Math.min(Integer.MAX_VALUE -1, mission.getValue()/1000));
			} else {
				achieve.setValue((int) Math.min(Integer.MAX_VALUE -1, mission.getValue()));
			}
			
			// 这里客户端想用成就的，服务器是按照任务做的。两边差1
			achieve.setState(mission.getState() + 1);
			builder.addItem(achieve);
		}

		builder.setFightShowTime((int)CrossConstCfg.getInstance().getFightShowTime());
		builder.setFightScoreTime((int)CrossConstCfg.getInstance().getFightScoreTime());
		builder.setFightPrepareTime((int)CrossConstCfg.getInstance().getFightPrepareTime());
		builder.setFightPresidentTime((int)CrossConstCfg.getInstance().getFightPresidentTime());
		builder.setFightShowEndTime((int)CrossConstCfg.getInstance().getFightShowEndTime());
		
		if (isOpen()) {
			// 个人排行信息
			builder.setOwnRank(selfRank.getSelfRank(player.getId()));
			builder.setOwnRankScore(selfRank.getSelfScore(player.getId(), getTermId()));
			
			// 联盟排行信息
			if (player.hasGuild()) {
				builder.setGuildRank(guildRank.getSelfRank(player.getGuildId()));
				builder.setGuildRankScore(guildRank.getSelfScore(player.getGuildId(), getTermId()));
			}
			
			// 要塞占领数量
			Map<String, Integer> occupyCountMap = RedisProxy.getInstance().getCrossFortressOccupyCount();
			for (Entry<String, Integer> occupyInfo : occupyCountMap.entrySet()) {
				CrossOccupyFortressCount.Builder ocBuilder = CrossOccupyFortressCount.newBuilder();
				ocBuilder.setServerId(occupyInfo.getKey());
				ocBuilder.setCount(occupyInfo.getValue());
				builder.addFortressInfo(ocBuilder);
			}
		}
		
		long talentPoint = talentPointSelfRank.getSelfScore(player.getId(), getTermId());
		builder.setOwnTalentPoint((int)talentPoint);
		builder.setOfficeId(GameUtil.getOfficerId(player.getId()));
		builder.setRemainTotalTalentPoint((int)CrossSkillService.getInstance().getCrossTalentCollect(player.getMainServerId()).getLeftPoint());
		
		String crossFightPresident = RedisProxy.getInstance().getCrossFightPresident(player.getMainServerId());
		builder.setIsFightPresident(player.getId().equals(crossFightPresident));
		
		if (activityInfo != null) {
			builder.setRewardTime(activityInfo.getAwardTime());
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code.CROSS_CHARGE_PAGE_INFO_S_VALUE, builder));
	}

	/**
	 * 添加积分
	 * @param player
	 * @param type
	 * @param addScore
	 */
	public void addScore(Player player, CrossTargetType type, long addScore) {
		addScore(player, type, addScore, 0);
	}
	
	/**
	 * 添加积分
	 * @param player
	 * @param type
	 * @param addScore
	 * @param serverScoreRate 服务器积分增加比例
	 */
	public void addScore(Player player, CrossTargetType type, long addScore, int serverScoreRate) {
		// 是否是积分比拼阶段
		if (!isFightPeriod(CrossFightPeriod.CROSS_FIGHT_SCORE)) {
			return;
		}

		// 添加积分为0
		if (addScore <= 0) {
			return;
		}

		String serverId = player.getMainServerId();
		int termId = getTermId();
		if(type.provideScoreToRank(CrossRankType.C_SELF_RANK)){
			// 添加个人积分
			RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_SELF_RANK, player.getId(), termId, addScore);
			LogUtil.logCrossPlayerScore(player, serverId, type, addScore);
			LogUtil.logCrossActivtyScoreAdd(player, player.getGuildId(), selfRank.getSelfScore(player.getId(), termId), addScore, 0, CrossRankType.C_SELF_RANK.getNumber(), type.getType());
		
		}
		if(type.provideScoreToRank(CrossRankType.C_GUILD_RANK)){
			// 添加联盟积分
			if (player.hasGuild()) {
				RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_GUILD_RANK, player.getGuildId(), termId, addScore);
				LogUtil.logCrossGuildScore(player.getId(), serverId, type, addScore);
				LogUtil.logCrossActivtyScoreAdd(player, player.getGuildId(), guildRank.getSelfScore(player.getGuildId(), termId), addScore, 0, CrossRankType.C_GUILD_RANK.getNumber(), type.getType());
			}
		}

		if(type.provideScoreToRank(CrossRankType.C_SERVER_RANK)){
			// 添加全服积分
			if (!HawkOSOperator.isEmptyString(serverId)) {
				long serverScore = (long)(addScore * (10000 + serverScoreRate) * GsConst.EFF_PER);
				RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_SERVER_RANK, serverId, termId, serverScore);
				LogUtil.logCrossServerScore(serverId, type, serverScore);
				LogUtil.logCrossActivtyScoreAdd(player, player.getGuildId(), serverRank.getSelfScore(serverId, termId), addScore, 0, CrossRankType.C_SERVER_RANK.getNumber(), type.getType());
			}
		}
		
		// 推界面信息

		// 更新玩家跨服基础数据
		updatePlayerInfo(player, player.getGuildId());
	}

	/**
	 * 添加跨服战略点
	 * @param player
	 * @param add
	 */
	public void addCrossTalent(Player player, int addPoint) {
		RedisProxy.getInstance().addCrossTalentPoint(player.getMainServerId(), addPoint);
		
		int termId = getTermId();
		
		// 添加个人战略排行
		RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_TALENT_RANK, player.getId(), termId, addPoint);
		LogUtil.logCrossActivtyTalent(player, player.getGuildId(), talentPointSelfRank.getSelfScore(player.getId(), termId), addPoint, 0, 0);
		
		// 添加联盟战略排行
		if (player.hasGuild()) {
			RedisProxy.getInstance().addCrossActivityScore(CrossRankType.C_TALENT_GUILD_RANK, player.getGuildId(), termId, addPoint);
		}
	}
	
	/**
	 * 是否是x战斗阶段
	 * @param period
	 * @return
	 */
	public boolean isFightPeriod(CrossFightPeriod period) {
		return getCurrentFightPeriod().equals(period);
	}
	
	/**
	 * 获取当前战斗阶段
	 */
	public CrossFightPeriod getCurrentFightPeriod() {
		// 调试模式
		if (CrossConstCfg.getInstance().isDebugMode()) {
			return CrossFightPeriod.valueOf(CrossConstCfg.getInstance().getDebugFightState());
		}
		
		// 展示阶段
		if (CrossActivityService.getInstance().isState(CrossActivityState.C_SHOW)) {
			return CrossFightPeriod.CROSS_FIGHT_ACTIVITY_END;
		}
		
		// 活动结束了
		if (CrossActivityService.getInstance().isState(CrossActivityState.C_HIDDEN)) {
			return CrossFightPeriod.CROSS_FIGHT_ACTIVITY_END;
		}
		
		// 活动结束了
		if (CrossActivityService.getInstance().isState(CrossActivityState.C_END)) {
			return CrossFightPeriod.CROSS_FIGHT_SHOW_END;
		}
		
		long currentTime = HawkTime.getMillisecond();

		// 跨服活动开启时间
		long activityStartTime = activityInfo.getOpenTime();
		// 备战阶段开启时间
		long fightPrepareTime = activityStartTime + CrossConstCfg.getInstance().getFightPrepareTime();
		// 王战开启时间
		long fightPresidentTime = activityStartTime + CrossConstCfg.getInstance().getFightPresidentTime();
		// 结果展示阶段开启时间
		long fightShowEndTime = activityStartTime + CrossConstCfg.getInstance().getFightShowEndTime();

		// 积分比拼阶段
		if (currentTime < fightPrepareTime) {
			return CrossFightPeriod.CROSS_FIGHT_SCORE;
		}

		// 备战阶段
		if (currentTime < fightPresidentTime) {
			return CrossFightPeriod.CROSS_FIGHT_PREPARE;
		}

		// 王战阶段和等待结束阶段
		if (currentTime < fightShowEndTime) {
			int termId = getTermId();
			String presidentServer = presidentOpenServer.get(termId);
			if (!HawkOSOperator.isEmptyString(presidentServer)) {
				if (RedisProxy.getInstance().isCrossPresidentFightOver(termId)) {
					return CrossFightPeriod.CROSS_FIGHT_SHOW_END;
				}
			}
			
			return CrossFightPeriod.CROSS_FIGHT_PRESIDENT;
		}


		// 王战阶段
		return CrossFightPeriod.CROSS_FIGHT_SHOW_END;
	}

	/**
	 * 检测开启王战的服务器
	 */
	public void checkPresidentOpenServer() {
		// 是否是准备阶段
		if (getCurrentFightPeriod() != CrossFightPeriod.CROSS_FIGHT_PREPARE) {
			return;
		}

		// 是否已经选出区服
		String openServer = presidentOpenServer.get(getTermId());
		if (!HawkOSOperator.isEmptyString(openServer)) {
			return;
		}

		// 先刷新一下排行榜，不然可能导致排行榜不实时
		serverRank.refreshRank(activityInfo.getTermId());
		this.loadPylonOccupyCount();
		
		List<CrossRankInfo> rankInfos = serverRank.getRankInfos();

		// 选择开放王战的区服
		openServer = GsConfig.getInstance().getServerId();
		if (rankInfos != null && rankInfos.size() == 2) {
			openServer = rankInfos.get(1).getServerId();
		}

		// 写入redis
		boolean success = RedisProxy.getInstance().setCrossPresidentServer(getTermId(), openServer);
		if (!success) {
			openServer = RedisProxy.getInstance().getCrossPresidentServer(getTermId());
		}
		presidentOpenServer.put(getTermId(), openServer);

		logger.info("cross open president, openServer:{}", openServer);
		
		// 开启王战
		if (GsConfig.getInstance().getServerId().equals(openServer)) {
			long presidentStartTime = activityInfo.getOpenTime() + CrossConstCfg.getInstance().getFightPresidentTime();
			PresidentCity city = PresidentFightService.getInstance().getPresidentCity();
			city.setStartTime(presidentStartTime);
			city.broadcastPresidentInfo(null);
		}
		
		// 积分赛结束邮件
		if (rankInfos.size() == 2) {
			long currentTime = HawkTime.getMillisecond();
			long experiTime = 1000L * CrossConstCfg.getInstance().getcActivityRedisExpire();
			MailParames.Builder mailParams = MailParames.newBuilder();
			mailParams.addContents(rankInfos.get(0).getServerId());
			mailParams.addContents(rankInfos.get(1).getServerId());
			mailParams.setMailId(MailId.CROSS_ACTIVITY_MAIL_20190117);
			mailParams.addTitles(rankInfos.get(1).getServerId());
			mailParams.addSubTitles(rankInfos.get(1).getServerId());
			SystemMailService.getInstance().addGlobalMail(mailParams.build(), currentTime, currentTime + experiTime);
			
			ChatParames.Builder builder = ChatParames.newBuilder();
			builder.addParms(rankInfos.get(0).getServerId());
			builder.addParms(rankInfos.get(1).getServerId());
			builder.setKey(Const.NoticeCfgId.CROSS_ACTIVITY_NOTICE_362);
			builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(builder.build());
			
			logger.info("cross open president, send score end notice success");
		} else {
			logger.info("cross open president, send score end notice failed");
		}
	}

	/**
	 * 是否是充能胜利区分国王
	 */
	public boolean isChargeWinServerPresident(Player player, String chargeFinishServerId) {
		
		// 没有充能胜利区分
		if (HawkOSOperator.isEmptyString(chargeFinishServerId)) {
			return false;
		}
		
		// 不是充能胜利区分的玩家
		if (!chargeFinishServerId.equals(player.getMainServerId())) {
			return false;
		}
		
		CrossPlayerStruct serverKing = RedisProxy.getInstance().getServerKing(chargeFinishServerId);
		// 没有国王
		if (serverKing == null || HawkOSOperator.isEmptyString(serverKing.getPlayerId())) {
			return false;
		}
		
		// 国王不是自己
		if (!player.getId().equals(serverKing.getPlayerId())) {
			return false;
		}
		
		return true;
	}

	/**
	 * 获取交税的目标服
	 */
	public String getTaxServerId() {
		
		// 当前国王
		PresidentCity city = PresidentFightService.getInstance().getPresidentCity();
		CrossPlayerStruct serverKing = city.getCrossKingInfo();
		if (serverKing == null) {
			return null;
		}

		// 国王的所属区服
		String presidentServer = GlobalData.getInstance().getMainServerId(serverKing.getServerId());
		if (HawkOSOperator.isEmptyString(presidentServer)) {
			return null;
		}
		
		return presidentServer;
	}

	/**
	 * 收税
	 * 
	 * return 交税的目标区服
	 */
	public String addTax(Player player, AwardItems items) {
		
		// 公共接口加个try-catch，怂
		
		try {
			
			for (ItemInfo item : items.getAwardItems()) {
				if (item.getItemId() == PlayerAttr.GOLD_VALUE) {
					return null;
				}
			}
			
			// 跨服开启阶段，不交税
			if (isOpen()) {
				return null;
			}
			
			// 没有交税的目标区服
			String presidentServer = getTaxServerId();
			if (HawkOSOperator.isEmptyString(getTaxServerId())) {
				return null;
			}
			
			int taxRate = CrossConstCfg.getInstance().getTaxRate();
			if (presidentServer.equals(GsConfig.getInstance().getServerId())) {
				taxRate = CrossConstCfg.getInstance().getTaxRateOwnServer();
			}
			
			for (ItemInfo item : items.getAwardItems()) {
				long taxCount = (int) Math.floor(item.getCount() * taxRate * GsConst.EFF_PER);
				
				if (!presidentServer.equals(GsConfig.getInstance().getServerId())) {
					item.setCount(item.getCount() - taxCount);
				}
				
				addTax(item.getItemId(), taxCount);
			}
			
			return presidentServer;
		} catch (Exception e) {
			HawkException.catchException(e);
			return null;
		}
	}

	/**
	 * 收税
	 */
	private long addTax(int resType, long resValue) {
		AtomicLong tax = sendTax.get(resType);
		if (tax == null) {
			tax = new AtomicLong(0);
			AtomicLong oldTax = sendTax.putIfAbsent(resType, tax);
			if (oldTax != null) {
				tax = oldTax;
			}
		}
		return tax.addAndGet(resValue);
	}

	/**
	 * 税收记录到redis
	 */
	public void taxToRedis() {
		
		// 记录下需要交的税
		Map<Integer, Long> tax = new HashMap<>();
		for (Entry<Integer, AtomicLong> entry : sendTax.entrySet()) {
			tax.put(entry.getKey(), entry.getValue().get());
		}
		
		// 清除之前税收
		sendTax.clear();
		
		// 交税目标区服
		String targetServerId = getTaxServerId();
		
		// 没有目标区服，不交税
		if (HawkOSOperator.isEmptyString(targetServerId)) {
			return;
		}

		// 目标区服仓库总资源量
		long hasTax = 0;
		Map<Integer, Long> hasTaxed = RedisProxy.getInstance().getAllCrossTax(targetServerId);
		for (Entry<Integer, Long> entry : hasTaxed.entrySet()) {
			hasTax += entry.getValue();
		}
		
		// 目标区服仓库达到上限,不交税了
		if (hasTax >= CrossConstCfg.getInstance().getTaxMax()) {
			return;
		}
		
		// 本次交税数量
		long totalCount = 0;
		
		// 交税存到redis
		for (Entry<Integer, Long> entry : tax.entrySet()) {
			if (entry.getValue() <= 0) {
				continue;
			}
			
			RedisProxy.getInstance().incCrossTax(targetServerId, entry.getKey(), entry.getValue());
			logger.info("taxToRedis, targetServerId:{}, resType:{}, resValue:{}", targetServerId, entry.getKey(), entry.getValue());
			
			totalCount += entry.getValue();
		}

		// 没有资源可以交
		if (totalCount <= 0) {
			return;
		}

		// 记录条数
		int taxRecordLimit = CrossConstCfg.getInstance().getTaxRecordLimit();
		
		// 本服交税记录
		CrossTaxRecord.Builder sendBuilder = CrossTaxRecord.newBuilder();
		sendBuilder.setType(CrossTaxRecordType.CROSS_TAX_SEND);
		sendBuilder.setFromServerId(GsConfig.getInstance().getServerId());
		sendBuilder.setToServerId(targetServerId);
		sendBuilder.setTime(HawkTime.getMillisecond());
		for (Entry<Integer, Long> entry : tax.entrySet()) {
			CrossTaxInfo.Builder info = CrossTaxInfo.newBuilder();
			info.setResType(entry.getKey());
			info.setResValue(entry.getValue());
			sendBuilder.addTax(info);
		}
		RedisProxy.getInstance().addTaxRecord(GsConfig.getInstance().getServerId(), sendBuilder, taxRecordLimit);

		if (!GsConfig.getInstance().getServerId().equals(targetServerId)) {
			// 目标服收税记录
			CrossTaxRecord.Builder receiveBuilder = CrossTaxRecord.newBuilder();
			receiveBuilder.setType(CrossTaxRecordType.CROSS_TAX_RECEIVED);
			receiveBuilder.setFromServerId(GsConfig.getInstance().getServerId());
			receiveBuilder.setToServerId(targetServerId);
			receiveBuilder.setTime(HawkTime.getMillisecond());
			for (Entry<Integer, Long> entry : tax.entrySet()) {
				CrossTaxInfo.Builder info = CrossTaxInfo.newBuilder();
				info.setResType(entry.getKey());
				info.setResValue(entry.getValue());
				receiveBuilder.addTax(info);
			}
			RedisProxy.getInstance().addTaxRecord(targetServerId, receiveBuilder, taxRecordLimit);
		}
	}

	/**
	 * 交税tick
	 */
	public void taxTick() {
		
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastTaxTime < CrossConstCfg.getInstance().getTaxPeroid()) {
			return;
		}
		lastTaxTime = currentTime;

		// 交税
		taxToRedis();
	}

	/**
	 * 发奖仓库资源
	 */
	public void sendTax(Player player, CrossTaxSendReq req) {
		
		// 不是司令,不能发
		boolean isPresident = PresidentFightService.getInstance().isPresidentPlayer(player.getId());
		if (!isPresident) {
			return;
		}

		// 总数量
		long totalCount = 0;

		String serverId = GsConfig.getInstance().getServerId();
		Map<Integer, Long> crossTax = RedisProxy.getInstance().getAllCrossTax(serverId);
		for (CrossTaxInfo tax : req.getTaxList()) {
			Long count = crossTax.get(tax.getResType());
			if (count == null || count.longValue() < tax.getResValue()) {
				player.sendError(HP.code.CROSS_TAX_SEND_C_VALUE, Status.SysError.PARAMS_INVALID_VALUE, 0);
				return;
			}
//			int weight = WorldMarchConstProperty.getInstance().getResWeightByType(tax.getResType());
//			totalCount = totalCount + tax.getResValue() * weight;
			totalCount = totalCount + tax.getResValue();
		}

		// 目标玩家
		Player tarPlayer = GlobalData.getInstance().makesurePlayer(req.getTargetPlayerId());
		if (tarPlayer == null) {
			return;
		}

		// 数量限制
		int taxReceiveCountLimit = CrossConstCfg.getInstance().getTaxReceiveCountLimit();
		if (totalCount > taxReceiveCountLimit) {
			player.sendError(HP.code.CROSS_TAX_SEND_C_VALUE, Status.CrossServerError.CROSS_SEND_TAX_COUNT_LIMIT_VALUE, 0);
			return;
		}

		// 次数限制
		int tarReceiveTimes = RedisProxy.getInstance().getCrossTaxReceiveTimes(getTermId(), tarPlayer.getId());
		int taxReceiveTimesLimit = CrossConstCfg.getInstance().getTaxReceiveTimesLimit();
		if (tarReceiveTimes >= taxReceiveTimesLimit) {
			player.sendError(HP.code.CROSS_TAX_SEND_C_VALUE, Status.CrossServerError.CROSS_SEND_TAX_TIMES_LIMIT_VALUE, 0);
			return;
		}

		// 扣除总额
		for (CrossTaxInfo tax : req.getTaxList()) {
			RedisProxy.getInstance().incCrossTax(serverId, tax.getResType(), -tax.getResValue());
			logger.info("sendTax, presidentId:{}, tarPlayerId:{}, resType:{}, resValue:{}", player.getId(), tarPlayer.getId(), tax.getResType(), tax.getResValue());
		}

		// 更新玩家接受次数
		RedisProxy.getInstance().addCrossTaxReceiveTimes(getTermId(), tarPlayer.getId());

		// 奖励
		List<ItemInfo> awardItem = new ArrayList<>();
		for (CrossTaxInfo tax : req.getTaxList()) {
			awardItem.add(new ItemInfo(ItemType.PLAYER_ATTR_VALUE, tax.getResType(), tax.getResValue()));
		}

		// 发奖邮件
		SystemMailService.getInstance().sendMail(MailParames.newBuilder()
				.addContents(tarPlayer.getName())
				.addContents(player.getName())
				.setPlayerId(tarPlayer.getId())
				.setRewards(awardItem)
				.setAwardStatus(MailRewardStatus.NOT_GET)
				.setMailId(MailId.CROSS_TAX_SEND)
				.build());

		// 推成功协议
		player.responseSuccess(HP.code.CROSS_TAX_SEND_C_VALUE);
		
		// 记录条数
		CrossTaxSendRecord.Builder builder = CrossTaxSendRecord.newBuilder();
		builder.setFromPlayerName(player.getName());
		builder.setToPlayerName(tarPlayer.getName());
		builder.setTime(HawkTime.getMillisecond());
		builder.addAllTax(req.getTaxList());
		builder.setType(CorssRecordType.CROSS_SEND_TAX);
		RedisProxy.getInstance().addTaxSendRecord(builder);
	}

	/**
	 * 获取跨服任务
	 */
	public Map<Integer, MissionEntityItem> getCrossMission(String playerId) {
		Map<Integer, MissionEntityItem> missions = playerMissions.get(playerId);

		if (missions == null) {

			missions = new ConcurrentHashMap<>();

			// 从redis取
			String redisMission = RedisProxy.getInstance().getCrossMission(playerId, getTermId());

			if (HawkOSOperator.isEmptyString(redisMission)) {
				ConfigIterator<CrossMissionCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(CrossMissionCfg.class);
				while (cfgIter.hasNext()) {
					CrossMissionCfg cfg = cfgIter.next();
					MissionEntityItem missionEntityItem = new MissionEntityItem(cfg.getId(), 0, 0);
					missions.put(cfg.getId(), missionEntityItem);
				}
				updateCrossMission(playerId);
			} else {

				String[] missionArr = redisMission.split(",");
				for (String missionStr : missionArr) {
					String[] mission = missionStr.split("_");
					MissionEntityItem missionItem = new MissionEntityItem(Integer.parseInt(mission[0]), Integer.parseInt(mission[1]), Integer.parseInt(mission[2]));
					missions.put(missionItem.getCfgId(), missionItem);
				}
			}

			playerMissions.put(playerId, missions);
		}

		return missions;
	}

	/**
	 * 更新跨服任务
	 */
	public void updateCrossMission(String playerId) {
		Map<Integer, MissionEntityItem> missions = playerMissions.get(playerId);
		if (missions == null) {
			return;
		}

		StringBuilder result = new StringBuilder();

		int i = 0;
		for (MissionEntityItem item : missions.values()) {
			if (i > 0) {
				result.append(",");
			}
			result.append(item.toString());
			i++;
		}
		RedisProxy.getInstance().updateCrossMission(playerId, getTermId(), result.toString());
	}

	/**
	 * 添加跨服医院死兵信息
	 */
	public void addCrossHospitalArmy(String playerId, Map<Integer, Integer> infos) {
		try {
			// 跨服未开启，死兵不进医院
			if (!isOpen()) {
				return;
			}
			
			Map<Integer, Integer> crossHospital = RedisProxy.getInstance().getCrossHospital(playerId, getTermId());
			for (Entry<Integer, Integer> info : infos.entrySet()) {
				int arnyId = info.getKey();
				int value = info.getValue();
				int oldValue = crossHospital.getOrDefault(arnyId, 0);
				crossHospital.put(arnyId, oldValue + value);
				logger.info("add cross hospital army, playerId:{}, armyId:{}, add:{}, after:{}", playerId, arnyId, oldValue, oldValue + value);
			}
			
			// 更新医院死兵信息
			RedisProxy.getInstance().updateCrossHospital(playerId, getTermId(), crossHospital);
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 检测跨服道具移除
	 */
	public void checkCrossItemRemove(Player player) {
		try {
			
			// 没有跨服相关道具,不处理
			if (getCrossItem(player).isEmpty()) {
				return;
			}
			
			// 活动隐藏的时候直接删除掉
			if (CrossActivityService.getInstance().isState(CrossActivityState.C_HIDDEN) || CrossActivityService.getInstance().isState(CrossActivityState.C_SHOW)) {
				removeCrossItem(player);
				return;
			}
			
			int lastTermId = RedisProxy.getInstance().getCrossItemCheckTerm(player.getId());
			if (lastTermId != getTermId()) {
				removeCrossItem(player);
				RedisProxy.getInstance().setCrossItemCheckTerm(player.getId(), getTermId());
			}
			
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取跨服相关道具
	 */
	public List<ItemEntity> getCrossItem(Player player) {
		List<ItemEntity> removes = new ArrayList<>();

		List<ItemInfo> items = CrossConstCfg.getInstance().getCrossCheckRemoveItemList();
		for (ItemInfo item : items) {
			List<ItemEntity> playerItem = player.getData().getItemsByItemId(item.getItemId());
			if (!playerItem.isEmpty()) {
				removes.addAll(playerItem);
			}
		}
		return removes;
	}
	
	/**
	 * 移除跨服相关道具
	 */
	public void removeCrossItem(Player player) {
		List<ItemEntity> removes = getCrossItem(player);
		if (removes.isEmpty()) {
			return;
		}

		ConsumeItems consume = ConsumeItems.valueOf();
		for (ItemEntity item : removes) {
			consume.addItemConsume(item.getItemId(), item.getItemCount(), false);
			logger.info("removeCrossItem, playerId:{}, item:{}", player.getId(), item.toString());
		}
		
		if (consume.checkConsume(player, 0)) {
			consume.consumeAndPush(player, Action.GM_EXPLOIT);
		}
	}

	/**
	 * 停服
	 */
	public void shutdown() {
		try {
			// 交税
			taxToRedis();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	/**
	 * 获取王战开启的区服
	 */
	public String getPresidentOpenServer() {
		int termId = getTermId();
		return presidentOpenServer.get(termId);
	}

	/**
	 * 获取服务器积分排行榜
	 * @return
	 */
	public List<CrossRankInfo> getServerRank() {
		return serverRank.getRankInfos();
	}
	
	/**
	 * 检测周期公告
	 */
	public void checkPeriodNotice() {
		// 阶段未变化
		if (noticePeriod.equals(getCurrentFightPeriod())) {
			return;
		}

		// 变化后的阶段
		noticePeriod = getCurrentFightPeriod();

		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
					pushCrossPageInfo(player);
				}
				return null;
			}
		});
		
		ChatParames.Builder builder = ChatParames.newBuilder();

		NoticeCfgId noticeId = null;
		
		// 争夺战开启
		if (noticePeriod.equals(CrossFightPeriod.CROSS_FIGHT_PRESIDENT)) {
			List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
			noticeId = Const.NoticeCfgId.CROSS_ACTIVITY_NOTICE_363;
			builder.addParms(rankInfos.get(1).getServerId());
			for (Player sendPlayer : GlobalData.getInstance().getOnlinePlayers()) {
				pushServerScoreRank(sendPlayer);	
				syncGuildOccupyAccumulateData(sendPlayer);
			}
		}

		// 争夺战结束
		if (noticePeriod.equals(CrossFightPeriod.CROSS_FIGHT_SHOW_END)) {
			String openServer = presidentOpenServer.get(getTermId());
			if (HawkOSOperator.isEmptyString(openServer)) {
				return;
			}

			CrossPlayerStruct serverKing = RedisProxy.getInstance().getServerKing(openServer);
			if (serverKing == null) {
				return;
			}

			noticeId = Const.NoticeCfgId.CROSS_PRESIDENT_END;

			// 是否是远征军国王
			boolean kingCross = !GlobalData.getInstance().getMainServerId(serverKing.getServerId()).equals(openServer);
			builder.addParms(presidentOpenServer.get(getTermId()));
			builder.addParms(kingCross);
			builder.addParms(GlobalData.getInstance().getMainServerId(serverKing.getServerId()));
			builder.addParms(serverKing.getGuildTag());
			builder.addParms(serverKing.getName());
		}

		if (noticeId == null) {
			return;
		}
		
		builder.setKey(noticeId);
		builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);

		ChatService.getInstance().addWorldBroadcastMsg(builder.build());
	}

	/**
	 * 刷9级矿
	 */
	public void refreshSpecialRes(boolean first) {
		WorldThreadScheduler.getInstance().postWorldTask(new WorldTask(GsConst.WorldTaskType.RESOURCE_PYLON) {
			@Override
			public boolean onInvoke() {
				
				// 获取9级矿刷新结束时间
				String serverId = GsConfig.getInstance().getServerId();
				long refreshEndTime = RedisProxy.getInstance().getSpecialResRefresh(serverId);
				if (refreshEndTime == 0L) {
					return true;
				}
				
				// 已经超出结束时间了
				if (HawkTime.getMillisecond() > refreshEndTime) {
					RedisProxy.getInstance().deleteSpecialResRefresh(serverId);
					return true;
				}

				// 刷新
				WorldResourceService.getInstance().refreshCrossWinResource();

				if (first) {
					ChatParames.Builder builder = ChatParames.newBuilder();
					builder.setKey(Const.NoticeCfgId.CROSS_RES_REFRESH);
					builder.setChatType(Const.ChatType.SPECIAL_BROADCAST);
					ChatService.getInstance().addWorldBroadcastMsg(builder.build());
				}
				return true;
			}
		});
	}

	/**
	 * 清除缓存中的任务
	 * @param playerId
	 */
	public void removePlayerMission(String playerId) {
		playerMissions.remove(playerId);
	}

	/**
	 * 检测9级矿刷新
	 */
	public void checkSpecialResRefresh() {
		if (HawkTime.getMillisecond() - lastRefresSpecialResTime < CrossConstCfg.getInstance().getSpecialResourceTickPeriod()) {
			return;
		}
		lastRefresSpecialResTime = HawkTime.getMillisecond();
		refreshSpecialRes(false);
	}
	
	public void updateSpecialResRefreshTime() {
		lastRefresSpecialResTime = HawkTime.getMillisecond();
	}
	
	/**
	 * 占领王城
	 */
	public void occupyPresident(String guildId, Set<String> playerIds) {
		if (!getCurrentFightPeriod().equals(CrossFightPeriod.CROSS_FIGHT_PRESIDENT)) {
			return;
		}
		
		boolean firstOccupy = RedisProxy.getInstance().isCrossFirstOccupy(getTermId());
		if (firstOccupy) {
			RedisProxy.getInstance().updateCrossPresidentFirOccupy(getTermId());
			
			for (String playerId : playerIds) {
				// 邮件 首次占领奖励 发给队伍内
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(playerId)
						.setMailId(MailId.CROSS_PRESIDENT_FIRST_OCCUPY)
						.addContents()
						.setRewards(ItemInfo.valueListOf(CrossConstCfg.getInstance().getFirstOccupyAward()))
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
		}
		
	}
	
	/**
	 * 王战胜利
	 */
	public void presidentWin(String serverId, String presidentName) {
		if (!isOpen()) {
			return;
		}

		try {
			// 队伍内玩家发控制奖励
			BlockingDeque<String> marchs = WorldMarchService.getInstance().getPresidentMarchs();
			for (String marchId : marchs) {
				IWorldMarch march = WorldMarchService.getInstance().getMarch(marchId);
				if (march == null || march.getPlayer() == null) {
					continue;
				}
				SystemMailService.getInstance().sendMail(MailParames.newBuilder()
						.setPlayerId(march.getPlayerId())
						.setMailId(MailId.CROSS_PRESIDENT_WIN_AWARD)
						.addContents()
						.setRewards(ItemInfo.valueListOf(CrossConstCfg.getInstance().getControlAward()))
						.setAwardStatus(MailRewardStatus.NOT_GET)
						.build());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 通知其它服发盟总结束邮件/公告
		try {
			sendPresidentWinMail(serverId, presidentName);
			
			List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
			for (CrossRankInfo rankInfo : rankInfos) {
				if (GsConfig.getInstance().getServerId().equals(rankInfo.getServerId())) {
					continue;
				}
				KeyValuePairStr.Builder builder = KeyValuePairStr.newBuilder();
				builder.setKey(0);
				builder.setVal(serverId + ":" + presidentName);
				HawkProtocol protocol = HawkProtocol.valueOf(HP.code2.CROSS_PRESIDENT_MAIL, builder);
				CrossProxy.getInstance().sendNotify(protocol, rankInfo.getServerId(), null);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		// 生成世界宝箱
		try {
			List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
			// 如果胜利的区服是积分赛第一名的区服,则在积分赛最后一名的区服生成宝箱
			if (serverId.equals(rankInfos.get(0).getServerId())) {
				String winServer = rankInfos.get(0).getServerId();
				String loseServer = rankInfos.get(rankInfos.size() - 1).getServerId();
				genResourceSpreeBox(winServer, loseServer);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发王战结束邮件
	 * @param serverId
	 * @param presidentName 盟军总司令名字
	 */
	public void sendPresidentWinMail(String serverId, String presidentName) {
		if (!isOpen()) {
			return;
		}
		if (HawkOSOperator.isEmptyString(serverId)) {
			return;
		}
		List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
		if (rankInfos == null || rankInfos.size() != 2) {
			return;
		}
		long currentTime = HawkTime.getMillisecond();
		long experiTime = 1000L * CrossConstCfg.getInstance().getcActivityRedisExpire();
		
		String atkServer = rankInfos.get(0).getServerId();
		if (serverId.equals(atkServer)) {
			// 邮件
			MailParames.Builder build = MailParames.newBuilder();
			build.setMailId(MailId.CROSS_ACTIVITY_MAIL_20190118);
			build.addContents(rankInfos.get(0).getServerId());
			build.addContents(rankInfos.get(1).getServerId());
			build.addContents(presidentName);
			SystemMailService.getInstance().addGlobalMail(build.build(), currentTime, currentTime + experiTime);
			
			// 公告
			ChatParames.Builder notice = ChatParames.newBuilder();
			notice.addParms(rankInfos.get(0).getServerId());
			notice.addParms(rankInfos.get(1).getServerId());
			build.addContents(presidentName);
			notice.setKey(Const.NoticeCfgId.CROSS_ACTIVITY_NOTICE_364);
			notice.setChatType(Const.ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(notice.build());
		} else {
			// 邮件
			MailParames.Builder build = MailParames.newBuilder();
			build.setMailId(MailId.CROSS_ACTIVITY_MAIL_20190119);
			build.addContents(rankInfos.get(1).getServerId());
			build.addContents(rankInfos.get(0).getServerId());
			build.addContents(presidentName);
			SystemMailService.getInstance().addGlobalMail(build.build(), currentTime, currentTime + experiTime);
			
			// 公告
			ChatParames.Builder notice = ChatParames.newBuilder();
			notice.addParms(rankInfos.get(1).getServerId());
			notice.addParms(rankInfos.get(0).getServerId());
			notice.setKey(Const.NoticeCfgId.CROSS_ACTIVITY_NOTICE_365);
			notice.setChatType(Const.ChatType.SPECIAL_BROADCAST);
			ChatService.getInstance().addWorldBroadcastMsg(notice.build());
		}
	}
	
	public void autoMatchTick() {
		if (activityInfo.getState() != CrossActivityState.C_HIDDEN) {
			return;
		}
		
		//非正常服不参与.
		if (GsConfig.getInstance().getServerType() != ServerType.NORMAL) {
			return;
		}
		CrossConstCfg constCfg = CrossConstCfg.getInstance();
		long curTime = HawkTime.getMillisecond();		
		ConfigIterator<CrossTimeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		CrossTimeCfg cfg = null;
		CrossTimeCfg lastCfg = null;
		//过滤掉预发布,战斗服等服.
		if (constCfg.getLimitServerList().contains(GsConfig.getInstance().getServerId())) {
			return;
		}
		
		while (iter.hasNext()) {
			CrossTimeCfg tmp = iter.next();			
			if (tmp.getShowTimeValue() > curTime) {
				cfg = tmp;
				break;
			}
			lastCfg = tmp;
		}
		
		if (cfg == null) {
			return;
		}
		
		//到了 刷新战力-匹配.
		if (curTime + constCfg.getFlushServerBattleTime() <  cfg.getShowTimeValue() || cfg.getShowTimeValue() <= curTime) { 
			return;
		}
		boolean rlt = checkServerMergeTime(cfg, lastCfg);
		if(!rlt){
			return;
		}
		if (this.crossMatchInfo == null) {
			this.crossMatchInfo = new CrossMatchInfo();
			this.crossMatchInfo.setTermId(cfg.getTermId());
		} else if (this.crossMatchInfo.getTermId() != cfg.getTermId()) {
			this.crossMatchInfo.reset();
			this.crossMatchInfo.setTermId(cfg.getTermId());			
		}
		
		//这是在写入区服战力阶段
		if (curTime < cfg.getShowTimeValue() - constCfg.getMatchTime()) {
			if (this.crossMatchInfo.needFlush()) {
				boolean flushResult = flushGuildBattle(cfg);
				if (flushResult) {
					this.crossMatchInfo.incrFlushBattleNumber();
				}				
			}			
		} else {
			if (!this.crossMatchInfo.isGenerated()) {
				boolean loadSuccess = loadCrossServerList(cfg.getTermId(), true);
				if (loadSuccess) {
					this.crossMatchInfo.setGenerated(true);
				} else {
					String lockKey = RedisProxy.CROSS_MATCH_LOCK + ":" + cfg.getTermId();
					String field = "time";
					int curSeconds = HawkTime.getSeconds();
					boolean getLock = RedisProxy.getInstance().getRedisSession().hSetNx(lockKey, field, curSeconds+"") > 0;
					if (getLock) {
						if(CrossActivitySeasonService.getInstance().seasonInOpening()){
							//赛季匹配
							CrossActivitySeasonService.getInstance().doMatch(cfg.getTermId());
						}else{
							//普通匹配
							doMatch(cfg.getTermId());
						}
					} else {
						String timeString = RedisProxy.getInstance().getRedisSession().hGet(lockKey, field);
						Integer intTime = Integer.parseInt(timeString);
						if (HawkTime.getSeconds() - intTime > 5) {
							RedisProxy.getInstance().getRedisSession().hDel(lockKey, field);
							logger.info("cross server list match timeout serverId:{}", GsConfig.getInstance().getServerId());
						}
					}					
				}
			}
		}
		
	}

	public boolean checkServerMergeTime(CrossTimeCfg cfg, CrossTimeCfg lastCfg) {
		CrossConstCfg constCfg = CrossConstCfg.getInstance();
		if (GsApp.getInstance().getServerOpenTime() + constCfg.getServerDelayTime() >  cfg.getShowTimeValue()) {
			return false;
		}
		long flushTime = cfg.getShowTimeValue() - constCfg.getFlushServerBattleTime();
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		Long mergeServerTime = assembleDataManager.getServerMergeTime(GsConfig.getInstance().getServerId());
		//如果合服时间不为空则看下合服时间和这次的跨服时间是否是同一个星期.
		if (mergeServerTime != null) {			
			//合服时间和当前时间交叉不参与
			if (assembleDataManager.isTimeCross(mergeServerTime, mergeServerTime, flushTime, cfg.getHiddenTimeValue())) {
				return false;
			}
			//合服时间小于当前开启的这一期的时间(即在这一期之前合服)
			if (mergeServerTime < cfg.getShowTimeValue()) {
				//照当上期
				if (lastCfg == null) {
					return false;
				}
				//合服必须小于等于上期的结束时间本期才可以开.
				if (lastCfg.getHiddenTimeValue() < mergeServerTime) {
					return false;
				}
			}
		}
		return true;
	}
	
	public Map<Integer, String> doMatch(int termId) {
		CrossConstCfg constCfg = CrossConstCfg.getInstance();
		Map<String, CrossServerInfo> serverMap = RedisProxy.getInstance().getCrossMatchServerBattleMap(termId);
		int groupServerNum = CrossConstCfg.getInstance().getGroupServerNumber();
		if (serverMap.isEmpty() || serverMap.size() < groupServerNum) {
			return null;
		}
		long curTime = HawkTime.getMillisecond();
		int matchSize = constCfg.getMatchSize();
		AtomicInteger atoCrossId = new AtomicInteger(0);
		Map<Integer,List<CrossServerInfo>> matchMap = new HashMap<>();
		List<CrossServerInfo> extList = new ArrayList<>();
		Map<Integer, String> crossServerListMap = new HashMap<>();
		List<CrossServerInfo> lastList = new ArrayList<>();
		//填充匹配池
		this.fillMatchPool(serverMap, matchMap, extList);
		int poolSize =  CrossConstCfg.getInstance().getMatchOpenDaysPoolSize();
		for(int poolIndex = 0; poolIndex < poolSize;poolIndex ++){
			List<CrossServerInfo> serverPool = matchMap.get(poolIndex);
			if(Objects.isNull(serverPool)){
				continue;
			}
			//上次剩余的添加进来
			serverPool.addAll(lastList);
			lastList = this.doMatchPool(serverPool,groupServerNum,matchSize,atoCrossId,crossServerListMap);
		}
		
		//额外池匹配
		if(extList.size() > 0){
			extList.addAll(lastList);
			lastList = this.doMatchPool(extList,groupServerNum,matchSize,atoCrossId,crossServerListMap);
		}
		RedisProxy.getInstance().addCrossMatchList(termId, crossServerListMap,
				GsConfig.getInstance().getPlayerRedisExpire());
		//Tlog
		for(Entry<Integer, String> entry : crossServerListMap.entrySet()){
			int cId = entry.getKey();
			String servers = entry.getValue();
			LogUtil.logCrossActivityMatch(termId, cId, servers);
		}
		if(lastList.size() > 0){
			for(CrossServerInfo cs : lastList){
				HawkLog.logPrintln("CrossActivityService match fail,serverId:{},power:{},openTime:{},openDays:{}", 
						cs.getServerId(),cs.getBattleValue(),cs.getOpenServerTime(),cs.getOpenServerDays(curTime));
			}
		}
		return crossServerListMap;
	}
	
	
	
	public List<CrossServerInfo> doMatchPool(List<CrossServerInfo> serverList,int groupServerNum,int matchSize,AtomicInteger atoCrossId,Map<Integer, String> crossServerListMap){
		//删除落单的区服.
		List<CrossServerInfo> remainServerList = new ArrayList<>();
		if (serverList.isEmpty() || serverList.size() < groupServerNum) {
			remainServerList.addAll(serverList);
			return remainServerList;
		}
		Collections.sort(serverList, new OpenServerCompartor());
		int allServerCount = serverList.size();
		int remainServer = allServerCount % groupServerNum;						
		for (int i = 0; i < remainServer; i++) {
			CrossServerInfo rs = serverList.remove(serverList.size() - 1);
			remainServerList.add(rs);
		}
		Collections.sort(serverList);
		StringJoiner sj = new StringJoiner("_");
		List<CrossServerInfo> matchList = new ArrayList<>();
		for (CrossServerInfo crossServerInfo : serverList) {
			matchList.add(crossServerInfo);
			//凑够了匹配池，开始匹配.
			if (matchList.size() >= matchSize) {
				sj = new StringJoiner("_");
				for (int i = 0; i < groupServerNum; i ++) {
					if (i == 0) {
						//先取头部的玩家.
						sj.add(matchList.remove(0).getServerId());
					} else {
						sj.add(matchList.remove(HawkRand.randInt(0, matchList.size() - 1)).getServerId());
					}					
				}					
				int crossId = atoCrossId.incrementAndGet();
				crossServerListMap.put(crossId, sj.toString());
			}						
		}
		
		if (!CollectionUtils.isEmpty(matchList)) {
			Collections.shuffle(matchList);
		}
		int count = 0;
		sj = new StringJoiner("_");
		for (CrossServerInfo csi : matchList) {
			count ++;
			sj.add(csi.getServerId());
			if (count >= groupServerNum) {
				int crossId = atoCrossId.incrementAndGet();
				crossServerListMap.put(crossId, sj.toString());
				sj = new StringJoiner("_");
				count = 0;
			}
		}
		return remainServerList;
	}
	
	/**
	 * 分配匹配池
	 * @param dataList
	 * @param matchMap
	 * @param extList
	 */
	public void fillMatchPool(Map<String, CrossServerInfo> dataMap,Map<Integer,List<CrossServerInfo>> matchMap,
			List<CrossServerInfo> extList){
		long curTime = HawkTime.getMillisecond();
		for(CrossServerInfo data : dataMap.values()){
			int poolIndex = CrossConstCfg.getInstance().getMatchOpenDaysPoolIndex(data.getOpenServerDays(curTime));
			if(poolIndex < 0){
				extList.add(data);
				continue;
			}
			List<CrossServerInfo> matchList = matchMap.get(poolIndex);
			if(Objects.isNull(matchList)){
				matchList = new ArrayList<>();
				matchMap.put(poolIndex, matchList);
			}
			matchList.add(data);
		}
	}
	
	public boolean flushGuildBattle(CrossTimeCfg crossTimeCfg) {

		List<RankInfo> rankList = RankService.getInstance().getRankCache(RankType.ALLIANCE_FIGHT_KEY);
		if (rankList == null || rankList.size() < CrossConstCfg.getInstance().getMinGuildNumberJoinMatch()) {
			return true;
		}
		
		long matchPower = this.getMatchPower(crossTimeCfg.getTermId());
		if (matchPower > 0) {
			int expireTime = GsConfig.getInstance().getPlayerRedisExpire();
			CrossServerInfo crossServerInfo = new CrossServerInfo();
			crossServerInfo.setServerId(GsConfig.getInstance().getServerId());
			crossServerInfo.setOpenServerTime(GsApp.getInstance().getServerOpenTime());
			crossServerInfo.setBattleValue(matchPower);
			RedisProxy.getInstance().addCrossMatchServerBattle(crossTimeCfg.getTermId(), crossServerInfo, expireTime);
		}
		
		//日志
		LogUtil.logCrossActivityMatchPower(crossTimeCfg.getTermId(), 0, matchPower);
		return true;
	}
	
	public long getMatchPower(int termId){
		try {
			//获取列表
			int count = CrossConstCfg.getInstance().getCrossMatchNumLimit() -1;
			count = Math.max(count, 0);
			Set<Tuple> rankList = MatchStrengthRank.getInstance().getStrengthList(count);
			//列表为空则不走写入逻辑.
			if (rankList == null || rankList.size() <= 0) {
				return 0;
			}
			double memberPower = 0;
			int rank = 0;
			for(Tuple info : rankList){
				rank ++;
				String playerId = info.getElement();
				long power = (long) info.getScore();
				double powerWeight = this.getPowerWeight(rank);
				double addPower =  (power * powerWeight);
				memberPower += addPower;
				//日志
				LogUtil.logCrossActivityPlayerStrength(termId, playerId, rank, power, powerWeight, addPower);
				HawkLog.logPrintln("CrossActivityService match power,playerId:{},rank:{},power:{},powerWeight:{},memberPower:{},", 
						playerId,rank,power,powerWeight, addPower);
			}
			
			double teamParam = this.getTeamMatchParam(termId);
			long matchPower = (long) (teamParam * memberPower);
			HawkLog.logPrintln("CrossActivityService match power,matchPower:{}", matchPower);
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
	private double getTeamMatchParam(int termId){
		int count = CrossConstCfg.getInstance().getCrossMatchTimesLimit() -1;
		count = Math.max(count, 0);
		String serverId = GsConfig.getInstance().getServerId();
		List<CrossActivityRecord> logList = RedisProxy.getInstance().getCrossActivityRecord(serverId, count);
		double param = 0;
		for(CrossActivityRecord record : logList){
			int historyTerm = record.getTermId();
			int rank = record.getServerRank();
			double rankParam = CrossConstCfg.getInstance().getCrossMatchBattleResultValue(rank);
			param += rankParam;
			//日志
			LogUtil.logCrossActivityTeamParam(termId, historyTerm, rank, rankParam);
			HawkLog.logPrintln("CrossActivityService match power, getTeamMatchParam,termId:{},rank:{},param:{}", 
					record.getTermId(),rank,rankParam);
		}
		param = Math.min(param, CrossConstCfg.getInstance().getCrossMatchCofMaxValue());
		param = Math.max(param, CrossConstCfg.getInstance().getCrossMatchCofMinValue());
		HawkLog.logPrintln("CrossActivityService match power, getTeamMatchParam, result,param:{}",param);
		return param + 1;
	}
	
	/**
	 * 战力排名权重
	 * @param rank
	 * @return
	 */
	private double getPowerWeight(int rank){
		List<TeamStrengthWeightCfg> cfgList = AssembleDataManager.getInstance().getTeamStrengthWeightCfgList(30);
		for(TeamStrengthWeightCfg cfg : cfgList){
			if(cfg.getRankUpper()<= rank && rank <= cfg.getRankLower()){
				return cfg.getWeightValue();
			}
		}
		return 0;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean loadCrossServerList() {		
		CrossTimeCfg timeCfg = this.getCurrentTimeCfg();
		if (timeCfg == null) {
			timeCfg = getNextCrossTimeCfg();
			if (timeCfg == null) {
				return false;
			}
		}		
		
		return loadCrossServerList(timeCfg.getTermId(), false);
	}
	
	
	public CrossTimeCfg getCurrentTimeCfg() {
		long curTime = HawkTime.getMillisecond();		
		ConfigIterator<CrossTimeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		CrossTimeCfg cfg = null;
		
		while (iter.hasNext()) {
			CrossTimeCfg tmp = iter.next();
			if (tmp.getShowTimeValue() <= curTime && tmp.getHiddenTimeValue() > curTime) {
				cfg = tmp;
				break;
			}
		}
		
		return cfg;
	}
	/**
	 * 当前时间的下一个cfg
	 * @return
	 */
	public CrossTimeCfg getNextCrossTimeCfg() {
		long curTime = HawkTime.getMillisecond();		
		ConfigIterator<CrossTimeCfg> iter = HawkConfigManager.getInstance().getConfigIterator(CrossTimeCfg.class);
		CrossTimeCfg cfg = null;
		
		while (iter.hasNext()) {
			CrossTimeCfg tmp = iter.next();
			if (tmp.getShowTimeValue() > curTime) {
				cfg = tmp;
				break;
			}
		}
		
		return cfg;
	}
	
	public boolean loadCrossServerList(int termId, boolean needPrint) {
		Map<Integer, String> idMap = RedisProxy.getInstance().getCrossServerList(termId);
		//测试在测试的时候会出现下一期没有生成用上一期的列表所以这里我先覆盖了.
		AssembleDataManager.getInstance().parseCrossServerList(idMap);
		if (needPrint) {
			logger.info("cross server list:{}", idMap);
		}
		if (!idMap.isEmpty()) {								
			return true;
		} else {
			return false;
		}
	}
	
	public boolean checkMergeServerTimeWithCrossTime() {
		ConfigIterator<MergeServerTimeCfg> timeIterator = HawkConfigManager.getInstance().getConfigIterator(MergeServerTimeCfg.class);
		long curTime = HawkTime.getMillisecond();
		while (timeIterator.hasNext()) {
			MergeServerTimeCfg timeCfg = timeIterator.next();
			if (timeCfg.getMergeTimeValue() < curTime) {
				continue;
			}
			boolean result = checkMergeServerTimeWithCrossTime(timeCfg);
			if (!result) {
				return false;
			}
		}
		
		return true;
	}
	/**
	 * 这里不只判断本服,还判断其它发因为诸如预发布.
	 * @param timeCfg
	 * @return
	 */
	public boolean checkMergeServerTimeWithCrossTime(MergeServerTimeCfg timeCfg) {
		long curTime = HawkTime.getMillisecond();
		//只检测将来的配置.
		if (curTime > timeCfg.getMergeTimeValue()) {
			return true;
		}
		AssembleDataManager assembleDataManager = AssembleDataManager.getInstance();
		Map<String, CrossServerListCfg> serverListMap = assembleDataManager.getCrossServerListCfgMap();
		CrossConstCfg crossConstCfg = CrossConstCfg.getInstance();
		if (activityInfo.getState() == CrossActivityState.C_HIDDEN) {
			CrossTimeCfg crossTimeCfg = this.getNextCrossTimeCfg();
			if (crossTimeCfg == null) {
				return true;
			}
			//没有走到刷新阶段.
			long flushTime = crossTimeCfg.getShowTimeValue() - crossConstCfg.getFlushServerBattleTime();
			if (curTime < flushTime) {
				return true;
			}
			//时间不交叉.
			if (!assembleDataManager.isTimeCross(timeCfg.getMergeTimeValue(), timeCfg.getMergeTimeValue(), flushTime, crossTimeCfg.getHiddenTimeValue())) {
				return true;
			}
			Map<String, CrossServerInfo> serverInfoMap = RedisProxy.getInstance().getCrossMatchServerBattleMap(crossTimeCfg.getTermId());
			for (String serverId : timeCfg.getMergeServerList()) {
				if (serverInfoMap.containsKey(serverId)) {
					logger.info("server id had flushed in waiting match list serverId:{}, merge_server_time.xml id:{}", serverId, timeCfg.getId());
					return false;
				}
			}
						
		} else {
			CrossTimeCfg crossTimeCfg = activityInfo.getTimeCfg();
			//时间有交叉,那就判断一下匹配列表里面是不是有参与跨服的区服.
			if (assembleDataManager.isTimeCross(timeCfg.getMergeTimeValue(), timeCfg.getMergeTimeValue(),
					crossTimeCfg.getShowTimeValue(), crossTimeCfg.getHiddenTimeValue())) {
				Optional<CrossServerListCfg> optional = serverListMap.values().stream().filter(cfg->{
					for (String serverId : cfg.getServerList()) {
						if (timeCfg.getMergeServerList().contains(serverId)) {
							return true;
						}
					}
					
					return false;
				}).findAny();
				if (optional.isPresent()) {
					logger.info("server id had flushed in waiting match list merge_server_time.xml id:{}", timeCfg.getId());
					return false;
				}
			}			
		}
		
		return true;
	}
	

	
	/**
	 * 积分排行排行buff
	 */
	public int getCrossScoreRankBuff(String playerId, int effectType) {
		// 此方法高频调用 这里千万不要判断当前阶段,避免请求redis ！！！
		if (!isOpen()) {
			return 0;
		}
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		int selfRank = serverRank.getSelfRank(player.getMainServerId());
		CrossScoreRankBuffCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CrossScoreRankBuffCfg.class, selfRank);
		if (cfg == null) {
			return 0;
		}
		return cfg.getEffectVal(effectType);
	}


	/**
	 * 获取能量塔buff
	 */
	public int getPylonBuff(String playerId, int effectType) {
		// 此方法高频调用 这里千万不要判断当前阶段,避免请求redis ！！！
		if (!isOpen()) {
			return 0;
		}
		if (pylonCountMap == null || pylonCountMap.isEmpty()) {
			return 0;
		}
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		String serverId = player.getMainServerId();
		if (!pylonCountMap.containsKey(serverId)) {
			return 0;
		}
		int count = pylonCountMap.get(serverId);
		
		CrossPylonBuffCfg getCfg = null;
		ConfigIterator<CrossPylonBuffCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(CrossPylonBuffCfg.class);
		while (cfgIter.hasNext()) {
			CrossPylonBuffCfg cfg = cfgIter.next();
			if (count >= cfg.getNum()) {
				getCfg = cfg;
			}
		}
		
		if (getCfg == null) {
			return 0;
		}
		
		return getCfg.getEffectVal(effectType);
	}
	
	/**
	 * 获取占领能量塔的数量
	 * @param serverId
	 */
	public int getPylonCount(String serverId) {
		return pylonCountMap.getOrDefault(serverId, 0);
	}
	
	/**
	 * 获取箭塔buff
	 */
	public int getTowerBuff(String playerId, int effectType) {
		// 此方法高频调用 这里千万不要判断当前阶段,避免请求redis ！！！
		if (!isOpen()) {
			return 0;
		}
		if (HawkOSOperator.isEmptyString(playerId)) {
			return 0;
		}
		int count = 0;
		Player player = GlobalData.getInstance().makesurePlayer(playerId);
		for (PresidentTowerPointId point : GsConst.PresidentTowerPointId.values()) {
			String towerGuildId = PresidentFightService.getInstance().getPresidentTowerGuild(point.getPointId());
			if (HawkOSOperator.isEmptyString(towerGuildId)) {
				continue;
			}
			String playerGuildId = player.getGuildId();
			if(HawkOSOperator.isEmptyString(playerGuildId)){
				continue;
			}
			if(towerGuildId.equals(playerGuildId)){
				count++;
			}
//			String towerServerId = getCrossTowerOwner(towerGuildId);
//			if (HawkOSOperator.isEmptyString(towerServerId)) {
//				continue;
//			}
//			String playerServerId = player.getMainServerId();
//			int camp1 = CrossActivityService.getInstance().getCamp(towerServerId);
//			int camp2 = CrossActivityService.getInstance().getCamp(playerServerId);
//			if (camp1 == camp2) {
//				count++;
//			}
		}
		
		// 遍历取配置
		CrossTowerBuffCfg getCfg = null;
		ConfigIterator<CrossTowerBuffCfg> cfgIter = HawkConfigManager.getInstance().getConfigIterator(CrossTowerBuffCfg.class);
		while (cfgIter.hasNext()) {
			CrossTowerBuffCfg cfg = cfgIter.next();
			if (count >= cfg.getNum()) {
				getCfg = cfg;
			}
		}
		
		if (getCfg == null) {
			return 0;
		}
		return getCfg.getEffectVal(effectType);
	}

	/**
	 * 生成资源宝箱
	 * @param winServer
	 * @param loseServer
	 */
	private void genResourceSpreeBox(String winServer, String loseServer) {
		// 判断活动是否开启
		if (!isOpen()) {
			logger.info("gen res spree box, activity not open");
			return;
		}
		
		// 获取国家仓库
		NationWearhouse nationWearhouse = (NationWearhouse) NationService.getInstance().getNationBuildingByType(NationbuildingType.NATION_WEARHOUSE);
		if (nationWearhouse == null || nationWearhouse.getLevel() <= 0) {
			logger.info("gen res spree box, nationWearhouse");
			return;
		}
		
		// 资源保护比率
		int safeResource = nationWearhouse.getSafeResource();
		if (safeResource == 0) {
			logger.info("gen res spree box, safeResource zero");
			return;
		}
		
		// 获取仓库资源
		Map<Integer, Long> wareHouse = NationService.getInstance().getNationalWarehouseResourse(loseServer);
		if (wareHouse.isEmpty()) {
			logger.info("gen res spree box, wareHouse empty");
			return;
		}
		
		// 宝箱数量
		Map<Integer,Integer> boxMap = new HashMap<Integer,Integer>();
		for (Entry<Integer, Long> resInfo : wareHouse.entrySet()) {
			try {
				
				// 仓库里的资源类型和数量
				int resType = resInfo.getKey();
				long resValue = resInfo.getValue();
				
				int resBoxProportion = CrossConstCfg.getInstance().getResBoxProportion();
				
				// 爆仓资源数量
				long reduceNum = (long)(resValue * (10000 - safeResource) * GsConst.EFF_PER * resBoxProportion * GsConst.EFF_PER);
				if (reduceNum == 0) {
					logger.info("gen res spree box, reduceNum zero, resType:{}, resValue:{}", resType, resValue);
					continue;
				}
				
				// 资源宝箱生成比率
				int resBoxRate = CrossConstCfg.getInstance().getResBoxRate(resType);
				if (resBoxRate == 0) {
					logger.info("gen res spree box, resBoxRate zero, resType:{}, resValue:{}", resType, resValue);
					continue;
				}
				
				// 资源宝箱id
				int resBoxId = CrossConstCfg.getInstance().getResBoxId(resType);
				if (resBoxId == 0) {
					logger.info("gen res spree box, resBoxId zero, resType:{}, resValue:{}", resType, resValue);
					continue;
				}
				
				// 国家仓库减少资源
				NationService.getInstance().nationalWarehouseResourceConsume(resType, reduceNum, loseServer);
				
				// 宝箱数量
				int resBoxNum = (int)(reduceNum / resBoxRate);
				resBoxNum = Math.max(resBoxNum, CrossConstCfg.getInstance().getResNumMin());
				resBoxNum = Math.min(resBoxNum, CrossConstCfg.getInstance().getResNumMax());
				
				boxMap.put(resBoxId, resBoxNum);
				
				LogUtil.logCrossActivityGenBox(resType, resValue, resValue - reduceNum, winServer);
				logger.info("gen res spree box, resType:{}, resValue:{}, reduceNum:{}, resBoxId{}, resBoxNum:{}, winServerId:{}, loseServerId:{},",
						resType, resValue, reduceNum, resBoxId, resBoxNum, winServer, loseServer);
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		// 分批生成世界宝箱
		if (!boxMap.isEmpty()) {
			List<String> winner = new ArrayList<>();
			winner.add(winServer);
			ResourceSpreeBoxRefersh boxRefersh = new ResourceSpreeBoxRefersh(getTermId(), winner, 1000, boxMap, 100, 30000);
			WorldThreadScheduler.getInstance().postDelayWorldTask(boxRefersh);
		}
	}
	
	/**
	 * 删除资狂欢宝箱
	 */
	private void removeResourceSpreeBox(){
		logger.info("CrossActivityService removeResourceSpreeBox...");
		ResourceSpreeBoxDelete delete = new ResourceSpreeBoxDelete();
		WorldThreadScheduler.getInstance().postDelayWorldTask(delete);
	}
	
	
	/**
	 * 跨服结束记录
	 */
	private void recordCrossActivityInfo(){
		try {
			int termId = activityInfo.getTermId();
			String serverId = GsConfig.getInstance().getServerId();
			int serverRank = this.serverRank.getSelfRank(serverId);
			//添加记录
			CrossActivityRecord record = new CrossActivityRecord();
			record.setServerId(serverId);
			record.setTermId(termId);
			record.setServerRank(serverRank);
			RedisProxy.getInstance().saveCrossActivityRecord(record);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	
	/**
	 * 推积分排行
	 * @param player
	 */
	public void pushServerScoreRank(Player player) {
		GetCrossRankResp.Builder builder = CrossActivityService.getInstance().getRankInfo(player, CrossRankType.C_SERVER_RANK);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_SERVER_SCORE_RANK_PUSH, builder));
	}
	
	/**
	 * 是否是积分赛排名第一的区服
	 * @param serverId
	 */
	public boolean isScoreRankWinServer(String serverId) {
		return serverRank.getSelfRank(serverId) == 1;
	}

	/**
	 * 获取积分赛排名第一的区服
	 * @param serverId
	 */
	public String getScoreRankWinServer() {
		List<CrossRankInfo> rankInfos = serverRank.getRankInfos();
		for (CrossRankInfo rank : rankInfos) {
			if (rank.getRank() == 1) {
				return rank.getServerId();
			}
		}
		return "";
	}
	
	/**
	 * 获取跨服电塔归属国
	 * @param guildId
	 * @return
	 */
	public String getCrossTowerOwner(String guildId) {
		if (crossTowerOwner.containsKey(guildId)) {
			return crossTowerOwner.get(guildId);
		}
		String ownerServer = GuildService.getInstance().getGuildServerId(guildId);
		if (ownerServer == null) {
			return "";
		}
		return ownerServer;
	}

	/**
	 * 设置跨服电塔归属国
	 * @param guildId
	 * @param serverId
	 */
	public void setCrossTowerOwner(String guildId, String serverId) {
		crossTowerOwner.put(guildId, serverId);
	}

	/**
	 * 获取修改电塔时间
	 * @param guildId
	 * @return
	 */
	public long getChangeBelongTime(String guildId) {
		return changeBelongTimeMap.getOrDefault(guildId, 0L);
	}

	/**
	 * 设置修改电塔时间
	 * @param changeBelongTimeMap
	 */
	public void updateChangeBelongTimeMap(String guildId) {
		changeBelongTimeMap.put(guildId, HawkTime.getMillisecond());
	}

	
	public PresidentCrossAccumulateInfo getAccumulateInfo() {
		return accumulateInfo;
	}
	
	/**
	 * 改变盟总占领方
	 */
	public void changeCrossPresidentOccupy(String guildId) {
//		String guildServerId = GuildService.getInstance().getGuildServerId(guildId);
//		boolean isAtker = (serverRank.getSelfRank(guildServerId) == 1);
//		
//		int speed = CrossConstCfg.getInstance().getCrossAttackSpeed();
//		if (isAtker) {
//			speed = CrossConstCfg.getInstance().getCrossDefenseSpeed();
//		}
//		rateInfo.changeOccupy(speed, isAtker);
		
		String guildServerId = GuildService.getInstance().getGuildServerId(guildId);
		String openServer = this.getPresidentOpenServer();
		boolean isAtker = !openServer.equals(guildServerId);
		this.accumulateInfo.changeOccupy(guildId,isAtker);
		this.boardGuildOccupyAccumulateData();
	}
	
	/**
	 * 胜利需要占领盟总时长
	 * @return
	 */
	public int getNeedPresidentOccupyTime(){
		int time = CrossConstCfg.getInstance().getPresidentOccupyTime();
		String curGuild = this.accumulateInfo.getGuild();
		if(HawkOSOperator.isEmptyString(curGuild)){
			return time;
		}
		//已经累计占领的时间
		long needTime = CrossConstCfg.getInstance().getCrossWinAccumulateOccupyTime();
		long accumulateTime = this.accumulateInfo.getAccumulateGuildOccupyTime().getOrDefault(curGuild, 0l);
		needTime -= accumulateTime;
		needTime = Math.max(0, needTime);
		return (int) Math.min(needTime, time);
	}
	
	
	/**
	 * 获取跨服总司令
	 * @return
	 */
	public CrossPlayerStruct getCrossPresidentFromRedis(){
		String openServer = presidentOpenServer.get(getTermId());
		if (!HawkOSOperator.isEmptyString(openServer)) {
			CrossPlayerStruct serverKing = RedisProxy.getInstance().getServerKing(openServer);
			if (serverKing != null) {
				return serverKing;
			}
		}
		return null;
	}
	
	
	
	public void boardGuildOccupyAccumulateData(){
		for (Player player : GlobalData.getInstance().getOnlinePlayers()) {
			syncGuildOccupyAccumulateData(player);
		}
	}
	
	public void syncGuildOccupyAccumulateData(Player player){
		String selfserverId = GsConfig.getInstance().getServerId();
		if(!selfserverId.equals(this.getPresidentOpenServer())){
			return;
		}
		if(this.getCurrentFightPeriod() != CrossFightPeriod.CROSS_FIGHT_PRESIDENT){
			return;
		}
		GuildOccupyAccumulateDataResp.Builder builder = GuildOccupyAccumulateDataResp.newBuilder();
		if(!HawkOSOperator.isEmptyString(this.accumulateInfo.getGuild())){
			String curGuild = this.accumulateInfo.getGuild();
			String serverId = GuildService.getInstance().getGuildServerId(curGuild);
			String guidName = GuildService.getInstance().getGuildName(curGuild);
			long ostartTime = this.accumulateInfo.getStartOccupyTime();
			builder.setServerId(serverId);
			builder.setGuildId(curGuild);
			builder.setGuildName(guidName);
			builder.setOccupyStartTime(ostartTime);
		}else{
			builder.setServerId("");
			builder.setGuildId("");
			builder.setGuildName("");
			builder.setOccupyStartTime(0);
		}
		Map<String, Long> omap = this.accumulateInfo.getAccumulateGuildOccupyTime();
		for(Map.Entry<String, Long> entry : omap.entrySet()){
			String guildId = entry.getKey();
			long otime = entry.getValue();
			String serverId = GuildService.getInstance().getGuildServerId(guildId);
			String guidName = GuildService.getInstance().getGuildName(guildId);
			int occupyTime = (int) (otime);
			if(HawkOSOperator.isEmptyString(serverId) || 
					HawkOSOperator.isEmptyString(guidName) || occupyTime <= 0){
				continue;
			}
			
			GuildOccupyAccumulateData.Builder gbuilder = GuildOccupyAccumulateData.newBuilder();
			gbuilder.setServerId(serverId);
			gbuilder.setGuildId(guildId);
			gbuilder.setGuildName(guidName);
			gbuilder.setOccupyTime(occupyTime);
			
			builder.addAccumulates(gbuilder);
		}
		
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_PRESIDENT_OCCUPY_TIME_RESP, builder));
		
	}
	
	
	public void syncPylonOccupyData(Player player){
		
		String thisServerId = GsConfig.getInstance().getServerId();
		CrossServerListCfg cfg = AssembleDataManager.getInstance().getCrossServerListCfg(thisServerId);
		if (cfg == null) {
			return;
		}
		CrossPylonOccupyResp.Builder builder = CrossPylonOccupyResp.newBuilder();
		List<String> serverList = cfg.getServerList();
		for (String serverId : serverList) {
			int count = RedisProxy.getInstance().getCrossPylonOccupyCount(serverId);
			CrossPylonData.Builder pbuilder = CrossPylonData.newBuilder();
			pbuilder.setServerId(serverId);
			pbuilder.setOccupyCount(count);
			builder.addDatas(pbuilder);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_PYLON_OCCUPY_INFO_RESP, builder));
	}
	
	
	public void getPylonRankData(Player player,String serverId){
		int termId = activityInfo.getTermId();
		CrossActivityPylonRankResp.Builder resp = this.pylonRank.buildRankInfoResp(player, termId, serverId);
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.CROSS_ACTIVITY_PYLON_RANK_RESP, resp));
	}
}
