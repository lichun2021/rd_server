package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;
import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.DressActiveEvent;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.dressCollectionTwo.DressCollectionTwoActivity;
import com.hawk.activity.type.impl.dressCollectionTwo.cfg.DressCollectionTwoAchieveCfg;
import com.hawk.activity.type.impl.dresscollection.DressCollectionActivity;
import com.hawk.activity.type.impl.dresscollection.cfg.DressCollectionAchieveCfg;
import com.hawk.game.protocol.Activity.ActivityType;

public class DressActiveParse extends AchieveParser<DressActiveEvent> {
    @Override
    public AchieveType geAchieveType() {
        return AchieveType.DRESS_COLLECT;
    }

    @Override
    public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
        return false;
    }

    @Override
    protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, DressActiveEvent event) {
    	if(achieveConfig instanceof DressCollectionAchieveCfg){
    		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(ActivityType.DRESS_COLLECTION_VALUE);
        	DressCollectionActivity activity = (DressCollectionActivity) optional.get();
        	int dressId = event.getDressId();
        	if (activity.dressCollectCheck(event) && achieveData.getDataList().indexOf(dressId) <= 0) {
        		achieveData.setValue(0, achieveData.getValue(0) + 1);
        		achieveData.getDataList().add(dressId);
        		return true;
        	}
    	}
    	
    	//复制了DressCollectionActivity活动，这里不单独处理了
    	if(achieveConfig instanceof DressCollectionTwoAchieveCfg){
    		Optional<ActivityBase> optional = ActivityManager.getInstance().getActivity(ActivityType.DRESS_COLLECTION_TWO_VALUE);
    		DressCollectionTwoActivity activity = (DressCollectionTwoActivity) optional.get();
        	int dressId = event.getDressId();
        	if (activity.dressCollectCheck(event) && achieveData.getDataList().indexOf(dressId) <= 0) {
        		achieveData.setValue(0, achieveData.getValue(0) + 1);
        		achieveData.getDataList().add(dressId);
        		return true;
        	}
    	}
    	
        return false;
    }
}
