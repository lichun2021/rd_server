package com.hawk.activity.type.impl.exchangeDecorate.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.type.impl.exchangeDecorate.ExchangeDecorateActivity;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateActivityKVCfg;
import com.hawk.activity.type.impl.exchangeDecorate.cfg.ExchangeDecorateLevelExpCfg;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ActivityExchangeDecorateEntity;
import com.hawk.activity.type.impl.exchangeDecorate.entity.ExchangeDecorateInfo;
import com.hawk.game.protocol.Activity;
import com.hawk.game.protocol.Activity.ExchangeCommonType;
import com.hawk.game.protocol.Const.ItemType;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward;
import com.hawk.game.protocol.Status;
import com.hawk.log.Action;

public class ExchangeDecorateLevelReward implements IExchangeCommon{

	static{
		new ExchangeDecorateLevelReward();
	}
	
	public ExchangeDecorateLevelReward() {
		IExchangeCommon.register(ExchangeCommonType.DECORATE_LEVEL_NOLOCK, this);
	}

	@Override
	public void exchangeCommon(HawkDBEntity baseEntity, List<Integer> params) {
		ActivityExchangeDecorateEntity entity = (ActivityExchangeDecorateEntity) baseEntity;
		
		ExchangeDecorateActivityKVCfg sysCfg = HawkConfigManager.getInstance().getKVInstance(ExchangeDecorateActivityKVCfg.class);
		if(entity.getWeekBuyExpNum()>=sysCfg.getUnlockTime()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_BUY_LIMIT_NUMBER_ERROR_VALUE);
			return;
		}
		
		int levelId = params.get(0);
		
		ExchangeDecorateInfo opInfo = null;
		for (ExchangeDecorateInfo info : entity.getLevelRewardList()) {
			if(info.getLevelId() == levelId){
				opInfo = info;
				break;
			}
		}
		if(opInfo == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_YEST_VALUE);
			return;
		}
		if(!opInfo.isInitReward()){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.DECORATE_EXCHANGE_LEVEL_LIST_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_YEST_VALUE);
			return;
		}
		
		ExchangeDecorateLevelExpCfg cfg = HawkConfigManager.getInstance().getConfigByKey(ExchangeDecorateLevelExpCfg.class, levelId);
		if(cfg == null){
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,
					Status.Error.EXCHANGE_DECORATE_LEVEL_REWARD_ID_NOT_FIND_VALUE);
			return;
		}
		
		int needExp = cfg.getLevelUpExp()-entity.getExp();
		float unlockRate = (float)(sysCfg.getUnlockGold()/10000f);
		int needGold = (int) (needExp * unlockRate);
		
		Optional<ActivityBase> opActivity = ActivityManager.getInstance().getActivity(Activity.ActivityType.EXCHANGE_DECORATE_VALUE);
		ExchangeDecorateActivity exchangeDecorateActivity = (ExchangeDecorateActivity) opActivity.get();
		
		Reward.RewardItem.Builder costBuilder = RewardHelper.toRewardItem(ItemType.PLAYER_ATTR_VALUE, sysCfg.getUnlockCostId(), needGold);
		boolean flag = exchangeDecorateActivity.getDataGeter().cost(entity.getPlayerId(), Arrays.asList(costBuilder), 1, Action.EXCHANGE_DECORATE_LEVEL_ONLOCK_COST, true);
		if (!flag) {
			PlayerPushHelper.getInstance().sendErrorAndBreak(entity.getPlayerId(), HP.code.EXCHANGE_COMMON_REQ_VALUE,Status.Error.EXCHANGE_DECORATE_COST_ERROR_VALUE);
			return;
		}
		entity.setWeekBuyExpNum(entity.getWeekBuyExpNum()+1);
		//修改可领取状态
//		info.setStateReward();
		exchangeDecorateActivity.addExp(entity,needExp);
		//刷新列表
		exchangeDecorateActivity.syncActivityDataInfo(entity.getPlayerId());
	}


}
