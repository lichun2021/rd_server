package com.hawk.activity.type.impl.energies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.EnergiesGuildScoreEvent;
import com.hawk.activity.event.impl.EnergiesSelfScoreEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.SummonMonsterEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.energies.cfg.EnergiesAchieve;
import com.hawk.activity.type.impl.energies.cfg.EnergiesActivityKVCfg;
import com.hawk.activity.type.impl.energies.cfg.EnergiesGuildRankRewardCfg;
import com.hawk.activity.type.impl.energies.cfg.EnergiesSelfRankRewardCfg;
import com.hawk.activity.type.impl.energies.entity.EnergiesEntity;
import com.hawk.activity.type.impl.energies.rank.EnergiesRankObject;
import com.hawk.game.protocol.Activity.EnergiesRankType;
import com.hawk.game.protocol.Activity.GetEnergiesPageInfoResp;
import com.hawk.game.protocol.Activity.GetEnergiesRankInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

import redis.clients.jedis.Tuple;

public class EnergiesActivity extends ActivityBase implements AchieveProvider {

	public EnergiesActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.ENERGIES_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.ENERGIES_AWARD;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	/** 上次排行刷新时间*/
	private long lastCheckTime = 0;
	
	/** 个人积分排行*/
	private EnergiesRankObject selfRank= new EnergiesRankObject(EnergiesRankType.ENERGIES_SELF_RANK);
	
	/** 联盟积分排行*/
	private EnergiesRankObject guildRank= new EnergiesRankObject(EnergiesRankType.ENERGIES_GUILD_RANK);
	
	/**
	 * 联盟每日积分
	 */
	private Map<String, Long> guildScoreMap = new ConcurrentHashMap<>();
	
