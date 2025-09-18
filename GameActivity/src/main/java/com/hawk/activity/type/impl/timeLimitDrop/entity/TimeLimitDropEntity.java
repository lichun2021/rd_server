package com.hawk.activity.type.impl.timeLimitDrop.entity; 

import org.hawk.db.HawkDBEntity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.persistence.Column;
import org.hawk.annotation.IndexProp;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.GeneratedValue;
import org.hibernate.annotations.GenericGenerator;

import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
*	
*	auto generate do not modified
*/
@Entity
@Table(name="activity_time_limit_drop")
public class TimeLimitDropEntity extends HawkDBEntity implements IActivityDataEntity{

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;

	/***/
    @IndexProp(id = 2)
	@Column(name="playerId", nullable = false, length=64)
	private String playerId;

	/***/
    @IndexProp(id = 3)
	@Column(name="termId", nullable = false, length=10)
	private int termId;

	/***/
    @IndexProp(id = 4)
	@Column(name="collectRemainTime", nullable = false, length=10)
	private int collectRemainTime;

	/***/
    @IndexProp(id = 5)
	@Column(name="wolrdCollectRemainTime", nullable = false, length=10)
	private int wolrdCollectRemainTime;

	/***/
    @IndexProp(id = 6)
	@Column(name="beatYuriTimes", nullable = false, length=10)
	private int beatYuriTimes;

	/***/
    @IndexProp(id = 7)
	@Column(name="wishTimes", nullable = false, length=10)
	private int wishTimes;

	/***/
    @IndexProp(id = 8)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 9)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 10)
	@Column(name="invalid", nullable = false, length=3)
	private boolean invalid;
	
    @IndexProp(id = 11)
	@Column(name="achieveItems", nullable = false, length=1024)
	private String achieveItems;
	
    @IndexProp(id = 12)
	@Column(name="wolrdCollectTimes", nullable = false)
	private int wolrdCollectTimes;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
		
	public String getId() {
		return this.id; 
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPlayerId() {
		return this.playerId; 
	}

	public void setPlayerId(String playerId) {
		this.playerId = playerId;
	}

	public int getTermId() {
		return this.termId; 
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getCollectRemainTime() {
		return this.collectRemainTime; 
	}

	public void setCollectRemainTime(int collectRemainTime) {
		this.collectRemainTime = collectRemainTime;
	}

	public int getWolrdCollectRemainTime() {
		return this.wolrdCollectRemainTime; 
	}

	public void setWolrdCollectRemainTime(int wolrdCollectRemainTime) {
		this.wolrdCollectRemainTime = wolrdCollectRemainTime;
	}

	public int getBeatYuriTimes() {
		return this.beatYuriTimes; 
	}

	public void setBeatYuriTimes(int beatYuriTimes) {
		this.beatYuriTimes = beatYuriTimes;
	}

	public int getWishTimes() {
		return this.wishTimes; 
	}

	public void setWishTimes(int wishTimes) {
		this.wishTimes = wishTimes;
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
	
	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
		
	}

	@Override
	public boolean isInvalid() {
		return false;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;
	}
	
	@Override
	public void beforeWrite() {
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}
	
	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;		
	}

	public int getWolrdCollectTimes() {
		return wolrdCollectTimes;
	}

	public void setWolrdCollectTimes(int wolrdCollectTimes) {
		this.wolrdCollectTimes = wolrdCollectTimes;
	}
}
