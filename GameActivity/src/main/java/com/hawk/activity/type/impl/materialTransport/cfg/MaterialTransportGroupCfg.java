
package com.hawk.activity.type.impl.materialTransport.cfg;

import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;

import com.hawk.serialize.string.SerializeHelper;

/**
 * 机甲破世 
 * @author RickMei 
 *
 */
@HawkConfigManager.XmlResource(file = "activity/material_transport/material_transport_group.xml")
public class MaterialTransportGroupCfg extends HawkConfigBase {
	// <data id="104" type="1" quality="5" rewardId="10,11,12,13,14,15,16,1,2,3" robNumber="3" />
	@Id
	private final int id;

	private final String rewardId;

	private final int type;
	private final int quality;
	private final int robNumber;
	private List<Integer> rewardList;

	public MaterialTransportGroupCfg() {
		id = 0;
		rewardId = "";
		type = 0;
		quality = 0;
		robNumber = 0;
	}

	@Override
	protected boolean assemble() {
		rewardList = SerializeHelper.stringToList(Integer.class, rewardId, SerializeHelper.BETWEEN_ITEMS);
		return true;
	}

	public int getId() {
		return id;
	}

	public List<Integer> getRewardList() {
		return rewardList;
	}

	public void setRewardList(List<Integer> rewardList) {
		this.rewardList = rewardList;
	}

	public String getRewardId() {
		return rewardId;
	}

	public int getType() {
		return type;
	}

	public int getQuality() {
		return quality;
	}

	public int getRobNumber() {
		return robNumber;
	}

}
