package com.hawk.activity.type.impl.goldBabyNew;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GoldBabyOnlineTimeEvent;
import com.hawk.activity.event.impl.GoldBabyPoolLevelUpEvent;
import com.hawk.activity.event.impl.HonourHeroReturnDailyLoginEvent;
import com.hawk.activity.event.impl.LoginDayGoldBabyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.parser.GoldBabyFindTimesEvent;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewCumulativeCfg;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewDailyAchieveCfg;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewKVCfg;
import com.hawk.activity.type.impl.goldBabyNew.cfg.GoldBabyNewRewardCfg;
import com.hawk.activity.type.impl.goldBabyNew.entity.GoldBabyNewEntity;
import com.hawk.activity.type.impl.goldBabyNew.entity.GoldBabyNewRewardPool;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.GoldBabyFindResp;
import com.hawk.game.protocol.Activity.GoldBabyInfoSyncPB;
import com.hawk.game.protocol.Activity.GoldBabyPoolPB;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 金币夺宝
 * @author fwj
 *
 */
public class GoldBabyNewActivity extends ActivityBase implements AchieveProvider{
	/**
	 * 奖池数量
	 */
	private static final int POOL_NUM = 3;
	
	/**
	 * 构造方法
	 * @param activityId
	 * @param activityEntity
	 */
	public GoldBabyNewActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.GOLD_BABY_NEW_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	/**
	 * 新建实例
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GoldBabyNewActivity activity = new GoldBabyNewActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	/**
	 * 获取Entity
	 */
	@Override
	protected HawkDBEntity loadFromDB(java.lang.String playerId, int termId) {
		List<GoldBabyNewEntity> queryList = HawkDBManager.getInstance().query("from GoldBabyNewEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GoldBabyNewEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建Entity
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GoldBabyNewEntity entity = new GoldBabyNewEntity(playerId, termId);
		return entity;
	}
	
	/**
	 * 活动开启
	 */
	@Override
	public void onOpen() {
		// 在线玩家初始化活动,并且抛到玩家线程处理
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.GOLD_BABY_NEW_INIT, () -> {
				this.initDataInfo(playerId);
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	/**
	 * 初始化数据
	 * @param playerId
	 */
	private void initDataInfo(String playerId) {
		//初始化成就数据
		initAchieveInfo(playerId);
		
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		GoldBabyNewEntity entity = optional.get();
		
		//已初始化则直接返回
		if (entity.getPoolList().size() > 0) {
			return;
		}
		
		//设置抽取次数为0
		entity.setFindTimes(0);
		
		//初始化已购买次数
		entity.setBuyTimes(0);
		
		//初始化奖池数据
		for (int i = 1; i <= POOL_NUM; i++) {
			GoldBabyNewRewardPool rewardPool = new GoldBabyNewRewardPool();
			rewardPool.setPoolId(i);
			rewardPool.setFindOver(0);
			rewardPool.setPoolLevel(1);
			rewardPool.setResetTimes(0);
			rewardPool.setTimes(0);
			rewardPool.setLockTopGrade(0);
			rewardPool.setRandomRewards();
			entity.addPool(rewardPool);
		}
	}
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}	
		String playerId = event.getPlayerId();
		
		// 取Entity
		Optional<GoldBabyNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GoldBabyNewEntity entity = opEntity.get();
		entity.setLastLoginTime(HawkTime.getMillisecond());
		
        //抛每日登陆成就事件
        ActivityManager.getInstance().postEvent(new HonourHeroReturnDailyLoginEvent(playerId, 1), true);
        
		// 判断跨天
		long lastRefreshTime = entity.getRefreshTime();
		long nowTime = HawkTime.getMillisecond();
		if (!HawkTime.isSameDay(lastRefreshTime, nowTime)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(HawkTime.getMillisecond());
			//重置每日成就
			entity.resetDailyAchieve();
			//发送累计登陆事件
			ActivityManager.getInstance().postEvent(new LoginDayGoldBabyEvent(playerId, entity.getLoginDays()), true);
		}
		
		// 同步天数
		syncActivityDataInfo(playerId);
	}
	
	/**
	 * 玩家登录
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		
		Optional<GoldBabyNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		GoldBabyNewEntity entity = opEntity.get();
		
		entity.setLastLoginTime(HawkTime.getMillisecond());
	
		syncActivityDataInfo(playerId);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		// 活动已经通过GM关闭
		if(isInvalid()){
			return;
		}
		
		// 活动未开启
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<GoldBabyNewEntity> optional=getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		GoldBabyNewEntity entity=optional.get();
		
		//未初始化则先初始化
		if(entity.getPoolList().size() == 0){
			this.initDataInfo(playerId);
		}
		
		GoldBabyInfoSyncPB.Builder builder = GoldBabyInfoSyncPB.newBuilder();
		builder.setBuyTimes(entity.getBuyTimes());
		builder.setFindTimes(entity.getFindTimes());
		for(GoldBabyNewRewardPool rewardPool:entity.getPoolList()){
			GoldBabyPoolPB.Builder itemBuilder = GoldBabyPoolPB.newBuilder();
			itemBuilder.setPoolId(rewardPool.getPoolId());
			itemBuilder.setPoolLevel(rewardPool.getPoolLevel());
			itemBuilder.setResetTimes(rewardPool.getResetTimes());
			itemBuilder.setTimes(rewardPool.getTimes());
			itemBuilder.setFindOver(rewardPool.getFindOver());
			itemBuilder.setLockTopGrade(rewardPool.getLockTopGrade());
			itemBuilder.addAllRewardIds(rewardPool.getRewardIds());
			builder.addPools(itemBuilder);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code2.GOLD_BABY_NEW_INFO_SYNC_VALUE, builder));
	}

	/**
	 * 锁定最高档
	 * @param playerId
	 * @param poolId
	 * @return
	 */
	public Result<?> lockTopGrade(String playerId,int poolId){
		Optional<GoldBabyNewEntity> optional=getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			HawkLog.errPrintln("goldbayNew lockTopGrade failed, data error, playerId: {}, giftId: {}", playerId, poolId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		GoldBabyNewEntity entity=optional.get();
		
		//锁定最高档	
		GoldBabyNewRewardPool pool = entity.getPoolById(poolId);
		pool.setLockTopGrade(pool.getLockTopGrade() ^ 1);
		HawkLog.logPrintln("goldbabyNew lockTopGrade, playerId: {}, isLock： {}", playerId, pool.getLockTopGrade());
		//同步
		syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	
	/**
	 * 购买道具
	 * @param playerId
	 * @param count
	 * @return
	 */
	public Result<?> buyTicket(String playerId, int count){
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			HawkLog.errPrintln("goldbayNew buyTicket failed, data error, playerId: {}", playerId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		GoldBabyNewEntity entity=optional.get();
		if (entity==null) {
			HawkLog.errPrintln("goldbayNew buyTicket failed, data error, playerId: {}", playerId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		GoldBabyNewKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(GoldBabyNewKVCfg.class);
		
		//判断剩余购买次数
		int limit = cfg.getLimit();
		int buyTimes = entity.getBuyTimes();
		if (count + buyTimes > limit) {
			HawkLog.errPrintln("goldbayNew buyTicket buyTimes error, data error, playerId： {},  buyTimes: {}", playerId, count + buyTimes);
			return Result.fail(Status.Error.GOLD_BABY_BUY_TICKET_ERROR_VALUE);
		}
		//设置已购买次数
		entity.setBuyTimes(count + buyTimes);
		
		String price = cfg.getPrice();
		List<RewardItem.Builder> costList = RewardHelper.toRewardItemImmutableList(price);
		boolean flag = this.getDataGeter().cost(playerId, costList, count, Action.GOLD_BABY_NEW_BUY_COST, false);
		//金币不够
		if (!flag) {
			HawkLog.errPrintln("goldbayNew buyTicket consume failed, playerId: {}", playerId);
			return Result.fail(Status.Error.GOLD_BABY_BUY_TICKET_COST_ERROR_VALUE);
		}
		
		//发货
		String item = cfg.getItem();
		List<RewardItem.Builder> itemList = RewardHelper.toRewardItemImmutableList(item);
		itemList.get(0).setItemCount(count);
		this.getDataGeter().takeReward(playerId,itemList,Action.GOLD_BABY_NEW_BUY_ITEM,true);
		HawkLog.logPrintln("goldbabyNew buyTicket, playerId: {}, buyCount: {}", playerId, count);
		//同步信息
		syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	/**
	 * 请求页面获取信息
	 * @param playerId
	 * @return
	 */
	public Result<?> onInfoReq(String playerId){
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			HawkLog.errPrintln("goldbayNew infoReq failed, data error, playerId: {}", playerId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		GoldBabyNewEntity entity=optional.get();
		//发送累计在线事件
		ActivityManager.getInstance().postEvent(new GoldBabyOnlineTimeEvent(playerId,entity.getLastLoginTime()));
		entity.setLastLoginTime(HawkTime.getMillisecond());
		
		//向客户端发送成就数据
		AchievePushHelper.pushAchieveInfo(playerId, entity.getAchieveItemList());
		//同步信息
		syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	/**
	 * 搜寻
	 * @param playerId
	 * @param poolId
	 * @return
	 */
	public Result<?> findReward(String playerId, int poolId){
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			HawkLog.errPrintln("goldbayNew findReward failed, data error, playerId: {}", playerId);
			return Result.fail(Status.SysError.DATA_ERROR_VALUE);
		}
		GoldBabyNewEntity entity=optional.get();
			
		// 奖池抽完了，重置次数用完
		GoldBabyNewRewardPool pool= entity.getPoolById(poolId);
		if (pool == null || pool.getFindOver() == 1) {
			HawkLog.errPrintln("goldbayNew findReward failed, pool find over or pool is null, playerId: {}", playerId);
			return Result.fail(Status.Error.GOLD_BABY_FIND_ERROR_VALUE);
		}

		// 抽取奖励配置
		GoldBabyNewRewardCfg cfg = null;
		if (pool.getLockTopGrade() == 0){
			List<GoldBabyNewRewardCfg> cfgs = HawkConfigManager.getInstance().getConfigIterator(GoldBabyNewRewardCfg.class).toList();
			cfgs= cfgs.stream().filter( i-> pool.getRewardIds().contains(i.getId())).collect(Collectors.toList());
			cfg = HawkRand.randomWeightObject(cfgs);
		} else{
			cfg = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewRewardCfg.class, pool.getRewardIds().get(pool.getRewardIds().size() - 1));
		}
		
		// 消耗
		GoldBabyNewKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GoldBabyNewKVCfg.class);
		RewardItem.Builder costItem = null;
		if (pool.getLockTopGrade() == 0) {
			costItem = kvCfg.getFindCostByIdAndTimes(poolId, pool.getTimes());
		}else{
			costItem = kvCfg.getLockCostByIdAndTimes(poolId, pool.getTimes());
		}
		boolean flag = getDataGeter().cost(playerId, Arrays.asList(costItem), Action.GOLD_BABY_NEW_FIND_COST);
		if (!flag) {
			HawkLog.errPrintln("goldbayNew findReward failed, ticket not enough, playerId: {}", playerId);
			return Result.fail(Status.Error.GOLD_BABY_FIND_COST_ERROR_VALUE);
		}
		
		// 获取倍率并乘以奖励数量
		int magnification = getRandMagnification(cfg);
		
		// 发奖
		long count = cfg.getRewardList().get(0).getItemCount();
		ActivityReward reward = new ActivityReward(cfg.getRewardList(), Action.GOLD_BABY_NEW_FIND_REWARD);
		reward.getRewardList().get(0).setItemCount(count * magnification / 100);
		postReward(playerId, reward);
		HawkLog.logPrintln("goldbabyNew findReward, playerId: {}, poolId: {}, poolTimes: {}, poolLevel: {}, reward: {}, magnification: {}",
				playerId, poolId, pool.getTimes(),pool.getPoolLevel(),cfg.getRewards(),magnification);
		// 数据更新
		entity.setFindTimes(entity.getFindTimes()+1);
		poolLevelUp(pool);
		entity.notifyUpdate();
		//发送搜寻事件
		ActivityManager.getInstance().postEvent(new GoldBabyFindTimesEvent(playerId, entity.getFindTimes()));
		
		// 推送搜寻结果
		GoldBabyFindResp.Builder builder = GoldBabyFindResp.newBuilder();
		builder.setItemId(cfg.getId());
		builder.setMagnification(magnification);
		builder.setPoolId(poolId);
		builder.setIsLock(pool.getLockTopGrade());
		pushToPlayer(playerId, HP.code2.GOLD_BABY_NEW_FIND_RESP_VALUE, builder);
		
		// 同步界面信息
		syncActivityDataInfo(playerId);
		this.getDataGeter().logGoldBabyFindReward(playerId, getActivityType(),poolId, pool.getLockTopGrade(), (int)costItem.getItemCount(), (int)count, magnification);
		return Result.success();
	}
	@Override
	public void onPlayerLogout(String playerId) {
		Optional<GoldBabyNewEntity> optional=getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		GoldBabyNewEntity entity=optional.get();
		ActivityManager.getInstance().postEvent(new GoldBabyOnlineTimeEvent(playerId,entity.getLastLoginTime()));
	}
	/**
	 * 随机奖品倍率
	 * @param cfg
	 * @return
	 */
	private int getRandMagnification(GoldBabyNewRewardCfg cfg) {
		String magnification1 = cfg.getMagnification1();
		String magnification2 = cfg.getMagnification2();
		String[] list1 = magnification1.split(SerializeHelper.ATTRIBUTE_SPLIT);
		String[] list2 = magnification2.split(SerializeHelper.ATTRIBUTE_SPLIT);
		java.util.Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		map.put(Integer.valueOf(list1[0]), Integer.valueOf(list1[1]));
		map.put(Integer.valueOf(list2[0]), Integer.valueOf(list2[1]));
		return HawkRand.randomWeightObject(map);
	}
	
	/**
	 * 奖池升级
	 * @param pool
	 */
	private void poolLevelUp(GoldBabyNewRewardPool pool){
		//抽完了直接返回
		if (pool.getFindOver()>0) {
			return;
		}
		
		GoldBabyNewKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(GoldBabyNewKVCfg.class);
		//该奖池可抽取次数
		int poolFindTimes = kvCfg.getPoolFindTimesById(pool.getPoolId());
		//奖池可重置次数
		int maxResetTimes = kvCfg.getResetTimesById(pool.getPoolId());
		
		//当前小于可抽取次数则 ：等级+1，次数+1
		if(pool.getTimes()<poolFindTimes){
			pool.setTimes(pool.getTimes()+1);
			pool.setPoolLevel(pool.getPoolLevel()+1);
		}
		if (pool.getTimes() < poolFindTimes) {
			pool.setRandomRewards();
		}
		//抽取次数到达最大，若有重置次数则自动重置，否则设置表示不可再抽
		if (pool.getTimes() == poolFindTimes) {
			if (pool.getResetTimes()<maxResetTimes) {
				pool.setTimes(0);
				pool.setPoolLevel(1);
				pool.setResetTimes(pool.getResetTimes()+1);	
				pool.setRandomRewards();
			}else{
				pool.setFindOver(1);
			}
		}
		
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
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		GoldBabyNewEntity entity = optional.get();
		if (entity.getAchieveItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		AchieveItems achieveItems = new AchieveItems(entity.getAchieveItemList(), entity);
		return Optional.of(achieveItems);
	}
	
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	public void initAchieveInfo(String playerId){
		Optional<GoldBabyNewEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		
		GoldBabyNewEntity entity = optional.get();
		
		//成就是否已经初始化
		if (!entity.getAchieveItemList().isEmpty()) {
			return;
		}

		//初始化成就项
		ConfigIterator<GoldBabyNewDailyAchieveCfg> cIterator = HawkConfigManager.getInstance().getConfigIterator(GoldBabyNewDailyAchieveCfg.class);
		while (cIterator.hasNext()) {
			GoldBabyNewDailyAchieveCfg next = cIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addAchieveItems(item);
		}		
		ConfigIterator<GoldBabyNewCumulativeCfg> cIterator1 = HawkConfigManager.getInstance().getConfigIterator(GoldBabyNewCumulativeCfg.class);
		while (cIterator1.hasNext()) {
			GoldBabyNewCumulativeCfg next = cIterator1.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addAchieveItems(item);
		}
		
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getAchieveItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayGoldBabyEvent(playerId, 1), true);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewDailyAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewCumulativeCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.GOLD_BABY_NEW_ACHIEVE_REWARD;
	}
	
	/**
	 * 获取成就奖励
	 */
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		GoldBabyNewDailyAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewDailyAchieveCfg.class, achieveId);
		if (achieveCfg == null) {			
			GoldBabyNewCumulativeCfg cumulativeCfg = HawkConfigManager.getInstance().getConfigByKey(GoldBabyNewCumulativeCfg.class, achieveId);
			if (cumulativeCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
}

