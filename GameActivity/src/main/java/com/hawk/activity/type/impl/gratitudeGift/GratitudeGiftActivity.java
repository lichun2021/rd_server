package com.hawk.activity.type.impl.gratitudeGift;

import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;
import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;
import org.hawk.net.protocol.HawkProtocol;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.helper.PlayerPushHelper;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.gratitudeGift.entity.GratitudeGiftEntity;
import com.hawk.game.protocol.Activity.PBGratitudeGiftSync;
import com.hawk.game.protocol.HP;

public class GratitudeGiftActivity extends ActivityBase {
	/** 只有在此时间之前注册的玩家可领取 2018-07-27-21:00:00 */
	public final long LAST_CREATE = 1532696352521L;

	public GratitudeGiftActivity(int activityId, ActivityEntity activityEntity) {
		super(activityId, activityEntity);
	}

	@Override
	public ActivityType getActivityType() {
		return ActivityType.GRATITUDE_GIFT;
	}

	@Override
	public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
		return new GratitudeGiftActivity(config.getActivityId(), activityEntity);
	}

	@Override
	public void syncActivityDataInfo(String playerId) {
		Optional<GratitudeGiftEntity> opEntity = getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return;
		}
		PBGratitudeGiftSync.Builder resp = PBGratitudeGiftSync.newBuilder();
		GratitudeGiftEntity gratitudeGiftEntity = opEntity.get();
		resp.setHasReward(rewardsState(gratitudeGiftEntity));

		PlayerPushHelper.getInstance().pushToPlayer(playerId, HawkProtocol.valueOf(HP.code.GRATITUDE_GIFT_SYNC, resp));
	}

	public int rewardsState(GratitudeGiftEntity gratitudeGiftEntity) {
		if (getDataGeter().getPlayerCreateTime(gratitudeGiftEntity.getPlayerId()) > LAST_CREATE) {// 注册时间在之后, 不能领
			return 2;
		}
		int hasReward = StringUtils.isEmpty(gratitudeGiftEntity.getRewardsGet()) ? 1 : 0;
		return hasReward;
	}

	@Override
	public void onPlayerLogin(String playerId) {
	}

	@Override
	protected HawkDBEntity loadFromDB(String playerId, int termId) {
		List<GratitudeGiftEntity> queryList = HawkDBManager.getInstance()
				.query("from GratitudeGiftEntity where playerId = ?  and termId = ?  and invalid = 0", playerId, termId);
		if (queryList != null && queryList.size() > 0) {
			GratitudeGiftEntity entity = queryList.get(0);
			return entity;
		}
		return null;
	}

	@Override
	protected HawkDBEntity createDataEntity(String playerId, int termId) {
		GratitudeGiftEntity entity = new GratitudeGiftEntity();
		entity.setPlayerId(playerId);
		entity.setTermId(termId);
		return entity;
	}

	@Override
	public void onPlayerMigrate(String playerId) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onImmigrateInPlayer(String playerId) {
		// TODO Auto-generated method stub

	}

}
