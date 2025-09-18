package com.hawk.activity.type.impl.heroBack;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.heroBack.cfg.HeroBackKVConfig;
import com.hawk.activity.type.impl.heroBack.cfg.HeroBackShopCfg;
import com.hawk.activity.type.impl.heroBack.entity.HeroBackEntity;
import com.hawk.game.protocol.Activity.HeroBackInfo;
import com.hawk.game.protocol.Activity.HeroBackItem;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

public class HeroBackActivity extends ActivityBase {
	
	private static final Logger logger = LoggerFactory.getLogger("Server");

	public HeroBackActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_BACK;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
		}
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.HERO_BACK_INIT, () -> {
				syncActivityInfo(playerId);
			});
		}
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<HeroBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		HeroBackKVConfig config = HawkConfigManager.getInstance().getKVInstance(HeroBackKVConfig.class);
		if(!config.isReset()){
			return;
		}
		HeroBackEntity entity = opEntity.get();
		//记录一下清空之前的购买记录
		logger.info("HeroBackActivity before clear entity msg:{}", entity);
		entity.crossDay();
		entity.notifyUpdate();
		syncActivityInfo(event.getPlayerId());
	}

	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<HeroBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HeroBackEntity entity = opEntity.get();
		
		HeroBackShopCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(HeroBackShopCfg.class, chestId);
		if(chestCfg == null){
			logger.error("HeroBackActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("HeroBackActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.HERO_BACK_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		//给宝箱奖励
		logger.info("HeroBackActivity player:{}, buy chest, chestId:{}, count:{}, entity:{}", playerId, chestId, count, entity);
		String awardItems = this.getDataGeter().sendAwardFromAwardCfg(chestCfg.getAwardId(), count, playerId, true, Action.HERO_BACK_REWARD, RewardOrginType.SHOPPING_GIFT);
		
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//同步界面信息
		syncActivityInfo(playerId);
		
		// 购买宝箱数据打点：活动ID，宝箱ID，购买数量，货币消耗，获得道具
		this.getDataGeter().logHeroBackBuyChest(playerId, Activity.ActivityType.HERO_BACK_VALUE, chestId, count, chestCfg.getPrice(), awardItems);
		
		return null;
	}

	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<HeroBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("HeroBackEntity init error, can't init entity, playerId:{}", playerId);
			return;
		}
		HeroBackEntity entity = opEntity.get();
		
		HeroBackInfo.Builder build = HeroBackInfo.newBuilder();
		ConfigIterator<HeroBackShopCfg> ite = HawkConfigManager.getInstance().getConfigIterator(HeroBackShopCfg.class);
		while(ite.hasNext()){
			HeroBackShopCfg config = ite.next();
			HeroBackItem.Builder item = HeroBackItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HERO_BACK_INFO_S_VALUE, build));
	}
	
//	/***
//	 * 构建宝箱奖励
//	 * @param chestCfg
//	 * @param count
//	 * @return
//	 */
//	private List<RewardItem.Builder> buildChestReward(SupplyStationChestCfg chestCfg, int count){
//		if(chestCfg == null || count <= 0){
//			return null;
//		}
//		List<RewardItem.Builder> reward = new ArrayList<RewardItem.Builder>();
//		for(int i = 0 ; i < count ; i ++){
//			reward.addAll(chestCfg.buildOrdinaryReward()); //先把普通奖励弄进来
//			reward.addAll(getChanceReward(chestCfg)); //概率性道具加入
//		}
//		return reward;
//	}
	
//	/***
//	 * 构建一次机会物品
//	 * @param chestCfg
//	 * @return
//	 */
//	private List<RewardItem.Builder> getChanceReward(SupplyStationChestCfg chestCfg){
//		if(chestCfg == null){
//			return null;
//		}
//		Map<String, Integer> map = new HashMap<>();
//		String[] chance_rewards = chestCfg.getChanceRewards();
//		Integer[] rates = chestCfg.getChanceRates();
//		if(chance_rewards.length != rates.length){
//			HawkLog.errPrintln("HeroBackActivity chance reward error. config error:{}" + chestCfg);
//			return null;
//		}
//		for(int i = 0 ; i < chance_rewards.length ; i ++){
//			map.put(chance_rewards[i], rates[i]);
//		}
//		//随机出来的物品
//		String reward = HawkRand.randomWeightObject(map);
//		return RewardHelper.toRewardItemList(reward);
//	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroBackActivity activity = new HeroBackActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroBackEntity> queryList = HawkDBManager.getInstance()
				.query("from HeroBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HeroBackEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroBackEntity entity = new HeroBackEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
