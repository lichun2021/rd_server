package com.hawk.activity.type.impl.allyBeatBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AllyBeatBackGetItemEvent;
import com.hawk.activity.event.impl.CityResourceCollectEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackAchieveCfg;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackCfg;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackItemCfg;
import com.hawk.activity.type.impl.allyBeatBack.cfg.AllyBeatBackTypeCfg;
import com.hawk.activity.type.impl.allyBeatBack.entity.AllyBeatBackEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.PlayerAchieve.AchieveState;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.AllyBeatBackInfoResp;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;

public class AllyBeatBackActivity extends ActivityBase implements AchieveProvider {

	public AllyBeatBackActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public boolean isProviderActive(String playerId) {
		return this.isAllowOprate(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return true;
	}	
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<AllyBeatBackEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Optional.empty();
		}
		
		AllyBeatBackEntity playerDataEntity = opPlayerDataEntity.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			this.initAchieve(playerDataEntity,  false);
		}
		
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);		
		return Optional.of(items);
	}
	
	private void initAchieve(String playerId) {
		Optional<AllyBeatBackEntity> optionalEntity = this.getPlayerDataEntity(playerId);
		if (!optionalEntity.isPresent()) {
			return;
		}
		//只有为空的时候才初始化
		AllyBeatBackEntity entity = optionalEntity.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		this.initAchieve(entity, true);
	}
	private void initAchieve(AllyBeatBackEntity playerDataEntity, boolean needPush) {		
		ConfigIterator<AllyBeatBackAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(AllyBeatBackAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			AllyBeatBackAchieveCfg cfg = configIterator.next();				
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}		
		playerDataEntity.setItemList(itemList);
		HawkLog.logPrintln("AllyBeatBackInitAchieve playerId:{}, itemList", playerDataEntity.getPlayerId(), itemList);		
		if (needPush) {
			AchievePushHelper.pushAchieveUpdate(playerDataEntity.getPlayerId(), playerDataEntity.getItemList());
		}		
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackAchieveCfg.class, achieveId);
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {		
		
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public Action takeRewardAction() {
		return Action.ALLY_BEAT_BACK_TASK_REWARD;
	}

	@Override
	public ActivityType getActivityType() {
		  return ActivityType.ALLY_BEAT_BACK;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AllyBeatBackActivity allyBeatBack = new AllyBeatBackActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(allyBeatBack);
		
		return allyBeatBack;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AllyBeatBackEntity> queryList = HawkDBManager.getInstance()
				.query("from AllyBeatBackEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AllyBeatBackEntity allyBeatBackEntity = new AllyBeatBackEntity();
		allyBeatBackEntity.setPlayerId(playerId);
		allyBeatBackEntity.setTermId(termId);
		
		return allyBeatBackEntity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {

	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (!this.isAllowOprate(event.getPlayerId())) {
			return;
		}
		if (event.isCrossDay()) {
			this.cleanData(event.getPlayerId());
			Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
			if (!opEntity.isPresent()) {
				return;
			}			
			this.pushInfo(event.getPlayerId(), opEntity.get());
			this.initAchieve(opEntity.get(), true);			
		}
	}
	
	@Override
	public void onOpen() {
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		for (String playerId : playerIds) {
			this.callBack(playerId, GameConst.MsgId.ALLY_BEAT_BACK_INIT_ACHIEVE, ()->{
				initAchieve(playerId);
			});
		}
	}
	/**
	 * 数据重置
	 * @param AllyBeatBack
	 */
	private void cleanData(String playerId) {
		HawkLog.logPrintln("AllyBeatBackResetData playerId:{}", playerId);
		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		AllyBeatBackEntity entity = opEntity.get();		
		entity.setCollectRemainTime(0);
		entity.setBeatYuriTimes(0);
		entity.setWishTimes(0);
		entity.setWolrdCollectRemainTime(0);
		entity.setWolrdCollectTimes(0);
	}
	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		AllyBeatBackEntity entity = opEntity.get();
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		AllyBeatBackTypeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackTypeCfg.class,
				AllyBeatBackConst.WORLD_COLLECT);
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
				takeReward(event.getPlayerId(), cfg.getDropId(), num,
						Action.ALLY_BEAT_BACK_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
	}

	@Subscribe
	public void wishingEvent(WishingEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		AllyBeatBackEntity entity = opEntity.get();
		entity.setWishTimes(entity.getWishTimes() + 1);

		AllyBeatBackTypeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackTypeCfg.class,
				AllyBeatBackConst.WISH);
		if (cfg == null) {
			return;
		}
		
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.ALLY_BEAT_BACK_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
		}
	
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

		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		AllyBeatBackEntity entity = opEntity.get();
		int remainTime = entity.getCollectRemainTime();

		AllyBeatBackTypeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackTypeCfg.class,
				AllyBeatBackConst.RESOURCE_COLLECT);
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
			takeReward(event.getPlayerId(), cfg.getDropId(), num,
					Action.ALLY_BEAT_BACK_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
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
		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		AllyBeatBackEntity entity = opEntity.get();
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		AllyBeatBackTypeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackTypeCfg.class,
				AllyBeatBackConst.BEAT_YURI);
		if (cfg == null) {
			return;
		}
		
		if (atkTimes >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
					Action.ALLY_BEAT_BACK_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
		}
	}	

	private void takeReward(String playerId, int dropId, int num, Action action, int mailId,
			String name, String activityName) {		
		this.getDataGeter().takeReward(playerId, dropId, num, action, mailId, name, activityName, false, null);
	}
	
	public Result<Void> exchage(String playerId, int exchangeId, int num) {		
		AllyBeatBackItemCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllyBeatBackItemCfg.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}

		Optional<AllyBeatBackEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		AllyBeatBackEntity entity = opEntity.get();
		if (cfg.getTimes() > 0 && entity.getExchangeTimes(exchangeId) + num  > cfg.getTimes()) {
			return Result.fail(Status.Error.ITEM_EXCHANGE_TIME_LIMIT_VALUE);
		}
		
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.ALLY_BEAT_BACK_EXCHANGE_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.ALLY_BEAT_BACK_EXCHANGE_REWARD, true, RewardOrginType.ALLY_BEAT_BACK_EXCHANGE_REWARD);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), exchangeId);
		Map<Integer, Integer> itemMap = getItemMap(cfg.getGainItemList());
		//支持多个兑换
		for (Entry<Integer, Integer> entry : itemMap.entrySet()) {
			entry.setValue(entry.getValue() * num);
		}
		ActivityManager.getInstance().postEvent(new AllyBeatBackGetItemEvent(playerId, itemMap));
		
		entity.addExchangeTimes(exchangeId, num);

		return Result.success();
	}
	
	private Map<Integer, Integer> getItemMap(List<RewardItem.Builder> builderList) {
		Map<Integer, Integer> map = new HashMap<>();
		for (RewardItem.Builder builder : builderList) {
			Integer num = map.get(builder.getItemId());
			if (num == null) {
				map.put(builder.getItemId(), (int)builder.getItemCount());
			} else {
				map.put(builder.getItemId(), (int)builder.getItemCount() + num);
			}
		}
		
		return map;
	} 
	
	public Result<Void> receiveReward(String playerId) {
		Optional<AllyBeatBackEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		AllyBeatBackEntity entity = opEntity.get();
		if (!isFinishAllAchieve(entity)) {
			return Result.fail(Status.Error.HELL_FIRE_NOT_FINISH_VALUE);
		}
		
		if (entity.getReceivedTime() != 0 && HawkTime.isSameDay(entity.getReceivedTime() * 1000l, HawkTime.getMillisecond())) {
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		
		HawkLog.logPrintln("AllyBeatBackReceiveReward playerId:{}", playerId);
		entity.setReceivedTime(HawkTime.getSeconds());
		AllyBeatBackCfg config = AllyBeatBackCfg.getInstance(); 
		this.getDataGeter().takeReward(playerId, config.getEverydayAwardList(), Action.ALLY_BEAT_BACK_EXTRA_REWARD, true);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), 0);
		this.pushInfo(playerId, entity);
		
		return Result.success(); 
	}
	
	private boolean isFinishAllAchieve(AllyBeatBackEntity entity) {
		List<AchieveItem> itemList = entity.getItemList();
		if (itemList.isEmpty()) {
			return false;
		}
		
		for (AchieveItem item : itemList) {
			if (item.getState() == AchieveState.ACHIEVE_NOT_FINISH_VALUE) {
				return false;
			}
		}
		
		return true;
	}
	
	public void pushInfo(String playerId, AllyBeatBackEntity activity) {		
		AllyBeatBackInfoResp.Builder sbuilder = AllyBeatBackInfoResp.newBuilder();
		sbuilder.setReceivedTime(activity.getReceivedTime());
		for (Entry<Integer, Integer> entry : activity.getExchengeTimesMap().entrySet()) {
			sbuilder.addExchangeTimes(KeyValuePairInt.newBuilder().setKey(entry.getKey()).setVal(entry.getValue()));
		}
		HawkProtocol protocol = HawkProtocol.valueOf(HP.code.ALLY_BEAT_BACK_INFO_RESP, sbuilder);
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, protocol);
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<AllyBeatBackEntity> opEntity = this.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		AllyBeatBackEntity activity = opEntity.get();
		this.pushInfo(playerId, activity);
	} 
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
