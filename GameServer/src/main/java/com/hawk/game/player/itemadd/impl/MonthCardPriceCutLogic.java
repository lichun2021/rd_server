package com.hawk.game.player.itemadd.impl;

import org.hawk.os.HawkTime;

import com.hawk.activity.ActivityManager;
import com.hawk.activity.event.impl.MonthCardPriceCutItemAddEvent;
import com.hawk.game.config.ConstProperty;
import com.hawk.game.entity.CustomDataEntity;
import com.hawk.game.player.Player;
import com.hawk.game.player.itemadd.ItemAddLogic;
import com.hawk.game.util.GsConst;
import com.hawk.log.Action;

public class MonthCardPriceCutLogic implements ItemAddLogic {
	
	@Override
	public void addLogic(Player player, int itemId, int addCount, Action action) {
		// 特权礼包半价券道具
		if (itemId != ConstProperty.getInstance().getGoldPrivilegeDiscountItem()) {
			return;
		}
		// 防止跨服期间生成道具，倒计时设定需要在此时进行
		setMonthCardPriceCutNotifyTime(player);
		ActivityManager.getInstance().postEvent(new MonthCardPriceCutItemAddEvent(player.getId()));
	}
	
	/**
	 * 特权礼包半价券提醒时间设置
	 */
	private void setMonthCardPriceCutNotifyTime(Player player) {
		CustomDataEntity customData = player.getData().getCustomDataEntity(GsConst.GOLD_PRIVILEGE_NOTITY_VALIDTIME);
		if (customData == null) {
			int endTime = HawkTime.getSeconds() + ConstProperty.getInstance().getGoldPrivilegeButtonTime();
			player.getData().createCustomDataEntity(GsConst.GOLD_PRIVILEGE_NOTITY_VALIDTIME, endTime, "");
		}
	}
}
