package com.hawk.activity.type.impl.supplyStationTwo;

import java.util.*;

import com.hawk.activity.type.impl.supplyStationTwo.cfg.SupplyStationTwoExchangeCfg;
import com.hawk.game.protocol.Activity;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.result.Result;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.supplyStationTwo.cfg.SupplyStationTwoChestCfg;
import com.hawk.activity.type.impl.supplyStationTwo.cfg.SupplyStationTwoKVConfig;
import com.hawk.activity.type.impl.supplyStationTwo.entity.SupplyStationTwoEntity;
import com.hawk.game.protocol.Activity.SupplyStationInfo;
import com.hawk.game.protocol.Activity.SupplyStationItem;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 装扮专场
 * 历史背景：
 * 原来的活动是复制一个老活动
 * 后来把100和101两个活动合并了
 */
public class SupplyStationTwoActivity extends ActivityBase {
	/**
	 * 构造函数
	 * @param activityId 活动id
	 * @param activityEntity 活动实例
	 */
	public SupplyStationTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 获得活动类型
	 * @return 活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.SUPPLY_STATION_TWO_ACTIVITY;
	}

	/**
	 * 创建活动实例
	 * @param config 活动配置
	 * @param activityEntity 活动实例
	 * @return
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SupplyStationTwoActivity activity = new SupplyStationTwoActivity(config.getActivityId(), activityEntity);
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
		List<SupplyStationTwoEntity> queryList = HawkDBManager.getInstance()
				.query("from SupplyStationTwoEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SupplyStationTwoEntity entity = queryList.get(0);
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
		SupplyStationTwoEntity entity = new SupplyStationTwoEntity(playerId, termId);
		return entity;
	}

	/**
	 * 玩家登录
	 * @param playerId 玩家id
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		if(isOpening(playerId)){
			syncActivityInfo(playerId);
		}
	}

	/**
	 * 活动开启
	 */
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for(String playerId : onlinePlayerIds){
			callBack(playerId, GameConst.MsgId.SUPPLY_STATION_TWO_INIT, () -> {
				syncActivityInfo(playerId);
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
		Optional<SupplyStationTwoEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		SupplyStationTwoKVConfig config = SupplyStationTwoKVConfig.getInstance();
		if(!config.isReset()){
			return;
		}
		SupplyStationTwoEntity entity = opEntity.get();
		//记录一下清空之前的购买记录
		logger.info("SupplyStationTwoActivity before clear entity msg:{}", entity);
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
		//判断活动是否开启，如果没有开启返回错误码
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//获得玩家活动数据如果活动数据为空返回错误码
		Optional<SupplyStationTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SupplyStationTwoEntity entity = opEntity.get();
		SupplyStationTwoChestCfg chestCfg = HawkConfigManager.getInstance().getConfigByKey(SupplyStationTwoChestCfg.class, chestId);
		if(chestCfg == null){
			logger.error("SupplyStationTwoActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("SupplyStationTwoActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.SUPPLY_STATION_TWO_COST);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//同步界面信息
		syncActivityBuyInfo(playerId, entity);
		//给宝箱奖励
		logger.info("SupplyStationTwoActivity player:{}, buy chest, chestId:{}, count:{}", playerId, chestId, count);
		this.getDataGeter().sendAwardFromAwardCfg(chestCfg.getAwardId(), count, playerId, true, Action.SUPPLY_STATION_TWO_REWARD, RewardOrginType.SHOPPING_GIFT);
		return Result.success();
	}

	/***
	 * 客户端勾提醒兑换
	 * @param playerId 玩家id
	 * @param tips : 0为去掉 1为增加 2为全选 3为全取消
	 */
	public Result<?> reqActivityTips(String playerId, int id, int tips){
		//判断活动是否开启，如果没有开启返回错误码
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//获得玩家活动数据如果活动数据为空返回错误码
		Optional<SupplyStationTwoEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SupplyStationTwoEntity entity = opt.get();
		switch(tips){
			case 0:{
				//去掉
				entity.addTips(id);
			}
			break;
			case 1:{
				//增加
				entity.removeTips(id);

			}
			break;
			case 2:{
				//全选
				entity.getPlayerPoints().clear();
				entity.notifyUpdate();
			}
			break;
			case 3:{
				//全取消
				ConfigIterator<SupplyStationTwoExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(SupplyStationTwoExchangeCfg.class);
				List<Integer> ids = new ArrayList<Integer>();
				while(ite.hasNext()){
					SupplyStationTwoExchangeCfg cfg = ite.next();
					ids.add(cfg.getId());
				}
				entity.setPlayerPoints(ids);
			}
			break;
		}
		//同步兑换数据
		syncActivityExchangeInfo(playerId, entity);
		//返回结果
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
		//判断活动是否开启，如果没有开启返回错误码
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		//获得活动兑换配置
		SupplyStationTwoExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SupplyStationTwoExchangeCfg.class, exchangeId);
		//如果配置为空返回错误码
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		//获得玩家活动数据如果活动数据为空返回错误码
		Optional<SupplyStationTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		SupplyStationTwoEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.DOME_EXCHANGE_TWO, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.DOME_EXCHANGE_TWO, true, RewardOrginType.DOME_EXCHANGE_REWARD);
		logger.info("dome_exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityExchangeInfo(playerId, entity);
		return Result.success(newNum);
	}

	/**
	 * 同步活动数据
	 * @param playerId 玩家id
	 */
	public void syncActivityInfo(String playerId){
		//获得玩家活动数据如果活动数据为空直接返回
		Optional<SupplyStationTwoEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			logger.error("SupplyStationTwoEntity init error, can't init entity, playerId:{}", playerId);
			return;
		}
		SupplyStationTwoEntity entity = opEntity.get();
		syncActivityBuyInfo(playerId, entity);
		syncActivityExchangeInfo(playerId, entity);
	}

	/**
	 * 同步购买信息
	 * @param playerId 玩家id
	 * @param entity 玩家数据库实例
	 */
	private void syncActivityBuyInfo(String playerId, SupplyStationTwoEntity entity) {
		//组装数据
		SupplyStationInfo.Builder build = SupplyStationInfo.newBuilder();
		ConfigIterator<SupplyStationTwoChestCfg> ite = HawkConfigManager.getInstance().getConfigIterator(SupplyStationTwoChestCfg.class);
		while(ite.hasNext()){
			SupplyStationTwoChestCfg config = ite.next();
			SupplyStationItem.Builder item = SupplyStationItem.newBuilder();
			item.setId(config.getId());
			item.setBuyCnt(entity.getBuyCnt(config.getId()));
			build.addItems(item);
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.SUPPLY_STATION_TWO_INFO_S_VALUE, build));
	}

	/**
	 * 同步兑换信息
	 * @param playerId 玩家id
	 * @param entity 玩家数据库实例
	 */
	private void syncActivityExchangeInfo(String playerId, SupplyStationTwoEntity entity) {
		//组装数据
		Activity.domeExchangeSyncInfoSyn.Builder sbuilder = Activity.domeExchangeSyncInfoSyn.newBuilder();
		Activity.DomeExchangeMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Map.Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = Activity.DomeExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		ConfigIterator<SupplyStationTwoExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(SupplyStationTwoExchangeCfg.class);
		while(ite.hasNext()){
			SupplyStationTwoExchangeCfg cfg = ite.next();
			if(!entity.getPlayerPoints().contains(cfg.getId())){
				sbuilder.addTips(cfg.getId());
			}
		}
		//同步信息
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.DOME_EXCHANGE_TWO_TIPS_INFO_S_VALUE, sbuilder));
	}
}
