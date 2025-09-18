package com.hawk.activity.timeController.impl;

import java.util.Optional;

import org.hawk.db.HawkDBEntity;

import com.hawk.activity.timeController.ITimeController;

/***
 * 老玩家回归活动时间控制器
 * @author yang.rao
 *
 */
public abstract class PlayerComeBackTimeController extends ITimeController {

	@Override
	public long getServerDelay() {
		return 0;
	}
	
	/***
	 * 获得对应时间控制的entity
	 * @param playerId
	 * @return
	 */
	protected Optional<HawkDBEntity> getDBEntity(String playerId){
		return null;
	}
}
