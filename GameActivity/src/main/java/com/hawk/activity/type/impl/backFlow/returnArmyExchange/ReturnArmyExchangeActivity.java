package com.hawk.activity.type.impl.backFlow.returnArmyExchange;

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
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.cfg.ReturnArmyExchangeConfig;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.cfg.ReturnArmyExchangeTimeCfg;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.cfg.ReturnArmyTypeCfg;
import com.hawk.activity.type.impl.backFlow.returnArmyExchange.entity.ReturnArmyExchangeEntity;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeInfos;
import com.hawk.game.protocol.Activity.ComeBackPlayerExchangeMsg;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/***
 * 专属军资 活动(原107)
 * @author yang.rao
 *
 */
public class ReturnArmyExchangeActivity extends ActivityBase {

	public ReturnArmyExchangeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RETURN_ARMY_EXCHANGE;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		Optional<ReturnArmyExchangeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		
		ReturnArmyExchangeEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		//检查新开活动
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			ReturnArmyTypeCfg dataCfg = this.getPlayerType(backFlowPlayer);
			long startTime = HawkTime.getAM0Date(
					new Date(backFlowPlayer.getBackTimeStamp())).getTime();
			long continueTime = 0;
			int backType = 0;
			if(dataCfg != null){
				continueTime = dataCfg.getDuration() * HawkTime.DAY_MILLI_SECONDS - 1000;
				backType = dataCfg.getId();
			}
			long overTime = startTime + continueTime;
			entity.setBackCount(backTimes);
			entity.setBackType(backType);
			entity.setStartTime(startTime);
			entity.setOverTime(overTime);
			entity.resetExchangeNumMap();
			entity.notifyUpdate();
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
		}
		
		if(!isHidden(playerId)){
			syncActivityInfo(playerId, entity);
		}
	}

	public Result<?> onPlayerExchange(String playerId, int exchangeId, int num){
	
		ReturnArmyExchangeConfig cfg = HawkConfigManager.getInstance().getConfigByKey(ReturnArmyExchangeConfig.class, exchangeId);
		if (cfg == null) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		
		Optional<ReturnArmyExchangeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_DATA_NOT_FOUND_VALUE);
		}
		ReturnArmyExchangeEntity entity = opEntity.get();
		int playerType = entity.getBackType();
		// 回归档次不一致
		if (playerType != cfg.getPlayerType()) {
			return Result.fail(Status.Error.ACTIVITY_CONFIG_NOT_FOUND_VALUE);
		}
		Integer buyNum = entity.getExchangeNumMap().get(exchangeId);
		int newNum = (buyNum == null ? 0 : buyNum) + num;
		if (newNum > cfg.getLimit()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		boolean flag = this.getDataGeter().cost(playerId, cfg.getNeedItemList(), num, Action.COME_BACK_PLAYER_EXCHANGE, true);
		if (!flag) {
			return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
		}
		entity.getExchangeNumMap().put(exchangeId, newNum);
		entity.notifyUpdate();
		// 添加物品
		this.getDataGeter().takeReward(playerId, cfg.getGainItemList(), num, Action.COME_BACK_PLAYER_EXCHANGE, true, RewardOrginType.COME_BACK_PLAYER_EXCHANGE_REWARD);
		logger.info("RetrunArmyExchangeActivity playerId:{}, exchangeId:{}, num:{}", playerId, exchangeId, num);
		//记录TLog日志
		getDataGeter().logComeBackExchange(playerId, exchangeId, num);
		this.syncActivityInfo(playerId, entity);
		return Result.success(newNum);
	}
	
	/***
	 * 刷新界面给客户端
	 * @param playerId
	 * @param entity
	 */
	private void syncActivityInfo(String playerId, ReturnArmyExchangeEntity entity) {
		ComeBackPlayerExchangeInfos.Builder sbuilder = ComeBackPlayerExchangeInfos.newBuilder();
		int playerType = entity.getBackType();
		sbuilder.setPlayerType(playerType);
		ComeBackPlayerExchangeMsg.Builder msgBuilder = null;
		if (entity.getExchangeNumMap() != null && !entity.getExchangeNumMap().isEmpty()) {
			for (Entry<Integer, Integer> entry : entity.getExchangeNumMap().entrySet()) {
				msgBuilder = ComeBackPlayerExchangeMsg.newBuilder();
				msgBuilder.setExchangeId(entry.getKey());
				msgBuilder.setNum(entry.getValue());
				sbuilder.addExchangeInfo(msgBuilder);
			}
		}
		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.RETURN_ARMY_EXCHANGE_INFO_S_VALUE, sbuilder));
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		ReturnArmyExchangeActivity activity = new ReturnArmyExchangeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<ReturnArmyExchangeEntity> queryList = HawkDBManager.getInstance()
				.query("from ReturnArmyExchangeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			ReturnArmyExchangeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		ReturnArmyExchangeEntity entity = new ReturnArmyExchangeEntity(playerId, termId);
		return entity;
	}

	
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,ReturnArmyExchangeEntity entity) {
		if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
			logger.info("checkFitLostParams failed, BackCount data fail , playerId: "
					+ "{},backCount:{},entityBackCount:{}", backFlowPlayer.getPlayerId(),
					backFlowPlayer.getBackCount(),entity.getBackCount());
			return false;
		}
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
		ReturnArmyExchangeTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(ReturnArmyExchangeTimeCfg.class, termId);
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
	public ReturnArmyTypeCfg getPlayerType(BackFlowPlayer backFlowPlayer){
		List<ReturnArmyTypeCfg> kvConfig = HawkConfigManager.getInstance().
				getConfigIterator(ReturnArmyTypeCfg.class).toList();
		for(ReturnArmyTypeCfg cfg : kvConfig){
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
		Optional<ReturnArmyExchangeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		ReturnArmyExchangeEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}

}
