package com.hawk.game.config;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

import com.hawk.serialize.string.SerializeHelper;

@HawkConfigManager.XmlResource(file = "xml/travel_shop_pool.xml")
public class TravelShopPoolCfg extends HawkConfigBase {
	/**
	 *池ID
	 */
	@Id
	private final int id;
	/**
	 * 池的类型
	 */
	private final int type;
	/**
	 * 解锁等级
	 */
	private final int unlockEffectId;
	/**
	 * 特殊礼包
	 */
	private final String specialGift;
	/**
	 * 特殊礼包id列表
	 */
	private List<Integer> giftIdList;
	/**
	 * 特殊礼包的概率
	 */
	private List<Integer> giftRateList;
	
	public TravelShopPoolCfg() {
		super();
		this.id = 0;
		this.type = 0;
		this.unlockEffectId = 0;
		this.specialGift = "";
	}
	
	public int getId() {
		return id;
	}
	public int getType() {
		return type;
	}
	public int getUnlockEffectId() {
		return unlockEffectId;
	}
	public List<Integer> getGiftIdList() {
		return giftIdList;
	}
	public List<Integer> getGiftRateList() {
		return giftRateList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean assemble() {
		if (!HawkOSOperator.isEmptyString(specialGift)) {
			String[] specialGiftArray = specialGift.split(SerializeHelper.BETWEEN_ITEMS);
			List<Integer> idList = new ArrayList<>(specialGiftArray.length);
			List<Integer> rateList = new ArrayList<>(specialGiftArray.length);
			for (String specialColumn : specialGiftArray) {
				String[] idRate = specialColumn.split(SerializeHelper.ATTRIBUTE_SPLIT);
				idList.add(Integer.parseInt(idRate[0]));
				rateList.add(Integer.parseInt(idRate[1]));
			}
			
			this.giftIdList = idList;
			this.giftRateList = rateList;
		} else {
			this.giftIdList = Collections.EMPTY_LIST;
			this.giftRateList = Collections.EMPTY_LIST;
		} 
		
		return true;
	}
	
	@Override
	public boolean checkValid() {
		HawkConfigManager configManager = HawkConfigManager.getInstance();
		TravelShopGiftCfg shopGiftCfg = null;
		for (Integer id : this.giftIdList) {
			shopGiftCfg = configManager.getConfigByKey(TravelShopGiftCfg.class, id);
			if (shopGiftCfg == null) {
				throw new InvalidParameterException(String.format("special gift id=%s not exist in travel_shop_gift", id));
			}
		}
		
		return true;		
	}
}
