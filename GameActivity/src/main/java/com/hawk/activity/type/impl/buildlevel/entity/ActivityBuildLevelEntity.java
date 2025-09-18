package com.hawk.activity.type.impl.buildlevel.entity;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.db.HawkDBEntity;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

/**
 * 建筑等级活动数据存储
 * @author PhilChen
 *
 */
@Entity
@Table(name = "activity_build_level")
public class ActivityBuildLevelEntity extends HawkDBEntity implements IActivityDataEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId = null;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;

	/** 活动项数据 */
    @IndexProp(id = 4)
	@Column(name = "activityItems", nullable = false)
	private String activityItems;

    @IndexProp(id = 5)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 6)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 7)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<BuildLevelItem> itemList;

	public ActivityBuildLevelEntity() {
		this.itemList = new ArrayList<>();
	}

	public ActivityBuildLevelEntity(String playerId) {
		this.playerId = playerId;
		this.activityItems = "";
		this.itemList = new ArrayList<>();
	}
	
	public ActivityBuildLevelEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.activityItems = "";
		this.itemList = new ArrayList<>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return playerId;
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}

	@Override
	public long getCreateTime() {
		return createTime;
	}

	@Override
	protected void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	@Override
	public long getUpdateTime() {
		return updateTime;
	}

	@Override
	protected void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	@Override
	public boolean isInvalid() {
		return invalid;
	}

	@Override
	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public void setActivityItems(String activityItems) {
		this.activityItems = activityItems;
	}

	public String getActivityItems() {
		return activityItems;
	}

	public void itemsToString() {
		String itemStr = SerializeHelper.collectionToString(itemList, SerializeHelper.BETWEEN_ITEMS);
		setActivityItems(itemStr);
		notifyUpdate();
	}

	public void stringToItems() {
		String[] array = SerializeHelper.split(activityItems, SerializeHelper.BETWEEN_ITEMS);
		List<BuildLevelItem> itemList = new ArrayList<>();
		for (String data : array) {
			itemList.add(BuildLevelItem.valueOf(data));
		}
		fillItemList(itemList);
	}

	public void addItem(BuildLevelItem item) {
		this.itemList.add(item);
	}

	public void fillItemList(List<BuildLevelItem> itemList) {
		this.itemList.clear();
		this.itemList.addAll(itemList);
	}

	public List<BuildLevelItem> getItemList() {
		return itemList;
	}

	public BuildLevelItem getItem(int itemId) {
		for (BuildLevelItem item : itemList) {
			if (item.getItemId() == itemId) {
				return item;
			}
		}
		return null;
	}

	@Override
	public int getTermId() {
		return termId;
	}
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}
}
