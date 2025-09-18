package com.hawk.activity.type.impl.seaTreasure;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.seaTreasure.cfg.SeaTreasureAdvancedAwardCfg;
import com.hawk.activity.type.impl.seaTreasure.cfg.SeaTreasureAwardPoolCfg;
import com.hawk.activity.type.impl.seaTreasure.cfg.SeaTreasureKVCfg;
import com.hawk.activity.type.impl.seaTreasure.entity.SeaTreasureEntity;
import com.hawk.activity.type.impl.seaTreasure.item.SeaTreasureBoxItem;
import com.hawk.activity.type.impl.seaTreasure.item.SeaTreasureReceiveItem;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureBoxInfo;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureOpenResp;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasurePageInfo;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureReceiveAdvanced;
import com.hawk.game.protocol.ActivitySeaTreasure.SeaTreasureReceiveCommon;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

/**
 * 秘海珍寻
 * @author Golden
 *
 */
public class SeaTreasureActivity extends ActivityBase {

	/**
	 * 构造方法
	 * 
	 * @param activityId
	 * @param activityEntity
	 */
	public SeaTreasureActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	/**
	 * 获取活动类型
	 */
	@Override
	public ActivityType getActivityType() {
		return ActivityType.SEA_TREASURE;
	}

	/**
	 * 新建实例
	 */
	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		SeaTreasureActivity activity = new SeaTreasureActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	/**
	 * 从DB加载
	 */
	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<SeaTreasureEntity> queryList = HawkDBManager.getInstance().query("from SeaTreasureEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			SeaTreasureEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	/**
	 * 创建DB数据实体
	 */
	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		SeaTreasureEntity entity = new SeaTreasureEntity(playerId, termId);
		return entity;
	}
	
	@Override
	public void onPlayerLogin(String playerId) {
		if (!isHidden(playerId)) {
			syncPageInfo(playerId);
		}
	}
	
	/**
	 * 推界面信息
	 * @param playerId
	 */
	public void syncPageInfo(String playerId) {
		// 数据检测
		doCheck(playerId);
		
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		SeaTreasurePageInfo.Builder builder = SeaTreasurePageInfo.newBuilder();
		builder.setTreasureTimes(entity.getFindTimes());
		builder.setToolBuyTimes(entity.getToolBuyTimes());
		for (SeaTreasureBoxItem boxInfo : entity.getBoxInfoMap().values()) {
			SeaTreasureBoxInfo.Builder boxInfoBuilder = SeaTreasureBoxInfo.newBuilder();
			boxInfoBuilder.setGrid(boxInfo.getGrid());
			boxInfoBuilder.setAdvancedRewardId(boxInfo.getAdvanceRewardId());
			boxInfoBuilder.setStartTime(boxInfo.getStartTime());
			boxInfoBuilder.setStart(boxInfo.getStartTime() > 0L);
			builder.addBoxInfos(boxInfoBuilder);
		}
		for (Entry<Integer, Integer> receiveInfo : entity.getCommonReceiveInfos().entrySet()) {
			SeaTreasureReceiveCommon.Builder receiveInfoBuilder = SeaTreasureReceiveCommon.newBuilder();
			receiveInfoBuilder.setId(receiveInfo.getKey());
			receiveInfoBuilder.setCount(receiveInfo.getValue());
			builder.addReceiveCommon(receiveInfoBuilder);
		}
		for (SeaTreasureReceiveItem receiveInfo : entity.getAdvReceiveInfosList()) {
			SeaTreasureReceiveAdvanced.Builder receiveInfoBuilder = SeaTreasureReceiveAdvanced.newBuilder();
			receiveInfoBuilder.setAdvancedRewardId(receiveInfo.getAdvanceRewardId());
			receiveInfoBuilder.setReceiveTime(receiveInfo.getReceiveTime());
			receiveInfoBuilder.setRemainTimes(receiveInfo.getRemainTimes());
			builder.addReceiveAdvanced(receiveInfoBuilder);
		}
		pushToPlayer(playerId, HP.code2.ACTIVITY_SEA_TREASURE_PAGEINFO_RESP_VALUE, builder);
	}
	
