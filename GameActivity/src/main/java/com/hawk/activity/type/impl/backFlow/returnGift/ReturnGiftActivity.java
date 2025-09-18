package com.hawk.activity.type.impl.backFlow.returnGift;

import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backFlow.returnGift.cfg.ReturnGiftConfig;
import com.hawk.activity.type.impl.backFlow.returnGift.cfg.ReturnGiftTimeCfg;
import com.hawk.activity.type.impl.backFlow.returnGift.cfg.ReturnGiftTypeCfg;
import com.hawk.activity.type.impl.backFlow.returnGift.entity.ReturnGiftEntity;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyInfo;
import com.hawk.game.protocol.Activity.ComeBackPlayerBuyMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/***
 * 低折回馈(原108)
 * @author yang.rao
 *
 */
public class ReturnGiftActivity extends ActivityBase {

	public ReturnGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RETURN_GIFT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ReturnGiftActivity activity = new ReturnGiftActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		Optional<ReturnGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		
		ReturnGiftEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		//检查新开活动
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			ReturnGiftTypeCfg dataCfg = this.getPlayerType(backFlowPlayer);
			long startTime = HawkTime.getAM0Date(
					new Date(backFlowPlayer.getBackTimeStamp())).getTime();
			long continueTime = 0;
			int backType = 0;
			if(dataCfg != null){
				continueTime = dataCfg.getDuration() * HawkTime.DAY_MILLI_SECONDS  - 1000;
				backType = dataCfg.getId();
			}
			long overTime = startTime + continueTime;
			entity.setBackCount(backTimes);
			entity.setBackType(backType);
			entity.setStartTime(startTime);
			entity.setOverTime(overTime);
			entity.resetBuyMsg();
			entity.notifyUpdate();
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
		}
		
		if(!isHidden(playerId)){
			syncActivityInfo(playerId, entity);
		}
	}

	public Result<?> onPlayerBuyChest(int chestId, int count, String playerId){
		if(!isOpening(playerId)){
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		Optional<ReturnGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		ReturnGiftEntity entity = opEntity.get();
		int playerType = entity.getBackType();
		ReturnGiftConfig chestCfg = HawkConfigManager.getInstance().getConfigByKey(ReturnGiftConfig.class, chestId);
		if(chestCfg == null){
			logger.error("ComeBackBuyActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		// TODO 回归档次不一致
		if(playerType != chestCfg.getPlayerType()){
			logger.error("ComeBackBuyActivity send error chestId:" + chestId);
		    return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		if(!entity.canBuy(chestId, count, chestCfg.getLimit())){
			logger.error("ComeBackBuyActivity buy error, chestId:{}, count:{}, entity:{}", chestId, count, entity);
			return Result.fail(Status.Error.ITEM_BUY_COUNT_EXCEED_VALUE);
		}
		List<RewardItem.Builder> prize = chestCfg.buildPrize(count);
			//扣道具
		boolean flag = this.getDataGeter().cost(playerId, prize, Action.COME_BACK_PLAYER_BUY);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		//给宝箱奖励
		logger.info("ComeBackBuyActivity player:{}, buy chest, chestId:{}, count:{}, entity:{}", playerId, chestId, count, entity);
		this.getDataGeter().takeReward(playerId, chestCfg.getGainItemList(), count, Action.COME_BACK_PLAYER_BUY, true, RewardOrginType.COME_BACK_PLAYER_BUY_REWARD);
		//更新数据到数据库
		entity.onPlayerBuy(chestId, count);
		entity.notifyUpdate();
		//记录TLog日志
		getDataGeter().logComeBackBuy(playerId, chestId, count);
		//同步界面信息
		syncActivityInfo(playerId, entity);
		return null;
	}
	
	public void syncActivityInfo(String playerId, ReturnGiftEntity entity){
		ComeBackPlayerBuyInfo.Builder build = ComeBackPlayerBuyInfo.newBuilder();
		int playerType = entity.getBackType();
		build.setPlayerType(playerType);
		ComeBackPlayerBuyMsg.Builder msgBuilder = null;
		if (entity.getBuyMsg() != null && !entity.getBuyMsg().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getBuyMsg().entrySet()) {
				msgBuilder = ComeBackPlayerBuyMsg.newBuilder();
				msgBuilder.setBuyId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				build.addItems(msgBuilder);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RETURN_GIFT_INFO_S, build));
	}
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ReturnGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from ReturnGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ReturnGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ReturnGiftEntity entity = new ReturnGiftEntity(playerId, termId);
		return entity;
	}
	
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,ReturnGiftEntity entity) {
		if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
			logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
					+ "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
					backFlowPlayer.getBackCount(),entity.getBackCount());
			return false;
		}
		//此次回流是否在正取时间内
		long backTime = backFlowPlayer.getBackTimeStamp();
		//如果在活动中，只更新期数，不更新其他数据
		if(backTime < entity.getOverTime() && backTime > entity.getStartTime()){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,in activity, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		//停止触发，只更新期数，不更新其他数据
		if(!this.canTrigger(backTime)){
			entity.setBackCount(backFlowPlayer.getBackCount());
			entity.notifyUpdate();
			logger.info("checkFitLostParams failed,can not Trigger, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		int lossDays = backFlowPlayer.getLossDays();
		logger.info("checkFitLostParams sucess,  playerId: "
				+ "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
		return true;
	}
	
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		ReturnGiftTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ReturnGiftTimeCfg.class, termId);
		if(cfg == null){
			return false;
		}
		if(backTime < cfg.getStartTimeValue()){
			return false;
		}
		if(backTime > cfg.getStopTriggerValue()){
			return false;
		}
		return true;
	}
	
	/**
	 * 获取玩家回归档次类型
	 * @param playerId
	 * @return
	 */
	public ReturnGiftTypeCfg getPlayerType(BackFlowPlayer backFlowPlayer){
		List<ReturnGiftTypeCfg> kvConfig = HawkConfigManager.getInstance().
				getConfigIterator(ReturnGiftTypeCfg.class).toList();
		for(ReturnGiftTypeCfg cfg : kvConfig){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
	

	@Override
	public void onPlayerMigrate(String playerId) {
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}
	
	@Override
	public boolean isHidden(String playerId) {
		Optional<ReturnGiftEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		ReturnGiftEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}

}
