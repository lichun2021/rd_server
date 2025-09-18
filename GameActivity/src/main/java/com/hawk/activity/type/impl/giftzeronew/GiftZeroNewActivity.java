package com.hawk.activity.type.impl.giftzeronew;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.xid.HawkXID;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.giftzeronew.cfg.GiftZeroNewActivityKVCfg;
import com.hawk.activity.type.impl.giftzeronew.cfg.GiftZeroNewActivityTimeCfg;
import com.hawk.activity.type.impl.giftzeronew.cfg.GiftZeroNewRewardCfg;
import com.hawk.activity.type.impl.giftzeronew.entity.GiftZeroNewEntity;
import com.hawk.activity.type.impl.giftzeronew.entity.GiftZeroNewItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.GiftZeroNewInfoList;
import com.hawk.game.protocol.Activity.GiftZeroNewInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/**
 * 新0元礼包活动
 * 
 * @author lating
 *
 */
public class GiftZeroNewActivity extends ActivityBase {
	
	private static final String GIFT_ZERO_REDIS_KEY = "gift_zero_new"; // 使用时需要带上区服ID

	public GiftZeroNewActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GIFT_ZERO_NEW_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GiftZeroNewActivity activity = new GiftZeroNewActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GiftZeroNewEntity> queryList = HawkDBManager.getInstance()
				.query("from GiftZeroNewEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GiftZeroNewEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GiftZeroNewEntity entity = new GiftZeroNewEntity(playerId, termId);
		return entity;
	}
	
	public boolean isInvalid() {
		int termId = this.getActivityTermId();
		GiftZeroNewActivityTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroNewActivityTimeCfg.class, termId);
		if (cfg == null) {
			return true;
		}
		
