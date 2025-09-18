package com.hawk.game.service.pushgift.impl;

import java.util.List;

import com.hawk.game.config.PushGiftLevelCfg;
import com.hawk.game.player.PlayerData;
import com.hawk.game.service.pushgift.AbstractPushGiftCondition;
import com.hawk.game.service.pushgift.PushGiftConditionEnum;

/**
 * 礼包类型 = 3200
 * 礼包条件：用户近X时间内充值金额达到Y元，返回主界面，且VIP等级在vipMin_vipMax之间 时触发
 * 配置格式：X_Y_vipMin_vipMax
 * @author Golden
 *
 */
public class PushGiftCondition3200 extends AbstractPushGiftCondition {

	@Override
	public boolean isReach(PlayerData playerData, PushGiftLevelCfg cfg, List<Integer> param) {
		return false;
	}
	
	@Override
	public boolean isReach(List<Integer> cfgParam, List<Integer> param) {
		return false;
	}

	@Override
	public int getConditionType() {
		return PushGiftConditionEnum.PUSH_GIFT_3200.getType();
	}
}
