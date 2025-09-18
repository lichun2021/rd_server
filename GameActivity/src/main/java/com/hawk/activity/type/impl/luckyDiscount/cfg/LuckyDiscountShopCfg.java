package com.hawk.activity.type.impl.luckyDiscount.cfg;

import java.security.InvalidParameterException;
import java.util.ArrayList;
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
@HawkConfigManager.XmlResource(file = "activity/lucky_discount/luckydiscount_shop.xml")
public class LuckyDiscountShopCfg extends HawkConfigBase {
	/** */
	@Id
	private final int id;
	/** 奖池*/
	private final int pool;
	/** 奖励列表*/
	private final String item;
	/** 花费*/
	private final String newPrice;
	/**限购次数*/
	private final int limitNum;
	
	private List<RewardItem.Builder> itemList;

	private List<RewardItem.Builder> priceList;
	
	private static Map<Integer, List<LuckyDiscountShopCfg>> shopGroupMap = new HashMap<Integer, List<LuckyDiscountShopCfg>>();
	
	public static List<LuckyDiscountShopCfg> getShopsByGroup( int groupId ){
		return shopGroupMap.get(groupId);
	}
	
	public LuckyDiscountShopCfg() {
		id = 0;
		pool = 0;
		item = "";
		newPrice = "";
		limitNum = 0;
	}
	
	@Override
	protected boolean assemble() {
		try {
			itemList = RewardHelper.toRewardItemImmutableList(item);
			priceList = RewardHelper.toRewardItemImmutableList(newPrice);
			
			List<LuckyDiscountShopCfg> findList = shopGroupMap.get(pool);
			if(null == findList){
				findList = new ArrayList<LuckyDiscountShopCfg>();
				shopGroupMap.put(pool, findList);
			}
			findList.add(this);

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
			throw new InvalidParameterException(String.format("LuckyDiscountShopCfg reward error, id: %s , reward: %s", id, item));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(newPrice);
		if (!valid) {
			throw new InvalidParameterException(String.format("LuckyDiscountShopCfg newPrice error, id: %s , newPrice: %s", id, newPrice));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getPool() {
		return pool;
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
