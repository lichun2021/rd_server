package com.hawk.activity.type.impl.backFlow.powerSend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.math.NumberUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;
import org.hawk.redis.HawkRedisSession;
import org.hawk.uuid.HawkUUIDGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.SendFriendsGiftsEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.redis.ActivityRedisKey;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.backFlow.comm.BackFlowPlayer;
import com.hawk.activity.type.impl.backFlow.powerSend.cfg.PowerSendActivityTimeCfg;
import com.hawk.activity.type.impl.backFlow.powerSend.cfg.PowerSendAwardCfg;
import com.hawk.activity.type.impl.backFlow.powerSend.cfg.PowerSendDateCfg;
import com.hawk.activity.type.impl.backFlow.powerSend.cfg.PowerSendKVCfg;
import com.hawk.activity.type.impl.backFlow.powerSend.entity.PowerSendEntity;
import com.hawk.game.protocol.Activity.PowerMessageState;
import com.hawk.game.protocol.Activity.PowerSendActivityInfo;
import com.hawk.game.protocol.Activity.PowerSendMessage;
import com.hawk.game.protocol.Activity.PowerSendMessageInfoResp;
import com.hawk.game.protocol.Const.NoticeCfgId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;

/***
 * 体力赠送
 * @author che
 *
 */
public class PowerSendActivity extends ActivityBase {

	private static final Logger logger = LoggerFactory.getLogger("Server");

	
	public PowerSendActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public void onPlayerLogin(String playerId) {
		
		Optional<PowerSendEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PowerSendEntity entity = optional.get();
		BackFlowPlayer backFlowPlayer = this.getDataGeter().getBackFlowPlayer(playerId);
		if(backFlowPlayer == null){
			return;
		}
		if(this.checkFitLostParams(backFlowPlayer,entity)){
			int backTimes = backFlowPlayer.getBackCount();
			PowerSendDateCfg dataCfg = this.getDateConfig(backFlowPlayer);
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
			entity.setSendCount(0);
			entity.notifyUpdate();
			SendPowerInfo info= new SendPowerInfo(playerId, backType, startTime, overTime);
			this.savePlayerActivityInfo(info);
			logger.info("onPlayerLogin  checkFitLostParams init sucess,  playerId: "+ 
					"{},backCount:{},backType:{},backTime:{},startTime:{}.overTime:{}", 
					playerId,backTimes,backType,backFlowPlayer.getBackTimeStamp(),startTime,overTime);
		}
	}
	
	
	