	/**
	 * 寻宝
	 * @param playerId
	 */
	public void doSearch(String playerId) {
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 每日寻宝次数限制
		if (entity.getFindTimes() >= SeaTreasureKVCfg.getInstance().getTreasureNumber()) {
			return;
		}
		
		// 宝箱栏位满了
		if (entity.getBoxInfoMap().size() >= SeaTreasureKVCfg.getInstance().getBoxNumeber()) {
			return;
		}
		
		// 随机奖励
		ConfigIterator<SeaTreasureAwardPoolCfg> poolCfgIter = HawkConfigManager.getInstance().getConfigIterator(SeaTreasureAwardPoolCfg.class);
		List<SeaTreasureAwardPoolCfg> poolList = poolCfgIter.stream()
				.filter(r -> entity.getReceiveTimesMap().getOrDefault(r.getRewardIdId(), 0) < r.getTimesLimit())
				.collect(Collectors.toList());
		SeaTreasureAwardPoolCfg poolCfg = HawkRand.randomWeightObject(poolList);
		
		// 添加寻宝次数和获取次数
		entity.addFindTimes();
		entity.addReceiveTimes(poolCfg.getRewardIdId());
		
		// 发奖 宝箱奖励/普通奖励
		if (poolCfg.isAdvancedReward()) {
			ConfigIterator<SeaTreasureAdvancedAwardCfg> advCfgIter = HawkConfigManager.getInstance().getConfigIterator(SeaTreasureAdvancedAwardCfg.class);
			List<SeaTreasureAdvancedAwardCfg> advCfgList = advCfgIter.stream().filter(r -> r.getAdvancedType() == poolCfg.getAdvancedType()).collect(Collectors.toList());
			SeaTreasureAdvancedAwardCfg advCfg = HawkRand.randomWeightObject(advCfgList);
			
			// 添加高级奖励获取次数
			SeaTreasureReceiveItem receiveItem = new SeaTreasureReceiveItem();
			int remainTimes = poolCfg.getTimesLimit() - entity.getReceiveTimes(poolCfg.getRewardIdId());
			receiveItem.setAdvanceRewardId(advCfg.getAdvancedRewardId());
			receiveItem.setReceiveTime(HawkTime.getMillisecond());
			receiveItem.setRemainTimes(remainTimes);
			entity.addAdvReceiveInfo(receiveItem);
			
			SeaTreasureBoxItem boxItem = new SeaTreasureBoxItem();
			int grid = 0;
			for (int i = 1; i <= SeaTreasureKVCfg.getInstance().getBoxNumeber(); i++) {
				SeaTreasureBoxItem boxInfo = entity.getBoxInfo(i);
				if (boxInfo == null) {
					grid = i;
					break;
				}
			}
			if (grid > 0) {
				boxItem.setGrid(grid);
				boxItem.setAdvanceRewardId(advCfg.getAdvancedRewardId());
				boxItem.setCreateTime(HawkTime.getMillisecond());
				entity.addBoxInfo(boxItem);
			}
		} else {
			// 添加普通奖励获取次数
			entity.addCommonReceiveInfos(poolCfg.getRewardIdId());
			// 发奖
			ActivityReward reward = new ActivityReward(poolCfg.getReward(), Action.SEA_TREASURE_COMMON);
			reward.setOrginType(null, getActivityId());
			postReward(playerId, reward, false);
		}
		
		// 推送界面信息
		syncPageInfo(playerId);
		
		// 返回
		SeaTreasureOpenResp.Builder builder = SeaTreasureOpenResp.newBuilder();
		builder.setReawrdId(poolCfg.getRewardIdId());
		pushToPlayer(playerId, HP.code2.ACTIVITY_SEA_TREASURE_DO_RESP_VALUE, builder);
	}
	
	/**
	 * 开启宝箱
	 * @param playerId
	 * @param grid格子id
	 */
	public void doOpen(String playerId, int grid) {
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 同一时间只能开启一个
		for (SeaTreasureBoxItem boxInfo : entity.getBoxInfoMap().values()) {
			if (boxInfo.getStartTime() > 0L) {
				return;
			}
		}
		
		// 宝箱信息
		SeaTreasureBoxItem boxInfo = entity.getBoxInfo(grid);
		if (boxInfo == null) {
			syncPageInfo(playerId);
			return;
		}
		
		boxInfo.setStartTime(HawkTime.getMillisecond());
		entity.notifyUpdate();
		syncPageInfo(playerId);
	}
	
	/**
	 * 收取宝箱
	 * @param playerId
	 * @param grid格子id
	 */
	public void doReceive(String playerId, int grid) {
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 宝箱信息
		SeaTreasureBoxItem boxInfo = entity.getBoxInfo(grid);
		if (boxInfo == null) {
			syncPageInfo(playerId);
			return;
		}
		
		// 收取
		Set<Integer> gridList = new HashSet<>();
		gridList.add(grid);
		doReceive(playerId, gridList, HP.code2.ACTIVITY_SEA_TREASURE_RECEIVE_VALUE);
		
		// 刷界面
		syncPageInfo(playerId);
	}
	
	/**
	 * 收取所有宝箱
	 * @param playerId
	 */
	public void doReceiveAll(String playerId) {
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 收取宝箱
		doReceive(playerId, entity.getBoxInfoMap().keySet(), HP.code2.ACTIVITY_SEA_TREASURE_RECEIVE_ALL_VALUE);
		
		// 刷界面
		syncPageInfo(playerId);
	}
	
