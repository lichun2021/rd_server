package com.hawk.activity.type.impl.destinyRevolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.LoginDayDestinyRevolverEvent;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverAchieveCfg;
import com.hawk.activity.type.impl.destinyRevolver.cfg.DestinyRevolverCfg;
import com.hawk.activity.type.impl.destinyRevolver.entity.DestinyRevolverEntity;
import com.hawk.game.protocol.Activity.DRTarotInfo;
import com.hawk.game.protocol.Activity.DestinyRevolverPageInfoPush;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.log.Action;

/**
 * 命运左轮
 * @author golden
 *
 */
public class DestinyRevolverActivity extends ActivityBase implements AchieveProvider {

	private Map<String, Integer> gachaFiveTimesMap = new ConcurrentHashMap<>();
	
	public DestinyRevolverActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.DESTINY_REVOLVER;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		DestinyRevolverActivity activity = new DestinyRevolverActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<DestinyRevolverEntity> queryList = HawkDBManager.getInstance().query("from DestinyRevolverEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			DestinyRevolverEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		DestinyRevolverEntity entity = new DestinyRevolverEntity(playerId, termId);
		entity.resetItems();
		return entity;
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
		Optional<DestinyRevolverEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		DestinyRevolverEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	/**
	 * 初始化成就信息
	 */
	private void initAchieveInfo(String playerId) {
		Optional<DestinyRevolverEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		// 初始添加成就项
		ConfigIterator<DestinyRevolverAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(DestinyRevolverAchieveCfg.class);
		while (configIterator.hasNext()) {
			DestinyRevolverAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			entity.addItem(item);
		}
		entity.notifyUpdate();
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayDestinyRevolverEvent(playerId, 1), true);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(DestinyRevolverAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(DestinyRevolverAchieveCfg.class, achieveId);
		}
		return config;
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public Action takeRewardAction() {
		return Action.DESTINY_REVOLVER_ACHIEVE_AWARD;
	}
	
	/**
	 * 玩家登录
	 */
	@Override
	public void onPlayerLogin(String playerId) {
		syncPageInfo(playerId);
	}
	
	/**
	 * 推送界面信息
	 */
	public void syncPageInfo(String playerId) {
		
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<DestinyRevolverEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = opEntity.get();
		boolean inTarot = entity.isInTarot() && entity.getNineEndTime() > HawkTime.getMillisecond();
		if (!inTarot) {
			entity.resetItems();
			super.logger.info("destinyrevolver, syncPageInfo reset, playerId:{}", playerId);
		}
		
		DestinyRevolverPageInfoPush.Builder builder = DestinyRevolverPageInfoPush.newBuilder();
		builder.setFirstKick(entity.hasFirstKick());
		builder.setInTarot(inTarot);
		
		if (inTarot) {
			DRTarotInfo.Builder tarotInfo = DRTarotInfo.newBuilder();
			tarotInfo.setRemainTime(entity.getNineEndTime());
			tarotInfo.addAllRewardId(entity.getGridist());
			int openCount = (int) entity.getGridist().stream().filter(i -> i > 0).count();
			if (openCount < 9) {
				DestinyRevolverCfg cfg = HawkConfigManager.getInstance().getKVInstance(DestinyRevolverCfg.class);
				tarotInfo.setOpenCost(cfg.getNineCost());
			}
			builder.setTarotInfo(tarotInfo);
		}
		pushToPlayer(playerId, HP.code.DESTINY_REVOLVER_PAGE_INFO_PUSH_VALUE, builder);
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<DestinyRevolverEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		DestinyRevolverEntity entity = optional.get();
		List<AchieveItem> retainList = new ArrayList<>();
		//这个表重置
		ConfigIterator<DestinyRevolverAchieveCfg> taskIterator = HawkConfigManager.getInstance().getConfigIterator(DestinyRevolverAchieveCfg.class);
		while (taskIterator.hasNext()) {
			DestinyRevolverAchieveCfg cfg = taskIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
		entity.resetItemList(retainList);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayDestinyRevolverEvent(playerId, 1), true);
		//push
		AchievePushHelper.pushAchieveUpdate(playerId, retainList);
	}
	
	private String getPlayerGachaFiveKey(int termId, String playerId) {
		return ActivityRedisKey.DESTINY_REVOLVER_FIVE + ":" + termId + ":" + playerId;
	}
	
	public int getPlayerGachaFiveTimes(String playerId) {
		String key = getPlayerGachaFiveKey(getActivityTermId(), playerId);
		String times = ActivityLocalRedis.getInstance().get(key);
		if (HawkOSOperator.isEmptyString(times)) {
			return 0;
		}
		return Integer.parseInt(times);
	}
	
	public int addPlayerGachaFiveTimes(String playerId) {
		int beforeTimes = getPlayerGachaFiveTimes(playerId);
		int afterTimes = beforeTimes + 1;
		
		String key = getPlayerGachaFiveKey(getActivityTermId(), playerId);
		ActivityLocalRedis.getInstance().set(key, String.valueOf(afterTimes));
		
		return afterTimes;
	}
}
