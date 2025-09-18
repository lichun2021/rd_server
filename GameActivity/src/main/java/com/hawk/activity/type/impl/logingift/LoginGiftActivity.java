package com.hawk.activity.type.impl.logingift;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.hawk.app.HawkApp;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkDelayTask;
import org.hawk.task.HawkTaskManager;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.BuyFundEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.logingift.cfg.LoginGiftActivityKVCfg;
import com.hawk.activity.type.impl.logingift.cfg.LoginGiftRewardCfg;
import com.hawk.activity.type.impl.logingift.entity.LoginGiftEntity;
import com.hawk.game.protocol.Activity.LoginGiftActivityInfo;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;

/**
 * 新版新手登录（登录豪礼）活动
 * 
 * @author lating
 *
 */
public class LoginGiftActivity extends ActivityBase {
	/**
	 * 进阶礼包进入购买倒计时的玩家
	 */
	private Map<String, Long> advanceGiftCountDownMap = new ConcurrentHashMap<String, Long>();
	private long tickTime = 0;

	public LoginGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.LOGIN_GIFT_ACTIVITY;
	}
	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		LoginGiftActivity activity = new LoginGiftActivity(config.getActivityId(), activityEntity);
		return activity;
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<LoginGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from LoginGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			LoginGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		LoginGiftEntity entity = new LoginGiftEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isActivityClose(String playerId) {
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return true;
		}
		
		LoginGiftEntity entity = opEntity.get();
		int totalRewardDays = HawkConfigManager.getInstance().getConfigSize(LoginGiftRewardCfg.class);
		// 累计登录天数还没满
		if (entity.getLoginDayTotal() < totalRewardDays) {
			return false;
		}
		// 还有奖励未领取
		if (entity.getReceivedCommDaySet().size() < totalRewardDays) {
			return false;
		}
		// 进阶奖励已解锁但还没有领取完
		if (entity.getBuyAdvanceTime() > 0 && entity.getReceivedAdvanceDaySet().size() < totalRewardDays) {
			return false;
		}
		
		if (entity.getAdvanceEndTime() > HawkTime.getMillisecond()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isOpening(String playerId) {
		// 活动是否失效
		if (isInvalid()) {
			return false;
		}
		
		if (isActivityClose(playerId)) {
			return false;
		}
		
		ActivityState state = getIActivityEntity(playerId).getActivityState();
		return state == ActivityState.OPEN;
	}
	
	/**
	 * 活动tick
	 */
	public void onTick() {
		if (advanceGiftCountDownMap.isEmpty()) {
			return;
		}
		
		long now = HawkApp.getInstance().getCurrentTime();
		if (now - tickTime < 5000) {
			return;
		}
		
		tickTime = now;
		List<String> playerList = advanceGiftCountDownMap.entrySet().stream().filter(e -> e.getValue() <= now).map(e -> e.getKey()).collect(Collectors.toList());
		for (String playerId : playerList) {
			try {
				advanceGiftCountDownMap.remove(playerId);
				PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
			} catch (Exception e) {
				HawkException.catchException(e);
			}
		}
	}
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if(!isOpening(playerId)){
			loginProcessDelay(playerId);
			return;
		}
		
		syncActivityInfo(playerId);
	}
	
	/**
	 * 延迟处理
	 * @param playerId
	 */
	private void loginProcessDelay(String playerId) {
		// 第一次登录时取不到数据，所以要做延迟处理
		HawkTaskManager.getInstance().postTask( new HawkDelayTask(10000, 10000, 1) {
			@Override
			public Object run() {
				if(isOpening(playerId)){
					syncActivityInfo(playerId);
				}
				return null;
			}
		});
	}
	
	/**
	 * 同步活动数据
	 * 
	 * @param playerId
	 */
	private void syncActivityInfo(String playerId) {
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		LoginGiftEntity dataEntity = opEntity.get();
		int totalRewardDays = HawkConfigManager.getInstance().getConfigSize(LoginGiftRewardCfg.class);
		// 累计登录天数满了，就不要再累计了
		if (dataEntity.getLoginDayTotal() < totalRewardDays) {
			dataEntity.addLoginDay(HawkTime.getYyyyMMddIntVal());
		}
		
		syncActivityDataInfo(playerId);
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		if(!isOpening(playerId)){
			return;
		}
		
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		LoginGiftEntity dataEntity = opEntity.get();
		LoginGiftActivityInfo.Builder builder = buildActivityInfo(dataEntity);
		pushToPlayer(playerId, HP.code2.LOGIN_GIFT_ACTIVITY_INFO_SYNC_VALUE, builder);
	}

	/**
	 * 构建builder
	 * @param dataEntity
	 * @return
	 */
	private LoginGiftActivityInfo.Builder buildActivityInfo(LoginGiftEntity dataEntity) {
		LoginGiftActivityInfo.Builder builder = LoginGiftActivityInfo.newBuilder();
		builder.setBuyAdvance(dataEntity.getBuyAdvanceTime() > 0);
		builder.setLoginDays(dataEntity.getLoginDayTotal());
		builder.addAllCommRecievedDay(dataEntity.getReceivedCommDaySet());
		builder.addAllAdvReceivedDay(dataEntity.getReceivedAdvanceDaySet());
		builder.setAdvanceEndTime(dataEntity.getAdvanceEndTime());
		return builder;
	}
	
	/**
	 * 领取登录奖励
	 * 
	 * @param playerId
	 * @param receiveDay 0：一键领取所有奖励，或具体的天数：领取某天对应栏目的奖励
	 * @param advance 0-普通奖励，1-进阶奖励（如果receiveDay传的是0，不管这个advance传什么值，都是领取所有奖励）
	 */
	@SuppressWarnings("deprecation")
	public int onRecieveLoginReward(String playerId, int receiveDay, int advance) {
		if(!isOpening(playerId)){
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}

		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		LoginGiftEntity dataEntity = opEntity.get();
		int checkResult = receiveRewardCheck(dataEntity, receiveDay, advance);
		if (checkResult != Status.SysError.SUCCESS_OK_VALUE) {
			return checkResult;
		}
		
		Set<Integer> commRecievedDaySet = dataEntity.getReceivedCommDaySet();
		Set<Integer> advReceivedDaySet = dataEntity.getReceivedAdvanceDaySet();
		List<RewardItem.Builder> rewardItems = new ArrayList<RewardItem.Builder>();
		for (int day = 1 ; day <= dataEntity.getLoginDayTotal(); day++) {
			// 领取特定登录天的奖励，当前遍历到的又不是那一天，直接跳过
			if (receiveDay > 0 && receiveDay != day) {
				continue;
			}
			
			LoginGiftRewardCfg rewardcfg = LoginGiftRewardCfg.getConfig(day);
			if (receiveDay == 0) {
				if (!commRecievedDaySet.contains(day)) {
					dataEntity.addCommReceivedDay(day);
					rewardItems.addAll(RewardHelper.toRewardItemList(rewardcfg.getCommonRewards()));
				}
				
				if (dataEntity.getBuyAdvanceTime() > 0 && !advReceivedDaySet.contains(day)) {
					dataEntity.addAdvanceReceivedDay(day);
					rewardItems.addAll(RewardHelper.toRewardItemList(rewardcfg.getAdvancedReward()));
				}
				continue;
			}
			
			if (advance == 0) {
				dataEntity.addCommReceivedDay(day);
				rewardItems.addAll(RewardHelper.toRewardItemList(rewardcfg.getCommonRewards()));
			} else {
				dataEntity.addAdvanceReceivedDay(day);
				rewardItems.addAll(RewardHelper.toRewardItemList(rewardcfg.getAdvancedReward()));
			}
		}
		
		dataEntity.notifyUpdate();
		int totalRewardDays = HawkConfigManager.getInstance().getConfigSize(LoginGiftRewardCfg.class);
		if (dataEntity.getBuyAdvanceTime() == 0 && dataEntity.getAdvanceEndTime() == 0) {
			// 在所有普通奖励都领取完时，如果还没有购买进阶礼包，且还没有设置过倒计时，则进行设置
			if (dataEntity.getReceivedCommDaySet().size() == totalRewardDays) {
				LoginGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginGiftActivityKVCfg.class);
				dataEntity.setAdvanceEndTime(HawkTime.getMillisecond() + cfg.getAdvancedTime() * 1000L);
				advanceGiftCountDownMap.put(playerId, dataEntity.getAdvanceEndTime());
				HawkLog.logPrintln("player receive login reward last, playerId: {}, recieveDay: {}, advance: {}, advanceEndTime: {}", playerId, receiveDay, advance, dataEntity.getAdvanceEndTime());
			}
		}
		
		Action action = Action.LOGIN_GIFTS_ALL_REWARD;
		if (receiveDay != 0) {
			action = advance == 0 ? Action.LOGIN_GIFTS_COMM_REWARD : Action.LOGIN_GIFTS_ADV_REWARD;
		}
		this.getDataGeter().takeReward(playerId, rewardItems, 1, action, true);
		// 此时如果此处再不进行同步，外层就同步不了了，因为活动已经结束了
		if (dataEntity.getReceivedCommDaySet().size() == totalRewardDays && dataEntity.getReceivedAdvanceDaySet().size() == totalRewardDays) {
			LoginGiftActivityInfo.Builder builder = buildActivityInfo(dataEntity);
			pushToPlayer(playerId, HP.code2.LOGIN_GIFT_ACTIVITY_INFO_SYNC_VALUE, builder);
			PlayerPushHelper.getInstance().syncActivityStateInfo(playerId, this);
		}
		HawkLog.logPrintln("player receive login reward finish, playerId: {}, recieveDay: {}, advance: {}", playerId, receiveDay, advance);
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 领取奖励条件检验
	 * @param dataEntity
	 * @param receiveDay
	 * @param advance
	 * @return
	 */
	private int receiveRewardCheck(LoginGiftEntity dataEntity, int receiveDay, int advance) {
		Set<Integer> commRecievedDaySet = dataEntity.getReceivedCommDaySet();
		Set<Integer> advReceivedDaySet = dataEntity.getReceivedAdvanceDaySet();
		// 一键领取所有奖励，不管advance传什么值，都是领取所有
		if (receiveDay == 0) {
			// 当前没有可领取的奖励
			if (commRecievedDaySet.size() >= dataEntity.getLoginDayTotal() && (dataEntity.getBuyAdvanceTime() == 0 || advReceivedDaySet.size() >= dataEntity.getLoginDayTotal())) {
				return Status.Error.LOGIN_GIFTS_COMM_RECIEVED_ALL_VALUE;
			}
			
			for (int day = 1 ; day <= dataEntity.getLoginDayTotal(); day++) {
				LoginGiftRewardCfg rewardcfg = LoginGiftRewardCfg.getConfig(day);
				if (rewardcfg == null) {
					return Status.SysError.CONFIG_ERROR_VALUE;
				}
			}
			
			return Status.SysError.SUCCESS_OK_VALUE;
		} 
		
		// 超过累计登录天数
		if (receiveDay > dataEntity.getLoginDayTotal()) {
			return Status.Error.LOGIN_GIFTS_DAY_PARAM_ERROR_VALUE;
		}
		
		// 已累计登录天数的普通奖励都领取完了
		if (advance == 0 && commRecievedDaySet.size() >= dataEntity.getLoginDayTotal()) {
			return Status.Error.LOGIN_GIFTS_COMM_RECIEVED_ALL_VALUE;
		}
		
		// 普通奖励已领取
		if (advance == 0 && commRecievedDaySet.contains(receiveDay)) {
			return Status.Error.LOGIN_GIFTS_RECIEVED_VALUE;
		}
		
		// 进阶奖励未解锁
		if (advance != 0 && dataEntity.getBuyAdvanceTime() == 0) {
			return Status.Error.LOGIN_GIFTS_ADV_LOCKED_VALUE;
		}
		
		// 已累计登录天数的进阶奖励都领取完了
		if (advance != 0 && advReceivedDaySet.size() >= dataEntity.getLoginDayTotal()) {
			return Status.Error.LOGIN_GIFTS_ADV_RECIEVED_ALL_VALUE;
		}
		
		// 进阶奖励已领取
		if (advance != 0 && advReceivedDaySet.contains(receiveDay)) {
			return Status.Error.LOGIN_GIFTS_RECIEVED_VALUE;
		}
		
		if (LoginGiftRewardCfg.getConfig(receiveDay) == null) {
			return Status.SysError.CONFIG_ERROR_VALUE;
		}
		
		return Status.SysError.SUCCESS_OK_VALUE;
	}
	
	/**
	 * 判断是否已购买进阶奖励解锁礼包
	 * 
	 * @param playerId
	 * @return
	 */
	public boolean isBuyAdvance(String playerId) {
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		LoginGiftEntity entity = opEntity.get();
		return entity.getBuyAdvanceTime() > 0;
	}
	
	/**
	 * 判断是否可以购买进阶礼
	 * @param playerId
	 * @return
	 */
	public int checkAdvanceBuy(String playerId, String giftId) {
		if(!isOpening(playerId)){
			return Status.SysError.ACTIVITY_CLOSED_VALUE;
		}
		
		LoginGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginGiftActivityKVCfg.class);
		if (!giftId.equals(cfg.getAndroidPayId()) && !giftId.equals(cfg.getIosPayId())) {
			HawkLog.logPrintln("player buy login gifts advanced check failed, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.PARAMS_INVALID_VALUE;
		}
		
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Status.SysError.ACTIVITY_CLOSED_VALUE;
		}
		
		LoginGiftEntity dataEntity = opEntity.get();
		
		return dataEntity.getBuyAdvanceTime() == 0 ? Status.SysError.SUCCESS_OK_VALUE : Status.Error.PAY_GIFT_BUY_FULL_TODAY_VALUE;
	}
	
	@Subscribe
	public void onAdvanceBuyEvent(PayGiftBuyEvent event) {
		String playerId = event.getPlayerId();
		String giftId = event.getGiftId();
		LoginGiftActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(LoginGiftActivityKVCfg.class);
		if (!giftId.equals(cfg.getAndroidPayId()) && !giftId.equals(cfg.getIosPayId())) {
			HawkLog.logPrintln("player buy login gifts advanced callback failed, playerId: {}, giftId: {}", playerId, giftId);
			return;
		}

		if(!isOpening(playerId)){
			return;
		}
		
		Optional<LoginGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		
		LoginGiftEntity dataEntity = opEntity.get();
		dataEntity.setBuyAdvanceTime(HawkTime.getMillisecond());
		dataEntity.setAdvanceEndTime(0L);
        advanceGiftCountDownMap.remove(playerId);
		
		// 进阶奖励解锁后，仍需手动领取进阶奖励，所以这里不发奖
		HawkLog.logPrintln("player buy login gifts advanced success, playerId: {}", event.getPlayerId());
		
		ActivityManager.getInstance().postEvent(new BuyFundEvent(playerId,this.getActivityType().intValue(), 0));
		LoginGiftActivityInfo.Builder builder = buildActivityInfo(dataEntity);
		pushToPlayer(playerId, HP.code2.LOGIN_GIFT_ACTIVITY_INFO_SYNC_VALUE, builder);
		// 下发收据邮件
		sendMailToPlayer(playerId, MailId.LOGIN_GIFT_ADVANCE_MAIL, null, null, null, Collections.emptyList(), true);
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		
	}

}
