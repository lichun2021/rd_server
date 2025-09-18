
package com.hawk.activity.type.impl.appointget.cfg;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkRandObj;

/**
 * 推广员系统成就配置表 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/appoint_get/appoint_get_draw.xml")
public class AppointGetDrawCfg extends HawkConfigBase implements HawkRandObj {
	// <data id="1" weight="100" gainItem="30000_1000005_20" notice="1" />
	@Id
	private final int id;

	private final int weight;
	private final String gainItem;

	private final int notice;

	public AppointGetDrawCfg() {
		id = 0;
		notice = 0;
		weight = 1;
		gainItem = "";
	}

	@Override
	protected boolean assemble() {
		// ImmutableList<Builder> rewardList = RewardHelper.toRewardItemImmutableList(gainItem);
		return true;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getWeight() {
		return weight;
	}

	public String getGainItem() {
		return gainItem;
	}

	public int getNotice() {
		return notice;
	}

}
