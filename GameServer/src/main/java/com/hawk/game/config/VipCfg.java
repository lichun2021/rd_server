package com.hawk.game.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.serialize.string.SerializeHelper;

/**
 * VIP功能配置
 * 
 * @author
 *
 */
@HawkConfigManager.XmlResource(file = "xml/vip.xml")
public class VipCfg extends HawkConfigBase {
	@Id
	// vip等级
	protected final int level;
	// vip等级升到此等级需要累计的vip经验值
	protected final int vipExp;
	// vip礼包
	protected final String vipGift;
	// 恢复体力上限提升，百分比数据
	protected final int recoverEnergyLimitAdd;
	// 作用号加成数据id_value结构
	protected final String effects;
	// 每日购买体力次数上限
	protected final int buyEnergyTimes;
	// 城建队列添加数量
	protected final int buildingQueueAdd;
	// 英雄队列添加数量
	protected final int heroQueueAdd;
	// 重置资源捐献次数
	protected final int donateResetTimes;
	// 解锁英雄
	protected final String unlockHero;
	// 是否开启天赋2路线
	protected final int unlockTalentLine3;
	

	protected final int unlockTalentLine4;

	protected final int unlockTalentLine5;
	
	// 是否开启第二建造队列
	protected final int unlockBuildQueue2;
	// 免费切换天赋
	protected final int freeToExchangeTalent;
	// 编队组数
	protected final int troopTeamNum;
	// VIP等级专属礼包
	protected final String vipExclusiveBox;
	// VIP等级福利礼包
	protected final String vipBenefitBox;
	// 专属礼包原价
	protected final String vipExclusiveBefore;
	// 福利礼包原价
	protected final String vipBenefitNameBefore;
	// 专属礼包现价
	protected final String vipExclusiveNow;
	// 一键领取礼物
	protected final int drawAllGift;
	// 可开启自动打野的行军队列数
	protected final int autoFightQueue;
	// 编队数量
	protected final int formation;
	// 行军表情免费使用次数
	protected final int emojiFreeTimes;
	
	protected final String atkAttr;
	protected final String hpAttr;
	
	/**
	 * 最大等级
	 */
	private static int maxLevel = 0;
	/**
	 * 最大vip经验值
	 */
	private static int maxVipExp = 0;
	/**
	 * vip作用号数据
	 */
	Map<Integer, Integer> effMap = new HashMap<Integer, Integer>();
	/**
	 * vip礼包数据
	 */
	List<ItemInfo> vipGiftItems;
	/**
	 * vip专属礼包数据
	 */
	List<ItemInfo> vipExclusiveItems;
	/**
	 * vip福利礼包数据
	 */
	List<ItemInfo> vipBenefitItems;

	ItemInfo vipExclusiveNowPrice;

	public VipCfg() {
		level = 0;
		vipExp = 0;
		vipGift = "";
		recoverEnergyLimitAdd = 0;
		effects = "";
		buyEnergyTimes = 10;
		buildingQueueAdd = 0;
		heroQueueAdd = 0;
		donateResetTimes = 0;
		unlockHero = "";
		unlockTalentLine3 = 0;
		unlockTalentLine4 = 0;
		unlockTalentLine5 = 0;
		unlockBuildQueue2 = 0;
		freeToExchangeTalent = 0;
		troopTeamNum = 0;
		vipExclusiveBox = "";
		vipBenefitBox = "";
		vipExclusiveBefore = "";
		vipBenefitNameBefore = "";
		vipExclusiveNow = "";
		drawAllGift = 0;
		autoFightQueue = 0;
		formation = 4;
		emojiFreeTimes = 0;
		atkAttr = "";
		hpAttr = "";
	}

	public int getUnlockBuildQueue2() {
		return unlockBuildQueue2;
	}

	public int getLevel() {
		return level;
	}

	public int getVipExp() {
		return vipExp;
	}

	public int getBuyEnergyTimes() {
		return buyEnergyTimes;
	}

	public String getVipGift() {
		return vipGift;
	}

	public int getRecoverEnergyLimitAdd() {
		return recoverEnergyLimitAdd;
	}

	public int getBuildingQueueAdd() {
		return buildingQueueAdd;
	}

	public int getHeroQueueAdd() {
		return heroQueueAdd;
	}

	public int getDonateResetTimes() {
		return donateResetTimes;
	}

	public String getUnlockHero() {
		return unlockHero;
	}

	public int getUnlockTalentLine3() {
		return unlockTalentLine3;
	}

	public int getFreeToExchangeTalent() {
		return freeToExchangeTalent;
	}

