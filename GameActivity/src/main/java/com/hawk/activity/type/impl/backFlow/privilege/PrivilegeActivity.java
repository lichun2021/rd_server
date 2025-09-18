package com.hawk.activity.type.impl.backFlow.privilege;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backFlow.privilege.cfg.PrivilegeBuffConfig;
import com.hawk.activity.type.impl.backFlow.privilege.cfg.PrivilegeDateCfg;
import com.hawk.activity.type.impl.backFlow.privilege.cfg.PrivilegeTimeCfg;
import com.hawk.activity.type.impl.backFlow.privilege.entity.PrivilegeEntity;
import com.hawk.game.protocol.Activity.BackPrivilegeBuff;
import com.hawk.game.protocol.Activity.BackPrivilegeInfoResp;
import com.hawk.game.protocol.Const.EffType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.log.Action;

/***
 * 再续前缘
 * @author che
 *
 */
public class PrivilegeActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");
	
	public PrivilegeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}
	
	

	@Override
	public ActivityType getActivityType() {
		return ActivityType.BACK_PRIVILEGE;
	}

	@Override
	public void onPlayerLogin(String playerId) {
		Optional<PrivilegeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PrivilegeEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			PrivilegeDateCfg dataCfg = this.getBackGfitDateCfg(backFlowPlayer);
			long backTime = backFlowPlayer.getBackTimeStamp();
			long startTime = HawkTime.getAM0Date(
					new Date(backTime)).getTime();
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
			entity.setReward(0);
			entity.setBuffStartTime(backTime);
			initBuff(entity,dataCfg);
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
		}
		
	}
	
	
	
	
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,PrivilegeEntity entity) {
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
			logger.info("checkFitLostParams failed,in activity, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		//停止触发，只更新期数，不更新其他数据
		if(!this.canTrigger(backTime)){
			entity.setBackCount(backFlowPlayer.getBackCount());
			logger.info("checkFitLostParams failed,can not Trigger, playerId: "
					+ "{},backCount:{},backTime:{}", entity.getPlayerId(),entity.getBackCount(),backTime);
			return false;
		}
		int lossDays = backFlowPlayer.getLossDays();
		logger.info("checkFitLostParams sucess, playerId: "
				+ "{},loss:{}", backFlowPlayer.getPlayerId(),lossDays);
		return true;
	}
	
	
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		PrivilegeTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(PrivilegeTimeCfg.class, termId);
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
	
	


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PrivilegeActivity activity = new PrivilegeActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PrivilegeEntity> queryList = HawkDBManager.getInstance()
				.query("from PrivilegeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PrivilegeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PrivilegeEntity entity = new PrivilegeEntity(playerId, termId);
		return entity;
	}



	@Override
	public boolean isHidden(String playerId) {
		Optional<PrivilegeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		PrivilegeEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}

	/**
	 * 获取活动持续时间
	 * @param backFlowPlayer
	 * @return
	 */
	public PrivilegeDateCfg getBackGfitDateCfg(BackFlowPlayer backFlowPlayer){
		List<PrivilegeDateCfg> congfigs = HawkConfigManager.getInstance().
				getConfigIterator(PrivilegeDateCfg.class).toList();
		for(PrivilegeDateCfg cfg : congfigs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
	
	
	/**
	 * 初始化BUFF
	 * @param entity
	 */
	private void initBuff(PrivilegeEntity entity,PrivilegeDateCfg dateCfg){
		String playerId = entity.getPlayerId();
		List<Integer> blist = dateCfg.getBuffList();
		for(int bid : blist){
			PrivilegeBuffConfig buffCfg = HawkConfigManager.getInstance().getConfigByKey(PrivilegeBuffConfig.class,bid);
			if(buffCfg == null){
				continue;
			}
			long endTime = entity.getStartTime() + buffCfg.getBufftime() * 1000;
			this.getDataGeter().addBuff(playerId, buffCfg.getBuff(), endTime);
		}
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<PrivilegeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PrivilegeEntity entity = optional.get();
		BackPrivilegeInfoResp.Builder builder = BackPrivilegeInfoResp.newBuilder();
		
		long startTime = entity.getStartTime();
		PrivilegeDateCfg dateCfg = HawkConfigManager.getInstance().getConfigByKey(PrivilegeDateCfg.class, entity.getBackType());
		List<Integer> blist = dateCfg.getBuffList();
		for(int bid : blist){
			PrivilegeBuffConfig buffCfg = HawkConfigManager.getInstance().getConfigByKey(PrivilegeBuffConfig.class,bid);
			if(buffCfg == null){
				continue;
			}
			BackPrivilegeBuff.Builder buffBuilder = BackPrivilegeBuff.newBuilder();
			long endTime = startTime + buffCfg.getBufftime() * 1000;
			buffBuilder.setId(bid);
			buffBuilder.setStartTime(String.valueOf(startTime));
			buffBuilder.setEndTime(String.valueOf(endTime));
			builder.addBuffs(buffBuilder);
		}
		builder.setReward(entity.getReward() > 0);
		builder.setBackDataId(entity.getBackType());
		builder.setEffectTimes28102(this.getDataGeter().effectTodayUsedTimes(playerId, 
				EffType.BACK_PRIVILEGE_ATK_MONSTER_AWARD_DOUBLE_TIMES));
		pushToPlayer(playerId, HP.code.BACK_PRIVILEGE_INFO_RESP_VALUE, builder);
	} 
	
	
	public void reward(String playerId){
		Optional<PrivilegeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PrivilegeEntity entity = optional.get();
		if(entity.getReward() > 0){
			return;
		}
		entity.setReward(1);
		PrivilegeDateCfg dateCfg = HawkConfigManager.getInstance().getConfigByKey(PrivilegeDateCfg.class, entity.getBackType());
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.addAll(dateCfg.getRewardList());
		this.getDataGeter().takeReward(playerId,rewardList, 1, Action.BACK_PRIVILEGE_REWARD, true);
		this.syncActivityDataInfo(playerId);
	}
}
