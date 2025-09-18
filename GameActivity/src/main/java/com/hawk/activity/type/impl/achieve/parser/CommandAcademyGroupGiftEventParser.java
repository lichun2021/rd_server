package com.hawk.activity.type.impl.achieve.parser;

import java.util.Optional;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.CommandAcademyGroupGiftEvent;
import com.hawk.activity.type.ActivityType;
import com.hawk.activity.type.impl.achieve.AchieveParser;
import com.hawk.activity.type.impl.achieve.AchieveType;
import com.hawk.activity.type.impl.achieve.cfg.AchieveConfig;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.activity.type.impl.commandAcademy.CommandAcademyActivity;
import com.hawk.activity.type.impl.commandAcademy.cfg.CommandAcademyBoxAchieveCfg;
import com.hawk.activity.type.impl.commandAcademy.entity.CommandAcademyEntity;

public class CommandAcademyGroupGiftEventParser extends AchieveParser<CommandAcademyGroupGiftEvent> {

	@Override
	public AchieveType geAchieveType() {
		return AchieveType.COMMAND_ACADEMY_GROUP_BUY;
	}
	
	@Override
	public boolean initDataOnOpen(String playerId, AchieveItem achieveItem, AchieveConfig achieveConfig) {
		Optional<CommandAcademyActivity>  opt = ActivityManager.getInstance().
				getGameActivityByType(ActivityType.COMMAND_ACADEMY_ACTIVITY.intValue());
		if (!opt.isPresent()) {
			return false;
		} 
		CommandAcademyActivity activity = opt.get();
		if(activity == null){
			return false;
		}
		AchieveConfig cfg =activity.getAchieveCfg(achieveItem.getAchieveId());
		if(cfg == null){
			return false;
		}
		if(!(cfg instanceof CommandAcademyBoxAchieveCfg)){
			return false;
		}
		Optional<CommandAcademyEntity> opEntity = activity.getPlayerDataEntity(playerId);
		if (!opEntity.isPresent()) {
			return false;
		}
		CommandAcademyEntity entity = opEntity.get();
		CommandAcademyBoxAchieveCfg boxCfg = (CommandAcademyBoxAchieveCfg) cfg;
		int buyCount = activity.getBuyCount(boxCfg.getStageId());
		boolean isBuy = entity.isBuyGift();
		CommandAcademyGroupGiftEvent event = new CommandAcademyGroupGiftEvent(playerId, buyCount, isBuy);
		this.updateAchieve(achieveItem, achieveConfig, event);
		return true;
	}

	@Override
	protected boolean updateAchieve(AchieveItem achieveData, AchieveConfig achieveConfig,
			CommandAcademyGroupGiftEvent event) {
		int configValue = achieveConfig.getConditionValue(0);
		int value = event.getBuyCount();
		if (value >= configValue) {
			value = configValue;
			achieveData.setValue(0, value);
		}
		achieveData.setValue(1,event.isBuy()?1:0);
		return true;
	}
	
	@Override
	public boolean isFinish(AchieveItem achieveItem, AchieveConfig achieveConfig) {
		int configValue = achieveConfig.getConditionValue(0);
		int needBuy = achieveConfig.getConditionValue(1);
		int value = achieveItem.getValue(0);
		int isBuy = achieveItem.getValue(1);
		if(value >= configValue && needBuy == 0){
			return true;
		}
		if(value >= configValue && needBuy == 1 && isBuy == 1){
			return true;
		}
		return false;
	}
}
