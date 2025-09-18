package com.hawk.activity.type.impl.equipCraftsman;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.os.HawkRand;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.equipCraftsman.cfg.EquipCarftsmanGachaCfg;
import com.hawk.activity.type.impl.equipCraftsman.cfg.EquipCarftsmanKVCfg;
import com.hawk.activity.type.impl.equipCraftsman.entity.EquipCarftsmanEntity;
import com.hawk.activity.type.impl.equipCraftsman.item.EquipCarftsmanItem;
import com.hawk.game.protocol.Activity.EquipCraftsmanGachaReq;
import com.hawk.game.protocol.Activity.EquipCraftsmanGachaResp;
import com.hawk.game.protocol.Activity.EquipCraftsmanPageInfo;
import com.hawk.game.protocol.Activity.EquipEntryInfo;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardItem.Builder;
import com.hawk.log.Action;

/**
 * 装备工匠
 * 
 * @author Golden
 *
 */
public class EquipCarftsmanActivity extends ActivityBase {

	public EquipCarftsmanActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.EQUIP_CARFTSMAN_ACTIVITY;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		EquipCarftsmanActivity activity = new EquipCarftsmanActivity(config.getActivityId(), activityEntity);
		return activity;
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<EquipCarftsmanEntity> queryList = HawkDBManager.getInstance().query("from EquipCarftsmanEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			EquipCarftsmanEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		EquipCarftsmanEntity entity = new EquipCarftsmanEntity(playerId, termId);
		return entity;
	}

	/**
	 * 同步界面信息
	 * 
	 * returnMain:是否是返回主界面
	 * gotoSecond:是否前往二级界面
	 */
	public void syncPageInfo(String playerId) {
		
		// 玩家数据实体
		Optional<EquipCarftsmanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EquipCarftsmanEntity entity = opEntity.get();
		
		EquipCraftsmanPageInfo.Builder builder = EquipCraftsmanPageInfo.newBuilder();
		builder.setGachaTimes(entity.getGachaTimes());
		builder.setSecondPage(entity.getPage() > 0);
		for (Entry<String, EquipCarftsmanItem> boxEntry : entity.getAttrBoxMap().entrySet()) {
			EquipEntryInfo.Builder entryBuilder = EquipEntryInfo.newBuilder();
			entryBuilder.setUuid(boxEntry.getValue().getUuid());
			entryBuilder.setAttrCfgId(boxEntry.getValue().getGachaId());
			entryBuilder.setGachaTime(boxEntry.getValue().getGachaTime());
			builder.addEquipEntry(entryBuilder);
		}
		
		pushToPlayer(playerId, HP.code.EQUIP_CRAFTSMAN_PAGE_INFO_RESP_VALUE, builder);
	}
	
	/**
	 * 清除AB品质的属性条
	 */
	public void clearABAttr(String playerId) {
		
		// 玩家数据实体
		Optional<EquipCarftsmanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EquipCarftsmanEntity entity = opEntity.get();
		
		Set<String> removeSet = new HashSet<>();
		for (EquipCarftsmanItem boxEntry : entity.getAttrBoxMap().values()) {
			EquipCarftsmanGachaCfg cfg = HawkConfigManager.getInstance().getConfigByKey(EquipCarftsmanGachaCfg.class, boxEntry.getGachaId());
			if (cfg == null || !cfg.isPerfect()) {
				removeSet.add(boxEntry.getUuid());
			}
		}
		
		List<Integer> removeRewardCfgIds = new ArrayList<>();
		for (String removeAttr : removeSet) {
			if (!entity.getAttrBoxMap().containsKey(removeAttr)) {
				continue;
			}
			removeRewardCfgIds.add(entity.getAttrBoxMap().get(removeAttr).getGachaId());
			entity.removeAttr(removeAttr);
			
		}
		
		if (removeRewardCfgIds.isEmpty()) {
			return;
		}
		
		List<RewardItem.Builder> rewards = new ArrayList<>();
		for (Integer cfgId : removeRewardCfgIds) {
			EquipCarftsmanGachaCfg gachaCfg = HawkConfigManager.getInstance().getConfigByKey(EquipCarftsmanGachaCfg.class, cfgId);
			if (gachaCfg == null) {
				continue;
			}
			rewards.addAll(RewardHelper.toRewardItemList(gachaCfg.getReward()));
			
			getDataGeter().logEquipCarftsmanAttr(playerId, gachaCfg.getId(), gachaCfg.getAdditionId(), gachaCfg.getAttributeType(), gachaCfg.getAttributeValue(), 2, 0);
		}
		ActivityReward reward = new ActivityReward(rewards, Action.EQUIP_CARFTSMAN_GACHA_DELETE);
		reward.setAlert(true);
		postReward(entity.getPlayerId(), reward);
		
		syncPageInfo(playerId);
		
	}
	
