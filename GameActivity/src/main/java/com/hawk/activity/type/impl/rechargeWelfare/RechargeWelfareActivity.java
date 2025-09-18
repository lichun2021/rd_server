package com.hawk.activity.type.impl.rechargeWelfare;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.DiamondRechargeEvent;
import com.hawk.activity.event.impl.IDIPGmRechargeEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.RechargeWelfareLotteryEvent;
import com.hawk.activity.event.impl.ShareProsperityEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.rechargeWelfare.cfg.RechargeWelfareAchieveCfg;
import com.hawk.activity.type.impl.rechargeWelfare.cfg.RechargeWelfareActivityKVCfg;
import com.hawk.activity.type.impl.rechargeWelfare.cfg.RechargeWelfareRewardCfg;
import com.hawk.activity.type.impl.rechargeWelfare.entity.RechargeWelfareEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.CouponFreeInfo;
import com.hawk.game.protocol.Activity.CouponInfo;
import com.hawk.game.protocol.Activity.RechargeWelfareInfoSync;
import com.hawk.game.protocol.Activity.RechargeWelfareItemSetReq;
import com.hawk.game.protocol.Activity.RechargeWelfareLotteryReq;
import com.hawk.game.protocol.Activity.RechargeWelfareLotteryResp;
import com.hawk.game.protocol.Activity.WelfareState;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 点券夺宝
 * 
 * @author Admin
 *
 */
