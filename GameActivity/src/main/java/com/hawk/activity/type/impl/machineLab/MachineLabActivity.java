package com.hawk.activity.type.impl.machineLab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.net.protocol.HawkProtocol;
import org.hawk.os.HawkException;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.hawk.tuple.HawkTuple2;
import org.hawk.tuple.HawkTuples;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.event.impl.AgencyFinishEvent;
import com.hawk.activity.event.impl.CrScoreEvent;
import com.hawk.activity.event.impl.MachineAwakeTwoEvent;
import com.hawk.activity.event.impl.MonsterAttackEvent;
import com.hawk.activity.event.impl.PayGiftBuyEvent;
import com.hawk.activity.event.impl.TavernScoreBoxFinishEvent;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.redis.ActivityGlobalRedis;
import com.hawk.activity.type.ActivityState;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabExchangeCfg;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabKVCfg;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabLevel;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabPointRewardCfg;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabRankCfg;
import com.hawk.activity.type.impl.machineLab.cfg.MachineLabTimeCfg;
import com.hawk.activity.type.impl.machineLab.entity.MachineLabEntity;
import com.hawk.activity.type.impl.machineLab.rank.MachineLabRank;
import com.hawk.activity.type.impl.machineLab.rank.MachineLabRankMember;
import com.hawk.game.protocol.Activity.PBMachineLabContributeResp;
import com.hawk.game.protocol.Activity.PBMachineLabDrop;
import com.hawk.game.protocol.Activity.PBMachineLabExchange;
import com.hawk.game.protocol.Activity.PBMachineLabInfoResp;
import com.hawk.game.protocol.Activity.PBMachineLabRankMember;
import com.hawk.game.protocol.Activity.PBMachineLabRankResp;
import com.hawk.game.protocol.Activity.PBMachineLabTipAction;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
public class MachineLabActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");
	//数据很小，过期时间设置长一些，因为涉及补发奖励
	public static final int expireSeconds = (int)TimeUnit.DAYS.toSeconds(120);
	//当前服经验值
	private long serverExp;
	//攻坚排行榜
	private MachineLabRank rank;
	//注水时间点
	private long assistTime;
	//心跳
	private long tickTime;
	
	public MachineLabActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	
	@Override
	public ActivityType getActivityType() {
		return ActivityType.MACHINE_LAB_ACTIVITY;
	}

	
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		MachineLabActivity activity = new MachineLabActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<MachineLabEntity> queryList = HawkDBManager.getInstance()
				.query("from MachineLabEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			MachineLabEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		MachineLabEntity entity = new MachineLabEntity(playerId, termId);
		return entity;
	}
	
	
	@Override
	public void onOpen() {
		int termId = this.getActivityTermId();
		String serverId = this.getDataGeter().getServerId();
		//初始化公共数据
		this.initActivityServerDatda(termId, serverId);
		//广播在线玩家
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.MACHINE_LIB_INIT, () -> {
				Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
				if (opDataEntity.isPresent()) {
					MachineLabEntity entity = opDataEntity.get();
					//初始化玩家数据
					initActivityPlayerData(entity);
					syncActivityInfo(entity);
				}
			});
		}
	}
	
	@Override
	public void onTick() {
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		String serverId = this.getDataGeter().getServerId();
		if(this.tickTime == 0){
			this.tickTime = curTime;
			//初始化公共数据
			this.initActivityServerDatda(termId, serverId);
			return;
		}
		if(curTime < this.tickTime + 10 * 1000){
			return;
		}
		this.tickTime = curTime;
		if (this.getActivityEntity().getActivityState() == ActivityState.OPEN) {
			//注水
			this.assistServerExp();
			//更新广播
			Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
			for (String playerId : onlinePlayerIds) {
				callBack(playerId, MsgId.MACHINE_LIB_SYNC, () -> {
					Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
					if (opDataEntity.isPresent()) {
						MachineLabEntity entity = opDataEntity.get();
						syncActivityInfo(entity);
					}
				});
			}
		}
		
	}
	
	@Override
	public void onEnd() {
		//发放奖励
		this.sendRankReward();
	}
	
	@Override
	public void onHidden() {
		//清楚在线玩家无用的道具
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.MACHINE_LIB_CLEAR, () -> {
				clearPlayerLastItem(playerId);
			});
		}
	}
	
	
	@Override
	public void onPlayerLogin(String playerId) {
		//补发奖励
		this.rewardSupplement(playerId);
		//初始化玩家数据
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			MachineLabEntity entity = opDataEntity.get();
			//初始化玩家数据
			initActivityPlayerData(entity);
			//刷榜
			this.updateRankScore(playerId, entity.getStormingPointTotal());
		}else{
			this.clearPlayerLastItem(playerId);
		}
		
	}
	
	
	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(opDataEntity.get());
		}
	}
	
	/**
	 * 补发奖励
	 */
	private void rewardSupplement(String playerId){
		int lastTermId = this.getLastTermId();
		if(lastTermId <= 0){
			return;
		}
		String spKey = playerId + ":rewardSupplement:" + lastTermId;
		String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(spKey);
		if (StringUtils.isNotEmpty(val)){
			return;
		}
		ActivityGlobalRedis.getInstance().getRedisSession().setString(spKey, spKey, 30 * 24 * 3600);
		
		HawkDBEntity dbEntity = this.loadFromDB(playerId, lastTermId);
		if (null == dbEntity) {
			return;
		}
        MachineLabEntity machineLabEntity = (MachineLabEntity) dbEntity;
        if(machineLabEntity.getSupplementTime() > 0){
        	return;
        }
        long curTime = HawkTime.getMillisecond();
        machineLabEntity.setSupplementTime(curTime);
        String serverId = this.getDataGeter().getServerId();
        long serverExp = this.getServerExp(lastTermId, serverId);
		List<RewardItem.Builder> items = this.calRewardItems(machineLabEntity, serverExp);
		machineLabEntity.notifyUpdate(false, 0);
		if(items.size() <= 0){
			return;
		}
		//发邮件
		Object[] content =  new Object[]{lastTermId};
		this.getDataGeter().sendMail(playerId, MailId.MACHINE_LAB_SUPPLEMENT_MAIL, null, null, content,
				items, false);
	}
	
	
	
	
	/**
	 * 初始玩家数据
	 * @param entity
	 */
	private void initActivityPlayerData(MachineLabEntity entity){
		if(entity.getInitTime() <= 0){
			String playerServer = this.getDataGeter().getServerId();
			long time = HawkTime.getMillisecond();
			entity.setInitTime(time);
			entity.setPlayerServer(playerServer);
			clearPlayerLastItem(entity.getPlayerId());
		}
	}
	
	/**
	 * 初始公共数据
	 * @param termId
	 * @param serverId
	 */
	private void initActivityServerDatda(int termId,String serverId){
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		//初始化服务器总经验
		this.serverExp = this.getServerExp(termId, serverId);
		//初始化排行榜
		this.rank = new MachineLabRank(termId, serverId, expireSeconds);
		//排行榜加载
		this.rank.getRankShowMembers(kvcfg.getRankShowSize());
		//注水时间
		this.assistTime = this.getAssistTime();
	}
	
	/**
	 * 清除玩家剩余的攻坚芯片道具
	 * @param playerId
	 */
	private void clearPlayerLastItem(String playerId){
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		RewardItem.Builder dBuilder= kvcfg.getDonateItem();
		RewardItem.Builder sBuilder = kvcfg.getStormingItem();
		int donateItemCount = this.getDataGeter().getItemNum(playerId, dBuilder.getItemId());
		int stormingItemCount = this.getDataGeter().getItemNum(playerId, sBuilder.getItemId());
		if(donateItemCount > 0 || stormingItemCount > 0){
			//扣掉以前的道具
			List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
			if(donateItemCount > 0){
				dBuilder.setItemCount(donateItemCount);		
				rewardList.add(dBuilder);
			}
			if(stormingItemCount > 0){
				sBuilder.setItemCount(stormingItemCount);		
				rewardList.add(sBuilder);
			}
			//是否有足够的道具
			boolean consumeResult = this.getDataGeter().consumeItems
					(playerId, rewardList, 1, Action.MACHINE_LAB_CLEAR_ITEM);
			if(consumeResult){
				//日志
				logger.info("MachineLabActivity,clearPlayerLastItem,playerId: "
						+ "{},donateItemCount:{},stormingItemCount:{}", playerId,donateItemCount,stormingItemCount);
			}
		}
	}
	
	/**
	 * 高能打野专属
	 * @param type
	 * @param entity
	 * @param atkTimes
	 */
	private void killMonsterMultiAchieve(int type, MachineLabEntity entity, int atkTimes){
		MachineLabPointRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MachineLabPointRewardCfg.class, type);
		if(cfg == null){
			return;
		}
		
		int dropTotal = 0;
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		for (int i = 0; i < atkTimes; i++) {
			int alreadyNum = entity.getDropCount(type);
			int dropProb = cfg.getDropProb() / 100;
			if(alreadyNum >= cfg.getDropLimit() || dropProb <= 0){
				break;
			}
			
			int random = HawkRand.randInt(1, 100);
			if(random > dropProb){
				continue;
			}
			int dropMax = cfg.getDropLimit() - alreadyNum;
			int dorpNum = cfg.getDropNum();
			MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
			if(entity.getBuyGift() > 0){
				int giftMult = kvcfg.getGiftDropMultiple();
				dorpNum *= giftMult;
			}
			dorpNum = Math.min(dropMax, dorpNum);
			dropTotal += dorpNum;
			entity.addDropCount(type, dorpNum);
			RewardItem.Builder moneyBuilder = kvcfg.getDonateItem();
			moneyBuilder.setItemCount(dorpNum);
			rewardList.add(moneyBuilder);
		}
		
		if (dropTotal <= 0) {
			return;
		}
		
		this.getDataGeter().takeReward(entity.getPlayerId(), rewardList, 1, Action.MACHINE_LAB_FINISH_EVENT_ACHIEVE, false);
		Object[] content =  new Object[]{atkTimes};
		this.getDataGeter().sendMail(entity.getPlayerId(), MailId.MACHINE_LAB_EVENT_MAIL_MULTI, null, null, content, rewardList, true);
		this.getDataGeter().logMachineLabDrop(entity.getPlayerId(), entity.getTermId(), type, dropTotal);
		this.syncActivityInfo(entity);
	}
	
	/**
	 * 打野事件
	 * @param event
	 */
	@Subscribe
	public void onKillMonster(MonsterAttackEvent event){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opDataEntity.isPresent()) {
			return;
		}
		
		MachineLabEntity entity = opDataEntity.get();
		if (event.getAtkTimes() > 1) {
			this.killMonsterMultiAchieve(1, entity, event.getAtkTimes());
		} else {
			this.achieveContributeItem(1, entity);
		}
	}
	
	/**
	 * 完成试炼
	 * @param event
	 */
	@Subscribe
	public void onCrScoreAdd(CrScoreEvent event){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (opDataEntity.isPresent()) {
			this.achieveContributeItem(2, opDataEntity.get());
		}
	}
	
	/**
	 * 完成情报中心
	 * @param event
	 */
	@Subscribe
	public void onAgencyFinish(AgencyFinishEvent event){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (opDataEntity.isPresent()) {
			this.achieveContributeItem(3, opDataEntity.get());
		}
	}
	
	/**
	 * 攻打机甲
	 * @param event
	 */
	@Subscribe
	public void onMachineAwake(MachineAwakeTwoEvent event){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (opDataEntity.isPresent()) {
			if(event.isKill()){
				this.achieveContributeItem(5, opDataEntity.get());
			}else if(event.isFinalKill()){
				this.achieveContributeItem(7, opDataEntity.get());
			}else{
				this.achieveContributeItem(4, opDataEntity.get());
			}
		}
	}
	
	
	/**
	 * 每日任务积分宝箱完成
	 * @param event
	 */
	@Subscribe
	public void onTavernScoreBoxFinish(TavernScoreBoxFinishEvent event){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (opDataEntity.isPresent()) {
			this.achieveContributeItem(6, opDataEntity.get(), event.getScore());
		}
	}
	
	
	/**
	 * 发放攻坚芯片
	 * @param type
	 * @param entity
	 * @param params
	 */
	private void achieveContributeItem(int type, MachineLabEntity entity, Integer... params){
		MachineLabPointRewardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(MachineLabPointRewardCfg.class, type);
		if(cfg == null){
			return;
		}
		int alreadyNum = entity.getDropCount(type);
		if(alreadyNum >= cfg.getDropLimit()){
			return;
		}
		int dropMax = cfg.getDropLimit() - alreadyNum;
		int dropProb = 0;
		//有1个类型有限制，如果后面类型增加，在提出去
		if(type == 6){
			int boxId = params[0];
			if(cfg.getConditionList().contains(boxId)){
				dropProb = cfg.getDropProb()/100;
			}
		}else{
			dropProb = cfg.getDropProb()/100;
		}
		if(dropProb <= 0){
			return;
		}
		int random = HawkRand.randInt(1, 100);
		if(random > dropProb){
			return;
		}
		int dorpNum = cfg.getDropNum();
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		if(entity.getBuyGift() > 0){
			int giftMult = kvcfg.getGiftDropMultiple();
			dorpNum *= giftMult;
		}
		dorpNum = Math.min(dropMax, dorpNum);
		entity.addDropCount(type, dorpNum);
		List<RewardItem.Builder> rewardList = new ArrayList<RewardItem.Builder>();
		RewardItem.Builder moneyBuilder = kvcfg.getDonateItem();
		moneyBuilder.setItemCount(dorpNum);
		rewardList.add(moneyBuilder);
		this.getDataGeter().takeReward(entity.getPlayerId(), rewardList, 1, Action.MACHINE_LAB_FINISH_EVENT_ACHIEVE, false);
		Object[] content =  new Object[]{dorpNum};
		this.getDataGeter().sendMail(entity.getPlayerId(), MailId.MACHINE_LAB_EVENT_MAIL, null, null, content, rewardList, true);
		this.getDataGeter().logMachineLabDrop(entity.getPlayerId(), entity.getTermId(), type, dorpNum);
		this.syncActivityInfo(entity);
	}
	
	
	/**
	 * 捐献攻坚芯片
	 * @param player
	 * @param count
	 */
	public void contributeItems(String playerId,int count,int protoType){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		if(count > kvcfg.getMaxDonateLimit()){
			return;
		}
		Optional<MachineLabEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		MachineLabEntity entity = opt.get();
		//消耗
		List<RewardItem.Builder> consumeList = new ArrayList<RewardItem.Builder>();
		RewardItem.Builder moneyBuilder = kvcfg.getDonateItem();
		moneyBuilder.setItemCount(count);		
		consumeList.add(moneyBuilder);
		//是否有足够的道具
		boolean consumeResult = this.getDataGeter().consumeItems
				(playerId, consumeList, protoType, Action.MACHINE_LAB_CONTRIBUTE_COST);
		if(!consumeResult){
			return;
		}
		HawkTuple2<Integer, Integer> serverLevelBef = this.getServerLevelExp((int)this.serverExp);
		int serverExpAdd = kvcfg.getDonateServerExp() * count;
		int playerExpAdd = kvcfg.getDonatePlayerExp() * count;
		int stormingPointadd = kvcfg.getDonateStormingPoint() * count;
		int donatMultip = 0;
		Integer mult = HawkRand.randomWeightObject(kvcfg.getDonatMultipleMap());
		if(mult!= null){
			donatMultip = mult;
			serverExpAdd *= mult;
			playerExpAdd *= mult;
			stormingPointadd *= mult;
		}
		int termId = this.getActivityTermId();
		String serverId = this.getDataGeter().getServerId();
		//更新自身数据
		int totalStormingPoint = entity.getStormingPointTotal() + stormingPointadd;
		entity.setStormingPointTotal(totalStormingPoint);
		entity.addPlayerExp(playerExpAdd);
		//全服经验增加
		this.serverExp = this.addServerExp(termId, serverId, serverExpAdd);
		//攻坚排行榜更新
		this.updateRankScore(playerId, entity.getStormingPointTotal());
		//发攻坚点道具
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		RewardItem.Builder addBuilder = kvcfg.getStormingItem();
		addBuilder.setItemCount(stormingPointadd);		
		rewardList.add(addBuilder);
		this.getDataGeter().takeReward(playerId, rewardList, 
				1, Action.MACHINE_LAB_CONTRIBUTE_ACHIEVE, false);
		
		HawkTuple2<Integer, Integer> serverLevelAft = this.getServerLevelExp((int)this.serverExp);
		int serverLevelValBef = serverLevelBef.first;
		int serverLevelValAft = serverLevelAft.first;
		//返回捐献数据
		PBMachineLabContributeResp.Builder respBuilder = PBMachineLabContributeResp.newBuilder();
		respBuilder.setCount(count);
		respBuilder.setServerExpAdd(kvcfg.getDonateServerExp() * count);
		respBuilder.setPlayerExpAdd(kvcfg.getDonatePlayerExp() * count);
		respBuilder.setStormingPointAdd(kvcfg.getDonateStormingPoint() * count);
		respBuilder.setMultVal(donatMultip);
		if(serverLevelValBef != serverLevelValAft){
			respBuilder.setServerLevelUp(serverLevelValAft);
		}else{
			respBuilder.setServerLevelUp(0);
		}
		PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(),
				HawkProtocol.valueOf(HP.code2.MACHINE_LAB_CONTRIBUTE_RESP, respBuilder));
		//同步数据
		this.syncActivityInfo(entity);
		this.getDataGeter().logMachineLabContribute(playerId, termId, count, 0, donatMultip, serverExpAdd, playerExpAdd, 
				stormingPointadd, totalStormingPoint);
	}
	
	/**
	 * 领奖
	 * @param playerId
	 */
	public void achieveReward(String playerId){
		Optional<MachineLabEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		MachineLabEntity entity = opt.get();
		List<RewardItem.Builder> items = this.calRewardItems(entity, this.serverExp);
		//发奖励
		if(!items.isEmpty()){
			this.getDataGeter().takeReward(playerId, items, 
					1, Action.MACHINE_LAB_ORDER_ACHIEVE, true);
		}
		//同步数据
		this.syncActivityInfo(entity);
	}
	
	

	/**
	 * 计算奖励
	 * @param entity
	 * @param serverExpVal
	 * @return
	 */
	private List<RewardItem.Builder> calRewardItems(MachineLabEntity entity,long serverExpVal){
		HawkTuple2<Integer, Integer> serverLevelExp = this.getServerLevelExp((int)serverExpVal);
		HawkTuple2<Integer, Integer> playerLevelExp = this.getPlayerLevelExp(entity.getPlayerExp());
		int serverLevel = serverLevelExp.first;
		int playerLevel = playerLevelExp.first;
		//奖励列表
		List<RewardItem.Builder> items = new ArrayList<>();
		List<Integer> serverOrderLevels = new ArrayList<>();
		List<Integer> playerOrderLevels = new ArrayList<>();
		List<Integer> advOrderLevels = new ArrayList<>();
		int serverAchieveLevel = entity.getServerRewardLevel();
		int playerAchieveLevel = entity.getPlayerRewardLevel();
		int playerAdvAchieveLevel = entity.getPlayerAdvRewardLevel();
		if(serverAchieveLevel < serverLevel){
			for(int level =(serverAchieveLevel + 1);level <= serverLevel;level++){
				MachineLabLevel levelCfg = HawkConfigManager.getInstance()
						.getConfigByKey(MachineLabLevel.class, level);
				if(levelCfg != null){
					items.addAll(levelCfg.getRewardItemList());
					serverOrderLevels.add(levelCfg.getlevel());
				}
			}
			entity.setServerRewardLevel(serverLevel);
		}
		int playerRewardLevel = Math.min(playerLevel, serverLevel);
		if(playerAchieveLevel < playerRewardLevel){
			for(int level =(playerAchieveLevel + 1);level <= playerRewardLevel;level++){
				MachineLabLevel levelCfg = HawkConfigManager.getInstance()
						.getConfigByKey(MachineLabLevel.class, level);
				if(levelCfg != null){
					items.addAll(levelCfg.getPlayerRewardItemList());
					playerOrderLevels.add(levelCfg.getlevel());
				}
			}
			entity.setPlayerRewardLevel(playerRewardLevel);
		}
		
		if(entity.getBuyGift() > 0 && playerAdvAchieveLevel < serverLevel){
			for(int level =(playerAdvAchieveLevel + 1);level <= serverLevel;level++){
				MachineLabLevel levelCfg = HawkConfigManager.getInstance()
						.getConfigByKey(MachineLabLevel.class, level);
				if(levelCfg != null){
					items.addAll(levelCfg.getPlayerAdvRewardItemList());
					advOrderLevels.add(levelCfg.getlevel());
				}
			}
			entity.setPlayerAdvRewardLevel(serverLevel);
		}
		//日志
		try {
			if(serverOrderLevels.size() > 0){
				this.getDataGeter().logMachineLabOrderReward(entity.getPlayerId(), entity.getTermId(), 
						1, StringUtils.join(serverOrderLevels, ","));
			}
			if(playerOrderLevels.size() > 0){
				this.getDataGeter().logMachineLabOrderReward(entity.getPlayerId(), entity.getTermId(), 
						2, StringUtils.join(playerOrderLevels, ","));
			}
			if(advOrderLevels.size() > 0){
				this.getDataGeter().logMachineLabOrderReward(entity.getPlayerId(), entity.getTermId(), 
						3, StringUtils.join(advOrderLevels, ","));
			}
		} catch (Exception e) {
			HawkException.catchException(e);
		}
		return items;
	}
	
	
	
	/**
	 * 礼包购买检查
	 * @param playerId
	 * @return
	 */
	public boolean buyGiftCheck(String playerId,String payId){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return false;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return false;
		}
		MachineLabEntity entity = opDataEntity.get();
		if(entity.getBuyGift() > 0){
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
		MachineLabKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		int buyId = Integer.parseInt(event.getGiftId());
		if(buyId != cfg.getAndroidPay() && buyId != cfg.getIosPay()){
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(event.getPlayerId());
		if (!opDataEntity.isPresent()) {
			return;
		}
		MachineLabEntity entity = opDataEntity.get();
		if(entity.getBuyGift() > 0){
			return;
		}
		entity.setBuyGift(buyId);
		this.syncActivityInfo(entity);
		//发邮件
		Object[] content =  new Object[]{buyId};
		this.getDataGeter().sendMail(entity.getPlayerId(), MailId.MACHINE_LAB_BUY_GIFT_MAIL, null, null, content,
				null, true);
	}
	

	
	/**
	 * 道具兑换
	 * @param playerId
	 * @param protolType
	 */
	public void itemExchange(String playerId,int exchangeId,int exchangeCount,int protocolType){
		MachineLabExchangeCfg config = HawkConfigManager.getInstance().
				getConfigByKey(MachineLabExchangeCfg.class, exchangeId);
		if (config == null) {
			return;
		}
		Optional<MachineLabEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		MachineLabEntity entity = opDataEntity.get();
		int eCount = entity.getExchangeCount(exchangeId);
		if(eCount + exchangeCount > config.getTimes()){
			logger.info("MachineLabActivity,itemExchange,fail,countless,playerId: "
					+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
			return;
		}
		
		List<RewardItem.Builder> makeCost = config.getNeedItemList();
		boolean cost = this.getDataGeter().cost(playerId,makeCost, exchangeCount,
				Action.MACHINE_LAB_EXCAHNGE_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					protocolType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		
		//增加兑换次数
		entity.addExchangeCount(exchangeId, exchangeCount);
		//发奖励
		this.getDataGeter().takeReward(playerId, config.getGainItemList(), 
				exchangeCount, Action.MACHINE_LAB_EXCAHNGE_GAIN, true);
		//同步
		this.syncActivityInfo(entity);
		this.getDataGeter().logMachineLabExchange(playerId, entity.getTermId(), 
				exchangeId, exchangeCount);
		logger.info("MachineLabActivity,itemExchange,sucess,playerId: "
				+ "{},exchangeType:{},ecount:{}", playerId,exchangeId,eCount);
		
	}
	
	
	/**
	 * 更新关注
	 * @param playerId
	 * @param actions
	 */
	public void updateActivityTips(String playerId, List<PBMachineLabTipAction> actions){
		if(isHidden(playerId)){
			return;
		}
		if(actions == null || actions.size() <= 0){
			return;
		}
		Optional<MachineLabEntity> opt = getPlayerDataEntity(playerId);
		if(!opt.isPresent()){
			return;
		}
		MachineLabEntity entity = opt.get();
		for(PBMachineLabTipAction action : actions){
			int id = action.getId();
			int tip = action.getTip();
			MachineLabExchangeCfg config = HawkConfigManager.getInstance().
					getConfigByKey(MachineLabExchangeCfg.class, id);
			if (config == null) {
				continue;
			}
			if(tip > 0){
				entity.removeCareIgnore(id);
			}else{
				entity.addCareIgnore(id);
			}
		}
		this.syncActivityInfo(entity);
	}
	
	public void rankInfo(String playerId){
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		int rankSize = kvcfg.getRankShowSize();
		List<MachineLabRankMember> mlist = null;
		MachineLabRankMember self = null;
		if(this.rank != null){
			mlist = this.rank.getRankShowMembers(rankSize);
			self = this.rank.getRank(playerId);
		}
		PBMachineLabRankResp.Builder builder = PBMachineLabRankResp.newBuilder();
		if(mlist != null){
			for(MachineLabRankMember member : mlist){
				try {
					String name = getDataGeter().getPlayerName(member.getPlayerId());
					String guildTag = this.getDataGeter().getGuildTagByPlayerId(member.getPlayerId());
					PBMachineLabRankMember.Builder mbuilder = PBMachineLabRankMember.newBuilder();
					mbuilder.setPlayerId(member.getPlayerId());
					mbuilder.setPlayerName(name);
					mbuilder.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(member.getPlayerId()));
					mbuilder.setRankIndex(member.getRank());
					mbuilder.setScore((int)member.getScore());
					if(!HawkOSOperator.isEmptyString(guildTag)){
						mbuilder.setGuildTag(guildTag);
					}
					builder.addMembers(mbuilder);
				} catch (Exception e) {
				}
			}
		}
		int selfRank = 0;
		int selfScore = 0;
		String selfName = getDataGeter().getPlayerName(playerId);
		String selfGuildTag = this.getDataGeter().getGuildTagByPlayerId(playerId);
		if(self != null){
			selfRank = self.getRank();
			selfScore = (int)self.getScore();
		}
		PBMachineLabRankMember.Builder sbuilder = PBMachineLabRankMember.newBuilder();
		sbuilder.setPlayerId(playerId);
		sbuilder.setPlayerName(selfName);
		sbuilder.addAllPersonalProtectSwitch(getDataGeter().getPersonalProtectVals(playerId));
		sbuilder.setRankIndex(selfRank);
		sbuilder.setScore(selfScore);
		if(!HawkOSOperator.isEmptyString(selfGuildTag)){
			sbuilder.setGuildTag(selfGuildTag);
		}
		builder.setSelfRank(sbuilder);
		PlayerPushHelper.getInstance().pushToPlayer(playerId,
				HawkProtocol.valueOf(HP.code2.MACHINE_LAB_RANK_RESP, builder));

	}
	
	private void updateRankScore(String playerId,int score){
		if (this.getActivityEntity().getActivityState() != ActivityState.OPEN) {
			return;
		}
		if(score > 0){
			this.rank.setScore(playerId, score);
		}
	}
	
	
	/**
	 * 发放排行榜奖励
	 */
	private void sendRankReward(){
		int size = this.getRankRewardSize();
		List<MachineLabRankMember> rankMembers = this.rank.doRankSort(size);
		for(MachineLabRankMember member: rankMembers){
			int rank = member.getRank();
			MachineLabRankCfg cfg = this.getMachineLabRankCfg(rank);
			if(cfg == null){
				continue;
			}
			String playerId = member.getPlayerId();
			Object[] content =  new Object[]{rank};
			this.getDataGeter().sendMail(playerId, MailId.MACHINE_LAB_RANK_MAIL, null, null, content,
					cfg.getRewardItemList(), false);
		}
	}
	
	private MachineLabRankCfg getMachineLabRankCfg(int rank){
		ConfigIterator<MachineLabRankCfg> ite = HawkConfigManager.getInstance()
				.getConfigIterator(MachineLabRankCfg.class);
		while (ite.hasNext()) {
			MachineLabRankCfg cfg = ite.next();
			if(cfg.getRankUpper() <= rank && rank <= cfg.getRankLower()){
				return cfg;
			}
		}
		return null;
	}
	private int getRankRewardSize(){
		int size = 0;
		List<MachineLabRankCfg> list = HawkConfigManager.getInstance()
				.getConfigIterator(MachineLabRankCfg.class).toList();
		for(MachineLabRankCfg cfg : list){
			if(cfg.getRankLower() > size){
				size = cfg.getRankLower();
			}
		}
		return size;
	}
	
	private void assistServerExp(){
		if(this.assistTime > 0){
			MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
			if(this.tickTime >= this.assistTime && this.serverExp < kvcfg.getFloodAimExp()){
				int termId = this.getActivityTermId();
				MachineLabTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(MachineLabTimeCfg.class,termId);
				if(timeCfg != null){
					long assistBegin = timeCfg.getStartTimeValue() + kvcfg.getFloodBegin()*1000;
					long assistEnd = timeCfg.getStartTimeValue() + kvcfg.getFloodEnd()*1000;
					long curAssist = (this.assistTime - assistBegin)/(kvcfg.getFloodCd() * 1000);
					long TotalAssist = (assistEnd - assistBegin)/(kvcfg.getFloodCd() * 1000);
					long lastAssist = (TotalAssist - curAssist) + 1;
					long assistCount = (kvcfg.getFloodAimExp() - this.serverExp)/lastAssist;
					if(assistCount > 0){
						String serverId = this.getDataGeter().getServerId();
						this.serverExp = this.addServerExp(termId, serverId,(int)assistCount);
					}
				}
			}
		}
		this.assistTime = this.getAssistTime();
	}
	
	private long getAssistTime(){
		long curTime = HawkTime.getMillisecond();
		int termId = this.getActivityTermId();
		MachineLabTimeCfg timeCfg = HawkConfigManager.getInstance().getConfigByKey(MachineLabTimeCfg.class,termId);
		if(timeCfg == null){
			return 0;
		}
		MachineLabKVCfg kvcfg = HawkConfigManager.getInstance().getKVInstance(MachineLabKVCfg.class);
		long assistBegin = timeCfg.getStartTimeValue() + kvcfg.getFloodBegin() * 1000;
		long assistEnd = timeCfg.getStartTimeValue() + kvcfg.getFloodEnd() * 1000;
		if(curTime < assistBegin || curTime > assistEnd){
			return 0;
		}
		long timePass = (curTime - assistBegin) / (kvcfg.getFloodCd() * 1000);
		long assistTime = assistBegin + (timePass + 1) * (kvcfg.getFloodCd() * 1000);
		return assistTime;
	}
	/**
	 * 信息同步
	 * @param playerId
	 */
	public void syncActivityInfo(MachineLabEntity entity){
		PBMachineLabInfoResp.Builder builder = PBMachineLabInfoResp.newBuilder();
		HawkTuple2<Integer, Integer> serverLevelExp = this.getServerLevelExp((int)this.serverExp);
		HawkTuple2<Integer, Integer> playerLevelExp = this.getPlayerLevelExp(entity.getPlayerExp());
		Map<Integer, Integer> emap = entity.getExchangeNumMap();
		Set<Integer> careIgnoreList = entity.getCareIgnoreList();
		Map<Integer, Integer> dmap = entity.getDropNumMap();
		
		builder.setServerLevel(serverLevelExp.first);
		builder.setServerExp(serverLevelExp.second);
		builder.setPlayerLevel(playerLevelExp.first);
		builder.setPlayerExp(playerLevelExp.second);
		
		builder.setServerRewardLevel(entity.getServerRewardLevel());
		builder.setPlayerRewardLevel(entity.getPlayerRewardLevel());
		builder.setPlayerAdvRewardLevel(entity.getPlayerAdvRewardLevel());
		builder.setBuyGift(entity.getBuyGift());
		
		for(Entry<Integer, Integer> entry : emap.entrySet()){
			PBMachineLabExchange.Builder ebuilder = PBMachineLabExchange.newBuilder();
			ebuilder.setExchangeId(entry.getKey());
			ebuilder.setNum(entry.getValue());
			builder.addExchanges(ebuilder);
		}
		
		List<MachineLabExchangeCfg> eList = HawkConfigManager.getInstance()
				.getConfigIterator(MachineLabExchangeCfg.class).toList();
		for(MachineLabExchangeCfg ecfg : eList){
			if(!careIgnoreList.contains(ecfg.getId())){
				builder.addTips(ecfg.getId());
			}
		}
		
		for(Entry<Integer, Integer> entry : dmap.entrySet()){
			PBMachineLabDrop.Builder dbuilder = PBMachineLabDrop.newBuilder();
			dbuilder.setDropType(entry.getKey());
			dbuilder.setDropCount(entry.getValue());
			builder.addDrops(dbuilder);
		}
		
		PlayerPushHelper.getInstance().pushToPlayer(entity.getPlayerId(),
				HawkProtocol.valueOf(HP.code2.MACHINE_LAB_INFO_RESP, builder));
	}

	
	
	
	private HawkTuple2<Integer, Integer> getServerLevelExp(int exp){
		int level = 0;
		int levelExp = exp;
		int levelCount = HawkConfigManager.getInstance().getConfigIterator(MachineLabLevel.class).size();
		for(int i=1;i<=levelCount;i++ ){
			MachineLabLevel levelCfg = HawkConfigManager.getInstance()
					.getConfigByKey(MachineLabLevel.class, i);
			if(levelCfg == null){
				break;
			}
			if(levelExp < levelCfg.getLevelExp()){
				break;
			}
			level = levelCfg.getlevel();
			levelExp -= levelCfg.getLevelExp();
		}
		return HawkTuples.tuple(level,levelExp);
	}
	
	
	
	private HawkTuple2<Integer, Integer> getPlayerLevelExp(int exp){
		int level = 0;
		int levelExp = exp;
		int levelCount = HawkConfigManager.getInstance().getConfigIterator(MachineLabLevel.class).size();
		for(int i=1;i<=levelCount;i++ ){
			MachineLabLevel levelCfg = HawkConfigManager.getInstance()
					.getConfigByKey(MachineLabLevel.class, i);
			if(levelCfg == null){
				break;
			}
			if(levelExp < levelCfg.getPlayerLevelExp()){
				break;
			}
			level = levelCfg.getlevel();
			levelExp -= levelCfg.getPlayerLevelExp();
		}
		return new HawkTuple2<Integer, Integer>(level,levelExp);
	}
	
	private long addServerExp(int termId, String serverId, int add) {
		String redisKey = this.getServerExpRedisKey(termId, serverId);
		long result = ActivityGlobalRedis.getInstance().getRedisSession()
				.increaseBy(redisKey, add, expireSeconds);
		//getDataGeter().dungeonRedisLog(redisKey, "add{} return {}", add, result);
		logger.info("MachineLabActivity addServerExp add{} return {}", add, result);
		return result;
	}
	
	
	
	private long getServerExp(int termId,String serverId){
		String redisKey = this.getServerExpRedisKey(termId,serverId);
		String val = ActivityGlobalRedis.getInstance().getRedisSession().getString(redisKey);
		if(!HawkOSOperator.isEmptyString(val)){
			return Integer.parseInt(val);
		}
		return 0;
	}
	
	
	private String getServerExpRedisKey(int termId,String serverId){
		String key = "MECHA_INSTITUTE_SERVER_EXP:"+ serverId+":"+termId;
		return key;
	}
	
	
	private int getLastTermId() {
	    long curTime = HawkTime.getMillisecond();
	    MachineLabTimeCfg lastCfg = null;
	    List<MachineLabTimeCfg> list = HawkConfigManager.getInstance()
	    		.getConfigIterator(MachineLabTimeCfg.class).toList();
	    for(MachineLabTimeCfg cfg : list){
	        if(cfg.getHiddenTimeValue() < curTime){
	            if(lastCfg == null){
	                lastCfg = cfg;
	            }
	            if(cfg.getTermId() > lastCfg.getTermId()){
	                lastCfg = cfg;
	            }
	        }
	    }
	    if(lastCfg == null){
	        return  0;
	    }
	    return lastCfg.getTermId();
	}
	
	/**
	 * 删除排行榜数据
	 */
	public void removePlayerRank(String playerId) {
		if (this.getActivityEntity().getActivityState() == com.hawk.activity.type.ActivityState.HIDDEN) {
			return;
		}
		this.rank.removeRank(playerId);
		this.rank.rankMembersReset();
		HawkLog.logPrintln("remove player rank, playerId: {}, activity: {}", playerId, this.getActivityType().intValue());
	}
	
}
