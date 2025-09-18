package com.hawk.activity.type.impl.plantweaponback.cfg;

import org.hawk.config.HawkConfigManager;
import org.hawk.log.HawkLog;

import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.gamelib.activity.ConfigChecker;
import java.security.InvalidParameterException;

@HawkConfigManager.XmlResource(file = "activity/plant_weapon_back/plant_weapon_back_shop.xml")
public class PlantWeaponBackShopCfg extends AExchangeTipConfig {
	@Id
	private final int id;
	
	private final int times;
	
	private final String needItem;
	
	private final String gainItem;
	
	private final int notExchange;
	
	private final int manhattanId;
	
	public PlantWeaponBackShopCfg(){
		this.id = 0;
		this.times = 0;
		this.needItem = "";
		this.gainItem = "";
		this.notExchange = 0;
		this.manhattanId = 0;
	}
	
	public boolean assemble() {
		if (notExchange > 0 && manhattanId <= 0) {
			HawkLog.errPrintln("activity/plant_weapon_back/plant_weapon_back_shop.xml, config manhattanId missed, id: {}", id);
			return false;
		}
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(needItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlantWeaponBackShopCfg reward error, id: %s , needItem: %s", id, needItem));
		}
		valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("PlantWeaponBackShopCfg reward error, id: %s , gainItem: %s", id, gainItem));
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

	public int getNotExchange() {
		return notExchange;
	}

	public int getManhattanId() {
		return manhattanId;
	}

}
