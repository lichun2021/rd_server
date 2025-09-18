package com.hawk.activity.type.impl.giftzero;

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
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.redis.ActivityLocalRedis;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.giftzero.cfg.GiftZeroRewardCfg;
import com.hawk.activity.type.impl.giftzero.entity.GiftZeroEntity;
import com.hawk.activity.type.impl.giftzero.entity.GiftZeroItem;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.GiftZeroInfoList;
import com.hawk.game.protocol.Activity.GiftZeroInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.log.Action;

/**
 * 0元礼包活动
 * 
 * @author lating
 *
 */
public class GiftZeroActivity extends ActivityBase {
	
	private static final String GIFT_ZERO_REDIS_KEY = "gift_zero";

	public GiftZeroActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GIFT_ZERO_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		GiftZeroActivity activity = new GiftZeroActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GiftZeroEntity> queryList = HawkDBManager.getInstance()
				.query("from GiftZeroEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GiftZeroEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GiftZeroEntity entity = new GiftZeroEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isHidden(playerId)) {
			return;
		}
		
		detectGiftComsumeBack(playerId);
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
	}
	
	private void detectGiftComsumeBack(String playerId) {
		Optional<GiftZeroEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		long now = HawkTime.getMillisecond();
		GiftZeroEntity entity = opEntity.get();
		boolean update = false;
		
		for (GiftZeroItem item : entity.getItemList()) {
			if (item.getConsumeBackTime() > 0) {
				continue;
			}
			
			GiftZeroRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroRewardCfg.class, item.getGiftId());
			if (rewardCfg == null) {
				HawkLog.errPrintln("GiftZeroActivity login detect consumeBack failed, config error, playerId: {}, giftId: {}", playerId, item.getGiftId());
				continue;
			}
			
            int crossDay = HawkTime.getCrossDay(item.getPurchaseTime(), now, 0);
            if (crossDay < rewardCfg.getReturnDays()) {
            	continue;
            }
            
            String key = playerId + ":" + item.getGiftId();
    		ActivityLocalRedis.getInstance().hDel(GIFT_ZERO_REDIS_KEY, key);
    		item.setConsumeBackTime(now);
    		update = true;
    		
            // 发邮件
			this.getDataGeter().sendMail(playerId, MailId.GIFT_ZERO_CONSUME_BACK_REWARD,
					new Object[] { this.getActivityCfg().getActivityName() },
					new Object[] { this.getActivityCfg().getActivityName() }, 
					new Object[] {},
					rewardCfg.getConsumeBackItem(), false); 
			
			HawkLog.logPrintln("GiftZeroActivity login detect consumeBack success, playerId: {}, giftId: {}", playerId, item.getGiftId());
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
	 * 补发0元礼包购买消耗
	 */
	private void grantConsumeBackReward() {
		ActivityEntity activityEntity = getActivityEntity();
		int termId = activityEntity.getTermId();
		long now = HawkTime.getMillisecond();
		Map<String, String> map = ActivityLocalRedis.getInstance().hgetAll(GIFT_ZERO_REDIS_KEY);
		for (Entry<String, String> entry : map.entrySet()) {
			try {
				String[] keyInfo = entry.getKey().split(":");
				String playerId = keyInfo[0];
				if (!this.getDataGeter().isServerPlayer(playerId)) {
					continue;
				}

				int giftId = Integer.valueOf(keyInfo[1]);
				HawkDBEntity dbEntity = loadFromDB(playerId, termId);
				if (dbEntity == null) {
					HawkLog.errPrintln("GiftZeroActivity end detect consumeBack failed, dbEntity not eixst, playerId: {}, giftId: {}", playerId, giftId);
					continue;
				}
				
				GiftZeroRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroRewardCfg.class, giftId);
				if (rewardCfg == null) {
					HawkLog.errPrintln("GiftZeroActivity end detect consumeBack failed, config error, playerId: {}, giftId: {}", playerId, giftId);
					continue;
				}
				
				GiftZeroEntity entity = (GiftZeroEntity)dbEntity;
				GiftZeroItem item = entity.getPurchaseItem(giftId);
				if (item != null) {
					// 发邮件
					this.getDataGeter().sendMail(playerId, MailId.GIFT_ZERO_CONSUME_BACK_REWARD,
							new Object[] { this.getActivityCfg().getActivityName() },
							new Object[] { this.getActivityCfg().getActivityName() }, 
							new Object[] {},
							rewardCfg.getConsumeBackItem(), false); 
					
					ActivityLocalRedis.getInstance().hDel(GIFT_ZERO_REDIS_KEY, entry.getKey());

					item.setConsumeBackTime(now);
					entity.notifyUpdate();
					HawkLog.logPrintln("GiftZeroActivity end detect consumeBack success, playerId: {}, giftId: {}", playerId, giftId);
				}
				
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
		
		Optional<GiftZeroEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftZeroActivity syncGiftZeroInfo failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		GiftZeroEntity entity = opEntity.get();
		GiftZeroInfoList.Builder listBuilder = GiftZeroInfoList.newBuilder();
		for (GiftZeroItem item : entity.getItemList()) {
			GiftZeroInfoPB.Builder builder = GiftZeroInfoPB.newBuilder();
			builder.setGiftId(item.getGiftId());
			builder.setPurchaseTime(item.getPurchaseTime());
			listBuilder.addGift(builder.build());
		}
		
		pushToPlayer(playerId, HP.code.GIFT_ZERO_INFO_PUSH_VALUE, listBuilder);
		
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
		
		Optional<GiftZeroEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("GiftZeroActivity buyGift failed, entity data not exist, playerId: {}", playerId);
			return Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE;
		}
		
		GiftZeroRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(GiftZeroRewardCfg.class, giftId);
		if (rewardCfg == null) {
			HawkLog.errPrintln("GiftZeroActivity buyGift failed, config error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		GiftZeroEntity entity = opEntity.get();
		GiftZeroItem item = entity.getPurchaseItem(giftId);
		if (item != null) {
			HawkLog.errPrintln("GiftZeroActivity buyGift failed, gift has already bought, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.GIFT_ZERO_ALREADY_BOUGHT_VALUE;
		} 
		
		boolean success = getDataGeter().consumeItems(playerId, rewardCfg.getConsumeItem(), HP.code.GIFT_ZERO_BUY_REQ_VALUE, Action.GIFT_ZERO_CONSUME);
		if (!success) {
			HawkLog.errPrintln("GiftZeroActivity buyGift failed, consumeItems not enought, playerId: {}, giftId: {}", playerId, giftId);
			return 0; // 这里不要去假设消耗的时什么东西，不足时在掉消耗接口时已经返回错误提示了，所以这里可以不用去管到底是什么不足的问题
		}
		
		item = new GiftZeroItem(giftId);
		entity.addItem(item);
		getDataGeter().takeReward(playerId, rewardCfg.getRewardList(), Action.GIFT_ZERO_REWARD, true);
		
		String key = playerId + ":" + giftId;
		ActivityLocalRedis.getInstance().hset(GIFT_ZERO_REDIS_KEY, key, String.valueOf(item.getPurchaseTime()));
		
		syncGiftZeroInfo(playerId);
		
		HawkLog.logPrintln("GiftZeroActivity buyGift success, playerId: {}, giftId: {}", playerId, giftId);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
}
