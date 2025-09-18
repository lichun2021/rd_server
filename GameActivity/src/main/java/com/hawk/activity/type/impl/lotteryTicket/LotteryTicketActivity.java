package com.hawk.activity.type.impl.lotteryTicket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.task.HawkTaskManager;
import org.hawk.thread.HawkTask;
import org.hawk.tuple.HawkTuple3;
import org.hawk.tuple.HawkTuples;
import org.hawk.uuid.HawkUUIDGenerator;
import org.hawk.xid.HawkXID;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.constant.ObjType;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.msg.PlayerRewardFromActivityMsg;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketGiftCfg;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketKVCfg;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketRewardCfg;
import com.hawk.activity.type.impl.lotteryTicket.config.LotteryTicketTimeCfg;
import com.hawk.activity.type.impl.lotteryTicket.entitiy.LotteryTicketEntity;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistAchieveUpdateResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistApply;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistApplyUpdateResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistRecordResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistSearchResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketAssistSendResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketBarrage;
import com.hawk.game.protocol.Activity.PBLotteryTicketBarrageResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketFriendAssist;
import com.hawk.game.protocol.Activity.PBLotteryTicketGiftBuy;
import com.hawk.game.protocol.Activity.PBLotteryTicketPageResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketUseRecordResp;
import com.hawk.game.protocol.Activity.PBLotteryTicketUseResp;
import com.hawk.game.protocol.Const.ChatType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
import com.hawk.log.LogConst.LogInfoType;

