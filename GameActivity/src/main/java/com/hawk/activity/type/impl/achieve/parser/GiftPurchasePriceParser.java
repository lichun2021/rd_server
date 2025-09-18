package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;

import com.hawk.activity.event.impl.GiftPurchasePriceEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.datatype.ListValueData;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 购买价格XX金条的超值礼包次数
 * 
 * @author lating
 *
 */
public class GiftPurchasePriceParser extends AchieveParser<GiftPurchasePriceEvent> {
	
	private ListValueData listValueData = new ListValueData();

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.PRICE_SALE_GIFT_PURCHASE_TIMES;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return false;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, GiftPurchasePriceEvent event) {
		
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		if (conditionValues.get(0) != 0 && listValueData.isInList(conditionValues, event.getPrice()) == false) {
			return false;
		}

		int count = achieveData.getValue(0) + event.getTimes();
		achieveData.setValue(0, count);
		return true;
	}
}