	public PowerSendDateCfg getDateConfig(BackFlowPlayer backFlowPlayer){
		List<PowerSendDateCfg> confgs = HawkConfigManager.getInstance().
				getConfigIterator(PowerSendDateCfg.class).toList();
		for(PowerSendDateCfg cfg : confgs){
			if(cfg.isAdapt(backFlowPlayer)){
				return cfg;
			}
		}
		return null;
	}
	/**
	 * 是否可以触发
	 * @return
	 */
	public boolean canTrigger(long backTime){
		int termId = this.getActivityTermId();
		PowerSendActivityTimeCfg cfg = HawkConfigManager.getInstance().
				getConfigByKey(PowerSendActivityTimeCfg.class, termId);
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
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		if (!event.isCrossDay()) {
			return;
		}
		String playerId = event.getPlayerId();
		if(this.isHidden(playerId)){
			return;
		}
		Optional<PowerSendEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return;
		}
		PowerSendEntity entity = optional.get();
		entity.setSendCount(0);
		entity.notifyUpdate();
	}


	
	/**
	 * 检查参数是否可以进行活动
	 * @param backFlowPlayer
	 * @param entity
	 * @return
	 */
	public boolean checkFitLostParams(BackFlowPlayer backFlowPlayer,PowerSendEntity entity) {
		if(backFlowPlayer.getBackCount() <= entity.getBackCount()){
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
	 * 发送 赠送体力消息，改成监听事件
	 * @param sender
	 * @param receivers
	 */
	@Subscribe
	public void sendPowerMessage(SendFriendsGiftsEvent event){
		if(!event.isInGameFriend()){
			logger.info("sendPowerMessage, SendFriendsGiftsEvent,not isInGameFriend ,playerId: {}",event.getPlayerId());
			return;
		}
		String senderId = event.getPlayerId();
		Optional<PowerSendEntity> optional = getPlayerDataEntity(senderId);
		if (!optional.isPresent()){
			return;
		}
		PowerSendEntity entity = optional.get();
		if(isHidden(senderId)){
			logger.info("sendPowerMessage, SendFriendsGiftsEvent,hidden ,playerId: {},startTime:{},overTime:{},",
					event.getPlayerId(),entity.getStartTime(),entity.getOverTime());
			return;
		}
		Set<String> receivers = event.getFriends();
		if(event.getFriends() == null){
			logger.info("sendPowerMessage, getFriends null ,playerId: {}",
					event.getPlayerId());
			return;
		}
		if(event.getFriends().size() <= 0){
			logger.info("sendPowerMessage, getFriends 0 ,playerId: {}",
					event.getPlayerId());
			return;
		}
		
		List<String> friends = new ArrayList<String>();
		friends.addAll(receivers);
		Collections.sort(friends, new Comparator<String>() {
			@Override
			public int compare(String arg0, String arg1) {
				long lastLoginTime0 = getDataGeter().getAccountLoginTime(arg0);
				long lastLoginTime1 = getDataGeter().getAccountLoginTime(arg1);
				if(lastLoginTime0 < lastLoginTime1){
					return 1;
				}
				return -1;
			}
		});
		long sendCount = entity.getSendCount();
		long sendLimitCount = this.getSendMessageLimit(entity);
		if(sendCount >= sendLimitCount){
			logger.info("sendPowerMessage, sendCount >  sendLimitCount ,playerId: {},sendCount: {},sendLimitCount: {}",
					event.getPlayerId(),sendCount,sendLimitCount);
			return;
		}
		long sendNeed = sendLimitCount - sendCount;
		if(friends.size() < sendNeed){
			sendNeed = friends.size();
		}
		Set<String> sendList = new HashSet<String>();
		for(int i=0;i<sendNeed;i++){
			String receiverId = friends.get(i);
			sendList.add(receiverId);
		}
		entity.addSendCount(sendList.size());
		Map<String,SendPowerInfo> bpMap = this.getSendPowerInfoMap(sendList);
		List<PowerMessage> messags = new ArrayList<PowerMessage>();
		for(String receiverId : sendList){
			SendPowerInfo info = bpMap.get(receiverId);
			String openAwards = getMessageRewards(info);
			String backAwards = getMessageBackRewards(entity);
			//发送老朋友的问候
			String messageId = HawkUUIDGenerator.genUUID();
			PowerMessage message = new PowerMessage(messageId, senderId, receiverId,openAwards,backAwards,entity.getOverTime());
			messags.add(message);
			List<String> receiverIds = new ArrayList<>();
			receiverIds.add(receiverId);
			getDataGeter().sendChatRoomMessage(senderId, receiverIds, NoticeCfgId.BACK_PLAYER_ASK, messageId,entity.getOverTime());
			logger.info("sendPowerMessage, sendder: {},receiver:{},openAwards:{},backAwards:{}", entity.getPlayerId(),receiverId,openAwards,backAwards);
		}
		if(messags.size() > 0){
			saveSendRecord(messags);
		}
		int termId = this.getActivityTermId();
		this.getDataGeter().logPowerSendMessageCount(senderId, termId, entity.getBackCount(), messags.size(), entity.getSendCount());
	}
	
	
	
	
	
	
	
	
	
	
	
	/**
	 * 催促回信
	 * @param messageId
	 * @param playerId
	 */
	public void pressedSendPowerMessageBack(String messageId,String playerId){
		long pressedCount = this.getBackPressMessageCount(messageId);
		if(pressedCount > 0){
			sendErrorAndBreak(playerId, HP.code.POWER_SEND_MESSAGE_PRESSED_BACK_REQ_VALUE,
					Status.Error.POWER_MESSAGE_PRESSED_VALUE);
			return;
		}
		PowerMessage message = this.getPowerMessage(messageId);
		if(message == null){
			return;
		}
		if(message.isOutTime()){
			sendErrorAndBreak(playerId, HP.code.POWER_SEND_MESSAGE_PRESSED_BACK_REQ_VALUE,
					Status.Error.POWER_MESSAGE_OUT_TIME_VALUE);
			return;
		}
		if(message.getState() == PowerMessageState.MESSAGE_BACK_VALUE){
			logger.info("readSendPowerMessage fail state != 0, playerId: {},messageId:{},state:{}",
					playerId,messageId,message.getState());
			return;
		}
		if(!message.getSender().equals(playerId)){
			logger.info("readSendPowerMessage fail sender err, playerId: {},messageId: {},sender:{}",
					playerId,messageId,message.getSender());
			return;
		}
		List<String> receiverIds = new ArrayList<>();
		receiverIds.add(message.getReviever());
		getDataGeter().sendChatRoomMessage(playerId, receiverIds, NoticeCfgId.BACK_PLAYER_GIFT_PRESSED_BACK, message.getMessageId());
		this.dayAddBackPressMessageCount(messageId, 1);
		PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.POWER_SEND_MESSAGE_PRESSED_BACK_REQ_VALUE);
		
	}
	
	/**
	 * 领取信件奖励
	 * @param playerId
	 * @param messageId
	 */
	public void achievePowerMessageRewards(PowerSendEntity entity,PowerMessage message){
		if(message.getAchieveRewardsTime() >0){
			logger.info("openSendPowerMessage fail state != 0, playerId: {},messageId:{},state:{}",
					entity.getPlayerId(),message.getMessageId(),message.getState());
			return;
		}
		SendPowerInfo info = new SendPowerInfo(entity.getPlayerId(), 
				entity.getBackType(), entity.getStartTime(), entity.getOverTime());
		long curTime = HawkTime.getMillisecond();
		long openLimitCount = this.getOpenMessageLimit(info);
		long openCount = this.getRecieveMessageCount(info.getPlayerId());
		if(openCount >= openLimitCount){
			logger.info("achievePowerMessageRewards openCount > openLimitCount, playerId: {},messageId:{},openCount:{},openLimitCount:{}",
					info.getPlayerId(),message.getMessageId(),openCount,openLimitCount);
			return;
		}
		List<RewardItem.Builder>  alist = RewardHelper.toRewardItemList(message.getOpenRewards());
		this.getDataGeter().takeReward(info.getPlayerId(),alist, Action.POWER_SEND_ACHIEVE_REWARD, true);
		message.setAchieveRewardsTime(curTime);
		this.dayAddRecieveMessageCount(info.getPlayerId(), 1);
		logger.info("openSendPowerMessage sucess, playerId: {},messageId:{},state:{}",
				info.getPlayerId(),message.getMessageId(),message.getState());
	}
	
	
	/**
	 * 体力回赠
	 * @param sender
	 * @param receiver
	 */
	public void sendPowerBackAndAchieve(String recieverId,String messageId){
		Optional<PowerSendEntity> optional = getPlayerDataEntity(recieverId);
		if (!optional.isPresent()){
			logger.info("PowerSendEntity entity null, playerId: {},messageId:{}",
					recieverId,messageId);
			return;
		}
		PowerSendEntity entity = optional.get();
		PowerMessage message = this.getPowerMessage(messageId);
		if(message == null){
			logger.info("sendPowerBackAndAchieve message null, playerId: {},messageId:{}",
					recieverId,messageId);
			return;
		}
		if(message.isOutTime()){
			sendErrorAndBreak(recieverId, HP.code.POWER_SEND_MESSAGE_BACK_REQ_VALUE,
					Status.Error.POWER_MESSAGE_OUT_TIME_VALUE);
			return;
		}
		if(message.getState() != PowerMessageState.MESSAGE_CLOSE_VALUE){
			logger.info("sendPowerBackAndAchieve state open, playerId: {},messageId:{}",
					recieverId,messageId);
			return;
		}
		
		this.achievePowerMessageRewards(entity, message);
		this.backMessage(recieverId, message);
		List<PowerMessage> messages =new ArrayList<PowerMessage>();
		messages.add(message);
		this.saveSendRecord(messages);
		PlayerPushHelper.getInstance().responseSuccess(recieverId, HP.code.POWER_SEND_MESSAGE_BACK_REQ_VALUE);
		
	}
	
	
	public void backMessage(String recieverId,PowerMessage message){
		if(message.getBackTime() >0){
			logger.info("getBackTime fail state != 0, playerId: {},messageId:{},state:{}",
					recieverId,message.getMessageId(),message.getState());
			return;
		}
		
		long curTime = HawkTime.getMillisecond();
		String senderId = message.getSender();
		String recieverName = this.getDataGeter().getPlayerName(recieverId);
		String sendName = this.getDataGeter().getPlayerName(senderId);
		SendPowerInfo info = this.getSendPowerInfo(senderId);
		long backLimitCount = this.getReceiveBackLimit(info);
		long backCount = this.getBackRecieveMessageCount(senderId);
		logger.info("backMessage, playerId: {},senderId：{},messageId:{},backLimitCount:{},backCount:{}",
				recieverId,senderId,message.getMessageId(),backLimitCount,backCount);
		if(backCount < backLimitCount){
			//发邮件
			List<RewardItem.Builder>  alist = RewardHelper.toRewardItemList(message.getBackRewards());
			Object[] content =  new Object[]{sendName,recieverName};
			this.getDataGeter().sendMail(senderId, MailId.POWER_SEND_BACK_GFIT, null, null, content,
					alist, false);
			this.dayAddBackRecieveMessageCount(senderId, 1);
			logger.info("achievePowerMessageRewards email send, playerId: {},messageId:{},backCount:{},backLimitCount:{}",
					info.getPlayerId(),message.getMessageId(),backCount,backLimitCount);
		}
		message.setBackTime(curTime);
		message.setState(PowerMessageState.MESSAGE_BACK_VALUE);
		List<PowerMessage> messages = new ArrayList<>();
		messages.add(message);
		this.saveSendRecord(messages);
		//添加私人聊天
		String senderName = this.getDataGeter().getPlayerName(message.getSender());
		List<String> receiverIds = new ArrayList<>();
		receiverIds.add(message.getSender());
		getDataGeter().sendChatRoomMessage(message.getReviever(), receiverIds, NoticeCfgId.BACK_PLAYER_GIFT_RECEIVE, message.getMessageId(),senderName);
		logger.info("sendPowerBack sucess,playerId: {},messageId:{},state:{}",
				recieverId,message.getMessageId(),message.getState());
	}
	
	public void getMessageInfo(String messageId,String playerId){
		PowerMessage message = this.getPowerMessage(messageId);
		if(message == null){
			logger.info("getMessageInfo  null, playerId: {},messageId:{}",
					playerId,messageId);
			return;
		}
		if(message.isOutTime()){
			sendErrorAndBreak(playerId, HP.code.POWER_SEND_MESSAGE_INFO_REQ_VALUE,
					Status.Error.POWER_MESSAGE_OUT_TIME_VALUE);
			return;
		}
		if(message.getSender().equals(playerId) || message.getReviever().equals(playerId)){
			Optional<PowerSendEntity> optional = getPlayerDataEntity(playerId);
			if (!optional.isPresent()){
				return;
			}
			PowerSendEntity entity = optional.get();
			SendPowerInfo info = new SendPowerInfo(entity.getPlayerId(), 
					entity.getBackType(), entity.getStartTime(), entity.getOverTime());
			this.syncMessageInfo(message,info);
		}
	}
	
	public void syncMessageInfo(PowerMessage message,SendPowerInfo info){
		PowerSendMessage.Builder builder = PowerSendMessage.newBuilder();
		builder.setMessageId(message.getMessageId());
		List<RewardItem.Builder>  alist = RewardHelper.toRewardItemList(message.getOpenRewards());
		for(RewardItem.Builder item : alist){
			builder.addGifts(item);
		}
		List<RewardItem.Builder>  blist = RewardHelper.toRewardItemList(message.getBackRewards());
		for(RewardItem.Builder item : blist){
			builder.addBackGifts(item);
		}
		String senderName = this.getDataGeter().getPlayerName(message.getSender());
		builder.setSenderId(message.getSender());
		builder.setSenderName(senderName);
		String recieveName = this.getDataGeter().getPlayerName(message.getReviever());
		builder.setRecieverId(message.getReviever());
		builder.setRecieverName(recieveName);
		builder.setOutTime(message.getOutTime());
		builder.setState(PowerMessageState.valueOf(message.getState()));
		
		PowerSendActivityInfo.Builder abuilder = PowerSendActivityInfo.newBuilder();
		abuilder.setCanAchieveCount(this.getOpenMessageLimit(info));
		abuilder.setAchieveCount((int)this.getRecieveMessageCount(info.getPlayerId()));
		abuilder.setCanRevieveBackCount(this.getReceiveBackLimit(info));
		abuilder.setRevieveBackCount((int)this.getBackRecieveMessageCount(info.getPlayerId()));
		
		PowerSendMessageInfoResp.Builder rbuilder = PowerSendMessageInfoResp.newBuilder();
		rbuilder.setActivtyInfo(abuilder);
		rbuilder.setMessage(builder);
		pushToPlayer(info.getPlayerId(), HP.code.POWER_SEND_MESSAGE_INFO_RESP_VALUE, rbuilder);
	}
	
	
	/**
	 * 获取发送限制次数
	 * @param bplayer
	 */
	public int getSendMessageLimit(PowerSendEntity entity){
		if(entity == null){
			return 0;
		}
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(entity.getBackType());
		if(acfg == null){
			return 0;
		}
		return acfg.getDailySendGiftLimit();
	}
	
	
	/**
	 * 获取可以收获信息次数
	 * @param bplayer
	 */
	public int getGainMessageLimit(SendPowerInfo info){
		PowerSendKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(PowerSendKVCfg.class);
		if(info == null){
			return cfg.getDailyGainMessageLimit();
		}
		if(!this.inActivity(info)){
			return cfg.getDailyGainMessageLimit();
		}
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(info.getBackType());
		if(acfg == null){
			return cfg.getDailyGainMessageLimit();
		}
		return acfg.getDailyGainMessageLimit();
	}
	
	/**
	 * 获取可以收获信息奖励次数
	 * @param bplayer
	 */
	public int getOpenMessageLimit(SendPowerInfo info){
		PowerSendKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(PowerSendKVCfg.class);
		if(info == null){
			return cfg.getDailyGainGiftLimit();
		}
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(info.getBackType());
		if(acfg == null){
			return cfg.getDailyGainGiftLimit();
		}
		return acfg.getDailyGainGiftLimit();
	}
	
	/**
	 * 获取可以收到回礼次数
	 * @return
	 */
	public int getReceiveBackLimit(SendPowerInfo info){
		if(info == null){
			return 0;
		}
		if(!this.inActivity(info)){
			return 0;
		}
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(info.getBackType());
		if(acfg == null){
			return 0;
		}
		return acfg.getDailyGainReceiptLimit();
	}
	
	/**
	 * 打开奖励
	 * @param bplayer
	 * @return
	 */
	public String getMessageRewards(SendPowerInfo info){
		PowerSendKVCfg cfg = HawkConfigManager.getInstance().
				getKVInstance(PowerSendKVCfg.class);
		if(info == null){
			return cfg.getGainGiftReward();
		}
		if(!this.inActivity(info)){
			return cfg.getGainGiftReward();
		}
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(info.getBackType());
		if(acfg == null){
			return cfg.getGainGiftReward();
		}
		return acfg.getGainGiftReward();
	}
	
	/**
	 * 回礼礼品
	 * @param bplayer
	 * @return
	 */
	public String getMessageBackRewards(PowerSendEntity entity){
		PowerSendAwardCfg acfg = this.getPowerSendAwardCfg(entity.getBackType());
		if(acfg == null){
			return "";
		}
		return acfg.getGainReceiptReward();
	}
	
	
	
	public PowerSendAwardCfg getPowerSendAwardCfg(int type){
		List<PowerSendAwardCfg> configs = HawkConfigManager.getInstance().
				getConfigIterator(PowerSendAwardCfg.class).toList();
		for(PowerSendAwardCfg cfg : configs){
			if(cfg.getPlayerType() == type){
				return cfg;
			}
		}
		return null;
	}
	
	
	

	
	/**
	 * 是否在活动中
	 * @param playerId
	 * @return
	 */
	public boolean inActivity(SendPowerInfo info){
		if(info == null){
			return false;
		}
		long curTime = HawkTime.getMillisecond();
		if(curTime > info.getOverTime()){
			return false;
		}
		if(curTime < info.getStartTime()){
			return false;
		}
		//回流时间是否达标
		PowerSendDateCfg config = HawkConfigManager.getInstance().
				getConfigByKey(PowerSendDateCfg.class, info.getBackType());
		if(config == null){
			return false;
		}
		return true;
	}
	
	
	
	
	
	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.POWER_SEND;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		PowerSendActivity activity = new PowerSendActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<PowerSendEntity> queryList = HawkDBManager.getInstance()
				.query("from PowerSendEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			PowerSendEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		PowerSendEntity entity = new PowerSendEntity(playerId, termId);
		return entity;
	}



	@Override
	public boolean isHidden(String playerId) {
		Optional<PowerSendEntity> optional = getPlayerDataEntity(playerId);
		if (!optional.isPresent()){
			return true;
		}
		long curTime = HawkTime.getMillisecond();
		PowerSendEntity entity = optional.get();
		if(curTime > entity.getOverTime() || 
				curTime < entity.getStartTime()){
			return true;
		}
		return super.isHidden(playerId);
	}
	
	/***
	 *  增加催回次数
	 * @param playerId
	 * @param count
	 * @return
	 */
	public long dayAddBackPressMessageCount(String messageId,int count){
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_MESSAGE_PRESSED + ":" + messageId + ":" + day;
		return ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(
				key, count, (int) TimeUnit.DAYS.toSeconds(1));
	}

	/**
	 * 获取催回次数
	 * @param playerId
	 * @return
	 */
	public long getBackPressMessageCount(String messageId){
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_MESSAGE_PRESSED + ":" + messageId + ":" + day;
		String cc = redisSession.getString(key);
		return NumberUtils.toInt(cc);
	}
	
	
	/**
	 * 获取发送次数
	 * @param playerId
	 * @return
	 */
	public long getBackRecieveMessageCount(String playerId){
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_BACK_RECEIVE_COUNT_KEY + ":" + playerId + ":" + day;
		String cc = redisSession.getString(key);
		return NumberUtils.toInt(cc);
	}
	

	
	
	/***
	 *  增加当天收奖次数
	 * @param playerId
	 * @param count
	 * @return
	 */
	public long dayAddRecieveMessageCount(String playerId, int count){
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_RECEIVE_COUNT_KEY + ":" + playerId + ":" + day;
		return ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(
				key, count, (int) TimeUnit.DAYS.toSeconds(1));
	}
	
	
	
	/**
	 * 获取收获次数
	 * @param playerId
	 * @return
	 */
	public long getRecieveMessageCount(String playerId){
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_RECEIVE_COUNT_KEY + ":" + playerId + ":" + day;
		String cc = redisSession.getString(key);
		return NumberUtils.toInt(cc);
	}
	
	
	

	/**
	 * 保存信件
	 * @param messages
	 */
	public void saveSendRecord(List<PowerMessage> messages){
		HawkRedisSession redisSession = ActivityGlobalRedis.getInstance().getRedisSession();
		try (Jedis jedis = redisSession.getJedis(); Pipeline pip = jedis.pipelined()) {
			for(PowerMessage message : messages){
				String key = ActivityRedisKey.SEND_POWER_MESSAGE_KEY +":"+message.getMessageId();
				pip.set(key,message.serializ());
				pip.expire(key, (int)TimeUnit.DAYS.toSeconds(30));
			}
			pip.sync();
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		
		
	}
	
	/**
	 * 获取信件记录
	 * @param messageId
	 * @return
	 */
	public PowerMessage getPowerMessage(String messageId){
		String key = ActivityRedisKey.SEND_POWER_MESSAGE_KEY +":"+messageId;
		String info =ActivityGlobalRedis.getInstance().get(key);
		if(HawkOSOperator.isEmptyString(info)){
			return null;
		}
		PowerMessage message = new PowerMessage();
		message.mergeFrom(info);
		return message;
	}
	
	/***
	 *  增加当天收获回礼次数
	 * @param playerId
	 * @param count
	 * @return
	 */
	public long dayAddBackRecieveMessageCount(String playerId, int count){
		int day = HawkTime.getYearDay();
		String key = ActivityRedisKey.SEND_POWER_BACK_RECEIVE_COUNT_KEY + ":" + playerId + ":" + day;
		return ActivityGlobalRedis.getInstance().getRedisSession().increaseBy(
				key, count, (int) TimeUnit.DAYS.toSeconds(1));
	}
	
	
	public void savePlayerActivityInfo(SendPowerInfo info){
		String key = ActivityRedisKey.SEND_POWER_ACTIVITY_INFO + ":" + info.getPlayerId();
		ActivityGlobalRedis.getInstance().getRedisSession().
		setString(key, info.serializ(), (int) TimeUnit.DAYS.toSeconds(30));
	}
	
	
	/**
	 * 获取活动基本信息
	 * @param playerId
	 * @return
	 */
	public SendPowerInfo getSendPowerInfo(String playerId){
		Set<String> playerIds = new HashSet<>();
		playerIds.add(playerId);
		Map<String,SendPowerInfo> infos = getSendPowerInfoMap(playerIds);
		return infos.get(playerId);
	}
	
	/**
	 * 获取活动基本信息
	 * @param ids
	 * @return
	 */
	public Map<String,SendPowerInfo> getSendPowerInfoMap(Set<String> playerIds){
		Map<String,SendPowerInfo> infoMap = new HashMap<String,SendPowerInfo>();
		Map<String,Response<String>> piplineRes = new HashMap<String,Response<String>>();
		try(Jedis jedis = ActivityGlobalRedis.getInstance().getRedisSession().getJedis(); 
				Pipeline pip = jedis.pipelined()){
			for( String playerId : playerIds ){
				String key = ActivityRedisKey.SEND_POWER_ACTIVITY_INFO + ":" + playerId;
				Response<String> onePiplineResp = pip.get(key);
				piplineRes.put(playerId,onePiplineResp );
			}
			pip.sync();
			if( piplineRes.size() == playerIds.size() ){
 	    		for(Entry<String,Response<String>> entry : piplineRes.entrySet()){
 	    			Response<String> value = entry.getValue();
 	    			String retStr = value.get();
 	    			if (!HawkOSOperator.isEmptyString(retStr)) {
 	    				SendPowerInfo info =new SendPowerInfo();
 	    				info.mergeFrom(retStr);
 	    				infoMap.put(info.getPlayerId(), info);
 	    			}
 	    		}   		
			}
		}catch (Exception e) {
			HawkException.catchException(e);
		}
		return infoMap;
	}
	
}
