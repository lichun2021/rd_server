package com.hawk.activity.type.impl.timeLimitBuy;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.timeLimitBuy.cfg.TimeLimitBuyGoodsCfg;
import com.hawk.activity.type.impl.timeLimitBuy.cfg.TimeLimitBuyKVCfg;
import com.hawk.activity.type.impl.timeLimitBuy.entity.TimeLimitBuyEntity;
import com.hawk.game.protocol.ActivityTimeLimitBuy.TimeLimitBuyInfo;
import com.hawk.game.protocol.ActivityTimeLimitBuy.TimeLimitBuyPageInfoResp;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 限时抢购
 * 
 * @author Golden
 *
 */
public class TimeLimitBuyActivity extends ActivityBase {

	/**
	 * 物品redis存储key值 为了避免key值太长或重复,用了活动id:274作为key值的一部分
	 */
	private static final String GOODS_KEY = "activity274_goods";
	
	/**
	 * 注水
	 */
	private static final String WATER_FLOOD_KEY = "activity274_waterflood";

	/**
	 * 真正购买数量
	 */
	private static final String REAL_BUY = "activity274_real_buy";
			
	/**
	 * 已经购买的物品信息 整个大区共享,通过redis做同步 redis同步间隔500ms一次
	 */
	private Map<Integer, Integer> goodsBuy = new ConcurrentHashMap<>();

	/**
	 * 本服购买的信息，周期500ms落地到redis一次，避免redis qps过高
	 */
	private Map<Integer, Integer> goodsBuyToBeWrite = new ConcurrentHashMap<>();
	
	/**
	 * 上一次刷新全服购买数量的时间
	 */
	private long lastResetGoodsBuyTime = 0L;
	
	/**
	 * 上一次注水时间
	 */
	private long lastWaterFlood = 0L;
	
	/**
	 * 是否已经跑马灯
	 */
	private boolean hasNotice = false;
	
