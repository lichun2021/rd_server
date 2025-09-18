package com.hawk.activity.type.impl.playerComeBack.timeController;

import java.util.Optional;
import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.playerComeBack.ComeBackVersionActivity;
import com.hawk.activity.type.impl.playerComeBack.cfg.version.PlayerComeBackVersionTimeCfg;
import com.hawk.activity.type.impl.playerComeBack.entity.PlayerComeBackEntity;
import com.hawk.game.protocol.Activity;

public class ComeBackPlayerVersionTimeController extends PlayerComeBackTimeController {

	@Override
	public long getShowTimeByTermId(int termId, String playerId) {
		Optional<HawkDBEntity> optional = getDBEntity(playerId);
		if (!optional.isPresent()){
			return 0;
		}
		PlayerComeBackEntity entity = (PlayerComeBackEntity)optional.get();
		return entity.getStartTime();
	}

	@Override
	public long getStartTimeByTermId(int termId, String playerId) {
		return getShowTimeByTermId(termId, playerId);
	}

	@Override
	public long getEndTimeByTermId(int termId, String playerId) {
		return super.getEndTimeByTermId(termId, playerId);
	}

	@Override
	public long getHiddenTimeByTermId(int termId, String playerId) {
		return super.getHiddenTimeByTermId(termId, playerId);
	}
	
	@Override
	public Class<? extends HawkConfigBase> getTimeCfgClass() {
		return PlayerComeBackVersionTimeCfg.class;
	}
	
	@Override
	protected Optional<HawkDBEntity> getDBEntity(String playerId) {
		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.COME_BACK_PLAYER_VERSION_VALUE);
		if(!optional.isPresent()){
			return Optional.empty();
		}
		ComeBackVersionActivity activity = (ComeBackVersionActivity)optional.get();
		return activity.getPlayerDataEntity(playerId);
	}
}
