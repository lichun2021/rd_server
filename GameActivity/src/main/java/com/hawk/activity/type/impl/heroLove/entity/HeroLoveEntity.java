package com.hawk.activity.type.impl.heroLove.entity; 

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

import com.hawk.activity.AchieveActivityEntity;
import com.hawk.activity.type.IActivityDataEntity;
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

/**
*	a
*	auto generate do not modified
*/
@Entity
@Table(name="activity_hero_love")
public class HeroLoveEntity extends AchieveActivityEntity implements IActivityDataEntity{

	/***/
	@Id
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
	@GeneratedValue(generator = "uuid")
	private String id;
	
    @IndexProp(id = 2)
	@Column(name="termId", nullable = false, length=10)
	private int termId;
	
	/***/
    @IndexProp(id = 3)
	@Column(name="playerId", nullable = false, length=32)
	private String playerId;

	/***/
    @IndexProp(id = 4)
	@Column(name="score", nullable = false, length=10)
	private int score;

	/***/
    @IndexProp(id = 5)
	@Column(name="achieveItems", nullable = false, length=1024)
	private String achieveItems;
	/**
	 * 记录上一次的登录时间，主要是用于初始化.
	 */
    @IndexProp(id = 6)
	@Column(name="lastLoginTime", nullable = false)
	private long lastLoginTime;
	
	/***/
    @IndexProp(id = 7)
	@Column(name="createTime", nullable = false, length=19)
	private long createTime;

	/***/
    @IndexProp(id = 8)
	@Column(name="updateTime", nullable = false, length=19)
	private long updateTime;

	/***/
    @IndexProp(id = 9)
	@Column(name="invalid", nullable = false, length=0)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();
	
	public HeroLoveEntity() {
		
	}
	
	public HeroLoveEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
	}

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

	public int getScore() {
		return this.score; 
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getAchieveItems() {
		return this.achieveItems; 
	}

	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
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

	@Override
	public void afterRead() {
		this.itemList.clear();
		SerializeHelper.stringToList(AchieveItem.class, this.achieveItems, this.itemList);
	}

	@Override
	public void beforeWrite() {		
		this.achieveItems = SerializeHelper.collectionToString(this.itemList, SerializeHelper.ELEMENT_DELIMITER);
	}

	@Override
	public String getPrimaryKey() {
		return id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;		
	}

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
	}
		
	public void addItem(AchieveItem achieveItem) {
		this.itemList.add(achieveItem);
		this.notifyUpdate();
	}

	public long getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(long lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

}
