package com.hawk.game.module.dayazhizhan.playerteam.cfg;

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
@HawkConfigManager.XmlResource(file = "xml/dyzz_shop_refresh_time.xml")
public class DYZZShopRefreshTimeCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int id;

	/** 赛博商店重置时间 */
	private final String refreshTime;
	
	private long refreshTimeValue;


	public DYZZShopRefreshTimeCfg() {
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
		ConfigIterator<DYZZShopRefreshTimeCfg> it = HawkConfigManager.getInstance().getConfigIterator(DYZZShopRefreshTimeCfg.class);
		int baseId = 0;
		long baseTime = 0;
		for (DYZZShopRefreshTimeCfg timeCfg : it) {
			int id = timeCfg.getId();
			if (id <= baseId) {
				HawkLog.errPrintln(" DYZZShopRefreshTimeCfg check valid failed, id order error, id: {}", id);
				return false;
			}
			long timeValue = timeCfg.getRefreshTimeValue();
			if (timeValue <= baseTime) {
				HawkLog.errPrintln(" DYZZShopRefreshTimeCfg check valid failed, timeValue order error, id: {}", id);
				return false;
			}
			baseId = id;
			baseTime = timeValue;
		}
		return true;
	}
}
