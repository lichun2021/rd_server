package com.hawk.activity.type.impl.achieve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.constant.ActivityConst;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.event.ActivityEvent;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.extend.KeyValue;
import com.hawk.activity.helper.PlayerDataHelper;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.TakeAchieveRewardMultiReq;
import com.hawk.game.protocol.Activity.TakeAchieveRewardReq;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.serialize.string.SerializeHelper;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 成就管理
 * @author PhilChen
 *
 */
public class AchieveManager {

	static Logger logger = LoggerFactory.getLogger("Server");

	private static AchieveManager instance;
	private Map<Integer, KeyValue<AchieveConfig, AchieveProvider>> achieveConfigAndProviderMap = new HashMap<>();
	private AchieveManager() {
	}

	public static AchieveManager getInstance() {
		if (instance == null) {
			instance = new AchieveManager();
		}
		return instance;
	}

	/**
	 * 获取处于激活状态的指定成就id数据
	 * @param playerId
	 * @param achieveId
	 * @return
	 */
	public KeyValue<AchieveItem, AchieveItems> getActiveAchieveItem(String playerId, int achieveId) {
		List<AchieveProvider> providerList = AchieveContext.getProviders();
		for (AchieveProvider achieveProvider : providerList) {
			if (!achieveProvider.isProviderNeedSync(playerId)) {
				continue;
			}
			Optional<AchieveItems> opAchieveItems = achieveProvider.getAchieveItems(playerId);
			if (!opAchieveItems.isPresent()) {
				continue;
			}
			AchieveItems achieveItems = opAchieveItems.get();
			for (AchieveItem achieveItem : achieveItems.getItems()) {
				if (achieveItem.getAchieveId() == achieveId) {
					return new KeyValue<AchieveItem, AchieveItems>(achieveItem, achieveItems);
				}
			}
		}
		return null;
	}

