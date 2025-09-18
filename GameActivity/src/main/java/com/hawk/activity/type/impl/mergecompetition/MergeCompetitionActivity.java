package com.hawk.activity.type.impl.mergecompetition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BattlePointChangeEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GiftPurchasePriceEvent;
import com.hawk.activity.event.impl.GuildDismissEvent;
import com.hawk.activity.event.impl.GuildQuiteEvent;
import com.hawk.activity.event.impl.JoinGuildEvent;
import com.hawk.activity.event.impl.MergeCompeteRefreshEvent;
import com.hawk.activity.event.impl.CostDiamondBuyGiftEvent;
import com.hawk.activity.event.impl.VitCostEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionGiftCfg;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionRankCfg;
import com.hawk.activity.type.impl.mergecompetition.cfg.MergeCompetitionTargetCfg;
import com.hawk.activity.type.impl.mergecompetition.entity.MergeCompetitionEntity;
import com.hawk.activity.type.impl.mergecompetition.rank.CompeteServerInfo;
import com.hawk.activity.type.impl.mergecompetition.rank.GiftRewardInfo;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompeteRankHelper;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRank;
import com.hawk.activity.type.impl.mergecompetition.rank.MergeCompetitionRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.RankGuildInfo;
import com.hawk.activity.type.impl.mergecompetition.rank.RankPlayerInfo;
import com.hawk.activity.type.impl.mergecompetition.rank.ServerRankInfo;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.GiftScoreRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.GuildPowerRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.PersonalPowerRankProvider;
import com.hawk.activity.type.impl.mergecompetition.rank.impl.VitCostRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankContext;
import com.hawk.activity.type.impl.rank.ActivityRankProvider;
import com.hawk.activity.type.impl.rank.ActivityRankType;
import com.hawk.common.ServerInfo;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.MCAwardInfo;
import com.hawk.game.protocol.Activity.MergeCompeteInfoPB;
import com.hawk.game.protocol.Activity.MergeCompeteRank;
import com.hawk.game.protocol.Activity.MergeCompeteRankType;
import com.hawk.game.protocol.Activity.MergeCompeteServerScore;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.gamelib.GameConst;
import com.hawk.gamelib.player.PowerData;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 新服合服比拼活动
 * 
 * @author lating
 *
 */
public class MergeCompetitionActivity extends ActivityBase implements AchieveProvider{
	
	/**
	 * 联盟战力变化最新时间
	 */
	private Map<String, AtomicLong> guildPowerLastChangeTimeMap = new ConcurrentHashMap<>();
	/**
	 * 嘉奖礼包变化最新时间
	 */
	private AtomicLong giftRewardLastChangeTime = new AtomicLong(0);
	
	private MergeCompeteRankHelper rankHelper = new MergeCompeteRankHelper();
	
	private CompeteServerInfo competeServerInfo = new CompeteServerInfo();
	
	private long tickTime;

	public MergeCompetitionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MERGE_COMPETITION;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MergeCompetitionActivity activity = new MergeCompetitionActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MergeCompetitionEntity> queryList = HawkDBManager.getInstance().query("from MergeCompetitionEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		return new MergeCompetitionEntity(playerId, termId);
	}
	
