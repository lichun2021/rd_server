package com.hawk.activity.entity;

import com.hawk.activity.type.ActivityState;

public interface IActivityEntity {
	public String getId();

	public int getActivityId();

	public int getTermId();

	public int getState();
	
	public long getNewlyTime();

	public ActivityState getActivityState();
}
