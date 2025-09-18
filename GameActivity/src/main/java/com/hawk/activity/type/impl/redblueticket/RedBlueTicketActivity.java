package com.hawk.activity.type.impl.redblueticket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkRand;
import org.hawk.result.Result;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.redblueticket.cfg.RedBlueTicketActivityKVCfg;
import com.hawk.activity.type.impl.redblueticket.cfg.RedBlueTicketActivityRewardCfg;
import com.hawk.activity.type.impl.redblueticket.entity.RedBlueTicketActivityEntity;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.gamelib.GameConst.MsgId;
import com.hawk.game.protocol.Activity.RedBlueTicketInfoPB;
import com.hawk.game.protocol.Activity.RedBlueTicketPoolInfoPB;
import com.hawk.game.protocol.MailConst.MailId;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 红蓝对决翻牌活动
 * 
 * @author lating
 */
public class RedBlueTicketActivity extends ActivityBase{

	public RedBlueTicketActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.REDBLUE_TICKET_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		RedBlueTicketActivity activity = new RedBlueTicketActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<RedBlueTicketActivityEntity> queryList = HawkDBManager.getInstance()
				.query("from RedBlueTicketActivityEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			RedBlueTicketActivityEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		RedBlueTicketActivityEntity entity = new RedBlueTicketActivityEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isOpening(playerId)) {
			return;
		}
		
