package com.hawk.game.service.pushgift.impl;

import java.util.List;
import com.hawk.game.cfgElement.ArmourStarExplores;
import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.entity.CommanderEntity;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 星能解锁礼包
 * 礼包条件：解锁星能探索功能 时触发
 * 配置格式：无参数
 * @author lating
 *
 */
public class PushGiftCondition3800 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		CommanderEntity entity = playerData.getCommanderEntity();
		ArmourStarExplores starExplores = entity.getStarExplores();
		return starExplores.getIsActive() != 0;
	}
	
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return true;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.PUSH_GIFT_3800.getType();
	}
}
