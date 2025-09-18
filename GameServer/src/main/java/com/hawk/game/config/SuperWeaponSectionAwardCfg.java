package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 超级武器特赛季奖励
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/super_barrack_section_award.xml")
public class SuperWeaponSectionAwardCfg extends HawkConfigBase {

	@Id
	protected final int section;
	
	/**
	 * 奖励
	 */
	protected final String award;
	
	/**
	 * 奖励列表
	 */
	private List<ItemInfo> rewardItem;
	
	public SuperWeaponSectionAwardCfg() {
		section = 0;
		award = "";
	}

	public int getSection() {
		return section;
	}

	public String getAward() {
		return award;
	}
	
	public List<ItemInfo> getRewardItem() {
		List<ItemInfo> ret = new ArrayList<>();
		for (ItemInfo item : rewardItem) {
			ret.add(item.clone());
		}
		return ret;
	}

	@Override
	protected boolean assemble() {
		if (HawkOSOperator.isEmptyString(award)) {
			return false;
		}
		rewardItem = ItemInfo.valueListOf(award);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		boolean awardIdCheckResult = ConfigChecker.getDefaultChecker().checkAwardsValid(award);
		return awardIdCheckResult;
	}
}
