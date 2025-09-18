package com.hawk.game.config;

import java.util.ArrayList;
import java.util.List;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.os.HawkOSOperator;
import org.hawk.os.HawkTime;

import com.hawk.game.item.AwardItems;
import com.hawk.game.item.ItemInfo;

/**
 * 特殊CDK兑换
 * 
 * @date 2017-4-15 15:37:31
 */
@HawkConfigManager.XmlResource(file = "xml/special_cdk.xml")
public class SpecialCdkCfg extends HawkConfigBase {
	/**
	 * 兑换CDK
	 */
	@Id
	protected final String id;
	/**
	 * 奖励
	 */
	protected final String award;
	/**
	 * 有效起始时间
	 */
	protected final String startTime;
	/**
	 * 有效结束时间
	 */
	protected final String endTime;
	
	private List<ItemInfo> awardItems;
	
	private long startTimeLong;
	
	private long endTimeLong;

	public SpecialCdkCfg() {
		id = null;
		award = "";
		startTime= "0";
		endTime = "0";
	}

	public String getId() {
		return id;
	}

	public String getAward() {
		return award;
	}
	
	@Override
	protected boolean assemble() {
		if(!HawkOSOperator.isEmptyString(award)) {
			awardItems = new ArrayList<ItemInfo>();
			String[] items = award.split(",");
			for (String item : items) {
				awardItems.add(ItemInfo.valueOf(item));
			}
		}
		
		if(!HawkOSOperator.isEmptyString(startTime)) {
			startTimeLong = HawkTime.parseTime(startTime);
		}
		
		if(!HawkOSOperator.isEmptyString(endTime)) {
			endTimeLong = HawkTime.parseTime(endTime);
		}
		return true;
	}
	
	@Override
	protected boolean checkValid() {
		return true;
	}

	public AwardItems getAwardItems() {
		if(awardItems != null) {
			AwardItems awardItem = AwardItems.valueOf();
			for(ItemInfo item : awardItems) {
				awardItem.addItem(item.clone());
			}
			return awardItem;
		}
		return null;
	}

	public long getStartTime() {
		return startTimeLong;
	}

	public long getEndTime() {
		return endTimeLong;
	}
}
