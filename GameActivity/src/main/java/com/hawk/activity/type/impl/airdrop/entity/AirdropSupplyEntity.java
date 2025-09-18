package com.hawk.activity.type.impl.airdrop.entity;

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
@Table(name="activity_airdrop_supply")
public class AirdropSupplyEntity extends HawkDBEntity implements IActivityDataEntity{

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
	
	/** 活动成就项数据 */
    @IndexProp(id = 4)
	@Column(name = "achieveItems", nullable = false)
	private String achieveItems;
	
	/**宝箱个数*/
    @IndexProp(id = 5)
	@Column(name = "boxNum", nullable = false)
	private int boxNum;
	
	/**是否购买*/
    @IndexProp(id = 6)
	@Column(name = "isBuy", nullable = false)
	private boolean isBuy;
	
    @IndexProp(id = 7)
	@Column(name = "loginDays", nullable = false)
	private int loginDays;
	
	/**
	 * 跨天天数更新时间
	 */
    @IndexProp(id = 8)
	@Column(name = "refreshTime", nullable = false)
	private long refreshTime;
	
    @IndexProp(id = 9)
	@Column(name = "updateTime", nullable = false)
	private long updateTime;
	
    @IndexProp(id = 10)
	@Column(name = "createTime", nullable = false)
	private long createTime;
	
    @IndexProp(id = 11)
	@Column(name = "invalid", nullable = false)
	private boolean invalid;
	
	@Transient
	private List<Integer> itemsetList = new ArrayList<>();
	
	@Transient
	private List<AchieveItem> itemList;
	
	public AirdropSupplyEntity() {
		this.itemList = new ArrayList<>();
	}
	
	public AirdropSupplyEntity(String playerId) {
		this.playerId = playerId;
		this.itemList = new ArrayList<>();
	}
	
	public AirdropSupplyEntity(String playerId, int termId) {
		this.playerId = playerId;
		this.termId = termId;
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

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public int getLoginDays() {
		return loginDays;
	}

	public void setLoginDays(int loginDays) {
		this.loginDays = loginDays;
	}

	
	public long getRefreshTime() {
		return refreshTime;
	}

	public void setRefreshTime(long refreshTime) {
		this.refreshTime = refreshTime;
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
	
	public int getBoxNum() {
		return boxNum;
	}

	public void setBoxNum(int boxNum) {
		this.boxNum = boxNum;
	}

	public boolean isBuy() {
		return isBuy;
	}

	public void setBuy(boolean isBuy) {
		this.isBuy = isBuy;
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
	
	public List<Integer> getItemsetList() {
		return itemsetList;
	}

	public void setItemsetList(List<Integer> itemsetList) {
		this.itemsetList = itemsetList;
	}

	public List<AchieveItem> getItemList() {
		return itemList;
	}

	public void setItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
	}
	
	public void resetItemList(List<AchieveItem> itemList) {
		this.itemList = itemList;
		this.notifyUpdate();
	}
	
	@Override
	public boolean isInvalid() {
		return this.invalid;
	}

	@Override
	protected void setInvalid(boolean invalid) {
		this.invalid = invalid;		
	}		
}
