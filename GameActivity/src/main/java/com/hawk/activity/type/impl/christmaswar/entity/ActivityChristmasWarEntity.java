package com.hawk.activity.type.impl.christmaswar.entity; 

import org.hawk.db.HawkDBEntity;
import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;
import javax.persistence.Transient;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.serialize.string.SerializeHelper;

import java.util.ArrayList;
import java.util.List;

/**
*	
*	auto generate do not modified
*/
@Entity
@Table(name="activity_christmas_war")
public class ActivityChristmasWarEntity extends HawkDBEntity implements IActivityDataEntity{

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/***/
    @IndexProp(id = 2)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/***/
    @IndexProp(id = 3)
	@Column(name="playerId", nullable = false, length=64)
	private String playerId;

	/**Integer*/
    @IndexProp(id = 4)
	@Column(name="receivedIds", nullable = false, length=256)
	private String receivedIds;

	/***/
    @IndexProp(id = 5)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 6)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 7)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;

	/** complex type @receivedIds*/
	@Transient
	private List<Integer> receivedIdsList;
	
	public ActivityChristmasWarEntity() {
		
	}
	
	public ActivityChristmasWarEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.receivedIdsList = new ArrayList<>();
	}
	
	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public String getReceivedIds() {
		return this.receivedIds; 
	}

	public void setReceivedIds(String receivedIds) {
		this.receivedIds = receivedIds;
	}

	public long getCreateTime() {
		return this.createTime; 
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getUpdateTime() {
		return this.updateTime; 
	}

	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}

	public boolean isInvalid() {
		return this.invalid; 
	}

	public void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}

	public List<Integer> getReceivedIdsList() {
		return this.receivedIdsList; 
	}

	public void setReceivedIdsList(List<Integer> receivedIdsList) {
		this.receivedIdsList = receivedIdsList;
	}

	@Override
	public void afterRead() {		
		this.receivedIdsList = SerializeHelper.stringToList(Integer.class, receivedIds, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	@Override
	public void beforeWrite() {		
		this.receivedIds = SerializeHelper.collectionToString(receivedIdsList, SerializeHelper.ATTRIBUTE_SPLIT);
	}

	public void addReceivedIds(Integer element) {
		this.receivedIdsList.add(element);
		this.notifyUpdate();
	}

	public void addReceivedIds(List<Integer> elements) {
		this.receivedIdsList.addAll(elements);
		this.notifyUpdate();
	}

	public void removeReceivedIds(Integer element) {
		this.receivedIdsList.remove(element);
		this.notifyUpdate();
	}

	public void removeReceivedIds(List<Integer> elements) {
		this.receivedIdsList.removeAll(elements);
		this.notifyUpdate();
	}

	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String id) {
		this.id = id;	
	}
	
	public void reset() {
		this.receivedIdsList = new ArrayList<>();
		this.notifyUpdate();
	}
}
