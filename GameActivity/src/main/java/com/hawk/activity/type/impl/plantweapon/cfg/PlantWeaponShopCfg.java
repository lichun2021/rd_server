package com.hawk.activity.type.impl.plantweapon.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import com.hawk.gamelib.activity.ConfigChecker;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

@HawkConfigManager.XmlResource(file = "activity/plant_weapon/plant_weapon_shop.xml")
public class PlantWeaponShopCfg extends HawkConfigBase {
	@Id
	private final int id;
	
	private final int shopItemType;
	
	private final int times;
	
	private final int payQuota;
	
	private final String iosPayId;
	private final String androidPayID;
	
	private final String payItem;
	private final String getItem;
	
	private static String freeAward;
	private static Map<String, PlantWeaponShopCfg> buyItemGoodsMap = new HashMap<>();
	
	public PlantWeaponShopCfg(){
		this.id = 0;
		this.shopItemType = 0;
		this.times = 0;
		this.payQuota = 0;
		this.iosPayId = "";
		this.androidPayID = "";
		this.payItem = "";
		this.getItem = "";
	}
	
	public boolean assemble() {
		if (shopItemType == 0) {
			freeAward = getItem;
		}
		buyItemGoodsMap.put(androidPayID, this);
		buyItemGoodsMap.put(iosPayId, this);
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(getItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlantWeaponShopCfg reward error, id: %s , reward: %s", id, getItem));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public int getShopItemType() {
		return shopItemType;
	}

	public int getTimes() {
		return times;
	}

	public int getPayQuota() {
		return payQuota;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public String getAndroidPayID() {
		return androidPayID;
	}

	public String getPayItem() {
		return payItem;
	}

	public String getGetItem() {
		return getItem;
	}

	public static PlantWeaponShopCfg getConfig(String goodsId) {
		return buyItemGoodsMap.get(goodsId);
	}
	
	public static String getFreeAward() {
		return freeAward;
	}
}
