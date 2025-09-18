package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.game.item.ItemInfo;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 超级武器特殊奖励配置
 * @author golden
 *
 */
@HawkConfigManager.XmlResource(file = "xml/super_barrack_special_award.xml")
public class SuperWeaponSpecialAwardCfg extends HawkConfigBase {
	
	@Id
	protected final int id;
	
	protected final int x;
	
	protected final int y;
	/**
	 * 期数
	 */
	protected final int round;
	/**
	 * 奖励类型
	 */
	protected final int type;
	/**
	 * 礼包名称
	 */
	protected final String giftName;
	/**
	 * 奖励个数
	 */
	protected final int totalNumber;
	/**
	 * 奖励id
	 */
	protected final String awardId;
	/**
	 * 每个玩家限制奖励数量 
	 */
	protected final int numberLimit;
	/**
	 * 奖励列表
	 */
	private List<ItemInfo> rewardItem;
	
	public SuperWeaponSpecialAwardCfg() {
		id = 0;
		round = 1;
		x = 0;
		y = 0;
		type = 0;
		giftName = "";
		totalNumber = 1;
		awardId = "";
		numberLimit = 1;
	}

	public int getId() {
		return id;
	}

	public int getRound() {
		return round;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getType() {
		return type;
	}

	public String getGiftName() {
		return giftName;
	}

	public String getAwardId() {
		return awardId;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public int getNumberLimit() {
		return numberLimit;
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
		if (HawkOSOperator.isEmptyString(awardId)) {
			return false;
		}
		rewardItem = ItemInfo.valueListOf(awardId);
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		boolean awardIdCheckResult = ConfigChecker.getDefaultChecker().checkAwardsValid(awardId);
		boolean numCheckResult = totalNumber > 0;
		return awardIdCheckResult && numCheckResult;
	}
}