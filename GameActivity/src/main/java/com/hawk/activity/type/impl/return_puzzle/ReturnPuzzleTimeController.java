package com.hawk.activity.type.impl.return_puzzle;

import java.util.Optional;

import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.return_puzzle.cfg.ReturnPuzzleTimeCfg;
import com.hawk.activity.type.impl.return_puzzle.entity.ReturnPuzzleEntity;
import com.hawk.game.protocol.Activity;

public class ReturnPuzzleTimeController extends PlayerComeBackTimeController {

	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		ReturnPuzzleEntity entity = (ReturnPuzzleEntity)optional.get();
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
		ReturnPuzzleEntity entity = (ReturnPuzzleEntity)optional.get();
		return entity.getOverTime();
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return getEndTimeByTermId(termId, playerId);
	}
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return ReturnPuzzleTimeCfg.class;
	}

	@Override
	protected Optional<HawkDBEntity> getDBEntity(String playerId) {
		Optional<ActivityBase> optional = ActivityManager.getInstance().
				getActivity(Activity.ActivityType.RETURN_PUZZLE_VALUE);
		if(!optional.isPresent()){
			return Optional.empty();
		}
		ReturnPuzzleActivity activity = (ReturnPuzzleActivity)optional.get();
		return activity.getPlayerDataEntity(playerId);
	}
}
