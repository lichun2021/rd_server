package com.hawk.activity.type.impl.brokenexchangeTwo;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.PackageBuyEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.brokenexchangeTwo.cfg.ActivityExchangeTwoCfg;
import com.hawk.activity.type.impl.brokenexchangeTwo.cfg.ActivityItemCollectTwoCfg;
import com.hawk.activity.type.impl.brokenexchangeTwo.entity.BrokenTwoActivityEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.BrokenExchangeInfoSyn;
import com.hawk.game.protocol.Activity.BrokenExchangeMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

/**
 * 战略储备2(按开服时间开启)
 * @author Jesse
 *
 */
public class BrokenExchangeTwoActivity extends ActivityBase {

	public BrokenExchangeTwoActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BROKEN_EXCHANGE_TWO_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new BrokenExchangeTwoActivity(config.getActivityId(), activityEntity);
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BrokenTwoActivityEntity> queryList = HawkDBManager.getInstance()
				.query("from BrokenTwoActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BrokenTwoActivityEntity brokentActivityEntity = new BrokenTwoActivityEntity(playerId, termId);
		brokentActivityEntity.setExchangeNumMap(new HashMap<>());
		brokentActivityEntity.setExchangeNum("");
		return brokentActivityEntity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<BrokenTwoActivityEntity> opDataEntity = this.getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}

