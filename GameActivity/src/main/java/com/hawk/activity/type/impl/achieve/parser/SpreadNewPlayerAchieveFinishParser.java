package com.hawk.activity.type.impl.achieve.parser;


import java.util.Optional;

import org.hawk.os.HawkException;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.spread.SpreadActivity;
import com.hawk.activity.type.impl.spread.entity.SpreadEntity;
import com.hawk.game.protocol.Activity.ActivityType;

/**
 * 
 * @author RickMei 推广员活动绑定玩家主堡等级提升
 */
//public class SpreadNewPlayerAchieveFinishParser extends AchieveParser<SpreadBindPlayerAchieveFinishEvent> {
//
//	@Override
//	public AchieveType geAchieveType() {
//		return AchieveType.SPREAD_FINISH_ACHIEVE;
//	}
//
//	@Override
//	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
//		return true;
//	}
//
//	@Override
//	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, SpreadBindPlayerAchieveFinishEvent event) {
//		
//		try{
//			int achieveId = achieveConfig.getConditionValue(1);
//			int val = achieveData.getValue(0);
//			if(event.getFriendAchieveIds().stream().anyMatch(e -> e == achieveId)){
//				achieveData.setValue(0, val + 1);
//				
//				Optional<ActivityBase> opt =  ActivityManager.getInstance().getActivity( ActivityType.SPREAD_VALUE );
//				if(!opt.isPresent()){
//					return false;
//				}
//				((SpreadActivity)opt.get()).onEventOldAchieveFinish( event.getFriendPlayerId(), achieveData.getAchieveId()  );
//				
//				return true;
//			}
//		}catch(Exception e){
//			HawkException.catchException(e);
//		}
//		return false;
//	}
//}
