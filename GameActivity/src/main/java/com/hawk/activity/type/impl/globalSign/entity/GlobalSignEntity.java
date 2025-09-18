package com.hawk.activity.type.impl.globalSign.entity;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_global_sign")
public class GlobalSignEntity  extends HawkDBEntity implements IActivityDataEntity{

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
	@Column(name = "bullectChatControl", nullable = false)
	private int bullectChatControl;
	
    @IndexProp(id = 5)
	@Column(name = "bulletChatTime", nullable = false)
	private long bulletChatTime;
	/** 签到时间*/
    @IndexProp(id = 6)
	@Column(name = "signTime", nullable = false)
	private long signTime;
	
	/** 活动成就项数据 */
    @IndexProp(id = 7)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;

    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<AchieveItem> itemList = new CopyOnWriteArrayList<AchieveItem>();

	public GlobalSignEntity() {
	}
	
	public GlobalSignEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
		this.achieveItems = "";
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	

	public int getBullectChatControl() {
		return bullectChatControl;
	}

	public void setBullectChatControl(int bullectChatControl) {
		this.bullectChatControl = bullectChatControl;
	}

	public long getBulletChatTime() {
		return bulletChatTime;
	}

	public void setBulletChatTime(long bulletChatTime) {
		this.bulletChatTime = bulletChatTime;
	}

	public long getSignTime() {
		return signTime;
	}

	public void setSignTime(long signTime) {
		this.signTime = signTime;
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
	
	public String getAchieveItems() {
		return achieveItems;
	}
	
	public void setAchieveItems(String achieveItems) {
		this.achieveItems = achieveItems;
	}
	
	
	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public List<AchieveItem> getItemList() {
		return itemList;
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
	
	@Override
	public String getPrimaryKey() {
		return this.id;
	}

	@Override
	public void setPrimaryKey(String primaryKey) {
		this.id = primaryKey;
	}

}
