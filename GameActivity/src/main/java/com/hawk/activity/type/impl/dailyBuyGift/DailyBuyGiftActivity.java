package com.hawk.activity.type.impl.dailyBuyGift;

import java.util.List;

import org.hawk.db.HawkDBEntity;
import org.hawk.db.HawkDBManager;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.config.ActivityCfg;
import com.hawk.activity.entity.ActivityEntity;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.dailyBuyGift.entity.DailyBuyGiftEntity;

public class DailyBuyGiftActivity extends ActivityBase {

	
    public DailyBuyGiftActivity(int activityId, ActivityEntity activityEntity) {
        super(activityId, activityEntity);
    }

    @Override
    public ActivityType getActivityType() {
        return ActivityType.DAILY_BUY_GIFT;
    }
    

    @Override
    public ActivityBase newInstance(ActivityCfg config, ActivityEntity activityEntity) {
    	DailyBuyGiftActivity activity = new DailyBuyGiftActivity(config.getActivityId(), activityEntity);
        return activity;
    }

    @Override
    protected HawkDBEntity loadFromDB(String playerId, int termId) {
        List<DailyBuyGiftEntity> queryList = HawkDBManager.getInstance()
                .query("from DailyBuyGiftEntity where playerId = ? and termId = ? and invalid = 0", playerId, termId);
        if (queryList != null && queryList.size() > 0) {
        	DailyBuyGiftEntity entity = queryList.get(0);
            return entity;
        }
        return null;
    }

    @Override
    protected HawkDBEntity createDataEntity(String playerId, int termId) {
    	DailyBuyGiftEntity entity = new DailyBuyGiftEntity(playerId, termId);
        return entity;
    }


    
}