		Optional<RedBlueTicketActivityEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return;
		}
		RedBlueTicketActivityEntity entity = opDataEntity.get();
		syncActivityInfo(playerId, entity);
	}
	
	@Override
	public void onOpen() {
		Collection<String> onlinePlayerIds = getDataGeter().getOnlinePlayers();
		for (String playerId : onlinePlayerIds) {
			callBack(playerId, MsgId.RED_BLUE_TICKET, () -> {
				Optional<RedBlueTicketActivityEntity> opDataEntity = getPlayerDataEntity(playerId);
				if (!opDataEntity.isPresent()) {
					return;
				}
				RedBlueTicketActivityEntity entity = opDataEntity.get();
				syncActivityInfo(playerId, entity);
			});
		}
	}
	
	/**
	 * 翻牌
	 * @param playerId
	 * @param poolId   红方或蓝方
	 * @param ticketId 翻出的牌ID
	 */
	public Result<?> openTicket(String playerId, int poolId, int ticketId){
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		RedBlueTicketActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedBlueTicketActivityKVCfg.class);
		if (ticketId < cfg.getTicketIdMin() || ticketId > cfg.getTicketIdMax()) {
			return Result.fail(Status.SysError.PARAMS_INVALID_VALUE);
		}
		
		Optional<RedBlueTicketActivityEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		RedBlueTicketActivityEntity entity = opDataEntity.get();
		// 判断是否开始了（第一次是否开启）
		if (entity.getStarted() <= 0) {
			return Result.fail(Status.Error.RED_BLUE_NOT_START_VALUE);
		}
		
		if (poolId != 0 && poolId != 1) {
			poolId = 1;
		}
		
		Map<Integer, Integer> poolATicketMap = entity.getPoolATickets();
		Map<Integer, Integer> poolBTicketMap = entity.getPoolBTickets();
		boolean exist = poolId == 0 ? poolATicketMap.containsKey(ticketId) : poolBTicketMap.containsKey(ticketId);
		if (exist) {
			HawkLog.logPrintln("redblue ticket open repeated, playerId: {}, poolId: {}, ticketId: {}", playerId, poolId, ticketId);
			return Result.fail(Status.Error.RED_BLUE_TICKET_REPEATED_VALUE);
		}
		
		// 随机奖励
		int rewardId = this.randomReward(entity, poolId);
		if (rewardId == 0) {
			HawkLog.logPrintln("redblue ticket open error, playerId: {}, poolId: {}, rewardId: {}", playerId, poolId, rewardId);
			return Result.fail(Status.Error.RED_BLUE_TICKET_REPEATED_VALUE);
		}
		
		// 消耗
		RewardItem.Builder costItem = poolId == 0 ? cfg.getConsumeItemAList().get(poolATicketMap.size()) 
				: cfg.getConsumeItemBList().get(poolBTicketMap.size());
		if (costItem.getItemCount() > 0) {
			List<RewardItem.Builder> makeCost = new ArrayList<>();
			makeCost.add(costItem);
			boolean cost = this.getDataGeter().cost(playerId, makeCost, 1, Action.RED_BLUE_OPEN_TICKET_CONSUME, true);
			if (!cost) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
		}
		
		// 先发翻牌奖励，再发必得奖励（策划要求分开发，且先发翻牌奖励）
		if ((poolId == 0 && poolBTicketMap.containsValue(rewardId)) || (poolId == 1 && poolATicketMap.containsValue(rewardId))) {
			RedBlueTicketActivityRewardCfg rewardCfg = HawkConfigManager.getInstance().getConfigByKey(RedBlueTicketActivityRewardCfg.class, rewardId);
			@SuppressWarnings("deprecation")
			List<RewardItem.Builder> rewardItemBuilder = RewardHelper.toRewardItemList(rewardCfg.getItem());
			this.getDataGeter().takeReward(playerId, rewardItemBuilder, 1, Action.RED_BLUE_OPEN_TICKET_REWARD, true, RewardOrginType.RED_BLUE_TICKET_OPEN);
		}
		
		// 发必得奖励
		RewardItem.Builder rewardItem = poolId == 0 ? cfg.getRewardItemAList().get(poolATicketMap.size()) 
				: cfg.getRewardItemBList().get(poolBTicketMap.size());
		List<RewardItem.Builder> rewardList = new ArrayList<>();
		rewardList.add(rewardItem);
		// 发邮件
		this.getDataGeter().sendMail(playerId, MailId.RED_BLUE_OPEN_TICKET_AWARD,
				new Object[] { this.getActivityCfg().getActivityName() },
				new Object[] { this.getActivityCfg().getActivityName() }, 
				new Object[] {},
				rewardList, false); 
		
		if (poolId == 0) {
			entity.addTicketToPoolA(ticketId, rewardId);
		} else {
			entity.addTicketToPoolB(ticketId, rewardId);
		}
		
		// 将结果通知客户端
		syncActivityInfo(playerId, entity);
		
		// tlog -> termId, operType, pool, ticketId, rewardId, refreshTimes
		this.getDataGeter().logRedbludTicketFlow(playerId, this.getActivityTermId(), 1, poolId, ticketId, rewardId, entity.getPoolRefreshTimes());
		
		return Result.success();
	}

	/**
	 * 随机奖励
	 * @param entity
	 * @param stageCfg
	 * @return
	 */
	private int randomReward(RedBlueTicketActivityEntity entity, int poolId){
		Map<Integer, Integer> poolTicketMap = entity.getPoolATickets();
		if (poolId == 1) {
			poolTicketMap = entity.getPoolBTickets();
		}
		
		ConfigIterator<RedBlueTicketActivityRewardCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(RedBlueTicketActivityRewardCfg.class);
		Map<Integer, Integer> futureAwardWeightMap = new HashMap<Integer, Integer>();
		while (iterator.hasNext()) {
			RedBlueTicketActivityRewardCfg cfg = iterator.next();
			if (!poolTicketMap.containsValue(cfg.getRewardId())) {
				futureAwardWeightMap.put(cfg.getRewardId(), poolId == 0 ? cfg.getAweight() : cfg.getBweight());
			}
		}
		
		if (futureAwardWeightMap.isEmpty()) {
			return 0;
		}
		
		int rewardId = HawkRand.randomWeightObject(futureAwardWeightMap);
		return rewardId;
	}
	
	/**
	 * 刷新牌局
	 * @param playerId
	 */
	public Result<?> refreshTick(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<RedBlueTicketActivityEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		RedBlueTicketActivityEntity entity = opDataEntity.get();
		if (entity.getStarted() <= 0) {
			HawkLog.logPrintln("redblue ticket already reset, playerId: {}", playerId);
			return Result.success();
		}
		
		RedBlueTicketActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedBlueTicketActivityKVCfg.class);
		if (entity.getPoolRefreshTimes() >= cfg.getLimittimes()) {
			HawkLog.logPrintln("redblue ticket reset times limit, playerId: {}, times: {}", playerId, entity.getPoolRefreshTimes());
			return Result.fail(Status.Error.RED_BLUE_REFRESH_LIMIT_VALUE);
		}
		
		List<RewardItem.Builder> costItemList = cfg.getExpendItemList();
		int index = entity.getPoolRefreshTimes();
		index = Math.min(index, costItemList.size() - 1);
		index = Math.max(0, index);
		RewardItem.Builder item = costItemList.get(index);
		if (item.getItemCount() > 0) {
			List<RewardItem.Builder> costItem = new ArrayList<>();
			costItem.add(item);
			boolean cost = this.getDataGeter().cost(playerId, costItem, 1, Action.RED_BLUE_REFRESH_CONSUME, true);
			if (!cost) {
				return Result.fail(Status.Error.ITEM_NOT_ENOUGH_VALUE);
			}
		}
			
		entity.setStarted(0);
		entity.getPoolATickets().clear();
		entity.getPoolBTickets().clear();
		entity.setPoolRefreshTimes(entity.getPoolRefreshTimes() + 1);
		syncActivityInfo(playerId, entity);
		
		// tlog -> termId, operType, pool, ticketId, rewardId, refreshTimes
		this.getDataGeter().logRedbludTicketFlow(playerId, this.getActivityTermId(), 2, 0, 0, 0, entity.getPoolRefreshTimes());
		
		return Result.success();
	}
	
	/**
	 * 开启翻牌
	 * @param playerId
	 * @return
	 */
	public Result<?> openStart(String playerId) {
		if (!isOpening(playerId)) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		Optional<RedBlueTicketActivityEntity> opDataEntity = getPlayerDataEntity(playerId);
		if (!opDataEntity.isPresent()) {
			return Result.fail(Status.Error.ACTIVITY_NOT_OPEN_VALUE);
		}
		
		RedBlueTicketActivityEntity entity = opDataEntity.get();
		if (entity.getStarted() > 0) {
			return Result.success();
		}
		
		entity.setStarted(1);
		
		syncActivityInfo(playerId, entity);
		
		// tlog -> termId, operType, pool, ticketId, rewardId, refreshTimes
		this.getDataGeter().logRedbludTicketFlow(playerId, this.getActivityTermId(), 3, 0, 0, 0, entity.getPoolRefreshTimes());

		return Result.success();
	}
	
	/**
	 * 信息同步
	 * @param playerId
	 * @param entity
	 */
	public void syncActivityInfo(String playerId, RedBlueTicketActivityEntity entity){
		RedBlueTicketActivityKVCfg cfg = HawkConfigManager.getInstance().getKVInstance(RedBlueTicketActivityKVCfg.class);
		RedBlueTicketPoolInfoPB.Builder builder = RedBlueTicketPoolInfoPB.newBuilder();
		builder.setRemainRefreshTimes(cfg.getLimittimes() - entity.getPoolRefreshTimes());
		builder.setStarted(entity.getStarted());
		Map<Integer, Integer> poolATicketMap = entity.getPoolATickets();
		Map<Integer, Integer> poolBTicketMap = entity.getPoolBTickets();
		for (Entry<Integer, Integer> entry : poolATicketMap.entrySet()) {
			RedBlueTicketInfoPB.Builder ticket = RedBlueTicketInfoPB.newBuilder();
			ticket.setTicketId(entry.getKey());
			ticket.setRewardId(entry.getValue());
			builder.addRedPoolTicket(ticket);
		}
		
		for (Entry<Integer, Integer> entry : poolBTicketMap.entrySet()) {
			RedBlueTicketInfoPB.Builder ticket = RedBlueTicketInfoPB.newBuilder();
			ticket.setTicketId(entry.getKey());
			ticket.setRewardId(entry.getValue());
			builder.addBluePoolTicket(ticket);
		}
		
		pushToPlayer(playerId, HP.code.RED_BLUE_TICKET_INFO_PUSH_VALUE, builder);
	}

}
