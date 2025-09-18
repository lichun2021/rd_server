package com.hawk.activity.type.impl.medalAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
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
import com.hawk.activity.event.impl.LoginDayMedalActionEvent;
import com.hawk.activity.event.impl.MedalActionLotteryDrawEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionAchieveCfg;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionActivityKVCfg;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionLotteryCfg;
import com.hawk.activity.type.impl.medalAction.cfg.MedalActionTaskAchieveCfg;
import com.hawk.activity.type.impl.medalAction.entity.MedalActionEntity;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.MedalLotteryResponse;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/** 勋章行动活动
 * @author Winder
 */
public class MedalActionActivity extends ActivityBase implements AchieveProvider {
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public MedalActionActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MEDAL_ACTION_ACTIVITY;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MedalActionActivity activity  = new MedalActionActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MedalActionEntity> queryList = HawkDBManager.getInstance()
				.query("from MedalActionEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MedalActionEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MedalActionEntity entity = new MedalActionEntity(playerId, termId);
		return entity;
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
		Optional<MedalActionEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		MedalActionEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity);
		return Optional.of(items);
	}
	
	
	@Override
	public void onOpen() {
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		for (String playerId : playerIds) {
			this.callBack(playerId, GameConst.MsgId.ACHIEVE_INIT_MEDAL_TREASURE, ()->{
				initAchieve(playerId);
			});
		}
	}
	//初始化成就
	private void initAchieve(String playerId){
		Optional<MedalActionEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空则初始化
		MedalActionEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<MedalActionAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MedalActionAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while(configIterator.hasNext()){
			MedalActionAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		ConfigIterator<MedalActionTaskAchieveCfg> taConfigIterator = HawkConfigManager.getInstance().getConfigIterator(MedalActionTaskAchieveCfg.class);
		while(taConfigIterator.hasNext()){
			MedalActionTaskAchieveCfg cfg = taConfigIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, itemList), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayMedalActionEvent(playerId, 1), true);
	}
	
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		MedalActionAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(MedalActionAchieveCfg.class, achieveId);
		if (achieveCfg == null) {
			MedalActionTaskAchieveCfg taCfg = HawkConfigManager.getInstance().getConfigByKey(MedalActionTaskAchieveCfg.class, achieveId);
			if (taCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		Optional<MedalActionEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		ActivityManager.getInstance().getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}
	
	/***
	 * 抽奖
	 * @param playerId
	 * @param lotCnt 抽奖次数
	 */
	public Result<?> lottery(String playerId, boolean isTenTimes){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<MedalActionEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MedalActionEntity entity = opEntity.get();
		MedalActionActivityKVCfg config = HawkConfigManager.getInstance().getKVInstance(MedalActionActivityKVCfg.class);
		if (entity.getBuyNum() > config.getDailyTimes()) {
			return Result.fail(Status.Error.MEDAL_ACTION_LOTTERY_TIMES_LIMIT_VALUE);
		}
		//消耗道具
		RewardItem.Builder cost = null;
		if (isTenTimes) {
			cost = config.getTenConsume();
		}else {
			cost = config.getSingleConsume();
		}
		//已有道具数量
		int itemCnt = getDataGeter().getItemNum(playerId, cost.getItemId());
		List<RewardItem.Builder> consumeList = new ArrayList<>();
		if (itemCnt >= cost.getItemCount()) {
			consumeList.add(cost);
		}else{
			//还需要道具数量
			int needBuyCnt = (int) (cost.getItemCount() - itemCnt);
			//单个物品价格
			RewardItem.Builder price = config.getItemConsume();
			//总价格
			price.setItemCount(price.getItemCount() * needBuyCnt);
			consumeList.add(price);
			if (itemCnt > 0) {
				cost.setItemCount(itemCnt);
				consumeList.add(cost);
			}
		}
		boolean consumeResult = getDataGeter().consumeItems(playerId, consumeList, HP.code.MEDAL_ACTION_LOTTERY_REQ_VALUE, Action.MEDAL_ACTION_LOTTERY_COST);
		if (!consumeResult) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}	
		MedalLotteryResponse.Builder result = MedalLotteryResponse.newBuilder();
		
		List<RewardItem.Builder> rewardList = lotteryRewards(playerId, isTenTimes, result);
		//发送奖励
		this.getDataGeter().takeReward(playerId, rewardList, 1,  Action.MEDAL_ACTION_LOTTERY, false, RewardOrginType.MEDAL_ACTION_LOTTERY_REWARD);
		int lotCnt = isTenTimes ? 10 : 1;
		//更新次数
		entity.setBuyNum(entity.getBuyNum() + lotCnt);
		//抽奖事件
		ActivityManager.getInstance().postEvent(new MedalActionLotteryDrawEvent(playerId, lotCnt));
		//抽奖打点
		getDataGeter().logMedalTreasureLottery(playerId, lotCnt);
		logger.info("MedalActionActivity lottery  lotCnt:{}, itemCnt:{}", playerId, lotCnt, itemCnt);
		return Result.success(result);
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		MedalActionActivityKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(MedalActionActivityKVCfg.class);
		if (!kvCfg.isDailyReset()) {
			return;
		}
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<MedalActionEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MedalActionEntity entity = optional.get();
		List<AchieveItem> retainList = new ArrayList<>();
		//这个表重置
		ConfigIterator<MedalActionTaskAchieveCfg> taskIterator = HawkConfigManager.getInstance().getConfigIterator(MedalActionTaskAchieveCfg.class);
		while (taskIterator.hasNext()) {
			MedalActionTaskAchieveCfg cfg = taskIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
		//这个表重置
		ConfigIterator<MedalActionAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(MedalActionAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			MedalActionAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
		entity.resetItemList(retainList);
		entity.setBuyNum(0);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayMedalActionEvent(playerId, 1), true);
		//push
		AchievePushHelper.pushAchieveUpdate(playerId, retainList);
	}

	/**获取奖励
	 * @param playerId
	 * @param single
	 * @param result
	 * @return
	 */
	private List<RewardItem.Builder> lotteryRewards(String playerId, boolean isTenTimes, MedalLotteryResponse.Builder result){
		List<RewardItem.Builder> lastRewardList = new ArrayList<>();
		if (isTenTimes) {
			for (int i = 0; i < 10; i++) {
				lastRewardList.addAll(getRandomRewardConf().getRewardList());
			}
		}else{
			MedalActionLotteryCfg cfg = getRandomRewardConf();
			lastRewardList = cfg.getRewardList();
			result.setLotId(cfg.getId());
		}
		//result 填充奖励数据
		for (RewardItem.Builder builder : lastRewardList) {
			result.addReward(builder);
		}
		return lastRewardList;
	}

	/**
	 * 随机抽取函数
	 * @return
	 */
	private MedalActionLotteryCfg getRandomRewardConf(){
		ConfigIterator<MedalActionLotteryCfg> configItrator = HawkConfigManager.getInstance().getConfigIterator(MedalActionLotteryCfg.class);
		Map<MedalActionLotteryCfg, Integer> map = new HashMap<>();
		while(configItrator.hasNext()){
			MedalActionLotteryCfg cfg = configItrator.next();
			map.put(cfg, cfg.getSingleWeight());
		}
		MedalActionLotteryCfg chose = HawkRand.randomWeightObject(map);
		if(chose == null){
			throw new RuntimeException("can not found MedalActionLotteryCfg:" + map);
		}
		
		return chose;
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().getConfigByKey(MedalActionAchieveCfg.class, achieveId);
		if (cfg == null) {
			cfg = HawkConfigManager.getInstance().getConfigByKey(MedalActionTaskAchieveCfg.class, achieveId);
		}
		return cfg;
	}

	@Override
	public Action takeRewardAction() {
		return Action.MEDAL_ACTION_TASK_REWARD;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
