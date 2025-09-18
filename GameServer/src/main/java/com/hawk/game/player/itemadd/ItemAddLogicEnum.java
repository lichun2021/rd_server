package com.hawk.game.player.itemadd;

import com.hawk.game.player.itemadd.impl.*;

/**
 * 道具添加额外处理逻辑枚举
 * 
 * @author lating
 *
 */
public enum ItemAddLogicEnum {
	/**
	 * 触发任务累计：关联const.xml表的recordItems字段
	 */
	RECORD_MISSION(new RecordMissionLogic()),
	
	/**
	 * 触发月卡降价：关联const.xml表的goldPrivilegeDiscountItem字段
	 */
	MONTHCARD_PRICE_CUT(new MonthCardPriceCutLogic()),
	
	/**
	 * 关联item.xml表的protectionPeriod字段
	 */
	SEND_TIME_LIMIT(new SendTimeLimitLogic()),
	
	/**
	 * 关联荣耀同享活动 ShareGloryActivity
	 */
	SHARE_GLORY_DONATE(new ShareGloryDonateLogic()),
	/**
	 * 家园
	 */
	HOME_LAND_DONATE(new HomeLandBuildLogic()),
	;
	
	private ItemAddLogic logicObj;
	
	ItemAddLogicEnum(ItemAddLogic logicObj) {
		this.logicObj = logicObj;
	}
	
	public ItemAddLogic getLogicObj() {
		return logicObj;
	}
	
}