	@Override
	public void onOpen() {
		HawkLog.logPrintln("MergeCompetitionActivity open");
		initServerGroup();
		if (HawkOSOperator.isEmptyString(competeServerInfo.getServerGroup())) {
			return;
		}
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			if (!isOpening(playerId)) {
				continue;
			}
			callBack(playerId, GameConst.MsgId.MERGE_COMPETE_INIT, ()->{
				initAchieveItems(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		Long serverMergeTime = this.getDataGeter().getServerMergeTime();
		if (serverMergeTime != null && serverMergeTime.longValue() < HawkApp.getInstance().getCurrentTime()) {
			return true;
		}
		if (competeServerInfo.isServerEmpty()) {
			initServerGroup();
		}
		if (HawkOSOperator.isEmptyString(competeServerInfo.getServerGroup())) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean isOpening(String playerId) {
		if (isInvalid()) {
			return false;
		}
		
		if (isActivityClose(playerId)) {
			return false;
		}
		
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		return state == ActivityState.OPEN;
	}
	
	@Override
	public void onTick() {
		long showTime = getShowTime();
		if (showTime <= 0) {
			String serverId = getDataGeter().getServerId();
			if (Integer.parseInt(serverId) / 10000 == 1 && Integer.parseInt(serverId) >= 10923 
					&& !HawkOSOperator.isEmptyString(competeServerInfo.getServerGroup())) {
				competeServerInfo.getServerList().clear();
				competeServerInfo.setServerGroup("");
			}
			return;
		}
		
		if (competeServerInfo.isServerEmpty()) {
			initServerGroup();
		}
		if (HawkOSOperator.isEmptyString(competeServerInfo.getServerGroup())) {
			return;
		}
		
		if (tickTime == 0) {
			tickTime = HawkTime.getMillisecond();
			return;
		}
		
		long now = HawkTime.getMillisecond();
		if (now - tickTime < 10000L) {
			return;
		}
		
		tickTime = now;
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				continue;
			}
			MergeCompetitionEntity entity = opEntity.get();
			long giftRewardChangeTime = giftRewardLastChangeTime.get();
			boolean giftReward = false, guildPower = false;
			if (giftRewardChangeTime > 0 && giftRewardChangeTime - entity.getAwardIdRefreshTime() >= 3000L) {
				giftReward = true;
			}
			
			String guildId = this.getDataGeter().getGuildId(playerId);
			if (!HawkOSOperator.isEmptyString(guildId) && entity.getGuildPowerTargetFinish() == 0) {
				AtomicLong timeLong = guildPowerLastChangeTimeMap.get(guildId);
				if (timeLong != null && timeLong.get() - entity.getGuildPowerTargetTime() >= 3000L) {
					guildPower = true;
				}
			}
			
			if (giftReward || guildPower) {
				HawkLog.debugPrintln("MergeCompetitionActivity tick postEvent, playerId: {}, giftReward: {}, guildPower: {}", playerId, giftReward, guildPower);
				ActivityManager.getInstance().postEvent(new MergeCompeteRefreshEvent(playerId, giftReward, guildPower));
			}
		}
	}
	
	/**
	 * 服务器组信息初始化
	 */
	private void initServerGroup() {
		String serverId = getDataGeter().getServerId();
		if (Integer.parseInt(serverId) / 10000 == 2 || Integer.parseInt(serverId) < 10915) {
			return;
		}
		
		if (!HawkOSOperator.isEmptyString(competeServerInfo.getServerGroup())) {
			return;
		}
		
		long showTime = getShowTime();
		if (showTime <= 0) {
			return;
		}

		Map<String, String> mergeSvrInfoMap = getRedis().hGetAll(MergeCompetitionConst.MERGE_SERVER_INFO);
		if (mergeSvrInfoMap.isEmpty() || !mergeSvrInfoMap.containsKey(serverId)) {
			if (showTime <= HawkTime.getMillisecond()) {
				competeServerInfo.addServer(serverId);
				HawkLog.logPrintln("MergeCompetitionActivity serverGroup: {}, mergeSvrInfo: {}, showTime: {}, serverCount: {}, serverlist: {}", serverId, mergeSvrInfoMap.keySet(), showTime, competeServerInfo.getServerCount(), competeServerInfo.getServerList());
			} 
			return;
		}
		
		Map<String, Long> serverOpenTimeMap = new HashMap<>();
		List<ServerInfo> serverList = this.getDataGeter().getServerList();
		serverList.stream().forEach(e -> serverOpenTimeMap.put(e.getId(), HawkTime.parseTime(e.getOpenTime())));
		long thisServerOpenTime = serverOpenTimeMap.getOrDefault(serverId, 0L);
		if (thisServerOpenTime <= 0) {
			HawkLog.logPrintln("MergeCompetitionActivity serverGroup: {}, thisServerOpenTime empty", serverId);
			return;
		}
		
		int mergeTimeInt = Integer.MAX_VALUE;
		List<Integer> intServerGroup = new ArrayList<>();
		intServerGroup.add(Integer.parseInt(serverId));
		for (Entry<String, String> entry : mergeSvrInfoMap.entrySet()) {
			try {
				String server = entry.getKey();
				String mergeTime = entry.getValue();
				if (server.equals(serverId)) {
					competeServerInfo.addServer(server);
					mergeTimeInt = Math.min(mergeTimeInt, Integer.parseInt(mergeTime));
					continue;
				}
				long serverOpenTime = serverOpenTimeMap.getOrDefault(server, 0L);
				if (HawkTime.isSameDay(thisServerOpenTime, serverOpenTime)) {
					competeServerInfo.addServer(server);
					mergeTimeInt = Math.min(mergeTimeInt, Integer.parseInt(mergeTime));
					intServerGroup.add(Integer.parseInt(server));
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (intServerGroup.size() < 2) {
			if (showTime <= HawkTime.getMillisecond()) {
				competeServerInfo.addServer(serverId);
				HawkLog.logPrintln("MergeCompetitionActivity serverGroup: {}, server not enough, showTime: {}, serverCount: {}, serverlist: {}", serverId, showTime, competeServerInfo.getServerCount(), competeServerInfo.getServerList());
			}
			return;
		}
		
		competeServerInfo.setMergeTime(mergeTimeInt);
		Collections.sort(intServerGroup);
		String serverGroup = SerializeHelper.collectionToString(intServerGroup, "_"); 
		rankHelper.setServerGroup(serverGroup);
		competeServerInfo.setServerGroup(serverGroup);
		HawkLog.logPrintln("MergeCompetitionActivity serverGroup: {}, mergeTime: {}, serverCount: {}, serverlist: {}", serverGroup, mergeTimeInt, competeServerInfo.getServerCount(), competeServerInfo.getServerList());
	}
	
	public long getShowTime() {
		long now = HawkTime.getMillisecond();
		int termId = this.getTimeControl().getActivityTermId(now);
		long showTime = this.getTimeControl().getShowTimeByTermId(termId);
		return showTime;
	}
	
	/**
	 * 获取服务器组信息
	 * @return
	 */
	public String getServerGroup() {
		if (competeServerInfo.isServerEmpty()) {
			initServerGroup();
		}
		return competeServerInfo.getServerGroup();
	}
	
	public String getGiftRewardKey() {
		return MergeCompetitionConst.GIFT_REWARD + getDataGeter().getServerId();
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
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(MergeCompetitionTargetCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.MERGE_COMPETE_ACHIEVE;
	}
	
	@Override
	public void onTakeRewardSuccessAfter(String playerId, List<Reward.RewardItem.Builder> reweardList, int achieveId) {
		//玩家领取各目标档位奖励
		AchieveConfig config = getAchieveCfg(achieveId);
		String guildId = this.getDataGeter().getGuildId(playerId);
		Map<String, Object> param = new HashMap<>();
        param.put("achieveId", achieveId);                              //任务id
        param.put("achieveType", config.getAchieveType().getValue());   //任务类型
        param.put("guildId", guildId == null ? "" : guildId);           //玩家所属联盟id
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_compete_target_reward, param);
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		if (isHidden(playerId)) {
			return Optional.empty();
		}
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		MergeCompetitionEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	/***
	 * 初始化成就
	 * @param playerId
	 * @return
	 */
	private void initAchieveItems(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		MergeCompetitionEntity entity = opEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		ConfigIterator<MergeCompetitionTargetCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MergeCompetitionTargetCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			MergeCompetitionTargetCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());				
			itemList.add(item);
		}
		entity.setItemList(itemList);
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			serverRewardCheck(playerId);
			return;
		}
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		MergeCompetitionEntity entity = opEntity.get();
		long now = HawkTime.getMillisecond();
		//从redis中获取全员礼包数据
		if(now - entity.getAwardIdRefreshTime() > HawkTime.MINUTE_MILLI_SECONDS) {
			refreshGiftReward(entity);
		}
		
		//联盟战力成就数据刷新
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (!HawkOSOperator.isEmptyString(guildId) && now - entity.getGuildPowerTargetTime() > HawkTime.MINUTE_MILLI_SECONDS) {
			refreshGuildPowerTargetData(entity, playerId, guildId);
		}
		
		syncActivityDataInfo(playerId);
	}
	
	/**
	 * 活动结束后检测区服排名奖励发放情况
	 * @param playerId
	 */
	private void serverRewardCheck(String playerId) {
		try {
			String serverRankInfoStr = getRedis().hGet(MergeCompetitionConst.SERVER_RANK_REWARD, getDataGeter().getServerId());
			if (HawkOSOperator.isEmptyString(serverRankInfoStr)) {
				return;
			}
			
			ServerRankInfo serverRankInfo = ServerRankInfo.parseObj(serverRankInfoStr);
			long logoutTime = this.getDataGeter().getPlayerLogoutTime(playerId);
			if (logoutTime >= serverRankInfo.getRewardTime()) {
				return;
			}
			
			String key = MergeCompetitionConst.SERVER_RANK_REWARD + ":" + playerId;
			if (!getRedis().setNx(key, "1")) {
				return;
			}
			
			HawkLog.logPrintln("MergeCompetitionActivity serverRewardCheck player: {}", playerId);
			String oppServerId = competeServerInfo.getOppServer();
			Optional<ServerInfo> serverOp = this.getDataGeter().getServerList().stream().filter(e -> e.getId().equals(oppServerId)).findAny();
			String oppServerName = serverOp.isPresent() ? serverOp.get().getName() : "";
			String contentInfo = "[" + oppServerId + "区]-" + oppServerName;
			
			MailId mailId = serverRankInfo.getRank() == 1 ? MailId.MERGE_COMPETE_RANK_SERVER_VICTORY : MailId.MERGE_COMPETE_RANK_SERVER_FAILED;
			List<Builder> rewardList = RewardHelper.toRewardItemImmutableList(serverRankInfo.getRewards());
			Object[] content = new Object[2];
			content[0] = contentInfo;
			content[1] = serverRankInfo.getScore();
			sendMailToPlayer(playerId, mailId, null, null, content, rewardList);
			HawkLog.logPrintln("MergeCompetitionActivity serverRewardCheck sendMail player: {}", playerId);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 刷新个人嘉奖礼包数据
	 * @param entity
	 */
	private void refreshGiftReward(MergeCompetitionEntity entity) {
		try {
			long now = HawkTime.getMillisecond();
			long latestTime = 0;
			Set<String> memberSet = getRedis().zRangeByScore(getGiftRewardKey(), entity.getAwardIdRefreshTime(), now, getRedisExpire());
			for (String member : memberSet) {
				GiftRewardInfo info = GiftRewardInfo.parseObj(member);
				entity.addAwardId(info.getRewardId(), info.getCount());
				latestTime = Math.max(latestTime, info.getTime());
			}
			
			if (latestTime > 0) {
				HawkLog.logPrintln("MergeCompetitionActivity refreshGiftReward player: {}, latestTime: {}", entity.getPlayerId(), latestTime);
				entity.setAwardIdRefreshTime(now);
				syncActivityDataInfoAdd(entity.getPlayerId(), entity);
			} 
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 更新联盟战力成就数据的时间
	 * @param playerId
	 */
	public void refreshGuildPowerTargetTime(String playerId) {
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		MergeCompetitionEntity entity = opEntity.get();
		entity.setGuildPowerTargetTime(HawkTime.getMillisecond());
	}
	
	/**
	 * 刷新数据
	 * @param event
	 */
	@Subscribe
	public void onEvent(MergeCompeteRefreshEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		MergeCompetitionEntity entity = opEntity.get();
		//记录新添加的嘉奖礼包
		if (event.isGiftReward()) {
			HawkLog.debugPrintln("MergeCompetitionActivity MergeCompeteRefreshEvent giftReward player: {}", playerId);
			refreshGiftReward(entity);
		}
		
		//刷新联盟战力目标成就数据 
		if (event.isGuildPower()) {
			String guildId = this.getDataGeter().getGuildId(playerId);
			HawkLog.debugPrintln("MergeCompetitionActivity MergeCompeteRefreshEvent guildPower player: {}, guildId: {}", playerId, guildId);
			refreshGuildPowerTargetData(entity, playerId, guildId);
		}
	}
	
	/**
	 * 推送礼包消耗金条
	 * @param event
	 */
	@Subscribe
	public void onEvent(CostDiamondBuyGiftEvent event) {
		costDiamondEvent(event.getPlayerId(), event.getPrice());
	}
	
	/**
	 * 购买礼包消耗金条
	 * @param event
	 */
	@Subscribe
	public void onEvent(GiftPurchasePriceEvent event) {
		costDiamondEvent(event.getPlayerId(), event.getPrice());
	}
	
	private void costDiamondEvent(String playerId, int costDiamonds) {
		if(!isOpening(playerId)){
			return;
		}
		MergeCompetitionGiftCfg selectConfig = null;
		ConfigIterator<MergeCompetitionGiftCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(MergeCompetitionGiftCfg.class);
		while (iterator.hasNext()) {
			MergeCompetitionGiftCfg cfg = iterator.next();
			if (cfg.getMin() <= costDiamonds && cfg.getMax() >= costDiamonds) {
				selectConfig = cfg;
				break;
			}
		}
		
		if (selectConfig == null) {
			return;
		}
		int rewardId = selectConfig.getReward();
		int rewardCount = selectConfig.getNum();
		long now = HawkTime.getMillisecond();
		//给全员提供礼包（给在线玩家抛事件、非在线玩家存redis，上线时从redis拉取）
		GiftRewardInfo info = GiftRewardInfo.valueOf(rewardId, rewardCount, now, playerId);
		getRedis().zAdd(getGiftRewardKey(), now, info.toString(), getRedisExpire());
		giftRewardLastChangeTime.set(now);
		
		int point = selectConfig.getPoint() * rewardCount;
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		MergeCompetitionEntity entity = opEntity.get();
		entity.addAwardId(rewardId, rewardCount);
		entity.setAwardIdRefreshTime(now);  //ontick中取time跟此处，不是同一个线程执行，存在风险（通过在entity中调用相关接口时加synchronized来规避）
		entity.setGiftScore(entity.getGiftScore() + point);
		
		updatePersonRank(playerId, ActivityRankType.MERGE_COMPETITION_GIFT_SCORE_RANK, entity.getGiftScore());
		
		HawkLog.logPrintln("MergeCompetitionActivity GiftPurchasePriceEvent player: {}, costDiamonds: {}, rewardId: {}, cfgId: {}, rewardCount: {}, giftScore: {} {}", 
				playerId, costDiamonds, rewardId, selectConfig.getId(), rewardCount, point, entity.getGiftScore());
		
		//玩家触发最高档位的嘉奖礼包时，需要在本服世界频道及跑马灯公告：[联盟简称]玩家名称财大气粗，赠送全服1个至尊嘉奖礼。
		if (selectConfig.getLevel() >= MergeCompetitionGiftCfg.getMaxLevel()) {
			String guildId = this.getDataGeter().getGuildId(playerId);
			String guildTag = null;
			if (!HawkOSOperator.isEmptyString(guildId)) {
				guildTag = this.getDataGeter().getGuildTag(guildId);
			}
			String playerName = this.getDataGeter().getPlayerName(playerId);
			sendBroadcast(Const.NoticeCfgId.MREGE_COMPETE_MAX_LEVEL_GIFT, null, guildTag, playerName, rewardCount);
		}
		
		syncActivityDataInfoAdd(playerId, entity);
	}
	
	/**
	 * 体力消耗通知
	 * @param event
	 */
	@Subscribe
	public void onEvent(VitCostEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		if (event.getCost() <= 0) {
			return;
		}
		
		//非集结消耗1点体力算1点，集结消耗1点算2点
		int costVit = event.getCost();
		if (event.isMass()) {
			costVit = costVit * 2;
		}
		
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		MergeCompetitionEntity entity = opEntity.get();
		entity.setCostVit(entity.getCostVit() + costVit);
		updatePersonRank(playerId, ActivityRankType.MERGE_COMPETITION_VITCOST_RANK, entity.getCostVit());
		syncActivityDataInfoAdd(playerId, entity);
		
		HawkLog.logPrintln("MergeCompetitionActivity VitCostEvent player: {}, mass: {}, costVit: {} {}", playerId, event.isMass(), costVit, entity.getCostVit());
	}
	
	/**
	 * 玩家战力变化通知
	 * @param event
	 */
	@Subscribe
	public void onEvent(BattlePointChangeEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		PowerData powerData = event.getPowerData();
		//计算去兵战力
		long power = Math.max(powerData.getBattlePoint() - powerData.getArmyBattlePoint() - powerData.getTrapBattlePoint(), 0);
		if (power <= 0) {
			return;
		}
		
		updatePersonRank(playerId, ActivityRankType.MERGE_COMPETITION_PERSON_POWER_RANK, power);
		
		String guildId = this.getDataGeter().getGuildId(playerId);
		
		HawkLog.logPrintln("MergeCompetitionActivity BattlePointChangeEvent player: {}, guildId: {}, power: {}", playerId, guildId, power);
		
		if (!HawkOSOperator.isEmptyString(guildId)) {
			//通知联盟的内别的玩家：（给在线玩家抛事件、非在线玩家存redis，上线时从redis拉取）
			AtomicLong atomicTime = guildPowerLastChangeTimeMap.get(guildId);
			if (atomicTime == null) {
				guildPowerLastChangeTimeMap.putIfAbsent(guildId, new AtomicLong(HawkTime.getMillisecond()));
			} else {
				atomicTime.set(HawkTime.getMillisecond());
			}
			
			updateGuildRank(guildId, power);
		}
	}
	
	/**
	 * 加入联盟
	 */
	@Subscribe
	public void onJoinGuild(JoinGuildEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}

		String guildId = getDataGeter().getGuildId(playerId);
		boolean ok = updateGuildRank(guildId, 1);
		HawkLog.logPrintln("MergeCompetitionActivity JoinGuildEvent player: {}, guildId: {}, updateGuildRank result: {}", playerId, guildId, ok);
		
		if (ok) {
			//刷新个人成就数据
			Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
			refreshGuildPowerTargetData(opEntity.get(), playerId, guildId);
		}
	}
	
	/**
	 * 更新联盟排行榜数据
	 * @param guildId
	 * @param power
	 */
	private boolean updateGuildRank(String guildId, long power) {
		if (HawkOSOperator.isEmptyString(guildId)) {
			return false;
		}
		MergeCompetitionRank guildIdRank = new MergeCompetitionRank(guildId, power);
		ActivityRankProvider<MergeCompetitionRank> guildRankProvider = ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_GUILD_POWER_RANK, MergeCompetitionRank.class);
		guildRankProvider.insertIntoRank(guildIdRank);
		updateRankGuildInfo(guildId);
		return true;
	}
	
	/**
	 * 更新个人排行榜数据
	 * @param playerId
	 * @param rankType
	 * @param rankVal
	 */
	private void updatePersonRank(String playerId, ActivityRankType rankType, long rankVal) {
		MergeCompetitionRank rank = new MergeCompetitionRank(playerId, rankVal); //全量数据
		ActivityRankProvider<MergeCompetitionRank> rankProvider = ActivityRankContext.getRankProvider(rankType, MergeCompetitionRank.class);
		rankProvider.insertIntoRank(rank);
		updateRankPlayerInfo(playerId);
	}
	
	/**
	 * 加入联盟后刷新联盟相关成就数据
	 * @param playerId
	 * @param guildId
	 */
	private void refreshGuildPowerTargetData(MergeCompetitionEntity entity, String playerId, String guildId) {
		try {
			List<AchieveItem> needPush = new ArrayList<>();
			long noarmypower = this.getDataGeter().getGuildNoArmyPower(guildId);
			for (AchieveItem achieveItem : entity.getItemList()) {
				MergeCompetitionTargetCfg achieveConfig = HawkConfigManager.getInstance().getConfigByKey(MergeCompetitionTargetCfg.class, achieveItem.getAchieveId());
				if (achieveConfig == null) {
					continue;
				}
				if (achieveConfig.getAchieveType() != AchieveType.MERGE_COMPETE_GUILD_NOARMY_POWER) {
					continue;
				}
				if (achieveItem.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				
				needPush.add(achieveItem);
				int configValue = achieveConfig.getConditionValue(0);
				if (noarmypower < configValue) {
					achieveItem.setValue(0, (int)noarmypower);
					continue;
				}
				achieveItem.setValue(0, configValue);
				achieveItem.setState(AchieveState.NOT_REWARD_VALUE);
				HawkLog.logPrintln("achieve finish. playerId: {}, achieveId: {}", playerId, achieveConfig.getAchieveId());
			}
			
			entity.setGuildPowerTargetTime(HawkTime.getMillisecond());
			HawkLog.logPrintln("MergeCompetitionActivity refreshGuildPowerTarget player: {}, needPush empty: {}", entity.getPlayerId(), needPush.isEmpty());
			if (!needPush.isEmpty()) {
				AchievePushHelper.pushAchieveUpdate(playerId, needPush);
			} else {
				entity.setGuildPowerTargetFinish(1); //联盟去兵战力目标已全部达成
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 联盟退出
	 */
	@Subscribe
	public void onGuildQuit(GuildQuiteEvent event){
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		updateGuildRank(event.getGuildId(), 1);
		HawkLog.logPrintln("MergeCompetitionActivity GuildQuiteEvent player: {}, guildId: {}", playerId, event.getGuildId());
	}
	
	/**
	 * 联盟解散
	 */
	@Subscribe
	public void onGuildDismiss(GuildDismissEvent event) {
		if(!isOpening(event.getPlayerId())){
			return;
		}

		updateGuildRank(event.getGuildId(), 0);
		HawkLog.logPrintln("MergeCompetitionActivity GuildDismissEvent player: {}, guildId: {}", event.getPlayerId(), event.getGuildId());
	}
	
	/**
	 * 玩家数据更新
	 * @param playerId
	 * @param rankProvider
	 */
	public void updateRankPlayerInfo(String playerId) {
		RankPlayerInfo playerInfo = new RankPlayerInfo();
		playerInfo.setPlayerId(playerId);
		playerInfo.setPlayerName(getDataGeter().getPlayerName(playerId));
		playerInfo.setGuildTag(getDataGeter().getGuildTagByPlayerId(playerId));
		playerInfo.setIcon(getDataGeter().getIcon(playerId));
		playerInfo.setPfIcon(getDataGeter().getPfIcon(playerId));
		playerInfo.setServerId(getDataGeter().getServerId());
		rankHelper.updatePlayerInfo(playerId, playerInfo);
	}
	
	/**
	 * 联盟数据更新
	 * 排行榜展示数据有：服务器名称/联盟简称/联盟全称/盟主角色名/联盟去兵战力值/当前排名对应的服务器积分
	 * @param guildId
	 * @param rankProvider
	 */
	public void updateRankGuildInfo(String guildId) {
		RankGuildInfo guildInfo = new RankGuildInfo();
		String leaderId = getDataGeter().getGuildLeaderId(guildId);
		guildInfo.setGuildId(guildId);
		guildInfo.setGuildName(getDataGeter().getGuildName(guildId));
		guildInfo.setGuildTag(getDataGeter().getGuildTag(guildId));
		guildInfo.setLeaderId(leaderId);
		guildInfo.setLeaderName(getDataGeter().getGuildLeaderName(guildId));
		guildInfo.setServerId(getDataGeter().getServerId());
		
		guildInfo.setIcon(getDataGeter().getIcon(leaderId));
		guildInfo.setPfIcon(getDataGeter().getPfIcon(leaderId));
		rankHelper.updateGuildInfo(guildId, guildInfo);
	}
	
	/**
	 * 请求领取嘉奖礼包
	 */
	public void recieveGiftAward(String playerId, int awardId, int protocol) {
		if(isHidden(playerId)){
			return;
		}
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}

		MergeCompetitionEntity entity = opEntity.get();
		int count = entity.getAwardMap().getOrDefault(awardId, 0);
		if (count <= 0) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, protocol, Status.Error.MERGE_COMPETE_GIFT_AWARD_ERR_VALUE);
			syncActivityDataInfoAdd(playerId, entity);
			return;
		}
		
		this.getDataGeter().takeReward(playerId, awardId, count, Action.MERGE_COMPETE_GIFT_AWARD, RewardOrginType.ACTIVITY_REWARD, true);
		entity.getAwardMap().remove(awardId);
		entity.notifyUpdate();
		syncActivityDataInfoAdd(playerId, entity);
		HawkLog.logPrintln("MergeCompetitionActivity recieveGiftAward player: {}, awardId: {}", playerId, awardId, count);
		
		//玩家领取全军嘉奖礼物
		String guildId = this.getDataGeter().getGuildId(playerId);
		Map<String, Object> param = new HashMap<>();
        param.put("awardLevel", MergeCompetitionGiftCfg.getLevelByRewardId(awardId));  //嘉奖礼物档位
        param.put("awardCount", count);                                                //领取礼物的数量
        param.put("guildId", guildId == null ? "" : guildId);                          //玩家所属联盟id
        getDataGeter().logActivityCommon(playerId, LogInfoType.merge_compete_gift_reward, param);
	}

	/**
	 * 同步活动数据
	 */
	@Override
	public void syncActivityDataInfo(String playerId) {
		if(isHidden(playerId)){
			return;
		}
		
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MergeCompetitionEntity entity = opEntity.get();
		
		MergeCompeteInfoPB.Builder builder = MergeCompeteInfoPB.newBuilder();
		for (MergeCompeteRankType rankType : MergeCompeteRankType.values()) {
			builder.addRank(buildActivityRank(playerId, rankType.getNumber(), entity));
		}
		
		List<MergeCompetitionRank> rankList = rankHelper.getServerScore();
		List<String> serverList = new ArrayList<>();
		for (MergeCompetitionRank rank : rankList) {
			serverList.add(rank.getId());
			MergeCompeteServerScore.Builder serverScoreBuilder = MergeCompeteServerScore.newBuilder();
			serverScoreBuilder.setServerId(rank.getId());
			serverScoreBuilder.setScore(rank.getScore());
			builder.addServerScore(serverScoreBuilder);
		}
		
		for (String serverId : competeServerInfo.getServerList()) {
			if (serverList.contains(serverId)) {
				continue;
			}
			MergeCompeteServerScore.Builder serverScoreBuilder = MergeCompeteServerScore.newBuilder();
			serverScoreBuilder.setServerId(serverId);
			serverScoreBuilder.setScore(0);
			builder.addServerScore(serverScoreBuilder);
		}
		
		builder.setSyncType(0);
		builder.setCostVit(entity.getCostVit());
		builder.setGiftScore(entity.getGiftScore());
		builder.setMergeTime(competeServerInfo.getMergeTime());
		
		for (Entry<Integer, Integer> entry : entity.getAwardMap().entrySet()) {
			MCAwardInfo.Builder awardBuilder = MCAwardInfo.newBuilder();
			awardBuilder.setAwardId(entry.getKey());
			awardBuilder.setCount(entry.getValue());
			awardBuilder.setLevel(MergeCompetitionGiftCfg.getLevelByRewardId(entry.getKey()));
			builder.addAwardInfo(awardBuilder);
		}
		
		pushToPlayer(playerId, HP.code2.MERGE_COMPETE_ACTIVITY_INFO_S_VALUE, builder);
	}
	
	/**
	 * 增量同步活动数据
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityDataInfoAdd(String playerId, MergeCompetitionEntity entity) {
		MergeCompeteInfoPB.Builder builder = MergeCompeteInfoPB.newBuilder();
		builder.setSyncType(1);
		builder.setCostVit(entity.getCostVit());
		builder.setGiftScore(entity.getGiftScore());
		builder.setMergeTime(competeServerInfo.getMergeTime());
		for (Entry<Integer, Integer> entry : entity.getAwardMap().entrySet()) {
			MCAwardInfo.Builder awardBuilder = MCAwardInfo.newBuilder();
			awardBuilder.setAwardId(entry.getKey());
			awardBuilder.setCount(entry.getValue());
			awardBuilder.setLevel(MergeCompetitionGiftCfg.getLevelByRewardId(entry.getKey()));
			builder.addAwardInfo(awardBuilder);
		}
		
		pushToPlayer(playerId, HP.code2.MERGE_COMPETE_ACTIVITY_INFO_S_VALUE, builder);
	}
	
	/**
	 * 同步榜单数据
	 * @param playerId
	 * @param rankType
	 */
	public void syncActivityRankInfo(String playerId, int rankType, int protocol) {
		if(isHidden(playerId)){
			return;
		}
		MergeCompeteRankType type = MergeCompeteRankType.valueOf(rankType);
		if (type == null) {
			return;
		}
		
		Optional<MergeCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		MergeCompetitionEntity entity = opEntity.get();
		
		MergeCompeteRank.Builder builder = buildActivityRank(playerId, rankType, entity);
		pushToPlayer(playerId, HP.code2.MERGE_COMPETE_SINGLE_RANK_S_VALUE, builder);
	}
	
	/**
	 * 榜单数据构建
	 * @param playerId
	 * @param rankType
	 * @return
	 */
	private MergeCompeteRank.Builder buildActivityRank(String playerId, int rankType, MergeCompetitionEntity entity) {
		MergeCompeteRankType type = MergeCompeteRankType.valueOf(rankType);
		MergeCompetitionRankProvider rankProvider = null;
		MergeCompeteRank.Builder builder = MergeCompeteRank.newBuilder();
		builder.setRankType(rankType);
		long selfScore = 0;
		if (type == MergeCompeteRankType.RANKTYPE_PERSON_POWER) {
			rankProvider = (PersonalPowerRankProvider)ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_PERSON_POWER_RANK, MergeCompetitionRank.class);
			selfScore = this.getDataGeter().getPlayerNoArmyPower(playerId);
		} else if (type == MergeCompeteRankType.RANKTYPE_VITCOST_POWER) {
			rankProvider = (VitCostRankProvider)ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_VITCOST_RANK, MergeCompetitionRank.class);
			selfScore = entity.getCostVit();
		} else if (type == MergeCompeteRankType.RANKTYPE_GIFT_SCORE) {
			rankProvider = (GiftScoreRankProvider)ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_GIFT_SCORE_RANK, MergeCompetitionRank.class);
			selfScore = entity.getGiftScore();
		} else if (type == MergeCompeteRankType.RANKTYPE_GUILD_POWER) {
			rankProvider = (GuildPowerRankProvider)ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_GUILD_POWER_RANK, MergeCompetitionRank.class);
			String guildId = this.getDataGeter().getGuildId(playerId);
			selfScore = this.getDataGeter().getGuildNoArmyPower(guildId);
		}

		if (rankProvider != null) {
			rankProvider.buildActivityRank(playerId, builder, selfScore);
		}
		return builder;
	} 
	
	@Override
	public void onHidden() {
	}
	
	@Override
	public void onEnd() {
		//活动结束时，根据排行榜，以邮件的形式， 为玩家发放个人去兵战力排行榜奖励
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				HawkLog.logPrintln("MergeCompetitionActivity onEnd sendMail -> playerPower");
				sendRankReward(ActivityRankType.MERGE_COMPETITION_PERSON_POWER_RANK, MailId.MERGE_COMPETE_RANK_AWARD_POWER, MergeCompetitionConst.RANK_TYPE_PERSON_POWER);
				return null;
			}
		});
		
