package com.hawk.activity.type.impl.order;

import com.hawk.activity.type.impl.order.cfg.IOrderTaskCfg;
import com.hawk.activity.type.impl.order.entity.IOrderDateEntity;
import com.hawk.activity.type.impl.order.entity.OrderItem;

public interface IOrderActivity{
	
	/**
	 * 经验变化来源
	 */
	public static final int EXP_REASON_INIT = 0;
	public static final int EXP_REASON_TASK = 1;
	public static final int EXP_REASON_AUTH = 2;
	public static final int EXP_REASON_BUY = 3;
	
	
	public void addExp(IOrderDateEntity dataEntity, int addExp, int reason, int reasonId) ;
	
	default void logOrderFinishId(IOrderDateEntity dataEntity,IOrderTaskCfg cfg,
			OrderItem orderItem,int addTimes){}
}
