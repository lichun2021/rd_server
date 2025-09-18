package com.hawk.activity.type.impl.aftercompetition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.result.Result;

import com.alibaba.fastjson.JSONObject;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AfterCompetitionGiftRecEvent;
import com.hawk.activity.event.impl.AfterCompetitionHomageRefreshEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionAchieveCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionConstCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionShopCfg;
import com.hawk.activity.type.impl.aftercompetition.cfg.AfterCompetitionUnionCfg;
import com.hawk.activity.type.impl.aftercompetition.data.ChampionGuildInfo;
import com.hawk.activity.type.impl.aftercompetition.data.GiftBigAwardInfo;
import com.hawk.activity.type.impl.aftercompetition.data.RecGiftInfo;
import com.hawk.activity.type.impl.aftercompetition.data.SendGiftInfo;
import com.hawk.activity.type.impl.aftercompetition.entity.ACGiftItem;
import com.hawk.activity.type.impl.aftercompetition.entity.AfterCompetitionEntity;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.AfterCompetitionActivityInfo;
import com.hawk.game.protocol.Activity.BigAwardPermissionPB;
import com.hawk.game.protocol.Activity.ChampionGuildPB;
import com.hawk.game.protocol.Activity.GiftRecAmountPB;
import com.hawk.game.protocol.Activity.GiftRecAmountResp;
import com.hawk.game.protocol.Activity.GiftRecRecordSyncPB;
import com.hawk.game.protocol.Activity.GiftSendRecordSyncPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

/**
 * 赛后庆典371
 * 
 * @author lating
 */
public class AfterCompetitionActivity extends ActivityBase implements AchieveProvider {
	/**
	 * 致敬次数
	 */
	private volatile AtomicInteger homageCountGlobal = new AtomicInteger(0);
	private volatile AtomicInteger homageCountLocal = new AtomicInteger(0);
	private int lastHomageCountLocal = -1;
	
