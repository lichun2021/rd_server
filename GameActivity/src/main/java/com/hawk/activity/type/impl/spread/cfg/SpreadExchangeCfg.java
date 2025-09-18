package com.hawk.activity.type.impl.spread.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.HawkConfigManager.XmlResource;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 兑换配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/spread/spread_exchange.xml")
public class SpreadExchangeCfg extends AExchangeTipConfig {

	/** */
	@Id
	private final int id;
	/** 兑换所需道具*/
	private final String needItem;
	/** 兑换所得道具*/
	private final String gainItem;
	/**可兑换次数*/
	private final int times;
	
	private List<RewardItem.Builder> needItemList;

	private List<RewardItem.Builder> gainItemList;
	

	public SpreadExchangeCfg() {
		id = 0;
		times = 0;
		needItem = "";
		gainItem = "";
	}
	
	protected boolean assemble() {
		try {
			needItemList = RewardHelper.toRewardItemImmutableList(needItem);
			gainItemList = RewardHelper.toRewardItemImmutableList(gainItem);
		} catch (Exception e) {
			HawkLog.errPrintln("assemble error! file: {}, id: {}, reward: {}", this.getClass().getAnnotation(XmlResource.class).file(), this.id, this.needItem);
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("check reward error, id: %s , reward: %s", id, needItem));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getReward() {
		return needItem;
	}

	public List<RewardItem.Builder> getNeedItemList() {
		return needItemList;
	}

	public List<RewardItem.Builder> getGainItemList() {
		return gainItemList;
	}

	public String getGainItem() {
		return gainItem;
	}

	public int getTimes() {
		return times;
	}
}