	/**
	 * 抽
	 */
	public void gacha(String playerId, EquipCraftsmanGachaReq req) {
		
		if (!isOpening(playerId)) {
			return;
		}
		
		// 玩家数据实体
		Optional<EquipCarftsmanEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		EquipCarftsmanEntity entity = opEntity.get();
		
		// 锁定的兵种类型
		int armyLockType = req.getArmyLockType();
		// 锁定的属性类型
		int attrLockType = req.getAttrLockType();
		// 是否抽取十次
		int gachaTimes = req.getGachaTen() ? 10 : 1;
		
		// 抽取次数限制
		if (entity.getGachaTimes() + gachaTimes > EquipCarftsmanKVCfg.getInstance().getInheritNum()) {
			return;
		}
		
		// 需要道具的份数
		int needToolCount = gachaTimes;
		if (armyLockType > 0) {
			needToolCount *= EquipCarftsmanKVCfg.getInstance().getSoldierRate();
		}
		if (attrLockType > 0) {
			needToolCount *= EquipCarftsmanKVCfg.getInstance().getAttrRate();
		}
		
		RewardItem.Builder baseCost = RewardHelper.toRewardItem(EquipCarftsmanKVCfg.getInstance().getCostItem());
		// 背包里道具的数量
		int bagItemCount = getDataGeter().getItemNum(playerId, baseCost.getItemId());
		
		// 需要道具的份数
		int costItemCount = 0;
		// 需要钻石的份数
		int costDiamondCount = 0;
		
		if (bagItemCount >= needToolCount) {
			costItemCount = needToolCount;
		} else {
			costItemCount = bagItemCount;
			costDiamondCount = needToolCount - costItemCount;
		}
		
		// 道具消耗
		List<Builder> consume = new ArrayList<>();
		if (costItemCount > 0) {
			RewardItem.Builder itemCost = RewardHelper.toRewardItem(EquipCarftsmanKVCfg.getInstance().getCostItem());
			itemCost.setItemCount(itemCost.getItemCount() * costItemCount);
			consume.add(itemCost);
		}
		
		// 钻石消耗
		if (costDiamondCount > 0) {
			RewardItem.Builder diamondCost = RewardHelper.toRewardItem(EquipCarftsmanKVCfg.getInstance().getItemPrice());
			diamondCost.setItemCount(diamondCost.getItemCount() * costDiamondCount);
			consume.add(diamondCost);
		}
		
		// 筛选抽取的配置
		Map<EquipCarftsmanGachaCfg, Integer> gachaCfg = new HashMap<>();
		ConfigIterator<EquipCarftsmanGachaCfg> cfgIterator = HawkConfigManager.getInstance().getConfigIterator(EquipCarftsmanGachaCfg.class);
		while(cfgIterator.hasNext()) {
			EquipCarftsmanGachaCfg cfg = cfgIterator.next();
			if (armyLockType > 0 && cfg.getSoldierType() != armyLockType) {
				continue;
			}
			if (attrLockType > 0 && cfg.getAttributeType() != attrLockType) {
				continue;
			}
			gachaCfg.put(cfg, cfg.getCraftsmanWeight());
		}
		
		if (gachaCfg.isEmpty()) {
			return;
		}
		
		// 检测消耗
		boolean checkConsume = getDataGeter().consumeItems(playerId, consume, HP.code.EQUIP_CRAFTSMAN_GACHA_REQ_VALUE, Action.EQUIP_CARFTSMAN_GACHA);
		if (!checkConsume) {
			return;
		}
		
		EquipCraftsmanGachaResp.Builder builder = EquipCraftsmanGachaResp.newBuilder();
		
		// 添加抽取次数
		entity.setGachaTimes(entity.getGachaTimes() + gachaTimes);
		
		// 抽
		for (int i = 0; i < gachaTimes; i++) {
		
			// 真正的单次抽取
			EquipCarftsmanGachaCfg randCfg = HawkRand.randomWeightObject(gachaCfg);
			EquipCarftsmanItem item = EquipCarftsmanItem.valueOf(randCfg.getId());
			
			// 添加到词条库
			entity.addAttr(item.getUuid(), item);
			
			// 拼返回
			EquipEntryInfo.Builder attrInfo = EquipEntryInfo.newBuilder();
			attrInfo.setUuid(item.getUuid());
			attrInfo.setAttrCfgId(item.getGachaId());
			attrInfo.setGachaTime(item.getGachaTime());
			builder.addEquipEntry(attrInfo);
			
			logger.info("EquipCarftsman gacha, playerId:{}, gachaUUID:{}, gachaCfgId:{}, gachaTimes:{}, currentTimes:{}, armyLockType:{}, attrLockType:{}",
					playerId, item.getUuid(), item.getGachaId(), gachaTimes, i + 1, armyLockType, attrLockType);
			
			getDataGeter().logEquipCarftsmanAttr(playerId, randCfg.getId(), randCfg.getAdditionId(), randCfg.getAttributeType(), randCfg.getAttributeValue(), 1, 0);
		}
		
		// 推界面信息
		syncPageInfo(playerId);
		
		// 推返回
		pushToPlayer(playerId, HP.code.EQUIP_CRAFTSMAN_GACHA_RESP_VALUE, builder);
		
		
		// 添加额外奖励
		int extCnt = gachaTimes;
		List<RewardItem.Builder> extRewards = RewardHelper.toRewardItemList(EquipCarftsmanKVCfg.getInstance().getExtReward());
		if (!extRewards.isEmpty()) {
			for (RewardItem.Builder extReward : extRewards) {
				extReward.setItemCount(extReward.getItemCount() * extCnt);
			}
			ActivityReward reward = new ActivityReward(extRewards, Action.EQUIP_CARFTSMAN_GACHA_EXTRA);
			reward.setAlert(false);
			postReward(playerId, reward);
		}
		
	}
}
