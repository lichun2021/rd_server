package com.hawk.game.config;

import java.util.List;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import com.google.common.collect.ImmutableList;
import com.hawk.game.item.ItemInfo;

/**
 * 至尊vip配置
 * 
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "xml/vip_super.xml")
public class VipSuperCfg extends HawkConfigBase {
	/**
	 * 至尊vip等级
	 */
	@Id
	protected final int level;
	/**
	 *  升至至尊vip等级需要的累计vip经验值
	 */
	protected final int vipExp;
	/**
	 *  至尊vip等级福利礼包
	 */
	protected final String vipBenefitBox;
	/**
	 *  是否开启天赋路线
	 */
	protected final int unlockTalentLine6;
	protected final int unlockTalentLine7;
	protected final int unlockTalentLine8;
	/**
	 *  编队组数
	 */
	protected final int troopTeamNum;
	/**
	 *  编队数量
	 */
	protected final int formation;
	/**
	 * 至尊vip月度礼包奖励
	 */
	protected final String monthGift;
	/**
	 * 至尊vip等级提升奖励
	 */
	protected final String firstGift;
	/**
	 * 至尊vip等级激活所需经验值（上个自然月积攒的活跃积分）
	 */
	protected final int activatePoints;
	/**
	 * 最低vip经验值
	 */
	private static int minVipExp = Integer.MAX_VALUE;
	
	private static int maxActivatePoints;

	/**
	 * vip福利礼包数据
	 */
	List<ItemInfo> vipBenefitItems;
	List<ItemInfo> monthGiftItems;
	List<ItemInfo> firstGiftItems;

	public VipSuperCfg() {
		level = 0;
		vipExp = 0;
		vipBenefitBox = "";
		unlockTalentLine6 = 0;
		unlockTalentLine7 = 0;
		unlockTalentLine8 = 0;
		troopTeamNum = 0;
		formation = 4;
		monthGift = "";
		firstGift = "";
		activatePoints = 0;
	}

	public int getLevel() {
		return level;
	}

	public int getVipExp() {
		return vipExp;
	}
	
	public int getUnlockTalentLine6() {
		return unlockTalentLine6;
	}
	
	public int getUnlockTalentLine7() {
		return unlockTalentLine7;
	}
	
	public int getUnlockTalentLine8() {
		return unlockTalentLine8;
	}

	public int getTroopTeamNum() {
		return troopTeamNum;
	}

	public int getFormation() {
		return formation;
	}
	
	public int getActivatePoints() {
		return activatePoints;
	}
	
	public List<ItemInfo> getVipBenefitItems() {
		return vipBenefitItems;
	}
	
	public List<ItemInfo> getMonthGiftItems() {
		return monthGiftItems;
	}
	
	public List<ItemInfo> getFirstGiftItems() {
		return firstGiftItems;
	}
	
	@Override
	protected boolean assemble() {
		minVipExp = Math.min(minVipExp, vipExp);
		maxActivatePoints = Math.max(activatePoints, maxActivatePoints);
		vipBenefitItems = ImmutableList.copyOf(ItemInfo.valueListOf(vipBenefitBox));
		monthGiftItems = ImmutableList.copyOf(ItemInfo.valueListOf(monthGift));
		firstGiftItems = ImmutableList.copyOf(ItemInfo.valueListOf(firstGift));
		return true;
	}

	@Override
	protected boolean checkValid() {
		return true;
	}

	public static int getMinVipExp() {
		return minVipExp;
	}
	
	public static int getVipSuperLevel(int vipExp) {
		ConfigIterator<VipSuperCfg> iterator = HawkConfigManager.getInstance().getConfigIterator(VipSuperCfg.class);
		int level = 0;
		while (iterator.hasNext()) {
			VipSuperCfg cfg = iterator.next();
			if (vipExp >= cfg.getVipExp() && cfg.getLevel() > level) {
				level = cfg.getLevel();
			}
		}
		
		return level;
	}
	
	public static VipSuperCfg getVipSuperCfg(int vipExp) {
		int level = getVipSuperLevel(vipExp);
		return HawkConfigManager.getInstance().getConfigByKey(VipSuperCfg.class, level);
	}

	public static int getMaxActivatePoints() {
		return maxActivatePoints;
	}

}