	/**
	 * 成就统一事件监听
	 * @param event
	 */
	@Subscribe
	public void onEvent(ActivityEvent event) {
		long eventTime = HawkTime.getMillisecond();
		int cycleCnt = 0;
		String playerId = event.getPlayerId();
		List<AchieveParser<?>> parsers = AchieveContext.getParser(event.getClass());
		if (parsers == null || parsers.isEmpty()) {
			return;
		}
		
		List<AchieveItem> needPush = new ArrayList<>();
		
		for (AchieveParser<?> parser : parsers) {
			List<AchieveProvider> providers = AchieveContext.getProviders();
			for (AchieveProvider provider : providers) {
				if (!provider.isProviderActive(playerId)) {
					cycleCnt++;
					continue;
				}

				Optional<AchieveItems> opAchieveItems = provider.getAchieveItems(playerId);
				if (!opAchieveItems.isPresent()) {
					cycleCnt++;
					continue;
				}
				AchieveItems achieveItems = opAchieveItems.get();
				if (achieveItems.getItems().isEmpty()) {
					cycleCnt++;
					continue;
				}
				
				boolean update = false;
				for (AchieveItem achieveItem : achieveItems.getItems()) {
					// 更新具体成就数值和状态
					AchieveConfig achieveConfig = getAchieveConfig(achieveItem.getAchieveId());
					if (achieveConfig == null) {
						logger.error("achieve config not found, achieveId: {}", achieveItem.getAchieveId());
						cycleCnt++;
						continue;
					}
					if (achieveConfig.getAchieveType() != parser.geAchieveType()) {
						cycleCnt++;
						continue;
					}
					if(!provider.isProviderNeedUpdate(playerId, achieveItem.getAchieveId())){
						continue;
					}
					if (event.getActivityType() > 0 && event.getActivityType() != provider.providerActivityId()) {
						continue;
					}
					boolean temp = parser.updateAchieveData(achieveItem, achieveConfig, event, needPush);
					update = update || temp;
					cycleCnt++;
					if (temp && achieveItems.isNeedLog()) {
						// 记录打点日志
						PlayerDataHelper.getInstance().getDataGeter().logActivityAchieve(playerId, achieveItems.getActivityId(), 
								achieveItems.getTermId(), achieveItem.getAchieveId(), achieveItem.getState(), 
								SerializeHelper.collectionToString(achieveItem.getDataList(), SerializeHelper.BETWEEN_ITEMS));
					}
				}
				if (update) {
					achieveItems.getEntity().notifyUpdate();
				}
			}
		}
		
		if (!needPush.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, needPush);
		}
		eventTime = HawkTime.getMillisecond() - eventTime;
		if (eventTime >= ActivityConst.EVENT_TIMEOUT) {
			HawkLog.logPrintln("AchieveDealEvent timeout, playerId: {}, eventClass: {}, cycleCnt:{}, costtime: {}", playerId, event.getClass().getSimpleName(), cycleCnt, eventTime);
		}
	}

	/**
	 * 成就数据初始创建事件
	 * @param event
	 */
	@Subscribe
	public void onAchieveInit(AchieveItemCreateEvent event) {
		long eventTime = HawkTime.getMillisecond();
		Collection<AchieveItem> items = event.getItems();
		ArrayList<AchieveItem> needSync = new ArrayList<>();
		String playerId = event.getPlayerId();
		List<Integer> achieveIds = new ArrayList<>();
		for (AchieveItem achieveItem : items) {
			achieveIds.add(achieveItem.getAchieveId());
			AchieveConfig achieveConfig = getAchieveConfig(achieveItem.getAchieveId());
			if (achieveConfig == null) {
				logger.error("achieve config not found, achieveId: {}", achieveItem.getAchieveId());
				continue;
			}
			AchieveParser<?> parser = AchieveContext.getParser(achieveConfig.getAchieveType());
			if (parser == null) {
				logger.error("achieve parser not found, achieveId: {}, achieveType: {}", achieveConfig.getAchieveId(), achieveConfig.getAchieveType());
				continue;
			}
			try {
				boolean update = parser.initDataOnOpen(playerId, achieveItem, achieveConfig);
				if (update) {
					// 存在数据更新，向客户端推送同步消息
					if (parser.isFinish(achieveItem, achieveConfig)) {
						// 成就条件达成
						achieveItem.setState(AchieveState.NOT_REWARD_VALUE);

						logger.info("achieve finish, playerId: {}, achieveId: {}", playerId, achieveConfig.getAchieveId());

						KeyValue<AchieveConfig, AchieveProvider> configAndProvider = getAchieveConfigAndProvider(achieveConfig.getAchieveId());
						if (configAndProvider != null) {
							configAndProvider.getValue().onAchieveFinished(playerId, achieveItem);
						}
					}
				}
				needSync.add(achieveItem);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		AchievePushHelper.pushAchieveAdd(playerId, needSync);
		eventTime = HawkTime.getMillisecond() - eventTime;
		if (eventTime >= ActivityConst.EVENT_TIMEOUT) {
			HawkLog.logPrintln("AchieveInit timeout, playerId: {}, achieveIds: {}, costtime: {}", playerId, achieveIds, eventTime);
		}
	}

	/**
	 * 玩家登录事件
	 * @param playerId
	 * @return
	 */
	public boolean onPlayerLogin(String playerId) {
		syncAchieveInfo(playerId);
		return true;
	}

	/**
	 * 向客户端同步所有成就数据（集中下发所有成就项）
	 * @param playerId
	 */
	private void syncAchieveInfo(String playerId) {
		List<AchieveProvider> providerList = AchieveContext.getProviders();
		List<AchieveItem> itemList = new ArrayList<>();
		for (AchieveProvider achieveProvider : providerList) {
			if (!achieveProvider.isProviderNeedSync(playerId)) {
				continue;
			}
			
			Optional<AchieveItems> opAchieveItems = achieveProvider.getAchieveItems(playerId);
			if(!opAchieveItems.isPresent()){
				continue;
			}
			AchieveItems achieveItems = opAchieveItems.get();
			itemList.addAll(achieveItems.getItems());
		}
		AchievePushHelper.pushAchieveInfo(playerId, itemList);
	}

	public AchieveConfig getAchieveConfig(int achieveId) {
		KeyValue<AchieveConfig, AchieveProvider> kv = getAchieveConfigAndProvider(achieveId);
		if (kv != null) {
			return kv.getKey();
		}
		return null;
	}

	public KeyValue<AchieveConfig, AchieveProvider> getAchieveConfigAndProvider(int achieveId) {
		if (achieveConfigAndProviderMap.containsKey(achieveId)) {
			return achieveConfigAndProviderMap.get(achieveId);
		}

		List<AchieveProvider> providerList = AchieveContext.getProviders();
		for (AchieveProvider achieveProvider : providerList) {
			AchieveConfig config = achieveProvider.getAchieveCfg(achieveId);
			if (config != null) {
				KeyValue<AchieveConfig, AchieveProvider> result = new KeyValue<>(config, achieveProvider);
				achieveConfigAndProviderMap.put(achieveId, result);
				return result;
			}
		}
		return null;
	}

	public Map<Integer, KeyValue<AchieveConfig, AchieveProvider>> getAchieveConfigAndProviderMap() {
		return achieveConfigAndProviderMap;
	}

	/**
	 * 领取成就奖励
	 * @param playerId
	 * @param achieveId
	 * @return
	 */
	public Result<?> takeAchieveReward(String playerId, TakeAchieveRewardReq req) {
		int achieveId = req.getAchieveId();
		if (PlayerDataHelper.getInstance().getDataGeter().isTavernActivity(achieveId) 
				&& PlayerDataHelper.getInstance().getDataGeter().isGmClose(null)) {
			return Result.fail(Status.SysError.MODULE_CLOSED_VALUE);
		}
		
		KeyValue<AchieveConfig, AchieveProvider> configAndProvider = getAchieveConfigAndProvider(achieveId);
		if (configAndProvider == null) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		AchieveConfig achieveConfig = configAndProvider.getKey();
		KeyValue<AchieveItem, AchieveItems> keyValue = getActiveAchieveItem(playerId, achieveId);
		if (keyValue == null) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		
		AchieveItem achieveItem = keyValue.getKey();
		// 成就未达成
		if(achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE){
			return Result.fail(Status.Error.ACTIVITY_CAN_NOT_TAKE_REWARD_VALUE);
		}
		
		// 已领取
		if (achieveItem.getState() == AchieveState.TOOK_VALUE) {
			return Result.fail(Status.Error.ACTIVITY_REWARD_IS_TOOK_VALUE);
		}
		
		AchieveProvider provider = configAndProvider.getValue();
		// 八日庆典、成长基金、王者归来、使命战争、基金团购、英雄军团、精工锻造、登录基金
		Result<?> takeResult = provider.onTakeReward(playerId, achieveId);
		if (takeResult != null && takeResult.isFail()) {
			return takeResult;
		}
		
		achieveItem.setState(AchieveState.TOOK_VALUE);
		keyValue.getValue().getEntity().notifyUpdate();
		/**
		 * 领奖成功,回调通知进行活动关闭检测
		 */
		provider.onTakeRewardSuccess(playerId);
		boolean isNeedPush = provider.isNeedPush(playerId);
		
		List<RewardItem.Builder> reweardList = provider.getRewardList(playerId, achieveConfig);
		if (!reweardList.isEmpty()) {
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
			RewardOrginType orginType = req.hasOrgin() ? RewardOrginType.valueOf(req.getOrgin()) : RewardOrginType.ACTIVITY_REWARD;
			PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(reweardList, provider.takeRewardAction(), true, orginType, achieveId);
			HawkTaskManager.getInstance().postMsg(xid, msg);
			provider.onTakeRewardSuccessAfter(playerId, reweardList, achieveId);
		}

		if(isNeedPush){
			AchievePushHelper.pushAchieveUpdate(playerId, achieveItem);
		}
		AchieveItems achieveItems = keyValue.getValue();
		if (achieveItems.isNeedLog()) {
			PlayerDataHelper.getInstance().getDataGeter().logActivityAchieve(playerId, achieveItems.getActivityId(), 
					achieveItems.getTermId(), achieveItem.getAchieveId(), achieveItem.getState(), 
					SerializeHelper.collectionToString(achieveItem.getDataList(), SerializeHelper.BETWEEN_ITEMS));
		}

		logger.info("[achieve] take achieve reward. playerId: {}, achieveId: {}, source: {}", playerId, achieveId, provider.getClass().getSimpleName());
		return Result.success();
	}
	
	/**
	 * 领取成就奖励
	 * @param playerId
	 * @param req
	 * @return
	 */
	public Result<?> takeAchieveMultiReward(String playerId, TakeAchieveRewardMultiReq req) {
		List<Integer> achieveIdList = req.getAchieveIdList();
		List<RewardItem.Builder> rewardAllList = new ArrayList<>();
		List<AchieveItem> items = new ArrayList<>();
		Action action = Action.NULL;
		for (int achieveId : achieveIdList) {
			try {
				if (PlayerDataHelper.getInstance().getDataGeter().isTavernActivity(achieveId) 
						&& PlayerDataHelper.getInstance().getDataGeter().isGmClose(null)) {
					return Result.fail(Status.SysError.MODULE_CLOSED_VALUE);
				}
				
				KeyValue<AchieveConfig, AchieveProvider> configAndProvider = getAchieveConfigAndProvider(achieveId);
				if (configAndProvider == null) {
					return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
				}
				
				AchieveConfig achieveConfig = configAndProvider.getKey();
				KeyValue<AchieveItem, AchieveItems> keyValue = getActiveAchieveItem(playerId, achieveId);
				if (keyValue == null) {
					return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
				}
				
				AchieveItem achieveItem = keyValue.getKey();
				if(achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE){
					continue;
				}
				if (achieveItem.getState() == AchieveState.TOOK_VALUE) {
					continue;
				}
				AchieveProvider provider = configAndProvider.getValue();
				Result<?> takeResult = provider.onTakeReward(playerId, achieveId);
				if (takeResult != null && takeResult.isFail()) {
					continue;
				}
				
				achieveItem.setState(AchieveState.TOOK_VALUE);
				keyValue.getValue().getEntity().notifyUpdate();
				provider.onTakeRewardSuccess(playerId);
				boolean isNeedPush = provider.isNeedPush(playerId);
				List<RewardItem.Builder> rewardList = provider.getRewardList(playerId, achieveConfig);
				if (!rewardList.isEmpty()) {
					rewardAllList.addAll(rewardList);
					action = provider.takeRewardAction();
					provider.onTakeRewardSuccessAfter(playerId, rewardList, achieveId);
				}
				
				if(isNeedPush){
					items.add(achieveItem);
				}
				
				AchieveItems achieveItems = keyValue.getValue();
				if (achieveItems.isNeedLog()) {
					PlayerDataHelper.getInstance().getDataGeter().logActivityAchieve(playerId, achieveItems.getActivityId(), 
							achieveItems.getTermId(), achieveItem.getAchieveId(), achieveItem.getState(), 
							SerializeHelper.collectionToString(achieveItem.getDataList(), SerializeHelper.BETWEEN_ITEMS));
				}
				
				logger.info("[achieve] take achieve multi reward. playerId: {}, achieveId: {}, source: {}", playerId, achieveId, provider.getClass().getSimpleName());
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!rewardAllList.isEmpty()) {
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
			RewardOrginType orginType = req.hasOrgin() ? RewardOrginType.valueOf(req.getOrgin()) : RewardOrginType.ACTIVITY_REWARD;
			PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewardAllList, action, true, orginType, achieveIdList.get(0));
			HawkTaskManager.getInstance().postMsg(xid, msg);
		}
		
		if (!items.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, items);
		}
		
		return Result.success();
	}
	
	/**
	 * 成就触发特殊处理（不走统一处理方式）
	 * @param playerId
	 * @param provider
	 * @param achieveItems
	 * @param achieveType
	 * @param addVal
	 */
	public void onSpecialAchieve(AchieveProvider provider, String playerId, List<AchieveItem> achieveItems, AchieveType achieveType, int addVal) {
		List<AchieveItem> needPush = new ArrayList<>();
		for (AchieveItem achieveItem : achieveItems) {
			try {
				if (achieveItem.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
					continue;
				}
				AchieveConfig achieveConfig = provider.getAchieveCfg(achieveItem.getAchieveId());
				if (achieveConfig.getAchieveType() != achieveType) {
					continue;
				}
				int configValue = achieveConfig.getConditionValue(0);
				int totalVal = achieveItem.getValue(0)+ addVal;
				if (totalVal >= configValue) {
					totalVal = configValue;
					achieveItem.setState(AchieveState.NOT_REWARD_VALUE);
				}
				achieveItem.setValue(0, totalVal);
				needPush.add(achieveItem);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
		
		if (!needPush.isEmpty()) {
			AchievePushHelper.pushAchieveUpdate(playerId, needPush);
		}
	}

}
