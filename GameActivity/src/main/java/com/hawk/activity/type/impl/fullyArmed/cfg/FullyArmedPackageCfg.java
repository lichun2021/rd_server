package com.hawk.activity.type.impl.fullyArmed.cfg;

import java.security.InvalidParameterException;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 购买礼包配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/fully_armed/fully_armed_package.xml")
public class FullyArmedPackageCfg extends HawkConfigBase {


	/** */
	@Id
	private final int id;
	/** 奖池*/
	private final int awardId;
	/** 花费*/
	private final String price;
	/**限购次数*/
	private final int limit;
	
	private List<RewardItem.Builder> priceList;
	
	
	public FullyArmedPackageCfg() {
		id = 0;
		price = "";
		limit = 0;
		awardId = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			priceList = RewardHelper.toRewardItemImmutableList(price);

		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(price);
		if (!valid) {
			throw new InvalidParameterException(String.format("FullyArmedPackageCfg price error, id: %s , price: %s", id, price));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public List<RewardItem.Builder> getPriceList() {
		return priceList;
	}
	
	public int getAwardId() {
		return awardId;
	}

	public String getPrice() {
		return price;
	}

	public int getLimit() {
		return limit;
	}

}
