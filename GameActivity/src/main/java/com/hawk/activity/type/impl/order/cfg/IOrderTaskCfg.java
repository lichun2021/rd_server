package com.hawk.activity.type.impl.order.cfg;

import java.util.List;

import com.hawk.activity.type.impl.order.task.OrderTaskType;


public interface  IOrderTaskCfg {
	
	/**
	 * 获取成就id
	 * @return
	 */
	public int getId() ;
	
	public int getRepeatVal();
	
	public List<Integer> getConditionList();
	
	public int getConditionValue() ;
	
	public int getExp();
	
	public OrderTaskType getTaskType();
	
	

	
}
