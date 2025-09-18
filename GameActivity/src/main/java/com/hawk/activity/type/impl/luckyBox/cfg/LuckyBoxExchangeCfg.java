package com.hawk.activity.type.impl.luckyBox.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 幸运转盘兑换商店表格解析
 */
@HawkConfigManager.XmlResource(file = "activity/lucky_box/lucky_box_exchange.xml")
public class LuckyBoxExchangeCfg extends AExchangeTipConfig {
	/**
	 * 唯一ID
	 */
	@Id
	private final int id;

	/**
	 * 兑换需要的物品
	 */
	private final String exchangerequirements;
	/**
	 * 兑换获得物品
	 */
	private final String exchangeobtain;

	/**
	 * 最大数量
	 */
	private final int limittimes;

	public LuckyBoxExchangeCfg() {
		id = 0;
		exchangerequirements = "";
		exchangeobtain = "";
		limittimes = 0;
	}

	public int getId() {
		return id;
	}

	public int getLimittimes() {
		return limittimes;
	}

	/**
	 * 表格数据拆分
	 * @return
	 */
	public List<RewardItem.Builder> getNeedItemList() {
		return RewardHelper.toRewardItemImmutableList(this.exchangerequirements);
	}
	/**
	 * 表格数据拆分
	 * @return
	 */
	public List<RewardItem.Builder> getGainItemList() {
		return RewardHelper.toRewardItemImmutableList(this.exchangeobtain);
	}

	/**
	 * 表格数据检查
	 * @return
	 */
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(exchangerequirements);
		if (!valid) {
			throw new InvalidParameterException(String.format("LuckyBoxExchangeCfg exchangerequirements error, id: %s , needItem: %s", id, exchangerequirements));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(exchangeobtain);
		if (!valid) {
			throw new InvalidParameterException(String.format("LuckyBoxExchangeCfg exchangeobtain error, id: %s , gainItem: %s", id, exchangeobtain));
		}
		return super.checkValid();
	}
	
	

}
