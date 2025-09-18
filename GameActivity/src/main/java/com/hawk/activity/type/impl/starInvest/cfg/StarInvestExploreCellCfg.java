package com.hawk.activity.type.impl.starInvest.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;


@HawkConfigManager.XmlResource(file = "activity/star_invest/star_invest_explore_cell.xml")
public class StarInvestExploreCellCfg extends HawkConfigBase {
	// 礼包ID
	@Id 
	private final int id;
	
	private final  int openLimit;
	
	private final int commonTime;
	
	private final int advancedTime;
	
	
	public StarInvestExploreCellCfg() {
		id = 0;
		openLimit = 0;
		commonTime =0;
		advancedTime = 0;
	}
	
	@Override
	protected boolean assemble() {
		return true;
	}


	public int getId() {
		return id;
	}

	
	public int getOpenLimit() {
		return openLimit;
	}
	
	public int getCommonTime() {
		return commonTime;
	}
	
	public int getAdvancedTime() {
		return advancedTime;
	}
}
