package com.hawk.activity.type.impl.overlordBlessing.entity;

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
import com.hawk.activity.type.impl.achieve.entity.AchieveItem;
import com.hawk.serialize.string.SerializeHelper;

@Entity
@Table(name = "activity_overlord_blessing")
public class OverlordBlessingEntity extends HawkDBEntity implements IActivityDataEntity{
	@Id
	@GenericGenerator(name = "uuid", strategy = "org.hawk.uuid.HawkUUIDGenerator")
    @GeneratedValue(generator = "uuid")
    @IndexProp(id = 1)
	@Column(name = "id", unique = true, nullable = false)
	private String id;
	
    @IndexProp(id = 2)
	@Column(name = "playerId", nullable = false)
	private String playerId;
	
    @IndexProp(id = 3)
	@Column(name = "termId", nullable = false)
	private int termId;
	
	/**是否分享过 */
    @IndexProp(id = 4)
	@Column(name = "hasShare", nullable = false)
	private boolean hasShare;
	
	/**是否领取过分享奖励 */
    @IndexProp(id = 5)
	@Column(name = "receiveShare", nullable = false)
	private boolean receiveShare;
	
	/**是否膜拜过 */
    @IndexProp(id = 6)
	@Column(name = "hasBless", nullable = false)
	private boolean hasBless;
	
	/**是否领取过膜拜奖励 */
    @IndexProp(id = 7)
	@Column(name = "receiveBless", nullable = false)
	private boolean receiveBless;
	
    @IndexProp(id = 8)
	@Column(name = "createTime", nullable = false)
	private long createTime;

    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;

    @IndexProp(id = 10)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
    
    /** 活动成就项数据 */
    @IndexProp(id = 11)
    @Column(name = "achieveItems", nullable = false)
    private String achieveItems;
    
    /** 活动成就 */
    @Transient
    private List<AchieveItem> itemList = new ArrayList<AchieveItem>();
	

	public OverlordBlessingEntity() {
	}
	
	public OverlordBlessingEntity(String playerId) {
		this.playerId = playerId;
	}
	
	public OverlordBlessingEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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

	
	public boolean isHasShare() {
		return hasShare;
	}

	public void setHasShare(boolean hasShare) {
		this.hasShare = hasShare;
	}

	public boolean isReceiveShare() {
		return receiveShare;
	}

	public void setReceiveShare(boolean receiveShare) {
		this.receiveShare = receiveShare;
	}

	public boolean isHasBless() {
		return hasBless;
	}

	public void setHasBless(boolean hasBless) {
		this.hasBless = hasBless;
	}

	public boolean isReceiveBless() {
		return receiveBless;
	}

	public void setReceiveBless(boolean receiveBless) {
		this.receiveBless = receiveBless;
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
	
	public List<AchieveItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<AchieveItem> itemList) {
        this.itemList = itemList;
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
