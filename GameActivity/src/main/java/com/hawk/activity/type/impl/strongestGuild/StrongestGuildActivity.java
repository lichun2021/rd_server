package com.hawk.activity.type.impl.strongestGuild;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.util.HawkClassScaner;
import org.hawk.util.JsonUtils;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.config.ActivityConfig;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.speciality.StrongestEvent;
import com.hawk.activity.extend.ActivityDataProxy;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.redis.RedisIndex;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetParser;
import com.hawk.activity.type.impl.stronestleader.target.StrongestTargetType;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildCfg;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildKVCfg;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildPeronRankReward;
import com.hawk.activity.type.impl.strongestGuild.cfg.StrongestGuildTargetRewardCfg;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildEntity;
import com.hawk.activity.type.impl.strongestGuild.entity.StrongestGuildStageInfo;
import com.hawk.activity.type.impl.strongestGuild.entity.TargetInfo;
import com.hawk.activity.type.impl.strongestGuild.history.HistoryRank;
import com.hawk.activity.type.impl.strongestGuild.history.HistoryRank.GuildRank;
import com.hawk.activity.type.impl.strongestGuild.history.HistoryRank.PersonRank;
import com.hawk.activity.type.impl.strongestGuild.rank.StrongestGuildRank;
import com.hawk.activity.type.impl.strongestGuild.rank.history.StrongestHistoryRank;
import com.hawk.activity.type.impl.strongestGuild.rank.history.impl.StrongestHistoryGuildStageRank;
import com.hawk.activity.type.impl.strongestGuild.rank.history.impl.StrongestHistoryGuildTotalRank;
import com.hawk.activity.type.impl.strongestGuild.rank.history.impl.StrongestHistoryPersonStageRank;
import com.hawk.activity.type.impl.strongestGuild.rank.history.impl.StrongestHistoryPersonTotalRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildPersonalStageRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildPersonalTotalRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildStageGuildRank;
import com.hawk.activity.type.impl.strongestGuild.rank.impl.StrongestGuildTotalGuildRank;
import com.hawk.activity.type.impl.strongestGuild.target.StrongestGuildTargetParser;
import com.hawk.game.protocol.Activity.OtherStageGuildRank;
import com.hawk.game.protocol.Activity.OtherStagePersonRank;
import com.hawk.game.protocol.Activity.RankPB;
import com.hawk.game.protocol.Activity.StrongestGuildGuildRank;
import com.hawk.game.protocol.Activity.StrongestGuildHistory;
import com.hawk.game.protocol.Activity.StrongestGuildHistoryInfo;
import com.hawk.game.protocol.Activity.StrongestGuildInfo;
import com.hawk.game.protocol.Activity.StrongestGuildPersonRank;
import com.hawk.game.protocol.Activity.StrongestGuildRankInfo;
import com.hawk.game.protocol.Activity.StrongestGuildStateChange;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class StrongestGuildActivity extends ActivityBase {
	
	/** 存放阶段信息的集合(只读)  **/
	private static final Map<Integer, StrongestGuildStageInfo> map = new HashMap<Integer, StrongestGuildStageInfo>();
	
	/** 解析事件集合(只读) **/
	private static final Map<Integer, StrongestGuildTargetParser<?>> parseMap = new HashMap<>();
	
	/** 个人排行榜（阶段，总榜） **/
	private static final List<StrongestGuildRank> personRank = new ArrayList<StrongestGuildRank>();
	
	/** 联盟总榜单 **/
	private static final List<StrongestGuildRank> guildRank = new ArrayList<StrongestGuildRank>();
	
	/** 当前阶段id **/
	private volatile StrongestGuildStageInfo curStage;
	
	/** 历史榜单，最后一个阶段产生 **/
	private HistoryRank historyRank;
	
	public static final int expireTime = (int)(7 * 86400);
	
	public StrongestGuildActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.STRONGEST_GUILD_ACTIVITY;
	}
	
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		if(curStage == null){
			for(StrongestGuildStageInfo stage : map.values()){
				if(curTime >= stage.getPrepareTime() && curTime <= stage.getEndTime()){
					setStage(stage);
					break;
				}
			}
		}
		if(curStage == null){ //并不在活动开启时间之内
			return;
		}
		//如果当前时间比当前阶段的结束时间大，则切换阶段吧
		if(curTime >= curStage.getEndTime()){
			logger.info("StrongestGuildActivity begin changeStage..");
			changeStage();
			for(StrongestGuildRank rank : personRank){
				if(rank instanceof StrongestGuildPersonalStageRank){
					StrongestGuildPersonalStageRank personStage = (StrongestGuildPersonalStageRank)rank;
					personStage.clear(); //阶段积分清理
				}
			}
			
			for(StrongestGuildRank rank : guildRank){
				if(rank instanceof StrongestGuildStageGuildRank){
					StrongestGuildStageGuildRank guildStage = (StrongestGuildStageGuildRank)rank;
					guildStage.clear();
				}
			}
		}
		
		for(StrongestGuildRank rank : personRank){
			rank.doRank();
		}
		
		for(StrongestGuildRank rank : guildRank){
			rank.doRank();
		}
	}
	
	public void setStage(StrongestGuildStageInfo stage){
		this.curStage = stage;
		if(curStage.getNextStageId() == 0){
			int termId = getActivityTermId();
			long startTime = getTimeControl().getStartTimeByTermId(termId);
			long endTime = getTimeControl().getEndTimeByTermId(termId);
			historyRank = new HistoryRank(termId, startTime, endTime);
			logger.info("StrongestGuildActivity last stage init history, stage:{}", stage.getStageId());
		}
	}

	
	
	@Override
	public void onShow() {
		curStage = null; //重新生成阶段
		clearScoreData();
	}

	@Override
	public void onOpen() { //两期活动连着开，这个要重新清理了
		initStageInfo();
		clearScoreData();
	}
	
	private void clearScoreData(){
		for(StrongestGuildRank rank : personRank){
			rank.clear();
		}
		for(StrongestGuildRank rank : guildRank){
			rank.clear();
		}
	}

	@Override
	public void onEnd() {
		try {
			//1.发送最后一个活动阶段，个人排行奖励和联盟排行榜奖励
			if(historyRank == null){
				logger.error("StrongestGuildActivity 保存联盟历史排行列表的时候，发现historyRank没有初始化，gg了.");
				return;
			}
			int curStageId = getCurStageId();
			StrongestGuildCfg stageCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, curStageId);
			Object[] title = new Object[1];
			title[0] = stageCfg.getName();
			Object[] subTitle = new Object[1];
			subTitle[0] = stageCfg.getName();
			StrongestHistoryRank rank = new StrongestHistoryPersonStageRank(historyRank.getTermId(), curStageId);
			sendPersonRewardMail(stageCfg, stageCfg.getRankId(), MailId.STRONGEST_GUILD_PERSON_STAGE, title, subTitle, rank);
			
			//联盟阶段排行奖励邮件
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					StrongestHistoryRank guildRank = new StrongestHistoryGuildStageRank(historyRank.getTermId(), stageCfg.getStageId());
					sendGuildRewardMail(stageCfg, stageCfg.getAllianceRankId(), MailId.STRONGEST_GUILD_GUILD_STAGE, title, subTitle, guildRank);
					return null;
				}
			});
			//2.发送个人总榜和联盟总榜奖励
			StrongestGuildKVCfg kvConfig = StrongestGuildKVCfg.getInstance();
			StrongestHistoryRank perRank = new StrongestHistoryPersonTotalRank(historyRank.getTermId());
			sendPersonRewardMail(null, kvConfig.getCycleRankId(), MailId.STRONGEST_GUILD_PERSON_TOTAL, new Object[1], new Object[1] ,perRank);
			
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {

				@Override
				public Object run() {
					StrongestHistoryRank guildRank = new StrongestHistoryGuildTotalRank(historyRank.getTermId());
					sendGuildRewardMail(null, kvConfig.getCycleAllianceRankId(), MailId.STRONGEST_GUILD_GUILD_TOTAL, new Object[1], new Object[1], guildRank);
					return null;
				}
			});
			//活动结束存储本期活动历史榜单
			saveHistoryRank();
		} catch (Exception e) {
			HawkException.catchException(e);
		}finally{
			curStage = null;
			clearScoreData();
		}
	}
	
	/***
	 * 沿袭最强指挥官的事件集合
	 * @param event
	 */
	@Subscribe
	public void onEvent(StrongestEvent event){
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		int stageId = getCurStageId(); //当前的准确阶段
		if(stageId == 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		StrongestGuildStageInfo stage = map.get(stageId);
		if(curTime < stage.getStartTime()){
			return; //准备阶段还没结束
		}
		
		Optional<StrongestGuildEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		StrongestGuildEntity entity = opEntity.get();
		//尝试初始化阶段数据
		initPlayerStageData(playerId);
		StrongestGuildTargetParser<?> parser = parseMap.get(stageId);
		if(!parser.march(event)){
			return; //事件不匹配
		}
		StrongestGuildCfg circularCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, stageId);
		long update = parser.onEvent(entity, circularCfg, event.convert());
		if (update != 0) {
			//计算玩家目标是否达成
			logger.info("StrongestGuildEvent:{}, addScore:{}, playerId:{}, guildId:{}", event, update, playerId, getDataGeter().getGuildId(playerId));
			entity.checkTarget();
			entity.notifyUpdate(); //积分变了这个必须要执行
			// 推送积分变化
			sendPlayerScoreChange(playerId, entity);
			ActivityDataProxy dataGetter = getDataGeter();
			//将增加的积分添加进redis(不阻塞当前线程)
			HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
				@Override
				public Object run() {
					// 阶段/总榜-1/0
					int rankType = 1;
					for(StrongestGuildRank rank : personRank){
						rank.addScore(update, playerId); 
						getDataGeter().strongestGuildScoreRecord(playerId, rankType, getActivityTermId(), stageId, (long) rank.getScore(playerId));
						rankType --;
					}
					String guildId = dataGetter.getGuildId(playerId);
					if (!HawkOSOperator.isEmptyString(guildId)) {
						for (StrongestGuildRank rank : guildRank) {
							rank.addScore(update, guildId);
						}
					}
					return null;
				}
			});
		}
	}
	
	/**
	 * 联盟解散,移除联盟排行数据
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		String guildId = event.getGuildId();
		if (HawkOSOperator.isEmptyString(guildId)) {
			return;
		}
		if (isHidden(event.getPlayerId())) {
			return;
		}
		for (StrongestGuildRank rank : guildRank) {
			try {
				rank.remove(guildId);
			} catch (Exception e) {
				HawkLog.errPrintln("StrongestGuildActivity guild dismiss event process exception, guildId: {}, msg: {}", guildId, HawkException.formatStackMsg(e));
			}
		}
	}
	
	private void initParser() {
		List<Class<?>> allClasses = HawkClassScaner.getAllClasses(ActivityConst.STRONGEST_GUILD_PACKAGE);
		for (Class<?> clazz : allClasses) {
			if (clazz.isAssignableFrom(StrongestTargetParser.class)) {
				continue;
			}
			try {
				@SuppressWarnings("rawtypes")
				StrongestGuildTargetParser parser = (StrongestGuildTargetParser) clazz.newInstance();
				StrongestTargetType targetType = parser.getTargetType();
				ConfigIterator<StrongestGuildCfg> ite = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildCfg.class);
				while(ite.hasNext()){
					StrongestGuildCfg cfg = ite.next();
					if(targetType == cfg.getTargetType()){
						parseMap.put(cfg.getStageId(), parser);
						break;
					}
				}
			} catch (InstantiationException | IllegalAccessException e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/***
	 * 初始化个人排行榜榜单，联盟的动态初始化
	 */
	private void initRankList(){
		StrongestGuildRank stageRank = new StrongestGuildPersonalStageRank();
		StrongestGuildRank totalRank = new StrongestGuildPersonalTotalRank();
		personRank.add(stageRank);
		personRank.add(totalRank);
		
		StrongestGuildRank guildStageRank = new StrongestGuildStageGuildRank();
		StrongestGuildRank totalStageRank = new StrongestGuildTotalGuildRank();
		guildRank.add(guildStageRank);
		guildRank.add(totalStageRank);
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		StrongestGuildActivity activity = new StrongestGuildActivity(config.getActivityId(), activityEntity);
		activity.initStageInfo(); //初始化阶段相关信息
		activity.initParser(); //初始化事件解析器
		activity.initRankList(); //初始化个人榜单
		//从redis加载个人总排行和联盟总排行
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<StrongestGuildEntity> queryList = HawkDBManager.getInstance()
				.query("from StrongestGuildEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			StrongestGuildEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		StrongestGuildEntity entity = new StrongestGuildEntity(playerId, termId);
		return entity;
	}
	
	/***
	 * 初始化阶段相关信息
	 */
	private void initStageInfo(){
		map.clear();
		int termId = getActivityTermId();
		/** 获取活动开始时间 **/
		long activeStartTime = getTimeControl().getStartTimeByTermId(termId);
		StrongestGuildCfg beginCfg = getFirstStageCfg();
		long stageBeginTime = activeStartTime; //阶段初始时间，第一阶段是活动开始时间
		long prepareTime = 0l;
		long startTime = 0l;
		long endTime = 0l;
		int nextStage = 0;
		int beforeStage = 0;
		while(beginCfg != null){
			prepareTime = stageBeginTime;
			startTime = prepareTime + beginCfg.getPrepareTime();
			endTime = startTime + beginCfg.getContinueTime();
			nextStage = beginCfg.getNextStageId();
			beforeStage = beginCfg.getBeforeStageId();
			StrongestGuildStageInfo info = new StrongestGuildStageInfo(beginCfg.getStageId(), prepareTime, startTime, endTime, nextStage, beforeStage);
			map.put(beginCfg.getStageId(), info);
			beginCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, beginCfg.getNextStageId());
			stageBeginTime = endTime;
		}
	}
	
	private void changeStage(){
		int nextStageId = curStage.getNextStageId();
		if(nextStageId == 0){
			//当前阶段就是最后一个阶段
			return;
		}
		logger.info("StrongestGuildActivity curStage id:{}, nextStageId:{}", curStage.getStageId(), nextStageId);
		StrongestGuildStageInfo info = map.get(nextStageId);
		setStage(info);
		//切换阶段要发送联盟阶段排行奖励
		StrongestGuildCfg cfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, curStage.getBeforeStageId());
		if(cfg == null){
			logger.error("strongest guild config not find:{}", curStage.getStageId());
			return;
		}
		//发送个人阶段排行奖励
		StrongestGuildCfg stageCfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, curStage.getBeforeStageId());
		Object[] title = new Object[1];
		title[0] = stageCfg.getName();
		Object[] subTitle = new Object[1];
		subTitle[0] = stageCfg.getName();
		StrongestHistoryRank rank = new StrongestHistoryPersonStageRank(getActivityTermId(), stageCfg.getStageId());
		sendPersonRewardMail(stageCfg, stageCfg.getRankId(), MailId.STRONGEST_GUILD_PERSON_STAGE, title, subTitle, rank);
		//发送联盟阶段排行奖励
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				StrongestHistoryRank guildRank = new StrongestHistoryGuildStageRank(getActivityTermId(), stageCfg.getStageId());
				sendGuildRewardMail(stageCfg, stageCfg.getAllianceRankId(), MailId.STRONGEST_GUILD_GUILD_STAGE, title, subTitle, guildRank);
				return null;
			}
		});
	}
	
	/***
	 * 发送个人排名奖励
	 * @param stageCfg
	 * @param rankId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param rank
	 */
	private void sendPersonRewardMail(StrongestGuildCfg stageCfg, int rankId, MailId mailId, Object[] title, Object[] subTitle, StrongestHistoryRank rank){
		List<StrongestGuildPeronRankReward> rankListReward = getRankListReward(rankId);
		int high = getHasPersonRewardMaxRank(rankId);
		if(high == 0){
			logger.error("StrongestGuildActivity sendPersonRewardMail rankReward error, rankId:{}, stageId:{}, rankType:{}", rankId, getCurStageId(), rank);
			return;
		}
		Set<String> rankPlayers = rank.getHistoryRank(high, expireTime);
		//int maxRank = rankPlayers.size();
		int maxRank = 0;
		for(String s : rankPlayers){
			maxRank ++;
			for(StrongestGuildPeronRankReward rewardCfg : rankListReward){
				if(rewardCfg.getRankLower() >= maxRank && rewardCfg.getRankUpper() <= maxRank){
					Object[] content;
					if (stageCfg != null) {
						content = new Object[3];
						content[0] = getActivityCfg().getActivityName();
						content[1] = stageCfg.getName();
						content[2] = maxRank;
					} else {
						content = new Object[2];
						content[0] = getActivityCfg().getActivityName();
						content[1] = maxRank;
					}
					sendMailToPlayer(s, mailId, title, subTitle, content, rewardCfg.getRewardList());
					logger.info("StrongestGuildActivity send rankReward, playerId:{}, rank:{}, cfgId:{}, rankId:{}", s, maxRank, rewardCfg.getId(), rankId);
				}
			}
			//maxRank --;
		}
	}
	
	/***
	 * 发送联盟排行奖励
	 * @param stageCfg
	 * @param rankId
	 * @param mailId
	 * @param title
	 * @param subTitle
	 * @param rank
	 */
	private void sendGuildRewardMail(StrongestGuildCfg stageCfg, int rankId, MailId mailId, Object[] title, Object[] subTitle, StrongestHistoryRank rank){
		List<StrongestGuildPeronRankReward> rankListReward = getRankListReward(rankId);
		int high = getHasPersonRewardMaxRank(rankId);
		if(high == 0){
			logger.error("StrongestGuildActivity sendGuildRewardMail rankReward error, rankId:{}, stageId:{}, rankType:{}", rankId, getCurStageId(), rank);
			return;
		}
		Set<String> rankGuild = rank.getHistoryRank(high, expireTime);
		//int maxRank = rankGuild.size();
		int maxRank = 0;
		for(String s : rankGuild){
			maxRank ++;
			for(StrongestGuildPeronRankReward rewardCfg : rankListReward){
				if(rewardCfg.getRankLower() >= maxRank && rewardCfg.getRankUpper() <= maxRank){
					//给联盟所有的人都要发奖励邮件
					Collection<String> members = getDataGeter().getGuildMemberIds(s);
					for(String playerId : members){
						Object[] content;
						if (stageCfg != null) {
							content = new Object[3];
							content[0] = getActivityCfg().getActivityName();
							content[1] = stageCfg.getName();
							content[2] = maxRank;
						} else {
							content = new Object[2];
							content[0] = getActivityCfg().getActivityName();
							content[1] = maxRank;
						}
						sendMailToPlayer(playerId, mailId, title, subTitle, content, rewardCfg.getRewardList());
						logger.info("StrongestGuildActivity send guild rank mail rankReward, playerId:{}, rank:{}, cfgId:{}, rankId:{}, rank", playerId, maxRank, rewardCfg.getId(), rankId, rank);
					}
				}
			}
			//maxRank --;
		}
	}
	
	public void sendHistoryRank(String playerId){
		StrongestGuildHistory.Builder builder = StrongestGuildHistory.newBuilder();
		List<String> historyList = ActivityLocalRedis.getInstance().lall(ActivityRedisKey.STRONGEST_GUILD_HISTORY_RANK);
		int size = ActivityConfig.getInstance().getActivityCircularRankSize();
		for(int i = 0 ; i < historyList.size() ; i ++){
			try {
				String msg = historyList.get(i);
				HistoryRank rank = JsonUtils.String2Object(msg, HistoryRank.class);
				StrongestGuildHistoryInfo.Builder info = StrongestGuildHistoryInfo.newBuilder();
				info.setTermId(rank.getTermId());
				info.setStartTime(rank.getStartTime());
				info.setEndTime(rank.getEndTime());
				int psize = 0;
				for(PersonRank prank : rank.getPersonRank()){
					psize ++;
					if(psize > size){
						break;
					}
					RankPB.Builder personRank = RankPB.newBuilder();
					personRank.setScore(prank.getScore());
					personRank.setRank(prank.getRank());
					personRank.setPlayerName(prank.getPlayerName());
					personRank.setGuildName(prank.getGuildName());
					info.addPersonRank(personRank);
				}
				int gsize = 0;
				for(GuildRank grank : rank.getGuildRank()){
					gsize ++;
					if(gsize > size){
						break;
					}
					StrongestGuildRankInfo.Builder guildRank = StrongestGuildRankInfo.newBuilder();
					guildRank.setScore(grank.getScore());
					guildRank.setRank(grank.getRank());
					guildRank.setGuildTag(grank.getGuildTag());
					guildRank.setGuildName(grank.getGuildName());
					guildRank.setGuildFlag(grank.getGuildFlag());
					info.addGuildRank(guildRank);
				}
				builder.addInfos(info);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		pushToPlayer(playerId, HP.code.STRONGEST_GUILD_HISTORY_RESP_S_VALUE, builder);
	}
	
	private void saveHistoryRank(){
		if(historyRank == null){
			logger.error("StrongestGuildActivity 保存联盟历史排行列表的时候，发现historyRank没有初始化，gg了.");
			return;
		}
		
		String perkey = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSONAL_TOTAL_RANK, historyRank.getTermId());
		//Set<Tuple> personSet = ActivityLocalRedis.getInstance().zRevrangeWithScores(perkey);
		int size = ActivityConfig.getInstance().getActivityCircularRankSize();
		Set<Tuple> personSet = ActivityLocalRedis.getInstance().zrevrange(perkey, 0, Math.max((size - 1), 0));
		int rank = 0;
		for(Tuple t : personSet){
			try {
				String playerId = t.getElement();
				if (!getDataGeter().checkPlayerExist(playerId)) {
					continue;
				}
				PersonRank person = new PersonRank();
				String playerName = getDataGeter().getPlayerName(playerId);
				String guildName = getDataGeter().getGuildTagByPlayerId(playerId);
				person.setPlayerName(HawkOSOperator.isEmptyString(playerName) ? "" : playerName);
				person.setGuildName(HawkOSOperator.isEmptyString(guildName) ? "" : guildName);
				rank++;
				person.setScore((long)t.getScore());
				person.setRank(rank);
				historyRank.addPersonRank(person);
			} catch (Exception e) {
				HawkException.catchException(e, t.getElement());
			}
		}
		
		String guildKey = String.format(ActivityRedisKey.STRONGEST_GUILD_TOTAL_RANK, historyRank.getTermId());
		//Set<Tuple> guildSet = ActivityLocalRedis.getInstance().zRevrangeWithScores(guildKey);
		Set<Tuple> guildSet = ActivityLocalRedis.getInstance().zrevrange(guildKey, 0, Math.max((size - 1), 0));
		int guildRank = 0;
		for(Tuple t : guildSet){
			guildRank ++;
			String guildId = t.getElement();
			double score = t.getScore();
			GuildRank guild = new GuildRank();
			guild.setScore((long)score);
			guild.setRank(guildRank);
			String guildTag = getDataGeter().getGuildTag(guildId);
			String guildName = getDataGeter().getGuildName(guildId);
			guild.setGuildTag(HawkOSOperator.isEmptyString(guildTag) ? "" : guildTag);
			guild.setGuildName(HawkOSOperator.isEmptyString(guildName) ? "" : guildName);
			guild.setGuildFlag(getDataGeter().getGuildFlat(guildId));
			historyRank.addGuildRank(guild);
		}
		String value = JsonUtils.Object2Json(historyRank);
		ActivityLocalRedis.getInstance().lpush(ActivityRedisKey.STRONGEST_GUILD_HISTORY_RANK, value);
	}
	
	/***
	 * 领取个人阶段奖励
	 * @param playerId
	 */
	public Result<?> reqPersonStageReward(String playerId, int targetId){
		if(!isOpening(playerId)){ 
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<StrongestGuildEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		StrongestGuildEntity entity = opEntity.get();
		if(!entity.canAchieve(targetId)){
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		entity.notifyUpdate();
		//给奖励
		StrongestGuildTargetRewardCfg cfg = getTargetRewardConfig(targetId, entity.getBuildLevel(targetId));
		if(cfg == null){
			logger.error("StrongestGuildActivity recieve stage reward error, entity:{}", entity);
			return null;
		}
		logger.info("StrongestGuildActivity player recieve reward, cfg:{}, targetId:{}", cfg.getId(), targetId);
		this.getDataGeter().takeReward(playerId, cfg.getRewardList(), 1, Action.STRONGEST_GUILD_REWARD, true, RewardOrginType.ACTIVITY_REWARD);
		
		return null;
	}
	
	private StrongestGuildTargetRewardCfg getTargetRewardConfig(int targetId, int buildLevel){
//		ConfigIterator<StrongestGuildTargetRewardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildTargetRewardCfg.class);
//		while(ite.hasNext()){
//			StrongestGuildTargetRewardCfg cfg = ite.next();
//			if(cfg.getTargetId() == targetId){
//				if(cfg.getLvMax() >= buildLevel && cfg.getLvMin() <= buildLevel){
//					return cfg;
//				}
//			}
//		}
//		return null;
		return HawkConfigManager.getInstance().getConfigByKey(StrongestGuildTargetRewardCfg.class, targetId);
	}
	
	/***
	 * 给玩家推送活动界面信息
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<StrongestGuildEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		initPlayerStageData(playerId);
		StrongestGuildEntity entity = opEntity.get();
		StrongestGuildInfo.Builder info = StrongestGuildInfo.newBuilder();
		int curStage = getCurStageId();
		info.setStageId(curStage);
		info.setCurScore(entity.getScore());
		entity.buildTarget(info);
		StrongestGuildStageInfo stageInfo = map.get(curStage);
		info.setStartTime(stageInfo.getStartTime());
		info.setEndTime(stageInfo.getEndTime());
		info.setPrepareTime(stageInfo.getPrepareTime());
		//设置我的联盟总积分
		String guildId = getDataGeter().getGuildId(playerId);
		if(!HawkOSOperator.isEmptyString(guildId)){
			for(StrongestGuildRank rank : guildRank){
				if(rank instanceof StrongestGuildStageGuildRank){
					double score = rank.getScore(guildId); //设置联盟积分
					info.setGuildScore((long)score);
				}
			}
		}
		pushToPlayer(playerId, HP.code.STRONGEST_GUILD_INFO_S_VALUE, info);
	}
	
	/**
	 * 尝试初始化玩家阶段数据
	 * 
	 * @param playerId
	 * @param event 
	 */
	private void initPlayerStageData(String playerId) {
		int stageId = getCurStageId();
		Optional<StrongestGuildEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		StrongestGuildEntity entity = opEntity.get();
		// 阶段未发生变化,不进行处理
		if (entity.getStageId() == stageId) {
			return;
		}
		logger.info("StrongestGuildActivity change stege, before stage:{}, curStage:{}, entity:{}", entity.getStageId(), stageId, entity);
		entity.setStageId(stageId);
		entity.changeStage();
		int factoryLevel = getDataGeter().getConstructionFactoryLevel(playerId); //基地等级
		StrongestGuildCfg currencfg = HawkConfigManager.getInstance().getConfigByKey(StrongestGuildCfg.class, stageId);
		if (currencfg == null) {
			logger.error("ActivityCircularCfg is not found, stageId: {}", stageId);
			return;
		}
		for (Integer targetId : currencfg.getTargetIdList()) {
			ConfigIterator<StrongestGuildTargetRewardCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildTargetRewardCfg.class);
			while (cfgIterator.hasNext()) {
				StrongestGuildTargetRewardCfg targetCfg = cfgIterator.next();
				if (targetCfg.getTargetId() != targetId) {
					continue;
				}
				if (factoryLevel < targetCfg.getLvMin() || factoryLevel > targetCfg.getLvMax()) {
					continue;
				}
				//初始化
				TargetInfo target = new TargetInfo(targetCfg.getId(), targetCfg.getScore(), factoryLevel);
				entity.addTarget(target);
				entity.notifyUpdate();
			}
		}
	}
	
	/***
	 * 推送个人榜单
	 * @param playerId
	 */
	public void syncPersonRankInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		//获取个人阶段榜单
		StrongestGuildPersonRank.Builder builder = StrongestGuildPersonRank.newBuilder();
		for(StrongestGuildRank rank : personRank){
			buildPersonRank(rank, playerId, builder);
		}
		int stageId = getCurStageId();
		if(stageId != getFirstStageCfg().getStageId()){
			//构建其它阶段排行榜
			buildOtherPersonStageRank(stageId, builder, playerId);
		}
		
		pushToPlayer(playerId, HP.code.STRONGEST_GUILD_PERSONAL_RANK_S_VALUE, builder);
	}
	
	/***
	 * 构建当前阶段的前面阶段排行榜
	 * @param stageId 当前阶段
	 * @param builder
	 */
	private void buildOtherPersonStageRank(int stageId, StrongestGuildPersonRank.Builder builder, String playerId){
		StrongestGuildStageInfo curInfo = map.get(stageId);
		if(curInfo != null){
			int tempStageId = curInfo.getBeforeStageId();
			while(tempStageId != 0){
				String key = String.format(ActivityRedisKey.STRONGEST_GUILD_PERSONAL_RANK, getActivityTermId(), tempStageId);
				RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, playerId);
				int myRank = 0; //我的阶段排名，默认没上榜
				double myScore = 0;
				if (index != null) {
					myRank = index.getIndex().intValue() + 1;
					myScore = index.getScore().longValue();
				}
				
				RankPB.Builder rankBuilder = buildPlayerRankPB(playerId, myRank, myScore);
				if (rankBuilder != null) {
					OtherStagePersonRank.Builder otherStageRank = OtherStagePersonRank.newBuilder();
					otherStageRank.setRank(rankBuilder);
					builder.addOtherStageRank(otherStageRank);
				}
				StrongestGuildStageInfo info = map.get(tempStageId);
				tempStageId = info.getBeforeStageId();
			}
		}
	}
	
	
	/***
	 * 推送联盟榜单
	 * @param playerId
	 */
	public void syncGuildRankInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		StrongestGuildGuildRank.Builder builder = StrongestGuildGuildRank.newBuilder();
		String guildId = getDataGeter().getGuildId(playerId);
		for(StrongestGuildRank rank : guildRank){
			buildGuildRank(rank, guildId, builder);
		}
		int stageId = getCurStageId();
		if(stageId != getFirstStageCfg().getStageId()){
			//构建其它阶段排行榜
			buildOtherGuildStageRank(stageId, builder, guildId);
		}
		
		pushToPlayer(playerId, HP.code.STRONGEST_GUILD_GUILD_RANK_S_VALUE, builder);
	}
	
	private void buildOtherGuildStageRank(int stageId, StrongestGuildGuildRank.Builder builder, String myGuildId){
		if(HawkOSOperator.isEmptyString(myGuildId)){
			return;
		}
		StrongestGuildStageInfo curInfo = map.get(stageId);
		if(curInfo != null){
			int tempStageId = curInfo.getBeforeStageId();
			while(tempStageId != 0){
				String key = String.format(ActivityRedisKey.STRONGEST_GUILD_STAGE_RANK, getActivityTermId(), tempStageId);
				RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(key, myGuildId);
				OtherStageGuildRank.Builder otherStageRank = OtherStageGuildRank.newBuilder();
				int rank = 0;
				long score = 0;
				if (index != null) {
					rank = index.getIndex().intValue() + 1;
					score = index.getScore().longValue();
				}
				otherStageRank.setStageId(tempStageId);
				otherStageRank.addRank(buildGuildRankPB(myGuildId, rank, score));
				StrongestGuildStageInfo info = map.get(tempStageId);
				tempStageId = info.getBeforeStageId();
				builder.addOtherStageRank(otherStageRank);
				
//				Set<Tuple> set = ActivityLocalRedis.getInstance().zRevrangeWithScores(key);
//				int i = 0;
//				OtherStageGuildRank.Builder otherStageRank = OtherStageGuildRank.newBuilder();
//				otherStageRank.setStageId(tempStageId);
//				for(Tuple t : set){
//					i ++;
//					String id = t.getElement(); //guildId
//					otherStageRank.addRank(buildGuildRankPB(id, i, t.getScore()));
//				}
//				StrongestGuildStageInfo info = map.get(tempStageId);
//				tempStageId = info.getBeforeStageId();
//				builder.addOtherStageRank(otherStageRank);
			}
		}
	}
	
	private void buildGuildRank(StrongestGuildRank rank, String myGuildId, StrongestGuildGuildRank.Builder builder){
		Set<Tuple> totalSet = rank.getRankList();
		int i = 1;
		for(Tuple t : totalSet){
			String guildId = t.getElement();
			double score = t.getScore();
			StrongestGuildRankInfo.Builder rankInfo = buildGuildRankPB(guildId, i, score);
			if(rankInfo == null){
				continue;
			}
			if(rank instanceof StrongestGuildStageGuildRank){
				builder.addStageRank(rankInfo);
			}else if(rank instanceof StrongestGuildTotalGuildRank){
				builder.addTotalRank(rankInfo);
			}
			i ++;
		}
		if(!HawkOSOperator.isEmptyString(myGuildId)){
			//获取我的联盟积分
			StrongestGuildRankInfo.Builder guildRank = StrongestGuildRankInfo.newBuilder();
			RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(rank.key(), myGuildId);
			int myGuildrank = 0;
			long myGuildscore = 0;
			if (index != null) {
				myGuildrank = index.getIndex().intValue() + 1;
				myGuildscore = index.getScore().longValue();
			}
			guildRank.setRank(myGuildrank);
			String guildName = getDataGeter().getGuildName(myGuildId);
			if(!HawkOSOperator.isEmptyString(guildName)){
				guildRank.setGuildName(guildName);
			}
			guildRank.setScore(myGuildscore);
			guildRank.setGuildFlag(getDataGeter().getGuildFlat(myGuildId));
			String guildTag = getDataGeter().getGuildTag(myGuildId);
			if(!HawkOSOperator.isEmptyString(guildTag)){
				guildRank.setGuildTag(guildTag);
			}
			if(rank instanceof StrongestGuildStageGuildRank){
				builder.setMyStageRank(guildRank);
			}else if(rank instanceof StrongestGuildTotalGuildRank){
				builder.setMyTotalRank(guildRank);
			}
		}
	}
	
	private StrongestGuildRankInfo.Builder buildGuildRankPB(String guildId, int rank, double score){
		StrongestGuildRankInfo.Builder rankInfo = StrongestGuildRankInfo.newBuilder();
		rankInfo.setRank(rank);
		String guildName = getDataGeter().getGuildName(guildId);
		if (!HawkOSOperator.isEmptyString(guildName)) {
			rankInfo.setGuildName(guildName);
		}else{
			return null;
		}
		rankInfo.setScore((long)score);
		rankInfo.setGuildFlag(getDataGeter().getGuildFlat(guildId));
		String guildTag = getDataGeter().getGuildTag(guildId);
		if(!HawkOSOperator.isEmptyString(guildTag)){
			rankInfo.setGuildTag(guildTag);
		}
		return rankInfo;
	}
	
	private void buildPersonRank(StrongestGuildRank rank, String playerId, StrongestGuildPersonRank.Builder builder){
		Set<Tuple> mySet = rank.getRankList(); //个人总榜
		int i = 0;
		for(Tuple t : mySet){
			String id = t.getElement();
			if (!getDataGeter().checkPlayerExist(id)) {
				continue;
			}
			i++;
			if(rank instanceof StrongestGuildPersonalStageRank){
				builder.addStageRank(buildPlayerRankPB(id, i, t.getScore()));
			}else if(rank instanceof StrongestGuildPersonalTotalRank){
				builder.addTotalRank(buildPlayerRankPB(id, i, t.getScore()));
			}
		}
		RedisIndex index = ActivityLocalRedis.getInstance().zrevrank(rank.key(), playerId);
		int myRank = 0; //我的阶段排名，默认没上榜
		double myScore = 0;
		if (index != null) {
			myRank = index.getIndex().intValue() + 1;
			myScore = index.getScore().longValue();
		}
		if(rank instanceof StrongestGuildPersonalStageRank){
			builder.setMyStageRank(buildPlayerRankPB(playerId, myRank, myScore));
		}else if(rank instanceof StrongestGuildPersonalTotalRank){
			builder.setMyTotalRank(buildPlayerRankPB(playerId, myRank, myScore));
		}
	}
	
	private RankPB.Builder buildPlayerRankPB(String playerId, int rank, double score) {
		ActivityDataProxy dataGeter = getDataGeter();
		if (!dataGeter.checkPlayerExist(playerId)) {
			return null;
		}
		RankPB.Builder rankPBBuilder = RankPB.newBuilder();
		try {
			String playerName = dataGeter.getPlayerName(playerId);
			List<Integer> protectVals = dataGeter.getPersonalProtectVals(playerId);
			String guildName = dataGeter.getGuildTagByPlayerId(playerId);
			rankPBBuilder.setPlayerName(playerName);
			rankPBBuilder.addAllPersonalProtectSwitch(protectVals);
			if (!HawkOSOperator.isEmptyString(guildName)) {
				rankPBBuilder.setGuildName(guildName);
			}
		} catch (Exception e) {
			HawkException.catchException(e, playerId);
		}
		rankPBBuilder.setPlayerId(playerId);
		rankPBBuilder.setRank(rank);
		rankPBBuilder.setScore((long)score);
		return rankPBBuilder;
	}
	
	/***
	 * 推送玩家状态变化(积分改变)
	 * @param playerId
	 */
	private void sendPlayerScoreChange(String playerId, StrongestGuildEntity entity){
		StrongestGuildStateChange.Builder builder = StrongestGuildStateChange.newBuilder();
		builder.setScore(entity.getScore());
		pushToPlayer(playerId, HP.code.STRONGEST_GUILD_SCORE_CHANGE_S_VALUE, builder);
	}
	
	/***
	 * 个人排行奖励集合
	 * @param rankId
	 * @return
	 */
	private List<StrongestGuildPeronRankReward> getRankListReward(int rankId) {
		List<StrongestGuildPeronRankReward> list = new ArrayList<>();
		ConfigIterator<StrongestGuildPeronRankReward> configIterator = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildPeronRankReward.class);
		while (configIterator.hasNext()) {
			StrongestGuildPeronRankReward next = configIterator.next();
			if (next.getRankId() == rankId) {
				list.add(next);
			}
		}
		return list;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	/***
	 * 获取粗略阶段
	 * (由于tick几秒的差异，通过curStage获取的阶段是粗略的)
	 * @return0
	 */
	public int getCurStageId() {
		return curStage.getStageId();
	}
	
	/***
	 * 获取个人排行奖励最次的排行
	 * 此函数服务排行榜奖励
	 * @param stageId
	 * @return 最次的排行，一般都是配置的100
	 */
	public int getHasPersonRewardMaxRank(int rankId){
		ConfigIterator<StrongestGuildPeronRankReward> ite = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildPeronRankReward.class);
		int max = 0;
		while(ite.hasNext()){
			StrongestGuildPeronRankReward cfg = ite.next();
			if(cfg.getRankId() != rankId){
				continue;
			}
			if(cfg.getRankLower() > max){
				max = cfg.getRankLower();
			}
		}
		return max;
	}
	
	/***
	 * 获取第一个阶段的配置
	 * @return
	 */
	public StrongestGuildCfg getFirstStageCfg(){
		ConfigIterator<StrongestGuildCfg> ite = HawkConfigManager.getInstance().getConfigIterator(StrongestGuildCfg.class);
		while(ite.hasNext()){
			StrongestGuildCfg config = ite.next();
			if(config.getBeforeStageId() == 0){
				return config;
			}
		}
		return null;
	}
	
	/**
	 * 移除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		for(StrongestGuildRank rank : personRank){
			try {
				rank.remove(playerId);
				rank.doRank();
			} catch (Exception e){
				HawkException.catchException(e);
			}
		}
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
}