		//动结束时，根据排行榜，以邮件的形式， 为玩家发放个人体力消耗排行榜奖励
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				HawkLog.logPrintln("MergeCompetitionActivity onEnd sendMail -> vitCost");
				sendRankReward(ActivityRankType.MERGE_COMPETITION_VITCOST_RANK, MailId.MERGE_COMPETE_RANK_AWARD_VITCOST, MergeCompetitionConst.RANK_TYPE_VIT_COST);
				return null;
			}
		});
		
		//活动结束时，根据排行榜，以邮件的形式， 为玩家发放嘉奖积分排行榜奖励
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				HawkLog.logPrintln("MergeCompetitionActivity onEnd sendMail -> giftScore");
				sendRankReward(ActivityRankType.MERGE_COMPETITION_GIFT_SCORE_RANK, MailId.MERGE_COMPETE_RANK_AWARD_GIFT, MergeCompetitionConst.RANK_TYPE_GIFT_SCORE);
				return null;
			}
		});
		
		//活动结束时，根据排行榜，以邮件的形式， 为玩家发放联盟去兵战力排行榜奖励
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				HawkLog.logPrintln("MergeCompetitionActivity onEnd sendMail -> guildPower");
				sendGuildRankReward();
				return null;
			}
		});
		
		//活动结束时，根据区服积分，通过邮件为区服玩家发放比拼胜利、比拼失败奖励。
		HawkTaskManager.getInstance().postExtraTask(new HawkTask(){
			@Override
			public Object run() {
				HawkLog.logPrintln("MergeCompetitionActivity onEnd sendMail -> serverRank");
				sendServerRankReward();
				return null;
			}
		});
	}
	
	/**
	 * 发送个人排名奖励邮件
	 */
	private void sendRankReward(ActivityRankType rankType, MailId mailId, int rankCfgType) {
		ActivityRankProvider<MergeCompetitionRank> rankProvider = ActivityRankContext.getRankProvider(rankType, MergeCompetitionRank.class);
		rankProvider.loadRank();
		List<MergeCompetitionRankCfg> rankCfgList = MergeCompetitionRankCfg.getConfigByType(rankCfgType);
		for (MergeCompetitionRankCfg rankCfg : rankCfgList) {
			try {
				int highRank = rankCfg.getRankUpper();
				int lowRank = rankCfg.getRankLower();
				List<Builder> rewardList = RewardHelper.toRewardItemImmutableList(rankCfg.getRewards());
				List<MergeCompetitionRank> rankPlayers = rankProvider.getRanks(highRank, lowRank);
				for (MergeCompetitionRank rankData : rankPlayers) {
					String playerId = rankData.getId();
					//邮件发送奖励 -- 判断是否是本服玩家，保证不会重发
					if (this.getDataGeter().checkPlayerExist(playerId)) {
						Object[] content = new Object[2];
						content[0] = rankData.getScore(); //排名积分
						content[1] = rankData.getRank(); //名次
						Object[] subTitle = new Object[1];
						subTitle[0] = rankData.getRank(); //名次
						sendMailToPlayer(rankData.getId(), mailId, null, subTitle, content, rewardList);
						HawkLog.logPrintln("MergeCompetitionActivity send rank award, mailId: {}, playerId: {}, rank: {}, score: {}, this server: {}", mailId, playerId, rankData.getRank(), rankData.getScore(), true);
					} else {
						HawkLog.logPrintln("MergeCompetitionActivity send rank award, mailId: {}, playerId: {}, rank: {}, score: {}, serverId: {}", mailId, playerId, rankData.getRank(), rankData.getScore(), false);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 发送联盟排名奖励邮件
	 * @param mailId
	 * @param rankCfgList
	 * @param rankProvider
	 */
	private void sendGuildRankReward() {
		ActivityRankProvider<MergeCompetitionRank> rankProvider = ActivityRankContext.getRankProvider(ActivityRankType.MERGE_COMPETITION_GUILD_POWER_RANK, MergeCompetitionRank.class);
		rankProvider.loadRank();
		List<MergeCompetitionRankCfg> rankCfgList = MergeCompetitionRankCfg.getConfigByType(MergeCompetitionConst.RANK_TYPE_GUILD_POWER);
		for (MergeCompetitionRankCfg rankCfg : rankCfgList) {
			try {
				int highRank = rankCfg.getRankUpper();
				int lowRank = rankCfg.getRankLower();
				List<Builder> rewardList = RewardHelper.toRewardItemImmutableList(rankCfg.getRewards());
				List<MergeCompetitionRank> rankPlayers = rankProvider.getRanks(highRank, lowRank);
				for (MergeCompetitionRank rankData : rankPlayers) {
					String guildId = rankData.getId();
					// -- 判断是否是本服联盟，保证不会重发
					if (!this.getDataGeter().isGuildLocalExist(guildId)) {
						HawkLog.logPrintln("MergeCompetitionActivity send rank award, guildId: {}, rank: {}, score: {}, this server: {}", guildId, rankData.getRank(), rankData.getScore(), false);
						continue;
					}
					HawkLog.logPrintln("MergeCompetitionActivity send rank award, guildId: {}, rank: {}, score: {}, this server: {}", guildId, rankData.getRank(), rankData.getScore(), true);
					for (String playerId : this.getDataGeter().getGuildMemberIds(guildId)) {
						Object[] content = new Object[2];
						content[0] = rankData.getScore(); //联盟去兵战力值
						content[1] = rankData.getRank(); //名次
						Object[] subTitle = new Object[1];
						subTitle[0] = rankData.getRank(); //名次
						sendMailToPlayer(playerId, MailId.MERGE_COMPETE_RANK_AWARD_GUILD, null, subTitle, content, rewardList);
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	/**
	 * 发放区服排名奖励
	 */
	private void sendServerRankReward() {
		String serverId = this.getDataGeter().getServerId();
		String oppServerId = competeServerInfo.getOppServer();
		Optional<ServerInfo> serverOp = this.getDataGeter().getServerList().stream().filter(e -> e.getId().equals(oppServerId)).findAny();
		String oppServerName = serverOp.isPresent() ? serverOp.get().getName() : "";
		String contentInfo = "[" + oppServerId + "区]-" + oppServerName;
		
		List<MergeCompetitionRank> rankList = rankHelper.getServerScore();
		List<MergeCompetitionRankCfg> rankCfgList = MergeCompetitionRankCfg.getConfigByType(MergeCompetitionConst.RANK_TYPE_SERVER_SCORE);
		for (MergeCompetitionRankCfg rankCfg : rankCfgList) {
			try {
				int highRank = rankCfg.getRankUpper();
				int lowRank = rankCfg.getRankLower();
				List<Builder> rewardList = RewardHelper.toRewardItemImmutableList(rankCfg.getRewards());
				List<MergeCompetitionRank> rankServers = getRanks(rankList, highRank, lowRank);
				for (MergeCompetitionRank rankData : rankServers) {
					//判断是否是本服，保证不会重发 （此处，rankData的id就是区服ID）
					if (!this.getDataGeter().isLocalServer(rankData.getId())) {
						HawkLog.logPrintln("MergeCompetitionActivity send rank award, serverId: {}, rank: {}, score: {}, this server: {}", rankData.getId(), rankData.getRank(), rankData.getScore(), false);
						continue;
					}
					
					HawkLog.logPrintln("MergeCompetitionActivity send rank award, serverId: {}, rank: {}, score: {}, this server: {}", rankData.getId(), rankData.getRank(), rankData.getScore(), true);
					//在线玩家当场发，离线玩家登录时发
					ServerRankInfo info = ServerRankInfo.valueOf(rankData.getRank(), rankCfg.getRewards(), HawkTime.getMillisecond(), rankData.getScore());
					getRedis().hSet(MergeCompetitionConst.SERVER_RANK_REWARD, serverId, info.toString());
					
					//邮件正文：尊敬的指挥官，您的区服在与{0}的合服比拼活动中，最终区服积分为{1}，赢得了本次合服比拼的胜; 参数说明：{0}-目标区服的区服编号+名称，如“[21区]-所向披靡”，{1}-自己服的区服积分
					//邮件正文：尊敬的指挥官，您的区服在与{0}的合服比拼活动中，最终区服积分为{1}，在本次合服比拼中落败; 参数说明：{0}-目标区服的区服编号+名称，如“[21区]-所向披靡”，{1}-自己服的区服积分
					
					MailId mailId = rankData.getRank() == 1 ? MailId.MERGE_COMPETE_RANK_SERVER_VICTORY : MailId.MERGE_COMPETE_RANK_SERVER_FAILED;
					Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
					for(String playerId : onlinePlayers){
						Object[] content = new Object[2];
						content[0] = contentInfo;
						content[1] = rankData.getScore();
						sendMailToPlayer(playerId, mailId, null, null, content, rewardList);
						String key = MergeCompetitionConst.SERVER_RANK_REWARD + ":" + playerId;
						getRedis().setNx(key, "1");
					}
				}
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	private List<MergeCompetitionRank> getRanks(List<MergeCompetitionRank> rankList, int start, int end) {
		if (start > rankList.size()) {
			return Collections.emptyList();
		}
		start = start > 0 ? start - 1 : start;
		end = end > rankList.size() ? rankList.size() : end;
		return rankList.subList(start, end);
	}
	
	private HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	public int getRedisExpire() {
		return 3600 * 24 * 30;
	}

	public MergeCompeteRankHelper getRankHelper() {
		return rankHelper;
	}

}
