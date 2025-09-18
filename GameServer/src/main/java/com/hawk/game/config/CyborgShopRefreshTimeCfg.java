package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;
import org.hawk.os.HawkTime;

/**
 * 赛博之战赛季时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_shop_refresh_time.xml")
public class CyborgShopRefreshTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int id;

	/** 赛博商店重置时间 */
	private final String refreshTime;
	
	private long refreshTimeValue;


	public CyborgShopRefreshTimeCfg() {
		id = 0;
		refreshTime = "";
	}


	protected boolean assemble() {
		refreshTimeValue = HawkTime.parseTime(refreshTime);
		return true;
	}

	public int getId() {
		return id;
	}


	public long getRefreshTimeValue() {
		return refreshTimeValue;
	}


	@Override
	protected boolean checkValid() {
		ConfigIterator<CyborgShopRefreshTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(CyborgShopRefreshTimeCfg.class);
		int baseId = 0;
		long baseTime = 0;
		for (CyborgShopRefreshTimeCfg timeCfg : it) {
			int id = timeCfg.getId();
			if (id <= baseId) {
				HawkLog.errPrintln(" CyborgShopRefreshTimeCfg check valid failed, id order error, id: {}", id);
				return false;
			}
			long timeValue = timeCfg.getRefreshTimeValue();
			if (timeValue <= baseTime) {
				HawkLog.errPrintln(" CyborgShopRefreshTimeCfg check valid failed, timeValue order error, id: {}", id);
				return false;
			}
			baseId = id;
			baseTime = timeValue;
		}
		return true;
	}
}
