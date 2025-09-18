package com.hawk.game.module;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import org.hawk.annotation.MessageHandler;
import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.xid.HawkXID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.event.impl.DailyBuyItemCountEvent;
import com.hawk.activity.helper.PlayerAcrossDayLoginMsg;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.type.impl.achieve.AchieveContext;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.dailyBuyGift.DailyBuyGiftActivity;
import com.hawk.activity.type.impl.dailyBuyGift.config.DailyBuyGiftAchieveCfg;
import com.hawk.activity.type.impl.dailyBuyGift.config.DailyBuyGiftKVCfg;
import com.hawk.activity.type.impl.dailyBuyGift.config.DailyBuyGiftTimeCfg;
import com.hawk.activity.type.impl.dailyBuyGift.entity.DailyBuyGiftEntity;
import com.hawk.game.config.PayGiftCfg;
import com.hawk.game.config.ServerAwardCfg;
import com.hawk.game.entity.PlayerDailyGiftBuyEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.manager.AssembleDataManager;
import com.hawk.game.player.Player;
import com.hawk.game.player.PlayerModule;
import com.hawk.game.protocol.Activity.AchieveState;
import com.hawk.game.protocol.Activity.ActivityType;
import com.hawk.game.protocol.Activity.PBDailyBuyAchieveRewardTakeReq;
import com.hawk.game.protocol.Activity.PBDailyBuyConfigAchieve;
import com.hawk.game.protocol.Activity.PBDailyBuyConfigResp;
import com.hawk.game.protocol.Activity.PBDailyBuyInfoResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

/***************
 * 每日必买宝箱
 * 这个功能当初做的时候是按照活动做的，后来发现跨服的时候 活动数据不能带到目的服，
 * 所以重新搞一下
 * 还是采用了活动的配置
 * **************
 * @author che
 *
 */
public class PlayerDailyGiftBuyModule extends PlayerModule {
	static final Logger logger = LoggerFactory.getLogger("Server");

	/**
	 * 构造函数
	 *
	 * @param player
	 */
	public PlayerDailyGiftBuyModule(Player player) {
		super(player);
	}

