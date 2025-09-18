package com.hawk.activity.type.impl.groupBuy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang.math.NumberUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.GroupBuyScoreEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.timeController.ITimeController;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyAchieveCfg;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyGoodsCfg;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupBuyPriceCfg;
import com.hawk.activity.type.impl.groupBuy.cfg.GroupTopDiscountCfg;
import com.hawk.activity.type.impl.groupBuy.entity.GroupBuyEntity;
import com.hawk.activity.type.impl.groupBuy.entity.GroupBuyRecord;
import com.hawk.common.CommonConst.ServerType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.GroupBuyGiftInfo;
import com.hawk.game.protocol.Activity.GroupBuyPageInfo;
import com.hawk.game.protocol.Activity.GroupBuyRecordPageInfo;
import com.hawk.game.protocol.Activity.HotSellFreePointResp;
import com.hawk.game.protocol.Activity.TopDiscountRewardGetResp;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 团购活动 （万人团购）
 * hf
 */
public class GroupBuyActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	//每个礼包对应的注水更新时间
	private Map<Integer, Long> giftUpDateTimeMap = new HashMap<>();
	
	public GroupBuyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GROUP_BUY_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.GROUP_BUY_INIT, () -> {
				initAchieve(playerId);
			});
		}
	}

	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GroupBuyActivity activity = new GroupBuyActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GroupBuyEntity> queryList = HawkDBManager.getInstance()
				.query("from GroupBuyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GroupBuyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GroupBuyEntity entity = new GroupBuyEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}


	@Override
	public boolean isProviderActive(String playerId) {
		return !isHidden(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<GroupBuyEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		GroupBuyEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.GROUP_BUG_TASK_REWARD;
	}
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<GroupBuyEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		GroupBuyEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<GroupBuyAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GroupBuyAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			GroupBuyAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		return Result.success();
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GroupBuyEntity entity = opEntity.get();
		//push
		pushToPlayer(playerId, HP.code.GROUP_BUY_PAGE_INFO_RESP_VALUE, genGroupBuyGiftInfoList(entity));
	}

	/**
	 * 礼包购买记录请求
	 * @param playerId
	 * @return
	 */
	public GroupBuyRecordPageInfo.Builder getGroupBuyRecord(String playerId){
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return null;
		}
		return genGroupBuyRecordInfoList(opEntity.get());
	}
	
	/**
	 * 礼包详情信息
	 * @param entity
	 * @return
	 */
	public GroupBuyPageInfo.Builder genGroupBuyGiftInfoList(GroupBuyEntity entity){
		GroupBuyPageInfo.Builder gBuilder = GroupBuyPageInfo.newBuilder();
		//购买积分
		gBuilder.setBuyScore(entity.getBuyScore());
		ConfigIterator<GroupBuyGoodsCfg> giftConfigIterator = HawkConfigManager.getInstance().getConfigIterator(GroupBuyGoodsCfg.class);
		 while (giftConfigIterator.hasNext()) {
			 GroupBuyGoodsCfg cfg = giftConfigIterator.next();
			 int giftId = cfg.getId();
			 GroupBuyGiftInfo.Builder builder = GroupBuyGiftInfo.newBuilder();
			 builder.setId(giftId);
			 //全服购买次数
			 long giftGlobalBuyTimes = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(getWaterGiftKey(giftId)));
			 builder.setGlobalBuyTimes(giftGlobalBuyTimes);
			 int buyTimes = entity.getBuyTimesMap().getOrDefault(giftId, 0);
			 int remainBuyTimes = cfg.getBuyTimes() - buyTimes;
			 builder.setRemainBuyTimes(remainBuyTimes);
			 builder.setBuyTimes(buyTimes);
			 //最高折扣的奖励已领取的部分
			 List<Integer> topDiscountRewardGotList = entity.getTopDiscountRewardGotList(cfg.getId());
			 if (!topDiscountRewardGotList.isEmpty()) {
				 builder.addAllTopDiscountRewardGot(topDiscountRewardGotList);
			 } 
			 //热销商品免费积分是否已领取过
			 if (cfg.getHotSell() > 0) {
				 builder.setHotSellFreeGot(entity.getHotSellFreeGotList().contains(cfg.getId()) ? 1 : 0);
			 } else {
				 builder.setHotSellFreeGot(-1);
			 }
			 
			 gBuilder.addGroupBuyGiftInfo(builder);
		 }
		return gBuilder;
	}


	/**
	 * 礼包购买记录信息
	 * @param entity
	 * @return
	 */
	public GroupBuyRecordPageInfo.Builder genGroupBuyRecordInfoList(GroupBuyEntity entity){
		GroupBuyRecordPageInfo.Builder builder = GroupBuyRecordPageInfo.newBuilder();
		List<GroupBuyRecord> grRecords = new ArrayList<>(entity.getBuyRecordList());
		int size = grRecords.size();
		//只发送50条记录
		if (size > 50) {
			 grRecords.subList(0, (size - 50)).clear();
		}
		for (GroupBuyRecord groupBuyRecord : grRecords) {
			builder.addGroupBuyRecordInfo(groupBuyRecord.createBuilder().build());
		 }
		return builder;
	}
	
	/**
	 * 购买团购礼包
	 * @param playerId
	 * @param giftId
	 * @param num 购买个数
	 * @return
	 */
	public Result<?> groupBuyGift(String playerId, int giftId, int num,  int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		GroupBuyEntity entity = opEntity.get();
		GroupBuyGoodsCfg goodsCfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyGoodsCfg.class, giftId);
		int hasBuyTimes = entity.getBuyTimesMap().getOrDefault(giftId, 0);
		if (hasBuyTimes >= goodsCfg.getBuyTimes()) {
			logger.error("GroupBuyActivity groupBuyGift buyTimes limit , playerId: {}, giftId:{}", playerId, giftId);
			return Result.fail(Status.Error.GROUP_BUY_TIMES_LIMIT_VALUE);	
		}
		
		//根据礼包购买次数,获取礼包当前价格相关数据
		GroupBuyPriceCfg priceCfg = getGroupBuyPriceCfg(giftId);
		if (!entity.getTopDiscountGiftList().contains(giftId)) {
			GroupBuyPriceCfg topDiscountCfg = GroupBuyPriceCfg.getTopDiscountCfg(giftId);
			if (topDiscountCfg != null && priceCfg != null && topDiscountCfg.getId() == priceCfg.getId()) {
				entity.getTopDiscountGiftList().add(giftId);
				entity.notifyUpdate();
			}
		}
		
		//单次消耗
		List<RewardItem.Builder> counsumeList = new ArrayList<>();
		RewardItem.Builder counsumeItem = RewardHelper.toRewardItem(priceCfg.getPrice());
		counsumeItem.setItemCount(counsumeItem.getItemCount() * num);
		//消耗金条数量
		int costDiamond = (int) counsumeItem.getItemCount();
		counsumeList.add(counsumeItem);
		boolean success = getDataGeter().consumeItems(playerId, counsumeList, protoType, Action.GROUP_BUG_COST);
		if (!success) {
			logger.error("GroupBuyActivity groupBuyGift consume not enought, playerId: {}, giftId:{}", playerId, giftId);
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//发奖
		this.getDataGeter().takeReward(playerId, goodsCfg.getRewardList(), num,  Action.GROUP_BUG_REWARD, true);
		//update db
		entity.addBuyRecord(GroupBuyRecord.valueOf(giftId, priceCfg.getId(), HawkTime.getMillisecond(), num));
		//更新已经购买的次数
		entity.getBuyTimesMap().put(giftId, hasBuyTimes + num);
		entity.buyScoreAdd(costDiamond);
		//存真实数据
		long realBuyGiftTimes = ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(getGiftKey(giftId), num, getExpireSeconds());
		//存新注水数据
		long giftBuyTimes = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(getWaterGiftKey(giftId)));
		//注水系数
		long waterAddNum = getWaterRateByChannel(playerId, giftId, giftBuyTimes) * num;
		long waterBuyGiftTimes = ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(getWaterGiftKey(giftId), waterAddNum, getExpireSeconds());
		
		String serverId = this.getDataGeter().getServerId();
		String recordKey = getGroupBuyPlayerKeys(serverId);
		// 记录投资人的id
		ActivityGlobalRedis.getInstance().sAdd(recordKey, getExpireSeconds(), playerId);
		//post event
		ActivityManager.getInstance().postEvent(new GroupBuyScoreEvent(playerId, entity.getBuyScore()));
		//push
		syncActivityDataInfo(playerId);
		
		int termId = getActivityTermId();
		//Tlog
		this.getDataGeter().logGroupBuy(playerId, termId, giftId, priceCfg.getId(), realBuyGiftTimes, waterBuyGiftTimes);
		logger.info("GroupBuyActivity groupBuyGift playerId:{}, giftId:{}, realTimes:{}, waterTimes:{}", playerId, giftId, realBuyGiftTimes, waterBuyGiftTimes);
		return Result.success();
	}

	
	/**
	 * 折扣校验
	 * @param playerId
	 * @param giftId
	 * @param cfgId
	 * @param protoType
	 * @return
	 */
	public Result<?> groupBuyDiscountCheck(String playerId, int giftId, int cfgId){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		//根据礼包购买次数,获取礼包当前价格相关数据
		GroupBuyPriceCfg priceCfg = getGroupBuyPriceCfg(giftId);
		if (priceCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if (priceCfg.getId() != cfgId) {
			return Result.fail(Status.Error.GROUP_BUY_CHECK_DISCOUNT_FAILD_VALUE);
		}
		
		GroupBuyEntity entity = opEntity.get();
		if (!entity.getTopDiscountGiftList().contains(giftId)) {
			GroupBuyPriceCfg topDiscountCfg = GroupBuyPriceCfg.getTopDiscountCfg(giftId);
			if (topDiscountCfg != null && topDiscountCfg.getId() == priceCfg.getId()) {
				entity.getTopDiscountGiftList().add(giftId);
				entity.notifyUpdate();
			}
		}
		
		return Result.success();
	}
	
	
	/**
	 * 热销商品免费积分领取
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public Result<?> hotSellFreePointGet(String playerId, int giftId){
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		GroupBuyGoodsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyGoodsCfg.class, giftId);
		if (cfg == null || cfg.getHotSell() <= 0) {
			HawkLog.errPrintln("groupbuy activity hotSell freePoint got config error, playerId: {}, giftId: {}", playerId, giftId);
			return Result.fail(Status.Error.GROUP_BUY_HOT_SELL_ERR_VALUE); //不是热销商品
		}
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		GroupBuyEntity entity = opEntity.get();
		if (entity.getHotSellFreeGotList().contains(giftId)) {
			HawkLog.errPrintln("groupbuy activity hotSell freePoint got repeated error, playerId: {}, giftId: {}", playerId, giftId);
			return Result.fail(Status.Error.GROUP_BUY_HOT_SELL_REPEATED_VALUE); //已领取过了，不能重复领取
		}
		int points = cfg.getPoints();
		if (points <= 0) {
			HawkLog.errPrintln("groupbuy activity hotSell freePoint got error, playerId: {}, giftId: {}, points: {}", playerId, giftId, points);
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		
		entity.getHotSellFreeGotList().add(giftId);
		entity.buyScoreAdd(points);
		ActivityManager.getInstance().postEvent(new GroupBuyScoreEvent(playerId, entity.getBuyScore()));
		syncActivityDataInfo(playerId);
		
		HotSellFreePointResp.Builder builder = HotSellFreePointResp.newBuilder();
		builder.setGiftId(giftId);
		builder.setPoints(points);
		pushToPlayer(playerId, HP.code2.GROUP_BUY_HOTSELL_FREEPOINT_S_VALUE, builder);
		
		HawkLog.logPrintln("groupbuy activity hotSell freePoint got, playerId: {}, giftId: {}, points: {}", playerId, giftId, points);
		return Result.success();
	}
	
	/**
	 * 最高折扣奖励领取
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public Result<?> topDiscountRewardGet(String playerId, int giftId, int buyTimes){
		if (isHidden(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		GroupBuyGoodsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyGoodsCfg.class, giftId);
		GroupTopDiscountCfg topDisCountCfg = HawkConfigManager.getInstance().getConfigByKey(GroupTopDiscountCfg.class, giftId);
		if (cfg == null || topDisCountCfg == null) {
			return Result.fail(Status.SysError.CONFIG_ERROR_VALUE);
		}
		
		if (buyTimes != 0 && !topDisCountCfg.getBuyTimesList().contains(buyTimes)) {
			HawkLog.errPrintln("groupbuy activity topDiscount reward no item of buyTimes exist, playerId: {}, giftId: {}, buyTimes: {}", playerId, giftId, buyTimes);
			return Result.fail(Status.Error.GROUP_BUY_DISCOUNT_ITEM_ERR_VALUE); //当前礼包不存在购买该次数的档位
		}
		
		Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		GroupBuyEntity entity = opEntity.get();
		if (!entity.getTopDiscountGiftList().contains(giftId)) {
			GroupBuyPriceCfg priceCfg = getGroupBuyPriceCfg(giftId);
			GroupBuyPriceCfg topDiscountCfg = GroupBuyPriceCfg.getTopDiscountCfg(giftId);
			if (topDiscountCfg != null && priceCfg != null && topDiscountCfg.getId() == priceCfg.getId()) {
				entity.getTopDiscountGiftList().add(giftId);
				entity.notifyUpdate();
			}
			if (!entity.getTopDiscountGiftList().contains(giftId)) {
				HawkLog.errPrintln("groupbuy activity topDiscount reward cond of discount not enough, playerId: {}, giftId: {}, price now: {}", playerId, giftId, priceCfg.getPrice());
				return Result.fail(Status.Error.GROUP_BUY_DISCOUNT_LOW_VALUE); //还未达到最高折扣
			}
		}
		
		List<Integer> gotList = entity.getTopDiscountRewardGotList(giftId);
		if (gotList.contains(buyTimes)) {
			HawkLog.errPrintln("groupbuy activity topDiscount reward got repeated, playerId: {}, giftId: {}, buyTimes: {}", playerId, giftId, buyTimes);
			return Result.fail(Status.Error.GROUP_BUY_DISCOUNT_REWARD_REPEATED_VALUE); //已经领取过了
		}
		
		if (buyTimes > 0 && entity.getBuyTimesMap().getOrDefault(giftId, 0) < buyTimes) {
			HawkLog.errPrintln("groupbuy activity topDiscount reward buyTimes not enough, playerId: {}, giftId: {}, buyTimes: {}, already: {}", playerId, giftId, buyTimes, entity.getBuyTimesMap().getOrDefault(giftId, 0));
			return Result.fail(Status.Error.GROUP_BUY_DISCOUNT_BUYTIMES_ERR_VALUE); //当前礼包购买次数不满足条件
		}
		
		entity.addTopDiscountRewardGot(giftId, buyTimes);
		
		TopDiscountRewardGetResp.Builder builder = TopDiscountRewardGetResp.newBuilder();
		builder.setGiftId(giftId);
		builder.setBuyTimesItem(buyTimes);
		if (buyTimes == 0) {
			builder.setPoints(topDisCountCfg.getPoints());
			entity.buyScoreAdd(topDisCountCfg.getPoints());
			ActivityManager.getInstance().postEvent(new GroupBuyScoreEvent(playerId, entity.getBuyScore()));
		} else {
			int index = topDisCountCfg.getBuyTimesList().indexOf(buyTimes);
			String rewards = topDisCountCfg.getBuyTimesRewardList().get(index);
			ImmutableList<RewardItem.Builder> itemList = RewardHelper.toRewardItemImmutableList(rewards);
			this.getDataGeter().takeReward(playerId, itemList, 1,  Action.GROUP_BUY_TOP_DISCOUNT_REWARD, true);
		}
		pushToPlayer(playerId, HP.code2.GROUP_BUY_TOPDISCOUNT_REWARD_S_VALUE, builder);

		HawkLog.logPrintln("groupbuy activity topDiscount reward got, playerId: {}, giftId: {}, buyTimes: {}", playerId, giftId, buyTimes);
		return Result.success();
	}
	
	
	@Override
	public void onEnd() {
		String serverId = this.getDataGeter().getServerId();
		String recordKey = getGroupBuyPlayerKeys(serverId);
		Set<String> playerIds = ActivityGlobalRedis.getInstance().sMembers(recordKey);
		long startTime = getTimeControl().getStartTimeByTermId(getActivityTermId());
		long endTime = getTimeControl().getEndTimeByTermId(getActivityTermId());
		Long serverMergeTime = getDataGeter().getServerMergeTime();
		if (serverMergeTime == null) {
			serverMergeTime = 0l;
		}
		List<String> slaveServerList = getDataGeter().getSlaveServerList();
		// 本期活动开启之后合并的服务器,需要加载从服的投资人员列表
		if (!slaveServerList.isEmpty() && serverMergeTime >= startTime && serverMergeTime <= endTime) {
			for (String followServerId : slaveServerList) {
				String followRecordKey = getGroupBuyPlayerKeys(followServerId);
				Set<String> followPlayerIds = ActivityGlobalRedis.getInstance().sMembers(followRecordKey);
				if (!followPlayerIds.isEmpty()) {
					playerIds.addAll(followPlayerIds);
				}
			}
		}
		if (playerIds.isEmpty()) {
			HawkLog.logPrintln("GroupBuyActivity onEnd noPlayerGroupBuy, termId: {}", getActivityTermId());
			return;
		}
		Map<String, List<RewardItem.Builder>> dataMap = new HashMap<>();
		Map<String, Integer> buyTimesMap = new HashMap<>();
		for (String playerId : playerIds) {
			Optional<GroupBuyEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				HawkLog.logPrintln("GroupBuyActivity onEnd sendAwardError, entity is null, playerId: {}", playerId);
				continue;
			}
			GroupBuyEntity dataEntity = opEntity.get();
			//计算前后差价的奖励
			List<RewardItem.Builder> rewardList = calcReissueReward(dataEntity);
			if (rewardList == null || rewardList.isEmpty() || rewardList.get(0).getItemCount() <= 0) {
				HawkLog.logPrintln("GroupBuyActivity onEnd DiffRewardList is empty, playerId: {}", playerId);
				continue;
			}
			//购买次数
			int buyTimes = dataEntity.getBuyRecordList().size();
			buyTimesMap.put(playerId, buyTimes);
			dataMap.put(playerId, rewardList);
		}
		for (String playerId : playerIds) {
			final List<RewardItem.Builder> rewardData = dataMap.get(playerId);
			final int buyTimes = buyTimesMap.getOrDefault(playerId, 0);
			if (buyTimes == 0 || rewardData == null || rewardData.isEmpty()|| rewardData.get(0).getItemCount() <= 0) {
				continue;
			}
			callBack(playerId, MsgId.GROUP_BUY_END_REWARD, () -> {
				sendReward(playerId, rewardData, buyTimes);
			});
		}
	}
	
	/**
	 * 补发未领取的奖励
	 * 
	 * @param rewardData
	 */
	private void sendReward(String playerId,  List<RewardItem.Builder> rewardData, int buyTimes) {
		MailId mailId = MailId.GROUP_BUY_REWARD;
		//邮件发送奖励
		Object[] title = new Object[0];
		Object[] subTitle = new Object[0];
		Object[] content = new Object[2];
		content[0] = buyTimes;  //购买商品数量
		content[1] = rewardData.get(0).getItemCount();	//返回金条数量
		// 邮件发送奖励
		sendMailToPlayer(playerId, mailId, title, subTitle, content, rewardData);
		HawkLog.logPrintln("GroupBuyActivity sendDiscountReward success, playerId: {}", playerId);
	}
	
	/**
	 * 计算待补发奖励信息
	 * @param entity
	 * @return
	 */
	private List<RewardItem.Builder> calcReissueReward(GroupBuyEntity entity) {
		if (entity == null) {
			return null;
		}
		try {
			List<RewardItem.Builder> rewardList = new ArrayList<>();
			//当前礼包折扣数据
			Map<Integer, GroupBuyPriceCfg> discountMap = getDiscountMap();
			
			List<GroupBuyRecord> recordList = entity.getBuyRecordList();
			//实例奖励,取第一个奖励,即金条
			GroupBuyPriceCfg diamondPriceCfg = HawkConfigManager.getInstance().getConfigByIndex(GroupBuyPriceCfg.class, 0);
			RewardItem.Builder diamondCounsumeItem = RewardHelper.toRewardItem(diamondPriceCfg.getPrice());
			diamondCounsumeItem.setItemCount(0);
			
			for (GroupBuyRecord record : recordList) {
				int cfgId = record.getCfgId();
				int giftId = record.getId();
				GroupBuyPriceCfg priceCfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyPriceCfg.class, cfgId);
				//购买花费
				String priceStr = priceCfg.getPrice();
				long beforeCost = RewardHelper.toRewardItem(priceStr).getItemCount();
				//当前价格
				String currentCostStr = discountMap.get(giftId).getPrice();
				long currentCost =  RewardHelper.toRewardItem(currentCostStr).getItemCount();
				long addNum = beforeCost - currentCost;
				addNum = addNum * record.getNum();
				//每个礼包的差额奖励
				diamondCounsumeItem.setItemCount(diamondCounsumeItem.getItemCount() + addNum);
				HawkLog.logPrintln("GroupBuyActivity calcReissueReward playerId:{}, giftId:{}, addNum:{} ",entity.getPlayerId(), giftId, addNum);
			}
			rewardList.add(diamondCounsumeItem);
			return rewardList;
			
		} catch (Exception e) {
			HawkException.catchException(e);
			HawkLog.logPrintln("GroupBuyActivity calcRewardData error, playerId:{}, termId:{}, buyRecord:{},", entity.getPlayerId(), entity.getTermId(), entity.getBuyRecord());
		}
		return null;
	}
	
	/**
	 * 根据当前购买次数
	 * 获取礼包对应的折扣信息
	 * @return
	 */
	public Map<Integer, GroupBuyPriceCfg> getDiscountMap(){
		 Map<Integer, GroupBuyPriceCfg> discountMap = new HashMap<>();
		 
		 ConfigIterator<GroupBuyGoodsCfg> giftConfigIterator = HawkConfigManager.getInstance().getConfigIterator(GroupBuyGoodsCfg.class);
		 while (giftConfigIterator.hasNext()) {
			 GroupBuyGoodsCfg giftCfg = giftConfigIterator.next();
			 int giftId = giftCfg.getId();	
			 ConfigIterator<GroupBuyPriceCfg> priceConfigIterator = HawkConfigManager.getInstance().getConfigIterator(GroupBuyPriceCfg.class);
			 //取注水数,算折扣
			 long buyTimes = NumberUtils.toLong(ActivityGlobalRedis.getInstance().get(getWaterGiftKey(giftId)));
			 while (priceConfigIterator.hasNext()) {
				GroupBuyPriceCfg priceCfg = priceConfigIterator.next();
				if (priceCfg.getGoodsId() != giftId) {
					continue;
				}
				int[] conditionTime = SerializeHelper.string2IntArray(priceCfg.getBuyTimes());
				if (buyTimes >= conditionTime[0] && buyTimes <= conditionTime[1]) {
					discountMap.put(giftId, priceCfg); 
				}
			 }	
		}
		 return discountMap;
	}
	
	
	public GroupBuyPriceCfg getGroupBuyPriceCfg(int giftId){
		//根据当前购买次数计算的消耗数据
		Map<Integer, GroupBuyPriceCfg> discountMap = getDiscountMap();
		GroupBuyPriceCfg cfg = discountMap.get(giftId);
		return cfg;
	}
	

	/**礼包真实数据的key
	 * @param giftId
	 * @return
	 */
	public String getGiftKey(int giftId){
		int termId = this.getActivityTermId();
		String key = "activiy_group_buy_" + termId + "_" + giftId;
		return key;
	}
	
	/**礼包注水数据的key
	 * @param giftId
	 * @return
	 */
	public String getWaterGiftKey(int giftId){
		int termId = this.getActivityTermId();
		String key = "activiy_group_buy_water" + termId + "_" + giftId;
		return key;
	}
	
	/**本服购买礼包玩家的数据
	 * @param serverId
	 * @return
	 */
	private String getGroupBuyPlayerKeys(String serverId) {
		int termId = getActivityTermId();
		return "activiy_group_buy_players:" + serverId + ":" + termId;
	}
	
	/**
	 * 记录有效期
	 * @return
	 */
	private int getExpireSeconds() {
		int termId = getActivityTermId();
		ITimeController timeInfo = getTimeControl();
		long startTime = timeInfo.getStartTimeByTermId(termId);
		long hiddenTime = timeInfo.getHiddenTimeByTermId(termId);
		return (int) ((hiddenTime - startTime) / 1000 + 7 * 24 * 3600);
	}
	

	/**获取注水参数
	 * @param playerId
	 * @param giftId
	 * @param buyNum
	 * @return
	 */
	public long getWaterRateByChannel(String playerId, int giftId, long buyNum){
		GroupBuyGoodsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GroupBuyGoodsCfg.class, giftId);
		String areaId = this.getDataGeter().getAreaId();	
		String waterLimitStr = cfg.getWaterLimitWX();
		String waterScaleStr = cfg.getWaterScaleWX();
		if (Integer.valueOf(areaId) == 2) {
			waterLimitStr = cfg.getWaterLimitQQ();
			waterScaleStr = cfg.getWaterScaleQQ();
		}
		int waterNum = 1;
		int[] waterLimit = SerializeHelper.string2IntArray(waterLimitStr);
		int[] waterScale = SerializeHelper.string2IntArray(waterScaleStr);
		if (buyNum < waterLimit[0]) {
			waterNum = waterScale[0];
		}else if(buyNum >= waterLimit[0] && buyNum <= waterLimit[1] ){
			waterNum = waterScale[1];
		}
		return waterNum;
	}

	@Override
	public void onTick() {
		long currTime = HawkTime.getMillisecond();
		int termId = getActivityTermId();
		long endTime = getTimeControl().getEndTimeByTermId(termId);
		if (currTime >= endTime) {
			return;
		}
		
		String lockKey = "lock_group_buy_activity_" + this.getActivityTermId();
		String lockField = "lock";
		String result = ActivityGlobalRedis.getInstance().getRedisSession().hGet(lockKey, lockField);
		String serverId = getDataGeter().getServerId();
		if(HawkOSOperator.isEmptyString(result)) {
			int serverType = this.getDataGeter().getServerType();
			//如果不是正常服务器，不参与抢夺计算锁
			if(serverType != ServerType.NORMAL){
				return;
			}
			//抢锁
			ActivityGlobalRedis.getInstance().getRedisSession().hSetNx(lockKey,lockField, serverId);
			
			logger.info("GroupBuyActivity onTick getLockedsuccess serverId:{}", result);
			return;
		}
		if(result.equals(serverId)){
			//设置锁过期时间
			ActivityGlobalRedis.getInstance().getRedisSession().expire(lockKey, 15);
			//定时检测注水
			giftBuyCountAssistCheck();
			logger.info("GroupBuyActivity onTick giftBuyCountAssistCheck success serverId:{}", getDataGeter().getServerId());
		}
	}

	/**
	 * 定时注水
	 */
	private void giftBuyCountAssistCheck() {
		//获取服务器的平台信息
		String areaId = this.getDataGeter().getAreaId();	
		ConfigIterator<GroupBuyGoodsCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(GroupBuyGoodsCfg.class);
		while (configIterator.hasNext()) {
			GroupBuyGoodsCfg cfg = configIterator.next();
			int giftId = cfg.getId();
			List<int[]> assistList = cfg.getBuyCountAssistWXList();
			if (Integer.valueOf(areaId) == 2) { //2 手q
				assistList = cfg.getBuyCountAssistQQList();
			}
			long startTime = this.getTimeControl().getStartTimeByTermId(this.getActivityTermId());
			long curTime = HawkTime.getMillisecond();
			//对应礼包的刷新时间
			long giftBuyAssistUpdateTime = giftUpDateTimeMap.getOrDefault(giftId, 0L);
			for(int[] times : assistList){
				int sH = times[0];
				int eH = times[1];
				int interval = times[2];
				int addMin = times[3];
				int addMax = times[4];
				int add = HawkRand.randInt(addMin, addMax);
				if((curTime >startTime+sH * 1000) && (curTime <startTime + eH * 1000) && (curTime > (giftBuyAssistUpdateTime + interval * 1000))){
					long giftWaterTimes = ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(getWaterGiftKey(giftId), add, getExpireSeconds());
					giftUpDateTimeMap.put(giftId, curTime);
					logger.info("GroupBuyActivity giftBuyCountAssistCheck giftId:{}, giftWaterTimes:{},addNum:{},  termId:{}", giftId, giftWaterTimes, add, this.getActivityTermId());
				}
			}
		}
	}
	
}