	/**
	 * 联盟积分变更Map(定时更新)
	 */
	private Map<String, Long> guildScoreChangeMap = new ConcurrentHashMap<>();

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EnergiesActivity activity = new EnergiesActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EnergiesEntity> queryList = HawkDBManager.getInstance()
				.query("from EnergiesEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EnergiesEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EnergiesEntity entity = new EnergiesEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		selfRank = new EnergiesRankObject(EnergiesRankType.ENERGIES_SELF_RANK);
		guildRank = new EnergiesRankObject(EnergiesRankType.ENERGIES_GUILD_RANK);
		guildScoreMap = new ConcurrentHashMap<>();
		guildScoreChangeMap = new ConcurrentHashMap<>();
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_MACHINE_AWAKE, () -> {
				initAchieveInfo(playerId);
			});
		}
	}
	
	@Override
	public void onEnd() {
		int termId = getActivityTermId();
		sendSelfRankReward(termId);
		sendGuildRankReward(termId);
	}

	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<EnergiesEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		EnergiesEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<EnergiesAchieve> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergiesAchieve.class);
		while (configIterator.hasNext()) {
			EnergiesAchieve next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}
	
	@Override
	public boolean isProviderNeedSync(String playerId) {
		return !isHidden(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<EnergiesEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		EnergiesEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(EnergiesAchieve.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(EnergiesAchieve.class, achieveId);
		}
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<EnergiesEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		EnergiesEntity entity = opPlayerDataEntity.get();
		List<AchieveItem> items = new ArrayList<>();
		ConfigIterator<EnergiesAchieve> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(EnergiesAchieve.class);
		while (achieveIterator.hasNext()) {
			EnergiesAchieve achieveCfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			items.add(item);
		}
		entity.resetItemList(items);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		// 推送活动界面信息
		pullPageInfo(playerId);
	}

	
	/**
	 * 攻击野怪
	 * @param event
	 */
	@Subscribe
	public void onAttackMonster(MonsterAttackEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		String guildId = getDataGeter().getGuildId(playerId);
		int monsterType = event.getMosterType();
		int monsterId = event.getMonsterId();
		int termId = getActivityTermId();
		boolean needPush = false;
		EnergiesActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(EnergiesActivityKVCfg.class);
		switch (monsterType) {
		case MonsterType.TYPE_1_VALUE:
		case MonsterType.TYPE_2_VALUE:
			if (!event.isKill()) {
				return;
			}
			
			int count = 0;
			for (int i=0; i<event.getAtkTimes(); i++) {
				int rand = HawkRand.randInt(GameConst.RANDOM_MYRIABIT_BASE);
				// 额外掉落道具
				if (rand < kvCfg.getExtraRewardRate()) {
					count++;
				}
			}
			if (count > 0) {
				final int cnt = count;
				ImmutableList<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(kvCfg.getExtraReward());
				rewardList.forEach(e -> e.setItemCount(e.getItemCount() * cnt));
				if (event.getAtkTimes() > 1) {
					Object[] content =  new Object[]{event.getAtkTimes()};
					sendMailToPlayer(playerId,  MailId.ENERGIES_DROP_MULTI, new Object[0], new Object[0], content, rewardList);
				} else {
					Object[] content =  new Object[]{getActivityCfg().getActivityName()};
					sendMailToPlayer(playerId,  MailId.ENERGIES_DROP, new Object[0], new Object[0], content, rewardList);
				}
			}
			break;
		case MonsterType.TYPE_3_VALUE:
			if (!event.isKill()) {
				return;
			}
			int addScore = kvCfg.getKillScore(monsterId);
			if (addScore <= 0) {
				break;
			}
			
			addScore = addScore * event.getAtkTimes();
			selfScoreAdd(termId, playerId, addScore, 2);
			needPush = true;
			// 联盟积分处理,仅队长计算积分
			if (HawkOSOperator.isEmptyString(guildId) || !event.isLeader()) {
				break;
			}
			guildScoreAdd(playerId, guildId, addScore, 2);
			break;
		default:
			return;
		}

		// 推送主界面信息
		if (needPush) {
			pullPageInfo(playerId);
		}
	}
	
	/**
	 * 地图放置野怪
	 * @param event
	 */
	@Subscribe
	public void onSummonMonster(SummonMonsterEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		EnergiesActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(EnergiesActivityKVCfg.class);
		int termId = getActivityTermId();
		int monsterId = event.getMonsterId();
		String guildId = getDataGeter().getGuildId(playerId);
		int addScore = kvCfg.getSummonScore(monsterId);
		if (addScore <= 0) {
			return;
		}
		// 个人积分添加
		selfScoreAdd(termId, playerId, addScore, 1);
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildScoreAdd(playerId, guildId, addScore, 1);
		}
		// 推送主界面信息
		pullPageInfo(playerId);
	}
	
	/**
	 * 个人积分增加
	 * @param termId
	 * @param playerId
	 * @param addScore
	 */
	public void selfScoreAdd(int termId, String playerId, int addScore, int addType) {
		Optional<EnergiesEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		EnergiesEntity dataEntity = opPlayerDataEntity.get();
		dataEntity.setDailyScore(dataEntity.getDailyScore() + addScore);
		// 个人积分变更事件
		ActivityManager.getInstance().postEvent(new EnergiesSelfScoreEvent(playerId, dataEntity.getDailyScore()));
		selfRank.addRankScore(playerId, termId, addScore);
		getDataGeter().logEnergiesSelfScore(playerId, termId, addScore, addType, dataEntity.getDailyScore());
	}
	/**
	 * 添加联盟积分
	 * @param playerId
	 * @param guildId
	 * @param addScore
	 */
	public void guildScoreAdd(String playerId, String guildId, int addScore, int addType) {
		if (addScore <= 0) {
			return;
		}
		int termId = getActivityTermId();
		long oldScore = 0;
		// 添加联盟积分
		if (guildScoreMap.containsKey(guildId)) {
			oldScore = guildScoreMap.get(guildId);
		}
		guildScoreMap.put(guildId, oldScore + addScore);
		guildScoreChangeMap.put(guildId, guildScoreMap.get(guildId));
		long score = guildRank.addRankScore(guildId, termId, addScore);
		pushGuildScoreEvent(playerId, guildId);
		getDataGeter().logEnergiesGuildScore(guildId, termId, addScore, addType, score);
	}

	/**
	 * 联盟解散
	 * @param event
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event){
		guildRank.removeRank(event.getGuildId(), getActivityTermId());
		guildScoreMap.remove(event.getGuildId());
	}
	
	@Override
	public void onTick() {
		if(lastCheckTime == 0){
			loadGuildDailyScore();
			lastCheckTime = HawkTime.getMillisecond();
			return;
		}
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		EnergiesActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(EnergiesActivityKVCfg.class);
		long gap = kvCfg.getRankPeriod();
		// 跨天清除联盟每日积分数据
		if (!HawkTime.isSameDay(currTime, lastCheckTime)) {
			guildScoreMap = new ConcurrentHashMap<>();
			guildScoreChangeMap = new ConcurrentHashMap<>();
		}
		
		if (currTime - lastCheckTime > gap) {
			// 刷新榜单
			refreshRankInfo(termId);
			// 在线成员推送联盟积分事件
			pushGuildScoreEvent();
			// 更新联盟今日积分数据
			updateGuildScore();
			guildScoreChangeMap = new ConcurrentHashMap<>();
			lastCheckTime = currTime;
		}
	}
	
	/**
	 * 加载联盟每日积分
	 */
	private void loadGuildDailyScore() {
		String key = getDailyScoreKey();
		Set<Tuple> result = ActivityLocalRedis.getInstance().zRevrangeWithScores(key);
		Map<String, Long> scoreMap = new ConcurrentHashMap<>();
		for(Tuple tuple :result){
			scoreMap.put(tuple.getElement(), (long) tuple.getScore());
		}
		guildScoreMap = scoreMap;
		guildScoreChangeMap = new ConcurrentHashMap<>();
	}

	/**
	 * 获取联盟积分(用于同步联盟积分成就,跨天后一分钟内返回0)
	 * @param guildId
	 */
	private long getGuildScore(String guildId) {
		long dailyAM0 = HawkTime.getAM0Date().getTime();
		if (HawkTime.getMillisecond() <= dailyAM0 + 60 * 1000l) {
			return 0;
		}
		if (!guildScoreMap.containsKey(guildId)) {
			return 0;
		}
		return guildScoreMap.get(guildId);
	}
	
	/**
	 * 推送联盟积分事件
	 * @param playerId
	 * @param guildId
	 */
	private void pushGuildScoreEvent(String playerId, String guildId){
		long guildScore = getGuildScore(guildId);
		if (guildScore > 0) {
			// 联盟积分变更事件
			ActivityManager.getInstance().postEvent(new EnergiesGuildScoreEvent(playerId, guildScoreMap.get(guildId)));
		}
	}
	
	// 刷新榜单数据
	private void refreshRankInfo(int termId) {
		selfRank.refreshRank(termId);
		guildRank.refreshRank(termId);
		
	}

	/**
	 * 发送个人排行奖励
	 * @param termId
	 */
	private void sendSelfRankReward(int termId) {
		try {
			selfRank.refreshRank(termId);
			MailId mailId = MailId.ENERGIES_SELF_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = selfRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String playerId = tuple.getElement();
				EnergiesSelfRankRewardCfg rankCfg = getSelfRankReward(rank);
				if (rankCfg == null) {
					HawkLog.errPrintln("EnergiesSelfRankRewardCfg error! playerId: {}, rank :{}", playerId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;

				sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
				getDataGeter().logEnergiesRank(termId, 1, playerId, rank, (long) tuple.getScore());
				HawkLog.logPrintln("EnergiesActivity send self rankReward, playerId: {}, rank: {}, cfgId: {}", playerId, rank, rankCfg.getId());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 发送联盟排行奖励
	 * @param termId
	 */
	private void sendGuildRankReward(int termId) {
		try {
			guildRank.refreshRank(termId);
			MailId mailId = MailId.ENERGIES_GUILD_RANK;
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			Set<Tuple> rankTuple = guildRank.getRankTuples();
			int rank = 0;
			for (Tuple tuple : rankTuple) {
				rank++;
				String guildId = tuple.getElement();
				EnergiesGuildRankRewardCfg rankCfg = getGuildRankReward(rank);
				if (rankCfg == null) {
					HawkLog.errPrintln("EnergiesGuildRankRewardCfg guild rank cfg error! guildId: {}, rank :{}", guildId, rank);
					continue;
				}
				// 邮件发送奖励
				Object[] content;
				content = new Object[2];
				content[0] = getActivityCfg().getActivityName();
				content[1] = rank;
				Collection<String> ids = getDataGeter().getGuildMemberIds(guildId);
				for(String playerId : ids){
					sendMailToPlayer(playerId, mailId, title, subTitle, content, rankCfg.getRewardList());
					HawkLog.logPrintln("EnergiesActivity send guild rankReward, guildId: {}, playerId: {}, rank: {}, cfgId: {}", guildId, playerId, rank, rankCfg.getId());
				}
				getDataGeter().logEnergiesRank(termId, 2, guildId, rank, (long) tuple.getScore());
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 获取个人排行奖励配置
	 * @param rank
	 * @return
	 */
	private EnergiesSelfRankRewardCfg getSelfRankReward(int rank) {
		EnergiesSelfRankRewardCfg rankCfg = null;
		ConfigIterator<EnergiesSelfRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergiesSelfRankRewardCfg.class);
		for (EnergiesSelfRankRewardCfg cfg : configIterator) {
			if ( rank <= cfg.getRankLow() && rank >= cfg.getRankHigh()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}
	
	/**
	 * 获取联盟排行奖励配置
	 * @param rank
	 * @return
	 */
	private EnergiesGuildRankRewardCfg getGuildRankReward(int rank) {
		EnergiesGuildRankRewardCfg rankCfg = null;
		ConfigIterator<EnergiesGuildRankRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(EnergiesGuildRankRewardCfg.class);
		for (EnergiesGuildRankRewardCfg cfg : configIterator) {
			if ( rank <= cfg.getRankLow() && rank >= cfg.getRankHigh()) {
				rankCfg = cfg;
				break;
			}
		}
		return rankCfg;
	}

	/**
	 * 拉取活动界面信息
	 * @param playerId
	 */
	public void pullPageInfo(String playerId) {
		Optional<EnergiesEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		EnergiesEntity dataEntity = opPlayerDataEntity.get();
		GetEnergiesPageInfoResp.Builder builder = GetEnergiesPageInfoResp.newBuilder();
		builder.setDailyScore(dataEntity.getDailyScore());
		String guildId = getDataGeter().getGuildId(playerId);
		long guildScore = 0;
		if (!HawkOSOperator.isEmptyString(guildId)) {
			guildScore = guildScoreMap.containsKey(guildId) ? guildScoreMap.get(guildId) : 0;
		}
		builder.setGuildScore(guildScore);
		pushToPlayer(playerId, HP.code.ENERGIES_GET_PAGE_INFO_S_VALUE, builder);
		// 请求或同步界面信息时,推送联盟积分事件
		if (!HawkOSOperator.isEmptyString(guildId) && guildScoreChangeMap.containsKey(guildId)) {
			pushGuildScoreEvent(playerId, guildId);
		}
	}

	/**
	 * 拉取榜单信息
	 * @param playerId
	 * @param rankType
	 */
	public void pullRankInfo(String playerId, EnergiesRankType rankType) {
		Optional<EnergiesEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EnergiesEntity entity = opEntity.get();
		GetEnergiesRankInfoResp.Builder builder = null;
		switch (rankType) {
		case ENERGIES_SELF_RANK:
			builder = selfRank.buildRankInfoResp(entity);
			break;

		case ENERGIES_GUILD_RANK:
			builder = guildRank.buildRankInfoResp(entity);
			break;
		}
		
		if(builder != null){
			pushToPlayer(playerId, HP.code.ENERGIES_GET_RANK_INFO_S_VALUE, builder);
		}
		
	}

	/**
	 * 更新联盟积分
	 */
	public void updateGuildScore() {
		if (guildScoreChangeMap.isEmpty()) {
			return;
		}
		Map<String, Double> updateMap = new HashMap<>();
		for (Entry<String, Long> entry : guildScoreChangeMap.entrySet()) {
			updateMap.put(entry.getKey(), 1d * entry.getValue());
		}
		String key = getDailyScoreKey();
		ActivityLocalRedis.getInstance().zadd(key, updateMap, 5 * 24 * 3600);
	}
	
	/**
	 * 获取联盟每日积分存储的redis的key
	 * @return
	 */
	public String getDailyScoreKey() {
		int termId = getActivityTermId();
		long openTime = getTimeControl().getStartTimeByTermId(termId);
		int gapDay = HawkTime.calcBetweenDays(new Date(openTime), new Date(HawkTime.getMillisecond()));
		return ActivityRedisKey.ENERGIES_GUILD_DAILY_SCORE + ":" + termId + ":" + gapDay;
	}
	
	/**
	 * 联盟在线成员推送联盟积分事件
	 */
	public void pushGuildScoreEvent() {
		Set<String> guildIds = guildScoreChangeMap.keySet();
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				for (String guildId : guildIds) {
					List<String> onlineMembers = getDataGeter().getOnlineGuildMemberIds(guildId);
					if (onlineMembers.isEmpty()) {
						continue;
					}
					for (String playerId : onlineMembers) {
						pushGuildScoreEvent(playerId, guildId);
					}
				}
				return null;
			}
		});
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

	@Override
	public boolean handleForMergeServer() {
		if (getActivityEntity().getActivityState() == ActivityState.HIDDEN) {
			return true;
		}
	
		int termId = this.getActivityTermId();
		sendSelfRankReward(termId);
		sendGuildRankReward(termId);
	
		return true;
	}
}