	@Override
	protected boolean onPlayerLogin() {
		this.fixData();
		this.syncConfigData();
		this.updateActivityInfo();
		this.syncActivityDataInfo();
		return true;
	}
	
	
	public void fixData(){
		//这个时间是  此代码上线后，宝箱第一次刷新时间，过了这个刷新时间 ，就不检查以前的数据了。
		//10.16更新上线  10.21 0点此周期结束
		String fixTimeStr = "2024-10-21 00:00:00";
		long fixTime = HawkTime.parseTime(fixTimeStr);
		long curTime = HawkTime.getMillisecond();
		if(curTime >= fixTime){
			return;
		}
		PlayerDailyGiftBuyEntity entity = this.player.getData().getPlayerDailyGiftBuyEntity();
		//只有第一次才修复
		if(entity.getRefreshTime() > 0){
			return;
		}
		DailyBuyGiftActivity backActivity = this.getDailyBuyGiftActivity(this.player.getId());
		if(Objects.isNull(backActivity)){
			return;
		}
		Optional<DailyBuyGiftEntity> opDataEntity = backActivity.getPlayerDataEntity(this.player.getId());
		if (!opDataEntity.isPresent()) {
			return;
		}
		DailyBuyGiftEntity dataEntity = opDataEntity.get();
		//已经可以刷新
		if(dataEntity.getRefreshTime() <= curTime){
			return;
		}
		
		entity.setTermId(dataEntity.getTermId());
		entity.setRefreshTime(dataEntity.getRefreshTime());
		//成就复制
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		for(AchieveItem item : dataEntity.getItemList()){
			AchieveItem itemCopy = item.getCopy();
			itemList.add(itemCopy);
		}
		entity.setItemList(itemList);
		//购买数量复制
		for(Map.Entry<Integer, Integer> entry : dataEntity.getItemRecordMap().entrySet()){
			entity.addItemRecordCount(entry.getKey(), entry.getValue());
		}
	}
	
	
	public void syncConfigData(){
		DailyBuyGiftKVCfg config = HawkConfigManager.getInstance().getKVInstance(DailyBuyGiftKVCfg.class);
		PBDailyBuyConfigResp.Builder builder = PBDailyBuyConfigResp.newBuilder();
		builder.setTimeLimit(config.getTimeLimitValue());
		ConfigIterator<DailyBuyGiftAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DailyBuyGiftAchieveCfg.class);
		while (configIterator.hasNext()) {
			DailyBuyGiftAchieveCfg next = configIterator.next();
			PBDailyBuyConfigAchieve.Builder item = PBDailyBuyConfigAchieve.newBuilder();
			item.setAchieveId(next.getAchieveId());
			item.setConditionType(next.getAchieveType().getValue());
			item.setConditionValue(next.getConditionValue());
			item.setRewards(next.getReward());
			builder.addAchieves(item);
		}
		player.sendProtocol(HawkProtocol.valueOf(HP.code2.DAILY_BUY_GIFT_CONFIG_DATA_RESP_VALUE, builder));
	}
	
	
	@MessageHandler
	public void onCrossDay(PlayerAcrossDayLoginMsg msg) {
		DailyBuyGiftActivity activity = this.getDailyBuyGiftActivity(this.player.getId());
		if(Objects.isNull(activity)){
			return;
		}
        //去当前期数
        int termId = activity.getActivityTermId();
        //取当前期数结束时间
        long endTime = activity.getTimeControl().getEndTimeByTermId(termId, this.player.getId());
        //取当前时间
        long now = HawkTime.getMillisecond();
        //如果当前时间大于当前期数结束时间，不继续处理
        if (now >= endTime) {
            return;
        }
        
        this.updateActivityInfo();
        this.syncActivityDataInfo();
    }
    
    
    public void syncActivityDataInfo() {
    	PlayerDailyGiftBuyEntity entity = this.player.getData().getPlayerDailyGiftBuyEntity();
        PBDailyBuyInfoResp.Builder builder = PBDailyBuyInfoResp.newBuilder();
    	builder.setAchieveItemCount(entity.getItemRecordCount());
    	builder.setRefreshTime(entity.getRefreshTime());
    	List<AchieveItem> alist = entity.getItemList();
    	for(AchieveItem item : alist){
    		builder.addAchieves(item.createAchieveItemPB());
    	}
    	player.sendProtocol(HawkProtocol.valueOf(HP.code2.DAILY_BUY_GIFT_INFO_RESP_VALUE, builder));
    }
    
    
    public void onBuyGift(PayGiftCfg giftCfg){
    	DailyBuyGiftKVCfg config = HawkConfigManager.getInstance().getKVInstance(DailyBuyGiftKVCfg.class);
    	long curTime = HawkTime.getMillisecond();
    	if(curTime < config.getTimeLimitValue()){
    		return;
    	}
    	ServerAwardCfg cfg = AssembleDataManager.getInstance().getServerAwardByAwardId(giftCfg.getServerAwardId());
		if (cfg == null) {
			return;
		}
		AwardItems awardItems = cfg.getAwardItems();
		Map<Integer,Long> itemMap =  awardItems.getAwardItemsCount();
    	if(itemMap.isEmpty()){
    		return;
    	}
    	PlayerDailyGiftBuyEntity entity = this.player.getData().getPlayerDailyGiftBuyEntity();
    	for(Map.Entry<Integer,Long> entry : itemMap.entrySet()){
    		int itemId = entry.getKey();
    		long value = entry.getValue();
    		if(config.getRecordGiftItemList().contains(itemId)){
    			entity.addItemRecordCount(itemId, (int)value);
    		}
    	}
    	List<AchieveItem> alist = entity.getItemList();
    	List<AchieveItem> updates = new ArrayList<>();
    	DailyBuyItemCountEvent event = new DailyBuyItemCountEvent(player.getId(), entity.getItemRecordCount());
    	for(AchieveItem item : alist){
    		DailyBuyGiftAchieveCfg achieveCfg = this.getAchieveCfg(item.getAchieveId());
    		if(Objects.isNull(achieveCfg)){
    			continue;
    		}
    		if (item.getState() != AchieveState.NOT_ACHIEVE_VALUE) {
    			continue;
    		}
    		AchieveParser<?> parser = AchieveContext.getParser(achieveCfg.getAchieveType());
    		if(Objects.isNull(parser)){
    			continue;
    		}
    		parser.updateAchieveData(item, achieveCfg, event, updates);
    	}
    	if(updates.size() > 0){
    		//保存
    		entity.notifyUpdate();
    		//同步信息
        	this.syncActivityDataInfo();
    	}
    	
    }
    
    
    /**
     *  初始化玩家数据
     * @param playerId
     */
    private void updateActivityInfo(){
    	PlayerDailyGiftBuyEntity entity = this.player.getData().getPlayerDailyGiftBuyEntity();
		long curTime = HawkTime.getMillisecond();
		if(curTime < entity.getRefreshTime()){
			logger.info("DailyBuyGiftActivity  updateActivityInfo timeLimit, playerId: {},curTime:{},refreshTime:{}", 
					this.player.getId(),curTime,entity.getRefreshTime());
			return;
		}
		//任务
		List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		// 初始添加成就项
		ConfigIterator<DailyBuyGiftAchieveCfg> configIterator = HawkConfigManager.getInstance()
				.getConfigIterator(DailyBuyGiftAchieveCfg.class);
		while (configIterator.hasNext()) {
			DailyBuyGiftAchieveCfg next = configIterator.next();
			AchieveItem item = AchieveItem.valueOf(next.getAchieveId());
			itemList.add(item);
			
		}
		//成就列表
		entity.setItemList(itemList);
		//刷新时间
		long refreshTime = this.calRefreshTime();
		entity.setRefreshTime(refreshTime);
		//重置道具获取个数
		entity.clearItemRecordCount();
    }
    
    
    
    /**
     * 更新刷新时间
     * @param entity
     */
    private long calRefreshTime(){
    	DailyBuyGiftActivity activity = this.getDailyBuyGiftActivity(this.player.getId());
    	int termId = activity.getActivityTermId();
    	DailyBuyGiftKVCfg config = HawkConfigManager.getInstance().getKVInstance(DailyBuyGiftKVCfg.class);
    	DailyBuyGiftTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(DailyBuyGiftTimeCfg.class, termId);
    	long startTime = timeCfg.getStartTimeValue();
    	long startTimeZero = HawkTime.getAM0Date(new Date(startTime)).getTime();
    	long curTime = HawkTime.getMillisecond();
    	long count  = (curTime - startTimeZero) / (config.getRefreshDays() * HawkTime.DAY_MILLI_SECONDS) + 1;
    	long refreshTime = startTimeZero + (count * config.getRefreshDays() * HawkTime.DAY_MILLI_SECONDS);
    	return refreshTime;
    }
    
    public DailyBuyGiftAchieveCfg getAchieveCfg(int achieveId) {
    	DailyBuyGiftAchieveCfg config = HawkConfigManager.getInstance()
    			.getConfigByKey(DailyBuyGiftAchieveCfg.class, achieveId);
		return config;
    }
    

  
    
    
    /**
     * 是否在活动时间内
     * @param playerId
     * @return
     */
    public DailyBuyGiftActivity getDailyBuyGiftActivity(String playerId) {
		Optional<ActivityBase> activity = ActivityManager.getInstance().getGameActivityByType(ActivityType.DAILY_BUY_GIFT_VALUE);
		if(!activity.isPresent()){
			return null;
		}
		DailyBuyGiftActivity backActivity = (DailyBuyGiftActivity) activity.get();
		if(!backActivity.isShow(playerId)){
			return null;
		}
		return backActivity;
	} 
	

	/**
	 * 领奖
	 * 
	 * @param resourceType
	 * @return
	 */
	@ProtocolHandler(code = HP.code2.DAILY_BUY_GIFT_ACHIEVE_REWARD_TAKE_REQ_VALUE)
	public boolean onPlayerTakeReward(HawkProtocol protocol) {		
		PBDailyBuyAchieveRewardTakeReq req = protocol.parseProtocol(PBDailyBuyAchieveRewardTakeReq.getDefaultInstance());
		int achieveId = req.getAchieveId();
		DailyBuyGiftAchieveCfg achieveCfg = this.getAchieveCfg(achieveId);
		if(Objects.isNull(achieveCfg)){
			return false;
		}
		PlayerDailyGiftBuyEntity entity = this.player.getData().getPlayerDailyGiftBuyEntity();
		AchieveItem achieveItem = null;
		List<AchieveItem> alist = entity.getItemList();
		for(AchieveItem item : alist){
			if(item.getAchieveId() ==  achieveId){
				achieveItem = item;
			}
		}
		if(Objects.isNull(achieveItem)){
			return false;
		}
			
		// 成就未达成
		if(achieveItem.getState() == AchieveState.NOT_ACHIEVE_VALUE){
			return false;
		}
		// 已领取
		if (achieveItem.getState() == AchieveState.TOOK_VALUE) {
			return false;
		}
		achieveItem.setState(AchieveState.TOOK_VALUE);
		List<RewardItem.Builder> reweardList = achieveCfg.getRewardList();
		if (!reweardList.isEmpty()) {
			HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, this.player.getId());
			PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(reweardList, Action.DAIY_BUY_GIFT_ACHIEVE_REWARD, 
					true, RewardOrginType.ACTIVITY_REWARD, achieveId);
			HawkTaskManager.getInstance().postMsg(xid, msg);
		}
		entity.notifyUpdate();
		this.syncActivityDataInfo();
		logger.info("[achieve] take achieve reward. playerId: {}, achieveId: {}, source: {}", this.player.getId(), achieveId, "DailyBuyGiftAchieve");
		return true;
	}
	
}
