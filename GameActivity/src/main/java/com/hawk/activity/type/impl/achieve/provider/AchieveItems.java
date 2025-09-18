package com.hawk.activity.type.impl.achieve.provider;

import java.util.List;

import org.hawk.db.HawkDBEntity;

import com.hawk.activity.type.impl.achieve.entity.AchieveItem;

/**
 * 成就数据集合
 * @author PhilChen
 *
 */
public class AchieveItems {

	/**
	 * 成就项列表
	 */
	private List<AchieveItem> items;
	/**
	 * 成就数据所属的DB实体对象（用于成就数据处理后的数据更新）
	 */
	private HawkDBEntity entity;
	/**
	 * 是否需要记录打点日志
	 */
	private boolean needLog;
	/**
	 * 活动ID
	 */
	private int activityId;
	/**
	 * 活动期数
	 */
	private int termId;

	public AchieveItems() {
	}

	public AchieveItems(List<AchieveItem> items, HawkDBEntity entity) {
		this.items = items;
		this.entity = entity;
		this.needLog = false;
	}
	
	public AchieveItems(List<AchieveItem> items, HawkDBEntity entity, boolean needLog, int activityId, int termId) {
		this.items = items;
		this.entity = entity;
		this.needLog = needLog;
		this.activityId = activityId;
		this.termId = termId;
	}

	public List<AchieveItem> getItems() {
		return items;
	}

	public void setItems(List<AchieveItem> items) {
		this.items = items;
	}

	public HawkDBEntity getEntity() {
		return entity;
	}

	public void setEntity(HawkDBEntity entity) {
		this.entity = entity;
	}

	public boolean isNeedLog() {
		return needLog;
	}

	public void setNeedLog(boolean needLog) {
		this.needLog = needLog;
	}

	public int getActivityId() {
		return activityId;
	}

	public void setActivityId(int activityId) {
		this.activityId = activityId;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

}
