package com.hawk.activity.type.impl.exchangeDecorate.impl;

import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecoratePackageCfg;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateInfo;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateLimitInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ExchangeCommonType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardItem;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class ExchangeDecorateLevelOpenReward implements IExchangeCommon{

	static{
		new ExchangeDecorateLevelOpenReward();
	}
	
	public ExchangeDecorateLevelOpenReward() {
		IExchangeCommon.register(ExchangeCommonType.DECORATE_LIMIT_TIME, this);
	}

	@Override
	public void exchangeCommon(HawkDBEntity baseEntity, List<Integer> params) {
		ActivityExchangeDecorateEntity entity = (ActivityExchangeDecorateEntity) baseEntity;
		
		int levelId = params.get(0);
		int buyId = params.get(1);
		int buyNum= params.get(2);
		//获取该等级，可购买的列表
		ExchangeDecorateLimitInfo limitInfo = null;
		for (ExchangeDecorateLimitInfo tmpInfo : entity.getLevelOpenExchangeList()) {
			if(tmpInfo.getLevelId() == levelId){
				limitInfo = tmpInfo;
				break;
			}
		}
		if(limitInfo == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_LIMIT_ID_NOT_FIND_VALUE);
			return;
		}
		//购买的信息 检查
		ExchangeDecorateInfo buyInfo = null;
		for (ExchangeDecorateInfo info : limitInfo.getInfos()) {
			if(info.getLevelId() == buyId){
				buyInfo = info;
				break;
			}
		}
		
		if(buyInfo == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_ID_NOT_FIND_VALUE);
			return;
		}
		
		ExchangeDecoratePackageCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecoratePackageCfg.class, buyId);
		if(cfg == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_ID_NOT_FIND_VALUE);
			return;
		}
		
		if(buyInfo.getState()+buyNum > cfg.getNum()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_OPEN_NUMBER_ERROR_VALUE);
			return;
		}
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EXCHANGE_DECORATE_VALUE);
		ExchangeDecorateActivity exchangeDecorateActivity = (ExchangeDecorateActivity) opActivity.get();
		
		boolean flag = exchangeDecorateActivity.getDataGeter().cost(entity.getPlayerId(), cfg.getPriceItemItemList(), buyNum, Action.EXCHANGE_DECORATE_LEVEL_OPEN_COST, true);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,Status.Error.EXCHANGE_DECORATE_COST_ERROR_VALUE);
			return;
		}
		
		// 发奖
		exchangeDecorateActivity.getDataGeter().takeReward(entity.getPlayerId(), cfg.getGiveItemItemList(), buyNum,
				Action.EXCHANGE_DECORATE_LEVEL_OPEN_REWARD, true, RewardOrginType.EXCHANGE_DECORATE_LEVEL_REWARD);
		
		buyInfo.setState(buyInfo.getState()+buyNum);
		entity.notifyUpdate();
		
		exchangeDecorateActivity.exchangeLimitTimeList(entity.getPlayerId());
	}


}
