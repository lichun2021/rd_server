package com.hawk.activity.type.impl.hiddenTreasure.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

/**勋章宝藏成就数据
 * @author Winder
 */
@HawkConfigManager.XmlResource(file = "activity/hidden_treasure/hidden_treasure_exchange_award.xml")
public class HiddenTreasureExchangeAwardCfg extends HawkConfigBase {
	@Id
	private final int id;
	private final int limittimes;

	private final String exchangerequirements;
	private final String exchangeobtain;
	
	private final int noticeId;

	public HiddenTreasureExchangeAwardCfg() {
		id = 0;
		limittimes = 0;
		exchangerequirements = "";
		exchangeobtain = "";
		noticeId = 0;
	}

	public int getId() {
		return id;
	}

	public int getLimittimes() {
		return limittimes;
	}

	public String getExchangerequirements() {
		return exchangerequirements;
	}

	public String getExchangeobtain() {
		return exchangeobtain;
	}

	public int getNoticeId() {
		return noticeId;
	}

}
