package com.hawk.activity.type;

public interface IActivityDataEntity {
	/**
	 * 获取活动期数
	 * @return
	 */
	default int getTermId(){
		return 0;
	}
	
}
