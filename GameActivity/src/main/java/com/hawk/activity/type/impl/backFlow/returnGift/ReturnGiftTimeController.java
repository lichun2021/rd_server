package com.hawk.activity.type.impl.backFlow.returnGift;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.backFlow.returnGift.cfg.ReturnGiftTimeCfg;
import com.hawk.activity.type.impl.backFlow.returnGift.entity.ReturnGiftEntity;
import com.hawk.game.protocol.Activity;

public class ReturnGiftTimeController extends PlayerComeBackTimeController {


	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		ReturnGiftEntity entity = (ReturnGiftEntity)optional.get();
		return entity.getStartTime();
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		return getShowTimeByTermId(termId, playerId);
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		ReturnGiftEntity entity = (ReturnGiftEntity)optional.get();
		return entity.getOverTime();
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId, playerId);
	}
	

	@Override
	protected Optional<HawkDBEntity> getDBEntity(String playerId) {
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.RETURN_GIFT_VALUE);
		if(!optional.isPresent()){
			return Optional.empty();
		}
		ReturnGiftActivity activity = (ReturnGiftActivity)optional.get();
		return activity.getPlayerDataEntity(playerId);
	}
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ReturnGiftTimeCfg.class;
	}

	
	
	
}