public class LotteryTicketActivity extends ActivityBase{
	//生效中的代刮申请
	public Map<String, LotteryRecourse> lotteryRecourseMap = new ConcurrentHashMap<>();
	//受邀次数
	public Map<String,Integer> assistCountMap = new ConcurrentHashMap<>();
	//弹幕
	public Queue<LotteryBarrage> barrageQueue = new ConcurrentLinkedQueue<>();
	//tick时间点
	public long tickTime;
	public int tickCount;
	
	
    public LotteryTicketActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.LOTTERY_TICKET;
    }
    
    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
    	LotteryTicketActivity activity = new LotteryTicketActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<LotteryTicketEntity> queryList = HawkDBManager.getInstance()
                .query("from LotteryTicketEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
        	LotteryTicketEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
    	LotteryTicketEntity entity = new LotteryTicketEntity(playerId, termId);
        return entity;
    }

  

    
    @Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.LOTTERY_TICKET_INIT, ()-> {
				this.initActivityInfo(playerId);
				syncActivityDataInfo(playerId);
			});
		}
	}
    
    
    
    @Override
    public void onHidden() {
    	this.sendBackAssistTicket();
    	this.lotteryRecourseMap = new ConcurrentHashMap<>();
    	this.assistCountMap = new ConcurrentHashMap<>();
    	this.barrageQueue = new ConcurrentLinkedQueue<>();
    }
    
    
    @Override
    public void onTick() {
    	long curTime = HawkTime.getMillisecond();
    	if(this.tickTime <= 0){
    		this.loadActivityData();
    		this.tickTime = curTime;
    		tickCount = 1;
    		return;
    	}
    	//tick 10S
    	if(curTime - this.tickTime >= 10 * 1000){	
    		this.doUpdateLotteryRecourse();
    		//一分钟存一次
    		if(this.tickCount % 6 == 0){
    			this.doUpdateAssistCountMap(curTime);
    			this.saveBarrage();
    		}
    		this.tickTime = curTime;
    		tickCount ++;
    	}
    }
    
    
    @Override
   	public void onPlayerLogin(String playerId) {
       	//初始化数据
   		initActivityInfo(playerId);
   		
   	}
    
    /**
     * 初始化
     * @param playerId
     */
    public void initActivityInfo(String playerId){
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketEntity entity = opEntity.get();
        if(entity.getInitTime() > 0){
        	return;
        }
        long curTime = HawkTime.getMillisecond();
        entity.setInitTime(curTime);
    }
    
	
        
    /**
     * 直购礼包
     * @param event
     */
    @Subscribe
	public void onGiftBuyEvent(PayGiftBuyEvent event) {
    	String giftId = event.getGiftId();
    	int configId = LotteryTicketGiftCfg.getBuyId(giftId);
    	LotteryTicketGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketGiftCfg.class, configId);
    	if(Objects.isNull(cfg)){
    		return;
    	}
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(event.getPlayerId());
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketEntity entity = opEntity.get();
        entity.addBuyCount(configId, 1);
        this.syncActivityDataInfo(event.getPlayerId());
    }
    
    /**
     * 直购验证
     * @param playerId
     * @param giftId
     * @return
     */
    public boolean buyGiftVerify(String playerId,String giftId){
    	int configId = LotteryTicketGiftCfg.getBuyId(giftId);
    	LotteryTicketGiftCfg cfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketGiftCfg.class, configId);
    	if(Objects.isNull(cfg)){
    		return false;
    	}
    	
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return false;
        }
        LotteryTicketEntity entity = opEntity.get();
        int buyCount = entity.getBuyCount(configId);
        if(buyCount >= cfg.getTimes()){
        	return false;
        }
    	return true;
    }
    
    
    @Override
    public void syncActivityDataInfo(String playerId) {
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketEntity entity = opEntity.get();
        
        PBLotteryTicketPageResp.Builder builder = PBLotteryTicketPageResp.newBuilder();
    	
    	for(Map.Entry<Integer, Integer> buy : entity.getBuyNumMap().entrySet()){
    		PBLotteryTicketGiftBuy.Builder bbuilder = PBLotteryTicketGiftBuy.newBuilder();
        	bbuilder.setGiftId(buy.getKey());
        	bbuilder.setBuyCount(buy.getValue());
        	builder.addBuys(bbuilder);
        }
    	this.addLotteryAssistApplyBuilder(playerId, builder);
        pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_PAGE_RESP_VALUE, builder);
    }
    
    
    /**
     * 获取玩家好友求助信息
     * @param playerId
     */
    public void getPlayerFriendAssistPage(String playerId){
    	 if (!isOpening(playerId)) {
             return;
         }
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	PBLotteryTicketAssistSearchResp.Builder builder = PBLotteryTicketAssistSearchResp.newBuilder();
    	List<String> friends = this.getDataGeter().getPlayerGameFriends(playerId);
    	String serverId = this.getDataGeter().getServerId();
    	for(String id : friends){
    		if(HawkOSOperator.isEmptyString(id)){
    			continue;
    		}
    		if(this.getDataGeter().isCrossPlayer(id)){
    			continue;
    		}
    		if (!this.getDataGeter().checkPlayerExist(id)) {
    			continue;
    		}
    		String playerServer = this.getDataGeter().getPlayerMainServerId(id);
    		if(!serverId.equals(playerServer)){
    			continue;
    		}
    		
    		String name = ActivityManager.getInstance().getDataGeter().getPlayerName(id);
    		String guildName = ActivityManager.getInstance().getDataGeter().getGuildNameByByPlayerId(id);
    		String guildTag = ActivityManager.getInstance().getDataGeter().getGuildTagByPlayerId(id);
    		int icon = this.getDataGeter().getIcon(id);
    		String pfIcon = this.getDataGeter().getPfIcon(id);
    		
    		if(HawkOSOperator.isEmptyString(name)){
    			continue;
    		}
    		int count = this.assistCountMap.getOrDefault(id, 0);
    		int lastCount = config.getAssistLimit() - count;
    		lastCount = Math.max(lastCount, 0);
    		PBLotteryTicketFriendAssist.Builder fbuilder = PBLotteryTicketFriendAssist.newBuilder();
    		fbuilder.setPlayerId(id);
    		fbuilder.setPlayeName(name);
    		fbuilder.setAssistLastCount(lastCount);
    		fbuilder.setIcon(icon);
    		if(!HawkOSOperator.isEmptyString(guildName)){
    			fbuilder.setGuildName(guildName);
    		}
    		if(!HawkOSOperator.isEmptyString(guildTag)){
    			fbuilder.setGuildTag(guildTag);
    		}
    		if(!HawkOSOperator.isEmptyString(pfIcon)){
    			fbuilder.setPfIcon(pfIcon);
    		}
    		builder.addFriendAssist(fbuilder);
    	}
    	pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_PAGE_RESP_VALUE, builder);
    	
    }
    
    
    
    public void onPlayerAssistLotterySearch(String playerId,String searchName){
    	 if (!isOpening(playerId)) {
             return;
         }
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	
        PBLotteryTicketAssistSearchResp.Builder builder = PBLotteryTicketAssistSearchResp.newBuilder();
        String searchId = this.getDataGeter().getPlayerByName(searchName);
        if(HawkOSOperator.isEmptyString(searchId)){
        	pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_SEARCH_RESP_VALUE, builder);
        	return;
        }
		String guildName = this.getDataGeter().getGuildNameByByPlayerId(searchId);
		String guildTag = this.getDataGeter().getGuildTagByPlayerId(searchId);
		int icon = this.getDataGeter().getIcon(searchId);
		String pfIcon = this.getDataGeter().getPfIcon(searchId);
		
		int count = this.assistCountMap.getOrDefault(searchId, 0);
		int lastCount = config.getAssistLimit() - count;
		lastCount = Math.max(lastCount, 0);
		PBLotteryTicketFriendAssist.Builder fbuilder = PBLotteryTicketFriendAssist.newBuilder();
		fbuilder.setPlayerId(searchId);
		fbuilder.setPlayeName(searchName);
		fbuilder.setAssistLastCount(lastCount);
		fbuilder.setIcon(icon);
		if(!HawkOSOperator.isEmptyString(guildName)){
			fbuilder.setGuildName(guildName);
		}
		if(!HawkOSOperator.isEmptyString(guildTag)){
			fbuilder.setGuildTag(guildTag);
		}
		if(!HawkOSOperator.isEmptyString(pfIcon)){
			fbuilder.setPfIcon(pfIcon);
		}
		
		
		builder.addFriendAssist(fbuilder);
		pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_SEARCH_RESP_VALUE, builder);
    }
    
    /**
     * 玩家抽奖历史
     * @param playerId
     */
    public void getPlayerLotteryRecord(String playerId){
    	Map<String,LotteryRlt> dataMap = this.getPlayerLotteryRecordData(playerId);
    	PBLotteryTicketUseRecordResp.Builder builder = PBLotteryTicketUseRecordResp.newBuilder();
    	for(LotteryRlt rlt : dataMap.values()){
    		builder.addRlts(rlt.genBuilder());
    	}
    	pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_USE_RECORD_RESP_VALUE, builder);
    }
    
    
    
    
    
    /**
     * 玩家抽奖
     * @param playerId
     */
    public void onPlayerLottery(String playerId,int tIndex){
    	 if (!isOpening(playerId)) {
             return;
         }
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
		boolean cost = this.getDataGeter().cost(playerId, config.getLotteryCostItems(), 1,
	            Action.LOTTERY_TICKET_USE_COST, true);
	    if (!cost) {
	    	HawkLog.logPrintln("LotteryTicketActivity,onPlayerLottery item Less,playerId{},", playerId);
	        PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_USE_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
	        return;
	    }
	 
	    int termId = this.getActivityTermId();
	    long lotteryCount = this.addPlayerLotteryCount(playerId);
	    LotteryRlt rlt = this.getLotteryTicketRlt((int) lotteryCount);
	    //保存数据
	    this.savePlayerLotteryRlt(playerId, rlt);
	    //奖励相关
	    List<RewardItem.Builder> rewards = new ArrayList<>();
	    for(HawkTuple3<Integer, Integer, Integer> rewardTuple : rlt.getRewards()){
	    	int rewardId = rewardTuple.first;
	    	if(rewardId <= 0){
	    		continue;
	    	}
	    	LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, rewardId);
	    	List<RewardItem.Builder> rList = rewardCfg.getRewardItemList();
	    	if(!rList.isEmpty()){
	    		rewards.addAll(rList);
	    	}
	    	this.addBroadcastMsg(playerId,null, rewardId);
	    	//TLOG
		    this.logLotteryTicketUse(termId, playerId, 0,"0",rewardId,rewardCfg.getMultiplyValue());
	    }
	    //额外奖励
	    if(!HawkOSOperator.isEmptyString(config.getExtReward())){
    		rewards.addAll(RewardHelper.toRewardItemImmutableList(config.getExtReward()));
    	}
	    //发奖励
	    HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, playerId);
		PlayerRewardFromActivityMsg msg = PlayerRewardFromActivityMsg.valueOf(rewards, Action.LOTTERY_TICKET_USE_ACHIEVE,
				false, RewardOrginType.ACTIVITY_REWARD, 0);
		HawkTaskManager.getInstance().postMsg(xid, msg);
	    //发有奖通知
	    String playerName = this.getDataGeter().getPlayerName(playerId);
		Object[] content =  new Object[]{playerName};
	    this.getDataGeter().sendMail(playerId, MailId.LOTTERY_TICKET_USE_REWRD_MAIL, null, null, content,
	    		rewards, true);
	    //返回消息
	    PBLotteryTicketUseResp.Builder builder = PBLotteryTicketUseResp.newBuilder();
	    builder.setRlt(rlt.genBuilder());
	    builder.setTicketIndex(tIndex);
	    pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_USE_RESP_VALUE, builder);
	    //日志
	    HawkLog.logPrintln("LotteryTicketActivity,onPlayerLottery,playerId{},lotteryCount:{},rlt:{}", playerId,lotteryCount,rlt.serializ());
    }
    
    /**
     * 拒绝代刮
     * @param playerId
     * @param assistId
     */
    public void onPlayerAssistRefuse(String playerId,String assistId){
    	if (!isOpening(playerId)) {
            return;
        }
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        //已经过期
        LotteryRecourse lotteryRecourse = this.lotteryRecourseMap.get(assistId);
        if(Objects.isNull(lotteryRecourse)){
        	return;
        }
        if(!lotteryRecourse.getAssistId().equals(playerId)){
        	return;
        }
        //已经刮完了
        if(lotteryRecourse.getLotteryCount() >= lotteryRecourse.getTicketCount() ){
        	return;
        }
        //已经结束了
        if(lotteryRecourse.getFinishTime() > 0){
        	return;
        }
        long curTime = HawkTime.getMillisecond();
        lotteryRecourse.setRefuseTime(curTime);
        lotteryRecourse.setFinishTime(curTime);
        
        int termId = this.getActivityTermId();
        //先保存
	    String serverId = this.getDataGeter().getPlayerServerId(lotteryRecourse.getSourceId());
	    String dataKey = this.getLotteryTicketRecourseKey(termId, serverId);
    	ActivityGlobalRedis.getInstance().hset(dataKey, lotteryRecourse.getId(),lotteryRecourse.serializ(),
    			(int)TimeUnit.DAYS.toSeconds(30));
    	//发道具
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	int count = lotteryRecourse.getTicketCount() - lotteryRecourse.getLotteryCount();
    	List<RewardItem.Builder> list = config.getLotteryCostItems();
		list.forEach(re->re.setItemCount(re.getItemCount() * count));
//    	ActivityReward reward = new ActivityReward(list, Action.LOTTERY_TICKET_ASSIST_APPLY_REFUSE_BACK);
//		reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
//		reward.setAlert(false);
//		postReward(lotteryRecourse.getSourceId(), reward, false);
    	//发邮件
    	String playerName = this.getDataGeter().getPlayerName(playerId);
		Object[] content =  new Object[]{playerName,count};
	    this.getDataGeter().sendMail(lotteryRecourse.getSourceId(), MailId.LOTTERY_TICKET_APPY_BACK_REFUSE, null, null, content,
	    		list, false);
    	//更新受邀人信息
	    PBLotteryTicketAssistApply abuilder = lotteryRecourse.genAssistApplyBuilder();
	    PBLotteryTicketAssistAchieveUpdateResp.Builder achievebuilder = PBLotteryTicketAssistAchieveUpdateResp.newBuilder();
	    achievebuilder.setRlts(abuilder);
	    pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_ACHIEVE_UPDATERESP_VALUE, achievebuilder);
	    //更新邀请人信息
	    PBLotteryTicketAssistApplyUpdateResp.Builder applayBuilder = PBLotteryTicketAssistApplyUpdateResp.newBuilder();
	    applayBuilder.setRlts(abuilder);
	    pushToPlayer(lotteryRecourse.getSourceId(), HP.code2.LOTTERY_TICKET_ASSIST_APPLY_UPDATE_RESP_VALUE, applayBuilder);
	    //日志
	    this.logLotteryTicketAssistBack(termId, lotteryRecourse.getId(), lotteryRecourse.getSourceId(), lotteryRecourse.getAssistId(),
	    		lotteryRecourse.getTicketCount(), lotteryRecourse.getLotteryCount(), 2);
	    HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistRefuse,playerId{},applyId:{},sourceId:{},lotteryCount:{},ticketCount:{}", 
	    		playerId,lotteryRecourse.getId(),lotteryRecourse.getSourceId(),lotteryRecourse.getLotteryCount(),lotteryRecourse.getTicketCount());
        
    }
    
    
    
    /**
     * 好友代刮
     * @param playerId
     * @param assistId
     */
    public void onPlayerAssistLottery(String playerId,String assistId,int tIndex){
    	if (!isOpening(playerId)) {
            return;
        }
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        //已经过期
        LotteryRecourse lotteryRecourse = this.lotteryRecourseMap.get(assistId);
        if(Objects.isNull(lotteryRecourse)){
        	return;
        }
        if(lotteryRecourse.getFinishTime() > 0){
        	HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistLottery getFinishTime LIMIT,playerId{},assistId:{},outTime:{}", 
        			playerId,assistId,lotteryRecourse.getFinishTime());
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_REQ_VALUE,
 	        		Status.Error.LOTTERY_TICKET_ASSIST_OUT_LIMIT_VALUE);
        	return;
        }
        int termId = this.getActivityTermId();
        //数量不足
        int lotteryCount = lotteryRecourse.getLotteryCount();
        if(lotteryCount >= lotteryRecourse.getTicketCount()){
        	HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistLottery countLess,playerId{},applyId:{},assist:{},lotteryCount:{},allCount:{}", 
		    		playerId,lotteryRecourse.getId(),lotteryRecourse.getAssistId(),lotteryRecourse.getLotteryCount(),lotteryRecourse.getTicketCount());
        	return;
        }
        long curTime = HawkTime.getMillisecond();
        lotteryRecourse.setLotteryCount(lotteryCount + 1);
        //摇奖
        long senderLotteryCount = this.addPlayerLotteryCount(playerId);
	    LotteryRlt rlt = this.getLotteryTicketRlt((int) senderLotteryCount);
	    lotteryRecourse.getRlts().add(rlt);
	    if(lotteryRecourse.getLotteryCount() >= lotteryRecourse.getTicketCount()){
	    	lotteryRecourse.setFinishTime(curTime);
        }
	    //先保存
	    String serverId = this.getDataGeter().getPlayerServerId(lotteryRecourse.getSourceId());
	    String dataKey = this.getLotteryTicketRecourseKey(termId, serverId);
    	ActivityGlobalRedis.getInstance().hset(dataKey, lotteryRecourse.getId(),lotteryRecourse.serializ(),
    			(int)TimeUnit.DAYS.toSeconds(30));
    	
    	//发奖励
    	List<RewardItem.Builder> rewards = new ArrayList<>();
    	List<RewardItem.Builder> assistRewards = new ArrayList<>();
    	for(HawkTuple3<Integer, Integer, Integer> rewardTuple : rlt.getRewards()){
	    	int rewardId = rewardTuple.first;
	    	if(rewardId <= 0){
	    		continue;
	    	}
	    	LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, rewardId);
	    	List<RewardItem.Builder> rlist = rewardCfg.getRewardItemList();
	    	if(!rlist.isEmpty()){
	    		rewards.addAll(rlist);
	    	}
	    	//帮刮奖励
	    	List<RewardItem.Builder> alist = rewardCfg.getAssistRewardList();
	    	if(!alist.isEmpty()){
	    		assistRewards.addAll(alist);
	    	}
	    	//广播
	    	this.addBroadcastMsg(lotteryRecourse.getSourceId(),playerId, rewardId);
	    	//tlog
	    	this.logLotteryTicketUse(termId, playerId, 1,lotteryRecourse.getId(),rewardId,rewardCfg.getMultiplyValue());
	    }
    	//帮刮者奖励
    	if(!assistRewards.isEmpty()){
    		String sourceName = this.getDataGeter().getPlayerName(lotteryRecourse.getSourceId());
    		Object[] content =  new Object[]{sourceName};
    		this.getDataGeter().sendMail(playerId, MailId.LOTTERY_TICKET_ASSIST_USE_ASSISTER_REWARD, null, null, content,
    				assistRewards, false);
    	}
    	//邀请者奖励
	    String playerName = this.getDataGeter().getPlayerName(playerId);
	    Object[] content =  new Object[]{playerName};
	    this.getDataGeter().sendMail(lotteryRecourse.getSourceId(), MailId.LOTTERY_TICKET_ASSIST_USE_REWARD, null, null, content,
	    		rewards, false);
        //返回抽奖信息
	    PBLotteryTicketAssistResp.Builder builder = PBLotteryTicketAssistResp.newBuilder();
	    builder.setRlt(rlt.genBuilder());
	    builder.setTicketIndex(tIndex);
	    pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_RESP_VALUE, builder);
	    //更新受邀人信息
	    PBLotteryTicketAssistApply abuilder = lotteryRecourse.genAssistApplyBuilder();
	    PBLotteryTicketAssistAchieveUpdateResp.Builder achievebuilder = PBLotteryTicketAssistAchieveUpdateResp.newBuilder();
	    achievebuilder.setRlts(abuilder);
	    pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_ACHIEVE_UPDATERESP_VALUE, achievebuilder);
	    //更新邀请人信息
	    PBLotteryTicketAssistApplyUpdateResp.Builder applayBuilder = PBLotteryTicketAssistApplyUpdateResp.newBuilder();
	    applayBuilder.setRlts(abuilder);
	    pushToPlayer(lotteryRecourse.getSourceId(), HP.code2.LOTTERY_TICKET_ASSIST_APPLY_UPDATE_RESP_VALUE, applayBuilder);
	    //日志
	    HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistLottery sucess,playerId{},applyId:{},sourceId:{},lotteryCount:{},ticketCount:{},countParam:{},rlt:{}", 
	    		playerId,lotteryRecourse.getId(),lotteryRecourse.getSourceId(),lotteryRecourse.getLotteryCount(),lotteryRecourse.getTicketCount(),senderLotteryCount,rlt.serializ());
	    
    }
    
    
    /**
     * 求助好友代刮
     * @param playerId
     * @param assistId
     */
    public void onPlayerAssistApply(String playerId,String assistId,int ticketCount){
    	if (!isOpening(playerId)) {
            return;
        }
    	if(playerId.equals(assistId)){
    		return;
    	}
    	if(ticketCount <= 0){
    		HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply countERR,playerId{},ticketCount:{}", playerId,ticketCount);
    		return;
    	}
    	boolean cross = this.getDataGeter().isCrossPlayer(assistId);
    	if(cross){
    		HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply in cross,playerId{},assistId:{}", playerId,assistId);
    		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
	        		Status.Error.LOTTERY_TICKET_ASSIST_IN_CROSS_VALUE);
    	}
    	long curTime = HawkTime.getMillisecond();
    	if(!this.inAssistLotteryTime(curTime)){
    		HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply in limitTime,playerId{},assistId:{}", playerId,assistId);
    		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
	        		Status.Error.LOTTERY_TICKET_ASSIST_TIME_LESS_VALUE);
    		return;
    	}
    	Optional<LotteryTicketEntity> opEntity = getPlayerDataEntity(playerId);
        if (!opEntity.isPresent()) {
            return;
        }
        LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
        int assistCount = this.assistCountMap.getOrDefault(assistId, 0);
        if(assistCount >= config.getAssistLimit()){
        	HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply assistCountLimit,playerId{},assistId:{},assistCount:{}", playerId,assistId,assistCount);
        	PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
 	        		Status.Error.LOTTERY_TICKET_ASSIST_COUNT_LIMIT_VALUE);
        	return;
        }
		boolean cost = this.getDataGeter().cost(playerId, config.getLotteryCostItems(), ticketCount,
	            Action.LOTTERY_TICKET_ASSIST_APPLY_COST, true);
	    if (!cost) {
	    	HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply ItemLess,playerId{},assistId:{},assistCount:{}", playerId,assistId,ticketCount);
	        PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
	        		Status.Error.ITEM_NOT_ENOUGH_VALUE);
	        return;
	    }
		HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				boolean cross = getDataGeter().isCrossPlayer(assistId);
		    	if(cross){
		    		List<RewardItem.Builder> backItems = config.getLotteryCostItems();
		        	backItems.forEach(re->re.setItemCount(re.getItemCount() * ticketCount));
		        	ActivityReward reward = new ActivityReward(backItems, Action.LOTTERY_TICKET_ASSIST_APPLY_FAIL_BACK);
					reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
					reward.setAlert(false);
					postReward(playerId, reward, false);
		    		HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply in cross2,playerId{},assistId:{}", playerId,assistId);
		    		PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
			        		Status.Error.LOTTERY_TICKET_ASSIST_IN_CROSS_VALUE);
		    		return null;
		    	}
				//如果失败了 则返回道具
		    	int playerAssistCount = assistCountMap.getOrDefault(assistId, 0);
		        if(playerAssistCount >= config.getAssistLimit()){
		        	//返还道具
		        	List<RewardItem.Builder> backItems = config.getLotteryCostItems();
		        	backItems.forEach(re->re.setItemCount(re.getItemCount() * ticketCount));
		        	ActivityReward reward = new ActivityReward(backItems, Action.LOTTERY_TICKET_ASSIST_APPLY_FAIL_BACK);
					reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
					reward.setAlert(false);
					postReward(playerId, reward, false);
					HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply assistCountLimit2,playerId{},assistId:{},assistCount:{}", 
							playerId,assistId,assistCount);
					PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_REQ_VALUE,
				        		Status.Error.LOTTERY_TICKET_ASSIST_COUNT_LIMIT_VALUE);
		        	return null;
		        }
		        int termId = getActivityTermId();
			    String rId = HawkUUIDGenerator.genUUID();
			    LotteryRecourse lotteryRecourse = new LotteryRecourse(rId, playerId, assistId, ticketCount, curTime);
				//记录申请
				String serverId = getDataGeter().getPlayerServerId(lotteryRecourse.getSourceId());
				String dataKey = getLotteryTicketRecourseKey(termId, serverId);
		    	ActivityGlobalRedis.getInstance().hset(dataKey, lotteryRecourse.getId(), 
		    			lotteryRecourse.serializ(),(int)TimeUnit.DAYS.toSeconds(30));
		    	//冗余记录一下，方便后面查数据
		    	String playerDatakey = getPlayerLotteryAssistKey(termId, playerId);
		    	ActivityGlobalRedis.getInstance().hset(playerDatakey, lotteryRecourse.getId(), 
		    			HawkTime.formatTime(curTime),(int)TimeUnit.DAYS.toSeconds(30));
		    	//更新缓存
			    lotteryRecourseMap.put(lotteryRecourse.getId(), lotteryRecourse);
			    assistCountMap.put(assistId, playerAssistCount + lotteryRecourse.getTicketCount());
		    	//返回消息
		    	int assistCount = assistCountMap.getOrDefault(assistId, 0);
		    	assistCount = Math.max(0, config.getAssistLimit() - assistCount);
		    	PBLotteryTicketAssistSendResp.Builder rebuilder = PBLotteryTicketAssistSendResp.newBuilder();
		    	rebuilder.setAssistId(assistId);
		    	rebuilder.setAssistCoun(assistCount);
			    pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_APPLY_RESP_VALUE,rebuilder);
			    //更新邀请人信息
			    syncActivityDataInfo(playerId);
			    //更新受邀人
			    if(getDataGeter().isOnlinePlayer(assistId)){
			    	 syncActivityDataInfo(assistId);
			    }
			    //日志
			    logLotteryTicketAssistSend(termId, playerId, rId, assistId, ticketCount);
			    HawkLog.logPrintln("LotteryTicketActivity,onPlayerAssistApply sucess,playerId{},applyId:{},assist:{},lotteryCount:{}", 
			    		playerId,lotteryRecourse.getId(),lotteryRecourse.getAssistId(),lotteryRecourse.getTicketCount());
				return null;
			}
		}, 1);
    }
    
    
    /**
     * 代刮结果记录
     * @param playerId
     * @param assisId
     */
    public void onPlayerAssistRecord(String playerId,String assisId){
    	LotteryRecourse lotteryRecourse = this.lotteryRecourseMap.get(assisId);
    	if(Objects.isNull(lotteryRecourse)){
    		return;
    	} 
    	if(!lotteryRecourse.getSourceId().equals(playerId) &&
    			!lotteryRecourse.getAssistId().equals(playerId)){
    		return;
    	}
    	PBLotteryTicketAssistRecordResp.Builder builder = PBLotteryTicketAssistRecordResp.newBuilder();
    	for(LotteryRlt rlt : lotteryRecourse.getRlts()){
    		builder.addRlts(rlt.genBuilder());
    	}
    	pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_ASSIST_RECORD_RESP_VALUE, builder);
    }
    
    /**
     * 弹幕
     * @param playerId
     */
    public void onPlayerBarrage(String playerId){
    	List<LotteryBarrage> blist =new ArrayList<>();
    	blist.addAll(this.barrageQueue);
    	PBLotteryTicketBarrageResp.Builder builder = PBLotteryTicketBarrageResp.newBuilder();
    	for(LotteryBarrage barrage :blist){
    		String playerName = this.getDataGeter().getPlayerName(barrage.getPlayerId());
    		if(HawkOSOperator.isEmptyString(playerName)){
    			continue;
    		}
    		PBLotteryTicketBarrage.Builder bbuilder = PBLotteryTicketBarrage.newBuilder();
    		bbuilder.setPlayerName(playerName);
    		bbuilder.setRewardId(barrage.getRewardId());
    		bbuilder.setAssist(barrage.getAssist());
    		builder.addBarrage(bbuilder);
    	}
    	pushToPlayer(playerId, HP.code2.LOTTERY_TICKET_BARRAGE_RESP_VALUE, builder);
    }
    
    
    /**
     * 加载数据
     */
    public void loadActivityData(){
    	int termId = this.getActivityTermId();
    	Map<String, LotteryRecourse> lotteryRecourses = new ConcurrentHashMap<>();
    	Map<String,Integer> assists = new ConcurrentHashMap<>();
    	long curTime = HawkTime.getMillisecond();
    	List<String> slist = this.getDataGeter().getMergeServerList();
    	String sId = this.getDataGeter().getServerId();
    	Set<String> serverList = new HashSet<>();
    	if(Objects.nonNull(slist)){
    		serverList.addAll(slist);
    	}else{
    		serverList.add(sId);
    	}
    	for(String serverId : serverList){
    		String dataKey = this.getLotteryTicketRecourseKey(termId, serverId);
    		Map<String,String> rlt = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(dataKey);
    		for(String data : rlt.values()){
    			LotteryRecourse lotteryRecourse = new LotteryRecourse();
    			lotteryRecourse.mergeFrom(data);
    			lotteryRecourses.put(lotteryRecourse.getId(), lotteryRecourse);
    			if(HawkTime.isSameDay(curTime, lotteryRecourse.getApplyTime())){
    				int count = assists.getOrDefault(lotteryRecourse.getAssistId(), 0);
    				count += lotteryRecourse.getTicketCount();
    				assists.put(lotteryRecourse.getAssistId(), count);
    			}
    		}
    	}
    	
    	this.lotteryRecourseMap = lotteryRecourses;
    	this.assistCountMap = assists;
    	
    	Queue<LotteryBarrage> barrages = new ConcurrentLinkedQueue<>();
    	String serverId = this.getDataGeter().getServerId();
    	String key = this.getBarrageKey(serverId, termId);
    	List<String> datas = ActivityGlobalRedis.getInstance().getRedisSession()
    			.lRange(key, 0, -1, (int)TimeUnit.DAYS.toSeconds(30));
    	for(String data : datas){
    		LotteryBarrage barrage = new LotteryBarrage();
    		barrage.mergeFrom(data);
    		barrages.offer(barrage);
    	}
    	this.barrageQueue = barrages;
    	
    }
    
    
    
    /**
     * 定时更新
     */
    public void doUpdateLotteryRecourse(){
    	if(this.lotteryRecourseMap.size() <= 0){
    		return;
    	}
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	long curTime = HawkTime.getMillisecond();
    	Map<String,LotteryRecourse> updates = new HashMap<>();
    	for(LotteryRecourse lotteryRecourse : this.lotteryRecourseMap.values()){
    		//已经完成的
    		if(lotteryRecourse.getFinishTime() > 0){
    			continue;
    		}
    		//没有过期的
    		if(curTime < (lotteryRecourse.getApplyTime() + config.getAssistOutTime() * 1000)){
    			continue;
        	}
    		//过期的收集
    		updates.put(lotteryRecourse.getId(), lotteryRecourse);
    	}
    	//检查保存
    	this.saveLotteryRecourse(updates);
    }
    
    
    /**
     * 更新
     * @param curTime
     */
    public void doUpdateAssistCountMap(long curTime){
    	HawkTaskManager.getInstance().postTask(new HawkTask() {
			@Override
			public Object run() {
				Map<String,Integer> assistCountMapTemp = new ConcurrentHashMap<>();
				for(LotteryRecourse re : lotteryRecourseMap.values()){
		    		if(!HawkTime.isSameDay(curTime, re.getApplyTime())){
		    			continue;
		    		}
		    		int count = assistCountMapTemp.getOrDefault(re.getAssistId(),0);
		    		count += re.getTicketCount();
		    		assistCountMapTemp.put(re.getAssistId(), count);
				}
				assistCountMap = assistCountMapTemp;
				return null;
			}
		}, 1);
    	
    }
    
    
    /**
     * 检查保存数据
     * @param curTime
     */
    public void saveLotteryRecourse(Map<String,LotteryRecourse> updates){
    	if(updates.size() <= 0){
    		return;
    	}
    	Map<Integer,List<LotteryRecourse>> threadMap = new HashMap<>();
    	for(LotteryRecourse lotteryRecourse : updates.values()){
    		String assistId = lotteryRecourse.getAssistId();
    		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, assistId);
    		int threadId = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
    		List<LotteryRecourse> list = threadMap.get(threadId);
    		if(list == null){
    			list = new ArrayList<>();
    			threadMap.put(threadId, list);
    		}
    		list.add(lotteryRecourse);
    	}
    	//分发到目的线程去执行
    	int termId = this.getActivityTermId();
    	for(Map.Entry<Integer,List<LotteryRecourse>> entry : threadMap.entrySet()){
    		int threadIdx = entry.getKey();
    		List<LotteryRecourse> lotteryRecourseList = entry.getValue();
    		HawkTaskManager.getInstance().postTask(new HawkTask() {
    			@Override
    			public Object run() {
    				LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    				Table<String, String, String> dataTable = HashBasedTable.create();
    				List<LotteryRecourse> backList = new ArrayList<>();
    				long curTime = HawkTime.getMillisecond();
    				for(LotteryRecourse lotteryRecourse : lotteryRecourseList){
    					//已经完成了
    					if(lotteryRecourse.getFinishTime() <= 0){
    						lotteryRecourse.setFinishTime(curTime);
    						backList.add(lotteryRecourse);
    					}
    					String serverId = getDataGeter().getPlayerServerId(lotteryRecourse.getSourceId());
    					dataTable.put(serverId, lotteryRecourse.getId(),lotteryRecourse.serializ());
    				}
    				//保存
    				for(String serverId :dataTable.rowKeySet()){
    					Map<String,String> dataMap = dataTable.row(serverId);
    					String dataKey = getLotteryTicketRecourseKey(termId, serverId);
        		    	ActivityGlobalRedis.getInstance().hmset(dataKey, dataMap, (int)TimeUnit.DAYS.toSeconds(30));
    				}
    				//回退道具
    				for(LotteryRecourse lotteryRecourse : backList){
    					//发邮件归还
    					int sendCount = lotteryRecourse.getTicketCount() - lotteryRecourse.getLotteryCount();
    					if(sendCount <= 0){
    						continue;
    					}
    					//发道具
    					List<RewardItem.Builder> list = config.getLotteryCostItems();
    					list.forEach(re->re.setItemCount(re.getItemCount() * sendCount));
//    		        	ActivityReward reward = new ActivityReward(list, Action.LOTTERY_TICKET_ASSIST_APPLY_TIME_OUT_BACK);
//    					reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
//    					reward.setAlert(false);
//    					postReward(lotteryRecourse.getSourceId(), reward, false);
    			    	//发邮件
    					Object[] content =  new Object[]{sendCount};
    				    getDataGeter().sendMail(lotteryRecourse.getSourceId(), MailId.LOTTERY_TICKET_APPY_BACK_TIME_OUT, null, null, content,
    				    		list, false);
    					//日志
    					logLotteryTicketAssistBack(termId, lotteryRecourse.getId(), lotteryRecourse.getSourceId(),
    							lotteryRecourse.getAssistId(), lotteryRecourse.getTicketCount(), lotteryRecourse.getLotteryCount(), 1);
    				}
    				return null;
    			}
    		}, threadIdx);
    	}
    }
    
    
    
    
    /**
     * 随机奖励
     * @param lotteryCount
     * @return
     */
    public LotteryRlt getLotteryTicketRlt(int lotteryCount){
    	long curTime = HawkTime.getMillisecond();
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	Map<Integer, Integer> weightMap = null;
    	if(lotteryCount <= config.getRandomWeightLimit()){
    		weightMap = LotteryTicketRewardCfg.getWeightMap1(1);
    	}else{
    		weightMap = LotteryTicketRewardCfg.getWeightMap2(1);
    	}
    	boolean loss = true;
    	
    	LotteryRlt rlt = new LotteryRlt();
    	rlt.setId(HawkUUIDGenerator.genUUID());
    	rlt.setLotteryTime(curTime);
    	for(int i=0; i<config.getLotteryPosCount();i++){
    		int rewardId = HawkRand.randomWeightObject(weightMap);
        	LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, rewardId);
        	if(rewardCfg.getMultiplyValue() > 0){
    			loss = false;
    			HawkTuple3<Integer, Integer, Integer> tupe = HawkTuples.tuple(rewardCfg.getId(), rewardCfg.getShowValue(),0);
        		rlt.getRewards().add(tupe);
    		}else{
    			//最多循环100次，如果找不到 就是表有问题
    			for(int r= 0; r<100;r++){
    	    		int showNumRandom = HawkRand.randInt(config.getRandomFrom(), config.getRandomTo());
    	    		if(!LotteryTicketRewardCfg.getShowValSet().contains(showNumRandom)){
    	    			HawkTuple3<Integer, Integer, Integer> tupe = HawkTuples.tuple(rewardCfg.getId(), showNumRandom, 0);
                		rlt.getRewards().add(tupe);
                		break;
    	    		}
    	    	}
    		}
    	}
    	//都没中添加一个必中的奖励
    	if(loss){
    		if(lotteryCount <= config.getRandomWeightLimit()){
        		weightMap = LotteryTicketRewardCfg.getWeightMap1(2);
        	}else{
        		weightMap = LotteryTicketRewardCfg.getWeightMap2(2);
        	}
    		int rewardIdExt = HawkRand.randomWeightObject(weightMap);
    		LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, rewardIdExt);
    		HawkTuple3<Integer, Integer, Integer> tupe = HawkTuples.tuple(rewardCfg.getId(), rewardCfg.getShowValue(), 0);
    		int randomIndex = HawkRand.randInt(0, config.getLotteryPosCount()-1);
    		rlt.getRewards().set(randomIndex, tupe);
    	}
    	return rlt;
    	
    }
   
    
    /**
     * 获取玩家抽奖记录
     * @param playerId
     * @return
     */
    public Map<String,LotteryRlt> getPlayerLotteryRecordData(String playerId){
    	int termId = this.getActivityTermId();
    	Map<String,LotteryRlt> map = new HashMap<>();
    	String dataKey = this.getLotteryTicketRecourseRecordKey(termId, playerId);
		Map<String,String> rlt = ActivityGlobalRedis.getInstance().getRedisSession().hGetAll(dataKey);
		for(String data : rlt.values()){
			LotteryRlt lotteryRlt = new LotteryRlt();
			lotteryRlt.mergeFrom(data);
			map.put(lotteryRlt.getId(), lotteryRlt);
		}
		return map;
    }
    
    
    /**
     * 记录玩家抽奖记录
     * @param playerId
     * @param lotteryRlt
     */
    public void savePlayerLotteryRlt(String playerId,LotteryRlt lotteryRlt){
    	int termId = this.getActivityTermId();
    	String dataKey = this.getLotteryTicketRecourseRecordKey(termId, playerId);
    	ActivityGlobalRedis.getInstance().getRedisSession().hSet(dataKey, lotteryRlt.getId(), 
    			lotteryRlt.serializ(),(int)TimeUnit.DAYS.toSeconds(30));
    	
    }
    
    /**
     * 增加玩家抽奖次数
     * @param playerId
     * @return
     */
    public long addPlayerLotteryCount(String playerId){
    	int termId = this.getActivityTermId();
    	String dataKey = ActivityRedisKey.LOTTERY_TICKET_USE_COUNT+":"+ playerId + termId;
    	return ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(dataKey, 1, (int)TimeUnit.DAYS.toSeconds(30));
    }
   
    
    
    /**
     * 结束时候处理未完成的代刮申请
     */
    public void sendBackAssistTicket(){
    	if(this.lotteryRecourseMap.isEmpty()){
    		this.loadActivityData();
    	}
    	int termId = this.getActivityTermId();
    	Map<Integer,List<LotteryRecourse>> threadMap = new HashMap<>();
    	for(LotteryRecourse lotteryRecourse : this.lotteryRecourseMap.values()){
    		String assistId = lotteryRecourse.getAssistId();
    		HawkXID xid = HawkXID.valueOf(ObjType.PLAYER, assistId);
    		int threadId = xid.getHashThread(HawkTaskManager.getInstance().getThreadNum());
    		List<LotteryRecourse> list = threadMap.get(threadId);
    		if(list == null){
    			list = new ArrayList<>();
    			threadMap.put(threadId, list);
    		}
    		list.add(lotteryRecourse);
    	}
    	//分发到目的线程去执行
    	for(Map.Entry<Integer,List<LotteryRecourse>> entry : threadMap.entrySet()){
    		int threadIdx = entry.getKey();
    		List<LotteryRecourse> lotteryRecourseList = entry.getValue();
    		HawkTaskManager.getInstance().postTask(new HawkTask() {
    			@Override
    			public Object run() {
    				Table<String, String, String> dataTable = HashBasedTable.create();
    				List<LotteryRecourse> backList = new ArrayList<>();
    				long curTime = HawkTime.getMillisecond();
    				LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    				for(LotteryRecourse lotteryRecourse : lotteryRecourseList){
    					if(lotteryRecourse.getFinishTime() > 0){
    						continue;
    					}
    					lotteryRecourse.setFinishTime(curTime);
    					String serverId = getDataGeter().getPlayerServerId(lotteryRecourse.getSourceId());
    					dataTable.put(serverId, lotteryRecourse.getId(),lotteryRecourse.serializ());
    					backList.add(lotteryRecourse);
    				}
    				
    				for(String serverId :dataTable.rowKeySet()){
    					Map<String,String> dataMap = dataTable.row(serverId);
    					String dataKey = getLotteryTicketRecourseKey(termId, serverId);
        		    	ActivityGlobalRedis.getInstance().hmset(dataKey, dataMap,  (int)TimeUnit.DAYS.toSeconds(30));
    				}
    				
    				for(LotteryRecourse lotteryRecourse : backList){
    					//发邮件归还
    					int sendCount = lotteryRecourse.getTicketCount() - lotteryRecourse.getLotteryCount();
    					if(sendCount <= 0){
    						continue;
    					}
    					//发道具
    					List<RewardItem.Builder> list = config.getLotteryCostItems();
    					list.forEach(re->re.setItemCount(re.getItemCount() * sendCount));
//    		        	ActivityReward reward = new ActivityReward(list, Action.LOTTERY_TICKET_ASSIST_APPLY_TIME_OUT_BACK);
//    					reward.setOrginType(RewardOrginType.ACTIVITY_REWARD, getActivityId());
//    					reward.setAlert(false);
//    					postReward(lotteryRecourse.getSourceId(), reward, false);
    			    	//发邮件
    					Object[] content =  new Object[]{sendCount};
    				    getDataGeter().sendMail(lotteryRecourse.getSourceId(), MailId.LOTTERY_TICKET_APPY_BACK_TIME_OUT, null, null, content,
    				    		list, false);
    					
    					logLotteryTicketAssistBack(termId, lotteryRecourse.getId(), lotteryRecourse.getSourceId(), lotteryRecourse.getAssistId(),
    							lotteryRecourse.getTicketCount(), lotteryRecourse.getLotteryCount(), 1);
    				}
    				return null;
    			}
    		}, threadIdx);
    	}
    }
    
    
    public void removeActivityItem(String playerId){
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	//活动结束时回收道具ID
		RewardItem.Builder itemBuilder = RewardHelper.toRewardItem(config.getRecoverItem());
		if(Objects.isNull(itemBuilder)){
			return;
		}
		//取玩家身上此道具的数量
		int count = this.getDataGeter().getItemNum(playerId, itemBuilder.getItemId());
		if(count <= 0){
			return;
		}
		//扣除道具的数据准备
		List<RewardItem.Builder> costList = new ArrayList<>();
		RewardItem.Builder costBuilder = RewardItem.newBuilder();
		//类型为道具
		costBuilder.setItemType(ItemType.TOOL_VALUE);
		//待扣除物品ID
		costBuilder.setItemId(itemBuilder.getItemId());
		//待扣除的物品数量
		costBuilder.setItemCount(count);
		//把待扣除的物品数据加入参数容器
		costList.add(costBuilder);
		//注意这里先扣除源道具，如果失败，不给兑换后的道具
		boolean cost = this.getDataGeter().cost(playerId,costList, 1, Action.LOTTERY_TICKET_RECOVER_COST, true);
		//扣除失败不继续处理
		if (!cost) {
			return;
		}
		List<RewardItem.Builder> gainList =RewardHelper.toRewardItemImmutableList(config.getRecoverItemGain());
		if(!gainList.isEmpty()){
			for(RewardItem.Builder rbuilder : gainList){
				rbuilder.setItemCount(rbuilder.getItemCount() * count);
			}
			String playerName = this.getDataGeter().getPlayerName(playerId);
			Object[] content =  new Object[]{playerName,count};
		    this.getDataGeter().sendMail(playerId, MailId.LOTTERY_TICKET_ITEM_RECOVER_ACHIEVE, null, null, content,
		    		gainList, false);
		}
		//日志
		int termId = this.getActivityTermId();
		this.logLotteryTicketItemRecover(termId, playerId, itemBuilder.getItemId(),count);
		HawkLog.logPrintln("LotteryTicketActivity,recoverItem,playerId{},itemId:{},count:{}", playerId,itemBuilder.getItemId(),count);
    }
    
    
    /**
     * 是否在代刮时间内
     * @param curTime
     * @return
     */
    public boolean inAssistLotteryTime(long curTime){
    	int termId = this.getActivityTermId();
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	LotteryTicketTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketTimeCfg.class, termId);
    	if(curTime > timeCfg.getStartTimeValue() && 
    			curTime < (timeCfg.getEndTimeValue() - config.getAssistTimeLimit() * 1000)){
    		return true;
    	}
    	return false;
    }
    
    
    
    
    public void addBroadcastMsg(String sourceId,String assistId,int reward){
    	LotteryTicketKVCfg config = HawkConfigManager.getInstance().getKVInstance(LotteryTicketKVCfg.class);
    	LotteryTicketRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(LotteryTicketRewardCfg.class, reward);
    	if(rewardCfg.getBroadcast() <= 0){
    		return;
    	}
    	int assist = 0;
    	if(!HawkOSOperator.isEmptyString(assistId)){
    		assist = 1;
    	}
    	LotteryBarrage lotteryBarrage = null;
    	if(assist == 0){
    		//跑马灯
    		lotteryBarrage = new LotteryBarrage(sourceId, reward, assist);
        	String playerName = this.getDataGeter().getPlayerName(sourceId);
    		this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.LOTTERY_TICKET_REWARD,null, 
    				playerName,reward);
    	}else{
    		//跑马灯
    		lotteryBarrage = new LotteryBarrage(assistId, reward, assist);
        	String assistName = this.getDataGeter().getPlayerName(assistId);
    		this.addWorldBroadcastMsg(ChatType.SYS_BROADCAST, NoticeCfgId.LOTTERY_TICKET_REWARD_ASSIST,null, 
    				assistName,reward);
    	}
    	this.barrageQueue.offer(lotteryBarrage);
    	int pollCount = this.barrageQueue.size() - config.getBarrageSize();
    	if(pollCount > 0){
    		for(int i=0;i<pollCount;i++){
    			this.barrageQueue.poll();
    		}
    	}
    }
    
    public void saveBarrage(){
    	if(this.barrageQueue.size() <=0 ){
    		return;
    	}
    	String serverId = this.getDataGeter().getServerId();
    	int termId = this.getActivityTermId();
    	String key = this.getBarrageKey(serverId, termId);
    	List<LotteryBarrage> blist =new ArrayList<>();
    	blist.addAll(this.barrageQueue);
    	List<String> data = new ArrayList<>();
    	for(LotteryBarrage barrage :blist){
    		data.add(barrage.serializ());
    	}
    	ActivityGlobalRedis.getInstance().getRedisSession().del(key);
    	ActivityGlobalRedis.getInstance().getRedisSession().lPush(key,(int)TimeUnit.DAYS.toSeconds(30), data.toArray(new String[data.size()]));
    }
    
 
    
    public void addLotteryAssistApplyBuilder(String playerId,PBLotteryTicketPageResp.Builder builder){
        int assistCount = 0;
    	for(LotteryRecourse re : this.lotteryRecourseMap.values()){
    		if(re.getSourceId().equals(playerId)){
    			builder.addAssistApply(re.genAssistApplyBuilder());
    			assistCount += re.getTicketCount();
    		}
    		if(re.getAssistId().equals(playerId)){
    			builder.addAssistAchieve(re.genAssistApplyBuilder());
    		}
    	}
    	builder.setAssistApplyCnt(assistCount);
    }
    
    
    public boolean inApplay(String playerId){
    	for(LotteryRecourse res : this.lotteryRecourseMap.values()){
    		if(res.getFinishTime() > 0){
    			continue;
    		}
    		if(!res.getSourceId().equals(playerId)&&
    				!res.getAssistId().equals(playerId)){
    			continue;
    		}
    		return true;
    	}
    	return false;
    }
    
    
    
    private String getLotteryTicketRecourseKey(int termId,String serverId){
    	String dataKey = ActivityRedisKey.LOTTERY_TICKET_RECOURSE+":"+serverId+":"+termId;
    	return dataKey;
    }
    
    private String getLotteryTicketRecourseRecordKey(int termId,String playerId){
    	String dataKey = ActivityRedisKey.LOTTERY_TICKET_RECOURSE_RECORD+":"+playerId +":"+termId;
    	return dataKey;
    }
    
    
    private String getPlayerLotteryAssistKey(int termId,String playerId){
    	String dataKey = ActivityRedisKey.LOTTERY_TICKET_ASSIST_APPLY+":"+playerId +":"+termId;
    	return dataKey;
    }
    
    private String getBarrageKey(String serverId,int termId){
    	String dataKey = ActivityRedisKey.LOTTERY_TICKET_BARRAGE+":"+serverId +":"+termId;
    	return dataKey;
    }
    
    /**
     * 记录刮卡
     * @param termId
     * @param playerId
     * @param assist
     */
    private void logLotteryTicketUse(int termId,String playerId,int assist,String serId,int rewardId,int mult){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("assist", assist); //0自刮   1代刮
        param.put("serId", serId); //自刮为0  代刮为
        param.put("rewardId", rewardId); //中奖ID
        param.put("mult", mult); //倍数
        getDataGeter().logActivityCommon(playerId, LogInfoType.lottery_ticket_use, param);
    }
    
    /**
     * 记录邀请发送
     * @param termId
     * @param playerId
     * @param serId
     * @param assistId
     * @param ticketCount
     */
    private void logLotteryTicketAssistSend(int termId,String playerId,String serId,String assistId,int ticketCount){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("serId", serId); //邀请单ID
        param.put("assister", assistId); //受邀玩家ID
        param.put("ticketCount", ticketCount); //彩票个数
        getDataGeter().logActivityCommon(playerId, LogInfoType.lottery_ticket_assist_send, param);
    }
    
    /**
     * 邀请回退
     * @param termId
     * @param serId
     * @param playerId
     * @param assistId
     * @param ticketCount
     * @param useCount
     * @param backReason
     */
    private void logLotteryTicketAssistBack(int termId,String serId,String playerId,String assistId,int ticketCount,int useCount,int backReason){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("serId", serId); //邀请ID
        param.put("assister", assistId); //受邀玩家ID
        param.put("ticketCount", ticketCount); //发送个数
        param.put("useCount", useCount); //使用个数
        param.put("backReason", backReason); //1系统自动回退  2拒绝后回退
        getDataGeter().logActivityCommon(playerId, LogInfoType.lottery_ticket_assist_back, param);
    }
    
    /**
     * 道具回收
     * @param termId
     * @param playerId
     * @param itemId
     * @param itemCount
     */
    private void logLotteryTicketItemRecover(int termId,String playerId,int itemId,int itemCount){
    	Map<String, Object> param = new HashMap<>();
    	param.put("termId", termId); //期数
        param.put("itemId", itemId); //道具ID
        param.put("itemCount", itemCount); //道具数量
        getDataGeter().logActivityCommon(playerId, LogInfoType.lottery_ticket_item_recover, param);
    }
}
