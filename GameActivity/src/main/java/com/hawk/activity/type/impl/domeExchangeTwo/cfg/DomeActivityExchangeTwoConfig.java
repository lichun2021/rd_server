package com.hawk.activity.type.impl.domeExchangeTwo.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;

/**
 * 策划要求目录如此. 就是rose 
 * @author jm
 *
 */
@HawkConfigManager.XmlResource(file = "activity/rose/rose_exchange.xml")
public class DomeActivityExchangeTwoConfig extends HawkConfigBase {
	
	@Id
	private final int id;
	
	private final String needItem;
	
	private final String gainItem;
	
	private final int times;
	
	private List<RewardItem.Builder> gainItemList;

	private List<RewardItem.Builder> needItemList;
	
	public DomeActivityExchangeTwoConfig(){
		id = 0;
		needItem = "";
		gainItem = "";
		times = 0;
	}

	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getGainItem() {
		return gainItem;
	}
	
	public int getTimes() {
		return times;
	}

	public boolean assemble() {
		try {
			this.gainItemList = RewardHelper.toRewardItemImmutableList(this.gainItem);
			this.needItemList = RewardHelper.toRewardItemImmutableList(this.needItem);
			return true;
		} catch (Exception arg1) {
			HawkException.catchException(arg1, new Object[0]);
			return false;
		}
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("复制穹顶兑换配置错误 , id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("复制穹顶兑配置错误, id: %s , gainItem: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}
}