	public int getTroopTeamNum() {
		return troopTeamNum;
	}

	public ItemInfo getVipExclusiveNowPrice() {
		return vipExclusiveNowPrice.clone();
	}
	
	public int getAutoFightQueue() {
		return autoFightQueue;
	}
	
	public int getFormation() {
		return formation;
	}

	/**
	 * 获取vip作用号数据
	 * 
	 * @param effectMap
	 */
	public void assembleEffectMap(Map<Integer, Integer> effectMap) {
		effectMap.clear();
		for (int effId : effMap.keySet()) {
			effectMap.put(effId, effMap.get(effId));
		}
	}

	public List<ItemInfo> getVipGiftItems() {
		if (vipGiftItems != null) {
			return vipGiftItems.stream().map(ItemInfo::clone).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	/**
	 * 最大等级
	 */
	public static int getMaxLevel() {
		return maxLevel;
	}

	/**
	 * vip最大经验值
	 */
	public static int getMaxVipExp() {
		return maxVipExp;
	}

	@Override
	protected boolean assemble() {
		if (VipCfg.maxLevel < level) {
			VipCfg.maxLevel = level;
		}

		if (VipCfg.maxVipExp < vipExp) {
			VipCfg.maxVipExp = vipExp;
		}

		// vip作用号数据
		if (!HawkOSOperator.isEmptyString(effects)) {
			String[] array = effects.split(",");
			for (String info : array) {
				String[] items = info.split("_");
				if (items.length != 2) {
					return false;
				}
				effMap.put(Integer.parseInt(items[0]), Integer.parseInt(items[1]));
			}
		}

		// vip礼包数据
		if (!HawkOSOperator.isEmptyString(vipGift)) {
			vipGiftItems = new ArrayList<>();
			String[] array = vipGift.split(",");
			for (String info : array) {
				ItemInfo itemInfo = ItemInfo.valueOf(info);
				if (itemInfo != null) {
					vipGiftItems.add(itemInfo);
				}
			}
		}

		// vip解锁英雄数据
		if (!HawkOSOperator.isEmptyString(unlockHero)) {
			String[] array = unlockHero.split(",");
			for (String info : array) {
				ItemInfo itemInfo = ItemInfo.valueOf(info);
				if (itemInfo != null) {
					vipGiftItems.add(itemInfo);
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(vipExclusiveBox)) {
			vipExclusiveItems = new ArrayList<>();
			String[] array = vipExclusiveBox.split(",");
			for (String info : array) {
				ItemInfo itemInfo = ItemInfo.valueOf(info);
				if (itemInfo != null) {
					vipExclusiveItems.add(itemInfo);
				}
			}
		}

		if (!HawkOSOperator.isEmptyString(vipBenefitBox)) {
			vipBenefitItems = new ArrayList<>();
			String[] array = vipBenefitBox.split(",");
			for (String info : array) {
				ItemInfo itemInfo = ItemInfo.valueOf(info);
				if (itemInfo != null) {
					vipBenefitItems.add(itemInfo);
				}
			}
		}

		if (HawkOSOperator.isEmptyString(vipExclusiveNow)) {
			return false;
		}

		vipExclusiveNowPrice = ItemInfo.valueOf(vipExclusiveNow);

		return true;
	}

	@Override
	protected boolean checkValid() {
		if (level > 0 && HawkOSOperator.isEmptyString(effects)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取vip专属礼包数据
	 * 
	 * @return
	 */
	public List<ItemInfo> getVipExclusiveItems() {
		if (vipExclusiveItems != null) {
			return vipExclusiveItems.stream().map(e -> e.clone()).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	/**
	 * 获取Vip福利礼包数据
	 * 
	 * @return
	 */
	public List<ItemInfo> getVipBenefitItems() {
		if (vipBenefitItems != null) {
			return vipBenefitItems.stream().map(e -> e.clone()).collect(Collectors.toList());
		}

		return Collections.emptyList();
	}

	public int getDrawAllGift() {
		return drawAllGift;
	}

	public int getUnlockTalentLine4() {
		return unlockTalentLine4;
	}

	public int getUnlockTalentLine5() {
		return unlockTalentLine5;
	}
	
	public int getEmojiFreeTimes() {
		return emojiFreeTimes;
	}
	
	public int getAtkAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(atkAttr).getOrDefault(soldierType, 0);
	}

	public int getHpAttr(int soldierType) {
		return SerializeHelper.cfgStr2Map(hpAttr).getOrDefault(soldierType, 0);
	}
}
