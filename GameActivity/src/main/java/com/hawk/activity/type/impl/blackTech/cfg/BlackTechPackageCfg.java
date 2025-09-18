package com.hawk.activity.type.impl.blackTech.cfg;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkException;

import com.hawk.activity.helper.RewardHelper;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.gamelib.activity.ConfigChecker;


/**
 * 奖池对应奖励配置
 * @author RickMei
 *
 */
@HawkConfigManager.XmlResource(file = "activity/black_tech/black_tech_package.xml")
public class BlackTechPackageCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 奖励列表*/
	private final String item;
	/** 花费*/
	private final String newPrice;
	/**限购次数*/
	private final int limitNum;
	
	private List<RewardItem.Builder> itemList;

	private List<RewardItem.Builder> priceList;
	
	private static Map<Integer, List<BlackTechPackageCfg>> shopGroupMap = new HashMap<Integer, List<BlackTechPackageCfg>>();
	
	public static List<BlackTechPackageCfg> getShopsByGroup( int groupId ){
		return shopGroupMap.get(groupId);
	}
	
	public BlackTechPackageCfg() {
		id = 0;
		item = "";
		newPrice = "";
		limitNum = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			itemList = RewardHelper.toRewardItemImmutableList(item);
			priceList = RewardHelper.toRewardItemImmutableList(newPrice);
		} catch (Exception e) {
			HawkException.catchException(e);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(item);
		if (!valid) {
			throw new InvalidParameterException(String.format("BlackTechPackageCfg reward error, id: %s , reward: %s", id, item));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(newPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("BlackTechPackageCfg newPrice error, id: %s , newPrice: %s", id, newPrice));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getItem() {
		return item;
	}

	public String getNewPrice() {
		return newPrice;
	}

	public int getLimitNum() {
		return limitNum;
	}

	public List<RewardItem.Builder> getItemList() {
		return itemList;
	}

	public List<RewardItem.Builder> getPriceList() {
		return priceList;
	}

}