	/**
	 * 冠军联盟信息刷新tick时间
	 */
	private long topGuildInfoTick;
	/**
	 * 联盟信息
	 */
	private Map<Integer, ChampionGuildInfo> guildInfoMap = new ConcurrentHashMap<>();
	/**
	 * 注水逻辑
	 */
	AfterCompetitionAutoLogic autoLogic;
	/**
	 * 礼物管理
	 */
	AfterCompetitionGiftManager giftManager;
	
	
	public AfterCompetitionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
		autoLogic = new AfterCompetitionAutoLogic(this);
		giftManager = new AfterCompetitionGiftManager(this);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.AFTER_COMPETITION;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AfterCompetitionActivity activity = new AfterCompetitionActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AfterCompetitionEntity entity = new AfterCompetitionEntity(playerId, termId);
		return entity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AfterCompetitionEntity> queryList = HawkDBManager.getInstance()
				.query("from AfterCompetitionEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.AFTER_COMPETITION_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AfterCompetitionEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		AfterCompetitionEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId, entity);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	/**
	 * 初始化成就
	 * @param playerId
	 */
	private void initAchieveItems(String playerId, AfterCompetitionEntity entity){
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		HawkLog.logPrintln("AfterCompetitionActivity initAchieveItems, playerId: {}", playerId);
		ConfigIterator<AfterCompetitionAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AfterCompetitionAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			AfterCompetitionAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		refreshAchieveItems(entity, homageCountGlobal.get(), false);
		entity.notifyUpdate();
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionAchieveCfg.class, achieveId);
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.AFTER_COMPETITION_ACHIEVE;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
	}
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AfterCompetitionEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveItems(playerId, entity);
		} else {
			refreshAchieveItems(entity, homageCountGlobal.get(), true);
		}
		
		giftManager.refreshPlayerGiftInfo(playerId, entity);
	}
	
	
	@Override
	public void shutdown() {
		if (lastHomageCountLocal >= 0 && homageCountLocal.get() > lastHomageCountLocal) {
			int add = homageCountLocal.get() - lastHomageCountLocal;
			getRedis().increaseBy(getGlobalHomageKey(), add, getRedisExpire());
		}
	}
	
	public void onTick() {
		long currTime = HawkApp.getInstance().getCurrentTime();
		long endTime = this.getEndTime();
		if (currTime > endTime) {
			lastHomageCountLocal = -1;
			return;
		}
		
		//刚起服，初始化数据
		if (lastHomageCountLocal < 0) {
			lastHomageCountLocal = 0;
			autoLogic.setServerStartTime(currTime);
			homageCountGlobal.set(getGlobalHomageTimes());
			giftManager.initGiftInfo();
			return;
		}
		
		//刷新冠军联盟信息
		topGuildInfoRefresh();
		//将本服新增的致敬次数累加到全服累计计数上面
		if (homageCountLocal.get() > lastHomageCountLocal) {
			int newCountServer = homageCountLocal.get();
			int add = newCountServer - lastHomageCountLocal;
			getRedis().increaseBy(getGlobalHomageKey(), add, getRedisExpire());
			lastHomageCountLocal = newCountServer;
		}
		
		//注水
		autoLogic.autoAddHomage();
		int homageValGlobal = getGlobalHomageTimes();
		if (homageValGlobal > homageCountGlobal.get()) {
			homageCountGlobal.set(homageValGlobal);
			giftManager.newGiftUnlockCheck(homageValGlobal);
		}
		
		giftManager.giftInfoUpdateCheck();
		//活动结束前X小时，系统自动将所有未发送的冠军大赏发放（分别以手Q冠军联盟盟主、微信冠军联盟盟主的名义）
		giftManager.systemSendBigRewardCheck();
	}
	
	/**
	 * 获取最新的全服致敬次数
	 * @return
	 */
	private int getGlobalHomageTimes() {
		String homageStr = getRedis().getString(getGlobalHomageKey());
		return HawkOSOperator.isEmptyString(homageStr) ? 0 : Integer.parseInt(homageStr);
	}
	
	/**
	 * 联盟信息刷新
	 */
	private void topGuildInfoRefresh() {
		long currTime = HawkApp.getInstance().getCurrentTime();
		if (topGuildInfoTick > 0 && currTime - topGuildInfoTick < 300000L) {
			return;
		}
		
		topGuildInfoTick = currTime;
		
		try {
			String serverId = this.getDataGeter().getServerId();
			for (int channel : Arrays.asList(AfterCompetitionConst.CHANNEL_QQ, AfterCompetitionConst.CHANNEL_WX)) {
				for (int rank = 1; rank <= 10; rank++) {
					AfterCompetitionUnionCfg cfg = AfterCompetitionUnionCfg.getUnion(channel, AfterCompetitionConst.RACE_TYPE_3, rank);
					if (cfg == null || !this.getDataGeter().isGuildExist(cfg.getWinnerUnion())) {
						continue;
					}
					String guildId = cfg.getWinnerUnion();
					String leaderId = this.getDataGeter().getGuildLeaderId(guildId);
					if (leaderId == null) {
						continue;
					}
					String leaderName = this.getDataGeter().getGuildLeaderName(guildId);
					String guildName = this.getDataGeter().getGuildName(guildId);
					int icon = this.getDataGeter().getIcon(leaderId);
					String pfIcon = "";
					if (this.getDataGeter().checkPlayerExist(leaderId)) {
						pfIcon = this.getDataGeter().getPfIcon(leaderId);
					} else {
						HawkLog.errPrintln("AfterCompetitionActivity topGuildInfoRefresh player not exist: {}", leaderId);
					}
					ChampionGuildInfo guildInfo = new ChampionGuildInfo(guildId, guildName, leaderId, leaderName, icon, pfIcon, serverId, rank);
					getRedis().hSet(getGuildInfoKey(channel), String.valueOf(rank), JSONObject.toJSONString(guildInfo), 600); //10分钟过期
				}
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		Map<Integer, ChampionGuildInfo> guildInfoMapTmp = new ConcurrentHashMap<>();
		for (int channel : Arrays.asList(AfterCompetitionConst.CHANNEL_QQ, AfterCompetitionConst.CHANNEL_WX)) {
			Map<String, String> map = getRedis().hGetAll(getGuildInfoKey(channel));
			for (Entry<String, String> entry : map.entrySet()) {
				int rank = Integer.parseInt(entry.getKey());
				ChampionGuildInfo info = JSONObject.parseObject(entry.getValue(), ChampionGuildInfo.class);
				guildInfoMapTmp.put(channel*100 + rank, info);
			}
		}
		guildInfoMap = guildInfoMapTmp;
	}
	
	/**
	 * 在线玩家接收到礼物事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(AfterCompetitionGiftRecEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<AfterCompetitionEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		
		AfterCompetitionEntity entity = optional.get();
		int giftId = event.getGiftId();
		RecGiftInfo recRecord = new RecGiftInfo(event.getFromPlayerId(), event.getFromPlayerName(), giftId, event.getSendTime(), event.getItemInfo());
		entity.addRecGiftRecord(recRecord);
		ACGiftItem giftItem = entity.getGiftInfo(giftId);
		if (giftItem == null) {
			giftItem = entity.addGiftItem(giftId);
		}
		
		giftItem.addSelfRecCount(1);
		entity.notifyUpdate();
	}
	
	/**
	 * 全服致敬次数目标成就奖励
	 * @param event
	 */
	@Subscribe
	public void onEvent(AfterCompetitionHomageRefreshEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<AfterCompetitionEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		AfterCompetitionEntity entity = optional.get();
		refreshAchieveItems(entity, event.getHomageVal(), true);
		
		//同步最新全服致敬次数，以及新解锁的礼包
		syncActivityInfo(playerId, entity);
	}
	
	/**
	 * 刷新成就数据
	 * @param playerId
	 * @param homageVal
	 */
	private void refreshAchieveItems(AfterCompetitionEntity entity, int homageVal, boolean pushUpdate) {
		try {
			List<AchieveItem> needPush = new ArrayList<>();
			for (AchieveItem achieveItem : entity.getItemList()) {
				AfterCompetitionAchieveCfg achieveConfig = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionAchieveCfg.class, achieveItem.getAchieveId());
				if (achieveConfig == null) {
					continue;
				}
				if (achieveItem.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				
				needPush.add(achieveItem);
				int configValue = achieveConfig.getConditionValue(0);
				if (homageVal < configValue) {
					achieveItem.setValue(0, homageVal);
					continue;
				}
				achieveItem.setValue(0, configValue);
				achieveItem.setState(AchieveState.NOT_REWARD_VALUE);
				HawkLog.logPrintln("AfterCompetitionActivity achieve finish, playerId: {}, achieveId: {}", entity.getPlayerId(), achieveConfig.getAchieveId());
			}
			
			if (!needPush.isEmpty() && pushUpdate) {
				entity.notifyUpdate();
				AchievePushHelper.pushAchieveUpdate(entity.getPlayerId(), needPush);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	
	/**
	 * 致敬（玩家可通过致敬冠军联盟获得奖励，每人每日1次）
	 * @param playerId
	 */
	public int homage(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}

		AfterCompetitionEntity entity = opEntity.get();
		if (HawkTime.isToday(entity.getHomageTime())) {
			return Status.Error.AFTER_COMPE_HOMAGE_REPEATED_VALUE;
		}
		
		//计数
		entity.setHomageTime(HawkTime.getMillisecond());
		homageCountLocal.addAndGet(1);
		
		//发奖
		List<RewardItem.Builder> rewardItems = RewardHelper.toRewardItemImmutableList(AfterCompetitionConstCfg.getInstance().getRandHomageRewardStr());
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.AFTER_COMPETITION_HOMAGE_REWARD, true, RewardOrginType.ACTIVITY_REWARD);
		
		//同步最新全服致敬次数，以及新解锁的礼包
		syncActivityInfo(playerId, entity, 1);
		return 0;
	}
	
	/**
	 * 检查是否可以购买礼包
	 * 
	 * @param playerId
	 * @param payGiftId
	 * @return
	 */
	public int canPayRMBGift(String playerId, String payGiftId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		//活动结束前X小时不能进行赠礼
		if (this.getEndTime() - HawkTime.getMillisecond() <= AfterCompetitionConstCfg.getInstance().getCompetitionGiveTimeLimit()) {
			return Status.Error.AFTER_COMPE_SENGGIFT_TIME_END_VALUE;
		}
		
		int giftId = AfterCompetitionShopCfg.getGiftId(payGiftId);
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		if (homageCountGlobal.get() < giftCfg.getHomageValue()) {
			return Status.Error.AFTER_COMPE_GIFT_LOCK_ERR_VALUE;
		}
		
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		AfterCompetitionEntity entity = opEntity.get();
		ACGiftItem giftInfo = entity.getGiftInfo(giftId);
		if (giftInfo == null) {
			giftInfo = entity.addGiftItem(giftId);
		}
		
		if (HawkOSOperator.isEmptyString(giftInfo.getDefaultSendPlayer())) {
			HawkLog.logPrintln("AfterCompetitionActivity payRMB check, playerId: {}, payGiftId: {}, giftId: {}, sendTo: {}", playerId, payGiftId, giftId, giftInfo.getDefaultSendPlayer());
		}
		
		if (giftInfo.getSelfSendCount() >= giftCfg.getBuyTimesLimit()) {
			return Status.Error.AFTER_COMPE_GIFT_BUY_UPLIMIT_VALUE;
		}
		
		return 0;
	}
	
	/**
	 * 直购礼包赠送
	 * @param event
	 */
	@Subscribe
	public void onRMBGiftBuy(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String payGiftId = event.getGiftId();
		int giftId = AfterCompetitionShopCfg.getGiftId(payGiftId);
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if(giftCfg == null){
			return;
		}
		
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<AfterCompetitionEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		AfterCompetitionEntity entity = optional.get();
		String targetPlayerId = entity.getGiftInfo(giftId).getDefaultSendPlayer();
		buyGiftSucc(entity, giftCfg, targetPlayerId);
	}
	
	/**
	 * 买礼物（致敬数量达到指定目标解锁赠礼商店 - 买礼物送人，同时自己也获得）
	 * @param playerId
	 */
	public int buyGift(String playerId, int giftId, String targetPlayerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		//活动结束前X小时不能进行赠礼
		if (this.getEndTime() - HawkTime.getMillisecond() <= AfterCompetitionConstCfg.getInstance().getCompetitionGiveTimeLimit()) {
			return Status.Error.AFTER_COMPE_SENGGIFT_TIME_END_VALUE;
		}
		
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (giftCfg.isPayRMB()) {
			return Status.Error.AFTER_COMPE_GIFT_PAY_RMB_VALUE;
		}
		if (homageCountGlobal.get() < giftCfg.getHomageValue()) {
			return Status.Error.AFTER_COMPE_GIFT_LOCK_ERR_VALUE;
		}
		
		Optional<AfterCompetitionEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		AfterCompetitionEntity entity = optional.get();
		ACGiftItem giftInfo = entity.getGiftInfo(giftId);
		if (giftInfo == null) {
			giftInfo = entity.addGiftItem(giftId);
		}
		
		if (giftInfo.getSelfSendCount() >= giftCfg.getBuyTimesLimit()) {
			return Status.Error.AFTER_COMPE_GIFT_BUY_UPLIMIT_VALUE;
		}
		if (HawkOSOperator.isEmptyString(targetPlayerId)) {
			targetPlayerId = giftInfo.getDefaultSendPlayer();
		}
		if(!this.getDataGeter().checkPlayerExist(targetPlayerId)) {
			return Status.Error.AFTER_COMPE_RECIEVER_SERVER_ERR_VALUE;
		}
		
		List<RewardItem.Builder> consumeItems = RewardHelper.toRewardItemImmutableList(giftCfg.getCost());
		boolean flag = this.getDataGeter().cost(playerId, consumeItems, 1, Action.AFTER_COMPETITION_BUY_GIFT, false);
		if (!flag) {
			return 0;
		}
		
		buyGiftSucc(entity, giftCfg, targetPlayerId);
		return 0;
	}
	
	/**
	 * 购买礼物成功操作
	 * @param entity
	 * @param giftCfg
	 * @param targetPlayerId
	 */
	private void buyGiftSucc(AfterCompetitionEntity entity, AfterCompetitionShopCfg giftCfg, String targetPlayerId) {
		String playerId = entity.getPlayerId();
		int giftId = giftCfg.getId();
		ACGiftItem giftInfo = entity.getGiftInfo(giftId);
		if (giftInfo == null) {
			giftInfo = entity.addGiftItem(giftId);
		}
		
		giftInfo.addSelfSendCount(1);
		entity.notifyUpdate();
		//redis计数
		getRedis().hIncrBy(getGiftBuyCountKey(), String.valueOf(giftId), 1, getRedisExpire());

		List<RewardItem.Builder> rewardList = new ArrayList<>();
		String itemInfo = giftManager.getGiftRewardItems(giftCfg.getGetGoods(), rewardList);
		Object[] mailContent = new Object[1];
		mailContent[0] = giftId;
		sendMailToPlayer(playerId, MailId.ACTIVITY_371_SEND_GIFT, null, null, mailContent, rewardList); //送出赠礼后，自己通过邮件，收到额外奖励
		
		//需要有赠礼记录，记录自己送出赠礼时：赠礼目标、赠礼内容、赠礼时间
		giftManager.addSendRecord(entity, giftId, targetPlayerId, itemInfo);
		int recieveCount = 0;
		if (!HawkOSOperator.isEmptyString(targetPlayerId) && !targetPlayerId.equals(playerId)) {
			String giftRec = getRedis().hGet(getGiftRecKey(targetPlayerId), String.valueOf(giftId));
			int recCount = HawkOSOperator.isEmptyString(giftRec) ? 0 : Integer.parseInt(giftRec);
			if (recCount < giftCfg.getGetTimesLimit()) {
				recieveCount = recCount + 1; 
				getRedis().hIncrBy(getGiftRecKey(targetPlayerId), String.valueOf(giftId), 1, getRedisExpire());
				giftManager.sendToPlayer(playerId, giftCfg, targetPlayerId);
			} else {
				recieveCount = recCount;
				HawkLog.logPrintln("AfterCompetitionActivity buyGift send empty, recieve uplimit, playerId: {}, giftId: {}, sendTo: {}", playerId, giftId, targetPlayerId);
			}
		}
		
		int globalCountCache = giftManager.getGiftInfo(giftId) == null ? 0 : giftManager.getGiftInfo(giftId).getGlobalBuyCount();
		String globalCount = getRedis().hGet(getGiftBuyCountKey(), String.valueOf(giftId));
		int globalSendTotal = globalCount == null ? 0 : Integer.parseInt(globalCount);

		HawkLog.logPrintln("AfterCompetitionActivity buyGiftSucc succ, playerId: {}, giftId: {}, sendTo: {}:{}, selfSend: {}, globalSend: {}-{}", 
				playerId, giftId, targetPlayerId, recieveCount, giftInfo.getSelfSendCount(), globalCountCache, globalSendTotal);
		
		syncActivityInfo(playerId, entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.ACTIVITY371_BUY_FINISH_SYNC_VALUE));
		
		//送礼打点
		Map<String, Object> param = new HashMap<>();
        param.put("giftId", giftId);                             //礼包id
        param.put("targetPlayerId", targetPlayerId == null ? "" : targetPlayerId); //送礼对象玩家id
        param.put("selfSendTotal", giftInfo.getSelfSendCount()); //此礼物自己送过多少次
        param.put("targetRecTotal", recieveCount);               //此礼物接收方接收过多少次
        param.put("globalSendCache", globalCountCache);          //此礼物全服共送出过多少次（缓存数据）
        param.put("globalSendTotal", globalSendTotal);           //此礼物全服共送出过多少次
        getDataGeter().logActivityCommon(playerId, LogInfoType.after_comp_send_gift, param);
	}
	
	/**
	 *  赠礼次数达到指定数量，手Q/微信冠军联盟盟主可以在指定时间段发放全服大赏
	 */
	public int distGlobalBigAward(String playerId, int giftId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		String guildId = this.getDataGeter().getGuildId(playerId);
		if (HawkOSOperator.isEmptyString(guildId)) {
			return Status.Error.AFTER_COMPE_BIGAWARD_PERMISSION_VALUE;
		}
		
		String leaderId = this.getDataGeter().getGuildLeaderId(guildId);
		if (!playerId.equals(leaderId)) {
			return Status.Error.AFTER_COMPE_BIGAWARD_PERMISSION_VALUE;
		}
		
		//发送大赏的时间限定在活动期间的每日晚上8~10点（策划配置）
		if (!AfterCompetitionConstCfg.getInstance().checkSendTimeRange()) {
			return Status.Error.AFTER_COMPE_BIGAWARD_TIME_ERR_VALUE;
		}
		
		//判断是否是冠军联盟盟主
		String channelStr = this.getDataGeter().getPlayerChannel(playerId);
		int channel = "qq".equals(channelStr) ? AfterCompetitionConst.CHANNEL_QQ : AfterCompetitionConst.CHANNEL_WX;
		BigAwardPermissionPB.Builder builder = giftManager.buildPermissionInfo(playerId, channel);
		if (builder == null || !guildId.equals(builder.getGuildId())) {
			return Status.Error.AFTER_COMPE_BIGAWARD_PERMISSION_VALUE;
		}
		
		Set<String> awardUuids = giftManager.playerDistBigAward(channel, playerId, guildId, giftId);
		
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		AfterCompetitionEntity entity = opEntity.get();
		syncActivityInfo(playerId, entity);
		
		int globalBuyCache = giftManager.getGiftInfo(giftId) == null ? 0 : giftManager.getGiftInfo(giftId).getGlobalBuyCount();
		String globalCount = getRedis().hGet(getGiftBuyCountKey(), String.valueOf(giftId));
		int globalBuyTotal = globalCount == null ? 0 : Integer.parseInt(globalCount);
		HawkLog.logPrintln("AfterCompetitionActivity distGlobalBigAward succ, playerId: {}, giftId: {}, channel: {}, guildId: {}, globalBuy: {}-{}, awardUuids: {}", 
				playerId, giftId, channel, guildId, globalBuyCache, globalBuyTotal, awardUuids);
		return 0;
	}
	
	/**
	 * 领取全服大奖
	 * @param giftId
	 * @param channel
	 * @return
	 */
	public int recieveGlobalBigAward(String playerId, int giftId, int channel, String awardUuid) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		Optional<AfterCompetitionEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		AfterCompetitionEntity entity = optional.get();
		ACGiftItem giftItem = entity.getGiftInfo(giftId);
		GiftBigAwardInfo giftInfo = giftManager.getGiftInfo(giftId);
		if (giftItem == null) {
			if (giftInfo == null) {
				return Status.Error.AFTER_COMPE_GIFT_LOCK_ERR_VALUE;
			}
			giftItem = entity.addGiftItem(giftId);
		}
		
		Set<String> uuidSet = giftInfo.getSendAwardIds(channel);
		if (!uuidSet.contains(awardUuid)) {
			return Status.Error.AFTER_COMPE_BIGAWARD_NOT_EXIST_VALUE;
		}
		
		//已领取过了
		if (giftItem.getBigRewardRecList(channel).contains(awardUuid)) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		List<RewardItem.Builder> rewardItems = new ArrayList<>();
		giftManager.getGiftRewardItems(giftCfg.getRedPackReward(), rewardItems);
		this.getDataGeter().takeReward(playerId, rewardItems, 1, Action.AFTER_COMPETITION_BIG_REWARD, true, RewardOrginType.ACTIVITY_REWARD);
		giftItem.addRecieveAward(channel, awardUuid);
		entity.notifyUpdate();
		
		HawkLog.logPrintln("AfterCompetitionActivity recieveGlobalBigAward succ, playerId: {}, giftId: {}, channel: {}, awardUuid: {}", playerId, giftId, channel, awardUuid);

		syncActivityInfo(playerId, entity);
		
		//领取全服大奖
		Map<String, Object> param = new HashMap<>();
        param.put("giftId", giftId);       //礼包id
        param.put("channel", channel);     //渠道
        param.put("awardUuid", awardUuid); //奖励uuid
        getDataGeter().logActivityCommon(playerId, LogInfoType.after_comp_rec_bigaward, param);
		
		return 0;
	}
	
	/**
	 * 选定默认的礼物发送对象玩家
	 * @param playerId
	 * @param giftId
	 * @param toPlayerId
	 * @return
	 */
	public int setDefaultSend(String playerId, int giftId, String toPlayerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		AfterCompetitionShopCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(AfterCompetitionShopCfg.class, giftId);
		if (giftCfg == null) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		if (!this.getDataGeter().checkPlayerExist(toPlayerId) || playerId.equals(toPlayerId)) {
			return Status.Error.AFTER_COMPE_RECIEVER_SERVER_ERR_VALUE;
		}
		
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		AfterCompetitionEntity entity = opEntity.get();
		ACGiftItem giftItem = entity.getGiftInfo(giftId);
		if (giftItem == null && giftManager.getGiftInfo(giftId) == null) {
			return Status.Error.AFTER_COMPE_GIFT_LOCK_ERR_VALUE;
		}
		
		if(giftItem == null) {
			giftItem = entity.addGiftItem(giftId);
		}
		giftItem.setDefaultSendPlayer(toPlayerId);
		entity.notifyUpdate();
		HawkLog.logPrintln("AfterCompetitionActivity setDefaultSend succ, playerId: {}, giftId: {}, targetPlayerId: {}", playerId, giftId, toPlayerId);
		syncActivityInfo(playerId, entity);
		return 0;
	} 
	
	/**
	 * 查询指定玩家某一礼物领取过多少次
	 * @param playerId
	 * @param toPlayerId
	 * @return
	 */
	public int queryReceiveGiftCount(String playerId, String targetPlayerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		if (!this.getDataGeter().checkPlayerExist(targetPlayerId)) {
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Map<String, String> map = getRedis().hGetAll(getGiftRecKey(targetPlayerId));
		GiftRecAmountResp.Builder builder = GiftRecAmountResp.newBuilder();
		builder.setTargetPlayerId(targetPlayerId);
		for (Entry<String, String> entry : map.entrySet()) {
			GiftRecAmountPB.Builder b = GiftRecAmountPB.newBuilder();
			b.setGiftId(Integer.parseInt(entry.getKey()));
			b.setCount(Integer.parseInt(entry.getValue()));
			builder.addGiftInfo(b);
		}
		pushToPlayer(playerId, HP.code2.ACTIVITY371_TARGET_GIFTREC_S_VALUE, builder);
		return 0;
	}
	
	/**
	 * 获取发送礼物记录
	 * @param playerId
	 * @return
	 */
	public int querySendGiftRecord(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		AfterCompetitionEntity entity = opEntity.get();
		GiftSendRecordSyncPB.Builder builder = GiftSendRecordSyncPB.newBuilder();
		for (SendGiftInfo record : entity.getSendGiftRecordList()) {
			builder.addRecord(record.toBuilder());
		}
		
		pushToPlayer(playerId, HP.code2.ACTIVITY371_SEND_RECORD_S_VALUE, builder);
		return 0;
	}
	
	/**
	 * 获取收取礼物记录
	 * @param playerId
	 * @return
	 */
	public int queryRecieveGiftRecord(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		AfterCompetitionEntity entity = opEntity.get();
		GiftRecRecordSyncPB.Builder builder = GiftRecRecordSyncPB.newBuilder();
		for (RecGiftInfo record : entity.getRecGiftRecordList()) {
			builder.addRecord(record.toBuilder());
		}
		
		pushToPlayer(playerId, HP.code2.ACTIVITY371_RECIEVE_RECORD_S_VALUE, builder);
		return 0;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (!this.isOpening(playerId)) {
			return;
		}
		Optional<AfterCompetitionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		this.syncActivityInfo(playerId, opEntity.get());
	}

	/**
	 * 同步活动信息
	 * @param playerId
	 * @param entity
	 */
	protected void syncActivityInfo(String playerId, AfterCompetitionEntity entity) {
		syncActivityInfo(playerId, entity, 0);
	}
	
	/**
	 * 同步界面信息
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, AfterCompetitionEntity entity, int add) {
		if (!isOpening(playerId)) {
			return;
		}
		
		AfterCompetitionActivityInfo.Builder builder = AfterCompetitionActivityInfo.newBuilder();
		if (add > 0) {
			builder.setGlobalHomageCount(homageCountGlobal.get() + 1);
		} else {
			builder.setGlobalHomageCount(homageCountGlobal.get());	
		}
		builder.setHomageToday(HawkTime.isToday(entity.getHomageTime()) ? 1 : 0);
		BigAwardPermissionPB.Builder qqBuilder = giftManager.buildPermissionInfo(playerId, AfterCompetitionConst.CHANNEL_QQ);
		if (qqBuilder != null) {
			builder.setQqPermission(qqBuilder);
		}
		BigAwardPermissionPB.Builder wxBuilder = giftManager.buildPermissionInfo(playerId, AfterCompetitionConst.CHANNEL_WX);
		if (wxBuilder != null) {
			builder.setWxPermission(wxBuilder);
		}
		
		giftManager.buildGiftInfo(entity, builder);
		
		//展示三类赛季冠军的联盟信息
		ConfigIterator<AfterCompetitionUnionCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(AfterCompetitionUnionCfg.class);
		while (iterator.hasNext()) {
			AfterCompetitionUnionCfg cfg = iterator.next();
			if (cfg.getRank() == 1) {
				ChampionGuildPB.Builder guildBuilder = ChampionGuildPB.newBuilder();
				guildBuilder.setChannel(cfg.getChannel());
				guildBuilder.setRace(cfg.getRace());
				guildBuilder.setGuildId(cfg.getWinnerUnion());
				guildBuilder.setGuildName(cfg.getUnionName());
				guildBuilder.setServerId(cfg.getServerId());
				builder.addGuild(guildBuilder);
			}
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.ACTIVITY371_INFO_S, builder));
	}

	/**
	 * 获取活动的结束时间
	 * @return
	 */
	protected long getEndTime() {
		int termId = this.getActivityTermId();
		return this.getTimeControl().getEndTimeByTermId(termId);
	}
	
	/**
	 * 获取礼物信息
	 * @param giftId
	 * @return
	 */
	public GiftBigAwardInfo getGiftInfo(int giftId) {
		return giftManager.getGiftInfo(giftId);
	}
	
	/**
	 * 获取冠军联盟信息
	 * @param channel
	 * @param rank
	 * @return
	 */
	public ChampionGuildInfo getGuildInfo(int channel, int rank) {
		return guildInfoMap.get(channel*100 + rank);
	}
	
	/**
	 * 记录统计全服致敬次数的key
	 * @return
	 */
	public String getGlobalHomageKey(){
		return AfterCompetitionConst.REDIS_KEY_HOMAGE + getActivityTermId();
	}
	
	/**
	 * 记录礼物接受次数
	 * @param playerId
	 * @return
	 */
	public String getGiftRecKey(String playerId){
		return AfterCompetitionConst.REDIS_KEY_GIFT_REC + getActivityTermId() + ":" + playerId;
	}
	
	/**
	 * 记录每个礼包购买次数的key
	 * @return
	 */
	public String getGiftBuyCountKey(){
		return AfterCompetitionConst.REDIS_KEY_GIFT_PAY + getActivityTermId();
	}
	
	/**
	 * 记录礼包解锁时间
	 * @return
	 */
	public String getGiftUnlockKey(){
		return AfterCompetitionConst.REDIS_KEY_GIFT_UNLOCK + getActivityTermId();
	}
	
	/**
	 * 获取冠军联盟信息
	 * @param channel
	 * @return
	 */
	public String getGuildInfoKey(int channel){
		return AfterCompetitionConst.REDIS_KEY_GUILD_INFO + getActivityTermId() + ":" + channel;
	}
	
	/**
	 * 记录已发放的全服大奖
	 * @return
	 */
	public String getGlobalGiftKey(int channel, int giftId){
		return AfterCompetitionConst.REDIS_KEY_GLOBAL_GIFT + getActivityTermId() + ":" + channel + ":" + giftId;
	}
	
	/**
	 * 记录礼物领取记录
	 * @param playerId
	 * @return
	 */
	public String getGiftRecRecordKey(String playerId){
		return AfterCompetitionConst.REDIS_KEY_REC_RECORD + getActivityTermId() + ":" + playerId;
	}
	
	/**
	 * 记录礼物发送数据
	 * @param playerId
	 * @return
	 */
	public String getGiftSendRecordKey(String playerId){
		return AfterCompetitionConst.REDIS_KEY_SEND_RECORD + getActivityTermId() + ":" + playerId;
	}

	public HawkRedisSession getRedis() {
		return ActivityGlobalRedis.getInstance().getRedisSession();
	}
	
	public int getRedisExpire() {
		return 86400 * 60;
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
}
