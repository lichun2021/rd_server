package com.hawk.activity.type.impl.alliesWishing;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.ContinueLoginEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishExchangeCfg;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishKVCfg;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishNumRandomCfg;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishPosRandomCfg;
import com.hawk.activity.type.impl.alliesWishing.cfg.AllianceWishTimeCfg;
import com.hawk.activity.type.impl.alliesWishing.entity.AllianceWishEntity;
import com.hawk.activity.type.impl.alliesWishing.entity.WishMember;
import com.hawk.game.protocol.Activity.PBAllianceWishExchange;
import com.hawk.game.protocol.Activity.PBAllianceWishInfoResp;
import com.hawk.game.protocol.Activity.PBAllianceWishSignState;
import com.hawk.game.protocol.Activity.PBAllianceWishTipAction;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 盟军祝福
 * @author che
 *
 */
public class AllianceWishActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	private long tickTime;
	
	public AllianceWishActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ALLIANCE_WISH_ACTIVITY;
	}

	
	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		AllianceWishActivity activity = new AllianceWishActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<AllianceWishEntity> queryList = HawkDBManager.getInstance()
				.query("from AllianceWishEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			AllianceWishEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		AllianceWishEntity entity = new AllianceWishEntity(playerId, termId);
		return entity;
	}
	
	
	@Override
	public void onTick() {
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		long curTime = HawkTime.getMillisecond();
		if(this.tickTime == 0){
			this.tickTime = curTime;
			return;
		}
		long lastTiclTime = this.tickTime;
		this.tickTime = curTime;
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		long signEndTime = timeCfg.getSignEndTimeValue();
		if(lastTiclTime < signEndTime && signEndTime <= this.tickTime){
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				callBack(playerId, MsgId.ALLIANCE_WISH_SYNC, () -> {
					syncActivityDataInfo(playerId);
				});
			}
		}
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ALLIANCE_WISH_INIT, () -> {
				syncActivityDataInfo(playerId);
			});
		}
	}
	
	
	@Override
	public void onEnd() {
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		String serverId = this.getDataGeter().getServerId();
		String recordKey = this.getGfitBuyPlayerKeys(serverId);
		Set<String> playerIds = ActivityGlobalRedis.getInstance().sMembers(recordKey);
		long startTime = timeCfg.getStartTimeValue();
		long endTime = timeCfg.getEndTimeValue();
		Long serverMergeTime = getDataGeter().getServerMergeTime();
		if (serverMergeTime == null) {
			serverMergeTime = 0l;
		}
		List<String> slaveServerList = getDataGeter().getSlaveServerList();
		// 本期活动开启之后合并的服务器,需要加载从服的投资人员列表
		if (!slaveServerList.isEmpty() && serverMergeTime >= startTime && serverMergeTime <= endTime) {
			for (String followServerId : slaveServerList) {
				String followRecordKey = getGfitBuyPlayerKeys(followServerId);
				Set<String> followPlayerIds = ActivityGlobalRedis.getInstance().sMembers(followRecordKey);
				if (!followPlayerIds.isEmpty()) {
					playerIds.addAll(followPlayerIds);
				}
			}
		}
		//发奖励
		for (String playerId : playerIds) {
			this.sendEndReward(playerId);
		}
	}
	
	private void sendEndReward(String playerId){
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		Optional<AllianceWishEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			HawkLog.logPrintln("AllianceWishEntity onEnd sendAwardError, entity is null, playerId: {}", playerId);
			return;
		}
		AllianceWishEntity dataEntity = opEntity.get();
		if(dataEntity.getAchiveWish() > 0){
			HawkLog.logPrintln("AllianceWishEntity onEnd sendAwardError, entity AchiveWish, playerId: {}", playerId);
			return;
		}
		List<Integer> nums = dataEntity.getNumList();
		StringBuilder builder = new StringBuilder();
		for(int num : nums){
			builder.append(num);
		}
		String valStr = builder.toString();
		long val = Long.parseLong(valStr);
		if(dataEntity.getBuyGift() > 0){
			val *= cfg.getGiftMulitParam();
		}
		List<RewardItem.Builder> items = cfg.getAchiveItemList();
		for(RewardItem.Builder item : items){
			item.setItemCount(item.getItemCount() * val);
		}
		dataEntity.setAchiveWish(val);
		Object[] content =  new Object[]{this.getActivityTermId(),val};
		this.getDataGeter().sendMail(playerId, MailId.ALLIANCE_WISH_ACHIVE_SUPLLY, null, null, content,
				items, false);
		HawkLog.logPrintln("AllianceWishEntity onEnd, entity AchiveWish, playerId: {},gifit:{},num:{}", playerId,dataEntity.getBuyGift(),val);
		
	}
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}
	
	
	@Subscribe
	public void onContinueLogin(ContinueLoginEvent event) {
		String playerId = event.getPlayerId();
		if (!isOpening(event.getPlayerId())) {
			return;
		}
		if (event.isCrossDay()) {
			Optional<AllianceWishEntity> opEntity = getPlayerDataEntity(playerId);
			if (!opEntity.isPresent()) {
				return;
			}
			AllianceWishEntity entity = opEntity.get();
			this.syncActivityInfo(playerId, entity);
		}
	}
	
	/**
	 * 玩家签到
	 * @param playerId
	 */
	public void playerSign(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state != PBAllianceWishSignState.ALLIANCE_WISH_SIGN){
			return;
		}
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int pos = this.randomChangePos(cfg.getWishSignPosPoolId(), entity);
		if(pos == 0){
			return;
		}
		int numRandom = this.randomChangeNum(pos, entity);
		if(numRandom == 0){
			return;
		}
		long curTime = HawkTime.getMillisecond();
		entity.updateNumByPos(pos, numRandom);
		entity.setLastSignTime(curTime);
		entity.setSignDays(entity.getSignDays() + 1);
		this.syncActivityInfo(playerId, entity);
		int termId = this.getActivityTermId();
		this.getDataGeter().logAllianceWishSign(playerId, termId,1, pos, numRandom);
		logger.info("AllianceWishActivity,playerSign playerId:{}, pos:{},number:{}", playerId,pos,numRandom);
	}
	
	
	/**
	 * 补签
	 * @param playerId
	 */
	public void playerSupplySign(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		boolean canSign = this.canSupplySign(entity);
		if(!canSign){
			return;
		}
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int pos = this.randomChangePos(cfg.getWishSignPosPoolId(), entity);
		if(pos == 0){
			return;
		}
		int numRandom = this.randomChangeNum(pos, entity);
		if(numRandom == 0){
			return;
		}
		List<RewardItem.Builder> makeCost = cfg.getsupplySignCostItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, 1, Action.ALLIANCE_WISH_EXCAHNGE_RESET_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code2.ALLIANCE_WISH_SUPPLY_SIGN_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		entity.updateNumByPos(pos, numRandom);
		entity.setSupplySignDays(entity.getSupplySignDays() + 1);
		this.syncActivityInfo(playerId, entity);
		int termId = this.getActivityTermId();
		this.getDataGeter().logAllianceWishSign(playerId, termId,2, pos, numRandom);
		logger.info("AllianceWishActivity,playerSupplySign  playerId:{},pos:{},number:{}", playerId,pos,numRandom);
	}

	/**
	 * 发送联盟帮助
	 * @param playerId
	 */
	public void sendGuildHelp(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		String guildId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guildId)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ALLIANCE_WISH_SEND_GUILD_HELP_REQ_VALUE,
					Status.Error.ALLIANCE_WISH_SEND_HELP_GUILD_LIMIT_VALUE);
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		if(!this.canSendToGuild(entity)){
			return;
		}
		
		long curTime = HawkTime.getMillisecond();
		entity.setSendGuildTime(curTime);
		this.getDataGeter().addWorldBroadcastMsg(Const.ChatType.GUILD_HREF, guildId, 
				Const.NoticeCfgId.ALLIANCE_WISH_SEND_GUILD_HELP, playerId, playerId);
		this.syncActivityInfo(playerId, entity);
		logger.info("AllianceWishActivity,sendGuildHelp playerId:{},guildId:{}", playerId,guildId);
	}
	
	
	/**
	 * 帮助别人
	 * @param playerId
	 */
	@SuppressWarnings("deprecation")
	public void helpWish(String playerId,String targetPlayer){
		if(HawkOSOperator.isEmptyString(targetPlayer)){
			return;
		}
		if(playerId.equals(targetPlayer)){
			return;
		}
		
		String guilId = this.getDataGeter().getGuildId(playerId);
		if(HawkOSOperator.isEmptyString(guilId)){
			return;
		}
		String targetId = this.getDataGeter().getGuildId(targetPlayer);
		if(!guilId.equals(targetId)){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE,
					Status.Error.ALLIANCE_WISH_ACCEPT_HELP_NOT_SAME_GUILD_VALUE);
			return;
		}
		boolean targetCs = this.getDataGeter().isCrossPlayer(targetPlayer);
		if(targetCs){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE,
					Status.Error.ALLIANCE_WISH_ACCEPT_HELP_IN_CROSS_VALUE);
			return;
		}
		
		callBack(targetPlayer, MsgId.ALLIANCE_WISH_HELP_GUILD_MEMBER, () -> {
			Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(targetPlayer);
			if (!opDataEntity.isPresent()) {
				return;
			}
			AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
			AllianceWishEntity entity = opDataEntity.get();
			if(entity.getAchiveWish() > 0){
				PlayerPushHelper.getInstance().sendError(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE,
						Status.Error.ALLIANCE_WISH_ACCEPT_HELP_ACHIEVE_OVER_VALUE);
				return;
			}
			List<WishMember> wishList = entity.getWishMemberList();
			for(WishMember member: wishList){
				if(member.getPlayerId().equals(playerId)){
					PlayerPushHelper.getInstance().sendError(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE,
							Status.Error.ALLIANCE_WISH_ACCEPT_HELP_ALREADY_VALUE);
					return;
				}
			}
			int wishMemberCount = entity.getWishMemberList().size();
			if(wishMemberCount >= cfg.getWishGuildMemberCount() ){
				PlayerPushHelper.getInstance().sendError(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE,
						Status.Error.ALLIANCE_WISH_ACCEPT_HELP_OVER_VALUE);
				return;
			}
			String name = this.getDataGeter().getPlayerName(playerId);
			int icon = this.getDataGeter().getIcon(playerId);
			String pfIcon = this.getDataGeter().getPfIcon(playerId);
			WishMember member = new WishMember(playerId, name, icon, pfIcon);
			entity.addGuildWishMember(member);
			entity.setWishCount(entity.getWishCount() + 1);
			syncActivityInfo(targetPlayer, entity);
			PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code2.ALLIANCE_WISH_HELP_GUILD_MEMBER_REQ_VALUE);
			
			int termId = this.getActivityTermId();
			this.getDataGeter().logAllianceWishHelp(targetPlayer, termId, playerId);
			logger.info("AllianceWishActivity,helpWish  playerId:{},helper:{}", targetPlayer,playerId);
		});
	}
	
	
	
	/**
	 * 玩家使用祝福
	 * @param playerId
	 * @param type
	 */
	public void playerWish(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state != PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH){
			return;
		}
		int wishCount = entity.getWishCount();
		if(wishCount <=0){
			return;
		}
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int pos = this.randomChangePos(cfg.getWishCommPosPoolId(), entity);
		if(pos == 0){
			return;
		}
		int before = entity.getNumByPos(pos);
		int numRandom = this.randomChangeNum(pos, entity);
		if(numRandom == 0){
			return;
		}
		wishCount -=  1;
		wishCount = Math.max(0, wishCount);
		
		entity.updateNumByPos(pos, numRandom);
		entity.setWishCount(wishCount);
		
		this.syncActivityInfo(playerId, entity);
		logger.info("AllianceWishActivity,playerWish playerId:{},pos:{},before:{},after:{}", playerId,pos,before,numRandom);
	}
	
	
	
	/**
	 * 玩家使用祝福
	 * @param playerId
	 * @param type
	 */
	public void playerWishLuxury(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		int wishCount = entity.getLuxuryWishCount();
		if(wishCount <=0){
			return;
		}
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state != PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH){
			return;
		}
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int pos = this.randomChangePos(cfg.getWishLuxuryPosPoolId(), entity);
		if(pos == 0){
			return;
		}
		int before = entity.getNumByPos(pos);
		int numRandom = this.randomChangeNum(pos, entity);
		if(numRandom == 0){
			return;
		}
		wishCount -=  1;
		wishCount = Math.max(0, wishCount);
		
		entity.updateNumByPos(pos, numRandom);
		entity.setLuxuryWishCount(wishCount);
		
		this.syncActivityInfo(playerId, entity);
		logger.info("AllianceWishActivity,playerWishLuxury playerId:{},pos:{},before:{},after:{}", playerId,pos,before,numRandom);
	}
	
	
	/**
	 * 获取随机位置
	 * @param poolId
	 * @return
	 */
	private int randomChangePos(int poolId,AllianceWishEntity entity){
		Map<Integer,Integer> randomPosMap = new HashMap<>();
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		AllianceWishPosRandomCfg ranCfg = HawkConfigManager.getInstance()
				.getConfigByKey(AllianceWishPosRandomCfg.class, poolId);
		if(cfg.getWishSignPosPoolId() == poolId){
			int currSignCount = entity.getTotalSignDays() + 1;
			int mustPos = cfg.getSignMust(currSignCount);
			if(mustPos > 0 && entity.getNumByPos(mustPos)<9){
				return mustPos;
			}
		}
		int numCount = cfg.getNumCount();
		for(int i=1;i<=numCount;i++){
			int num = entity.getNumByPos(i);
			if(cfg.getWishCommPosPoolId() == poolId 
					&& i == 1 && num == 0 && !this.allMax(entity, 1)){
				//普通祝福不解锁万位,除非其他位置全都是9。固定写死
				continue;
			}
			if(num < 9){
				int weight = ranCfg.getPosWeight().getOrDefault(i, 0);
				if(weight <= 0){
					continue;
				}
				randomPosMap.put(i, weight);
			}
		}
		if(randomPosMap.size() > 0){
			Integer posRandom = HawkRand.randomWeightObject(randomPosMap);
			return posRandom;
		}
		//如果实在找不到就从头开始找
		for(int i=1;i<=numCount;i++){
			int num = entity.getNumByPos(i);
			if(num < 9){
				return i;
			}
		}
		return 0;
	}
	
	
	private boolean allMax(AllianceWishEntity entity,int expectPos){
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int numCount = cfg.getNumCount();
		for(int i=1;i<=numCount;i++){
			if(expectPos == i){
				continue;
			}
			int num = entity.getNumByPos(i);
			if(num < 9){
				return false;
			}
		}
		return true;
	}
	
	private int randomChangeNum(int pos,AllianceWishEntity entity){
		Map<Integer,Integer> randomNumMap = new HashMap<>();
		AllianceWishNumRandomCfg ranCfg = HawkConfigManager.getInstance()
				.getConfigByKey(AllianceWishNumRandomCfg.class, pos);
		int num = entity.getNumByPos(pos);
		for(Entry<Integer,Integer> entry : ranCfg.getNumWeight().entrySet()){
			int key = entry.getKey();
			int val = entry.getValue();
			if(key > num && val >= 0){
				randomNumMap.put(key, val);
			}
		}
		if(randomNumMap.size() <= 0){
			return 0;
		}
		Integer numRandom = HawkRand.randomWeightObject(randomNumMap);
		return numRandom;
	}
	
	/**
	 * 收货
	 * @param playerId
	 */
	public void playerWishAchive(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state != PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH_ACHIEVE){
			return;
		}
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		long val = this.getAchiveNum(entity);
		if(val <= 0){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code2.ALLIANCE_WISH_ACHIEVE_REQ_VALUE,
					Status.Error.ALLIANCE_WISH_ACHIEVE_ZERO_VALUE);
			return;
		}
		if(entity.getBuyGift() > 0){
			val *= cfg.getGiftMulitParam();
		}
		List<RewardItem.Builder> items = cfg.getAchiveItemList();
		for(RewardItem.Builder item : items){
			item.setItemCount(item.getItemCount() * val);
		}
		entity.setAchiveWish(val);
		//删除Redis记录
		if(entity.getBuyGift() > 0){
			String serverId = this.getDataGeter().getServerId();
			String recordKey = this.getGfitBuyPlayerKeys(serverId);
			ActivityGlobalRedis.getInstance().getRedisSession().sRem(recordKey, playerId);
		}
		this.getDataGeter().takeReward(playerId, items, 
				1, Action.ALLIANCE_WISH_ACHIEVE, true);
		this.syncActivityInfo(playerId, entity);
		//Tlog
		int termId = this.getActivityTermId();
		this.getDataGeter().logAllianceWishAchieve(playerId, termId, val, entity.getBuyGift());
		logger.info("AllianceWishActivity,playerWishAchive playerId:{},achive:{},", playerId,val);
	}
	
	
	private long getAchiveNum(AllianceWishEntity entity){
		StringBuilder builder = new StringBuilder();
		for(int num : entity.getNumList()){
			builder.append(num);
		}
		String valStr = builder.toString();
		long val = Long.parseLong(valStr);
		return val;
	}
	
	
	/**
	 * 礼包购买检查
	 * @param playerId
	 * @return
	 */
	public boolean buyGiftCheck(String playerId){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return false;
		}
		AllianceWishEntity entity = opDataEntity.get();
		if(entity.getBuyGift() > 0){
			return false;
		}
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state == PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH_FINISH){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 购买直购礼包
	 * @param event
	 */
	@Subscribe
	public void onBuyGiftEvent(PayGiftBuyEvent event) {
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		int buyId = Integer.parseInt(event.getGiftId());
		if(buyId != cfg.getAndroidPay() && buyId != cfg.getIosPay()){
			return;
		}
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		if(entity.getBuyGift() > 0){
			return;
		}
		entity.setBuyGift(buyId);
		entity.setWishCount(entity.getWishCount() + cfg.getGiftWishCount());
		entity.setLuxuryWishCount(entity.getLuxuryWishCount() + cfg.getGiftWishLuxuryCount());
		//Redis记录
		String serverId = this.getDataGeter().getServerId();
		String recordKey = this.getGfitBuyPlayerKeys(serverId);
		ActivityGlobalRedis.getInstance().sAdd(recordKey, getExpireSeconds(), event.getPlayerId());
		
		this.syncActivityInfo(event.getPlayerId(), entity);
		//Tlog
		int termId = this.getActivityTermId();
		long achieveCount = this.getAchiveNum(entity);
		int signCount = entity.getTotalSignDays();
		this.getDataGeter().logAllianceWishGiftBuy(event.getPlayerId(), termId,buyId, achieveCount, signCount);
		logger.info("AllianceWishActivity,onBuyGiftEvent playerId:{},gifitId:{},", event.getPlayerId(),event.getGiftId());
	}
	

	/**本服购买礼包玩家的数据
	 * @param serverId
	 * @return
	 */
	private String getGfitBuyPlayerKeys(String serverId) {
		int termId = getActivityTermId();
		return "activiy_alliance_buy_players:" + serverId + ":" + termId;
	}
	
	
	/**
	 * 记录有效期
	 * @return
	 */
	private int getExpireSeconds() {
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		long startTime = timeCfg.getStartTimeValue();
		long hiddenTime = timeCfg.getHiddenTimeValue();
		return (int) ((hiddenTime - startTime) / 1000 + 30 * 24 * 3600);
	}
	
	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId,int exchangeId,int exchangeCount,int protocolType){
		AllianceWishExchangeCfg config = HawkConfigManager.getInstance().
				getConfigByKey(AllianceWishExchangeCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeId);
		if(eCount + exchangeCount > config.getTimes()){
			logger.info("AllianceWishActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getNeedItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount, Action.ALLIANCE_WISH_EXCAHNGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		//增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				exchangeCount, Action.ALLIANCE_WISH_EXCAHNGE_GAIN, true);
		//同步
		this.syncActivityInfo(playerId,entity);
		logger.info("AllianceWishActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
		
	}
	
	/**
	 * 重置兑换
	 * @param playerId
	 */
	public void resetItemChange(String playerId,int protocolType){
		Optional<AllianceWishEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		AllianceWishEntity entity = opDataEntity.get();
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		List<RewardItem.Builder> makeCost = cfg.getResetCostItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, 1, Action.ALLIANCE_WISH_EXCAHNGE_RESET_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		entity.resetExchange();
		entity.setResetCount(entity.getResetCount() + 1);
		this.syncActivityInfo(playerId, entity);
		
		logger.info("AllianceWishActivity,resetItemChange,sucess,playerId: "
				+ "{},ecount:{}", playerId,entity.getResetCount());
	}
	
	public void updateActivityTips(String playerId, List<PBAllianceWishTipAction> actions){
		if(!isOpening(playerId)){
			return;
		}
		if(actions == null || actions.size() <= 0){
			return;
		}
		Optional<AllianceWishEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		AllianceWishEntity entity = opt.get();
		for(PBAllianceWishTipAction action : actions){
			int id = action.getId();
			int tip = action.getTip();
			AllianceWishExchangeCfg config = HawkConfigManager.getInstance().
					getConfigByKey(AllianceWishExchangeCfg.class, id);
			if (config == null) {
				continue;
			}
			if(tip > 0){
				entity.removeCareIgnore(id);
			}else{
				entity.addCareIgnore(id);
			}
		}
		this.syncActivityInfo(playerId, entity);
	}
	
	/**
	 * 是否可以发送联盟帮助
	 * @param entity
	 * @return
	 */
	public boolean canSendToGuild(AllianceWishEntity entity){
		long curTime = HawkTime.getMillisecond();
		long sendCountTime = entity.getGuildSendTime();
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		if(curTime < sendCountTime + cfg.getwishGuildSendCD() * 1000){
			return false;
		}
		int wishMemberCount = entity.getWishMemberList().size();
		if(wishMemberCount >= cfg.getWishGuildMemberCount() ){
			return false;
		}
		return true;
	}
	
	/**
	 * 是否可以补签
	 * @param entity
	 * @return
	 */
	public boolean canSupplySign(AllianceWishEntity entity){
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		long signEndTime = timeCfg.getSignEndTimeValue();
		long curTime = HawkTime.getMillisecond();
		if(curTime > signEndTime){
			return false;
		}
		int canSignCount = this.getCanSignCount();
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		if(state != PBAllianceWishSignState.ALLIANCE_WISH_SIGN_ALREADY ){
			return false;
		}
		if(canSignCount <= entity.getTotalSignDays()){
			return false;
		}
		return true;
	}
	
	
	/**
	 * 一共可以签到得次数
	 * @return
	 */
	public int getCanSignCount(){
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		long startTime = timeCfg.getStartTimeValue();
		long curTime = HawkTime.getMillisecond();
		
		long starTimeZero = HawkTime.getAM0Date(new Date(startTime)).getTime();
		long cross = curTime - starTimeZero;
		
		long rlt = cross / HawkTime.DAY_MILLI_SECONDS;
		long add = cross % HawkTime.DAY_MILLI_SECONDS;
		if(add > 0){
			rlt += 1;
		}
		return (int) rlt;
	}
	
	
	
	
	/**
	 * 获取活动状态
	 * @param entity
	 * @return
	 */
	public PBAllianceWishSignState getAllianceWishState(AllianceWishEntity entity){
		//是否已经领取
		if(entity.getAchiveWish() > 0){
			return PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH_FINISH;
		}
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		
		long curTime = HawkTime.getMillisecond();
		if(entity.getTotalSignDays() < cfg.getNumCount()
				&& curTime < timeCfg.getSignEndTimeValue()){
			if(HawkTime.isToday(entity.getLastSignTime())){
				return PBAllianceWishSignState.ALLIANCE_WISH_SIGN_ALREADY;
			}
			return PBAllianceWishSignState.ALLIANCE_WISH_SIGN;
		}else{
			//是否有祝福次数
			if(!entity.allMax() && (entity.getWishCount() > 0 || entity.getLuxuryWishCount() > 0)){
				return PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH;
			}
			//收货
			return PBAllianceWishSignState.ALLIANCE_WISH_SIGN_WISH_ACHIEVE;
		}
	}
	
	
	
	
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(String playerId,AllianceWishEntity entity){
		PBAllianceWishInfoResp.Builder builder = this.genAllianceWishInfoRespBuilder(entity);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.ALLIANCE_WISH_INFO_RESP, builder));
	}
	
	
	
	private PBAllianceWishInfoResp.Builder genAllianceWishInfoRespBuilder(AllianceWishEntity entity){
		AllianceWishKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(AllianceWishKVCfg.class);
		
		List<Integer> nums = entity.getNumList();
		PBAllianceWishSignState state = this.getAllianceWishState(entity);
		boolean canSupplySign = this.canSupplySign(entity);
		boolean buyGift = entity.getBuyGift() > 0;
		long sendGuildTime = entity.getGuildSendTime() + cfg.getwishGuildSendCD() * 1000;
		List<WishMember> members = entity.getWishMemberList();
		int wishCount = entity.getWishCount();
		int wishLuxuryCount = entity.getLuxuryWishCount();
		Map<Integer, Integer> emap = entity.getExchangeNumMap();
		int signDays = entity.getTotalSignDays();
		AllianceWishTimeCfg timeCfg = this.getAllianceWishTimeCfg();
		long signEndTime = timeCfg ==null?0:timeCfg.getSignEndTimeValue();
		Set<Integer> careIgnoreList = entity.getCareIgnoreList();
		
		PBAllianceWishInfoResp.Builder builder = PBAllianceWishInfoResp.newBuilder();
		builder.addAllOpenNumbers(nums);
		builder.setSignState(state);
		builder.setSupplyState(canSupplySign);
		builder.setBuyGift(buyGift);
		builder.setSignDays(signDays);
		builder.setSendHelpTime(sendGuildTime);
		builder.setSignEndTime(signEndTime);
		builder.setWishCount(wishCount);
		builder.setWishLuxuryCount(wishLuxuryCount);
		for(WishMember member : members){
			builder.addMembers(member.genPBWishMember());
		}
		for(Entry<Integer, Integer> entry : emap.entrySet()){
			PBAllianceWishExchange.Builder ebuilder = PBAllianceWishExchange.newBuilder();
			ebuilder.setExchangeId(entry.getKey());
			ebuilder.setNum(entry.getValue());
			builder.addExchanges(ebuilder);
		}
		
		List<AllianceWishExchangeCfg> eList = HawkConfigManager.getInstance()
				.getConfigIterator(AllianceWishExchangeCfg.class).toList();
		for(AllianceWishExchangeCfg ecfg : eList){
			if(!careIgnoreList.contains(ecfg.getId())){
				builder.addTips(ecfg.getId());
			}
		}
		
		return builder;
	}
	
	
	
	public AllianceWishTimeCfg getAllianceWishTimeCfg(){
		int termId = this.getActivityTermId();
		AllianceWishTimeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(AllianceWishTimeCfg.class,termId);
		return cfg;
	}
}
