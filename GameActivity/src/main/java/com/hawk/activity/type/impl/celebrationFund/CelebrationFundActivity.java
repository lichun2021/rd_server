package com.hawk.activity.type.impl.celebrationFund;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AddTavernScoreEvent;
import com.hawk.activity.event.impl.AddTavernScoreGMEvent;
import com.hawk.activity.event.impl.CelebrationFundGiftEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundBuyCfg;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundKVCfg;
import com.hawk.activity.type.impl.celebrationFund.cfg.CelebrationFundTimeCfg;
import com.hawk.activity.type.impl.celebrationFund.entity.CelebrationFundEntity;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.game.protocol.Activity.CelebrationFundInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

/**
 * 周年庆-庆典基金
 * LiJialiang,FangWeijie
 */
public class CelebrationFundActivity extends ActivityBase {

	public CelebrationFundActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.CELEBRATION_FUND_ACTIVITY;
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		CelebrationFundActivity activity = new CelebrationFundActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<CelebrationFundEntity> queryList = HawkDBManager.getInstance()
				.query("from CelebrationFundEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			CelebrationFundEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		CelebrationFundEntity entity = new CelebrationFundEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.CELEBRATION_FUND_GIFT, () -> {
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<CelebrationFundEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	@Subscribe
	public void onContinueLoginEvent(ContinueLoginEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
	
		syncActivityDataInfo(event.getPlayerId());
	}
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	private void syncActivityInfo(String playerId, CelebrationFundEntity entity){
		CelebrationFundInfoPB.Builder builder = CelebrationFundInfoPB.newBuilder();
		builder.setFundLevel(entity.getFundLevel());
		builder.setLevelScore(entity.getLevelScore());
		builder.setBuyOver(entity.getBuyOver());
		PlayerPushHelper.getInstance().pushToPlayer(playerId,HawkProtocol.valueOf(HP.code2.CELEBRATION_FUND_INFO_SYNC, builder));
	}
	
	/**
	 * 庆典基金档位购买判断
	 * @param playerId
	 * @param giftId
	 * @return
	 */
	public int buyGiftCheck(String playerId, String giftId){
		if (!isOpening(playerId)) {
			HawkLog.errPrintln("celebration fund buy check failed, activity not opened, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		CelebrationFundBuyCfg cfg = CelebrationFundBuyCfg.getConfigByGoodsId(giftId);
		if (cfg == null) {
			HawkLog.errPrintln("celebration fund buy check failed, config not exist, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.CELEBRATION_FUND_GOODSID_ERROR_VALUE;
		}

		Optional<CelebrationFundEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.errPrintln("celebration fund buy check failed, data error, playerId: {}, giftId: {}", playerId, giftId);
			return Status.SysError.DATA_ERROR_VALUE;
		}
		
		CelebrationFundEntity dataEntity = opEntity.get();
		//判断ID是否买过。。。
		if (dataEntity.getBuyOver() > 0) {
			HawkLog.errPrintln("celebration fund buy check failed, all fund level bought already, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.CELEBRATION_FUND_BUY_OVER_VALUE;
		}
		if (cfg.getId() != dataEntity.getFundLevel()) {
			HawkLog.errPrintln("celebration fund buy check failed, fund level not match, playerId: {}, giftId: {}", playerId, giftId);
			return Status.Error.CELEBRATION_FUND_LEVEL_ERROR_VALUE;
		}
		if (dataEntity.getLevelScore() < cfg.getNeedPoint()) {
			HawkLog.errPrintln("celebration fund buy check failed, fund need point not enough, playerId: {}, giftId: {}, point: {}", playerId, giftId, dataEntity.getLevelScore());
			return Status.Error.CELEBRATION_FUND_SCORE_NOT_ENOUGH_VALUE;
		}
		
		return 0;
	}
	
	/**
	 * 庆典基金档位购买发货
	 * @param event
	 */
	@Subscribe
	public void sendGift(CelebrationFundGiftEvent event){
		if (!isOpening(event.getPlayerId())) {
			HawkLog.errPrintln("celebration fund send gift failed, activity end, playerId: {}, giftId: {}", event.getPlayerId(), event.getGiftId());
			return;
		}
		String giftId = event.getGiftId();
		CelebrationFundBuyCfg cfg = CelebrationFundBuyCfg.getConfigByGoodsId(giftId);
		if (cfg == null) {
			HawkLog.errPrintln("celebration fund send gift failed, config not exist, playerId: {}, giftId: {}", event.getPlayerId(), giftId);
			return;
		}
		
		int diamondsBef = getDataGeter().getDiamonds(event.getPlayerId());
		// 发放奖励
		List<RewardItem.Builder> rewardList = RewardHelper.toRewardItemImmutableList(cfg.getMaxGoldValue());
		ActivityReward reward = new ActivityReward(rewardList, Action.CELEBRATION_FUND_REWARD);
		postReward(event.getPlayerId(), reward);
		// 更新数据
		Optional<CelebrationFundEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		CelebrationFundEntity dataEntity = opEntity.get();
		int fundLevelBef = dataEntity.getFundLevel();
		fundLevelUp(dataEntity);
		// 发收据邮件
		callBack(event.getPlayerId(), MsgId.CELEBRATION_FUND_REWARD, () -> {
			int diamondsAft = getDataGeter().getDiamonds(event.getPlayerId());
			Object[] contentParams = new Object[]{ cfg.getId(), rewardList.get(0).getItemCount(), diamondsBef, diamondsAft };
			Object[] subTitle = new Object[]{ cfg.getId() };
			sendMailToPlayer(event.getPlayerId(), MailId.CELEBRATION_FUND_RECHARGE_REWARD, null, subTitle, contentParams, rewardList, true);
		});
		// 给客户端同步数据
		syncActivityDataInfo(event.getPlayerId());
		getDataGeter().logCelebrationGiftBuy(event.getPlayerId(), fundLevelBef);
		HawkLog.logPrintln("celebration fund send gift finish, playerId: {}, giftId: {}, fundLevel: {}", event.getPlayerId(), giftId, fundLevelBef);
	}
	
	/**
	 * 庆典基金档位积分变化
	 * @param event
	 */
	@Subscribe
	public void scoreChange(AddTavernScoreGMEvent event){
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		int score = event.getScore();
		if(score <= 0){
			HawkLog.errPrintln("celebration fund score change data error, playerId: {}, score: {}", event.getPlayerId(), score);
			return;
		}
		
		Optional<CelebrationFundEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		CelebrationFundEntity dataEntity = opEntity.get();
		if (dataEntity.getBuyOver() > 0) {
			return;
		}
		
		CelebrationFundBuyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, dataEntity.getFundLevel());
		if (cfg.getNeedPoint() <= dataEntity.getLevelScore()) {
			HawkLog.errPrintln("celebration fund score change break; level score enough, playerId: {}, score: {}", event.getPlayerId(), dataEntity.getLevelScore());
			return;
		}
		
		dataEntity.setLevelScore(dataEntity.getLevelScore() + score);
		HawkLog.logPrintln("celebration fund score change, playerId: {}, addscore: {}", event.getPlayerId(), score);
		syncActivityDataInfo(event.getPlayerId());
	}
	
	
	@Subscribe
	public void scoreChange(AddTavernScoreEvent event) {
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		
		int score = event.getScore();
		if(score <= 0){
			HawkLog.errPrintln("celebration fund score change data error, playerId: {}, score: {}", event.getPlayerId(), score);
			return;
		}
		
		Optional<CelebrationFundEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
		CelebrationFundEntity dataEntity = opEntity.get();
		if (dataEntity.getBuyOver() > 0) {
			return;
		}
		
		CelebrationFundBuyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, dataEntity.getFundLevel());
		if (cfg.getNeedPoint() <= dataEntity.getLevelScore()) {
			HawkLog.errPrintln("celebration fund score change break; level score enough, playerId: {}, score: {}", event.getPlayerId(), dataEntity.getLevelScore());
			return;
		}
		
		dataEntity.setLevelScore(dataEntity.getLevelScore() + score);
		HawkLog.logPrintln("celebration fund score change, playerId: {}, addscore: {}", event.getPlayerId(), score);
		syncActivityDataInfo(event.getPlayerId());
	}
	/**
	 * 庆典基金购买积分
	 * @param playerId
	 * @param score
	 * @return
	 */
	public int buyScore(String playerId, int score) {
		if (!isOpening(playerId)) {
			HawkLog.logPrintln("celebration fund buy score failed, activity end, playerId: {}", playerId);
			return Status.Error.ACTIVITY_NOT_OPEN_VALUE;
		}
		
		Optional<CelebrationFundEntity> opEntity = getPlayerDataEntity(playerId);
		CelebrationFundEntity dataEntity = opEntity.get();
		
		CelebrationFundKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(CelebrationFundKVCfg.class);
		CelebrationFundTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundTimeCfg.class, dataEntity.getTermId());
		long startTime = timeCfg.getEndTimeValue() - kvCfg.getBuyPointTimeStartTime();
		long endTime = timeCfg.getEndTimeValue() - kvCfg.getBuyPointTimeEndTime();
		long timeNow = HawkTime.getMillisecond();
	    if (timeNow < startTime || timeNow > endTime) {
	    	HawkLog.logPrintln("celebration fund buy score failed, time error, playerId: {}, fund level: {}", playerId, dataEntity.getFundLevel());
	    	return Status.Error.CELEBRATION_FUND_BUY_SCORE_ERROR4_VALUE;
	    }
		
		if (dataEntity.getBuyOver() > 0) {
			HawkLog.logPrintln("celebration fund buy score failed, buy over already, playerId: {}, fund level: {}", playerId, dataEntity.getFundLevel());
			return Status.Error.CELEBRATION_FUND_BUY_SCORE_ERROR1_VALUE;
		}
		
		int nowLevel = dataEntity.getFundLevel();
		int nowScore = dataEntity.getLevelScore();
		CelebrationFundBuyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, nowLevel);
		int diff = cfg.getNeedPoint() - nowScore;
		if (diff <= 0) {
			HawkLog.logPrintln("celebration fund buy score failed, fund level score enough, playerId: {}, fund level: {}, score: {}", playerId, nowLevel, nowScore);
			return Status.Error.CELEBRATION_FUND_BUY_SCORE_ERROR3_VALUE;
		}
		
		int realScore = Math.min(diff, score);
		List<RewardItem.Builder> costList = RewardHelper.toRewardItemImmutableList(cfg.getBuyPrice());
		RewardItem.Builder item = costList.get(0);
		int count = (int)item.getItemCount() * realScore;
		boolean flag = this.getDataGeter().cost(playerId, costList, realScore, Action.CELEBRATION_FUND_BUYSCORE, false);
		if (!flag) {
			HawkLog.logPrintln("celebration fund buy score consume failed, playerId: {}, fund level: {}, addScore: {}", playerId, nowLevel, realScore);
			return Status.Error.CELEBRATION_FUND_BUY_SCORE_ERROR2_VALUE;
		}
		
		dataEntity.setLevelScore(dataEntity.getLevelScore() + realScore);	
		HawkLog.logPrintln("celebration fund buy score success, playerId: {}, addscore: {}, client score: {}", playerId, realScore, score);
		getDataGeter().logCelebrationScoreBuy(playerId, realScore, count);
		syncActivityDataInfo(playerId);
		return 0;
	}
	
	/**
	 * 庆典基金档位升级
	 * 
	 * @param dataEntity
	 * @return
	 */
	private boolean fundLevelUp(CelebrationFundEntity dataEntity){
		CelebrationFundBuyCfg currentCfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, dataEntity.getFundLevel());
		int newLevel = currentCfg.getNext();
		CelebrationFundBuyCfg cfg = HawkConfigManager.getInstance().getConfigByKey(CelebrationFundBuyCfg.class, newLevel);
		if(cfg == null) {
			dataEntity.setBuyOver(1);
			HawkLog.logPrintln("celebration fund topLevel finish, playerId: {}", dataEntity.getPlayerId());
			return false;
		}
		
		dataEntity.setFundLevel(newLevel);
		dataEntity.setLevelScore(0);
		return true;
	}
	
}
