package com.hawk.game.service;

import org.hawk.config.HawkConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hawk.game.config.AwardCfg;
import com.hawk.game.config.BattleSoldierCfg;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;
import com.hawk.game.player.Player;
import com.hawk.game.protocol.Const;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

/**
 * 奖励操作类
 * @author lating
 *
 */
public class AwardService {
	/**
	 * 日志对象
	 */
	static final Logger logger = LoggerFactory.getLogger("Server");
	
	private static AwardService instance = null;

	public static AwardService getInstance() {
		if (instance == null) {
			instance = new AwardService();
		}
		return instance;
	}

	/**
	 * 获取奖励内容，物品堆叠
	 * @param player
	 * @param awardId
	 * @param itemCount
	 * @param action
	 * @param popup
	 * @return
	 */
	public AwardItems takeAward(Player player, int awardId, int itemCount, Action action, boolean popup, int... orginArgs){
		AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(awardCfg == null){
			logger.info("unkown award config, playerId: {}, awardId: {}", player.getId(), awardId);
			return null;
		}

		int index = 0;
		AwardItems[] awardItemArray = new AwardItems[itemCount];
		AwardItems awardItem = awardCfg.getRandomAward();
		awardItemArray[index++] = awardItem;
		itemCount--;
		while(itemCount > 0) {
			AwardItems items = awardCfg.getRandomAward();
			if(items != null) {
				awardItemArray[index++] = items;
			}
			itemCount--;
		}

		if (orginArgs.length > 1 && orginArgs[1] == ConstProperty.getInstance().getDailyGiftBoxItemId()) {
			dailyGiftSpecialBox(player, awardItemArray);
		}

		AwardItems awardItems = AwardItems.valueOf();
		for (AwardItems items : awardItemArray) {
			awardItems.appendAward(items);
		}

		if(!popup){
			awardItems.rewardTakeAffectAndPush(player, action);
			return awardItems;
		}

		if (orginArgs.length > 1) {
			awardItems.rewardTakeAffectAndPush(player, action, true, RewardOrginType.valueOf(orginArgs[0]), orginArgs[1]);
		} else {
			awardItems.rewardTakeAffectAndPush(player, action, true);
		}

		return awardItems;
	}

	/**
	 * 获取奖励内容，物品不堆叠
	 * @param player
	 * @param awardId
	 * @param itemCount
	 * @param action
	 * @param popup
	 * @return
	 */
	public AwardItems takeRewardWithFixItem(Player player, String fixItem, int awardId, int itemCount,
											Action action, boolean popup, int... orginArgs){
		AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
		if(awardCfg == null){
			logger.info("unkown award config, playerId: {}, awardId: {}", player.getId(), awardId);
			return null;
		}

		int fixItemCount = itemCount;

		int index = 0;
		AwardItems[] awardItemArray = new AwardItems[itemCount];
		AwardItems awardItem = awardCfg.getRandomAward();
		awardItemArray[index++] = awardItem;
		itemCount--;
		while(itemCount > 0) {
			AwardItems items = awardCfg.getRandomAward();
			if(items != null) {
				awardItemArray[index++] = items;
			}
			itemCount--;
		}

		if (orginArgs.length > 1 && orginArgs[1] == ConstProperty.getInstance().getDailyGiftBoxItemId()) {
			dailyGiftSpecialBox(player, awardItemArray);
		}

		//处理固定物品奖励，固定物品堆叠，但不能和后面的随机物品中相同的进行堆叠
		AwardItems awardItems = AwardItems.valueOf(fixItem);
		for(int i=0; i<fixItemCount-1; ++i){
			AwardItems awardItemTmp = AwardItems.valueOf(fixItem);
			awardItems.appendAward(awardItemTmp);
		}

		//整理随机奖励物品，这里相同物品不堆叠，按每次随机单独
		for (AwardItems items : awardItemArray) {
			awardItems.appendAwardDoNotStacked(items);
		}

		if(!popup){
			awardItems.rewardTakeAffectAndPush(player, action);
			return awardItems;
		}

		if (orginArgs.length > 1) {
			awardItems.rewardTakeAffectAndPush(player, action, true, RewardOrginType.valueOf(orginArgs[0]), orginArgs[1]);
		} else {
			awardItems.rewardTakeAffectAndPush(player, action, true);
		}

		return awardItems;
	}

	/**
	 * 每日必买卡维丽珍藏伪随机掉落
	 * 
	 * @param times
	 */
	private void dailyGiftSpecialBox(Player player, AwardItems[] itemsArray) {
		CustomDataEntity entity = player.getData().getCustomDataEntity(GsConst.DAILY_GIFT_BOX);
		if (entity == null) {
			entity = player.getData().createCustomDataEntity(GsConst.DAILY_GIFT_BOX, 0, "");
		}
		
		int times = ConstProperty.getInstance().getDailyGiftBoxOpenTimes();
		int itemId = ConstProperty.getInstance().getDailyGiftMustRewardItemId();
		int awardId = ConstProperty.getInstance().getDailyGiftRandomAwardId();
		for (int i = 0; i < itemsArray.length; i++) {
			if (entity.getValue() + 1 == times) {
				entity.setValue(0);
				AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, awardId);
				itemsArray[i] = awardCfg.getRandomAward();
				continue;
			}

			AwardItems items = itemsArray[i];
			ItemInfo itemInfo = items.getItem(ItemType.TOOL_VALUE * GsConst.ITEM_TYPE_BASE, itemId);
			if (itemInfo.getCount() > 0) {
				entity.setValue(0);
			} else {
				entity.setValue(entity.getValue() + 1);
			}
		}
	}
	
	/**
	 * 只是取出奖励
	 * @param rewardId
	 * @param itemCount
	 * @return
	 */
	public AwardItems takeAward(int rewardId, int itemCount) {
		AwardCfg awardCfg =  HawkConfigManager.getInstance().getConfigByKey(AwardCfg.class, rewardId);
		AwardItems awardItems = awardCfg.getRandomAward();
		itemCount--;
		while(itemCount > 0) {
			AwardItems items = awardCfg.getRandomAward();
			if(items != null) {
				awardItems.appendAward(items);
			}
			itemCount--;
		}
		
		return awardItems;
	}
	
	/**
	 * 奖励士兵
	 */
	public void awardSoldier(Player player, int armyId, int count) {
		BattleSoldierCfg armyCfg = HawkConfigManager.getInstance().getConfigByKey(BattleSoldierCfg.class, armyId);
		if(armyCfg == null || count <= 0){
			logger.info("soldier award failed, params invalid, playerId: {}, armyId: {}, count: {}", player.getId(), armyId, count);
			return;
		}
		
		AwardItems awardItems = AwardItems.valueOf();
		awardItems.addItem(Const.ItemType.SOLDIER_VALUE, armyCfg.getId(), count);
		awardItems.rewardTakeAffectAndPush(player, Action.SOLDIER_REWARD);
	}
	
}
