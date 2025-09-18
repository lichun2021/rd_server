package com.hawk.activity.type.impl.backToNewFly;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.backToNewFly.cfg.BackToNewFlyTimeCfg;
import com.hawk.activity.type.impl.backToNewFly.entity.BackToNewFlyEntity;
import com.hawk.game.protocol.Activity;
import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;

import java.util.Optional;

public class BackToNewFlyTimeController extends PlayerComeBackTimeController {

    @Override
    public long getShowTimeByTermId(int termId, String playerId) {
        Optional<HawkDBEntity> optional = getDBEntity(playerId);
        if (!optional.isPresent()){
            return 0;
        }
        BackToNewFlyEntity entity = (BackToNewFlyEntity)optional.get();
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
        BackToNewFlyEntity entity = (BackToNewFlyEntity)optional.get();
        return entity.getOverTime();
    }

    @Override
    public long getHiddenTimeByTermId(int termId, String playerId) {
        return getEndTimeByTermId(termId, playerId);
    }


    @Override
    protected Optional<HawkDBEntity> getDBEntity(String playerId) {
        Optional<ActivityBase> optional = ActivityManager.getInstance().
                getActivity(Activity.ActivityType.BACK_TO_NEW_FLY_VALUE);
        if(!optional.isPresent()){
            return Optional.empty();
        }
        BackToNewFlyActivity activity = (BackToNewFlyActivity)optional.get();
        return activity.getPlayerDataEntity(playerId);
    }


    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return BackToNewFlyTimeCfg.class;
    }
}