		this.syncActivityInfo(playerId, opDataEntity.get());
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if(event.isCrossDay()){
			logger.info("receive clear dataEvent");
			this.cleanData(event.getPlayerId());
		}
	}
	
	@Override
	public void onOpen() {
		Set<String> idSet = this.getDataGeter().getOnlinePlayers();
		for (String id : idSet) {
			this.callBack(id, GameConst.MsgId.BROKEN_ACTIVITY_RESET_DATA, ()->{
				this.cleanData(id);
			});
		}
	}
	/**
	 * 数据重置
	 * @param BrokenTwoActivityEntity
	 */
	private void cleanData(String playerId) {
		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		logger.info("broken activity clean playerId:{}", entity.getPlayerId());
		entity.setCollectRemainTime(0);
		entity.setExchangeNum("");
		entity.setBeatYuriTimes(0);
		entity.setWishTimes(0);
		entity.setWolrdCollectRemainTime(0);
		entity.setGiftCostDiamond(0);
		entity.setWolrdCollectTimes(0);

		if (entity.getExchangeNumMap() != null) {
			entity.getExchangeNumMap().clear();
		}

		entity.setLastOperTime(HawkTime.getMillisecond());

		this.syncActivityInfo(entity.getPlayerId(), entity);
	}

	private void syncActivityInfo(String playerId, BrokenTwoActivityEntity BrokenTwoActivityEntity) {
		BrokenExchangeInfoSyn.Builder sbuilder = BrokenExchangeInfoSyn.newBuilder();
		BrokenExchangeMsg.Builder msgBuilder = null;
		if (BrokenTwoActivityEntity.getExchangeNumMap() != null && !BrokenTwoActivityEntity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : BrokenTwoActivityEntity.getExchangeNumMap().entrySet()) {
				msgBuilder = BrokenExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());

				sbuilder.addExchangeInfo(msgBuilder);
			}
		}

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BROKEN_EXCHANGE_TWO_INFO_SYN, sbuilder));
	}

	public Result<Integer> brokenExchange(String playerId, int exchangeId, int num) {

		ActivityExchangeTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityExchangeTwoCfg.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}

		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getDailyTimes()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}

		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.ACTIVITY_BROKEN_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}

		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.ACTIVITY_BROKEN_EXCHANGE, true, RewardOrginType.EXCHANGE_REWARD);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), exchangeId);
		logger.info("broken exchange playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);

		return Result.success(newNum);
	}

	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		BrokenTwoActivityEntity entity = opEntity.get();
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		ActivityItemCollectTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityItemCollectTwoCfg.class,
				Activity.BrokenExchangeOper.WORLD_COLLECT_VALUE);
		//配置不存在说明策划不想触发
		if (cfg == null) {
			return;
		}
		
		if (collectTime >= cfg.getDropParam()) {
			int num = collectTime / cfg.getDropParam();
			int remain = collectTime % cfg.getDropParam();
			if (cfg.getDropLimit() > 0) {
				num = num > cfg.getDropLimit()  - entity.getWolrdCollectTimes() ? cfg.getDropLimit()  - entity.getWolrdCollectTimes() : num;
			}
			if (num > 0) {
				this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
						Action.ACTIVITY_EXCHANGE_WORLD_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
		logger.info("broken activity worldCollect playerId:{}, beforeCollecTime:{}, afterCollectTime:{}, addCollectTime:{}", event.getPlayerId(), (collectTime - event.getCollectTime()), entity.getWolrdCollectRemainTime(), event.getCollectTime());
	}

	@Subscribe
	public void wishingEvent(WishingEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		entity.setWishTimes(entity.getWishTimes() + 1);

		ActivityItemCollectTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityItemCollectTwoCfg.class,
				Activity.BrokenExchangeOper.WISH_VALUE);
		if (cfg == null) {
			return;
		}
		
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.ACTIVITY_EXCHANGE_WISH, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
		}
		
		logger.info("broken activity wishing playerId:{}, wishTimes:{}", event.getPlayerId(), entity.getWishTimes());
	}

	@Subscribe
	public void packageBuyEvent(PackageBuyEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		int total = entity.getGiftCostDiamond() + event.getDiamond();

		ActivityItemCollectTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityItemCollectTwoCfg.class,
				Activity.BrokenExchangeOper.PACKAGE_BUY_VALUE);
		if (cfg == null) {
			return;
		}
		
		if (total >= cfg.getDropParam()) {
			int num = total / cfg.getDropParam();
			int remain = total % cfg.getDropParam();

			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
					Action.ACTIVITY_EXCHANGE_PACKAGE_BUY, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setGiftCostDiamond(remain);
		} else {
			entity.setGiftCostDiamond(total);
		}
		
		logger.info("broken activity packageBuy playerId:{}, addCostDiamond:{},lastDiamon:{}", event.getPlayerId(), event.getDiamond(), entity.getGiftCostDiamond());
	}

	@Subscribe
	public void resourceCollectEvent(CityResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		// 处理使用技能的情况
		if (event.getCollectTime().isEmpty()) {
			return;
		}

		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		int remainTime = entity.getCollectRemainTime();

		ActivityItemCollectTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityItemCollectTwoCfg.class,
				Activity.BrokenExchangeOper.RESOURCE_COLLECT_VALUE);
		if (cfg == null) {
			return;
		}
		
		int num = 0;
		int totalTime = 0;
		for (Integer timeLong : event.getCollectTime()) {
			totalTime = timeLong + remainTime;
			totalTime = totalTime > cfg.getDropLimit() ? cfg.getDropLimit() : totalTime;
			if (totalTime > cfg.getDropParam()) {
				num = totalTime / cfg.getDropParam() + num;
				remainTime = totalTime % cfg.getDropParam();
			}
		}

		if (num > 0) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,
					Action.ACTIVITY_EXCHANGE_RES_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
		}

		entity.setCollectRemainTime(remainTime);				
	}
	
	@Subscribe
	public void beatYuriEvent(MonsterAttackEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		int monsterType = event.getMosterType();
		switch(monsterType) {
		case MonsterType.TYPE_1_VALUE:
		case MonsterType.TYPE_2_VALUE:
			if (!event.isKill()) {
				return;
			}
			break;
		default:
			return;
		}
		
		int atkTimes = event.getAtkTimes();
		Optional<BrokenTwoActivityEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		BrokenTwoActivityEntity entity = opEntity.get();
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		ActivityItemCollectTwoCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ActivityItemCollectTwoCfg.class,
				Activity.BrokenExchangeOper.BEAT_YURI_VALUE);
		if (cfg == null) {
			return;
		}
		
		if (atkTimes >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
					Action.ACTIVITY_EXCHANGE_BEAT_YURI, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
		}
		
		logger.info("broken activity beatYuri playerId:{}, beatTimes:{}, totalBeatTimes", event.getPlayerId(),
				atkTimes, entity.getBeatYuriTimes());
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub
		
	}

}
