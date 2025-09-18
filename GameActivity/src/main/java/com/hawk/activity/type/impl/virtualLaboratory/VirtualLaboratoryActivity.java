package com.hawk.activity.type.impl.virtualLaboratory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
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
import com.hawk.activity.event.impl.LoginDayVirtualLaboratoryEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.virtualLaboratory.cfg.VirtualLaboratoryAchieveCfg;
import com.hawk.activity.type.impl.virtualLaboratory.cfg.VirtualLaboratoryCardCfg;
import com.hawk.activity.type.impl.virtualLaboratory.cfg.VirtualLaboratoryKVCfg;
import com.hawk.activity.type.impl.virtualLaboratory.entity.VirtualLaboratoryEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.CardState;
import com.hawk.game.protocol.Activity.VirtualCardInfo;
import com.hawk.game.protocol.Activity.VirtualOpenCardResp;
import com.hawk.game.protocol.Activity.VirtualPageInfo;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * @Desc:武装技术（翻牌）
 * @author:Winder
 * @date:2020年6月8日
 */
public class VirtualLaboratoryActivity extends ActivityBase implements AchieveProvider{
	
	public final Logger logger = LoggerFactory.getLogger("Server");

	public VirtualLaboratoryActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.VIRTUAL_LABORATORY_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		VirtualLaboratoryActivity activity = new VirtualLaboratoryActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}
	
	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<VirtualLaboratoryEntity> queryList = HawkDBManager.getInstance()
				.query("from VirtualLaboratoryEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			VirtualLaboratoryEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		VirtualLaboratoryEntity entity = new VirtualLaboratoryEntity(playerId, termId);
		entity.setCardInfoList(getInitCardInfoList());
		entity.setOpenCardInfoList(new ArrayList<>());
		return entity;
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<VirtualLaboratoryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		VirtualLaboratoryEntity entity = opEntity.get();
		
		if (entity.getCardInfoList().isEmpty()) {
			return;
		}
		//所有开拍数据
		List<Integer> cardInfoList = entity.getCardInfoList();
		//已打开卡牌的index数据
		List<Integer> openCardInfoList = entity.getOpenCardInfoList();
		//同步界面信息
		VirtualPageInfo.Builder builder = VirtualPageInfo.newBuilder();
		
		for (int i = 0; i < cardInfoList.size(); i++) {
			int value = cardInfoList.get(i);
			VirtualCardInfo.Builder cardBuilder = VirtualCardInfo.newBuilder();
			cardBuilder.setCardIndex(i);
			cardBuilder.setCardValue(value);
			CardState state = CardState.CARD_CLOSE;
			//已翻卡牌的信息 
			if (openCardInfoList.contains(i)) {
				state = CardState.CARD_OPEN;
			}
			cardBuilder.setState(state);
			builder.addVirtualCardInfo(cardBuilder);
		}
		
		VirtualLaboratoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(VirtualLaboratoryKVCfg.class);
		int resetTimes = cfg.getDailyResetTimes() - entity.getResetNum();
		if (resetTimes < 0) {
			resetTimes = 0;
		}
		builder.setResetNum(resetTimes);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.VIRTUAL_CARD_INFO_RESP, builder));
	}

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_FLIGHT_PLAN, ()-> {
				initAchieveInfo(playerId);
			});
		}
	}

	/**初始化牌的数据 表里配7个随机6个出来复制,一共12个
	 * @return
	 */
	public List<Integer> getInitCardInfoList(){
		List<Integer> cardInfoList = new ArrayList<Integer>();
		ConfigIterator<VirtualLaboratoryCardCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(VirtualLaboratoryCardCfg.class);
		List<VirtualLaboratoryCardCfg> list = new ArrayList<>(configIterator.toList());
		Collections.shuffle(list);
		list.remove(0);
		for (VirtualLaboratoryCardCfg cardCfg : list) {
			cardInfoList.add(cardCfg.getId());
			cardInfoList.add(cardCfg.getId());
		}
		Collections.shuffle(cardInfoList);
		return cardInfoList;
	}
	 /**初始化卡牌信息
	 * @param playerId
	 */
	public void initCardInfo(String playerId){
		Optional<VirtualLaboratoryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		VirtualLaboratoryEntity entity = opEntity.get();
		// 卡牌已初始化
		List<Integer> cardInfoList = getInitCardInfoList();
		entity.resetCardInfoList(cardInfoList);
		entity.notifyUpdate();
	}
	/**
	 * 初始化成就信息
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<VirtualLaboratoryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		VirtualLaboratoryEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
				
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<VirtualLaboratoryAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(VirtualLaboratoryAchieveCfg.class);
		while (configIterator.hasNext()) {
			VirtualLaboratoryAchieveCfg achieveCfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			itemList.add(item);
		}
		entity.resetItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);	
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayVirtualLaboratoryEvent(playerId, 1), true);
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
		Optional<VirtualLaboratoryEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if(!opPlayerDataEntity.isPresent()){
			return Optional.empty();
		}
		VirtualLaboratoryEntity playerDataEntity = opPlayerDataEntity.get();
		if(playerDataEntity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}

	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig config = HawkConfigManager.getInstance().getConfigByKey(VirtualLaboratoryAchieveCfg.class, achieveId);
		if (config == null) {
			config = HawkConfigManager.getInstance().getConfigByKey(VirtualLaboratoryAchieveCfg.class, achieveId);
		}
		return config;
	}

	@Override
	public Action takeRewardAction() {
		return Action.VIRTUAL_LABORATORY_REWARD;
	}
	
	
	/** 翻开卡牌
	 * @param playerId
	 * @param cardIndex
	 * @return
	 */
	public Result<?> openCard(String playerId, int cardIndex, int cardTwoIndex){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<VirtualLaboratoryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		VirtualLaboratoryEntity entity = opEntity.get();
		//所有牌的数据
		List<Integer> cardInfoList = entity.getCardInfoList();
		//已打开卡牌的index数据
		List<Integer> openCardInfoList = entity.getOpenCardInfoList();
		//牌子位置固定,第一次每次翻牌固定不动,第二次判定是否相同,相同获得奖励,不相同 两张牌都扣上
		if (openCardInfoList.contains(cardIndex) || openCardInfoList.contains(cardTwoIndex)) {
			return Result.fail(Status.Error.VIRTUAL_CARD_IS_OPENED_VALUE);
		}
		//已开牌数据里,添加之后偶数检测,后两个是否相等,不想等则移除
		openCardInfoList.add(cardIndex);
		openCardInfoList.add(cardTwoIndex);
		/*int size = openCardInfoList.size();
		//是否是一对,中奖
		boolean isSendReward = false;
		//偶数检测
		if (size != 0 && size % 2 == 0) {
			int index1 = openCardInfoList.get(size - 1);
			int index2 = openCardInfoList.get(size - 2);
			int card1 = cardInfoList.get(index1);
			int card2 = cardInfoList.get(index2);
			if (card1 != card2) {
				openCardInfoList.remove(Integer.valueOf(index1));
				openCardInfoList.remove(Integer.valueOf(index2));
			}else{
				isSendReward = true;
			}
		}*/
		entity.notifyUpdate();
		
		VirtualOpenCardResp.Builder result = VirtualOpenCardResp.newBuilder();
		int cardValue = cardInfoList.get(cardIndex);
		result.setCardId(cardValue);
		//成对发奖励
		VirtualLaboratoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(VirtualLaboratoryKVCfg.class);
		//发奖
		int rewardId = cfg.getRandReward();
		List<RewardItem.Builder> rewardList = genRewardItem(rewardId, result);
		this.getDataGeter().takeReward(playerId, rewardList, Action.VIRTUAL_LABORATORY_REWARD, false);
		//Tlog 打点
		this.getDataGeter().logVirtualLaboratoryOpenCard(playerId, entity.getTermId(), cardIndex, cardTwoIndex, cardValue);
		//同步界面
		syncActivityDataInfo(playerId);
		return Result.success(result);
	}
	
	
	/**奖励
	 * @param rewardId
	 * @param result
	 * @return
	 */
	public List<RewardItem.Builder> genRewardItem(int rewardId, VirtualOpenCardResp.Builder result){
		List<String> rewardList = this.getDataGeter().getAwardFromAwardCfg(rewardId);
		List<RewardItem.Builder> rewardItemList = new ArrayList<>();
		for (String rewardStr : rewardList) {
			List<RewardItem.Builder> rewardBuilders = RewardHelper.toRewardItemList(rewardStr);
			rewardItemList.addAll(rewardBuilders);
		}
		for (RewardItem.Builder builder : rewardItemList) {
			result.addRewardItem(builder);
		}
		return rewardItemList;
	}
	
	/** 翻卡重置
	 * @param playerId
	 * @return
	 */
	public Result<?> resetCard(String playerId, int protoType){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<VirtualLaboratoryEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		VirtualLaboratoryEntity entity =opEntity.get();
		//已打开卡牌的index数据
		List<Integer> openCardInfoList = entity.getOpenCardInfoList();
		//所有牌都是关闭状态不可以重置
		if (openCardInfoList.isEmpty()) {
			return Result.fail(Status.Error.VIRTUAL_CARD_IS_ALL_CLOSE_VALUE);
		}
		List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
		
		//需要用金条购买的次数
		int needBuyCount = getOpenCardConsume(playerId, consumeItemList);
		if (needBuyCount > 0) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		
		boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.VIRTUAL_LABORATORY_CONSUME);
		if (!success) {
			logger.error("VirtualLaboratoryActivity open card consume not enought, playerId: {}", playerId);
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		VirtualLaboratoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(VirtualLaboratoryKVCfg.class);
		//城建礼包
		List<RewardItem.Builder> extReward = RewardHelper.toRewardItemList(cfg.getExtReward());
		this.getDataGeter().takeReward(playerId, extReward, Action.VIRTUAL_LABORATORY_REWARD, false);
		entity.setResetNum(entity.getResetNum() + 1);
		//重置卡牌信息
		initCardInfo(playerId);
		//sync
		syncActivityDataInfo(playerId);
		return Result.success();
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		try {
			if (isOpening(playerId)) {
				Optional<VirtualLaboratoryEntity> opDataEntity = this.getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				ActivityManager.getInstance().postEvent(new LoginDayVirtualLaboratoryEvent(playerId, 1), true);
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		Optional<VirtualLaboratoryEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		VirtualLaboratoryEntity entity = opPlayerDataEntity.get();
		
		List<AchieveItem> oldItems = entity.getItemList();
		//成就中,跨天不重置任务数据
		List<AchieveItem> retainList = new ArrayList<>();
		for (AchieveItem item : oldItems) {
			VirtualLaboratoryAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(VirtualLaboratoryAchieveCfg.class, item.getAchieveId());
			if (achieveCfg != null && achieveCfg.getIsReset() == 0) {
				retainList.add(item);
			}
		}
		//如果为空，初始化
		boolean idRetainEmpty = retainList.isEmpty();
		ConfigIterator<VirtualLaboratoryAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(VirtualLaboratoryAchieveCfg.class);
		while (configIterator.hasNext()) {
			VirtualLaboratoryAchieveCfg cfg = configIterator.next();
			if (!idRetainEmpty && cfg.getIsReset() == 0) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
		
		entity.resetItemList(retainList);
		//重置次数置为0
		entity.setResetNum(0);
		// 初始化成就数据
		//ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		//跨天登录
		ActivityManager.getInstance().postEvent(new LoginDayVirtualLaboratoryEvent(playerId, 1), true);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		//sync
		syncActivityDataInfo(playerId);
	}
	
	/**翻牌消耗
	 * @param playerId
	 * @param drewTimes
	 * @param consumeItemList
	 * @return
	 */
	private int getOpenCardConsume(String playerId, List<RewardItem.Builder> consumeItemList) {
		VirtualLaboratoryKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(VirtualLaboratoryKVCfg.class);
		//单次消耗
		RewardItem.Builder drewCounsumeItem = RewardHelper.toRewardItem(cfg.getSinglePrice());
		int drewCounsumItemId = drewCounsumeItem.getItemId();
		int haveDrewCount = this.getDataGeter().getItemNum(playerId, drewCounsumItemId);
		
		int totalCount = (int) drewCounsumeItem.getItemCount();
		//需要购买的次数
		int needBuyCount = totalCount - haveDrewCount;
		
		if (needBuyCount > 0) {
			RewardItem.Builder buyCounsumeItem = RewardHelper.toRewardItem(cfg.getItemPrice());
			buyCounsumeItem.setItemCount(buyCounsumeItem.getItemCount() * needBuyCount);
			consumeItemList.add(buyCounsumeItem);
			if (haveDrewCount > 0) {
				drewCounsumeItem.setItemCount(haveDrewCount);
				consumeItemList.add(drewCounsumeItem);
			}
		}else{
			drewCounsumeItem.setItemCount(totalCount);
			consumeItemList.add(drewCounsumeItem);
		}
		return needBuyCount;
		
	}
	
	

}
