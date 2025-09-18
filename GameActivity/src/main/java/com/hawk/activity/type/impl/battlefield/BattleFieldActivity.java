package com.hawk.activity.type.impl.battlefield;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;
import org.hawk.tuple.HawkTuple3;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BattleFieldPurchaseEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.helper.AchievePushHelper;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldAchieveCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldActivityKVCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldCellAwardCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldCircleAwardCfg;
import com.hawk.activity.type.impl.battlefield.cfg.BattleFieldDailyLoginAwardCfg;
import com.hawk.activity.type.impl.battlefield.entity.BattleFieldEntity;
import com.hawk.game.protocol.Activity.BattleFieldActivityInfo;
import com.hawk.game.protocol.Activity.BattleFieldDiceRollResp;
import com.hawk.game.protocol.Activity.BattleFieldDiceType;
import com.hawk.game.protocol.Activity.BattleFieldLoginRewardResp;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.SysProtocol.ActivityBtns;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 战场寻宝活动
 * 
 * @author lating
 *
 */
public class BattleFieldActivity extends ActivityBase implements AchieveProvider {

	public BattleFieldActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
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
	public ActivityType getActivityType() {
		return ActivityType.BATTLE_FIELD_ACTIVITY;
	}
	
	public Action takeRewardAction() {
		return Action.BATTLE_FIELD_ACHIEVE_AWARD;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		BattleFieldActivity activity = new BattleFieldActivity(config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<BattleFieldEntity> queryList = HawkDBManager.getInstance()
				.query("from BattleFieldEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			BattleFieldEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		BattleFieldEntity entity = new BattleFieldEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ACHIEVE_INIT_BATTLE_FIELD, ()-> {
				initAchieveInfo(playerId);
				syncCellInfo(playerId);
			});
		}
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BattleFieldEntity entity = opEntity.get();
		if (entity.getItemList().isEmpty()) {
			initAchieveInfo(playerId);
		}
		
