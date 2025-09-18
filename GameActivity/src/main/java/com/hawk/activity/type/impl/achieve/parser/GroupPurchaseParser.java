package com.hawk.activity.type.impl.achieve.parser;

import java.util.List;
import java.util.Optional;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.GroupPurchaseEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.groupPurchase.GroupPurchaseActivity;
import com.hawk.activity.type.impl.growfundnew.GrowFundNewActivity;
import com.hawk.activity.type.impl.loginfund.LoginFundActivity;
import com.hawk.activity.type.impl.loginfundtwo.LoginFundTwoActivity;
import com.hawk.activity.type.impl.logingift.LoginGiftActivity;
import com.hawk.activity.type.impl.powerfund.PowerFundActivity;

public class GroupPurchaseParser extends AchieveParser<GroupPurchaseEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.GROUP_PURCHASE_COUNT;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return updateAchieveInfo(achieveItem, achieveConfig, playerId);
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, GroupPurchaseEvent event) {
		return updateAchieveInfo(achieveItem, achieveConfig, event.getPlayerId());
	}

	
	private boolean updateAchieveInfo(AchieveItem achieveItem, AchieveConfig achieveConfig, String playerId) {
		Optional<GroupPurchaseActivity> opActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GROUP_PURCHASE_ACTIVITY.intValue());
		if(!opActivity.isPresent()){
			return false;
		}
		boolean loginFundBuy = false;
		boolean growFundBuy = false;
		boolean powerFundBuy = false;
		boolean loginFundTwoBuy = false;
		boolean loginGiftBuy = false;

		Optional<LoginFundActivity> opLoginFundActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOGIN_FUND_ACTIVITY.intValue());
		if(opLoginFundActivity.isPresent()){
			loginFundBuy = opLoginFundActivity.get().hasBuy(playerId);
		}
		
		Optional<GrowFundNewActivity> opGrowFundActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.GROW_FUND_NEW_ACTIVITY.intValue());
		if(opGrowFundActivity.isPresent()){
			growFundBuy = opGrowFundActivity.get().hasBuy(playerId);
		}
		
		Optional<PowerFundActivity> opPowerFundActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.POWER_FUND_ACTIVITY.intValue());
		if(opPowerFundActivity.isPresent()){
			powerFundBuy = opPowerFundActivity.get().hasBuy(playerId);
		}

		Optional<LoginFundTwoActivity> opLoginFundTwoActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOGIN_FUND_TWO_ACTIVITY.intValue());
		if(opLoginFundTwoActivity.isPresent()){
			loginFundTwoBuy = opLoginFundTwoActivity.get().isHasBuyAnyType(playerId);
		}
		
		Optional<LoginGiftActivity> opLoginGiftActivity = ActivityManager.getInstance().getGameActivityByType(ActivityType.LOGIN_GIFT_ACTIVITY.intValue());
		if(opLoginGiftActivity.isPresent()){
			loginGiftBuy = opLoginGiftActivity.get().isBuyAdvance(playerId);
		}

		GroupPurchaseActivity activity = opActivity.get();
		int score = activity.getScore();
		List<Integer> conditionValues = achieveConfig.getConditionValues();
		int condition = conditionValues.get(0);
		int buyValue = loginFundBuy || growFundBuy || powerFundBuy || loginFundTwoBuy || loginGiftBuy ? 1 : 0;
		int scoreValue = Math.min(score, condition);
		if(scoreValue <= achieveItem.getValue(0) && buyValue <= achieveItem.getValue(1)){
			return false;
		}
		achieveItem.setValue(0, scoreValue);
		achieveItem.setValue(1, buyValue);
		return true;
	}

	@Override
	public boolean isFinish(AchieveItem achieveItem, AchieveConfig achieveConfig) {
		int scoreValue = achieveItem.getValue(0);
		int buyValue = achieveItem.getValue(1);
		int configValue = achieveConfig.getConditionValue(0);
		return buyValue > 0 && scoreValue >= configValue;
	}
	
	
}
