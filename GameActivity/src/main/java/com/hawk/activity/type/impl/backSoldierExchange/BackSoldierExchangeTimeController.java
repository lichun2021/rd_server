package com.hawk.activity.type.impl.backSoldierExchange;

import java.util.Date;
import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.config.HawkConfigManager;
import org.hawk.db.HawkDBEntity;
import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.JoinCurrentTermTimeController;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeActivityTimeCfg;
import com.hawk.activity.type.impl.backSoldierExchange.cfg.BackSoldierExchangeKVCfg;
import com.hawk.activity.type.impl.backSoldierExchange.entity.BackSoldierExchangeEntity;
import com.hawk.game.protocol.Activity;

public class BackSoldierExchangeTimeController extends JoinCurrentTermTimeController {

	
	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		BackSoldierExchangeEntity entity = (BackSoldierExchangeEntity)optional.get();
		return entity.getBackTime();
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
		BackSoldierExchangeKVCfg kvCfg = HawkConfigManager.getInstance().getKVInstance(BackSoldierExchangeKVCfg.class);
		BackSoldierExchangeEntity entity = (BackSoldierExchangeEntity)optional.get();
		return HawkTime.getAM0Date(new Date(entity.getBackTime())).getTime() +
		kvCfg.getContinueDays() * HawkTime.DAY_MILLI_SECONDS;
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId, playerId);
	}
	

	protected Optional<HawkDBEntity> getDBEntity(String playerId) {
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.BACK_SOLDIER_EXCHANGE_VALUE);
		if(!optional.isPresent()){
			return Optional.empty();
		}
		BackSoldierExchangeActivity activity = (BackSoldierExchangeActivity)optional.get();
		return activity.getPlayerDataEntity(playerId);
	}

	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return BackSoldierExchangeActivityTimeCfg.class;
	}

	@Override
	public long getServerDelay() {
		return 0;
	}
	
	
}
