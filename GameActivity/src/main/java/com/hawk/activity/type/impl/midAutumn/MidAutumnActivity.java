package com.hawk.activity.type.impl.midAutumn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
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
import com.hawk.activity.event.impl.LoginDayMidAutumnEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.WishingEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnAchieveCfg;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnDropCfg;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnExchangeCfg;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnGiftCfg;
import com.hawk.activity.type.impl.midAutumn.cfg.MidAutumnRewardCfg;
import com.hawk.activity.type.impl.midAutumn.entity.MidAutumnEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.BrokenExchangeOper;
import com.hawk.game.protocol.Activity.MidAutumnExchangeMsg;
import com.hawk.game.protocol.Activity.MidAutumnGiftBuyReq;
import com.hawk.game.protocol.Activity.MidAutumnGiftInfoMsg;
import com.hawk.game.protocol.Activity.MidAutumnInfoSyn;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst;
import com.hawk.log.Action;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 中秋庆典
 * @author Winder
 *
 */
public class MidAutumnActivity extends ActivityBase implements AchieveProvider{
	
	public final Logger logger = LoggerFactory.getLogger("Server");
	
	public MidAutumnActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.MID_AUTUMN_ACTIVITY;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MidAutumnActivity activity = new MidAutumnActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
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
		Optional<MidAutumnEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		MidAutumnEntity playerDataEntity = optional.get();
		if (playerDataEntity.getItemList().isEmpty()) {
			this.initAchieve(playerId);
		}
		AchieveItems items = new AchieveItems(playerDataEntity.getItemList(), playerDataEntity);
		return Optional.of(items);
	}
	
	/**
	 * 玩家初始化成就数据
	 * @param playerId
	 */
	private void initAchieve(String playerId){
		Optional<MidAutumnEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		//为空初始化
		MidAutumnEntity entity = optional.get();
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		ConfigIterator<MidAutumnAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MidAutumnAchieveCfg.class);
		List<AchieveItem> itemList = new ArrayList<>();
		while (configIterator.hasNext()) {
			MidAutumnAchieveCfg cfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			itemList.add(item);
		}
		entity.setItemList(itemList);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);
		ActivityManager.getInstance().postEvent(new LoginDayMidAutumnEvent(playerId, 1), true);
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MidAutumnEntity> queryList = HawkDBManager.getInstance()
				.query("from MidAutumnEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			return queryList.get(0);
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MidAutumnEntity entity = new MidAutumnEntity(playerId, termId);
		entity.setExchangeNum("");
		entity.setExchangeNumMap(new HashMap<>());
		entity.setBuyGiftNum("");
		entity.setBuyGiftList(new ArrayList<>());
		ConfigIterator<MidAutumnExchangeCfg> ite = HawkConfigManager.getInstance().getConfigIterator(MidAutumnExchangeCfg.class);
		List<Integer> ids = new ArrayList<Integer>();
		while(ite.hasNext()){
			MidAutumnExchangeCfg config = ite.next();
			ids.add(config.getId());
		}
		entity.initTips(ids);
		return entity;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<MidAutumnEntity> optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		syncActivityInfo(playerId, optional.get());
	}

	@Override
	public void onOpen() {
		Set<String> playerIds = this.getDataGeter().getOnlinePlayers();
		for (String playerId : playerIds) {
			this.callBack(playerId, GameConst.MsgId.ACHIEVE_INIT_MID_AUTUMN, ()->{
				initAchieve(playerId);
			});
		}
	}
	
	/** 同步活动ALL数据
	 * @param playerId
	 * @param midAutumnEntity
	 */
	private void syncActivityInfo(String playerId, MidAutumnEntity midAutumnEntity){
		MidAutumnInfoSyn.Builder builder = MidAutumnInfoSyn.newBuilder();
		builder.addAllExchangeInfo(makeMidAutumnExchangeMsg(midAutumnEntity));
		builder.addAllGiftInfo(makeMidAutumnGiftInfoMsg(midAutumnEntity));
		for(Integer id : midAutumnEntity.getPlayerPoints()){
			builder.addTips(id);
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.MID_AUTUMN_EXCHANGE_INFO_SYN, builder));
	}
	
	/**
	 * 生成兑换物品pro
	 * @param midAutumnEntity 
	 * @return
	 */
	public List<MidAutumnExchangeMsg> makeMidAutumnExchangeMsg(MidAutumnEntity midAutumnEntity){
		List<MidAutumnExchangeMsg> list = new ArrayList<>();
		if (midAutumnEntity.getExchangeNumMap() != null && !midAutumnEntity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : midAutumnEntity.getExchangeNumMap().entrySet()) {
				MidAutumnExchangeMsg.Builder exBuilder = MidAutumnExchangeMsg.newBuilder();
				exBuilder.setExchangeId(entry.getKey());
				exBuilder.setNum(entry.getValue());
				list.add(exBuilder.build());
			}
		}
		return list;
	}
	
	/**
	 * 生成购买礼包pro
	 * @param midAutumnEntity
	 * @return
	 */
	public List<MidAutumnGiftInfoMsg> makeMidAutumnGiftInfoMsg(MidAutumnEntity midAutumnEntity){
		List<MidAutumnGiftInfoMsg> listPro = new ArrayList<>();
		if (midAutumnEntity.getBuyGiftList() != null && !midAutumnEntity.getBuyGiftList().isEmpty()) {
			for (String buyGiftStr : midAutumnEntity.getBuyGiftList()) {
				MidAutumnGiftInfoMsg.Builder giftBuilder = MidAutumnGiftInfoMsg.newBuilder();
				List<Integer> list = SerializeHelper.cfgStr2List(buyGiftStr);
				if (list.size() < 2) {  //小与2 为无效数据
					continue;
				}
				giftBuilder.setGiftId(list.get(0));
				for (int i = 1; i < list.size(); i++) {
					giftBuilder.addRewardId(list.get(i));
				}
				listPro.add(giftBuilder.build());
				
			}
		}
		return listPro;
	}

	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
	}

	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(MidAutumnAchieveCfg.class, achieveId);
	}

	@Override
	public Action takeRewardAction() {
		return Action.MID_AUTUMN_REWARD;
	}
	
	/**
	 * 兑换物品
	 * @param playerId
	 * @param exchangeId
	 * @param num
	 * @return
	 */
	public Result<Void> exchange(String playerId, int exchangeId, int num) {
		MidAutumnExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnExchangeCfg.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<MidAutumnEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MidAutumnEntity entity = opEntity.get();
		Integer exchangeNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (exchangeNum == null ? 0 : exchangeNum) + num;
		if (newNum > cfg.getMaxTime()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.MID_AUTUMN_EXCHANGE_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.MID_AUTUMN_EXCHANGE_REWARD, true, RewardOrginType.MID_AUTUMN_EXCHANGE_REWARD);
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), exchangeId);
		logger.info("midAutumn exchange playerId:{}, exchangeId:{}, num: {}", playerId, exchangeId, num);
		//兑换物品-客户端自己维护一下 已兑换的信息noPush
		this.syncActivityDataInfo(playerId);
		return Result.success();
	}

	/**购买礼包
	 * @param playerId
	 * @param buyReq
	 * @return
	 */
	public Result<Void> buyGift(String playerId, MidAutumnGiftBuyReq buyReq) {
		MidAutumnGiftInfoMsg giftInfoMsg = buyReq.getBuyGiftInfo();
		//礼包id
		int buyGiftId = giftInfoMsg.getGiftId();
		MidAutumnGiftCfg giftCfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnGiftCfg.class, buyGiftId);
		if (giftCfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Optional<MidAutumnEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MidAutumnEntity midAutumnEntity = opEntity.get();
		List<String> buyGiftList = midAutumnEntity.getBuyGiftList();
		if (buyGiftList != null && !buyGiftList.isEmpty()) {
			for (String buyGiftStr : buyGiftList) {
				List<Integer> list = SerializeHelper.cfgStr2List(buyGiftStr);
				if (!list.isEmpty() && list.get(0) == buyGiftId) {
					return Result.fail(Status.Error.MID_AUTUMN_GIFT_BOUGHT_VALUE);
				}
			}
		}
		//礼包数据
		List<Integer> rewardIds = giftInfoMsg.getRewardIdList();
		for (Integer rewardId : rewardIds) {
			MidAutumnRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnRewardCfg.class, rewardId);
			if (rewardCfg == null) {
				return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
			}
		}
		
		boolean flag = this.getDataGeter().cost(playerId, giftCfg.getCostItemList(), 1, Action.MID_AUTUMN_BUY_GIFT_COST, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//购买礼包的数据String
		StringBuilder buyGiftBuilder = new StringBuilder();
		StringBuilder buyGiftBuilderLog = new StringBuilder();
		buyGiftBuilder.append(buyGiftId);
		List<RewardItem.Builder> lastRewardList = new ArrayList<>();
		for (int i = 0; i < rewardIds.size(); i++) {
			if (i != 0) {
				buyGiftBuilderLog.append("_");
			}
			int rewardId = rewardIds.get(i);
			buyGiftBuilderLog.append(rewardId);
			//奖励ID
			MidAutumnRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnRewardCfg.class, rewardId);
			//奖励
			List<RewardItem.Builder> rewardList = rewardCfg.getRewardItemList();
			lastRewardList.addAll(rewardList);
			//位置(list 遍历按顺序直接拼接String)
			buyGiftBuilder.append("_");
			buyGiftBuilder.append(rewardId);
		}
		String buyGiftStr = buyGiftBuilder.toString();
		if (!buyGiftStr.isEmpty()) {
			midAutumnEntity.addBuyGiftList(buyGiftStr);
		}
		// 添加物品
		this.getDataGeter().takeReward(playerId, lastRewardList, 1, Action.MID_AUTUMN_BUY_GIFT_REWARD, true, RewardOrginType.MID_AUTUMN_BUY_GIFT_REWARD);
		this.getDataGeter().logMidAutumnGift(playerId, buyGiftId, buyGiftBuilderLog.toString());
		//同步购买的礼包数据
		syncActivityDataInfo(playerId);
		logger.info("midAutumn buyGift playerId:{}, buyGiftInfo:{} ", playerId, buyGiftBuilder);
		return Result.success();
	}
	
	/***
	 * 客户端勾提醒兑换
	 * @param playerId
	 * @param tips : 0为去掉 1为增加
	 */
	public Result<?> reqActivityTips(String playerId, int id, int tips){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<MidAutumnEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		MidAutumnEntity entity = opt.get();
		if(tips > 0){
			entity.addTips(id);
		}else{
			entity.removeTips(id);
		}
		return Result.success();
	}
	
	@Subscribe
	public void onEvent(ContinueLoginEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		String playerId = event.getPlayerId();
		int termId = getActivityTermId(playerId);
		long endTime = getTimeControl().getEndTimeByTermId(termId, playerId);
		long now = HawkTime.getMillisecond();
		if (now >= endTime) {
			return;
		}
		if (!event.isCrossDay()) {
			return;
		}
		Optional<MidAutumnEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MidAutumnEntity entity = optional.get();
		/**
		 * 成就处理
		 */
		List<AchieveItem> oldItems = entity.getItemList();
		//成就中,登录不重置任务数据
		List<AchieveItem> retainList = new ArrayList<>();
		for (AchieveItem item : oldItems) {
			MidAutumnAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnAchieveCfg.class, item.getAchieveId());
			if (achieveCfg != null && achieveCfg.getIsReset() == 0) {
				retainList.add(item);
			}
		}
		//如果为空，初始化
		boolean idRetainEmpty = retainList.isEmpty();
		//if (retainList.isEmpty()) {}
		ConfigIterator<MidAutumnAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(MidAutumnAchieveCfg.class);
		while (configIterator.hasNext()) {
			MidAutumnAchieveCfg cfg = configIterator.next();
			if (!idRetainEmpty && cfg.getIsReset() == 0) {
				continue;
			}
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			retainList.add(item);
		}
	
		// 初始化成就数据
		entity.resetItemList(retainList);
		//登录
		if (event.isCrossDay() && !HawkTime.isSameDay(entity.getRefreshTime(), now)) {
			entity.setLoginDays(entity.getLoginDays() + 1);
			entity.setRefreshTime(now);
		}
		ActivityManager.getInstance().postEvent(new LoginDayMidAutumnEvent(playerId, entity.getLoginDays()), true);
		// 推送给客户端
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		/**
		 * 兑换,购买礼包数据处理
		 */
		resetMidAutumnData(playerId);
		//同步数据
		syncActivityDataInfo(playerId);
	}

	/**重置相关数据
	 * @param playerId
	 */
	public void resetMidAutumnData(String playerId){
		Optional<MidAutumnEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		MidAutumnEntity entity = optional.get();
		entity.setBeatYuriTimes(0);
		entity.setWishTimes(0);
		entity.setBuyGiftNum("");
		entity.setExchangeNum("");
		if (entity.getExchangeNumMap() != null) {
			entity.getExchangeNumMap().clear();
		}
		if (entity.getBuyGiftList() != null) {
			entity.getBuyGiftList().clear();
		}
		entity.notifyUpdate();
	}
	
	
	@Subscribe
	public void wishingEvent(WishingEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<MidAutumnEntity> optional = getPlayerDataEntity(event.getPlayerId());
		if (!optional.isPresent()) {
			return;
		}
		MidAutumnDropCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnDropCfg.class, BrokenExchangeOper.WISH_VALUE);
		if (cfg == null) {
			return;
		}
		MidAutumnEntity entity = optional.get();
		entity.setWishTimes(entity.getWishTimes() + 1);
		
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), 1, Action.MID_AUTUMN_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
		}
		logger.info("midAutumn activity wishing playerId:{}, wishTimes:{}", event.getPlayerId(), entity.getWishTimes());
	}
	
	//奖励邮件
	private void takeReward(String playerId, int dropId, int num, Action action, int mailId,
			String name, String activityName) {		
		this.getDataGeter().takeReward(playerId, dropId, num, action, mailId, name, activityName, false, null);
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
		Optional<MidAutumnEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		MidAutumnEntity entity = opEntity.get();
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		MidAutumnDropCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MidAutumnDropCfg.class, BrokenExchangeOper.BEAT_YURI_VALUE);
		if (cfg == null) {
			return;
		}
		if (atkTimes >= cfg.getDropParam()) {
			takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes, Action.MID_AUTUMN_TASK_REWARD, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setBeatYuriTimes(0);
		}
		logger.info("midAutumn activity beatYuri playerId:{}, beatTimes:{}, totalBeatTimes", event.getPlayerId(), atkTimes, entity.getBeatYuriTimes());
	}
	
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}
	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}
	
}
