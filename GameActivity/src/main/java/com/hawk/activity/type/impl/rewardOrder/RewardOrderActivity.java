package com.hawk.activity.type.impl.rewardOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import org.hawk.collection.ConcurrentHashSet;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
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
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderActivityCfg;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderCfg;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderTaskCfg;
import com.hawk.activity.type.impl.rewardOrder.cfg.RewardOrderTimeCfg;
import com.hawk.activity.type.impl.rewardOrder.entity.RewardOrder;
import com.hawk.activity.type.impl.rewardOrder.entity.RewardOrderEntity;
import com.hawk.activity.type.impl.rewardOrder.entity.RewardOrderTask;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.rewardOrder;
import com.hawk.game.protocol.Activity.rewardOrderInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/***
 * 盟军悬赏令
 * @author yang.rao
 *
 */
public class RewardOrderActivity extends ActivityBase implements AchieveProvider {
	
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	/** 悬赏令个数 **/
	private final int REWARD_ORDER_SIZE = 4;
	
	/** 领取了悬赏令的玩家集合(只包含未完成，完成或者失败的，需要移除玩家) **/
	private ConcurrentHashSet<String> receivePlayers = new ConcurrentHashSet<>();

	public RewardOrderActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			RewardOrderEntity entity = opEntity.get();
			RewardOrder order = entity.getOrder();
			receivePlayers.add(playerId);
			if (order != null) {
				long curTime = HawkTime.getMillisecond();
				order.onTick(curTime, entity);
				if (order.fail()) {
					// 通知玩家悬赏令失败
					entity.orderFail();
					addRandomOrder(entity);
					pushResult(playerId, entity);
					entity.notifyUpdate();
				}
				if (entity.systemFresh(curTime)) {
					refreshOrder(entity, true, true);
					setNextRefreshTime(entity.getNextFreshTime(), entity);
				}
			}
		}
	}
	
	
	
	@Override
	public void onOpen() {
		Set<String> onlinePlayers = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayers){
			receivePlayers.add(playerId);
		}
	}

	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		RewardOrderActivityCfg kvCfg = RewardOrderActivityCfg.getInstance();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		RewardOrderEntity entity = opEntity.get();
		// 不需要跨天重置
		entity.setFinishCnt(0);
		if (kvCfg.isReset()) {
			entity.setRefreshCnt(0);
		}
	}
	

	/***
	 * 请求悬赏令界面信息
	 * @param playerId
	 * @return
	 */
	public Result<?> reqRewardOrderInfo(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RewardOrderEntity entity = opEntity.get();
		rewardOrderInfo.Builder build = buildOrderInfo(entity);
		return Result.success(build);
	}
	
	/***
	 * 刷新悬赏令（手动刷新）
	 * @param playerId
	 * @return
	 */
	public Result<?> refreshRewardOrder(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RewardOrderEntity entity = opEntity.get();
		if(entity.isFirstRefresh()){
			//必出一个SS级别
			int result = firstRefresh(playerId, entity);
			if (result != 0) {
				return Result.fail(result);
			}
			entity.setRefreshCnt(entity.getRefreshCnt() + 1);
			entity.setFirstRefresh(false);
			return Result.success(buildOrderInfo(opEntity.get())); //返回列表
		}
		//如果已经存在S级别的悬赏令，则刷新的时候，不能低于该品质
		int result = refreshOrder(entity, false, false);
		if (result != 0) {
			return Result.fail(result);
		}
		entity.setRefreshCnt(entity.getRefreshCnt() + 1);
		entity.notifyUpdate();
		return Result.success(buildOrderInfo(opEntity.get())); //返回列表
	}
	
	/***
	 * 首次刷新悬赏令
	 * @param playerId
	 */
	private int firstRefresh(String playerId, RewardOrderEntity entity){
		RewardOrderActivityCfg cfg = RewardOrderActivityCfg.getInstance();
		RewardItem.Builder cost = cfg.getPlayerFreshCost(entity.getRefreshCnt() + 1); //下一次刷新的消耗
		List<RewardItem.Builder> list = new ArrayList<>();
		list.add(cost);
		boolean flag = this.getDataGeter().cost(playerId, list, Action.REWARD_ORDER_REWARD);
		if (!flag) {
			return Status.Error.ITEM_NOT_ENOUGH_VALUE;
		}
		//开始刷新
		List<RewardOrder> orderInfoList = entity.getOrderInfoList();
		for(int i = 0 ; i < orderInfoList.size() ; i ++){
			RewardOrder ro = orderInfoList.get(i);
			if(ro.getConfig().getQuality() > RewardOrderCfg.SSS && ro.canFresh()){
				RewardOrderCfg chose = getRate();
				if(ro.getConfig().getQuality() <= RewardOrderCfg.S && chose.getQuality() >= ro.getConfig().getQuality()){
					continue;
				}
				RewardOrder fresh = new RewardOrder(chose.getId(), chose);
				initOrder(fresh);
				orderInfoList.set(i, fresh);
			}
		}
		boolean containSS = false;
		for(int i = 0 ; i < orderInfoList.size() ; i ++){
			RewardOrder ro = orderInfoList.get(i);
			if(ro.getConfig().getQuality() <= RewardOrderCfg.SS){
				containSS = true;
				break;
			}
		}
		if(!containSS){
			//把第一个刷成ss
			RewardOrderCfg chose = getRate(RewardOrderCfg.S);
			RewardOrder addOrder = new RewardOrder(chose.getId(), chose);
			initOrder(addOrder);
			orderInfoList.set(0, addOrder);
		}
		return 0;
	}
	
	/***
	 * 刷新悬赏令
	 * @param entity 
	 * @param push 是否推送给客户端
	 */
	private int refreshOrder(RewardOrderEntity entity, boolean push, boolean system){
		RewardOrderActivityCfg cfg = RewardOrderActivityCfg.getInstance();
		RewardItem.Builder cost = cfg.getPlayerFreshCost(entity.getRefreshCnt() + 1); //下一次刷新的消耗
		if(!system){
			List<RewardItem.Builder> list = new ArrayList<>();
			list.add(cost);
			boolean flag = this.getDataGeter().cost(entity.getPlayerId(), list, Action.REWARD_ORDER_REWARD);
			if (!flag) {
				return Status.Error.ITEM_NOT_ENOUGH_VALUE;
			}
		}
		List<RewardOrder> orderInfoList = entity.getOrderInfoList();
		if(!system){
			for(int i = 0 ; i < orderInfoList.size() ; i ++){
				RewardOrder ro = orderInfoList.get(i);
				if(ro.getConfig().getQuality() > RewardOrderCfg.SSS && ro.canFresh()){
					RewardOrderCfg chose = getRate();
					if(ro.getConfig().getQuality() <= RewardOrderCfg.S && chose.getQuality() >= ro.getConfig().getQuality()){
						continue;
					}
					RewardOrder fresh = new RewardOrder(chose.getId(), chose);
					initOrder(fresh);
					orderInfoList.set(i, fresh);
				}
			}
		}else{
			//如果是系统刷新，那就全随机
			for(int i = 0 ; i < orderInfoList.size() ; i ++){
				RewardOrder ro = orderInfoList.get(i);
				if(ro.canFresh()){
					RewardOrderCfg chose = getRate();
					RewardOrder fresh = new RewardOrder(chose.getId(), chose);
					initOrder(fresh);
					orderInfoList.set(i, fresh);
				}
			}
		}
		if(push){
			PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(), HawkProtocol.valueOf(HP.code.REQ_REWARD_ORDER_INFO_VALUE, buildOrderInfo(entity)));
		}
		return 0;
	}
	
	private void initOrder(RewardOrderEntity entity){
		for(int i = 0 ; i < REWARD_ORDER_SIZE ; i ++){
			RewardOrderCfg config = getRate();
			RewardOrder order = new RewardOrder(config.getId(), config);
			initOrder(order);
			entity.addOrder(order);
		}
	}
	
	private void addRandomOrder(RewardOrderEntity entity){
		RewardOrderCfg config = getRate();
		if(config == null){
			throw new RuntimeException("RewardOrderActivity can not find least quality config, when player give up.");
		}
		RewardOrder order = new RewardOrder(config.getId(), config);
		initOrder(order);
		entity.addOrder(order);
	}
	
	/****
	 * 放弃悬赏令，给一个最差的
	 * @param entity
	 */
	private void giveUpOrderAddLeast(RewardOrderEntity entity){
		RewardOrderCfg config = getLeastRate();
		if(config == null){
			throw new RuntimeException("RewardOrderActivity can not find least quality config, when player give up.");
		}
		RewardOrder order = new RewardOrder(config.getId(), config);
		initOrder(order);
		entity.addOrder(order);
	}
	
	private rewardOrderInfo.Builder buildOrderInfo(RewardOrderEntity entity){
		List<RewardOrder> orderInfoList = entity.getOrderInfoList();
		rewardOrderInfo.Builder build = rewardOrderInfo.newBuilder();
		if(entity.getOrder() != null){
			RewardOrder ro = entity.getOrder();
			rewardOrder.Builder order = rewardOrder.newBuilder();
			order.setOrderId(ro.getId());
			order.setState(ro.calOrderStateProto());
			order.setEndTime(ro.calEndTime());
			build.addOrders(order);
		}else{
			for(RewardOrder ro : orderInfoList){
				rewardOrder.Builder order = rewardOrder.newBuilder();
				order.setOrderId(ro.getId());
				order.setState(ro.calOrderStateProto());
				build.addOrders(order);
			}
		}
		build.setFreshCnt(entity.getRefreshCnt());
		build.setNextFreshTime(entity.getNextFreshTime());
		build.setFinishCnt(entity.getFinishCnt());
		return build;
	}
	
	/***
	 * 随机抽取函数
	 * @param lastQuality 最低的品质 1:SSS 2:SS 3:S 4:A  5:B 6:C
	 * @return 大于最低品质的悬赏令(比如传入S，则可返回SS或者SSS)
	 */
	private RewardOrderCfg getRate(int lastQuality){
		ConfigIterator<RewardOrderCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(RewardOrderCfg.class);
		Map<RewardOrderCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			RewardOrderCfg cfg = configItrator.next();
			//map.put(cfg, cfg.getRate());
			if(cfg.getQuality() < lastQuality){
				map.put(cfg, cfg.getRate());
			}
		}
		RewardOrderCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found PandoraBoxRewardRateConfig:" + map);
		}
		return chose;
	}
	
	/***
	 * 随机抽取函数
	 * @return
	 */
	private RewardOrderCfg getRate(){
		ConfigIterator<RewardOrderCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(RewardOrderCfg.class);
		Map<RewardOrderCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			RewardOrderCfg cfg = configItrator.next();
			map.put(cfg, cfg.getRate());
		}
		RewardOrderCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found PandoraBoxRewardRateConfig:" + map);
		}
		return chose;
	}
	
	/***
	 * 获取一个品质最差的悬赏令
	 * @return
	 */
	private RewardOrderCfg getLeastRate(){
		ConfigIterator<RewardOrderCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(RewardOrderCfg.class);
		List<RewardOrderCfg> list = new ArrayList<>();
		int quality = 0; //默认值
		while(configItrator.hasNext()){
			RewardOrderCfg cfg = configItrator.next();
			if(cfg.getQuality() > quality){
				quality = cfg.getQuality();
			}
		}
		configItrator = HawkConfigManager.getInstance().getConfigIterator(RewardOrderCfg.class);
		while(configItrator.hasNext()){
			RewardOrderCfg cfg = configItrator.next();
			if(cfg.getQuality() == quality){
				list.add(cfg);
			}
		}
		if(list.size() == 1){
			return list.get(0);
		}else if(list.size() == 0){
			return null;
		}else{
			return list.get(new Random().nextInt(list.size()));
		}
	}
	
	private List<RewardOrderTaskCfg> getRewardOrderTask(int orderId){
		List<RewardOrderTaskCfg> list = new ArrayList<>();
		ConfigIterator<RewardOrderTaskCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(RewardOrderTaskCfg.class);
		while(configItrator.hasNext()){
			RewardOrderTaskCfg cfg = configItrator.next();
			if(cfg.getOrderId() == orderId && list.size() < REWARD_ORDER_SIZE){
				list.add(cfg);
			}
		}
		return list;
	}
	
	/***
	 * 领取悬赏令
	 * @param playerId
	 * @return
	 */
	public Result<?> receiveOrder(String playerId, int orderId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RewardOrderEntity entity = opEntity.get();
		if(entity.getOrder() != null){
			if(entity.getOrder().receive()){
				return Result.fail(Status.Error.HAS_REWARD_ORDER_VALUE);
			}
			if(entity.getOrder().finish()){
				return Result.fail(Status.Error.HAS_REWARD_ORDER_REWARD_VALUE);
			}
		}
		if(!entity.containOrder(orderId)){
			return Result.fail(Status.Error.NOT_CONTAIN_ORDER_ID_VALUE);
		}
		if(entity.getFinishCnt() >= RewardOrderActivityCfg.getInstance().getMaxFinishCount()){
			return Result.fail(Status.Error.REWARD_ORDER_RECIEVE_MAX_COUNT_VALUE);
		}
		entity.receiveOrder(orderId);
		logger.info("rewardOrder recieve sus. playerId:" + playerId + ", orderId:" + orderId);
		initAchieveItem(entity, playerId);
		entity.notifyUpdate();
		return Result.success(buildOrderInfo(entity));
	}
	
	
	
	public Result<?> giveUpOrder(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		RewardOrderEntity entity = opEntity.get();
		RewardOrder order = entity.getOrder();
		if(order == null){
			return Result.fail(Status.Error.NOT_CONTAIN_ORDER_ID_VALUE);
		}
		entity.giveUpOrder();
		//添加一个新的任务
		giveUpOrderAddLeast(entity);
		clearAchieveItem(entity);
		return Result.success(buildOrderInfo(entity));
	}
	
	/***
	 * 领取悬赏令奖励
	 * @param playerId
	 * @return
	 */
	public Result<?> takeOrderReward(String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		RewardOrderEntity entity = opEntity.get();
		RewardOrder order = entity.getOrder();
		if(order == null || !order.finish()){
			logger.error("player:" + playerId + "order is null or has't order take reward!");
			return null;
		}
		if(order.isTakeRewar()){
			logger.error("player:" + playerId + "order is take reward");
			return null;
		}
		order.setTakeRewar(true);
		for(AchieveItem item : entity.getItemList()){
			item.setState(AchieveState.TOOK_VALUE);
		}
		List<RewardItem.Builder> reward = order.getConfig().getRewardList();
		if(reward == null){
			return null;
		}
		this.getDataGeter().takeReward(playerId, reward, 1, Action.REWARD_ORDER_REWARD, true, RewardOrginType.ACTIVITY_REWARD);
		//干掉这个已经领奖的悬赏令，添加一个随机的悬赏令
		entity.takeOrderReward();
		addRandomOrder(entity);
		clearAchieveItem(entity);
		return Result.success(buildOrderInfo(entity));
	}

	private void initAchieveItem(RewardOrderEntity entity, String playerId){
		//初始化成就
		for(RewardOrderTask task : entity.getOrder().getTasks()){
			AchieveItem item = AchieveItem.valueOf(task.getId());
			entity.addItem(item);
		}
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
	}
	
	private void clearAchieveItem(RewardOrderEntity entity){
		entity.clearItem();
	}
	
	@Override
	public void onPlayerLogout(String playerId) {
		receivePlayers.remove(playerId);
	}

	@Override
	public void onTick() {
		long time = HawkTime.getMillisecond();
		Iterator<String> ite = receivePlayers.iterator();
		while(ite.hasNext()){
			String playerId = ite.next();
			Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				continue;
			}
			RewardOrderEntity entity = opEntity.get();
			RewardOrder order = entity.getOrder();
			if(order != null){
				order.onTick(time, entity);
				if(order.fail()){
					entity.orderFail();
					addRandomOrder(entity);
					pushResult(playerId, entity);
					entity.notifyUpdate();
					//从tick列表移除
					ite.remove();
				}
			}
			//如果到了刷新悬赏令的时间，则刷新悬赏令
			if(entity.systemFresh(time)){
				callBack(playerId, MsgId.REWARD_ORDER_SYSTEM_REFRESH, ()-> {
					setNextRefreshTime(entity.getNextFreshTime(), entity);
					refreshOrder(entity, true, true);
				});
			}
		}
	}
	
	private void pushResult(String playerId, RewardOrderEntity entity){
		rewardOrderInfo.Builder build = buildOrderInfo(entity);
		pushToPlayer(playerId, HP.code.REQ_REWARD_ORDER_INFO_VALUE, build);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.REWARD_ORDER;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RewardOrderActivity activity = new RewardOrderActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RewardOrderEntity> queryList = HawkDBManager.getInstance()
				.query("from RewardOrderEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RewardOrderEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}
	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RewardOrderEntity entity = new RewardOrderEntity(playerId, termId);
		entity.setFirstRefresh(true); //创建的时候，首次刷新一定为true
		//初始化四个悬赏令
		initOrder(entity);
		long activityStartTime = 0l;
		ConfigIterator<RewardOrderTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(RewardOrderTimeCfg.class);
		while(it.hasNext()){
			RewardOrderTimeCfg cfg = it.next();
			activityStartTime = cfg.getStartTimeValue();
			break;
		}
		setNextRefreshTime(activityStartTime, entity);
		return entity;
	}
	
	private void setNextRefreshTime(long time, RewardOrderEntity entity){
		long nextRefreshTime = 0l;
		RewardOrderActivityCfg activityCfg = RewardOrderActivityCfg.getInstance();
		nextRefreshTime = time + activityCfg.getFreshTime() * 1000l;
		long curTime = HawkTime.getMillisecond();
		while(nextRefreshTime <= curTime){
			nextRefreshTime += activityCfg.getFreshTime() * 1000l;
		}
		entity.setNextFreshTime(nextRefreshTime);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
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
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return Optional.empty();
		}
		RewardOrderEntity entity = opEntity.get();
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(achieveItems);
	}

	
	@Override
	public Result<?> onAchieveFinished(String playerId, AchieveItem achieveItem) {
		//判断悬赏令是否完成
		Optional<RewardOrderEntity> opEntity = getPlayerDataEntity(playerId);
		if (opEntity.isPresent()) {
			RewardOrderEntity entity = opEntity.get();
			RewardOrder order = entity.getOrder();
			if(order != null){
				order.achieveFinish(achieveItem, entity);
				if(order.finish()){
					//同步结果
					entity.setFinishCnt(entity.getFinishCnt() + 1);
					pushResult(playerId, entity);
				}
			}
		}
		return AchieveProvider.super.onAchieveFinished(playerId, achieveItem);
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(RewardOrderTaskCfg.class, achieveId);
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
	}

	@Override
	public Action takeRewardAction() {
		return null;
	}

	private RewardOrder initOrder(RewardOrder order){
		List<RewardOrderTaskCfg> taskCfgList = getRewardOrderTask(order.getId());
		order.initTask(taskCfgList);
		return order;
	}
	
}
