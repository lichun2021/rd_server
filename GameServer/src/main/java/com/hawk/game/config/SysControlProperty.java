package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;

/**
 * 系统控制配置
 * @author golden
 *
 */
@HawkConfigManager.KVResource(file = "cfg/sysControl.cfg")
public class SysControlProperty extends HawkConfigBase {
	/**
	 * 是否开启邮件发送
	 */
	protected final boolean mailEnable;
	/**
	 * 是否开启邮件奖励领取
	 */
	protected final boolean mailRewardEnable;
	/**
	 * 是否开启全服聊天
	 */
	protected final boolean worldChatEnable;
	/**
	 * 是否开启联盟聊天
	 */
	protected final boolean guildChatEnable;
	/**
	 * 是否开启支付
	 */
	protected final boolean payEnable;
	/**
	 * 是否开启商城直购
	 */
	protected final boolean shopEnable;
	/**
	 * 失效奖励
	 */
	protected final String disableAwards;
	
	protected final boolean premiumGiftEnable;
	
	protected final boolean salesGoodsEnable;
	
	protected final boolean vipShopEnable;
	
	protected final boolean vipGiftEnable;
	
	protected final boolean independentArmsEnable;
	
	protected final boolean alliedDepotEnable;
	
	protected final boolean dailyTaskEnable;
	
	protected final boolean itemUseEnable;
	
	protected final boolean itemGetEnable;
	
	protected final boolean diamondUseEnable;
	
	protected final boolean goldUseEnable;
	
	protected final boolean skillEnable;
	
	protected final boolean activityEnable;
	
	protected final boolean pushGiftEnable;
	protected final boolean grabResEnable;
	
	protected final boolean gatherResInCity;
	
	protected final String zeroEarningSystem;
	
	private List<Integer> zeroEarningSystemList;
	
	/**
	 * 失效奖励typeList
	 */
	private List<ItemInfo> disableAwardsList;
	
	/**
	 * 实例
	 */
	private static SysControlProperty instance = null;
	
	/**
	 * 获取实例
	 * @return
	 */
	public static SysControlProperty getInstance() {
		return instance;
	}
	
	
	/**
	 * 构造
	 */
	public SysControlProperty() {
		instance = this;
		mailEnable = true;
		mailRewardEnable = true;
		worldChatEnable = true;
		guildChatEnable = true;
		payEnable = true;
		shopEnable = true;
		disableAwards = "";
		premiumGiftEnable = true;
		salesGoodsEnable = true;
		vipShopEnable = true;
		vipGiftEnable = true;
		independentArmsEnable = true;
		alliedDepotEnable = true;
		dailyTaskEnable = true;
		itemUseEnable = true;
		itemGetEnable = true;
		diamondUseEnable = true;
		goldUseEnable = true;
		skillEnable = true;
		activityEnable = true;
		pushGiftEnable = true;
		grabResEnable = true;
		gatherResInCity = true;
		zeroEarningSystem = "";
	}

	
	public boolean isMailEnable() {
		return mailEnable;
	}

	public boolean isMailRewardEnable() {
		return mailRewardEnable;
	}

	public boolean isWorldChatEnable() {
		return worldChatEnable;
	}

	public boolean isGuildChatEnable() {
		return guildChatEnable;
	}

	public boolean isPayEnable() {
		return payEnable;
	}

	public boolean isShopEnable() {
		return shopEnable;
	}
	
	public boolean isPremiumGiftEnable() {
		return premiumGiftEnable;
	}

	public boolean isSalesGoodsEnable() {
		return salesGoodsEnable;
	}

	public boolean isVipShopEnable() {
		return vipShopEnable;
	}

	public boolean isVipGiftEnable() {
		return vipGiftEnable;
	}

	public boolean isIndependentArmsEnable() {
		return independentArmsEnable;
	}

	public boolean isAlliedDepotEnable() {
		return alliedDepotEnable;
	}

	public boolean isDailyTaskEnable() {
		return dailyTaskEnable;
	}

	public boolean isItemUseEnable() {
		return itemUseEnable;
	}

	public boolean isItemGetEnable() {
		return itemGetEnable;
	}

	public boolean isDiamondUseEnable() {
		return diamondUseEnable;
	}

	public boolean isGoldUseEnable() {
		return goldUseEnable;
	}

	public boolean isSkillEnable() {
		return skillEnable;
	}

	public boolean isActivityEnable() {
		return activityEnable;
	}

	public boolean isGrabResEnable() {
		return grabResEnable;
	}

	public boolean isGatherResInCity() {
		return gatherResInCity;
	}
	
	public boolean isUnderZeroEarningControl(int protocol) {
		return zeroEarningSystemList.contains(protocol);
	}


	/**
	 * 是否是失效的奖励
	 * @param awardType
	 * @return
	 */
	public boolean isDisableAward(int awardType, int awardId) {
		for (ItemInfo disableAward: disableAwardsList) {
			if (disableAward.getType() == awardType && disableAward.getItemId() == awardId) {
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	protected boolean assemble() {
		disableAwardsList = new ArrayList<ItemInfo>();
		if (!HawkOSOperator.isEmptyString(disableAwards)) {
			String[] disableAward = disableAwards.split(",");
			for (String award : disableAward) {
				String[] awardArr = award.split("_");
				ItemInfo item = new ItemInfo(Integer.parseInt(awardArr[0]), Integer.parseInt(awardArr[1]), 0);
				disableAwardsList.add(item);
			}
		}
		
		zeroEarningSystemList = new ArrayList<Integer>();
		if (!HawkOSOperator.isEmptyString(zeroEarningSystem)) {
			String[] protocols = zeroEarningSystem.split(",");
			for (String protocol : protocols) {
				zeroEarningSystemList.add(Integer.valueOf(protocol.trim()));
			}
		}
		
		return true;
	}


	public boolean isPushGiftEnable() {
		return pushGiftEnable;
	}
}