	/**
	 * 收取宝箱
	 * @param playerId
	 * @param gridList
	 */
	private void doReceive(String playerId, Set<Integer> gridList, int protoType) {
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 获取所有宝箱
		List<SeaTreasureBoxItem> boxList = new ArrayList<>();
		for (Integer grid : gridList) {
			SeaTreasureBoxItem box = entity.getBoxInfo(grid);
			boxList.add(box);
		}
		
		// 需要加速时间
		long remainTime = 0;
		for (SeaTreasureBoxItem box : boxList) {
			SeaTreasureAdvancedAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeaTreasureAdvancedAwardCfg.class, box.getAdvanceRewardId());
			if (box.getStartTime() > 0) {
				remainTime += Math.max(0, box.getStartTime() + cfg.getOpenTime() - HawkTime.getMillisecond());
			} else {
				remainTime += cfg.getOpenTime();
			}
		}
		
		if (remainTime > 0) {
			// 需要道具数量
			long speedTime = SeaTreasureKVCfg.getInstance().getAccelerateTime();
			int toolCount = (int)((remainTime / speedTime) + 1);
			if (remainTime % speedTime == 0) {
				toolCount = (int)(remainTime / speedTime);	
			}
			
			// 消耗
			List<RewardItem.Builder> costList = SeaTreasureKVCfg.getInstance().getAccelerateItemId();
			for (RewardItem.Builder cost : costList) {
				long count = cost.getItemCount();
				cost.setItemCount(count * toolCount);
			}
			boolean consume = getDataGeter().consumeItems(playerId, costList, HP.code2.ACTIVITY_SEA_TREASURE_RECEIVE_VALUE, Action.SEA_TREASURE_BOX);
			if (!consume) {
				sendErrorAndBreak(playerId, protoType, Status.Error.ITEM_NOT_ENOUGH_VALUE);
				return;
			}
		}
		
		List<Integer> rmList = new ArrayList<>();
		rmList.addAll(gridList);
		
		// 删除宝箱
		for (int grid : rmList) {
			entity.removeBoxInfo(grid);
		}
		
		// 奖励
		List<RewardItem.Builder> reaward = new ArrayList<>();
		for (SeaTreasureBoxItem box : boxList) {
			SeaTreasureAdvancedAwardCfg cfg = HawkConfigManager.getInstance().getConfigByKey(SeaTreasureAdvancedAwardCfg.class, box.getAdvanceRewardId());
			reaward.addAll(cfg.getReward());
		}
		// 合并奖励
		List<RewardItem.Builder> mergeReward = RewardHelper.mergeRewardItem(reaward);
		
		// 发奖
		ActivityReward reward = new ActivityReward(mergeReward, Action.SEA_TREASURE_ADV);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(true);
		postReward(playerId, reward, false);
	}
	
	/**
	 * 购买道具
	 * @param playerId
	 * @param grid
	 */
	public void doBuyItem(String playerId, int count, boolean notice) {
		// 玩家活动数据
		SeaTreasureEntity entity = (SeaTreasureEntity) getPlayerDataEntity(playerId).get();
		
		// 购买数量达到上限制
		if (entity.getToolBuyTimes() + count > SeaTreasureKVCfg.getInstance().getAccelerateNumber()) {
			return;
		}
		
		// 消耗
		List<RewardItem.Builder> costList = SeaTreasureKVCfg.getInstance().getCost();
		for (RewardItem.Builder cost : costList) {
			cost.setItemCount(cost.getItemCount() * count);
		}
		boolean consume = getDataGeter().consumeItems(playerId, costList, HP.code2.ACTIVITY_SEA_TREASURE_BUY_ITEM_VALUE, Action.SEA_TREASURE_BUY_ITEM);
		if (!consume) {
			return;
		}
		
		// 发道具
		List<RewardItem.Builder> rewardList = SeaTreasureKVCfg.getInstance().getAccelerateItemId();
		for (RewardItem.Builder reward : rewardList) {
			reward.setItemCount(reward.getItemCount() * count);
		}
		ActivityReward reward = new ActivityReward(rewardList, Action.SEA_TREASURE_BUY_ITEM);
		reward.setOrginType(null, getActivityId());
		reward.setAlert(notice);
		postReward(playerId, reward, false);
		
		// 添加购买次数
		entity.addToolBuyTimes(count);
		
		// 同步界面信息
		if (notice) {
			syncPageInfo(playerId);
		}
	}
	
	/**
	 * 检测
	 * @param playerId
	 * @return
	 */
	public boolean doCheck(String playerId) {
		Optional<SeaTreasureEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		SeaTreasureEntity entity = opEntity.get();
		
		// 清除每日数据
		int dayMark = HawkTime.getYearDay();
		if (dayMark != entity.getDayMark()) {
			entity.dailyClear();
			entity.setDayMark(dayMark);
		}
		return true;
	}
	
}
