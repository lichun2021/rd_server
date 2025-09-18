//package com.hawk.activity.type.impl.achieve.parser;
//
//import com.hawk.activity.event.impl.BuildingLevelUpEvent;
//import com.hawk.activity.type.impl.achieve.AchieveParser;
//import com.hawk.activity.type.impl.achieve.AchieveType;
//import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
//import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
//
///**
// * 提升资源产量*次 配置格式：资源ID(0为任意)_次数
// * @author PhilChen
// *
// */
//public class BuildLevelUpCountParser2 extends AchieveParser<BuildingLevelUpEvent> {
//
//	@Override
//	public AchieveType geAchieveType() {
//		return AchieveType.UPGRADE_RESOURCE_PRODUCTOR;
//	}
//	
//	@Override
//	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
//		return true;
//	}
//
//	@Override
//	protected boolean updateAchieve(AchieveItem achieveItem, AchieveConfig achieveConfig, BuildingLevelUpEvent event) {
//		int configBuildType = achieveConfig.getConditionValue(0);
//		if (configBuildType > 0 && event.getBuildType() != configBuildType) {
//			return false;
//		}
//		int oldCount = achieveItem.getValue(0);
//		achieveItem.setValue(0, oldCount + 1);
//		return true;
//	}
//
//}
