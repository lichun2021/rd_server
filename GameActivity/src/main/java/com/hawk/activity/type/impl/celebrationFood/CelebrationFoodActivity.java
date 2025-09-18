package com.hawk.activity.type.impl.celebrationFood;
import java.util.ArrayList;
import java.util.Collection;
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

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.GuildDonateEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.ResourceCollectEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.celebrationFood.cfg.CelebrationFoodDropCfg;
import com.hawk.activity.type.impl.celebrationFood.cfg.CelebrationFoodKVCfg;
import com.hawk.activity.type.impl.celebrationFood.cfg.CelebrationFoodMakeCfg;
import com.hawk.activity.type.impl.celebrationFood.entity.CelebrationFoodEntity;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.CelebrationFoodInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.World.MonsterType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 周年庆-庆典美食(蛋糕制作)
 * hf
 */
public class CelebrationFoodActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public CelebrationFoodActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.CELEBRATION_FOOD_ACTIVITY;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CelebrationFoodActivity activity = new CelebrationFoodActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CelebrationFoodEntity> queryList = HawkDBManager.getInstance()
				.query("from CelebrationFoodEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CelebrationFoodEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CelebrationFoodEntity entity = new CelebrationFoodEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.CELEBRATION_FOOD_INIT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CelebrationFoodEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if(!event.isCrossDay()){
			return;
		}
		Optional<CelebrationFoodEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		CelebrationFoodEntity entity = opEntity.get();
		entity.setWolrdCollectRemainTime(0);
		entity.setWolrdCollectTimes(0);
		entity.setBeatYuriTimes(0);
		entity.setBeatYuriTotalTimes(0);
		entity.setWishTimes(0);
		entity.setWishTotalTimes(0);
		entity.notifyUpdate();
		logger.info("CelebrationFoodActivity onContinueLoginEvent,finish,playerId:{},isCross:{}",event.getPlayerId(),event.isCrossDay());
		
	}
	
	
	/**制作美食
	 * @param playerId
	 * @param level
	 * @return
	 */
	public Result<?> celebrationFoodMake(String playerId, int level){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<CelebrationFoodEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		CelebrationFoodEntity entity = opPlayerDataEntity.get();
		
		int currentLevel = entity.getFoodLevel();
		//等级不对
		if (currentLevel != level - 1) {
			return Result.fail(Status.Error.CELEBRATION_FOOD_LEVEL_ERROR_VALUE);
		}
		//道具消耗
		CelebrationFoodMakeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFoodMakeCfg.class, level);
		List<RewardItem.Builder> costItem = RewardHelper.toRewardItemList(cfg.getCakeCost());
		
		boolean success = this.getDataGeter().cost(playerId, costItem, Action.CELEBRATION_FOOD_REWARD);
		if (!success) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//成功发奖励
		List<RewardItem.Builder> rewards = getFoodRewards(level, entity);
		sendRewardMail(playerId, rewards);
		//db
		entity.setFoodLevel(level);
		entity.notifyUpdate();
		// tlog打点
		int termId = this.getActivityTermId();
		this.getDataGeter().logCelebrationFoodMake(playerId, termId, level);
		//push
		syncActivityDataInfo(playerId);
		logger.info("CelebrationFoodActivity celebrationFoodMake success playerId:{}, level:{}", playerId, level);
		
		return Result.success();
		
	}
	
	/**
	 * 礼包购买条件判断
	 * 
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public boolean buyGiftCheck(String playerId, String giftId) {
		int id = Integer.valueOf(giftId);
		CelebrationFoodKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(CelebrationFoodKVCfg.class);
		int index = -1;
		if (kvCfg.getAndroidPayId().contains(id)) {
			index = kvCfg.getAndroidPayId().indexOf(id);
		} else {
			index = kvCfg.getIosPayId().indexOf(id);
		}
		
		if (index < 0) {
			logger.error("celebrationFood buyGiftCheck, playerId: {}, giftId: {}, index: {}", playerId, id, index);
			return false;
		}
		
		Optional<CelebrationFoodEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			logger.error("celebrationFood buyGiftCheck, playerId: {}, giftId: {}, index: -99991", playerId, id);
			return false;
		}
		CelebrationFoodEntity entity = opPlayerDataEntity.get();
		if (index == 0 && entity.isBuyAdvance()) {
			logger.error("celebrationFood buyGiftCheck, buy advance already, playerId: {}, giftId: {}, index: {}", playerId, id, index);
			return false;
		}
		
		if (index > 0 && !entity.isBuyAdvance()) {
			logger.error("celebrationFood buyGiftCheck, buy advance ahead need, playerId: {}, giftId: {}, index: {}", playerId, id, index);
			return false;
		}
		
		if (index > 0 && entity.isBuySuper()) {
			logger.error("celebrationFood buyGiftCheck, buy super already, playerId: {}, giftId: {}, index: {}", playerId, id, index);
			return false;
		}
		
		return true;
	}
	
	@Subscribe
	public void onCelebrationFoodBuyEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		int id = Integer.valueOf(event.getGiftId());
		CelebrationFoodKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(CelebrationFoodKVCfg.class);
		int index = -1;
		if (kvCfg.getAndroidPayId().contains(id)) {
			index = kvCfg.getAndroidPayId().indexOf(id);
		} else {
			index = kvCfg.getIosPayId().indexOf(id);
		}
		
		if (index < 0) {
			logger.error("onCelebrationFoodBuyEvent is faild playerId:{}, giftId:{}", playerId, id);
			return;
		}
		//判断是否是此活动礼包
		if (!isOpening(playerId)) {
			return;
		}
		Optional<CelebrationFoodEntity> opPlayerDataEntity = getPlayerDataEntity(playerId);
		if (!opPlayerDataEntity.isPresent()) {
			return;
		}
		CelebrationFoodEntity entity = opPlayerDataEntity.get();
		if (index == 0) {
			entity.setBuyAdvance(true);
		} else {
			entity.setBuySuper(true);
		}
		entity.notifyUpdate();
		//购买成功补发高级版奖励
		int currentLevel = entity.getFoodLevel();
		List<RewardItem.Builder> reissueRewards = getReissueRewards(currentLevel, index);
		if (!reissueRewards.isEmpty()) {
			//邮件发奖
			sendRewardMail(playerId, reissueRewards);
		}
		//push
		syncActivityDataInfo(playerId);
		
		logger.info("CelebrationFoodActivity onCelebrationFoodBuyEvent success playerId:{}, currentLevel:{}", playerId, currentLevel);
	}
	
	
	/** 制作蛋糕的奖励
	 * @param currentLevel
	 * @return
	 */
	public List<RewardItem.Builder> getFoodRewards(int currentLevel, CelebrationFoodEntity entity){
		//所有奖励
		List<RewardItem.Builder> allRewards = new ArrayList<>();
		CelebrationFoodMakeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFoodMakeCfg.class, currentLevel);
		List<RewardItem.Builder> commonReward = RewardHelper.toRewardItemList(cfg.getRewards());
		allRewards.addAll(commonReward);
		if (entity.isBuyAdvance()) {
			List<RewardItem.Builder> advanceReward = RewardHelper.toRewardItemList(cfg.getBestRewards());
			allRewards.addAll(advanceReward);
		}
		
		if (entity.isBuySuper()) {
			List<RewardItem.Builder> superReward = RewardHelper.toRewardItemList(cfg.getSupreRewards());
			allRewards.addAll(superReward);
		}
		return allRewards;
	}
	
	
	/**改等级之前的所有高级奖励
	 * @param currentLevel
	 * @return
	 */
	public List<RewardItem.Builder> getReissueRewards(int currentLevel, int index){
		//所有奖励
		List<RewardItem.Builder> allRewards = new ArrayList<>();
		ConfigIterator<CelebrationFoodMakeCfg> configIterator = HawkConfigManager.getInstance().getConfigIterator(CelebrationFoodMakeCfg.class);
		while (configIterator.hasNext()) {
			CelebrationFoodMakeCfg cfg = configIterator.next();
			if (cfg.getLv() <= currentLevel) {
				String rewardCfg = index > 0 ? cfg.getSupreRewards() : cfg.getBestRewards();
				List<RewardItem.Builder> reward = RewardHelper.toRewardItemList(rewardCfg);
				allRewards.addAll(reward);
			}
		}
		return allRewards;
	}
	
	
	/** 普通升级奖励邮件
	 * @param playerId
	 * @param rewards
	 */
	public void sendRewardMail(String playerId, List<RewardItem.Builder> rewards){
		try {
			// 邮件发送奖励
			Object[] content = new Object[0];
			Object[] title = new Object[0];
			Object[] subTitle = new Object[0];
			//发邮件
			sendMailToPlayer(playerId, MailConst.MailId.CELEBRATION_FOOD_COMMON_REWARD, title, subTitle, content, rewards);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId, CelebrationFoodEntity entity){
		CelebrationFoodInfo.Builder builder = CelebrationFoodInfo.newBuilder();
		builder.setBuyBest(entity.isBuyAdvance());
		builder.setBuySuper(entity.isBuySuper());
		builder.setCurrentLevel(entity.getFoodLevel());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code.CELEBRATION_FOOD_INFO_SYNC_VALUE, builder));
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
		Optional<CelebrationFoodEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		CelebrationFoodEntity entity = opEntity.get();
		CelebrationFoodDropCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFoodDropCfg.class,Activity.BrokenExchangeOper.BEAT_YURI_VALUE);
		if (cfg == null) {
			return;
		}
		int totalTimes = entity.getBeatYuriTotalTimes();
		if(totalTimes >= cfg.getDropLimit()){
			return;
		}
		entity.setBeatYuriTimes(entity.getBeatYuriTimes() + atkTimes);
		if (atkTimes >= cfg.getDropParam()) {
			if (atkTimes > 1) {
				String rewards = getDataGeter().getItemAward(cfg.getDropId());
				ImmutableList<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(rewards);
				rewardList.forEach(e -> e.setItemCount(e.getItemCount() * atkTimes));
				this.getDataGeter().sendMail(event.getPlayerId(), MailId.CELEBRATION_FOOD_DROP_REWARD_MULTI, null, null, new Object[]{atkTimes}, rewardList, false);
			} else {
				this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), atkTimes,
						Action.CELEBRATION_FOOD_BEAT_YURI, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			}
			entity.setBeatYuriTimes(0);
			entity.setBeatYuriTotalTimes(totalTimes + atkTimes);
		}
		logger.info("CelebrationFoodActivity beatYuri playerId:{}, beatTimes:{}, totalBeatTimes", event.getPlayerId(),
				atkTimes, entity.getBeatYuriTimes());
	}

	
	
	@Subscribe
	public void worldCollectEvent(ResourceCollectEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.getCollectTime() <= 0) {
			return;
		}

		Optional<CelebrationFoodEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		
		CelebrationFoodEntity entity = opEntity.get();
		int collectTime = event.getCollectTime() + entity.getWolrdCollectRemainTime();
		CelebrationFoodDropCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(CelebrationFoodDropCfg.class,
							Activity.BrokenExchangeOper.WORLD_COLLECT_VALUE);
		//配置不存在说明策划不想触发
		if (cfg == null) {
			return;
		}
		
		if (collectTime >= cfg.getDropParam()) {
			int num = collectTime / cfg.getDropParam();
			int remain = collectTime % cfg.getDropParam();
			if (cfg.getDropLimit() > 0) {
				num = num > cfg.getDropLimit() - entity.getWolrdCollectTimes() ? cfg.getDropLimit() - entity.getWolrdCollectTimes() : num;
			}
			if (num > 0) {
				this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), num,Action.CELEBRATION_FOOD_WORLD_COLLECT, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
				entity.setWolrdCollectTimes(num + entity.getWolrdCollectTimes());
				entity.setWolrdCollectRemainTime(remain);
			}			
		} else {
			entity.setWolrdCollectRemainTime(collectTime);
		}
		logger.info("CelebrationFoodActivity worldCollect playerId:{}, beforeCollecTime:{}, afterCollectTime:{}, addCollectTime:{}", event.getPlayerId(), (collectTime - event.getCollectTime()), entity.getWolrdCollectRemainTime(), event.getCollectTime());
	}

	//补给又改成联盟捐献,entity变量名字改不起了.
	@Subscribe
	public void wishingEvent(GuildDonateEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		Optional<CelebrationFoodEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return;
		}
		CelebrationFoodEntity entity = opEntity.get();
		entity.setWishTimes(entity.getWishTimes() + 1);

		CelebrationFoodDropCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFoodDropCfg.class,
				Activity.BrokenExchangeOper.GUILD_DONATE_VALUE);
		if (cfg == null) {
			return;
		}
		
		int totalTimes = entity.getWishTotalTimes();
		if(totalTimes >= cfg.getDropLimit()){
			return;
		}
		if (entity.getWishTimes() >= cfg.getDropParam()) {
			this.getDataGeter().takeReward(event.getPlayerId(), cfg.getDropId(), 1,
					Action.CELEBRATION_FOOD_WISH, cfg.getMailId(), cfg.getName(), this.getActivityCfg().getActivityName());
			entity.setWishTimes(0);
			entity.setWishTotalTimes(totalTimes + 1);
		}
		
		logger.info("CelebrationFoodActivity wishing playerId:{}, wishTimes:{}", event.getPlayerId(), entity.getWishTimes());
	}
}
