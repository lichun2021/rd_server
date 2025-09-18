package com.hawk.activity.type.impl.returnUpgrade;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.timeController.impl.PlayerComeBackTimeController;
import com.hawk.activity.type.impl.returnUpgrade.cfg.ReturnUpgradeTimeCfg;
import com.hawk.activity.type.impl.returnUpgrade.entity.ReturnUpgradeEntity;
import com.hawk.game.protocol.Activity;
import org.hawk.config.HawkConfigBase;
import org.hawk.db.HawkDBEntity;

import java.util.Optional;

public class ReturnUpgradeTimeController extends PlayerComeBackTimeController {
    @Override
    public long getShowTimeByTermId(int termId, String playerId) {
        Optional<HawkDBEntity> optional = getDBEntity(playerId);
        if (!optional.isPresent()){
            return 0;
        }
        ReturnUpgradeEntity entity = (ReturnUpgradeEntity)optional.get();
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
        ReturnUpgradeEntity entity = (ReturnUpgradeEntity)optional.get();
        return entity.getOverTime();
    }

    @Override
    public long getHiddenTimeByTermId(int termId, String playerId) {
        return getEndTimeByTermId(termId, playerId);
    }

    @Override
    public Class<? extends HawkConfigBase> getTimeCfgClass() {
        return ReturnUpgradeTimeCfg.class;
    }

    @Override
    protected Optional<HawkDBEntity> getDBEntity(String playerId) {
        Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(Activity.ActivityType.RETURN_UPGRADE_346_VALUE);
        if(!optional.isPresent()){
            return optional.empty();
        }
        ReturnUpgradeActivity activity = (ReturnUpgradeActivity) optional.get();
        return activity.getPlayerDataEntity(playerId);
    }
}
