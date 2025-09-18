package com.hawk.activity.type.impl.appointget.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hawk.annotation.IndexProp;
import org.hawk.os.HawkException;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_appointget")
public class AppointGetEntity extends AchieveActivityEntity implements IActivityDataEntity {
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

	@IndexProp(id = 4)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

	@IndexProp(id = 5)
	@Column(name = "trainCnt", nullable = false)
	private int trainCnt;
	
	@IndexProp(id = 13)
	@Column(name = "createTime", nullable = false)
	private long createTime;

	@IndexProp(id = 14)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

	@IndexProp(id = 15)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;

	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();

	public AppointGetEntity() {

	}

	public AppointGetEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
	}

	@Override
	public void beforeWrite() {
		try {
			this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public void afterRead() {
		try {
			this.itemList.clear();
			SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
		} catch (Exception e) {
			HawkException.catchException(e);
		}
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
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
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getAchieveItems() {
		return achieveItems;
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}

	public void addItem(AchieveItem item) {
		this.itemList.add(item);
		this.notifyUpdate();
	}

	@Override
	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
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

	public int getTrainCnt() {
		return trainCnt;
	}

	public void setTrainCnt(int trainCnt) {
		this.trainCnt = trainCnt;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}

}
