package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;

import org.hawk.log.HawkLog;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.LoginDayFundTwoEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.loginfundtwo.LoginFundTwoActivity;
import com.hawk.activity.type.impl.loginfundtwo.cfg.LoginFundActivityTwoAchieveCfg;
import com.hawk.activity.type.impl.loginfundtwo.entity.LoginFundTwoEntity;
import com.hawk.game.protocol.Activity;

/**
 * @author hf
 */
public class LoginDaysFundTwoParser extends AchieveParser<LoginDayFundTwoEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.LOGIN_DAYS_FUND_TWO;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, LoginDayFundTwoEvent event) {
		/**
		 * 任务成就中包含三种基金的成就
		 * 每种基金 计算各自购买之后登陆的天数
		 */
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.LOGIN_FUND_TWO_VALUE);
		if(!optional.isPresent()){
			return false;
		}
		String playerId = event.getPlayerId();
		LoginFundTwoActivity loginFundTwoActivity = (LoginFundTwoActivity) optional.get();

		Optional<LoginFundTwoEntity> opEntity = loginFundTwoActivity.getPlayerDataEntity(playerId);
		if(!opEntity.isPresent()){
			return false;
		}
		LoginFundTwoEntity entity = opEntity.get();

		LoginFundActivityTwoAchieveCfg activityTwoAchieveCfg = (LoginFundActivityTwoAchieveCfg) achieveConfig;
		//基金类型
		int fundType = activityTwoAchieveCfg.getFundType();
		//没买成就类型对应的基金
		if (!entity.isHasBuy(fundType)){
			HawkLog.logPrintln("LoginDaysFundTwoParser updateAchieve this type is not buy, playerId:{}, fundType:{}", playerId, fundType);
			return false;
		}
		//符合条件
		//类型基金购买在那天
		int buyDayInt = entity.getBuyInfoMap().getOrDefault(fundType, 0);
		//购买之后此类型基金的登录天数
		//拿到天数走更新成就的逻辑
		int value = entity.getLoginDaysCount(buyDayInt);
		int configValue = achieveConfig.getConditionValue(0);
		HawkLog.logPrintln("LoginDaysFundTwoParser updateAchieve success, playerId:{}, fundType:{}, typeLoginSum:{}", playerId, fundType, value);

		if(achieveItem.getValue(0) >= value){
			return false;
		}
		if (value > configValue) {
			value = configValue;
		}
		achieveItem.setValue(0, value);
		return true;
	}
}
