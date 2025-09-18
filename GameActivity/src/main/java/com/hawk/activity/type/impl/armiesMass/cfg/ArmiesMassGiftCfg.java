package com.hawk.activity.type.impl.armiesMass.cfg;

import java.util.HashMap;
import java.util.Map;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;

/**
 * 时空好礼时空之门直购礼包道具
 * 
 * @author che
 *
 */
@HawkConfigManager.XmlResource(file = "activity/armies_mass/armies_mass_gift.xml")
public class ArmiesMassGiftCfg extends HawkConfigBase {
	// 奖励ID
	@Id
	private final int id;
	//阶段
	private final int stageId;
	//持续时间
	private final int type;
	//组ID
	private final int groupId;
	//等级ID
	private final int level;
	
	private final String name;
	
	private final String androidPayId;
	
	private final String iosPayId;
	//消耗
	private final String price;
	//获得
	private final String rewards;
	
	private final int allianceGift;

	private static Map<String, Integer> payGiftIdMap = new HashMap<String, Integer>();
	public ArmiesMassGiftCfg() {
		id = 0;
		stageId = 0;
		type = 0;
		groupId = 0;
		androidPayId="";
		iosPayId = "";
		level = 0;
		price = "";
		rewards = "";
		allianceGift = 0;
		name = "";
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(androidPayId)){
			payGiftIdMap.put(androidPayId, id);
		}
		if(!HawkOSOperator.isEmptyString(iosPayId)){
			payGiftIdMap.put(iosPayId, id);
		}
		return true;
	}

	
	
	public int getId() {
		return id;
	}

	public int getStageId() {
		return stageId;
	}

	public int getType() {
		return type;
	}

	

	public int getGroupId() {
		return groupId;
	}

	public int getLevel() {
		return level;
	}

	
	
	public String getPrice() {
		return price;
	}

	public String getRewards() {
		return rewards;
	}
	
	

	public String getAndroidPayId() {
		return androidPayId;
	}

	public String getIosPayId() {
		return iosPayId;
	}

	public int getAllianceGift() {
		return allianceGift;
	}
	
	

	public String getName() {
		return name;
	}

	public static int getGiftId(String payGiftId) {
		if (!payGiftIdMap.containsKey(payGiftId)) {
			throw new RuntimeException("payGiftId not match customGiftId");
		}
		return payGiftIdMap.get(payGiftId);
	}
	

	
	
	
}
