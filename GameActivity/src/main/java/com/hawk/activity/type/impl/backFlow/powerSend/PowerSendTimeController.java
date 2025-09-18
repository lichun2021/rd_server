package com.hawk.activity.type.impl.backFlow.powerSend;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.backFlow.powerSend.cfg.PowerSendActivityTimeCfg;
import com.hawk.activity.type.impl.backFlow.powerSend.entity.PowerSendEntity;
import com.hawk.game.protocol.Activity;

public class PowerSendTimeController extends PlayerComeBackTimeController {

	
	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		PowerSendEntity entity = (PowerSendEntity)optional.get();
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
		PowerSendEntity entity = (PowerSendEntity)optional.get();
		return entity.getOverTime();
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId, playerId);
	}
	

	@Override
	protected Optional<HawkDBEntity> getDBEntity(String playerId) {
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.POWER_SEND_VALUE);
		if(!optional.isPresent()){
			return Optional.empty();
		}
		PowerSendActivity activity = (PowerSendActivity)optional.get();
		return activity.getPlayerDataEntity(playerId);
	}


	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PowerSendActivityTimeCfg.class;
	}
	
	
}
