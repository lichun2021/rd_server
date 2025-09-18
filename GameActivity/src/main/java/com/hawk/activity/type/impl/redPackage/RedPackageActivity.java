package com.hawk.activity.type.impl.redPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.redPackage.cfg.RedPackageAchieveCfg;
import com.hawk.activity.type.impl.redPackage.cfg.RedPackageKVCfg;
import com.hawk.activity.type.impl.redPackage.cfg.RedPackageRewardCfg;
import com.hawk.activity.type.impl.redPackage.entity.RedPackageEntity;
import com.hawk.game.protocol.Activity.PBRedPackageInfo;
import com.hawk.game.protocol.Activity.PBRedackageRecord;
import com.hawk.game.protocol.Activity.RedPackageActivityInfoResp;
import com.hawk.game.protocol.Activity.RedPackageRecieveResp;
import com.hawk.game.protocol.Activity.RedPackageState;
import com.hawk.game.protocol.Activity.RedackageRecordsResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;

public class RedPackageActivity extends ActivityBase {
	
	static Logger logger = LoggerFactory.getLogger("Server");

	
	
	public RedPackageActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.RED_PACKAGE_ACTIVITY;
	}
	
	@Override
	public void onOpen() {
		Set<String> onlines = getDataGeter().getOnlinePlayers();
		for(String playerId : onlines){
			callBack(playerId, MsgId.RED_PACKAGE_INFO_SYNC, ()->{
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	/**
	 * 红包领取纪录
	 * @param playerId
	 */
	public void getRedPackageRecords(String playerId){
		if(!isOpening(playerId)){
			return;
		}
		Optional<RedPackageEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		RedPackageEntity entity = opEntity.get();
		RedackageRecordsResp.Builder builder = RedackageRecordsResp.newBuilder();
		List<RedPackageRewardCfg> rewards = HawkConfigManager.getInstance().getConfigIterator(RedPackageRewardCfg.class).toList();
		for(RedPackageRewardCfg reward : rewards){
			if(!entity.isRecieve(reward.getId())){
				continue;
			}
			int stageId = reward.getId();
			int score = entity.getScore(stageId);
			List<RewardItem.Builder> items = this.getRedPackageRewards(stageId, score);
			PBRedackageRecord.Builder rbuilder = PBRedackageRecord.newBuilder();
			rbuilder.setStageId(stageId);
			rbuilder.setScore(score);
			items.forEach(item->rbuilder.addRewards(item));
			builder.addRecords(rbuilder);
		}
		pushToPlayer(playerId, HP.code.RED_PACKAGE_RECIEVE_RECORD_RESP_VALUE, builder);
		
	}
	
	
	public List<RewardItem.Builder> getRedPackageRewards(int stageId,int score){
		List<RewardItem.Builder> items = new ArrayList<>();
		RedPackageKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RedPackageKVCfg.class);
		for(int i=0;i<score;i++){
			String commReward = kvcfg.getReward();
			if(!HawkOSOperator.isEmptyString(commReward)){
				List<RewardItem.Builder> list = RewardHelper.toRewardItemList(commReward);
				items.addAll(list);
			}
		}
		List<RedPackageAchieveCfg> achives = HawkConfigManager.getInstance().getConfigIterator(RedPackageAchieveCfg.class).toList();
		for(RedPackageAchieveCfg acfg : achives){
			if(acfg.getRewardId() == stageId && score >=acfg.getIntegral() ){
				List<RewardItem.Builder> list = RewardHelper.toRewardItemList(acfg.getRewards());
				items.addAll(list);
			}
		}
		return items;
	}
	
	/***
	 * 抢红包
	 * @param playerId
	 * @param stageId 红包id
	 * @return
	 */
	public void onPlayerRecieveRedPackage(String playerId, int stageId,int score){
		if(!isOpening(playerId)){
			return;
		}
		Optional<RedPackageEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		RedPackageEntity entity = opEntity.get();
		//判断是否领取过
		if(entity.isRecieve(stageId)){
			return;
		}
		RedPackageRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(RedPackageRewardCfg.class, stageId);
		if(cfg == null){
			return;
		}
		logger.info("onPlayerRecieveRedPackage playerId:{},stage:{},score:{}",playerId,stageId,score);
		//判断该阶段是否开启
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		if(curTime > cfg.getEnd(termId) + cfg.getDuration() * 1000){
			return;
		}
		if(curTime < cfg.getStart(termId)){
			return;
		}
		if(score <=0){
			score = 1;
		}
		RedPackageKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(RedPackageKVCfg.class);
		score = Math.min(score, kvcfg.getCheckNum());
		score *= kvcfg.getGetIntegral();
		
		entity.addRecieve(stageId,score);
		//发奖励
		final List<RewardItem.Builder> items = this.getRedPackageRewards(stageId, score);
		getDataGeter().takeReward(playerId,items, 1, Action.RED_PACKAGE_RECIEVE_REWARD, false);
		List<RewardItem.Builder> showList = this.megerItems(items);
		RedPackageRecieveResp.Builder builder = RedPackageRecieveResp.newBuilder();
		PBRedackageRecord.Builder rbuilder = PBRedackageRecord.newBuilder();
		rbuilder.setStageId(stageId);
		rbuilder.setScore(score);
		showList.forEach(item->rbuilder.addRewards(item));
		builder.setInfo(rbuilder);
		pushToPlayer(playerId, HP.code.RED_PACKAGE_RECIEVE_RESP_VALUE, builder);
		syncActivityInfo(playerId, entity);
		this.getDataGeter().logRedPackageOpen(playerId, termId, stageId, score);
	}
	

	
	public List<RewardItem.Builder> megerItems(List<RewardItem.Builder> items){
		Map<Integer,RewardItem.Builder> map = new HashMap<Integer,RewardItem.Builder>();
		for(RewardItem.Builder item : items){
			RewardItem.Builder builder = map.get(item.getItemId());
			if(builder == null){
				builder = RewardItem.newBuilder();
				builder.setItemCount(item.getItemCount());
				builder.setItemId(item.getItemId());
				builder.setItemType(item.getItemType());
				map.put(item.getItemId(), builder);
				continue;
			}
			long cnt = builder.getItemCount() + item.getItemCount();
			builder.setItemCount(cnt);
		}
		return new ArrayList<>(map.values());
	}
	

	/**
	 * 同步活动信息
	 */
	@Override
	public void syncActivityDataInfo(String playerId){
		Optional<RedPackageEntity> opEntity = getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return;
		}
		RedPackageEntity entity = opEntity.get();
		this.syncActivityInfo(playerId, entity);
	}
	
	public void syncActivityInfo(String playerId,RedPackageEntity entity){
		int termId = entity.getTermId();
		RedPackageActivityInfoResp.Builder build = RedPackageActivityInfoResp.newBuilder();
		ConfigIterator<RedPackageRewardCfg> ite = HawkConfigManager.getInstance().getConfigIterator(RedPackageRewardCfg.class);
		for(RedPackageRewardCfg cfg : ite){
			PBRedPackageInfo.Builder info = PBRedPackageInfo.newBuilder();
			info.setId(cfg.getId());
			info.setShowTime(0);
			info.setStartTime(cfg.getStart(termId));
			info.setEndTime(cfg.getEnd(termId));
			info.setState(this.getRedPackageState(cfg,entity));
			build.addInfos(info);
		}
		pushToPlayer(playerId, HP.code.RED_PACKAGE_INFO_RESP_VALUE, build);
	}
	
	
	public RedPackageState getRedPackageState(RedPackageRewardCfg cfg,RedPackageEntity entity){
		int termId = entity.getTermId();
		long curTime = HawkTime.getMillisecond();
		long start = cfg.getStart(termId);
		long end = cfg.getEnd(termId);
		if(curTime < start ){
			return RedPackageState.RED_PARCKAGE_ONT_START;
		}else if(curTime > end){
			return RedPackageState.RED_PARCKAGE_ALREADY_OVER;
		}else{
			if(entity.isRecieve(cfg.getId())){
				return RedPackageState.RED_PARCKAGE_ALREADY_RECIEVED;
			}else{
				return RedPackageState.RED_PARCKAGE_CAN_RECIEVE;
			}
		}
	}


	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RedPackageActivity activity = new RedPackageActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RedPackageEntity> queryList = HawkDBManager.getInstance()
				.query("from RedPackageEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RedPackageEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RedPackageEntity entity = new RedPackageEntity(playerId, termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		
		
	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
	}

}