	/**
	 * 构造方法
	 * 
	 * @param activityId
	 * @param activityEntity
	 */
	public TimeLimitBuyActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 获取活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.TIME_LIMIT_BUY;
	}

	/**
	 * 新建实例
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		TimeLimitBuyActivity activity = new TimeLimitBuyActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	/**
	 * 从DB加载
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<TimeLimitBuyEntity> queryList = HawkDBManager.getInstance()
				.query("from TimeLimitBuyEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			TimeLimitBuyEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建DB数据实体
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		TimeLimitBuyEntity entity = new TimeLimitBuyEntity(playerId, termId);
		return entity;
	}

	/**
	 * 推界面信息,根据客户端请求的turnId返回
	 * 
	 * @param playerId
	 * @param turnId
	 */
	public void pushPageInfo(String playerId, int turnId) {

		// 玩家db数据
		Optional<TimeLimitBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		TimeLimitBuyEntity entity = opEntity.get();

		TimeLimitBuyPageInfoResp.Builder builder = TimeLimitBuyPageInfoResp.newBuilder();
		builder.setTurnId(turnId);
		builder.setCloseRemind(entity.getCloseRemind() != 0);

		List<TimeLimitBuyGoodsCfg> goodsCfgs = getGoodsCfgs(turnId);
		for (TimeLimitBuyGoodsCfg goodsCfg : goodsCfgs) {
			// 不是当前大区的物品
			if (!getDataGeter().getAreaId().equals(goodsCfg.getAreaId())) {
				continue;
			}
			
			TimeLimitBuyInfo.Builder goodsInfo = TimeLimitBuyInfo.newBuilder();
			int goodsId = goodsCfg.getGoodsId();
			goodsInfo.setGoodsId(goodsId);
			goodsInfo.setOwnBuyCount(entity.getBuyTimes(goodsId));

			TimeLimitBuyGoodsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitBuyGoodsCfg.class, goodsId);
			int allBuyCount = Math.min(cfg.getAwardBuyLimit(), getGoodsBuyCount(goodsId));
			goodsInfo.setAllBuyCount(allBuyCount);

			builder.addGoodsInfo(goodsInfo);
		}

		pushToPlayer(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_PAGE_RESP_VALUE, builder);
	}

	/**
	 * 抢购
	 * 
	 * @param playerId
	 * @param goodsId
	 */
	public void buy(String playerId, int goodsId) {
		// 活动未开启
		if (!isOpening(playerId)) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.ACTIVITY_NOT_OPEN_VALUE);
			return;
		}

		// 轮次未开启
		if (!inTurn()) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.TIME_LIMIT_BUY_NOT_IN_TURN_VALUE);
			return;
		}

		// 不是当前轮次
		TimeLimitBuyGoodsCfg cfg = HawkConfigManager.getInstance().getConfigByKey(TimeLimitBuyGoodsCfg.class, goodsId);
		if (getCurrentTurnId() != cfg.getTurn()) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.TIME_LIMIT_BUY_NOT_CURRENT_TURN_VALUE);
			return;
		}
		
		// 物品已经被抢光
		int allBuyCount = getGoodsBuyCount(goodsId);
		if (allBuyCount >= cfg.getAwardBuyLimit()) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.TIME_LIMIT_BUY_GOODS_HAVE_NO_VALUE);
			getDataGeter().logTimeLimitBuy(playerId, goodsId, 0);
			pushPageInfo(playerId, cfg.getTurn());
			return;
		}
		
		// 不是当前大区的物品
		if (!getDataGeter().getAreaId().equals(cfg.getAreaId())) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.TIME_LIMIT_BUY_AREAID_ERROR_VALUE);
			return;
		}
		
		// 玩家db数据
		Optional<TimeLimitBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			sendErrorAndBreak(playerId, HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Status.Error.TIME_LIMIT_BUY_OWN_LIMIT_VALUE);
			return;
		}
		TimeLimitBuyEntity entity = opEntity.get();
		
		// 个人购买数量达到上限
		if (entity.getBuyTimes(goodsId) >= cfg.getPersonalBuyLimit()) {
			pushPageInfo(playerId, cfg.getTurn());
			return;
		}
		
		// 消耗
		boolean consume = getDataGeter().consumeItems(playerId, cfg.getConsume(), HP.code.ACTIVITY_TIME_LIMIT_BUY_VALUE, Action.TIME_LIMIT_BUY_ACTION);
		if (!consume) {
			return;
		}
		
		// 更新个人购买数量
		entity.addBuy(goodsId);
		
		// 更新全服购买数量
		addGoodsBuy(goodsId);
		
		// 推送奖励
		ActivityReward reward = new ActivityReward(cfg.getReward(), Action.TIME_LIMIT_BUY_ACTION);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward, false);
		
		// 推界面信息
		pushPageInfo(playerId, cfg.getTurn());
		
		getDataGeter().logTimeLimitBuy(playerId, goodsId, 1);
		
		// 添加真正购买数量，仅用于记录
		addRealBuy(goodsId);
		
		logger.info("activity timeLimitBuy action, playerId:{}, goodsId:{}, allBuyCount:{}", playerId, goodsId, getGoodsBuyCount(goodsId));
	}

	/**
	 * 关闭提示
	 * 
	 * @param playerId
	 */
	public void closeRemind(String playerId, int turnId) {
		// 玩家db数据
		Optional<TimeLimitBuyEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		TimeLimitBuyEntity entity = opEntity.get();
		
		// 更改开关
		int remind = entity.getCloseRemind();
		entity.setCloseRemind(remind == 1 ? 0 : 1);
		
		// 推界面
		pushPageInfo(playerId, turnId);
	}
	
	/**
	 * 获取当前场次的物品配置
	 * 
	 * @param turnId
	 */
	private List<TimeLimitBuyGoodsCfg> getGoodsCfgs(int turnId) {
		List<TimeLimitBuyGoodsCfg> cfgs = new ArrayList<>();
		ConfigIterator<TimeLimitBuyGoodsCfg> cfgIter = HawkConfigManager.getInstance()
				.getConfigIterator(TimeLimitBuyGoodsCfg.class);
		while (cfgIter.hasNext()) {
			TimeLimitBuyGoodsCfg cfg = cfgIter.next();
			if (cfg.getTurn() != turnId) {
				continue;
			}
			cfgs.add(cfg);
		}
		return cfgs;
	}

	/**
	 * 活动帧更新
	 */
	public void onQuickTick() {

		// 定时从redis拉取已经被抢购的物品信息
		resetGoodsBuy();
		
		checkNotice();
	}
	
	/**
	 * 获取当前轮次
	 * 
	 */
	private int getCurrentTurnId() {
		int turn = 0;

		// 活动开始和结束时间
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long endTime = getTimeControl().getEndTimeByTermId(termId);

		// 活动一共开x天
		int betweenDays = HawkTime.calcBetweenDays(new Date(startTime), new Date(endTime));
		for (int i = 0; i <= betweenDays; i++) {

			// 当天的0点
			long startAM0Time = HawkTime.getAM0Date(new Date(startTime + i * ActivityConst.DAY_MILL_SECOND)).getTime();

			// 轮次几点开始
			List<Integer> openTimeList = TimeLimitBuyKVCfg.getInstance().getOpenTimeList();
			for (int openHour : openTimeList) {
				// 轮次开始时间
				long turnOpenTime = startAM0Time + openHour * ActivityConst.HOUR_MILL_SECOND;

				// 未到活动开启时间/已经超过活动结束时间
				if (turnOpenTime < startTime || turnOpenTime > endTime) {
					continue;
				}

				// 超过轮次开始时间，则轮次+1
				long currentTime = HawkTime.getMillisecond();
				if (currentTime >= turnOpenTime) {
					turn++;
				}
			}
		}

		return turn;
	}

	/**
	 * 是否在轮次进行中
	 * 
	 * @return
	 */
	private boolean inTurn() {
		if (!isOpening(null)) {
			return false;
		}
		return turnAlreadyOpenTime(HawkTime.getMillisecond()) > 0L;
	}

	/**
	 * 本轮次已经开启多久了(ms)
	 * 
	 * @return
	 */
	private long turnAlreadyOpenTime(long currentTime) {

		// 活动开始和结束时间
		int termId = getActivityTermId();
		long startTime = getTimeControl().getStartTimeByTermId(termId);
		long endTime = getTimeControl().getEndTimeByTermId(termId);

		// 活动一共开x天
		int betweenDays = HawkTime.calcBetweenDays(new Date(startTime), new Date(endTime));
		for (int i = 0; i <= betweenDays; i++) {

			// 当天的0点
			long startAM0Time = HawkTime.getAM0Date(new Date(startTime + i * ActivityConst.DAY_MILL_SECOND)).getTime();

			// 轮次几点开始
			List<Integer> openTimeList = TimeLimitBuyKVCfg.getInstance().getOpenTimeList();
			for (int openHour : openTimeList) {

				// 轮次开始/结束时间
				long turnOpenTime = startAM0Time + openHour * ActivityConst.HOUR_MILL_SECOND;
				long turnEndTime = startAM0Time + openHour * ActivityConst.HOUR_MILL_SECOND
						+ TimeLimitBuyKVCfg.getInstance().getContinueTime();

				// 未到活动开启时间/已经超过活动结束时间
				if (turnOpenTime < startTime || turnOpenTime > endTime) {
					continue;
				}

				// 在轮次进行时间内
				if (currentTime >= turnOpenTime & currentTime <= turnEndTime) {
					return currentTime - turnOpenTime;
				}
			}
		}

		return 0L;
	}

	/**
	 * 增加抢购数量
	 * 
	 * @param goodsId
	 */
	private synchronized void addGoodsBuy(int goodsId) {
		int beforeCount = goodsBuy.getOrDefault(goodsId, 0);
		goodsBuy.put(goodsId, beforeCount + 1);

		int beforeToBeCount = goodsBuyToBeWrite.getOrDefault(goodsId, 0);
		goodsBuyToBeWrite.put(goodsId, beforeToBeCount + 1);
	}

	/**
	 * 获取全服物品购买数量
	 * 
	 * @param goodsId
	 * @return
	 */
	private int getGoodsBuyCount(int goodsId) {
		return goodsBuy.getOrDefault(goodsId, 0);
	}

	/**
	 * 重置已经购买的物品
	 */
	private synchronized void resetGoodsBuy() {
		if (!isOpening(null)) {
			return;
		}
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastResetGoodsBuyTime < TimeLimitBuyKVCfg.getInstance().getResetGoodsBuyPeroid()) {
			return;
		}
		lastResetGoodsBuyTime = currentTime;

		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		// 500ms内的购买数据写到redis里，然后清空
		for (Entry<Integer, Integer> toBeWrite : goodsBuyToBeWrite.entrySet()) {
			int goodsId = toBeWrite.getKey();
			int value = toBeWrite.getValue();
			// 更新redis
			redisSession.hIncrBy(getGoodsKey(), String.valueOf(goodsId), value);
		}
		goodsBuyToBeWrite = new ConcurrentHashMap<>();
		
		
		// 500ms从redis同步数据到本地
		Map<String, String> infos = redisSession.hGetAll(getGoodsKey());
		Map<Integer, Integer> newGoodsBuy = new ConcurrentHashMap<>();
		if (infos != null) {
			for (Entry<String, String> info : infos.entrySet()) {
				newGoodsBuy.put(Integer.valueOf(info.getKey()), Integer.valueOf(info.getValue()));
			}
		}
		this.goodsBuy = newGoodsBuy;
		
		// 计算注水
		doWaterFlood();
	}

	/**
	 * 获取redis key
	 * 
	 * @return
	 */
	private String getGoodsKey() {
		int termId = getActivityTermId();
		return GOODS_KEY + ":" + termId;
	}
	
	/**
	 * 注水
	 */
	private void doWaterFlood() {
		if (!inTurn()) {
			return;
		}
		long currentTime = HawkTime.getMillisecond();
		if (currentTime - lastWaterFlood < 5000L) {
			return;
		}
		lastWaterFlood = currentTime;
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		
		// 抢锁
		boolean getLock = redisSession.hSetNx(WATER_FLOOD_KEY, "lock", currentTime + "") > 0;
		if (!getLock) {
			String timeString = redisSession.hGet(WATER_FLOOD_KEY, "lock");
			if (HawkOSOperator.isEmptyString(timeString)) {
				return;
			}
			if (currentTime- Long.parseLong(timeString) > TimeLimitBuyKVCfg.getInstance().getWaterFloodPeroid()) {
				redisSession.hDel(WATER_FLOOD_KEY, "lock");
			}
			return;
		}
		redisSession.expire(WATER_FLOOD_KEY, TimeLimitBuyKVCfg.getInstance().getWaterFloodPeroid() / 1000);
		
		// 本轮此已经开启的时间
		long turnAlreadyOpenTime = turnAlreadyOpenTime(HawkTime.getMillisecond());
		
		int turnId = getCurrentTurnId();
		List<TimeLimitBuyGoodsCfg> goodsCfgs = getGoodsCfgs(turnId);
		for (TimeLimitBuyGoodsCfg cfg : goodsCfgs) {
			
			// 不是当前大区的物品
			if (!getDataGeter().getAreaId().equals(cfg.getAreaId())) {
				continue;
			}
			
			int goodsId = cfg.getGoodsId();
			
			// 当前数量
			int currentCount = goodsBuy.getOrDefault(goodsId, 0);
			
			// 目标时间
			long tarTime = 0L;
			int tarTimeIndex = -1;
			for (int i = 0; i < cfg.getWaterFloodTime().size(); i++) {
				long time = cfg.getWaterFloodTime().get(i);
				if (turnAlreadyOpenTime < time) {
					tarTimeIndex = i;
					tarTime = time;
					break;
				}
			}
			
			if (tarTimeIndex < 0) {
				continue;
			}
			
			// 目标数量
			int tarCount = cfg.getWaterFloodValue().get(tarTimeIndex);
			if (currentCount >= tarCount) {
				continue;
			}
			
			// 当前时间节点应该达到的数量
			int addCount = (int)((tarCount - currentCount) * 5000L / (tarTime - turnAlreadyOpenTime));
			
			if (addCount > 0) {
				int beforeToBeCount = goodsBuyToBeWrite.getOrDefault(goodsId, 0);
				goodsBuyToBeWrite.put(goodsId, beforeToBeCount + addCount);
				logger.info("activity timeLimitBuy water flood, goodsId:{}, turnAlreadyOpenTime:{}, currentCount:{}, tarTime:{}, tarCount:{}, addCount:{}",
						goodsId, turnAlreadyOpenTime, currentCount, tarTime, tarCount, addCount);
				
				getDataGeter().logTimeLimitBuyWater(goodsId, addCount);
			}
		}
	}
	
	public void checkNotice() {
		if (!isOpening(null) || inTurn()) {
			hasNotice = false;
			return;
		}
		
		if (hasNotice) {
			return;
		}
		
		long currentTime = HawkTime.getMillisecond();
		long preheatTime = TimeLimitBuyKVCfg.getInstance().getPreheatTime();
		
		boolean nowOpen = turnAlreadyOpenTime(currentTime) > 0L;
		boolean afterOpen = turnAlreadyOpenTime(currentTime + preheatTime) > 0L;
		if (!nowOpen && afterOpen) {
			addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.TIME_LIMIT_BUY, null, preheatTime / 1000 / 60);
			hasNotice = true;
		}
	}
	
	/**
	 * 添加真正购买数量，仅用于记录
	 * @param goodsId
	 */
	private void addRealBuy(int goodsId) {
		int termId = getActivityTermId();
		String key1 = REAL_BUY + ":" + termId;
		
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		redisSession.hIncrBy(key1, String.valueOf(goodsId), 1);
	}
}