public class RechargeWelfareActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	public RechargeWelfareActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RECHARGE_WELFARE_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
		}
	}
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.RECHARGE_WELFARE_INIT, () -> {
				initAchieve(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}

	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		/*long startTime = getTimeControl().getStartTimeByTermId(getActivityTermId(), playerId);
		// 活动开启当天跨天,不重置免费次数
		if (HawkTime.isSameDay(startTime, HawkTime.getMillisecond())) {
			return;
		}*/
		RechargeWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		RechargeWelfareEntity entity = opEntity.get();
		//免费次数重置
		entity.setFreeTimes(kvCfg.getDailyFreeTimes());
		//活跃积分
		entity.setDailyScore(0);
		
		entity.setFreeRec(false);
		entity.setReceiveCoupon(0);
		entity.setTotalCoupon(0);
		entity.setReceiveDiamond(0);
		entity.setTotalDiamond(0);
		entity.setDailyLotteryTimes(0);
		
		entity.notifyUpdate();
		try {
			String redisKey = this.getDataGeter().getGmRechargeRedisKey();
			Map<String, String> rechargeDataMap = ActivityGlobalRedis.getInstance().hgetAll(redisKey + ":" + playerId);
	        String todayTime = String.valueOf(HawkTime.getYyyyMMddIntVal());
	        int rechargeDiamonds = Integer.parseInt(rechargeDataMap.getOrDefault(todayTime, "0"));
	        if (rechargeDiamonds > 0) {
	        	HawkLog.logPrintln("RechargeWelfareActivity rechargeCoupon on login, playerId: {}, rechargeDiamonds: {}", playerId, rechargeDiamonds);
	        	rechargeForCouponChange(playerId, rechargeDiamonds);
	        }
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		//sync
		syncActivityInfo(playerId, opEntity.get());
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RechargeWelfareActivity activity = new RechargeWelfareActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RechargeWelfareEntity> queryList = HawkDBManager.getInstance()
				.query("from RechargeWelfareEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RechargeWelfareEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RechargeWelfareEntity entity = new RechargeWelfareEntity(playerId, termId);
		//初始化奖励
		entity.setItemsetList(getInitItemSet());
		RechargeWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);

		entity.setFreeTimes(kvCfg.getDailyFreeTimes());
		entity.setFreeRec(false);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if (this.isOpening(playerId)) {
			Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			RechargeWelfareEntity entity = opEntity.get();
			this.syncActivityInfo(playerId, entity);
		}
	}

	
	private void syncActivityInfo(String playerId, RechargeWelfareEntity entity) {
		if (!isOpening(playerId)) {
			return;
		}
		RechargeWelfareInfoSync.Builder builder = genRechargeWelfareInfoPB(entity);
		//push
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.RECHARGE_WELFARE_INFO_SYNC, builder));
	}


	/**活动奖励设置
	 * @param playerId
	 * @param req
	 * @return
	 */
	public Result<?> rechargeWelfareSetRewardItem(String playerId, RechargeWelfareItemSetReq req) {
		try {
			if (!isOpening(playerId)) {
				return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			}
			Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			RechargeWelfareEntity entity = opEntity.get();
			
			List<Integer> itemSetList = req.getCfgIdsList();
			if (itemSetList.size() <= 0 || itemSetList.size()  > 9) {
				return Result.fail(Status.Error.RECHARGE_WELFARE_ITEM_SET_ERROR_VALUE);
			}
			// 验证参数
			for (Integer cfgId : itemSetList) {
				RechargeWelfareRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareRewardCfg.class,cfgId);
				//配置表错误
				if (null == cfg) {
					return Result.fail(Status.Error.RECHARGE_WELFARE_ITEM_SET_ERROR_VALUE);
				}
			}
			//设置不正确的时候不能保存
			if (!checkItemSetValid(itemSetList)) {
				return Result.fail(Status.Error.RECHARGE_WELFARE_ITEM_SET_ERROR_VALUE);
			}
			// 设置奖励
			entity.setItemsetList(itemSetList);
			entity.notifyUpdate();
			// 返回客户端
			this.syncActivityInfo(playerId, entity);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Result.success();
	}

	

	/** 抽奖
	 * @param playerId
	 * @param req
	 * @return
	 */
	public Result<?> rechargeWelfareLottery(String playerId, RechargeWelfareLotteryReq req, int protoType) {
		try {
			Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
			}
			RechargeWelfareEntity entity = opEntity.get();
			// 配置表
			RechargeWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
			//已经抽奖的次数
			int dailyLotteryTimes = entity.getDailyLotteryTimes();
			int count = req.getLotteryType().getNumber();
			if (dailyLotteryTimes >= kvCfg.getDailyTimesLimit()) {
				return Result.fail(Status.Error.RECHARGE_WELFARE_LOTTERY_TIMES_LIMIT_VALUE);
			}
			//times 只涉及消耗用, 其它用count
			int times = req.getLotteryType().getNumber();
			//免费次数只在抽一次的时候消耗
			if(req.getLotteryType().equals(Activity.RechargeLotteryType.ONE_NUM)){
				if(entity.getFreeTimes() == 1){
					times -= 1;
				}
			}
			List<RewardItem.Builder> consumeItemList = new ArrayList<>();
			//抽奖消耗
			int needBuyCount = getLotteryConsume(playerId, times, consumeItemList);
			//判断消耗
			boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.RECHARGE_WELFARE_LOTTERY_CONSUME);
			if (!success) {
				logger.error("GhostSecretActivity drew secret consume not enought, playerId: {}", playerId);
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
			//奖池
			List<Integer> itemSetList = entity.getItemsetList();
			//随机的奖励ID
			List<Integer> cfgIdList = getRandomCfgId(count, itemSetList);
			// 奖励
			List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
			// 随机到的奖励
			for(Integer cfgId : cfgIdList){
				RechargeWelfareRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareRewardCfg.class, cfgId);
				rewardList.addAll(rewardCfg.getRewardList());
				//跑马灯
				String playerName = getDataGeter().getPlayerName(playerId);
				if (rewardCfg.getPool() == 1) {
					this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.ACTIVITY_RECHARGE_WELFARE_LOTTERY_AWARD,null, playerName, rewardCfg.getReward());
				}
			}
			// 发固定
			this.getDataGeter().takeReward(playerId, kvCfg.getExtRewardList(), count, Action.RECHARGE_WELFARE_LOTTERY_REWARD, false);
			//发奖励
			if (!rewardList.isEmpty()) {
				ActivityReward reward = new ActivityReward(rewardList, Action.RECHARGE_WELFARE_LOTTERY_REWARD);
				reward.setOrginType(RewardOrginType.RECHARGE_WELFARE_LOTTERY_REWARD, getActivityId());
				reward.setAlert(true);
				postReward(playerId, reward, false);
			}
			//this.getDataGeter().takeReward(playerId, rewardList, 1, Action.RECHARGE_WELFARE_LOTTERY_REWARD,true, RewardOrginType.ACTIVITY_REWARD);
			
			//免费次数只在抽一次的时候消耗
			if(req.getLotteryType().equals(Activity.RechargeLotteryType.ONE_NUM)){
				if(entity.getFreeTimes() > 0){
					entity.setFreeTimes(0);
				}			
			}
			//每日抽奖次数
			entity.setDailyLotteryTimes(dailyLotteryTimes + count);
			entity.setLotteryTimes(entity.getLotteryTimes() + count);
			//update 
			entity.notifyUpdate();
			
			//抽奖事件
			ActivityManager.getInstance().postEvent(new RechargeWelfareLotteryEvent(playerId, entity.getLotteryTimes()));
			// 返回客户端
			RechargeWelfareLotteryResp.Builder lotteryResp = RechargeWelfareLotteryResp.newBuilder();
			lotteryResp.setLotteryType(req.getLotteryType());
			lotteryResp.addAllCfgIds(cfgIdList);
			//resp
			PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RECHARGE_WELFARE_LOTTERY_RESP, lotteryResp));
			//sync
			this.syncActivityInfo(playerId, entity);

		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return Result.success();
	}

	/**
	 * 领取充值点券
	 */
	public Result<?> receiveRechargeCoupon(String playerId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RechargeWelfareEntity entity = opEntity.get();
		
		RechargeWelfareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		//总的点券
		int totalCoupon = entity.getTotalCoupon();
	/*	int limitGainTimes = cfg.getDailyRechargeGainItemTimes();
		//已达上限无法领取
		if (totalCoupon >= limitGainTimes) {
			return Result.fail(Status.Error.RECHARGE_WELFARE_RECEIVE_COUPON_LIMIT_VALUE);
		}*/
		//已经领取的点券
		int receiveCoupon = entity.getReceiveCoupon();
		//可领取的点券
		int canRecCoupon = totalCoupon - receiveCoupon;
		//没有可领取的点券
		if (canRecCoupon <= 0) {
			return Result.fail(Status.Error.RECHARGE_WELFARE_RECEIVE_COUPON_NO_HAVE_VALUE);
		}
		//更新已领取点圈数
		entity.setReceiveCoupon(totalCoupon);
		//发送点券道具
		this.getDataGeter().takeReward(playerId,cfg.getDailyQuestRewardList(), canRecCoupon, Action.RECHARGE_WELFARE_RECEIVE_COUPON, true, RewardOrginType.ACTIVITY_REWARD);
		//sync
		syncActivityDataInfo(playerId);
		
		return Result.success();
	}
	
	/**
	 * 免费领取每日积分奖励点券
	 */
	public Result<?> receiveDailyScoreFreeCoupon(String playerId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RechargeWelfareEntity entity = opEntity.get();
		
		boolean isReceiveFree = entity.isFreeRec();
		//已经领取过
		if (isReceiveFree) {
			return Result.fail(Status.Error.RECHARGE_WELFARE_DAILY_RECEIVED_VALUE);
		}
		RechargeWelfareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		int dailyScore = entity.getDailyScore();
		//积分不满足
		if (dailyScore < cfg.getDailyQuestCondition()){
			return Result.fail(Status.Error.RECHARGE_WELFARE_DAILY_SCORE_NO_ENOUGH_VALUE);
		}
		
		this.getDataGeter().takeReward(playerId,cfg.getDailyQuestRewardList(), 1, Action.RECHARGE_WELFARE_RECEIVE_DAILY_COUPON, true, RewardOrginType.ACTIVITY_REWARD);
		entity.setFreeRec(true);
		entity.notifyUpdate();
		//sync
		syncActivityDataInfo(playerId);
		
		return Result.success();
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
		Optional<RechargeWelfareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		RechargeWelfareEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}

	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareAchieveCfg.class, achieveId);
		if (cfg == null) {
			cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareAchieveCfg.class, achieveId);
		}
		return cfg;
	}
	
	//初始化成就
	private void initAchieve(String playerId){
		Optional<RechargeWelfareEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		RechargeWelfareEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<RechargeWelfareAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeWelfareAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			RechargeWelfareAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		RechargeWelfareAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<RechargeWelfareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	@Override
	public Action takeRewardAction() {
		return Action.RECHARGE_WELFARE_TASK_REWARD;
	}
	
	/**
	 * 充值事件，活动期间充值，1元给一个物品
	 * @param event
	 */
	@Subscribe
	public void onEvent(DiamondRechargeEvent event) {
		String playerId = event.getPlayerId();
		//充值相关数据处理
		rechargeForCouponChange(playerId, event.getDiamondNum());
	}
	
	@Subscribe
	public void onEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		//充值相关数据处理
		rechargeForCouponChange(playerId, event.getDiamondNum());
	}
	
	@Subscribe
	public void onEvent(IDIPGmRechargeEvent event) {
		String playerId = event.getPlayerId();
		HawkLog.logPrintln("RechargeWelfareActivity rechargeCoupon on IDIPGmRechargeEvent, playerId: {}, rechargeDiamonds: {}", playerId, event.getDiamondNum());
		//充值相关数据处理
		rechargeForCouponChange(playerId, event.getDiamondNum());
	}
	
	
	@Subscribe
	public void onEvent(ShareProsperityEvent event) {
		String playerId = event.getPlayerId();
		HawkLog.logPrintln("RechargeWelfareActivity ShareProsperityEvent, playerId: {}, rechargeDiamonds: {}", playerId, event.getDiamondNum());
		rechargeForCouponChange(playerId, event.getDiamondNum());
	}
	
	
	/**根据充值钻石数计算点券数相关信息
	 * @param entity
	 * @param rechargeDiamon
	 */
	public void rechargeForCouponChange(String playerId, int rechargeDiamon){
		if (!isOpening(playerId)) {
			return;
		}
		Optional<RechargeWelfareEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		RechargeWelfareEntity entity = opEntity.get();
		
		RechargeWelfareActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		if (cfg == null) { 
			return;
		}
		//是否超上限
		int limit = cfg.getDailyRechargeGainItemLimit();
		int lastTotalDiamond = entity.getTotalDiamond(); 
		if (lastTotalDiamond >= limit) {
			return;
		}
		//相加后超上限则取上限相减后的余数
		int difDiamond = (lastTotalDiamond + rechargeDiamon) - limit;
		if (difDiamond > 0) {
			rechargeDiamon = limit - lastTotalDiamond;
		}
		int totalDiamond = entity.getTotalDiamond() + rechargeDiamon;
		//总的充值钻石数
		entity.setTotalDiamond(totalDiamond);
		
		int receiveDiamond = entity.getReceiveDiamond() + rechargeDiamon;
		//增加的点券数
		int addCoupon = receiveDiamond / cfg.getRechargeGainItem();
		//剩余的可累计的充值钻石数
		int leftReceive = receiveDiamond % cfg.getRechargeGainItem();
		
		entity.setReceiveDiamond(leftReceive);
		//update 
		int totalCoupon = entity.getTotalCoupon() + addCoupon;
		entity.setTotalCoupon(totalCoupon);
		entity.notifyUpdate();
		//sync
		syncActivityDataInfo(playerId);
		logger.info("RechargeWelfareActivity rechargeForCouponChange playerId:{},totalDiamond:{}, receiveDiamond:{}, addCoupon:{}, leftReceive:{}, totalCoupon:{}"
				,playerId, totalDiamond, receiveDiamond, addCoupon, leftReceive, totalCoupon );

	}
	
	/**每日任务积分事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(AddTavernScoreEvent event){
		String playerId = event.getPlayerId();
		if (!super.isOpening(playerId)) {
			return;
		}
		Optional<RechargeWelfareEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		RechargeWelfareEntity entity = optional.get();
		//更新积分
		int totalScore = event.getTotalScore();
		entity.setDailyScore(totalScore);
		entity.notifyUpdate();
		//push
		syncActivityDataInfo(playerId);
		
		logger.info("RechargeWelfareActivity AddTavernScoreEvent playerId:{}, dayth:{}, totalScore:{}", playerId, totalScore);
	}
	
	
	/**初始化奖励
	 * @return
	 */
	public List<Integer> getInitItemSet(){
		List<Integer> list = new ArrayList<>();
		ConfigIterator<RechargeWelfareRewardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(RechargeWelfareRewardCfg.class);
		while (configIterator.hasNext()) {
			RechargeWelfareRewardCfg cfg = configIterator.next();
			if (cfg.getDefaultChoose() == 1) {
				list.add(cfg.getId());
			}
		}
		return list;
	}
	
	/**检查奖励设置是否有效
	 * @param itemSetList
	 * @return
	 */
	public boolean checkItemSetValid(List<Integer> itemSetList){
		//判断是否有重复id
		boolean isRepeat = itemSetList.size() != new HashSet<>(itemSetList).size();
		if (isRepeat) {
			return false;
		}
		RechargeWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		Map<Integer, Integer> chooseItemMap = kvCfg.getPoolChooseItemsMap();
		Map<Integer, Integer> map = new HashMap<>();
		for (Integer cfgId : itemSetList) {
			RechargeWelfareRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareRewardCfg.class, cfgId);
			int pool = rewardCfg.getPool();
			if (!map.containsKey(pool)) {
				map.put(pool, 1);
			}else{
				map.put(pool, map.get(pool) + 1);
			}
		}
		for (Entry<Integer, Integer> entry : chooseItemMap.entrySet()) {
			int key = entry.getKey();
			int value = entry.getValue();
			if (!map.containsKey(key) || map.get(key) != value) {
				return false;
			}
		}
		return true;
	}
	
	/**获取活动随机奖励配置Id
	 * @param itemSetList
	 * @return
	 */
	public List<Integer> getRandomCfgId(int times, List<Integer> itemSetList){
		List<Integer> cfgIds = new ArrayList<>();
		Map<Integer, Integer> weightMap = new HashMap<>();
		for (Integer cfgId : itemSetList) {
			RechargeWelfareRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RechargeWelfareRewardCfg.class, cfgId);
			weightMap.put(cfgId, cfg.getWeight());
		}
		for (int i = 0; i < times; i++) {
			int cfgId = HawkRand.randomWeightObject(weightMap);
			cfgIds.add(cfgId);	
		}
		return cfgIds;
	}
	
	
	/**抽奖消耗
	 * @param playerId
	 * @param drewTimes
	 * @param consumeItemList
	 * @return
	 */
	private int getLotteryConsume(String playerId, int times, List<RewardItem.Builder> consumeItemList) {
		RechargeWelfareActivityKVCfg rechargeWelfareKVCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		//单次消耗
		RewardItem.Builder onceCounsumeItem = RewardHelper.toRewardItem(rechargeWelfareKVCfg.getItemOnce());
		int counsumItemId = onceCounsumeItem.getItemId();
		int haveDrewCount = this.getDataGeter().getItemNum(playerId, counsumItemId);
		
		int totalCount = times *  (int)onceCounsumeItem.getItemCount();
		//需要购买的次数
		int needBuyCount = totalCount - haveDrewCount;
		
		if (needBuyCount > 0) {
			RewardItem.Builder buyCounsumeItem = RewardHelper.toRewardItem(rechargeWelfareKVCfg.getItemPrice());
			buyCounsumeItem.setItemCount(buyCounsumeItem.getItemCount() * needBuyCount);
			consumeItemList.add(buyCounsumeItem);
			if (haveDrewCount > 0) {
				onceCounsumeItem.setItemCount(haveDrewCount);
				consumeItemList.add(onceCounsumeItem);
			}
		}else{
			onceCounsumeItem.setItemCount(totalCount);
			consumeItemList.add(onceCounsumeItem);
		}
		return needBuyCount;
	}
	
	
	
	/**PB协议构造
	 * @return
	 */
	public RechargeWelfareInfoSync.Builder genRechargeWelfareInfoPB(RechargeWelfareEntity entity){
		RechargeWelfareInfoSync.Builder builder = RechargeWelfareInfoSync.newBuilder();
		builder.addAllCfgIds(entity.getItemsetList());
		builder.setFreeTimes(entity.getFreeTimes());
		builder.setDailyLotteryTimes(entity.getDailyLotteryTimes());
		builder.setLotteryTimes(entity.getLotteryTimes());
		//每日充值获得点券信息
		CouponInfo.Builder conponBuilder = CouponInfo.newBuilder();
		
		int receiveCoupon = entity.getReceiveCoupon();
		conponBuilder.setReceiveCoupon(receiveCoupon);
		
		RechargeWelfareActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(RechargeWelfareActivityKVCfg.class);
		
		//sql总点券
		int totalCoupon = entity.getTotalCoupon();
	
		//领取的点券数
		conponBuilder.setAllCoupon(totalCoupon);
		
		//上限
		int maxRecharge = kvCfg.getDailyRechargeGainItemLimit();
		
		int totalDiamond = entity.getTotalDiamond() > maxRecharge ? maxRecharge:entity.getTotalDiamond();
		
		conponBuilder.setTodayRechargeRmb(totalDiamond /10);
		WelfareState state = WelfareState.NOT_COMPLETE;//未达成,获取更多
		if (totalCoupon > 0) {
			//有限判断是否能领取
			if(totalCoupon > receiveCoupon){//已达成未领取
				state = WelfareState.NOT_GET;
			}else{
				//不能领取判断是否达到充值上限
				if (totalDiamond >= maxRecharge) {//已达奖励上限
					state = WelfareState.GET_REWARD;
				}
			}
		}
		conponBuilder.setState(state);
		builder.setCouponInfo(conponBuilder.build());
		//每日积分获得点券信息
		CouponFreeInfo.Builder conponFreeBuilder = CouponFreeInfo.newBuilder();
		int todayScore = entity.getDailyScore();
		conponFreeBuilder.setTodayScore(todayScore);
		WelfareState freeState = WelfareState.NOT_COMPLETE;//未达成,获取更多
		boolean isReceiveFree = entity.isFreeRec();
		if (isReceiveFree) {
			freeState = WelfareState.GET_REWARD;
		}else{
			if (todayScore >= kvCfg.getDailyQuestCondition()) {
				freeState = WelfareState.NOT_GET;
			}
		}
		conponFreeBuilder.setState(freeState);
		
		builder.setCouponFreeInfo(conponFreeBuilder.build());
		return builder;
	}

}
