package com.hawk.activity.type.impl.bestprize.cfg;

import java.security.InvalidParameterException;
import org.hawk.config.HawkConfigManager;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;
	
/**
 * 兑换商店配置
 * @author lating
 *
 */
@HawkConfigManager.XmlResource(file = "activity/the_best_prize/the_best_prize_pointsShop.xml")
public class BestPrizeExchangeCfg extends AExchangeTipConfig {
	@Id
	private final int id;
	
	private final int times;
	
	private final String needItem;
	
	private final String gainItem;
	
	private static int consumeItemId;
	
	public BestPrizeExchangeCfg(){
		this.id = 0;
		this.times = 0;
		this.needItem = "";
		this.gainItem = "";
	}
	
	@Override
	public boolean assemble() {
		RewardItem.Builder builder = RewardHelper.toRewardItem(needItem);
		consumeItemId = builder.getItemId();
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("BestPrizeExchangeCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("BestPrizeExchangeCfg reward error, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getTimes() {
		return times;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
	}

	public static int getConsumeItemId() {
		return consumeItemId;
	}
	
}