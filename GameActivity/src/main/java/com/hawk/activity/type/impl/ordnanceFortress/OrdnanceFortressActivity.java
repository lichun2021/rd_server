package com.hawk.activity.type.impl.ordnanceFortress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressBigReward;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressCommReward;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressDailyReward;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressKVCfg;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressStage;
import com.hawk.activity.type.impl.ordnanceFortress.cfg.OrdnanceFortressWeight;
import com.hawk.activity.type.impl.ordnanceFortress.entity.OrdnanceFortressEntity;
import com.hawk.activity.type.impl.ordnanceFortress.entity.OrdnanceFortressTicket;
import com.hawk.game.protocol.Activity.OpenOrdnanceFortressTicketResp;
import com.hawk.game.protocol.Activity.OrdnanceFortressInfoResponse;
import com.hawk.game.protocol.Activity.OrdnanceFortressRewardType;
import com.hawk.game.protocol.Common.KeyValuePairInt;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.log.Action;
/**
 * 军械要塞
 * @author che
 *
 */
public class OrdnanceFortressActivity extends ActivityBase{

	private static final Logger logger = LoggerFactory.getLogger("Server");

	public OrdnanceFortressActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.ORDNANCE_FORTRESS_ACTIVITY;
	}

	
	@Override
	public void onPlayerLogin(String playerId) {
		if (isOpening(playerId)) {
			Optional<OrdnanceFortressEntity> opDataEntity = this.getPlayerDataEntity(playerId);
			if (!opDataEntity.isPresent()) {
				return;
			}
			//初始化成就数据
			if (opDataEntity.get().getStageId() == 0) {
				this.init(playerId, 1);
			}
		}
	}
	
	

	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.ORDNANCE_FORTRESS_INIT, () -> {
				this.init(playerId, 1);
				this.syncActivityDataInfo(playerId);
			});
		}
	}
	
	

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (opDataEntity.isPresent()) {
			syncActivityInfo(playerId, opDataEntity.get());
		}
	}

	

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		OrdnanceFortressActivity activity = new OrdnanceFortressActivity(
				config.getActivityId(), activityEntity);
		return activity;
	}

	
	
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<OrdnanceFortressEntity> queryList = HawkDBManager.getInstance()
				.query("from OrdnanceFortressEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			OrdnanceFortressEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		OrdnanceFortressEntity entity = new OrdnanceFortressEntity(playerId, termId);
		return entity;
	}
	
	
	/**
	 * 初始化数据
	 * @param playerId
	 * @param stage
	 */
	public void init(String playerId,int stage){
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		OrdnanceFortressEntity entity = opDataEntity.get();
		logger.info("OrdnanceFortress,init,playerId: {} ,stageId: {}", playerId, stage);
		OrdnanceFortressStage stageCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressStage.class, stage);
		if(stageCfg == null){
			return;
		}
		int bigRewardTime = this.getBigRewardWinTime(stageCfg);
		int defaultChose = entity.getDefaultChose(stageCfg.getBigAwardPool());
		OrdnanceFortressBigReward rewardCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressBigReward.class, defaultChose);
		if(rewardCfg != null){
			int bigRewardCount = entity.getBigRewardCount(defaultChose);
			if(bigRewardCount >= rewardCfg.getMaxLimit()){
				defaultChose = 0;
				entity.removeDeDefaultChose(stageCfg.getBigAwardPool());
			}
		}
		entity.setStageId(stage);
		entity.setRewardShow(0);
		entity.setBigRewardId(defaultChose);
		entity.setBigAwardTimes(bigRewardTime);
		entity.setOpenCount(0);
		entity.getTicketList().clear();
		entity.notifyUpdate();
		logger.info("OrdnanceFortress,init,finish,playerId: {} ,stageId: {},bigRewardTime:{},defaultChose:{}",
				playerId, stage,bigRewardTime,defaultChose);
		
	}

	public void showRewardOver(String playerId){
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		OrdnanceFortressEntity entity = opDataEntity.get();
		entity.setRewardShow(1);
		this.syncActivityInfo(playerId, entity);
		PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.ORDNANCE_FORTRESS_SHOW_REWARD_OVER_REQ_VALUE);

	}
	
	public void choseDefaultBigReward(String playerId,int poolId,int bigRewardId,int type){
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		OrdnanceFortressEntity entity = opDataEntity.get();
		OrdnanceFortressBigReward rewardCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressBigReward.class, bigRewardId);
		if(rewardCfg == null){
			return;
		}
		if(rewardCfg.getPool() != poolId){
			return;
		}
		
		if(type == 1){
			int bigRewardCount = entity.getBigRewardCount(rewardCfg.getId());
			if(bigRewardCount >= rewardCfg.getMaxLimit()){
				PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ORDNANCE_FORTRESS_DEFAULT_BIG_REWARD_REQ_VALUE,
						Status.Error.ORDNANCE_FORTRESS_BIG_REWARD_COUNT_LIMIT_VALUE);
				return;
			}
			entity.updateDeDefaultChose(poolId, bigRewardId);
		}else{
			entity.removeDeDefaultChose(poolId);
		}
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
		PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.ORDNANCE_FORTRESS_SHOW_REWARD_OVER_REQ_VALUE);
	}
	/**
	 * 获取大奖中奖次数
	 */
	public int getBigRewardWinTime(OrdnanceFortressStage stage){
		int wpool = stage.getBigAwardWeight();
		List<OrdnanceFortressWeight> list =  HawkConfigManager.getInstance().getConfigIterator(OrdnanceFortressWeight.class).toList();
		Map<Integer,Integer> weightMap = new HashMap<>();
		for(OrdnanceFortressWeight weight : list){
			if(wpool == 1){
				weightMap.put(weight.getTimes(), weight.getWeight1());
			}else if(wpool == 2){
				weightMap.put(weight.getTimes(), weight.getWeight2());
			}else if(wpool == 3){
				weightMap.put(weight.getTimes(), weight.getWeight3());
			}
		}
		Integer times = HawkRand.randomWeightObject(weightMap);
		return times;
	}
	
	
	/**
	 * 选择大奖
	 * @param playerId
	 * @param rewardId
	 */
	public void chooseBigReward(String playerId,int rewardId){
		OrdnanceFortressBigReward rewardCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressBigReward.class, rewardId);
		if(rewardCfg == null){
			return;
		}
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		OrdnanceFortressEntity entity = opDataEntity.get();
		int stageId = entity.getStageId();
		OrdnanceFortressStage stageCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressStage.class, stageId);
		if(stageCfg.getBigAwardPool() != rewardCfg.getPool()){
			logger.info("OrdnanceFortress,chooseBigReward,fail,playerId: {} ,stageId: {},rewardId:{}", playerId, stageId,rewardId);
			return;
		}
		int bigRewardCount = entity.getBigRewardCount(rewardCfg.getId());
		if(bigRewardCount >= rewardCfg.getMaxLimit()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, HP.code.ORDNANCE_FORTRESS_CHOSE_BIG_REWARD_REQ_VALUE,
					Status.Error.ORDNANCE_FORTRESS_BIG_REWARD_COUNT_LIMIT_VALUE);
			return;
		}
		entity.setBigRewardId(rewardId);
		entity.notifyUpdate();
		this.syncActivityInfo(playerId, entity);
		PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.ORDNANCE_FORTRESS_CHOSE_BIG_REWARD_REQ_VALUE);
		logger.info("OrdnanceFortress,chooseBigReward,playerId: {} ,stageId: {},rewardId:{}", playerId, stageId,rewardId);
		
	}


	/**
	 * 打开奖券
	 * @param ticketId
	 */
	public void openTicket(String playerId,int ticketId){
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		OrdnanceFortressEntity entity = opDataEntity.get();
		if(entity.getRewardShow() <= 0){
			return;
		}
		int stageId = entity.getStageId();
		OrdnanceFortressStage stageCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressStage.class, stageId);
		boolean valite = stageCfg.valiteId(ticketId);
		if(!valite){
			logger.info("OrdnanceFortress,openTicket,valiteId,fail,playerId: {} ,stageId: {},ticketId:{}", playerId, stageId,ticketId);
			return;
		}
		OrdnanceFortressTicket ticket = entity.getTicket(ticketId);
		if(ticket != null){
			logger.info("OrdnanceFortress,openTicket,ticket,opened,playerId: {} ,stageId: {},ticketId:{}", playerId, stageId,ticketId);
			return;
		}
		OrdnanceFortressKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(OrdnanceFortressKVCfg.class);
		List<RewardItem.Builder> makeCost = RewardHelper.toRewardItemList(cfg.getCostItem());
		boolean cost = this.getDataGeter().cost(playerId,makeCost, 1, Action.ORDNANCE_FORTRESS_OPEN_COST, true);
		if (!cost) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(playerId, 
					HP.code.OPEN_ORDNANCE_FORTRESS_TICKET_REQ_VALUE, Status.Error.ITEM_NOT_ENOUGH_VALUE);
			return;
		}
		List<OrdnanceFortressTicket> list = this.randomTicketAward(ticketId, entity, stageCfg);
		//给客户端的信息
		entity.notifyUpdate();
		List<RewardItem.Builder> rewards = new ArrayList<RewardItem.Builder>();
		OpenOrdnanceFortressTicketResp.Builder builder = OpenOrdnanceFortressTicketResp.newBuilder();
		builder.setTicketId(ticketId);
		for(OrdnanceFortressTicket tic : list){
			builder.addTickets(tic.toBuilder());
			rewards.addAll(tic.rewards());
		}
		this.getDataGeter().takeReward(playerId,rewards, 1, Action.ORDNANCE_FORTRESS_OPEN_ACHIVE, true, RewardOrginType.Ordnance_Fortress_OPEN_REWARD);
		pushToPlayer(playerId, HP.code.OPEN_ORDNANCE_FORTRESS_TICKET_RESP_VALUE, builder);
		int termId = this.getActivityTermId();
		//日志记录
		for(OrdnanceFortressTicket tic : list){
			if(tic.getTicketId() == ticketId){
				this.getDataGeter().logOrdnanceFortressOpen(playerId, termId, stageId, tic.getTicketId(), tic.getOpenCount(), cfg.getCostItem(), tic.getRewardType(), tic.getRewardId());
			}else{
				this.getDataGeter().logOrdnanceFortressOpen(playerId, termId, stageId, tic.getTicketId(), tic.getOpenCount(), "0", tic.getRewardType(), tic.getRewardId());
			}
		}
	}

	
	/**
	 * 随机奖励
	 * @param ticketId
	 * @param entity
	 * @return
	 */
	public List<OrdnanceFortressTicket> randomTicketAward(int ticketId,OrdnanceFortressEntity entity,
			OrdnanceFortressStage stageCfg){
		List<Integer> addOpen = new ArrayList<Integer>();
		addOpen.add(ticketId);
		List<OrdnanceFortressTicket> list = new ArrayList<OrdnanceFortressTicket>();
		int maxCount = OrdnanceFortressStage.xLength * OrdnanceFortressStage.yLength;
		for(int i=0;i<maxCount;i++){
			if(addOpen.size() <= 0){
				break;
			}
			int tid = addOpen.remove(0);
			OrdnanceFortressTicket ticket = entity.getTicket(tid);
			if(ticket != null){
				logger.info("OrdnanceFortress,randomTicketAward,ticket,opened,playerId: {} ,stageId: {},ticketId:{}",
						entity.getPlayerId(), entity.getStageId(),ticketId);
				continue;
			}
			int count = entity.getOpenCount();
			int bigRewardTime = entity.getBigAwardTimes();
			//中大奖
			if((count +1)>= bigRewardTime && !entity.isBigReward()){
				int bigRewardId = entity.getBigRewardId();
				OrdnanceFortressTicket big = OrdnanceFortressTicket.valueOf(tid, OrdnanceFortressRewardType.BIG_REWEARD_VALUE,
						bigRewardId, count + 1, HawkTime.getMillisecond());
				list.add(big);
				entity.addOrdnanceFortressTicket(big);
				entity.addOpenCount(1);
				entity.addBigRewardCount(bigRewardId, 1);
				logger.info("OrdnanceFortress,randomTicketAward,playerId: {} ,stageId: {},ticketId:{},count:{},awardType:{},awardId:{}", 
						entity.getPlayerId(), entity.getStageId(),ticketId,big.getOpenCount(),big.getRewardType(),big.getRewardId());
				continue;
			}
			int ranRewardId = this.randomCommReward(entity, stageCfg);
			//普通奖励
			OrdnanceFortressCommReward rewardCfg =  HawkConfigManager.getInstance().
					getConfigByKey(OrdnanceFortressCommReward.class, ranRewardId);
			if(rewardCfg == null){
				continue;
			}
			OrdnanceFortressTicket comm = OrdnanceFortressTicket.valueOf(tid, OrdnanceFortressRewardType.COMM_REWEARD_VALUE,
					ranRewardId, count + 1, HawkTime.getMillisecond());
			entity.addOrdnanceFortressTicket(comm);
			entity.addOpenCount(1);
			list.add(comm);
			logger.info("OrdnanceFortress,randomTicketAward,playerId: {} ,stageId: {},ticketId:{},count:{},awardType:{},awardId:{}", 
					entity.getPlayerId(), entity.getStageId(),ticketId,comm.getOpenCount(),comm.getRewardType(),comm.getRewardId());
			addOpen.addAll(rewardCfg.getExtendIds(tid, OrdnanceFortressStage.xLength, OrdnanceFortressStage.yLength));
			
		}
		return list;
	}
	
	
	/**
	 * 随机普通奖励
	 * @param entity
	 * @param stageCfg
	 * @return
	 */
	public int randomCommReward(OrdnanceFortressEntity entity,
			OrdnanceFortressStage stageCfg){
		Map<Integer,Integer> achiveMap = entity.getCommRewardMap();
		Map<Integer,Integer> rewardMap = stageCfg.getRewardMap();
		Map<Integer,Integer> weightMap = new HashMap<Integer,Integer>();
		for(Entry<Integer,Integer> entry : rewardMap.entrySet()){
			int id = entry.getKey();
			int val = entry.getValue();
			if(achiveMap.containsKey(id)){
				val -= achiveMap.get(id);
			}
			if(val > 0){
				weightMap.put(id, val);
			}
		}
		int rewardId = HawkRand.randomWeightObject(weightMap);
		return rewardId;
	}
	
	
	/**
	 * 进入下一个关卡
	 * @param playerId
	 */
	public void nextStage(String playerId){
		Optional<OrdnanceFortressEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		
		OrdnanceFortressEntity entity = opDataEntity.get();
		if (entity.getTicketList().isEmpty() || !entity.isBigReward()) {
			return;
		}
		
		int stageId = entity.getStageId();
		int nexstStage = stageId + 1;
		OrdnanceFortressStage stageCfg = HawkConfigManager.getInstance().getConfigByKey(OrdnanceFortressStage.class, nexstStage);
		if(stageCfg == null){
			return;
		}
		//在这里需要记录一下上一阶段的内容到日志
		this.init(playerId, nexstStage);
		this.syncActivityInfo(playerId, entity);
		PlayerPushHelper.getInstance().responseSuccess(playerId, HP.code.ORDNANCE_FORTRESS_ENTER_NEXT_STAGE_REQ_VALUE);
		int termId = this.getActivityTermId();
		this.getDataGeter().logOrdnanceFortressAdvance(playerId, termId, stageId, nexstStage);
	}
	
	
	public List<RewardItem.Builder> getDailyBoxExtRewards(int id){
		List<OrdnanceFortressDailyReward> list = HawkConfigManager.getInstance().
				getConfigIterator(OrdnanceFortressDailyReward.class).toList();
		for(OrdnanceFortressDailyReward reward : list){
			if(reward.getDaily_reward() == id){
				List<RewardItem.Builder> items = RewardHelper.toRewardItemList(reward.getKey());
				return items;
			}
		}
		return null;
	}
	
	
	

	/**
	 * 信息同步
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, OrdnanceFortressEntity entity){
		OrdnanceFortressInfoResponse.Builder builder = OrdnanceFortressInfoResponse.newBuilder();
		builder.setStageId(entity.getStageId());
		builder.setBigRewardId(entity.getBigRewardId());
		builder.setShowReward(entity.getRewardShow());
		for( Entry<Integer, Integer> entry : entity.getRewardChooseMap().entrySet()){
			int pool = entry.getKey();
			int chose= entry.getValue();
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(pool);
			kbuilder.setVal(chose);
			builder.addBigRewardDefault(kbuilder);
		}
		for( Entry<Integer, Integer> entry : entity.getBigRewardCountMap().entrySet()){
			int id = entry.getKey();
			int count= entry.getValue();
			KeyValuePairInt.Builder kbuilder = KeyValuePairInt.newBuilder();
			kbuilder.setKey(id);
			kbuilder.setVal(count);
			builder.addBigRewardCount(kbuilder);
		}
		
		for(OrdnanceFortressTicket ticket : entity.getTicketList()){
			builder.addTickets(ticket.toBuilder());
		}
		pushToPlayer(playerId, HP.code.ORDNANCE_FORTRESS_INFO_RESP_VALUE, builder);
	}


}
