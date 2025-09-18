package com.hawk.activity.type.impl.gratitudeGift;

import java.util.Optional;

import org.hawk.annotation.ProtocolHandler;
import org.hawk.config.HawkConfigManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityProtocolHandler;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.helper.RewardHelper;
import com.hawk.activity.item.ActivityReward;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.gratitudeGift.cfg.GratitudeGiftActivityCfg;
import com.hawk.activity.type.impl.gratitudeGift.entity.GratitudeGiftEntity;
import com.hawk.game.protocol.Activity.PBGratitudeGiftRewarResp;
import com.hawk.game.protocol.HP;
import com.hawk.game.protocol.Reward.RewardOrginType;
import com.hawk.log.Action;

public class GratitudeGiftActivityHandler extends ActivityProtocolHandler {

	@ProtocolHandler(code = HP.code.GRATITUDE_GIFT_GET_C_VALUE)
	public boolean onOnece(HawkProtocol protocol, String playerId) {
		GratitudeGiftActivityCfg actCfg = HawkConfigManager.getInstance().getConfigByIndex(GratitudeGiftActivityCfg.class, 0);
		GratitudeGiftActivity activity = getActivity(ActivityType.GRATITUDE_GIFT);
		Optional<GratitudeGiftEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		GratitudeGiftEntity gratitudeGiftEntity = opEntity.get();
		if (activity.rewardsState(gratitudeGiftEntity) != 1) {
			return false;
		}
		gratitudeGiftEntity.setRewardsGet(actCfg.getRewards());

		ActivityReward reward = new ActivityReward(RewardHelper.toRewardItemList(actCfg.getRewards()), Action.ACTIVITY_GRATITUDE_GIFT_GET);
		reward.setAlert(true);
		reward.setOrginType(RewardOrginType.SHOPPING_GIFT, activity.getActivityId());
		activity.postReward(playerId, reward);

		// 返回奖品
		PBGratitudeGiftRewarResp.Builder resp = PBGratitudeGiftRewarResp.newBuilder();
		resp.setRewards(actCfg.getRewards());

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.GRATITUDE_GIFT_GET_S, resp));

		activity.syncActivityDataInfo(playerId);

		return true;
	}

}
