package com.hawk.activity.type.impl.honourMobilize;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AchieveItemCreateEvent;
import com.hawk.activity.event.impl.BuildingLevelUpEvent;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.HonourMobilizeCountEvent;
import com.hawk.activity.event.impl.VipLevelupEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.achieve.provider.AchieveItems;
import com.hawk.activity.type.impl.achieve.provider.AchieveProvider;
import com.hawk.activity.type.impl.honourMobilize.cfg.HonourMobilizeAchieveCfg;
import com.hawk.activity.type.impl.honourMobilize.cfg.HonourMobilizeKVCfg;
import com.hawk.activity.type.impl.honourMobilize.cfg.HonourMobilizeRewardCfg;
import com.hawk.activity.type.impl.honourMobilize.entity.HonourMobilizeEntity;
import com.hawk.game.protocol.Activity.HonorMobilizeInfoSync;
import com.hawk.game.protocol.Activity.HonorMobilizeLotteryResp;
import com.hawk.game.protocol.Const.BuildingType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;
/**
 * 荣耀英雄降临-荣耀凯恩
 * @author che
 *
 */
public class HonourMobilizeActivity extends ActivityBase  implements AchieveProvider {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public HonourMobilizeActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.HONOUR_MOBILIZE;
	}

	@Override
	public int providerActivityId() {
		return this.getActivityType().intValue();
	}
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		HonourMobilizeActivity activity = new HonourMobilizeActivity(
				config.getActivityId(), activityEntity);
		AchieveContext.registeProvider(activity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<HonourMobilizeEntity> queryList = HawkDBManager.getInstance()
				.query("from HonourMobilizeEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			HonourMobilizeEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		HonourMobilizeEntity entity = new HonourMobilizeEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public boolean isProviderActive(String playerId) {
		return isOpening(playerId);
	}

	@Override
	public boolean isProviderNeedSync(String playerId) {
		return isShow(playerId);
	}

	@Override
	public Optional<AchieveItems> getAchieveItems(String playerId) {
		Optional<HonourMobilizeEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return Optional.empty();
		}
		HonourMobilizeEntity entity = optional.get();
		if (entity.getItemList().isEmpty()) {
			return Optional.empty();
		}
		AchieveItems items = new AchieveItems(entity.getItemList(), entity, true, getActivityId(), entity.getTermId());
		return Optional.of(items);
	}
	
	@Override
	public AchieveConfig getAchieveCfg(int achieveId) {
		AchieveConfig cfg =  HawkConfigManager.getInstance().
				getConfigByKey(HonourMobilizeAchieveCfg.class, achieveId);
		return cfg;
	}
	
	@Override
	public List<Builder> getRewardList(String playerId, AchieveConfig achieveConfig) {
		return AchieveProvider.super.getRewardList(playerId, achieveConfig);
	}

	@Override
	public Action takeRewardAction() {
		return Action.HONOUR_MOBILIZE_ACHIEVE_REWARD;
	}

	
	@Override
	public boolean isActivityClose(String playerId) {
		HonourMobilizeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(HonourMobilizeKVCfg.class);
		//检查主城等级
		int cityLevel = this.getDataGeter().getBuildMaxLevel(playerId, BuildingType.CONSTRUCTION_FACTORY_VALUE);
		if(cityLevel < kvCfg.getBaseLimit()){
			return true; 
		}
		//vip等级
		int vipLevel = this.getDataGeter().getVipLevel(playerId);
		if(vipLevel < kvCfg.getVIPLimit()){
			return true; 
		}
		return super.isActivityClose(playerId);
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.HONOUR_MOBILIZE_INIT, () -> {
				Optional<HonourMobilizeEntity>  optional = this.getPlayerDataEntity(playerId);
				if (!optional.isPresent()) {
					return;
				}
				this.initAcivityData(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		this.initAcivityData(playerId);
	}
	
	@Subscribe
	public void onEvent(BuildingLevelUpEvent event) {
		if(!isOpening(event.getPlayerId())){
			return;
	    }
		this.syncActivityStateInfo(event.getPlayerId());
		this.syncActivityDataInfo(event.getPlayerId());
	}
	
	@Subscribe
	public void onEvent(VipLevelupEvent event) {
		if(!isOpening(event.getPlayerId())){
			return;
	    }
		this.syncActivityStateInfo(event.getPlayerId());
		this.syncActivityDataInfo(event.getPlayerId());
	}
	
	
	/**
	 * 跨天刷新信息到客户端
	 * @param event
	 */
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
        //活动是否开启，没开不继续处理
        if (!isOpening(playerId)) {
            return;
        }
        //去当前期数
        int termId = getActivityTermId(playerId);
        //取当前期数结束时间
        long hiddenTime = getTimeControl().getHiddenTimeByTermId(termId, playerId);
        //取当前时间
        long now = HawkTime.getMillisecond();
        //如果当前时间大于当前期数结束时间，不继续处理
        if (now >= hiddenTime) {
            return;
        }
        Optional<HonourMobilizeEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        HonourMobilizeEntity entity = opEntity.get();
        //如果没有初始化就先初始化
        if(entity.getInitTime() <= 0){
        	this.initAcivityData(playerId);;
        	this.syncActivityDataInfo(playerId);
        }
        //玩家数据里记录当前天的的日期，说明已经处理过每日登陆成就，不继续处理
        if (entity.getLoginDaysList().contains(HawkTime.getYyyyMMddIntVal())) {
            return;
        }
        //记录当天日期
        entity.recordLoginDay();
        entity.setFreeCount(0);
		entity.setLotteryCount(0);
        //给客户端同步数据
        this.syncActivityDataInfo(playerId);
        //抛每天登录事件
        HawkLog.logPrintln("HonourMobilizeActivity ContinueLoginEvent reset playerId:{}", playerId);
	
	}
	
	
	//初始化成就
	private void initAcivityData(String playerId){
		int termId = this.getActivityTermId();
		if(termId <= 0){
			return;
		}
		Optional<HonourMobilizeEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		HonourMobilizeEntity entity = opEntity.get();
		if(entity.getInitTime() > 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		//记录初始化时间
		entity.setInitTime(curTime);
		entity.setFreeCount(0);
		entity.setLotteryCount(0);
		//记录当天日期
        entity.recordLoginDay();
        //回收道具
        HawkLog.logPrintln("HonourMobilizeActivity,initActivityInfo,playerId:{}", playerId);
	}
	
	
	
	public void updateAchieveData(HonourMobilizeEntity entity){
		List<AchieveItem> itemList = new ArrayList<>();
		HonourMobilizeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HonourMobilizeRewardCfg.class, entity.getChooseId());
		if(Objects.isNull(cfg)){
			return;
		}
		for(int aid :cfg.getAchieveId()){
			HonourMobilizeAchieveCfg achieveCfg = HawkConfigManager.getInstance().getConfigByKey(HonourMobilizeAchieveCfg.class, aid);
			if(Objects.nonNull(achieveCfg)){
				AchieveItem item = AchieveItem.valueOf(achieveCfg.getAchieveId());
				itemList.add(item);
			}
		}
		entity.setItemList(itemList);
		//初始化成就
		ActivityManager.getInstance().postEvent(new AchieveItemCreateEvent(entity.getPlayerId(), itemList), true);
		 //回收道具
        HawkLog.logPrintln("HonourMobilizeActivity,updateAchieveData,playerId:{}", entity.getPlayerId());
	}
	
	
	
	/**
	 * 选择
	 * @param playerId
	 */
	public void chooseLotteryId(String playerId,int choose){
		Optional<HonourMobilizeEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		HonourMobilizeEntity entity = optional.get();
		if(entity.getChooseId() > 0){
			return;
		}
		
		HonourMobilizeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HonourMobilizeRewardCfg.class, choose);
		if(Objects.isNull(cfg)){
			return;
		}
		entity.setChooseId(choose);
		//初始化任务
		updateAchieveData(entity);
		//同步
		this.syncActivityDataInfo(playerId);
		//TLOG
		int termId = this.getActivityTermId();
		this.logHonorMobilizeChoose(playerId, termId, choose);
	}
	
	
	
	/**
	 * 选择
	 * @param playerId
	 */
	public void lottery(String playerId,int type,int hp){
		if(type== 1){
			this.lotteryRewardsOne(playerId,hp);
		}else if(type == 2){
			this.lotteryRewardsTen(playerId,hp);
		}
	}
	
	
	
	/**
	 * 10抽
	 * @param playerId
	 */
	private void lotteryRewardsTen(String playerId,int hp){
		Optional<HonourMobilizeEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		int lotteryCount = 10;
		//为空则初始化
		HonourMobilizeEntity entity = optional.get();
		HonourMobilizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourMobilizeKVCfg.class);
		List<RewardItem.Builder> costList = cfg.getTenCostItemList();
		//是否选择
		if(entity.getChooseId() <= 0){
			return;
		}
		//次数不足()
		int lCount = entity.getLotteryCount();
		if(lCount + lotteryCount > cfg.getDailyLimit()){
			logger.info("HonourMobilizeActivity,lotteryRewardsTen,fail,countless,playerId: "
					+ "{},curCount:{},lotteryCount:{}", playerId,lCount,10);
			return;
		}
		//检查消耗
		boolean cost = this.getDataGeter().cost(playerId,costList, 1, 
				Action.HONOUR_MOBILIZE_LOTTERY_TEN_COST,false);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		//每日
		entity.addLotteryCount(lotteryCount);
		//总计
		entity.addLotteryTotalCount(lotteryCount);
		//发奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		//随机奖励
		List<RewardItem.Builder> ranList = this.getRandomReward(entity.getChooseId(),lotteryCount);
		rewardList.addAll(ranList);
		//固定奖励
		List<RewardItem.Builder> fixReward = cfg.getExtRewards(lotteryCount);
		rewardList.addAll(fixReward);
		if(!ranList.isEmpty()){
			this.getDataGeter().takeReward(playerId, rewardList, 1, 
					Action.HONOUR_MOBILIZE_LOTTERY_TEN_REWARD, false,RewardOrginType.HONOUR_MOBILIZE_REWARD);
		}
		//抛事件
		ActivityManager.getInstance().postEvent(new HonourMobilizeCountEvent(playerId, entity.getLotteryTotalCount()), true);
		//返回数据
		this.syncLotteryDataInfo(playerId, ranList, fixReward,2);
		//刷新数据
		this.syncActivityDataInfo(playerId);
		//TLOG
		this.logHonorMobilizeLottery(playerId, entity.getTermId(), entity.getChooseId(), 10);
		logger.info("HonourMobilizeActivity ten lottery, playerId: {},result:{}",playerId,rewardList);
	}
	
	/**
	 * 单抽
	 * @param playerId
	 */
	private void lotteryRewardsOne(String playerId,int hp){
		Optional<HonourMobilizeEntity>  optional = this.getPlayerDataEntity(playerId);
		if (!optional.isPresent()) {
			return;
		}
		int lotteryCount = 1;
		HonourMobilizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourMobilizeKVCfg.class);
		HonourMobilizeEntity entity = optional.get();
		//是否已经选定
		if(entity.getChooseId() <= 0){
			return;
		}
		//单抽会有免费次数
		int freeCount = this.getFreeCount(entity);
		boolean free = (freeCount >= lotteryCount);
		//次数不足
		int lCount = entity.getLotteryCount();
		if(!free && lCount + lotteryCount > cfg.getDailyLimit()){
			logger.info("HonourHeroBefellActivity,lotteryRewardsOne,fail,countless,playerId: "
					+ "{},curCount:{},lotteryCount:{}", playerId,lCount,1);
			return;
		}
		if(!free){
			//检查消耗
			List<RewardItem.Builder> costList = cfg.getOneCostItemList();
			boolean cost = this.getDataGeter().cost(playerId,costList, 1, 
					Action.HONOUR_MOBILIZE_LOTTERY_ONE_COST,false);
			if (!cost) {
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, hp, Status.Error.ITEM_NOT_ENOUGH_VALUE);
				return;
			}
		}
		if(free){
			entity.addFreeCount(lotteryCount);
		}else{
			entity.addLotteryCount(lotteryCount);
		}
		entity.addLotteryTotalCount(lotteryCount);
		
		//发奖励
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		//随机奖励
		List<RewardItem.Builder> ranList = this.getRandomReward(entity.getChooseId(),lotteryCount);
		rewardList.addAll(ranList);
		//固定奖励
		List<RewardItem.Builder> fixReward = cfg.getExtRewards(lotteryCount);
		rewardList.addAll(fixReward);
		if(!ranList.isEmpty()){
			this.getDataGeter().takeReward(playerId, rewardList, 
					1, Action.HONOUR_MOBILIZE_LOTTERY_ONE_REWARD, false,RewardOrginType.HONOUR_MOBILIZE_REWARD);
		}
		//抛事件
		ActivityManager.getInstance().postEvent(new HonourMobilizeCountEvent(playerId, entity.getLotteryTotalCount()), true);
		//返回数据
		this.syncLotteryDataInfo(playerId, ranList, fixReward,1);
		//刷新数据
		this.syncActivityDataInfo(playerId);
		//TLOG
		this.logHonorMobilizeLottery(playerId, entity.getTermId(), entity.getChooseId(), 1);
		logger.info("HonourHeroBefellActivity one lottery, playerId: {},result:{}",playerId,ranList);
	}
	
	

	/**
	 * 同步抽奖信息
	 * @param playerId
	 * @param ranList
	 * @param fixReward
	 */
	public void syncLotteryDataInfo(String playerId,List<RewardItem.Builder> ranList,List<RewardItem.Builder> fixReward,int type){
		HonorMobilizeLotteryResp.Builder builder = HonorMobilizeLotteryResp.newBuilder();
		for(RewardItem.Builder itemBuilder : ranList){
			builder.addRandomRewards(itemBuilder.clone());
		}
//		for(RewardItem.Builder itemBuilder : fixReward){
//			builder.addExtRewards(itemBuilder.clone());
//		}
		builder.setType(type);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HONOR_MOBILIZE_LOTTERY_RESP_VALUE,builder));
	}
	
	
	/**
	 * 单抽是否免费
	 * @param entity
	 * @return
	 */
	private int getFreeCount(HonourMobilizeEntity entity){
		HonourMobilizeKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(HonourMobilizeKVCfg.class);
		int freeCount = cfg.getFreeCount() - entity.getFreeCount();
		return Math.max(0, freeCount);
	}
	
	
	/**
	 * 随机奖励
	 * @param count
	 * @return
	 */
	private List<RewardItem.Builder> getRandomReward(int id,int count){
		List<RewardItem.Builder> rlt = new ArrayList<>();
		HonourMobilizeRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(HonourMobilizeRewardCfg.class, id);
		for(int i=0;i<count;i++){
			List<String>  alist = this.getDataGeter().getAwardFromAwardCfg(cfg.getAward());
			for(String astr : alist){
				List<RewardItem.Builder> rlist = RewardHelper.toRewardItemImmutableList(astr);
				if(Objects.nonNull(rlist)){
					rlt.addAll(rlist);
				}
			}
		}
		return rlt;
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<HonourMobilizeEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		HonourMobilizeEntity entity = opDataEntity.get();
		HonorMobilizeInfoSync.Builder builder = HonorMobilizeInfoSync.newBuilder();
		builder.setChooseId(entity.getChooseId());
		builder.setFreeCount(this.getFreeCount(entity));
		builder.setDailyLotteryCount(entity.getLotteryCount());
		builder.setTotalLotteryCount(entity.getLotteryTotalCount());
		
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.HONOR_MOBILIZE_INFO_RESP_VALUE,builder));
	}
	


    /**
     * 荣耀动员选定英雄 
     * @param playerId
     * @param termId
     * @param chooseId  选定ID
     */
    private void logHonorMobilizeChoose(String playerId, int termId,int chooseId){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("chooseId", chooseId);
        getDataGeter().logActivityCommon(playerId, LogInfoType.honor_mobilize_choose, param);
    }
    
    /**
     * 荣耀动员抽取英雄
     * @param playerId
     * @param termId
     * @param chooseId  选定ID
     * @param lotteryType  抽奖类型 1单抽 10十抽
     */
    private void logHonorMobilizeLottery(String playerId, int termId, int chooseId, int lotteryType){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId);
        param.put("chooseId", chooseId);
        param.put("lotteryType", lotteryType);
        getDataGeter().logActivityCommon(playerId, LogInfoType.honor_mobilize_lottery, param);
    }
    
    
}
