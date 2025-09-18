package com.hawk.activity.type.impl.mechacoreexplore.cfg;

import org.hawk.config.HawkConfigManager;
import com.hawk.activity.type.impl.exchangeTip.AExchangeTipConfig;
import com.hawk.gamelib.activity.ConfigChecker;
import java.security.InvalidParameterException;

@HawkConfigManager.XmlResource(file = "activity/core_explore/core_explore_shop.xml")
public class CoreExploreShopCfg extends AExchangeTipConfig {
	@Id
	private final int id;
	private final String needItem;
	private final String needItem2;
	private final String gainItem;
	private final int times;
	private final int time2;
	private final int techId;
	
	public CoreExploreShopCfg(){
		this.id = 0;
		this.needItem = "";
		this.needItem2 = "";
		this.gainItem = "";
		this.times = 0;
		this.time2 = 0;
		this.techId = 0;
	}
	
	public boolean assemble() {
		
		return true;
	}
	
	@Override
	protected final boolean checkValid() {
		boolean valid = ConfigChecker.getDefaultChecker().checkAwardsValid(gainItem);
		if (!valid) {
			throw new InvalidParameterException(String.format("CoreExploreShopCfg reward error, id: %s , reward: %s", id, gainItem));
		}
		return super.checkValid();
	}

	public int getId() {
		return id;
	}

	public String getNeedItem() {
		return needItem;
	}

	public String getNeedItem2() {
		return needItem2;
	}

	public String getGainItem() {
		return gainItem;
	}

	public int getTimes() {
		return times;
	}

	public int getTime2() {
		return time2;
	}

	public int getTechId() {
		return techId;
	}

}
