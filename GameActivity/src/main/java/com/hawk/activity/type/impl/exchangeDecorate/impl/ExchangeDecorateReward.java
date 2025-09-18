package com.hawk.activity.type.impl.exchangeDecorate.impl;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateExchangeCfg;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ExchangeCommonType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class ExchangeDecorateReward implements IExchangeCommon{

	static{
		new ExchangeDecorateReward();
	}
	
	public ExchangeDecorateReward() {
		IExchangeCommon.register(ExchangeCommonType.DECORATE_EXCHANGE, this);
	}

	@Override
	public void exchangeCommon(HawkDBEntity baseEntity, List<Integer> params) {
		ActivityExchangeDecorateEntity entity = (ActivityExchangeDecorateEntity) baseEntity;
		
		int levelId = params.get(0);
		int number =  params.get(1);

		ExchangeDecorateInfo exchangeDecorateInfo = null;
		for (ExchangeDecorateInfo info : entity.getDecorateList()) {
			if(info.getLevelId() == levelId){
				exchangeDecorateInfo = info;
				break;
			}
		}
		
		ExchangeDecorateExchangeCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecorateExchangeCfg.class, levelId);
		if(cfg == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_ID_NOT_FIND_VALUE);
			return;
		}
		int curNumber = 0;
		if(exchangeDecorateInfo!=null){
			curNumber = exchangeDecorateInfo.getState()+number;
		}else{
			curNumber = number;
		}
		if(curNumber > cfg.getTimes()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_NUMBER_ERROR_VALUE);
			return;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EXCHANGE_DECORATE_VALUE);
		ExchangeDecorateActivity exchangeDecorateActivity = (ExchangeDecorateActivity) opActivity.get();
		
		boolean flag = exchangeDecorateActivity.getDataGeter().cost(entity.getPlayerId(), cfg.getNeedItemList(), number, Action.EXCHANGE_DECORATE_COST, true);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,Status.Error.EXCHANGE_DECORATE_COST_ERROR_VALUE);
			return;
		}
		
		// 发奖
		exchangeDecorateActivity.getDataGeter().takeReward(entity.getPlayerId(), cfg.getGainItemItemList(), number,
				Action.EXCHANGE_DECORATE_LEVEL_OPEN_REWARD, true, RewardOrginType.EXCHANGE_DECORATE_REWARD);
		
		if(exchangeDecorateInfo == null){
			exchangeDecorateInfo = new ExchangeDecorateInfo();
			exchangeDecorateInfo.setLevelId(levelId);
			exchangeDecorateInfo.setState(number);
			entity.getDecorateList().add(exchangeDecorateInfo);
		}else{
			exchangeDecorateInfo.setState(exchangeDecorateInfo.getState()+number);
		}
		entity.notifyUpdate();
		exchangeDecorateActivity.exchangeList(entity.getPlayerId());
	}


}
