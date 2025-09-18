package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;

import com.hawk.activity.ActivityBase;
import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.BuildingLevelUpSpreadEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.spread.SpreadActivity;
import com.hawk.activity.type.impl.spread.entity.SpreadEntity;
import com.hawk.game.protocol.Const;

/**
 * 
 * @author RickMei 推广员活动绑定玩家主堡等级提升
 */
public class SpreadNewPlayerCityLevelupParser extends AchieveParser<BuildingLevelUpSpreadEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.SPREAD_N_CITY_LEVEL;
	}

	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig, BuildingLevelUpSpreadEvent event) {
		if(Const.BuildingType.CONSTRUCTION_FACTORY_VALUE != event.getBuildType()){
			return false;
		}
		Optional<ActivityBase> opt =  ActivityManager.getInstance().getActivity( ActivityType.SPREAD_ACTIVITY.intValue() );
		if(!opt.isPresent()){
			return false;
		}
		
		Optional<SpreadEntity> opEntity =  ((SpreadActivity)opt.get()).getPlayerDataEntity(event.getPlayerId());
		if (!opEntity.isPresent()) {
			return false;
		}
		
		SpreadEntity entity = opEntity.get();
		if(entity.getIsBindCode()){
			//如果跨服期间成就不完成,待回本服登录的时候再完成成就
			if(opt.get().getDataGeter().isCrossPlayer(event.getPlayerId())){
				return false;
			}
			int configValue = achieveConfig.getConditionValue(0);
			int value = event.getLevel();
			if (value > configValue) {
				value = configValue;
			}
			achieveData.setValue(0, value);
			return true;		
		}
		return false;
	}
}