		return this.getDataGeter().getServerOpenDate() <= cfg.getTriggerTimeValue(); 
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isHidden(playerId)) {
			return;
		}
		
		detectGiftComsumeBack(playerId);
		syncGiftZeroInfo(playerId);
	}
	
	/**
	 * 跨天事件
	 * 
	 * @param event
	 */
	@Subscribe
	public void onEvent(ContinueLoginEvent event) {
		if (isHidden(event.getPlayerId())) {
			return;
		}
		
		detectGiftComsumeBack(event.getPlayerId());
		syncGiftZeroInfo(event.getPlayerId());
	}
	
	private void detectGiftComsumeBack(String playerId) {
		Optional<GiftZeroNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		GiftZeroNewEntity entity = opEntity.get();
		boolean update = false;
		
		for (GiftZeroNewItem item : entity.getItemList()) {
			GiftZeroNewRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroNewRewardCfg.class, item.getGiftId());
			if (rewardCfg == null) {
				HawkLog.errPrintln("GiftZeroNewActivity login detect consumeBack failed, config error, playerId: {}, giftId: {}", playerId, item.getGiftId());
				continue;
			}
			
			if (item.getBackDay() == rewardCfg.getReturnDays()) {
				continue;
			}
			
            int crossDay = HawkTime.getCrossDay(item.getPurchaseTime(), now, 0);
            long consumeBackTime = Math.max(item.getPurchaseTime(), item.getConsumeBackTime());
            int crossDayToLasttime = HawkTime.getCrossDay(consumeBackTime, now, 0);
            // 还没隔天，不补发
            if (crossDay <= 0 || crossDayToLasttime <= 0) {
            	continue;
            }
            
            int backDay = item.getBackDay();
            // crossDayToLasttime是几就补发几天的邮件
            for (int i = 1; i <= crossDayToLasttime; i++) {
            	int day = backDay + i;
            	List<RewardItem.Builder> consumeBackItem = rewardCfg.getConsumeBackItem(day);
            	if (consumeBackItem == null) {
            		continue;
            	}
            	
            	this.getDataGeter().sendMail(playerId, MailId.GIFT_ZERO_CONSUME_BACK_REWARD,
            			new Object[] { this.getActivityCfg().getActivityName() },
            			new Object[] { this.getActivityCfg().getActivityName() }, 
            			new Object[] { day },
            			consumeBackItem, false); 
            	item.setBackDay(day);
            	HawkLog.logPrintln("GiftZeroNewActivity login detect consumeBack success, playerId: {}, giftId: {}, day: {}", playerId, item.getGiftId(), day);
            }
            
            item.setConsumeBackTime(now);
            update = true;

            //如果到最后一天了，直接删除redis数据
            if (item.getBackDay() == rewardCfg.getReturnDays()) {
            	String key = playerId + ":" + item.getGiftId();
            	ActivityLocalRedis.getInstance().hDel(getRedisKey(), key);
            }
		}
		
		if (update) {
			entity.notifyUpdate();
		}
	}

	@Override
	public void onOpen() {
		
	}
	
	@Override
	public void onHidden() {
		HawkTaskManager.getInstance().postExtraTask(new HawkTask() {
			@Override
			public Object run() {
				grantConsumeBackReward();
				return null;
			}
		});
	}
	
	/**
	 * 获取redis存储key
	 * 
	 * @return
	 */
	private String getRedisKey() {
		return GIFT_ZERO_REDIS_KEY + ":" + this.getDataGeter().getServerId();
	}
	
	/**
	 * 补发0元礼包购买消耗
	 */
	private void grantConsumeBackReward() {
		ActivityEntity activityEntity = getActivityEntity();
		int termId = activityEntity.getTermId();
		long now = HawkTime.getMillisecond();
		Map<String, String> map = ActivityLocalRedis.getInstance().hgetAll(getRedisKey());
		for (Entry<String, String> entry : map.entrySet()) {
			try {
				String[] keyInfo = entry.getKey().split(":");
				String playerId = keyInfo[0];
				int giftId = Integer.valueOf(keyInfo[1]);
				HawkDBEntity dbEntity = loadFromDB(playerId, termId);
				if (dbEntity == null) {
					HawkLog.errPrintln("GiftZeroNewActivity end detect consumeBack failed, dbEntity not eixst, playerId: {}, giftId: {}", playerId, giftId);
					continue;
				}
				
				GiftZeroNewRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroNewRewardCfg.class, giftId);
				if (rewardCfg == null) {
					HawkLog.errPrintln("GiftZeroNewActivity end detect consumeBack failed, config error, playerId: {}, giftId: {}", playerId, giftId);
					continue;
				}
				
				GiftZeroNewEntity entity = (GiftZeroNewEntity)dbEntity;
				GiftZeroNewItem item = entity.getPurchaseItem(giftId);
				if (item == null) {
					continue;
				}
				
				if (item.getBackDay() == rewardCfg.getReturnDays()) {
					continue;
				}
				
				int crossDay = HawkTime.getCrossDay(item.getPurchaseTime(), now, 0);
				long consumeBackTime = Math.max(item.getPurchaseTime(), item.getConsumeBackTime());
	            int crossDayToLasttime = HawkTime.getCrossDay(consumeBackTime, now, 0);
	            // 还没隔天，不补发
	            if (crossDay <= 0 || crossDayToLasttime <= 0) {
	            	continue;
	            }
		            
	            int backDay = item.getBackDay();
	            // crossDayToLasttime是几就补发几天的邮件
	            for (int i = 1; i <= crossDayToLasttime; i++) {
	            	int day = backDay + i;
	            	List<RewardItem.Builder> consumeBackItem = rewardCfg.getConsumeBackItem(day);
	            	if (consumeBackItem == null) {
	            		continue;
	            	}
	            	
	            	// 发邮件
					this.getDataGeter().sendMail(playerId, MailId.GIFT_ZERO_CONSUME_BACK_REWARD,
							new Object[] { this.getActivityCfg().getActivityName() },
							new Object[] { this.getActivityCfg().getActivityName() }, 
							new Object[] { day },
							consumeBackItem, false); 
	            	item.setBackDay(day);
	            	HawkLog.logPrintln("GiftZeroNewActivity end detect consumeBack success, playerId: {}, giftId: {}, day: {}", playerId, item.getGiftId(), day);
	            }
	            
				ActivityLocalRedis.getInstance().hDel(getRedisKey(), entry.getKey());
				item.setConsumeBackTime(now);
				entity.notifyUpdate();
				HawkLog.logPrintln("GiftZeroNewActivity end detect consumeBack success, playerId: {}, giftId: {}", playerId, giftId);
				
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

	/**
	 * 同步礼包信息
	 * 
	 * @param playerId
	 */
	public int syncGiftZeroInfo(String playerId) {
		if (isHidden(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<GiftZeroNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftZeroNewActivity syncGiftZeroInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		GiftZeroNewEntity entity = opEntity.get();
		GiftZeroNewInfoList.Builder listBuilder = GiftZeroNewInfoList.newBuilder();
		for (GiftZeroNewItem item : entity.getItemList()) {
			GiftZeroNewInfoPB.Builder builder = GiftZeroNewInfoPB.newBuilder();
			builder.setGiftId(item.getGiftId());
			builder.setPurchaseTime(item.getPurchaseTime());
			listBuilder.addGift(builder.build());
		}
		
		listBuilder.setFreeRewardTaken(!HawkTime.isCrossDay(entity.getFreeTakenTime(), HawkTime.getMillisecond(), 0));
		
		pushToPlayer(playerId, HP.code.GIFT_ZERO_NEW_INFO_PUSH_VALUE, listBuilder);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 购买礼包
	 * 
	 * @param playerId
	 * @param giftId
	 */
	public int buyGift(String playerId, int giftId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<GiftZeroNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftZeroNewActivity buyGift failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		GiftZeroNewRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroNewRewardCfg.class, giftId);
		if (rewardCfg == null) {
			HawkLog.errPrintln("GiftZeroNewActivity buyGift failed, config error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		GiftZeroNewEntity entity = opEntity.get();
		GiftZeroNewItem item = entity.getPurchaseItem(giftId);
		if (item != null) {
			HawkLog.errPrintln("GiftZeroNewActivity buyGift failed, gift has already bought, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.GIFT_ZERO_ALREADY_BOUGHT_VALUE;
		} 
		
		boolean success = getDataGeter().consumeItems(playerId, rewardCfg.getConsumeItem(), HP.code.GIFT_ZERO_NEW_BUY_REQ_VALUE, Action.GIFT_ZERO_CONSUME);
		if (!success) {
			HawkLog.errPrintln("GiftZeroNewActivity buyGift failed, consumeItems not enought, playerId: {}, giftId: {}", playerId, giftId);
			return 0; // 这里不要去假设消耗的时什么东西，不足时在掉消耗接口时已经返回错误提示了，所以这里可以不用去管到底是什么不足的问题
		}
		
		item = new GiftZeroNewItem(giftId);
		entity.addItem(item);
		getDataGeter().takeReward(playerId, rewardCfg.getRewardList(), Action.GIFT_ZERO_REWARD, true);
		
		String key = playerId + ":" + giftId;
		ActivityLocalRedis.getInstance().hset(getRedisKey(), key, String.valueOf(item.getPurchaseTime()));
		
		syncGiftZeroInfo(playerId);
		
		HawkLog.logPrintln("GiftZeroNewActivity buyGift success, playerId: {}, giftId: {}", playerId, giftId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}

	/**
	 * 领取免费奖励
	 * 
	 * @param playerId
	 * @return
	 */
	public int freeTakeReward(String playerId) {
		if (!isOpening(playerId)) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<GiftZeroNewEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftZeroNewActivity take free reward failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		GiftZeroNewActivityKVCfg kvConfig = HawkConfigManager.getInstance().getKVInstance(GiftZeroNewActivityKVCfg.class);
		if (kvConfig == null) {
			return Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE;
		}
		
		long curTime = HawkTime.getMillisecond();
		GiftZeroNewEntity entity = opEntity.get();
		if (!HawkTime.isCrossDay(curTime, entity.getFreeTakenTime(), 0)) {
			return Status.Error.GIFTZERO_FREE_REWARD_TAKEN_VALUE;
		}
		
		{
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
			PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(RewardHelper.toRewardItemList(kvConfig.getReward()), 
					Action.GIFT_ZERO_FREE_REWARD, true, RewardOrginType.ACTIVITY_REWARD, 0);
			HawkTaskManager.getInstance().postMsg(xid, msg);
		}
		
		entity.setFreeTakenTime(curTime);
		syncGiftZeroInfo(playerId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
}