		syncCellInfo(playerId);
	}
	
	/**
	 * 初始化成就信息
	 * 
	 * @param playerId
	 */
	private void initAchieveInfo(String playerId) {
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BattleFieldEntity entity = opEntity.get();
		// 成就已初始化
		if (!entity.getItemList().isEmpty()) {
			return;
		}
		
		List<AchieveItem> itemList = new ArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<BattleFieldAchieveCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(BattleFieldAchieveCfg.class);
		while (configIterator.hasNext()) {
			BattleFieldAchieveCfg achieveCfg = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
			itemList.add(item);
		}

		entity.resetItemList(itemList);
		
		if (entity.getLoginDays() == 0) {
			entity.setLoginDays(1); // 首次进入即为第一天登录
		}
		HawkLog.logPrintln("battle field treasure activity acheiveItem init success, playerId: {}", playerId);
		// 初始化成就数据
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(playerId, entity.getItemList()), true);	
		// 初始化奖池
		if (entity.getPoolAwardList().isEmpty()) {
			refreshPoolAward(entity);
		}
	}

	/**
	 * 刷新奖池奖励
	 * 
	 * @param entity
	 */
	private void refreshPoolAward(BattleFieldEntity entity) {
		List<Integer> poolAwardList = new ArrayList<Integer>();
		BattleFieldActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		List<Integer> poolIds = kvcfg.getAwardPoolOrderList();
		for (int poolId : poolIds) {
			BattleFieldCircleAwardCfg poolCfg = HawkConfigManager.getInstance().getConfigByKey(BattleFieldCircleAwardCfg.class, poolId);
			if (poolCfg == null) {
				continue;
			}
		
			List<Integer> awardList = poolCfg.getAwardIdList();
			Collections.shuffle(awardList);
			poolAwardList.addAll(awardList);
		}
		
		entity.resetPoolAwardList(poolAwardList);
		HawkLog.logPrintln("battle field treasure activity pool award refresh success, playerId: {}", entity.getPlayerId());
	}
	
	/**
	 * 同步信息
	 * 
	 * @param entity
	 */
	public void syncCellInfo(String playerId) {
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BattleFieldEntity entity = opEntity.get();
		BattleFieldActivityInfo.Builder builder = BattleFieldActivityInfo.newBuilder();
		builder.setStay(entity.getCellId());
		builder.setBuyGift(entity.getBuyTime() > 0);
		builder.setLoginDays(entity.getLoginDays());
		builder.setFixedTimes(entity.getFixedRollTimes());
		builder.setRandomTimes(entity.getRandomRollTimes());
		builder.addAllRewardDay(entity.getReceiveDayList());
		// 格子奖励
		builder.addAllAwardId(entity.getPoolAwardList());
		
		BattleFieldActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		int randomDiceCount = this.getDataGeter().getItemNum(playerId, kvcfg.getRandomDiceItemId());
		int fixedDiceCount = this.getDataGeter().getItemNum(playerId, kvcfg.getFixedDiceItemId());
		builder.setRandomPointDiceCount(randomDiceCount);
		builder.setFixedPointDiceCount(fixedDiceCount);
		builder.addAllPassedCell(entity.getPassedCellList());
		builder.setBuyOrdinary(entity.getBuyOrdinary());
		builder.setBuyControl(entity.getBuyControl());
		builder.setYijianpaotuCnt(Math.max(0, kvcfg.getOneTimeLimit() - entity.getYijianpaotu()));
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BATTLE_FIELD_ACTIVITY_INFO_SYNC_VALUE, builder));
	}
	
	/**
	 * 购买骰子道具：单日购买有上限，投掷次数无上限
	 * 
	 * @param playerId
	 * @param type 骰子类型：1随机点数的骰子，或者2固定点数的骰子
	 * @param num  数量
	 * @return
	 */
	public int buy(String playerId, int type, int num){
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return 0;
		}
		
		BattleFieldActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		
		BattleFieldEntity entity = opEntity.get();
		int holdNumber = type == BattleFieldDiceType.RANDOM_POINT_DICE_VALUE ? entity.getBuyOrdinary() : entity.getBuyControl();
		int limitNumber = type == BattleFieldDiceType.RANDOM_POINT_DICE_VALUE ? kvcfg.getOrdinaryDiceLimit() : kvcfg.getControlDiceLimit();
		if(holdNumber + num > limitNumber ){
			return Status.Error.BATTLE_FIELD_BUY_DICE_OVERLIMIT_VALUE;
		}

		String consumeItems = type == BattleFieldDiceType.RANDOM_POINT_DICE_VALUE ? kvcfg.getOrdinaryDicePrice() : kvcfg.getControlDicePrice();
		Reward.RewardItem.Builder itemPrice = RewardHelper.toRewardItem(consumeItems);
		itemPrice.setItemCount(itemPrice.getItemCount() * num);
		List<RewardItem.Builder> costList = new ArrayList<RewardItem.Builder>();
		costList.add(itemPrice);	
		
		boolean success = getDataGeter().consumeItems(playerId, costList, HP.code.BATTLE_FIELD_DICE_BUY_REQ_VALUE, Action.BATTLE_FIELD_BUYDICE_CONSUME);
		if (!success) {
			HawkLog.errPrintln("battle field activity buy dice consume not enought, playerId: {}", playerId);
			return Status.Error.BATTLE_FIELD_BUY_DICE_FAILED_VALUE;
		}
		int itemId = 0;
		if(type == BattleFieldDiceType.FIXED_POINT_DICE_VALUE){
			entity.setBuyControl(entity.getBuyControl() + num);
			itemId = kvcfg.getFixedDiceItemId();
		}else{
			entity.setBuyOrdinary(entity.getBuyOrdinary() + num);
			itemId = kvcfg.getRandomDiceItemId();
		}
		
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		RewardItem.Builder builder = RewardItem.newBuilder();
		builder.setItemType(ItemType.TOOL_VALUE);
		builder.setItemId(itemId);
		builder.setItemCount(num);
		rewardList.add(builder);
		
		this.getDataGeter().takeReward(playerId, rewardList, 1, Action.BATTLE_FIELD_BUYDICE_AWARD, true, RewardOrginType.EXCHANGE_REWARD);
		this.getDataGeter().logBattleFieldDice(playerId, entity.getTermId(), true, type, num, this.getDataGeter().getItemNum(playerId, itemId));
		
		syncCellInfo(playerId);
		return 0;
	}

	/**
	 * 跨天事件
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BattleFieldEntity entity = opEntity.get();
		if (!event.isCrossDay()) {
			return;
		}
		
		// 第一天初始化成就数据的时候已经设置过了，这里不用再设置了
		if (!HawkTime.isSameDay(entity.getCreateTime(), HawkTime.getMillisecond()) || entity.getLoginDays() == 0) {
			entity.setLoginDays(entity.getLoginDays() + 1);
		}
		entity.setFixedRollTimes(0);
		entity.setRandomRollTimes(0);
		entity.setBuyOrdinary(0);
		entity.setBuyControl(0);
		
		List<AchieveItem> items = new ArrayList<AchieveItem>();
		ConfigIterator<BattleFieldAchieveCfg> achieveIterator = HawkConfigManager.getInstance().getConfigIterator(BattleFieldAchieveCfg.class);
		while (achieveIterator.hasNext()) {
			BattleFieldAchieveCfg cfg = achieveIterator.next();
			AchieveItem item = AchieveItem.valueOf(cfg.getAchieveId());
			items.add(item);
		}
		
		entity.resetItemList(items);
		
		if (entity.getPoolAwardList().isEmpty()) {
			refreshPoolAward(entity);
		}
		
		HawkLog.logPrintln("battle field treasure activity cross day refresh acheiveItem success, playerId: {}", playerId);
		
		AchievePushHelper.pushAchieveUpdate(playerId, entity.getItemList());
		
		syncCellInfo(playerId);
	}
	
	/**
	 * 直购事件
	 * @param event
	 */
	@Subscribe
	public void onPurchaseEvent(BattleFieldPurchaseEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		BattleFieldEntity entity = opEntity.get();
		long oldTime = entity.getBuyTime();
		entity.setBuyTime(HawkTime.getMillisecond());
		this.getDataGeter().logBattleFieldBuyGift(playerId, entity.getTermId());
		HawkLog.logPrintln("battle field treasure buy gift success, playerId: {}, giftId: {}, oldTime: {}", playerId, event.getGiftId(), oldTime);
		syncCellInfo(playerId);
	}
	
	/**
	 * 摇骰子获取奖励
	 * 
	 * @param playerId
	 * @param type 骰子类型
	 * @param point 固定点数的骰子对应的点数, 0表示随机点数的骰子，实际点数是随机生成
	 * 
	 */
	public int onRollDice(String playerId, BattleFieldDiceType type, int point, boolean useGold, int protoType) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		BattleFieldEntity entity = opEntity.get();
		BattleFieldActivityKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		int itemId = kvcfg.getRandomDiceItemId();
		if (type == BattleFieldDiceType.FIXED_POINT_DICE) {
			itemId = kvcfg.getFixedDiceItemId();
		}else{
			itemId = kvcfg.getRandomDiceItemId();
		}
		
		boolean buyDice = false;
		// 消耗
		List<RewardItem.Builder> consumeItemList = new ArrayList<RewardItem.Builder>();
		int diceCount = this.getDataGeter().getItemNum(playerId, itemId);
		if (diceCount <= 0) {
			if (!useGold) {
				HawkLog.errPrintln("battle field activity roll dice consume not enought, playerId: {}, itemId: {}, useGolde: {}", playerId, itemId, useGold);
				return 0;
			} 
			
			buyDice = true;
			String award = type == BattleFieldDiceType.FIXED_POINT_DICE ? kvcfg.getControlDicePrice() : kvcfg.getOrdinaryDicePrice();
			List<RewardItem.Builder> dicePrice = RewardHelper.toRewardItemList(award);
			consumeItemList.addAll(dicePrice);
		} else {
			RewardItem.Builder builder = RewardItem.newBuilder();
			builder.setItemType(ItemType.TOOL_VALUE * 10000);
			builder.setItemId(itemId);
			builder.setItemCount(1);
			consumeItemList.add(builder);
		}
		
		boolean success = getDataGeter().consumeItems(playerId, consumeItemList, protoType, Action.BATTLE_FIELD_ROLLDICE_CONSUME);
		if (!success) {
			HawkLog.errPrintln("battle field activity roll dice consume not enought, playerId: {}", playerId);
			return 0;
		}
		
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		HawkTuple3<Integer, Integer, Boolean> result = rollDiceOnce(entity, point, rewardList);
		int cellId = result.first;
		point = result.second;
		
		if (type == BattleFieldDiceType.FIXED_POINT_DICE) {
			entity.setFixedRollTimes(entity.getFixedRollTimes() + 1);
		} else {
			entity.setRandomRollTimes(entity.getRandomRollTimes() + 1);
		}
		
		if (buyDice) {
			if (type == BattleFieldDiceType.FIXED_POINT_DICE) {
				entity.setBuyControl(entity.getBuyControl() + 1);
			} else {
				entity.setBuyOrdinary(entity.getBuyOrdinary() + 1);
			}
		}
		
		this.getDataGeter().logBattleFieldDice(playerId, entity.getTermId(), false, type.getNumber(), 1, Math.max(0, diceCount - 1));
		if (!rewardList.isEmpty()) {
			ActivityReward reward = new ActivityReward(rewardList, Action.BATTLE_FIELD_ROLLDICE_AWARD);
			reward.setAlert(false);
			postReward(playerId, reward, false);
		}
		
		BattleFieldDiceRollResp.Builder resp = BattleFieldDiceRollResp.newBuilder();
		resp.setBuyControl(entity.getBuyControl());
		resp.setBuyOrdinary(entity.getBuyOrdinary());
		resp.setType(type);
		resp.setPoint(point);  // 当超过终点时，point是保留真实的点数，还是和终点之间的差值点数
		resp.setStay(cellId);  // 当走到终点时，是否需要重置为0，看客户端需要
		rewardList.forEach(e -> resp.addRewards(e));
		
		resp.setFixedTimes(entity.getFixedRollTimes());
		resp.setRandomTimes(entity.getRandomRollTimes());
		int randomDiceCount = this.getDataGeter().getItemNum(playerId, kvcfg.getRandomDiceItemId());
		int fixedDiceCount = this.getDataGeter().getItemNum(playerId, kvcfg.getFixedDiceItemId());
		resp.setRandomPointDiceCount(randomDiceCount);
		resp.setFixedPointDiceCount(fixedDiceCount);
		resp.addAllPassedCell(entity.getPassedCellList());
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BATTLE_FIELD_ROLL_DICE_RESP_VALUE, resp));
		
		// 走完一圈，刷新奖励
		if (result.third) {
			refreshPoolAward(entity);
			syncCellInfo(playerId);
		}
		
		HawkLog.logPrintln("battle field activity roll dice success, playerId: {}, type: {}, cellId: {}, point: {}", playerId, type, cellId, point);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 单次投掷骰子
	 * 
	 * @param entity
	 * @return
	 */
	private HawkTuple3<Integer, Integer, Boolean> rollDiceOnce(BattleFieldEntity entity, int point, List<RewardItem.Builder> rewardList) {
		int count = entity.getCellAwardCount();
		BattleFieldActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(BattleFieldActivityKVCfg.class);
		if (point <= 0) {
			point = HawkRand.randInt(1, cfg.getPointLimit());
		}
		
		// 最终的目标格子 = 起始位置 + 随机出的步数
		int endCell = entity.getCellId() + point;
		endCell = Math.min(endCell, count);
		entity.setCellId(endCell);
		entity.addPassedCell(endCell);
		int awardId = entity.getAwardId(endCell - 1);
		
		BattleFieldCellAwardCfg awardCfg = HawkConfigManager.getInstance().getConfigByKey(BattleFieldCellAwardCfg.class, awardId);
		if (awardCfg == null) {
			HawkLog.errPrintln("battle field activity roll dice failed, config error, playerId: {}, awardId: {}", entity.getPlayerId(), awardId);
			return new HawkTuple3<Integer, Integer, Boolean>(endCell, point, false);
		}
		
		rewardList.addAll(RewardHelper.toRewardItemList(awardCfg.getRewards()));
		boolean over = false;
		if (endCell == count) {
			over = true;
			entity.setCellId(0);
			entity.clearPassedCells();
			rewardList.addAll(RewardHelper.toRewardItemList(cfg.getFinalAward()));
		}
		
		this.getDataGeter().logBattleFieldDiceReward(entity.getPlayerId(), entity.getTermId(), awardCfg.getAwardType(), awardId, endCell);
		return new HawkTuple3<Integer, Integer, Boolean>(endCell, point, over);
	}
	
	/**
	 * 领取累计登录天数的奖励
	 * 
	 * @param playerId
	 * @param day
	 * @param type
	 * @return
	 */
	public int onReceiveAccLoginAward(String playerId, int day, int type) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		BattleFieldEntity entity = opEntity.get();
		if (entity.getBuyTime() <= 0) {
			HawkLog.logPrintln("battle field activity receive login award failed, need buy gift, playerId: {}, day: {}", playerId, day);
			return Status.Error.BATTLE_FIELD_LOGIN_REWARD_NEED_PURCHARSE_VALUE;
		}
		
		if (day > entity.getLoginDays()) {
			HawkLog.logPrintln("battle field activity receive login award over loginDays, playerId: {}, day: {}, loginDays: {}", playerId, day, entity.getLoginDays());
			return Status.Error.BATTLE_FIELD_LOGIN_REWARD_DAY_OVERLIMIT_VALUE;
		}
		
		if (entity.getReceiveDayList().contains(day)) {
			HawkLog.logPrintln("battle field activity receive login award repeated, playerId: {}, day: {}", playerId, day);
			return Status.Error.BATTLE_FIELD_LOGIN_REWARD_REPEATED_VALUE;
		}
		
		entity.addReceiveDay(day);
		
		
		// 发奖
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemList(BattleFieldDailyLoginAwardCfg.getAward(day));
		this.getDataGeter().takeReward(playerId,rewardList, 1,Action.BATTLE_FIELD_LOGINDAY_AWARD, true, RewardOrginType.EXCHANGE_REWARD);
		
		BattleFieldLoginRewardResp.Builder resp = BattleFieldLoginRewardResp.newBuilder();
		resp.addAllRewardDay(entity.getReceiveDayList());
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.BATTLE_FIELD_LOGIN_REWARD_RESP_VALUE, resp));
		
		HawkLog.logPrintln("battle field activity receive login award success, playerId: {}, day: {}", playerId, day);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<BattleFieldEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Optional.empty();
		}
		
		BattleFieldEntity entity = opEntity.get();
		if(entity.getItemList().isEmpty()){
			initAchieveInfo(playerId);
		}
		
		AchieveItems achieveItems = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
		return Optional.of(achieveItems);
	}

	@Override
	public BattleFieldAchieveCfg getAchieveCfg(int achieveId) {
		return HawkConfigManager.getInstance().getConfigByKey(BattleFieldAchieveCfg.class, achieveId);
	}
	
	@Override
	public Result<?> onTakeReward(String playerId, int achieveId) {
		this.getDataGeter().recordActivityRewardClick(playerId, ActivityBtns.ActivityChildCellBtn, getActivityType(), achieveId);
		return Result.success();
	}

	@Override
	public void onTakeRewardSuccess(String playerId) {
		checkActivityClose(playerId);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
}
