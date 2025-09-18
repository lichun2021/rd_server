package com.hawk.game.config;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.game.item.ItemInfo;

@HawkConfigManager.KVResource(file = "xml/guardian_const.xml")
public class GuardianConstConfig extends HawkConfigBase {
	/**
	 * 邀请需要的道具
	 */
	private final String inviteCost;
	/**
	 * 邀请的有效时间
	 */
	private final int validityTime;
	/**
	 * 守护帮助功能解锁需求好感度
	 */
	private final int needGuardianValue;
	/**
	 * 解除守护关系，保留好感度万分比
	 */
	private final int keepRate;
	/**
	 * 灭火的价格
	 */
	private final String putOutFirePrice;
	/**
	 * 邀请需要的道具.
	 */
	private  List<ItemInfo> inviteCostList;
	/**
	 * 灭火的价格
	 */
	private  List<ItemInfo> putOutFirePriceList;
	/**
	 * 最多排多少人.
	 */
	private final int rankMaxNum;
	/**
	 * 榜上显示多少人.
	 */
	private final int rankCondition;
	/**
	 * 周期时间.
	 */
	private final long perioTime;
	
	private static GuardianConstConfig instance = null;
	
	public static GuardianConstConfig getInstance() {
		return instance;
	}
	
	public GuardianConstConfig() {
		instance = this;
		this.inviteCost = "";
		this.validityTime = 0;
		this.needGuardianValue = 0;
		this.keepRate = 0;
		this.putOutFirePrice = "";
		this.rankMaxNum = 0;
		this.rankCondition = 0;
		this.perioTime = 300000;
	}
	public int getValidityTime() {
		return validityTime;
	}
	public int getNeedGuardianValue() {
		return needGuardianValue;
	}
	public int getKeepRate() {
		return keepRate;
	}
	public List<ItemInfo> getInviteCostList() {
		return inviteCostList;
	}
	public List<ItemInfo> getPutOutFirePriceList() {
		return putOutFirePriceList;
	}
	
	@Override
	public boolean assemble() {
		this.inviteCostList = ItemInfo.valueListOf(inviteCost);
		this.putOutFirePriceList = ItemInfo.valueListOf(putOutFirePrice);
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		/*for (ItemInfo itemInfo : inviteCostList) {
			RewardHelper.checkRewardItem(itemInfo.getType(), itemInfo.getItemId(), itemInfo.getCount());
		}
		
		putOutFirePriceList.stream().forEach((item)->{
			RewardHelper.checkRewardItem(item.getType(), item.getItemId(), item.getCount());
		});*/
		
		return true;
	}

	public int getRankMaxNum() {
		return rankMaxNum;
	}

	public int getRankCondition() {
		return rankCondition;
	}

	public long getPerioTime() {
		return perioTime ;
	}
}
