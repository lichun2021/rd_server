package com.hawk.activity.type.impl.heroBackExchange;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map.Entry;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.type.impl.heroBackExchange.cfg.HeroBackExchangeKVConfig;
import com.hawk.activity.type.impl.heroBackExchange.cfg.HeroBackExchangeShopCfg;
import com.hawk.game.protocol.Reward;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.heroBackExchange.cfg.HeroBackExchangeConfig;
import com.hawk.activity.type.impl.heroBackExchange.entity.HeroBackExchangeEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.HeroBackExchangeMsg;
import com.hawk.game.protocol.Activity.HeroBackExchangeSyncInfoSyn;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/**
 * 英雄专场
 * 历史背景：
 * 原来的活动是复制一个老活动
 * 后来把100和101两个活动合并了
 */
public class HeroBackExchangeActivity extends ActivityBase {
	/**
	 * 构造函数
	 * @param activityId 活动id
	 * @param activityEntity 活动实例
	 */
	public HeroBackExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 获得活动类型
	 * @return 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.HERO_BACK_EXCHANGE;
	}

	/**
	 * 创建活动实例
	 * @param config 活动配置
	 * @param activityEntity 活动实例
	 * @return
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HeroBackExchangeActivity activity = new HeroBackExchangeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	/**
	 * 从数据库加载玩家活动数据
	 * @param playerId 玩家id
	 * @param termId 活动期数
	 * @return 玩家活动数据
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HeroBackExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from HeroBackExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HeroBackExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建玩家活动数据数据库实例
	 * @param playerId 玩家数据
	 * @param termId 活动期数
	 * @return
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HeroBackExchangeEntity entity = new HeroBackExchangeEntity(playerId, termId);
		return entity;
	}

	/**
	 * 玩家登录
	 * @param playerId 玩家id
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			this.syncActivityInfo(playerId);
		}
	}

	/**
	 * 活动开启
	 */
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.ON_HERO_BACK_EXCHANGE_ACTIVITY_OPEN, () -> {
				this.syncActivityInfo(playerId);
			});
		}
	}

	/**
	 * 处理跨天时间
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<HeroBackExchangeEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		HeroBackExchangeKVConfig config = HawkConfigManager.getInstance().getKVInstance(HeroBackExchangeKVConfig.class);
		if(!config.isReset()){
			return;
		}
		HeroBackExchangeEntity entity = opEntity.get();
		//记录一下清空之前的购买记录
		logger.info("HeroBackActivity before clear entity msg:{}", entity);
		entity.crossDay();
		entity.notifyUpdate();
		syncActivityBuyInfo(event.getPlayerId(), entity);
	}


	/**
	 * 购买宝箱
	 * @param chestId 宝箱id
	 * @param count 购买数量
	 * @param playerId 玩家id
	 * @return
	 */
	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<HeroBackExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HeroBackExchangeEntity entity = opEntity.get();

		HeroBackExchangeShopCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(HeroBackExchangeShopCfg.class, chestId);
		if(chestCfg == null){
			logger.error("HeroBackActivity send error chestId:" + chestId);
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("HeroBackActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<Reward.RewardItem.Builder> prize = chestCfg.buildPrize(count);
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
		syncActivityBuyInfo(playerId, entity);

		// 购买宝箱数据打点：活动ID，宝箱ID，购买数量，货币消耗，获得道具
		this.getDataGeter().logHeroBackBuyChest(playerId, Activity.ActivityType.HERO_BACK_VALUE, chestId, count, chestCfg.getPrice(), awardItems);

		return Result.success();
	}
	
	/***
	 * 客户端勾提醒兑换
	 * @param playerId
	 * 
	 * @param tips : 0为去掉 1为增加 2为全选 3为全取消
	 */
	public Result<?> reqActivityTips(String playerId, int id, int tips){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<HeroBackExchangeEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HeroBackExchangeEntity entity = opt.get();
		switch(tips){
			case 0:{
				entity.addTips(id);
			}
			break;
			case 1:{
				entity.removeTips(id);
			}
			break;
			case 2:{
				entity.getPlayerPoints().clear();
				entity.notifyUpdate();
			}
			break;
			case 3:{
				ConfigIterator<HeroBackExchangeConfig> ite = HawkConfigManager.getInstance().getConfigIterator(HeroBackExchangeConfig.class);
				List<Integer> ids = new ArrayList<Integer>();
				while(ite.hasNext()){
					HeroBackExchangeConfig cfg = ite.next();
					ids.add(cfg.getId());
				}
				entity.setPlayerPoints(ids);
			}
			break;
		}
		syncActivityExchangeInfo(playerId, entity);
		return Result.success();
	}

	/**
	 * 兑换
	 * @param playerId 玩家id
	 * @param exchangeId 兑换id
	 * @param num 兑换数量
	 * @return 兑换结果
	 */
	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		HeroBackExchangeConfig cfg = HawkConfigManager.getInstance().getConfigByKey(HeroBackExchangeConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<HeroBackExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		HeroBackExchangeEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.HERO_BACK_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.HERO_BACK_EXCHANGE, true, RewardOrginType.HERO_BACK_EXCHANGE_REWARD);
		logger.info("heroback_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityExchangeInfo(playerId, entity);
		
		// 兑换道具数据打点：活动ID， 档位ID， 消耗道具，所得道具， 兑换次数
		this.getDataGeter().logHeroBackExchange(playerId, Activity.ActivityType.HERO_BACK_EXCHANGE_VALUE, exchangeId, cfg.getNeedItem(), cfg.getGainItem(), num);

		return Result.success(newNum);
	}

	/**
	 * 同步活动数据
	 * @param playerId 玩家id
	 */
	public void syncActivityInfo(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<HeroBackExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("HeroBackEntity init error, can't init entity, playerId:{}", playerId);
			return;
		}
		HeroBackExchangeEntity entity = opEntity.get();
		syncActivityBuyInfo(playerId, entity);
		syncActivityExchangeInfo(playerId, entity);
	}

	/**
	 * 同步购买信息
	 * @param playerId 玩家id
	 * @param entity 玩家数据库实例
	 */
	private void syncActivityBuyInfo(String playerId, HeroBackExchangeEntity entity) {
		Activity.HeroBackInfo.Builder build = Activity.HeroBackInfo.newBuilder();
		ConfigIterator<HeroBackExchangeShopCfg> ite = HawkConfigManager.getInstance().getConfigIterator(HeroBackExchangeShopCfg.class);
		while(ite.hasNext()){
			HeroBackExchangeShopCfg config = ite.next();
			Activity.HeroBackItem.Builder item = Activity.HeroBackItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HERO_BACK_INFO_S_VALUE, build));
	}

	/**
	 * 同步兑换信息
	 * @param playerId 玩家id
	 * @param entity 玩家数据库实例
	 */
	private void syncActivityExchangeInfo(String playerId, HeroBackExchangeEntity entity) {
		//组装数据
		HeroBackExchangeSyncInfoSyn.Builder sbuilder = HeroBackExchangeSyncInfoSyn.newBuilder();
		HeroBackExchangeMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = HeroBackExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		ConfigIterator<HeroBackExchangeConfig> ite = HawkConfigManager.getInstance().getConfigIterator(HeroBackExchangeConfig.class);
		while(ite.hasNext()) {
			HeroBackExchangeConfig cfg = ite.next();
			if(!entity.getPlayerPoints().contains(cfg.getId())){
				sbuilder.addTips(cfg.getId());
			}
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.HERO_BACK_EXCHANGE_TIPS_INFO_S_VALUE, sbuilder));
	}

}
