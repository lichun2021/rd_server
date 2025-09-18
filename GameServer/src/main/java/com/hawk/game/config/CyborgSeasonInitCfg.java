package com.hawk.game.config;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.config.iterator.ConfigIterator;
import org.hawk.log.HawkLog;

/**
 * 赛博之战赛季时间配置
 * 
 * @author Jesse
 *
 */
@HawkConfigManager.XmlResource(file = "xml/cyborg_season_init.xml")
public class CyborgSeasonInitCfg extends HawkConfigBase {
	/** 活动期数 */
	@Id
	private final int id;

	/** 额外初始星数 */
	private final int extraStar;
	
	/** 排行TOP占比*/
	private final int topeRate;
	


	public CyborgSeasonInitCfg() {
		id = 0;
		extraStar = 0;
		topeRate = 0;
	}


	protected boolean assemble() {
		return true;
	}

	public int getId() {
		return id;
	}


	public int getExtraStar() {
		return extraStar;
	}


	public int getTopeRate() {
		return topeRate;
	}


	@Override
	protected boolean checkValid() {
		ConfigIterator<CyborgSeasonInitCfg> it = HawkConfigManager.getInstance().getConfigIterator(CyborgSeasonInitCfg.class);
		int baseId = 0;
		int baseRate = 0;
		for (CyborgSeasonInitCfg timeCfg : it) {
			int id = timeCfg.getId();
			if (id <= baseId) {
				HawkLog.errPrintln(" CyborgSeasonInitCfg check valid failed, id order error, id: {}", id);
				return false;
			}
			int topRate = timeCfg.getTopeRate();
			if (topRate <= baseRate) {
				HawkLog.errPrintln(" CyborgSeasonInitCfg check valid failed, timeValue order error, topRate: {}", topRate);
				return false;
			}
			baseId = id;
			baseRate = topRate;
		}
		return true;
	}
}
